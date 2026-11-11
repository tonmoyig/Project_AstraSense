package com.example.model

enum class UserRole {
  ADMIN,
  CREW
}

enum class RiskLevel(val label: String) {
  LOW("LOW RISK"),
  MODERATE("MODERATE RISK"),
  HIGH("HIGH RISK")
}

data class VitalSigns(
  val heartRateBpm: Int,
  val systolicBp: Int,
  val diastolicBp: Int,
  val spo2Percentage: Int,
  val coreTempCelsius: Double,
  val respirationRateBpm: Int,
  val hrvMs: Int // Heart rate variability
)

data class RadiationMetrics(
  val dailyDoseMsv: Double,
  val missionCumulativeMsv: Double,
  val careerThresholdPercentage: Double,
  val dosimeterStatus: String = "ACTIVE • CALIBRATED",
  val stormShelterRequired: Boolean = false
)

data class SleepAndCircadian(
  val totalSleepHours: Double,
  val deepSleepPercent: Int,
  val remSleepPercent: Int,
  val sleepQualityScore: Int, // 0-100
  val circadianPhaseOffsetHours: Double, // +/- hours from nominal ship schedule
  val blueLightVisorRecommendation: String
)

data class BehaviorAndMovement(
  val postureScore: Int, // 0-100 (microgravity spinal posture)
  val movementSpeedMps: Double, // meters per second in zero-g translation
  val facialStrainIndex: Int, // 0-100 (camera-based fatigue indicator)
  val socialParticipationRate: Int, // percentage of group meals/briefings
  val voiceAcousticStress: Int // 0-100
)

data class BoneAndMuscleHealth(
  val aredDailyForceKn: Double, // Advanced Resistive Exercise Device load
  val t2TreadmillMinutes: Int,
  val musclePowerRetentionPercent: Double, // compared to pre-mission baseline
  val jointRangeOfMotionPercent: Double,
  val estimatedBmdRetentionPercent: Double
)

data class MentalWellbeing(
  val stressLevelIndex: Int, // 0-100
  val lonelinessIndicator: Int, // 0-100
  val sensoryOverloadStatus: String, // "OPTIMAL", "MILD ELEVATION", "REST REQUIRED"
  val lastCheckInNote: String,
  val suggestedIntervention: String
)

data class CrewMember(
  val id: String, // e.g., "NASA-AS-701"
  val username: String,
  val fullName: String,
  val email: String,
  val role: UserRole,
  val gender: String,
  val age: Int,
  val missionSpecialty: String,
  val currentModuleLocation: String,
  val currentActivity: String,
  val riskLevel: RiskLevel,
  val vitals: VitalSigns,
  val radiation: RadiationMetrics,
  val sleep: SleepAndCircadian,
  val behavior: BehaviorAndMovement,
  val boneMuscle: BoneAndMuscleHealth,
  val mentalWellbeing: MentalWellbeing,
  val lastPvtReactionTimeMs: Int,
  val isOnlineTelemetry: Boolean = true
)
