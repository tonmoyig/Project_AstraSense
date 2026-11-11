package com.example.data

import com.example.base.FirestoreEmulatorTestBase
import com.google.firebase.firestore.FieldValue
import com.google.firebase.firestore.FirebaseFirestoreException
import kotlinx.coroutines.runBlocking
import kotlinx.coroutines.tasks.await
import org.junit.Assert.assertNotNull
import org.junit.Assert.assertTrue
import org.junit.Assert.fail
import org.junit.Test

class AstraFirestoreRuleTest : FirestoreEmulatorTestBase() {

  @Test
  fun unauthenticatedUserCannotWriteProfile() = runBlocking {
    auth.signOut()
    try {
      firestore.collection("users").document("astronaut_123").set(
        mapOf(
          "userId" to "astronaut_123",
          "email" to "astronaut@nasa.gov",
          "fullName" to "Commander Shepard",
          "adminGivenId" to "NASA-AS-701",
          "role" to "crew",
          "createdAt" to FieldValue.serverTimestamp()
        )
      ).await()
      fail("Unauthenticated write should have thrown PERMISSION_DENIED")
    } catch (e: Exception) {
      val firestoreException = e.unwrapFirestoreException()
      assertNotNull("Expected FirebaseFirestoreException", firestoreException)
      assertTrue(
        "Expected PERMISSION_DENIED but got: ${firestoreException?.code}",
        firestoreException?.code == FirebaseFirestoreException.Code.PERMISSION_DENIED
      )
    }
  }

  @Test
  fun authenticatedUserCanWriteOwnProfile() = runBlocking {
    val uid = signInTestUser("astro.shepard@nasa.gov")
    val data = mapOf(
      "userId" to uid,
      "email" to "astro.shepard@nasa.gov",
      "fullName" to "Commander Shepard",
      "adminGivenId" to "NASA-AS-701",
      "role" to "admin",
      "createdAt" to FieldValue.serverTimestamp()
    )
    firestore.collection("users").document(uid).set(data).await()
    val doc = firestore.collection("users").document(uid).get().await()
    assertTrue(doc.exists())
  }

  @Test
  fun authenticatedUserCannotWriteOtherUserProfile() = runBlocking {
    val uid1 = signInTestUser("astro.one@nasa.gov")
    val uid2 = "other_astronaut_999"

    try {
      firestore.collection("users").document(uid2).set(
        mapOf(
          "userId" to uid2,
          "email" to "other@nasa.gov",
          "fullName" to "Other Astro",
          "adminGivenId" to "NASA-AS-702",
          "role" to "crew",
          "createdAt" to FieldValue.serverTimestamp()
        )
      ).await()
      fail("Writing other user's document should have failed")
    } catch (e: Exception) {
      val firestoreException = e.unwrapFirestoreException()
      assertNotNull("Expected FirebaseFirestoreException", firestoreException)
      assertTrue(firestoreException?.code == FirebaseFirestoreException.Code.PERMISSION_DENIED)
    }
  }
}
