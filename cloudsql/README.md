# AstraSense Cloud SQL for PostgreSQL Architecture

This directory contains the production relational database architecture, PostgreSQL schema DDL, connection pooling implementation, and offline-first queue synchronization for the AstraSense astronaut health and spacecraft monitoring suite.

---

## 1. Relational Database Schema (DDL)

The complete SQL schema is defined in [`cloudsql/schema.sql`](./schema.sql).

### Tables:

1. **`astronauts`**: Flight crew profiles, mission roles, access clearance, and encrypted access keys.
   - Primary Key: `id VARCHAR(64)` (e.g. `NASA-AS-701`)
   - Unique: `email VARCHAR(255)`
   - Indexes: `idx_astronauts_email`, `idx_astronauts_clearance`
   - Encrypted keys: Protected with PostgreSQL `pgcrypto` (`crypt()` with Blowfish salt).

2. **`biometric_telemetry`**: Time-series physiological vitals and radiation dosimetry.
   - Foreign Key: `astronaut_id` referencing `astronauts(id)` ON DELETE CASCADE
   - Fields: `heart_rate`, `blood_pressure`, `oxygen_saturation`, `core_temp`, `respiration_rate`, `radiation_dose_rate`, `risk_score`, `hrv_ms`, `sync_origin`
   - Composite Index: `(astronaut_id, timestamp DESC)` for fast crew trend retrieval.
   - Index: `timestamp DESC` for fleet-wide time slicing and batch ingestion.

3. **`mission_logs`**: Spacecraft events, NASA DONKI space weather solar storms, and emergency detections.
   - Fields: `event_type`, `severity`, `description`, `timestamp`, `status`, `metadata JSONB`
   - Indexes: `idx_mission_logs_query` on `(event_type, severity, timestamp DESC)`, plus GIN index on `metadata` JSONB for fast indexing of solar flare classes and CME velocities.

4. **`system_overrides`**: Spacecraft life support setpoints and manual flight surgeon overrides.
   - Fields: `cabin_pressure`, `co2_level`, `radiation_flux`, `oxygen_pct`, `applied_by`, `reason`, `storm_shield_engaged`
   - Index: `timestamp DESC`

---

## 2. Backend Connection & ORM/Query Layer

### Node.js / TypeScript (`cloudsql/backend/src/`):
- **`db.ts`**: Implements Google Cloud SQL Connector (`@google-cloud/cloud-sql-connector`) with IAM Database Authentication (`AuthType.IAM`), TLS encryption, and high-performance connection pooling (`pg.Pool`).
- **`queries.ts`**:
  - `authenticateCrewMember(identifier, rawAccessKey)`: Validates credentials securely using PostgreSQL `crypt()` comparisons.
  - `insertBatchBiometricTelemetry(records)`: High-throughput batch ingestion using multi-row parameterized `VALUES (...)` syntax in a single roundtrip.
  - `getFleetHealthRiskAggregation()`: Aggregates latest risk score per crew member, calculates fleet mean risk, counts active critical hazards, and ranks crew by risk.
  - `getCrewTelemetryTrends(astronautId, hours)`: Rolling window statistics (average, min, max vitals, cumulative radiation dose, and time-series points).
  - `queryMissionAndDonkiLogs(filters)`: Parameterized queries for space weather solar flares and onboard hazard logs.
  - `insertSystemOverride(override)`: Records manual environmental life-support overrides.

### Python / SQLAlchemy Alternative (`cloudsql/backend/python/cloudsql_db.py`):
- Full SQLAlchemy 2.0 ORM models and Cloud SQL Python Connector integration (`cloud-sql-python-connector["pg8000"]`) for microservices implemented in Python.

---

## 3. Offline-First Synchronization Strategy (Android)

During deep-space exploration, missions frequently encounter RF blackouts (orbital lunar/planetary occultation, intense solar particle radiation interference). AstraSense guarantees 100% uninterrupted operation through an offline-first architecture:

1. **Local SQLite Queue (`AstraOfflineQueueDbHelper.kt`)**:
   - On-device database (`astrasense_offline_queue.db`) storing uncommitted telemetry packets in table `offline_sync_queue`.
   - Records are categorized by `BIOMETRIC_TELEMETRY`, `MISSION_LOG`, and `SYSTEM_OVERRIDE`.

2. **Sync Coordinator (`CloudSqlSyncManager.kt`)**:
   - Monitored by Android Coroutines with reactive `StateFlow`.
   - When **Autonomous Deep Space Offline Mode** is toggled ON, the sync manager shifts to `OFFLINE_RF_BLACKOUT` status and buffers all vitals locally without transmission attempts.
   - When communication is restored, the coordinator dequeues batches of up to 50 records at a time, bulk-inserts them into Cloud SQL for PostgreSQL, and prunes acknowledged rows from the SQLite queue.
   - Visible to crew and flight surgeons in the `EarthSyncScreen` dashboard under **Cloud SQL for PostgreSQL**.
