package com.example.ui.components

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
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
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.DirectionsRun
import androidx.compose.material.icons.automirrored.filled.Send
import androidx.compose.material.icons.filled.Air
import androidx.compose.material.icons.filled.Bedtime
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.CrisisAlert
import androidx.compose.material.icons.filled.Favorite
import androidx.compose.material.icons.filled.Group
import androidx.compose.material.icons.filled.Groups
import androidx.compose.material.icons.filled.Info
import androidx.compose.material.icons.filled.Psychology
import androidx.compose.material.icons.filled.Shield
import androidx.compose.material.icons.filled.Speed
import androidx.compose.material.icons.filled.Warning
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.OutlinedTextFieldDefaults
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.derivedStateOf
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateListOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.window.Dialog
import com.example.model.AlertSeverity
import com.example.model.CabinEnvironment
import com.example.model.CrewMember
import com.example.service.AggregateCrewHealthService
import com.example.service.AggregateHealthSummary
import com.example.service.MissionStressorPattern
import com.example.service.StressorType
import com.example.ui.theme.BrightTeal
import com.example.ui.theme.CelestialAmber
import com.example.ui.theme.CriticalRed
import com.example.ui.theme.ElectricIndigo
import com.example.ui.theme.GlassBorderStroke
import com.example.ui.theme.NeonCyan
import com.example.ui.theme.NominalGreen
import com.example.ui.theme.StarlightBlue
import com.example.ui.theme.WarningAmber

@Composable
fun AggregateHealthSummaryView(
  allCrewMembers: List<CrewMember>,
  cabinEnv: CabinEnvironment,
  onTransmitFleetDirective: (directive: String) -> Unit,
  modifier: Modifier = Modifier
) {
  // Multi-select set of crew member IDs
  val selectedMemberIds = remember {
    mutableStateListOf<String>().apply {
      addAll(allCrewMembers.map { it.id })
    }
  }

  val selectedCrewMembers by remember(allCrewMembers, selectedMemberIds.toList()) {
    derivedStateOf {
      allCrewMembers.filter { it.id in selectedMemberIds }
    }
  }

  val summary by remember(selectedCrewMembers, cabinEnv) {
    derivedStateOf {
      AggregateCrewHealthService.computeAggregateSummary(selectedCrewMembers, cabinEnv)
    }
  }

  var showDirectiveDialog by remember { mutableStateOf(false) }
  var selectedStressorForDirective by remember { mutableStateOf<MissionStressorPattern?>(null) }

  Column(
    modifier = modifier.testTag("aggregate_health_view"),
    verticalArrangement = Arrangement.spacedBy(14.dp)
  ) {
    // 1. Cohort Selector Strip
    GlassCard(modifier = Modifier.fillMaxWidth()) {
      Column {
        Row(
          modifier = Modifier.fillMaxWidth(),
          horizontalArrangement = Arrangement.SpaceBetween,
          verticalAlignment = Alignment.CenterVertically
        ) {
          Row(verticalAlignment = Alignment.CenterVertically) {
            Icon(Icons.Default.Groups, contentDescription = null, tint = NeonCyan, modifier = Modifier.size(18.dp))
            Spacer(modifier = Modifier.width(8.dp))
            Text(
              "SELECT COHORT TO AGGREGATE",
              style = MaterialTheme.typography.titleSmall.copy(
                color = Color.White,
                fontWeight = FontWeight.Bold,
                letterSpacing = 1.sp
              )
            )
          }

          val allSelected = selectedMemberIds.size == allCrewMembers.size
          Text(
            text = if (allSelected) "DESELECT ALL" else "SELECT ALL",
            style = MaterialTheme.typography.labelSmall.copy(color = BrightTeal, fontWeight = FontWeight.Bold),
            modifier = Modifier
              .clickable {
                if (allSelected) {
                  selectedMemberIds.clear()
                  // Keep at least one
                  allCrewMembers.firstOrNull()?.let { selectedMemberIds.add(it.id) }
                } else {
                  selectedMemberIds.clear()
                  selectedMemberIds.addAll(allCrewMembers.map { it.id })
                }
              }
              .padding(4.dp)
          )
        }

        Spacer(modifier = Modifier.height(10.dp))

        // Astronaut Chip Selector
        Row(
          modifier = Modifier.fillMaxWidth(),
          horizontalArrangement = Arrangement.spacedBy(6.dp)
        ) {
          allCrewMembers.forEach { member ->
            val isSelected = member.id in selectedMemberIds
            Box(
              modifier = Modifier
                .weight(1f)
                .clip(RoundedCornerShape(8.dp))
                .background(if (isSelected) NeonCyan.copy(alpha = 0.2f) else Color.Black.copy(alpha = 0.4f))
                .border(
                  1.dp,
                  if (isSelected) NeonCyan else Color.White.copy(alpha = 0.08f),
                  RoundedCornerShape(8.dp)
                )
                .clickable {
                  if (isSelected) {
                    if (selectedMemberIds.size > 1) {
                      selectedMemberIds.remove(member.id)
                    }
                  } else {
                    selectedMemberIds.add(member.id)
                  }
                }
                .padding(vertical = 8.dp, horizontal = 4.dp),
              contentAlignment = Alignment.Center
            ) {
              Column(horizontalAlignment = Alignment.CenterHorizontally) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                  if (isSelected) {
                    Icon(Icons.Default.Check, contentDescription = null, tint = NeonCyan, modifier = Modifier.size(10.dp))
                    Spacer(modifier = Modifier.width(3.dp))
                  }
                  Text(
                    member.fullName.split(" ").last(),
                    style = MaterialTheme.typography.labelSmall.copy(
                      color = if (isSelected) Color.White else Color.Gray,
                      fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Normal,
                      fontSize = 9.sp
                    )
                  )
                }
                Text(
                  member.id,
                  style = MaterialTheme.typography.labelSmall.copy(
                    color = if (isSelected) BrightTeal else Color.DarkGray,
                    fontSize = 7.sp,
                    fontFamily = FontFamily.Monospace
                  )
                )
              }
            }
          }
        }
      }
    }

    // 2. High-Level Fleet Risk Distribution & Metrics Card
    GlassCard(modifier = Modifier.fillMaxWidth()) {
      Column {
        Row(
          modifier = Modifier.fillMaxWidth(),
          horizontalArrangement = Arrangement.SpaceBetween,
          verticalAlignment = Alignment.CenterVertically
        ) {
          Column {
            Text(
              "COHORT COMPOSITE RISK (${summary.selectedCrewCount} ASTRONAUTS)",
              style = MaterialTheme.typography.labelSmall.copy(color = BrightTeal, letterSpacing = 1.sp)
            )
            Row(verticalAlignment = Alignment.Bottom) {
              Text(
                "${String.format("%.1f", summary.averageRiskScore)}",
                style = MaterialTheme.typography.displaySmall.copy(
                  color = Color.White,
                  fontWeight = FontWeight.Bold
                )
              )
              Text(
                " / 100 MEAN",
                style = MaterialTheme.typography.labelMedium.copy(color = Color.Gray),
                modifier = Modifier.padding(bottom = 6.dp, start = 4.dp)
              )
            }
          }

          Column(horizontalAlignment = Alignment.End) {
            Text("PEAK INDIVIDUAL RISK", style = MaterialTheme.typography.labelSmall.copy(color = Color.Gray, fontSize = 8.sp))
            Text(
              "${summary.maxRiskScore}/100",
              style = MaterialTheme.typography.titleMedium.copy(
                color = if (summary.maxRiskScore >= 70) CriticalRed else if (summary.maxRiskScore >= 35) WarningAmber else NominalGreen,
                fontWeight = FontWeight.Bold,
                fontFamily = FontFamily.Monospace
              )
            )
            Text(
              summary.highestRiskMemberName.split(" ").last(),
              style = MaterialTheme.typography.labelSmall.copy(color = Color.LightGray, fontSize = 9.sp)
            )
          }
        }

        Spacer(modifier = Modifier.height(10.dp))

        // Risk Category Distribution Bar
        Row(
          modifier = Modifier
            .fillMaxWidth()
            .height(12.dp)
            .clip(RoundedCornerShape(6.dp))
            .background(Color.White.copy(alpha = 0.08f))
        ) {
          val total = summary.selectedCrewCount.toFloat().coerceAtLeast(1f)
          if (summary.lowRiskCount > 0) {
            Box(
              modifier = Modifier
                .weight(summary.lowRiskCount / total)
                .fillMaxWidth()
                .background(NominalGreen)
            )
          }
          if (summary.moderateRiskCount > 0) {
            Box(
              modifier = Modifier
                .weight(summary.moderateRiskCount / total)
                .fillMaxWidth()
                .background(WarningAmber)
            )
          }
          if (summary.highRiskCount > 0) {
            Box(
              modifier = Modifier
                .weight(summary.highRiskCount / total)
                .fillMaxWidth()
                .background(CriticalRed)
            )
          }
        }

        Spacer(modifier = Modifier.height(6.dp))

        Row(
          modifier = Modifier.fillMaxWidth(),
          horizontalArrangement = Arrangement.SpaceBetween
        ) {
          Text("● ${summary.lowRiskCount} Low Risk", style = MaterialTheme.typography.labelSmall.copy(color = NominalGreen, fontSize = 8.sp))
          Text("● ${summary.moderateRiskCount} Moderate", style = MaterialTheme.typography.labelSmall.copy(color = WarningAmber, fontSize = 8.sp))
          Text("● ${summary.highRiskCount} Critical High", style = MaterialTheme.typography.labelSmall.copy(color = CriticalRed, fontSize = 8.sp))
        }
      }
    }

    // 3. Four-Quadrant Detailed Aggregate Telemetry Grid
    Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
      Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(8.dp)) {
        // Autonomic & Vitals
        AggregateMetricTile(
          icon = Icons.Default.Favorite,
          iconColor = CriticalRed,
          categoryTitle = "AUTONOMIC & VITALS",
          primaryStat = "${String.format("%.0f", summary.averageHeartRateBpm)} bpm",
          secondaryStat = "HRV: ${String.format("%.0f", summary.averageHrvMs)}ms • SpO₂: ${String.format("%.0f", summary.averageSpo2Percent)}%",
          statusLabel = if (summary.averageHeartRateBpm > 80) "Autonomic Elevation" else "Nominal Homeostasis",
          isAlert = summary.averageHeartRateBpm > 80,
          modifier = Modifier.weight(1f)
        )

        // Cognitive & PVT Alertness
        AggregateMetricTile(
          icon = Icons.Default.Speed,
          iconColor = NeonCyan,
          categoryTitle = "COGNITIVE VIGILANCE",
          primaryStat = "${String.format("%.0f", summary.averagePvtReactionMs)} ms",
          secondaryStat = "Lapses: ${summary.totalPvtLapses} • Sleep: ${String.format("%.1f", summary.averageSleepHours)}h",
          statusLabel = if (summary.averagePvtReactionMs > 250) "Reaction Latency Lag" else "Alertness Peak",
          isAlert = summary.averagePvtReactionMs > 250,
          modifier = Modifier.weight(1f)
        )
      }

      Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(8.dp)) {
        // Radiation Dose
        AggregateMetricTile(
          icon = Icons.Default.Shield,
          iconColor = WarningAmber,
          categoryTitle = "CUMULATIVE RADIATION",
          primaryStat = "${String.format("%.2f", summary.averageDailyRadiationMsv)} mSv",
          secondaryStat = "Max: ${String.format("%.2f", summary.maxDailyRadiationMsv)} mSv/d",
          statusLabel = if (summary.averageDailyRadiationMsv > 0.60) "High SPE Exposure" else "Shielded Nominal",
          isAlert = summary.averageDailyRadiationMsv > 0.60,
          modifier = Modifier.weight(1f)
        )

        // Musculoskeletal & Stress
        AggregateMetricTile(
          icon = Icons.AutoMirrored.Filled.DirectionsRun,
          iconColor = BrightTeal,
          categoryTitle = "CONDITIONING & STRESS",
          primaryStat = "${String.format("%.1f", summary.averageAredForceKn)} kN",
          secondaryStat = "BMD: ${String.format("%.1f", summary.averageBoneRetentionPercent)}% • Stress: ${String.format("%.0f", summary.averageStressIndex)}",
          statusLabel = if (summary.averageAredForceKn < 4.5) "ARED Load Deficit" else "Target Force Met",
          isAlert = summary.averageAredForceKn < 4.5,
          modifier = Modifier.weight(1f)
        )
      }
    }

    // 4. Mission-Wide Stressor Patterns & Early Warnings
    GlassCard(modifier = Modifier.fillMaxWidth()) {
      Column {
        Row(
          modifier = Modifier.fillMaxWidth(),
          horizontalArrangement = Arrangement.SpaceBetween,
          verticalAlignment = Alignment.CenterVertically
        ) {
          Row(verticalAlignment = Alignment.CenterVertically) {
            Icon(Icons.Default.CrisisAlert, contentDescription = null, tint = WarningAmber, modifier = Modifier.size(18.dp))
            Spacer(modifier = Modifier.width(8.dp))
            Text(
              "MISSION-WIDE STRESSORS & PATTERNS",
              style = MaterialTheme.typography.titleMedium.copy(
                color = Color.White,
                fontWeight = FontWeight.Bold,
                letterSpacing = 0.5.sp
              )
            )
          }

          StatusBadge(
            text = "${summary.detectedStressors.size} PATTERNS",
            color = if (summary.detectedStressors.any { it.severity == AlertSeverity.CRITICAL }) CriticalRed else WarningAmber
          )
        }

        Spacer(modifier = Modifier.height(6.dp))

        Text(
          "Cross-astronaut physiological pattern recognition correlating cabin environmental sensor data with vital trends.",
          style = MaterialTheme.typography.bodySmall.copy(color = Color(0xFF94A3B8), fontSize = 10.sp)
        )

        Spacer(modifier = Modifier.height(12.dp))

        if (summary.detectedStressors.isEmpty()) {
          Box(
            modifier = Modifier
              .fillMaxWidth()
              .clip(RoundedCornerShape(10.dp))
              .background(Color.Black.copy(alpha = 0.3f))
              .padding(16.dp),
            contentAlignment = Alignment.Center
          ) {
            Text(
              "No multi-crew stressor patterns detected. All systemic telemetry within nominal bounds.",
              color = NominalGreen,
              style = MaterialTheme.typography.bodySmall
            )
          }
        } else {
          Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
            summary.detectedStressors.forEach { stressor ->
              StressorPatternCard(
                pattern = stressor,
                onInitiateDirective = {
                  selectedStressorForDirective = stressor
                  showDirectiveDialog = true
                }
              )
            }
          }
        }
      }
    }

    // 5. Button to Broadcast Fleet Medical Directive
    Button(
      onClick = {
        selectedStressorForDirective = summary.detectedStressors.firstOrNull()
        showDirectiveDialog = true
      },
      modifier = Modifier
        .fillMaxWidth()
        .height(46.dp)
        .testTag("broadcast_fleet_directive_button"),
      colors = ButtonDefaults.buttonColors(containerColor = NeonCyan, contentColor = Color.Black),
      shape = RoundedCornerShape(12.dp)
    ) {
      Icon(Icons.AutoMirrored.Filled.Send, contentDescription = null, modifier = Modifier.size(16.dp))
      Spacer(modifier = Modifier.width(8.dp))
      Text(
        "TRANSMIT FLEET MEDICAL COUNTERMEASURE DIRECTIVE",
        style = MaterialTheme.typography.labelSmall.copy(fontWeight = FontWeight.Bold, letterSpacing = 0.5.sp)
      )
    }
  }

  // Dialog to transmit fleet-wide directive
  if (showDirectiveDialog) {
    FleetDirectiveDialog(
      prefilledDirective = selectedStressorForDirective?.recommendedFleetCountermeasure ?: "Maintain scheduled circadian rest cycle and nominal electrolyte hydration.",
      targetAstronautsCount = selectedCrewMembers.size,
      onDismiss = { showDirectiveDialog = false },
      onConfirm = { directiveText ->
        onTransmitFleetDirective(directiveText)
        showDirectiveDialog = false
      }
    )
  }
}

@Composable
private fun AggregateMetricTile(
  icon: ImageVector,
  iconColor: Color,
  categoryTitle: String,
  primaryStat: String,
  secondaryStat: String,
  statusLabel: String,
  isAlert: Boolean,
  modifier: Modifier = Modifier
) {
  Box(
    modifier = modifier
      .clip(RoundedCornerShape(12.dp))
      .background(Color.Black.copy(alpha = 0.4f))
      .border(
        1.dp,
        if (isAlert) WarningAmber.copy(alpha = 0.5f) else Color.White.copy(alpha = 0.08f),
        RoundedCornerShape(12.dp)
      )
      .padding(10.dp)
  ) {
    Column {
      Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically
      ) {
        Text(categoryTitle, style = MaterialTheme.typography.labelSmall.copy(color = Color.Gray, fontSize = 7.5.sp))
        Icon(icon, contentDescription = null, tint = iconColor, modifier = Modifier.size(13.dp))
      }

      Spacer(modifier = Modifier.height(4.dp))

      Text(
        primaryStat,
        style = MaterialTheme.typography.titleMedium.copy(
          color = Color.White,
          fontWeight = FontWeight.Bold,
          fontFamily = FontFamily.Monospace
        )
      )

      Spacer(modifier = Modifier.height(2.dp))

      Text(secondaryStat, style = MaterialTheme.typography.labelSmall.copy(color = Color.LightGray, fontSize = 8.sp))

      Spacer(modifier = Modifier.height(4.dp))

      Text(
        statusLabel,
        style = MaterialTheme.typography.labelSmall.copy(
          color = if (isAlert) WarningAmber else NominalGreen,
          fontSize = 8.sp,
          fontWeight = FontWeight.SemiBold
        )
      )
    }
  }
}

@Composable
private fun StressorPatternCard(
  pattern: MissionStressorPattern,
  onInitiateDirective: () -> Unit
) {
  val borderCol = when (pattern.severity) {
    AlertSeverity.CRITICAL -> CriticalRed
    AlertSeverity.WARNING -> WarningAmber
    AlertSeverity.ADVISORY -> NeonCyan
  }

  Box(
    modifier = Modifier
      .fillMaxWidth()
      .clip(RoundedCornerShape(12.dp))
      .background(Color.Black.copy(alpha = 0.35f))
      .border(1.dp, borderCol.copy(alpha = 0.4f), RoundedCornerShape(12.dp))
      .padding(12.dp)
  ) {
    Column {
      // Header: Type + Severity Badge
      Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically
      ) {
        Row(verticalAlignment = Alignment.CenterVertically) {
          Box(
            modifier = Modifier
              .clip(RoundedCornerShape(4.dp))
              .background(borderCol.copy(alpha = 0.2f))
              .padding(horizontal = 6.dp, vertical = 2.dp)
          ) {
            Text(
              pattern.stressorType.label.uppercase(),
              style = MaterialTheme.typography.labelSmall.copy(
                color = borderCol,
                fontWeight = FontWeight.Bold,
                fontSize = 7.5.sp
              )
            )
          }

          Spacer(modifier = Modifier.width(6.dp))

          Text(
            "• ${pattern.affectedCrewCount} of ${pattern.totalCrewCount} AFFECTED",
            style = MaterialTheme.typography.labelSmall.copy(color = Color.LightGray, fontSize = 8.sp)
          )
        }

        Text(
          pattern.severity.name,
          style = MaterialTheme.typography.labelSmall.copy(color = borderCol, fontSize = 8.sp, fontWeight = FontWeight.Bold)
        )
      }

      Spacer(modifier = Modifier.height(6.dp))

      // Title
      Text(
        pattern.title,
        style = MaterialTheme.typography.bodyMedium.copy(color = Color.White, fontWeight = FontWeight.SemiBold)
      )

      Spacer(modifier = Modifier.height(4.dp))

      // Evidence & Root Cause
      Text(
        pattern.evidenceSummary,
        style = MaterialTheme.typography.bodySmall.copy(color = Color(0xFFCBD5E1), fontSize = 10.sp)
      )

      Spacer(modifier = Modifier.height(4.dp))

      Text(
        "Correlated Driver: ${pattern.correlationFactor}",
        style = MaterialTheme.typography.labelSmall.copy(color = WarningAmber, fontSize = 9.sp)
      )

      Spacer(modifier = Modifier.height(8.dp))

      // Recommended Fleet Countermeasure Box
      Box(
        modifier = Modifier
          .fillMaxWidth()
          .clip(RoundedCornerShape(8.dp))
          .background(Color.Black.copy(alpha = 0.5f))
          .border(1.dp, borderCol.copy(alpha = 0.3f), RoundedCornerShape(8.dp))
          .padding(8.dp)
      ) {
        Row(verticalAlignment = Alignment.Top) {
          Icon(Icons.Default.Info, contentDescription = null, tint = borderCol, modifier = Modifier.size(13.dp).padding(top = 1.dp))
          Spacer(modifier = Modifier.width(6.dp))
          Column {
            Text(
              "RECOMMENDED FLEET COUNTERMEASURE:",
              style = MaterialTheme.typography.labelSmall.copy(color = borderCol, fontSize = 7.5.sp, fontWeight = FontWeight.Bold)
            )
            Spacer(modifier = Modifier.height(2.dp))
            Text(
              pattern.recommendedFleetCountermeasure,
              style = MaterialTheme.typography.bodySmall.copy(color = Color.White, fontSize = 9.5.sp)
            )
          }
        }
      }
    }
  }
}

@Composable
private fun FleetDirectiveDialog(
  prefilledDirective: String,
  targetAstronautsCount: Int,
  onDismiss: () -> Unit,
  onConfirm: (String) -> Unit
) {
  var directiveText by remember { mutableStateOf(prefilledDirective) }

  Dialog(onDismissRequest = onDismiss) {
    Surface(
      shape = RoundedCornerShape(16.dp),
      color = Color(0xFF0C162E),
      border = androidx.compose.foundation.BorderStroke(1.dp, NeonCyan.copy(alpha = 0.4f)),
      modifier = Modifier
        .fillMaxWidth()
        .padding(8.dp)
    ) {
      Column(modifier = Modifier.padding(16.dp)) {
        Row(
          modifier = Modifier.fillMaxWidth(),
          horizontalArrangement = Arrangement.SpaceBetween,
          verticalAlignment = Alignment.CenterVertically
        ) {
          Row(verticalAlignment = Alignment.CenterVertically) {
            Icon(Icons.AutoMirrored.Filled.Send, contentDescription = null, tint = NeonCyan, modifier = Modifier.size(18.dp))
            Spacer(modifier = Modifier.width(8.dp))
            Text(
              "FLEET MEDICAL DIRECTIVE",
              style = MaterialTheme.typography.titleMedium.copy(color = Color.White, fontWeight = FontWeight.Bold)
            )
          }
        }

        Spacer(modifier = Modifier.height(8.dp))

        Text(
          "Broadcast medical order to all $targetAstronautsCount selected crew consoles simultaneously.",
          style = MaterialTheme.typography.bodySmall.copy(color = Color.LightGray, fontSize = 10.sp)
        )

        Spacer(modifier = Modifier.height(12.dp))

        OutlinedTextField(
          value = directiveText,
          onValueChange = { directiveText = it },
          label = { Text("Directive Order & Countermeasure Protocol", fontSize = 10.sp) },
          modifier = Modifier.fillMaxWidth().testTag("fleet_directive_input"),
          colors = OutlinedTextFieldDefaults.colors(
            focusedBorderColor = NeonCyan,
            unfocusedBorderColor = GlassBorderStroke,
            focusedTextColor = Color.White,
            unfocusedTextColor = Color.White
          ),
          shape = RoundedCornerShape(8.dp)
        )

        Spacer(modifier = Modifier.height(16.dp))

        Row(
          modifier = Modifier.fillMaxWidth(),
          horizontalArrangement = Arrangement.spacedBy(8.dp)
        ) {
          Button(
            onClick = onDismiss,
            modifier = Modifier.weight(1f).height(40.dp),
            colors = ButtonDefaults.buttonColors(containerColor = Color.White.copy(alpha = 0.1f), contentColor = Color.LightGray),
            shape = RoundedCornerShape(8.dp)
          ) {
            Text("CANCEL", style = MaterialTheme.typography.labelSmall)
          }

          Button(
            onClick = {
              if (directiveText.isNotBlank()) {
                onConfirm(directiveText.trim())
              }
            },
            modifier = Modifier.weight(1.5f).height(40.dp).testTag("confirm_fleet_directive_button"),
            colors = ButtonDefaults.buttonColors(containerColor = NeonCyan, contentColor = Color.Black),
            shape = RoundedCornerShape(8.dp)
          ) {
            Text("TRANSMIT DIRECTIVE", style = MaterialTheme.typography.labelSmall.copy(fontWeight = FontWeight.Bold))
          }
        }
      }
    }
  }
}
