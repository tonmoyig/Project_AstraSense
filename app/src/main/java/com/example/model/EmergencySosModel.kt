package com.example.model

import java.util.UUID

/**
 * High-frequency telemetry sample logged during emergency SOS broadcast or high-duty cycles.
 */
data class HighFrequencyTelemetrySample(
  val id: String = UUID.randomUUID().toString(),
  val timestamp: String,
  val timestampMs: Long,
  val heartRate: Int,
  val systolicBp: Int,
  val diastolicBp: Int,
  val spO2: Int,
  val coreTemp: Double,
  val hrvMs: Int,
  val respirationRate: Int,
  val cabinPressureKpa: Double,
  val radiationRateMsvPerHour: Double,
  val isSosTriggered: Boolean = true
)

/**
 * Control mode for Mission Risk Calculation.
 */
enum class RiskCalculationMode {
  AUTOMATED,
  MANUAL_OVERRIDE
}

/**
 * Comprehensive system configuration holding manual overrides, feature toggles,
 * and operational telemetry rates for all application subsystems.
 */
data class SystemFeatureConfig(
  val layer1HardwareRulesEnabled: Boolean = true,
  val layer2AiDirectivesEnabled: Boolean = true,
  val biometricRadarEnabled: Boolean = true,
  val acousticPhonationScanEnabled: Boolean = true,
  val offlineAutonomyForced: Boolean = false,
  val highFrequencyLoggingEnabled: Boolean = false,
  val autoBroadcastSosOnCriticalRisk: Boolean = true,
  val telemetrySampleRateHz: Double = 0.1,
  val manualRiskOverride: RiskLevel? = null,
  val manualRiskScoreValue: Int? = null,
  val pushNotificationsEnabled: Boolean = true,
  val criticalRiskNotificationThreshold: Int = 70,
  val notificationSoundEnabled: Boolean = true,
  val notificationVibrationEnabled: Boolean = true,
  val mandatoryMedicalAckEnabled: Boolean = true,
  val aiVoiceTtsEnabled: Boolean = true,
  val aiStrictEmergencyMode: Boolean = true,
  val radarSweepSpeedMultiplier: Float = 1.0f,
  val manualCommsDelayOverrideSeconds: Int? = null
)
