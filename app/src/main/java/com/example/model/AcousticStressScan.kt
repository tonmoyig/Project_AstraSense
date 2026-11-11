package com.example.model

data class AcousticStressScanResult(
  val scanId: String,
  val astronautId: String,
  val astronautName: String,
  val timestampMs: Long = System.currentTimeMillis(),
  val pitchJitterPercent: Double,        // Nominal < 2.5%, elevated > 3.5%
  val amplitudeShimmerPercent: Double,   // Nominal < 3.0%, elevated > 4.5%
  val fundamentalFrequencyHz: Double,    // Baseline vocal cord pitch (e.g. 120-220 Hz)
  val speechCadenceWpm: Int,             // Words per minute (nominal 130-160 WPM, fatigue < 115 WPM)
  val articulationPauseRatio: Double,    // Ratio of silence to speech (nominal < 0.25, fatigue > 0.38)
  val cognitiveWorkloadIndex: Int,       // 0 - 100
  val compositeStressScore: Int,         // 0 - 100
  val statusLabel: String,               // "NOMINAL • RESTED", "MILD WORKLOAD", "ELEVATED STRESS", "HIGH COGNITIVE FATIGUE"
  val clinicalInsight: String,
  val frequencySpectrogram: List<Float>  // 16 to 24 audio spectral bar magnitudes (0f to 1f)
)

data class RadarBiosensorTarget(
  val id: String,
  val astronautId: String,
  val astronautName: String,
  val moduleLocation: String,
  val angleDegrees: Float,       // 0 to 360 degrees
  val distanceFraction: Float,   // 0.2 to 0.85 from center (based on module and anomaly)
  val severity: AlertSeverity,
  val primaryMetric: String,     // e.g. "HR 86 bpm • SpO₂ 97%"
  val acousticStressIndex: Int,  // 0 - 100
  val isEVA: Boolean = false
)
