package com.example.ui.screens

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
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
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.Air
import androidx.compose.material.icons.filled.AssignmentTurnedIn
import androidx.compose.material.icons.filled.Bedtime
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.DirectionsRun
import androidx.compose.material.icons.filled.Mail
import androidx.compose.material.icons.filled.MedicalServices
import androidx.compose.material.icons.filled.Psychology
import androidx.compose.material.icons.filled.Shield
import androidx.compose.material.icons.filled.Speed
import androidx.compose.material.icons.filled.Warning
import androidx.compose.material.icons.filled.WaterDrop
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.data.AstraRepository
import com.example.model.AlertLayer
import com.example.model.AlertSeverity
import com.example.model.CrewMember
import com.example.model.DestinationType
import com.example.ui.components.AnatomicalBiometricView
import com.example.ui.components.BiometricSensorRadarCard
import com.example.ui.components.GlassCard
import com.example.ui.components.ShiftTimelineComponent
import com.example.ui.components.Spacecraft3DCanvas
import com.example.ui.components.StatusBadge
import com.example.ui.components.TopMissionBar
import androidx.compose.material.icons.filled.Public
import com.example.ui.theme.BrightTeal
import com.example.ui.theme.CelestialAmber
import com.example.ui.theme.CriticalRed
import com.example.ui.theme.NeonCyan
import com.example.ui.theme.NominalGreen
import com.example.ui.theme.StarlightBlue
import com.example.ui.theme.WarningAmber

@Composable
fun CrewPortalScreen(
  crewMember: CrewMember,
  currentDestination: DestinationType,
  onDestinationSelected: (DestinationType) -> Unit,
  isDarkMode: Boolean,
  onToggleTheme: () -> Unit,
  onOpenPvtTest: () -> Unit,
  onOpenAiMedicalChat: () -> Unit,
  onOpenEarthSync: () -> Unit,
  onOpenSettings: () -> Unit,
  onSwitchPortal: () -> Unit,
  onOpenHealthAssessment: () -> Unit = {},
  onOpenNasaOpenData: () -> Unit = {},
  modifier: Modifier = Modifier
) {
  val allCrewMembers by AstraRepository.crewMembers.collectAsState()
  var currentViewingCrew by remember(crewMember) { mutableStateOf(crewMember) }

  val cabinEnv by AstraRepository.cabinEnvironment.collectAsState()
  val allAlerts by AstraRepository.safetyAlerts.collectAsState()
  val allMessages by AstraRepository.messages.collectAsState()
  val unsyncedPackets by AstraRepository.unsyncedPacketsCount.collectAsState()
  val allShiftEvents by AstraRepository.shiftEvents.collectAsState()

  // Filter 24-hour shift events for this astronaut or broadcast ship-wide
  val crewShiftEvents = remember(allShiftEvents, currentViewingCrew.id) {
    allShiftEvents.filter { it.astronautId == currentViewingCrew.id || it.astronautId == "ALL" }
  }

  // Filter alerts relevant to this astronaut or ship-wide
  val crewAlerts = remember(allAlerts, currentViewingCrew.id) {
    allAlerts.filter { it.targetCrewMemberId == null || it.targetCrewMemberId == currentViewingCrew.id }
  }

  // Filter messages for this astronaut or broadcast "ALL"
  val myMessages = remember(allMessages, currentViewingCrew.id) {
    allMessages.filter { it.recipientId == "ALL" || it.recipientId == currentViewingCrew.id }
  }

  val missionRisk = remember(currentViewingCrew, cabinEnv) {
    AstraRepository.getMissionRiskScore(currentViewingCrew, cabinEnv)
  }
  val riskScore = missionRisk.compositeScore
  val riskLevel = missionRisk.riskLevel

  var mentalCheckInCompleted by remember { mutableStateOf(false) }

  // Handle Android system back gesture to navigate to Portal Selection Gateway
  androidx.activity.compose.BackHandler {
    onSwitchPortal()
  }

  LazyColumn(
    modifier = modifier
      .fillMaxSize()
      .background(MaterialTheme.colorScheme.background)
      .padding(horizontal = 14.dp, vertical = 10.dp),
    verticalArrangement = Arrangement.spacedBy(14.dp)
  ) {

    // Top Mission Navigation Bar
    item {
      TopMissionBar(
        currentDestination = currentDestination,
        onDestinationSelected = onDestinationSelected,
        currentUser = currentViewingCrew,
        isDarkMode = isDarkMode,
        onToggleTheme = onToggleTheme,
        unsyncedPackets = unsyncedPackets,
        onOpenSync = onOpenEarthSync,
        onOpenProfile = onOpenSettings,
        onSwitchPortal = onSwitchPortal,
        activePortalName = "CREW PORTAL (INDIVIDUAL ACCESS)"
      )
    }

    // Switch Portal Access Mode Quick-Return Banner
    item {
      Row(
        modifier = Modifier
          .fillMaxWidth()
          .clip(RoundedCornerShape(8.dp))
          .background(Color(0xFF0F172A).copy(alpha = 0.85f))
          .border(1.dp, BrightTeal.copy(alpha = 0.4f), RoundedCornerShape(8.dp))
          .clickable { onSwitchPortal() }
          .padding(horizontal = 12.dp, vertical = 8.dp)
          .testTag("crew_switch_portal_header_button"),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically
      ) {
        Row(verticalAlignment = Alignment.CenterVertically) {
          Icon(
            imageVector = Icons.AutoMirrored.Filled.ArrowBack,
            contentDescription = "Switch Portal",
            tint = BrightTeal,
            modifier = Modifier.size(14.dp)
          )
          Spacer(modifier = Modifier.width(7.dp))
          Text(
            "SWITCH ACCESS PORTAL",
            style = MaterialTheme.typography.labelSmall.copy(
              color = BrightTeal,
              fontWeight = FontWeight.Bold,
              fontSize = 9.5.sp,
              letterSpacing = 0.5.sp
            )
          )
        }
        Text(
          "PORTAL GATEWAY ➔",
          style = MaterialTheme.typography.labelSmall.copy(
            color = Color(0xFF94A3B8),
            fontWeight = FontWeight.SemiBold,
            fontSize = 8.5.sp
          )
        )
      }
    }

    // High-Priority Emergency SOS Card & Automated Broadcast
    item {
      com.example.ui.components.EmergencySosCard(
        currentUser = currentViewingCrew,
        onTriggerSos = {
          AstraRepository.triggerEmergencySos(currentViewingCrew)
        },
        onDeactivateSos = {
          AstraRepository.deactivateEmergencySos()
        }
      )
    }

    // Visual Tool: Astronaut Self-Assessment & Interactive Anatomical Scan
    item {
      GlassCard(
        modifier = Modifier.fillMaxWidth().testTag("launch_health_assessment_card"),
        cornerRadius = 16.dp
      ) {
        Row(
          modifier = Modifier.fillMaxWidth(),
          horizontalArrangement = Arrangement.SpaceBetween,
          verticalAlignment = Alignment.CenterVertically
        ) {
          Row(verticalAlignment = Alignment.CenterVertically, modifier = Modifier.weight(1f)) {
            Box(
              modifier = Modifier
                .size(40.dp)
                .clip(CircleShape)
                .background(NeonCyan.copy(alpha = 0.2f))
                .border(1.dp, NeonCyan, CircleShape),
              contentAlignment = Alignment.Center
            ) {
              Icon(Icons.Default.Psychology, contentDescription = null, tint = NeonCyan, modifier = Modifier.size(20.dp))
            }
            Spacer(modifier = Modifier.width(10.dp))
            Column(modifier = Modifier.weight(1f)) {
              Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(6.dp)
              ) {
                Text(
                  "HEALTH ASSESSMENT",
                  style = MaterialTheme.typography.titleSmall.copy(color = Color.White, fontWeight = FontWeight.Bold, fontSize = 11.sp),
                  maxLines = 1,
                  overflow = TextOverflow.Ellipsis,
                  modifier = Modifier.weight(1f, fill = false)
                )
                StatusBadge(text = "NASA HRP", color = BrightTeal)
              }
              Spacer(modifier = Modifier.height(2.dp))
              Text(
                "Interactive Anatomical Scan • Blood Glucose • Ultrasound",
                style = MaterialTheme.typography.bodySmall.copy(color = Color.LightGray, fontSize = 8.sp),
                maxLines = 1,
                overflow = TextOverflow.Ellipsis
              )
            }
          }

          Spacer(modifier = Modifier.width(8.dp))

          Button(
            onClick = onOpenHealthAssessment,
            colors = ButtonDefaults.buttonColors(containerColor = NeonCyan, contentColor = Color.Black),
            shape = RoundedCornerShape(8.dp),
            modifier = Modifier.height(34.dp).testTag("open_self_assessment_button")
          ) {
            Text("LAUNCH SCAN", style = MaterialTheme.typography.labelSmall.copy(fontWeight = FontWeight.Black, fontSize = 8.5.sp))
          }
        }
      }
    }

    // NASA Open Data & Space Weather Telemetry Card (data.nasa.gov & DONKI)
    item {
      GlassCard(
        modifier = Modifier.fillMaxWidth().testTag("crew_nasa_open_data_card"),
        cornerRadius = 14.dp
      ) {
        Row(
          modifier = Modifier.fillMaxWidth(),
          horizontalArrangement = Arrangement.SpaceBetween,
          verticalAlignment = Alignment.CenterVertically
        ) {
          Row(verticalAlignment = Alignment.CenterVertically, modifier = Modifier.weight(1f)) {
            Box(
              modifier = Modifier
                .size(40.dp)
                .clip(CircleShape)
                .background(CelestialAmber.copy(alpha = 0.2f))
                .border(1.dp, CelestialAmber, CircleShape),
              contentAlignment = Alignment.Center
            ) {
              Icon(Icons.Default.Public, contentDescription = null, tint = CelestialAmber, modifier = Modifier.size(20.dp))
            }
            Spacer(modifier = Modifier.width(10.dp))
            Column(modifier = Modifier.weight(1f)) {
              Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(6.dp)
              ) {
                Text(
                  "NASA OPEN DATA",
                  style = MaterialTheme.typography.titleSmall.copy(color = Color.White, fontWeight = FontWeight.Bold, fontSize = 11.sp),
                  maxLines = 1,
                  overflow = TextOverflow.Ellipsis,
                  modifier = Modifier.weight(1f, fill = false)
                )
                StatusBadge(text = "DATA.NASA.GOV", color = CelestialAmber)
              }
              Spacer(modifier = Modifier.height(2.dp))
              Text(
                "EVA Spacewalk Logs • DONKI Solar Alerts • 5 Hazards",
                style = MaterialTheme.typography.bodySmall.copy(color = Color.LightGray, fontSize = 8.sp),
                maxLines = 1,
                overflow = TextOverflow.Ellipsis
              )
            }
          }

          Spacer(modifier = Modifier.width(8.dp))

          Button(
            onClick = onOpenNasaOpenData,
            colors = ButtonDefaults.buttonColors(containerColor = CelestialAmber, contentColor = Color.Black),
            shape = RoundedCornerShape(8.dp),
            modifier = Modifier.height(34.dp).testTag("open_nasa_open_data_button")
          ) {
            Text("EXPLORE", style = MaterialTheme.typography.labelSmall.copy(fontWeight = FontWeight.Black, fontSize = 8.5.sp))
          }
        }
      }
    }

    // Role & Astronaut Title Hero Card (inspired by NOVA reference design)
    item {
      GlassCard(
        modifier = Modifier.fillMaxWidth().testTag("crew_hero_card"),
        cornerRadius = 20.dp
      ) {
        Column {
          Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
          ) {
            StatusBadge(text = "CREW PORTAL • INDIVIDUAL ACCESS", color = BrightTeal)
            Text(
              "BADGE: ${currentViewingCrew.id}",
              style = MaterialTheme.typography.labelSmall.copy(color = Color.LightGray)
            )
          }

          Spacer(modifier = Modifier.height(10.dp))

          Text(
            text = "MONITOR. ENDURE. THRIVE.",
            style = MaterialTheme.typography.displayMedium.copy(
              color = Color.White,
              letterSpacing = 1.5.sp
            )
          )

          Spacer(modifier = Modifier.height(4.dp))

          Text(
            text = "Onboard individual biometrics, radiation exposure, and circadian safety for ${currentViewingCrew.fullName} (${currentViewingCrew.missionSpecialty}).",
            style = MaterialTheme.typography.bodyMedium.copy(color = Color(0xFF94A3B8))
          )

          Spacer(modifier = Modifier.height(10.dp))

          // Switch active crew individual view
          Text("SELECT CREW PROFILE TO VIEW:", style = MaterialTheme.typography.labelSmall.copy(color = Color.Gray, fontSize = 8.sp))
          Spacer(modifier = Modifier.height(4.dp))
          Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(6.dp)) {
            allCrewMembers.forEach { member ->
              val isSel = member.id == currentViewingCrew.id
              Box(
                modifier = Modifier
                  .weight(1f)
                  .clip(RoundedCornerShape(8.dp))
                  .background(if (isSel) BrightTeal.copy(alpha = 0.25f) else Color.Black.copy(alpha = 0.35f))
                  .border(1.dp, if (isSel) BrightTeal else Color.White.copy(alpha = 0.08f), RoundedCornerShape(8.dp))
                  .clickable { currentViewingCrew = member }
                  .padding(vertical = 6.dp),
                contentAlignment = Alignment.Center
              ) {
                Text(
                  member.fullName.split(" ").last(),
                  style = MaterialTheme.typography.labelSmall.copy(
                    color = if (isSel) BrightTeal else Color.LightGray,
                    fontSize = 9.sp,
                    fontWeight = if (isSel) androidx.compose.ui.text.font.FontWeight.Bold else androidx.compose.ui.text.font.FontWeight.Normal
                  )
                )
              }
            }
          }

          Spacer(modifier = Modifier.height(14.dp))

          // Quick Action Launcher Pills
          Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(8.dp)
          ) {
            Button(
              onClick = onOpenAiMedicalChat,
              modifier = Modifier.weight(1f).height(42.dp).testTag("quick_ai_consult_button"),
              colors = ButtonDefaults.buttonColors(
                containerColor = if (riskLevel == com.example.model.RiskLevel.HIGH) CriticalRed else NeonCyan,
                contentColor = if (riskLevel == com.example.model.RiskLevel.HIGH) Color.White else Color.Black
              ),
              shape = RoundedCornerShape(12.dp)
            ) {
              Icon(Icons.Default.MedicalServices, contentDescription = null, modifier = Modifier.size(16.dp))
              Spacer(modifier = Modifier.width(6.dp))
              Text(
                if (riskLevel == com.example.model.RiskLevel.HIGH) "EMERGENCY AI" else "EMERGENCY AI CO-PILOT",
                style = MaterialTheme.typography.labelSmall.copy(fontWeight = androidx.compose.ui.text.font.FontWeight.Bold)
              )
            }

            Button(
              onClick = onOpenPvtTest,
              modifier = Modifier.weight(1f).height(42.dp).testTag("quick_pvt_test_button"),
              colors = ButtonDefaults.buttonColors(containerColor = StarlightBlue.copy(alpha = 0.25f), contentColor = Color.White),
              border = androidx.compose.foundation.BorderStroke(1.dp, StarlightBlue),
              shape = RoundedCornerShape(12.dp)
            ) {
              Icon(Icons.Default.Speed, contentDescription = null, tint = NeonCyan, modifier = Modifier.size(16.dp))
              Spacer(modifier = Modifier.width(6.dp))
              Text("30s PVT TEST", style = MaterialTheme.typography.labelSmall)
            }
          }
        }
      }
    }

    // Two-Layer AI Safety System (Layer 1 Instant Alerts & Layer 2 AI Assistant Directives)
    item {
      Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
        Row(
          modifier = Modifier.fillMaxWidth(),
          horizontalArrangement = Arrangement.SpaceBetween,
          verticalAlignment = Alignment.CenterVertically
        ) {
          Text(
            text = "TWO-LAYER AI SAFETY SYSTEM",
            style = MaterialTheme.typography.titleMedium.copy(color = NeonCyan, letterSpacing = 1.sp)
          )
          StatusBadge(
            text = "${crewAlerts.size} ACTIVE NOTICES",
            color = if (crewAlerts.any { it.severity == AlertSeverity.CRITICAL }) CriticalRed else BrightTeal
          )
        }

        if (crewAlerts.isEmpty()) {
          GlassCard(modifier = Modifier.fillMaxWidth()) {
            Row(verticalAlignment = Alignment.CenterVertically) {
              Icon(Icons.Default.CheckCircle, contentDescription = null, tint = NominalGreen, modifier = Modifier.size(20.dp))
              Spacer(modifier = Modifier.width(10.dp))
              Text("All Layer 1 hardware rules and Layer 2 AI patterns nominal.", color = Color(0xFFCBD5E1))
            }
          }
        } else {
          crewAlerts.forEach { alert ->
            val alertBorder = when (alert.severity) {
              AlertSeverity.CRITICAL -> CriticalRed
              AlertSeverity.WARNING -> WarningAmber
              AlertSeverity.ADVISORY -> NeonCyan
            }

            GlassCard(
              modifier = Modifier.fillMaxWidth().testTag("alert_card_${alert.id}"),
              borderColor = alertBorder.copy(alpha = 0.7f),
              backgroundColor = alertBorder.copy(alpha = 0.12f)
            ) {
              Column {
                Row(
                  modifier = Modifier.fillMaxWidth(),
                  horizontalArrangement = Arrangement.SpaceBetween,
                  verticalAlignment = Alignment.CenterVertically
                ) {
                  Row(verticalAlignment = Alignment.CenterVertically) {
                    Icon(
                      imageVector = if (alert.severity == AlertSeverity.CRITICAL) Icons.Default.Warning else Icons.Default.Shield,
                      contentDescription = null,
                      tint = alertBorder,
                      modifier = Modifier.size(16.dp)
                    )
                    Spacer(modifier = Modifier.width(6.dp))
                    Text(
                      text = if (alert.layer == AlertLayer.LAYER_1_HARDWARE_RULES) "LAYER 1: INSTANT RULE" else "LAYER 2: AI ASSISTANT",
                      style = MaterialTheme.typography.labelSmall.copy(color = alertBorder)
                    )
                  }
                  Text(alert.timestamp, style = MaterialTheme.typography.labelSmall.copy(color = Color.Gray))
                }

                Spacer(modifier = Modifier.height(6.dp))

                Text(
                  text = alert.title,
                  style = MaterialTheme.typography.titleMedium.copy(color = Color.White)
                )

                Spacer(modifier = Modifier.height(4.dp))

                Text(
                  text = alert.description,
                  style = MaterialTheme.typography.bodyMedium.copy(color = Color(0xFFE2E8F0))
                )

                Spacer(modifier = Modifier.height(8.dp))

                // Clear Action Directive instead of complicated charts
                Box(
                  modifier = Modifier
                    .fillMaxWidth()
                    .clip(RoundedCornerShape(8.dp))
                    .background(Color.Black.copy(alpha = 0.4f))
                    .border(1.dp, alertBorder.copy(alpha = 0.4f), RoundedCornerShape(8.dp))
                    .padding(8.dp)
                ) {
                  Text(
                    text = "ACTION: ${alert.actionDirective}",
                    style = MaterialTheme.typography.bodyMedium.copy(
                      color = alertBorder,
                      fontWeight = androidx.compose.ui.text.font.FontWeight.SemiBold
                    )
                  )
                }
              }
            }
          }
        }
      }
    }

    // Unified Mission Risk Score (Aggregated Health, Radiation, Fatigue Gauge Visualization)
    item {
      com.example.ui.components.MissionRiskGaugeCard(
        missionRisk = missionRisk,
        astronautName = currentViewingCrew.fullName,
        modifier = Modifier.fillMaxWidth()
      )
    }

    // Combined Health & Radiation Risk Score and Biometric Wireframe
    item {
      AnatomicalBiometricView(
        crewMember = currentViewingCrew,
        compositeRiskScore = riskScore,
        riskLevel = riskLevel,
        modifier = Modifier.fillMaxWidth()
      )
    }

    // 24-Hour Vertical Biometric, Medication & Emergency Timeline
    item {
      ShiftTimelineComponent(
        events = crewShiftEvents,
        astronautName = currentViewingCrew.fullName,
        astronautId = currentViewingCrew.id,
        onLogNewEvent = { newEvent ->
          AstraRepository.logShiftEvent(newEvent)
        },
        modifier = Modifier.fillMaxWidth()
      )
    }

    // Spacecraft Cabin Environmental & Safety Sensors
    item {
      GlassCard(modifier = Modifier.fillMaxWidth()) {
        Column {
          Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
          ) {
            Column {
              Text(
                "SPACECRAFT CABIN ENVIRONMENT",
                style = MaterialTheme.typography.labelMedium.copy(color = BrightTeal)
              )
              Text(
                "${currentDestination.habitatVehicle} • SENSORS",
                style = MaterialTheme.typography.titleMedium.copy(color = Color.White)
              )
            }
            StatusBadge(
              text = if (cabinEnv.cabinPressureKpa >= 98.0) "ECLSS NOMINAL" else "PRESSURE ALERT",
              color = if (cabinEnv.cabinPressureKpa >= 98.0) NominalGreen else CriticalRed
            )
          }

          Spacer(modifier = Modifier.height(12.dp))

          Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(8.dp)) {
            EnvironmentalSensorCard(
              label = "CABIN PRESSURE",
              value = "${cabinEnv.cabinPressureKpa} kPa",
              subtext = "Nominal: 101.3 kPa",
              isAlert = cabinEnv.cabinPressureKpa < 95.0,
              modifier = Modifier.weight(1f)
            )
            EnvironmentalSensorCard(
              label = "CO₂ LEVEL",
              value = "${cabinEnv.co2Ppm} ppm",
              subtext = "Threshold: < 3000 ppm",
              isAlert = cabinEnv.co2Ppm > 4500,
              modifier = Modifier.weight(1f)
            )
          }

          Spacer(modifier = Modifier.height(8.dp))

          Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(8.dp)) {
            EnvironmentalSensorCard(
              label = "O₂ FRACTION",
              value = "${cabinEnv.o2Percentage}%",
              subtext = "Sea level equiv",
              isAlert = cabinEnv.o2Percentage < 19.5,
              modifier = Modifier.weight(1f)
            )
            EnvironmentalSensorCard(
              label = "RADIATION FLUX",
              value = "${cabinEnv.radiationRateMsvPerHour} mSv/h",
              subtext = "Cosmic + Solar",
              isAlert = cabinEnv.radiationRateMsvPerHour > 1.0,
              modifier = Modifier.weight(1f)
            )
          }
        }
      }
    }

    // 3D Spacecraft Habitat Modules
    item {
      Spacecraft3DCanvas(
        modifier = Modifier.fillMaxWidth(),
        isStormShieldEngaged = cabinEnv.stormShieldEngaged,
        cabinPressureKpa = cabinEnv.cabinPressureKpa
      )
    }

    // Biometric Sensor Radar & Crew Acoustic Stress Scanner (NOVA System Scan)
    item {
      BiometricSensorRadarCard(
        crewMembers = allCrewMembers,
        modifier = Modifier.fillMaxWidth()
      )
    }

    // Mental Health & Behaviour Early Warning System
    item {
      GlassCard(modifier = Modifier.fillMaxWidth()) {
        Column {
          Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
          ) {
            Column {
              Text(
                "MENTAL HEALTH & CREW SUPPORT",
                style = MaterialTheme.typography.labelMedium.copy(color = NeonCyan)
              )
              Text(
                "Early Warning & Wellbeing",
                style = MaterialTheme.typography.titleMedium.copy(color = Color.White)
              )
            }
            StatusBadge(
              text = currentViewingCrew.mentalWellbeing.sensoryOverloadStatus,
              color = if (currentViewingCrew.mentalWellbeing.stressLevelIndex > 40) WarningAmber else NominalGreen
            )
          }

          Spacer(modifier = Modifier.height(10.dp))

          Text(
            text = "AI analyzes patterns in sleep, communication volume, voice acoustics, and voluntary logs to identify stress early.",
            style = MaterialTheme.typography.bodyMedium.copy(color = Color(0xFF94A3B8))
          )

          Spacer(modifier = Modifier.height(10.dp))

          // Smart Support Recommendation Box
          Box(
            modifier = Modifier
              .fillMaxWidth()
              .clip(RoundedCornerShape(10.dp))
              .background(Color.Black.copy(alpha = 0.35f))
              .border(1.dp, NeonCyan.copy(alpha = 0.3f), RoundedCornerShape(10.dp))
              .padding(10.dp)
          ) {
            Column {
              Row(verticalAlignment = Alignment.CenterVertically) {
                Icon(Icons.Default.Psychology, contentDescription = null, tint = NeonCyan, modifier = Modifier.size(16.dp))
                Spacer(modifier = Modifier.width(6.dp))
                Text("SMART CREW SUPPORT SUGGESTION", style = MaterialTheme.typography.labelSmall.copy(color = NeonCyan))
              }
              Spacer(modifier = Modifier.height(4.dp))
              Text(
                text = currentViewingCrew.mentalWellbeing.suggestedIntervention,
                style = MaterialTheme.typography.bodyMedium.copy(color = Color.White)
              )
            }
          }

          Spacer(modifier = Modifier.height(10.dp))

          // Voluntary Daily Check-in Button
          Button(
            onClick = { mentalCheckInCompleted = true },
            modifier = Modifier.fillMaxWidth().height(42.dp).testTag("mental_checkin_button"),
            colors = ButtonDefaults.buttonColors(
              containerColor = if (mentalCheckInCompleted) NominalGreen.copy(alpha = 0.3f) else StarlightBlue.copy(alpha = 0.2f),
              contentColor = Color.White
            ),
            border = androidx.compose.foundation.BorderStroke(1.dp, if (mentalCheckInCompleted) NominalGreen else StarlightBlue),
            shape = RoundedCornerShape(10.dp)
          ) {
            Icon(
              imageVector = if (mentalCheckInCompleted) Icons.Default.CheckCircle else Icons.Default.AssignmentTurnedIn,
              contentDescription = null,
              tint = if (mentalCheckInCompleted) NominalGreen else NeonCyan,
              modifier = Modifier.size(16.dp)
            )
            Spacer(modifier = Modifier.width(6.dp))
            Text(
              if (mentalCheckInCompleted) "VOLUNTARY LOG RECORDED (CONFIDENTIAL)" else "LOG CONFIDENTIAL DAILY REFLECTION",
              style = MaterialTheme.typography.labelSmall
            )
          }
        }
      }
    }

    // Direct Messages from Mission Commander / Flight Surgeon
    item {
      GlassCard(modifier = Modifier.fillMaxWidth()) {
        Column {
          Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
          ) {
            Row(verticalAlignment = Alignment.CenterVertically) {
              Icon(Icons.Default.Mail, contentDescription = null, tint = BrightTeal, modifier = Modifier.size(18.dp))
              Spacer(modifier = Modifier.width(8.dp))
              Text("CREW COMMS DISPATCH", style = MaterialTheme.typography.titleMedium.copy(color = Color.White))
            }
            Text("${myMessages.size} Messages", style = MaterialTheme.typography.labelSmall.copy(color = Color.Gray))
          }

          Spacer(modifier = Modifier.height(10.dp))

          if (myMessages.isEmpty()) {
            Text("No dispatches in your inbox.", style = MaterialTheme.typography.bodyMedium.copy(color = Color.Gray))
          } else {
            myMessages.take(3).forEach { msg ->
              Box(
                modifier = Modifier
                  .fillMaxWidth()
                  .padding(vertical = 4.dp)
                  .clip(RoundedCornerShape(8.dp))
                  .background(Color.Black.copy(alpha = 0.3f))
                  .border(1.dp, if (msg.isHighPriority) WarningAmber.copy(alpha = 0.4f) else Color.White.copy(alpha = 0.06f), RoundedCornerShape(8.dp))
                  .padding(8.dp)
              ) {
                Column {
                  Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween
                  ) {
                    Text(
                      "${msg.senderName} (${msg.senderRole})",
                      style = MaterialTheme.typography.labelSmall.copy(color = NeonCyan)
                    )
                    Text(msg.timestamp, style = MaterialTheme.typography.labelSmall.copy(color = Color.Gray, fontSize = 9.sp))
                  }
                  Spacer(modifier = Modifier.height(2.dp))
                  Text(msg.content, style = MaterialTheme.typography.bodyMedium.copy(color = Color(0xFFE2E8F0)))
                }
              }
            }
          }
        }
      }
    }

    item {
      Spacer(modifier = Modifier.height(16.dp))
    }
  }
}

@Composable
private fun EnvironmentalSensorCard(
  label: String,
  value: String,
  subtext: String,
  isAlert: Boolean,
  modifier: Modifier = Modifier
) {
  val border = if (isAlert) CriticalRed else Color.White.copy(alpha = 0.08f)
  val bg = if (isAlert) CriticalRed.copy(alpha = 0.15f) else Color.Black.copy(alpha = 0.35f)

  Box(
    modifier = modifier
      .clip(RoundedCornerShape(10.dp))
      .background(bg)
      .border(1.dp, border, RoundedCornerShape(10.dp))
      .padding(10.dp)
  ) {
    Column {
      Text(label, style = MaterialTheme.typography.labelSmall.copy(color = Color.Gray, fontSize = 8.sp))
      Spacer(modifier = Modifier.height(2.dp))
      Text(
        value,
        style = MaterialTheme.typography.titleMedium.copy(
          color = if (isAlert) CriticalRed else Color.White
        )
      )
      Spacer(modifier = Modifier.height(2.dp))
      Text(subtext, style = MaterialTheme.typography.labelSmall.copy(color = Color.LightGray, fontSize = 8.sp))
    }
  }
}
