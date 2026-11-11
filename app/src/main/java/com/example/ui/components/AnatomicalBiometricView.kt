package com.example.ui.components

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.core.FastOutSlowInEasing
import androidx.compose.animation.core.RepeatMode
import androidx.compose.animation.core.animateFloat
import androidx.compose.animation.core.infiniteRepeatable
import androidx.compose.animation.core.rememberInfiniteTransition
import androidx.compose.animation.core.tween
import androidx.compose.foundation.Canvas
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
import androidx.compose.material.icons.filled.Accessibility
import androidx.compose.material.icons.filled.Favorite
import androidx.compose.material.icons.filled.FitnessCenter
import androidx.compose.material.icons.filled.Psychology
import androidx.compose.material.icons.filled.Shield
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.model.CrewMember
import com.example.model.RiskLevel
import com.example.ui.theme.BrightTeal
import com.example.ui.theme.CriticalRed
import com.example.ui.theme.NeonCyan
import com.example.ui.theme.NominalGreen
import com.example.ui.theme.StarlightBlue
import com.example.ui.theme.WarningAmber

@Composable
fun AnatomicalBiometricView(
  crewMember: CrewMember,
  compositeRiskScore: Int,
  riskLevel: RiskLevel,
  modifier: Modifier = Modifier
) {
  var selectedNodeIndex by remember { mutableIntStateOf(1) } // 0: Cranial, 1: Chest/Cardio, 2: Shoulder/Dosimeter, 3: Spine/Posture, 4: Quads/Bone-Muscle

  val infiniteTransition = rememberInfiniteTransition(label = "pulse")
  val pulseRadius by infiniteTransition.animateFloat(
    initialValue = 4f,
    targetValue = 10f,
    animationSpec = infiniteRepeatable(
      animation = tween(1200, easing = FastOutSlowInEasing),
      repeatMode = RepeatMode.Reverse
    ),
    label = "pulseRadius"
  )

  GlassCard(
    modifier = modifier.testTag("anatomical_biometric_card"),
    cornerRadius = 20.dp
  ) {
    Column {
      // Header: Astronaut Name & Combined Risk Score Banner
      Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically
      ) {
        Column(modifier = Modifier.weight(1f).padding(end = 8.dp)) {
          Text(
            text = "BIOMETRIC TELEMETRY & RISK",
            style = MaterialTheme.typography.labelSmall.copy(color = NeonCyan, letterSpacing = 0.5.sp, fontSize = 9.sp),
            maxLines = 1,
            overflow = TextOverflow.Ellipsis
          )
          Text(
            text = crewMember.fullName,
            style = MaterialTheme.typography.titleMedium.copy(color = Color.White, fontWeight = FontWeight.Bold),
            maxLines = 1,
            overflow = TextOverflow.Ellipsis
          )
        }

        // Combined Risk Score Gauge Capsule
        val riskColor = when (riskLevel) {
          RiskLevel.LOW -> NominalGreen
          RiskLevel.MODERATE -> WarningAmber
          RiskLevel.HIGH -> CriticalRed
        }

        Box(
          modifier = Modifier
            .clip(RoundedCornerShape(8.dp))
            .background(riskColor.copy(alpha = 0.18f))
            .border(1.dp, riskColor.copy(alpha = 0.6f), RoundedCornerShape(8.dp))
            .padding(horizontal = 8.dp, vertical = 5.dp)
        ) {
          Row(verticalAlignment = Alignment.CenterVertically) {
            Box(
              modifier = Modifier
                .size(6.dp)
                .clip(CircleShape)
                .background(riskColor)
            )
            Spacer(modifier = Modifier.width(5.dp))
            Text(
              text = "${riskLevel.label} ($compositeRiskScore/100)",
              style = MaterialTheme.typography.labelSmall.copy(
                color = riskColor,
                fontWeight = FontWeight.Bold,
                fontSize = 9.sp
              ),
              maxLines = 1,
              softWrap = false
            )
          }
        }
      }

      Spacer(modifier = Modifier.height(14.dp))

      // Center layout: Anatomical Wireframe Canvas & Quick Selector
      Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.spacedBy(12.dp),
        verticalAlignment = Alignment.CenterVertically
      ) {
        // Human Silhouette Canvas with Hotspot Sensor Nodes
        Box(
          modifier = Modifier
            .size(130.dp, 190.dp)
            .clip(RoundedCornerShape(12.dp))
            .background(Color(0xFF040813))
            .border(1.dp, Color(0x3338BDF8), RoundedCornerShape(12.dp)),
          contentAlignment = Alignment.Center
        ) {
          Canvas(modifier = Modifier.size(130.dp, 190.dp)) {
            val cx = size.width / 2f
            val topY = 22.dp.toPx()

            // Draw Astronaut Wireframe Silhouette
            val bodyPath = Path().apply {
              // Helmet / Head
              addOval(androidx.compose.ui.geometry.Rect(cx - 14.dp.toPx(), topY, cx + 14.dp.toPx(), topY + 28.dp.toPx()))

              // Neck & Shoulders
              moveTo(cx - 5.dp.toPx(), topY + 28.dp.toPx())
              lineTo(cx - 30.dp.toPx(), topY + 44.dp.toPx()) // Left shoulder
              lineTo(cx - 36.dp.toPx(), topY + 95.dp.toPx()) // Left wrist
              lineTo(cx - 26.dp.toPx(), topY + 95.dp.toPx())
              lineTo(cx - 22.dp.toPx(), topY + 54.dp.toPx()) // Left armpit
              lineTo(cx - 18.dp.toPx(), topY + 95.dp.toPx()) // Torso left
              lineTo(cx - 22.dp.toPx(), topY + 155.dp.toPx()) // Left ankle
              lineTo(cx - 8.dp.toPx(), topY + 155.dp.toPx())
              lineTo(cx - 4.dp.toPx(), topY + 100.dp.toPx()) // Crotch

              // Right leg & right side
              lineTo(cx + 4.dp.toPx(), topY + 100.dp.toPx())
              lineTo(cx + 8.dp.toPx(), topY + 155.dp.toPx())
              lineTo(cx + 22.dp.toPx(), topY + 155.dp.toPx())
              lineTo(cx + 18.dp.toPx(), topY + 95.dp.toPx())
              lineTo(cx + 22.dp.toPx(), topY + 54.dp.toPx())
              lineTo(cx + 26.dp.toPx(), topY + 95.dp.toPx())
              lineTo(cx + 36.dp.toPx(), topY + 95.dp.toPx())
              lineTo(cx + 30.dp.toPx(), topY + 44.dp.toPx())
              lineTo(cx + 5.dp.toPx(), topY + 28.dp.toPx())
              close()
            }

            drawPath(bodyPath, Color(0x3338BDF8), style = Stroke(1.5f))

            // Nodes Coordinates
            val cranialOffset = Offset(cx, topY + 14.dp.toPx())
            val chestOffset = Offset(cx, topY + 56.dp.toPx())
            val shoulderOffset = Offset(cx + 26.dp.toPx(), topY + 48.dp.toPx())
            val spineOffset = Offset(cx, topY + 84.dp.toPx())
            val quadsOffset = Offset(cx - 12.dp.toPx(), topY + 120.dp.toPx())

            val nodePoints = listOf(cranialOffset, chestOffset, shoulderOffset, spineOffset, quadsOffset)

            nodePoints.forEachIndexed { i, offset ->
              val isSelected = i == selectedNodeIndex
              val nodeColor = if (isSelected) NeonCyan else StarlightBlue

              if (isSelected) {
                drawCircle(nodeColor.copy(alpha = 0.35f), radius = pulseRadius + 3f, center = offset)
                drawCircle(nodeColor, radius = 5f, center = offset)
              } else {
                drawCircle(nodeColor.copy(alpha = 0.6f), radius = 3.5f, center = offset)
              }
            }
          }
        }

        // Quick Category Selector Buttons
        Column(
          modifier = Modifier.weight(1f),
          verticalArrangement = Arrangement.spacedBy(6.dp)
        ) {
          val categories = listOf(
            Triple(0, "Cranial / Sleep", Icons.Default.Psychology),
            Triple(1, "Cardio / Vitals", Icons.Default.Favorite),
            Triple(2, "RadWatch Dosimeter", Icons.Default.Shield),
            Triple(3, "Posture / Behavior", Icons.Default.Accessibility),
            Triple(4, "Muscle & Bone", Icons.Default.FitnessCenter)
          )

          categories.forEach { (idx, title, icon) ->
            val isSelected = idx == selectedNodeIndex
            Row(
              modifier = Modifier
                .fillMaxWidth()
                .clip(RoundedCornerShape(8.dp))
                .background(
                  if (isSelected) NeonCyan.copy(alpha = 0.2f)
                  else Color.White.copy(alpha = 0.04f)
                )
                .border(
                  1.dp,
                  if (isSelected) NeonCyan.copy(alpha = 0.5f) else Color.Transparent,
                  RoundedCornerShape(8.dp)
                )
                .clickable { selectedNodeIndex = idx }
                .padding(horizontal = 8.dp, vertical = 6.dp),
              verticalAlignment = Alignment.CenterVertically
            ) {
              Icon(
                imageVector = icon,
                contentDescription = null,
                tint = if (isSelected) NeonCyan else Color.Gray,
                modifier = Modifier.size(16.dp)
              )
              Spacer(modifier = Modifier.width(6.dp))
              Text(
                text = title,
                style = MaterialTheme.typography.labelSmall.copy(
                  color = if (isSelected) Color.White else Color.LightGray
                )
              )
            }
          }
        }
      }

      Spacer(modifier = Modifier.height(14.dp))

      // Detailed Biometric Panel according to selected node
      when (selectedNodeIndex) {
        0 -> {
          // Cranial, Sleep & Circadian
          BiometricDataGrid(
            items = listOf(
              "TOTAL SLEEP" to "${crewMember.sleep.totalSleepHours} hrs",
              "SLEEP QUALITY" to "${crewMember.sleep.sleepQualityScore}%",
              "DEEP / REM" to "${crewMember.sleep.deepSleepPercent}% / ${crewMember.sleep.remSleepPercent}%",
              "CIRCADIAN SHIFT" to "${crewMember.sleep.circadianPhaseOffsetHours}h offset",
              "PVT ALERTNESS" to "${crewMember.lastPvtReactionTimeMs} ms",
              "VISOR REGIMEN" to "480nm Daylight Active"
            ),
            recommendation = crewMember.sleep.blueLightVisorRecommendation
          )
        }
        1 -> {
          // Cardio / Vitals
          BiometricDataGrid(
            items = listOf(
              "HEART RATE" to "${crewMember.vitals.heartRateBpm} BPM",
              "BLOOD PRESSURE" to "${crewMember.vitals.systolicBp}/${crewMember.vitals.diastolicBp} mmHg",
              "BLOOD OXYGEN" to "${crewMember.vitals.spo2Percentage}% SpO₂",
              "CORE TEMP" to "${crewMember.vitals.coreTempCelsius}°C",
              "RESPIRATION" to "${crewMember.vitals.respirationRateBpm} BPM",
              "HRV VARIABILITY" to "${crewMember.vitals.hrvMs} ms"
            ),
            recommendation = if (crewMember.vitals.heartRateBpm > 85)
              "High cardiovascular workload detected post-activity. Maintain electrolyte intake."
            else
              "Cardiovascular stability nominal. Hemodynamics adapted to microgravity."
          )
        }
        2 -> {
          // RadWatch Dosimeter & Radiation
          BiometricDataGrid(
            items = listOf(
              "DAILY DOSE" to "${crewMember.radiation.dailyDoseMsv} mSv/d",
              "MISSION TOTAL" to "${crewMember.radiation.missionCumulativeMsv} mSv",
              "CAREER LIMIT" to "${crewMember.radiation.careerThresholdPercentage}% Used",
              "DOSIMETER NFC" to "CALIBRATED (99.8%)",
              "GCR FLUX" to "0.048 mSv/h",
              "SPE SHIELD" to if (crewMember.radiation.stormShelterRequired) "SHELTER" else "STANDBY"
            ),
            recommendation = if (crewMember.radiation.dailyDoseMsv > 0.8)
              "Radiation dose elevated from recent EVA spacewalk. Restrict further hull transit."
            else
              "Radiation accumulation within NASA STD-3001 Permissible Exposure Limits."
          )
        }
        3 -> {
          // Camera-Based Movement & Behavior Tracking
          BiometricDataGrid(
            items = listOf(
              "POSTURE SCORE" to "${crewMember.behavior.postureScore}/100",
              "TRANSLATION VEL" to "${crewMember.behavior.movementSpeedMps} m/s",
              "FACIAL STRAIN" to "${crewMember.behavior.facialStrainIndex}/100",
              "GROUP ACTIVITIES" to "${crewMember.behavior.socialParticipationRate}%",
              "VOICE STRESS" to "${crewMember.behavior.voiceAcousticStress}/100",
              "SENSORY OVERLOAD" to crewMember.mentalWellbeing.sensoryOverloadStatus
            ),
            recommendation = if (crewMember.behavior.facialStrainIndex > 40)
              "Camera tracking flags elevated ocular saccades and facial strain. Suggest sensory rest."
            else
              "Movement kinematics nominal. Zero-g coordination verified by optical sensors."
          )
        }
        4 -> {
          // Bone and Muscle Health (ARED & Treadmill)
          BiometricDataGrid(
            items = listOf(
              "ARED RESISTANCE" to "${crewMember.boneMuscle.aredDailyForceKn} kN Load",
              "T2 TREADMILL" to "${crewMember.boneMuscle.t2TreadmillMinutes} min",
              "MUSCLE POWER" to "${crewMember.boneMuscle.musclePowerRetentionPercent}% Baseline",
              "JOINT ROM" to "${crewMember.boneMuscle.jointRangeOfMotionPercent}%",
              "ESTIMATED BMD" to "${crewMember.boneMuscle.estimatedBmdRetentionPercent}% Retention",
              "EXERCISE CYCLE" to "6-Day Protocol Nominal"
            ),
            recommendation = if (crewMember.boneMuscle.musclePowerRetentionPercent < 95.0)
              "Slight lower-body peak torque reduction. Increase ARED deadlift intensity by 5%."
            else
              "Bone mineral density and muscular retention on target for long-duration stay."
          )
        }
      }
    }
  }
}

@Composable
private fun BiometricDataGrid(
  items: List<Pair<String, String>>,
  recommendation: String
) {
  Column {
    // 2x3 Grid
    Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(8.dp)) {
      items.take(3).forEach { (label, value) ->
        Box(
          modifier = Modifier
            .weight(1f)
            .clip(RoundedCornerShape(8.dp))
            .background(Color.Black.copy(alpha = 0.35f))
            .border(1.dp, Color.White.copy(alpha = 0.06f), RoundedCornerShape(8.dp))
            .padding(8.dp)
        ) {
          Column {
            Text(label, style = MaterialTheme.typography.labelSmall.copy(color = Color.Gray, fontSize = 8.sp))
            Spacer(modifier = Modifier.height(2.dp))
            Text(value, style = MaterialTheme.typography.titleMedium.copy(color = Color.White))
          }
        }
      }
    }

    Spacer(modifier = Modifier.height(6.dp))

    Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(8.dp)) {
      items.drop(3).take(3).forEach { (label, value) ->
        Box(
          modifier = Modifier
            .weight(1f)
            .clip(RoundedCornerShape(8.dp))
            .background(Color.Black.copy(alpha = 0.35f))
            .border(1.dp, Color.White.copy(alpha = 0.06f), RoundedCornerShape(8.dp))
            .padding(8.dp)
        ) {
          Column {
            Text(label, style = MaterialTheme.typography.labelSmall.copy(color = Color.Gray, fontSize = 8.sp))
            Spacer(modifier = Modifier.height(2.dp))
            Text(value, style = MaterialTheme.typography.titleMedium.copy(color = Color.White))
          }
        }
      }
    }

    Spacer(modifier = Modifier.height(10.dp))

    // Clear Action Directive Box
    Box(
      modifier = Modifier
        .fillMaxWidth()
        .clip(RoundedCornerShape(8.dp))
        .background(NeonCyan.copy(alpha = 0.1f))
        .border(1.dp, NeonCyan.copy(alpha = 0.3f), RoundedCornerShape(8.dp))
        .padding(10.dp)
    ) {
      Row(verticalAlignment = Alignment.CenterVertically) {
        Box(
          modifier = Modifier
            .size(6.dp)
            .clip(CircleShape)
            .background(NeonCyan)
        )
        Spacer(modifier = Modifier.width(8.dp))
        Text(
          text = recommendation,
          style = MaterialTheme.typography.bodyMedium.copy(color = Color(0xFFE2E8F0))
        )
      }
    }
  }
}
