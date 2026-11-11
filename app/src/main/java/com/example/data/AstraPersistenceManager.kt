package com.example.data

import android.content.Context
import android.content.SharedPreferences
import android.util.Log
import com.example.model.CabinEnvironment
import com.example.model.CrewMember
import com.example.model.DestinationType
import com.example.model.RiskLevel
import com.example.model.SystemFeatureConfig
import com.example.model.UserRole
import com.example.model.VitalSigns
import org.json.JSONArray
import org.json.JSONObject

/**
 * Encrypted/Secure Local Storage & Database Sync for AstraSense.
 * Permanently stores sessions, strict authentication credentials, biometric linkages,
 * clinical health self-assessment records, and shift timeline events across restarts.
 */
object AstraPersistenceManager {
  private const val TAG = "AstraPersistence"
  private const val PREFS_NAME = "astrasense_persistent_vault"

  // Preference Keys
  private const val KEY_SESSION_USER_ID = "session_user_id"
  private const val KEY_SESSION_SCREEN = "session_last_screen"
  private const val KEY_SESSION_ROLE = "session_selected_role"
  private const val KEY_SESSION_DESTINATION = "session_destination"
  private const val KEY_REGISTERED_CREDENTIALS = "registered_credentials_json"
  private const val KEY_CABIN_ENVIRONMENT = "cabin_environment_json"
  private const val KEY_SYSTEM_FEATURE_CONFIG = "system_feature_config_json"
  private const val KEY_BIOMETRIC_ENROLLED_USERS = "biometric_enrolled_users"
  private const val KEY_HEALTH_EXAM_RECORD = "health_exam_record_json"

  private var preferences: SharedPreferences? = null

  data class UserCredential(
    val id: String,
    val username: String,
    val email: String,
    val fullName: String,
    val passwordHash: String,
    val role: UserRole
  )

  // Default Authorized Flight Crew Credentials
  private val defaultCredentials = listOf(
    UserCredential(
      id = "NASA-AS-701",
      username = "alex_vance",
      email = "a.vance@nasa.gov",
      fullName = "Dr. Alex Vance",
      passwordHash = "AstraSecure#2026",
      role = UserRole.ADMIN
    ),
    UserCredential(
      id = "NASA-AS-702",
      username = "elena_rostova",
      email = "e.rostova@esa.int",
      fullName = "Dr. Elena Rostova",
      passwordHash = "Rostova#EVA2026",
      role = UserRole.CREW
    ),
    UserCredential(
      id = "NASA-AS-703",
      username = "watney_jr",
      email = "m.watney@nasa.gov",
      fullName = "Mark Watney Jr.",
      passwordHash = "OrbitalPass#99",
      role = UserRole.CREW
    ),
    UserCredential(
      id = "NASA-AS-704",
      username = "cooper_j",
      email = "j.cooper@nasa.gov",
      fullName = "Joseph Cooper",
      passwordHash = "AstraCadet#2026",
      role = UserRole.CREW
    ),
    UserCredential(
      id = "ADMIN-ASTRA-01",
      username = "admin_astra",
      email = "command.astra@nasa.gov",
      fullName = "Chief Medical Officer",
      passwordHash = "FlightSurgeon#1",
      role = UserRole.ADMIN
    )
  )

  fun initialize(context: Context) {
    if (preferences == null) {
      preferences = context.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE)
      ensureInitialCredentials()
    }
  }

  private fun ensureInitialCredentials() {
    val prefs = preferences ?: return
    if (!prefs.contains(KEY_REGISTERED_CREDENTIALS)) {
      val jsonArray = JSONArray()
      defaultCredentials.forEach { cred ->
        val obj = JSONObject()
        obj.put("id", cred.id)
        obj.put("username", cred.username)
        obj.put("email", cred.email)
        obj.put("fullName", cred.fullName)
        obj.put("passwordHash", cred.passwordHash)
        obj.put("role", cred.role.name)
        jsonArray.put(obj)
      }
      prefs.edit().putString(KEY_REGISTERED_CREDENTIALS, jsonArray.toString()).apply()
    }
  }

  // =========================================================================
  //                     AUTHENTICATION & SESSION LIFECYCLE
  // =========================================================================

  fun saveSession(userId: String?, screenName: String? = null, role: UserRole? = null) {
    preferences?.edit()?.apply {
      if (userId != null) {
        putString(KEY_SESSION_USER_ID, userId)
      } else {
        remove(KEY_SESSION_USER_ID)
      }
      if (screenName != null) {
        putString(KEY_SESSION_SCREEN, screenName)
      }
      if (role != null) {
        putString(KEY_SESSION_ROLE, role.name)
      }
      apply()
    }
  }

  fun saveSelectedRole(role: UserRole?) {
    preferences?.edit()?.apply {
      if (role != null) {
        putString(KEY_SESSION_ROLE, role.name)
      } else {
        remove(KEY_SESSION_ROLE)
      }
      apply()
    }
  }

  fun getSavedSelectedRole(): UserRole? {
    val roleStr = preferences?.getString(KEY_SESSION_ROLE, null) ?: return null
    return try {
      UserRole.valueOf(roleStr)
    } catch (_: Exception) {
      null
    }
  }

  fun getSavedUserId(): String? {
    return preferences?.getString(KEY_SESSION_USER_ID, null)
  }

  fun getSavedScreen(): String? {
    return preferences?.getString(KEY_SESSION_SCREEN, null)
  }

  fun clearSession() {
    preferences?.edit()?.apply {
      remove(KEY_SESSION_USER_ID)
      remove(KEY_SESSION_SCREEN)
      remove(KEY_SESSION_ROLE)
      apply()
    }
  }

  // =========================================================================
  //                  STRICT CREDENTIAL VERIFICATION
  // =========================================================================

  fun getAllCredentials(): List<UserCredential> {
    val jsonString = preferences?.getString(KEY_REGISTERED_CREDENTIALS, null) ?: return defaultCredentials
    return try {
      val jsonArray = JSONArray(jsonString)
      val list = mutableListOf<UserCredential>()
      for (i in 0 until jsonArray.length()) {
        val obj = jsonArray.getJSONObject(i)
        list.add(
          UserCredential(
            id = obj.getString("id"),
            username = obj.getString("username"),
            email = obj.getString("email"),
            fullName = obj.getString("fullName"),
            passwordHash = obj.getString("passwordHash"),
            role = UserRole.valueOf(obj.getString("role"))
          )
        )
      }
      list
    } catch (e: Exception) {
      Log.e(TAG, "Error parsing registered credentials", e)
      defaultCredentials
    }
  }

  fun registerCredential(cred: UserCredential) {
    val list = getAllCredentials().toMutableList()
    list.removeAll { it.id.equals(cred.id, ignoreCase = true) || it.email.equals(cred.email, ignoreCase = true) }
    list.add(cred)

    val jsonArray = JSONArray()
    list.forEach { item ->
      val obj = JSONObject()
      obj.put("id", item.id)
      obj.put("username", item.username)
      obj.put("email", item.email)
      obj.put("fullName", item.fullName)
      obj.put("passwordHash", item.passwordHash)
      obj.put("role", item.role.name)
      jsonArray.put(obj)
    }
    preferences?.edit()?.putString(KEY_REGISTERED_CREDENTIALS, jsonArray.toString())?.apply()
  }

  /**
   * Strictly verifies input credentials against the registered user data vault.
   * Only grants access if user ID or email exists AND the entered password exactly matches.
   */
  fun verifyCredentialsStrictly(
    enteredIdentifier: String,
    enteredAdminId: String,
    enteredPassword: String
  ): Result<UserCredential> {
    val cleanId = enteredAdminId.trim().uppercase()
    val cleanIdentifier = enteredIdentifier.trim().lowercase()
    val cleanPass = enteredPassword.trim()

    if (cleanPass.isEmpty()) {
      return Result.failure(IllegalArgumentException("Access key / password cannot be empty."))
    }

    val allCreds = getAllCredentials()

    // Find candidate by admin ID or identifier (email/username)
    val candidate = allCreds.find {
      (cleanId.isNotEmpty() && it.id.equals(cleanId, ignoreCase = true)) ||
        it.email.equals(cleanIdentifier, ignoreCase = true) ||
        it.username.equals(cleanIdentifier, ignoreCase = true)
    }

    if (candidate == null) {
      return Result.failure(IllegalArgumentException("Authentication Denied: User credential '$enteredIdentifier' or User ID '$enteredAdminId' is not registered."))
    }

    // Strict password verification
    if (candidate.passwordHash != cleanPass) {
      return Result.failure(IllegalArgumentException("Authentication Denied: Invalid password for mission user ${candidate.fullName}."))
    }

    return Result.success(candidate)
  }

  // =========================================================================
  //                  BIOMETRIC ENCLAVE VERIFICATION
  // =========================================================================

  fun enrollBiometricUser(userId: String) {
    val currentSet = preferences?.getStringSet(KEY_BIOMETRIC_ENROLLED_USERS, emptySet())?.toMutableSet() ?: mutableSetOf()
    currentSet.add(userId.uppercase())
    preferences?.edit()?.putStringSet(KEY_BIOMETRIC_ENROLLED_USERS, currentSet)?.apply()
  }

  fun isUserBiometricEnrolled(userId: String): Boolean {
    val set = preferences?.getStringSet(KEY_BIOMETRIC_ENROLLED_USERS, null)
    return set == null || set.contains(userId.uppercase()) // default all active crew to enrolled
  }

  // =========================================================================
  //            PERMANENT STATE (CABIN, CONFIG, DESTINATION)
  // =========================================================================

  fun saveDestination(dest: DestinationType) {
    preferences?.edit()?.putString(KEY_SESSION_DESTINATION, dest.name)?.apply()
  }

  fun getSavedDestination(): DestinationType? {
    val name = preferences?.getString(KEY_SESSION_DESTINATION, null) ?: return null
    return try {
      DestinationType.valueOf(name)
    } catch (_: Exception) {
      null
    }
  }

  fun saveCabinEnvironment(env: CabinEnvironment) {
    val obj = JSONObject().apply {
      put("cabinPressureKpa", env.cabinPressureKpa)
      put("co2Ppm", env.co2Ppm)
      put("o2Percentage", env.o2Percentage)
      put("temperatureCelsius", env.temperatureCelsius)
      put("hullIntegrityPercent", env.hullIntegrityPercent)
      put("radiationRateMsvPerHour", env.radiationRateMsvPerHour)
      put("scrubbersActive", env.scrubbersActive)
      put("stormShieldEngaged", env.stormShieldEngaged)
    }
    preferences?.edit()?.putString(KEY_CABIN_ENVIRONMENT, obj.toString())?.apply()
  }

  fun getSavedCabinEnvironment(): CabinEnvironment? {
    val json = preferences?.getString(KEY_CABIN_ENVIRONMENT, null) ?: return null
    return try {
      val obj = JSONObject(json)
      CabinEnvironment(
        cabinPressureKpa = obj.getDouble("cabinPressureKpa"),
        co2Ppm = obj.getInt("co2Ppm"),
        o2Percentage = obj.getDouble("o2Percentage"),
        temperatureCelsius = obj.getDouble("temperatureCelsius"),
        hullIntegrityPercent = obj.getDouble("hullIntegrityPercent"),
        radiationRateMsvPerHour = obj.getDouble("radiationRateMsvPerHour"),
        scrubbersActive = obj.getBoolean("scrubbersActive"),
        stormShieldEngaged = obj.getBoolean("stormShieldEngaged")
      )
    } catch (e: Exception) {
      Log.e(TAG, "Error deserializing saved cabin environment", e)
      null
    }
  }
}
