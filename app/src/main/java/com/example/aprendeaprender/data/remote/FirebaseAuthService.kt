package com.example.aprendeaprender.data.remote

import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.FirebaseUser
import kotlinx.coroutines.tasks.await

class FirebaseAuthService(
    private val auth: FirebaseAuth = FirebaseAuth.getInstance()
) {

    init {
        auth.useAppLanguage()
    }

    fun currentUser(): FirebaseUser? = auth.currentUser

    suspend fun signIn(email: String, password: String): FirebaseUser {
        val result = auth.signInWithEmailAndPassword(email, password).await()
        return result.user ?: throw IllegalStateException("No se pudo obtener el usuario autenticado.")
    }

    suspend fun register(email: String, password: String): FirebaseUser {
        val result = auth.createUserWithEmailAndPassword(email, password).await()
        return result.user ?: throw IllegalStateException("No se pudo obtener el usuario registrado.")
    }

    suspend fun sendPasswordResetEmail(email: String) {
        auth.sendPasswordResetEmail(email).await()
    }

    suspend fun sendEmailVerification(user: FirebaseUser) {
        user.sendEmailVerification().await()
    }

    suspend fun reloadCurrentUser() {
        auth.currentUser?.reload()?.await()
    }

    fun signOut() {
        auth.signOut()
    }
}