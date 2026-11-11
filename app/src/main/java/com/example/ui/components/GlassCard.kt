package com.example.ui.components

import androidx.compose.animation.core.FastOutSlowInEasing
import androidx.compose.animation.core.RepeatMode
import androidx.compose.animation.core.animateFloat
import androidx.compose.animation.core.infiniteRepeatable
import androidx.compose.animation.core.rememberInfiniteTransition
import androidx.compose.animation.core.tween
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.BoxScope
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.testTag
import androidx.compose.foundation.layout.wrapContentSize
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.ui.theme.GlassBorderStroke
import com.example.ui.theme.NeonCyan
import com.example.ui.theme.NominalGreen

/**
 * NASA-inspired Glassmorphism container featuring:
 * - Multi-stop translucent navy background gradient
 * - Frosted glass reflection highlight on the upper edge
 * - Subtle glowing cyan/electric-blue perimeter border
 * - Strict 8dp grid alignment
 */
@Composable
fun GlassCard(
  modifier: Modifier = Modifier,
  cornerRadius: Dp = 18.dp,
  borderColor: Color = GlassBorderStroke,
  borderWidth: Dp = 1.dp,
  contentPadding: Dp = 14.dp,
  backgroundColor: Color = MaterialTheme.colorScheme.surface.copy(alpha = 0.72f),
  content: @Composable BoxScope.() -> Unit
) {
  val shape = RoundedCornerShape(cornerRadius)

  // Multi-stop astronomical frosted glass gradient
  val backgroundBrush = Brush.verticalGradient(
    colors = listOf(
      backgroundColor.copy(alpha = 0.88f),
      backgroundColor.copy(alpha = 0.62f),
      backgroundColor.copy(alpha = 0.78f)
    )
  )

  // Border gradient with subtle starlight sheen
  val borderBrush = Brush.linearGradient(
    colors = listOf(
      borderColor.copy(alpha = 0.75f),
      borderColor.copy(alpha = 0.25f),
      borderColor.copy(alpha = 0.60f)
    )
  )

  Surface(
    modifier = modifier
      .clip(shape)
      .border(BorderStroke(borderWidth, borderBrush), shape),
    shape = shape,
    color = Color.Transparent,
    tonalElevation = 8.dp
  ) {
    Box(
      modifier = Modifier
        .background(backgroundBrush)
        .padding(contentPadding)
    ) {
      // Top glass highlight line (simulates physical lens specular reflection)
      Box(
        modifier = Modifier
          .fillMaxWidth()
          .height(1.dp)
          .align(Alignment.TopCenter)
          .background(
            Brush.horizontalGradient(
              listOf(
                Color.Transparent,
                Color.White.copy(alpha = 0.22f),
                Color.Transparent
              )
            )
          )
      )

      content()
    }
  }
}

@Composable
fun StatusBadge(
  text: String,
  modifier: Modifier = Modifier,
  color: Color = NominalGreen,
  hasPulse: Boolean = true,
  testTag: String = "status_badge"
) {
  val infiniteTransition = rememberInfiniteTransition(label = "badgePulse")
  val alpha by infiniteTransition.animateFloat(
    initialValue = 0.45f,
    targetValue = 1.0f,
    animationSpec = infiniteRepeatable(
      animation = tween(1100, easing = FastOutSlowInEasing),
      repeatMode = RepeatMode.Reverse
    ),
    label = "pulseAlpha"
  )

  Surface(
    modifier = modifier.wrapContentSize(Alignment.Center).testTag(testTag),
    shape = RoundedCornerShape(8.dp),
    color = color.copy(alpha = 0.16f),
    border = BorderStroke(1.dp, color.copy(alpha = 0.55f))
  ) {
    Row(
      modifier = Modifier.padding(horizontal = 7.dp, vertical = 3.dp),
      verticalAlignment = Alignment.CenterVertically
    ) {
      Box(
        modifier = Modifier
          .size(5.dp)
          .clip(CircleShape)
          .background(color.copy(alpha = if (hasPulse) alpha else 1.0f))
      )
      Spacer(modifier = Modifier.width(4.dp))
      Text(
        text = text.uppercase(),
        style = MaterialTheme.typography.labelSmall.copy(
          letterSpacing = 0.5.sp,
          color = color,
          fontSize = 8.sp,
          fontWeight = FontWeight.Bold
        ),
        maxLines = 1,
        softWrap = false
      )
    }
  }
}
