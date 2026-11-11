package com.example

import android.content.Context
import androidx.test.core.app.ApplicationProvider
import com.example.ai.SpaceAiAssistant
import com.example.data.AstraRepository
import com.example.model.CabinEnvironment
import com.example.model.DestinationType
import com.example.model.RiskLevel
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertNotNull
import org.junit.Assert.assertTrue
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.RobolectricTestRunner
import org.robolectric.annotation.Config

@RunWith(RobolectricTestRunner::class)
@Config(sdk = [34])
class ExampleRobolectricTest {

  @Test
  fun `read app_name string from context matches AstraSense`() {
    val context = ApplicationProvider.getApplicationContext<Context>()
    val appName = context.getString(R.string.app_name)
    assertEquals("AstraSense", appName)
  }

  @Test
  fun `verify authorized admin IDs and registration validation`() {
    assertTrue(AstraRepository.validateAdminId("NASA-AS-701"))
    assertTrue(AstraRepository.validateAdminId("NASA-AS-702"))
    assertTrue(AstraRepository.validateAdminId("NASA-AS-703"))
    assertTrue(AstraRepository.validateAdminId("NASA-AS-704"))
    assertTrue(AstraRepository.validateAdminId("ADMIN-ASTRA-01"))
    assertFalse(AstraRepository.validateAdminId("UNKNOWN-BADGE-999"))
  }

  @Test
  fun `verify mission destinations parameter changes`() {
    AstraRepository.setDestination(DestinationType.MARS_TRANSIT)
    assertEquals(DestinationType.MARS_TRANSIT, AstraRepository.currentDestination.value)
    assertEquals(840, AstraRepository.currentDestination.value.commsDelaySeconds)

    AstraRepository.setDestination(DestinationType.LUNAR_GATEWAY)
    assertEquals(DestinationType.LUNAR_GATEWAY, AstraRepository.currentDestination.value)
    assertEquals(2, AstraRepository.currentDestination.value.commsDelaySeconds)

    AstraRepository.setDestination(DestinationType.LEO_ISS)
    assertEquals(DestinationType.LEO_ISS, AstraRepository.currentDestination.value)
    assertEquals(0, AstraRepository.currentDestination.value.commsDelaySeconds)
  }

  @Test
  fun `verify composite risk calculation changes with cabin emergency`() {
    val member = AstraRepository.crewMembers.value.first()
    val nominalCabin = CabinEnvironment(
      cabinPressureKpa = 101.3,
      co2Ppm = 2000,
      o2Percentage = 20.9,
      temperatureCelsius = 21.0,
      hullIntegrityPercent = 100.0,
      radiationRateMsvPerHour = 0.04
    )
    val (nomScore, nomLevel) = AstraRepository.calculateCompositeRisk(member, nominalCabin)
    assertEquals(RiskLevel.LOW, nomLevel)

    val emergencyCabin = nominalCabin.copy(
      cabinPressureKpa = 89.0, // severe pressure loss
      radiationRateMsvPerHour = 1.95 // solar particle storm
    )
    val (critScore, critLevel) = AstraRepository.calculateCompositeRisk(member, emergencyCabin)
    assertTrue(critScore >= 70)
    assertEquals(RiskLevel.HIGH, critLevel)
  }

  @Test
  fun `verify AstraAuthManager email OTP generation and validation`() {
    val email = "astronaut.test@nasa.gov"
    val otp = com.example.auth.AstraAuthManager.dispatchEmailOtp(email)
    assertEquals(4, otp.length)
    assertTrue(otp.all { it.isDigit() })
    assertTrue(com.example.auth.AstraAuthManager.verifyEmailOtp(email, otp))
    assertFalse(com.example.auth.AstraAuthManager.verifyEmailOtp(email, "0000"))
  }

  @Test
  fun `verify MissionRiskScoreService aggregates health radiation and fatigue metrics`() {
    val member = AstraRepository.crewMembers.value.first()
    val nominalCabin = CabinEnvironment(
      cabinPressureKpa = 101.3,
      co2Ppm = 2100,
      o2Percentage = 20.9,
      temperatureCelsius = 21.5,
      hullIntegrityPercent = 100.0,
      radiationRateMsvPerHour = 0.04
    )

    val nominalRisk = com.example.service.MissionRiskScoreService.calculateMissionRisk(member, nominalCabin)
    assertEquals(RiskLevel.LOW, nominalRisk.riskLevel)
    assertTrue(nominalRisk.compositeScore < 35)
    assertTrue(nominalRisk.healthSubScore <= 25)
    assertTrue(nominalRisk.radiationSubScore <= 25)
    assertTrue(nominalRisk.fatigueSubScore <= 25)
    assertTrue(nominalRisk.missionReadinessPercentage >= 75)
    assertTrue(nominalRisk.clinicalActionDirective.contains("NOMINAL"))

    // Fatigue Escalation: Acute sleep loss & PVT reaction lag
    val fatiguedMember = member.copy(
      sleep = member.sleep.copy(totalSleepHours = 3.5, sleepQualityScore = 40),
      lastPvtReactionTimeMs = 380
    )
    val fatigueRisk = com.example.service.MissionRiskScoreService.calculateMissionRisk(fatiguedMember, nominalCabin)
    assertTrue(fatigueRisk.fatigueSubScore >= 50)
    assertTrue(fatigueRisk.compositeScore >= 35)
    assertTrue(fatigueRisk.primaryRiskDrivers.any { it.contains("sleep") || it.contains("PVT") })

    // Radiation Escalation: Solar Particle Event (SPE)
    val stormCabin = nominalCabin.copy(
      radiationRateMsvPerHour = 1.88,
      stormShieldEngaged = false
    )
    val stormRisk = com.example.service.MissionRiskScoreService.calculateMissionRisk(member, stormCabin)
    assertEquals(RiskLevel.HIGH, stormRisk.riskLevel)
    assertTrue(stormRisk.radiationSubScore >= 70)
    assertTrue(stormRisk.clinicalActionDirective.contains("Storm Shelter") || stormRisk.clinicalActionDirective.contains("Shelter"))
  }

  @Test
  fun `verify 24-hour shift timeline tracks biometric events medication intake and emergency detections`() {
    val initialEvents = AstraRepository.shiftEvents.value
    assertTrue("Should have initial shift timeline events", initialEvents.isNotEmpty())

    // Verify presence of all three required event categories over the shift
    val hasBiometric = initialEvents.any { it.category == com.example.model.ShiftEventCategory.BIOMETRIC }
    val hasMedication = initialEvents.any { it.category == com.example.model.ShiftEventCategory.MEDICATION }
    val hasEmergency = initialEvents.any { it.category == com.example.model.ShiftEventCategory.EMERGENCY }

    assertTrue("Timeline must track significant biometric events", hasBiometric)
    assertTrue("Timeline must track medication intake", hasMedication)
    assertTrue("Timeline must track emergency detections", hasEmergency)

    // Verify specific NASA-standard countermeasures exist in shift log
    val promethazine = initialEvents.find { it.title.contains("Motion Sickness") || it.primaryMetric?.contains("Promethazine") == true }
    assertNotNull("Promethazine medication should be tracked", promethazine)
    assertEquals(com.example.model.ShiftEventCategory.MEDICATION, promethazine?.category)

    val speAlert = initialEvents.find { it.category == com.example.model.ShiftEventCategory.EMERGENCY }
    assertNotNull("Emergency detection event should be tracked", speAlert)
    assertEquals(com.example.model.AlertSeverity.CRITICAL, speAlert?.severity)

    // Verify chronological ordering and shift hour spans the 24h cycle
    val elenaEvents = AstraRepository.getShiftEventsForAstronaut("NASA-AS-702")
    assertTrue(elenaEvents.size >= 4)
    assertTrue("Events should span early to late shift", elenaEvents.first().shiftHour < elenaEvents.last().shiftHour)

    // Test logging a new medication event
    val newMedEvent = com.example.model.ShiftTimelineEvent(
      id = "TEST-MED-99",
      astronautId = "NASA-AS-702",
      astronautName = "Dr. Elena Rostova",
      shiftHour = 20.5,
      shiftTimeLabel = "T+20:30",
      utcTimestamp = "02:30 UTC",
      category = com.example.model.ShiftEventCategory.MEDICATION,
      severity = com.example.model.AlertSeverity.ADVISORY,
      title = "Electrolyte Rehydration Formulation Pack",
      description = "Post-shift oral rehydration therapy 500ml consumed.",
      primaryMetric = "Electrolytes 500ml Oral",
      status = "ADMINISTERED"
    )
    AstraRepository.logShiftEvent(newMedEvent)

    val updatedEvents = AstraRepository.shiftEvents.value
    assertTrue(updatedEvents.any { it.id == "TEST-MED-99" })
  }

  @Test
  fun `verify AstraMissionRiskNotificationService triggers alerts at critical threshold requiring medical officer acknowledgement`() {
    val context = ApplicationProvider.getApplicationContext<Context>()
    com.example.service.AstraMissionRiskNotificationService.initialize(context)

    // Check notification channel created on API 26+
    val notificationManager = context.getSystemService(Context.NOTIFICATION_SERVICE) as android.app.NotificationManager
    val channel = notificationManager.getNotificationChannel(com.example.service.AstraMissionRiskNotificationService.CHANNEL_CRITICAL_RISK)
    assertNotNull("Notification channel must be created", channel)
    assertEquals(android.app.NotificationManager.IMPORTANCE_HIGH, channel.importance)

    val member = AstraRepository.crewMembers.value.first()
    val nominalCabin = CabinEnvironment(
      cabinPressureKpa = 101.3,
      co2Ppm = 2000,
      o2Percentage = 20.9,
      temperatureCelsius = 21.0,
      hullIntegrityPercent = 100.0,
      radiationRateMsvPerHour = 0.04
    )

    // Nominal conditions should NOT trigger notification (< 70 threshold)
    val nominalTriggered = com.example.service.AstraMissionRiskNotificationService.evaluateAndNotify(context, member, nominalCabin)
    assertFalse("Nominal score should not trigger critical notification", nominalTriggered)

    // Emergency Cabin: Pressure drop to 89 kPa and solar storm radiation spike to 1.95 mSv/h
    val criticalCabin = nominalCabin.copy(
      cabinPressureKpa = 89.0,
      radiationRateMsvPerHour = 1.95
    )

    val criticalTriggered = com.example.service.AstraMissionRiskNotificationService.evaluateAndNotify(context, member, criticalCabin)
    assertTrue("Critical risk score >= 70 must trigger notification", criticalTriggered)

    val pendingAlerts = com.example.service.AstraMissionRiskNotificationService.getPendingAlerts()
    assertTrue("Must have at least one unacknowledged critical alert", pendingAlerts.isNotEmpty())

    val pendingAlert = pendingAlerts.first()
    assertEquals(member.id, pendingAlert.astronautId)
    assertTrue(pendingAlert.compositeScore >= com.example.service.AstraMissionRiskNotificationService.CRITICAL_RISK_THRESHOLD)
    assertFalse(pendingAlert.isAcknowledged)

    // Medical Officer Sign-off / Acknowledgement
    val acknowledged = com.example.service.AstraMissionRiskNotificationService.acknowledgeAlert(
      context = context,
      alertId = pendingAlert.alertId,
      officerId = "NASA-AS-701",
      notes = "Donned flight masks. Sealed hatch."
    )
    assertTrue("Acknowledgement must succeed", acknowledged)

    val remainingPending = com.example.service.AstraMissionRiskNotificationService.getPendingAlerts()
    assertFalse("Acknowledged alert must no longer be pending", remainingPending.any { it.alertId == pendingAlert.alertId })
  }

  @Test
  fun `verify AggregateCrewHealthService computes multi-crew summaries and detects mission stressors`() {
    val crew = AstraRepository.crewMembers.value
    assertTrue("Fleet crew size must be at least 4", crew.size >= 4)

    val nominalCabin = CabinEnvironment(
      cabinPressureKpa = 101.3,
      co2Ppm = 2100,
      o2Percentage = 20.9,
      temperatureCelsius = 21.5,
      hullIntegrityPercent = 100.0,
      radiationRateMsvPerHour = 0.04
    )

    val summary = com.example.service.AggregateCrewHealthService.computeAggregateSummary(crew, nominalCabin)
    assertEquals(crew.size, summary.selectedCrewCount)
    assertTrue("Average risk score must be non-negative", summary.averageRiskScore >= 0.0)
    assertTrue("Low risk count must be > 0", summary.lowRiskCount > 0)
    assertTrue("Average heart rate should be in realistic range", summary.averageHeartRateBpm in 50.0..100.0)
    assertTrue("Average sleep duration should be tracked", summary.averageSleepHours > 5.0)
    assertTrue("Average PVT reaction time should be positive", summary.averagePvtReactionMs > 150.0)

    // Verify detection of circadian desynchrony pattern from Elena Rostova's EVA schedule
    val circadianPattern = summary.detectedStressors.find { it.stressorType == com.example.service.StressorType.CIRCADIAN }
    assertNotNull("Circadian stressor pattern should be identified", circadianPattern)
    assertTrue(circadianPattern!!.affectedCrewCount >= 1)
    assertTrue(circadianPattern.recommendedFleetCountermeasure.contains("visor") || circadianPattern.recommendedFleetCountermeasure.contains("amber"))

    // Test cabin environmental CO2 stressor pattern detection
    val highCo2Cabin = nominalCabin.copy(co2Ppm = 4600)
    val co2Summary = com.example.service.AggregateCrewHealthService.computeAggregateSummary(crew, highCo2Cabin)
    val co2Pattern = co2Summary.detectedStressors.find { it.stressorType == com.example.service.StressorType.ENVIRONMENTAL }
    assertNotNull("CO2 cabin stressor must be detected", co2Pattern)
    assertEquals(com.example.model.AlertSeverity.CRITICAL, co2Pattern!!.severity)
    assertTrue(co2Pattern.recommendedFleetCountermeasure.contains("LiOH") || co2Pattern.recommendedFleetCountermeasure.contains("scrubber"))
  }

  @Test
  fun `verify BiometricSensorRadar and AcousticStressScanService generate valid targets and phonation metrics`() {
    val crew = AstraRepository.crewMembers.value
    val targets = com.example.service.AcousticStressScanService.generateRadarTargets(crew)
    assertEquals(crew.size, targets.size)

    // Verify radar angle distribution and distance scaling
    targets.forEach { target ->
      assertTrue("Angle must be within 0 to 360", target.angleDegrees in 0f..360f)
      assertTrue("Distance fraction must be between 0.1 and 1.0", target.distanceFraction in 0.1f..1.0f)
      assertNotNull("Target must have primary metric snippet", target.primaryMetric)
    }

    // Verify Elena Rostova's acoustic phonation analysis (post-EVA fatigue)
    val elena = crew.find { it.id == "NASA-AS-702" }!!
    val elenaAcoustic = com.example.service.AcousticStressScanService.analyzeCrewVoiceAcoustics(elena)
    assertNotNull(elenaAcoustic.scanId)
    assertTrue("Pitch jitter should be calculated", elenaAcoustic.pitchJitterPercent > 1.0)
    assertTrue("Speech cadence should be calculated", elenaAcoustic.speechCadenceWpm in 90..170)
    assertTrue("Frequency spectrogram must contain 20 frequency bands", elenaAcoustic.frequencySpectrogram.size == 20)
    assertTrue("Fatigued astronaut should exhibit elevated acoustic stress", elenaAcoustic.compositeStressScore >= 30)

    // Compare with rested Commander Alex Vance
    val alex = crew.find { it.id == "NASA-AS-701" }!!
    val alexAcoustic = com.example.service.AcousticStressScanService.analyzeCrewVoiceAcoustics(alex)
    assertTrue("Alex Vance should have lower acoustic stress than post-EVA Elena", alexAcoustic.compositeStressScore < elenaAcoustic.compositeStressScore)
    assertTrue(alexAcoustic.statusLabel.contains("NOMINAL") || alexAcoustic.statusLabel.contains("RESTED") || alexAcoustic.statusLabel.contains("MILD"))
  }

  @Test
  fun `verify SpaceAiAssistant detects active spacecraft hazards from telemetry snapshot`() {
    val nominalSnapshot = SpaceAiAssistant.captureCurrentSnapshot()
    val nominalHazards = SpaceAiAssistant.detectActiveHazards(nominalSnapshot)
    // In default seed data, cabin pressure (101.3 kPa), rad (0.018 mSv/h), CO2 (520 ppm) are nominal
    assertTrue("Nominal telemetry should produce zero critical hazards", nominalHazards.isEmpty())

    // Simulate acute decompression and elevated radiation
    val emergencySnapshot = nominalSnapshot.copy(
      cabinPressureKpa = 82.4, // < 95.0 kPa nominal
      cabinRadRate = 2.45,    // > 1.0 mSv/h
      cabinCo2Ppm = 3800,     // > 3,000 ppm
      spO2 = 89,              // < 92% hypoxia
      heartRate = 142         // > 130 tachycardia
    )
    val detectedHazards = SpaceAiAssistant.detectActiveHazards(emergencySnapshot)
    assertEquals(5, detectedHazards.size)
    assertTrue("Should detect depressurization", detectedHazards.any { it.contains("DEPRESSURIZATION") })
    assertTrue("Should detect radiation flux", detectedHazards.any { it.contains("RADIATION FLUX") })
    assertTrue("Should detect CO2 hypercapnia", detectedHazards.any { it.contains("CO₂ HYPERCAPNIC") })
    assertTrue("Should detect hypoxia", detectedHazards.any { it.contains("HYPOXIA") })
    assertTrue("Should detect tachycardia", detectedHazards.any { it.contains("TACHYCARDIA") })
  }

  @Test
  fun `verify SpaceAiAssistant parses emergency checklists and toggles step completion`() {
    val sampleEmergencyText = """
      ### [AUTONOMOUS ONBOARD FLIGHT SURGEON CORE]
      **Diagnosis:** Acute Spacecraft Depressurization

      #### EMERGENCY LIFE-CRITICAL PROTOCOL:
      1. Don Quick-Don emergency flight masks set to 100% positive pressure oxygen.
      2. Verify bulkhead pressure seals across Module Nodes 1, 2, and Airlock.
      3. Initiate continuous 100% O2 wash-out protocol to eliminate inert nitrogen bubbles.
      4. Transfer patient to Crew Lock configured for hyperbaric oxygen therapy.

      ### MEDICAL LITERATURE REFERENCES:
      * [NASA-STD-3001-V2]: NASA-STD-3001 Vol 2, Sec 6.2 | Decompression Prevention | FLIGHT RULE
    """.trimIndent()

    val checklist = SpaceAiAssistant.parseEmergencyChecklist(sampleEmergencyText)
    assertEquals("Should extract 4 numbered emergency action items", 4, checklist.size)
    assertTrue("First step must be don mask", checklist[0].contains("Don Quick-Don emergency flight masks"))
    assertTrue("Second step must be seals", checklist[1].contains("Verify bulkhead pressure seals"))

    // Test appendMessages with emergency classification
    val astronautId = "TEST-CREW-999"
    SpaceAiAssistant.clearSessionHistory(astronautId)
    SpaceAiAssistant.appendMessages(
      astronautId = astronautId,
      userText = "EMERGENCY: Rapid cabin pressure drop in Node 2",
      aiText = sampleEmergencyText,
      references = SpaceAiAssistant.parseLiteratureReferences(sampleEmergencyText)
    )

    val history = SpaceAiAssistant.getSessionHistory(astronautId)
    val aiMsg = history.last()
    assertTrue("Message must be flagged as emergency", aiMsg.isEmergency)
    assertEquals(4, aiMsg.checklist.size)
    assertTrue("Initially zero checklist items completed", aiMsg.completedChecklistIndices.isEmpty())

    // Toggle step 0 (Don masks completed)
    val updated1 = SpaceAiAssistant.toggleChecklistItem(astronautId, aiMsg.id, 0)
    assertNotNull(updated1)
    assertTrue(updated1!!.completedChecklistIndices.contains(0))

    // Toggle step 1 (Verify seals completed)
    val updated2 = SpaceAiAssistant.toggleChecklistItem(astronautId, aiMsg.id, 1)
    assertNotNull(updated2)
    assertEquals(setOf(0, 1), updated2!!.completedChecklistIndices)

    // Untoggle step 0
    val updated3 = SpaceAiAssistant.toggleChecklistItem(astronautId, aiMsg.id, 0)
    assertNotNull(updated3)
    assertEquals(setOf(1), updated3!!.completedChecklistIndices)
  }

  @Test
  fun `verify Emergency SOS triggers automated broadcast and high frequency 10Hz telemetry`() {
    val crew = AstraRepository.crewMembers.value
    val elena = crew.find { it.id == "NASA-AS-702" }!!

    // Trigger Emergency SOS for Elena
    AstraRepository.triggerEmergencySos(elena)

    assertTrue("Emergency SOS must be active", AstraRepository.isEmergencySosActive.value)
    assertEquals(elena.id, AstraRepository.emergencySosOriginator.value?.id)
    assertEquals(10.0, AstraRepository.systemFeatureConfig.value.telemetrySampleRateHz, 0.001)

    // Verify automated broadcast message dispatched to all medical officers
    val messages = AstraRepository.messages.value
    val sosMsg = messages.find { it.content.contains("EMERGENCY SOS BROADCAST", ignoreCase = true) }
    assertNotNull("Automated broadcast message must be sent to medical officers", sosMsg)
    assertEquals("ALL", sosMsg!!.recipientId)
    assertTrue(sosMsg.isHighPriority)

    // Verify critical alert posted
    val alerts = AstraRepository.safetyAlerts.value
    val sosAlert = alerts.find { it.title.contains("EMERGENCY SOS BROADCAST") }
    assertNotNull("Critical safety alert must be generated", sosAlert)
    assertEquals(com.example.model.AlertSeverity.CRITICAL, sosAlert!!.severity)

    // Verify high-frequency telemetry samples logged
    val hfLogs = AstraRepository.highFrequencyTelemetryLogs.value
    assertTrue("High-frequency samples must be logged", hfLogs.isNotEmpty())
    assertTrue("Telemetry sample rate must reflect emergency trigger", hfLogs.first().isSosTriggered)

    // Stand down / deactivate SOS
    AstraRepository.deactivateEmergencySos()
    assertFalse("Emergency SOS should be deactivated", AstraRepository.isEmergencySosActive.value)
    assertEquals(0.1, AstraRepository.systemFeatureConfig.value.telemetrySampleRateHz, 0.001)
  }

  @Test
  fun `verify manual feature controls edit cabin environment vitals and risk overrides`() {
    val initialEnv = AstraRepository.cabinEnvironment.value

    // Manually edit cabin environment
    val editedEnv = initialEnv.copy(
      cabinPressureKpa = 88.5,
      co2Ppm = 4200,
      radiationRateMsvPerHour = 1.45,
      scrubbersActive = false,
      stormShieldEngaged = false
    )
    AstraRepository.updateCabinEnvironment(editedEnv)
    assertEquals(88.5, AstraRepository.cabinEnvironment.value.cabinPressureKpa, 0.001)
    assertEquals(4200, AstraRepository.cabinEnvironment.value.co2Ppm)
    assertEquals(1.45, AstraRepository.cabinEnvironment.value.radiationRateMsvPerHour, 0.001)

    // Manually edit astronaut biometrics
    val elena = AstraRepository.crewMembers.value.find { it.id == "NASA-AS-702" }!!
    val updatedElena = elena.copy(
      vitals = elena.vitals.copy(heartRateBpm = 138, spo2Percentage = 87)
    )
    AstraRepository.updateCrewMember(updatedElena)
    val refreshedElena = AstraRepository.crewMembers.value.find { it.id == "NASA-AS-702" }!!
    assertEquals(138, refreshedElena.vitals.heartRateBpm)
    assertEquals(87, refreshedElena.vitals.spo2Percentage)

    // Manually override Mission Risk Calculation to HIGH
    AstraRepository.updateSystemFeatureConfig(
      AstraRepository.systemFeatureConfig.value.copy(
        manualRiskOverride = RiskLevel.HIGH
      )
    )
    val overriddenRisk = AstraRepository.getMissionRiskScore(refreshedElena, AstraRepository.cabinEnvironment.value)
    assertEquals(RiskLevel.HIGH, overriddenRisk.riskLevel)
    assertTrue(overriddenRisk.primaryRiskDrivers.any { it.contains("MANUAL FLIGHT SURGEON OVERRIDE") })

    // Reset all features to nominal flight factory defaults
    AstraRepository.resetAllFeaturesToNominal()
    assertEquals(101.3, AstraRepository.cabinEnvironment.value.cabinPressureKpa, 0.001)
    assertEquals(null, AstraRepository.systemFeatureConfig.value.manualRiskOverride)
    assertFalse(AstraRepository.isEmergencySosActive.value)

    // Verify custom exact risk score dial
    AstraRepository.updateSystemFeatureConfig(
      AstraRepository.systemFeatureConfig.value.copy(
        manualRiskScoreValue = 78
      )
    )
    val dialedRisk = AstraRepository.getMissionRiskScore(refreshedElena, AstraRepository.cabinEnvironment.value)
    assertEquals(78, dialedRisk.compositeScore)
    assertEquals(RiskLevel.HIGH, dialedRisk.riskLevel)
    assertTrue(dialedRisk.primaryRiskDrivers.any { it.contains("78/100") })

    // Verify manual timeline injector and clear
    AstraRepository.clearShiftEvents()
    val baselineCount = AstraRepository.shiftEvents.value.size
    AstraRepository.logShiftEvent(
      com.example.model.ShiftTimelineEvent(
        id = "EVT-TEST-123",
        astronautId = "NASA-AS-702",
        astronautName = "Dr. Elena Rostova",
        shiftHour = 21.0,
        shiftTimeLabel = "T+21:00",
        utcTimestamp = "21:00 UTC",
        category = com.example.model.ShiftEventCategory.MEDICATION,
        severity = com.example.model.AlertSeverity.ADVISORY,
        title = "MANUAL INJECTED EVENT",
        description = "Test description",
        primaryMetric = "Test Metric",
        secondaryMetric = "Secondary",
        actionDirective = "Action",
        status = "COMPLETED",
        iconType = "medication"
      )
    )
    assertEquals(baselineCount + 1, AstraRepository.shiftEvents.value.size)
    AstraRepository.clearShiftEvents()
    assertEquals(baselineCount, AstraRepository.shiftEvents.value.size)
  }

  @Test
  fun `verify Astronaut Health Assessment evaluates multi-modal biomarkers and awards gamification XP`() {
    val initialExam = com.example.service.HealthAssessmentService.currentExam.value
    assertNotNull(initialExam)

    // Test Blood Glucose update
    com.example.service.HealthAssessmentService.updateBloodGlucose(175)
    val updatedExam = com.example.service.HealthAssessmentService.currentExam.value
    assertEquals(175, updatedExam.bloodGlucoseMgDl)

    // Guidance for Metabolic should trigger watch or critical
    val metabolicGuidance = com.example.service.HealthAssessmentService.getGuidanceForSystem(
      com.example.model.BodySystemRegion.METABOLIC_ENDOCRINE,
      updatedExam
    )
    assertTrue(
      metabolicGuidance.status == com.example.model.HealthStatusLevel.CRITICAL_ACTION ||
        metabolicGuidance.status == com.example.model.HealthStatusLevel.ELEVATED_WATCH
    )
    assertTrue(metabolicGuidance.whyExamNeeded.contains("Microgravity", ignoreCase = true))

    // Test Ocular Ultrasound (SANS)
    com.example.service.HealthAssessmentService.updateOcularUltrasound(
      onsdMm = 6.0,
      iopMmHg = 22,
      acuity = 0.5
    )
    val sansGuidance = com.example.service.HealthAssessmentService.getGuidanceForSystem(
      com.example.model.BodySystemRegion.EYES_SANS,
      com.example.service.HealthAssessmentService.currentExam.value
    )
    assertEquals(com.example.model.HealthStatusLevel.CRITICAL_ACTION, sansGuidance.status)
    assertTrue(sansGuidance.whenToAlertFlightSurgeon.contains("5.5 mm", ignoreCase = true))
    assertTrue(sansGuidance.actionableTreatments.any { it.contains("Lower Body Negative Pressure", ignoreCase = true) })

    // Test Gamification XP award and Expedition state
    val expedition = com.example.service.HealthAssessmentService.expeditionState.value
    assertTrue("Expedition XP should increase when logging exam data", expedition.currentExpeditionXp > 0)
    assertTrue("Expedition badges must be present", expedition.earnedBadges.isNotEmpty())

    // Test Mission Environments
    com.example.service.HealthAssessmentService.setMissionEnvironment(com.example.model.MissionEnvironment.MARS_ROVER)
    assertEquals(
      com.example.model.MissionEnvironment.MARS_ROVER,
      com.example.service.HealthAssessmentService.currentExam.value.environment
    )
  }

  @Test
  fun `verify interactive SVG human figure parses all 7 body regions and computes biometric details`() {
    val crew = AstraRepository.crewMembers.value.first()
    assertNotNull(crew)

    // Check all 7 enum regions exist
    val regions = com.example.ui.components.SvgBodyRegion.entries
    assertEquals(7, regions.size)

    regions.forEach { region ->
      // Verify SVG path is non-empty and starts with M
      assertTrue("Path for ${region.name} should start with M", region.svgPathData.startsWith("M"))

      // Parse SVG path to Android Path
      val androidPath = androidx.core.graphics.PathParser.createPathFromPathData(region.svgPathData)
      assertNotNull(androidPath)
      val bounds = android.graphics.RectF()
      androidPath.computeBounds(bounds, true)
      assertTrue("Width of ${region.name} should be > 0", bounds.width() > 0)
      assertTrue("Height of ${region.name} should be > 0", bounds.height() > 0)

      // Verify biometric details computation
      val details = com.example.ui.components.computeBiometricDetails(
        region = region,
        crewMember = crew,
        glucose = 95,
        onsd = 4.8f,
        heartRate = 72,
        aredTonnage = 15.0f
      )
      assertNotNull(details)
      assertEquals(region, details.region)
      assertTrue(details.primaryMetricTitle.isNotEmpty())
      assertTrue(details.primaryMetricValue.isNotEmpty())
      assertTrue(details.spaceflightHazardNotice.isNotEmpty())
      assertTrue(details.activeTreatmentProtocol.isNotEmpty())
      assertTrue(details.keyVitals.isNotEmpty())
    }
  }

  @Test
  fun `verify persistence manager strict auth and session persistence`() {
    val context = ApplicationProvider.getApplicationContext<Context>()
    com.example.data.AstraPersistenceManager.initialize(context)

    // Verify valid seeded crew credentials succeed
    val result = com.example.data.AstraPersistenceManager.verifyCredentialsStrictly("a.vance@nasa.gov", "NASA-AS-701", "AstraSecure#2026")
    assertTrue(result.isSuccess)
    val user1 = result.getOrNull()
    assertNotNull(user1)
    assertEquals("NASA-AS-701", user1?.id)

    // Verify random or incorrect credentials fail
    val invalidUser = com.example.data.AstraPersistenceManager.verifyCredentialsStrictly("random.person@space.org", "BADGE-999", "wrong-password")
    assertTrue(invalidUser.isFailure)

    val wrongPassword = com.example.data.AstraPersistenceManager.verifyCredentialsStrictly("a.vance@nasa.gov", "NASA-AS-701", "random_wrong_password")
    assertTrue(wrongPassword.isFailure)

    // Verify session save and retrieval
    com.example.data.AstraPersistenceManager.saveSession("NASA-AS-701", "CREW_PORTAL")
    assertEquals("NASA-AS-701", com.example.data.AstraPersistenceManager.getSavedUserId())
    assertEquals("CREW_PORTAL", com.example.data.AstraPersistenceManager.getSavedScreen())

    // Clear session
    com.example.data.AstraPersistenceManager.clearSession()
    org.junit.Assert.assertNull(com.example.data.AstraPersistenceManager.getSavedUserId())
  }

  @Test
  fun `verify audio alarm service mute toggle and siren status`() {
    val context = ApplicationProvider.getApplicationContext<Context>()
    com.example.service.AstraAudioAlarmService.initialize(context)

    com.example.service.AstraAudioAlarmService.setMuted(false)
    assertFalse(com.example.service.AstraAudioAlarmService.isMuted.value)

    com.example.service.AstraAudioAlarmService.toggleMute()
    assertTrue(com.example.service.AstraAudioAlarmService.isMuted.value)

    com.example.service.AstraAudioAlarmService.setMuted(false)
    assertFalse(com.example.service.AstraAudioAlarmService.isMuted.value)
  }

  @Test
  fun `verify in-app notification manager post and dismiss lifecycle`() {
    com.example.service.AstraInAppNotificationManager.postNotification(
      title = "SOLAR FLARE DETECTED",
      message = "High radiation threshold warning in Section Bravo",
      severity = com.example.model.AlertSeverity.WARNING,
      requiresAck = true
    )

    val active = com.example.service.AstraInAppNotificationManager.activeNotification.value
    assertNotNull(active)
    assertEquals("SOLAR FLARE DETECTED", active?.title)
    assertTrue(active?.requiresAck == true)

    // Dismiss notification
    val notificationId = active?.id ?: ""
    com.example.service.AstraInAppNotificationManager.dismissNotification(notificationId)
    org.junit.Assert.assertNull(com.example.service.AstraInAppNotificationManager.activeNotification.value)
  }

  @Test
  fun `verify NASA EVA dataset records and spacewalk statistics from data_nasa_gov`() {
    val evaRecords = com.example.data.NasaOpenDataRepository.evaRecords.value
    assertTrue(evaRecords.isNotEmpty())
    assertEquals(8, evaRecords.size)

    val usEva90 = evaRecords.first { it.id == "EVA-ISS-US-90" }
    assertEquals(90, usEva90.evaNumber)
    assertEquals("USA", usEva90.country)
    assertTrue(usEva90.crewMembers.contains("Jasmin Moghbeli"))
    assertEquals(402, usEva90.durationMinutes)
    assertEquals("6h 42m", usEva90.formattedDuration)
    assertTrue(usEva90.radiationDoseEstimateMsv > 0.0)

    val apollo11 = evaRecords.first { it.id == "EVA-APOLLO-11" }
    assertTrue(apollo11.crewMembers.contains("Neil Armstrong"))
    assertEquals("Apollo 11 Lunar Module (Eagle)", apollo11.vehicle)
  }

  @Test
  fun `verify NASA DONKI space weather notifications and cabin radiation sync`() {
    val alerts = com.example.data.NasaOpenDataRepository.spaceWeatherAlerts.value
    assertTrue(alerts.isNotEmpty())

    val criticalAlert = alerts.first { it.messageType == "SEP" }
    assertEquals(com.example.model.RiskLevel.HIGH, criticalAlert.radiationRiskLevel)
    assertTrue(criticalAlert.peakFluxRate >= 1.0)

    // Test applying space weather directly to ship cabin telemetry
    val originalCabin = com.example.data.AstraRepository.cabinEnvironment.value
    com.example.data.NasaOpenDataRepository.applySpaceWeatherToShipCabin(criticalAlert)

    val updatedCabin = com.example.data.AstraRepository.cabinEnvironment.value
    assertEquals(criticalAlert.peakFluxRate, updatedCabin.radiationRateMsvPerHour, 0.001)
    assertTrue(updatedCabin.stormShieldEngaged)

    // Restore original cabin for test hygiene
    com.example.data.AstraRepository.updateCabinEnvironment(originalCabin)
  }

  @Test
  fun `verify NASA Human Research Program 5 hazards matrix`() {
    val hazards = com.example.data.NasaOpenDataRepository.hrpHazards
    assertEquals(5, hazards.size)

    val acronyms = hazards.map { it.acronym }.toSet()
    assertTrue(acronyms.contains("RAD"))
    assertTrue(acronyms.contains("GRAV"))
    assertTrue(acronyms.contains("ISO"))
    assertTrue(acronyms.contains("ENV"))
    assertTrue(acronyms.contains("DIST"))

    hazards.forEach { hazard ->
      assertTrue(hazard.hazardName.isNotEmpty())
      assertTrue(hazard.primaryPhysiologicalRisks.isNotEmpty())
      assertTrue(hazard.primaryBiomarkers.isNotEmpty())
      assertTrue(hazard.nasaCountermeasureProtocol.isNotEmpty())
      assertTrue(hazard.deepSpaceMultiplier.isNotEmpty())
    }
  }

  @Test
  fun `verify NASA Open Data package search query execution`() = kotlinx.coroutines.test.runTest {
    val result = com.example.data.NasaOpenDataRepository.searchNasaDatasets("radiation")
    assertTrue(result.isSuccess)
    val packages = result.getOrNull()
    assertNotNull(packages)
    assertTrue(packages!!.isNotEmpty())

    val first = packages.first()
    assertTrue(first.title.isNotEmpty())
    assertTrue(first.resources.isNotEmpty())
  }

  @Test
  fun `verify Cloud SQL offline-first queue caches vitals during RF blackout and bulk-syncs when link restored`() = kotlinx.coroutines.test.runTest {
    val context = androidx.test.core.app.ApplicationProvider.getApplicationContext<android.content.Context>()
    com.example.data.cloudsql.CloudSqlSyncManager.initialize(context)

    // Simulate RF Blackout (Autonomous Deep Space mode active)
    com.example.data.cloudsql.CloudSqlSyncManager.setRfBlackoutMode(true)
    assertEquals(
      com.example.data.cloudsql.CloudSqlSyncStatus.OFFLINE_RF_BLACKOUT,
      com.example.data.cloudsql.CloudSqlSyncManager.syncState.value
    )

    // Enqueue biometric vitals, mission logs, and environmental overrides
    val crew = AstraRepository.crewMembers.value.first()
    com.example.data.cloudsql.CloudSqlSyncManager.enqueueBiometricTelemetry(
      astronautId = crew.id,
      vitals = crew.vitals,
      riskScore = 18
    )

    com.example.data.cloudsql.CloudSqlSyncManager.enqueueMissionLog(
      eventType = "DONKI_SOLAR_FLARE_SPE",
      severity = "CRITICAL",
      description = "Class X2.1 solar proton flux event logged in local queue",
      status = "ACTIVE",
      metadata = mapOf("flare_class" to "X2.1")
    )

    com.example.data.cloudsql.CloudSqlSyncManager.enqueueSystemOverride(
      cabin = AstraRepository.cabinEnvironment.value,
      appliedBy = crew.id,
      reason = "Cabin O2 ratio recalibration"
    )

    // Verify records are cached in local SQLite database
    val pending = com.example.data.cloudsql.CloudSqlSyncManager.pendingQueueCount.value
    assertTrue("At least 3 records must be queued in local SQLite during RF blackout", pending >= 3)
    assertEquals(
      com.example.data.cloudsql.CloudSqlSyncStatus.OFFLINE_RF_BLACKOUT,
      com.example.data.cloudsql.CloudSqlSyncManager.syncState.value
    )

    // Restore RF link
    com.example.data.cloudsql.CloudSqlSyncManager.setRfBlackoutMode(false)

    // Perform bulk synchronization
    val syncResult = com.example.data.cloudsql.CloudSqlSyncManager.syncPendingRecordsAsync()
    assertTrue("Bulk synchronization to Cloud SQL must succeed", syncResult)

    // Verify queue is flushed and state is ONLINE_SYNCED
    assertEquals(0, com.example.data.cloudsql.CloudSqlSyncManager.pendingQueueCount.value)
    assertEquals(
      com.example.data.cloudsql.CloudSqlSyncStatus.ONLINE_SYNCED,
      com.example.data.cloudsql.CloudSqlSyncManager.syncState.value
    )
    assertTrue(com.example.data.cloudsql.CloudSqlSyncManager.totalSyncedRecords.value >= 3)
  }

  @Test
  fun `verify Cloud SQL schema DDL and query models integrity`() {
    val astronaut = com.example.data.cloudsql.CloudSqlAstronaut(
      id = "NASA-AS-701",
      name = "Dr. Alex Vance",
      missionRole = "Flight Surgeon",
      email = "a.vance@nasa.gov",
      clearanceStatus = "ACTIVE_FLIGHT_SURGEON",
      encryptedAccessKey = "hashed_key_xyz"
    )
    assertEquals("NASA-AS-701", astronaut.id)
    assertEquals("a.vance@nasa.gov", astronaut.email)

    val telemetry = com.example.data.cloudsql.CloudSqlBiometricTelemetry(
      astronautId = astronaut.id,
      timestamp = "2026-11-07T13:45:00.000Z",
      heartRate = 64,
      bloodPressure = "116/74",
      oxygenSaturation = 99,
      coreTemp = 36.6,
      respirationRate = 13,
      radiationDoseRate = 0.056,
      riskScore = 7
    )
    assertEquals(64, telemetry.heartRate)
    assertEquals(7, telemetry.riskScore)

    val missionLog = com.example.data.cloudsql.CloudSqlMissionLog(
      eventType = "DONKI_CME",
      severity = "MODERATE",
      description = "Earth/Mars directed coronal mass ejection detected",
      timestamp = "2026-11-07T13:45:00.000Z"
    )
    assertEquals("DONKI_CME", missionLog.eventType)
    assertEquals("MODERATE", missionLog.severity)
  }
}


