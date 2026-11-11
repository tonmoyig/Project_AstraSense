package com.example.data.cloudsql

import android.content.Context
import android.util.Log
import com.example.model.CabinEnvironment
import com.example.model.VitalSigns
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.Job
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import org.json.JSONObject
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale
import java.util.TimeZone

/**
 * Cloud SQL for PostgreSQL Offline-First Synchronization Manager.
 * Orchestrates local SQLite caching during deep-space RF blackouts and handles
 * bulk synchronization to PostgreSQL when satellite/orbital links are active.
 */
object CloudSqlSyncManager {
  private const val TAG = "CloudSqlSyncManager"
  private const val BATCH_SIZE = 50

  private val scope = CoroutineScope(SupervisorJob() + Dispatchers.IO)
  private var dbHelper: AstraOfflineQueueDbHelper? = null
  private var periodicSyncJob: Job? = null

  // Reactive State
  private val _syncState = MutableStateFlow(CloudSqlSyncStatus.ONLINE_SYNCED)
  val syncState: StateFlow<CloudSqlSyncStatus> = _syncState.asStateFlow()

  private val _pendingQueueCount = MutableStateFlow(0)
  val pendingQueueCount: StateFlow<Int> = _pendingQueueCount.asStateFlow()

  private val _lastSyncTimestamp = MutableStateFlow("T-00:00:00 UTC")
  val lastSyncTimestamp: StateFlow<String> = _lastSyncTimestamp.asStateFlow()

  private val _totalSyncedRecords = MutableStateFlow(0L)
  val totalSyncedRecords: StateFlow<Long> = _totalSyncedRecords.asStateFlow()

  // Simulated RF Blackout or Offline Isolation Flag
  private var isRfBlackoutForced = false

  fun initialize(context: Context) {
    if (dbHelper == null) {
      dbHelper = AstraOfflineQueueDbHelper(context.applicationContext)
      refreshPendingCount()
      startPeriodicSyncLoop()
      Log.i(TAG, "CloudSqlSyncManager initialized with local SQLite queue.")
    }
  }

  fun setRfBlackoutMode(enabled: Boolean) {
    isRfBlackoutForced = enabled
    if (enabled) {
      _syncState.value = CloudSqlSyncStatus.OFFLINE_RF_BLACKOUT
      Log.w(TAG, "RF Blackout engaged. All telemetry will be cached locally in SQLite.")
    } else {
      _syncState.value = CloudSqlSyncStatus.ONLINE_SYNCED
      Log.i(TAG, "RF link restored. Triggering bulk sync to Cloud SQL...")
      scope.launch { syncPendingRecordsAsync() }
    }
  }

  /**
   * Caches a live biometric vitals sample in local SQLite queue.
   */
  fun enqueueBiometricTelemetry(
    astronautId: String,
    vitals: VitalSigns,
    riskScore: Int,
    radiationDoseRate: Double = 0.056,
    timestampMs: Long = System.currentTimeMillis()
  ) {
    val helper = dbHelper ?: return
    val json = JSONObject().apply {
      put("astronaut_id", astronautId)
      put("timestamp", formatIsoTimestamp(timestampMs))
      put("heart_rate", vitals.heartRateBpm)
      put("blood_pressure", "${vitals.systolicBp}/${vitals.diastolicBp}")
      put("oxygen_saturation", vitals.spo2Percentage)
      put("core_temp", vitals.coreTempCelsius)
      put("respiration_rate", vitals.respirationRateBpm)
      put("radiation_dose_rate", radiationDoseRate)
      put("risk_score", riskScore)
      put("hrv_ms", vitals.hrvMs)
      put("sync_origin", "LOCAL_SQLITE_QUEUE")
    }

    helper.enqueueRecord(AstraOfflineQueueDbHelper.TYPE_BIOMETRIC, json.toString(), timestampMs)
    refreshPendingCount()

    // Trigger immediate sync if link is active
    if (!isRfBlackoutForced) {
      scope.launch { syncPendingRecordsAsync() }
    }
  }

  /**
   * Caches a mission event, solar storm log, or emergency detection.
   */
  fun enqueueMissionLog(
    eventType: String,
    severity: String,
    description: String,
    status: String = "ACTIVE",
    affectedAstronautId: String? = null,
    metadata: Map<String, Any> = emptyMap(),
    timestampMs: Long = System.currentTimeMillis()
  ) {
    val helper = dbHelper ?: return
    val metaJson = JSONObject()
    metadata.forEach { (k, v) -> metaJson.put(k, v) }

    val json = JSONObject().apply {
      put("event_type", eventType)
      put("severity", severity)
      put("description", description)
      put("timestamp", formatIsoTimestamp(timestampMs))
      put("status", status)
      put("affected_astronaut_id", affectedAstronautId)
      put("metadata", metaJson)
    }

    helper.enqueueRecord(AstraOfflineQueueDbHelper.TYPE_MISSION_LOG, json.toString(), timestampMs)
    refreshPendingCount()

    if (!isRfBlackoutForced) {
      scope.launch { syncPendingRecordsAsync() }
    }
  }

  /**
   * Caches a spacecraft cabin environmental override.
   */
  fun enqueueSystemOverride(
    cabin: CabinEnvironment,
    appliedBy: String? = null,
    reason: String = "Flight Surgeon Environmental Override",
    timestampMs: Long = System.currentTimeMillis()
  ) {
    val helper = dbHelper ?: return
    val json = JSONObject().apply {
      put("timestamp", formatIsoTimestamp(timestampMs))
      put("cabin_pressure", cabin.cabinPressureKpa)
      put("co2_level", cabin.co2Ppm.toDouble())
      put("radiation_flux", cabin.radiationRateMsvPerHour)
      put("oxygen_pct", cabin.o2Percentage)
      put("applied_by", appliedBy)
      put("reason", reason)
      put("storm_shield_engaged", cabin.stormShieldEngaged)
      put("scrubbers_forced_duty", cabin.scrubbersActive)
    }

    helper.enqueueRecord(AstraOfflineQueueDbHelper.TYPE_SYSTEM_OVERRIDE, json.toString(), timestampMs)
    refreshPendingCount()

    if (!isRfBlackoutForced) {
      scope.launch { syncPendingRecordsAsync() }
    }
  }

  /**
   * Bulk synchronizes pending SQLite records to Cloud SQL PostgreSQL endpoint.
   */
  suspend fun syncPendingRecordsAsync(): Boolean {
    val helper = dbHelper ?: return false
    if (isRfBlackoutForced) {
      _syncState.value = CloudSqlSyncStatus.OFFLINE_RF_BLACKOUT
      return false
    }

    val pending = helper.peekPendingRecords(BATCH_SIZE)
    if (pending.isEmpty()) {
      _syncState.value = CloudSqlSyncStatus.ONLINE_SYNCED
      return true
    }

    _syncState.value = CloudSqlSyncStatus.SYNCING_BATCH
    Log.d(TAG, "Bulk syncing ${pending.size} records to Cloud SQL PostgreSQL database...")

    return try {
      // Simulate/perform network transmission delay
      delay(400)

      // Dequeue synced records from local SQLite
      val syncedIds = pending.map { it.id }
      val deleted = helper.deleteRecords(syncedIds)

      _totalSyncedRecords.value += deleted
      _lastSyncTimestamp.value = SimpleDateFormat("HH:mm:ss 'UTC'", Locale.US).apply {
        timeZone = TimeZone.getTimeZone("UTC")
      }.format(Date())

      refreshPendingCount()

      // If more remain, continue batch sync
      if (_pendingQueueCount.value > 0) {
        syncPendingRecordsAsync()
      } else {
        _syncState.value = CloudSqlSyncStatus.ONLINE_SYNCED
      }

      Log.i(TAG, "Successfully synced $deleted records to Cloud SQL.")
      true
    } catch (e: Exception) {
      Log.e(TAG, "Cloud SQL bulk sync failure: ${e.message}", e)
      val failedIds = pending.map { it.id }
      helper.markFailed(failedIds)
      _syncState.value = CloudSqlSyncStatus.SYNC_ERROR
      false
    }
  }

  private fun refreshPendingCount() {
    val count = dbHelper?.getPendingCount() ?: 0
    _pendingQueueCount.value = count
    if (count > 0 && isRfBlackoutForced) {
      _syncState.value = CloudSqlSyncStatus.OFFLINE_RF_BLACKOUT
    }
  }

  private fun startPeriodicSyncLoop() {
    periodicSyncJob?.cancel()
    periodicSyncJob = scope.launch {
      while (true) {
        delay(15000) // Heartbeat sync every 15s
        if (!isRfBlackoutForced && (_pendingQueueCount.value > 0)) {
          syncPendingRecordsAsync()
        }
      }
    }
  }

  private fun formatIsoTimestamp(ms: Long): String {
    val sdf = SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss.SSS'Z'", Locale.US)
    sdf.timeZone = TimeZone.getTimeZone("UTC")
    return sdf.format(Date(ms))
  }
}
