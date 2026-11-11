"""
AstraSense Cloud SQL for PostgreSQL - SQLAlchemy 2.0 ORM & Query Layer
Supports Google Cloud SQL Python Connector with IAM Database Authentication
"""

import os
from datetime import datetime
from typing import List, Optional, Dict, Any
from google.cloud.sql.connector import Connector, IPTypes
import sqlalchemy
from sqlalchemy import (
    create_engine, Column, Integer, BigInteger, String, Float,
    Numeric, DateTime, Boolean, Text, ForeignKey, func, desc
)
from sqlalchemy.dialects.postgresql import JSONB
from sqlalchemy.orm import declarative_base, sessionmaker, Session, relationship

Base = declarative_base()

# ------------------------------------------------------------------------------
# ORM Models
# ------------------------------------------------------------------------------

class Astronaut(Base):
    __tablename__ = "astronauts"

    id = Column(String(64), primary_key=True)
    name = Column(String(255), nullable=False)
    mission_role = Column(String(128), nullable=False)
    email = Column(String(255), unique=True, nullable=False, index=True)
    clearance_status = Column(String(64), nullable=False, default="ACTIVE_FLIGHT_CREW")
    encrypted_access_key = Column(String(255), nullable=False)
    department = Column(String(128), default="FLIGHT_OPERATIONS")
    assigned_vessel = Column(String(128), default="NSS ASTRA-V")
    created_at = Column(DateTime(timezone=True), default=datetime.utcnow)

    telemetry = relationship("BiometricTelemetry", back_populates="astronaut", cascade="all, delete-orphan")


class BiometricTelemetry(Base):
    __tablename__ = "biometric_telemetry"

    id = Column(BigInteger, primary_key=True, autoincrement=True)
    astronaut_id = Column(String(64), ForeignKey("astronauts.id", ondelete="CASCADE"), nullable=False, index=True)
    timestamp = Column(DateTime(timezone=True), nullable=False, index=True)
    heart_rate = Column(Integer, nullable=False)
    blood_pressure = Column(String(32), nullable=False)
    oxygen_saturation = Column(Integer, nullable=False)
    core_temp = Column(Numeric(4, 2), nullable=False)
    respiration_rate = Column(Integer, nullable=False)
    radiation_dose_rate = Column(Numeric(8, 4), nullable=False, default=0.0)
    risk_score = Column(Integer, nullable=False, index=True)
    hrv_ms = Column(Integer, default=72)
    sync_origin = Column(String(64), default="BLE_BIO_HARNESS")
    created_at = Column(DateTime(timezone=True), default=datetime.utcnow)

    astronaut = relationship("Astronaut", back_populates="telemetry")


class MissionLog(Base):
    __tablename__ = "mission_logs"

    id = Column(BigInteger, primary_key=True, autoincrement=True)
    event_type = Column(String(64), nullable=False, index=True)
    severity = Column(String(32), nullable=False, index=True)
    description = Column(Text, nullable=False)
    timestamp = Column(DateTime(timezone=True), nullable=False, index=True)
    status = Column(String(64), nullable=False, default="ACTIVE", index=True)
    affected_astronaut_id = Column(String(64), ForeignKey("astronauts.id", ondelete="SET NULL"), nullable=True)
    metadata_json = Column("metadata", JSONB, default=dict)
    created_at = Column(DateTime(timezone=True), default=datetime.utcnow)


class SystemOverride(Base):
    __tablename__ = "system_overrides"

    id = Column(BigInteger, primary_key=True, autoincrement=True)
    timestamp = Column(DateTime(timezone=True), default=datetime.utcnow, index=True)
    cabin_pressure = Column(Numeric(6, 2), nullable=False)
    co2_level = Column(Numeric(6, 2), nullable=False)
    radiation_flux = Column(Numeric(8, 4), nullable=False)
    oxygen_pct = Column(Numeric(4, 2), nullable=False)
    applied_by = Column(String(64), ForeignKey("astronauts.id", ondelete="SET NULL"), nullable=True)
    reason = Column(Text, nullable=True)
    storm_shield_engaged = Column(Boolean, default=False)
    scrubbers_forced_duty = Column(Boolean, default=False)
    created_at = Column(DateTime(timezone=True), default=datetime.utcnow)


# ------------------------------------------------------------------------------
# Database Engine Initialization
# ------------------------------------------------------------------------------

def get_engine():
    instance_name = os.getenv("INSTANCE_CONNECTION_NAME", "astrasense-fleet:us-central1:astrasense-pg")
    db_user = os.getenv("DB_USER", "astrasense-iam-user@astrasense-fleet.iam")
    db_name = os.getenv("DB_NAME", "astrasensedb")
    use_iam = os.getenv("USE_IAM_AUTH", "true").lower() == "true"

    if os.getenv("USE_CLOUD_SQL_CONNECTOR", "false").lower() == "true":
        connector = Connector()

        def getconn():
            return connector.connect(
                instance_name,
                "pg8000",
                user=db_user,
                db=db_name,
                enable_iam_auth=use_iam,
                ip_type=IPTypes.PUBLIC
            )

        engine = create_engine(
            "postgresql+pg8000://",
            creator=getconn,
            pool_size=15,
            max_overflow=5,
            pool_timeout=30,
            pool_recycle=1800
        )
        return engine
    else:
        # Local Auth Proxy fallback
        db_pass = os.getenv("DB_PASSWORD", "postgres")
        db_host = os.getenv("DB_HOST", "127.0.0.1")
        db_port = os.getenv("DB_PORT", "5432")
        url = f"postgresql+pg8000://{db_user}:{db_pass}@{db_host}:{db_port}/{db_name}"
        return create_engine(url, pool_size=10, max_overflow=5)


# ------------------------------------------------------------------------------
# Core Query Operations
# ------------------------------------------------------------------------------

def authenticate_crew_member(session: Session, identifier: str, raw_access_key: str) -> Optional[Astronaut]:
    """Authenticates an astronaut using pgcrypto crypt verify."""
    sql = sqlalchemy.text("""
        SELECT id FROM astronauts
        WHERE (email = :id OR id = :id)
          AND encrypted_access_key = crypt(:key, encrypted_access_key)
        LIMIT 1;
    """)
    res = session.execute(sql, {"id": identifier, "key": raw_access_key}).fetchone()
    if res:
        return session.query(Astronaut).filter(Astronaut.id == res[0]).first()
    return None


def bulk_insert_telemetry(session: Session, records: List[Dict[str, Any]]) -> int:
    """Inserts batch Bio-Harness telemetry samples."""
    if not records:
        return 0
    objects = [BiometricTelemetry(**r) for r in records]
    session.bulk_save_objects(objects)
    session.commit()
    return len(objects)


def get_fleet_risk_summary(session: Session) -> Dict[str, Any]:
    """Computes aggregated fleet risk metrics and crew rankings."""
    latest_sub = session.query(
        BiometricTelemetry.astronaut_id,
        func.max(BiometricTelemetry.timestamp).label("max_ts")
    ).group_by(BiometricTelemetry.astronaut_id).subquery()

    latest_rows = session.query(BiometricTelemetry, Astronaut).join(
        latest_sub,
        (BiometricTelemetry.astronaut_id == latest_sub.c.astronaut_id) &
        (BiometricTelemetry.timestamp == latest_sub.c.max_ts)
    ).join(Astronaut, BiometricTelemetry.astronaut_id == Astronaut.id).all()

    fleet_size = len(latest_rows)
    total_risk = sum(t.risk_score for t, a in latest_rows)
    mean_risk = round(total_risk / fleet_size) if fleet_size > 0 else 0

    return {
        "fleet_size": fleet_size,
        "mean_fleet_risk": mean_risk,
        "high_risk_crew_count": sum(1 for t, a in latest_rows if t.risk_score >= 60),
        "crew_rankings": [
            {
                "id": a.id,
                "name": a.name,
                "role": a.mission_role,
                "risk_score": t.risk_score,
                "heart_rate": t.heart_rate,
                "spo2": t.oxygen_saturation,
                "last_reported": t.timestamp.isoformat()
            }
            for t, a in sorted(latest_rows, key=lambda x: x[0].risk_score, reverse=True)
        ]
    }
