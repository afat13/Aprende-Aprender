package com.example.aprendeaprender.data.remote

import com.example.aprendeaprender.data.model.Subject
import com.google.firebase.database.FirebaseDatabase
import kotlinx.coroutines.tasks.await

class RealtimeSubjectService(
    private val database: FirebaseDatabase = FirebaseDatabase.getInstance(
        "https://backend-34179-default-rtdb.firebaseio.com/"
    )
) {

    private fun subjectsRef(userId: String) =
        database.getReference("usuarios").child(userId).child("materias")

    suspend fun createSubject(subject: Subject): String {
        val ref = subjectsRef(subject.userId).push()
        val subjectWithId = subject.copy(id = ref.key ?: "")
        ref.setValue(subjectWithId).await()
        return ref.key ?: ""
    }

    suspend fun getSubjectsByUser(userId: String): List<Subject> {
        val snapshot = subjectsRef(userId).get().await()
        return snapshot.children.mapNotNull { child ->
            Subject(
                id = child.key ?: "",
                userId = userId,
                asignatura = child.child("asignatura").getValue(String::class.java).orEmpty(),
                instructor = child.child("instructor").getValue(String::class.java).orEmpty(),
                temas = child.child("temas").children
                    .mapNotNull { it.getValue(String::class.java) },
                createdAt = child.child("createdAt").getValue(Long::class.java) ?: 0L
            )
        }.sortedByDescending { it.createdAt }
    }

    suspend fun deleteSubject(userId: String, subjectId: String) {
        subjectsRef(userId).child(subjectId).removeValue().await()
    }
}