package com.example.aprendeaprender.data.repository

import com.example.aprendeaprender.data.model.UserProfile
import com.example.aprendeaprender.data.remote.FirebaseAuthService
import com.example.aprendeaprender.data.remote.FirestoreUserService

class ProfileRepository(
    private val authService: FirebaseAuthService,
    private val firestoreUserService: FirestoreUserService
) {

    suspend fun getProfile(): UserProfile {
        val user = authService.currentUser()
            ?: throw IllegalStateException("No hay usuario autenticado.")

        return firestoreUserService.getUserProfile(
            uid = user.uid,
            fallbackEmail = user.email.orEmpty()
        )
    }

    suspend fun updateProfile(
        nombre: String,
        apellido: String,
        telefono: String
    ) {
        val user = authService.currentUser()
            ?: throw IllegalStateException("No hay usuario autenticado.")

        val currentProfile = firestoreUserService.getUserProfile(
            uid = user.uid,
            fallbackEmail = user.email.orEmpty()
        )

        val updatedProfile = currentProfile.copy(
            uid = user.uid,
            email = user.email.orEmpty(),
            nombre = nombre.trim(),
            apellido = apellido.trim(),
            telefono = telefono.trim()
        )

        firestoreUserService.saveUserProfile(updatedProfile)
    }
}