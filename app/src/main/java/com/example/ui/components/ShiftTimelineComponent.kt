package com.example.ui.components

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.core.FastOutSlowInEasing
import androidx.compose.animation.core.RepeatMode
import androidx.compose.animation.core.animateFloat
import androidx.compose.animation.core.infiniteRepeatable
import androidx.compose.animation.core.rememberInfiniteTransition
import androidx.compose.animation.core.tween
import androidx.compose.animation.expandVertically
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.shrinkVertically
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.IntrinsicSize
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.DirectionsRun
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Bedtime
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.CrisisAlert
import androidx.compose.material.icons.filled.Favorite
import androidx.compose.material.icons.filled.FilterList
import androidx.compose.material.icons.filled.Info
import androidx.compose.material.icons.filled.Medication
import androidx.compose.material.icons.filled.Psychology
import androidx.compose.material.icons.filled.Schedule
import androidx.compose.material.icons.filled.Shield
import androidx.compose.material.icons.filled.Warning
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.OutlinedTextFieldDefaults
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableDoubleStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.window.Dialog
import com.example.model.AlertSeverity
import com.example.model.ShiftEventCategory
import com.example.model.ShiftTimelineEvent
import com.example.ui.theme.BrightTeal
import com.example.ui.theme.CelestialAmber
import com.example.ui.theme.CriticalRed
import com.example.ui.theme.ElectricIndigo
import com.example.ui.theme.GlassBorderStroke
import com.example.ui.theme.NeonCyan
import com.example.ui.theme.NominalGreen
import com.example.ui.theme.StarlightBlue
import com.example.ui.theme.WarningAmber
import java.util.UUID

@Composable
fun ShiftTimelineComponent(
  events: List<ShiftTimelineEvent>,
  astronautName: String,
  astronautId: String,
  onLogNewEvent: (ShiftTimelineEvent) -> Unit,
  modifier: Modifier = Modifier
) {
  var selectedCategoryFilter by remember { mutableStateOf<ShiftEventCategory?>(null) }
  var showLogEventDialog by remember { mutableStateOf(false) }
  var expandedEventId by remember { mutableStateOf<String?>(null) }

  val filteredEvents = remember(events, selectedCategoryFilter) {
    if (selectedCategoryFilter == null) events else events.filter { it.category == selectedCategoryFilter }
  }

  val totalBiometricCount = events.count { it.category == ShiftEventCategory.BIOMETRIC }
  val totalMedicationCount = events.count { it.category == ShiftEventCategory.MEDICATION }
  val totalEmergencyCount = events.count { it.category == ShiftEventCategory.EMERGENCY }

  // Pulsing animation for critical and active events
  val infiniteTransition = rememberInfiniteTransition(label = "TimelinePulse")
  val pulseScale by infiniteTransition.animateFloat(
    initialValue = 0.85f,
    targetValue = 1.25f,
    animationSpec = infiniteRepeatable(
      animation = tween(1200, easing = FastOutSlowInEasing),
      repeatMode = RepeatMode.Reverse
    ),
    label = "PulseScale"
  )

  GlassCard(
    modifier = modifier.testTag("shift_timeline_card"),
    cornerRadius = 20.dp
  ) {
    Column {
      // Header: Title & Shift Metadata
      Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically
      ) {
        Column(modifier = Modifier.weight(1f).padding(end = 8.dp)) {
          Row(verticalAlignment = Alignment.CenterVertically) {
            Icon(Icons.Default.Schedule, contentDescription = null, tint = NeonCyan, modifier = Modifier.size(17.dp))
            Spacer(modifier = Modifier.width(6.dp))
            Text(
              "24-HOUR SHIFT TIMELINE",
              style = MaterialTheme.typography.titleMedium.copy(
                color = Color.White,
                fontWeight = FontWeight.Bold,
                letterSpacing = 0.8.sp,
                fontSize = 13.sp
              ),
              maxLines = 1,
              overflow = TextOverflow.Ellipsis
            )
          }
          Spacer(modifier = Modifier.height(2.dp))
          Text(
            "Biometrics • Medications • Emergencies for $astronautName",
            style = MaterialTheme.typography.bodySmall.copy(color = Color(0xFF94A3B8), fontSize = 9.sp),
            maxLines = 1,
            overflow = TextOverflow.Ellipsis
          )
        }

        Button(
          onClick = { showLogEventDialog = true },
          modifier = Modifier
            .height(36.dp)
            .testTag("log_shift_event_button"),
          colors = ButtonDefaults.buttonColors(containerColor = NeonCyan.copy(alpha = 0.2f), contentColor = NeonCyan),
          border = androidx.compose.foundation.BorderStroke(1.dp, NeonCyan),
          shape = RoundedCornerShape(8.dp)
        ) {
          Icon(Icons.Default.Add, contentDescription = null, modifier = Modifier.size(14.dp))
          Spacer(modifier = Modifier.width(4.dp))
          Text("LOG EVENT", style = MaterialTheme.typography.labelSmall.copy(fontWeight = FontWeight.Bold))
        }
      }

      Spacer(modifier = Modifier.height(14.dp))

      // 24-Hour Shift Progress Track
      Box(
        modifier = Modifier
          .fillMaxWidth()
          .clip(RoundedCornerShape(10.dp))
          .background(Color.Black.copy(alpha = 0.45f))
          .border(1.dp, Color.White.copy(alpha = 0.08f), RoundedCornerShape(10.dp))
          .padding(horizontal = 12.dp, vertical = 8.dp)
      ) {
        Column {
          Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
          ) {
            Text(
              "MISSION SHIFT ALPHA • T+00:00 → T+24:00",
              style = MaterialTheme.typography.labelSmall.copy(
                color = BrightTeal,
                fontFamily = FontFamily.Monospace,
                fontSize = 9.sp
              )
            )
            Text(
              "CURRENT: T+19:30 UTC",
              style = MaterialTheme.typography.labelSmall.copy(
                color = NeonCyan,
                fontFamily = FontFamily.Monospace,
                fontWeight = FontWeight.Bold,
                fontSize = 9.sp
              )
            )
          }

          Spacer(modifier = Modifier.height(6.dp))

          // 24h Gauge Track
          Box(
            modifier = Modifier
              .fillMaxWidth()
              .height(8.dp)
              .clip(RoundedCornerShape(4.dp))
              .background(Color.White.copy(alpha = 0.08f))
          ) {
            // Elapsed progress (19.5h / 24h = 81.25%)
            Box(
              modifier = Modifier
                .fillMaxWidth(0.8125f)
                .fillMaxHeight()
                .clip(RoundedCornerShape(4.dp))
                .background(
                  Brush.horizontalGradient(
                    colors = listOf(BrightTeal, NeonCyan, StarlightBlue)
                  )
                )
            )
          }

          Spacer(modifier = Modifier.height(6.dp))

          // Hour milestone ticks
          Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween
          ) {
            Text("T+00h (Sleep)", style = MaterialTheme.typography.labelSmall.copy(color = Color.Gray, fontSize = 8.sp))
            Text("T+06h (EVA)", style = MaterialTheme.typography.labelSmall.copy(color = Color.Gray, fontSize = 8.sp))
            Text("T+12h (Mid-Shift)", style = MaterialTheme.typography.labelSmall.copy(color = Color.Gray, fontSize = 8.sp))
            Text("T+18h (Exercise)", style = MaterialTheme.typography.labelSmall.copy(color = Color.Gray, fontSize = 8.sp))
            Text("T+24h (Rest)", style = MaterialTheme.typography.labelSmall.copy(color = Color.Gray, fontSize = 8.sp))
          }
        }
      }

      Spacer(modifier = Modifier.height(12.dp))

      // Category Filter Pills
      Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.spacedBy(6.dp)
      ) {
        FilterPill(
          label = "ALL",
          count = events.size,
          isSelected = selectedCategoryFilter == null,
          accentColor = NeonCyan,
          onClick = { selectedCategoryFilter = null },
          modifier = Modifier.weight(1f)
        )
        FilterPill(
          label = "BIOMETRIC",
          count = totalBiometricCount,
          isSelected = selectedCategoryFilter == ShiftEventCategory.BIOMETRIC,
          accentColor = BrightTeal,
          onClick = { selectedCategoryFilter = ShiftEventCategory.BIOMETRIC },
          modifier = Modifier.weight(1.1f)
        )
        FilterPill(
          label = "MEDICATION",
          count = totalMedicationCount,
          isSelected = selectedCategoryFilter == ShiftEventCategory.MEDICATION,
          accentColor = ElectricIndigo,
          onClick = { selectedCategoryFilter = ShiftEventCategory.MEDICATION },
          modifier = Modifier.weight(1.2f)
        )
        FilterPill(
          label = "EMERGENCY",
          count = totalEmergencyCount,
          isSelected = selectedCategoryFilter == ShiftEventCategory.EMERGENCY,
          accentColor = CriticalRed,
          onClick = { selectedCategoryFilter = ShiftEventCategory.EMERGENCY },
          modifier = Modifier.weight(1.2f)
        )
      }

      Spacer(modifier = Modifier.height(14.dp))

      // Vertical Timeline Nodes
      if (filteredEvents.isEmpty()) {
        Box(
          modifier = Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(10.dp))
            .background(Color.Black.copy(alpha = 0.3f))
            .padding(20.dp),
          contentAlignment = Alignment.Center
        ) {
          Text("No events recorded for this filter category.", color = Color.Gray, style = MaterialTheme.typography.bodyMedium)
        }
      } else {
        Column(modifier = Modifier.fillMaxWidth()) {
          filteredEvents.forEachIndexed { index, event ->
            val isFirst = index == 0
            val isLast = index == filteredEvents.size - 1
            val isExpanded = expandedEventId == event.id

            TimelineNodeRow(
              event = event,
              isFirst = isFirst,
              isLast = isLast,
              isExpanded = isExpanded,
              pulseScale = pulseScale,
              onToggleExpand = {
                expandedEventId = if (isExpanded) null else event.id
              }
            )
          }
        }
      }
    }
  }

  // Dialog to Log New Shift Event
  if (showLogEventDialog) {
    LogShiftEventDialog(
      astronautId = astronautId,
      astronautName = astronautName,
      onDismiss = { showLogEventDialog = false },
      onConfirm = { newEvent ->
        onLogNewEvent(newEvent)
        showLogEventDialog = false
      }
    )
  }
}

@Composable
private fun TimelineNodeRow(
  event: ShiftTimelineEvent,
  isFirst: Boolean,
  isLast: Boolean,
  isExpanded: Boolean,
  pulseScale: Float,
  onToggleExpand: () -> Unit
) {
  val nodeColor = when (event.category) {
    ShiftEventCategory.EMERGENCY -> CriticalRed
    ShiftEventCategory.MEDICATION -> ElectricIndigo
    ShiftEventCategory.BIOMETRIC -> when (event.severity) {
      AlertSeverity.CRITICAL -> CriticalRed
      AlertSeverity.WARNING -> WarningAmber
      AlertSeverity.ADVISORY -> BrightTeal
    }
  }

  val iconVector = when (event.iconType) {
    "pill" -> Icons.Default.Medication
    "alert" -> Icons.Default.CrisisAlert
    "radiation" -> Icons.Default.Shield
    "sleep" -> Icons.Default.Bedtime
    "exercise" -> Icons.AutoMirrored.Filled.DirectionsRun
    "brain" -> Icons.Default.Psychology
    else -> Icons.Default.Favorite
  }

  Row(
    modifier = Modifier
      .fillMaxWidth()
      .height(IntrinsicSize.Min)
  ) {
    // Left Track: Timestamp & Vertical Timeline Axis with Connected Line
    Column(
      modifier = Modifier
        .width(76.dp)
        .fillMaxHeight(),
      horizontalAlignment = Alignment.CenterHorizontally
    ) {
      // Time label
      Text(
        text = event.shiftTimeLabel,
        style = MaterialTheme.typography.labelSmall.copy(
          color = Color.White,
          fontFamily = FontFamily.Monospace,
          fontWeight = FontWeight.Bold,
          fontSize = 11.sp
        )
      )
      Text(
        text = event.utcTimestamp.replace(" UTC", ""),
        style = MaterialTheme.typography.labelSmall.copy(
          color = Color.Gray,
          fontFamily = FontFamily.Monospace,
          fontSize = 8.sp
        )
      )

      Spacer(modifier = Modifier.height(4.dp))

      // Vertical Line & Node Canvas
      Box(
        modifier = Modifier
          .fillMaxWidth()
          .weight(1f),
        contentAlignment = Alignment.TopCenter
      ) {
        // Timeline connector line
        Canvas(modifier = Modifier.fillMaxHeight().width(2.dp)) {
          val startY = 0f
          val endY = size.height
          drawLine(
            brush = Brush.verticalGradient(
              colors = listOf(
                nodeColor.copy(alpha = 0.8f),
                nodeColor.copy(alpha = 0.3f)
              )
            ),
            start = Offset(size.width / 2, startY),
            end = Offset(size.width / 2, endY),
            strokeWidth = 2.dp.toPx()
          )
        }

        // Glowing Node Pin
        Box(
          modifier = Modifier
            .padding(top = 2.dp)
            .size(26.dp)
            .clip(CircleShape)
            .background(nodeColor.copy(alpha = 0.2f))
            .border(1.5.dp, nodeColor, CircleShape),
          contentAlignment = Alignment.Center
        ) {
          if (event.severity == AlertSeverity.CRITICAL) {
            // Pulsing emergency halo
            Box(
              modifier = Modifier
                .size(24.dp * pulseScale)
                .clip(CircleShape)
                .background(CriticalRed.copy(alpha = 0.15f))
            )
          }

          Icon(
            imageVector = iconVector,
            contentDescription = null,
            tint = nodeColor,
            modifier = Modifier.size(13.dp)
          )
        }
      }
    }

    Spacer(modifier = Modifier.width(6.dp))

    // Right Content Card
    Box(
      modifier = Modifier
        .weight(1f)
        .padding(bottom = 12.dp)
        .clip(RoundedCornerShape(12.dp))
        .background(Color.Black.copy(alpha = 0.35f))
        .border(1.dp, nodeColor.copy(alpha = if (isExpanded) 0.6f else 0.25f), RoundedCornerShape(12.dp))
        .clickable { onToggleExpand() }
        .padding(10.dp)
        .testTag("timeline_event_${event.id}")
    ) {
      Column {
        // Tag Header
        Row(
          modifier = Modifier.fillMaxWidth(),
          horizontalArrangement = Arrangement.SpaceBetween,
          verticalAlignment = Alignment.CenterVertically
        ) {
          Row(verticalAlignment = Alignment.CenterVertically) {
            Box(
              modifier = Modifier
                .clip(RoundedCornerShape(4.dp))
                .background(nodeColor.copy(alpha = 0.2f))
                .padding(horizontal = 6.dp, vertical = 2.dp)
            ) {
              Text(
                text = event.category.displayName.uppercase(),
                style = MaterialTheme.typography.labelSmall.copy(
                  color = nodeColor,
                  fontWeight = FontWeight.Bold,
                  fontSize = 8.sp,
                  letterSpacing = 0.5.sp
                )
              )
            }

            Spacer(modifier = Modifier.width(6.dp))

            Text(
              text = "• ${event.status}",
              style = MaterialTheme.typography.labelSmall.copy(
                color = when (event.status) {
                  "ADMINISTERED", "RESOLVED", "VERIFIED" -> NominalGreen
                  "MONITORING" -> WarningAmber
                  else -> NeonCyan
                },
                fontSize = 8.sp
              )
            )
          }

          StatusSeverityDot(event.severity)
        }

        Spacer(modifier = Modifier.height(4.dp))

        // Title
        Text(
          text = event.title,
          style = MaterialTheme.typography.bodyMedium.copy(
            color = Color.White,
            fontWeight = FontWeight.SemiBold
          )
        )

        Spacer(modifier = Modifier.height(6.dp))

        // Primary & Secondary Metric Chips
        if (event.primaryMetric != null || event.secondaryMetric != null) {
          Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(6.dp)
          ) {
            event.primaryMetric?.let { metric ->
              Box(
                modifier = Modifier
                  .clip(RoundedCornerShape(6.dp))
                  .background(Color.White.copy(alpha = 0.06f))
                  .border(1.dp, nodeColor.copy(alpha = 0.3f), RoundedCornerShape(6.dp))
                  .padding(horizontal = 8.dp, vertical = 4.dp)
              ) {
                Text(
                  text = metric,
                  style = MaterialTheme.typography.labelSmall.copy(
                    color = nodeColor,
                    fontWeight = FontWeight.Bold,
                    fontFamily = FontFamily.Monospace,
                    fontSize = 9.sp
                  )
                )
              }
            }

            event.secondaryMetric?.let { secMetric ->
              Box(
                modifier = Modifier
                  .clip(RoundedCornerShape(6.dp))
                  .background(Color.White.copy(alpha = 0.04f))
                  .padding(horizontal = 8.dp, vertical = 4.dp)
              ) {
                Text(
                  text = secMetric,
                  style = MaterialTheme.typography.labelSmall.copy(
                    color = Color.LightGray,
                    fontFamily = FontFamily.Monospace,
                    fontSize = 9.sp
                  )
                )
              }
            }
          }

          Spacer(modifier = Modifier.height(6.dp))
        }

        // Narrative Description
        Text(
          text = event.description,
          style = MaterialTheme.typography.bodySmall.copy(
            color = Color(0xFFCBD5E1),
            lineHeight = 16.sp
          )
        )

        // Expanded Clinical Action Directive & Flight Surgeon Protocol
        AnimatedVisibility(
          visible = isExpanded,
          enter = fadeIn() + expandVertically(),
          exit = fadeOut() + shrinkVertically()
        ) {
          Column(modifier = Modifier.padding(top = 8.dp)) {
            event.actionDirective?.let { directive ->
              Box(
                modifier = Modifier
                  .fillMaxWidth()
                  .clip(RoundedCornerShape(8.dp))
                  .background(Color.Black.copy(alpha = 0.5f))
                  .border(1.dp, nodeColor.copy(alpha = 0.4f), RoundedCornerShape(8.dp))
                  .padding(8.dp)
              ) {
                Row(verticalAlignment = Alignment.Top) {
                  Icon(
                    Icons.Default.Info,
                    contentDescription = null,
                    tint = nodeColor,
                    modifier = Modifier.size(14.dp).padding(top = 2.dp)
                  )
                  Spacer(modifier = Modifier.width(6.dp))
                  Column {
                    Text(
                      "CLINICAL ACTION DIRECTIVE:",
                      style = MaterialTheme.typography.labelSmall.copy(
                        color = nodeColor,
                        fontWeight = FontWeight.Bold,
                        fontSize = 8.sp
                      )
                    )
                    Spacer(modifier = Modifier.height(2.dp))
                    Text(
                      directive,
                      style = MaterialTheme.typography.bodySmall.copy(
                        color = Color.White,
                        fontSize = 10.sp
                      )
                    )
                  }
                }
              }
            }

            Spacer(modifier = Modifier.height(6.dp))

            Row(
              modifier = Modifier.fillMaxWidth(),
              horizontalArrangement = Arrangement.SpaceBetween,
              verticalAlignment = Alignment.CenterVertically
            ) {
              Text(
                "Astronaut: ${event.astronautName} (${event.astronautId})",
                style = MaterialTheme.typography.labelSmall.copy(color = Color.Gray, fontSize = 8.sp)
              )
              Text(
                "Tap to collapse",
                style = MaterialTheme.typography.labelSmall.copy(color = nodeColor, fontSize = 8.sp)
              )
            }
          }
        }
      }
    }
  }
}

@Composable
private fun StatusSeverityDot(severity: AlertSeverity) {
  val color = when (severity) {
    AlertSeverity.CRITICAL -> CriticalRed
    AlertSeverity.WARNING -> WarningAmber
    AlertSeverity.ADVISORY -> BrightTeal
  }

  Row(verticalAlignment = Alignment.CenterVertically) {
    Box(
      modifier = Modifier
        .size(6.dp)
        .clip(CircleShape)
        .background(color)
    )
    Spacer(modifier = Modifier.width(4.dp))
    Text(
      severity.name,
      style = MaterialTheme.typography.labelSmall.copy(color = color, fontSize = 8.sp)
    )
  }
}

@Composable
private fun FilterPill(
  label: String,
  count: Int,
  isSelected: Boolean,
  accentColor: Color,
  onClick: () -> Unit,
  modifier: Modifier = Modifier
) {
  Box(
    modifier = modifier
      .clip(RoundedCornerShape(8.dp))
      .background(if (isSelected) accentColor.copy(alpha = 0.25f) else Color.White.copy(alpha = 0.04f))
      .border(1.dp, if (isSelected) accentColor else Color.White.copy(alpha = 0.08f), RoundedCornerShape(8.dp))
      .clickable { onClick() }
      .padding(vertical = 6.dp),
    contentAlignment = Alignment.Center
  ) {
    Text(
      text = "$label ($count)",
      style = MaterialTheme.typography.labelSmall.copy(
        color = if (isSelected) accentColor else Color.LightGray,
        fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Normal,
        fontSize = 8.sp
      )
    )
  }
}

@Composable
private fun LogShiftEventDialog(
  astronautId: String,
  astronautName: String,
  onDismiss: () -> Unit,
  onConfirm: (ShiftTimelineEvent) -> Unit
) {
  var selectedCategory by remember { mutableStateOf(ShiftEventCategory.MEDICATION) }
  var eventTitle by remember { mutableStateOf("") }
  var eventDescription by remember { mutableStateOf("") }
  var primaryMetric by remember { mutableStateOf("") }
  var clinicalDirective by remember { mutableStateOf("") }
  var shiftHour by remember { mutableDoubleStateOf(20.0) }
  var severity by remember { mutableStateOf(AlertSeverity.ADVISORY) }

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
            Icon(Icons.Default.Medication, contentDescription = null, tint = NeonCyan, modifier = Modifier.size(18.dp))
            Spacer(modifier = Modifier.width(8.dp))
            Text(
              "LOG SHIFT EVENT",
              style = MaterialTheme.typography.titleMedium.copy(color = Color.White, fontWeight = FontWeight.Bold)
            )
          }
          IconButton(onClick = onDismiss, modifier = Modifier.size(24.dp)) {
            Icon(Icons.Default.Close, contentDescription = "Close", tint = Color.Gray)
          }
        }

        Spacer(modifier = Modifier.height(10.dp))

        Text(
          "Record medical intake, vital shifts, or emergency triggers for $astronautName.",
          style = MaterialTheme.typography.bodySmall.copy(color = Color.LightGray, fontSize = 10.sp)
        )

        Spacer(modifier = Modifier.height(12.dp))

        // Category Selector
        Text("CATEGORY:", style = MaterialTheme.typography.labelSmall.copy(color = Color.Gray, fontSize = 8.sp))
        Spacer(modifier = Modifier.height(4.dp))
        Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(6.dp)) {
          ShiftEventCategory.entries.forEach { cat ->
            val isSel = selectedCategory == cat
            val col = when (cat) {
              ShiftEventCategory.EMERGENCY -> CriticalRed
              ShiftEventCategory.MEDICATION -> ElectricIndigo
              ShiftEventCategory.BIOMETRIC -> BrightTeal
            }
            Box(
              modifier = Modifier
                .weight(1f)
                .clip(RoundedCornerShape(6.dp))
                .background(if (isSel) col.copy(alpha = 0.25f) else Color.White.copy(alpha = 0.05f))
                .border(1.dp, if (isSel) col else Color.Transparent, RoundedCornerShape(6.dp))
                .clickable {
                  selectedCategory = cat
                  if (cat == ShiftEventCategory.EMERGENCY) severity = AlertSeverity.CRITICAL
                }
                .padding(vertical = 6.dp),
              contentAlignment = Alignment.Center
            ) {
              Text(
                cat.displayName,
                style = MaterialTheme.typography.labelSmall.copy(
                  color = if (isSel) col else Color.LightGray,
                  fontSize = 9.sp,
                  fontWeight = if (isSel) FontWeight.Bold else FontWeight.Normal
                )
              )
            }
          }
        }

        Spacer(modifier = Modifier.height(10.dp))

        // Quick Preset Buttons
        Text("QUICK CLINICAL TEMPLATES:", style = MaterialTheme.typography.labelSmall.copy(color = Color.Gray, fontSize = 8.sp))
        Spacer(modifier = Modifier.height(4.dp))
        Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(6.dp)) {
          Box(
            modifier = Modifier
              .weight(1f)
              .clip(RoundedCornerShape(6.dp))
              .background(Color.White.copy(alpha = 0.06f))
              .clickable {
                selectedCategory = ShiftEventCategory.MEDICATION
                eventTitle = "Promethazine 25mg Dose"
                primaryMetric = "Promethazine 25mg Oral"
                eventDescription = "Administered for acute space motion sickness."
                clinicalDirective = "Monitor sensorimotor recovery."
              }
              .padding(4.dp),
            contentAlignment = Alignment.Center
          ) {
            Text("Promethazine", style = MaterialTheme.typography.labelSmall.copy(color = NeonCyan, fontSize = 8.sp))
          }

          Box(
            modifier = Modifier
              .weight(1f)
              .clip(RoundedCornerShape(6.dp))
              .background(Color.White.copy(alpha = 0.06f))
              .clickable {
                selectedCategory = ShiftEventCategory.MEDICATION
                eventTitle = "Melatonin 3mg Circadian Reset"
                primaryMetric = "Melatonin 3mg Oral"
                eventDescription = "Administered 60 min before sleep pod ingress."
                clinicalDirective = "Engage sleep visor 490nm amber filter."
              }
              .padding(4.dp),
            contentAlignment = Alignment.Center
          ) {
            Text("Melatonin", style = MaterialTheme.typography.labelSmall.copy(color = NeonCyan, fontSize = 8.sp))
          }

          Box(
            modifier = Modifier
              .weight(1f)
              .clip(RoundedCornerShape(6.dp))
              .background(Color.White.copy(alpha = 0.06f))
              .clickable {
                selectedCategory = ShiftEventCategory.BIOMETRIC
                eventTitle = "Tachypnea / HR Exertion Surge"
                primaryMetric = "HR 128 bpm • SpO₂ 97%"
                eventDescription = "Spike observed during cargo transfer."
                clinicalDirective = "Ordered 10-minute resting cycle."
              }
              .padding(4.dp),
            contentAlignment = Alignment.Center
          ) {
            Text("Vitals Spike", style = MaterialTheme.typography.labelSmall.copy(color = NeonCyan, fontSize = 8.sp))
          }
        }

        Spacer(modifier = Modifier.height(10.dp))

        // Title Input
        OutlinedTextField(
          value = eventTitle,
          onValueChange = { eventTitle = it },
          label = { Text("Event Title (e.g. Melatonin Intake)", fontSize = 10.sp) },
          modifier = Modifier.fillMaxWidth().testTag("log_event_title_input"),
          colors = OutlinedTextFieldDefaults.colors(
            focusedBorderColor = NeonCyan,
            unfocusedBorderColor = GlassBorderStroke,
            focusedTextColor = Color.White,
            unfocusedTextColor = Color.White
          ),
          shape = RoundedCornerShape(8.dp)
        )

        Spacer(modifier = Modifier.height(6.dp))

        // Metric Input
        OutlinedTextField(
          value = primaryMetric,
          onValueChange = { primaryMetric = it },
          label = { Text("Metric / Dose (e.g. 5mg Oral or HR 118 bpm)", fontSize = 10.sp) },
          modifier = Modifier.fillMaxWidth().testTag("log_event_metric_input"),
          colors = OutlinedTextFieldDefaults.colors(
            focusedBorderColor = NeonCyan,
            unfocusedBorderColor = GlassBorderStroke,
            focusedTextColor = Color.White,
            unfocusedTextColor = Color.White
          ),
          shape = RoundedCornerShape(8.dp)
        )

        Spacer(modifier = Modifier.height(6.dp))

        // Description Input
        OutlinedTextField(
          value = eventDescription,
          onValueChange = { eventDescription = it },
          label = { Text("Clinical Narrative & Observations", fontSize = 10.sp) },
          modifier = Modifier.fillMaxWidth().testTag("log_event_desc_input"),
          colors = OutlinedTextFieldDefaults.colors(
            focusedBorderColor = NeonCyan,
            unfocusedBorderColor = GlassBorderStroke,
            focusedTextColor = Color.White,
            unfocusedTextColor = Color.White
          ),
          shape = RoundedCornerShape(8.dp)
        )

        Spacer(modifier = Modifier.height(6.dp))

        // Clinical Action Directive Input
        OutlinedTextField(
          value = clinicalDirective,
          onValueChange = { clinicalDirective = it },
          label = { Text("Action Directive (e.g. Rest protocol)", fontSize = 10.sp) },
          modifier = Modifier.fillMaxWidth().testTag("log_event_directive_input"),
          colors = OutlinedTextFieldDefaults.colors(
            focusedBorderColor = NeonCyan,
            unfocusedBorderColor = GlassBorderStroke,
            focusedTextColor = Color.White,
            unfocusedTextColor = Color.White
          ),
          shape = RoundedCornerShape(8.dp)
        )

        Spacer(modifier = Modifier.height(14.dp))

        // Submit Button
        Button(
          onClick = {
            if (eventTitle.isNotBlank()) {
              val newEvt = ShiftTimelineEvent(
                id = "EVT-" + UUID.randomUUID().toString().take(6).uppercase(),
                astronautId = astronautId,
                astronautName = astronautName,
                shiftHour = shiftHour,
                shiftTimeLabel = "T+${shiftHour.toInt().toString().padStart(2, '0')}:30",
                utcTimestamp = "20:30 UTC",
                category = selectedCategory,
                severity = severity,
                title = eventTitle.trim(),
                description = if (eventDescription.isBlank()) "Logged by astronaut telemetry console." else eventDescription.trim(),
                primaryMetric = primaryMetric.ifBlank { null },
                secondaryMetric = "Shift Hour ${shiftHour.toInt()}",
                actionDirective = clinicalDirective.ifBlank { null },
                status = if (selectedCategory == ShiftEventCategory.MEDICATION) "ADMINISTERED" else "LOGGED",
                iconType = when (selectedCategory) {
                  ShiftEventCategory.MEDICATION -> "pill"
                  ShiftEventCategory.EMERGENCY -> "alert"
                  ShiftEventCategory.BIOMETRIC -> "heart"
                }
              )
              onConfirm(newEvt)
            }
          },
          modifier = Modifier.fillMaxWidth().height(42.dp).testTag("confirm_log_event_button"),
          colors = ButtonDefaults.buttonColors(containerColor = NeonCyan, contentColor = Color.Black),
          shape = RoundedCornerShape(8.dp)
        ) {
          Icon(Icons.Default.CheckCircle, contentDescription = null, modifier = Modifier.size(16.dp))
          Spacer(modifier = Modifier.width(6.dp))
          Text("SUBMIT SHIFT LOG ENTRY", style = MaterialTheme.typography.labelSmall.copy(fontWeight = FontWeight.Bold))
        }
      }
    }
  }
}
