package com.example.ui.components

import androidx.compose.animation.animateColorAsState
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
import androidx.compose.foundation.layout.BoxWithConstraints
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Air
import androidx.compose.material.icons.filled.Favorite
import androidx.compose.material.icons.filled.FitnessCenter
import androidx.compose.material.icons.filled.Medication
import androidx.compose.material.icons.filled.Psychology
import androidx.compose.material.icons.filled.Shield
import androidx.compose.material.icons.filled.Visibility
import androidx.compose.material.icons.filled.WaterDrop
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.model.AstronautHealthExamRecord
import com.example.model.BodySystemRegion
import com.example.model.HealthStatusLevel
import com.example.service.HealthAssessmentService
import com.example.ui.theme.BrightTeal
import com.example.ui.theme.CriticalRed
import com.example.ui.theme.GlassBorderStroke
import com.example.ui.theme.NeonCyan
import com.example.ui.theme.NominalGreen
import com.example.ui.theme.WarningAmber

/**
 * Interactive Anatomical Human Figure Visualization for Astronaut Health Assessment.
 * Displays a futuristic holographic astronaut body silhouette with clickable anatomical nodes
 * representing major organ systems affected by spaceflight hazards.
 */
@Composable
fun AnatomicalHumanFigureView(
  examRecord: AstronautHealthExamRecord,
  selectedRegion: BodySystemRegion,
  onSelectRegion: (BodySystemRegion) -> Unit,
  modifier: Modifier = Modifier
) {
  val infiniteTransition = rememberInfiniteTransition(label = "pulse")
  val pulseScale by infiniteTransition.animateFloat(
    initialValue = 0.85f,
    targetValue = 1.25f,
    animationSpec = infiniteRepeatable(
      animation = tween(1200),
      repeatMode = RepeatMode.Reverse
    ),
    label = "pulseScale"
  )

  Column(modifier = modifier.fillMaxWidth()) {
    // Header & Instructions
    Row(
      modifier = Modifier.fillMaxWidth(),
      horizontalArrangement = Arrangement.SpaceBetween,
      verticalAlignment = Alignment.CenterVertically
    ) {
      Row(verticalAlignment = Alignment.CenterVertically) {
        Icon(Icons.Default.Psychology, contentDescription = null, tint = NeonCyan, modifier = Modifier.size(16.dp))
        Spacer(modifier = Modifier.width(6.dp))
        Text(
          "INTERACTIVE ANATOMICAL BODY SCAN",
          style = MaterialTheme.typography.titleSmall.copy(color = Color.White, fontWeight = FontWeight.Bold, fontSize = 11.5.sp)
        )
      }

      Text(
        "TAP ORGAN NODE TO ASSESS",
        style = MaterialTheme.typography.labelSmall.copy(color = BrightTeal, fontSize = 8.5.sp, fontWeight = FontWeight.SemiBold)
      )
    }

    Spacer(modifier = Modifier.height(6.dp))

    // Interactive Body Canvas & System Chips
    BoxWithConstraints(
      modifier = Modifier
        .fillMaxWidth()
        .height(255.dp)
        .clip(RoundedCornerShape(14.dp))
        .background(Color(0xFF030A16).copy(alpha = 0.85f))
        .border(1.dp, GlassBorderStroke, RoundedCornerShape(14.dp))
        .padding(8.dp)
        .testTag("interactive_anatomical_canvas_box")
    ) {
      val canvasWidth = constraints.maxWidth.toFloat()
      val canvasHeight = constraints.maxHeight.toFloat()
      val centerX = canvasWidth * 0.42f

      // Define anatomical node coordinates mapped proportionally to canvas
      val nodeHead = Offset(centerX, canvasHeight * 0.12f)
      val nodeEyes = Offset(centerX, canvasHeight * 0.18f)
      val nodeLungs = Offset(centerX - 24.dp.value, canvasHeight * 0.32f)
      val nodeHeart = Offset(centerX + 20.dp.value, canvasHeight * 0.35f)
      val nodeMetabolism = Offset(centerX, canvasHeight * 0.47f)
      val nodeKidneys = Offset(centerX - 22.dp.value, canvasHeight * 0.54f)
      val nodeMusculoskeletal = Offset(centerX + 26.dp.value, canvasHeight * 0.68f)
      val nodeSkin = Offset(centerX - 36.dp.value, canvasHeight * 0.72f)

      // Canvas Rendering the Holographic Astronaut Figure
      Canvas(
        modifier = Modifier
          .fillMaxSize()
          .pointerInput(Unit) {
            detectTapGestures { tapOffset ->
              val nodes = listOf(
                BodySystemRegion.BRAIN_NEURO to nodeHead,
                BodySystemRegion.EYES_SANS to nodeEyes,
                BodySystemRegion.LUNGS_ECLSS to nodeLungs,
                BodySystemRegion.CARDIOVASCULAR to nodeHeart,
                BodySystemRegion.METABOLIC_ENDOCRINE to nodeMetabolism,
                BodySystemRegion.RENAL_URINARY to nodeKidneys,
                BodySystemRegion.MUSCULOSKELETAL to nodeMusculoskeletal,
                BodySystemRegion.SKIN_IMMUNE to nodeSkin
              )
              val hit = nodes.minByOrNull { (_, offset) ->
                val dx = tapOffset.x - offset.x
                val dy = tapOffset.y - offset.y
                dx * dx + dy * dy
              }
              if (hit != null) {
                val distSq = (tapOffset.x - hit.second.x) * (tapOffset.x - hit.second.x) + (tapOffset.y - hit.second.y) * (tapOffset.y - hit.second.y)
                if (distSq <= 4000f) {
                  onSelectRegion(hit.first)
                }
              }
            }
          }
      ) {
        val strokeColor = NeonCyan.copy(alpha = 0.28f)
        val gridColor = Color.White.copy(alpha = 0.04f)

        // Draw Holographic Coordinate Grid lines
        for (i in 0..6) {
          val y = canvasHeight * (i / 6f)
          drawLine(gridColor, Offset(0f, y), Offset(canvasWidth, y), strokeWidth = 1f)
        }

        // Draw Anatomical Astronaut Silhouette with Realistic Human Physique
        // 1. Contoured Head, Cranium & Cervical Neck
        val headPath = Path().apply {
          val headTop = canvasHeight * 0.04f
          val chin = canvasHeight * 0.16f
          val neckBottom = canvasHeight * 0.20f
          val craniumR = 18.dp.toPx()
          val jawR = 13.dp.toPx()
          val neckR = 7.dp.toPx()

          moveTo(centerX, headTop)
          cubicTo(centerX - craniumR, headTop, centerX - craniumR, canvasHeight * 0.10f, centerX - jawR, chin)
          lineTo(centerX - neckR, neckBottom)
          lineTo(centerX + neckR, neckBottom)
          lineTo(centerX + jawR, chin)
          cubicTo(centerX + craniumR, canvasHeight * 0.10f, centerX + craniumR, headTop, centerX, headTop)
          close()
        }
        drawPath(
          headPath,
          brush = Brush.radialGradient(
            listOf(NeonCyan.copy(alpha = 0.18f), Color.Transparent),
            center = nodeHead,
            radius = 36.dp.toPx()
          )
        )
        drawPath(headPath, color = strokeColor, style = Stroke(width = 1.8f))

        // 2. Realistic Contoured Torso (Shoulders, Clavicles, Ribcage, Natural Waist, Pelvis)
        val torsoPath = Path().apply {
          val shoulderY = canvasHeight * 0.21f
          val axillaY = canvasHeight * 0.30f
          val ribY = canvasHeight * 0.42f
          val waistY = canvasHeight * 0.50f
          val hipY = canvasHeight * 0.58f
          val perineumY = canvasHeight * 0.62f

          val shoulderW = 34.dp.toPx()
          val axillaW = 28.dp.toPx()
          val ribW = 26.dp.toPx()
          val waistW = 20.dp.toPx()
          val hipW = 24.dp.toPx()
          val groinW = 8.dp.toPx()

          moveTo(centerX - 7.dp.toPx(), canvasHeight * 0.20f)
          quadraticTo(centerX - 20.dp.toPx(), shoulderY - 3.dp.toPx(), centerX - shoulderW, shoulderY)
          cubicTo(centerX - axillaW, axillaY, centerX - ribW, ribY, centerX - waistW, waistY)
          cubicTo(centerX - hipW, hipY - 4.dp.toPx(), centerX - hipW, hipY, centerX - groinW, perineumY)
          lineTo(centerX + groinW, perineumY)
          cubicTo(centerX + hipW, hipY, centerX + hipW, hipY - 4.dp.toPx(), centerX + waistW, waistY)
          cubicTo(centerX + ribW, ribY, centerX + axillaW, axillaY, centerX + shoulderW, shoulderY)
          quadraticTo(centerX + 20.dp.toPx(), shoulderY - 3.dp.toPx(), centerX + 7.dp.toPx(), canvasHeight * 0.20f)
          close()
        }
        drawPath(
          torsoPath,
          brush = Brush.verticalGradient(
            listOf(NeonCyan.copy(alpha = 0.12f), BrightTeal.copy(alpha = 0.04f))
          )
        )
        drawPath(torsoPath, color = strokeColor, style = Stroke(width = 1.8f))

        // 3. Contoured Realistic Arms (Deltoid, Bicep, Forearm, Wrist)
        val leftArmPath = Path().apply {
          moveTo(centerX - 34.dp.toPx(), canvasHeight * 0.21f)
          cubicTo(centerX - 44.dp.toPx(), canvasHeight * 0.28f, centerX - 50.dp.toPx(), canvasHeight * 0.38f, centerX - 54.dp.toPx(), canvasHeight * 0.46f)
          cubicTo(centerX - 56.dp.toPx(), canvasHeight * 0.54f, centerX - 60.dp.toPx(), canvasHeight * 0.62f, centerX - 62.dp.toPx(), canvasHeight * 0.68f)
        }
        drawPath(leftArmPath, color = strokeColor, style = Stroke(width = 3.2f, cap = androidx.compose.ui.graphics.StrokeCap.Round))

        val rightArmPath = Path().apply {
          moveTo(centerX + 34.dp.toPx(), canvasHeight * 0.21f)
          cubicTo(centerX + 44.dp.toPx(), canvasHeight * 0.28f, centerX + 50.dp.toPx(), canvasHeight * 0.38f, centerX + 54.dp.toPx(), canvasHeight * 0.46f)
          cubicTo(centerX + 56.dp.toPx(), canvasHeight * 0.54f, centerX + 60.dp.toPx(), canvasHeight * 0.62f, centerX + 62.dp.toPx(), canvasHeight * 0.68f)
        }
        drawPath(rightArmPath, color = strokeColor, style = Stroke(width = 3.2f, cap = androidx.compose.ui.graphics.StrokeCap.Round))

        // 4. Contoured Realistic Legs (Quadriceps, Knees, Calves, Ankles)
        val leftLegPath = Path().apply {
          moveTo(centerX - 16.dp.toPx(), canvasHeight * 0.61f)
          cubicTo(centerX - 20.dp.toPx(), canvasHeight * 0.70f, centerX - 22.dp.toPx(), canvasHeight * 0.76f, centerX - 20.dp.toPx(), canvasHeight * 0.80f)
          cubicTo(centerX - 18.dp.toPx(), canvasHeight * 0.86f, centerX - 22.dp.toPx(), canvasHeight * 0.92f, centerX - 22.dp.toPx(), canvasHeight * 0.96f)
        }
        drawPath(leftLegPath, color = strokeColor, style = Stroke(width = 3.8f, cap = androidx.compose.ui.graphics.StrokeCap.Round))

        val rightLegPath = Path().apply {
          moveTo(centerX + 16.dp.toPx(), canvasHeight * 0.61f)
          cubicTo(centerX + 20.dp.toPx(), canvasHeight * 0.70f, centerX + 22.dp.toPx(), canvasHeight * 0.76f, centerX + 22.dp.toPx(), canvasHeight * 0.80f)
          cubicTo(centerX + 18.dp.toPx(), canvasHeight * 0.86f, centerX + 22.dp.toPx(), canvasHeight * 0.92f, centerX + 22.dp.toPx(), canvasHeight * 0.96f)
        }
        drawPath(rightLegPath, color = strokeColor, style = Stroke(width = 3.8f, cap = androidx.compose.ui.graphics.StrokeCap.Round))

        // Spine Alignment Beam (Microgravity Elongation Line)
        drawLine(
          brush = Brush.verticalGradient(listOf(NeonCyan.copy(alpha = 0.6f), BrightTeal.copy(alpha = 0.2f))),
          start = Offset(centerX, canvasHeight * 0.24f),
          end = Offset(centerX, canvasHeight * 0.60f),
          strokeWidth = 2f
        )

        // Draw Interactive Organ Nodes with Glow and Status Rings
        val nodesWithStatus = listOf(
          Triple(BodySystemRegion.BRAIN_NEURO, nodeHead, HealthAssessmentService.getGuidanceForSystem(BodySystemRegion.BRAIN_NEURO, examRecord).status),
          Triple(BodySystemRegion.EYES_SANS, nodeEyes, HealthAssessmentService.getGuidanceForSystem(BodySystemRegion.EYES_SANS, examRecord).status),
          Triple(BodySystemRegion.LUNGS_ECLSS, nodeLungs, HealthAssessmentService.getGuidanceForSystem(BodySystemRegion.LUNGS_ECLSS, examRecord).status),
          Triple(BodySystemRegion.CARDIOVASCULAR, nodeHeart, HealthAssessmentService.getGuidanceForSystem(BodySystemRegion.CARDIOVASCULAR, examRecord).status),
          Triple(BodySystemRegion.METABOLIC_ENDOCRINE, nodeMetabolism, HealthAssessmentService.getGuidanceForSystem(BodySystemRegion.METABOLIC_ENDOCRINE, examRecord).status),
          Triple(BodySystemRegion.RENAL_URINARY, nodeKidneys, HealthAssessmentService.getGuidanceForSystem(BodySystemRegion.RENAL_URINARY, examRecord).status),
          Triple(BodySystemRegion.MUSCULOSKELETAL, nodeMusculoskeletal, HealthAssessmentService.getGuidanceForSystem(BodySystemRegion.MUSCULOSKELETAL, examRecord).status),
          Triple(BodySystemRegion.SKIN_IMMUNE, nodeSkin, HealthAssessmentService.getGuidanceForSystem(BodySystemRegion.SKIN_IMMUNE, examRecord).status)
        )

        nodesWithStatus.forEach { (region, center, status) ->
          val isSelected = region == selectedRegion
          val statusColor = when (status) {
            HealthStatusLevel.OPTIMAL, HealthStatusLevel.NOMINAL -> NominalGreen
            HealthStatusLevel.ELEVATED_WATCH -> WarningAmber
            HealthStatusLevel.CRITICAL_ACTION -> CriticalRed
          }

          // Connecting Leader Line to labels on the right
          if (isSelected) {
            drawLine(
              color = NeonCyan.copy(alpha = 0.7f),
              start = center,
              end = Offset(canvasWidth * 0.74f, center.y),
              strokeWidth = 1.5f
            )
            drawCircle(
              color = NeonCyan.copy(alpha = 0.35f),
              radius = 18f * pulseScale,
              center = center
            )
          }

          // Outer halo
          drawCircle(
            color = statusColor.copy(alpha = if (isSelected) 0.5f else 0.25f),
            radius = if (isSelected) 12f else 8f,
            center = center
          )

          // Inner solid core
          drawCircle(
            color = if (isSelected) Color.White else statusColor,
            radius = if (isSelected) 6f else 4.5f,
            center = center
          )
        }
      }

      // Right-Hand Interactive Node Selection Rail
      Column(
        modifier = Modifier
          .align(Alignment.CenterEnd)
          .width(135.dp)
          .fillMaxSize()
          .padding(vertical = 4.dp),
        verticalArrangement = Arrangement.SpaceBetween
      ) {
        BodySystemRegion.entries.take(5).forEach { region ->
          val isSelected = region == selectedRegion
          val guidance = HealthAssessmentService.getGuidanceForSystem(region, examRecord)
          val badgeColor = when (guidance.status) {
            HealthStatusLevel.OPTIMAL, HealthStatusLevel.NOMINAL -> NominalGreen
            HealthStatusLevel.ELEVATED_WATCH -> WarningAmber
            HealthStatusLevel.CRITICAL_ACTION -> CriticalRed
          }

          Row(
            modifier = Modifier
              .fillMaxWidth()
              .clip(RoundedCornerShape(6.dp))
              .background(if (isSelected) NeonCyan.copy(alpha = 0.22f) else Color.Black.copy(alpha = 0.4f))
              .border(1.dp, if (isSelected) NeonCyan else Color.White.copy(alpha = 0.08f), RoundedCornerShape(6.dp))
              .clickable { onSelectRegion(region) }
              .padding(horizontal = 6.dp, vertical = 3.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.SpaceBetween
          ) {
            Row(verticalAlignment = Alignment.CenterVertically, modifier = Modifier.weight(1f)) {
              Box(
                modifier = Modifier
                  .size(6.dp)
                  .clip(CircleShape)
                  .background(badgeColor)
              )
              Spacer(modifier = Modifier.width(5.dp))
              Text(
                region.displayName.split(" ").first(),
                style = MaterialTheme.typography.labelSmall.copy(
                  color = if (isSelected) Color.White else Color.LightGray,
                  fontSize = 8.5.sp,
                  fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Normal
                ),
                maxLines = 1
              )
            }
          }
        }
      }
    }
  }
}
