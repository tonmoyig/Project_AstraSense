package com.example.data.cloudsql

import com.example.model.RiskLevel

/**
 * Cloud SQL Relational Model Representations for Android Client Offline Sync.
 * Mirrors the PostgreSQL DDL schema in cloudsql/schema.sql.
 */

data class CloudSqlAstronaut(
  val id: String,
  val name: String,
  val missionRole: String,
  val email: String,
  val clearanceStatus: String,
  val encryptedAccessKey: String,
  val department: String = "FLIGHT_OPERATIONS",
  val assignedVessel: String = "NSS ASTRA-V"
)

data class CloudSqlBiometricTelemetry(
  val astronautId: String,
  val timestamp: String,
  val heartRate: Int,
  val bloodPressure: String,
  val oxygenSaturation: Int,
  val coreTemp: Double,
  val respirationRate: Int,
  val radiationDoseRate: Double,
  val riskScore: Int,
  val hrvMs: Int = 72,
  val syncOrigin: String = "OFFLINE_SYNC_QUEUE"
)

data class CloudSqlMissionLog(
  val eventType: String,
  val severity: String,
  val description: String,
  val timestamp: String,
  val status: String = "ACTIVE",
  val affectedAstronautId: String? = null,
  val metadataJson: String = "{}"
)

data class CloudSqlSystemOverride(
  val timestamp: String,
  val cabinPressureKpa: Double,
  val co2Ppm: Double,
  val radiationFluxMsv: Double,
  val oxygenPct: Double,
  val appliedBy: String? = null,
  val reason: String = "Manual Flight Surgeon Override",
  val stormShieldEngaged: Boolean = false,
  val scrubbersForcedDuty: Boolean = false
)

enum class CloudSqlSyncStatus(val label: String) {
  ONLINE_SYNCED("CLOUD SQL: SYNCHRONIZED"),
  OFFLINE_RF_BLACKOUT("RF BLACKOUT: QUEUED LOCALLY"),
  SYNCING_BATCH("SYNCING TO CLOUD SQL..."),
  SYNC_ERROR("TRANSMISSION ERROR: RETRYING")
}

data class QueuedSyncRecord(
  val id: Long,
  val recordType: String,
  val payloadJson: String,
  val timestampMs: Long,
  val retryCount: Int,
  val status: String
)
