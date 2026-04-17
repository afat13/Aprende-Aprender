package com.example.aprendeaprender.data.remote

import com.example.aprendeaprender.data.model.UserProfile
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.SetOptions
import kotlinx.coroutines.tasks.await

class FirestoreUserService(
    private val firestore: FirebaseFirestore = FirebaseFirestore.getInstance()
) {

    private val usersCollection = firestore.collection("users")

    suspend fun getUserProfile(uid: String, fallbackEmail: String): UserProfile {
        val snapshot = usersCollection.document(uid).get().await()

        if (!snapshot.exists()) {
            return UserProfile(
                uid = uid,
                email = fallbackEmail,
                nombre = "",
                apellido = "",
                telefono = ""
            )
        }

        return UserProfile(
            uid = uid,
            email = snapshot.getString("email").orEmpty().ifBlank { fallbackEmail },
            nombre = snapshot.getString("nombre").orEmpty(),
            apellido = snapshot.getString("apellido").orEmpty(),
            telefono = snapshot.getString("telefono").orEmpty()
        )
    }

    suspend fun saveUserProfile(profile: UserProfile) {
        usersCollection
            .document(profile.uid)
            .set(profile, SetOptions.merge())
            .await()
    }

    suspend fun createUserProfile(profile: UserProfile) {
        usersCollection
            .document(profile.uid)
            .set(profile, SetOptions.merge())
            .await()
    }
}