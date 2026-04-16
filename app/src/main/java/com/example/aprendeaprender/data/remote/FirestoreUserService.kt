package com.example.aprendeaprender.data.remote

import com.example.aprendeaprender.data.model.UserProfile
import com.google.firebase.firestore.FirebaseFirestore
import kotlinx.coroutines.tasks.await

class FirestoreUserService(
    private val firestore: FirebaseFirestore = FirebaseFirestore.getInstance()
) {

    suspend fun createUserProfile(profile: UserProfile) {
        firestore.collection("usuarios")
            .document(profile.uid)
            .set(profile)
            .await()
    }
}