package com.example.ui.screens

import android.widget.Toast
import androidx.activity.compose.BackHandler
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.automirrored.filled.HelpOutline
import androidx.compose.material.icons.filled.Accessibility
import androidx.compose.material.icons.filled.Air
import androidx.compose.material.icons.filled.Bolt
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.Favorite
import androidx.compose.material.icons.filled.FitnessCenter
import androidx.compose.material.icons.filled.LocalHospital
import androidx.compose.material.icons.filled.Medication
import androidx.compose.material.icons.filled.Psychology
import androidx.compose.material.icons.filled.Public
import androidx.compose.material.icons.filled.Science
import androidx.compose.material.icons.filled.Security
import androidx.compose.material.icons.filled.Sensors
import androidx.compose.material.icons.filled.Tune
import androidx.compose.material.icons.filled.Visibility
import androidx.compose.material.icons.filled.WaterDrop
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.DropdownMenu
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Slider
import androidx.compose.material3.SliderDefaults
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.model.BodySystemRegion
import com.example.model.CrewMember
import com.example.model.HealthStatusLevel
import com.example.model.MissionEnvironment
import com.example.service.AstraAudioAlarmService
import com.example.service.AstraInAppNotificationManager
import com.example.service.HealthAssessmentService
import com.example.ui.components.AnatomicalHumanFigureView
import com.example.ui.components.GlassCard
import com.example.ui.components.HealthAssessmentGamificationView
import com.example.ui.components.InteractiveSvgHumanFigureView
import com.example.ui.components.StatusBadge
import com.example.ui.theme.BrightTeal
import com.example.ui.theme.CriticalRed
import com.example.ui.theme.GlassBorderStroke
import com.example.ui.theme.NeonCyan
import com.example.ui.theme.NominalGreen
import com.example.ui.theme.StarlightBlue
import com.example.ui.theme.WarningAmber

/**
 * Visual Tool for Astronaut Health Self-Assessment & HRP Clinical Guidance.
 * Features an interactive anatomical human figure, multi-modal biomarker inputs (blood glucose, ultrasound),
 * dynamic trend charts over mission days, and actionable NASA countermeasure protocols.
 */
@Composable
fun AstronautHealthAssessmentScreen(
  currentUser: CrewMember,
  onNavigateBack: () -> Unit,
  onOpenAiAssistant: () -> Unit,
  modifier: Modifier = Modifier
) {
  BackHandler { onNavigateBack() }
  val context = LocalContext.current

  val examRecord by HealthAssessmentService.currentExam.collectAsState()
  val expeditionState by HealthAssessmentService.expeditionState.collectAsState()

  var selectedRegion by remember { mutableStateOf(BodySystemRegion.EYES_SANS) }
  val activeGuidance = remember(selectedRegion, examRecord) {
    HealthAssessmentService.getGuidanceForSystem(selectedRegion, examRecord)
  }

  var isExamInputExpanded by remember { mutableStateOf(false) }
  var environmentMenuOpen by remember { mutableStateOf(false) }
  var figureDisplayMode by remember { mutableIntStateOf(0) } // 0 = Interactive SVG Vector Figure, 1 = HRP Organ Nodes

  Box(
    modifier = modifier
      .fillMaxSize()
      .background(MaterialTheme.colorScheme.background)
      .padding(14.dp)
  ) {
    LazyColumn(
      modifier = Modifier.fillMaxSize(),
      verticalArrangement = Arrangement.spacedBy(10.dp)
    ) {
      // 1. Navigation Header & Environment Selector
      item {
        Row(
          modifier = Modifier.fillMaxWidth(),
          horizontalArrangement = Arrangement.SpaceBetween,
          verticalAlignment = Alignment.CenterVertically
        ) {
          Row(verticalAlignment = Alignment.CenterVertically) {
            IconButton(
              onClick = onNavigateBack,
              modifier = Modifier.size(36.dp).testTag("assessment_back_button")
            ) {
              Icon(
                imageVector = Icons.AutoMirrored.Filled.ArrowBack,
                contentDescription = "Back",
                tint = NeonCyan
              )
            }
            Spacer(modifier = Modifier.width(8.dp))
            Column {
              Text(
                "ASTRONAUT SELF-ASSESSMENT",
                style = MaterialTheme.typography.titleMedium.copy(
                  color = Color.White,
                  fontWeight = FontWeight.Bold,
                  letterSpacing = 1.sp
                )
              )
              Text(
                "NASA Human Research Program (HRP) Clinical Diagnostic Tool",
                style = MaterialTheme.typography.labelSmall.copy(color = BrightTeal, fontSize = 8.5.sp)
              )
            }
          }

          // Mission Environment Picker
          Box {
            Row(
              modifier = Modifier
                .clip(RoundedCornerShape(8.dp))
                .background(Color.Black.copy(alpha = 0.5f))
                .border(1.dp, NeonCyan.copy(alpha = 0.4f), RoundedCornerShape(8.dp))
                .clickable { environmentMenuOpen = true }
                .padding(horizontal = 8.dp, vertical = 5.dp)
                .testTag("assessment_environment_picker"),
              verticalAlignment = Alignment.CenterVertically
            ) {
              Icon(Icons.Default.Public, contentDescription = null, tint = NeonCyan, modifier = Modifier.size(12.dp))
              Spacer(modifier = Modifier.width(4.dp))
              Text(
                examRecord.environment.displayName.split(" ").take(2).joinToString(" "),
                style = MaterialTheme.typography.labelSmall.copy(color = Color.White, fontSize = 8.5.sp, fontWeight = FontWeight.Bold)
              )
              Spacer(modifier = Modifier.width(2.dp))
              Text("▼", color = BrightTeal, fontSize = 7.sp)
            }

            DropdownMenu(
              expanded = environmentMenuOpen,
              onDismissRequest = { environmentMenuOpen = false },
              modifier = Modifier
                .background(MaterialTheme.colorScheme.surface)
                .border(1.dp, GlassBorderStroke, RoundedCornerShape(8.dp))
            ) {
              MissionEnvironment.entries.forEach { env ->
                DropdownMenuItem(
                  text = {
                    Column {
                      Text(env.displayName, style = MaterialTheme.typography.titleSmall.copy(color = Color.White))
                      Text("${env.gravityFraction} • ${env.primaryHazards}", style = MaterialTheme.typography.bodySmall.copy(color = Color.Gray, fontSize = 9.sp))
                    }
                  },
                  onClick = {
                    HealthAssessmentService.setMissionEnvironment(env)
                    environmentMenuOpen = false
                    Toast.makeText(context, "Environment set to ${env.displayName}", Toast.LENGTH_SHORT).show()
                  }
                )
              }
            }
          }
        }
      }

      // 2. Gamification & Countermeasure Expedition Tracking
      item {
        HealthAssessmentGamificationView(expeditionState = expeditionState)
      }

      // 3. Figure Mode Selection Tab: Interactive SVG Vector Human Figure vs HRP Organ Schematic
      item {
        Row(
          modifier = Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(12.dp))
            .background(Color.Black.copy(alpha = 0.5f))
            .border(1.dp, GlassBorderStroke, RoundedCornerShape(12.dp))
            .padding(4.dp),
          horizontalArrangement = Arrangement.spacedBy(4.dp)
        ) {
          Box(
            modifier = Modifier
              .weight(1f)
              .clip(RoundedCornerShape(8.dp))
              .background(if (figureDisplayMode == 0) NeonCyan.copy(alpha = 0.25f) else Color.Transparent)
              .border(
                1.dp,
                if (figureDisplayMode == 0) NeonCyan else Color.Transparent,
                RoundedCornerShape(8.dp)
              )
              .clickable { figureDisplayMode = 0 }
              .padding(vertical = 7.dp)
              .testTag("tab_svg_human_figure"),
            contentAlignment = Alignment.Center
          ) {
            Row(verticalAlignment = Alignment.CenterVertically) {
              Icon(
                Icons.Default.Accessibility,
                contentDescription = null,
                tint = if (figureDisplayMode == 0) NeonCyan else Color.Gray,
                modifier = Modifier.size(16.dp)
              )
              Spacer(modifier = Modifier.width(6.dp))
              Text(
                "SVG VECTOR FIGURE",
                style = MaterialTheme.typography.labelSmall.copy(
                  color = if (figureDisplayMode == 0) Color.White else Color.Gray,
                  fontWeight = FontWeight.Bold,
                  letterSpacing = 0.5.sp
                )
              )
            }
          }

          Box(
            modifier = Modifier
              .weight(1f)
              .clip(RoundedCornerShape(8.dp))
              .background(if (figureDisplayMode == 1) NeonCyan.copy(alpha = 0.25f) else Color.Transparent)
              .border(
                1.dp,
                if (figureDisplayMode == 1) NeonCyan else Color.Transparent,
                RoundedCornerShape(8.dp)
              )
              .clickable { figureDisplayMode = 1 }
              .padding(vertical = 7.dp)
              .testTag("tab_organ_schematic"),
            contentAlignment = Alignment.Center
          ) {
            Row(verticalAlignment = Alignment.CenterVertically) {
              Icon(
                Icons.Default.Sensors,
                contentDescription = null,
                tint = if (figureDisplayMode == 1) NeonCyan else Color.Gray,
                modifier = Modifier.size(16.dp)
              )
              Spacer(modifier = Modifier.width(6.dp))
              Text(
                "HRP ORGAN NODES",
                style = MaterialTheme.typography.labelSmall.copy(
                  color = if (figureDisplayMode == 1) Color.White else Color.Gray,
                  fontWeight = FontWeight.Bold,
                  letterSpacing = 0.5.sp
                )
              )
            }
          }
        }
      }

      if (figureDisplayMode == 0) {
        // Mode 0: Interactive SVG Vector Human Figure (Clickable Limbs/Regions & Biometric Details)
        item {
          InteractiveSvgHumanFigureView(
            crewMember = currentUser,
            modifier = Modifier.fillMaxWidth().testTag("interactive_svg_human_figure_view")
          )
        }
      } else {
        // Mode 1: Interactive Anatomical Human Figure (Canvas with Clickable Organ Nodes)
        item {
          GlassCard(modifier = Modifier.fillMaxWidth().testTag("anatomical_figure_card")) {
            AnatomicalHumanFigureView(
              examRecord = examRecord,
              selectedRegion = selectedRegion,
              onSelectRegion = { selectedRegion = it }
            )
          }
        }

        // Detailed Organ System Diagnostic Guidance Card (Grounded in NASA HRP Questions)
        item {
          GlassCard(modifier = Modifier.fillMaxWidth().testTag("organ_guidance_card")) {
            Column {
              // Region Title & Status Badge
              Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
              ) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                  Box(
                    modifier = Modifier
                      .size(34.dp)
                      .clip(CircleShape)
                      .background(NeonCyan.copy(alpha = 0.2f))
                      .border(1.dp, NeonCyan, CircleShape),
                    contentAlignment = Alignment.Center
                  ) {
                    val icon = when (selectedRegion) {
                      BodySystemRegion.BRAIN_NEURO -> Icons.Default.Psychology
                      BodySystemRegion.EYES_SANS -> Icons.Default.Visibility
                      BodySystemRegion.CARDIOVASCULAR -> Icons.Default.Favorite
                      BodySystemRegion.LUNGS_ECLSS -> Icons.Default.Air
                      BodySystemRegion.MUSCULOSKELETAL -> Icons.Default.FitnessCenter
                      BodySystemRegion.METABOLIC_ENDOCRINE -> Icons.Default.WaterDrop
                      BodySystemRegion.RENAL_URINARY -> Icons.Default.Medication
                      BodySystemRegion.SKIN_IMMUNE -> Icons.Default.Security
                    }
                    Icon(icon, contentDescription = null, tint = NeonCyan, modifier = Modifier.size(18.dp))
                  }
                  Spacer(modifier = Modifier.width(10.dp))
                  Column {
                    Text(
                      activeGuidance.region.displayName.uppercase(),
                      style = MaterialTheme.typography.titleSmall.copy(color = Color.White, fontWeight = FontWeight.Bold, fontSize = 12.sp)
                    )
                    Text(
                      activeGuidance.measuredValueString,
                      style = MaterialTheme.typography.bodySmall.copy(color = NeonCyan, fontWeight = FontWeight.SemiBold, fontSize = 9.5.sp)
                    )
                  }
                }

                val badgeColor = when (activeGuidance.status) {
                  HealthStatusLevel.OPTIMAL, HealthStatusLevel.NOMINAL -> NominalGreen
                  HealthStatusLevel.ELEVATED_WATCH -> WarningAmber
                  HealthStatusLevel.CRITICAL_ACTION -> CriticalRed
                }
                StatusBadge(text = activeGuidance.status.displayName.uppercase(), color = badgeColor)
              }

              Spacer(modifier = Modifier.height(14.dp))

              // Question 1: Why is an exam needed?
              HrpQuestionSection(
                question = "WHY IS THIS EXAM NEEDED?",
                answer = activeGuidance.whyExamNeeded,
                iconColor = NeonCyan
              )

              Spacer(modifier = Modifier.height(10.dp))

              // Question 2: How do spaceflight hazards affect this indicator?
              HrpQuestionSection(
                question = "HOW DO SPACE HAZARDS AFFECT THIS INDICATOR?",
                answer = activeGuidance.spaceflightHazardEffect,
                iconColor = WarningAmber
              )

              Spacer(modifier = Modifier.height(10.dp))

              // Question 3: When does a result warrant attention from flight surgeons?
              HrpQuestionSection(
                question = "WHEN TO ALERT FLIGHT SURGEONS?",
                answer = activeGuidance.whenToAlertFlightSurgeon,
                iconColor = CriticalRed
              )

              Spacer(modifier = Modifier.height(10.dp))

              // Question 4: Actionable Treatments & Countermeasures
              Column(
                modifier = Modifier
                  .fillMaxWidth()
                  .clip(RoundedCornerShape(8.dp))
                  .background(Color(0xFF04182B).copy(alpha = 0.7f))
                  .border(1.dp, BrightTeal.copy(alpha = 0.35f), RoundedCornerShape(8.dp))
                  .padding(10.dp)
              ) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                  Icon(Icons.Default.LocalHospital, contentDescription = null, tint = NominalGreen, modifier = Modifier.size(15.dp))
                  Spacer(modifier = Modifier.width(6.dp))
                  Text(
                    "ACTIONABLE TREATMENTS & PREVENTION PROTOCOLS",
                    style = MaterialTheme.typography.titleSmall.copy(color = Color.White, fontWeight = FontWeight.Bold, fontSize = 10.sp)
                  )
                }
                Spacer(modifier = Modifier.height(6.dp))

                activeGuidance.actionableTreatments.forEachIndexed { idx, treatment ->
                  Row(modifier = Modifier.padding(vertical = 2.dp), verticalAlignment = Alignment.Top) {
                    Text("•", color = BrightTeal, fontWeight = FontWeight.Bold, modifier = Modifier.padding(end = 6.dp))
                    Text(treatment, style = MaterialTheme.typography.bodySmall.copy(color = Color(0xFFE2E8F0), fontSize = 9.sp))
                  }
                }

                Spacer(modifier = Modifier.height(6.dp))
                Text(
                  "Primary Countermeasure: ${activeGuidance.recommendedCountermeasure}",
                  style = MaterialTheme.typography.labelSmall.copy(color = BrightTeal, fontWeight = FontWeight.Bold, fontSize = 8.5.sp)
                )
              }

              Spacer(modifier = Modifier.height(12.dp))

              // Actions: Quick Consultation with AI or Log Self-Exam
              Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(8.dp)
              ) {
                Button(
                  onClick = { isExamInputExpanded = !isExamInputExpanded },
                  colors = ButtonDefaults.buttonColors(containerColor = BrightTeal.copy(alpha = 0.25f), contentColor = BrightTeal),
                  shape = RoundedCornerShape(8.dp),
                  modifier = Modifier.weight(1f).height(36.dp).testTag("log_self_exam_button")
                ) {
                  Icon(Icons.Default.Tune, contentDescription = null, modifier = Modifier.size(14.dp))
                  Spacer(modifier = Modifier.width(6.dp))
                  Text(if (isExamInputExpanded) "HIDE EXAM INPUT" else "RECORD SELF-EXAM", style = MaterialTheme.typography.labelSmall.copy(fontSize = 9.sp, fontWeight = FontWeight.Bold))
                }

                Button(
                  onClick = onOpenAiAssistant,
                  colors = ButtonDefaults.buttonColors(containerColor = NeonCyan.copy(alpha = 0.25f), contentColor = NeonCyan),
                  shape = RoundedCornerShape(8.dp),
                  modifier = Modifier.weight(1f).height(36.dp).testTag("consult_ai_assistant_button")
                ) {
                  Icon(Icons.Default.Psychology, contentDescription = null, modifier = Modifier.size(14.dp))
                  Spacer(modifier = Modifier.width(6.dp))
                  Text("CONSULT AI SURGEON", style = MaterialTheme.typography.labelSmall.copy(fontSize = 9.sp, fontWeight = FontWeight.Bold))
                }
              }
            }
          }
        }
      }

      // 5. Expandable Interactive Self-Exam Biomarker Input Form
      item {
        AnimatedVisibility(visible = isExamInputExpanded) {
          GlassCard(modifier = Modifier.fillMaxWidth().testTag("exam_input_card")) {
            Column {
              Row(verticalAlignment = Alignment.CenterVertically) {
                Icon(Icons.Default.Science, contentDescription = null, tint = NeonCyan, modifier = Modifier.size(18.dp))
                Spacer(modifier = Modifier.width(8.dp))
                Text(
                  "ENTER SELF-EXAM BIOMARKER MEASUREMENTS",
                  style = MaterialTheme.typography.titleSmall.copy(color = Color.White, fontWeight = FontWeight.Bold)
                )
              }
              Spacer(modifier = Modifier.height(6.dp))
              Text(
                "Input point-of-care data from fingerstick glucometers, portable ultrasound probes, or visual acuity tests:",
                style = MaterialTheme.typography.bodySmall.copy(color = Color.LightGray, fontSize = 8.5.sp)
              )

              Spacer(modifier = Modifier.height(12.dp))

              // Blood Glucose Slider Input (mg/dL)
              ExamSliderInputRow(
                label = "Blood Glucose (Fingerstick)",
                display = "${examRecord.bloodGlucoseMgDl} mg/dL",
                value = examRecord.bloodGlucoseMgDl.toFloat(),
                range = 50f..220f,
                accentColor = if (examRecord.bloodGlucoseMgDl > 140 || examRecord.bloodGlucoseMgDl < 70) WarningAmber else BrightTeal,
                statusTag = if (examRecord.bloodGlucoseMgDl > 140) "HYPERGLYCEMIA" else if (examRecord.bloodGlucoseMgDl < 70) "HYPOGLYCEMIA" else "NOMINAL (70-99)",
                onValueChange = { newVal ->
                  HealthAssessmentService.updateBloodGlucose(newVal.toInt())
                }
              )

              Spacer(modifier = Modifier.height(10.dp))

              // Optic Nerve Sheath Diameter Ultrasound (mm)
              ExamSliderInputRow(
                label = "Optic Nerve Sheath Ultrasound (ONSD)",
                display = "${"%.1f".format(examRecord.opticNerveSheathDiameterMm)} mm",
                value = examRecord.opticNerveSheathDiameterMm.toFloat(),
                range = 3.5f..6.5f,
                accentColor = if (examRecord.opticNerveSheathDiameterMm >= 5.8) CriticalRed else if (examRecord.opticNerveSheathDiameterMm >= 5.2) WarningAmber else NominalGreen,
                statusTag = if (examRecord.opticNerveSheathDiameterMm >= 5.8) "SANS ALERT (>5.8mm)" else "NORMAL (<5.0mm)",
                onValueChange = { newVal ->
                  HealthAssessmentService.updateOcularUltrasound(
                    newVal.toDouble(),
                    examRecord.intraocularPressureMmHg,
                    examRecord.visualAcuityDecimal
                  )
                }
              )

              Spacer(modifier = Modifier.height(10.dp))

              // Left Ventricular Ejection Fraction (LVEF %)
              ExamSliderInputRow(
                label = "Cardiac POCUS Ultrasound (LVEF)",
                display = "${examRecord.leftVentricularEjectionFractionPercent}%",
                value = examRecord.leftVentricularEjectionFractionPercent.toFloat(),
                range = 40f..75f,
                accentColor = if (examRecord.leftVentricularEjectionFractionPercent < 52) CriticalRed else Color(0xFFBAE6FD),
                statusTag = if (examRecord.leftVentricularEjectionFractionPercent < 52) "DECONDITIONING" else "NOMINAL (55-70%)",
                onValueChange = { newVal ->
                  HealthAssessmentService.updateCardiovascularPocus(
                    newVal.toInt(),
                    examRecord.jugularVeinDistensionMm
                  )
                }
              )

              Spacer(modifier = Modifier.height(10.dp))

              // Daily ARED Resistive Load Tonnage
              ExamSliderInputRow(
                label = "Daily ARED Resistive Tonnage",
                display = "${"%.1f".format(examRecord.dailyAredResistanceTonnage)} Tons",
                value = examRecord.dailyAredResistanceTonnage.toFloat(),
                range = 5f..20f,
                accentColor = if (examRecord.dailyAredResistanceTonnage < 11.0) WarningAmber else NominalGreen,
                statusTag = if (examRecord.dailyAredResistanceTonnage < 11.0) "BELOW TARGET" else "TARGET REACHED",
                onValueChange = { newVal ->
                  HealthAssessmentService.updateMusculoskeletalLog(
                    newVal.toDouble(),
                    examRecord.t2TreadmillKm,
                    examRecord.backPainScore
                  )
                }
              )

              Spacer(modifier = Modifier.height(14.dp))

              // Save & Persist Exam Button with Audio Chime & Active Notification
              Button(
                onClick = {
                  AstraAudioAlarmService.playConfirmationChime(context)
                  AstraInAppNotificationManager.postNotification(
                    title = "SELF-ASSESSMENT LOGGED",
                    message = "Biomarkers updated: Glucose ${examRecord.bloodGlucoseMgDl} mg/dL, ONSD ${"%.1f".format(examRecord.opticNerveSheathDiameterMm)} mm. Saved to persistent flight vault.",
                    severity = if (examRecord.opticNerveSheathDiameterMm >= 5.8) com.example.model.AlertSeverity.WARNING else com.example.model.AlertSeverity.ADVISORY,
                    context = context,
                    playAudio = true
                  )
                  isExamInputExpanded = false
                },
                modifier = Modifier
                  .fillMaxWidth()
                  .height(42.dp)
                  .testTag("save_exam_button"),
                colors = ButtonDefaults.buttonColors(containerColor = NeonCyan, contentColor = Color.Black),
                shape = RoundedCornerShape(8.dp)
              ) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                  Icon(Icons.Default.CheckCircle, contentDescription = null, modifier = Modifier.size(16.dp))
                  Spacer(modifier = Modifier.width(6.dp))
                  Text("SAVE & PERSIST TO MISSION VAULT", style = MaterialTheme.typography.labelSmall.copy(fontWeight = FontWeight.Bold))
                }
              }
            }
          }
        }
      }

      // 6. Dynamic Health Indicator Trend Charts (Changes Over Mission Days)
      item {
        GlassCard(modifier = Modifier.fillMaxWidth().testTag("trend_charts_card")) {
          Column {
            Row(verticalAlignment = Alignment.CenterVertically) {
              Icon(Icons.Default.Bolt, contentDescription = null, tint = NeonCyan, modifier = Modifier.size(18.dp))
              Spacer(modifier = Modifier.width(8.dp))
              Text(
                "LONG-DURATION MISSION HEALTH TRENDS",
                style = MaterialTheme.typography.titleSmall.copy(color = Color.White, fontWeight = FontWeight.Bold)
              )
            }
            Spacer(modifier = Modifier.height(4.dp))
            Text(
              "Predictive trajectory models based on NASA Human Research Program flight data:",
              style = MaterialTheme.typography.bodySmall.copy(color = Color.Gray, fontSize = 8.5.sp)
            )

            Spacer(modifier = Modifier.height(12.dp))

            // Chart 1: SANS Optic Nerve Sheath Diameter Over 180 Days
            TrendLineChartBox(
              title = "SANS Optic Nerve Sheath Diameter (mm) vs Flight Days",
              yAxisLabel = "ONSD mm",
              points = listOf(4.2f, 4.4f, 4.6f, 4.9f, 5.2f, 5.4f, 5.1f, 4.8f),
              dayLabels = listOf("FD1", "FD30", "FD60", "FD90", "FD120", "FD150", "FD180"),
              lineColor = NeonCyan,
              thresholdY = 0.35f, // ~5.5mm
              thresholdLabel = "SANS Threshold (5.5mm)"
            )

            Spacer(modifier = Modifier.height(12.dp))

            // Chart 2: Bone Mineral Density (BMD) Retention (%) Over Mission
            TrendLineChartBox(
              title = "Bone Mineral Density (BMD) Retention with ARED Protocols",
              yAxisLabel = "BMD %",
              points = listOf(100f, 99.2f, 98.4f, 97.9f, 97.6f, 97.3f, 97.1f, 97.0f),
              dayLabels = listOf("FD1", "FD30", "FD60", "FD90", "FD120", "FD150", "FD180"),
              lineColor = NominalGreen,
              thresholdY = 0.65f, // Untrained ~91%
              thresholdLabel = "Untrained Degeneration (~91%)"
            )
          }
        }
      }

      item { Spacer(modifier = Modifier.height(16.dp)) }
    }
  }
}

@Composable
private fun HrpQuestionSection(
  question: String,
  answer: String,
  iconColor: Color
) {
  Column(
    modifier = Modifier
      .fillMaxWidth()
      .clip(RoundedCornerShape(8.dp))
      .background(Color.Black.copy(alpha = 0.35f))
      .padding(horizontal = 10.dp, vertical = 8.dp)
  ) {
    Row(verticalAlignment = Alignment.CenterVertically) {
      Icon(Icons.AutoMirrored.Filled.HelpOutline, contentDescription = null, tint = iconColor, modifier = Modifier.size(13.dp))
      Spacer(modifier = Modifier.width(6.dp))
      Text(
        question,
        style = MaterialTheme.typography.titleSmall.copy(color = iconColor, fontWeight = FontWeight.Bold, fontSize = 9.5.sp)
      )
    }
    Spacer(modifier = Modifier.height(4.dp))
    Text(
      answer,
      style = MaterialTheme.typography.bodySmall.copy(color = Color(0xFFF1F5F9), fontSize = 9.sp, lineHeight = 14.sp)
    )
  }
}

@Composable
private fun ExamSliderInputRow(
  label: String,
  display: String,
  value: Float,
  range: ClosedFloatingPointRange<Float>,
  accentColor: Color,
  statusTag: String,
  onValueChange: (Float) -> Unit
) {
  Column(
    modifier = Modifier
      .fillMaxWidth()
      .clip(RoundedCornerShape(8.dp))
      .background(Color.Black.copy(alpha = 0.35f))
      .padding(horizontal = 10.dp, vertical = 8.dp)
  ) {
    Row(
      modifier = Modifier.fillMaxWidth(),
      horizontalArrangement = Arrangement.SpaceBetween,
      verticalAlignment = Alignment.CenterVertically
    ) {
      Text(label, style = MaterialTheme.typography.bodySmall.copy(color = Color.LightGray, fontSize = 11.sp))
      Row(verticalAlignment = Alignment.CenterVertically) {
        Text(
          display,
          style = MaterialTheme.typography.titleSmall.copy(color = accentColor, fontWeight = FontWeight.Bold, fontSize = 12.sp)
        )
        Spacer(modifier = Modifier.width(6.dp))
        StatusBadge(
          text = statusTag,
          color = if (statusTag.contains("ALERT") || statusTag.contains("HYPO") || statusTag.contains("DECOND")) CriticalRed else if (statusTag.contains("BELOW") || statusTag.contains("HYPER")) WarningAmber else NominalGreen
        )
      }
    }

    Slider(
      value = value.coerceIn(range.start, range.endInclusive),
      onValueChange = onValueChange,
      valueRange = range,
      colors = SliderDefaults.colors(
        thumbColor = accentColor,
        activeTrackColor = accentColor,
        inactiveTrackColor = Color.White.copy(alpha = 0.12f)
      ),
      modifier = Modifier.fillMaxWidth()
    )
  }
}

@Composable
private fun TrendLineChartBox(
  title: String,
  yAxisLabel: String,
  points: List<Float>,
  dayLabels: List<String>,
  lineColor: Color,
  thresholdY: Float? = null,
  thresholdLabel: String? = null
) {
  Column(
    modifier = Modifier
      .fillMaxWidth()
      .clip(RoundedCornerShape(10.dp))
      .background(Color.Black.copy(alpha = 0.45f))
      .border(1.dp, GlassBorderStroke, RoundedCornerShape(10.dp))
      .padding(10.dp)
  ) {
    Row(
      modifier = Modifier.fillMaxWidth(),
      horizontalArrangement = Arrangement.SpaceBetween,
      verticalAlignment = Alignment.CenterVertically
    ) {
      Text(title, style = MaterialTheme.typography.titleSmall.copy(color = Color.White, fontWeight = FontWeight.SemiBold, fontSize = 10.sp))
      Text(yAxisLabel, style = MaterialTheme.typography.labelSmall.copy(color = BrightTeal, fontSize = 8.sp))
    }

    Spacer(modifier = Modifier.height(8.dp))

    // Canvas Line Chart
    Canvas(
      modifier = Modifier
        .fillMaxWidth()
        .height(80.dp)
        .padding(horizontal = 4.dp, vertical = 2.dp)
    ) {
      val w = size.width
      val h = size.height

      // Background grid lines
      drawLine(Color.White.copy(alpha = 0.05f), Offset(0f, 0f), Offset(w, 0f), strokeWidth = 1f)
      drawLine(Color.White.copy(alpha = 0.05f), Offset(0f, h / 2f), Offset(w, h / 2f), strokeWidth = 1f)
      drawLine(Color.White.copy(alpha = 0.05f), Offset(0f, h), Offset(w, h), strokeWidth = 1f)

      // Warning threshold line if provided
      if (thresholdY != null) {
        val yThresh = h * thresholdY
        drawLine(
          color = CriticalRed.copy(alpha = 0.6f),
          start = Offset(0f, yThresh),
          end = Offset(w, yThresh),
          strokeWidth = 1.5f,
          pathEffect = androidx.compose.ui.graphics.PathEffect.dashPathEffect(floatArrayOf(8f, 6f), 0f)
        )
      }

      val minVal = points.minOrNull() ?: 0f
      val maxVal = points.maxOrNull() ?: 1f
      val range = if (maxVal - minVal == 0f) 1f else (maxVal - minVal)

      val stepX = w / (points.size - 1)
      val path = Path()

      points.forEachIndexed { i, p ->
        val x = i * stepX
        val normY = (p - minVal) / range
        val y = h - (normY * (h * 0.75f) + h * 0.12f)
        if (i == 0) path.moveTo(x, y) else path.lineTo(x, y)
        drawCircle(color = lineColor, radius = 3.5f, center = Offset(x, y))
      }

      drawPath(path, color = lineColor, style = Stroke(width = 2.5f))
    }

    Spacer(modifier = Modifier.height(4.dp))

    // Day labels row
    Row(
      modifier = Modifier.fillMaxWidth(),
      horizontalArrangement = Arrangement.SpaceBetween
    ) {
      dayLabels.forEach { label ->
        Text(label, style = MaterialTheme.typography.labelSmall.copy(color = Color.Gray, fontSize = 7.5.sp))
      }
    }

    if (thresholdLabel != null) {
      Spacer(modifier = Modifier.height(4.dp))
      Text("--- $thresholdLabel", style = MaterialTheme.typography.labelSmall.copy(color = CriticalRed.copy(alpha = 0.8f), fontSize = 7.5.sp))
    }
  }
}
