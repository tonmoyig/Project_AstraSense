package com.example.ui.components

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.core.LinearEasing
import androidx.compose.animation.core.RepeatMode
import androidx.compose.animation.core.animateFloat
import androidx.compose.animation.core.infiniteRepeatable
import androidx.compose.animation.core.rememberInfiniteTransition
import androidx.compose.animation.core.tween
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.gestures.detectTapGestures
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowForward
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.CrisisAlert
import androidx.compose.material.icons.filled.GraphicEq
import androidx.compose.material.icons.filled.Mic
import androidx.compose.material.icons.filled.Radar
import androidx.compose.material.icons.filled.Shield
import androidx.compose.material.icons.filled.Speed
import androidx.compose.material.icons.filled.VolumeUp
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
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
import androidx.compose.ui.graphics.PathEffect
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.graphics.drawscope.rotate
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.window.Dialog
import com.example.model.AcousticStressScanResult
import com.example.model.AlertSeverity
import com.example.model.CrewMember
import com.example.model.RadarBiosensorTarget
import com.example.service.AcousticStressScanService
import com.example.ui.theme.BrightTeal
import com.example.ui.theme.CriticalRed
import com.example.ui.theme.ElectricIndigo
import com.example.ui.theme.NeonCyan
import com.example.ui.theme.NominalGreen
import com.example.ui.theme.StarlightBlue
import com.example.ui.theme.WarningAmber
import kotlinx.coroutines.delay
import kotlin.math.cos
import kotlin.math.sin

@Composable
fun BiometricSensorRadarCard(
  crewMembers: List<CrewMember>,
  modifier: Modifier = Modifier
) {
  val radarTargets = remember(crewMembers) {
    AcousticStressScanService.generateRadarTargets(crewMembers)
  }

  var selectedTargetId by remember { mutableStateOf<String?>(null) }
  var showFullScanModal by remember { mutableStateOf(false) }

  // Rotating Radar Sweep Angle: 0 to 360 degrees
  val infiniteTransition = rememberInfiniteTransition(label = "RadarSweep")
  val sweepAngle by infiniteTransition.animateFloat(
    initialValue = 0f,
    targetValue = 360f,
    animationSpec = infiniteRepeatable(
      animation = tween(4000, easing = LinearEasing),
      repeatMode = RepeatMode.Restart
    ),
    label = "SweepAngle"
  )

  // Pulsing animation for active blips
  val blipPulse by infiniteTransition.animateFloat(
    initialValue = 1f,
    targetValue = 1.6f,
    animationSpec = infiniteRepeatable(
      animation = tween(1000, easing = LinearEasing),
      repeatMode = RepeatMode.Reverse
    ),
    label = "BlipPulse"
  )

  val lockedTarget = remember(radarTargets, selectedTargetId) {
    radarTargets.find { it.id == selectedTargetId }
  }

  GlassCard(
    modifier = modifier.testTag("biometric_radar_card"),
    cornerRadius = 20.dp
  ) {
    Column {
      // 1. Top Bar: "SYSTEM SCAN" • "LIVE" (matching reference image)
      Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically
      ) {
        Row(verticalAlignment = Alignment.CenterVertically) {
          Icon(
            Icons.Default.Radar,
            contentDescription = null,
            tint = NeonCyan,
            modifier = Modifier.size(16.dp)
          )
          Spacer(modifier = Modifier.width(6.dp))
          Text(
            "SYSTEM SCAN",
            style = MaterialTheme.typography.titleSmall.copy(
              color = Color.White,
              fontWeight = FontWeight.Bold,
              letterSpacing = 1.2.sp
            )
          )
        }

        Row(verticalAlignment = Alignment.CenterVertically) {
          Box(
            modifier = Modifier
              .size(8.dp)
              .clip(CircleShape)
              .background(NominalGreen)
          )
          Spacer(modifier = Modifier.width(6.dp))
          Text(
            "LIVE",
            style = MaterialTheme.typography.labelSmall.copy(
              color = NominalGreen,
              fontWeight = FontWeight.Bold,
              letterSpacing = 1.sp
            )
          )
        }
      }

      Spacer(modifier = Modifier.height(12.dp))

      // 2. Circular Sweeping Radar Canvas
      Box(
        modifier = Modifier
          .fillMaxWidth()
          .height(260.dp),
        contentAlignment = Alignment.Center
      ) {
        Canvas(
          modifier = Modifier
            .size(240.dp)
            .pointerInput(radarTargets) {
              detectTapGestures { offset ->
                val centerX = size.width / 2f
                val centerY = size.height / 2f
                val radius = size.width * 0.45f

                // Find closest blip to tap
                val clickedTarget = radarTargets.minByOrNull { target ->
                  val angleRad = Math.toRadians(target.angleDegrees.toDouble() - 90.0)
                  val blipX = centerX + (radius * target.distanceFraction * cos(angleRad)).toFloat()
                  val blipY = centerY + (radius * target.distanceFraction * sin(angleRad)).toFloat()
                  val dx = offset.x - blipX
                  val dy = offset.y - blipY
                  dx * dx + dy * dy
                }

                if (clickedTarget != null) {
                  selectedTargetId = clickedTarget.id
                }
              }
            }
        ) {
          val centerX = size.width / 2f
          val centerY = size.height / 2f
          val maxRadius = size.width * 0.46f

          // Dark radar background circle
          drawCircle(
            color = Color(0xFF030A18),
            radius = maxRadius,
            center = Offset(centerX, centerY)
          )

          // 3 Concentric Range Rings
          val ringFractions = listOf(0.33f, 0.66f, 1.0f)
          ringFractions.forEachIndexed { idx, frac ->
            drawCircle(
              color = if (idx == 2) NeonCyan.copy(alpha = 0.5f) else Color.White.copy(alpha = 0.12f),
              radius = maxRadius * frac,
              center = Offset(centerX, centerY),
              style = Stroke(
                width = if (idx == 2) 1.5.dp.toPx() else 1.dp.toPx(),
                pathEffect = if (idx == 1) PathEffect.dashPathEffect(floatArrayOf(6f, 6f), 0f) else null
              )
            )
          }

          // Crosshairs (Vertical and Horizontal lines)
          drawLine(
            color = Color.White.copy(alpha = 0.15f),
            start = Offset(centerX - maxRadius, centerY),
            end = Offset(centerX + maxRadius, centerY),
            strokeWidth = 1.dp.toPx()
          )
          drawLine(
            color = Color.White.copy(alpha = 0.15f),
            start = Offset(centerX, centerY - maxRadius),
            end = Offset(centerX, centerY + maxRadius),
            strokeWidth = 1.dp.toPx()
          )

          // Diagonal subtle guideline ticks
          drawLine(
            color = Color.White.copy(alpha = 0.08f),
            start = Offset(centerX - maxRadius * 0.707f, centerY - maxRadius * 0.707f),
            end = Offset(centerX + maxRadius * 0.707f, centerY + maxRadius * 0.707f),
            strokeWidth = 1.dp.toPx()
          )
          drawLine(
            color = Color.White.copy(alpha = 0.08f),
            start = Offset(centerX - maxRadius * 0.707f, centerY + maxRadius * 0.707f),
            end = Offset(centerX + maxRadius * 0.707f, centerY - maxRadius * 0.707f),
            strokeWidth = 1.dp.toPx()
          )

          // Rotating Sweep Beam with Fading Gradient Trail
          rotate(degrees = sweepAngle, pivot = Offset(centerX, centerY)) {
            // Sweep line
            drawLine(
              brush = Brush.horizontalGradient(
                colors = listOf(Color.Transparent, BrightTeal, NeonCyan)
              ),
              start = Offset(centerX, centerY),
              end = Offset(centerX + maxRadius, centerY),
              strokeWidth = 2.dp.toPx()
            )

            // Fading trail cone arc
            drawArc(
              brush = Brush.sweepGradient(
                0.0f to BrightTeal.copy(alpha = 0.22f),
                0.12f to BrightTeal.copy(alpha = 0.04f),
                0.18f to Color.Transparent,
                1.0f to Color.Transparent,
                center = Offset(centerX, centerY)
              ),
              startAngle = -45f,
              sweepAngle = 45f,
              useCenter = true,
              size = androidx.compose.ui.geometry.Size(maxRadius * 2, maxRadius * 2),
              topLeft = Offset(centerX - maxRadius, centerY - maxRadius)
            )
          }

          // Center Habitat Origin Node
          drawCircle(
            color = NeonCyan,
            radius = 3.5.dp.toPx(),
            center = Offset(centerX, centerY)
          )

          // Biosensor Radar Blips
          radarTargets.forEach { target ->
            val angleRad = Math.toRadians(target.angleDegrees.toDouble() - 90.0)
            val blipX = centerX + (maxRadius * target.distanceFraction * cos(angleRad)).toFloat()
            val blipY = centerY + (maxRadius * target.distanceFraction * sin(angleRad)).toFloat()
            val blipCenter = Offset(blipX, blipY)

            val blipColor = when (target.severity) {
              AlertSeverity.CRITICAL -> CriticalRed
              AlertSeverity.WARNING -> WarningAmber
              AlertSeverity.ADVISORY -> NominalGreen
            }

            val isSelected = target.id == selectedTargetId

            // Pulsing outer halo for active targets
            drawCircle(
              color = blipColor.copy(alpha = if (isSelected) 0.45f else 0.22f),
              radius = (if (isSelected) 10.dp.toPx() else 7.dp.toPx()) * blipPulse,
              center = blipCenter
            )

            // Inner solid glowing dot
            drawCircle(
              color = if (isSelected) Color.White else blipColor,
              radius = if (isSelected) 5.dp.toPx() else 4.dp.toPx(),
              center = blipCenter
            )

            // Target reticle if selected
            if (isSelected) {
              drawCircle(
                color = NeonCyan,
                radius = 14.dp.toPx(),
                center = blipCenter,
                style = Stroke(width = 1.5.dp.toPx())
              )
            }
          }
        }
      }

      // 3. Bottom Row: Stats & View Action (Directly matching reference image layout)
      Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically
      ) {
        // Left: Big number + SIGNALS DETECTED
        Column {
          Text(
            text = "${radarTargets.size}",
            style = MaterialTheme.typography.displayMedium.copy(
              color = Color.White,
              fontWeight = FontWeight.Bold,
              fontFamily = FontFamily.Monospace,
              fontSize = 32.sp
            )
          )
          Text(
            text = "SIGNALS DETECTED",
            style = MaterialTheme.typography.labelSmall.copy(
              color = Color.Gray,
              letterSpacing = 1.sp,
              fontSize = 8.5.sp
            )
          )
        }

        // Center / Selected Target Snippet
        lockedTarget?.let { target ->
          Box(
            modifier = Modifier
              .weight(1f)
              .padding(horizontal = 8.dp)
              .clip(RoundedCornerShape(8.dp))
              .background(Color.Black.copy(alpha = 0.5f))
              .border(1.dp, NeonCyan.copy(alpha = 0.4f), RoundedCornerShape(8.dp))
              .padding(horizontal = 8.dp, vertical = 6.dp)
          ) {
            Column {
              Text(
                "LOCKED: ${target.astronautName.split(" ").last()}",
                style = MaterialTheme.typography.labelSmall.copy(color = NeonCyan, fontWeight = FontWeight.Bold, fontSize = 8.sp)
              )
              Text(
                target.primaryMetric,
                style = MaterialTheme.typography.labelSmall.copy(color = Color.White, fontSize = 8.sp)
              )
            }
          }
        } ?: Spacer(modifier = Modifier.weight(1f))

        // Right: "VIEW ↗" Button (matching reference image)
        Button(
          onClick = { showFullScanModal = true },
          colors = ButtonDefaults.buttonColors(containerColor = Color.Black.copy(alpha = 0.6f), contentColor = Color.White),
          border = androidx.compose.foundation.BorderStroke(1.dp, Color.White.copy(alpha = 0.15f)),
          shape = RoundedCornerShape(12.dp),
          modifier = Modifier
            .height(40.dp)
            .testTag("view_radar_scan_button")
        ) {
          Text("VIEW", style = MaterialTheme.typography.labelSmall.copy(fontWeight = FontWeight.Bold, letterSpacing = 1.sp))
          Spacer(modifier = Modifier.width(4.dp))
          Icon(
            Icons.AutoMirrored.Filled.ArrowForward,
            contentDescription = null,
            modifier = Modifier.size(12.dp)
          )
        }
      }
    }
  }

  // Full Screen / Modal Inspection: Biometric Radar Drilldown & Crew Acoustic Stress Scan
  if (showFullScanModal) {
    CrewAcousticStressScanModal(
      crewMembers = crewMembers,
      initialTargetId = selectedTargetId,
      onDismiss = { showFullScanModal = false }
    )
  }
}

@Composable
fun CrewAcousticStressScanModal(
  crewMembers: List<CrewMember>,
  initialTargetId: String?,
  onDismiss: () -> Unit
) {
  var selectedMember by remember(crewMembers, initialTargetId) {
    val initial = crewMembers.find { "RADAR-BLIP-${it.id}" == initialTargetId }
    mutableStateOf(initial ?: crewMembers.first())
  }

  val acousticResult = remember(selectedMember) {
    AcousticStressScanService.analyzeCrewVoiceAcoustics(selectedMember)
  }

  var isSimulatingRecording by remember { mutableStateOf(false) }
  var recordingProgressSeconds by remember { mutableIntStateOf(0) }

  // Waveform animation
  val infiniteTransition = rememberInfiniteTransition(label = "WaveformOscillator")
  val waveOscillator by infiniteTransition.animateFloat(
    initialValue = 0.4f,
    targetValue = 1.0f,
    animationSpec = infiniteRepeatable(
      animation = tween(400, easing = LinearEasing),
      repeatMode = RepeatMode.Reverse
    ),
    label = "WaveOscillator"
  )

  LaunchedEffect(isSimulatingRecording) {
    if (isSimulatingRecording) {
      recordingProgressSeconds = 0
      for (i in 1..10) {
        delay(1000)
        recordingProgressSeconds = i
      }
      isSimulatingRecording = false
    }
  }

  Dialog(onDismissRequest = onDismiss) {
    Surface(
      shape = RoundedCornerShape(20.dp),
      color = Color(0xFF070E1F),
      border = androidx.compose.foundation.BorderStroke(1.dp, NeonCyan.copy(alpha = 0.4f)),
      modifier = Modifier
        .fillMaxWidth()
        .padding(4.dp)
    ) {
      Column(modifier = Modifier.padding(18.dp)) {
        // Modal Header
        Row(
          modifier = Modifier.fillMaxWidth(),
          horizontalArrangement = Arrangement.SpaceBetween,
          verticalAlignment = Alignment.CenterVertically
        ) {
          Row(verticalAlignment = Alignment.CenterVertically) {
            Icon(Icons.Default.GraphicEq, contentDescription = null, tint = BrightTeal, modifier = Modifier.size(20.dp))
            Spacer(modifier = Modifier.width(8.dp))
            Text(
              "CREW ACOUSTIC STRESS SCAN",
              style = MaterialTheme.typography.titleMedium.copy(
                color = Color.White,
                fontWeight = FontWeight.Bold,
                letterSpacing = 1.sp
              )
            )
          }

          IconButton(onClick = onDismiss, modifier = Modifier.size(28.dp)) {
            Icon(Icons.Default.Close, contentDescription = "Close", tint = Color.Gray)
          }
        }

        Spacer(modifier = Modifier.height(4.dp))
        Text(
          "NASA Phonation & Voice Stress Telemetry • Passive Comms Analysis",
          style = MaterialTheme.typography.bodySmall.copy(color = Color(0xFF94A3B8), fontSize = 10.sp)
        )

        Spacer(modifier = Modifier.height(12.dp))

        // Astronaut Selector Chips
        Row(
          modifier = Modifier.fillMaxWidth(),
          horizontalArrangement = Arrangement.spacedBy(6.dp)
        ) {
          crewMembers.forEach { member ->
            val isSelected = member.id == selectedMember.id
            Box(
              modifier = Modifier
                .weight(1f)
                .clip(RoundedCornerShape(8.dp))
                .background(if (isSelected) BrightTeal.copy(alpha = 0.25f) else Color.Black.copy(alpha = 0.35f))
                .border(1.dp, if (isSelected) BrightTeal else Color.White.copy(alpha = 0.08f), RoundedCornerShape(8.dp))
                .clickable { selectedMember = member }
                .padding(vertical = 6.dp),
              contentAlignment = Alignment.Center
            ) {
              Text(
                member.fullName.split(" ").last(),
                style = MaterialTheme.typography.labelSmall.copy(
                  color = if (isSelected) BrightTeal else Color.LightGray,
                  fontSize = 8.5.sp,
                  fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Normal
                )
              )
            }
          }
        }

        Spacer(modifier = Modifier.height(14.dp))

        // Live Audio Waveform Spectrogram
        Box(
          modifier = Modifier
            .fillMaxWidth()
            .height(90.dp)
            .clip(RoundedCornerShape(12.dp))
            .background(Color.Black.copy(alpha = 0.5f))
            .border(1.dp, NeonCyan.copy(alpha = 0.25f), RoundedCornerShape(12.dp))
            .padding(12.dp),
          contentAlignment = Alignment.Center
        ) {
          Row(
            modifier = Modifier.fillMaxSize(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
          ) {
            acousticResult.frequencySpectrogram.forEachIndexed { idx, barValue ->
              val factor = if (isSimulatingRecording) {
                ((sin(idx * 0.7 + waveOscillator * 4.0) + 1.0) * 0.5 * barValue).toFloat().coerceIn(0.1f, 1f)
              } else {
                barValue * waveOscillator
              }

              Box(
                modifier = Modifier
                  .width(6.dp)
                  .fillMaxHeight(factor)
                  .clip(RoundedCornerShape(3.dp))
                  .background(
                    Brush.verticalGradient(
                      colors = listOf(
                        NeonCyan,
                        BrightTeal.copy(alpha = 0.6f)
                      )
                    )
                  )
              )
            }
          }

          if (isSimulatingRecording) {
            Box(
              modifier = Modifier
                .clip(RoundedCornerShape(6.dp))
                .background(CriticalRed.copy(alpha = 0.85f))
                .padding(horizontal = 8.dp, vertical = 2.dp)
            ) {
              Text(
                "REC ${recordingProgressSeconds}s / 10s • ANALYZING",
                style = MaterialTheme.typography.labelSmall.copy(color = Color.White, fontFamily = FontFamily.Monospace, fontSize = 8.5.sp)
              )
            }
          }
        }

        Spacer(modifier = Modifier.height(12.dp))

        // 4 Acoustic Metric Tiles
        Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(8.dp)) {
          AcousticStatTile(
            label = "PITCH JITTER",
            value = "${String.format("%.1f", acousticResult.pitchJitterPercent)}%",
            baseline = "Nominal: < 2.5%",
            isAlert = acousticResult.pitchJitterPercent > 3.0,
            modifier = Modifier.weight(1f)
          )
          AcousticStatTile(
            label = "AMP SHIMMER",
            value = "${String.format("%.1f", acousticResult.amplitudeShimmerPercent)}%",
            baseline = "Nominal: < 3.0%",
            isAlert = acousticResult.amplitudeShimmerPercent > 3.5,
            modifier = Modifier.weight(1f)
          )
        }

        Spacer(modifier = Modifier.height(8.dp))

        Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(8.dp)) {
          AcousticStatTile(
            label = "SPEECH CADENCE",
            value = "${acousticResult.speechCadenceWpm} WPM",
            baseline = "Baseline: 152 WPM",
            isAlert = acousticResult.speechCadenceWpm < 120,
            modifier = Modifier.weight(1f)
          )
          AcousticStatTile(
            label = "VOICE STRESS",
            value = "${acousticResult.compositeStressScore}/100",
            baseline = acousticResult.statusLabel,
            isAlert = acousticResult.compositeStressScore >= 45,
            modifier = Modifier.weight(1f)
          )
        }

        Spacer(modifier = Modifier.height(12.dp))

        // Clinical Phonation Insight
        Box(
          modifier = Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(10.dp))
            .background(Color.Black.copy(alpha = 0.4f))
            .border(1.dp, BrightTeal.copy(alpha = 0.3f), RoundedCornerShape(10.dp))
            .padding(10.dp)
        ) {
          Column {
            Text(
              "CLINICAL PHONATION INSIGHT:",
              style = MaterialTheme.typography.labelSmall.copy(color = BrightTeal, fontWeight = FontWeight.Bold, fontSize = 8.sp)
            )
            Spacer(modifier = Modifier.height(2.dp))
            Text(
              acousticResult.clinicalInsight,
              style = MaterialTheme.typography.bodySmall.copy(color = Color(0xFFE2E8F0), fontSize = 10.sp)
            )
          }
        }

        Spacer(modifier = Modifier.height(14.dp))

        // Voice Comm Check-in Action Button
        Button(
          onClick = { isSimulatingRecording = true },
          enabled = !isSimulatingRecording,
          modifier = Modifier
            .fillMaxWidth()
            .height(44.dp)
            .testTag("run_voice_stress_scan_button"),
          colors = ButtonDefaults.buttonColors(containerColor = NeonCyan, contentColor = Color.Black),
          shape = RoundedCornerShape(10.dp)
        ) {
          Icon(Icons.Default.Mic, contentDescription = null, modifier = Modifier.size(16.dp))
          Spacer(modifier = Modifier.width(6.dp))
          Text(
            if (isSimulatingRecording) "ACQUIRING 10s VOICE AUDIO STREAM..." else "RUN 10s VOICE COMM CHECK-IN SCAN",
            style = MaterialTheme.typography.labelSmall.copy(fontWeight = FontWeight.Bold)
          )
        }
      }
    }
  }
}

@Composable
private fun AcousticStatTile(
  label: String,
  value: String,
  baseline: String,
  isAlert: Boolean,
  modifier: Modifier = Modifier
) {
  Box(
    modifier = modifier
      .clip(RoundedCornerShape(10.dp))
      .background(Color.Black.copy(alpha = 0.4f))
      .border(
        1.dp,
        if (isAlert) WarningAmber.copy(alpha = 0.5f) else Color.White.copy(alpha = 0.08f),
        RoundedCornerShape(10.dp)
      )
      .padding(8.dp)
  ) {
    Column {
      Text(label, style = MaterialTheme.typography.labelSmall.copy(color = Color.Gray, fontSize = 7.5.sp))
      Spacer(modifier = Modifier.height(2.dp))
      Text(
        value,
        style = MaterialTheme.typography.titleMedium.copy(
          color = if (isAlert) WarningAmber else Color.White,
          fontWeight = FontWeight.Bold,
          fontFamily = FontFamily.Monospace,
          fontSize = 14.sp
        )
      )
      Spacer(modifier = Modifier.height(1.dp))
      Text(
        baseline,
        style = MaterialTheme.typography.labelSmall.copy(
          color = if (isAlert) WarningAmber else Color.LightGray,
          fontSize = 7.5.sp
        )
      )
    }
  }
}
