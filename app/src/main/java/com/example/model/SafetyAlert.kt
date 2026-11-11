package com.example.model

enum class AlertSeverity {
  CRITICAL,  // Red flash, immediate alarm
  WARNING,   // Amber caution
  ADVISORY   // Cyan/Blue operational notice
}

val AlertSeverity.displayName: String
  get() = when (this) {
    AlertSeverity.CRITICAL -> "CRITICAL THREAT"
    AlertSeverity.WARNING -> "WARNING ALERT"
    AlertSeverity.ADVISORY -> "ADVISORY NOTICE"
  }

enum class AlertLayer {
  LAYER_1_HARDWARE_RULES, // Instant sensor thresholds
  LAYER_2_AI_ASSISTANT    // Predictive pattern correlation
}

data class SafetyAlert(
  val id: String,
  val title: String,
  val description: String,
  val actionDirective: String, // Clear action instead of complicated charts
  val severity: AlertSeverity,
  val layer: AlertLayer,
  val targetCrewMemberId: String? = null, // null means ship-wide
  val timestamp: String,
  val isAcknowledged: Boolean = false
)

data class CabinEnvironment(
  val cabinPressureKpa: Double, // nominal 101.3 kPa, critical < 95.0 kPa
  val co2Ppm: Int,               // nominal < 3000, caution 3000-5000, emergency > 5000
  val o2Percentage: Double,       // nominal 20.9%
  val temperatureCelsius: Double,// nominal 21.5°C
  val hullIntegrityPercent: Double, // 100% nominal
  val radiationRateMsvPerHour: Double, // nominal 0.05 mSv/h, critical > 1.2 mSv/h
  val scrubbersActive: Boolean = true,
  val stormShieldEngaged: Boolean = false
)

data class PvtTestResult(
  val testId: String,
  val astronautId: String,
  val timestamp: Long,
  val meanReactionTimeMs: Int,
  val fastestReactionMs: Int,
  val slowestReactionMs: Int,
  val lapsesCount: Int, // reaction > 500ms
  val totalTrials: Int,
  val alertnessScore: Int, // 0-100
  val readinessStatus: String // "FULL FLIGHT DUTY", "REST RECOMMENDED", "EVA RESTRICTED"
)

data class CrewMessage(
  val id: String,
  val senderName: String,
  val senderRole: String,
  val recipientId: String, // "ALL" or specific crew member ID
  val content: String,
  val timestamp: String,
  val isHighPriority: Boolean = false
)
