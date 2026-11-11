package com.example.ui.components

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.core.FastOutSlowInEasing
import androidx.compose.animation.core.animateFloatAsState
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
import androidx.compose.material.icons.filled.Bedtime
import androidx.compose.material.icons.filled.Bolt
import androidx.compose.material.icons.filled.ExpandLess
import androidx.compose.material.icons.filled.ExpandMore
import androidx.compose.material.icons.filled.Favorite
import androidx.compose.material.icons.filled.HealthAndSafety
import androidx.compose.material.icons.filled.Info
import androidx.compose.material.icons.filled.Psychology
import androidx.compose.material.icons.filled.Shield
import androidx.compose.material.icons.filled.Speed
import androidx.compose.material.icons.filled.Warning
import androidx.compose.material3.Icon
import androidx.compose.material3.LinearProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.model.RiskLevel
import com.example.service.MissionRiskScore
import com.example.ui.theme.BrightTeal
import com.example.ui.theme.CriticalRed
import com.example.ui.theme.NeonCyan
import com.example.ui.theme.NominalGreen
import com.example.ui.theme.StarlightBlue
import com.example.ui.theme.WarningAmber
import kotlin.math.cos
import kotlin.math.sin

/**
 * High-fidelity aerospace gauge visualization for the aggregated Mission Risk Score.
 * Features a semi-circular curved arc with gradient thresholds (Low/Moderate/High),
 * animated needle pointer, sub-metric breakdown bars (Health, Radiation, Fatigue),
 * and actionable NASA flight surgeon directives.
 */
@Composable
fun MissionRiskGaugeCard(
  missionRisk: MissionRiskScore,
  astronautName: String,
  modifier: Modifier = Modifier
) {
  var showDetails by remember { mutableStateOf(false) }

  val targetProgress = (missionRisk.compositeScore.coerceIn(0, 100) / 100f)
  val animatedProgress by animateFloatAsState(
    targetValue = targetProgress,
    animationSpec = tween(durationMillis = 1000, easing = FastOutSlowInEasing),
    label = "riskProgress"
  )

  val riskColor = when (missionRisk.riskLevel) {
    RiskLevel.LOW -> NominalGreen
    RiskLevel.MODERATE -> WarningAmber
    RiskLevel.HIGH -> CriticalRed
  }

  GlassCard(
    modifier = modifier.testTag("mission_risk_gauge_card"),
    cornerRadius = 20.dp,
    borderColor = riskColor.copy(alpha = 0.5f)
  ) {
    Column(modifier = Modifier.fillMaxWidth()) {
      // Top Header
      Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically
      ) {
        Row(
          verticalAlignment = Alignment.CenterVertically,
          modifier = Modifier.weight(1f).padding(end = 8.dp)
        ) {
          Icon(
            imageVector = Icons.Default.HealthAndSafety,
            contentDescription = null,
            tint = NeonCyan,
            modifier = Modifier.size(18.dp)
          )
          Spacer(modifier = Modifier.width(6.dp))
          Column(modifier = Modifier.weight(1f, fill = false)) {
            Text(
              text = "UNIFIED MISSION RISK SCORE",
              style = MaterialTheme.typography.titleSmall.copy(
                color = NeonCyan,
                letterSpacing = 1.sp,
                fontWeight = FontWeight.Bold,
                fontSize = 11.5.sp
              ),
              maxLines = 1,
              overflow = TextOverflow.Ellipsis
            )
            Text(
              text = "NASA-STD-3001 MULTI-METRIC AGGREGATION",
              style = MaterialTheme.typography.labelSmall.copy(color = BrightTeal, fontSize = 8.sp),
              maxLines = 1,
              overflow = TextOverflow.Ellipsis
            )
          }
        }

        // Operational Readiness Badge
        Box(
          modifier = Modifier
            .clip(RoundedCornerShape(8.dp))
            .background(Color.Black.copy(alpha = 0.45f))
            .border(1.dp, Color.White.copy(alpha = 0.15f), RoundedCornerShape(8.dp))
            .padding(horizontal = 7.dp, vertical = 3.5.dp)
        ) {
          Text(
            text = "READINESS: ${missionRisk.missionReadinessPercentage}%",
            style = MaterialTheme.typography.labelSmall.copy(
              color = Color(0xFFBAE6FD),
              fontWeight = FontWeight.Bold,
              fontSize = 8.sp
            ),
            maxLines = 1,
            softWrap = false
          )
        }
      }

      Spacer(modifier = Modifier.height(6.dp))

      // Center Gauge Canvas
      Box(
        modifier = Modifier
          .fillMaxWidth()
          .height(152.dp),
        contentAlignment = Alignment.Center
      ) {
        MissionRiskArcGauge(
          progress = animatedProgress,
          riskLevel = missionRisk.riskLevel,
          modifier = Modifier
            .size(240.dp, 150.dp)
            .testTag("mission_risk_gauge")
        )

        // Center Digital Display
        Column(
          horizontalAlignment = Alignment.CenterHorizontally,
          modifier = Modifier.padding(top = 18.dp)
        ) {
          Row(verticalAlignment = Alignment.Bottom) {
            Text(
              text = "${missionRisk.compositeScore}",
              style = MaterialTheme.typography.displayMedium.copy(
                color = Color.White,
                fontWeight = FontWeight.Black,
                fontFamily = FontFamily.Monospace,
                fontSize = 38.sp,
                letterSpacing = (-1).sp
              ),
              modifier = Modifier.testTag("mission_risk_score_value")
            )
            Text(
              text = "/100",
              style = MaterialTheme.typography.titleMedium.copy(
                color = Color.Gray,
                fontFamily = FontFamily.Monospace,
                fontWeight = FontWeight.Bold,
                fontSize = 13.sp
              ),
              modifier = Modifier.padding(bottom = 6.dp, start = 2.dp)
            )
          }

          // Risk Level Pill Badge (single RISK label)
          Box(
            modifier = Modifier
              .clip(RoundedCornerShape(10.dp))
              .background(riskColor.copy(alpha = 0.22f))
              .border(1.dp, riskColor.copy(alpha = 0.7f), RoundedCornerShape(10.dp))
              .padding(horizontal = 9.dp, vertical = 3.dp)
              .testTag("mission_risk_level_badge")
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
                text = missionRisk.riskLevel.label,
                style = MaterialTheme.typography.labelSmall.copy(
                  color = riskColor,
                  fontWeight = FontWeight.Bold,
                  fontSize = 9.sp,
                  letterSpacing = 0.8.sp
                ),
                maxLines = 1,
                softWrap = false
              )
            }
          }
        }
      }

      Spacer(modifier = Modifier.height(12.dp))

      // Sub-metric Aggregation Breakdown (Health, Radiation, Fatigue)
      Text(
        text = "AGGREGATED MULTI-SYSTEM TELEMETRY",
        style = MaterialTheme.typography.labelSmall.copy(
          color = Color(0xFF94A3B8),
          fontSize = 9.sp,
          letterSpacing = 0.5.sp
        )
      )

      Spacer(modifier = Modifier.height(6.dp))

      Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.spacedBy(8.dp)
      ) {
        // 1. Health Subscore
        SubScoreItem(
          label = "HEALTH",
          score = missionRisk.healthSubScore,
          icon = Icons.Default.Favorite,
          baseColor = BrightTeal,
          modifier = Modifier
            .weight(1f)
            .testTag("health_subscore_bar")
        )

        // 2. Radiation Subscore
        SubScoreItem(
          label = "RADIATION",
          score = missionRisk.radiationSubScore,
          icon = Icons.Default.Shield,
          baseColor = if (missionRisk.radiationSubScore > 35) WarningAmber else NeonCyan,
          modifier = Modifier
            .weight(1f)
            .testTag("radiation_subscore_bar")
        )

        // 3. Fatigue Subscore
        SubScoreItem(
          label = "FATIGUE",
          score = missionRisk.fatigueSubScore,
          icon = Icons.Default.Bedtime,
          baseColor = if (missionRisk.fatigueSubScore > 35) WarningAmber else StarlightBlue,
          modifier = Modifier
            .weight(1f)
            .testTag("fatigue_subscore_bar")
        )
      }

      Spacer(modifier = Modifier.height(10.dp))

      // Action Directive Banner (NASA Flight Protocol)
      Box(
        modifier = Modifier
          .fillMaxWidth()
          .clip(RoundedCornerShape(10.dp))
          .background(riskColor.copy(alpha = 0.12f))
          .border(1.dp, riskColor.copy(alpha = 0.4f), RoundedCornerShape(10.dp))
          .padding(10.dp)
      ) {
        Column {
          Row(verticalAlignment = Alignment.CenterVertically) {
            Icon(
              imageVector = if (missionRisk.riskLevel == RiskLevel.HIGH) Icons.Default.Warning else Icons.Default.Info,
              contentDescription = null,
              tint = riskColor,
              modifier = Modifier.size(16.dp)
            )
            Spacer(modifier = Modifier.width(6.dp))
            Text(
              text = "FLIGHT SURGEON ACTION PROTOCOL",
              style = MaterialTheme.typography.labelSmall.copy(
                color = riskColor,
                fontWeight = FontWeight.Bold,
                fontSize = 9.sp
              )
            )
          }

          Spacer(modifier = Modifier.height(4.dp))

          Text(
            text = missionRisk.clinicalActionDirective,
            style = MaterialTheme.typography.bodySmall.copy(
              color = Color.White,
              fontWeight = FontWeight.SemiBold,
              lineHeight = 16.sp
            )
          )
        }
      }

      Spacer(modifier = Modifier.height(8.dp))

      // Toggleable Risk Drivers Breakdown
      Row(
        modifier = Modifier
          .fillMaxWidth()
          .clip(RoundedCornerShape(8.dp))
          .clickable { showDetails = !showDetails }
          .padding(vertical = 4.dp),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically
      ) {
        Text(
          text = if (showDetails) "HIDE BIOMETRIC & ENVIRONMENTAL DRIVERS" else "VIEW ${missionRisk.primaryRiskDrivers.size} DETECTED DRIVERS",
          style = MaterialTheme.typography.labelSmall.copy(color = NeonCyan, fontSize = 9.sp)
        )
        Icon(
          imageVector = if (showDetails) Icons.Default.ExpandLess else Icons.Default.ExpandMore,
          contentDescription = null,
          tint = NeonCyan,
          modifier = Modifier.size(16.dp)
        )
      }

      AnimatedVisibility(visible = showDetails) {
        Column(
          modifier = Modifier
            .fillMaxWidth()
            .padding(top = 4.dp),
          verticalArrangement = Arrangement.spacedBy(4.dp)
        ) {
          missionRisk.primaryRiskDrivers.forEach { driver ->
            Row(verticalAlignment = Alignment.CenterVertically) {
              Box(
                modifier = Modifier
                  .size(4.dp)
                  .clip(CircleShape)
                  .background(BrightTeal)
              )
              Spacer(modifier = Modifier.width(6.dp))
              Text(
                text = driver,
                style = MaterialTheme.typography.bodySmall.copy(
                  color = Color(0xFFCBD5E1),
                  fontSize = 11.sp
                )
              )
            }
          }
        }
      }
    }
  }
}

/**
 * Circular arc gauge drawn via Canvas with color gradient zones and needle indicator.
 */
@Composable
fun MissionRiskArcGauge(
  progress: Float,
  riskLevel: RiskLevel,
  modifier: Modifier = Modifier
) {
  Canvas(modifier = modifier) {
    val canvasWidth = size.width
    val canvasHeight = size.height
    val strokeWidth = 9.dp.toPx()

    // Determine circular dial radius to fit inside canvas with calibrated margins
    val maxOuter = minOf(canvasWidth / 2f - 14.dp.toPx(), (canvasHeight - 20.dp.toPx()) / 1.342f)
    val radius = maxOuter - strokeWidth * 0.75f - 8.dp.toPx()
    val centerX = canvasWidth / 2f
    val centerY = 12.dp.toPx() + maxOuter

    val arcRect = androidx.compose.ui.geometry.Rect(
      left = centerX - radius,
      top = centerY - radius,
      right = centerX + radius,
      bottom = centerY + radius
    )

    val startAngle = 160f
    val sweepAngle = 220f

    // 1. Radial Calibration Ticks (Professional Flight Instrument scale)
    val totalTicks = 24
    for (i in 0..totalTicks) {
      val tickProgress = i / totalTicks.toFloat()
      val tickAngle = Math.toRadians((startAngle + tickProgress * sweepAngle).toDouble())
      val isMajor = i % 6 == 0
      val tickLength = if (isMajor) 7.dp.toPx() else 3.5.dp.toPx()
      val tickColor = if (isMajor) {
        when {
          tickProgress >= 0.70f -> CriticalRed.copy(alpha = 0.85f)
          tickProgress >= 0.35f -> WarningAmber.copy(alpha = 0.85f)
          else -> NominalGreen.copy(alpha = 0.85f)
        }
      } else {
        Color.White.copy(alpha = 0.18f)
      }

      val rInner = radius + strokeWidth * 0.75f
      val rOuter = rInner + tickLength

      val startX = centerX + rInner * cos(tickAngle).toFloat()
      val startY = centerY + rInner * sin(tickAngle).toFloat()
      val endX = centerX + rOuter * cos(tickAngle).toFloat()
      val endY = centerY + rOuter * sin(tickAngle).toFloat()

      drawLine(
        color = tickColor,
        start = Offset(startX, startY),
        end = Offset(endX, endY),
        strokeWidth = if (isMajor) 2.dp.toPx() else 1.dp.toPx(),
        cap = StrokeCap.Round
      )
    }

    // Outer subtle boundary guide rail
    drawArc(
      color = Color.White.copy(alpha = 0.12f),
      startAngle = startAngle,
      sweepAngle = sweepAngle,
      useCenter = false,
      topLeft = Offset(centerX - radius - strokeWidth * 0.75f, centerY - radius - strokeWidth * 0.75f),
      size = Size((radius + strokeWidth * 0.75f) * 2, (radius + strokeWidth * 0.75f) * 2),
      style = Stroke(width = 1.dp.toPx())
    )

    // 2. Inactive Background Channel Track
    drawArc(
      color = Color(0xFF071426),
      startAngle = startAngle,
      sweepAngle = sweepAngle,
      useCenter = false,
      topLeft = arcRect.topLeft,
      size = arcRect.size,
      style = Stroke(width = strokeWidth, cap = StrokeCap.Round)
    )

    // Inner subtle guide rail
    drawArc(
      color = Color.White.copy(alpha = 0.06f),
      startAngle = startAngle,
      sweepAngle = sweepAngle,
      useCenter = false,
      topLeft = Offset(centerX - radius + strokeWidth * 0.75f, centerY - radius + strokeWidth * 0.75f),
      size = Size((radius - strokeWidth * 0.75f) * 2, (radius - strokeWidth * 0.75f) * 2),
      style = Stroke(width = 1.dp.toPx())
    )

    // 3. Active Progress Arc with Gradient
    val activeSweep = (sweepAngle * progress.coerceIn(0f, 1f)).coerceAtLeast(2f)

    val activeBrush = when {
      progress >= 0.70f -> Brush.linearGradient(listOf(WarningAmber, CriticalRed))
      progress >= 0.35f -> Brush.linearGradient(listOf(NominalGreen, WarningAmber))
      else -> Brush.linearGradient(listOf(BrightTeal, NominalGreen))
    }

    // Outer bloom underglow pass
    drawArc(
      brush = activeBrush,
      startAngle = startAngle,
      sweepAngle = activeSweep,
      useCenter = false,
      topLeft = arcRect.topLeft,
      size = arcRect.size,
      style = Stroke(width = strokeWidth * 1.6f, cap = StrokeCap.Round),
      alpha = 0.35f
    )

    // Main active progress stroke
    drawArc(
      brush = activeBrush,
      startAngle = startAngle,
      sweepAngle = activeSweep,
      useCenter = false,
      topLeft = arcRect.topLeft,
      size = arcRect.size,
      style = Stroke(width = strokeWidth, cap = StrokeCap.Round)
    )

    // 4. Precision Instrument Indicator Bead on Arc
    val currentAngleRad = Math.toRadians((startAngle + activeSweep).toDouble())
    val needleX = centerX + radius * cos(currentAngleRad).toFloat()
    val needleY = centerY + radius * sin(currentAngleRad).toFloat()

    val indicatorColor = when {
      progress >= 0.70f -> CriticalRed
      progress >= 0.35f -> WarningAmber
      else -> NominalGreen
    }

    // Outer bloom halo
    drawCircle(
      color = indicatorColor.copy(alpha = 0.35f),
      radius = strokeWidth * 1.15f,
      center = Offset(needleX, needleY)
    )
    // Middle ring
    drawCircle(
      color = Color.White.copy(alpha = 0.85f),
      radius = strokeWidth * 0.55f,
      center = Offset(needleX, needleY)
    )
    // Inner core pip
    drawCircle(
      color = Color.White,
      radius = strokeWidth * 0.3f,
      center = Offset(needleX, needleY)
    )
  }
}

@Composable
private fun SubScoreItem(
  label: String,
  score: Int,
  icon: androidx.compose.ui.graphics.vector.ImageVector,
  baseColor: Color,
  modifier: Modifier = Modifier
) {
  Box(
    modifier = modifier
      .clip(RoundedCornerShape(10.dp))
      .background(Color.Black.copy(alpha = 0.35f))
      .border(1.dp, Color.White.copy(alpha = 0.08f), RoundedCornerShape(10.dp))
      .padding(8.dp)
  ) {
    Column {
      Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically
      ) {
        Row(verticalAlignment = Alignment.CenterVertically) {
          Icon(icon, contentDescription = null, tint = baseColor, modifier = Modifier.size(12.dp))
          Spacer(modifier = Modifier.width(4.dp))
          Text(
            text = label,
            style = MaterialTheme.typography.labelSmall.copy(color = Color.LightGray, fontSize = 8.sp)
          )
        }
        Text(
          text = "$score",
          style = MaterialTheme.typography.labelSmall.copy(
            color = baseColor,
            fontWeight = FontWeight.Bold,
            fontSize = 10.sp
          )
        )
      }

      Spacer(modifier = Modifier.height(6.dp))

      LinearProgressIndicator(
        progress = { (score / 100f).coerceIn(0f, 1f) },
        modifier = Modifier
          .fillMaxWidth()
          .height(4.dp)
          .clip(RoundedCornerShape(2.dp)),
        color = baseColor,
        trackColor = Color(0x33334155)
      )
    }
  }
}
