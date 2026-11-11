package com.example.model

enum class ShiftEventCategory(val displayName: String) {
  BIOMETRIC("Biometric"),
  MEDICATION("Medication"),
  EMERGENCY("Emergency")
}

data class ShiftTimelineEvent(
  val id: String,
  val astronautId: String,
  val astronautName: String,
  val shiftHour: Double, // 0.0 to 24.0 representing the 24-hour mission work shift
  val shiftTimeLabel: String, // e.g. "T+02:15"
  val utcTimestamp: String, // e.g. "08:15 UTC"
  val category: ShiftEventCategory,
  val severity: AlertSeverity,
  val title: String,
  val description: String,
  val primaryMetric: String? = null,
  val secondaryMetric: String? = null,
  val actionDirective: String? = null,
  val status: String = "LOGGED",
  val iconType: String = "heart" // "heart", "pill", "alert", "sleep", "radiation", "pressure", "brain", "exercise"
)
