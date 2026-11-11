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
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Campaign
import androidx.compose.material.icons.filled.CrisisAlert
import androidx.compose.material.icons.filled.Favorite
import androidx.compose.material.icons.filled.Groups
import androidx.compose.material.icons.filled.LocationOn
import androidx.compose.material.icons.filled.MedicalServices
import androidx.compose.material.icons.filled.Person
import androidx.compose.material.icons.filled.Psychology
import androidx.compose.material.icons.filled.Refresh
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.automirrored.filled.Send
import androidx.compose.material.icons.filled.Shield
import androidx.compose.material.icons.filled.Warning
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.OutlinedTextFieldDefaults
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
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.data.AstraRepository
import com.example.model.CabinEnvironment
import com.example.model.CrewMember
import com.example.model.DestinationType
import com.example.model.RiskLevel
import androidx.compose.material.icons.filled.Analytics
import com.example.model.UserRole
import com.example.ui.components.AggregateHealthSummaryView
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
import com.example.ui.theme.GlassBorderStroke
import com.example.ui.theme.NeonCyan
import com.example.ui.theme.NominalGreen
import com.example.ui.theme.StarlightBlue
import com.example.ui.theme.WarningAmber

@Composable
fun AdminPortalScreen(
  adminUser: CrewMember,
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
  val crewMembers by AstraRepository.crewMembers.collectAsState()
  val cabinEnv by AstraRepository.cabinEnvironment.collectAsState()
  val unsyncedPackets by AstraRepository.unsyncedPacketsCount.collectAsState()
  val allShiftEvents by AstraRepository.shiftEvents.collectAsState()

  // Selected Admin View: 0 = Fleet View (Everyone), 1 = Admin Own Telemetry
  var adminTab by remember { mutableIntStateOf(0) }
  var selectedCrewDrilldown by remember { mutableStateOf<CrewMember?>(null) }

  // Messaging State
  var messageRecipient by remember { mutableStateOf("ALL") }
  var messageContent by remember { mutableStateOf("") }
  var messageSentStatus by remember { mutableStateOf<String?>(null) }

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
        currentUser = adminUser,
        isDarkMode = isDarkMode,
        onToggleTheme = onToggleTheme,
        unsyncedPackets = unsyncedPackets,
        onOpenSync = onOpenEarthSync,
        onOpenProfile = onOpenSettings,
        onSwitchPortal = onSwitchPortal,
        activePortalName = "ADMIN PORTAL (FLEET COMMAND)"
      )
    }

    // Switch Portal Access Mode Quick-Return Banner
    item {
      Row(
        modifier = Modifier
          .fillMaxWidth()
          .clip(RoundedCornerShape(8.dp))
          .background(Color(0xFF0F172A).copy(alpha = 0.85f))
          .border(1.dp, NeonCyan.copy(alpha = 0.4f), RoundedCornerShape(8.dp))
          .clickable { onSwitchPortal() }
          .padding(horizontal = 12.dp, vertical = 8.dp)
          .testTag("admin_switch_portal_header_button"),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically
      ) {
        Row(verticalAlignment = Alignment.CenterVertically) {
          Icon(
            imageVector = Icons.AutoMirrored.Filled.ArrowBack,
            contentDescription = "Switch Portal",
            tint = NeonCyan,
            modifier = Modifier.size(14.dp)
          )
          Spacer(modifier = Modifier.width(7.dp))
          Text(
            "SWITCH ACCESS PORTAL",
            style = MaterialTheme.typography.labelSmall.copy(
              color = NeonCyan,
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

    // High-Priority Emergency SOS Card (Medical Officer Command & Live 10Hz Stream)
    item {
      com.example.ui.components.EmergencySosCard(
        currentUser = adminUser,
        onTriggerSos = {
          AstraRepository.triggerEmergencySos(adminUser)
        },
        onDeactivateSos = {
          AstraRepository.deactivateEmergencySos()
        }
      )
    }

    // Visual Tool: Astronaut Self-Assessment & Interactive Anatomical Scan
    item {
      GlassCard(
        modifier = Modifier.fillMaxWidth().testTag("admin_launch_health_assessment_card"),
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
                "Interactive Anatomical Figure • Blood Glucose • Ultrasound",
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
            modifier = Modifier.height(34.dp).testTag("admin_open_self_assessment_button")
          ) {
            Text("LAUNCH SCAN", style = MaterialTheme.typography.labelSmall.copy(fontWeight = FontWeight.Black, fontSize = 8.5.sp))
          }
        }
      }
    }

    // NASA Open Data & Space Weather Telemetry Card (data.nasa.gov & DONKI)
    item {
      GlassCard(
        modifier = Modifier.fillMaxWidth().testTag("admin_nasa_open_data_card"),
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
            modifier = Modifier.height(34.dp).testTag("admin_open_nasa_open_data_button")
          ) {
            Text("EXPLORE", style = MaterialTheme.typography.labelSmall.copy(fontWeight = FontWeight.Black, fontSize = 8.5.sp))
          }
        }
      }
    }

    // Portal Switcher: Fleet View vs Admin Personal Info
    item {
      GlassCard(
        modifier = Modifier.fillMaxWidth().testTag("admin_hero_card"),
        cornerRadius = 18.dp
      ) {
        Column {
          Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
          ) {
            StatusBadge(text = "FLIGHT SURGEON COMMAND", color = NeonCyan)
            Text(
              "FLEET: ${crewMembers.size} SOULS",
              style = MaterialTheme.typography.labelSmall.copy(
                color = BrightTeal,
                fontWeight = FontWeight.Bold,
                fontSize = 9.sp,
                letterSpacing = 0.5.sp
              ),
              maxLines = 1,
              softWrap = false
            )
          }

          Spacer(modifier = Modifier.height(10.dp))

          // Tab Toggle: 0 = Fleet Roster, 1 = Aggregate & Stressors, 2 = Admin Personal
          Row(
            modifier = Modifier
              .fillMaxWidth()
              .clip(RoundedCornerShape(12.dp))
              .background(Color.Black.copy(alpha = 0.4f))
              .padding(4.dp),
            horizontalArrangement = Arrangement.spacedBy(4.dp)
          ) {
            Box(
              modifier = Modifier
                .weight(1f)
                .clip(RoundedCornerShape(8.dp))
                .background(if (adminTab == 0) NeonCyan.copy(alpha = 0.25f) else Color.Transparent)
                .clickable { adminTab = 0 }
                .padding(vertical = 7.dp),
              contentAlignment = Alignment.Center
            ) {
              Row(verticalAlignment = Alignment.CenterVertically) {
                Icon(Icons.Default.Groups, contentDescription = null, tint = if (adminTab == 0) NeonCyan else Color.Gray, modifier = Modifier.size(13.dp))
                Spacer(modifier = Modifier.width(4.dp))
                Text(
                  "FLEET",
                  style = MaterialTheme.typography.labelSmall.copy(
                    color = if (adminTab == 0) NeonCyan else Color.Gray,
                    fontSize = 9.sp,
                    fontWeight = if (adminTab == 0) FontWeight.Bold else FontWeight.Normal
                  ),
                  maxLines = 1,
                  softWrap = false
                )
              }
            }

            Box(
              modifier = Modifier
                .weight(1.1f)
                .clip(RoundedCornerShape(8.dp))
                .background(if (adminTab == 1) BrightTeal.copy(alpha = 0.25f) else Color.Transparent)
                .clickable { adminTab = 1 }
                .padding(vertical = 7.dp),
              contentAlignment = Alignment.Center
            ) {
              Row(verticalAlignment = Alignment.CenterVertically) {
                Icon(Icons.Default.Analytics, contentDescription = null, tint = if (adminTab == 1) BrightTeal else Color.Gray, modifier = Modifier.size(13.dp))
                Spacer(modifier = Modifier.width(4.dp))
                Text(
                  "ANALYTICS",
                  style = MaterialTheme.typography.labelSmall.copy(
                    color = if (adminTab == 1) BrightTeal else Color.Gray,
                    fontSize = 9.sp,
                    fontWeight = if (adminTab == 1) FontWeight.Bold else FontWeight.Normal
                  ),
                  maxLines = 1,
                  softWrap = false
                )
              }
            }

            Box(
              modifier = Modifier
                .weight(1f)
                .clip(RoundedCornerShape(8.dp))
                .background(if (adminTab == 2) NeonCyan.copy(alpha = 0.25f) else Color.Transparent)
                .clickable { adminTab = 2 }
                .padding(vertical = 7.dp),
              contentAlignment = Alignment.Center
            ) {
              Row(verticalAlignment = Alignment.CenterVertically) {
                Icon(Icons.Default.Person, contentDescription = null, tint = if (adminTab == 2) NeonCyan else Color.Gray, modifier = Modifier.size(13.dp))
                Spacer(modifier = Modifier.width(4.dp))
                Text(
                  "PROFILE",
                  style = MaterialTheme.typography.labelSmall.copy(
                    color = if (adminTab == 2) NeonCyan else Color.Gray,
                    fontSize = 9.sp,
                    fontWeight = if (adminTab == 2) FontWeight.Bold else FontWeight.Normal
                  ),
                  maxLines = 1,
                  softWrap = false
                )
              }
            }
          }

          Spacer(modifier = Modifier.height(10.dp))

          // Emergency AI Assistant & Fleet Telemedicine Button
          Button(
            onClick = onOpenAiMedicalChat,
            modifier = Modifier.fillMaxWidth().height(38.dp).testTag("admin_emergency_ai_button"),
            colors = ButtonDefaults.buttonColors(containerColor = NeonCyan, contentColor = Color.Black),
            shape = RoundedCornerShape(10.dp)
          ) {
            Icon(Icons.Default.MedicalServices, contentDescription = null, modifier = Modifier.size(16.dp))
            Spacer(modifier = Modifier.width(6.dp))
            Text(
              "LAUNCH EMERGENCY AI CO-PILOT (FLEET TRIAGE)",
              style = MaterialTheme.typography.labelSmall.copy(fontWeight = FontWeight.Bold, fontSize = 9.sp)
            )
          }
        }
      }
    }

    if (adminTab == 1) {
      // --- AGGREGATE HEALTH SUMMARIES & MISSION-WIDE STRESSORS ---
      item {
        AggregateHealthSummaryView(
          allCrewMembers = crewMembers,
          cabinEnv = cabinEnv,
          onTransmitFleetDirective = { directiveText ->
            AstraRepository.sendMessage("ALL", directiveText)
          },
          modifier = Modifier.fillMaxWidth()
        )
      }
    } else if (adminTab == 2) {
      // --- ADMIN PERSONAL HEALTH & TELEMETRY PORTAL ---
      item {
        val (adminScore, adminRisk) = remember(adminUser, cabinEnv) {
          AstraRepository.calculateCompositeRisk(adminUser, cabinEnv)
        }
        AnatomicalBiometricView(
          crewMember = adminUser,
          compositeRiskScore = adminScore,
          riskLevel = adminRisk,
          modifier = Modifier.fillMaxWidth()
        )
      }

      item {
        val adminShiftEvents = remember(allShiftEvents, adminUser.id) {
          allShiftEvents.filter { it.astronautId == adminUser.id || it.astronautId == "ALL" }
        }
        ShiftTimelineComponent(
          events = adminShiftEvents,
          astronautName = adminUser.fullName,
          astronautId = adminUser.id,
          onLogNewEvent = { newEvent ->
            AstraRepository.logShiftEvent(newEvent)
          },
          modifier = Modifier.fillMaxWidth()
        )
      }
    } else {
      // --- FLEET OVERVIEW (ALL CREW HEALTH, LOCATION & ACTIVITY) ---
      item {
        Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
          Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
          ) {
            Text(
              "CREW HEALTH & ACTIVITY",
              style = MaterialTheme.typography.titleSmall.copy(
                color = BrightTeal,
                fontWeight = FontWeight.Bold,
                fontSize = 12.sp,
                letterSpacing = 0.8.sp
              ),
              maxLines = 1,
              overflow = TextOverflow.Ellipsis,
              modifier = Modifier.weight(1f).padding(end = 8.dp)
            )
            Row(verticalAlignment = Alignment.CenterVertically) {
              Box(
                modifier = Modifier
                  .size(5.dp)
                  .clip(CircleShape)
                  .background(NominalGreen)
              )
              Spacer(modifier = Modifier.width(4.dp))
              Text(
                "LIVE SYNC",
                style = MaterialTheme.typography.labelSmall.copy(
                  color = NominalGreen,
                  fontWeight = FontWeight.Bold,
                  fontSize = 8.5.sp,
                  letterSpacing = 0.5.sp
                ),
                maxLines = 1,
                softWrap = false
              )
            }
          }

          crewMembers.forEach { member ->
            val memberMissionRisk = remember(member, cabinEnv) {
              AstraRepository.getMissionRiskScore(member, cabinEnv)
            }
            val memberScore = memberMissionRisk.compositeScore
            val memberRisk = memberMissionRisk.riskLevel
            val riskBorder = when (memberRisk) {
              RiskLevel.LOW -> NominalGreen
              RiskLevel.MODERATE -> WarningAmber
              RiskLevel.HIGH -> CriticalRed
            }

            GlassCard(
              modifier = Modifier
                .fillMaxWidth()
                .clickable { selectedCrewDrilldown = if (selectedCrewDrilldown?.id == member.id) null else member }
                .testTag("crew_card_${member.id}"),
              borderColor = riskBorder.copy(alpha = 0.6f)
            ) {
              Column {
                Row(
                  modifier = Modifier.fillMaxWidth(),
                  horizontalArrangement = Arrangement.SpaceBetween,
                  verticalAlignment = Alignment.CenterVertically
                ) {
                  Row(
                    verticalAlignment = Alignment.CenterVertically,
                    modifier = Modifier.weight(1f).padding(end = 8.dp)
                  ) {
                    Box(
                      modifier = Modifier
                        .size(36.dp)
                        .clip(CircleShape)
                        .background(if (member.role == UserRole.ADMIN) NeonCyan.copy(alpha = 0.2f) else BrightTeal.copy(alpha = 0.2f))
                        .border(1.dp, if (member.role == UserRole.ADMIN) NeonCyan else BrightTeal, CircleShape),
                      contentAlignment = Alignment.Center
                    ) {
                      Text(
                        member.fullName.split(" ").mapNotNull { it.firstOrNull()?.toString() }.joinToString(""),
                        style = MaterialTheme.typography.labelSmall.copy(color = Color.White, fontWeight = FontWeight.Bold)
                      )
                    }

                    Spacer(modifier = Modifier.width(10.dp))

                    Column(modifier = Modifier.weight(1f, fill = false)) {
                      Text(
                        member.fullName,
                        style = MaterialTheme.typography.titleMedium.copy(color = Color.White),
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis
                      )
                      Text(
                        "${member.id} • ${member.missionSpecialty}",
                        style = MaterialTheme.typography.labelSmall.copy(color = Color.Gray, fontSize = 9.sp),
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis
                      )
                    }
                  }

                  StatusBadge(text = "${memberRisk.label} ($memberScore)", color = riskBorder)
                }

                Spacer(modifier = Modifier.height(10.dp))

                // Location & Activity Indicators
                Row(
                  modifier = Modifier.fillMaxWidth(),
                  horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                  Box(
                    modifier = Modifier
                      .weight(1f)
                      .clip(RoundedCornerShape(8.dp))
                      .background(Color.Black.copy(alpha = 0.35f))
                      .padding(8.dp)
                  ) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                      Icon(Icons.Default.LocationOn, contentDescription = null, tint = BrightTeal, modifier = Modifier.size(14.dp))
                      Spacer(modifier = Modifier.width(4.dp))
                      Column {
                        Text("LOCATION", style = MaterialTheme.typography.labelSmall.copy(color = Color.Gray, fontSize = 8.sp))
                        Text(member.currentModuleLocation, style = MaterialTheme.typography.bodySmall.copy(color = Color.White))
                      }
                    }
                  }

                  Box(
                    modifier = Modifier
                      .weight(1f)
                      .clip(RoundedCornerShape(8.dp))
                      .background(Color.Black.copy(alpha = 0.35f))
                      .padding(8.dp)
                  ) {
                    Column {
                      Text("CURRENT ACTIVITY", style = MaterialTheme.typography.labelSmall.copy(color = Color.Gray, fontSize = 8.sp))
                      Text(member.currentActivity, style = MaterialTheme.typography.bodySmall.copy(color = NeonCyan))
                    }
                  }
                }

                Spacer(modifier = Modifier.height(8.dp))

                // Vital Sign Quick Metric Strip
                Row(
                  modifier = Modifier.fillMaxWidth(),
                  horizontalArrangement = Arrangement.SpaceBetween
                ) {
                  Text("HR: ${member.vitals.heartRateBpm} bpm", style = MaterialTheme.typography.labelSmall.copy(color = Color.LightGray))
                  Text("BP: ${member.vitals.systolicBp}/${member.vitals.diastolicBp}", style = MaterialTheme.typography.labelSmall.copy(color = Color.LightGray))
                  Text("SpO₂: ${member.vitals.spo2Percentage}%", style = MaterialTheme.typography.labelSmall.copy(color = Color.LightGray))
                  Text("Rad: ${member.radiation.dailyDoseMsv} mSv/d", style = MaterialTheme.typography.labelSmall.copy(color = Color.LightGray))
                  Text("Sleep: ${member.sleep.totalSleepHours}h", style = MaterialTheme.typography.labelSmall.copy(color = Color.LightGray))
                }

                // If expanded, show detailed Mission Risk Gauge & Anatomical View for this crew member
                AnimatedVisibility(visible = selectedCrewDrilldown?.id == member.id) {
                  Column(
                    modifier = Modifier.padding(top = 12.dp),
                    verticalArrangement = Arrangement.spacedBy(10.dp)
                  ) {
                    com.example.ui.components.MissionRiskGaugeCard(
                      missionRisk = memberMissionRisk,
                      astronautName = member.fullName,
                      modifier = Modifier.fillMaxWidth()
                    )

                    AnatomicalBiometricView(
                      crewMember = member,
                      compositeRiskScore = memberScore,
                      riskLevel = memberRisk,
                      modifier = Modifier.fillMaxWidth()
                    )

                    val memberShiftEvents = remember(allShiftEvents, member.id) {
                      allShiftEvents.filter { it.astronautId == member.id || it.astronautId == "ALL" }
                    }
                    ShiftTimelineComponent(
                      events = memberShiftEvents,
                      astronautName = member.fullName,
                      astronautId = member.id,
                      onLogNewEvent = { newEvent ->
                        AstraRepository.logShiftEvent(newEvent)
                      },
                      modifier = Modifier.fillMaxWidth()
                    )
                  }
                }
              }
            }
          }
        }
      }

      // Biometric Sensor Radar & Crew Acoustic Stress Scanner (NOVA System Scan)
      item {
        BiometricSensorRadarCard(
          crewMembers = crewMembers,
          modifier = Modifier.fillMaxWidth()
        )
      }

      // Spacecraft Modules 3D Canvas
      item {
        Spacecraft3DCanvas(
          modifier = Modifier.fillMaxWidth(),
          isStormShieldEngaged = cabinEnv.stormShieldEngaged,
          cabinPressureKpa = cabinEnv.cabinPressureKpa
        )
      }

      // Emergency Simulation Drill Triggers (For testing Layer 1 Instant Alerting)
      item {
        GlassCard(modifier = Modifier.fillMaxWidth()) {
          Column {
            Row(
              modifier = Modifier.fillMaxWidth(),
              horizontalArrangement = Arrangement.SpaceBetween,
              verticalAlignment = Alignment.CenterVertically
            ) {
              Row(
                verticalAlignment = Alignment.CenterVertically,
                modifier = Modifier.weight(1f, fill = false).padding(end = 8.dp)
              ) {
                Icon(Icons.Default.CrisisAlert, contentDescription = null, tint = WarningAmber, modifier = Modifier.size(16.dp))
                Spacer(modifier = Modifier.width(6.dp))
                Text(
                  "EMERGENCY DRILL SIMULATION",
                  style = MaterialTheme.typography.titleSmall.copy(color = Color.White, fontWeight = FontWeight.Bold, fontSize = 11.sp),
                  maxLines = 1,
                  overflow = TextOverflow.Ellipsis
                )
              }
              Box(
                modifier = Modifier
                  .clip(RoundedCornerShape(6.dp))
                  .background(WarningAmber.copy(alpha = 0.15f))
                  .border(1.dp, WarningAmber.copy(alpha = 0.5f), RoundedCornerShape(6.dp))
                  .padding(horizontal = 7.dp, vertical = 3.dp)
              ) {
                Text(
                  "L1 RULES TEST",
                  style = MaterialTheme.typography.labelSmall.copy(
                    color = WarningAmber,
                    fontWeight = FontWeight.Bold,
                    fontSize = 8.5.sp
                  ),
                  maxLines = 1,
                  softWrap = false
                )
              }
            }

            Spacer(modifier = Modifier.height(10.dp))

            Text(
              "Simulate critical onboard anomalies to verify crew shelter protocols, oxygen masks, and immediate safety alarms.",
              style = MaterialTheme.typography.bodySmall.copy(color = Color(0xFF94A3B8))
            )

            Spacer(modifier = Modifier.height(10.dp))

            Row(
              modifier = Modifier.fillMaxWidth(),
              horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
              Button(
                onClick = { AstraRepository.triggerSolarStormRadiationSimulation() },
                modifier = Modifier.weight(1f).height(40.dp).testTag("sim_solar_storm_button"),
                colors = ButtonDefaults.buttonColors(containerColor = CriticalRed.copy(alpha = 0.25f), contentColor = CriticalRed),
                border = androidx.compose.foundation.BorderStroke(1.dp, CriticalRed),
                shape = RoundedCornerShape(8.dp)
              ) {
                Text("SOLAR SPE", style = MaterialTheme.typography.labelSmall)
              }

              Button(
                onClick = { AstraRepository.triggerEmergencyDepressurizationSimulation() },
                modifier = Modifier.weight(1f).height(40.dp).testTag("sim_pressure_loss_button"),
                colors = ButtonDefaults.buttonColors(containerColor = CriticalRed.copy(alpha = 0.25f), contentColor = CriticalRed),
                border = androidx.compose.foundation.BorderStroke(1.dp, CriticalRed),
                shape = RoundedCornerShape(8.dp)
              ) {
                Text("PRESSURE DROP", style = MaterialTheme.typography.labelSmall)
              }

              Button(
                onClick = { AstraRepository.triggerAirScrubberEmergencySimulation() },
                modifier = Modifier.weight(1f).height(40.dp).testTag("sim_co2_spike_button"),
                colors = ButtonDefaults.buttonColors(containerColor = WarningAmber.copy(alpha = 0.25f), contentColor = WarningAmber),
                border = androidx.compose.foundation.BorderStroke(1.dp, WarningAmber),
                shape = RoundedCornerShape(8.dp)
              ) {
                Text("HIGH CO₂", style = MaterialTheme.typography.labelSmall)
              }
            }

            Spacer(modifier = Modifier.height(8.dp))

            Button(
              onClick = { AstraRepository.resetCabinNominal() },
              modifier = Modifier.fillMaxWidth().height(36.dp).testTag("reset_nominal_button"),
              colors = ButtonDefaults.buttonColors(containerColor = NominalGreen.copy(alpha = 0.2f), contentColor = NominalGreen),
              border = androidx.compose.foundation.BorderStroke(1.dp, NominalGreen),
              shape = RoundedCornerShape(8.dp)
            ) {
              Icon(Icons.Default.Refresh, contentDescription = null, modifier = Modifier.size(14.dp))
              Spacer(modifier = Modifier.width(6.dp))
              Text("RESTORE NOMINAL FLIGHT CONDITIONS", style = MaterialTheme.typography.labelSmall)
            }
          }
        }
      }

      // Crew Comms Dispatcher (Send messages to all crew or individually)
      item {
        GlassCard(modifier = Modifier.fillMaxWidth().testTag("admin_comms_card")) {
          Column {
            Row(verticalAlignment = Alignment.CenterVertically) {
              Icon(Icons.Default.Campaign, contentDescription = null, tint = NeonCyan, modifier = Modifier.size(20.dp))
              Spacer(modifier = Modifier.width(8.dp))
              Text("CREW COMMS DISPATCHER", style = MaterialTheme.typography.titleMedium.copy(color = Color.White))
            }

            Spacer(modifier = Modifier.height(10.dp))

            // Recipient Selector
            Text("TARGET RECIPIENT:", style = MaterialTheme.typography.labelSmall.copy(color = Color.Gray))
            Spacer(modifier = Modifier.height(6.dp))

            Row(
              modifier = Modifier.fillMaxWidth(),
              horizontalArrangement = Arrangement.spacedBy(6.dp)
            ) {
              val options = listOf("ALL" to "ALL CREW (BROADCAST)") + crewMembers.filterNot { it.id == adminUser.id }.map { it.id to it.fullName }
              options.take(4).forEach { (recId, label) ->
                val isSelected = messageRecipient == recId
                Box(
                  modifier = Modifier
                    .clip(RoundedCornerShape(8.dp))
                    .background(if (isSelected) NeonCyan.copy(alpha = 0.25f) else Color.White.copy(alpha = 0.05f))
                    .border(1.dp, if (isSelected) NeonCyan else Color.White.copy(alpha = 0.1f), RoundedCornerShape(8.dp))
                    .clickable { messageRecipient = recId }
                    .padding(horizontal = 8.dp, vertical = 6.dp)
                ) {
                  Text(
                    text = if (recId == "ALL") "ALL CREW" else label.split(" ").last(),
                    style = MaterialTheme.typography.labelSmall.copy(
                      color = if (isSelected) NeonCyan else Color.LightGray
                    )
                  )
                }
              }
            }

            Spacer(modifier = Modifier.height(10.dp))

            OutlinedTextField(
              value = messageContent,
              onValueChange = { messageContent = it },
              placeholder = { Text("Enter mission directive or flight surgeon tele-order...", color = Color.Gray) },
              modifier = Modifier.fillMaxWidth().testTag("admin_message_input"),
              colors = OutlinedTextFieldDefaults.colors(
                focusedBorderColor = NeonCyan,
                unfocusedBorderColor = GlassBorderStroke,
                focusedTextColor = Color.White,
                unfocusedTextColor = Color.White
              ),
              shape = RoundedCornerShape(10.dp)
            )

            messageSentStatus?.let { status ->
              Spacer(modifier = Modifier.height(6.dp))
              Text(status, style = MaterialTheme.typography.bodySmall.copy(color = NominalGreen))
            }

            Spacer(modifier = Modifier.height(10.dp))

            Button(
              onClick = {
                if (messageContent.isNotBlank()) {
                  AstraRepository.sendMessage(messageRecipient, messageContent)
                  messageContent = ""
                  messageSentStatus = "Directive transmitted to ${if (messageRecipient == "ALL") "all crew members" else messageRecipient}."
                }
              },
              modifier = Modifier.fillMaxWidth().height(42.dp).testTag("admin_send_message_button"),
              colors = ButtonDefaults.buttonColors(containerColor = NeonCyan, contentColor = Color.Black),
              shape = RoundedCornerShape(10.dp)
            ) {
              Icon(Icons.AutoMirrored.Filled.Send, contentDescription = null, modifier = Modifier.size(16.dp))
              Spacer(modifier = Modifier.width(6.dp))
              Text("TRANSMIT DIRECTIVE", style = MaterialTheme.typography.labelSmall.copy(fontWeight = androidx.compose.ui.text.font.FontWeight.Bold))
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
