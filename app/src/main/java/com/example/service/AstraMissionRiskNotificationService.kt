package com.example.service

import android.Manifest
import android.app.NotificationChannel
import android.app.NotificationManager
import android.app.PendingIntent
import android.content.Context
import android.content.Intent
import android.content.pm.PackageManager
import android.graphics.Color as AndroidColor
import android.os.Build
import androidx.core.app.NotificationCompat
import androidx.core.app.NotificationManagerCompat
import androidx.core.content.ContextCompat
import com.example.MainActivity
import com.example.R
import com.example.data.AstraRepository
import com.example.model.AlertSeverity
import com.example.model.CabinEnvironment
import com.example.model.CrewMember
import com.example.model.RiskLevel
import com.example.model.ShiftEventCategory
import com.example.model.ShiftTimelineEvent
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import java.util.UUID

data class CriticalRiskAlert(
  val alertId: String,
  val astronautId: String,
  val astronautName: String,
  val compositeScore: Int,
  val riskLevel: RiskLevel,
  val hazardTitle: String,
  val clinicalDirective: String,
  val timestampMs: Long = System.currentTimeMillis(),
  val isAcknowledged: Boolean = false,
  val acknowledgedByOfficerId: String? = null,
  val acknowledgedTimestampMs: Long? = null,
  val clinicalNotes: String? = null
)

object AstraMissionRiskNotificationService {

  const val CHANNEL_CRITICAL_RISK = "astra_critical_mission_risk"
  const val CHANNEL_NAME = "AstraSense Critical Mission Risk Alerts"
  const val CHANNEL_DESCRIPTION = "High-priority alarms when astronaut mission risk score exceeds critical safety thresholds requiring medical officer sign-off."

  const val ACTION_ACKNOWLEDGE_CRITICAL = "com.example.action.ACKNOWLEDGE_CRITICAL_RISK"
  const val EXTRA_ALERT_ID = "extra_alert_id"
  const val EXTRA_ASTRONAUT_ID = "extra_astronaut_id"
  const val EXTRA_RISK_SCORE = "extra_risk_score"

  const val CRITICAL_RISK_THRESHOLD = 70

  private val _activeCriticalAlerts = MutableStateFlow<List<CriticalRiskAlert>>(emptyList())
  val activeCriticalAlerts: StateFlow<List<CriticalRiskAlert>> = _activeCriticalAlerts.asStateFlow()

  private var isChannelCreated = false

  fun initialize(context: Context) {
    if (isChannelCreated) return
    createNotificationChannel(context)
    isChannelCreated = true
  }

  private fun createNotificationChannel(context: Context) {
    if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
      val importance = NotificationManager.IMPORTANCE_HIGH
      val channel = NotificationChannel(CHANNEL_CRITICAL_RISK, CHANNEL_NAME, importance).apply {
        description = CHANNEL_DESCRIPTION
        enableLights(true)
        lightColor = AndroidColor.RED
        enableVibration(true)
        vibrationPattern = longArrayOf(0, 500, 200, 500, 200, 500)
        lockscreenVisibility = NotificationCompat.VISIBILITY_PUBLIC
        setShowBadge(true)
      }
      val notificationManager = context.getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager
      notificationManager.createNotificationChannel(channel)
    }
  }

  /**
   * Evaluates the Mission Risk Score against the critical threshold (>= 70 or RiskLevel.HIGH).
   * If threshold is reached and not already active/unacknowledged, triggers a high-priority local push notification.
   */
  fun evaluateAndNotify(
    context: Context,
    member: CrewMember,
    cabin: CabinEnvironment
  ): Boolean {
    initialize(context)
    val missionRisk = MissionRiskScoreService.calculateMissionRisk(member, cabin)

    if (missionRisk.compositeScore < CRITICAL_RISK_THRESHOLD && missionRisk.riskLevel != RiskLevel.HIGH) {
      return false
    }

    // Check if an unacknowledged alert already exists for this astronaut to prevent spamming
    val currentAlerts = _activeCriticalAlerts.value
    val existingPending = currentAlerts.find {
      it.astronautId == member.id && !it.isAcknowledged
    }
    if (existingPending != null) {
      return false
    }

    val alertId = "CRIT-" + UUID.randomUUID().toString().take(8).uppercase()
    val newAlert = CriticalRiskAlert(
      alertId = alertId,
      astronautId = member.id,
      astronautName = member.fullName,
      compositeScore = missionRisk.compositeScore,
      riskLevel = missionRisk.riskLevel,
      hazardTitle = "CRITICAL MISSION RISK: ${member.fullName.uppercase()} (${missionRisk.compositeScore}/100)",
      clinicalDirective = missionRisk.clinicalActionDirective
    )

    _activeCriticalAlerts.update { listOf(newAlert) + it }

    // Log emergency in shift timeline
    AstraRepository.logShiftEvent(
      ShiftTimelineEvent(
        id = "EVT-" + alertId,
        astronautId = member.id,
        astronautName = member.fullName,
        shiftHour = 19.5,
        shiftTimeLabel = "T+19:30",
        utcTimestamp = "01:30 UTC",
        category = ShiftEventCategory.EMERGENCY,
        severity = AlertSeverity.CRITICAL,
        title = "CRITICAL RISK: ${member.fullName} (${missionRisk.compositeScore}/100)",
        description = "Unified risk score exceeded critical safety threshold (${missionRisk.compositeScore}/100). Health: ${missionRisk.healthSubScore}, Radiation: ${missionRisk.radiationSubScore}, Fatigue: ${missionRisk.fatigueSubScore}.",
        primaryMetric = "Risk Score ${missionRisk.compositeScore}/100",
        secondaryMetric = "Medical Officer Sign-off Required",
        actionDirective = missionRisk.clinicalActionDirective,
        status = "CRITICAL_PENDING_ACK",
        iconType = "alert"
      )
    )

    // Trigger Android Notification if permission is granted
    postCriticalNotification(context, newAlert, missionRisk)
    return true
  }

  /**
   * Triggers an immediate Critical Risk Alert and high-priority notification specifically
   * for an astronaut Emergency SOS broadcast.
   */
  fun triggerEmergencySosNotification(
    context: Context,
    originator: CrewMember
  ): CriticalRiskAlert {
    initialize(context)
    val alertId = "SOS-" + UUID.randomUUID().toString().take(8).uppercase()
    val sosAlert = CriticalRiskAlert(
      alertId = alertId,
      astronautId = originator.id,
      astronautName = originator.fullName,
      compositeScore = 99,
      riskLevel = RiskLevel.HIGH,
      hazardTitle = "EMERGENCY SOS: ${originator.fullName.uppercase()} DISTRESS BROADCAST",
      clinicalDirective = "IMMEDIATE DISPATCH: Astronaut signaled Emergency SOS from ${originator.currentModuleLocation}. High-frequency telemetry acquisition active (10.0 Hz). Medical Officer intervention mandatory.",
      timestampMs = System.currentTimeMillis()
    )

    _activeCriticalAlerts.update { listOf(sosAlert) + it.filterNot { a -> a.alertId == alertId } }

    val missionRisk = MissionRiskScore(
      compositeScore = 99,
      riskLevel = RiskLevel.HIGH,
      healthSubScore = 95,
      radiationSubScore = 90,
      fatigueSubScore = 90,
      environmentalModifier = 30,
      primaryRiskDrivers = listOf("ASTRONAUT EMERGENCY SOS BROADCAST ACTIVE", "HIGH-FREQUENCY TELEMETRY (10.0 Hz)"),
      clinicalActionDirective = sosAlert.clinicalDirective,
      missionReadinessPercentage = 10
    )

    postCriticalNotification(context, sosAlert, missionRisk)
    return sosAlert
  }

  private fun postCriticalNotification(
    context: Context,
    alert: CriticalRiskAlert,
    risk: MissionRiskScore
  ) {
    if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
      if (ContextCompat.checkSelfPermission(context, Manifest.permission.POST_NOTIFICATIONS) != PackageManager.PERMISSION_GRANTED) {
        return
      }
    }

    val notificationId = alert.alertId.hashCode()

    // Intent to open MainActivity on notification tap
    val openAppIntent = Intent(context, MainActivity::class.java).apply {
      flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TOP
      putExtra(EXTRA_ALERT_ID, alert.alertId)
      putExtra(EXTRA_ASTRONAUT_ID, alert.astronautId)
    }
    val contentPendingIntent = PendingIntent.getActivity(
      context,
      notificationId,
      openAppIntent,
      PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
    )

    // Action button to Acknowledge directly from notification tray
    val acknowledgeIntent = Intent(context, MedicalOfficerActionReceiver::class.java).apply {
      action = ACTION_ACKNOWLEDGE_CRITICAL
      putExtra(EXTRA_ALERT_ID, alert.alertId)
      putExtra(EXTRA_ASTRONAUT_ID, alert.astronautId)
      putExtra(EXTRA_RISK_SCORE, alert.compositeScore)
    }
    val acknowledgePendingIntent = PendingIntent.getBroadcast(
      context,
      notificationId + 1,
      acknowledgeIntent,
      PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
    )

    val bigText = StringBuilder()
      .append("⚠️ CRITICAL THRESHOLD EXCEEDED: ${alert.compositeScore}/100\n")
      .append("Astronaut: ${alert.astronautName} (${alert.astronautId})\n")
      .append("DIRECTIVE: ${alert.clinicalDirective}\n\n")
      .append("Sub-Scores: Health ${risk.healthSubScore} | Rad ${risk.radiationSubScore} | Fatigue ${risk.fatigueSubScore}\n")
      .append("Requires immediate onboard Medical Officer / Flight Surgeon acknowledgement.")
      .toString()

    val notificationBuilder = NotificationCompat.Builder(context, CHANNEL_CRITICAL_RISK)
      .setSmallIcon(R.drawable.ic_launcher_foreground)
      .setContentTitle("🚨 CRITICAL RISK: ${alert.astronautName} (${alert.compositeScore}/100)")
      .setContentText(alert.clinicalDirective)
      .setStyle(NotificationCompat.BigTextStyle().bigText(bigText))
      .setPriority(NotificationCompat.PRIORITY_MAX)
      .setCategory(NotificationCompat.CATEGORY_ALARM)
      .setVisibility(NotificationCompat.VISIBILITY_PUBLIC)
      .setAutoCancel(true)
      .setOngoing(true) // Keeps notification until acknowledged
      .setContentIntent(contentPendingIntent)
      .addAction(
        android.R.drawable.ic_menu_agenda,
        "ACKNOWLEDGE ALERT",
        acknowledgePendingIntent
      )

    NotificationManagerCompat.from(context).notify(notificationId, notificationBuilder.build())
  }

  /**
   * Called by Medical Officer (via in-app UI or notification action button)
   * to acknowledge the critical alarm, sign off with officer ID, and dismiss notification.
   */
  fun acknowledgeAlert(
    context: Context,
    alertId: String,
    officerId: String,
    notes: String = "Acknowledged by Onboard Flight Surgeon."
  ): Boolean {
    var found = false
    _activeCriticalAlerts.update { list ->
      list.map { alert ->
        if (alert.alertId == alertId) {
          found = true
          alert.copy(
            isAcknowledged = true,
            acknowledgedByOfficerId = officerId,
            acknowledgedTimestampMs = System.currentTimeMillis(),
            clinicalNotes = notes
          )
        } else alert
      }
    }

    if (found) {
      // Dismiss the notification from system tray
      NotificationManagerCompat.from(context).cancel(alertId.hashCode())

      // Record acknowledgement entry in shift timeline
      AstraRepository.logShiftEvent(
        ShiftTimelineEvent(
          id = "ACK-" + alertId,
          astronautId = officerId,
          astronautName = "Flight Surgeon Sign-off",
          shiftHour = 19.6,
          shiftTimeLabel = "T+19:36",
          utcTimestamp = "01:36 UTC",
          category = ShiftEventCategory.EMERGENCY,
          severity = AlertSeverity.ADVISORY,
          title = "Medical Officer Acknowledgement Recorded",
          description = "Alert $alertId verified and signed off by Medical Officer $officerId. Clinical protocol initiated: $notes",
          primaryMetric = "Verified by $officerId",
          secondaryMetric = "Alarm Cleared",
          status = "RESOLVED",
          iconType = "alert"
        )
      )
    }

    return found
  }

  fun getPendingAlerts(): List<CriticalRiskAlert> {
    return _activeCriticalAlerts.value.filter { !it.isAcknowledged }
  }
}
