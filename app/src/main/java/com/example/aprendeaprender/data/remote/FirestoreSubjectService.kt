package com.example.aprendeaprender.data.remote

import com.example.aprendeaprender.data.model.Subject
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.Query
import kotlinx.coroutines.tasks.await

class FirestoreSubjectService(
    private val firestore: FirebaseFirestore = FirebaseFirestore.getInstance()
) {

    private val subjectsCollection = firestore.collection("subjects")

    suspend fun createSubject(subject: Subject): String {
        val docRef = subjectsCollection.document()
        val subjectWithId = subject.copy(id = docRef.id)
        docRef.set(subjectWithId).await()
        return docRef.id
    }

    suspend fun getSubjectsByUser(userId: String): List<Subject> {
        val snapshot = subjectsCollection
            .whereEqualTo("userId", userId)
            .orderBy("createdAt", Query.Direction.DESCENDING)
            .get()
            .await()

        return snapshot.documents.map { doc ->
            Subject(
                id = doc.id,
                userId = doc.getString("userId").orEmpty(),
                asignatura = doc.getString("asignatura").orEmpty(),
                instructor = doc.getString("instructor").orEmpty(),
                temas = (doc.get("temas") as? List<*>)?.mapNotNull { it as? String } ?: emptyList(),
                createdAt = doc.getLong("createdAt") ?: 0L
            )
        }
    }

    suspend fun deleteSubject(subjectId: String) {
        subjectsCollection.document(subjectId).delete().await()
    }
}