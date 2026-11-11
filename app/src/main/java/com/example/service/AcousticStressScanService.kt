package com.example.service

import com.example.model.AcousticStressScanResult
import com.example.model.AlertSeverity
import com.example.model.CrewMember
import com.example.model.RadarBiosensorTarget
import com.example.model.RiskLevel
import java.util.UUID
import kotlin.random.Random

object AcousticStressScanService {

  fun analyzeCrewVoiceAcoustics(member: CrewMember): AcousticStressScanResult {
    val baseStress = member.behavior.voiceAcousticStress.toDouble()
    val pvtFactor = (member.lastPvtReactionTimeMs - 200).coerceAtLeast(0) / 4.0
    val sleepDeficit = (8.0 - member.sleep.totalSleepHours).coerceAtLeast(0.0) * 5.0
    val hrFactor = (member.vitals.heartRateBpm - 70).coerceAtLeast(0) * 0.4

    val compositeScore = (baseStress * 0.70 + pvtFactor * 0.15 + sleepDeficit * 0.15 + hrFactor * 0.10)
      .toInt().coerceIn(8, 95)

    // Acoustic parameters derived according to NASA behavioral acoustics literature
    val jitter = (1.2 + (compositeScore / 100.0) * 2.8).coerceIn(1.1, 4.8)
    val shimmer = (1.8 + (compositeScore / 100.0) * 3.4).coerceIn(1.5, 5.8)
    val baselinePitch = if (member.gender.equals("Female", ignoreCase = true)) 212.0 else 126.0
    val fundamentalPitch = baselinePitch + (compositeScore * 0.22) // High stress elevates fundamental frequency

    // Cognitive workload slows cadence & increases pause ratio
    val cadence = (152 - (compositeScore * 0.45).toInt()).coerceIn(95, 165)
    val pauseRatio = (0.18 + (compositeScore / 100.0) * 0.26).coerceIn(0.15, 0.48)
    val cognitiveWorkload = (compositeScore * 1.05).toInt().coerceIn(10, 98)

    val status = when {
      compositeScore >= 65 -> "HIGH COGNITIVE FATIGUE"
      compositeScore >= 45 -> "ELEVATED STRESS"
      compositeScore >= 25 -> "MILD WORKLOAD"
      else -> "NOMINAL • RESTED"
    }

    val clinicalInsight = when {
      compositeScore >= 65 -> "Acoustic micro-tremor and severe cadence deceleration (-${152 - cadence} WPM) indicate acute central nervous system fatigue. Immediate cognitive stand-down recommended."
      compositeScore >= 45 -> "Moderate pitch jitter elevation (${String.format("%.1f", jitter)}%) and increased hesitation pauses. Correlated with post-EVA exertion or circadian shift."
      compositeScore >= 25 -> "Slight formant shift consistent with normal microgravity cephalad fluid redistribution. Phonation stable."
      else -> "Vocal harmonic-to-noise ratio and fundamental frequency (${String.format("%.0f", fundamentalPitch)} Hz) fully nominal. Speech cadence fluid."
    }

    // Generate 20 spectral frequency magnitudes
    val random = Random(member.id.hashCode() + compositeScore)
    val spectrum = List(20) { index ->
      val baseCurve = 1.0f - (index / 24.0f)
      val variance = random.nextFloat() * 0.35f
      ((baseCurve + variance) * (compositeScore / 100f * 0.5f + 0.5f)).coerceIn(0.1f, 0.98f)
    }

    return AcousticStressScanResult(
      scanId = "VOCAL-" + UUID.randomUUID().toString().take(6).uppercase(),
      astronautId = member.id,
      astronautName = member.fullName,
      pitchJitterPercent = jitter,
      amplitudeShimmerPercent = shimmer,
      fundamentalFrequencyHz = fundamentalPitch,
      speechCadenceWpm = cadence,
      articulationPauseRatio = pauseRatio,
      cognitiveWorkloadIndex = cognitiveWorkload,
      compositeStressScore = compositeScore,
      statusLabel = status,
      clinicalInsight = clinicalInsight,
      frequencySpectrogram = spectrum
    )
  }

  fun generateRadarTargets(crew: List<CrewMember>): List<RadarBiosensorTarget> {
    return crew.mapIndexed { index, member ->
      // Spread evenly around the 360-degree radar circle
      val baseAngle = when (member.id) {
        "NASA-AS-701" -> 45f   // Command Bridge
        "NASA-AS-702" -> 140f  // Airlock Quest (EVA)
        "NASA-AS-703" -> 220f  // Node 3 Life Support
        "NASA-AS-704" -> 315f  // Destiny / Kibo Module
        else -> (index * 90f + 30f) % 360f
      }

      val distance = when (member.riskLevel) {
        RiskLevel.HIGH -> 0.82f // Outer perimeter for high risk/critical
        RiskLevel.MODERATE -> 0.58f // Mid ring
        RiskLevel.LOW -> 0.35f // Inner core nominal zone
      }

      val severity = when (member.riskLevel) {
        RiskLevel.HIGH -> AlertSeverity.CRITICAL
        RiskLevel.MODERATE -> AlertSeverity.WARNING
        RiskLevel.LOW -> AlertSeverity.ADVISORY
      }

      RadarBiosensorTarget(
        id = "RADAR-BLIP-${member.id}",
        astronautId = member.id,
        astronautName = member.fullName,
        moduleLocation = member.currentModuleLocation,
        angleDegrees = baseAngle,
        distanceFraction = distance,
        severity = severity,
        primaryMetric = "HR ${member.vitals.heartRateBpm} bpm • SpO₂ ${member.vitals.spo2Percentage}%",
        acousticStressIndex = member.behavior.voiceAcousticStress,
        isEVA = member.currentModuleLocation.contains("Airlock", ignoreCase = true) || member.currentActivity.contains("EVA", ignoreCase = true)
      )
    }
  }
}
