package com.example.aprendeaprender.data.remote

import com.example.aprendeaprender.data.model.Task
import com.google.firebase.database.FirebaseDatabase
import kotlinx.coroutines.tasks.await

class RealtimeTaskService(
    private val database: FirebaseDatabase = FirebaseDatabase.getInstance(
        "https://backend-34179-default-rtdb.firebaseio.com/"
    )
) {

    private fun materiasRef(userId: String) =
        database.getReference("usuarios")
            .child(userId)
            .child("materias")

    private fun tareasDeMateriaRef(userId: String, subjectId: String) =
        materiasRef(userId)
            .child(subjectId)
            .child("tareas")

    suspend fun createTask(task: Task): String {
        require(task.userId.isNotBlank()) { "La tarea no tiene userId." }
        require(task.subjectId.isNotBlank()) { "La tarea no tiene subjectId." }

        val ref = tareasDeMateriaRef(task.userId, task.subjectId).push()
        val taskId = ref.key ?: ""

        val taskWithId = task.copy(id = taskId)

        ref.setValue(taskWithId).await()

        return taskId
    }

    suspend fun getTasksByUser(userId: String): List<Task> {
        val snapshot = materiasRef(userId).get().await()

        return snapshot.children.flatMap { subjectSnapshot ->
            val subjectId = subjectSnapshot.key.orEmpty()
            val subjectNameFromSubject = subjectSnapshot
                .child("asignatura")
                .getValue(String::class.java)
                .orEmpty()

            subjectSnapshot.child("tareas").children.map { taskSnapshot ->
                val storedSubjectId = taskSnapshot
                    .child("subjectId")
                    .getValue(String::class.java)
                    .orEmpty()

                val storedSubjectName = taskSnapshot
                    .child("subjectName")
                    .getValue(String::class.java)
                    .orEmpty()

                Task(
                    id = taskSnapshot.child("id").getValue(String::class.java)
                        ?: taskSnapshot.key.orEmpty(),
                    userId = userId,
                    subjectId = storedSubjectId.ifBlank { subjectId },
                    subjectName = storedSubjectName.ifBlank { subjectNameFromSubject },
                    titulo = taskSnapshot.child("titulo").getValue(String::class.java).orEmpty(),
                    descripcion = taskSnapshot.child("descripcion").getValue(String::class.java).orEmpty(),
                    fechaEntrega = taskSnapshot.child("fechaEntrega").getValue(Long::class.java) ?: 0L,
                    prioridad = taskSnapshot.child("prioridad").getValue(String::class.java)
                        ?: Task.Prioridad.MEDIA.name,
                    estado = taskSnapshot.child("estado").getValue(String::class.java)
                        ?: Task.Estado.PENDIENTE.name,
                    createdAt = taskSnapshot.child("createdAt").getValue(Long::class.java) ?: 0L
                )
            }
        }.sortedBy { it.fechaEntrega }
    }

    suspend fun updateTaskEstado(
        userId: String,
        subjectId: String,
        taskId: String,
        estado: String
    ) {
        tareasDeMateriaRef(userId, subjectId)
            .child(taskId)
            .child("estado")
            .setValue(estado)
            .await()
    }

    suspend fun deleteTask(
        userId: String,
        subjectId: String,
        taskId: String
    ) {
        tareasDeMateriaRef(userId, subjectId)
            .child(taskId)
            .removeValue()
            .await()
    }
}