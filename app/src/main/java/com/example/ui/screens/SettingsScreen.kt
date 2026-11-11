package com.example.ui.screens

import androidx.activity.compose.BackHandler
import androidx.compose.animation.AnimatedVisibility
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
import androidx.compose.material.icons.automirrored.filled.Logout
import androidx.compose.material.icons.automirrored.filled.VolumeOff
import androidx.compose.material.icons.automirrored.filled.VolumeUp
import androidx.compose.material.icons.filled.Air
import androidx.compose.material.icons.filled.Bolt
import androidx.compose.material.icons.filled.Brightness4
import androidx.compose.material.icons.filled.Brightness7
import com.example.service.AstraAudioAlarmService
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.CrisisAlert
import androidx.compose.material.icons.filled.Emergency
import androidx.compose.material.icons.filled.Favorite
import androidx.compose.material.icons.filled.LocalFireDepartment
import androidx.compose.material.icons.filled.Person
import androidx.compose.material.icons.filled.Psychology
import androidx.compose.material.icons.filled.Radar
import androidx.compose.material.icons.filled.Refresh
import androidx.compose.material.icons.filled.RestartAlt
import androidx.compose.material.icons.filled.Security
import androidx.compose.material.icons.filled.Sensors
import androidx.compose.material.icons.filled.Shield
import androidx.compose.material.icons.filled.Speed
import androidx.compose.material.icons.filled.Thermostat
import androidx.compose.material.icons.filled.Tune
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.DropdownMenu
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Slider
import androidx.compose.material3.SliderDefaults
import androidx.compose.material3.Switch
import androidx.compose.material3.SwitchDefaults
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
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.data.AstraRepository
import com.example.model.CrewMember
import com.example.model.DestinationType
import com.example.model.RiskLevel
import com.example.model.UserRole
import com.example.ui.components.EmergencySosCard
import com.example.ui.components.GlassCard
import com.example.ui.components.StatusBadge
import com.example.ui.theme.BrightTeal
import com.example.ui.theme.CriticalRed
import com.example.ui.theme.GlassBorderStroke
import com.example.ui.theme.NeonCyan
import com.example.ui.theme.NominalGreen
import com.example.ui.theme.StarlightBlue
import com.example.ui.theme.WarningAmber

@Composable
fun SettingsScreen(
  currentUser: CrewMember,
  currentDestination: DestinationType,
  isDarkMode: Boolean,
  onToggleTheme: () -> Unit,
  onLogout: () -> Unit,
  onNavigateBack: () -> Unit,
  onDestinationSelected: (DestinationType) -> Unit = { AstraRepository.setDestination(it) },
  modifier: Modifier = Modifier
) {
  BackHandler { onNavigateBack() }

  val allCrewMembers by AstraRepository.crewMembers.collectAsState()
  val cabinEnv by AstraRepository.cabinEnvironment.collectAsState()
  val featureConfig by AstraRepository.systemFeatureConfig.collectAsState()
  val isSosActive by AstraRepository.isEmergencySosActive.collectAsState()

  // Tab 0 = Manual Feature Control & Overrides, Tab 1 = Account & Hardware Manifest
  var settingsTab by remember { mutableIntStateOf(0) }

  // Target Crew Member for manual biometrics adjustments
  var selectedMemberId by remember(currentUser) { mutableStateOf(currentUser.id) }
  val targetMember = remember(allCrewMembers, selectedMemberId) {
    allCrewMembers.find { it.id == selectedMemberId } ?: currentUser
  }

  // Local state for hardware toggles
  var bluetoothPaired by remember { mutableStateOf(true) }
  var nfcDosimeterActive by remember { mutableStateOf(true) }
  var hipaaEncryptionEnforced by remember { mutableStateOf(true) }
  var opticalGaitActive by remember { mutableStateOf(true) }

  Box(
    modifier = modifier
      .fillMaxSize()
      .background(MaterialTheme.colorScheme.background)
      .padding(14.dp)
  ) {
    LazyColumn(
      modifier = Modifier.fillMaxSize(),
      verticalArrangement = Arrangement.spacedBy(14.dp)
    ) {
      // 1. Header Navigation Bar
      item {
        Row(
          modifier = Modifier.fillMaxWidth(),
          horizontalArrangement = Arrangement.SpaceBetween,
          verticalAlignment = Alignment.CenterVertically
        ) {
          Row(verticalAlignment = Alignment.CenterVertically) {
            IconButton(
              onClick = onNavigateBack,
              modifier = Modifier.size(36.dp).testTag("settings_back_button")
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
                "SYSTEM SETTINGS & CONTROL",
                style = MaterialTheme.typography.titleMedium.copy(
                  color = Color.White,
                  fontWeight = FontWeight.Bold,
                  letterSpacing = 1.sp
                )
              )
              Text(
                "Manual Feature Controls & Flight Manifest Configuration",
                style = MaterialTheme.typography.labelSmall.copy(color = BrightTeal, fontSize = 8.5.sp)
              )
            }
          }
          StatusBadge(text = if (isSosActive) "SOS 10Hz" else "NOMINAL", color = if (isSosActive) CriticalRed else NominalGreen)
        }
      }

      // 2. High-Priority Emergency SOS Card (Automated Medical Officer Broadcast & 10 Hz Telemetry)
      item {
        EmergencySosCard(
          currentUser = currentUser,
          onTriggerSos = { AstraRepository.triggerEmergencySos(currentUser) },
          onDeactivateSos = { AstraRepository.deactivateEmergencySos() }
        )
      }

      // 3. Settings Tab Selector: Manual Feature Controls vs Account & Sensors
      item {
        Row(
          modifier = Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(12.dp))
            .background(Color.Black.copy(alpha = 0.5f))
            .border(1.dp, GlassBorderStroke, RoundedCornerShape(12.dp))
            .padding(4.dp)
        ) {
          Box(
            modifier = Modifier
              .weight(1f)
              .clip(RoundedCornerShape(8.dp))
              .background(if (settingsTab == 0) NeonCyan.copy(alpha = 0.25f) else Color.Transparent)
              .clickable { settingsTab = 0 }
              .padding(vertical = 8.dp)
              .testTag("tab_manual_controls"),
            contentAlignment = Alignment.Center
          ) {
            Row(verticalAlignment = Alignment.CenterVertically) {
              Icon(Icons.Default.Tune, contentDescription = null, tint = if (settingsTab == 0) NeonCyan else Color.Gray, modifier = Modifier.size(15.dp))
              Spacer(modifier = Modifier.width(6.dp))
              Text(
                "MANUAL FEATURE CONTROLS",
                style = MaterialTheme.typography.labelSmall.copy(
                  color = if (settingsTab == 0) NeonCyan else Color.Gray,
                  fontWeight = if (settingsTab == 0) FontWeight.Bold else FontWeight.Normal,
                  fontSize = 8.5.sp
                )
              )
            }
          }

          Box(
            modifier = Modifier
              .weight(1f)
              .clip(RoundedCornerShape(8.dp))
              .background(if (settingsTab == 1) BrightTeal.copy(alpha = 0.25f) else Color.Transparent)
              .clickable { settingsTab = 1 }
              .padding(vertical = 8.dp)
              .testTag("tab_account_manifest"),
            contentAlignment = Alignment.Center
          ) {
            Row(verticalAlignment = Alignment.CenterVertically) {
              Icon(Icons.Default.Person, contentDescription = null, tint = if (settingsTab == 1) BrightTeal else Color.Gray, modifier = Modifier.size(15.dp))
              Spacer(modifier = Modifier.width(6.dp))
              Text(
                "ACCOUNT & MANIFEST",
                style = MaterialTheme.typography.labelSmall.copy(
                  color = if (settingsTab == 1) BrightTeal else Color.Gray,
                  fontWeight = if (settingsTab == 1) FontWeight.Bold else FontWeight.Normal,
                  fontSize = 8.5.sp
                )
              )
            }
          }
        }
      }

      if (settingsTab == 0) {
        // =========================================================================
        // TAB 0: MANUAL FEATURE CONTROL & OVERRIDES
        // =========================================================================
        item {
          com.example.ui.components.ManualFeatureControlsContent(
            currentUser = currentUser,
            allCrewMembers = allCrewMembers,
            cabinEnv = cabinEnv,
            featureConfig = featureConfig,
            currentDestination = currentDestination,
            onDestinationSelected = onDestinationSelected
          )
        }
      } else {
        // =========================================================================
        // TAB 1: ACCOUNT & HARDWARE MANIFEST
        // =========================================================================

        // Account Information Card
        item {
          GlassCard(modifier = Modifier.fillMaxWidth().testTag("account_info_card")) {
            Column {
              Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
              ) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                  Box(
                    modifier = Modifier
                      .size(46.dp)
                      .clip(CircleShape)
                      .background(if (currentUser.role == UserRole.ADMIN) NeonCyan.copy(alpha = 0.25f) else BrightTeal.copy(alpha = 0.25f))
                      .border(1.5.dp, if (currentUser.role == UserRole.ADMIN) NeonCyan else BrightTeal, CircleShape),
                    contentAlignment = Alignment.Center
                  ) {
                    Icon(Icons.Default.Person, contentDescription = null, tint = Color.White, modifier = Modifier.size(24.dp))
                  }
                  Spacer(modifier = Modifier.width(12.dp))
                  Column {
                    Text(currentUser.fullName, style = MaterialTheme.typography.titleMedium.copy(color = Color.White))
                    Text(
                      "USER ID: ${currentUser.id} • ${currentUser.role.name}",
                      style = MaterialTheme.typography.labelSmall.copy(color = BrightTeal)
                    )
                  }
                }

                StatusBadge(text = currentUser.role.name, color = if (currentUser.role == UserRole.ADMIN) NeonCyan else BrightTeal)
              }

              Spacer(modifier = Modifier.height(14.dp))

              // Details Grid
              Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                AccountDetailRow("OFFICIAL EMAIL", currentUser.email)
                AccountDetailRow("MISSION SPECIALTY", currentUser.missionSpecialty)
                AccountDetailRow("CURRENT DESTINATION", currentDestination.displayName)
                AccountDetailRow("HABITAT VEHICLE", currentDestination.habitatVehicle)
                AccountDetailRow("GENDER & AGE", "${currentUser.gender}, ${currentUser.age} Earth years")
                AccountDetailRow("HABITAT LOCATION", currentUser.currentModuleLocation)
              }
            }
          }
        }

        // Hardware Sensor Pairing & Connectivity
        item {
          GlassCard(modifier = Modifier.fillMaxWidth().testTag("sensor_pairing_card")) {
            Column {
              Row(verticalAlignment = Alignment.CenterVertically) {
                Icon(Icons.Default.Sensors, contentDescription = null, tint = NeonCyan, modifier = Modifier.size(18.dp))
                Spacer(modifier = Modifier.width(8.dp))
                Text(
                  "HARDWARE SENSORS & TELEMETRY PAIRING",
                  style = MaterialTheme.typography.titleSmall.copy(color = Color.White)
                )
              }

              Spacer(modifier = Modifier.height(10.dp))

              // Bluetooth Bio-Harness
              FeatureToggleRow(
                title = "Bluetooth Low Energy Bio-Harness",
                subtitle = "Continuous ECG, HRV, Continuous SpO₂ & Skin Temperature",
                checked = bluetoothPaired,
                onCheckedChange = { bluetoothPaired = it }
              )

              Spacer(modifier = Modifier.height(8.dp))

              // NFC RadWatch Dosimeter
              FeatureToggleRow(
                title = "NFC RadWatch Active Dosimeter",
                subtitle = "Ionizing radiation dosimeter on shoulder EVA patch",
                checked = nfcDosimeterActive,
                onCheckedChange = { nfcDosimeterActive = it }
              )

              Spacer(modifier = Modifier.height(8.dp))

              // Optical Camera Tracking
              FeatureToggleRow(
                title = "Optical Camera Posture & Gait Analysis",
                subtitle = "Hab module video kinematics for muscle atrophy detection",
                checked = opticalGaitActive,
                onCheckedChange = { opticalGaitActive = it }
              )
            }
          }
        }

        // Privacy, Security & HIPAA Flight Medicine Compliance
        item {
          GlassCard(modifier = Modifier.fillMaxWidth()) {
            Column {
              Row(verticalAlignment = Alignment.CenterVertically) {
                Icon(Icons.Default.Security, contentDescription = null, tint = NominalGreen, modifier = Modifier.size(18.dp))
                Spacer(modifier = Modifier.width(8.dp))
                Text(
                  "DATA PROTECTION & CLINICAL INTEGRITY",
                  style = MaterialTheme.typography.titleSmall.copy(color = Color.White)
                )
              }

              Spacer(modifier = Modifier.height(8.dp))

              Text(
                "All mental health check-ins, medical queries, and behavioral kinematics are strictly gated by NASA Flight Surgeon privacy rules and HIPAA-grade local encryption. Alerts remain confidential between the astronaut and onboard medical officer.",
                style = MaterialTheme.typography.bodySmall.copy(color = Color(0xFF94A3B8), lineHeight = 18.sp)
              )

              Spacer(modifier = Modifier.height(10.dp))

              FeatureToggleRow(
                title = "HIPAA On-Device Encryption (AES-256)",
                subtitle = "Zero-trust client keys; no unencrypted telemetry broadcast",
                checked = hipaaEncryptionEnforced,
                onCheckedChange = { hipaaEncryptionEnforced = it }
              )
            }
          }
        }

        // Real-Time Audio Warning System & Emergency Siren Controls
        item {
          val isMuted by AstraAudioAlarmService.isMuted.collectAsState()
          val isEmergencyAlarmActive by AstraAudioAlarmService.isEmergencyAlarmActive.collectAsState()
          val context = androidx.compose.ui.platform.LocalContext.current

          GlassCard(modifier = Modifier.fillMaxWidth().testTag("audio_warning_settings_card")) {
            Column {
              Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
              ) {
                Row(verticalAlignment = Alignment.CenterVertically, modifier = Modifier.weight(1f)) {
                  Icon(
                    imageVector = if (isMuted) Icons.AutoMirrored.Filled.VolumeOff else Icons.AutoMirrored.Filled.VolumeUp,
                    contentDescription = null,
                    tint = if (isMuted) WarningAmber else NeonCyan,
                    modifier = Modifier.size(20.dp)
                  )
                  Spacer(modifier = Modifier.width(8.dp))
                  Column {
                    Text(
                      "AUDIO WARNING SYSTEM & SIREN",
                      style = MaterialTheme.typography.titleSmall.copy(color = Color.White, fontWeight = FontWeight.Bold)
                    )
                    Text(
                      if (isMuted) "Audio alerts muted (SOS overrides mute)" else "Real-time acoustic alert sounds active",
                      style = MaterialTheme.typography.labelSmall.copy(color = if (isMuted) WarningAmber else BrightTeal, fontSize = 8.5.sp)
                    )
                  }
                }

                Switch(
                  checked = !isMuted,
                  onCheckedChange = { active -> AstraAudioAlarmService.setMuted(!active, context) },
                  colors = SwitchDefaults.colors(
                    checkedThumbColor = NeonCyan,
                    checkedTrackColor = NeonCyan.copy(alpha = 0.3f)
                  )
                )
              }

              Spacer(modifier = Modifier.height(10.dp))

              Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                Button(
                  onClick = { AstraAudioAlarmService.playThreatWarningSound(context) },
                  modifier = Modifier.weight(1f).height(36.dp).testTag("test_warning_sound_button"),
                  colors = ButtonDefaults.buttonColors(containerColor = BrightTeal.copy(alpha = 0.2f), contentColor = BrightTeal),
                  shape = RoundedCornerShape(8.dp)
                ) {
                  Text("TEST WARNING BEEP", style = MaterialTheme.typography.labelSmall.copy(fontSize = 8.5.sp))
                }

                Button(
                  onClick = {
                    if (isEmergencyAlarmActive) {
                      AstraAudioAlarmService.stopEmergencyAlarm()
                    } else {
                      AstraAudioAlarmService.playEmergencyAlarm(context)
                    }
                  },
                  modifier = Modifier.weight(1f).height(36.dp).testTag("test_emergency_siren_button"),
                  colors = ButtonDefaults.buttonColors(
                    containerColor = if (isEmergencyAlarmActive) CriticalRed else CriticalRed.copy(alpha = 0.2f),
                    contentColor = if (isEmergencyAlarmActive) Color.White else CriticalRed
                  ),
                  shape = RoundedCornerShape(8.dp)
                ) {
                  Text(if (isEmergencyAlarmActive) "STOP SIREN" else "TEST SIREN ALARM", style = MaterialTheme.typography.labelSmall.copy(fontSize = 8.5.sp))
                }
              }
            }
          }
        }

        // Display Mode: Standard Ops vs Dark Cosmic Mode
        item {
          GlassCard(modifier = Modifier.fillMaxWidth()) {
            Row(
              modifier = Modifier.fillMaxWidth(),
              horizontalArrangement = Arrangement.SpaceBetween,
              verticalAlignment = Alignment.CenterVertically
            ) {
              Column {
                Text(
                  "VISUALIZATION DESIGN MODE",
                  style = MaterialTheme.typography.titleSmall.copy(color = Color.White)
                )
                Text(
                  if (isDarkMode) "1. Dark Cosmic Mode (Deep Space Void)" else "2. Standard Flight Deck Mode (NASA Navy)",
                  style = MaterialTheme.typography.bodySmall.copy(color = NeonCyan)
                )
              }

              IconButton(onClick = onToggleTheme, modifier = Modifier.size(40.dp)) {
                Icon(
                  imageVector = if (isDarkMode) Icons.Default.Brightness7 else Icons.Default.Brightness4,
                  contentDescription = "Toggle Mode",
                  tint = NeonCyan
                )
              }
            }
          }
        }

        // Logout / Switch User Button
        item {
          Button(
            onClick = onLogout,
            modifier = Modifier.fillMaxWidth().height(46.dp).testTag("logout_button"),
            colors = ButtonDefaults.buttonColors(containerColor = CriticalRed.copy(alpha = 0.2f), contentColor = CriticalRed),
            border = androidx.compose.foundation.BorderStroke(1.dp, CriticalRed.copy(alpha = 0.6f)),
            shape = RoundedCornerShape(12.dp)
          ) {
            Icon(Icons.AutoMirrored.Filled.Logout, contentDescription = null, modifier = Modifier.size(18.dp))
            Spacer(modifier = Modifier.width(8.dp))
            Text("LOG OUT / SWITCH CREW MANIFEST", style = MaterialTheme.typography.titleSmall)
          }
        }
      }

      item { Spacer(modifier = Modifier.height(16.dp)) }
    }
  }
}



@Composable
private fun FeatureToggleRow(
  title: String,
  subtitle: String,
  checked: Boolean,
  onCheckedChange: (Boolean) -> Unit
) {
  Row(
    modifier = Modifier
      .fillMaxWidth()
      .clip(RoundedCornerShape(8.dp))
      .background(Color.Black.copy(alpha = 0.3f))
      .padding(horizontal = 10.dp, vertical = 6.dp),
    verticalAlignment = Alignment.CenterVertically,
    horizontalArrangement = Arrangement.SpaceBetween
  ) {
    Column(modifier = Modifier.weight(1f)) {
      Text(title, style = MaterialTheme.typography.titleSmall.copy(color = Color.White, fontSize = 11.sp))
      Text(subtitle, style = MaterialTheme.typography.bodySmall.copy(color = Color.Gray, fontSize = 8.5.sp))
    }

    Switch(
      checked = checked,
      onCheckedChange = onCheckedChange,
      colors = SwitchDefaults.colors(
        checkedThumbColor = NeonCyan,
        checkedTrackColor = NeonCyan.copy(alpha = 0.3f)
      )
    )
  }
}

@Composable
private fun AccountDetailRow(label: String, value: String) {
  Row(
    modifier = Modifier
      .fillMaxWidth()
      .clip(RoundedCornerShape(6.dp))
      .background(Color.Black.copy(alpha = 0.3f))
      .padding(horizontal = 10.dp, vertical = 6.dp),
    horizontalArrangement = Arrangement.SpaceBetween
  ) {
    Text(label, style = MaterialTheme.typography.labelSmall.copy(color = Color.Gray, fontSize = 9.sp))
    Text(value, style = MaterialTheme.typography.labelSmall.copy(color = Color(0xFFF1F5F9)))
  }
}
