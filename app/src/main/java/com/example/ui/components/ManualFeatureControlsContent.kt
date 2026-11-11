package com.example.ui.components

import android.widget.Toast
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Air
import androidx.compose.material.icons.filled.Bolt
import androidx.compose.material.icons.filled.CrisisAlert
import androidx.compose.material.icons.filled.Favorite
import androidx.compose.material.icons.filled.Notifications
import androidx.compose.material.icons.filled.NotificationsActive
import androidx.compose.material.icons.filled.Public
import androidx.compose.material.icons.filled.Radar
import androidx.compose.material.icons.filled.Refresh
import androidx.compose.material.icons.filled.RestartAlt
import androidx.compose.material.icons.filled.Security
import androidx.compose.material.icons.filled.Speed
import androidx.compose.material.icons.filled.Tune
import androidx.compose.material.icons.filled.VolumeUp
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.DropdownMenu
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Slider
import androidx.compose.material3.SliderDefaults
import androidx.compose.material3.Switch
import androidx.compose.material3.SwitchDefaults
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.data.AstraRepository
import com.example.model.AlertSeverity
import com.example.model.CabinEnvironment
import com.example.model.CrewMember
import com.example.model.DestinationType
import com.example.model.RiskLevel
import com.example.model.ShiftEventCategory
import com.example.model.ShiftTimelineEvent
import com.example.model.SystemFeatureConfig
import com.example.service.AstraMissionRiskNotificationService
import com.example.ui.theme.BrightTeal
import com.example.ui.theme.CriticalRed
import com.example.ui.theme.GlassBorderStroke
import com.example.ui.theme.NeonCyan
import com.example.ui.theme.NominalGreen
import com.example.ui.theme.StarlightBlue
import com.example.ui.theme.WarningAmber
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale
import java.util.UUID

/**
 * Reusable Manual Feature Controls & Configuration Component.
 * Allows flight surgeons, mission controllers, and crew to manually edit and dial
 * all parameters, environmental metrics, biometrics, subsystem toggles, and notification alerts.
 */
@Composable
fun ManualFeatureControlsContent(
  currentUser: CrewMember,
  allCrewMembers: List<CrewMember>,
  cabinEnv: CabinEnvironment,
  featureConfig: SystemFeatureConfig,
  currentDestination: DestinationType,
  onDestinationSelected: (DestinationType) -> Unit,
  modifier: Modifier = Modifier
) {
  val context = LocalContext.current

  // Target Crew Member for manual biometrics adjustments
  var selectedMemberId by remember(currentUser) { mutableStateOf(currentUser.id) }
  val targetMember = remember(allCrewMembers, selectedMemberId) {
    allCrewMembers.find { it.id == selectedMemberId } ?: currentUser
  }

  var destinationMenuOpen by remember { mutableStateOf(false) }

  Column(modifier = modifier.fillMaxWidth(), verticalArrangement = Arrangement.spacedBy(14.dp)) {

    // 1. Simulation Presets & Master Reset
    GlassCard(modifier = Modifier.fillMaxWidth().testTag("simulation_presets_card")) {
      Column {
        Row(
          modifier = Modifier.fillMaxWidth(),
          horizontalArrangement = Arrangement.SpaceBetween,
          verticalAlignment = Alignment.CenterVertically
        ) {
          Row(verticalAlignment = Alignment.CenterVertically) {
            Icon(Icons.Default.Bolt, contentDescription = null, tint = NeonCyan, modifier = Modifier.size(16.dp))
            Spacer(modifier = Modifier.width(6.dp))
            Text(
              "QUICK SIMULATION PRESETS",
              style = MaterialTheme.typography.titleSmall.copy(color = Color.White, fontWeight = FontWeight.Bold, fontSize = 11.sp)
            )
          }

          Button(
            onClick = {
              AstraRepository.resetAllFeaturesToNominal()
              Toast.makeText(context, "System reset to NASA nominal defaults", Toast.LENGTH_SHORT).show()
            },
            colors = ButtonDefaults.buttonColors(containerColor = BrightTeal.copy(alpha = 0.25f), contentColor = BrightTeal),
            shape = RoundedCornerShape(8.dp),
            modifier = Modifier.height(30.dp).testTag("reset_all_defaults_button")
          ) {
            Icon(Icons.Default.RestartAlt, contentDescription = null, modifier = Modifier.size(12.dp))
            Spacer(modifier = Modifier.width(4.dp))
            Text("MASTER RESET", style = MaterialTheme.typography.labelSmall.copy(fontSize = 8.5.sp))
          }
        }

        Spacer(modifier = Modifier.height(10.dp))

        Row(
          modifier = Modifier.fillMaxWidth().horizontalScroll(rememberScrollState()),
          horizontalArrangement = Arrangement.spacedBy(6.dp)
        ) {
          ControlPresetChip("⚡ NOMINAL", NeonCyan) {
            AstraRepository.resetCabinNominal()
            Toast.makeText(context, "Applied: Nominal Flight Condition", Toast.LENGTH_SHORT).show()
          }
          ControlPresetChip("⚠️ DEPRESS (91.4 kPa)", CriticalRed) {
            AstraRepository.triggerEmergencyDepressurizationSimulation()
            Toast.makeText(context, "Applied: Depressurization Emergency", Toast.LENGTH_SHORT).show()
          }
          ControlPresetChip("☢️ SOLAR SPE (1.84 mSv/h)", CriticalRed) {
            AstraRepository.triggerSolarStormRadiationSimulation()
            Toast.makeText(context, "Applied: Solar Storm Event", Toast.LENGTH_SHORT).show()
          }
          ControlPresetChip("💨 HIGH CO₂ (5,420 ppm)", WarningAmber) {
            AstraRepository.triggerAirScrubberEmergencySimulation()
            Toast.makeText(context, "Applied: Air Scrubber Failure", Toast.LENGTH_SHORT).show()
          }
          ControlPresetChip("💔 TACHYCARDIA (145 bpm)", CriticalRed) {
            val v = targetMember.vitals.copy(heartRateBpm = 145, spo2Percentage = 89, systolicBp = 148)
            AstraRepository.updateCrewMember(targetMember.copy(vitals = v))
            Toast.makeText(context, "Applied: Cardiac Tachycardia Spike", Toast.LENGTH_SHORT).show()
          }
          ControlPresetChip("😴 FATIGUE ACCUMULATION", WarningAmber) {
            val v = targetMember.vitals.copy(hrvMs = 24)
            AstraRepository.updateCrewMember(targetMember.copy(vitals = v, lastPvtReactionTimeMs = 430))
            Toast.makeText(context, "Applied: Acute Crew Fatigue", Toast.LENGTH_SHORT).show()
          }
        }
      }
    }

    // 2. Spacecraft Cabin & ECLSS Environment Overrides
    GlassCard(modifier = Modifier.fillMaxWidth().testTag("cabin_controls_card")) {
      Column {
        Row(verticalAlignment = Alignment.CenterVertically) {
          Icon(Icons.Default.Air, contentDescription = null, tint = NeonCyan, modifier = Modifier.size(18.dp))
          Spacer(modifier = Modifier.width(8.dp))
          Text(
            "SPACECRAFT CABIN & ECLSS MANUAL OVERRIDES",
            style = MaterialTheme.typography.titleSmall.copy(color = Color.White, fontWeight = FontWeight.Bold)
          )
        }

        Spacer(modifier = Modifier.height(12.dp))

        // Cabin Pressure (kPa)
        ControlSliderRow(
          label = "Cabin Pressure",
          valueDisplay = "${"%.1f".format(cabinEnv.cabinPressureKpa)} kPa",
          value = cabinEnv.cabinPressureKpa.toFloat(),
          range = 70f..105f,
          accentColor = if (cabinEnv.cabinPressureKpa < 95.0) CriticalRed else Color.White,
          statusTag = if (cabinEnv.cabinPressureKpa < 95.0) "DECOMPRESSION ALERT" else "NOMINAL",
          onValueChange = { newVal ->
            AstraRepository.updateCabinEnvironment(cabinEnv.copy(cabinPressureKpa = newVal.toDouble()))
          }
        )

        Spacer(modifier = Modifier.height(10.dp))

        // CO2 ppm
        ControlSliderRow(
          label = "Cabin CO₂ Concentration",
          valueDisplay = "${cabinEnv.co2Ppm} ppm",
          value = cabinEnv.co2Ppm.toFloat(),
          range = 400f..6500f,
          accentColor = if (cabinEnv.co2Ppm > 3000) WarningAmber else BrightTeal,
          statusTag = if (cabinEnv.co2Ppm > 3000) "HYPERCAPNIA ALERT" else "NOMINAL",
          onValueChange = { newVal ->
            AstraRepository.updateCabinEnvironment(cabinEnv.copy(co2Ppm = newVal.toInt()))
          }
        )

        Spacer(modifier = Modifier.height(10.dp))

        // Radiation Flux Rate (mSv/h)
        ControlSliderRow(
          label = "Ionizing Radiation Flux Rate",
          valueDisplay = "${"%.3f".format(cabinEnv.radiationRateMsvPerHour)} mSv/h",
          value = cabinEnv.radiationRateMsvPerHour.toFloat(),
          range = 0.01f..3.0f,
          accentColor = if (cabinEnv.radiationRateMsvPerHour > 1.0) CriticalRed else NeonCyan,
          statusTag = if (cabinEnv.radiationRateMsvPerHour > 1.0) "SOLAR SPE STORM" else "NOMINAL",
          onValueChange = { newVal ->
            AstraRepository.updateCabinEnvironment(cabinEnv.copy(radiationRateMsvPerHour = newVal.toDouble()))
          }
        )

        Spacer(modifier = Modifier.height(10.dp))

        // Oxygen Percentage (%)
        ControlSliderRow(
          label = "Atmospheric Oxygen (O₂)",
          valueDisplay = "${"%.1f".format(cabinEnv.o2Percentage)}%",
          value = cabinEnv.o2Percentage.toFloat(),
          range = 14f..25f,
          accentColor = if (cabinEnv.o2Percentage < 19.5) WarningAmber else NominalGreen,
          statusTag = if (cabinEnv.o2Percentage < 19.5) "LOW OXYGEN" else "NOMINAL",
          onValueChange = { newVal ->
            AstraRepository.updateCabinEnvironment(cabinEnv.copy(o2Percentage = newVal.toDouble()))
          }
        )

        Spacer(modifier = Modifier.height(10.dp))

        // Cabin Temperature (°C)
        ControlSliderRow(
          label = "Cabin Temperature",
          valueDisplay = "${"%.1f".format(cabinEnv.temperatureCelsius)}°C",
          value = cabinEnv.temperatureCelsius.toFloat(),
          range = 16f..28f,
          accentColor = Color(0xFFBAE6FD),
          statusTag = "NOMINAL",
          onValueChange = { newVal ->
            AstraRepository.updateCabinEnvironment(cabinEnv.copy(temperatureCelsius = newVal.toDouble()))
          }
        )

        Spacer(modifier = Modifier.height(10.dp))

        // Hull Structural Integrity (%)
        ControlSliderRow(
          label = "Hull Structural Integrity",
          valueDisplay = "${"%.1f".format(cabinEnv.hullIntegrityPercent)}%",
          value = cabinEnv.hullIntegrityPercent.toFloat(),
          range = 60f..100f,
          accentColor = if (cabinEnv.hullIntegrityPercent < 90.0) CriticalRed else NeonCyan,
          statusTag = if (cabinEnv.hullIntegrityPercent < 90.0) "BREACH RISK" else "NOMINAL",
          onValueChange = { newVal ->
            AstraRepository.updateCabinEnvironment(cabinEnv.copy(hullIntegrityPercent = newVal.toDouble()))
          }
        )

        Spacer(modifier = Modifier.height(12.dp))

        // Subsystem Switches
        Row(
          modifier = Modifier.fillMaxWidth(),
          horizontalArrangement = Arrangement.SpaceBetween,
          verticalAlignment = Alignment.CenterVertically
        ) {
          Column {
            Text("CDRA / LiOH CO₂ Scrubbers Active", style = MaterialTheme.typography.bodySmall.copy(color = Color.White))
            Text("Atmospheric regenerative scrubbers", style = MaterialTheme.typography.labelSmall.copy(color = Color.Gray, fontSize = 8.5.sp))
          }
          Switch(
            checked = cabinEnv.scrubbersActive,
            onCheckedChange = { isChecked ->
              AstraRepository.updateCabinEnvironment(cabinEnv.copy(scrubbersActive = isChecked))
            },
            colors = SwitchDefaults.colors(checkedThumbColor = NeonCyan, checkedTrackColor = NeonCyan.copy(alpha = 0.3f))
          )
        }

        Spacer(modifier = Modifier.height(8.dp))

        Row(
          modifier = Modifier.fillMaxWidth(),
          horizontalArrangement = Arrangement.SpaceBetween,
          verticalAlignment = Alignment.CenterVertically
        ) {
          Column {
            Text("Polyethylene Storm Shielding", style = MaterialTheme.typography.bodySmall.copy(color = Color.White))
            Text("Water-wall radiation shelter perimeter", style = MaterialTheme.typography.labelSmall.copy(color = Color.Gray, fontSize = 8.5.sp))
          }
          Switch(
            checked = cabinEnv.stormShieldEngaged,
            onCheckedChange = { isChecked ->
              AstraRepository.updateCabinEnvironment(cabinEnv.copy(stormShieldEngaged = isChecked))
            },
            colors = SwitchDefaults.colors(checkedThumbColor = NeonCyan, checkedTrackColor = NeonCyan.copy(alpha = 0.3f))
          )
        }
      }
    }

    // 3. Astronaut Biometrics & Physiological Manual Overrides
    GlassCard(modifier = Modifier.fillMaxWidth().testTag("biometrics_override_card")) {
      Column {
        Row(
          modifier = Modifier.fillMaxWidth(),
          horizontalArrangement = Arrangement.SpaceBetween,
          verticalAlignment = Alignment.CenterVertically
        ) {
          Row(verticalAlignment = Alignment.CenterVertically) {
            Icon(Icons.Default.Favorite, contentDescription = null, tint = CriticalRed, modifier = Modifier.size(18.dp))
            Spacer(modifier = Modifier.width(8.dp))
            Text(
              "ASTRONAUT BIOMETRICS OVERRIDES",
              style = MaterialTheme.typography.titleSmall.copy(color = Color.White, fontWeight = FontWeight.Bold)
            )
          }

          // Member Switcher Chips
          Row(horizontalArrangement = Arrangement.spacedBy(4.dp)) {
            allCrewMembers.forEach { member ->
              val isSel = member.id == selectedMemberId
              Box(
                modifier = Modifier
                  .clip(RoundedCornerShape(6.dp))
                  .background(if (isSel) NeonCyan else Color.White.copy(alpha = 0.08f))
                  .clickable { selectedMemberId = member.id }
                  .padding(horizontal = 6.dp, vertical = 2.dp)
              ) {
                Text(
                  member.fullName.split(" ").last(),
                  style = MaterialTheme.typography.labelSmall.copy(
                    color = if (isSel) Color.Black else Color.LightGray,
                    fontSize = 8.sp,
                    fontWeight = if (isSel) FontWeight.Bold else FontWeight.Normal
                  )
                )
              }
            }
          }
        }

        Spacer(modifier = Modifier.height(6.dp))
        Text(
          "Configuring parameters for: ${targetMember.fullName} (${targetMember.id})",
          style = MaterialTheme.typography.labelSmall.copy(color = BrightTeal, fontSize = 9.sp)
        )

        Spacer(modifier = Modifier.height(12.dp))

        // Heart Rate
        ControlSliderRow(
          label = "Heart Rate (BPM)",
          valueDisplay = "${targetMember.vitals.heartRateBpm} bpm",
          value = targetMember.vitals.heartRateBpm.toFloat(),
          range = 40f..180f,
          accentColor = if (targetMember.vitals.heartRateBpm > 120 || targetMember.vitals.heartRateBpm < 50) CriticalRed else BrightTeal,
          statusTag = if (targetMember.vitals.heartRateBpm > 120) "TACHYCARDIA" else if (targetMember.vitals.heartRateBpm < 50) "BRADYCARDIA" else "NOMINAL",
          onValueChange = { newVal ->
            val v = targetMember.vitals.copy(heartRateBpm = newVal.toInt())
            AstraRepository.updateCrewMember(targetMember.copy(vitals = v))
          }
        )

        Spacer(modifier = Modifier.height(10.dp))

        // SpO2
        ControlSliderRow(
          label = "Oxygen Saturation (SpO₂)",
          valueDisplay = "${targetMember.vitals.spo2Percentage}%",
          value = targetMember.vitals.spo2Percentage.toFloat(),
          range = 80f..100f,
          accentColor = if (targetMember.vitals.spo2Percentage < 92) CriticalRed else NominalGreen,
          statusTag = if (targetMember.vitals.spo2Percentage < 92) "HYPOXIA ALERT" else "NOMINAL",
          onValueChange = { newVal ->
            val v = targetMember.vitals.copy(spo2Percentage = newVal.toInt())
            AstraRepository.updateCrewMember(targetMember.copy(vitals = v))
          }
        )

        Spacer(modifier = Modifier.height(10.dp))

        // Systolic Blood Pressure
        ControlSliderRow(
          label = "Systolic Blood Pressure",
          valueDisplay = "${targetMember.vitals.systolicBp} mmHg",
          value = targetMember.vitals.systolicBp.toFloat(),
          range = 90f..180f,
          accentColor = Color(0xFFBAE6FD),
          statusTag = "SYS: ${targetMember.vitals.systolicBp}",
          onValueChange = { newVal ->
            val v = targetMember.vitals.copy(systolicBp = newVal.toInt())
            AstraRepository.updateCrewMember(targetMember.copy(vitals = v))
          }
        )

        Spacer(modifier = Modifier.height(10.dp))

        // Diastolic Blood Pressure
        ControlSliderRow(
          label = "Diastolic Blood Pressure",
          valueDisplay = "${targetMember.vitals.diastolicBp} mmHg",
          value = targetMember.vitals.diastolicBp.toFloat(),
          range = 50f..120f,
          accentColor = Color(0xFFBAE6FD),
          statusTag = "DIA: ${targetMember.vitals.diastolicBp}",
          onValueChange = { newVal ->
            val v = targetMember.vitals.copy(diastolicBp = newVal.toInt())
            AstraRepository.updateCrewMember(targetMember.copy(vitals = v))
          }
        )

        Spacer(modifier = Modifier.height(10.dp))

        // Core Body Temperature
        ControlSliderRow(
          label = "Core Body Temperature",
          valueDisplay = "${"%.1f".format(targetMember.vitals.coreTempCelsius)}°C",
          value = targetMember.vitals.coreTempCelsius.toFloat(),
          range = 35.5f..40.5f,
          accentColor = WarningAmber,
          statusTag = if (targetMember.vitals.coreTempCelsius > 38.0) "PYREXIA / FEVER" else "NOMINAL",
          onValueChange = { newVal ->
            val v = targetMember.vitals.copy(coreTempCelsius = newVal.toDouble())
            AstraRepository.updateCrewMember(targetMember.copy(vitals = v))
          }
        )

        Spacer(modifier = Modifier.height(10.dp))

        // Respiration Rate
        ControlSliderRow(
          label = "Respiration Rate",
          valueDisplay = "${targetMember.vitals.respirationRateBpm} bpm",
          value = targetMember.vitals.respirationRateBpm.toFloat(),
          range = 8f..32f,
          accentColor = Color(0xFFE2E8F0),
          statusTag = if (targetMember.vitals.respirationRateBpm > 24) "TACHYPNEA" else "NOMINAL",
          onValueChange = { newVal ->
            val v = targetMember.vitals.copy(respirationRateBpm = newVal.toInt())
            AstraRepository.updateCrewMember(targetMember.copy(vitals = v))
          }
        )

        Spacer(modifier = Modifier.height(10.dp))

        // Heart Rate Variability (HRV ms)
        ControlSliderRow(
          label = "Heart Rate Variability (HRV)",
          valueDisplay = "${targetMember.vitals.hrvMs} ms",
          value = targetMember.vitals.hrvMs.toFloat(),
          range = 10f..110f,
          accentColor = if (targetMember.vitals.hrvMs < 30) WarningAmber else NominalGreen,
          statusTag = if (targetMember.vitals.hrvMs < 30) "AUTONOMIC STRESS" else "STABLE",
          onValueChange = { newVal ->
            val v = targetMember.vitals.copy(hrvMs = newVal.toInt())
            AstraRepository.updateCrewMember(targetMember.copy(vitals = v))
          }
        )

        Spacer(modifier = Modifier.height(10.dp))

        // PVT Reaction Time (ms)
        ControlSliderRow(
          label = "PVT Cognitive Reaction Time",
          valueDisplay = "${targetMember.lastPvtReactionTimeMs} ms",
          value = targetMember.lastPvtReactionTimeMs.toFloat(),
          range = 180f..550f,
          accentColor = if (targetMember.lastPvtReactionTimeMs > 300) WarningAmber else BrightTeal,
          statusTag = if (targetMember.lastPvtReactionTimeMs > 300) "FATIGUE DECREMENT" else "NOMINAL",
          onValueChange = { newVal ->
            AstraRepository.updateCrewMember(targetMember.copy(lastPvtReactionTimeMs = newVal.toInt()))
          }
        )
      }
    }

    // 4. Mission Risk Engine & Score Overrides
    GlassCard(modifier = Modifier.fillMaxWidth().testTag("subsystem_config_card")) {
      Column {
        Row(verticalAlignment = Alignment.CenterVertically) {
          Icon(Icons.Default.Speed, contentDescription = null, tint = NeonCyan, modifier = Modifier.size(18.dp))
          Spacer(modifier = Modifier.width(8.dp))
          Text(
            "MISSION RISK ENGINE & SCORE OVERRIDES",
            style = MaterialTheme.typography.titleSmall.copy(color = Color.White, fontWeight = FontWeight.Bold)
          )
        }

        Spacer(modifier = Modifier.height(12.dp))

        Text(
          "Risk Calculation Mode Override:",
          style = MaterialTheme.typography.bodySmall.copy(color = Color.White, fontWeight = FontWeight.SemiBold)
        )
        Spacer(modifier = Modifier.height(6.dp))

        Row(
          modifier = Modifier.fillMaxWidth(),
          horizontalArrangement = Arrangement.spacedBy(6.dp)
        ) {
          listOf(
            null to "Automatic",
            RiskLevel.LOW to "Force Low",
            RiskLevel.MODERATE to "Force Mod",
            RiskLevel.HIGH to "Force High"
          ).forEach { (overrideLevel, label) ->
            val isSelected = featureConfig.manualRiskOverride == overrideLevel && featureConfig.manualRiskScoreValue == null
            Box(
              modifier = Modifier
                .weight(1f)
                .clip(RoundedCornerShape(8.dp))
                .background(if (isSelected) (if (overrideLevel == RiskLevel.HIGH) CriticalRed.copy(alpha = 0.35f) else BrightTeal.copy(alpha = 0.25f)) else Color.White.copy(alpha = 0.05f))
                .border(1.dp, if (isSelected) (if (overrideLevel == RiskLevel.HIGH) CriticalRed else BrightTeal) else Color.White.copy(alpha = 0.1f), RoundedCornerShape(8.dp))
                .clickable {
                  AstraRepository.updateSystemFeatureConfig(
                    featureConfig.copy(manualRiskOverride = overrideLevel, manualRiskScoreValue = null)
                  )
                }
                .padding(vertical = 6.dp),
              contentAlignment = Alignment.Center
            ) {
              Text(
                label,
                style = MaterialTheme.typography.labelSmall.copy(
                  color = if (isSelected) Color.White else Color.LightGray,
                  fontSize = 8.sp,
                  fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Normal
                )
              )
            }
          }
        }

        Spacer(modifier = Modifier.height(12.dp))

        // Direct Exact Risk Score Value Dial (0..100)
        val exactScore = featureConfig.manualRiskScoreValue ?: 0
        ControlSliderRow(
          label = "Custom Exact Risk Dial (0-100)",
          valueDisplay = if (featureConfig.manualRiskScoreValue != null) "$exactScore/100 (MANUAL)" else "AUTO COMPUTED",
          value = exactScore.toFloat(),
          range = 0f..100f,
          accentColor = if (exactScore >= 70) CriticalRed else if (exactScore >= 34) WarningAmber else NominalGreen,
          statusTag = if (featureConfig.manualRiskScoreValue != null) "CUSTOM DIAL ACTIVE" else "TAP SLIDER TO DIAL",
          onValueChange = { newVal ->
            AstraRepository.updateSystemFeatureConfig(
              featureConfig.copy(manualRiskScoreValue = newVal.toInt(), manualRiskOverride = null)
            )
          }
        )

        if (featureConfig.manualRiskScoreValue != null) {
          Spacer(modifier = Modifier.height(6.dp))
          Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.End
          ) {
            Button(
              onClick = {
                AstraRepository.updateSystemFeatureConfig(featureConfig.copy(manualRiskScoreValue = null))
              },
              colors = ButtonDefaults.buttonColors(containerColor = Color.White.copy(alpha = 0.1f)),
              shape = RoundedCornerShape(6.dp),
              modifier = Modifier.height(28.dp)
            ) {
              Text("REVERT TO LIVE AUTOMATIC RISK", style = MaterialTheme.typography.labelSmall.copy(color = BrightTeal, fontSize = 8.sp))
            }
          }
        }
      }
    }

    // 5. Local Push Notifications & Medical Alert Thresholds
    GlassCard(modifier = Modifier.fillMaxWidth().testTag("notifications_config_card")) {
      Column {
        Row(
          modifier = Modifier.fillMaxWidth(),
          horizontalArrangement = Arrangement.SpaceBetween,
          verticalAlignment = Alignment.CenterVertically
        ) {
          Row(verticalAlignment = Alignment.CenterVertically) {
            Icon(Icons.Default.NotificationsActive, contentDescription = null, tint = WarningAmber, modifier = Modifier.size(18.dp))
            Spacer(modifier = Modifier.width(8.dp))
            Text(
              "LOCAL PUSH NOTIFICATIONS & ALARM CONTROLS",
              style = MaterialTheme.typography.titleSmall.copy(color = Color.White, fontWeight = FontWeight.Bold)
            )
          }

          Button(
            onClick = {
              AstraMissionRiskNotificationService.triggerEmergencySosNotification(context, currentUser)
              Toast.makeText(context, "Local Emergency Push Notification Dispatched!", Toast.LENGTH_SHORT).show()
            },
            colors = ButtonDefaults.buttonColors(containerColor = CriticalRed.copy(alpha = 0.25f), contentColor = CriticalRed),
            shape = RoundedCornerShape(8.dp),
            modifier = Modifier.height(28.dp).testTag("test_push_notification_button")
          ) {
            Text("TEST ALARM NOW", style = MaterialTheme.typography.labelSmall.copy(fontSize = 8.sp, fontWeight = FontWeight.Bold))
          }
        }

        Spacer(modifier = Modifier.height(12.dp))

        ControlToggleRow(
          title = "Local High-Priority Push Notifications",
          subtitle = "Triggers local heads-up notification when risk exceeds threshold",
          checked = featureConfig.pushNotificationsEnabled,
          onCheckedChange = { isChecked ->
            AstraRepository.updateSystemFeatureConfig(featureConfig.copy(pushNotificationsEnabled = isChecked))
          }
        )

        Spacer(modifier = Modifier.height(8.dp))

        // Threshold Slider
        ControlSliderRow(
          label = "Critical Trigger Threshold",
          valueDisplay = "${featureConfig.criticalRiskNotificationThreshold} / 100",
          value = featureConfig.criticalRiskNotificationThreshold.toFloat(),
          range = 50f..95f,
          accentColor = CriticalRed,
          statusTag = "CRITICAL",
          onValueChange = { newVal ->
            AstraRepository.updateSystemFeatureConfig(featureConfig.copy(criticalRiskNotificationThreshold = newVal.toInt()))
          }
        )

        Spacer(modifier = Modifier.height(8.dp))

        ControlToggleRow(
          title = "Medical Siren Audio Alarm",
          subtitle = "Audible tone emitted on critical threshold breaches",
          checked = featureConfig.notificationSoundEnabled,
          onCheckedChange = { isChecked ->
            AstraRepository.updateSystemFeatureConfig(featureConfig.copy(notificationSoundEnabled = isChecked))
          }
        )

        Spacer(modifier = Modifier.height(8.dp))

        ControlToggleRow(
          title = "Mandatory Medical Officer Acknowledgement",
          subtitle = "Notification requires active sign-off to dismiss hazard",
          checked = featureConfig.mandatoryMedicalAckEnabled,
          onCheckedChange = { isChecked ->
            AstraRepository.updateSystemFeatureConfig(featureConfig.copy(mandatoryMedicalAckEnabled = isChecked))
          }
        )
      }
    }

    // 6. Space AI Assistant & Biometric Radar Subsystems
    GlassCard(modifier = Modifier.fillMaxWidth().testTag("ai_radar_config_card")) {
      Column {
        Row(verticalAlignment = Alignment.CenterVertically) {
          Icon(Icons.Default.Radar, contentDescription = null, tint = BrightTeal, modifier = Modifier.size(18.dp))
          Spacer(modifier = Modifier.width(8.dp))
          Text(
            "AI ASSISTANT & RADAR SUBSYSTEMS",
            style = MaterialTheme.typography.titleSmall.copy(color = Color.White, fontWeight = FontWeight.Bold)
          )
        }

        Spacer(modifier = Modifier.height(12.dp))

        // Telemetry Sampling Rates
        Text(
          "Telemetry Acquisition Rate:",
          style = MaterialTheme.typography.bodySmall.copy(color = Color.White, fontWeight = FontWeight.SemiBold)
        )
        Spacer(modifier = Modifier.height(4.dp))
        Row(
          modifier = Modifier.fillMaxWidth(),
          horizontalArrangement = Arrangement.spacedBy(6.dp)
        ) {
          listOf(0.1 to "0.1 Hz (Nominal)", 1.0 to "1.0 Hz (Duty)", 10.0 to "10.0 Hz (SOS)").forEach { (rate, label) ->
            val isSelected = featureConfig.telemetrySampleRateHz == rate
            Box(
              modifier = Modifier
                .weight(1f)
                .clip(RoundedCornerShape(8.dp))
                .background(if (isSelected) NeonCyan.copy(alpha = 0.25f) else Color.White.copy(alpha = 0.05f))
                .border(1.dp, if (isSelected) NeonCyan else Color.White.copy(alpha = 0.1f), RoundedCornerShape(8.dp))
                .clickable {
                  AstraRepository.updateSystemFeatureConfig(featureConfig.copy(telemetrySampleRateHz = rate))
                }
                .padding(vertical = 8.dp),
              contentAlignment = Alignment.Center
            ) {
              Text(
                label,
                style = MaterialTheme.typography.labelSmall.copy(
                  color = if (isSelected) NeonCyan else Color.LightGray,
                  fontSize = 8.5.sp,
                  fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Normal
                )
              )
            }
          }
        }

        Spacer(modifier = Modifier.height(12.dp))

        ControlToggleRow(
          title = "Layer 1 Instant Hardware Rules",
          subtitle = "Threshold triggers for depressurization, radiation, and CO2",
          checked = featureConfig.layer1HardwareRulesEnabled,
          onCheckedChange = { isChecked ->
            AstraRepository.updateSystemFeatureConfig(featureConfig.copy(layer1HardwareRulesEnabled = isChecked))
          }
        )

        Spacer(modifier = Modifier.height(8.dp))

        ControlToggleRow(
          title = "Layer 2 AI Telemedicine Directives",
          subtitle = "Grounding in NASA-STD-3001 spaceflight clinical protocols",
          checked = featureConfig.layer2AiDirectivesEnabled,
          onCheckedChange = { isChecked ->
            AstraRepository.updateSystemFeatureConfig(featureConfig.copy(layer2AiDirectivesEnabled = isChecked))
          }
        )

        Spacer(modifier = Modifier.height(8.dp))

        ControlToggleRow(
          title = "Biometric Sensor Radar Active",
          subtitle = "Sweeping radar visualization of physiological status",
          checked = featureConfig.biometricRadarEnabled,
          onCheckedChange = { isChecked ->
            AstraRepository.updateSystemFeatureConfig(featureConfig.copy(biometricRadarEnabled = isChecked))
          }
        )

        Spacer(modifier = Modifier.height(8.dp))

        ControlToggleRow(
          title = "Acoustic Phonation Voice Stress Scanner",
          subtitle = "Detects micro-tremors, jitter, and pitch instability in crew voice",
          checked = featureConfig.acousticPhonationScanEnabled,
          onCheckedChange = { isChecked ->
            AstraRepository.updateSystemFeatureConfig(featureConfig.copy(acousticPhonationScanEnabled = isChecked))
          }
        )

        Spacer(modifier = Modifier.height(8.dp))

        ControlToggleRow(
          title = "Space AI Spoken Voice Readout (TTS)",
          subtitle = "Synthesizes verbal guidance during acute emergency checklists",
          checked = featureConfig.aiVoiceTtsEnabled,
          onCheckedChange = { isChecked ->
            AstraRepository.updateSystemFeatureConfig(featureConfig.copy(aiVoiceTtsEnabled = isChecked))
          }
        )

        Spacer(modifier = Modifier.height(8.dp))

        ControlToggleRow(
          title = "Strict Emergency Checklist Directives (NASA-STD-3001)",
          subtitle = "Formats emergency responses into interactive verification checklists",
          checked = featureConfig.aiStrictEmergencyMode,
          onCheckedChange = { isChecked ->
            AstraRepository.updateSystemFeatureConfig(featureConfig.copy(aiStrictEmergencyMode = isChecked))
          }
        )

        Spacer(modifier = Modifier.height(8.dp))

        ControlToggleRow(
          title = "Force Autonomous Deep Space Offline AI Core",
          subtitle = "Guarantees offline edge inference during orbital RF blackout",
          checked = featureConfig.offlineAutonomyForced,
          onCheckedChange = { isChecked ->
            AstraRepository.updateSystemFeatureConfig(featureConfig.copy(offlineAutonomyForced = isChecked))
          }
        )
      }
    }

    // 7. 24-Hour Shift Timeline Event Injector
    GlassCard(modifier = Modifier.fillMaxWidth().testTag("timeline_injector_card")) {
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
            Icon(Icons.Default.Tune, contentDescription = null, tint = NeonCyan, modifier = Modifier.size(16.dp))
            Spacer(modifier = Modifier.width(6.dp))
            Text(
              "TIMELINE EVENT INJECTOR",
              style = MaterialTheme.typography.titleSmall.copy(color = Color.White, fontWeight = FontWeight.Bold, fontSize = 11.5.sp),
              maxLines = 1,
              overflow = TextOverflow.Ellipsis
            )
          }

          Row(
            modifier = Modifier
              .clip(RoundedCornerShape(6.dp))
              .background(BrightTeal.copy(alpha = 0.16f))
              .border(1.dp, BrightTeal.copy(alpha = 0.6f), RoundedCornerShape(6.dp))
              .clickable {
                AstraRepository.clearShiftEvents()
                Toast.makeText(context, "Shift timeline restored to default schedule", Toast.LENGTH_SHORT).show()
              }
              .padding(horizontal = 9.dp, vertical = 5.dp),
            verticalAlignment = Alignment.CenterVertically
          ) {
            Icon(Icons.Default.Refresh, contentDescription = "Reset Timeline", tint = BrightTeal, modifier = Modifier.size(13.dp))
            Spacer(modifier = Modifier.width(4.dp))
            Text(
              "RESET",
              style = MaterialTheme.typography.labelSmall.copy(color = BrightTeal, fontWeight = FontWeight.Bold, fontSize = 9.sp),
              maxLines = 1,
              softWrap = false
            )
          }
        }

        Spacer(modifier = Modifier.height(8.dp))
        Text(
          "Inject custom physiological anomalies or medication logs directly into the 24-hour timeline:",
          style = MaterialTheme.typography.bodySmall.copy(color = Color.LightGray, fontSize = 9.sp)
        )

        Spacer(modifier = Modifier.height(10.dp))

        Row(
          modifier = Modifier.fillMaxWidth().horizontalScroll(rememberScrollState()),
          horizontalArrangement = Arrangement.spacedBy(6.dp)
        ) {
          ControlPresetChip("💊 LOG MEDICATION (Zolpidem 10mg)", BrightTeal) {
            val nowTime = SimpleDateFormat("HH:mm", Locale.US).format(Date())
            AstraRepository.logShiftEvent(
              ShiftTimelineEvent(
                id = "EVT-MED-" + UUID.randomUUID().toString().take(6),
                astronautId = targetMember.id,
                astronautName = targetMember.fullName,
                shiftHour = 22.0,
                shiftTimeLabel = "T+22:00",
                utcTimestamp = "$nowTime UTC",
                category = ShiftEventCategory.MEDICATION,
                severity = AlertSeverity.ADVISORY,
                title = "MEDICATION: SLEEP COUNTERMEASURE",
                description = "Administered 10mg Zolpidem for sleep facilitation prior to rest cycle.",
                primaryMetric = "Zolpidem 10mg oral",
                secondaryMetric = "Prescribed by Flight Surgeon",
                actionDirective = "Log biometric onset latency in 45 minutes.",
                status = "COMPLETED",
                iconType = "medication"
              )
            )
            Toast.makeText(context, "Injected Medication Event into Timeline", Toast.LENGTH_SHORT).show()
          }

          ControlPresetChip("⚠️ LOG BIOMETRIC ANOMALY (Arrhythmia)", WarningAmber) {
            val nowTime = SimpleDateFormat("HH:mm", Locale.US).format(Date())
            AstraRepository.logShiftEvent(
              ShiftTimelineEvent(
                id = "EVT-BIO-" + UUID.randomUUID().toString().take(6),
                astronautId = targetMember.id,
                astronautName = targetMember.fullName,
                shiftHour = 14.5,
                shiftTimeLabel = "T+14:30",
                utcTimestamp = "$nowTime UTC",
                category = ShiftEventCategory.BIOMETRIC,
                severity = AlertSeverity.WARNING,
                title = "CARDIAC ARRHYTHMIA (PVC BURST)",
                description = "Premature ventricular contractions noted during high-intensity cycling countermeasure.",
                primaryMetric = "HR: 154 bpm • 6 PVCs/min",
                secondaryMetric = "Module: Columbus Lab",
                actionDirective = "Reduce workload; administer oral hydration + electrolytes.",
                status = "ACKNOWLEDGED",
                iconType = "biometric"
              )
            )
            Toast.makeText(context, "Injected Arrhythmia Event into Timeline", Toast.LENGTH_SHORT).show()
          }

          ControlPresetChip("💧 LOG HYDRATION COUNTERMEASURE", NeonCyan) {
            val nowTime = SimpleDateFormat("HH:mm", Locale.US).format(Date())
            AstraRepository.logShiftEvent(
              ShiftTimelineEvent(
                id = "EVT-HYD-" + UUID.randomUUID().toString().take(6),
                astronautId = targetMember.id,
                astronautName = targetMember.fullName,
                shiftHour = 11.0,
                shiftTimeLabel = "T+11:00",
                utcTimestamp = "$nowTime UTC",
                category = ShiftEventCategory.MEDICATION,
                severity = AlertSeverity.ADVISORY,
                title = "ELECTROLYTE REHYDRATION PACK",
                description = "Consumed 500 mL sodium-potassium fluid pack for microgravity fluid redistribution.",
                primaryMetric = "500 mL Balanced Electrolytes",
                secondaryMetric = "Scheduled Countermeasure",
                actionDirective = "Continue nominal shift schedule.",
                status = "COMPLETED",
                iconType = "countermeasure"
              )
            )
            Toast.makeText(context, "Injected Hydration Event into Timeline", Toast.LENGTH_SHORT).show()
          }
        }
      }
    }

    // 8. Mission Trajectory & Comms Queue Controls
    GlassCard(modifier = Modifier.fillMaxWidth().testTag("trajectory_config_card")) {
      Column {
        Row(verticalAlignment = Alignment.CenterVertically) {
          Icon(Icons.Default.Public, contentDescription = null, tint = NeonCyan, modifier = Modifier.size(18.dp))
          Spacer(modifier = Modifier.width(8.dp))
          Text(
            "MISSION TRAJECTORY & TELEMETRY COMMS",
            style = MaterialTheme.typography.titleSmall.copy(color = Color.White, fontWeight = FontWeight.Bold)
          )
        }

        Spacer(modifier = Modifier.height(12.dp))

        // Mission Destination Selector Dropdown
        Text(
          "Spacecraft Mission Destination Profile:",
          style = MaterialTheme.typography.bodySmall.copy(color = Color.White, fontWeight = FontWeight.SemiBold)
        )
        Spacer(modifier = Modifier.height(4.dp))

        Box {
          Row(
            modifier = Modifier
              .fillMaxWidth()
              .clip(RoundedCornerShape(8.dp))
              .background(Color.Black.copy(alpha = 0.4f))
              .border(1.dp, NeonCyan.copy(alpha = 0.5f), RoundedCornerShape(8.dp))
              .clickable { destinationMenuOpen = true }
              .padding(horizontal = 12.dp, vertical = 10.dp),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
          ) {
            Column {
              Text(
                currentDestination.displayName,
                style = MaterialTheme.typography.titleSmall.copy(color = Color.White, fontWeight = FontWeight.Bold)
              )
              Text(
                "${currentDestination.distanceDescription} • Comms Delay: ${currentDestination.commsDelaySeconds}s",
                style = MaterialTheme.typography.bodySmall.copy(color = BrightTeal, fontSize = 9.sp)
              )
            }
            Text("CHANGE ▼", style = MaterialTheme.typography.labelSmall.copy(color = NeonCyan, fontWeight = FontWeight.Bold, fontSize = 9.sp))
          }

          DropdownMenu(
            expanded = destinationMenuOpen,
            onDismissRequest = { destinationMenuOpen = false },
            modifier = Modifier
              .background(MaterialTheme.colorScheme.surface)
              .border(1.dp, GlassBorderStroke, RoundedCornerShape(8.dp))
          ) {
            DestinationType.entries.forEach { dest ->
              DropdownMenuItem(
                text = {
                  Column {
                    Text(dest.displayName, style = MaterialTheme.typography.titleSmall.copy(color = Color.White))
                    Text("${dest.distanceDescription} • ${dest.habitatVehicle}", style = MaterialTheme.typography.bodySmall.copy(color = Color.Gray, fontSize = 10.sp))
                  }
                },
                onClick = {
                  onDestinationSelected(dest)
                  destinationMenuOpen = false
                  Toast.makeText(context, "Destination set to ${dest.displayName}", Toast.LENGTH_SHORT).show()
                }
              )
            }
          }
        }
      }
    }
  }
}

@Composable
fun ControlSliderRow(
  label: String,
  valueDisplay: String,
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
      Text(
        text = label,
        style = MaterialTheme.typography.bodySmall.copy(color = Color.LightGray, fontSize = 11.sp),
        maxLines = 1,
        overflow = TextOverflow.Ellipsis,
        modifier = Modifier.weight(1f, fill = false).padding(end = 8.dp)
      )
      Row(
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(6.dp)
      ) {
        Text(
          valueDisplay,
          style = MaterialTheme.typography.titleSmall.copy(color = accentColor, fontWeight = FontWeight.Bold, fontSize = 11.sp)
        )
        val displayTag = when {
          statusTag.equals("CRITICAL", ignoreCase = true) -> "CRIT"
          statusTag.equals("WARNING", ignoreCase = true) -> "WARN"
          statusTag.equals("NOMINAL", ignoreCase = true) -> "NORM"
          statusTag.length > 5 -> statusTag.take(4).uppercase()
          else -> statusTag.uppercase()
        }
        StatusBadge(
          text = displayTag,
          color = if (statusTag.contains("ALERT") || statusTag.contains("STORM") || statusTag.contains("TACHY") || statusTag.contains("CRIT")) CriticalRed else if (statusTag.contains("FEVER") || statusTag.contains("STRESS") || statusTag.contains("HYPER") || statusTag.contains("WARN")) WarningAmber else BrightTeal
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
fun ControlToggleRow(
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
fun ControlPresetChip(
  label: String,
  color: Color,
  onClick: () -> Unit
) {
  Box(
    modifier = Modifier
      .clip(RoundedCornerShape(8.dp))
      .background(color.copy(alpha = 0.2f))
      .border(1.dp, color.copy(alpha = 0.6f), RoundedCornerShape(8.dp))
      .clickable { onClick() }
      .padding(horizontal = 8.dp, vertical = 6.dp)
  ) {
    Text(
      label,
      style = MaterialTheme.typography.labelSmall.copy(color = Color.White, fontWeight = FontWeight.Bold, fontSize = 9.sp)
    )
  }
}
