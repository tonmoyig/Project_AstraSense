package com.example.ui.components

import androidx.compose.foundation.background
import androidx.compose.foundation.border
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
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.EmojiEvents
import androidx.compose.material.icons.filled.Favorite
import androidx.compose.material.icons.filled.FitnessCenter
import androidx.compose.material.icons.filled.LocalFireDepartment
import androidx.compose.material.icons.filled.MilitaryTech
import androidx.compose.material.icons.filled.Shield
import androidx.compose.material.icons.filled.Visibility
import androidx.compose.material.icons.filled.WaterDrop
import androidx.compose.material3.Icon
import androidx.compose.material3.LinearProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.model.AstronautHealthExpeditionState
import com.example.model.ExpeditionBadge
import com.example.ui.theme.BrightTeal
import com.example.ui.theme.GlassBorderStroke
import com.example.ui.theme.NeonCyan
import com.example.ui.theme.NominalGreen
import com.example.ui.theme.StarlightBlue
import com.example.ui.theme.WarningAmber

/**
 * Gamification & Countermeasure Expedition Tracking for Astronaut Health.
 * Displays level progression, daily streak counter, resilience score, and milestone badges.
 */
@Composable
fun HealthAssessmentGamificationView(
  expeditionState: AstronautHealthExpeditionState,
  modifier: Modifier = Modifier
) {
  GlassCard(modifier = modifier.fillMaxWidth().testTag("health_gamification_card")) {
    Column {
      // Header: Level & Streak
      Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically
      ) {
        Row(verticalAlignment = Alignment.CenterVertically) {
          Box(
            modifier = Modifier
              .size(32.dp)
              .clip(CircleShape)
              .background(NeonCyan.copy(alpha = 0.2f))
              .border(1.dp, NeonCyan, CircleShape),
            contentAlignment = Alignment.Center
          ) {
            Icon(Icons.Default.MilitaryTech, contentDescription = null, tint = NeonCyan, modifier = Modifier.size(18.dp))
          }
          Spacer(modifier = Modifier.width(8.dp))
          Column {
            Text(
              "ASTRONAUT HEALTH EXPEDITION",
              style = MaterialTheme.typography.titleSmall.copy(color = Color.White, fontWeight = FontWeight.Bold, fontSize = 11.5.sp)
            )
            Text(
              "LEVEL ${expeditionState.expeditionLevel} • FLIGHT SURGEON RESILIENCE PROGRAM",
              style = MaterialTheme.typography.labelSmall.copy(color = BrightTeal, fontSize = 8.5.sp)
            )
          }
        }

        // Daily Habit Streak Badge
        Row(
          modifier = Modifier
            .clip(RoundedCornerShape(8.dp))
            .background(WarningAmber.copy(alpha = 0.2f))
            .border(1.dp, WarningAmber.copy(alpha = 0.5f), RoundedCornerShape(8.dp))
            .padding(horizontal = 8.dp, vertical = 4.dp),
          verticalAlignment = Alignment.CenterVertically
        ) {
          Icon(Icons.Default.LocalFireDepartment, contentDescription = null, tint = WarningAmber, modifier = Modifier.size(13.dp))
          Spacer(modifier = Modifier.width(4.dp))
          Text(
            "${expeditionState.streakDays}D STREAK",
            style = MaterialTheme.typography.labelSmall.copy(color = WarningAmber, fontWeight = FontWeight.Black, fontSize = 9.sp)
          )
        }
      }

      Spacer(modifier = Modifier.height(10.dp))

      // XP Progress Bar & Resilience Index
      Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically
      ) {
        Text(
          "Expedition XP: ${expeditionState.currentExpeditionXp} / ${expeditionState.targetLevelXp}",
          style = MaterialTheme.typography.bodySmall.copy(color = Color.LightGray, fontSize = 9.5.sp)
        )
        Text(
          "Resilience Index: ${expeditionState.resilienceIndexScore}%",
          style = MaterialTheme.typography.titleSmall.copy(color = NominalGreen, fontWeight = FontWeight.Bold, fontSize = 10.sp)
        )
      }

      Spacer(modifier = Modifier.height(4.dp))

      val progress = (expeditionState.currentExpeditionXp.toFloat() / expeditionState.targetLevelXp.toFloat()).coerceIn(0f, 1f)
      LinearProgressIndicator(
        progress = { progress },
        modifier = Modifier
          .fillMaxWidth()
          .height(6.dp)
          .clip(RoundedCornerShape(3.dp)),
        color = NeonCyan,
        trackColor = Color.White.copy(alpha = 0.1f)
      )

      Spacer(modifier = Modifier.height(12.dp))

      // Milestone Badges Horizontal Scroll
      Text(
        "EARNED COUNTERMEASURE EXPEDITION BADGES:",
        style = MaterialTheme.typography.labelSmall.copy(color = Color.Gray, fontSize = 8.5.sp, fontWeight = FontWeight.Bold)
      )
      Spacer(modifier = Modifier.height(6.dp))

      Row(
        modifier = Modifier
          .fillMaxWidth()
          .horizontalScroll(rememberScrollState()),
        horizontalArrangement = Arrangement.spacedBy(8.dp)
      ) {
        expeditionState.earnedBadges.forEach { badge ->
          BadgeItem(badge)
        }
      }
    }
  }
}

@Composable
private fun BadgeItem(badge: ExpeditionBadge) {
  val isUnlocked = badge.isUnlocked
  val borderColor = if (isUnlocked) NeonCyan.copy(alpha = 0.5f) else Color.White.copy(alpha = 0.1f)
  val bgColor = if (isUnlocked) Color(0xFF07192C) else Color.Black.copy(alpha = 0.4f)
  val iconColor = if (isUnlocked) NeonCyan else Color.Gray

  Row(
    modifier = Modifier
      .clip(RoundedCornerShape(10.dp))
      .background(bgColor)
      .border(1.dp, borderColor, RoundedCornerShape(10.dp))
      .padding(horizontal = 8.dp, vertical = 6.dp),
    verticalAlignment = Alignment.CenterVertically
  ) {
    Box(
      modifier = Modifier
        .size(24.dp)
        .clip(CircleShape)
        .background(if (isUnlocked) NeonCyan.copy(alpha = 0.15f) else Color.White.copy(alpha = 0.05f)),
      contentAlignment = Alignment.Center
    ) {
      val iconVector = when (badge.iconName) {
        "shield" -> Icons.Default.Shield
        "visibility" -> Icons.Default.Visibility
        "fitness_center" -> Icons.Default.FitnessCenter
        "water_drop" -> Icons.Default.WaterDrop
        "favorite" -> Icons.Default.Favorite
        else -> Icons.Default.EmojiEvents
      }
      Icon(iconVector, contentDescription = null, tint = iconColor, modifier = Modifier.size(13.dp))
    }

    Spacer(modifier = Modifier.width(6.dp))

    Column {
      Row(verticalAlignment = Alignment.CenterVertically) {
        Text(
          badge.title,
          style = MaterialTheme.typography.labelSmall.copy(
            color = if (isUnlocked) Color.White else Color.Gray,
            fontWeight = FontWeight.Bold,
            fontSize = 9.sp
          )
        )
        if (isUnlocked) {
          Spacer(modifier = Modifier.width(4.dp))
          Text("✓", color = NominalGreen, fontSize = 8.sp, fontWeight = FontWeight.Bold)
        }
      }
      Text(
        if (isUnlocked) "UNLOCKED" else "LOCKED (IN PROGRESS)",
        style = MaterialTheme.typography.labelSmall.copy(
          color = if (isUnlocked) NominalGreen else Color.Gray,
          fontSize = 7.5.sp
        )
      )
    }
  }
}
