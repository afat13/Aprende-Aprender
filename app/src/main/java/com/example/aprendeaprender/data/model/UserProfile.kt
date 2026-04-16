package com.example.aprendeaprender.data.model

data class UserProfile(
    val uid: String = "",
    val email: String = "",
    val createdAt: Long = System.currentTimeMillis()
)