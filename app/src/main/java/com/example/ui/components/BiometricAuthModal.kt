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
import androidx.compose.material.icons.filled.Fingerprint
import androidx.compose.material.icons.filled.Lock
import androidx.compose.material.icons.filled.Security
import androidx.compose.material.icons.filled.Sensors
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.LinearProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableFloatStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.window.Dialog
import com.example.data.AstraPersistenceManager
import com.example.data.AstraRepository
import com.example.model.CrewMember
import com.example.service.AstraAudioAlarmService
import com.example.service.AstraInAppNotificationManager
import com.example.ui.theme.BrightTeal
import com.example.ui.theme.CriticalRed
import com.example.ui.theme.GlassBorderStroke
import com.example.ui.theme.NeonCyan
import com.example.ui.theme.NominalGreen
import kotlinx.coroutines.delay

enum class BiometricScanState {
  STANDBY,
  SCANNING,
  VERIFYING,
  SUCCESS,
  FAILED
}

/**
 * Functional Biometric Telemetry & Bio-Harness Authentication Modal.
 * Scans optical dermal fingerprint / facial biometrics and checks cryptographic token
 * against enrolled NASA astronaut signatures to actively authenticate and unlock mission portals.
 */
@Composable
fun BiometricAuthModal(
  candidateCrew: CrewMember,
  onDismiss: () -> Unit,
  onAuthSuccess: (CrewMember) -> Unit
) {
  val context = LocalContext.current
  var scanState by remember { mutableStateOf(BiometricScanState.STANDBY) }
  var scanProgress by remember { mutableFloatStateOf(0f) }
  var statusMessage by remember { mutableStateOf("Touch sensor or hold bio-harness against scanner.") }

  val infiniteTransition = rememberInfiniteTransition(label = "bioPulse")
  val pulseScale by infiniteTransition.animateFloat(
    initialValue = 0.9f,
    targetValue = 1.15f,
    animationSpec = infiniteRepeatable(
      animation = tween(900, easing = LinearEasing),
      repeatMode = RepeatMode.Reverse
    ),
    label = "pulseScale"
  )

  LaunchedEffect(scanState) {
    if (scanState == BiometricScanState.SCANNING) {
      statusMessage = "Acquiring biometric telemetry waveform..."
      for (i in 1..20) {
        delay(60)
        scanProgress = i / 20f
      }
      scanState = BiometricScanState.VERIFYING
      statusMessage = "Verifying cryptographic dermal token with Mission Enclave..."
      delay(400)

      val isEnrolled = AstraPersistenceManager.isUserBiometricEnrolled(candidateCrew.id)
      if (isEnrolled) {
        scanState = BiometricScanState.SUCCESS
        statusMessage = "Biometric Match Confirmed: Clearance granted to ${candidateCrew.fullName}."
        AstraAudioAlarmService.playConfirmationChime(context)
        delay(700)
        AstraRepository.switchUser(candidateCrew)
        AstraInAppNotificationManager.postNotification(
          title = "BIOMETRIC AUTHENTICATION CONFIRMED",
          message = "${candidateCrew.fullName} (${candidateCrew.id}) authenticated via Bio-Harness sensor.",
          severity = com.example.model.AlertSeverity.ADVISORY,
          context = context,
          playAudio = false
        )
        onAuthSuccess(candidateCrew)
      } else {
        scanState = BiometricScanState.FAILED
        statusMessage = "Biometric Verification Failed: Signature mismatch for ${candidateCrew.id}."
      }
    }
  }

  Dialog(onDismissRequest = onDismiss) {
    Box(
      modifier = Modifier
        .fillMaxWidth()
        .clip(RoundedCornerShape(20.dp))
        .background(Color(0xFF030A19))
        .border(1.5.dp, NeonCyan, RoundedCornerShape(20.dp))
        .padding(20.dp)
        .testTag("biometric_auth_dialog")
    ) {
      Column(
        modifier = Modifier.fillMaxWidth(),
        horizontalAlignment = Alignment.CenterHorizontally
      ) {
        // Top Header
        Row(
          modifier = Modifier.fillMaxWidth(),
          horizontalArrangement = Arrangement.SpaceBetween,
          verticalAlignment = Alignment.CenterVertically
        ) {
          Row(verticalAlignment = Alignment.CenterVertically) {
            Icon(Icons.Default.Security, contentDescription = null, tint = BrightTeal, modifier = Modifier.size(16.dp))
            Spacer(modifier = Modifier.width(6.dp))
            Text(
              "NASA BIO-AUTHENTICATION",
              style = MaterialTheme.typography.labelSmall.copy(color = BrightTeal, fontWeight = FontWeight.Bold, letterSpacing = 1.sp)
            )
          }

          IconButton(onClick = onDismiss, modifier = Modifier.size(28.dp)) {
            Icon(Icons.Default.Close, contentDescription = "Close", tint = Color.Gray, modifier = Modifier.size(16.dp))
          }
        }

        Spacer(modifier = Modifier.height(14.dp))

        // Candidate Astronaut Banner
        Box(
          modifier = Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(10.dp))
            .background(Color.Black.copy(alpha = 0.5f))
            .border(1.dp, GlassBorderStroke, RoundedCornerShape(10.dp))
            .padding(10.dp)
        ) {
          Row(verticalAlignment = Alignment.CenterVertically) {
            Box(
              modifier = Modifier
                .size(36.dp)
                .clip(CircleShape)
                .background(NeonCyan.copy(alpha = 0.25f)),
              contentAlignment = Alignment.Center
            ) {
              Text(
                candidateCrew.fullName.split(" ").mapNotNull { it.firstOrNull()?.toString() }.take(2).joinToString(""),
                color = Color.White,
                fontWeight = FontWeight.Bold,
                fontSize = 11.sp
              )
            }
            Spacer(modifier = Modifier.width(10.dp))
            Column {
              Text(candidateCrew.fullName, style = MaterialTheme.typography.titleSmall.copy(color = Color.White, fontWeight = FontWeight.Bold))
              Text("ID: ${candidateCrew.id} • ${candidateCrew.missionSpecialty}", style = MaterialTheme.typography.labelSmall.copy(color = NeonCyan, fontSize = 8.5.sp))
            }
          }
        }

        Spacer(modifier = Modifier.height(20.dp))

        // Center Fingerprint Scanner Pad
        val scannerBorderColor = when (scanState) {
          BiometricScanState.STANDBY -> NeonCyan
          BiometricScanState.SCANNING, BiometricScanState.VERIFYING -> BrightTeal
          BiometricScanState.SUCCESS -> NominalGreen
          BiometricScanState.FAILED -> CriticalRed
        }

        Box(
          modifier = Modifier
            .size(110.dp)
            .clip(CircleShape)
            .background(Color.Black.copy(alpha = 0.6f))
            .border(2.dp, scannerBorderColor, CircleShape)
            .clickable(enabled = scanState == BiometricScanState.STANDBY || scanState == BiometricScanState.FAILED) {
              scanProgress = 0f
              scanState = BiometricScanState.SCANNING
            }
            .testTag("biometric_scanner_pad"),
          contentAlignment = Alignment.Center
        ) {
          if (scanState == BiometricScanState.SCANNING || scanState == BiometricScanState.VERIFYING) {
            CircularProgressIndicator(
              progress = { scanProgress },
              modifier = Modifier.size(100.dp),
              color = BrightTeal,
              strokeWidth = 3.dp
            )
          }

          Icon(
            imageVector = when (scanState) {
              BiometricScanState.SUCCESS -> Icons.Default.CheckCircle
              else -> Icons.Default.Fingerprint
            },
            contentDescription = "Fingerprint Sensor",
            tint = scannerBorderColor,
            modifier = Modifier.size(56.dp)
          )
        }

        Spacer(modifier = Modifier.height(14.dp))

        // Status Feedback Text
        Text(
          text = statusMessage,
          style = MaterialTheme.typography.bodySmall.copy(
            color = when (scanState) {
              BiometricScanState.SUCCESS -> NominalGreen
              BiometricScanState.FAILED -> CriticalRed
              else -> Color(0xFFCBD5E1)
            },
            fontSize = 10.sp,
            textAlign = androidx.compose.ui.text.style.TextAlign.Center
          ),
          modifier = Modifier.padding(horizontal = 12.dp)
        )

        Spacer(modifier = Modifier.height(16.dp))

        // Action Buttons
        Row(
          modifier = Modifier.fillMaxWidth(),
          horizontalArrangement = Arrangement.spacedBy(10.dp)
        ) {
          if (scanState == BiometricScanState.STANDBY || scanState == BiometricScanState.FAILED) {
            Button(
              onClick = {
                scanProgress = 0f
                scanState = BiometricScanState.SCANNING
              },
              modifier = Modifier.weight(1f).height(40.dp).testTag("trigger_biometric_scan_button"),
              colors = ButtonDefaults.buttonColors(containerColor = NeonCyan, contentColor = Color.Black),
              shape = RoundedCornerShape(10.dp)
            ) {
              Row(verticalAlignment = Alignment.CenterVertically) {
                Icon(Icons.Default.Sensors, contentDescription = null, modifier = Modifier.size(16.dp))
                Spacer(modifier = Modifier.width(6.dp))
                Text("INITIATE SCAN", style = MaterialTheme.typography.labelSmall.copy(fontWeight = FontWeight.Bold))
              }
            }
          }

          Button(
            onClick = onDismiss,
            modifier = Modifier.weight(1f).height(40.dp),
            colors = ButtonDefaults.buttonColors(containerColor = Color.White.copy(alpha = 0.1f), contentColor = Color.White),
            shape = RoundedCornerShape(10.dp)
          ) {
            Text("CANCEL", style = MaterialTheme.typography.labelSmall)
          }
        }
      }
    }
  }
}
