package com.example.aprendeaprender.data.remote

import com.example.aprendeaprender.data.model.Task
import com.google.firebase.database.FirebaseDatabase
import kotlinx.coroutines.tasks.await

class RealtimeTaskService(
    private val database: FirebaseDatabase = FirebaseDatabase.getInstance(
        "https://backend-34179-default-rtdb.firebaseio.com/"
    )
) {
    private fun tasksRef(userId: String) =
        database.getReference("usuarios").child(userId).child("tareas")

    suspend fun createTask(task: Task): String {
        val ref = tasksRef(task.userId).push()
        val taskWithId = task.copy(id = ref.key ?: "")
        ref.setValue(taskWithId).await()
        return ref.key ?: ""
    }

    suspend fun getTasksByUser(userId: String): List<Task> {
        val snapshot = tasksRef(userId).get().await()
        return snapshot.children.mapNotNull { child ->
            Task(
                id = child.key ?: "",
                userId = userId,
                subjectId = child.child("subjectId").getValue(String::class.java).orEmpty(),
                subjectName = child.child("subjectName").getValue(String::class.java).orEmpty(),
                titulo = child.child("titulo").getValue(String::class.java).orEmpty(),
                descripcion = child.child("descripcion").getValue(String::class.java).orEmpty(),
                fechaEntrega = child.child("fechaEntrega").getValue(Long::class.java) ?: 0L,
                prioridad = child.child("prioridad").getValue(String::class.java) ?: Task.Prioridad.MEDIA.name,
                estado = child.child("estado").getValue(String::class.java) ?: Task.Estado.PENDIENTE.name,
                createdAt = child.child("createdAt").getValue(Long::class.java) ?: 0L
            )
        }.sortedBy { it.fechaEntrega }
    }

    suspend fun updateTaskEstado(userId: String, taskId: String, estado: String) {
        tasksRef(userId).child(taskId).child("estado").setValue(estado).await()
    }

    suspend fun deleteTask(userId: String, taskId: String) {
        tasksRef(userId).child(taskId).removeValue().await()
    }
}