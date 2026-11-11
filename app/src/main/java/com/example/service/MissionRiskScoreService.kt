package com.example.service

import com.example.model.CabinEnvironment
import com.example.model.CrewMember
import com.example.model.RiskLevel

/**
 * Detailed breakdown of the unified Mission Risk Score aggregated across
 * health, radiation, fatigue, and spacecraft environmental metrics.
 */
data class MissionRiskScore(
  val compositeScore: Int,                 // 0 to 100
  val riskLevel: RiskLevel,                // LOW, MODERATE, HIGH
  val healthSubScore: Int,                 // 0 to 100
  val radiationSubScore: Int,              // 0 to 100
  val fatigueSubScore: Int,                // 0 to 100
  val environmentalModifier: Int,          // 0 to 50
  val primaryRiskDrivers: List<String>,    // Top physiological & mission drivers
  val clinicalActionDirective: String,     // Unambiguous NASA flight surgeon protocol
  val missionReadinessPercentage: Int      // Inverse readiness index (e.g. 96%)
)

/**
 * Service to aggregate health, radiation, and fatigue metrics into a unified 'Mission Risk Score'
 * with actionable spaceflight clinical directives adhering to NASA-STD-3001 and HRP standards.
 */
object MissionRiskScoreService {

  /**
   * Aggregates multi-system biometric, radiation, fatigue, and cabin metrics.
   */
  fun calculateMissionRisk(
    member: CrewMember,
    cabin: CabinEnvironment
  ): MissionRiskScore {
    val drivers = mutableListOf<String>()

    // 1. HEALTH METRICS AGGREGATION (0 - 100)
    var healthScore = 8
    val vitals = member.vitals

    // Heart Rate (Baseline 60-80 bpm)
    if (vitals.heartRateBpm > 100) {
      healthScore += 25
      drivers.add("Tachycardia detected (${vitals.heartRateBpm} bpm)")
    } else if (vitals.heartRateBpm < 50) {
      healthScore += 20
      drivers.add("Bradycardia detected (${vitals.heartRateBpm} bpm)")
    }

    // Oxygen Saturation (SpO2)
    if (vitals.spo2Percentage < 92) {
      healthScore += 45
      drivers.add("Critical hypoxemia (SpO₂ ${vitals.spo2Percentage}%)")
    } else if (vitals.spo2Percentage < 95) {
      healthScore += 25
      drivers.add("Mild peripheral desaturation (SpO₂ ${vitals.spo2Percentage}%)")
    }

    // Blood Pressure (Systolic / Diastolic)
    if (vitals.systolicBp > 140 || vitals.diastolicBp > 90) {
      healthScore += 25
      drivers.add("Elevated blood pressure (${vitals.systolicBp}/${vitals.diastolicBp} mmHg)")
    } else if (vitals.systolicBp < 90) {
      healthScore += 25
      drivers.add("Hypotension / cephalic shift risk (${vitals.systolicBp}/${vitals.diastolicBp} mmHg)")
    }

    // Autonomic Reserve / HRV
    if (vitals.hrvMs < 35) {
      healthScore += 15
      drivers.add("Depressed HRV (${vitals.hrvMs} ms autonomic stress)")
    }

    // Core Temperature
    if (vitals.coreTempCelsius > 38.0 || vitals.coreTempCelsius < 35.5) {
      healthScore += 25
      drivers.add("Abnormal core temperature (${vitals.coreTempCelsius}°C)")
    }

    // Musculoskeletal power
    if (member.boneMuscle.musclePowerRetentionPercent < 92.0) {
      healthScore += 15
      drivers.add("Musculoskeletal power loss (${member.boneMuscle.musclePowerRetentionPercent}%)")
    }

    val finalHealthScore = healthScore.coerceIn(5, 100)

    // 2. RADIATION METRICS AGGREGATION (0 - 100)
    var radScore = 6
    val rad = member.radiation

    // Ambient Spacecraft Flux (mSv/h)
    if (cabin.radiationRateMsvPerHour > 1.2) {
      radScore += 75
      drivers.add("Critical Solar Particle Event (SPE) flux (${cabin.radiationRateMsvPerHour} mSv/h)")
    } else if (cabin.radiationRateMsvPerHour > 0.5) {
      radScore += 45
      drivers.add("Elevated ambient radiation rate (${cabin.radiationRateMsvPerHour} mSv/h)")
    } else if (cabin.radiationRateMsvPerHour > 0.15) {
      radScore += 20
      drivers.add("Moderate radiation flux (${cabin.radiationRateMsvPerHour} mSv/h)")
    }

    // Personal Daily Ionizing Dose (mSv/d)
    if (rad.dailyDoseMsv > 2.0) {
      radScore += 40
      drivers.add("Severe daily ionizing dose (${rad.dailyDoseMsv} mSv/d)")
    } else if (rad.dailyDoseMsv > 0.8) {
      radScore += 20
      drivers.add("Elevated daily ionizing dose (${rad.dailyDoseMsv} mSv/d)")
    }

    // Cumulative Mission Exposure (% of career threshold)
    if (rad.careerThresholdPercentage > 25.0) {
      radScore += 30
      drivers.add("High cumulative career exposure (${rad.careerThresholdPercentage}%)")
    } else if (rad.careerThresholdPercentage > 10.0) {
      radScore += 15
    }

    // Unshielded cabin during solar flare
    if (!cabin.stormShieldEngaged && cabin.radiationRateMsvPerHour > 0.2) {
      radScore += 20
      drivers.add("Polyethylene storm shield not engaged during elevated flux")
    }

    val finalRadScore = radScore.coerceIn(5, 100)

    // 3. FATIGUE & COGNITIVE METRICS AGGREGATION (0 - 100)
    var fatigueScore = 7
    val sleep = member.sleep

    // Total Sleep Duration (last 24h)
    if (sleep.totalSleepHours < 4.5) {
      fatigueScore += 50
      drivers.add("Severe acute sleep deprivation (${sleep.totalSleepHours}h sleep)")
    } else if (sleep.totalSleepHours < 6.0) {
      fatigueScore += 25
      drivers.add("Cumulative sleep debt (${sleep.totalSleepHours}h sleep)")
    }

    // Sleep Quality & Architecture
    if (sleep.sleepQualityScore < 60) {
      fatigueScore += 20
      drivers.add("Fragmented sleep architecture (Quality ${sleep.sleepQualityScore}/100)")
    }

    // PVT (Psychomotor Vigilance Test) Reaction Latency
    if (member.lastPvtReactionTimeMs > 340) {
      fatigueScore += 45
      drivers.add("Critical PVT reaction lag (${member.lastPvtReactionTimeMs} ms, cognitive lapses)")
    } else if (member.lastPvtReactionTimeMs > 280) {
      fatigueScore += 20
      drivers.add("Mild PVT reaction delay (${member.lastPvtReactionTimeMs} ms)")
    }

    // Circadian Phase Desynchronosis
    if (Math.abs(sleep.circadianPhaseOffsetHours) > 2.5) {
      fatigueScore += 15
      drivers.add("Circadian phase offset (${sleep.circadianPhaseOffsetHours}h desynchronosis)")
    }

    // Behavioral Strain Index
    if (member.behavior.facialStrainIndex > 65 || member.mentalWellbeing.stressLevelIndex > 65) {
      fatigueScore += 15
      drivers.add("Elevated neurobehavioral strain index")
    }

    val finalFatigueScore = fatigueScore.coerceIn(5, 100)

    // 4. SPACECRAFT CABIN ENVIRONMENTAL MODIFIER
    var envModifier = 0
    if (cabin.co2Ppm > 4500) {
      envModifier += 35
      drivers.add("Critical cabin hypercapnia (CO₂ ${cabin.co2Ppm} ppm)")
    } else if (cabin.co2Ppm > 3000) {
      envModifier += 18
      drivers.add("Elevated cabin CO₂ (${cabin.co2Ppm} ppm)")
    }

    if (cabin.cabinPressureKpa < 92.0) {
      envModifier += 45
      drivers.add("Acute cabin depressurization (${cabin.cabinPressureKpa} kPa)")
    } else if (cabin.cabinPressureKpa < 98.0) {
      envModifier += 25
      drivers.add("Cabin pressure below nominal (${cabin.cabinPressureKpa} kPa)")
    }

    // 5. UNIFIED COMPOSITE MISSION RISK SCORE (0 - 100)
    // Multi-factor weighted aggregation: 35% Health, 35% Radiation, 30% Fatigue + Environmental Adders
    val baseComposite = (
      finalHealthScore * 0.35f +
      finalRadScore * 0.35f +
      finalFatigueScore * 0.30f +
      envModifier * 0.85f
    ).toInt()

    // Spaceflight Safety Rule: An acute life-critical single hazard (e.g. cabin depressurization or SPE flare)
    // elevates overall mission risk to reflect immediate danger
    val emergencyFloor = when {
      finalRadScore >= 70 -> finalRadScore
      envModifier >= 40 -> (envModifier + 35)
      finalHealthScore >= 70 -> finalHealthScore
      finalFatigueScore >= 75 -> finalFatigueScore
      else -> 0
    }

    val compositeScore = maxOf(baseComposite, emergencyFloor).coerceIn(5, 100)

    // Risk Level Classification: Low / Moderate / High
    val riskLevel = when {
      compositeScore >= 70 -> RiskLevel.HIGH
      compositeScore >= 35 -> RiskLevel.MODERATE
      else -> RiskLevel.LOW
    }

    // NASA Flight Surgeon Clinical Action Directives
    val actionDirective = when (riskLevel) {
      RiskLevel.HIGH -> when {
        finalRadScore >= 65 -> "MANDATORY: Evacuate crew to Node 1 Polyethylene Storm Shelter. Administer radioprotective antioxidants."
        envModifier >= 30 -> "EMERGENCY: Don flight masks with 100% O₂. Isolate depressurized or toxic module."
        finalFatigueScore >= 60 -> "RESTRICTED: Postpone EVA, docking, and robotics operations. Mandate 90-minute sleep recovery."
        else -> "CLINICAL ALERT: Initiate tele-consultation with Flight Surgeon. Deploy MedKit Alpha diagnostic triage."
      }
      RiskLevel.MODERATE -> when {
        finalFatigueScore >= 35 -> "ADVISORY: Circadian light adjustment (480nm LED). Schedule 30-second PVT re-test before critical shifts."
        finalRadScore >= 35 -> "CAUTION: Monitor dosimeter logs. Limit external spacewalk operations."
        finalHealthScore >= 35 -> "MONITOR: Increase fluid intake. Recheck SpO₂ and blood pressure in 2 hours."
        else -> "CAUTION: Environmental scrubbers active. Continue regular biometric tracking."
      }
      RiskLevel.LOW -> "NOMINAL: All biometric, radiation, and cognitive indicators within approved NASA-STD-3001 margins. Operational clearance granted."
    }

    if (drivers.isEmpty()) {
      drivers.add("All biometric telemetry nominal")
      drivers.add("Radiation exposure within deep-space baseline")
      drivers.add("Cognitive alertness & sleep architecture optimal")
    }

    val readiness = (100 - (compositeScore * 0.75f)).toInt().coerceIn(20, 99)

    return MissionRiskScore(
      compositeScore = compositeScore,
      riskLevel = riskLevel,
      healthSubScore = finalHealthScore,
      radiationSubScore = finalRadScore,
      fatigueSubScore = finalFatigueScore,
      environmentalModifier = envModifier,
      primaryRiskDrivers = drivers,
      clinicalActionDirective = actionDirective,
      missionReadinessPercentage = readiness
    )
  }
}
