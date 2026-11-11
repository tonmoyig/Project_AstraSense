package com.example.data

import android.content.Context
import com.example.model.AlertLayer
import com.example.model.AlertSeverity
import com.example.model.BehaviorAndMovement
import com.example.model.BoneAndMuscleHealth
import com.example.model.CabinEnvironment
import com.example.model.CrewMember
import com.example.model.CrewMessage
import com.example.model.DestinationType
import com.example.model.MentalWellbeing
import com.example.model.PvtTestResult
import com.example.model.RadiationMetrics
import com.example.model.RiskLevel
import com.example.model.SafetyAlert
import com.example.model.SleepAndCircadian
import com.example.model.UserRole
import com.example.model.VitalSigns
import com.example.model.ShiftEventCategory
import com.example.model.ShiftTimelineEvent
import com.example.model.HighFrequencyTelemetrySample
import com.example.model.SystemFeatureConfig
import com.example.model.RiskCalculationMode
import com.example.service.AstraAudioAlarmService
import com.example.service.AstraInAppNotificationManager
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale
import java.util.UUID

object AstraRepository {

  // Authorized Admin-Issued User IDs for Crew Registration validation
  val AUTHORIZED_ADMIN_IDS = setOf(
    "NASA-AS-701",
    "NASA-AS-702",
    "NASA-AS-703",
    "NASA-AS-704",
    "ADMIN-ASTRA-01",
    "NASA-ASTRA-777"
  )

  // Current Mission Destination
  private val _currentDestination = MutableStateFlow(DestinationType.MARS_TRANSIT)
  val currentDestination: StateFlow<DestinationType> = _currentDestination.asStateFlow()

  // Current Logged-in User
  private val _currentUser = MutableStateFlow<CrewMember?>(null)
  val currentUser: StateFlow<CrewMember?> = _currentUser.asStateFlow()

  // Spacecraft Cabin Environment Telemetry
  private val _cabinEnvironment = MutableStateFlow(
    CabinEnvironment(
      cabinPressureKpa = 101.3,
      co2Ppm = 2150,
      o2Percentage = 20.9,
      temperatureCelsius = 21.8,
      hullIntegrityPercent = 99.8,
      radiationRateMsvPerHour = 0.056,
      scrubbersActive = true,
      stormShieldEngaged = false
    )
  )
  val cabinEnvironment: StateFlow<CabinEnvironment> = _cabinEnvironment.asStateFlow()

  // Crew Roster
  private val _crewMembers = MutableStateFlow<List<CrewMember>>(createInitialCrew())
  val crewMembers: StateFlow<List<CrewMember>> = _crewMembers.asStateFlow()

  // Safety Alerts (Layer 1 & Layer 2)
  private val _safetyAlerts = MutableStateFlow<List<SafetyAlert>>(createInitialAlerts())
  val safetyAlerts: StateFlow<List<SafetyAlert>> = _safetyAlerts.asStateFlow()

  // Internal Crew Communications
  private val _messages = MutableStateFlow<List<CrewMessage>>(createInitialMessages())
  val messages: StateFlow<List<CrewMessage>> = _messages.asStateFlow()

  // 24-Hour Shift Biometric, Medication, and Emergency Timeline Events
  private val _shiftEvents = MutableStateFlow<List<ShiftTimelineEvent>>(createInitialShiftEvents())
  val shiftEvents: StateFlow<List<ShiftTimelineEvent>> = _shiftEvents.asStateFlow()

  // PVT Reaction Test Logs
  private val _pvtResults = MutableStateFlow<List<PvtTestResult>>(emptyList())
  val pvtResults: StateFlow<List<PvtTestResult>> = _pvtResults.asStateFlow()

  // Earth Sync Status
  private val _unsyncedPacketsCount = MutableStateFlow(14)
  val unsyncedPacketsCount: StateFlow<Int> = _unsyncedPacketsCount.asStateFlow()

  private val _lastSyncTimestamp = MutableStateFlow("T-04:12:08 UTC")
  val lastSyncTimestamp: StateFlow<String> = _lastSyncTimestamp.asStateFlow()

  private val _isSyncing = MutableStateFlow(false)
  val isSyncing: StateFlow<Boolean> = _isSyncing.asStateFlow()

  // Hardware Sensor Pairing Status
  private val _bluetoothBioHarnessConnected = MutableStateFlow(true)
  val bluetoothBioHarnessConnected: StateFlow<Boolean> = _bluetoothBioHarnessConnected.asStateFlow()

  private val _nfcRadWatchDosimeterConnected = MutableStateFlow(true)
  val nfcRadWatchDosimeterConnected: StateFlow<Boolean> = _nfcRadWatchDosimeterConnected.asStateFlow()

  // Emergency SOS Broadcast & High-Frequency Telemetry Subsystem
  private val _isEmergencySosActive = MutableStateFlow(false)
  val isEmergencySosActive: StateFlow<Boolean> = _isEmergencySosActive.asStateFlow()

  private val _emergencySosOriginator = MutableStateFlow<CrewMember?>(null)
  val emergencySosOriginator: StateFlow<CrewMember?> = _emergencySosOriginator.asStateFlow()

  private val _highFrequencyTelemetryLogs = MutableStateFlow<List<HighFrequencyTelemetrySample>>(emptyList())
  val highFrequencyTelemetryLogs: StateFlow<List<HighFrequencyTelemetrySample>> = _highFrequencyTelemetryLogs.asStateFlow()

  // Manual System Feature Configuration & Overrides
  private val _systemFeatureConfig = MutableStateFlow(SystemFeatureConfig())
  val systemFeatureConfig: StateFlow<SystemFeatureConfig> = _systemFeatureConfig.asStateFlow()

  private var appContext: Context? = null

  fun initialize(context: Context) {
    appContext = context.applicationContext
    AstraPersistenceManager.initialize(context)
    AstraAudioAlarmService.initialize(context)

    // Restore saved destination
    AstraPersistenceManager.getSavedDestination()?.let { savedDest ->
      _currentDestination.value = savedDest
    }

    // Restore saved cabin environment
    AstraPersistenceManager.getSavedCabinEnvironment()?.let { savedCabin ->
      _cabinEnvironment.value = savedCabin
    }

    // Restore persistent session if user previously logged in
    val savedUserId = AstraPersistenceManager.getSavedUserId()
    if (savedUserId != null) {
      val user = _crewMembers.value.find { it.id.equals(savedUserId, ignoreCase = true) }
      if (user != null) {
        _currentUser.value = user
      }
    }
  }

  fun logout() {
    _currentUser.value = null
    AstraPersistenceManager.clearSession()
  }

  fun setDestination(destination: DestinationType) {
    _currentDestination.value = destination
    AstraPersistenceManager.saveDestination(destination)
    // Recompute baseline cabin radiation based on destination
    _cabinEnvironment.update { env ->
      val newRadRate = when (destination) {
        DestinationType.MARS_TRANSIT -> 0.058
        DestinationType.LUNAR_GATEWAY -> 0.030
        DestinationType.LEO_ISS -> 0.015
      }
      env.copy(radiationRateMsvPerHour = newRadRate)
    }
    AstraPersistenceManager.saveCabinEnvironment(_cabinEnvironment.value)
  }

  fun switchUser(crewMember: CrewMember) {
    _currentUser.value = crewMember
    AstraPersistenceManager.saveSession(crewMember.id)
  }

  /**
   * Strict login verification against registered credential vault.
   * Rejects random credentials or mismatched passwords.
   */
  fun authenticateStrictly(
    identifier: String,
    adminId: String,
    password: String
  ): Result<CrewMember> {
    val result = AstraPersistenceManager.verifyCredentialsStrictly(identifier, adminId, password)
    return result.mapCatching { cred ->
      val member = _crewMembers.value.find { it.id.equals(cred.id, ignoreCase = true) }
        ?: createFallbackCrewForCredential(cred)
      _currentUser.value = member
      AstraPersistenceManager.saveSession(member.id)
      member
    }
  }

  private fun createFallbackCrewForCredential(cred: AstraPersistenceManager.UserCredential): CrewMember {
    val template = _crewMembers.value.first()
    val newCrew = template.copy(
      id = cred.id,
      username = cred.username,
      fullName = cred.fullName,
      email = cred.email,
      role = cred.role
    )
    _crewMembers.update { it + newCrew }
    return newCrew
  }

  fun login(credential: String): Boolean {
    val clean = credential.trim()
    val match = _crewMembers.value.find {
      it.id.equals(clean, ignoreCase = true) || it.username.equals(clean, ignoreCase = true)
    }
    return if (match != null) {
      _currentUser.value = match
      AstraPersistenceManager.saveSession(match.id)
      true
    } else {
      false
    }
  }

  fun validateAdminId(adminId: String): Boolean {
    return AUTHORIZED_ADMIN_IDS.contains(adminId.trim().uppercase())
  }

  fun registerCrewMember(
    adminId: String,
    username: String,
    fullName: String,
    email: String,
    gender: String,
    age: Int,
    role: UserRole,
    password: String = "AstraSecure#2026"
  ): CrewMember? {
    if (!validateAdminId(adminId)) return null

    val newMember = CrewMember(
      id = adminId.trim().uppercase(),
      username = username.trim().lowercase(),
      fullName = fullName.trim(),
      email = email.trim(),
      role = role,
      gender = gender,
      age = age,
      missionSpecialty = if (role == UserRole.ADMIN) "Mission Command & Medical" else "Mission Payload & EVA",
      currentModuleLocation = "Node 2 (Harmony) / Crew Quarters",
      currentActivity = "Biometric Onboarding & Baseline Check",
      riskLevel = RiskLevel.LOW,
      vitals = VitalSigns(
        heartRateBpm = 68,
        systolicBp = 118,
        diastolicBp = 76,
        spo2Percentage = 98,
        coreTempCelsius = 36.8,
        respirationRateBpm = 14,
        hrvMs = 62
      ),
      radiation = RadiationMetrics(
        dailyDoseMsv = 0.42,
        missionCumulativeMsv = 14.8,
        careerThresholdPercentage = 3.2
      ),
      sleep = SleepAndCircadian(
        totalSleepHours = 7.6,
        deepSleepPercent = 22,
        remSleepPercent = 24,
        sleepQualityScore = 88,
        circadianPhaseOffsetHours = 0.0,
        blueLightVisorRecommendation = "Nominal spectrum. 460nm daylight mode active."
      ),
      behavior = BehaviorAndMovement(
        postureScore = 92,
        movementSpeedMps = 1.15,
        facialStrainIndex = 14,
        socialParticipationRate = 96,
        voiceAcousticStress = 16
      ),
      boneMuscle = BoneAndMuscleHealth(
        aredDailyForceKn = 4.8,
        t2TreadmillMinutes = 45,
        musclePowerRetentionPercent = 98.4,
        jointRangeOfMotionPercent = 99.0,
        estimatedBmdRetentionPercent = 99.1
      ),
      mentalWellbeing = MentalWellbeing(
        stressLevelIndex = 18,
        lonelinessIndicator = 12,
        sensoryOverloadStatus = "OPTIMAL",
        lastCheckInNote = "Crew registration completed. Operational readiness high.",
        suggestedIntervention = "Nominal routine. Group meal scheduled in Hab Node 1."
      ),
      lastPvtReactionTimeMs = 212
    )

    _crewMembers.update { list ->
      val filtered = list.filterNot { it.id.equals(newMember.id, ignoreCase = true) }
      filtered + newMember
    }
    _currentUser.value = newMember
    AstraPersistenceManager.registerCredential(
      AstraPersistenceManager.UserCredential(
        id = newMember.id,
        username = newMember.username,
        email = newMember.email,
        fullName = newMember.fullName,
        passwordHash = password,
        role = role
      )
    )
    AstraPersistenceManager.saveSession(newMember.id)
    return newMember
  }

  fun sendMessage(recipientId: String, content: String) {
    val sender = _currentUser.value ?: return
    val newMsg = CrewMessage(
      id = UUID.randomUUID().toString(),
      senderName = sender.fullName,
      senderRole = if (sender.role == UserRole.ADMIN) "FLIGHT SURGEON / COMMANDER" else "CREW SPECIALIST",
      recipientId = recipientId,
      content = content,
      timestamp = SimpleDateFormat("HH:mm:ss", Locale.US).format(Date()),
      isHighPriority = sender.role == UserRole.ADMIN
    )
    _messages.update { listOf(newMsg) + it }
  }

  fun recordPvtResult(result: PvtTestResult) {
    _pvtResults.update { listOf(result) + it }
    // Update astronaut's last PVT reaction time and readiness in state
    _crewMembers.update { list ->
      list.map { crew ->
        if (crew.id == result.astronautId) {
          crew.copy(lastPvtReactionTimeMs = result.meanReactionTimeMs)
        } else crew
      }
    }
  }

  fun triggerEmergencyDepressurizationSimulation() {
    _cabinEnvironment.update {
      it.copy(cabinPressureKpa = 91.4)
    }
    val alert = SafetyAlert(
      id = UUID.randomUUID().toString(),
      title = "CRITICAL: RAPID CABIN PRESSURE DROP",
      description = "Cabin pressure dropped below 95.0 kPa threshold (Current: 91.4 kPa). Hull integrity alert.",
      actionDirective = "IMMEDIATE: Don emergency flight helmets. Verify hatch seals on Airlock and Node 3.",
      severity = AlertSeverity.CRITICAL,
      layer = AlertLayer.LAYER_1_HARDWARE_RULES,
      timestamp = "NOW (SIMULATED)"
    )
    _safetyAlerts.update { listOf(alert) + it }
    com.example.data.cloudsql.CloudSqlSyncManager.enqueueMissionLog(
      eventType = "CABIN_DEPRESSURIZATION_ANOMALY",
      severity = "CRITICAL",
      description = "Cabin pressure dropped below 95.0 kPa threshold (Current: 91.4 kPa). Emergency drill triggered.",
      status = "ACTIVE",
      metadata = mapOf("cabin_pressure_kpa" to 91.4, "threshold_kpa" to 95.0)
    )
  }

  fun triggerSolarStormRadiationSimulation() {
    _cabinEnvironment.update {
      it.copy(
        radiationRateMsvPerHour = 1.84,
        stormShieldEngaged = false
      )
    }
    val alert = SafetyAlert(
      id = UUID.randomUUID().toString(),
      title = "LAYER 1: SOLAR PARTICLE EVENT (SPE) SPIKE",
      description = "External dosimeter reading 1.84 mSv/h. Critical threshold exceeded (> 1.2 mSv/h).",
      actionDirective = "MANDATORY: Evacuate crew to storm shelter (Node 1 Water Wall). Deploy poly-shielding.",
      severity = AlertSeverity.CRITICAL,
      layer = AlertLayer.LAYER_1_HARDWARE_RULES,
      timestamp = "NOW (SIMULATED)"
    )
    _safetyAlerts.update { listOf(alert) + it }
    com.example.data.cloudsql.CloudSqlSyncManager.enqueueMissionLog(
      eventType = "DONKI_SOLAR_FLARE_SPE",
      severity = "CRITICAL",
      description = "External dosimeter reading 1.84 mSv/h. Critical solar storm threshold exceeded.",
      status = "ACTIVE",
      metadata = mapOf("radiation_rate_msv_h" to 1.84, "threshold_msv_h" to 1.2)
    )
  }

  fun triggerAirScrubberEmergencySimulation() {
    _cabinEnvironment.update {
      it.copy(co2Ppm = 5420, scrubbersActive = false)
    }
    val alert = SafetyAlert(
      id = UUID.randomUUID().toString(),
      title = "LAYER 1: DANGEROUS CO₂ ACCUMULATION",
      description = "Cabin CO₂ reached 5420 ppm (Exceeds critical limit of 5000 ppm). Scrubbers offline.",
      actionDirective = "ACTION: Activate secondary LiOH canisters immediately. Don breathing apparatus in Work Lab.",
      severity = AlertSeverity.CRITICAL,
      layer = AlertLayer.LAYER_1_HARDWARE_RULES,
      timestamp = "NOW (SIMULATED)"
    )
    _safetyAlerts.update { listOf(alert) + it }
  }

  fun resetCabinNominal() {
    _cabinEnvironment.value = CabinEnvironment(
      cabinPressureKpa = 101.3,
      co2Ppm = 2150,
      o2Percentage = 20.9,
      temperatureCelsius = 21.8,
      hullIntegrityPercent = 99.8,
      radiationRateMsvPerHour = 0.056,
      scrubbersActive = true,
      stormShieldEngaged = true
    )
    // Remove critical alerts
    _safetyAlerts.update { alerts ->
      alerts.filterNot { it.severity == AlertSeverity.CRITICAL }
    }
  }

  fun acknowledgeAlert(alertId: String) {
    _safetyAlerts.update { list ->
      list.map { if (it.id == alertId) it.copy(isAcknowledged = true) else it }
    }
  }

  fun syncWithEarth() {
    _isSyncing.value = true
    _unsyncedPacketsCount.value = 0
    _lastSyncTimestamp.value = "JUST NOW (AUTHENTICATED)"
    _isSyncing.value = false
  }

  fun logShiftEvent(event: ShiftTimelineEvent) {
    _shiftEvents.update { list ->
      // Insert in chronological order based on shiftHour
      (listOf(event) + list).sortedBy { it.shiftHour }
    }
  }

  fun getShiftEventsForAstronaut(astronautId: String): List<ShiftTimelineEvent> {
    return _shiftEvents.value.filter { it.astronautId == astronautId || it.astronautId == "ALL" }
  }

  // Unified Mission Risk Score computation with optional manual override
  fun getMissionRiskScore(member: CrewMember, cabin: CabinEnvironment): com.example.service.MissionRiskScore {
    val base = com.example.service.MissionRiskScoreService.calculateMissionRisk(member, cabin)
    val customScore = _systemFeatureConfig.value.manualRiskScoreValue
    val override = _systemFeatureConfig.value.manualRiskOverride
    return if (customScore != null) {
      val level = when {
        customScore >= 70 -> RiskLevel.HIGH
        customScore >= 34 -> RiskLevel.MODERATE
        else -> RiskLevel.LOW
      }
      base.copy(
        compositeScore = customScore,
        riskLevel = level,
        primaryRiskDrivers = listOf("MANUAL FLIGHT SURGEON OVERRIDE: $customScore/100") + base.primaryRiskDrivers
      )
    } else if (override != null) {
      val overrideScore = when (override) {
        RiskLevel.LOW -> 18
        RiskLevel.MODERATE -> 48
        RiskLevel.HIGH -> 85
      }
      base.copy(
        compositeScore = overrideScore,
        riskLevel = override,
        primaryRiskDrivers = listOf("MANUAL FLIGHT SURGEON OVERRIDE ENGAGED") + base.primaryRiskDrivers
      )
    } else {
      base
    }
  }

  fun calculateCompositeRisk(member: CrewMember, cabin: CabinEnvironment): Pair<Int, RiskLevel> {
    val risk = getMissionRiskScore(member, cabin)
    return Pair(risk.compositeScore, risk.riskLevel)
  }

  /**
   * Triggers high-priority Emergency SOS distress broadcast to medical officers and
   * elevates telemetry acquisition to high-frequency (10.0 Hz).
   */
  fun triggerEmergencySos(originator: CrewMember? = null) {
    val user = originator ?: _currentUser.value ?: _crewMembers.value.first()
    _isEmergencySosActive.value = true
    _emergencySosOriginator.value = user
    _systemFeatureConfig.update { it.copy(telemetrySampleRateHz = 10.0, highFrequencyLoggingEnabled = true) }

    val nowIso = SimpleDateFormat("HH:mm:ss", Locale.US).format(Date())

    // 1. Send automated high-priority message to all Medical Officers
    val sosMsg = CrewMessage(
      id = "SOS-MSG-" + UUID.randomUUID().toString().take(8),
      senderName = user.fullName,
      senderRole = "EMERGENCY SOS DISTRESS CALL",
      recipientId = "ALL",
      content = "[HIGH-PRIORITY EMERGENCY SOS BROADCAST] 🚨 Emergency distress triggered by ${user.fullName} (${user.id}) in ${user.currentModuleLocation}! High-frequency telemetry acquisition engaged (10.0 Hz). Immediate medical officer assistance requested!",
      timestamp = nowIso,
      isHighPriority = true
    )
    _messages.update { listOf(sosMsg) + it }

    // 2. Post critical Layer 1 Safety Alert
    val sosAlert = SafetyAlert(
      id = "SOS-ALERT-" + UUID.randomUUID().toString().take(8),
      title = "CRITICAL: ASTRONAUT EMERGENCY SOS BROADCAST",
      description = "Emergency SOS signal broadcast by ${user.fullName} (${user.id}). High-frequency biometrics (10.0 Hz) telemetry active. Acute distress protocol initiated.",
      actionDirective = "IMMEDIATE: Medical Officer dispatch to ${user.currentModuleLocation}. Open High-Frequency Telemetry Stream.",
      severity = AlertSeverity.CRITICAL,
      layer = AlertLayer.LAYER_1_HARDWARE_RULES,
      timestamp = "NOW (EMERGENCY SOS)"
    )
    _safetyAlerts.update { listOf(sosAlert) + it }

    // 3. Log into 24-hour Shift Timeline
    logShiftEvent(
      ShiftTimelineEvent(
        id = "EVT-SOS-" + UUID.randomUUID().toString().take(8),
        astronautId = user.id,
        astronautName = user.fullName,
        shiftHour = 20.0,
        shiftTimeLabel = "T+20:00",
        utcTimestamp = "$nowIso UTC",
        category = ShiftEventCategory.EMERGENCY,
        severity = AlertSeverity.CRITICAL,
        title = "ASTRONAUT EMERGENCY SOS BROADCAST",
        description = "Emergency SOS initiated by ${user.fullName}. High-frequency telemetry logging engaged (10.0 Hz). Automated broadcast transmitted to Medical Officer.",
        primaryMetric = "Frequency: 10.0 Hz Burst",
        secondaryMetric = "Location: ${user.currentModuleLocation}",
        actionDirective = "Immediate Medical Officer dispatch to ${user.currentModuleLocation}.",
        status = "CRITICAL_PENDING_ACK",
        iconType = "emergency"
      )
    )

    // 4. Generate high-frequency telemetry samples (10.0 Hz rapid samples)
    val nowMs = System.currentTimeMillis()
    val initialSamples = (0 until 12).map { i ->
      val sampleTimeMs = nowMs - (11 - i) * 100L
      val formatted = SimpleDateFormat("HH:mm:ss.SSS", Locale.US).format(Date(sampleTimeMs))
      val hrJitter = (Math.sin(i.toDouble()) * 4).toInt()
      HighFrequencyTelemetrySample(
        id = UUID.randomUUID().toString(),
        timestamp = formatted,
        timestampMs = sampleTimeMs,
        heartRate = (user.vitals.heartRateBpm + 25 + hrJitter).coerceAtMost(180),
        systolicBp = user.vitals.systolicBp + 15,
        diastolicBp = user.vitals.diastolicBp + 8,
        spO2 = (user.vitals.spo2Percentage - 3).coerceAtLeast(82),
        coreTemp = user.vitals.coreTempCelsius + 0.3,
        hrvMs = (user.vitals.hrvMs - 15).coerceAtLeast(18),
        respirationRate = user.vitals.respirationRateBpm + 6,
        cabinPressureKpa = _cabinEnvironment.value.cabinPressureKpa,
        radiationRateMsvPerHour = _cabinEnvironment.value.radiationRateMsvPerHour,
        isSosTriggered = true
      )
    }
    _highFrequencyTelemetryLogs.update { initialSamples + it.take(100) }

    // 5. Trigger audible emergency alarm and on-screen active notification banner
    appContext?.let { ctx ->
      AstraAudioAlarmService.playEmergencyAlarm(ctx)
    }
    AstraInAppNotificationManager.postNotification(
      title = "CRITICAL: ASTRONAUT EMERGENCY SOS BROADCAST",
      message = "Emergency distress triggered by ${user.fullName} (${user.id}) in ${user.currentModuleLocation}! 10.0 Hz high-frequency telemetry burst active.",
      severity = AlertSeverity.CRITICAL,
      requiresAck = true,
      actionLabel = "ACKNOWLEDGE DISTRESS",
      context = appContext,
      playAudio = false
    )
  }

  /**
   * Deactivates the Emergency SOS state and returns telemetry acquisition to nominal rate.
   */
  fun deactivateEmergencySos() {
    _isEmergencySosActive.value = false
    _emergencySosOriginator.value = null
    _systemFeatureConfig.update { it.copy(telemetrySampleRateHz = 0.1, highFrequencyLoggingEnabled = false) }

    // Halt siren alarm and post resolution banner
    AstraAudioAlarmService.stopEmergencyAlarm()
    AstraInAppNotificationManager.postNotification(
      title = "EMERGENCY SOS STAND DOWN",
      message = "Emergency SOS condition stood down by crew / medical officer. Telemetry sampling restored to nominal 0.1 Hz.",
      severity = AlertSeverity.ADVISORY,
      context = appContext,
      playAudio = true
    )

    val nowIso = SimpleDateFormat("HH:mm:ss", Locale.US).format(Date())
    logShiftEvent(
      ShiftTimelineEvent(
        id = "EVT-SOS-STANDDOWN-" + UUID.randomUUID().toString().take(8),
        astronautId = _currentUser.value?.id ?: "ALL",
        astronautName = _currentUser.value?.fullName ?: "Flight Crew",
        shiftHour = 20.2,
        shiftTimeLabel = "T+20:12",
        utcTimestamp = "$nowIso UTC",
        category = ShiftEventCategory.EMERGENCY,
        severity = AlertSeverity.ADVISORY,
        title = "EMERGENCY SOS STAND DOWN / NOMINAL RESUMED",
        description = "Emergency SOS condition stood down by crew / medical officer. Telemetry sampling restored to nominal 0.1 Hz.",
        primaryMetric = "Frequency: 0.1 Hz (Nominal)",
        secondaryMetric = "Status: Nominal",
        actionDirective = "Resume scheduled flight activities.",
        status = "RESOLVED",
        iconType = "check"
      )
    )
  }

  /**
   * Updates manual feature configuration and subsystem toggles.
   */
  fun updateSystemFeatureConfig(newConfig: SystemFeatureConfig) {
    val old = _systemFeatureConfig.value
    _systemFeatureConfig.value = newConfig
    if (old.offlineAutonomyForced != newConfig.offlineAutonomyForced) {
      com.example.data.cloudsql.CloudSqlSyncManager.setRfBlackoutMode(newConfig.offlineAutonomyForced)
    }
  }

  /**
   * Manually updates the spacecraft cabin environmental parameters.
   */
  fun updateCabinEnvironment(newEnv: CabinEnvironment) {
    _cabinEnvironment.value = newEnv
    com.example.data.cloudsql.CloudSqlSyncManager.enqueueSystemOverride(
      cabin = newEnv,
      appliedBy = _currentUser.value?.id,
      reason = "Manual Environmental Telemetry Override"
    )
  }

  /**
   * Manually updates a crew member's vitals and physiological metrics.
   */
  fun updateCrewMember(updated: CrewMember) {
    _crewMembers.update { list ->
      list.map { if (it.id == updated.id) updated else it }
    }
    if (_currentUser.value?.id == updated.id) {
      _currentUser.value = updated
    }
    val risk = getMissionRiskScore(updated, _cabinEnvironment.value)
    com.example.data.cloudsql.CloudSqlSyncManager.enqueueBiometricTelemetry(
      astronautId = updated.id,
      vitals = updated.vitals,
      riskScore = risk.compositeScore,
      radiationDoseRate = updated.radiation.dailyDoseMsv
    )
  }

  /**
   * Resets all parameters, environmental variables, and feature overrides to NASA nominal factory defaults.
   */
  fun resetAllFeaturesToNominal() {
    resetCabinNominal()
    _systemFeatureConfig.value = SystemFeatureConfig()
    _crewMembers.value = createInitialCrew()
    val current = _currentUser.value
    if (current != null) {
      _currentUser.value = _crewMembers.value.find { it.id == current.id } ?: _crewMembers.value.first()
    }
    deactivateEmergencySos()
  }

  fun setUnsyncedPacketsCount(count: Int) {
    _unsyncedPacketsCount.value = count
  }

  fun clearShiftEvents() {
    _shiftEvents.value = createInitialShiftEvents()
  }

  private fun createInitialCrew(): List<CrewMember> {
    return listOf(
      CrewMember(
        id = "NASA-AS-701",
        username = "alex_vance",
        fullName = "Dr. Alex Vance",
        email = "a.vance@nasa.gov",
        role = UserRole.ADMIN,
        gender = "Female",
        age = 42,
        missionSpecialty = "Flight Surgeon & Mission Commander",
        currentModuleLocation = "Cupola / Command Bridge",
        currentActivity = "Mission Telemetry Review & Crew Check",
        riskLevel = RiskLevel.LOW,
        vitals = VitalSigns(
          heartRateBpm = 64,
          systolicBp = 116,
          diastolicBp = 74,
          spo2Percentage = 99,
          coreTempCelsius = 36.6,
          respirationRateBpm = 13,
          hrvMs = 72
        ),
        radiation = RadiationMetrics(
          dailyDoseMsv = 0.38,
          missionCumulativeMsv = 46.2,
          careerThresholdPercentage = 7.1
        ),
        sleep = SleepAndCircadian(
          totalSleepHours = 7.8,
          deepSleepPercent = 25,
          remSleepPercent = 26,
          sleepQualityScore = 92,
          circadianPhaseOffsetHours = 0.1,
          blueLightVisorRecommendation = "Nominal circadian alignment. Light cycle 16h on / 8h off."
        ),
        behavior = BehaviorAndMovement(
          postureScore = 95,
          movementSpeedMps = 1.22,
          facialStrainIndex = 12,
          socialParticipationRate = 100,
          voiceAcousticStress = 14
        ),
        boneMuscle = BoneAndMuscleHealth(
          aredDailyForceKn = 5.2,
          t2TreadmillMinutes = 50,
          musclePowerRetentionPercent = 99.2,
          jointRangeOfMotionPercent = 100.0,
          estimatedBmdRetentionPercent = 99.4
        ),
        mentalWellbeing = MentalWellbeing(
          stressLevelIndex = 15,
          lonelinessIndicator = 10,
          sensoryOverloadStatus = "OPTIMAL",
          lastCheckInNote = "Readiness excellent. Flight operations fully nominal.",
          suggestedIntervention = "Maintain circadian hygiene and scheduled family comms."
        ),
        lastPvtReactionTimeMs = 208
      ),
      CrewMember(
        id = "NASA-AS-702",
        username = "elena_rostova",
        fullName = "Dr. Elena Rostova",
        email = "e.rostova@esa.int",
        role = UserRole.CREW,
        gender = "Female",
        age = 37,
        missionSpecialty = "Astrobiologist & EVA Specialist",
        currentModuleLocation = "Columbus / Airlock Quest",
        currentActivity = "Post-EVA Suit Decontamination & Sample Prep",
        riskLevel = RiskLevel.MODERATE,
        vitals = VitalSigns(
          heartRateBpm = 86,
          systolicBp = 128,
          diastolicBp = 82,
          spo2Percentage = 97,
          coreTempCelsius = 37.1,
          respirationRateBpm = 17,
          hrvMs = 46
        ),
        radiation = RadiationMetrics(
          dailyDoseMsv = 0.94, // higher due to EVA outside shielding
          missionCumulativeMsv = 68.4,
          careerThresholdPercentage = 10.5
        ),
        sleep = SleepAndCircadian(
          totalSleepHours = 5.4,
          deepSleepPercent = 14,
          remSleepPercent = 16,
          sleepQualityScore = 62,
          circadianPhaseOffsetHours = -1.5,
          blueLightVisorRecommendation = "Phase delay detected. Wear 490nm amber visor during rest prep."
        ),
        behavior = BehaviorAndMovement(
          postureScore = 78,
          movementSpeedMps = 0.94,
          facialStrainIndex = 48,
          socialParticipationRate = 84,
          voiceAcousticStress = 44
        ),
        boneMuscle = BoneAndMuscleHealth(
          aredDailyForceKn = 4.1,
          t2TreadmillMinutes = 35,
          musclePowerRetentionPercent = 94.6,
          jointRangeOfMotionPercent = 93.5,
          estimatedBmdRetentionPercent = 97.2
        ),
        mentalWellbeing = MentalWellbeing(
          stressLevelIndex = 46,
          lonelinessIndicator = 28,
          sensoryOverloadStatus = "MILD ELEVATION",
          lastCheckInNote = "Slight shoulder fatigue following 6.5h external spacewalk.",
          suggestedIntervention = "30-minute zero-g acoustic relaxation; 400ml electrolytic rehydration."
        ),
        lastPvtReactionTimeMs = 286
      ),
      CrewMember(
        id = "NASA-AS-703",
        username = "mark_watney",
        fullName = "Mark Watney Jr.",
        email = "m.watney@nasa.gov",
        role = UserRole.CREW,
        gender = "Male",
        age = 39,
        missionSpecialty = "Systems Engineer & ECLSS Specialist",
        currentModuleLocation = "Node 3 (Tranquility) / Life Support",
        currentActivity = "CO₂ Scrubber Filter Reconfiguration",
        riskLevel = RiskLevel.LOW,
        vitals = VitalSigns(
          heartRateBpm = 70,
          systolicBp = 120,
          diastolicBp = 78,
          spo2Percentage = 98,
          coreTempCelsius = 36.7,
          respirationRateBpm = 14,
          hrvMs = 65
        ),
        radiation = RadiationMetrics(
          dailyDoseMsv = 0.44,
          missionCumulativeMsv = 51.3,
          careerThresholdPercentage = 7.9
        ),
        sleep = SleepAndCircadian(
          totalSleepHours = 7.1,
          deepSleepPercent = 20,
          remSleepPercent = 22,
          sleepQualityScore = 84,
          circadianPhaseOffsetHours = 0.2,
          blueLightVisorRecommendation = "Circadian rhythm stable."
        ),
        behavior = BehaviorAndMovement(
          postureScore = 90,
          movementSpeedMps = 1.18,
          facialStrainIndex = 18,
          socialParticipationRate = 92,
          voiceAcousticStress = 20
        ),
        boneMuscle = BoneAndMuscleHealth(
          aredDailyForceKn = 5.0,
          t2TreadmillMinutes = 45,
          musclePowerRetentionPercent = 97.8,
          jointRangeOfMotionPercent = 98.2,
          estimatedBmdRetentionPercent = 98.6
        ),
        mentalWellbeing = MentalWellbeing(
          stressLevelIndex = 22,
          lonelinessIndicator = 18,
          sensoryOverloadStatus = "OPTIMAL",
          lastCheckInNote = "ECLSS scrubbers operating at 99.4% filtration efficiency.",
          suggestedIntervention = "Nominal rest cycle. Botanical greenhouse observation recommended."
        ),
        lastPvtReactionTimeMs = 224
      ),
      CrewMember(
        id = "NASA-AS-704",
        username = "kenji_sato",
        fullName = "Kenji Sato",
        email = "k.sato@jaxa.jp",
        role = UserRole.CREW,
        gender = "Male",
        age = 35,
        missionSpecialty = "Mission Pilot & Trajectory Officer",
        currentModuleLocation = "Destiny Lab / Kibo Module",
        currentActivity = "Orbital Burn Vector Computation",
        riskLevel = RiskLevel.LOW,
        vitals = VitalSigns(
          heartRateBpm = 66,
          systolicBp = 114,
          diastolicBp = 72,
          spo2Percentage = 99,
          coreTempCelsius = 36.5,
          respirationRateBpm = 13,
          hrvMs = 70
        ),
        radiation = RadiationMetrics(
          dailyDoseMsv = 0.40,
          missionCumulativeMsv = 42.8,
          careerThresholdPercentage = 6.6
        ),
        sleep = SleepAndCircadian(
          totalSleepHours = 7.4,
          deepSleepPercent = 24,
          remSleepPercent = 23,
          sleepQualityScore = 89,
          circadianPhaseOffsetHours = 0.0,
          blueLightVisorRecommendation = "Full circadian synchrony."
        ),
        behavior = BehaviorAndMovement(
          postureScore = 93,
          movementSpeedMps = 1.20,
          facialStrainIndex = 15,
          socialParticipationRate = 95,
          voiceAcousticStress = 18
        ),
        boneMuscle = BoneAndMuscleHealth(
          aredDailyForceKn = 4.9,
          t2TreadmillMinutes = 45,
          musclePowerRetentionPercent = 98.0,
          jointRangeOfMotionPercent = 99.0,
          estimatedBmdRetentionPercent = 99.0
        ),
        mentalWellbeing = MentalWellbeing(
          stressLevelIndex = 16,
          lonelinessIndicator = 14,
          sensoryOverloadStatus = "OPTIMAL",
          lastCheckInNote = "Guidance telemetry nominal. Reaction time within top 5th percentile.",
          suggestedIntervention = "Maintain scheduled cycle ergometer session."
        ),
        lastPvtReactionTimeMs = 198
      )
    )
  }

  private fun createInitialAlerts(): List<SafetyAlert> {
    return listOf(
      SafetyAlert(
        id = "ALERT-001",
        title = "LAYER 2 AI ASSISTANT: ELEVATED CUMULATIVE FATIGUE",
        description = "Pattern detected: Dr. Elena Rostova recorded 5.4h sleep + rising resting HR (+12 bpm) following EVA spacewalk.",
        actionDirective = "RECOMMENDATION: Schedule additional 90-min sleep cycle before high-risk robotic arm duty.",
        severity = AlertSeverity.WARNING,
        layer = AlertLayer.LAYER_2_AI_ASSISTANT,
        targetCrewMemberId = "NASA-AS-702",
        timestamp = "14:22 UTC"
      ),
      SafetyAlert(
        id = "ALERT-002",
        title = "LAYER 1 HARDWARE: RADWATCH DOSIMETER FLUX",
        description = "Daily radiation exposure in Columbus module reached 0.94 mSv (Nominal is < 0.60 mSv/d).",
        actionDirective = "ACTION: Restrict non-essential science operations in outboard unshielded racks for 18 hours.",
        severity = AlertSeverity.WARNING,
        layer = AlertLayer.LAYER_1_HARDWARE_RULES,
        targetCrewMemberId = "NASA-AS-702",
        timestamp = "11:05 UTC"
      ),
      SafetyAlert(
        id = "ALERT-003",
        title = "LAYER 2 AI ASSISTANT: EXERCISE PERFORMANCE SHIFT",
        description = "ARED peak resistance load decreased by 7.2% across last 3 workout sessions.",
        actionDirective = "RECOMMENDATION: Request medical review for lower-limb loading calibration.",
        severity = AlertSeverity.ADVISORY,
        layer = AlertLayer.LAYER_2_AI_ASSISTANT,
        targetCrewMemberId = "NASA-AS-702",
        timestamp = "08:40 UTC"
      )
    )
  }

  private fun createInitialMessages(): List<CrewMessage> {
    return listOf(
      CrewMessage(
        id = "MSG-001",
        senderName = "Dr. Alex Vance",
        senderRole = "FLIGHT SURGEON / COMMANDER",
        recipientId = "ALL",
        content = "All crew: Mission solar array reorientation scheduled at 16:00 UTC. Ensure all loose tools are stowed.",
        timestamp = "13:30 UTC",
        isHighPriority = true
      ),
      CrewMessage(
        id = "MSG-002",
        senderName = "Dr. Alex Vance",
        senderRole = "FLIGHT SURGEON",
        recipientId = "NASA-AS-702",
        content = "Elena, your post-EVA hydration levels look slightly low. Please take 500ml electrolyte pouch and rest.",
        timestamp = "12:15 UTC",
        isHighPriority = false
      ),
      CrewMessage(
        id = "MSG-003",
        senderName = "Mark Watney Jr.",
        senderRole = "CREW SPECIALIST",
        recipientId = "ALL",
        content = "Node 3 scrubber filter cycle complete. Air quality nominal at 2150 ppm CO2.",
        timestamp = "10:00 UTC",
        isHighPriority = false
      )
    )
  }

  private fun createInitialShiftEvents(): List<ShiftTimelineEvent> {
    return listOf(
      ShiftTimelineEvent(
        id = "EVT-SHIFT-01",
        astronautId = "NASA-AS-702",
        astronautName = "Dr. Elena Rostova",
        shiftHour = 0.5,
        shiftTimeLabel = "T+00:30",
        utcTimestamp = "06:30 UTC",
        category = ShiftEventCategory.BIOMETRIC,
        severity = AlertSeverity.ADVISORY,
        title = "Waking Circadian Vitals & Sleep Log",
        description = "Bio-harness logged 5.4h total sleep duration with 14% deep sleep and circadian phase delay of -1.5h. Resting HR baseline at 68 bpm.",
        primaryMetric = "HR 68 bpm • Sleep 5.4h",
        secondaryMetric = "SpO₂ 98% • HRV 58ms",
        actionDirective = "Blue light visor schedule initiated per circadian offset protocol.",
        status = "LOGGED",
        iconType = "sleep"
      ),
      ShiftTimelineEvent(
        id = "EVT-SHIFT-02",
        astronautId = "NASA-AS-702",
        astronautName = "Dr. Elena Rostova",
        shiftHour = 1.25,
        shiftTimeLabel = "T+01:15",
        utcTimestamp = "07:15 UTC",
        category = ShiftEventCategory.MEDICATION,
        severity = AlertSeverity.ADVISORY,
        title = "Pre-EVA Motion Sickness Countermeasure",
        description = "Administered Promethazine 25mg oral tablet with 300ml electrolyte solution prior to Quest Airlock pre-breathe protocol.",
        primaryMetric = "Promethazine 25mg Oral",
        secondaryMetric = "Fluid Intake 300ml",
        actionDirective = "Monitor for mild sedation during 100% O₂ decompression pre-breathe.",
        status = "ADMINISTERED",
        iconType = "pill"
      ),
      ShiftTimelineEvent(
        id = "EVT-SHIFT-03",
        astronautId = "NASA-AS-701",
        astronautName = "Dr. Alex Vance",
        shiftHour = 2.0,
        shiftTimeLabel = "T+02:00",
        utcTimestamp = "08:00 UTC",
        category = ShiftEventCategory.BIOMETRIC,
        severity = AlertSeverity.ADVISORY,
        title = "ARED Resistive Exercise Cycle Completed",
        description = "Advanced Resistive Exercise Device session completed at 5.2 kN peak load. Cardiovascular output and joint range of motion nominal.",
        primaryMetric = "Peak Load 5.2 kN",
        secondaryMetric = "Treadmill 50 min • HR 142 bpm",
        actionDirective = "Maintain scheduled rehydration and calcium intake.",
        status = "VERIFIED",
        iconType = "exercise"
      ),
      ShiftTimelineEvent(
        id = "EVT-SHIFT-04",
        astronautId = "NASA-AS-702",
        astronautName = "Dr. Elena Rostova",
        shiftHour = 3.75,
        shiftTimeLabel = "T+03:45",
        utcTimestamp = "09:45 UTC",
        category = ShiftEventCategory.BIOMETRIC,
        severity = AlertSeverity.WARNING,
        title = "EVA High Metabolic Exertion Spike",
        description = "During outboard orbital replacement unit (ORU) translation on truss segment P6, heart rate peaked at 134 bpm with core body temperature rising to 37.6°C.",
        primaryMetric = "HR 134 bpm • Temp 37.6°C",
        secondaryMetric = "Workload 460 kcal/h",
        actionDirective = "Flight Surgeon ordered 5-minute tethered cooling pause in sublimator loop.",
        status = "VERIFIED",
        iconType = "heart"
      ),
      ShiftTimelineEvent(
        id = "EVT-SHIFT-05",
        astronautId = "NASA-AS-702",
        astronautName = "Dr. Elena Rostova",
        shiftHour = 6.33,
        shiftTimeLabel = "T+06:20",
        utcTimestamp = "12:20 UTC",
        category = ShiftEventCategory.EMERGENCY,
        severity = AlertSeverity.CRITICAL,
        title = "Solar Particle Event (SPE) Outboard Dosimeter Flux",
        description = "Layer 1 hardware sensor detected abrupt energetic proton flux spike exceeding critical threshold (0.94 mSv/h outside shielding). Instant acoustic alarm triggered.",
        primaryMetric = "Radiation Flux 0.94 mSv/h",
        secondaryMetric = "Daily Exposure +0.48 mSv",
        actionDirective = "IMMEDIATE: Abort EVA. Ingress Quest airlock and secure hatch within 20 minutes.",
        status = "RESOLVED",
        iconType = "radiation"
      ),
      ShiftTimelineEvent(
        id = "EVT-SHIFT-06",
        astronautId = "NASA-AS-702",
        astronautName = "Dr. Elena Rostova",
        shiftHour = 7.1,
        shiftTimeLabel = "T+07:05",
        utcTimestamp = "13:05 UTC",
        category = ShiftEventCategory.MEDICATION,
        severity = AlertSeverity.WARNING,
        title = "Post-SPE Radioprotectant & Oral Rehydration",
        description = "In response to acute solar flux spike, astronaut ingested Potassium Iodide (KI) 65mg prophylactic countermeasure accompanied by 500ml oral rehydration salts.",
        primaryMetric = "Potassium Iodide 65mg",
        secondaryMetric = "Rehydration Pouch 500ml",
        actionDirective = "Track thyroid uptake and maintain urine output logs in Medical Locker.",
        status = "ADMINISTERED",
        iconType = "pill"
      ),
      ShiftTimelineEvent(
        id = "EVT-SHIFT-07",
        astronautId = "NASA-AS-703",
        astronautName = "Mark Watney Jr.",
        shiftHour = 9.0,
        shiftTimeLabel = "T+09:00",
        utcTimestamp = "15:00 UTC",
        category = ShiftEventCategory.EMERGENCY,
        severity = AlertSeverity.WARNING,
        title = "Cabin CO₂ Hypercapnia Threshold Surge",
        description = "Life support scrubbers in Node 3 experienced temporary bypass valve obstruction, elevating local carbon dioxide concentration to 4,120 ppm.",
        primaryMetric = "Cabin CO₂ 4,120 ppm",
        secondaryMetric = "Threshold: < 3000 ppm",
        actionDirective = "Activated secondary LiOH canister canisters; donned respiratory masks until nominal.",
        status = "RESOLVED",
        iconType = "alert"
      ),
      ShiftTimelineEvent(
        id = "EVT-SHIFT-08",
        astronautId = "NASA-AS-702",
        astronautName = "Dr. Elena Rostova",
        shiftHour = 11.25,
        shiftTimeLabel = "T+11:15",
        utcTimestamp = "17:15 UTC",
        category = ShiftEventCategory.BIOMETRIC,
        severity = AlertSeverity.WARNING,
        title = "Post-EVA Orthostatic Hypotension & SpO₂ Dip",
        description = "Post-airlock medical checkup revealed transient blood pressure drop to 102/68 mmHg with elevated resting pulse at 86 bpm and SpO₂ dipping to 97%.",
        primaryMetric = "BP 102/68 • HR 86 bpm",
        secondaryMetric = "SpO₂ 97% • Temp 37.1°C",
        actionDirective = "Prescribed lower-body compression garments and 30-minute quiet horizontal rest.",
        status = "MONITORING",
        iconType = "heart"
      ),
      ShiftTimelineEvent(
        id = "EVT-SHIFT-09",
        astronautId = "NASA-AS-702",
        astronautName = "Dr. Elena Rostova",
        shiftHour = 14.33,
        shiftTimeLabel = "T+14:20",
        utcTimestamp = "20:20 UTC",
        category = ShiftEventCategory.BIOMETRIC,
        severity = AlertSeverity.WARNING,
        title = "PVT Cognitive Vigilance & Reaction Lag Alert",
        description = "Scheduled 30-second Psychomotor Vigilance Test recorded mean reaction time of 286ms (+78ms above baseline) with 2 lapses (>500ms).",
        primaryMetric = "PVT 286ms (Alert Score 64)",
        secondaryMetric = "2 Lapses (>500ms)",
        actionDirective = "Robotic arm docking duties transferred to Commander Vance. Rest protocol assigned.",
        status = "VERIFIED",
        iconType = "brain"
      ),
      ShiftTimelineEvent(
        id = "EVT-SHIFT-10",
        astronautId = "NASA-AS-704",
        astronautName = "Kenji Sato",
        shiftHour = 16.0,
        shiftTimeLabel = "T+16:00",
        utcTimestamp = "22:00 UTC",
        category = ShiftEventCategory.BIOMETRIC,
        severity = AlertSeverity.ADVISORY,
        title = "Orbital Burn Acceleration Hemodynamics",
        description = "During 180-second propulsion vector trim burn, heart rate remained stable at 72 bpm, demonstrating optimal G-tolerance and autonomic stability.",
        primaryMetric = "HR 72 bpm • BP 118/76",
        secondaryMetric = "Max Delta-V 0.38G",
        actionDirective = "Trajectory parameters nominal. Stand down from high-g posture.",
        status = "VERIFIED",
        iconType = "heart"
      ),
      ShiftTimelineEvent(
        id = "EVT-SHIFT-11",
        astronautId = "NASA-AS-702",
        astronautName = "Dr. Elena Rostova",
        shiftHour = 17.5,
        shiftTimeLabel = "T+17:30",
        utcTimestamp = "23:30 UTC",
        category = ShiftEventCategory.MEDICATION,
        severity = AlertSeverity.ADVISORY,
        title = "Circadian Phase Resynchronization Protocol",
        description = "Administered Melatonin 3mg oral tablet with blue-light filtering amber glasses (490nm wavelength cut) 60 minutes prior to crew quarters rest cycle.",
        primaryMetric = "Melatonin 3mg Oral",
        secondaryMetric = "Amber Filter Visor Active",
        actionDirective = "Dim crew module illumination to 10% lux. Avoid screen exposure.",
        status = "ADMINISTERED",
        iconType = "pill"
      ),
      ShiftTimelineEvent(
        id = "EVT-SHIFT-12",
        astronautId = "NASA-AS-702",
        astronautName = "Dr. Elena Rostova",
        shiftHour = 19.0,
        shiftTimeLabel = "T+19:00",
        utcTimestamp = "01:00 UTC",
        category = ShiftEventCategory.BIOMETRIC,
        severity = AlertSeverity.ADVISORY,
        title = "Pre-Sleep Autonomic HRV Stabilization",
        description = "Pre-sleep resting assessment confirmed heart rate reduction to 64 bpm and HRV recovery to 46ms. Stress index down 22 points.",
        primaryMetric = "HR 64 bpm • HRV 46ms",
        secondaryMetric = "Stress Index 24/100",
        actionDirective = "Sleep pod environmental acoustic dampening set to 42 dB. Nominal night rest.",
        status = "LOGGED",
        iconType = "sleep"
      )
    )
  }
}
