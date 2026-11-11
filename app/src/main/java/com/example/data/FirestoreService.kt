package com.example.data

import android.content.Context
import android.util.Log
import com.example.R
import com.example.model.CrewMember
import com.example.model.UserRole
import com.google.firebase.Firebase
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.auth
import com.google.firebase.firestore.FieldValue
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.SetOptions
import kotlinx.coroutines.tasks.await
import org.json.JSONArray
import org.json.JSONObject

enum class OperationType(val value: String) {
  CREATE("create"),
  UPDATE("update"),
  DELETE("delete"),
  LIST("list"),
  GET("get"),
  WRITE("write")
}

fun handleFirestoreError(exception: Exception, operationType: OperationType, path: String?): String {
  val auth = Firebase.auth
  val currentUser = auth.currentUser

  val providerInfoList = currentUser?.providerData?.map { provider ->
    JSONObject().apply {
      put("providerId", provider.providerId)
      put("email", provider.email)
    }
  } ?: emptyList()

  val authInfoJson = JSONObject().apply {
    put("userId", currentUser?.uid)
    put("email", currentUser?.email)
    put("emailVerified", currentUser?.isEmailVerified)
    put("tenantId", currentUser?.tenantId)
    put("providerInfo", JSONArray(providerInfoList))
  }

  val errorInfoJson = JSONObject().apply {
    put("error", exception.message ?: exception.toString())
    put("operationType", operationType.value)
    put("path", path)
    put("authInfo", authInfoJson)
  }

  val jsonString = errorInfoJson.toString()
  Log.e("FirestoreError", "Firestore Error: $jsonString")
  return jsonString
}

class FirestoreService(
  private val firestore: FirebaseFirestore
) {
  constructor(context: Context) : this(
    FirebaseFirestore.getInstance(context.getString(R.string.firestore_database_id))
  )

  private val auth: FirebaseAuth get() = Firebase.auth

  fun requireUserId(): String {
    return auth.currentUser?.uid
      ?: throw IllegalStateException("User must be authenticated before performing cloud operations.")
  }

  suspend fun saveUserProfile(member: CrewMember): Result<Unit> {
    val uid = auth.currentUser?.uid ?: return Result.failure(IllegalStateException("No authenticated user"))
    val path = "users/$uid"
    return try {
      val data = hashMapOf<String, Any>(
        "userId" to uid,
        "email" to member.email,
        "fullName" to member.fullName,
        "username" to member.username,
        "adminGivenId" to member.id,
        "role" to if (member.role == UserRole.ADMIN) "admin" else "crew",
        "gender" to member.gender,
        "age" to member.age,
        "assignedMission" to member.missionSpecialty,
        "createdAt" to FieldValue.serverTimestamp(),
        "updatedAt" to FieldValue.serverTimestamp()
      )
      firestore.collection("users").document(uid).set(data, SetOptions.merge()).await()
      Result.success(Unit)
    } catch (e: Exception) {
      handleFirestoreError(e, OperationType.WRITE, path)
      Result.failure(e)
    }
  }

  suspend fun fetchUserProfile(uid: String): Result<CrewMember?> {
    val path = "users/$uid"
    return try {
      val doc = firestore.collection("users").document(uid).get().await()
      if (!doc.exists()) {
        Result.success(null)
      } else {
        val roleStr = doc.getString("role") ?: "crew"
        val role = if (roleStr == "admin") UserRole.ADMIN else UserRole.CREW
        val adminId = doc.getString("adminGivenId") ?: "NASA-AS-701"
        val existing = AstraRepository.crewMembers.value.find { it.id.equals(adminId, ignoreCase = true) }
        val member = (existing ?: AstraRepository.crewMembers.value.first()).copy(
          id = adminId,
          fullName = doc.getString("fullName") ?: "Astronaut",
          email = doc.getString("email") ?: (auth.currentUser?.email ?: ""),
          username = doc.getString("username") ?: "astronaut",
          role = role,
          gender = doc.getString("gender") ?: "Not specified",
          age = (doc.getLong("age")?.toInt()) ?: 35
        )
        Result.success(member)
      }
    } catch (e: Exception) {
      handleFirestoreError(e, OperationType.GET, path)
      Result.failure(e)
    }
  }
}
