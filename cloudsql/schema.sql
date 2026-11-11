-- ==============================================================================
-- AstraSense Cloud SQL for PostgreSQL Relational Database Schema
-- Optimized for Deep Space Astronaut Biometrics, Fleet Roster, NASA Open Data,
-- and Autonomous System Overrides.
-- ==============================================================================

-- Enable cryptographic functions and UUID generation
CREATE EXTENSION IF NOT EXISTS "uuid-ossp";
CREATE EXTENSION IF NOT EXISTS "pgcrypto";

-- Clean existing schema if required (migration safety)
DROP TABLE IF EXISTS system_overrides CASCADE;
DROP TABLE IF EXISTS mission_logs CASCADE;
DROP TABLE IF EXISTS biometric_telemetry CASCADE;
DROP TABLE IF EXISTS astronauts CASCADE;

-- ------------------------------------------------------------------------------
-- 1. ASTRONAUTS TABLE
-- Stores flight crew profiles, access credentials, and security clearance.
-- ------------------------------------------------------------------------------
CREATE TABLE astronauts (
    id VARCHAR(64) PRIMARY KEY,
    name VARCHAR(255) NOT NULL,
    mission_role VARCHAR(128) NOT NULL,
    email VARCHAR(255) UNIQUE NOT NULL,
    clearance_status VARCHAR(64) NOT NULL DEFAULT 'ACTIVE_FLIGHT_CREW',
    encrypted_access_key VARCHAR(255) NOT NULL,
    department VARCHAR(128) DEFAULT 'FLIGHT_OPERATIONS',
    assigned_vessel VARCHAR(128) DEFAULT 'NSS ASTRA-V',
    created_at TIMESTAMPTZ NOT NULL DEFAULT NOW(),
    updated_at TIMESTAMPTZ NOT NULL DEFAULT NOW()
);

-- Index on email for rapid astronaut authentication
CREATE INDEX idx_astronauts_email ON astronauts(email);
CREATE INDEX idx_astronauts_clearance ON astronauts(clearance_status);

-- ------------------------------------------------------------------------------
-- 2. BIOMETRIC_TELEMETRY TABLE
-- Time-series telemetry from wearable Bio-Harness BLE sensors and RadWatch dosimeters.
-- ------------------------------------------------------------------------------
CREATE TABLE biometric_telemetry (
    id BIGSERIAL PRIMARY KEY,
    astronaut_id VARCHAR(64) NOT NULL REFERENCES astronauts(id) ON DELETE CASCADE,
    timestamp TIMESTAMPTZ NOT NULL,
    heart_rate INT NOT NULL CHECK (heart_rate >= 0 AND heart_rate <= 300),
    blood_pressure VARCHAR(32) NOT NULL,
    oxygen_saturation INT NOT NULL CHECK (oxygen_saturation >= 0 AND oxygen_saturation <= 100),
    core_temp NUMERIC(4, 2) NOT NULL CHECK (core_temp >= 25.0 AND core_temp <= 45.0),
    respiration_rate INT NOT NULL CHECK (respiration_rate >= 0 AND respiration_rate <= 80),
    radiation_dose_rate NUMERIC(8, 4) NOT NULL DEFAULT 0.0, -- mSv/hr
    risk_score INT NOT NULL CHECK (risk_score >= 0 AND risk_score <= 100),
    hrv_ms INT DEFAULT 72,
    sync_origin VARCHAR(64) DEFAULT 'BLE_BIO_HARNESS',
    created_at TIMESTAMPTZ NOT NULL DEFAULT NOW()
);

-- Composite Index for fast crew time-series queries and trend analysis
CREATE INDEX idx_biometric_astronaut_time ON biometric_telemetry(astronaut_id, timestamp DESC);
-- Index for fleet-wide time slicing and batch synchronization
CREATE INDEX idx_biometric_timestamp ON biometric_telemetry(timestamp DESC);
-- Index for high-risk telemetry threshold monitoring
CREATE INDEX idx_biometric_risk ON biometric_telemetry(risk_score DESC);

-- ------------------------------------------------------------------------------
-- 3. MISSION_LOGS TABLE
-- Spacecraft events, NASA DONKI space weather reports, and emergency detections.
-- ------------------------------------------------------------------------------
CREATE TABLE mission_logs (
    id BIGSERIAL PRIMARY KEY,
    event_type VARCHAR(64) NOT NULL, -- e.g. 'DONKI_SOLAR_FLARE', 'DONKI_CME', 'CABIN_ANOMALY', 'EMERGENCY_DRILL'
    severity VARCHAR(32) NOT NULL,   -- e.g. 'LOW', 'MODERATE', 'HIGH', 'CRITICAL'
    description TEXT NOT NULL,
    timestamp TIMESTAMPTZ NOT NULL,
    status VARCHAR(64) NOT NULL DEFAULT 'ACTIVE', -- e.g. 'ACTIVE', 'ACKNOWLEDGED', 'RESOLVED'
    affected_astronaut_id VARCHAR(64) REFERENCES astronauts(id) ON DELETE SET NULL,
    metadata JSONB DEFAULT '{}'::jsonb,
    created_at TIMESTAMPTZ NOT NULL DEFAULT NOW()
);

-- Fast lookup for critical unresolved hazard logs & event categories
CREATE INDEX idx_mission_logs_query ON mission_logs(event_type, severity, timestamp DESC);
CREATE INDEX idx_mission_logs_status ON mission_logs(status, timestamp DESC);
-- GIN index for querying JSONB metadata (e.g. DONKI flare class, coordinates, speed)
CREATE INDEX idx_mission_logs_metadata ON mission_logs USING GIN (metadata);

-- ------------------------------------------------------------------------------
-- 4. SYSTEM_OVERRIDES TABLE
-- Spacecraft environmental configurations, cabin life-support setpoints, and manual overrides.
-- ------------------------------------------------------------------------------
CREATE TABLE system_overrides (
    id BIGSERIAL PRIMARY KEY,
    timestamp TIMESTAMPTZ NOT NULL DEFAULT NOW(),
    cabin_pressure NUMERIC(6, 2) NOT NULL, -- kPa (Nominal: 101.3)
    co2_level NUMERIC(6, 2) NOT NULL,      -- ppm (Threshold: 2400)
    radiation_flux NUMERIC(8, 4) NOT NULL, -- mSv/hr
    oxygen_pct NUMERIC(4, 2) NOT NULL,     -- % (Nominal: 20.9)
    applied_by VARCHAR(64) REFERENCES astronauts(id) ON DELETE SET NULL,
    reason TEXT,
    storm_shield_engaged BOOLEAN NOT NULL DEFAULT FALSE,
    scrubbers_forced_duty BOOLEAN NOT NULL DEFAULT FALSE,
    created_at TIMESTAMPTZ NOT NULL DEFAULT NOW()
);

CREATE INDEX idx_system_overrides_timestamp ON system_overrides(timestamp DESC);

-- ------------------------------------------------------------------------------
-- INITIAL SEED DATA
-- Default Authorized Flight Crew matching AstraSense Roster & Missions
-- ------------------------------------------------------------------------------
INSERT INTO astronauts (id, name, mission_role, email, clearance_status, encrypted_access_key) VALUES
('NASA-AS-701', 'Dr. Alex Vance', 'Flight Surgeon & Lead Medical Officer', 'a.vance@nasa.gov', 'ACTIVE_FLIGHT_SURGEON', crypt('AstraSecure#2026', gen_salt('bf', 10))),
('NASA-AS-702', 'Dr. Elena Rostova', 'Mission Specialist & EVA Lead', 'e.rostova@esa.int', 'ACTIVE_FLIGHT_CREW', crypt('Rostova#EVA2026', gen_salt('bf', 10))),
('NASA-AS-703', 'Mark Watney Jr.', 'Botanist & Life Support Engineer', 'm.watney@nasa.gov', 'ACTIVE_FLIGHT_CREW', crypt('OrbitalPass#99', gen_salt('bf', 10))),
('NASA-AS-704', 'Joseph Cooper', 'Pilot & Navigation Specialist', 'j.cooper@nasa.gov', 'ACTIVE_FLIGHT_CREW', crypt('AstraCadet#2026', gen_salt('bf', 10))),
('ADMIN-ASTRA-01', 'Chief Medical Officer', 'Ground Flight Surgeon Command', 'command.astra@nasa.gov', 'ACTIVE_COMMAND_LEVEL_3', crypt('FlightSurgeon#1', gen_salt('bf', 10)))
ON CONFLICT (id) DO NOTHING;

-- Initial Nominal Cabin Environment Override Baseline
INSERT INTO system_overrides (timestamp, cabin_pressure, co2_level, radiation_flux, oxygen_pct, applied_by, reason, storm_shield_engaged) VALUES
(NOW() - INTERVAL '4 hours', 101.32, 2150.00, 0.0560, 20.90, 'NASA-AS-701', 'Pre-flight ECLSS nominal stabilization baseline', FALSE);

-- Initial NASA DONKI Space Weather & System Log Sample
INSERT INTO mission_logs (event_type, severity, description, timestamp, status, metadata) VALUES
('DONKI_SOLAR_FLARE', 'HIGH', 'NASA DONKI Alert: Class X1.8 Solar Energetic Particle event detected from Active Region 3842. Proton flux exceeding 100 pfu at >10 MeV.', NOW() - INTERVAL '1 hour', 'ACKNOWLEDGED', '{"flare_class": "X1.8", "cme_speed_km_s": 1420, "radiation_source": "Active Region 3842"}'::jsonb),
('CABIN_ANOMALY', 'MODERATE', 'CO2 partial pressure elevation in Habitat Node 2 reached 2420 ppm due to high physical workload.', NOW() - INTERVAL '45 minutes', 'RESOLVED', '{"sensor_id": "CO2-NODE2-B", "peak_ppm": 2420, "remedy": "Secondary amine bed cycle engaged"}'::jsonb);
