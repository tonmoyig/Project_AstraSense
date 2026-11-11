package com.example.service

import com.example.model.AstronautHealthExamRecord
import com.example.model.AstronautHealthExpeditionState
import com.example.model.BodySystemRegion
import com.example.model.ExpeditionBadge
import com.example.model.HealthStatusLevel
import com.example.model.MissionEnvironment
import com.example.model.OrganSystemGuidance
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale
import java.util.UUID

/**
 * Service managing Astronaut Health Self-Assessments grounded in NASA's Human Research Program (HRP).
 * Evaluates multi-modal physiological exam indicators and computes countermeasure directives.
 */
object HealthAssessmentService {

  private val _currentExam = MutableStateFlow(
    AstronautHealthExamRecord(
      id = "EXAM-INITIAL-01",
      astronautId = "NASA-AS-702",
      timestamp = SimpleDateFormat("yyyy-MM-dd HH:mm", Locale.US).format(Date()),
      environment = MissionEnvironment.DEEP_SPACE_TRANSIT,
      bloodGlucoseMgDl = 94,
      bloodLactateMmolL = 1.3,
      opticNerveSheathDiameterMm = 4.8,
      intraocularPressureMmHg = 17,
      visualAcuityDecimal = 1.0,
      leftVentricularEjectionFractionPercent = 63,
      jugularVeinDistensionMm = 7.0,
      dailyAredResistanceTonnage = 14.8,
      t2TreadmillKm = 5.4,
      backPainScore = 1,
      otolithPosturographyScore = 89,
      spaceMotionSicknessScore = 0
    )
  )
  val currentExam: StateFlow<AstronautHealthExamRecord> = _currentExam.asStateFlow()

  private val _expeditionState = MutableStateFlow(AstronautHealthExpeditionState())
  val expeditionState: StateFlow<AstronautHealthExpeditionState> = _expeditionState.asStateFlow()

  fun updateExamRecord(updated: AstronautHealthExamRecord) {
    _currentExam.value = updated
    // Award Gamification XP for recording physiological telemetry
    awardExpeditionXp(150)
  }

  fun setMissionEnvironment(env: MissionEnvironment) {
    _currentExam.update { it.copy(environment = env) }
  }

  fun updateBloodGlucose(glucoseMgDl: Int) {
    _currentExam.update { it.copy(bloodGlucoseMgDl = glucoseMgDl) }
    awardExpeditionXp(50)
  }

  fun updateOcularUltrasound(onsdMm: Double, iopMmHg: Int, acuity: Double) {
    _currentExam.update {
      it.copy(
        opticNerveSheathDiameterMm = onsdMm,
        intraocularPressureMmHg = iopMmHg,
        visualAcuityDecimal = acuity
      )
    }
    awardExpeditionXp(100)
    if (onsdMm <= 5.0) {
      unlockBadge("HAWKEYE_ORBITAL")
    }
  }

  fun updateCardiovascularPocus(lvefPercent: Int, jugularMm: Double) {
    _currentExam.update {
      it.copy(
        leftVentricularEjectionFractionPercent = lvefPercent,
        jugularVeinDistensionMm = jugularMm
      )
    }
    awardExpeditionXp(100)
  }

  fun updateMusculoskeletalLog(tonnage: Double, treadmillKm: Double, backPain: Int) {
    _currentExam.update {
      it.copy(
        dailyAredResistanceTonnage = tonnage,
        t2TreadmillKm = treadmillKm,
        backPainScore = backPain
      )
    }
    awardExpeditionXp(120)
    if (tonnage >= 14.0 && treadmillKm >= 5.0) {
      unlockBadge("MARS_ENDURANCE")
    }
  }

  private fun awardExpeditionXp(points: Int) {
    _expeditionState.update { state ->
      val newXp = state.currentExpeditionXp + points
      if (newXp >= state.targetLevelXp) {
        state.copy(
          expeditionLevel = state.expeditionLevel + 1,
          currentExpeditionXp = newXp - state.targetLevelXp,
          targetLevelXp = (state.targetLevelXp * 1.25).toInt(),
          resilienceIndexScore = (state.resilienceIndexScore + 2).coerceAtMost(100)
        )
      } else {
        state.copy(currentExpeditionXp = newXp)
      }
    }
  }

  private fun unlockBadge(badgeId: String) {
    _expeditionState.update { state ->
      val updated = state.earnedBadges.map { badge ->
        if (badge.id == badgeId) badge.copy(isUnlocked = true) else badge
      }
      state.copy(earnedBadges = updated)
    }
  }

  /**
   * Generates clinically grounded NASA HRP guidance for an anatomical body system region
   * based on the astronaut's active self-exam indicators and mission environment.
   */
  fun getGuidanceForSystem(region: BodySystemRegion, exam: AstronautHealthExamRecord): OrganSystemGuidance {
    return when (region) {
      BodySystemRegion.BRAIN_NEURO -> {
        val status = if (exam.spaceMotionSicknessScore >= 2 || exam.otolithPosturographyScore < 65) {
          HealthStatusLevel.ELEVATED_WATCH
        } else {
          HealthStatusLevel.OPTIMAL
        }
        OrganSystemGuidance(
          region = region,
          status = status,
          measuredValueString = "Otolith Stability: ${exam.otolithPosturographyScore}/100 • SAS Score: ${exam.spaceMotionSicknessScore}/3",
          whyExamNeeded = "Microgravity removes constant vestibular gravity vectors. The otolith organs in the inner ear signal falling, while visual cues signal stability, triggering acute neurovestibular conflict (Space Adaptation Syndrome).",
          spaceflightHazardEffect = "Causes neuro-sensory disorientation, illusions of inverted body orientation (inversion illusion), and acute psychomotor reaction lag during EVAs.",
          whenToAlertFlightSurgeon = "Persistent motion sickness beyond Flight Day 4, severe spatial disorientation preventing flight computer interaction, or posturography stability <60.",
          actionableTreatments = listOf(
            "Administer 25mg oral Promethazine (or IM injection if emesis prevents oral intake).",
            "Restrict head saccadic movements across sagittal and coronal axes.",
            "Utilize tactile body restraints in crew sleep quarters to re-anchor somatosensory cues."
          ),
          recommendedCountermeasure = "Visual-vestibular adaptation training & 20 min head movement protocol."
        )
      }

      BodySystemRegion.EYES_SANS -> {
        val onsd = exam.opticNerveSheathDiameterMm
        val acuity = exam.visualAcuityDecimal
        val status = when {
          onsd >= 5.8 || acuity <= 0.6 -> HealthStatusLevel.CRITICAL_ACTION
          onsd >= 5.2 || acuity <= 0.8 -> HealthStatusLevel.ELEVATED_WATCH
          else -> HealthStatusLevel.OPTIMAL
        }
        OrganSystemGuidance(
          region = region,
          status = status,
          measuredValueString = "ONSD Ultrasound: ${"%.1f".format(onsd)} mm • Acuity: 20/${(20 / acuity).toInt()}",
          whyExamNeeded = "Spaceflight-Associated Neuro-ocular Syndrome (SANS) affects up to 70% of long-duration astronauts due to continuous cephalad venous pressure and cerebrospinal fluid pooling behind the optic nerve sheath.",
          spaceflightHazardEffect = "Leads to optic disc edema (papilledema), posterior globe flattening, choroidal folds, and hyperopic visual decline (inability to read checklist displays).",
          whenToAlertFlightSurgeon = "Optic Nerve Sheath Diameter > 5.5 mm on point-of-care ultrasound, sudden loss of near visual acuity > 2 Snellen lines, or emergence of scotomas.",
          actionableTreatments = listOf(
            "Initiate 45-minute daily Lower Body Negative Pressure (LBNP / Chibis device at -25 to -35 mmHg).",
            "Adjust cabin lighting to 500 lux; switch displays to high-contrast inverted typography.",
            "Issue adjustable focus 'Space Flight Glasses' for focal length compensation."
          ),
          recommendedCountermeasure = "Scheduled ocular B-scan ultrasound & weekly Amsler grid self-assessment."
        )
      }

      BodySystemRegion.CARDIOVASCULAR -> {
        val lvef = exam.leftVentricularEjectionFractionPercent
        val jugular = exam.jugularVeinDistensionMm
        val status = when {
          lvef < 50 || jugular > 10.0 -> HealthStatusLevel.CRITICAL_ACTION
          lvef < 55 || jugular > 8.5 -> HealthStatusLevel.ELEVATED_WATCH
          else -> HealthStatusLevel.NOMINAL
        }
        OrganSystemGuidance(
          region = region,
          status = status,
          measuredValueString = "Cardiac LVEF: $lvef% • Internal Jugular: ${"%.1f".format(jugular)} mm",
          whyExamNeeded = "Absence of hydrostatic pressure gradient redistributes ~1.5 to 2.0 Liters of blood and interstitial fluid upward toward the thoracic and cranial compartments.",
          spaceflightHazardEffect = "Decreases myocardial workload, promoting left ventricular mass shrinkage (~8-10% deconditioning over 6 months), jugular venous stagnation/thrombosis risk, and post-landing orthostatic collapse.",
          whenToAlertFlightSurgeon = "LVEF drops below 52%, presence of internal jugular venous stasis/reverse flow on Doppler ultrasound, or persistent resting heart rate >110 bpm.",
          actionableTreatments = listOf(
            "Engage high-intensity interval training (HIIT) on T2 treadmill to preserve stroke volume.",
            "Apply lower-limb gradient compression garments (G-suit / calf cuffs) prior to de-orbit.",
            "Implement oral fluid + electrolyte salt tablet loading (1.0 L saline equivalent) 2 hours prior to re-entry."
          ),
          recommendedCountermeasure = "Daily 30-min cardiovascular cycle ergometer / T2 treadmill at 75-85% VO₂ peak."
        )
      }

      BodySystemRegion.LUNGS_ECLSS -> {
        val status = HealthStatusLevel.NOMINAL
        OrganSystemGuidance(
          region = region,
          status = status,
          measuredValueString = "Pulmonary Ventilation: Optimal • SpO₂: 98% • Airway Resistance: Nominal",
          whyExamNeeded = "In microgravity, dust, shed skin particles, and aerosols do not settle by sedimentation; they remain perpetually suspended in the cabin atmosphere, inhaled directly into bronchial airways.",
          spaceflightHazardEffect = "Elevated ambient CO2 (>3.0 mmHg) triggers hypercapnic headache, mild respiratory acidosis, dyspnea, and respiratory tract irritation.",
          whenToAlertFlightSurgeon = "Persistent cough with wheezing, SpO₂ dropping below 93% on ambient air, or detection of B-lines (pulmonary comet tails) on lung ultrasound.",
          actionableTreatments = listOf(
            "Verify high-efficiency particulate air (HEPA) cabin filtration airflow is unobstructed.",
            "Administer inhaled bronchodilator (Albuterol 2 puffs) for acute bronchospasm.",
            "Increase ECLSS primary lithium hydroxide / CDRA scrubber bypass throughput."
          ),
          recommendedCountermeasure = "Routine lung point-of-care ultrasound (POCUS) and cabin HEPA filter inspection."
        )
      }

      BodySystemRegion.MUSCULOSKELETAL -> {
        val tonnage = exam.dailyAredResistanceTonnage
        val backPain = exam.backPainScore
        val status = when {
          tonnage < 9.0 || backPain >= 6 -> HealthStatusLevel.CRITICAL_ACTION
          tonnage < 12.0 || backPain >= 3 -> HealthStatusLevel.ELEVATED_WATCH
          else -> HealthStatusLevel.OPTIMAL
        }
        OrganSystemGuidance(
          region = region,
          status = status,
          measuredValueString = "ARED Load: ${"%.1f".format(tonnage)} Metric Tons • Lumbar Pain: $backPain/10",
          whyExamNeeded = "Without 1-g axial skeleton loading, osteoclasts outpace osteoblasts, causing bone mineral loss of 1.0% to 1.5% per month primarily in the calcaneus, femoral head, and lumbar vertebrae.",
          spaceflightHazardEffect = "Results in severe muscular atrophy of anti-gravity postural muscles (soleus, gastrocnemius), 5-7 cm spinal disc unweighting elongation, and high intervertebral disc herniation risk.",
          whenToAlertFlightSurgeon = "Severe radiating lumbar radiculopathy (pinched nerve), inability to achieve minimum 11-ton ARED daily loading, or acute joint effusion.",
          actionableTreatments = listOf(
            "Increase ARED deadlift, squat, and heel-raise resistance by +5% progressive overload.",
            "Perform fetal-tuck axial lumbar decompression stretches in crew sleep berth.",
            "Ensure daily oral Bisphosphonate / Vitamin D3 (1000 IU) and Calcium supplementation."
          ),
          recommendedCountermeasure = "Daily 2.5 hours scheduled countermeasure: 1 hour ARED resistance + 45 min T2 running."
        )
      }

      BodySystemRegion.METABOLIC_ENDOCRINE -> {
        val glucose = exam.bloodGlucoseMgDl
        val lactate = exam.bloodLactateMmolL
        val status = when {
          glucose > 160 || glucose < 60 || lactate > 3.0 -> HealthStatusLevel.CRITICAL_ACTION
          glucose > 115 || glucose < 70 || lactate > 2.0 -> HealthStatusLevel.ELEVATED_WATCH
          else -> HealthStatusLevel.OPTIMAL
        }
        OrganSystemGuidance(
          region = region,
          status = status,
          measuredValueString = "Blood Glucose: $glucose mg/dL • Blood Lactate: ${"%.1f".format(lactate)} mmol/L",
          whyExamNeeded = "Microgravity induces peripheral insulin resistance due to physical inactivity of postural slow-twitch muscle fibers and altered metabolic energy pathways in space.",
          spaceflightHazardEffect = "Causes glucose dysregulation, postprandial hyperglycemia, elevated resting cortisol, and diminished glycogen storage efficiency.",
          whenToAlertFlightSurgeon = "Fasting glucose persistently >130 mg/dL, symptomatic hypoglycemia (<65 mg/dL with diaphoresis/tremors), or resting lactate >2.5 mmol/L.",
          actionableTreatments = listOf(
            "Shift spaceflight nutrition to low-glycemic index shelf-stable food pouches.",
            "Hydrate with 750 mL balanced electrolyte fluid immediately.",
            "Schedule postprandial 15-minute low-resistance cycle ergometer session."
          ),
          recommendedCountermeasure = "Weekly fingerstick glucometer check & standardized macronutrient ratio logging."
        )
      }

      BodySystemRegion.RENAL_URINARY -> {
        val status = HealthStatusLevel.NOMINAL
        OrganSystemGuidance(
          region = region,
          status = status,
          measuredValueString = "Kidney Stone Risk: Low • Hydration Index: Optimal (1.8 L/day)",
          whyExamNeeded = "Bone demineralization dumps massive quantities of free ionic calcium into the bloodstream, which is filtered by the kidneys into the urine (hypercalciuria).",
          spaceflightHazardEffect = "Combined with mild chronic microgravity dehydration, this drastically increases the precipitation of calcium oxalate and calcium phosphate kidney stones (nephrolithiasis).",
          whenToAlertFlightSurgeon = "Sudden acute flank or groin pain (renal colic), gross hematuria (blood in urine), or persistent oliguria (<500 mL urine/day).",
          actionableTreatments = listOf(
            "Increase mandatory daily fluid intake to minimum 2.5 Liters of water.",
            "Administer oral Potassium Citrate (20 mEq) to alkalinize urine and inhibit crystal formation.",
            "If acute renal colic develops: administer IM Ketorolac and perform renal point-of-care ultrasound."
          ),
          recommendedCountermeasure = "Daily fluid target tracking (minimum 2.0 L) and urinary pH strip test."
        )
      }

      BodySystemRegion.SKIN_IMMUNE -> {
        val status = HealthStatusLevel.NOMINAL
        OrganSystemGuidance(
          region = region,
          status = status,
          measuredValueString = "Erythema Index: 0 • Viral Reactivation: None • Wound Healing: Stable",
          whyExamNeeded = "Confinement, circadian disruption, altered microbiome, and space radiation cause dysregulation of T-cell immune surveillance and skin barrier thinning.",
          spaceflightHazardEffect = "Results in spontaneous reactivation of latent Epstein-Barr virus (EBV) and Varicella-Zoster virus (VZV), delayed cutaneous wound repair, and contact dermatitis.",
          whenToAlertFlightSurgeon = "Dermatomal blistering rash (shingles / VZV), non-healing skin laceration >5 days, or systemic fever with unexplained lymphadenopathy.",
          actionableTreatments = listOf(
            "Apply topical hydrocortisone 1% or antibiotic barrier ointment to abrasive suit chafing.",
            "Initiate oral Acyclovir 800mg 5x/day for confirmed acute herpes zoster reactivation.",
            "Retire to shielded crew quarters during elevated solar particle flux periods."
          ),
          recommendedCountermeasure = "Weekly skin examination, microbial surface swab protocol, and radiation dosimeter logging."
        )
      }
    }
  }
}
