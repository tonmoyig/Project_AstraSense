package com.example.ui.components

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.core.FastOutSlowInEasing
import androidx.compose.animation.core.RepeatMode
import androidx.compose.animation.core.animateFloat
import androidx.compose.animation.core.infiniteRepeatable
import androidx.compose.animation.core.rememberInfiniteTransition
import androidx.compose.animation.core.tween
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.slideInVertically
import androidx.compose.animation.slideOutVertically
import androidx.compose.foundation.background
import androidx.compose.foundation.border
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
import androidx.compose.material.icons.filled.CrisisAlert
import androidx.compose.material.icons.filled.HealthAndSafety
import androidx.compose.material.icons.filled.Shield
import androidx.compose.material.icons.filled.VerifiedUser
import androidx.compose.material.icons.filled.Warning
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.OutlinedTextFieldDefaults
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.window.Dialog
import com.example.model.CrewMember
import com.example.model.UserRole
import com.example.service.CriticalRiskAlert
import com.example.ui.theme.CriticalRed
import com.example.ui.theme.GlassBorderStroke
import com.example.ui.theme.NeonCyan
import com.example.ui.theme.NominalGreen
import com.example.ui.theme.WarningAmber

@Composable
fun CriticalRiskAcknowledgementModal(
  pendingAlert: CriticalRiskAlert,
  currentUser: CrewMember?,
  onAcknowledge: (alertId: String, officerId: String, notes: String) -> Unit
) {
  val defaultOfficerName = if (currentUser?.role == UserRole.ADMIN) {
    "${currentUser.fullName} (${currentUser.id})"
  } else {
    "Dr. Alex Vance (NASA-AS-701)"
  }

  var officerSignature by remember { mutableStateOf(defaultOfficerName) }
  var clinicalNotes by remember { mutableStateOf("Immediate medical countermeasure verified. Crew briefed.") }

  // Pulsing emergency glow animation
  val infiniteTransition = rememberInfiniteTransition(label = "EmergencyPulse")
  val pulseBorderAlpha by infiniteTransition.animateFloat(
    initialValue = 0.4f,
    targetValue = 1f,
    animationSpec = infiniteRepeatable(
      animation = tween(800, easing = FastOutSlowInEasing),
      repeatMode = RepeatMode.Reverse
    ),
    label = "BorderPulse"
  )

  Dialog(onDismissRequest = { /* Modal is mandatory until acknowledged */ }) {
    Surface(
      shape = RoundedCornerShape(20.dp),
      color = Color(0xFF0F0712),
      border = androidx.compose.foundation.BorderStroke(2.dp, CriticalRed.copy(alpha = pulseBorderAlpha)),
      modifier = Modifier
        .fillMaxWidth()
        .testTag("critical_risk_modal")
    ) {
      Column(modifier = Modifier.padding(20.dp)) {
        // Emergency Header with flashing beacon
        Row(
          modifier = Modifier.fillMaxWidth(),
          horizontalArrangement = Arrangement.SpaceBetween,
          verticalAlignment = Alignment.CenterVertically
        ) {
          Row(verticalAlignment = Alignment.CenterVertically) {
            Box(
              modifier = Modifier
                .size(36.dp)
                .clip(CircleShape)
                .background(CriticalRed.copy(alpha = 0.25f))
                .border(1.5.dp, CriticalRed, CircleShape),
              contentAlignment = Alignment.Center
            ) {
              Icon(Icons.Default.CrisisAlert, contentDescription = null, tint = CriticalRed, modifier = Modifier.size(20.dp))
            }
            Spacer(modifier = Modifier.width(10.dp))
            Column {
              Text(
                "CRITICAL RISK ALARM",
                style = MaterialTheme.typography.titleMedium.copy(
                  color = CriticalRed,
                  fontWeight = FontWeight.Bold,
                  letterSpacing = 1.2.sp
                )
              )
              Text(
                "MANDATORY MEDICAL OFFICER ACKNOWLEDGEMENT",
                style = MaterialTheme.typography.labelSmall.copy(color = Color.LightGray, fontSize = 8.sp)
              )
            }
          }

          Box(
            modifier = Modifier
              .clip(RoundedCornerShape(8.dp))
              .background(CriticalRed)
              .padding(horizontal = 8.dp, vertical = 4.dp)
          ) {
            Text(
              "${pendingAlert.compositeScore}/100",
              style = MaterialTheme.typography.labelMedium.copy(
                color = Color.White,
                fontWeight = FontWeight.Bold,
                fontFamily = FontFamily.Monospace
              )
            )
          }
        }

        Spacer(modifier = Modifier.height(14.dp))

        // Astronaut Details Box
        Box(
          modifier = Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(12.dp))
            .background(Color.Black.copy(alpha = 0.45f))
            .border(1.dp, Color.White.copy(alpha = 0.1f), RoundedCornerShape(12.dp))
            .padding(12.dp)
        ) {
          Column {
            Text(
              "AFFECTED CREW MEMBER:",
              style = MaterialTheme.typography.labelSmall.copy(color = Color.Gray, fontSize = 8.sp)
            )
            Spacer(modifier = Modifier.height(2.dp))
            Text(
              "${pendingAlert.astronautName} • ID: ${pendingAlert.astronautId}",
              style = MaterialTheme.typography.titleMedium.copy(color = Color.White, fontWeight = FontWeight.Bold)
            )
            Spacer(modifier = Modifier.height(4.dp))
            Text(
              "Risk Status: ${pendingAlert.riskLevel.label} (Score ${pendingAlert.compositeScore})",
              style = MaterialTheme.typography.bodySmall.copy(color = WarningAmber)
            )
          }
        }

        Spacer(modifier = Modifier.height(10.dp))

        // Action Directive Warning Box
        Box(
          modifier = Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(12.dp))
            .background(CriticalRed.copy(alpha = 0.12f))
            .border(1.dp, CriticalRed.copy(alpha = 0.5f), RoundedCornerShape(12.dp))
            .padding(12.dp)
        ) {
          Row(verticalAlignment = Alignment.Top) {
            Icon(Icons.Default.Warning, contentDescription = null, tint = CriticalRed, modifier = Modifier.size(16.dp).padding(top = 2.dp))
            Spacer(modifier = Modifier.width(8.dp))
            Column {
              Text(
                "IMMEDIATE CLINICAL ACTION DIRECTIVE:",
                style = MaterialTheme.typography.labelSmall.copy(color = CriticalRed, fontWeight = FontWeight.Bold)
              )
              Spacer(modifier = Modifier.height(3.dp))
              Text(
                pendingAlert.clinicalDirective,
                style = MaterialTheme.typography.bodyMedium.copy(color = Color.White, fontWeight = FontWeight.SemiBold)
              )
            }
          }
        }

        Spacer(modifier = Modifier.height(12.dp))

        // Flight Surgeon Sign-off Section
        Text(
          "MEDICAL OFFICER SIGNATURE & BADGE:",
          style = MaterialTheme.typography.labelSmall.copy(color = NeonCyan, fontSize = 8.sp)
        )
        Spacer(modifier = Modifier.height(4.dp))
        OutlinedTextField(
          value = officerSignature,
          onValueChange = { officerSignature = it },
          placeholder = { Text("Enter Flight Surgeon Name / Badge ID", color = Color.Gray) },
          modifier = Modifier.fillMaxWidth().testTag("medical_officer_signature_input"),
          colors = OutlinedTextFieldDefaults.colors(
            focusedBorderColor = NeonCyan,
            unfocusedBorderColor = GlassBorderStroke,
            focusedTextColor = Color.White,
            unfocusedTextColor = Color.White
          ),
          shape = RoundedCornerShape(10.dp)
        )

        Spacer(modifier = Modifier.height(8.dp))

        Text(
          "CLINICAL ACTION & PROTOCOL INITIATED:",
          style = MaterialTheme.typography.labelSmall.copy(color = NeonCyan, fontSize = 8.sp)
        )
        Spacer(modifier = Modifier.height(4.dp))
        OutlinedTextField(
          value = clinicalNotes,
          onValueChange = { clinicalNotes = it },
          placeholder = { Text("Document medical countermeasures taken...", color = Color.Gray) },
          modifier = Modifier.fillMaxWidth().testTag("medical_officer_notes_input"),
          colors = OutlinedTextFieldDefaults.colors(
            focusedBorderColor = NeonCyan,
            unfocusedBorderColor = GlassBorderStroke,
            focusedTextColor = Color.White,
            unfocusedTextColor = Color.White
          ),
          shape = RoundedCornerShape(10.dp)
        )

        Spacer(modifier = Modifier.height(16.dp))

        // Confirm & Acknowledge Button
        Button(
          onClick = {
            if (officerSignature.isNotBlank()) {
              onAcknowledge(pendingAlert.alertId, officerSignature.trim(), clinicalNotes.trim())
            }
          },
          modifier = Modifier
            .fillMaxWidth()
            .height(46.dp)
            .testTag("confirm_medical_acknowledgement_button"),
          colors = ButtonDefaults.buttonColors(containerColor = NominalGreen, contentColor = Color.Black),
          shape = RoundedCornerShape(10.dp)
        ) {
          Icon(Icons.Default.VerifiedUser, contentDescription = null, modifier = Modifier.size(18.dp))
          Spacer(modifier = Modifier.width(8.dp))
          Text(
            "DISPATCH MEDICAL OFFICER SIGN-OFF",
            style = MaterialTheme.typography.labelMedium.copy(fontWeight = FontWeight.Bold, letterSpacing = 0.5.sp)
          )
        }
      }
    }
  }
}
