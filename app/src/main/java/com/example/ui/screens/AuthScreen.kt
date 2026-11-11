package com.example.ui.screens

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
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Badge
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.Email
import androidx.compose.material.icons.filled.Fingerprint
import androidx.compose.material.icons.filled.Key
import androidx.compose.material.icons.filled.Lock
import androidx.compose.material.icons.filled.Person
import androidx.compose.material.icons.filled.RocketLaunch
import androidx.compose.material.icons.filled.Security
import androidx.compose.material.icons.filled.Visibility
import androidx.compose.material.icons.filled.VisibilityOff
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
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
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.compose.ui.text.input.VisualTransformation
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.data.AstraRepository
import com.example.model.CrewMember
import com.example.model.UserRole
import com.example.ui.components.GlassCard
import com.example.ui.components.StatusBadge
import com.example.ui.theme.BrightTeal
import com.example.ui.theme.CelestialAmber
import com.example.ui.theme.CriticalRed
import com.example.ui.theme.GlassBorderStroke
import com.example.ui.theme.NeonCyan
import com.example.ui.theme.NominalGreen
import com.example.ui.components.BiometricAuthModal
import com.example.ui.theme.StarlightBlue

import android.app.Activity
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.ui.platform.LocalContext
import androidx.credentials.CredentialManager
import com.example.auth.AstraAuthManager
import com.example.data.FirestoreService
import kotlinx.coroutines.launch

@Composable
fun AuthScreen(
  onAuthenticated: (CrewMember) -> Unit,
  modifier: Modifier = Modifier
) {
  val context = LocalContext.current
  val coroutineScope = rememberCoroutineScope()
  val credentialManager = remember { CredentialManager.create(context) }
  val isAuthenticating by AstraAuthManager.isAuthenticating.collectAsState()
  val lastAuthError by AstraAuthManager.lastAuthError.collectAsState()

  var isRegistering by remember { mutableStateOf(false) }

  // ---------------- LOGIN FORM STATES ----------------
  var loginAuthMethod by remember { mutableStateOf("PASSWORD") } // "PASSWORD" or "OTP"
  var loginEmailOrUsername by remember { mutableStateOf("a.vance@nasa.gov") }
  var loginAdminId by remember { mutableStateOf("NASA-AS-701") }
  var loginPassword by remember { mutableStateOf("AstraSecure#2026") }
  var isLoginPasswordVisible by remember { mutableStateOf(false) }
  var loginOtpInput by remember { mutableStateOf("") }
  var loginOtpSent by remember { mutableStateOf(false) }
  var simulatedLoginOtp by remember { mutableStateOf("892147") }
  var loginErrorMessage by remember { mutableStateOf<String?>(null) }
  var showBiometricModal by remember { mutableStateOf(false) }

  // ---------------- REGISTRATION FORM STATES ----------------
  var regFullName by remember { mutableStateOf("Mark Watney Jr.") }
  var regUsername by remember { mutableStateOf("watney_jr") }
  var regEmail by remember { mutableStateOf("m.watney@nasa.gov") }
  var regAdminId by remember { mutableStateOf("NASA-AS-703") }
  var regPassword by remember { mutableStateOf("OrbitalPass#99") }
  var isRegPasswordVisible by remember { mutableStateOf(false) }
  var regGender by remember { mutableStateOf("Male") }
  var regAge by remember { mutableStateOf("39") }
  var regRole by remember { mutableStateOf(UserRole.CREW) }
  var regOtpInput by remember { mutableStateOf("") }
  var regOtpSent by remember { mutableStateOf(false) }
  var simulatedOtpCode by remember { mutableStateOf("7492") }
  var regErrorMessage by remember { mutableStateOf<String?>(null) }

  // Subtle animated star twinkle
  val infiniteTransition = rememberInfiniteTransition(label = "stars")
  val starTwinkle by infiniteTransition.animateFloat(
    initialValue = 0.3f,
    targetValue = 1.0f,
    animationSpec = infiniteRepeatable(
      animation = tween(2400, easing = LinearEasing),
      repeatMode = RepeatMode.Reverse
    ),
    label = "starTwinkle"
  )

  Box(
    modifier = modifier
      .fillMaxSize()
      .background(
        Brush.verticalGradient(
          listOf(
            Color(0xFF02050D),
            Color(0xFF070F24),
            Color(0xFF030713)
          )
        )
      )
  ) {
    // Background Starlight Particles Canvas
    Canvas(modifier = Modifier.fillMaxSize()) {
      val stars = listOf(
        Pair(0.15f, 0.12f), Pair(0.85f, 0.18f), Pair(0.32f, 0.28f),
        Pair(0.72f, 0.42f), Pair(0.12f, 0.65f), Pair(0.88f, 0.75f),
        Pair(0.48f, 0.85f), Pair(0.22f, 0.92f), Pair(0.78f, 0.94f)
      )
      stars.forEach { (fx, fy) ->
        val center = Offset(size.width * fx, size.height * fy)
        drawCircle(
          color = Color(0xFF38BDF8).copy(alpha = starTwinkle * 0.4f),
          radius = 2.5f,
          center = center
        )
        drawCircle(
          color = Color.White.copy(alpha = starTwinkle * 0.7f),
          radius = 1.2f,
          center = center
        )
      }
    }

    // Centered Mission Container Card
    Box(
      modifier = Modifier
        .fillMaxSize()
        .padding(horizontal = 16.dp, vertical = 20.dp),
      contentAlignment = Alignment.Center
    ) {
      GlassCard(
        modifier = Modifier
          .fillMaxWidth()
          .verticalScroll(rememberScrollState())
          .testTag("auth_screen_card"),
        cornerRadius = 24.dp
      ) {
        Column(
          modifier = Modifier.fillMaxWidth(),
          horizontalAlignment = Alignment.CenterHorizontally
        ) {
          // Top NASA Telemetry Security Tag
          Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
          ) {
            StatusBadge(text = "NASA STD-3001 • CLEARANCE SECURE", color = BrightTeal)
            Row(verticalAlignment = Alignment.CenterVertically) {
              Icon(Icons.Default.Lock, contentDescription = null, tint = NominalGreen, modifier = Modifier.size(12.dp))
              Spacer(modifier = Modifier.width(4.dp))
              Text("AES-256", style = MaterialTheme.typography.labelSmall.copy(color = NominalGreen, fontSize = 9.sp))
            }
          }

          Spacer(modifier = Modifier.height(14.dp))

          // AstraSense Telemetry Emblem
          Box(
            modifier = Modifier
              .size(54.dp)
              .clip(CircleShape)
              .background(NeonCyan.copy(alpha = 0.18f))
              .border(1.5.dp, NeonCyan, CircleShape),
            contentAlignment = Alignment.Center
          ) {
            Icon(
              imageVector = Icons.Default.RocketLaunch,
              contentDescription = "AstraSense Insignia",
              tint = NeonCyan,
              modifier = Modifier.size(28.dp)
            )
          }

          Spacer(modifier = Modifier.height(8.dp))

          Text(
            text = "A S T R A S E N S E",
            style = MaterialTheme.typography.headlineMedium.copy(
              color = Color.White,
              letterSpacing = 2.5.sp,
              fontWeight = FontWeight.Black
            )
          )

          Spacer(modifier = Modifier.height(2.dp))

          Text(
            text = "DEEP SPACE BIOMETRICS & MISSION AI TELEMETRY",
            style = MaterialTheme.typography.labelSmall.copy(
              color = BrightTeal,
              letterSpacing = 1.2.sp,
              fontSize = 8.5.sp
            )
          )

          Spacer(modifier = Modifier.height(18.dp))

          // Glassmorphic Tab Switcher: Login vs Crew Registration
          Row(
            modifier = Modifier
              .fillMaxWidth()
              .clip(RoundedCornerShape(12.dp))
              .background(Color.Black.copy(alpha = 0.5f))
              .border(1.dp, GlassBorderStroke.copy(alpha = 0.6f), RoundedCornerShape(12.dp))
              .padding(4.dp)
          ) {
            Box(
              modifier = Modifier
                .weight(1f)
                .clip(RoundedCornerShape(9.dp))
                .background(if (!isRegistering) NeonCyan.copy(alpha = 0.25f) else Color.Transparent)
                .clickable {
                  isRegistering = false
                  loginErrorMessage = null
                }
                .padding(vertical = 10.dp),
              contentAlignment = Alignment.Center
            ) {
              Row(verticalAlignment = Alignment.CenterVertically) {
                Icon(
                  imageVector = Icons.Default.Security,
                  contentDescription = null,
                  tint = if (!isRegistering) NeonCyan else Color.Gray,
                  modifier = Modifier.size(14.dp)
                )
                Spacer(modifier = Modifier.width(6.dp))
                Text(
                  "CREW LOGIN",
                  style = MaterialTheme.typography.labelMedium.copy(
                    color = if (!isRegistering) NeonCyan else Color.Gray,
                    fontWeight = if (!isRegistering) FontWeight.Bold else FontWeight.Normal
                  )
                )
              }
            }

            Box(
              modifier = Modifier
                .weight(1f)
                .clip(RoundedCornerShape(9.dp))
                .background(if (isRegistering) NeonCyan.copy(alpha = 0.25f) else Color.Transparent)
                .clickable {
                  isRegistering = true
                  regErrorMessage = null
                }
                .padding(vertical = 10.dp),
              contentAlignment = Alignment.Center
            ) {
              Row(verticalAlignment = Alignment.CenterVertically) {
                Icon(
                  imageVector = Icons.Default.Person,
                  contentDescription = null,
                  tint = if (isRegistering) NeonCyan else Color.Gray,
                  modifier = Modifier.size(14.dp)
                )
                Spacer(modifier = Modifier.width(6.dp))
                Text(
                  "CREW REGISTRATION",
                  style = MaterialTheme.typography.labelMedium.copy(
                    color = if (isRegistering) NeonCyan else Color.Gray,
                    fontWeight = if (isRegistering) FontWeight.Bold else FontWeight.Normal
                  )
                )
              }
            }
          }

          Spacer(modifier = Modifier.height(18.dp))

          if (!isRegistering) {
            // =================================================================
            //                         LOGIN VIEW
            // =================================================================
            Column(modifier = Modifier.fillMaxWidth()) {
              // 1. Email / Username Field
              Text(
                "OFFICIAL MISSION EMAIL OR USERNAME",
                style = MaterialTheme.typography.labelSmall.copy(color = Color(0xFFCBD5E1), fontSize = 9.sp)
              )
              Spacer(modifier = Modifier.height(5.dp))
              OutlinedTextField(
                value = loginEmailOrUsername,
                onValueChange = { loginEmailOrUsername = it },
                placeholder = { Text("e.g. alex.vance@nasa.gov or alex_vance", color = Color.Gray, fontSize = 12.sp) },
                leadingIcon = {
                  Icon(Icons.Default.Email, contentDescription = null, tint = NeonCyan, modifier = Modifier.size(18.dp))
                },
                modifier = Modifier
                  .fillMaxWidth()
                  .testTag("login_email_input"),
                colors = OutlinedTextFieldDefaults.colors(
                  focusedBorderColor = NeonCyan,
                  unfocusedBorderColor = GlassBorderStroke,
                  focusedTextColor = Color.White,
                  unfocusedTextColor = Color.White
                ),
                shape = RoundedCornerShape(12.dp)
              )

              Spacer(modifier = Modifier.height(12.dp))

              // 2. Admin-Provided User ID Field
              Text(
                "ADMIN-PROVIDED USER ID",
                style = MaterialTheme.typography.labelSmall.copy(color = Color(0xFFCBD5E1), fontSize = 9.sp)
              )
              Spacer(modifier = Modifier.height(5.dp))
              OutlinedTextField(
                value = loginAdminId,
                onValueChange = { loginAdminId = it.uppercase() },
                placeholder = { Text("e.g. NASA-AS-701 or ADMIN-ASTRA-01", color = Color.Gray, fontSize = 12.sp) },
                leadingIcon = {
                  Icon(Icons.Default.Badge, contentDescription = null, tint = BrightTeal, modifier = Modifier.size(18.dp))
                },
                modifier = Modifier
                  .fillMaxWidth()
                  .testTag("login_admin_id_input"),
                colors = OutlinedTextFieldDefaults.colors(
                  focusedBorderColor = BrightTeal,
                  unfocusedBorderColor = GlassBorderStroke,
                  focusedTextColor = Color.White,
                  unfocusedTextColor = Color.White
                ),
                shape = RoundedCornerShape(12.dp)
              )

              // Auth Mode Selector: Password vs 6-Digit Verification Code
              Row(
                modifier = Modifier
                  .fillMaxWidth()
                  .clip(RoundedCornerShape(10.dp))
                  .background(Color.Black.copy(alpha = 0.45f))
                  .padding(3.dp),
                horizontalArrangement = Arrangement.spacedBy(4.dp)
              ) {
                Box(
                  modifier = Modifier
                    .weight(1f)
                    .clip(RoundedCornerShape(8.dp))
                    .background(if (loginAuthMethod == "PASSWORD") NeonCyan.copy(alpha = 0.25f) else Color.Transparent)
                    .border(1.dp, if (loginAuthMethod == "PASSWORD") NeonCyan else Color.Transparent, RoundedCornerShape(8.dp))
                    .clickable { loginAuthMethod = "PASSWORD"; loginErrorMessage = null }
                    .padding(vertical = 8.dp),
                  contentAlignment = Alignment.Center
                ) {
                  Text(
                    "MISSION PASSWORD",
                    style = MaterialTheme.typography.labelSmall.copy(
                      color = if (loginAuthMethod == "PASSWORD") NeonCyan else Color.Gray,
                      fontWeight = FontWeight.Bold,
                      fontSize = 9.sp
                    )
                  )
                }

                Box(
                  modifier = Modifier
                    .weight(1f)
                    .clip(RoundedCornerShape(8.dp))
                    .background(if (loginAuthMethod == "OTP") BrightTeal.copy(alpha = 0.25f) else Color.Transparent)
                    .border(1.dp, if (loginAuthMethod == "OTP") BrightTeal else Color.Transparent, RoundedCornerShape(8.dp))
                    .clickable { loginAuthMethod = "OTP"; loginErrorMessage = null }
                    .padding(vertical = 8.dp),
                  contentAlignment = Alignment.Center
                ) {
                  Text(
                    "6-DIGIT VERIFICATION CODE",
                    style = MaterialTheme.typography.labelSmall.copy(
                      color = if (loginAuthMethod == "OTP") BrightTeal else Color.Gray,
                      fontWeight = FontWeight.Bold,
                      fontSize = 9.sp
                    )
                  )
                }
              }

              Spacer(modifier = Modifier.height(12.dp))

              if (loginAuthMethod == "PASSWORD") {
                // 3. Security Password Field with interactive eye toggle
                Text(
                  "ENCRYPTED ACCESS KEY / PASSWORD",
                  style = MaterialTheme.typography.labelSmall.copy(color = Color(0xFFCBD5E1), fontSize = 9.sp)
                )
                Spacer(modifier = Modifier.height(5.dp))
                OutlinedTextField(
                  value = loginPassword,
                  onValueChange = { loginPassword = it; loginErrorMessage = null },
                  placeholder = { Text("Enter mission security key", color = Color.Gray, fontSize = 12.sp) },
                  leadingIcon = {
                    Icon(Icons.Default.Lock, contentDescription = null, tint = NeonCyan, modifier = Modifier.size(18.dp))
                  },
                  trailingIcon = {
                    IconButton(onClick = { isLoginPasswordVisible = !isLoginPasswordVisible }) {
                      Icon(
                        imageVector = if (isLoginPasswordVisible) Icons.Default.Visibility else Icons.Default.VisibilityOff,
                        contentDescription = if (isLoginPasswordVisible) "Hide password" else "Show password",
                        tint = NeonCyan,
                        modifier = Modifier.size(18.dp)
                      )
                    }
                  },
                  visualTransformation = if (isLoginPasswordVisible) VisualTransformation.None else PasswordVisualTransformation(),
                  keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Password),
                  modifier = Modifier
                    .fillMaxWidth()
                    .testTag("login_password_input"),
                  colors = OutlinedTextFieldDefaults.colors(
                    focusedBorderColor = NeonCyan,
                    unfocusedBorderColor = GlassBorderStroke,
                    focusedTextColor = Color.White,
                    unfocusedTextColor = Color.White
                  ),
                  shape = RoundedCornerShape(12.dp)
                )
              } else {
                // 6-Digit OTP Verification Option
                Column(verticalArrangement = Arrangement.spacedBy(6.dp)) {
                  Button(
                    onClick = {
                      val code = AstraAuthManager.dispatchEmailOtp(loginEmailOrUsername)
                      simulatedLoginOtp = code
                      loginOtpSent = true
                      loginErrorMessage = null
                    },
                    modifier = Modifier
                      .fillMaxWidth()
                      .height(38.dp)
                      .testTag("login_send_otp_button"),
                    colors = ButtonDefaults.buttonColors(containerColor = BrightTeal.copy(alpha = 0.2f), contentColor = BrightTeal),
                    border = androidx.compose.foundation.BorderStroke(1.dp, BrightTeal),
                    shape = RoundedCornerShape(10.dp)
                  ) {
                    Text(
                      if (loginOtpSent) "RESEND 6-DIGIT CODE" else "SEND 6-DIGIT VERIFICATION CODE",
                      style = MaterialTheme.typography.labelSmall.copy(fontWeight = FontWeight.Bold, fontSize = 9.5.sp)
                    )
                  }

                  if (loginOtpSent) {
                    Box(
                      modifier = Modifier
                        .fillMaxWidth()
                        .clip(RoundedCornerShape(8.dp))
                        .background(NominalGreen.copy(alpha = 0.15f))
                        .border(1.dp, NominalGreen.copy(alpha = 0.45f), RoundedCornerShape(8.dp))
                        .padding(horizontal = 10.dp, vertical = 6.dp)
                    ) {
                      Text(
                        "Clearance code dispatched to $loginEmailOrUsername (Token: $simulatedLoginOtp)",
                        style = MaterialTheme.typography.labelSmall.copy(color = NominalGreen, fontSize = 9.sp)
                      )
                    }
                  }

                  OutlinedTextField(
                    value = loginOtpInput,
                    onValueChange = { if (it.length <= 6) loginOtpInput = it; loginErrorMessage = null },
                    label = { Text("Enter 6-Digit Clearance OTP", color = Color.Gray, fontSize = 10.sp) },
                    placeholder = { Text("e.g. $simulatedLoginOtp", color = Color.Gray, fontSize = 12.sp) },
                    singleLine = true,
                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                    modifier = Modifier
                      .fillMaxWidth()
                      .testTag("login_otp_input"),
                    colors = OutlinedTextFieldDefaults.colors(
                      focusedBorderColor = BrightTeal,
                      unfocusedBorderColor = GlassBorderStroke,
                      focusedTextColor = Color.White,
                      unfocusedTextColor = Color.White
                    ),
                    shape = RoundedCornerShape(12.dp)
                  )
                }
              }

              // Error notification if authentication fails
              (loginErrorMessage ?: lastAuthError)?.let { err ->
                Spacer(modifier = Modifier.height(8.dp))
                Text(
                  text = err,
                  style = MaterialTheme.typography.bodySmall.copy(color = CriticalRed)
                )
              }

              Spacer(modifier = Modifier.height(18.dp))

              // Google Sign-In with Jetpack Credential Manager (Mission SSO)
              Button(
                onClick = {
                  val activity = context as? Activity
                  if (activity != null) {
                    AstraAuthManager.signInWithGoogle(
                      activity = activity,
                      credentialManager = credentialManager,
                      onAuthSuccess = onAuthenticated,
                      onAuthError = { err -> loginErrorMessage = err },
                      scope = coroutineScope
                    )
                  } else {
                    loginErrorMessage = "System context error: Activity host unavailable."
                  }
                },
                modifier = Modifier
                  .fillMaxWidth()
                  .height(48.dp)
                  .testTag("google_sign_in_button"),
                colors = ButtonDefaults.buttonColors(
                  containerColor = Color.White,
                  contentColor = Color.Black
                ),
                shape = RoundedCornerShape(12.dp),
                enabled = !isAuthenticating
              ) {
                if (isAuthenticating) {
                  CircularProgressIndicator(
                    modifier = Modifier.size(20.dp),
                    color = Color.Black,
                    strokeWidth = 2.dp
                  )
                  Spacer(modifier = Modifier.width(8.dp))
                  Text(
                    "AUTHENTICATING MISSION SSO...",
                    style = MaterialTheme.typography.labelMedium.copy(fontWeight = FontWeight.Bold)
                  )
                } else {
                  Row(verticalAlignment = Alignment.CenterVertically) {
                    Icon(
                      imageVector = Icons.Default.Security,
                      contentDescription = "Google SSO Clearance",
                      modifier = Modifier.size(18.dp),
                      tint = Color(0xFF1E293B)
                    )
                    Spacer(modifier = Modifier.width(8.dp))
                    Text(
                      "SIGN IN WITH GOOGLE / MISSION SSO",
                      style = MaterialTheme.typography.titleSmall.copy(fontWeight = FontWeight.Bold, color = Color(0xFF0F172A))
                    )
                  }
                }
              }

              Spacer(modifier = Modifier.height(10.dp))

              // Authenticate Mission Access Primary Button (Astronaut Manifest credentials)
              Button(
                onClick = {
                  if (loginAuthMethod == "PASSWORD") {
                    val result = AstraRepository.authenticateStrictly(
                      identifier = loginEmailOrUsername,
                      adminId = loginAdminId,
                      password = loginPassword
                    )
                    result.onSuccess { user ->
                      loginErrorMessage = null
                      coroutineScope.launch {
                        try {
                          FirestoreService(context).saveUserProfile(user)
                        } catch (_: Exception) {}
                      }
                      onAuthenticated(user)
                    }.onFailure { err ->
                      loginErrorMessage = err.message ?: "Authentication Denied: Invalid credentials or password."
                    }
                  } else {
                    // OTP Authentication
                    if (loginOtpInput.isBlank()) {
                      loginErrorMessage = "Please enter the 6-digit clearance code."
                      return@Button
                    }
                    if (AstraAuthManager.verifyEmailOtp(loginEmailOrUsername, loginOtpInput)) {
                      loginErrorMessage = null
                      val matched = AstraRepository.crewMembers.value.find {
                        it.id.equals(loginAdminId, ignoreCase = true) || it.email.equals(loginEmailOrUsername, ignoreCase = true)
                      } ?: AstraRepository.crewMembers.value.first()
                      AstraRepository.switchUser(matched)
                      onAuthenticated(matched)
                    } else {
                      loginErrorMessage = "Invalid 6-digit verification code. Please check your dispatched code or resend."
                    }
                  }
                },
                modifier = Modifier
                  .fillMaxWidth()
                  .height(48.dp)
                  .testTag("login_submit_button"),
                colors = ButtonDefaults.buttonColors(containerColor = NeonCyan, contentColor = Color.Black),
                shape = RoundedCornerShape(12.dp)
              ) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                  Icon(Icons.Default.Badge, contentDescription = null, modifier = Modifier.size(18.dp))
                  Spacer(modifier = Modifier.width(8.dp))
                  Text(
                    if (loginAuthMethod == "PASSWORD") "AUTHENTICATE MISSION ACCESS" else "VERIFY CODE & ENTER PORTAL",
                    style = MaterialTheme.typography.titleSmall.copy(fontWeight = FontWeight.Bold)
                  )
                }
              }

              Spacer(modifier = Modifier.height(10.dp))

              // Active Biometric Flight Sensor & Bio-Harness Login
              Button(
                onClick = {
                  showBiometricModal = true
                },
                modifier = Modifier
                  .fillMaxWidth()
                  .height(42.dp)
                  .testTag("biometric_login_button"),
                colors = ButtonDefaults.buttonColors(
                  containerColor = BrightTeal.copy(alpha = 0.15f),
                  contentColor = BrightTeal
                ),
                border = androidx.compose.foundation.BorderStroke(1.dp, BrightTeal.copy(alpha = 0.4f)),
                shape = RoundedCornerShape(12.dp)
              ) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                  Icon(Icons.Default.Fingerprint, contentDescription = null, tint = BrightTeal, modifier = Modifier.size(18.dp))
                  Spacer(modifier = Modifier.width(8.dp))
                  Text(
                    "SCAN BIO-HARNESS TELEMETRY ID",
                    style = MaterialTheme.typography.labelSmall.copy(fontWeight = FontWeight.SemiBold)
                  )
                }
              }

              Spacer(modifier = Modifier.height(16.dp))

              // Flight Roster Quick Presets
              Text(
                "QUICK MISSION ROSTER PRESETS (TAP TO AUTO-FILL):",
                style = MaterialTheme.typography.labelSmall.copy(color = Color.Gray, fontSize = 8.5.sp)
              )
              Spacer(modifier = Modifier.height(8.dp))

              Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                Box(
                  modifier = Modifier
                    .weight(1f)
                    .clip(RoundedCornerShape(8.dp))
                    .background(Color.Black.copy(alpha = 0.35f))
                    .border(1.dp, NeonCyan.copy(alpha = 0.45f), RoundedCornerShape(8.dp))
                    .clickable {
                      loginEmailOrUsername = "a.vance@nasa.gov"
                      loginAdminId = "NASA-AS-701"
                      loginErrorMessage = null
                    }
                    .padding(8.dp)
                ) {
                  Column {
                    Text("DR. ALEX VANCE", style = MaterialTheme.typography.labelSmall.copy(color = NeonCyan))
                    Text("ID: NASA-AS-701 (ADMIN)", style = MaterialTheme.typography.labelSmall.copy(color = Color.LightGray, fontSize = 7.5.sp))
                  }
                }

                Box(
                  modifier = Modifier
                    .weight(1f)
                    .clip(RoundedCornerShape(8.dp))
                    .background(Color.Black.copy(alpha = 0.35f))
                    .border(1.dp, BrightTeal.copy(alpha = 0.45f), RoundedCornerShape(8.dp))
                    .clickable {
                      loginEmailOrUsername = "e.rostova@esa.int"
                      loginAdminId = "NASA-AS-702"
                      loginErrorMessage = null
                    }
                    .padding(8.dp)
                ) {
                  Column {
                    Text("DR. ELENA ROSTOVA", style = MaterialTheme.typography.labelSmall.copy(color = BrightTeal))
                    Text("ID: NASA-AS-702 (CREW)", style = MaterialTheme.typography.labelSmall.copy(color = Color.LightGray, fontSize = 7.5.sp))
                  }
                }
              }
            }
          } else {
            // =================================================================
            //                     CREW REGISTRATION VIEW
            // =================================================================
            Column(modifier = Modifier.fillMaxWidth()) {
              // 1. Admin-Provided User ID (Mandatory verification)
              Text(
                "ADMIN-PROVIDED USER ID (REQUIRED BY MISSION PROTOCOL)",
                style = MaterialTheme.typography.labelSmall.copy(color = Color(0xFFCBD5E1), fontSize = 9.sp)
              )
              Spacer(modifier = Modifier.height(5.dp))
              OutlinedTextField(
                value = regAdminId,
                onValueChange = { regAdminId = it.uppercase() },
                placeholder = { Text("e.g. NASA-AS-703, NASA-AS-704, or ADMIN-ASTRA-01", color = Color.Gray, fontSize = 12.sp) },
                leadingIcon = {
                  Icon(Icons.Default.Badge, contentDescription = null, tint = NeonCyan, modifier = Modifier.size(18.dp))
                },
                modifier = Modifier
                  .fillMaxWidth()
                  .testTag("reg_admin_id_input"),
                colors = OutlinedTextFieldDefaults.colors(
                  focusedBorderColor = NeonCyan,
                  unfocusedBorderColor = GlassBorderStroke,
                  focusedTextColor = Color.White,
                  unfocusedTextColor = Color.White
                ),
                shape = RoundedCornerShape(12.dp)
              )
              Text(
                "Only pre-authorized Admin user IDs issued by Flight Command will be accepted.",
                style = MaterialTheme.typography.labelSmall.copy(color = BrightTeal, fontSize = 8.5.sp),
                modifier = Modifier.padding(top = 2.dp)
              )

              Spacer(modifier = Modifier.height(10.dp))

              // 2. Full Name & Username in symmetric row
              Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                OutlinedTextField(
                  value = regFullName,
                  onValueChange = { regFullName = it },
                  label = { Text("Full Name", color = Color.Gray, fontSize = 10.sp) },
                  modifier = Modifier
                    .weight(1f)
                    .testTag("reg_full_name_input"),
                  colors = OutlinedTextFieldDefaults.colors(
                    focusedBorderColor = NeonCyan,
                    unfocusedBorderColor = GlassBorderStroke,
                    focusedTextColor = Color.White,
                    unfocusedTextColor = Color.White
                  ),
                  shape = RoundedCornerShape(12.dp)
                )
                OutlinedTextField(
                  value = regUsername,
                  onValueChange = { regUsername = it },
                  label = { Text("Username", color = Color.Gray, fontSize = 10.sp) },
                  modifier = Modifier
                    .weight(1f)
                    .testTag("reg_username_input"),
                  colors = OutlinedTextFieldDefaults.colors(
                    focusedBorderColor = NeonCyan,
                    unfocusedBorderColor = GlassBorderStroke,
                    focusedTextColor = Color.White,
                    unfocusedTextColor = Color.White
                  ),
                  shape = RoundedCornerShape(12.dp)
                )
              }

              Spacer(modifier = Modifier.height(10.dp))

              // 3. Official Email Address
              OutlinedTextField(
                value = regEmail,
                onValueChange = { regEmail = it },
                label = { Text("Official Mission Email Address (for 2FA OTP)", color = Color.Gray, fontSize = 10.sp) },
                leadingIcon = {
                  Icon(Icons.Default.Email, contentDescription = null, tint = BrightTeal, modifier = Modifier.size(18.dp))
                },
                modifier = Modifier
                  .fillMaxWidth()
                  .testTag("reg_email_input"),
                colors = OutlinedTextFieldDefaults.colors(
                  focusedBorderColor = BrightTeal,
                  unfocusedBorderColor = GlassBorderStroke,
                  focusedTextColor = Color.White,
                  unfocusedTextColor = Color.White
                ),
                shape = RoundedCornerShape(12.dp)
              )

              Spacer(modifier = Modifier.height(10.dp))

              // 4. Password with Show/Hide toggle
              OutlinedTextField(
                value = regPassword,
                onValueChange = { regPassword = it },
                label = { Text("Create Mission Security Password", color = Color.Gray, fontSize = 10.sp) },
                leadingIcon = {
                  Icon(Icons.Default.Lock, contentDescription = null, tint = NeonCyan, modifier = Modifier.size(18.dp))
                },
                trailingIcon = {
                  IconButton(onClick = { isRegPasswordVisible = !isRegPasswordVisible }) {
                    Icon(
                      imageVector = if (isRegPasswordVisible) Icons.Default.Visibility else Icons.Default.VisibilityOff,
                      contentDescription = null,
                      tint = Color.Gray,
                      modifier = Modifier.size(18.dp)
                    )
                  }
                },
                visualTransformation = if (isRegPasswordVisible) VisualTransformation.None else PasswordVisualTransformation(),
                modifier = Modifier
                  .fillMaxWidth()
                  .testTag("reg_password_input"),
                colors = OutlinedTextFieldDefaults.colors(
                  focusedBorderColor = NeonCyan,
                  unfocusedBorderColor = GlassBorderStroke,
                  focusedTextColor = Color.White,
                  unfocusedTextColor = Color.White
                ),
                shape = RoundedCornerShape(12.dp)
              )

              Spacer(modifier = Modifier.height(10.dp))

              // 5. Gender Selection (Segmented Cybernetic Selector)
              Text(
                "ASTRONAUT GENDER (FLIGHT MEDICINE PROTOCOL):",
                style = MaterialTheme.typography.labelSmall.copy(color = Color(0xFFCBD5E1), fontSize = 9.sp)
              )
              Spacer(modifier = Modifier.height(4.dp))
              Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(6.dp)
              ) {
                listOf("Male", "Female", "Non-Binary").forEach { g ->
                  val isSelected = regGender.equals(g, ignoreCase = true)
                  Box(
                    modifier = Modifier
                      .weight(1f)
                      .clip(RoundedCornerShape(8.dp))
                      .background(if (isSelected) NeonCyan.copy(alpha = 0.22f) else Color.White.copy(alpha = 0.05f))
                      .border(1.dp, if (isSelected) NeonCyan else Color.White.copy(alpha = 0.15f), RoundedCornerShape(8.dp))
                      .clickable { regGender = g }
                      .padding(vertical = 8.dp),
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

              Spacer(modifier = Modifier.height(10.dp))

              // Age Numeric Input Field with Flight Limits Label
              Text(
                "ASTRONAUT AGE (OPERATIONAL FLIGHT LIMITS: 21-65 YRS):",
                style = MaterialTheme.typography.labelSmall.copy(color = Color(0xFFCBD5E1), fontSize = 9.sp)
              )
              Spacer(modifier = Modifier.height(4.dp))
              OutlinedTextField(
                value = regAge,
                onValueChange = { if (it.all { ch -> ch.isDigit() } && it.length <= 3) regAge = it; regErrorMessage = null },
                placeholder = { Text("e.g. 35", color = Color.Gray, fontSize = 12.sp) },
                singleLine = true,
                keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                modifier = Modifier
                  .fillMaxWidth()
                  .testTag("reg_age_input"),
                colors = OutlinedTextFieldDefaults.colors(
                  focusedBorderColor = NeonCyan,
                  unfocusedBorderColor = GlassBorderStroke,
                  focusedTextColor = Color.White,
                  unfocusedTextColor = Color.White
                ),
                shape = RoundedCornerShape(12.dp)
              )

              Spacer(modifier = Modifier.height(12.dp))

              // 6. Role Selection (Admin vs Crew)
              Text("PORTAL CLEARANCE ROLE:", style = MaterialTheme.typography.labelSmall.copy(color = Color(0xFFCBD5E1), fontSize = 9.sp))
              Spacer(modifier = Modifier.height(5.dp))
              Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                Box(
                  modifier = Modifier
                    .weight(1f)
                    .clip(RoundedCornerShape(10.dp))
                    .background(if (regRole == UserRole.ADMIN) NeonCyan.copy(alpha = 0.25f) else Color.White.copy(alpha = 0.05f))
                    .border(1.dp, if (regRole == UserRole.ADMIN) NeonCyan else Color.White.copy(alpha = 0.15f), RoundedCornerShape(10.dp))
                    .clickable { regRole = UserRole.ADMIN }
                    .padding(10.dp),
                  contentAlignment = Alignment.Center
                ) {
                  Text(
                    "ADMIN (FLEET COMMAND)",
                    style = MaterialTheme.typography.labelSmall.copy(
                      color = if (regRole == UserRole.ADMIN) NeonCyan else Color.LightGray,
                      fontWeight = if (regRole == UserRole.ADMIN) FontWeight.Bold else FontWeight.Normal,
                      fontSize = 9.sp
                    )
                  )
                }
                Box(
                  modifier = Modifier
                    .weight(1f)
                    .clip(RoundedCornerShape(10.dp))
                    .background(if (regRole == UserRole.CREW) BrightTeal.copy(alpha = 0.25f) else Color.White.copy(alpha = 0.05f))
                    .border(1.dp, if (regRole == UserRole.CREW) BrightTeal else Color.White.copy(alpha = 0.15f), RoundedCornerShape(10.dp))
                    .clickable { regRole = UserRole.CREW }
                    .padding(10.dp),
                  contentAlignment = Alignment.Center
                ) {
                  Text(
                    "CREW (INDIVIDUAL SUITE)",
                    style = MaterialTheme.typography.labelSmall.copy(
                      color = if (regRole == UserRole.CREW) BrightTeal else Color.LightGray,
                      fontWeight = if (regRole == UserRole.CREW) FontWeight.Bold else FontWeight.Normal,
                      fontSize = 9.sp
                    )
                  )
                }
              }

              Spacer(modifier = Modifier.height(14.dp))

              // 7. Email Two-Factor OTP Verification Card
              Box(
                modifier = Modifier
                  .fillMaxWidth()
                  .clip(RoundedCornerShape(12.dp))
                  .background(Color.Black.copy(alpha = 0.45f))
                  .border(1.dp, GlassBorderStroke, RoundedCornerShape(12.dp))
                  .padding(12.dp)
              ) {
                Column {
                  Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                  ) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                      Icon(Icons.Default.Security, contentDescription = null, tint = BrightTeal, modifier = Modifier.size(16.dp))
                      Spacer(modifier = Modifier.width(6.dp))
                      Text("EMAIL TWO-FACTOR OTP", style = MaterialTheme.typography.labelSmall.copy(color = BrightTeal))
                    }
                    TextButton(
                      onClick = {
                        val code = AstraAuthManager.dispatchEmailOtp(regEmail)
                        simulatedOtpCode = code
                        regOtpSent = true
                      }
                    ) {
                      Text(if (regOtpSent) "RESEND CODE" else "DISPATCH OTP", color = NeonCyan, fontSize = 10.sp)
                    }
                  }

                  if (regOtpSent) {
                    Text(
                      "Secure verification OTP dispatched to $regEmail (Mission Token: $simulatedOtpCode)",
                      style = MaterialTheme.typography.bodySmall.copy(color = NominalGreen, fontSize = 10.sp)
                    )
                    Spacer(modifier = Modifier.height(8.dp))
                    Row(
                      modifier = Modifier.fillMaxWidth(),
                      verticalAlignment = Alignment.CenterVertically,
                      horizontalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                      OutlinedTextField(
                        value = regOtpInput,
                        onValueChange = { regOtpInput = it },
                        placeholder = { Text("4-digit code", color = Color.Gray, fontSize = 11.sp) },
                        modifier = Modifier
                          .weight(1f)
                          .testTag("reg_otp_input"),
                        colors = OutlinedTextFieldDefaults.colors(
                          focusedBorderColor = NeonCyan,
                          unfocusedBorderColor = GlassBorderStroke,
                          focusedTextColor = Color.White,
                          unfocusedTextColor = Color.White
                        ),
                        shape = RoundedCornerShape(8.dp)
                      )
                      Button(
                        onClick = { regOtpInput = simulatedOtpCode },
                        colors = ButtonDefaults.buttonColors(containerColor = BrightTeal, contentColor = Color.Black),
                        shape = RoundedCornerShape(8.dp)
                      ) {
                        Text("AUTO-FILL", style = MaterialTheme.typography.labelSmall)
                      }
                    }
                  }
                }
              }

              regErrorMessage?.let { err ->
                Spacer(modifier = Modifier.height(8.dp))
                Text(
                  text = err,
                  style = MaterialTheme.typography.bodySmall.copy(color = CriticalRed)
                )
              }

              Spacer(modifier = Modifier.height(16.dp))

              // Complete Crew Registration Button
              Button(
                onClick = {
                  if (!AstraRepository.validateAdminId(regAdminId)) {
                    regErrorMessage = "ID Validation Error: Only existing user ID given by admins will be accepted. (Valid: NASA-AS-701, NASA-AS-702, NASA-AS-703, NASA-AS-704, ADMIN-ASTRA-01)"
                    return@Button
                  }
                  if (regOtpSent && !AstraAuthManager.verifyEmailOtp(regEmail, regOtpInput)) {
                    regErrorMessage = "Invalid OTP verification code. Please input code '$simulatedOtpCode'."
                    return@Button
                  }

                  val ageInt = regAge.toIntOrNull()
                  if (ageInt == null || ageInt !in 21..65) {
                    regErrorMessage = "Invalid Astronaut Age: Must be between 21 and 65 operational flight years."
                    return@Button
                  }
                  val newCrew = AstraRepository.registerCrewMember(
                    adminId = regAdminId,
                    username = regUsername,
                    fullName = regFullName,
                    email = regEmail,
                    gender = regGender,
                    age = ageInt,
                    role = regRole,
                    password = regPassword
                  )

                  if (newCrew != null) {
                    coroutineScope.launch {
                      try {
                        FirestoreService(context).saveUserProfile(newCrew)
                      } catch (e: Exception) {
                        // Offline or local fallback
                      }
                    }
                    onAuthenticated(newCrew)
                  } else {
                    regErrorMessage = "Registration failed: Could not authorize mission ID."
                  }
                },
                modifier = Modifier
                  .fillMaxWidth()
                  .height(48.dp)
                  .testTag("reg_submit_button"),
                colors = ButtonDefaults.buttonColors(containerColor = NeonCyan, contentColor = Color.Black),
                shape = RoundedCornerShape(12.dp)
              ) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                  Icon(Icons.Default.CheckCircle, contentDescription = null, modifier = Modifier.size(18.dp))
                  Spacer(modifier = Modifier.width(8.dp))
                  Text(
                    "COMPLETE CREW REGISTRATION",
                    style = MaterialTheme.typography.titleSmall.copy(fontWeight = FontWeight.Bold)
                  )
                }
              }
            }
          }
        }
      }
    }

    // Active Biometric Verification Modal
    if (showBiometricModal) {
      val candidate = AstraRepository.crewMembers.value.find {
        it.id.equals(loginAdminId, ignoreCase = true) || it.email.equals(loginEmailOrUsername, ignoreCase = true)
      } ?: AstraRepository.crewMembers.value.first()

      BiometricAuthModal(
        candidateCrew = candidate,
        onDismiss = { showBiometricModal = false },
        onAuthSuccess = { authedCrew ->
          showBiometricModal = false
          onAuthenticated(authedCrew)
        }
      )
    }
  }
}
