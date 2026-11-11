package com.example.service

import android.content.Context
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.slideInVertically
import androidx.compose.animation.slideOutVertically
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
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.CrisisAlert
import androidx.compose.material.icons.filled.Emergency
import androidx.compose.material.icons.filled.Info
import androidx.compose.material.icons.filled.Warning
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.model.AlertSeverity
import com.example.ui.theme.BrightTeal
import com.example.ui.theme.CelestialAmber
import com.example.ui.theme.CriticalRed
import com.example.ui.theme.NeonCyan
import com.example.ui.theme.NominalGreen
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale
import java.util.UUID

private val AlertSeverity.displayName: String
  get() = when (this) {
    AlertSeverity.CRITICAL -> "CRITICAL THREAT"
    AlertSeverity.WARNING -> "WARNING ALERT"
    AlertSeverity.ADVISORY -> "ADVISORY NOTICE"
  }

data class InAppNotification(
  val id: String = UUID.randomUUID().toString(),
  val title: String,
  val message: String,
  val severity: AlertSeverity,
  val timestamp: String = SimpleDateFormat("HH:mm:ss", Locale.US).format(Date()),
  val requiresAck: Boolean = false,
  val actionLabel: String? = null,
  val onAction: (() -> Unit)? = null
)

/**
 * On-screen active notification manager for AstraSense.
 * Displays floating heads-up alert banners at the top of the interface
 * that adapt to portrait & landscape modes, with persistent acknowledgments for critical alerts.
 */
object AstraInAppNotificationManager {
  private val _activeNotification = MutableStateFlow<InAppNotification?>(null)
  val activeNotification: StateFlow<InAppNotification?> = _activeNotification.asStateFlow()

  private val _notificationHistory = MutableStateFlow<List<InAppNotification>>(emptyList())
  val notificationHistory: StateFlow<List<InAppNotification>> = _notificationHistory.asStateFlow()

  private val notificationScope = CoroutineScope(Dispatchers.Main)

  fun postNotification(
    title: String,
    message: String,
    severity: AlertSeverity = AlertSeverity.ADVISORY,
    requiresAck: Boolean = false,
    actionLabel: String? = null,
    context: Context? = null,
    playAudio: Boolean = true,
    onAction: (() -> Unit)? = null
  ) {
    val notification = InAppNotification(
      title = title,
      message = message,
      severity = severity,
      requiresAck = requiresAck,
      actionLabel = actionLabel,
      onAction = onAction
    )

    _activeNotification.value = notification
    _notificationHistory.update { listOf(notification) + it.take(40) }

    // Trigger audio based on severity
    if (playAudio) {
      when (severity) {
        AlertSeverity.CRITICAL -> AstraAudioAlarmService.playEmergencyAlarm(context ?: return)
        AlertSeverity.WARNING -> AstraAudioAlarmService.playThreatWarningSound(context)
        AlertSeverity.ADVISORY -> AstraAudioAlarmService.playConfirmationChime(context)
      }
    }

    // Auto-dismiss advisory and warning notifications after 6 seconds if they don't require user acknowledgment
    if (!requiresAck && severity != AlertSeverity.CRITICAL) {
      notificationScope.launch {
        delay(6000)
        if (_activeNotification.value?.id == notification.id) {
          _activeNotification.value = null
        }
      }
    }
  }

  fun dismissNotification(id: String) {
    if (_activeNotification.value?.id == id) {
      _activeNotification.value = null
    }
  }

  fun acknowledgeNotification(id: String) {
    val current = _activeNotification.value
    if (current?.id == id) {
      current.onAction?.invoke()
      _activeNotification.value = null
      // Stop continuous emergency siren if it was an emergency notification
      if (current.severity == AlertSeverity.CRITICAL) {
        AstraAudioAlarmService.stopEmergencyAlarm()
      }
    }
  }
}

/**
 * Composable that displays the active in-app floating banner on top of the app.
 * Works seamlessly in both portrait and landscape orientations.
 */
@Composable
fun InAppNotificationBannerOverlay(
  modifier: Modifier = Modifier
) {
  val notification by AstraInAppNotificationManager.activeNotification.collectAsState()

  AnimatedVisibility(
    visible = notification != null,
    enter = slideInVertically { -it } + fadeIn(),
    exit = slideOutVertically { -it } + fadeOut(),
    modifier = modifier
      .fillMaxWidth()
      .padding(horizontal = 14.dp, vertical = 8.dp)
  ) {
    notification?.let { alert ->
      val (accentColor, bgColor, icon) = when (alert.severity) {
        AlertSeverity.CRITICAL -> Triple(
          CriticalRed,
          Color(0xFF240408),
          Icons.Default.Emergency
        )
        AlertSeverity.WARNING -> Triple(
          CelestialAmber,
          Color(0xFF231704),
          Icons.Default.Warning
        )
        AlertSeverity.ADVISORY -> Triple(
          NeonCyan,
          Color(0xFF041926),
          Icons.Default.Info
        )
      }

      Box(
        modifier = Modifier
          .fillMaxWidth()
          .clip(RoundedCornerShape(14.dp))
          .background(bgColor.copy(alpha = 0.95f))
          .border(1.5.dp, accentColor, RoundedCornerShape(14.dp))
          .padding(12.dp)
          .testTag("in_app_notification_banner")
      ) {
        Column(modifier = Modifier.fillMaxWidth()) {
          Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
          ) {
            Row(verticalAlignment = Alignment.CenterVertically, modifier = Modifier.weight(1f)) {
              Box(
                modifier = Modifier
                  .size(30.dp)
                  .clip(CircleShape)
                  .background(accentColor.copy(alpha = 0.22f)),
                contentAlignment = Alignment.Center
              ) {
                Icon(
                  imageVector = icon,
                  contentDescription = null,
                  tint = accentColor,
                  modifier = Modifier.size(16.dp)
                )
              }
              Spacer(modifier = Modifier.width(8.dp))
              Column {
                Text(
                  text = alert.title.uppercase(),
                  style = MaterialTheme.typography.titleSmall.copy(
                    color = Color.White,
                    fontWeight = FontWeight.Bold,
                    fontSize = 11.5.sp
                  )
                )
                Text(
                  text = "${alert.severity.displayName} • ${alert.timestamp} UTC",
                  style = MaterialTheme.typography.labelSmall.copy(color = accentColor, fontSize = 8.5.sp)
                )
              }
            }

            IconButton(
              onClick = { AstraInAppNotificationManager.dismissNotification(alert.id) },
              modifier = Modifier.size(26.dp)
            ) {
              Icon(Icons.Default.Close, contentDescription = "Dismiss", tint = Color.LightGray, modifier = Modifier.size(14.dp))
            }
          }

          Spacer(modifier = Modifier.height(6.dp))

          Text(
            text = alert.message,
            style = MaterialTheme.typography.bodySmall.copy(color = Color(0xFFE2E8F0), fontSize = 10.sp)
          )

          if (alert.requiresAck || alert.actionLabel != null) {
            Spacer(modifier = Modifier.height(10.dp))
            Row(
              modifier = Modifier.fillMaxWidth(),
              horizontalArrangement = Arrangement.End
            ) {
              Button(
                onClick = { AstraInAppNotificationManager.acknowledgeNotification(alert.id) },
                colors = ButtonDefaults.buttonColors(containerColor = accentColor, contentColor = Color.Black),
                shape = RoundedCornerShape(8.dp),
                modifier = Modifier.height(32.dp).testTag("notification_ack_button")
              ) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                  Icon(Icons.Default.CheckCircle, contentDescription = null, modifier = Modifier.size(13.dp))
                  Spacer(modifier = Modifier.width(4.dp))
                  Text(
                    text = alert.actionLabel ?: "ACKNOWLEDGE ALERT",
                    style = MaterialTheme.typography.labelSmall.copy(fontWeight = FontWeight.Bold, fontSize = 8.5.sp)
                  )
                }
              }
            }
          }
        }
      }
    }
  }
}
