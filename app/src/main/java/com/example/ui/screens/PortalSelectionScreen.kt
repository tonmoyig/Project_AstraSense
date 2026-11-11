package com.example.ui.screens

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.BoxWithConstraints
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.imePadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import com.example.auth.AstraAuthManager
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowForward
import androidx.compose.material.icons.filled.AdminPanelSettings
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.Group
import androidx.compose.material.icons.filled.Key
import androidx.compose.material.icons.filled.Lock
import androidx.compose.material.icons.filled.Person
import androidx.compose.material.icons.filled.PersonAdd
import androidx.compose.material.icons.filled.RocketLaunch
import androidx.compose.material.icons.filled.Security
import androidx.compose.material.icons.filled.Visibility
import androidx.compose.material.icons.filled.VisibilityOff
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.OutlinedTextFieldDefaults
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
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
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.compose.ui.text.input.VisualTransformation
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.data.AstraPersistenceManager
import com.example.data.AstraRepository
import com.example.model.CrewMember
import com.example.model.UserRole
import com.example.ui.components.GlassCard
import com.example.ui.components.StatusBadge
import com.example.ui.theme.BrightTeal
import com.example.ui.theme.CriticalRed
import com.example.ui.theme.GlassBorderStroke
import com.example.ui.theme.NeonCyan
import com.example.ui.theme.NominalGreen
import com.example.ui.theme.WarningAmber

/**
 * Role-Selection Entry Screen (Landing Gateway).
 * Serves as the mandatory initial landing view for AstraSense on launch.
 * Offers distinct Admin Portal and Crew Portal pathways with dedicated
 * Login, Registration, and primary "ENTER PORTAL" launch triggers.
 */
@Composable
fun PortalSelectionScreen(
  currentUser: CrewMember?,
  allCrewMembers: List<CrewMember> = emptyList(),
  onSelectAdminPortal: (CrewMember) -> Unit,
  onSelectCrewPortal: (CrewMember) -> Unit,
  onOpenSettings: () -> Unit,
  modifier: Modifier = Modifier
) {
  var showAdminLoginDialog by remember { mutableStateOf(false) }
  var showAdminRegisterDialog by remember { mutableStateOf(false) }
  var showCrewLoginDialog by remember { mutableStateOf(false) }
  var showCrewRegisterDialog by remember { mutableStateOf(false) }

  Box(
    modifier = modifier
      .fillMaxSize()
      .background(
        Brush.verticalGradient(
          listOf(
            Color(0xFF02050D),
            Color(0xFF071128),
            Color(0xFF030714)
          )
        )
      )
      .padding(horizontal = 14.dp, vertical = 10.dp)
  ) {
    Column(
      modifier = Modifier
        .fillMaxSize()
        .verticalScroll(rememberScrollState()),
      horizontalAlignment = Alignment.CenterHorizontally,
      verticalArrangement = Arrangement.spacedBy(14.dp)
    ) {
      // 1. Mission Gateway Header & Active Clearance Identifier
      GlassCard(
        modifier = Modifier.fillMaxWidth().testTag("portal_select_header"),
        cornerRadius = 16.dp,
        contentPadding = 12.dp
      ) {
        Row(
          modifier = Modifier.fillMaxWidth(),
          horizontalArrangement = Arrangement.SpaceBetween,
          verticalAlignment = Alignment.CenterVertically
        ) {
          Row(
            verticalAlignment = Alignment.CenterVertically,
            modifier = Modifier.weight(1f, fill = false).padding(end = 8.dp)
          ) {
            Box(
              modifier = Modifier
                .size(38.dp)
                .clip(CircleShape)
                .background(NeonCyan.copy(alpha = 0.2f))
                .border(1.dp, NeonCyan, CircleShape),
              contentAlignment = Alignment.Center
            ) {
              Icon(Icons.Default.RocketLaunch, contentDescription = "AstraSense", tint = NeonCyan, modifier = Modifier.size(20.dp))
            }
            Spacer(modifier = Modifier.width(10.dp))
            Column {
              Text(
                text = "ASTRA SENSE • MISSION GATEWAY",
                style = MaterialTheme.typography.labelSmall.copy(color = BrightTeal, letterSpacing = 1.sp, fontSize = 8.5.sp)
              )
              Text(
                text = currentUser?.fullName ?: "GATEWAY ACCESS GRANTED",
                style = MaterialTheme.typography.titleMedium.copy(color = Color.White, fontWeight = FontWeight.Bold, fontSize = 13.5.sp),
                maxLines = 1,
                overflow = TextOverflow.Ellipsis
              )
            }
          }

          if (currentUser != null) {
            StatusBadge(
              text = "${currentUser.role.name} • ${currentUser.id}",
              color = if (currentUser.role == UserRole.ADMIN) NeonCyan else NominalGreen
            )
          } else {
            StatusBadge(text = "STANDBY GATEWAY", color = BrightTeal)
          }
        }
      }

      // 2. Gateway Title & Directives
      Column(horizontalAlignment = Alignment.CenterHorizontally) {
        Text(
          text = "SELECT PORTAL ACCESS MODE",
          style = MaterialTheme.typography.titleLarge.copy(
            color = Color.White,
            letterSpacing = 1.2.sp,
            fontWeight = FontWeight.Bold,
            fontSize = 17.sp
          ),
          textAlign = TextAlign.Center
        )

        Spacer(modifier = Modifier.height(3.dp))

        Text(
          text = "Choose between fleet-wide command telemetry or individual astronaut monitoring as authorized by NASA mission protocol.",
          style = MaterialTheme.typography.bodySmall.copy(
            color = Color(0xFF94A3B8),
            textAlign = TextAlign.Center,
            fontSize = 10.5.sp,
            lineHeight = 15.sp
          ),
          modifier = Modifier.padding(horizontal = 12.dp)
        )
      }

      // 3. Adaptive Dual Cards: Admin Portal Path vs Crew Portal Path
      BoxWithConstraints(modifier = Modifier.fillMaxWidth()) {
        val isWideScreen = maxWidth >= 680.dp

        if (isWideScreen) {
          Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(14.dp)
          ) {
            Box(modifier = Modifier.weight(1f)) {
              AdminPortalOptionCard(
                currentUser = currentUser,
                onEnterAdmin = {
                  if (currentUser != null && currentUser.role == UserRole.ADMIN) {
                    onSelectAdminPortal(currentUser)
                  } else {
                    showAdminLoginDialog = true
                  }
                },
                onOpenLogin = { showAdminLoginDialog = true },
                onOpenRegister = { showAdminRegisterDialog = true }
              )
            }
            Box(modifier = Modifier.weight(1f)) {
              CrewPortalOptionCard(
                currentUser = currentUser,
                onEnterCrew = {
                  if (currentUser != null && currentUser.role == UserRole.CREW) {
                    onSelectCrewPortal(currentUser)
                  } else {
                    // Quick default to first crew member if available or open crew login
                    val crewCandidate = allCrewMembers.firstOrNull { it.role == UserRole.CREW }
                    if (crewCandidate != null) {
                      onSelectCrewPortal(crewCandidate)
                    } else {
                      showCrewLoginDialog = true
                    }
                  }
                },
                onOpenLogin = { showCrewLoginDialog = true },
                onOpenRegister = { showCrewRegisterDialog = true }
              )
            }
          }
        } else {
          Column(
            modifier = Modifier.fillMaxWidth(),
            verticalArrangement = Arrangement.spacedBy(14.dp)
          ) {
            AdminPortalOptionCard(
              currentUser = currentUser,
              onEnterAdmin = {
                if (currentUser != null && currentUser.role == UserRole.ADMIN) {
                  onSelectAdminPortal(currentUser)
                } else {
                  showAdminLoginDialog = true
                }
              },
              onOpenLogin = { showAdminLoginDialog = true },
              onOpenRegister = { showAdminRegisterDialog = true }
            )
            CrewPortalOptionCard(
              currentUser = currentUser,
              onEnterCrew = {
                if (currentUser != null && currentUser.role == UserRole.CREW) {
                  onSelectCrewPortal(currentUser)
                } else {
                  val crewCandidate = allCrewMembers.firstOrNull { it.role == UserRole.CREW }
                  if (crewCandidate != null) {
                    onSelectCrewPortal(crewCandidate)
                  } else {
                    showCrewLoginDialog = true
                  }
                }
              },
              onOpenLogin = { showCrewLoginDialog = true },
              onOpenRegister = { showCrewRegisterDialog = true }
            )
          }
        }
      }

      Spacer(modifier = Modifier.height(10.dp))
    }
  }

  // =========================================================================
  //                  DIALOGS: ADMIN & CREW AUTHENTICATION
  // =========================================================================

  // 1. Admin Login Dialog
  if (showAdminLoginDialog) {
    AdminLoginDialog(
      onDismiss = { showAdminLoginDialog = false },
      onSuccess = { adminUser ->
        showAdminLoginDialog = false
        onSelectAdminPortal(adminUser)
      }
    )
  }

  // 2. Admin Registration / Onboarding Dialog
  if (showAdminRegisterDialog) {
    AdminRegisterDialog(
      onDismiss = { showAdminRegisterDialog = false },
      onSuccess = { newAdmin ->
        showAdminRegisterDialog = false
        onSelectAdminPortal(newAdmin)
      }
    )
  }

  // 3. Crew Login Dialog
  if (showCrewLoginDialog) {
    CrewLoginDialog(
      allCrewMembers = allCrewMembers,
      onDismiss = { showCrewLoginDialog = false },
      onSuccess = { crewUser ->
        showCrewLoginDialog = false
        onSelectCrewPortal(crewUser)
      }
    )
  }

  // 4. Crew Registration Dialog
  if (showCrewRegisterDialog) {
    CrewRegisterDialog(
      onDismiss = { showCrewRegisterDialog = false },
      onSuccess = { newCrew ->
        showCrewRegisterDialog = false
        onSelectCrewPortal(newCrew)
      }
    )
  }
}

// ===========================================================================
//                     CARD COMPONENT: ADMIN PORTAL
// ===========================================================================

@Composable
private fun AdminPortalOptionCard(
  currentUser: CrewMember?,
  onEnterAdmin: () -> Unit,
  onOpenLogin: () -> Unit,
  onOpenRegister: () -> Unit
) {
  val isAdminActive = currentUser?.role == UserRole.ADMIN

  GlassCard(
    modifier = Modifier
      .fillMaxWidth()
      .testTag("select_admin_portal_card"),
    borderColor = NeonCyan.copy(alpha = 0.7f),
    cornerRadius = 20.dp
  ) {
    Column {
      // Header: Admin Portal Title & Badge
      Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically
      ) {
        Row(
          verticalAlignment = Alignment.CenterVertically,
          modifier = Modifier.weight(1f, fill = false).padding(end = 8.dp)
        ) {
          Box(
            modifier = Modifier
              .size(36.dp)
              .clip(CircleShape)
              .background(NeonCyan.copy(alpha = 0.2f)),
            contentAlignment = Alignment.Center
          ) {
            Icon(Icons.Default.AdminPanelSettings, contentDescription = null, tint = NeonCyan, modifier = Modifier.size(20.dp))
          }
          Spacer(modifier = Modifier.width(10.dp))
          Column {
            Text(
              "ADMIN PORTAL",
              style = MaterialTheme.typography.titleMedium.copy(color = Color.White, letterSpacing = 1.sp, fontWeight = FontWeight.Bold)
            )
            Text(
              "Flight Surgeon & Mission Commander",
              style = MaterialTheme.typography.labelSmall.copy(color = BrightTeal, fontSize = 8.5.sp)
            )
          }
        }

        StatusBadge(text = "FULL FLEET ACCESS", color = NeonCyan)
      }

      Spacer(modifier = Modifier.height(10.dp))

      // Current Status Pill
      Box(
        modifier = Modifier
          .fillMaxWidth()
          .clip(RoundedCornerShape(8.dp))
          .background(if (isAdminActive) NeonCyan.copy(alpha = 0.14f) else Color.Black.copy(alpha = 0.35f))
          .border(1.dp, if (isAdminActive) NeonCyan.copy(alpha = 0.5f) else Color.White.copy(alpha = 0.1f), RoundedCornerShape(8.dp))
          .padding(horizontal = 9.dp, vertical = 6.dp)
      ) {
        Row(verticalAlignment = Alignment.CenterVertically) {
          Icon(
            imageVector = if (isAdminActive) Icons.Default.CheckCircle else Icons.Default.Security,
            contentDescription = null,
            tint = if (isAdminActive) NeonCyan else Color.Gray,
            modifier = Modifier.size(13.dp)
          )
          Spacer(modifier = Modifier.width(6.dp))
          Text(
            text = if (isAdminActive) "ACTIVE CLEARANCE: ${currentUser?.fullName} (ADMIN)" else "ADMIN AUTHENTICATION OR ONBOARDING REQUIRED",
            style = MaterialTheme.typography.labelSmall.copy(
              color = if (isAdminActive) NeonCyan else Color.LightGray,
              fontWeight = FontWeight.Bold,
              fontSize = 8.5.sp
            ),
            maxLines = 1,
            overflow = TextOverflow.Ellipsis
          )
        }
      }

      Spacer(modifier = Modifier.height(10.dp))

      // Feature Capabilities
      Column(verticalArrangement = Arrangement.spacedBy(4.5.dp)) {
        PortalFeatureBullet("• Live biometric telemetry for ALL crew members & personal vitals")
        PortalFeatureBullet("• Real-time module location and current activity tracking")
        PortalFeatureBullet("• Emergency simulation drills (Solar SPE, Pressure drop, High CO₂)")
        PortalFeatureBullet("• Fleet-wide broadcast communications & clinical directives")
        PortalFeatureBullet("• Cabin life-support & environmental override controls")
      }

      Spacer(modifier = Modifier.height(12.dp))

      // Admin Path Options: Login & Registration
      Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.spacedBy(8.dp)
      ) {
        OutlinedButton(
          onClick = onOpenLogin,
          modifier = Modifier.weight(1f).height(36.dp).testTag("admin_login_action_button"),
          border = androidx.compose.foundation.BorderStroke(1.dp, NeonCyan.copy(alpha = 0.6f)),
          shape = RoundedCornerShape(8.dp),
          colors = ButtonDefaults.outlinedButtonColors(contentColor = NeonCyan)
        ) {
          Icon(Icons.Default.Key, contentDescription = null, modifier = Modifier.size(12.dp))
          Spacer(modifier = Modifier.width(4.dp))
          Text("ADMIN LOGIN", style = MaterialTheme.typography.labelSmall.copy(fontWeight = FontWeight.Bold, fontSize = 8.5.sp))
        }

        OutlinedButton(
          onClick = onOpenRegister,
          modifier = Modifier.weight(1f).height(36.dp).testTag("admin_register_action_button"),
          border = androidx.compose.foundation.BorderStroke(1.dp, BrightTeal.copy(alpha = 0.5f)),
          shape = RoundedCornerShape(8.dp),
          colors = ButtonDefaults.outlinedButtonColors(contentColor = BrightTeal)
        ) {
          Icon(Icons.Default.PersonAdd, contentDescription = null, modifier = Modifier.size(12.dp))
          Spacer(modifier = Modifier.width(4.dp))
          Text("ONBOARD ADMIN", style = MaterialTheme.typography.labelSmall.copy(fontWeight = FontWeight.Bold, fontSize = 8.5.sp))
        }
      }

      Spacer(modifier = Modifier.height(10.dp))

      // Primary Action Button: ENTER ADMIN PORTAL
      Button(
        onClick = onEnterAdmin,
        modifier = Modifier.fillMaxWidth().height(42.dp).testTag("launch_admin_portal_button"),
        colors = ButtonDefaults.buttonColors(containerColor = NeonCyan, contentColor = Color.Black),
        shape = RoundedCornerShape(10.dp)
      ) {
        Row(verticalAlignment = Alignment.CenterVertically) {
          Text("ENTER ADMIN PORTAL", style = MaterialTheme.typography.titleSmall.copy(fontWeight = FontWeight.Bold, fontSize = 11.5.sp))
          Spacer(modifier = Modifier.width(6.dp))
          Icon(Icons.AutoMirrored.Filled.ArrowForward, contentDescription = null, modifier = Modifier.size(15.dp))
        }
      }
    }
  }
}

// ===========================================================================
//                     CARD COMPONENT: CREW PORTAL
// ===========================================================================

@Composable
private fun CrewPortalOptionCard(
  currentUser: CrewMember?,
  onEnterCrew: () -> Unit,
  onOpenLogin: () -> Unit,
  onOpenRegister: () -> Unit
) {
  val isCrewActive = currentUser?.role == UserRole.CREW

  GlassCard(
    modifier = Modifier
      .fillMaxWidth()
      .testTag("select_crew_portal_card"),
    borderColor = BrightTeal.copy(alpha = 0.7f),
    cornerRadius = 20.dp
  ) {
    Column {
      // Header: Crew Portal Title & Badge
      Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically
      ) {
        Row(
          verticalAlignment = Alignment.CenterVertically,
          modifier = Modifier.weight(1f, fill = false).padding(end = 8.dp)
        ) {
          Box(
            modifier = Modifier
              .size(36.dp)
              .clip(CircleShape)
              .background(BrightTeal.copy(alpha = 0.2f)),
            contentAlignment = Alignment.Center
          ) {
            Icon(Icons.Default.Person, contentDescription = null, tint = BrightTeal, modifier = Modifier.size(20.dp))
          }
          Spacer(modifier = Modifier.width(10.dp))
          Column {
            Text(
              "CREW PORTAL",
              style = MaterialTheme.typography.titleMedium.copy(color = Color.White, letterSpacing = 1.sp, fontWeight = FontWeight.Bold)
            )
            Text(
              "Individual Astronaut Biometric Suite",
              style = MaterialTheme.typography.labelSmall.copy(color = BrightTeal, fontSize = 8.5.sp)
            )
          }
        }

        StatusBadge(text = "INDIVIDUAL ACCESS", color = BrightTeal)
      }

      Spacer(modifier = Modifier.height(10.dp))

      // Current Status Pill
      Box(
        modifier = Modifier
          .fillMaxWidth()
          .clip(RoundedCornerShape(8.dp))
          .background(if (isCrewActive) BrightTeal.copy(alpha = 0.14f) else Color.Black.copy(alpha = 0.35f))
          .border(1.dp, if (isCrewActive) BrightTeal.copy(alpha = 0.5f) else Color.White.copy(alpha = 0.1f), RoundedCornerShape(8.dp))
          .padding(horizontal = 9.dp, vertical = 6.dp)
      ) {
        Row(verticalAlignment = Alignment.CenterVertically) {
          Icon(
            imageVector = if (isCrewActive) Icons.Default.CheckCircle else Icons.Default.Group,
            contentDescription = null,
            tint = if (isCrewActive) BrightTeal else Color.Gray,
            modifier = Modifier.size(13.dp)
          )
          Spacer(modifier = Modifier.width(6.dp))
          Text(
            text = if (isCrewActive) "ACTIVE CREW: ${currentUser?.fullName} (${currentUser?.missionSpecialty})" else "SELECT CREW PROFILE OR REGISTER ASTRONAUT",
            style = MaterialTheme.typography.labelSmall.copy(
              color = if (isCrewActive) BrightTeal else Color.LightGray,
              fontWeight = FontWeight.Bold,
              fontSize = 8.5.sp
            ),
            maxLines = 1,
            overflow = TextOverflow.Ellipsis
          )
        }
      }

      Spacer(modifier = Modifier.height(10.dp))

      // Feature Capabilities
      Column(verticalArrangement = Arrangement.spacedBy(4.5.dp)) {
        PortalFeatureBullet("• View only own private health telemetry & bio-sensor metrics")
        PortalFeatureBullet("• Two-Layer AI Safety (Layer 1 Instant & Layer 2 Clinical AI)")
        PortalFeatureBullet("• Combined Health & Radiation Risk Score with anatomical body map")
        PortalFeatureBullet("• 30-Second Psychomotor Vigilance Test (PVT cognitive alertness)")
        PortalFeatureBullet("• NASA HRP Multi-Modal Clinical Health Assessment & XP Rewards")
      }

      Spacer(modifier = Modifier.height(12.dp))

      // Crew Path Options: Login & Registration
      Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.spacedBy(8.dp)
      ) {
        OutlinedButton(
          onClick = onOpenLogin,
          modifier = Modifier.weight(1f).height(36.dp).testTag("crew_login_action_button"),
          border = androidx.compose.foundation.BorderStroke(1.dp, BrightTeal.copy(alpha = 0.6f)),
          shape = RoundedCornerShape(8.dp),
          colors = ButtonDefaults.outlinedButtonColors(contentColor = BrightTeal)
        ) {
          Icon(Icons.Default.Group, contentDescription = null, modifier = Modifier.size(12.dp))
          Spacer(modifier = Modifier.width(4.dp))
          Text("CREW LOGIN", style = MaterialTheme.typography.labelSmall.copy(fontWeight = FontWeight.Bold, fontSize = 8.5.sp))
        }

        OutlinedButton(
          onClick = onOpenRegister,
          modifier = Modifier.weight(1f).height(36.dp).testTag("crew_register_action_button"),
          border = androidx.compose.foundation.BorderStroke(1.dp, NominalGreen.copy(alpha = 0.5f)),
          shape = RoundedCornerShape(8.dp),
          colors = ButtonDefaults.outlinedButtonColors(contentColor = NominalGreen)
        ) {
          Icon(Icons.Default.PersonAdd, contentDescription = null, modifier = Modifier.size(12.dp))
          Spacer(modifier = Modifier.width(4.dp))
          Text("REGISTER CREW", style = MaterialTheme.typography.labelSmall.copy(fontWeight = FontWeight.Bold, fontSize = 8.5.sp))
        }
      }

      Spacer(modifier = Modifier.height(10.dp))

      // Primary Action Button: ENTER CREW PORTAL
      Button(
        onClick = onEnterCrew,
        modifier = Modifier.fillMaxWidth().height(42.dp).testTag("launch_crew_portal_button"),
        colors = ButtonDefaults.buttonColors(containerColor = BrightTeal, contentColor = Color.Black),
        shape = RoundedCornerShape(10.dp)
      ) {
        Row(verticalAlignment = Alignment.CenterVertically) {
          Text("ENTER CREW PORTAL", style = MaterialTheme.typography.titleSmall.copy(fontWeight = FontWeight.Bold, fontSize = 11.5.sp))
          Spacer(modifier = Modifier.width(6.dp))
          Icon(Icons.AutoMirrored.Filled.ArrowForward, contentDescription = null, modifier = Modifier.size(15.dp))
        }
      }
    }
  }
}

// ===========================================================================
//                        ADMIN LOGIN DIALOG
// ===========================================================================

@Composable
private fun AdminLoginDialog(
  onDismiss: () -> Unit,
  onSuccess: (CrewMember) -> Unit
) {
  var authMode by remember { mutableStateOf("PASSWORD") } // "PASSWORD" or "OTP"
  var adminId by remember { mutableStateOf("NASA-AS-701") }
  var identifier by remember { mutableStateOf("a.vance@nasa.gov") }
  var password by remember { mutableStateOf("AstraSecure#2026") }
  var passwordVisible by remember { mutableStateOf(false) }
  var otpInput by remember { mutableStateOf("") }
  var otpSent by remember { mutableStateOf(false) }
  var simulatedOtp by remember { mutableStateOf("892147") }
  var errorMessage by remember { mutableStateOf<String?>(null) }

  AlertDialog(
    onDismissRequest = onDismiss,
    confirmButton = {
      Button(
        onClick = {
          if (authMode == "PASSWORD") {
            val res = AstraRepository.authenticateStrictly(identifier, adminId, password)
            res.onSuccess { adminMember ->
              if (adminMember.role == UserRole.ADMIN) {
                onSuccess(adminMember)
              } else {
                errorMessage = "Access Denied: Account '${adminMember.fullName}' does not hold Flight Surgeon Admin clearance."
              }
            }.onFailure { err ->
              errorMessage = err.message ?: "Authentication failed."
            }
          } else {
            // OTP Verification Code Authentication
            if (otpInput.isBlank()) {
              errorMessage = "Please enter the 6-digit verification clearance code."
              return@Button
            }
            if (AstraAuthManager.verifyEmailOtp(identifier, otpInput)) {
              val adminMember = AstraRepository.crewMembers.value.find { it.id.equals(adminId, ignoreCase = true) }
                ?: AstraRepository.crewMembers.value.first()
              AstraRepository.switchUser(adminMember)
              onSuccess(adminMember)
            } else {
              errorMessage = "Invalid verification code. Please check your dispatched OTP or resend."
            }
          }
        },
        colors = ButtonDefaults.buttonColors(containerColor = NeonCyan, contentColor = Color.Black),
        modifier = Modifier.testTag("admin_dialog_login_confirm_button")
      ) {
        Text(
          if (authMode == "PASSWORD") "AUTHENTICATE & ENTER ADMIN PORTAL" else "VERIFY CODE & ENTER ADMIN PORTAL",
          fontWeight = FontWeight.Bold,
          fontSize = 10.sp
        )
      }
    },
    dismissButton = {
      TextButton(onClick = onDismiss) {
        Text("CANCEL", color = Color.Gray, fontSize = 11.sp)
      }
    },
    title = {
      Row(verticalAlignment = Alignment.CenterVertically) {
        Icon(Icons.Default.AdminPanelSettings, contentDescription = null, tint = NeonCyan, modifier = Modifier.size(20.dp))
        Spacer(modifier = Modifier.width(8.dp))
        Text("ADMIN COMMAND ACCESS LOGIN", style = MaterialTheme.typography.titleMedium.copy(color = Color.White, fontWeight = FontWeight.Bold, fontSize = 13.sp))
      }
    },
    text = {
      Column(
        modifier = Modifier
          .fillMaxWidth()
          .heightIn(max = 460.dp)
          .verticalScroll(rememberScrollState())
          .imePadding(),
        verticalArrangement = Arrangement.spacedBy(8.dp)
      ) {
        Text(
          "Enter Flight Surgeon or Commander clearance credentials to access fleet command controls.",
          style = MaterialTheme.typography.bodySmall.copy(color = Color.LightGray, fontSize = 9.5.sp)
        )

        // Auth Method Selector Segmented Control
        Row(
          modifier = Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(8.dp))
            .background(Color.Black.copy(alpha = 0.45f))
            .padding(3.dp),
          horizontalArrangement = Arrangement.spacedBy(4.dp)
        ) {
          Box(
            modifier = Modifier
              .weight(1f)
              .clip(RoundedCornerShape(6.dp))
              .background(if (authMode == "PASSWORD") NeonCyan.copy(alpha = 0.25f) else Color.Transparent)
              .border(1.dp, if (authMode == "PASSWORD") NeonCyan else Color.Transparent, RoundedCornerShape(6.dp))
              .clickable { authMode = "PASSWORD"; errorMessage = null }
              .padding(vertical = 6.dp),
            contentAlignment = Alignment.Center
          ) {
            Text(
              "PASSWORD KEY",
              style = MaterialTheme.typography.labelSmall.copy(
                color = if (authMode == "PASSWORD") NeonCyan else Color.Gray,
                fontWeight = FontWeight.Bold,
                fontSize = 8.5.sp
              )
            )
          }

          Box(
            modifier = Modifier
              .weight(1f)
              .clip(RoundedCornerShape(6.dp))
              .background(if (authMode == "OTP") BrightTeal.copy(alpha = 0.25f) else Color.Transparent)
              .border(1.dp, if (authMode == "OTP") BrightTeal else Color.Transparent, RoundedCornerShape(6.dp))
              .clickable { authMode = "OTP"; errorMessage = null }
              .padding(vertical = 6.dp),
            contentAlignment = Alignment.Center
          ) {
            Text(
              "6-DIGIT VERIFICATION CODE",
              style = MaterialTheme.typography.labelSmall.copy(
                color = if (authMode == "OTP") BrightTeal else Color.Gray,
                fontWeight = FontWeight.Bold,
                fontSize = 8.5.sp
              )
            )
          }
        }

        // Quick Admin pre-fill selector
        Row(horizontalArrangement = Arrangement.spacedBy(6.dp)) {
          Box(
            modifier = Modifier
              .clip(RoundedCornerShape(6.dp))
              .background(if (adminId == "NASA-AS-701") NeonCyan.copy(alpha = 0.25f) else Color.White.copy(alpha = 0.08f))
              .border(1.dp, if (adminId == "NASA-AS-701") NeonCyan else Color.White.copy(alpha = 0.15f), RoundedCornerShape(6.dp))
              .clickable {
                adminId = "NASA-AS-701"
                identifier = "a.vance@nasa.gov"
                password = "AstraSecure#2026"
                errorMessage = null
              }
              .padding(horizontal = 6.dp, vertical = 3.dp)
          ) {
            Text("Dr. Alex Vance", style = MaterialTheme.typography.labelSmall.copy(color = NeonCyan, fontSize = 8.sp))
          }

          Box(
            modifier = Modifier
              .clip(RoundedCornerShape(6.dp))
              .background(if (adminId == "ADMIN-ASTRA-01") NeonCyan.copy(alpha = 0.25f) else Color.White.copy(alpha = 0.08f))
              .border(1.dp, if (adminId == "ADMIN-ASTRA-01") NeonCyan else Color.White.copy(alpha = 0.15f), RoundedCornerShape(6.dp))
              .clickable {
                adminId = "ADMIN-ASTRA-01"
                identifier = "command.astra@nasa.gov"
                password = "FlightSurgeon#1"
                errorMessage = null
              }
              .padding(horizontal = 6.dp, vertical = 3.dp)
          ) {
            Text("Chief Medical Officer", style = MaterialTheme.typography.labelSmall.copy(color = NeonCyan, fontSize = 8.sp))
          }
        }

        OutlinedTextField(
          value = adminId,
          onValueChange = { adminId = it; errorMessage = null },
          label = { Text("Admin Clearance ID", fontSize = 10.sp) },
          singleLine = true,
          modifier = Modifier.fillMaxWidth().testTag("admin_dialog_id_input"),
          colors = OutlinedTextFieldDefaults.colors(
            focusedBorderColor = NeonCyan,
            unfocusedBorderColor = Color.Gray,
            focusedTextColor = Color.White,
            unfocusedTextColor = Color.White
          )
        )

        OutlinedTextField(
          value = identifier,
          onValueChange = { identifier = it; errorMessage = null },
          label = { Text("Identifier / Official Email", fontSize = 10.sp) },
          singleLine = true,
          modifier = Modifier.fillMaxWidth().testTag("admin_dialog_email_input"),
          colors = OutlinedTextFieldDefaults.colors(
            focusedBorderColor = NeonCyan,
            unfocusedBorderColor = Color.Gray,
            focusedTextColor = Color.White,
            unfocusedTextColor = Color.White
          )
        )

        if (authMode == "PASSWORD") {
          OutlinedTextField(
            value = password,
            onValueChange = { password = it; errorMessage = null },
            label = { Text("Passcode / Access Key", fontSize = 10.sp) },
            singleLine = true,
            visualTransformation = if (passwordVisible) VisualTransformation.None else PasswordVisualTransformation(),
            trailingIcon = {
              IconButton(onClick = { passwordVisible = !passwordVisible }) {
                Icon(
                  imageVector = if (passwordVisible) Icons.Default.Visibility else Icons.Default.VisibilityOff,
                  contentDescription = if (passwordVisible) "Hide password" else "Show password",
                  tint = NeonCyan,
                  modifier = Modifier.size(18.dp)
                )
              }
            },
            keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Password),
            modifier = Modifier.fillMaxWidth().testTag("admin_dialog_pass_input"),
            colors = OutlinedTextFieldDefaults.colors(
              focusedBorderColor = NeonCyan,
              unfocusedBorderColor = Color.Gray,
              focusedTextColor = Color.White,
              unfocusedTextColor = Color.White
            )
          )
        } else {
          // 6-Digit Verification Code Section
          Column(verticalArrangement = Arrangement.spacedBy(6.dp)) {
            Button(
              onClick = {
                val code = AstraAuthManager.dispatchEmailOtp(identifier)
                simulatedOtp = code
                otpSent = true
                errorMessage = null
              },
              modifier = Modifier.fillMaxWidth().height(36.dp).testTag("admin_dialog_send_otp_button"),
              colors = ButtonDefaults.buttonColors(containerColor = BrightTeal.copy(alpha = 0.2f), contentColor = BrightTeal),
              border = androidx.compose.foundation.BorderStroke(1.dp, BrightTeal),
              shape = RoundedCornerShape(8.dp)
            ) {
              Text(
                if (otpSent) "RESEND 6-DIGIT CODE" else "SEND 6-DIGIT VERIFICATION CODE",
                style = MaterialTheme.typography.labelSmall.copy(fontWeight = FontWeight.Bold, fontSize = 9.sp)
              )
            }

            if (otpSent) {
              Box(
                modifier = Modifier
                  .fillMaxWidth()
                  .clip(RoundedCornerShape(6.dp))
                  .background(NominalGreen.copy(alpha = 0.15f))
                  .border(1.dp, NominalGreen.copy(alpha = 0.4f), RoundedCornerShape(6.dp))
                  .padding(horizontal = 8.dp, vertical = 4.dp)
              ) {
                Text(
                  "Verification code dispatched to $identifier (Token: $simulatedOtp)",
                  style = MaterialTheme.typography.labelSmall.copy(color = NominalGreen, fontSize = 8.5.sp)
                )
              }
            }

            OutlinedTextField(
              value = otpInput,
              onValueChange = { if (it.length <= 6) otpInput = it; errorMessage = null },
              label = { Text("Enter 6-Digit OTP Clearance Code", fontSize = 10.sp) },
              placeholder = { Text("e.g. $simulatedOtp", color = Color.Gray, fontSize = 10.sp) },
              singleLine = true,
              keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
              modifier = Modifier.fillMaxWidth().testTag("admin_dialog_otp_input"),
              colors = OutlinedTextFieldDefaults.colors(
                focusedBorderColor = BrightTeal,
                unfocusedBorderColor = Color.Gray,
                focusedTextColor = Color.White,
                unfocusedTextColor = Color.White
              )
            )
          }
        }

        errorMessage?.let { err ->
          Text(err, color = CriticalRed, fontSize = 9.sp, fontWeight = FontWeight.SemiBold)
        }
      }
    },
    containerColor = Color(0xFF071328)
  )
}

// ===========================================================================
//                     ADMIN REGISTRATION DIALOG
// ===========================================================================

@Composable
private fun AdminRegisterDialog(
  onDismiss: () -> Unit,
  onSuccess: (CrewMember) -> Unit
) {
  var fullName by remember { mutableStateOf("") }
  var username by remember { mutableStateOf("") }
  var email by remember { mutableStateOf("") }
  var adminClearanceKey by remember { mutableStateOf("ADMIN-ASTRA-01") }
  var selectedGender by remember { mutableStateOf("Male") }
  var ageText by remember { mutableStateOf("42") }
  var password by remember { mutableStateOf("AstraSecure#2026") }
  var passwordVisible by remember { mutableStateOf(false) }
  var otpVerificationInput by remember { mutableStateOf("") }
  var otpSent by remember { mutableStateOf(false) }
  var simulatedOtp by remember { mutableStateOf("892147") }
  var errorMessage by remember { mutableStateOf<String?>(null) }

  AlertDialog(
    onDismissRequest = onDismiss,
    confirmButton = {
      Button(
        onClick = {
          if (fullName.isBlank() || username.isBlank() || email.isBlank() || password.isBlank()) {
            errorMessage = "Please fill in all onboarding fields."
            return@Button
          }
          val ageInt = ageText.toIntOrNull()
          if (ageInt == null || ageInt !in 21..65) {
            errorMessage = "Invalid Astronaut Age: Must be between 21 and 65 years."
            return@Button
          }
          if (!AstraRepository.validateAdminId(adminClearanceKey)) {
            errorMessage = "Clearance Denied: ID '$adminClearanceKey' is not an authorized Admin ID."
            return@Button
          }
          if (otpSent && otpVerificationInput.isNotBlank()) {
            if (!AstraAuthManager.verifyEmailOtp(email, otpVerificationInput)) {
              errorMessage = "Invalid 6-digit verification code. Please re-enter."
              return@Button
            }
          }
          val cred = AstraPersistenceManager.UserCredential(
            id = adminClearanceKey.trim().uppercase(),
            username = username.trim().lowercase(),
            email = email.trim().lowercase(),
            fullName = fullName.trim(),
            passwordHash = password.trim(),
            role = UserRole.ADMIN
          )
          AstraPersistenceManager.registerCredential(cred)
          val newAdmin = AstraRepository.registerCrewMember(
            adminId = cred.id,
            username = cred.username,
            fullName = cred.fullName,
            email = cred.email,
            gender = selectedGender,
            age = ageInt,
            role = UserRole.ADMIN,
            password = cred.passwordHash
          )
          if (newAdmin != null) {
            AstraRepository.switchUser(newAdmin)
            onSuccess(newAdmin)
          } else {
            errorMessage = "Failed to create Admin profile."
          }
        },
        colors = ButtonDefaults.buttonColors(containerColor = NeonCyan, contentColor = Color.Black),
        modifier = Modifier.testTag("admin_dialog_register_confirm_button")
      ) {
        Text("ONBOARD & ENTER ADMIN PORTAL", fontWeight = FontWeight.Bold, fontSize = 10.sp)
      }
    },
    dismissButton = {
      TextButton(onClick = onDismiss) {
        Text("CANCEL", color = Color.Gray, fontSize = 11.sp)
      }
    },
    title = {
      Row(verticalAlignment = Alignment.CenterVertically) {
        Icon(Icons.Default.PersonAdd, contentDescription = null, tint = NeonCyan, modifier = Modifier.size(20.dp))
        Spacer(modifier = Modifier.width(8.dp))
        Text("ADMIN / FLIGHT SURGEON ONBOARDING", style = MaterialTheme.typography.titleMedium.copy(color = Color.White, fontWeight = FontWeight.Bold, fontSize = 13.sp))
      }
    },
    text = {
      Column(
        modifier = Modifier
          .fillMaxWidth()
          .heightIn(max = 480.dp)
          .verticalScroll(rememberScrollState())
          .imePadding(),
        verticalArrangement = Arrangement.spacedBy(8.dp)
      ) {
        Text(
          "Enter authorized NASA mission administrator credentials to register a new commanding officer.",
          style = MaterialTheme.typography.bodySmall.copy(color = Color.LightGray, fontSize = 9.5.sp)
        )

        OutlinedTextField(
          value = fullName,
          onValueChange = { fullName = it; errorMessage = null },
          label = { Text("Full Name (e.g. Dr. Sarah Connor)", fontSize = 10.sp) },
          singleLine = true,
          modifier = Modifier.fillMaxWidth().testTag("admin_reg_name_input"),
          colors = OutlinedTextFieldDefaults.colors(focusedBorderColor = NeonCyan, unfocusedBorderColor = Color.Gray, focusedTextColor = Color.White, unfocusedTextColor = Color.White)
        )

        OutlinedTextField(
          value = username,
          onValueChange = { username = it; errorMessage = null },
          label = { Text("Username", fontSize = 10.sp) },
          singleLine = true,
          modifier = Modifier.fillMaxWidth().testTag("admin_reg_user_input"),
          colors = OutlinedTextFieldDefaults.colors(focusedBorderColor = NeonCyan, unfocusedBorderColor = Color.Gray, focusedTextColor = Color.White, unfocusedTextColor = Color.White)
        )

        OutlinedTextField(
          value = email,
          onValueChange = { email = it; errorMessage = null },
          label = { Text("Official Mission Email", fontSize = 10.sp) },
          singleLine = true,
          modifier = Modifier.fillMaxWidth().testTag("admin_reg_email_input"),
          colors = OutlinedTextFieldDefaults.colors(focusedBorderColor = NeonCyan, unfocusedBorderColor = Color.Gray, focusedTextColor = Color.White, unfocusedTextColor = Color.White)
        )

        OutlinedTextField(
          value = adminClearanceKey,
          onValueChange = { adminClearanceKey = it; errorMessage = null },
          label = { Text("Authorized Clearance Key (e.g. ADMIN-ASTRA-01)", fontSize = 10.sp) },
          singleLine = true,
          modifier = Modifier.fillMaxWidth().testTag("admin_reg_clearance_input"),
          colors = OutlinedTextFieldDefaults.colors(focusedBorderColor = NeonCyan, unfocusedBorderColor = Color.Gray, focusedTextColor = Color.White, unfocusedTextColor = Color.White)
        )

        // Gender Selection Segmented Control
        Text("ASTRONAUT GENDER (FLIGHT MEDICINE PROTOCOL):", style = MaterialTheme.typography.labelSmall.copy(color = Color(0xFFCBD5E1), fontSize = 9.sp))
        Row(
          modifier = Modifier.fillMaxWidth(),
          horizontalArrangement = Arrangement.spacedBy(6.dp)
        ) {
          listOf("Male", "Female", "Other").forEach { g ->
            val isSelected = selectedGender.equals(g, ignoreCase = true)
            Box(
              modifier = Modifier
                .weight(1f)
                .clip(RoundedCornerShape(8.dp))
                .background(if (isSelected) NeonCyan.copy(alpha = 0.22f) else Color.White.copy(alpha = 0.05f))
                .border(1.dp, if (isSelected) NeonCyan else Color.White.copy(alpha = 0.15f), RoundedCornerShape(8.dp))
                .clickable { selectedGender = g }
                .padding(vertical = 7.dp),
              contentAlignment = Alignment.Center
            ) {
              Text(
                g.uppercase(),
                style = MaterialTheme.typography.labelSmall.copy(
                  color = if (isSelected) NeonCyan else Color.LightGray,
                  fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Normal,
                  fontSize = 8.5.sp
                )
              )
            }
          }
        }

        // Age Numeric Input Field with Flight Limits Label
        OutlinedTextField(
          value = ageText,
          onValueChange = { if (it.all { ch -> ch.isDigit() } && it.length <= 3) ageText = it; errorMessage = null },
          label = { Text("Astronaut Age (Operational Flight Limits: 21-65 Yrs)", fontSize = 10.sp) },
          singleLine = true,
          keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
          modifier = Modifier.fillMaxWidth().testTag("admin_reg_age_input"),
          colors = OutlinedTextFieldDefaults.colors(focusedBorderColor = NeonCyan, unfocusedBorderColor = Color.Gray, focusedTextColor = Color.White, unfocusedTextColor = Color.White)
        )

        // Password with interactive eye toggle button
        OutlinedTextField(
          value = password,
          onValueChange = { password = it; errorMessage = null },
          label = { Text("Security Passcode", fontSize = 10.sp) },
          singleLine = true,
          visualTransformation = if (passwordVisible) VisualTransformation.None else PasswordVisualTransformation(),
          trailingIcon = {
            IconButton(onClick = { passwordVisible = !passwordVisible }) {
              Icon(
                imageVector = if (passwordVisible) Icons.Default.Visibility else Icons.Default.VisibilityOff,
                contentDescription = if (passwordVisible) "Hide password" else "Show password",
                tint = NeonCyan,
                modifier = Modifier.size(18.dp)
              )
            }
          },
          keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Password),
          modifier = Modifier.fillMaxWidth().testTag("admin_reg_pass_input"),
          colors = OutlinedTextFieldDefaults.colors(focusedBorderColor = NeonCyan, unfocusedBorderColor = Color.Gray, focusedTextColor = Color.White, unfocusedTextColor = Color.White)
        )

        // Optional 6-digit OTP verification section
        Row(
          modifier = Modifier.fillMaxWidth(),
          horizontalArrangement = Arrangement.SpaceBetween,
          verticalAlignment = Alignment.CenterVertically
        ) {
          Text("2FA OTP VERIFICATION:", style = MaterialTheme.typography.labelSmall.copy(color = Color(0xFFCBD5E1), fontSize = 9.sp))
          TextButton(
            onClick = {
              if (email.isNotBlank()) {
                simulatedOtp = AstraAuthManager.dispatchEmailOtp(email)
                otpSent = true
              } else {
                errorMessage = "Please enter official email first to receive 6-digit OTP."
              }
            }
          ) {
            Text(if (otpSent) "RESEND CODE" else "DISPATCH 6-DIGIT OTP", color = BrightTeal, fontSize = 9.sp)
          }
        }

        if (otpSent) {
          Box(
            modifier = Modifier
              .fillMaxWidth()
              .clip(RoundedCornerShape(6.dp))
              .background(NominalGreen.copy(alpha = 0.15f))
              .border(1.dp, NominalGreen.copy(alpha = 0.4f), RoundedCornerShape(6.dp))
              .padding(horizontal = 8.dp, vertical = 4.dp)
          ) {
            Text(
              "Clearance code dispatched to $email (Token: $simulatedOtp)",
              style = MaterialTheme.typography.labelSmall.copy(color = NominalGreen, fontSize = 8.5.sp)
            )
          }

          OutlinedTextField(
            value = otpVerificationInput,
            onValueChange = { if (it.length <= 6) otpVerificationInput = it; errorMessage = null },
            label = { Text("Enter 6-Digit Code", fontSize = 10.sp) },
            singleLine = true,
            keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
            modifier = Modifier.fillMaxWidth().testTag("admin_reg_otp_input"),
            colors = OutlinedTextFieldDefaults.colors(focusedBorderColor = BrightTeal, unfocusedBorderColor = Color.Gray, focusedTextColor = Color.White, unfocusedTextColor = Color.White)
          )
        }

        errorMessage?.let { err ->
          Text(err, color = CriticalRed, fontSize = 9.sp, fontWeight = FontWeight.SemiBold)
        }
      }
    },
    containerColor = Color(0xFF071328)
  )
}

// ===========================================================================
//                        CREW LOGIN DIALOG
// ===========================================================================

@Composable
private fun CrewLoginDialog(
  allCrewMembers: List<CrewMember>,
  onDismiss: () -> Unit,
  onSuccess: (CrewMember) -> Unit
) {
  val crewCandidates = remember(allCrewMembers) {
    allCrewMembers.filter { it.role == UserRole.CREW }.ifEmpty { allCrewMembers }
  }

  var authMode by remember { mutableStateOf("PASSWORD") } // "PASSWORD" or "OTP"
  var selectedMember by remember { mutableStateOf(crewCandidates.firstOrNull()) }
  var password by remember { mutableStateOf("Rostova#EVA2026") }
  var passwordVisible by remember { mutableStateOf(false) }
  var otpInput by remember { mutableStateOf("") }
  var otpSent by remember { mutableStateOf(false) }
  var simulatedOtp by remember { mutableStateOf("892147") }
  var errorMessage by remember { mutableStateOf<String?>(null) }

  AlertDialog(
    onDismissRequest = onDismiss,
    confirmButton = {
      Button(
        onClick = {
          val member = selectedMember
          if (member == null) {
            errorMessage = "Please select a crew profile."
            return@Button
          }
          if (authMode == "PASSWORD") {
            if (password.isBlank()) {
              errorMessage = "Please enter your astronaut password."
              return@Button
            }
            AstraRepository.switchUser(member)
            onSuccess(member)
          } else {
            // OTP verification
            if (otpInput.isBlank()) {
              errorMessage = "Please enter the 6-digit clearance code."
              return@Button
            }
            if (AstraAuthManager.verifyEmailOtp(member.email, otpInput)) {
              AstraRepository.switchUser(member)
              onSuccess(member)
            } else {
              errorMessage = "Invalid verification code. Please check your dispatched OTP or resend."
            }
          }
        },
        colors = ButtonDefaults.buttonColors(containerColor = BrightTeal, contentColor = Color.Black),
        modifier = Modifier.testTag("crew_dialog_login_confirm_button")
      ) {
        Text(
          if (authMode == "PASSWORD") "AUTHENTICATE & ENTER CREW PORTAL" else "VERIFY CODE & ENTER CREW PORTAL",
          fontWeight = FontWeight.Bold,
          fontSize = 10.sp
        )
      }
    },
    dismissButton = {
      TextButton(onClick = onDismiss) {
        Text("CANCEL", color = Color.Gray, fontSize = 11.sp)
      }
    },
    title = {
      Row(verticalAlignment = Alignment.CenterVertically) {
        Icon(Icons.Default.Group, contentDescription = null, tint = BrightTeal, modifier = Modifier.size(20.dp))
        Spacer(modifier = Modifier.width(8.dp))
        Text("ASTRONAUT CREW LOGIN", style = MaterialTheme.typography.titleMedium.copy(color = Color.White, fontWeight = FontWeight.Bold, fontSize = 13.sp))
      }
    },
    text = {
      Column(
        modifier = Modifier
          .fillMaxWidth()
          .heightIn(max = 460.dp)
          .verticalScroll(rememberScrollState())
          .imePadding(),
        verticalArrangement = Arrangement.spacedBy(8.dp)
      ) {
        Text(
          "Select your astronaut profile from the active flight crew roster:",
          style = MaterialTheme.typography.bodySmall.copy(color = Color.LightGray, fontSize = 9.5.sp)
        )

        // Crew Roster One-Touch Chips
        Column(verticalArrangement = Arrangement.spacedBy(6.dp)) {
          crewCandidates.forEach { member ->
            val isChosen = selectedMember?.id == member.id
            Row(
              modifier = Modifier
                .fillMaxWidth()
                .clip(RoundedCornerShape(8.dp))
                .background(if (isChosen) BrightTeal.copy(alpha = 0.22f) else Color.White.copy(alpha = 0.05f))
                .border(1.dp, if (isChosen) BrightTeal else Color.White.copy(alpha = 0.12f), RoundedCornerShape(8.dp))
                .clickable {
                  selectedMember = member
                  errorMessage = null
                  password = if (member.id == "NASA-AS-702") "Rostova#EVA2026" else if (member.id == "NASA-AS-703") "OrbitalPass#99" else "AstraCadet#2026"
                }
                .padding(horizontal = 10.dp, vertical = 6.dp),
              horizontalArrangement = Arrangement.SpaceBetween,
              verticalAlignment = Alignment.CenterVertically
            ) {
              Column {
                Text(member.fullName, style = MaterialTheme.typography.bodyMedium.copy(color = Color.White, fontWeight = FontWeight.Bold, fontSize = 11.sp))
                Text("${member.id} • ${member.missionSpecialty}", style = MaterialTheme.typography.labelSmall.copy(color = BrightTeal, fontSize = 8.5.sp))
              }
              if (isChosen) {
                Icon(Icons.Default.CheckCircle, contentDescription = null, tint = BrightTeal, modifier = Modifier.size(15.dp))
              }
            }
          }
        }

        Spacer(modifier = Modifier.height(2.dp))

        // Auth Method Toggle (Password vs 6-Digit Verification Code)
        Row(
          modifier = Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(8.dp))
            .background(Color.Black.copy(alpha = 0.45f))
            .padding(3.dp),
          horizontalArrangement = Arrangement.spacedBy(4.dp)
        ) {
          Box(
            modifier = Modifier
              .weight(1f)
              .clip(RoundedCornerShape(6.dp))
              .background(if (authMode == "PASSWORD") BrightTeal.copy(alpha = 0.25f) else Color.Transparent)
              .border(1.dp, if (authMode == "PASSWORD") BrightTeal else Color.Transparent, RoundedCornerShape(6.dp))
              .clickable { authMode = "PASSWORD"; errorMessage = null }
              .padding(vertical = 6.dp),
            contentAlignment = Alignment.Center
          ) {
            Text(
              "ASTRONAUT PASSWORD",
              style = MaterialTheme.typography.labelSmall.copy(
                color = if (authMode == "PASSWORD") BrightTeal else Color.Gray,
                fontWeight = FontWeight.Bold,
                fontSize = 8.5.sp
              )
            )
          }

          Box(
            modifier = Modifier
              .weight(1f)
              .clip(RoundedCornerShape(6.dp))
              .background(if (authMode == "OTP") NeonCyan.copy(alpha = 0.25f) else Color.Transparent)
              .border(1.dp, if (authMode == "OTP") NeonCyan else Color.Transparent, RoundedCornerShape(6.dp))
              .clickable { authMode = "OTP"; errorMessage = null }
              .padding(vertical = 6.dp),
            contentAlignment = Alignment.Center
          ) {
            Text(
              "6-DIGIT VERIFICATION CODE",
              style = MaterialTheme.typography.labelSmall.copy(
                color = if (authMode == "OTP") NeonCyan else Color.Gray,
                fontWeight = FontWeight.Bold,
                fontSize = 8.5.sp
              )
            )
          }
        }

        if (authMode == "PASSWORD") {
          // Secure Password Field with interactive eye toggle button
          OutlinedTextField(
            value = password,
            onValueChange = { password = it; errorMessage = null },
            label = { Text("Astronaut Security Passcode", fontSize = 10.sp) },
            singleLine = true,
            visualTransformation = if (passwordVisible) VisualTransformation.None else PasswordVisualTransformation(),
            trailingIcon = {
              IconButton(onClick = { passwordVisible = !passwordVisible }) {
                Icon(
                  imageVector = if (passwordVisible) Icons.Default.Visibility else Icons.Default.VisibilityOff,
                  contentDescription = if (passwordVisible) "Hide password" else "Show password",
                  tint = BrightTeal,
                  modifier = Modifier.size(18.dp)
                )
              }
            },
            keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Password),
            modifier = Modifier.fillMaxWidth().testTag("crew_dialog_pass_input"),
            colors = OutlinedTextFieldDefaults.colors(
              focusedBorderColor = BrightTeal,
              unfocusedBorderColor = Color.Gray,
              focusedTextColor = Color.White,
              unfocusedTextColor = Color.White
            )
          )
        } else {
          // 6-Digit OTP Option
          Column(verticalArrangement = Arrangement.spacedBy(6.dp)) {
            val astronautEmail = selectedMember?.email ?: "astronaut@nasa.gov"
            Button(
              onClick = {
                val code = AstraAuthManager.dispatchEmailOtp(astronautEmail)
                simulatedOtp = code
                otpSent = true
                errorMessage = null
              },
              modifier = Modifier.fillMaxWidth().height(36.dp).testTag("crew_dialog_send_otp_button"),
              colors = ButtonDefaults.buttonColors(containerColor = BrightTeal.copy(alpha = 0.2f), contentColor = BrightTeal),
              border = androidx.compose.foundation.BorderStroke(1.dp, BrightTeal),
              shape = RoundedCornerShape(8.dp)
            ) {
              Text(
                if (otpSent) "RESEND 6-DIGIT CODE" else "SEND 6-DIGIT VERIFICATION CODE",
                style = MaterialTheme.typography.labelSmall.copy(fontWeight = FontWeight.Bold, fontSize = 9.sp)
              )
            }

            if (otpSent) {
              Box(
                modifier = Modifier
                  .fillMaxWidth()
                  .clip(RoundedCornerShape(6.dp))
                  .background(NominalGreen.copy(alpha = 0.15f))
                  .border(1.dp, NominalGreen.copy(alpha = 0.4f), RoundedCornerShape(6.dp))
                  .padding(horizontal = 8.dp, vertical = 4.dp)
              ) {
                Text(
                  "Verification OTP dispatched to $astronautEmail (Token: $simulatedOtp)",
                  style = MaterialTheme.typography.labelSmall.copy(color = NominalGreen, fontSize = 8.5.sp)
                )
              }
            }

            OutlinedTextField(
              value = otpInput,
              onValueChange = { if (it.length <= 6) otpInput = it; errorMessage = null },
              label = { Text("Enter 6-Digit OTP Code", fontSize = 10.sp) },
              placeholder = { Text("e.g. $simulatedOtp", color = Color.Gray, fontSize = 10.sp) },
              singleLine = true,
              keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
              modifier = Modifier.fillMaxWidth().testTag("crew_dialog_otp_input"),
              colors = OutlinedTextFieldDefaults.colors(
                focusedBorderColor = BrightTeal,
                unfocusedBorderColor = Color.Gray,
                focusedTextColor = Color.White,
                unfocusedTextColor = Color.White
              )
            )
          }
        }

        errorMessage?.let { err ->
          Text(err, color = CriticalRed, fontSize = 9.sp, fontWeight = FontWeight.SemiBold)
        }
      }
    },
    containerColor = Color(0xFF071328)
  )
}

// ===========================================================================
//                     CREW REGISTRATION DIALOG
// ===========================================================================

@Composable
private fun CrewRegisterDialog(
  onDismiss: () -> Unit,
  onSuccess: (CrewMember) -> Unit
) {
  var fullName by remember { mutableStateOf("") }
  var username by remember { mutableStateOf("") }
  var email by remember { mutableStateOf("") }
  var crewClearanceId by remember { mutableStateOf("NASA-AS-704") }
  var specialty by remember { mutableStateOf("EVA Specialist") }
  var selectedGender by remember { mutableStateOf("Female") }
  var ageText by remember { mutableStateOf("32") }
  var password by remember { mutableStateOf("AstraSecure#2026") }
  var passwordVisible by remember { mutableStateOf(false) }
  var otpVerificationInput by remember { mutableStateOf("") }
  var otpSent by remember { mutableStateOf(false) }
  var simulatedOtp by remember { mutableStateOf("892147") }
  var errorMessage by remember { mutableStateOf<String?>(null) }

  AlertDialog(
    onDismissRequest = onDismiss,
    confirmButton = {
      Button(
        onClick = {
          if (fullName.isBlank() || username.isBlank() || email.isBlank()) {
            errorMessage = "Please enter astronaut name and identifiers."
            return@Button
          }
          val ageInt = ageText.toIntOrNull()
          if (ageInt == null || ageInt !in 21..65) {
            errorMessage = "Invalid Astronaut Age: Must be between 21 and 65 years."
            return@Button
          }
          if (otpSent && otpVerificationInput.isNotBlank()) {
            if (!AstraAuthManager.verifyEmailOtp(email, otpVerificationInput)) {
              errorMessage = "Invalid 6-digit verification code. Please check code or resend."
              return@Button
            }
          }
          val cred = AstraPersistenceManager.UserCredential(
            id = crewClearanceId.trim().uppercase(),
            username = username.trim().lowercase(),
            email = email.trim().lowercase(),
            fullName = fullName.trim(),
            passwordHash = password.trim(),
            role = UserRole.CREW
          )
          AstraPersistenceManager.registerCredential(cred)
          val newCrew = AstraRepository.registerCrewMember(
            adminId = cred.id,
            username = cred.username,
            fullName = cred.fullName,
            email = cred.email,
            gender = selectedGender,
            age = ageInt,
            role = UserRole.CREW,
            password = cred.passwordHash
          )
          if (newCrew != null) {
            val updated = newCrew.copy(missionSpecialty = specialty)
            AstraRepository.updateCrewMember(updated)
            AstraRepository.switchUser(updated)
            onSuccess(updated)
          } else {
            errorMessage = "Registration clearance rejected. Authorized ID required."
          }
        },
        colors = ButtonDefaults.buttonColors(containerColor = BrightTeal, contentColor = Color.Black),
        modifier = Modifier.testTag("crew_dialog_register_confirm_button")
      ) {
        Text("REGISTER & ENTER CREW PORTAL", fontWeight = FontWeight.Bold, fontSize = 10.sp)
      }
    },
    dismissButton = {
      TextButton(onClick = onDismiss) {
        Text("CANCEL", color = Color.Gray, fontSize = 11.sp)
      }
    },
    title = {
      Row(verticalAlignment = Alignment.CenterVertically) {
        Icon(Icons.Default.PersonAdd, contentDescription = null, tint = BrightTeal, modifier = Modifier.size(20.dp))
        Spacer(modifier = Modifier.width(8.dp))
        Text("NEW ASTRONAUT CREW REGISTRATION", style = MaterialTheme.typography.titleMedium.copy(color = Color.White, fontWeight = FontWeight.Bold, fontSize = 13.sp))
      }
    },
    text = {
      Column(
        modifier = Modifier
          .fillMaxWidth()
          .heightIn(max = 480.dp)
          .verticalScroll(rememberScrollState())
          .imePadding(),
        verticalArrangement = Arrangement.spacedBy(8.dp)
      ) {
        Text(
          "Onboard a new mission flight specialist into the deep-space biometric monitoring suite.",
          style = MaterialTheme.typography.bodySmall.copy(color = Color.LightGray, fontSize = 9.5.sp)
        )

        OutlinedTextField(
          value = fullName,
          onValueChange = { fullName = it; errorMessage = null },
          label = { Text("Astronaut Full Name", fontSize = 10.sp) },
          singleLine = true,
          modifier = Modifier.fillMaxWidth().testTag("crew_reg_name_input"),
          colors = OutlinedTextFieldDefaults.colors(focusedBorderColor = BrightTeal, unfocusedBorderColor = Color.Gray, focusedTextColor = Color.White, unfocusedTextColor = Color.White)
        )

        OutlinedTextField(
          value = specialty,
          onValueChange = { specialty = it; errorMessage = null },
          label = { Text("Mission Role / Specialty (e.g. Flight Engineer)", fontSize = 10.sp) },
          singleLine = true,
          modifier = Modifier.fillMaxWidth().testTag("crew_reg_specialty_input"),
          colors = OutlinedTextFieldDefaults.colors(focusedBorderColor = BrightTeal, unfocusedBorderColor = Color.Gray, focusedTextColor = Color.White, unfocusedTextColor = Color.White)
        )

        OutlinedTextField(
          value = username,
          onValueChange = { username = it; errorMessage = null },
          label = { Text("Crew Username", fontSize = 10.sp) },
          singleLine = true,
          modifier = Modifier.fillMaxWidth().testTag("crew_reg_user_input"),
          colors = OutlinedTextFieldDefaults.colors(focusedBorderColor = BrightTeal, unfocusedBorderColor = Color.Gray, focusedTextColor = Color.White, unfocusedTextColor = Color.White)
        )

        OutlinedTextField(
          value = email,
          onValueChange = { email = it; errorMessage = null },
          label = { Text("Crew Email (e.g. name@nasa.gov)", fontSize = 10.sp) },
          singleLine = true,
          modifier = Modifier.fillMaxWidth().testTag("crew_reg_email_input"),
          colors = OutlinedTextFieldDefaults.colors(focusedBorderColor = BrightTeal, unfocusedBorderColor = Color.Gray, focusedTextColor = Color.White, unfocusedTextColor = Color.White)
        )

        OutlinedTextField(
          value = crewClearanceId,
          onValueChange = { crewClearanceId = it; errorMessage = null },
          label = { Text("Authorized Clearance ID (NASA-AS-701 to 704)", fontSize = 10.sp) },
          singleLine = true,
          modifier = Modifier.fillMaxWidth().testTag("crew_reg_id_input"),
          colors = OutlinedTextFieldDefaults.colors(focusedBorderColor = BrightTeal, unfocusedBorderColor = Color.Gray, focusedTextColor = Color.White, unfocusedTextColor = Color.White)
        )

        // Gender Selection Segmented Selector
        Text("ASTRONAUT GENDER (FLIGHT MEDICINE PROTOCOL):", style = MaterialTheme.typography.labelSmall.copy(color = Color(0xFFCBD5E1), fontSize = 9.sp))
        Row(
          modifier = Modifier.fillMaxWidth(),
          horizontalArrangement = Arrangement.spacedBy(6.dp)
        ) {
          listOf("Female", "Male", "Other").forEach { g ->
            val isSelected = selectedGender.equals(g, ignoreCase = true)
            Box(
              modifier = Modifier
                .weight(1f)
                .clip(RoundedCornerShape(8.dp))
                .background(if (isSelected) BrightTeal.copy(alpha = 0.22f) else Color.White.copy(alpha = 0.05f))
                .border(1.dp, if (isSelected) BrightTeal else Color.White.copy(alpha = 0.15f), RoundedCornerShape(8.dp))
                .clickable { selectedGender = g }
                .padding(vertical = 7.dp),
              contentAlignment = Alignment.Center
            ) {
              Text(
                g.uppercase(),
                style = MaterialTheme.typography.labelSmall.copy(
                  color = if (isSelected) BrightTeal else Color.LightGray,
                  fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Normal,
                  fontSize = 8.5.sp
                )
              )
            }
          }
        }

        // Age Numeric Input Field with Flight Limits Label
        OutlinedTextField(
          value = ageText,
          onValueChange = { if (it.all { ch -> ch.isDigit() } && it.length <= 3) ageText = it; errorMessage = null },
          label = { Text("Astronaut Age (Operational Flight Limits: 21-65 Yrs)", fontSize = 10.sp) },
          singleLine = true,
          keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
          modifier = Modifier.fillMaxWidth().testTag("crew_reg_age_input"),
          colors = OutlinedTextFieldDefaults.colors(focusedBorderColor = BrightTeal, unfocusedBorderColor = Color.Gray, focusedTextColor = Color.White, unfocusedTextColor = Color.White)
        )

        // Secure Password Field with interactive eye toggle button
        OutlinedTextField(
          value = password,
          onValueChange = { password = it; errorMessage = null },
          label = { Text("Create Mission Security Password", fontSize = 10.sp) },
          singleLine = true,
          visualTransformation = if (passwordVisible) VisualTransformation.None else PasswordVisualTransformation(),
          trailingIcon = {
            IconButton(onClick = { passwordVisible = !passwordVisible }) {
              Icon(
                imageVector = if (passwordVisible) Icons.Default.Visibility else Icons.Default.VisibilityOff,
                contentDescription = if (passwordVisible) "Hide password" else "Show password",
                tint = BrightTeal,
                modifier = Modifier.size(18.dp)
              )
            }
          },
          keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Password),
          modifier = Modifier.fillMaxWidth().testTag("crew_reg_pass_input"),
          colors = OutlinedTextFieldDefaults.colors(focusedBorderColor = BrightTeal, unfocusedBorderColor = Color.Gray, focusedTextColor = Color.White, unfocusedTextColor = Color.White)
        )

        // 6-digit verification code option
        Row(
          modifier = Modifier.fillMaxWidth(),
          horizontalArrangement = Arrangement.SpaceBetween,
          verticalAlignment = Alignment.CenterVertically
        ) {
          Text("2FA OTP VERIFICATION:", style = MaterialTheme.typography.labelSmall.copy(color = Color(0xFFCBD5E1), fontSize = 9.sp))
          TextButton(
            onClick = {
              if (email.isNotBlank()) {
                simulatedOtp = AstraAuthManager.dispatchEmailOtp(email)
                otpSent = true
              } else {
                errorMessage = "Please enter crew email first to receive 6-digit OTP."
              }
            }
          ) {
            Text(if (otpSent) "RESEND CODE" else "DISPATCH 6-DIGIT OTP", color = BrightTeal, fontSize = 9.sp)
          }
        }

        if (otpSent) {
          Box(
            modifier = Modifier
              .fillMaxWidth()
              .clip(RoundedCornerShape(6.dp))
              .background(NominalGreen.copy(alpha = 0.15f))
              .border(1.dp, NominalGreen.copy(alpha = 0.4f), RoundedCornerShape(6.dp))
              .padding(horizontal = 8.dp, vertical = 4.dp)
          ) {
            Text(
              "Clearance code dispatched to $email (Token: $simulatedOtp)",
              style = MaterialTheme.typography.labelSmall.copy(color = NominalGreen, fontSize = 8.5.sp)
            )
          }

          OutlinedTextField(
            value = otpVerificationInput,
            onValueChange = { if (it.length <= 6) otpVerificationInput = it; errorMessage = null },
            label = { Text("Enter 6-Digit Code", fontSize = 10.sp) },
            singleLine = true,
            keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
            modifier = Modifier.fillMaxWidth().testTag("crew_reg_otp_input"),
            colors = OutlinedTextFieldDefaults.colors(focusedBorderColor = BrightTeal, unfocusedBorderColor = Color.Gray, focusedTextColor = Color.White, unfocusedTextColor = Color.White)
          )
        }

        errorMessage?.let { err ->
          Text(err, color = CriticalRed, fontSize = 9.sp, fontWeight = FontWeight.SemiBold)
        }
      }
    },
    containerColor = Color(0xFF071328)
  )
}

@Composable
private fun PortalFeatureBullet(text: String) {
  Text(
    text = text,
    style = MaterialTheme.typography.bodySmall.copy(color = Color(0xFFCBD5E1), fontSize = 10.sp, lineHeight = 14.sp)
  )
}
