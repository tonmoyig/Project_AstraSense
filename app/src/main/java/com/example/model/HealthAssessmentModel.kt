package com.example.model

/**
 * Anatomical Body Systems impacted by spaceflight hazards according to NASA Human Research Program (HRP).
 */
enum class BodySystemRegion(
  val displayName: String,
  val iconName: String,
  val description: String
) {
  BRAIN_NEURO(
    displayName = "Neurovestibular & Brain",
    iconName = "psychology",
    description = "Space Adaptation Syndrome, otolith tilt conflict, intracranial pressure elevation, and circadian shifts."
  ),
  EYES_SANS(
    displayName = "Ocular & SANS",
    iconName = "visibility",
    description = "Spaceflight-Associated Neuro-ocular Syndrome (optic disc edema, globe flattening, and hyperopic visual shift)."
  ),
  CARDIOVASCULAR(
    displayName = "Cardiovascular & Fluid Shift",
    iconName = "favorite",
    description = "Cephalad fluid redistribution (puffy face syndrome), reduced stroke volume, cardiac deconditioning, and orthostatic intolerance."
  ),
  LUNGS_ECLSS(
    displayName = "Pulmonary & ECLSS",
    iconName = "air",
    description = "Aerosolized particulate accumulation, elevated ambient CO2 hypercapnia, and lung surfactant changes."
  ),
  MUSCULOSKELETAL(
    displayName = "Musculoskeletal & Spine",
    iconName = "fitness_center",
    description = "Bone mineral density demineralization (1-1.5%/month), postural muscle atrophy (soleus), and spinal elongation."
  ),
  METABOLIC_ENDOCRINE(
    displayName = "Metabolic & Blood Glucose",
    iconName = "water_drop",
    description = "Microgravity-induced insulin resistance, glucometric variability, and altered nutrient absorption."
  ),
  RENAL_URINARY(
    displayName = "Renal & Bone Excretion",
    iconName = "medication",
    description = "Hypercalciuria from bone demineralization leading to high nephrolithiasis (kidney stone) risk and dehydration."
  ),
  SKIN_IMMUNE(
    displayName = "Dermal, Immune & Radiation",
    iconName = "shield",
    description = "Impaired cutaneous healing, latent herpesvirus reactivation, contact dermatitis, and galactic cosmic radiation cellular damage."
  )
}

/**
 * Mission Operational Environment where health is assessed.
 */
enum class MissionEnvironment(
  val displayName: String,
  val gravityFraction: String,
  val primaryHazards: String
) {
  DEEP_SPACE_TRANSIT(
    displayName = "Deep Space Transit Vehicle",
    gravityFraction = "0.0 g (Microgravity)",
    primaryHazards = "Galactic Cosmic Radiation (GCR), zero-g fluid shifts, deep acoustic isolation, high distance-to-Earth."
  ),
  SPACE_STATION(
    displayName = "Space Station (LEO / Gateway)",
    gravityFraction = "0.0 g (Microgravity)",
    primaryHazards = "Rapid day/night orbital cycles (90 min), elevated CO2, high EVA frequency, acoustic noise."
  ),
  LUNAR_BASE(
    displayName = "Lunar Surface Base / Artemis Camp",
    gravityFraction = "0.166 g (1/6 Earth Gravity)",
    primaryHazards = "Abrasive electrostatically-charged lunar regolith dust, 14-day lunar nights, partial gravity locomotion."
  ),
  MARS_ROVER(
    displayName = "Mars Pressurized Rover / Hab",
    gravityFraction = "0.38 g (3/8 Earth Gravity)",
    primaryHazards = "Toxic perchlorate soil dust, 4-22 min comms delay, surface ultraviolet and cosmic radiation."
  )
}

/**
 * Clinical assessment data and self-exam entries logged by the astronaut.
 */
data class AstronautHealthExamRecord(
  val id: String,
  val astronautId: String,
  val timestamp: String,
  val environment: MissionEnvironment = MissionEnvironment.DEEP_SPACE_TRANSIT,
  // 1. Metabolic
  val bloodGlucoseMgDl: Int = 92, // Normal 70-99 fasting, 100-140 postprandial
  val bloodLactateMmolL: Double = 1.2, // Normal 0.5-2.0
  // 2. Ocular SANS Ultrasound & Acuity
  val opticNerveSheathDiameterMm: Double = 4.6, // Normal < 5.0 mm; > 5.8 mm indicates severe SANS/ICP
  val intraocularPressureMmHg: Int = 16, // Normal 10-21 mmHg
  val visualAcuityDecimal: Double = 1.0, // 1.0 = 20/20, 0.5 = 20/40
  // 3. Cardiovascular POCUS Ultrasound
  val leftVentricularEjectionFractionPercent: Int = 62, // Normal 55-70%
  val jugularVeinDistensionMm: Double = 7.2, // Normal 5-9 mm
  // 4. Musculoskeletal & Exercise
  val dailyAredResistanceTonnage: Double = 14.5, // Target > 12.0 metric tons total
  val t2TreadmillKm: Double = 5.2, // Daily target > 4.5 km
  val backPainScore: Int = 1, // 0 to 10
  // 5. Neurovestibular
  val otolithPosturographyScore: Int = 88, // 0-100 stability index
  val spaceMotionSicknessScore: Int = 0 // 0 none, 1 mild, 2 moderate, 3 severe
)

/**
 * NASA Human Research Program detailed guideline card for an organ system.
 */
data class OrganSystemGuidance(
  val region: BodySystemRegion,
  val status: HealthStatusLevel,
  val measuredValueString: String,
  val whyExamNeeded: String,
  val spaceflightHazardEffect: String,
  val whenToAlertFlightSurgeon: String,
  val actionableTreatments: List<String>,
  val recommendedCountermeasure: String
)

enum class HealthStatusLevel(val displayName: String) {
  OPTIMAL("Optimal"),
  NOMINAL("Nominal"),
  ELEVATED_WATCH("Elevated Watch"),
  CRITICAL_ACTION("Critical Action Required")
}

/**
 * Gamification achievements and health expedition streak system.
 */
data class AstronautHealthExpeditionState(
  val expeditionLevel: Int = 4,
  val currentExpeditionXp: Int = 3450,
  val targetLevelXp: Int = 5000,
  val streakDays: Int = 14,
  val resilienceIndexScore: Int = 94, // 0 to 100
  val earnedBadges: List<ExpeditionBadge> = listOf(
    ExpeditionBadge("SHIELD_OF_ARTEMIS", "Shield of Artemis", "Maintained nominal radiation dosimeter protocols for 14 continuous shifts.", "shield", true),
    ExpeditionBadge("HAWKEYE_ORBITAL", "Hawkeye SANS Vanguard", "Completed routine optic nerve sheath ultrasound inspection on schedule.", "visibility", true),
    ExpeditionBadge("MARS_ENDURANCE", "Mars VO₂ Master", "Surpassed 14 metric tons of ARED resistive loading and 5km T2 treadmill cardio.", "fitness_center", true),
    ExpeditionBadge("METABOLIC_HARMONY", "Metabolic Equilibrium", "Maintained fasting blood glucose within 70-99 mg/dL target zone for 10 shifts.", "water_drop", true),
    ExpeditionBadge("CHIBIS_PIONEER", "Chibis Negative Pressure Pro", "Completed 45 minutes of Lower Body Negative Pressure (LBNP) fluid shifting.", "favorite", false)
  )
)

data class ExpeditionBadge(
  val id: String,
  val title: String,
  val description: String,
  val iconName: String,
  val isUnlocked: Boolean
)
