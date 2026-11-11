/**
 * AstraSense Cloud SQL Query Layer
 * Optimized SQL operations for:
 * 1. Crew Authentication & Clearance Verification
 * 2. High-Throughput Batch Biometric Ingestion
 * 3. Fleet Health Risk Aggregations & Crew Telemetry Trends
 * 4. Space Weather / NASA DONKI & Hazard Warning Queries
 * 5. Spacecraft Cabin Environmental Overrides
 */

import { query } from './db';

// ------------------------------------------------------------------------------
// Type Definitions
// ------------------------------------------------------------------------------

export interface CrewProfile {
  id: string;
  name: string;
  mission_role: string;
  email: string;
  clearance_status: string;
  department: string;
  assigned_vessel: string;
  created_at: Date;
}

export interface BiometricTelemetryInput {
  astronaut_id: string;
  timestamp: string | Date;
  heart_rate: number;
  blood_pressure: string;
  oxygen_saturation: number;
  core_temp: number;
  respiration_rate: number;
  radiation_dose_rate: number;
  risk_score: number;
  hrv_ms?: number;
  sync_origin?: string;
}

export interface FleetRiskSummary {
  fleet_size: number;
  mean_fleet_risk: number;
  high_risk_crew_count: number;
  critical_hazards_count: number;
  latest_telemetry_timestamp: Date;
  crew_risk_rankings: Array<{
    astronaut_id: string;
    name: string;
    mission_role: string;
    latest_risk_score: number;
    heart_rate: number;
    oxygen_saturation: number;
    radiation_dose_rate: number;
    last_reported: Date;
  }>;
}

export interface TelemetryTrendResult {
  astronaut_id: string;
  name: string;
  time_window_hours: number;
  sample_count: number;
  stats: {
    avg_heart_rate: number;
    min_heart_rate: number;
    max_heart_rate: number;
    avg_spo2: number;
    min_spo2: number;
    avg_core_temp: number;
    cumulative_radiation_dose_msv: number;
    avg_risk_score: number;
  };
  time_series: Array<{
    timestamp: Date;
    heart_rate: number;
    blood_pressure: string;
    oxygen_saturation: number;
    core_temp: number;
    risk_score: number;
  }>;
}

export interface MissionLogRecord {
  id: number;
  event_type: string;
  severity: string;
  description: string;
  timestamp: Date;
  status: string;
  affected_astronaut_id?: string;
  metadata: Record<string, any>;
}

export interface SystemOverrideInput {
  cabin_pressure: number;
  co2_level: number;
  radiation_flux: number;
  oxygen_pct: number;
  applied_by?: string;
  reason?: string;
  storm_shield_engaged?: boolean;
  scrubbers_forced_duty?: boolean;
}

// ------------------------------------------------------------------------------
// 1. Crew Authentication & Clearance Verification
// ------------------------------------------------------------------------------

/**
 * Authenticates an astronaut or flight surgeon by identifier (Email or Astronaut ID)
 * and verifies access key using PostgreSQL pgcrypto crypt() comparison.
 */
export async function authenticateCrewMember(
  identifier: string,
  rawAccessKey: string
): Promise<CrewProfile | null> {
  const sql = `
    SELECT
      id,
      name,
      mission_role,
      email,
      clearance_status,
      department,
      assigned_vessel,
      created_at
    FROM astronauts
    WHERE (email = $1 OR id = $1)
      AND encrypted_access_key = crypt($2, encrypted_access_key)
    LIMIT 1;
  `;

  const result = await query<CrewProfile>(sql, [identifier.trim(), rawAccessKey]);
  return result.rows.length > 0 ? result.rows[0] : null;
}

// ------------------------------------------------------------------------------
// 2. High-Throughput Batch Biometric Ingestion
// ------------------------------------------------------------------------------

/**
 * Inserts a batch of wearable Bio-Harness telemetry samples in a single SQL roundtrip
 * using dynamic multi-row VALUES concatenation for ultra-low latency.
 */
export async function insertBatchBiometricTelemetry(
  records: BiometricTelemetryInput[]
): Promise<number> {
  if (!records || records.length === 0) return 0;

  const valuePlaceholders: string[] = [];
  const params: any[] = [];
  let paramIdx = 1;

  for (const r of records) {
    valuePlaceholders.push(`(
      $${paramIdx++}, $${paramIdx++}, $${paramIdx++}, $${paramIdx++},
      $${paramIdx++}, $${paramIdx++}, $${paramIdx++}, $${paramIdx++},
      $${paramIdx++}, $${paramIdx++}, $${paramIdx++}
    )`);

    params.push(
      r.astronaut_id,
      new Date(r.timestamp),
      r.heart_rate,
      r.blood_pressure,
      r.oxygen_saturation,
      r.core_temp,
      r.respiration_rate,
      r.radiation_dose_rate,
      r.risk_score,
      r.hrv_ms || 72,
      r.sync_origin || 'OFFLINE_SYNC_QUEUE'
    );
  }

  const sql = `
    INSERT INTO biometric_telemetry (
      astronaut_id,
      timestamp,
      heart_rate,
      blood_pressure,
      oxygen_saturation,
      core_temp,
      respiration_rate,
      radiation_dose_rate,
      risk_score,
      hrv_ms,
      sync_origin
    )
    VALUES ${valuePlaceholders.join(', ')}
    ON CONFLICT DO NOTHING;
  `;

  const result = await query(sql, params);
  return result.rowCount ?? records.length;
}

// ------------------------------------------------------------------------------
// 3. Fleet Health Risk Aggregations & Crew Telemetry Trends
// ------------------------------------------------------------------------------

/**
 * Calculates unified fleet-wide mission risk metrics and current crew standings
 * for the Flight Surgeon command dashboard.
 */
export async function getFleetHealthRiskAggregation(): Promise<FleetRiskSummary> {
  // Query latest biometric record per astronaut using DISTINCT ON
  const sqlRankings = `
    SELECT DISTINCT ON (b.astronaut_id)
      b.astronaut_id,
      a.name,
      a.mission_role,
      b.risk_score as latest_risk_score,
      b.heart_rate,
      b.oxygen_saturation,
      b.radiation_dose_rate,
      b.timestamp as last_reported
    FROM biometric_telemetry b
    JOIN astronauts a ON b.astronaut_id = a.id
    ORDER BY b.astronaut_id, b.timestamp DESC;
  `;

  const rankingsRes = await query(sqlRankings);
  const rankings = rankingsRes.rows;

  const sqlHazards = `
    SELECT COUNT(*) as active_hazards
    FROM mission_logs
    WHERE status = 'ACTIVE' AND severity IN ('HIGH', 'CRITICAL');
  `;
  const hazardsRes = await query(sqlHazards);
  const activeHazards = parseInt(hazardsRes.rows[0]?.active_hazards || '0', 10);

  const fleetSize = rankings.length;
  const totalRisk = rankings.reduce((acc, curr) => acc + curr.latest_risk_score, 0);
  const meanFleetRisk = fleetSize > 0 ? Math.round(totalRisk / fleetSize) : 0;
  const highRiskCount = rankings.filter(r => r.latest_risk_score >= 60).length;

  // Sort descending by risk score
  rankings.sort((a, b) => b.latest_risk_score - a.latest_risk_score);

  return {
    fleet_size: fleetSize,
    mean_fleet_risk: meanFleetRisk,
    high_risk_crew_count: highRiskCount,
    critical_hazards_count: activeHazards,
    latest_telemetry_timestamp: rankings[0]?.last_reported || new Date(),
    crew_risk_rankings: rankings,
  };
}

/**
 * Fetches rolling biometric trend statistics and timeline data for an individual astronaut.
 */
export async function getCrewTelemetryTrends(
  astronautId: string,
  hours: number = 24
): Promise<TelemetryTrendResult> {
  const sqlStats = `
    SELECT
      a.name,
      COUNT(b.id) as sample_count,
      COALESCE(ROUND(AVG(b.heart_rate)), 0) as avg_heart_rate,
      COALESCE(MIN(b.heart_rate), 0) as min_heart_rate,
      COALESCE(MAX(b.heart_rate), 0) as max_heart_rate,
      COALESCE(ROUND(AVG(b.oxygen_saturation)), 0) as avg_spo2,
      COALESCE(MIN(b.oxygen_saturation), 0) as min_spo2,
      COALESCE(ROUND(AVG(b.core_temp), 2), 0) as avg_core_temp,
      COALESCE(SUM(b.radiation_dose_rate * (1.0 / 60.0)), 0) as cumulative_radiation_dose_msv,
      COALESCE(ROUND(AVG(b.risk_score)), 0) as avg_risk_score
    FROM astronauts a
    LEFT JOIN biometric_telemetry b
      ON a.id = b.astronaut_id
      AND b.timestamp >= NOW() - ($2 || ' hours')::INTERVAL
    WHERE a.id = $1
    GROUP BY a.name;
  `;

  const statsRes = await query(sqlStats, [astronautId, hours]);
  const row = statsRes.rows[0] || {};

  const sqlTimeSeries = `
    SELECT
      timestamp,
      heart_rate,
      blood_pressure,
      oxygen_saturation,
      core_temp,
      risk_score
    FROM biometric_telemetry
    WHERE astronaut_id = $1
      AND timestamp >= NOW() - ($2 || ' hours')::INTERVAL
    ORDER BY timestamp ASC
    LIMIT 200;
  `;

  const timeSeriesRes = await query(sqlTimeSeries, [astronautId, hours]);

  return {
    astronaut_id: astronautId,
    name: row.name || astronautId,
    time_window_hours: hours,
    sample_count: parseInt(row.sample_count || '0', 10),
    stats: {
      avg_heart_rate: Number(row.avg_heart_rate || 0),
      min_heart_rate: Number(row.min_heart_rate || 0),
      max_heart_rate: Number(row.max_heart_rate || 0),
      avg_spo2: Number(row.avg_spo2 || 0),
      min_spo2: Number(row.min_spo2 || 0),
      avg_core_temp: Number(row.avg_core_temp || 0),
      cumulative_radiation_dose_msv: Number(row.cumulative_radiation_dose_msv || 0),
      avg_risk_score: Number(row.avg_risk_score || 0),
    },
    time_series: timeSeriesRes.rows,
  };
}

// ------------------------------------------------------------------------------
// 4. NASA DONKI Space Weather & System Log Queries
// ------------------------------------------------------------------------------

export interface LogFilterParams {
  severity?: string;
  eventType?: string;
  status?: string;
  limit?: number;
}

/**
 * Queries space weather logs (DONKI solar storms, CME impacts) and ship anomalies.
 */
export async function queryMissionAndDonkiLogs(
  filters: LogFilterParams = {}
): Promise<MissionLogRecord[]> {
  const conditions: string[] = [];
  const params: any[] = [];
  let pIdx = 1;

  if (filters.severity) {
    conditions.push(`severity = $${pIdx++}`);
    params.push(filters.severity);
  }

  if (filters.eventType) {
    conditions.push(`event_type = $${pIdx++}`);
    params.push(filters.eventType);
  }

  if (filters.status) {
    conditions.push(`status = $${pIdx++}`);
    params.push(filters.status);
  }

  const whereClause = conditions.length > 0 ? `WHERE ${conditions.join(' AND ')}` : '';
  const limitVal = filters.limit || 50;
  params.push(limitVal);

  const sql = `
    SELECT
      id,
      event_type,
      severity,
      description,
      timestamp,
      status,
      affected_astronaut_id,
      metadata
    FROM mission_logs
    ${whereClause}
    ORDER BY timestamp DESC
    LIMIT $${pIdx};
  `;

  const result = await query<MissionLogRecord>(sql, params);
  return result.rows;
}

// ------------------------------------------------------------------------------
// 5. Cabin Environmental Overrides Ingestion
// ------------------------------------------------------------------------------

/**
 * Persists manual cabin life-support overrides, pressure shifts, and radiation shield changes.
 */
export async function insertSystemOverride(override: SystemOverrideInput): Promise<number> {
  const sql = `
    INSERT INTO system_overrides (
      cabin_pressure,
      co2_level,
      radiation_flux,
      oxygen_pct,
      applied_by,
      reason,
      storm_shield_engaged,
      scrubbers_forced_duty
    )
    VALUES ($1, $2, $3, $4, $5, $6, $7, $8)
    RETURNING id;
  `;

  const res = await query(sql, [
    override.cabin_pressure,
    override.co2_level,
    override.radiation_flux,
    override.oxygen_pct,
    override.applied_by || null,
    override.reason || 'Manual Flight Surgeon Override',
    override.storm_shield_engaged || false,
    override.scrubbers_forced_duty || false,
  ]);

  return res.rows[0]?.id;
}
