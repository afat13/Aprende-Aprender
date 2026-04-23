package com.example.aprendeaprender.data.repository

import com.example.aprendeaprender.data.model.Subject
import com.example.aprendeaprender.data.remote.FirebaseAuthService
import com.example.aprendeaprender.data.remote.FirestoreSubjectService

class SubjectRepository(
    private val authService: FirebaseAuthService,
    private val subjectService: FirestoreSubjectService
) {

    private fun currentUserId(): String {
        return authService.currentUser()?.uid
            ?: throw IllegalStateException("No hay usuario autenticado.")
    }

    suspend fun createSubject(
        asignatura: String,
        instructor: String,
        temas: List<String>
    ): String {
        val subject = Subject(
            userId = currentUserId(),
            asignatura = asignatura.trim(),
            instructor = instructor.trim(),
            temas = temas.map { it.trim() }.filter { it.isNotBlank() }
        )
        return subjectService.createSubject(subject)
    }

    suspend fun getMySubjects(): List<Subject> {
        return subjectService.getSubjectsByUser(currentUserId())
    }

    suspend fun deleteSubject(subjectId: String) {
        subjectService.deleteSubject(subjectId)
    }
}