package com.example.ui.components

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.core.LinearEasing
import androidx.compose.animation.core.RepeatMode
import androidx.compose.animation.core.animateFloat
import androidx.compose.animation.core.infiniteRepeatable
import androidx.compose.animation.core.rememberInfiniteTransition
import androidx.compose.animation.core.tween
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
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.CrisisAlert
import androidx.compose.material.icons.filled.Emergency
import androidx.compose.material.icons.filled.ExpandLess
import androidx.compose.material.icons.filled.ExpandMore
import androidx.compose.material.icons.filled.LocalHospital
import androidx.compose.material.icons.filled.Sensors
import androidx.compose.material.icons.filled.Speed
import androidx.compose.material.icons.filled.StopCircle
import androidx.compose.material.icons.filled.Warning
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
import com.example.model.CrewMember
import com.example.model.HighFrequencyTelemetrySample
import com.example.ui.theme.BrightTeal
import com.example.ui.theme.CriticalRed
import com.example.ui.theme.NeonCyan
import com.example.ui.theme.NominalGreen
import com.example.ui.theme.StarlightBlue
import com.example.ui.theme.WarningAmber

/**
 * High-Priority Emergency SOS Card with Automated Medical Officer Broadcast
 * and live high-frequency telemetry stream indicator.
 */
@Composable
fun EmergencySosCard(
  currentUser: CrewMember?,
  onTriggerSos: () -> Unit,
  onDeactivateSos: () -> Unit,
  modifier: Modifier = Modifier
) {
  val isSosActive by AstraRepository.isEmergencySosActive.collectAsState()
  val originator by AstraRepository.emergencySosOriginator.collectAsState()
  val featureConfig by AstraRepository.systemFeatureConfig.collectAsState()
  val hfLogs by AstraRepository.highFrequencyTelemetryLogs.collectAsState()

  var isStreamExpanded by remember { mutableStateOf(false) }

  // Pulsing animation for SOS active state
  val infiniteTransition = rememberInfiniteTransition(label = "sosPulse")
  val pulseAlpha by infiniteTransition.animateFloat(
    initialValue = 0.4f,
    targetValue = 0.95f,
    animationSpec = infiniteRepeatable(tween(600, easing = LinearEasing), RepeatMode.Reverse),
    label = "pulseAlpha"
  )

  GlassCard(
    modifier = modifier.fillMaxWidth().testTag("emergency_sos_card"),
    cornerRadius = 16.dp,
    borderColor = if (isSosActive) CriticalRed.copy(alpha = pulseAlpha) else CriticalRed.copy(alpha = 0.6f),
    backgroundColor = if (isSosActive) CriticalRed.copy(alpha = 0.16f) else Color.Black.copy(alpha = 0.45f)
  ) {
    Column {
      // Header
      Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically
      ) {
        Row(
          verticalAlignment = Alignment.CenterVertically,
          modifier = Modifier.weight(1f).padding(end = 6.dp)
        ) {
          Box(
            modifier = Modifier
              .size(10.dp)
              .clip(CircleShape)
              .background(if (isSosActive) CriticalRed else WarningAmber)
          )
          Spacer(modifier = Modifier.width(8.dp))
          Text(
            if (isSosActive) "EMERGENCY SOS ACTIVE" else "ASTRONAUT EMERGENCY SOS",
            style = MaterialTheme.typography.titleSmall.copy(
              color = if (isSosActive) CriticalRed else Color.White,
              fontWeight = FontWeight.Bold,
              fontSize = 11.sp,
              letterSpacing = 0.5.sp
            ),
            maxLines = 1,
            overflow = TextOverflow.Ellipsis
          )
        }

        StatusBadge(
          text = if (isSosActive) "10.0 Hz BURST" else "0.1 Hz STANDBY",
          color = if (isSosActive) CriticalRed else BrightTeal
        )
      }

      Spacer(modifier = Modifier.height(8.dp))

      if (isSosActive) {
        // Active Distress Information
        Box(
          modifier = Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(8.dp))
            .background(Color.Black.copy(alpha = 0.5f))
            .border(1.dp, CriticalRed.copy(alpha = 0.6f), RoundedCornerShape(8.dp))
            .padding(10.dp)
        ) {
          Column {
            Row(verticalAlignment = Alignment.CenterVertically) {
              Icon(Icons.Default.LocalHospital, contentDescription = null, tint = CriticalRed, modifier = Modifier.size(16.dp))
              Spacer(modifier = Modifier.width(6.dp))
              Text(
                "AUTOMATED BROADCAST DELIVERED TO MEDICAL OFFICER",
                style = MaterialTheme.typography.labelSmall.copy(color = CriticalRed, fontWeight = FontWeight.Bold, fontSize = 9.sp)
              )
            }
            Spacer(modifier = Modifier.height(4.dp))
            Text(
              "Originator: ${originator?.fullName ?: currentUser?.fullName ?: "Crew Specialist"} (${originator?.id ?: currentUser?.id ?: "NASA-AS-701"})",
              style = MaterialTheme.typography.bodySmall.copy(color = Color.White, fontWeight = FontWeight.SemiBold)
            )
            Text(
              "Location: ${originator?.currentModuleLocation ?: currentUser?.currentModuleLocation ?: "Airlock Quest"}",
              style = MaterialTheme.typography.bodySmall.copy(color = Color.LightGray, fontSize = 11.sp)
            )
            Text(
              "Telemetry Sampling: 10.0 Hz (Continuous multi-system vital stream transmitted to Houston Flight Control).",
              style = MaterialTheme.typography.bodySmall.copy(color = BrightTeal, fontSize = 10.sp)
            )
          }
        }

        Spacer(modifier = Modifier.height(10.dp))

        // Actions Row
        Row(
          modifier = Modifier.fillMaxWidth(),
          horizontalArrangement = Arrangement.spacedBy(8.dp)
        ) {
          Button(
            onClick = onDeactivateSos,
            modifier = Modifier.weight(1f).height(42.dp).testTag("stand_down_sos_button"),
            colors = ButtonDefaults.buttonColors(containerColor = NominalGreen, contentColor = Color.Black),
            shape = RoundedCornerShape(10.dp)
          ) {
            Icon(Icons.Default.StopCircle, contentDescription = null, modifier = Modifier.size(16.dp))
            Spacer(modifier = Modifier.width(6.dp))
            Text("STAND DOWN / CANCEL SOS", style = MaterialTheme.typography.labelSmall.copy(fontWeight = FontWeight.Bold))
          }

          Button(
            onClick = { isStreamExpanded = !isStreamExpanded },
            modifier = Modifier.weight(1f).height(42.dp).testTag("view_hf_stream_button"),
            colors = ButtonDefaults.buttonColors(containerColor = StarlightBlue.copy(alpha = 0.3f), contentColor = Color.White),
            border = androidx.compose.foundation.BorderStroke(1.dp, StarlightBlue),
            shape = RoundedCornerShape(10.dp)
          ) {
            Icon(Icons.Default.Sensors, contentDescription = null, tint = NeonCyan, modifier = Modifier.size(16.dp))
            Spacer(modifier = Modifier.width(4.dp))
            Text(
              if (isStreamExpanded) "HIDE 10Hz LOGS" else "VIEW 10Hz STREAM",
              style = MaterialTheme.typography.labelSmall
            )
            Icon(
              imageVector = if (isStreamExpanded) Icons.Default.ExpandLess else Icons.Default.ExpandMore,
              contentDescription = null,
              tint = NeonCyan,
              modifier = Modifier.size(14.dp)
            )
          }
        }

        // Expandable High-Frequency Stream Table
        AnimatedVisibility(visible = isStreamExpanded) {
          Column(modifier = Modifier.padding(top = 10.dp)) {
            Text(
              "HIGH-FREQUENCY TELEMETRY SAMPLES (10.0 Hz REAL-TIME STREAM):",
              style = MaterialTheme.typography.labelSmall.copy(color = NeonCyan, fontWeight = FontWeight.Bold, fontSize = 8.5.sp)
            )
            Spacer(modifier = Modifier.height(6.dp))

            Column(
              modifier = Modifier
                .fillMaxWidth()
                .clip(RoundedCornerShape(8.dp))
                .background(Color(0xFF060D18))
                .border(1.dp, Color.White.copy(alpha = 0.1f), RoundedCornerShape(8.dp))
                .padding(8.dp),
              verticalArrangement = Arrangement.spacedBy(4.dp)
            ) {
              Row(
                modifier = Modifier.fillMaxWidth().padding(bottom = 2.dp),
                horizontalArrangement = Arrangement.SpaceBetween
              ) {
                Text("TIME (UTC.ms)", style = MaterialTheme.typography.labelSmall.copy(color = Color.Gray, fontSize = 8.sp))
                Text("HR", style = MaterialTheme.typography.labelSmall.copy(color = BrightTeal, fontSize = 8.sp))
                Text("SpO₂", style = MaterialTheme.typography.labelSmall.copy(color = NominalGreen, fontSize = 8.sp))
                Text("BP", style = MaterialTheme.typography.labelSmall.copy(color = Color(0xFFBAE6FD), fontSize = 8.sp))
                Text("TEMP", style = MaterialTheme.typography.labelSmall.copy(color = WarningAmber, fontSize = 8.sp))
                Text("CABIN", style = MaterialTheme.typography.labelSmall.copy(color = Color.White, fontSize = 8.sp))
              }

              hfLogs.take(6).forEach { sample ->
                Row(
                  modifier = Modifier.fillMaxWidth(),
                  horizontalArrangement = Arrangement.SpaceBetween,
                  verticalAlignment = Alignment.CenterVertically
                ) {
                  Text(sample.timestamp, style = MaterialTheme.typography.labelSmall.copy(color = Color.LightGray, fontSize = 8.sp))
                  Text("${sample.heartRate} bpm", style = MaterialTheme.typography.labelSmall.copy(color = CriticalRed, fontWeight = FontWeight.Bold, fontSize = 8.5.sp))
                  Text("${sample.spO2}%", style = MaterialTheme.typography.labelSmall.copy(color = if (sample.spO2 < 92) CriticalRed else NominalGreen, fontSize = 8.5.sp))
                  Text("${sample.systolicBp}/${sample.diastolicBp}", style = MaterialTheme.typography.labelSmall.copy(color = Color(0xFFBAE6FD), fontSize = 8.5.sp))
                  Text("${"%.1f".format(sample.coreTemp)}°C", style = MaterialTheme.typography.labelSmall.copy(color = WarningAmber, fontSize = 8.5.sp))
                  Text("${sample.cabinPressureKpa} kPa", style = MaterialTheme.typography.labelSmall.copy(color = Color.White, fontSize = 8.5.sp))
                }
              }
            }
          }
        }
      } else {
        // Standby State
        Text(
          "Pressing Emergency SOS immediately broadcasts an automated priority distress alert to the Flight Surgeon and increases telemetry sampling from 0.1 Hz to 10.0 Hz.",
          style = MaterialTheme.typography.bodySmall.copy(color = Color(0xFFCBD5E1), fontSize = 11.sp, lineHeight = 16.sp)
        )

        Spacer(modifier = Modifier.height(10.dp))

        Button(
          onClick = onTriggerSos,
          modifier = Modifier.fillMaxWidth().height(44.dp).testTag("trigger_emergency_sos_button"),
          colors = ButtonDefaults.buttonColors(containerColor = CriticalRed, contentColor = Color.White),
          shape = RoundedCornerShape(10.dp)
        ) {
          Icon(Icons.Default.Emergency, contentDescription = null, modifier = Modifier.size(16.dp))
          Spacer(modifier = Modifier.width(6.dp))
          Text(
            "TRIGGER EMERGENCY SOS (10Hz STREAM)",
            style = MaterialTheme.typography.titleSmall.copy(fontWeight = FontWeight.Bold, fontSize = 9.5.sp),
            maxLines = 1,
            softWrap = false
          )
        }
      }
    }
  }
}
