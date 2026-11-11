package com.example

import android.Manifest
import android.content.Context
import android.content.Intent
import android.os.Build
import android.os.Bundle
import android.util.Log
import androidx.activity.ComponentActivity
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.animation.AnimatedContent
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.slideInHorizontally
import androidx.compose.animation.slideOutHorizontally
import androidx.compose.animation.togetherWith
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.safeDrawing
import androidx.compose.material3.Scaffold
import androidx.compose.ui.Alignment
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.credentials.CredentialManager
import com.example.auth.AstraAuthManager
import com.example.data.AstraPersistenceManager
import com.example.data.AstraRepository
import com.example.model.UserRole
import com.example.service.AstraAudioAlarmService
import com.example.service.AstraInAppNotificationManager
import com.example.service.AstraMissionRiskNotificationService
import com.example.service.InAppNotificationBannerOverlay
import com.example.ui.components.CriticalRiskAcknowledgementModal
import com.example.ui.screens.AdminPortalScreen
import com.example.ui.screens.AiMedicalChatScreen
import com.example.ui.screens.AuthScreen
import com.example.ui.screens.CrewPortalScreen
import com.example.ui.screens.AstronautHealthAssessmentScreen
import com.example.ui.screens.EarthSyncScreen
import com.example.ui.screens.NasaOpenDataScreen
import com.example.ui.screens.PortalSelectionScreen
import com.example.ui.screens.PvtReactionTestScreen
import com.example.ui.screens.SettingsScreen
import com.example.ui.theme.AstraSenseTheme
import com.google.firebase.FirebaseApp
import com.google.firebase.appcheck.FirebaseAppCheck
import com.google.firebase.appcheck.debug.DebugAppCheckProviderFactory
import com.google.firebase.appcheck.recaptcha.RecaptchaAppCheckProviderFactory

private const val TAG = "AstraSenseApp"
private const val DEBUG_TOKEN_EXTRA = "FIREBASE_APPCHECK_DEBUG_TOKEN"
private const val DEBUG_STORE_PREFIX = "com.google.firebase.appcheck.debug.store."
private const val DEBUG_SECRET_KEY = "com.google.firebase.appcheck.debug.DEBUG_SECRET"
private const val RECAPTCHA_SITE_KEY_RES = "recaptcha_site_key"

enum class CurrentScreen {
  AUTH,
  PORTAL_SELECTION,
  ADMIN_PORTAL,
  CREW_PORTAL,
  PVT_TEST,
  AI_MEDICAL_CHAT,
  EARTH_SYNC,
  HEALTH_ASSESSMENT,
  SETTINGS,
  NASA_OPEN_DATA
}

class MainActivity : ComponentActivity() {

  override fun onCreate(savedInstanceState: Bundle?) {
    super.onCreate(savedInstanceState)
    enableEdgeToEdge()

    // Initialize local persistence vault, audio alarm engine, repository, and Cloud SQL sync manager
    AstraPersistenceManager.initialize(this)
    AstraRepository.initialize(this)
    AstraAudioAlarmService.initialize(this)
    com.example.data.cloudsql.CloudSqlSyncManager.initialize(this)

    // Configure App Check if Firebase is provisioned
    configureAppCheck(this, intent)

    setContent {
      AstraSenseApp()
    }
  }

  private fun configureAppCheck(context: Context, intent: Intent?) {
    try {
      if (FirebaseApp.getApps(context).isEmpty()) {
        FirebaseApp.initializeApp(context)
      }

      if (BuildConfig.DEBUG) {
        val debugToken = intent?.getStringExtra(DEBUG_TOKEN_EXTRA)
        if (!debugToken.isNullOrEmpty()) {
          val persistenceKey = FirebaseApp.getInstance().persistenceKey
          val prefs = context.getSharedPreferences(
            "$DEBUG_STORE_PREFIX$persistenceKey",
            Context.MODE_PRIVATE
          )
          prefs.edit()
            .putString(DEBUG_SECRET_KEY, debugToken)
            .commit()
          Log.d(TAG, "App Check debug secret configured successfully.")
        }
        FirebaseAppCheck.getInstance().installAppCheckProviderFactory(
          DebugAppCheckProviderFactory.getInstance()
        )
      } else {
        val resId = context.resources.getIdentifier(RECAPTCHA_SITE_KEY_RES, "string", context.packageName)
        if (resId != 0) {
          val siteKey = context.getString(resId)
          if (siteKey.isNotBlank()) {
            FirebaseAppCheck.getInstance().installAppCheckProviderFactory(
              RecaptchaAppCheckProviderFactory.getInstance(siteKey)
            )
          }
        }
      }
    } catch (e: Exception) {
      Log.w(TAG, "Firebase App Check initialization skipped (offline/unprovisioned): ${e.localizedMessage}")
    }
  }
}

@Composable
fun AstraSenseApp() {
  val context = LocalContext.current
  val coroutineScope = rememberCoroutineScope()
  val credentialManager = remember { CredentialManager.create(context) }

  val currentUser by AstraRepository.currentUser.collectAsState()
  val currentDestination by AstraRepository.currentDestination.collectAsState()
  val allCrewMembers by AstraRepository.crewMembers.collectAsState()
  val cabinEnv by AstraRepository.cabinEnvironment.collectAsState()

  val activeCriticalAlerts by AstraMissionRiskNotificationService.activeCriticalAlerts.collectAsState()
  val pendingCriticalAlert = remember(activeCriticalAlerts) {
    activeCriticalAlerts.firstOrNull { !it.isAcknowledged }
  }

  // Initial screen is strictly PORTAL_SELECTION (Landing Gateway) to prevent auto-redirecting to Admin
  val initialScreen = remember { CurrentScreen.PORTAL_SELECTION }

  var isDarkMode by rememberSaveable { mutableStateOf(true) }
  var currentScreen by rememberSaveable { mutableStateOf(initialScreen) }
  var returnPortalScreen by rememberSaveable { mutableStateOf(CurrentScreen.PORTAL_SELECTION) }

  // Persist session and current screen whenever screen transitions occur
  LaunchedEffect(currentScreen, currentUser) {
    if (currentUser != null && currentScreen != CurrentScreen.AUTH && currentScreen != CurrentScreen.PORTAL_SELECTION) {
      AstraPersistenceManager.saveSession(currentUser?.id, currentScreen.name, currentUser?.role)
    }
  }

  // Runtime Notification Permission Launcher (Android 13+ / API 33+)
  val notificationPermissionLauncher = rememberLauncherForActivityResult(
    contract = ActivityResultContracts.RequestPermission(),
    onResult = { isGranted ->
      Log.d(TAG, "Notification permission result: $isGranted")
    }
  )

  LaunchedEffect(Unit) {
    AstraMissionRiskNotificationService.initialize(context)
    if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
      notificationPermissionLauncher.launch(Manifest.permission.POST_NOTIFICATIONS)
    }
  }

  // Monitor crew members & cabin environment for critical risk threshold breaches (>= 70)
  LaunchedEffect(cabinEnv, allCrewMembers) {
    allCrewMembers.forEach { member ->
      AstraMissionRiskNotificationService.evaluateAndNotify(context, member, cabinEnv)
    }
  }

  // Attempt silent auto-sign-in on startup without auto-redirecting past Gateway
  LaunchedEffect(Unit) {
    if (currentUser == null) {
      AstraAuthManager.attemptAutoSignIn(
        context = context,
        credentialManager = credentialManager,
        onAuthSuccess = { user ->
          AstraRepository.switchUser(user)
          // Keep currentScreen as PORTAL_SELECTION so the user chooses their portal
        },
        onUnauthenticated = {
          // User will authenticate via Portal Selection Gateway
        },
        scope = coroutineScope
      )
    }
  }

  // Gateway is the primary landing route
  val effectiveScreen = currentScreen

  AstraSenseTheme(isDarkMode = isDarkMode) {
    Scaffold(
      contentWindowInsets = WindowInsets.safeDrawing,
      modifier = Modifier.fillMaxSize()
    ) { innerPadding ->
      Box(modifier = Modifier.fillMaxSize().padding(innerPadding)) {
        AnimatedContent(
          targetState = effectiveScreen,
          transitionSpec = {
            (slideInHorizontally { width -> width / 3 } + fadeIn())
              .togetherWith(slideOutHorizontally { width -> -width / 3 } + fadeOut())
          },
          label = "portalTransition",
          modifier = Modifier.fillMaxSize()
        ) { targetScreen ->
          when (targetScreen) {
            CurrentScreen.AUTH -> {
              AuthScreen(
                onAuthenticated = { user ->
                  AstraRepository.switchUser(user)
                  currentScreen = CurrentScreen.PORTAL_SELECTION
                  returnPortalScreen = CurrentScreen.PORTAL_SELECTION
                  AstraPersistenceManager.saveSession(user.id, CurrentScreen.PORTAL_SELECTION.name, user.role)
                }
              )
            }

          CurrentScreen.PORTAL_SELECTION -> {
            PortalSelectionScreen(
              currentUser = currentUser,
              allCrewMembers = allCrewMembers,
              onSelectAdminPortal = { adminUser ->
                AstraRepository.switchUser(adminUser)
                AstraPersistenceManager.saveSelectedRole(UserRole.ADMIN)
                AstraPersistenceManager.saveSession(adminUser.id, CurrentScreen.ADMIN_PORTAL.name, UserRole.ADMIN)
                returnPortalScreen = CurrentScreen.PORTAL_SELECTION
                currentScreen = CurrentScreen.ADMIN_PORTAL
              },
              onSelectCrewPortal = { crewUser ->
                AstraRepository.switchUser(crewUser)
                AstraPersistenceManager.saveSelectedRole(UserRole.CREW)
                AstraPersistenceManager.saveSession(crewUser.id, CurrentScreen.CREW_PORTAL.name, UserRole.CREW)
                returnPortalScreen = CurrentScreen.PORTAL_SELECTION
                currentScreen = CurrentScreen.CREW_PORTAL
              },
              onOpenSettings = {
                currentScreen = CurrentScreen.SETTINGS
              }
            )
          }

          CurrentScreen.ADMIN_PORTAL -> {
            val user = currentUser
            if (user != null) {
              AdminPortalScreen(
                adminUser = user,
                currentDestination = currentDestination,
                onDestinationSelected = { AstraRepository.setDestination(it) },
                isDarkMode = isDarkMode,
                onToggleTheme = { isDarkMode = !isDarkMode },
                onOpenPvtTest = {
                  returnPortalScreen = CurrentScreen.ADMIN_PORTAL
                  currentScreen = CurrentScreen.PVT_TEST
                },
                onOpenAiMedicalChat = {
                  returnPortalScreen = CurrentScreen.ADMIN_PORTAL
                  currentScreen = CurrentScreen.AI_MEDICAL_CHAT
                },
                onOpenEarthSync = {
                  returnPortalScreen = CurrentScreen.ADMIN_PORTAL
                  currentScreen = CurrentScreen.EARTH_SYNC
                },
                onOpenSettings = {
                  returnPortalScreen = CurrentScreen.ADMIN_PORTAL
                  currentScreen = CurrentScreen.SETTINGS
                },
                onOpenHealthAssessment = {
                  returnPortalScreen = CurrentScreen.ADMIN_PORTAL
                  currentScreen = CurrentScreen.HEALTH_ASSESSMENT
                },
                onOpenNasaOpenData = {
                  returnPortalScreen = CurrentScreen.ADMIN_PORTAL
                  currentScreen = CurrentScreen.NASA_OPEN_DATA
                },
                onSwitchPortal = {
                  currentScreen = CurrentScreen.PORTAL_SELECTION
                }
              )
            }
          }

          CurrentScreen.CREW_PORTAL -> {
            val user = currentUser
            if (user != null) {
              CrewPortalScreen(
                crewMember = user,
                currentDestination = currentDestination,
                onDestinationSelected = { AstraRepository.setDestination(it) },
                isDarkMode = isDarkMode,
                onToggleTheme = { isDarkMode = !isDarkMode },
                onOpenPvtTest = {
                  returnPortalScreen = CurrentScreen.CREW_PORTAL
                  currentScreen = CurrentScreen.PVT_TEST
                },
                onOpenAiMedicalChat = {
                  returnPortalScreen = CurrentScreen.CREW_PORTAL
                  currentScreen = CurrentScreen.AI_MEDICAL_CHAT
                },
                onOpenEarthSync = {
                  returnPortalScreen = CurrentScreen.CREW_PORTAL
                  currentScreen = CurrentScreen.EARTH_SYNC
                },
                onOpenSettings = {
                  returnPortalScreen = CurrentScreen.CREW_PORTAL
                  currentScreen = CurrentScreen.SETTINGS
                },
                onOpenHealthAssessment = {
                  returnPortalScreen = CurrentScreen.CREW_PORTAL
                  currentScreen = CurrentScreen.HEALTH_ASSESSMENT
                },
                onOpenNasaOpenData = {
                  returnPortalScreen = CurrentScreen.CREW_PORTAL
                  currentScreen = CurrentScreen.NASA_OPEN_DATA
                },
                onSwitchPortal = {
                  currentScreen = CurrentScreen.PORTAL_SELECTION
                }
              )
            }
          }

          CurrentScreen.PVT_TEST -> {
            val user = currentUser
            if (user != null) {
              PvtReactionTestScreen(
                crewMember = user,
                onNavigateBack = { currentScreen = returnPortalScreen }
              )
            }
          }

          CurrentScreen.AI_MEDICAL_CHAT -> {
            AiMedicalChatScreen(
              onNavigateBack = { currentScreen = returnPortalScreen }
            )
          }

          CurrentScreen.EARTH_SYNC -> {
            EarthSyncScreen(
              currentDestination = currentDestination,
              onOpenNasaOpenData = {
                returnPortalScreen = CurrentScreen.EARTH_SYNC
                currentScreen = CurrentScreen.NASA_OPEN_DATA
              },
              onNavigateBack = { currentScreen = returnPortalScreen }
            )
          }

          CurrentScreen.HEALTH_ASSESSMENT -> {
            val user = currentUser
            if (user != null) {
              AstronautHealthAssessmentScreen(
                currentUser = user,
                onNavigateBack = { currentScreen = returnPortalScreen },
                onOpenAiAssistant = {
                  returnPortalScreen = CurrentScreen.HEALTH_ASSESSMENT
                  currentScreen = CurrentScreen.AI_MEDICAL_CHAT
                }
              )
            }
          }

          CurrentScreen.NASA_OPEN_DATA -> {
            NasaOpenDataScreen(
              currentDestination = currentDestination,
              onNavigateBack = { currentScreen = returnPortalScreen }
            )
          }

          CurrentScreen.SETTINGS -> {
            val user = currentUser
            if (user != null) {
              SettingsScreen(
                currentUser = user,
                currentDestination = currentDestination,
                onDestinationSelected = { AstraRepository.setDestination(it) },
                isDarkMode = isDarkMode,
                onToggleTheme = { isDarkMode = !isDarkMode },
                onLogout = {
                  AstraAuthManager.signOut(
                    context = context,
                    credentialManager = credentialManager,
                    onSignOutComplete = {
                      currentScreen = CurrentScreen.AUTH
                    },
                    scope = coroutineScope
                  )
                },
                onNavigateBack = { currentScreen = returnPortalScreen }
              )
            }
          }
        }
      }

      // Active Floating Heads-Up Notification Banner (Portrait & Landscape responsive)
        InAppNotificationBannerOverlay(
          modifier = Modifier.align(Alignment.TopCenter)
        )
      }

      // Mandatory Medical Officer Acknowledgement Modal for Critical Risk Breaches
      pendingCriticalAlert?.let { alert ->
        CriticalRiskAcknowledgementModal(
          pendingAlert = alert,
          currentUser = currentUser,
          onAcknowledge = { alertId, officerId, notes ->
            AstraMissionRiskNotificationService.acknowledgeAlert(context, alertId, officerId, notes)
          }
        )
      }
    }
  }
}
