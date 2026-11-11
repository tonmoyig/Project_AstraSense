package com.example.service

import com.example.model.AlertSeverity
import com.example.model.CabinEnvironment
import com.example.model.CrewMember
import com.example.model.RiskLevel

enum class StressorType(val label: String) {
  CIRCADIAN("Circadian Rhythm"),
  ENVIRONMENTAL("Cabin Environment"),
  COGNITIVE("Cognitive & Fatigue"),
  MUSCULOSKELETAL("Musculoskeletal"),
  RADIATION("Radiation & Space Weather")
}

data class MissionStressorPattern(
  val id: String,
  val stressorType: StressorType,
  val title: String,
  val severity: AlertSeverity,
  val affectedCrewCount: Int,
  val totalCrewCount: Int,
  val evidenceSummary: String,
  val correlationFactor: String,
  val recommendedFleetCountermeasure: String
)

data class AggregateHealthSummary(
  val selectedCrewCount: Int,
  val averageRiskScore: Double,
  val maxRiskScore: Int,
  val highestRiskMemberName: String,
  val lowRiskCount: Int,
  val moderateRiskCount: Int,
  val highRiskCount: Int,
  val averageHeartRateBpm: Double,
  val averageHrvMs: Double,
  val averageSpo2Percent: Double,
  val averageDailyRadiationMsv: Double,
  val maxDailyRadiationMsv: Double,
  val averageSleepHours: Double,
  val averageSleepQuality: Double,
  val averagePvtReactionMs: Double,
  val totalPvtLapses: Int,
  val averageStressIndex: Double,
  val averageAredForceKn: Double,
  val averageBoneRetentionPercent: Double,
  val detectedStressors: List<MissionStressorPattern>
)

object AggregateCrewHealthService {

  fun computeAggregateSummary(
    crew: List<CrewMember>,
    cabin: CabinEnvironment
  ): AggregateHealthSummary {
    if (crew.isEmpty()) {
      return AggregateHealthSummary(
        selectedCrewCount = 0,
        averageRiskScore = 0.0,
        maxRiskScore = 0,
        highestRiskMemberName = "N/A",
        lowRiskCount = 0,
        moderateRiskCount = 0,
        highRiskCount = 0,
        averageHeartRateBpm = 0.0,
        averageHrvMs = 0.0,
        averageSpo2Percent = 0.0,
        averageDailyRadiationMsv = 0.0,
        maxDailyRadiationMsv = 0.0,
        averageSleepHours = 0.0,
        averageSleepQuality = 0.0,
        averagePvtReactionMs = 0.0,
        totalPvtLapses = 0,
        averageStressIndex = 0.0,
        averageAredForceKn = 0.0,
        averageBoneRetentionPercent = 0.0,
        detectedStressors = emptyList()
      )
    }

    val riskScores = crew.map { member ->
      val risk = MissionRiskScoreService.calculateMissionRisk(member, cabin)
      member to risk
    }

    val avgRisk = riskScores.map { it.second.compositeScore }.average()
    val maxRiskPair = riskScores.maxByOrNull { it.second.compositeScore }
    val maxRisk = maxRiskPair?.second?.compositeScore ?: 0
    val highestRiskName = maxRiskPair?.first?.fullName ?: "N/A"

    val lowCount = riskScores.count { it.second.riskLevel == RiskLevel.LOW }
    val modCount = riskScores.count { it.second.riskLevel == RiskLevel.MODERATE }
    val highCount = riskScores.count { it.second.riskLevel == RiskLevel.HIGH }

    val avgHr = crew.map { it.vitals.heartRateBpm }.average()
    val avgHrv = crew.map { it.vitals.hrvMs }.average()
    val avgSpo2 = crew.map { it.vitals.spo2Percentage }.average()
    val avgRad = crew.map { it.radiation.dailyDoseMsv }.average()
    val maxRad = crew.maxOfOrNull { it.radiation.dailyDoseMsv } ?: 0.0
    val avgSleep = crew.map { it.sleep.totalSleepHours }.average()
    val avgSleepQual = crew.map { it.sleep.sleepQualityScore }.average()
    val avgPvt = crew.map { it.lastPvtReactionTimeMs }.average()
    val totalLapses = crew.count { it.lastPvtReactionTimeMs > 250 }
    val avgStress = crew.map { it.mentalWellbeing.stressLevelIndex }.average()
    val avgAred = crew.map { it.boneMuscle.aredDailyForceKn }.average()
    val avgBone = crew.map { it.boneMuscle.estimatedBmdRetentionPercent }.average()

    val stressors = detectMissionStressors(crew, cabin, avgSleep, avgPvt, avgRad, avgHr)

    return AggregateHealthSummary(
      selectedCrewCount = crew.size,
      averageRiskScore = avgRisk,
      maxRiskScore = maxRisk,
      highestRiskMemberName = highestRiskName,
      lowRiskCount = lowCount,
      moderateRiskCount = modCount,
      highRiskCount = highCount,
      averageHeartRateBpm = avgHr,
      averageHrvMs = avgHrv,
      averageSpo2Percent = avgSpo2,
      averageDailyRadiationMsv = avgRad,
      maxDailyRadiationMsv = maxRad,
      averageSleepHours = avgSleep,
      averageSleepQuality = avgSleepQual,
      averagePvtReactionMs = avgPvt,
      totalPvtLapses = totalLapses,
      averageStressIndex = avgStress,
      averageAredForceKn = avgAred,
      averageBoneRetentionPercent = avgBone,
      detectedStressors = stressors
    )
  }

  private fun detectMissionStressors(
    crew: List<CrewMember>,
    cabin: CabinEnvironment,
    avgSleep: Double,
    avgPvt: Double,
    avgRad: Double,
    avgHr: Double
  ): List<MissionStressorPattern> {
    val patterns = mutableListOf<MissionStressorPattern>()

    // Pattern 1: Circadian Shift & Sleep Debt
    val sleepDeprived = crew.filter { it.sleep.totalSleepHours < 6.5 || kotlin.math.abs(it.sleep.circadianPhaseOffsetHours) > 1.0 }
    if (sleepDeprived.isNotEmpty()) {
      patterns.add(
        MissionStressorPattern(
          id = "STR-CIRCADIAN-01",
          stressorType = StressorType.CIRCADIAN,
          title = "Circadian Desynchrony & Cumulative Sleep Debt",
          severity = if (sleepDeprived.size >= crew.size / 2) AlertSeverity.WARNING else AlertSeverity.ADVISORY,
          affectedCrewCount = sleepDeprived.size,
          totalCrewCount = crew.size,
          evidenceSummary = "${sleepDeprived.size} of ${crew.size} astronauts exhibiting < 6.5h sleep or > 1.0h circadian phase offset. Fleet avg sleep is ${String.format("%.1f", avgSleep)}h.",
          correlationFactor = "High post-EVA workload and schedule transition between orbital work shifts.",
          recommendedFleetCountermeasure = "Advance crew rest cycle by 45 minutes. Initiate 490nm amber visor protocol 60 min before sleep pod ingress."
        )
      )
    }

    // Pattern 2: Environmental Hypercapnia & Respiratory Strain
    if (cabin.co2Ppm > 3000 || avgHr > 78.0) {
      patterns.add(
        MissionStressorPattern(
          id = "STR-ENV-CO2",
          stressorType = StressorType.ENVIRONMENTAL,
          title = "Cabin CO₂ Elevation & Autonomic Strain",
          severity = if (cabin.co2Ppm > 4500) AlertSeverity.CRITICAL else AlertSeverity.WARNING,
          affectedCrewCount = crew.size,
          totalCrewCount = crew.size,
          evidenceSummary = "Cabin CO₂ reading ${cabin.co2Ppm} ppm (Nominal: < 3000 ppm). Elevated resting HR avg (${String.format("%.0f", avgHr)} bpm).",
          correlationFactor = "Scrubber filter resistance during concurrent crew exercise cycles in Node 3.",
          recommendedFleetCountermeasure = "Activate secondary LiOH scrubber bed. Stagger aerobic workout schedules to distribute metabolic CO₂ production."
        )
      )
    }

    // Pattern 3: Cognitive Reaction Drift & Vigilance Lapses
    val reactionLagged = crew.filter { it.lastPvtReactionTimeMs > 250 }
    if (reactionLagged.isNotEmpty()) {
      patterns.add(
        MissionStressorPattern(
          id = "STR-PVT-LAG",
          stressorType = StressorType.COGNITIVE,
          title = "Psychomotor Vigilance Reaction Time Drift",
          severity = if (reactionLagged.any { it.lastPvtReactionTimeMs > 300 }) AlertSeverity.WARNING else AlertSeverity.ADVISORY,
          affectedCrewCount = reactionLagged.size,
          totalCrewCount = crew.size,
          evidenceSummary = "${reactionLagged.size} astronauts showing PVT reaction latency above 250ms (Fleet avg: ${String.format("%.0f", avgPvt)}ms).",
          correlationFactor = "Correlated with cumulative sleep deficit and continuous robotic arm/telemetry operation.",
          recommendedFleetCountermeasure = "Reassign dual-operator verification for critical burns and Canadarm3 robotics. Mandate 30-minute cognitive sensory rest."
        )
      )
    }

    // Pattern 4: Space Weather & Hull Radiation Gradient
    val highRadMembers = crew.filter { it.radiation.dailyDoseMsv > 0.60 }
    if (highRadMembers.isNotEmpty() || cabin.radiationRateMsvPerHour > 0.08) {
      patterns.add(
        MissionStressorPattern(
          id = "STR-RAD-EXPOSURE",
          stressorType = StressorType.RADIATION,
          title = "Solar Energetic Particle Flux & Outboard Rack Exposure",
          severity = if (cabin.radiationRateMsvPerHour > 1.0) AlertSeverity.CRITICAL else AlertSeverity.WARNING,
          affectedCrewCount = highRadMembers.size,
          totalCrewCount = crew.size,
          evidenceSummary = "${highRadMembers.size} astronauts received > 0.60 mSv/day (Max: ${String.format("%.2f", crew.maxOf { it.radiation.dailyDoseMsv })} mSv). Cabin flux: ${cabin.radiationRateMsvPerHour} mSv/h.",
          correlationFactor = "Unshielded science duty in Columbus / Quest airlock during elevated solar activity.",
          recommendedFleetCountermeasure = "Rotate crew assignments to heavily shielded Node 1 Water Wall. Enforce career threshold pacing."
        )
      )
    }

    // Pattern 5: Musculoskeletal Microgravity Conditioning Asymmetry
    val underloaded = crew.filter { it.boneMuscle.aredDailyForceKn < 4.5 || it.boneMuscle.musclePowerRetentionPercent < 95.0 }
    if (underloaded.isNotEmpty()) {
      patterns.add(
        MissionStressorPattern(
          id = "STR-MUSCLE-BMD",
          stressorType = StressorType.MUSCULOSKELETAL,
          title = "Musculoskeletal Conditioning & Resistive Load Deficit",
          severity = AlertSeverity.ADVISORY,
          affectedCrewCount = underloaded.size,
          totalCrewCount = crew.size,
          evidenceSummary = "${underloaded.size} astronauts showing < 4.5 kN ARED loading or < 95% muscle power retention.",
          correlationFactor = "Joint soreness and missed workout intervals following long-duration EVA sorties.",
          recommendedFleetCountermeasure = "Calibrate ARED eccentric resistance curve and prescribe 500IU Vitamin D3 + calcium citrate supplementation."
        )
      )
    }

    return patterns
  }
}
