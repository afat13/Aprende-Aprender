package com.example.aprendeaprender.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.aprendeaprender.R
import com.example.aprendeaprender.data.model.Task
import com.example.aprendeaprender.data.repository.SubjectRepository
import com.example.aprendeaprender.data.repository.TaskRepository
import com.google.firebase.FirebaseNetworkException
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import java.text.SimpleDateFormat
import java.util.Calendar
import java.util.Locale

class TaskViewModel(
    private val taskRepository: TaskRepository,
    private val subjectRepository: SubjectRepository
) : ViewModel() {

    private val _createUiState = MutableStateFlow(CreateTaskUiState())
    val createUiState: StateFlow<CreateTaskUiState> = _createUiState.asStateFlow()

    private val _listUiState = MutableStateFlow(TaskListUiState())
    val listUiState: StateFlow<TaskListUiState> = _listUiState.asStateFlow()

    // ── Cargar materias disponibles ──
    fun cargarMaterias() {
        _createUiState.update { it.copy(cargandoMaterias = true) }
        viewModelScope.launch {
            try {
                val materias = subjectRepository.getMySubjects()
                _createUiState.update {
                    it.copy(
                        cargandoMaterias = false,
                        materiasDisponibles = materias
                    )
                }
            } catch (e: Exception) {
                _createUiState.update { it.copy(cargandoMaterias = false) }
            }
        }
    }

    // ── Preseleccionar materia (desde SubjectSuccessScreen) ──
    fun preseleccionarMateria(subjectId: String, subjectName: String) {
        _createUiState.update {
            it.copy(subjectId = subjectId, subjectName = subjectName)
        }
    }

    // ── Form handlers ──
    fun onTituloChange(value: String) {
        _createUiState.update { it.copy(titulo = value, mensajeErrorResId = null) }
    }

    fun onDescripcionChange(value: String) {
        _createUiState.update { it.copy(descripcion = value) }
    }

    fun onFechaEntregaChange(timestamp: Long) {
        val sdf = SimpleDateFormat("dd/MM/yyyy", Locale.getDefault())
        val texto = sdf.format(timestamp)
        _createUiState.update {
            it.copy(fechaEntrega = timestamp, fechaEntregaTexto = texto)
        }
    }

    fun onPrioridadChange(prioridad: Task.Prioridad) {
        _createUiState.update { it.copy(prioridad = prioridad) }
    }

    fun onEstadoChange(estado: Task.Estado) {
        _createUiState.update { it.copy(estado = estado) }
    }

    fun onSubjectChange(subjectId: String, subjectName: String) {
        _createUiState.update {
            it.copy(subjectId = subjectId, subjectName = subjectName, mensajeErrorResId = null)
        }
    }

    // ── Crear tarea ──
    fun crearTarea() {
        val state = _createUiState.value

        when {
            state.subjectId.isBlank() -> {
                _createUiState.update { it.copy(mensajeErrorResId = R.string.task_error_no_subject) }
                return
            }
            state.titulo.isBlank() -> {
                _createUiState.update { it.copy(mensajeErrorResId = R.string.task_error_empty_title) }
                return
            }
            state.fechaEntrega == 0L -> {
                _createUiState.update { it.copy(mensajeErrorResId = R.string.task_error_no_date) }
                return
            }
        }

        _createUiState.update { it.copy(cargando = true, mensajeErrorResId = null) }

        viewModelScope.launch {
            try {
                android.util.Log.d("TASK_VM", "Creando tarea: ${state.titulo}")
                taskRepository.createTask(
                    subjectId = state.subjectId,
                    subjectName = state.subjectName,
                    titulo = state.titulo,
                    descripcion = state.descripcion,
                    fechaEntrega = state.fechaEntrega,
                    prioridad = state.prioridad,
                    estado = state.estado
                )
                android.util.Log.d("TASK_VM", "Tarea creada exitosamente")
                _createUiState.update { it.copy(cargando = false, tareaCreada = true) }
            } catch (e: Exception) {
                android.util.Log.e("TASK_VM", "Error creando tarea: ${e.message}", e)
                _createUiState.update {
                    it.copy(cargando = false, mensajeErrorResId = mapError(e))
                }
            }
        }
    }

    fun resetCreateForm() {
        _createUiState.update { CreateTaskUiState() }
    }

    // ── Cargar tareas ──
    fun cargarTareas() {
        _listUiState.update { it.copy(cargando = true, mensajeErrorResId = null) }
        viewModelScope.launch {
            try {
                val tareas = taskRepository.getMyTasks()
                val hoy = esHoy()
                val tareasHoy = tareas.filter { esDelDia(it.fechaEntrega, hoy) }
                _listUiState.update {
                    it.copy(
                        cargando = false,
                        tareasHoy = tareasHoy,
                        todasLasTareas = tareas
                    )
                }
            } catch (e: Exception) {
                android.util.Log.e("TASK_VM", "Error cargando tareas: ${e.message}", e)
                _listUiState.update {
                    it.copy(cargando = false, mensajeErrorResId = mapError(e))
                }
            }
        }
    }

    fun cambiarEstado(taskId: String, estado: Task.Estado) {
        viewModelScope.launch {
            try {
                taskRepository.updateTaskEstado(taskId, estado)
                cargarTareas()
            } catch (e: Exception) {
                android.util.Log.e("TASK_VM", "Error cambiando estado: ${e.message}", e)
            }
        }
    }

    fun eliminarTarea(taskId: String) {
        viewModelScope.launch {
            try {
                taskRepository.deleteTask(taskId)
                cargarTareas()
            } catch (e: Exception) {
                android.util.Log.e("TASK_VM", "Error eliminando tarea: ${e.message}", e)
            }
        }
    }

    // ── Helpers ──
    private fun esHoy(): Calendar {
        return Calendar.getInstance().apply {
            set(Calendar.HOUR_OF_DAY, 0)
            set(Calendar.MINUTE, 0)
            set(Calendar.SECOND, 0)
            set(Calendar.MILLISECOND, 0)
        }
    }

    private fun esDelDia(timestamp: Long, hoy: Calendar): Boolean {
        val cal = Calendar.getInstance().apply {
            timeInMillis = timestamp
            set(Calendar.HOUR_OF_DAY, 0)
            set(Calendar.MINUTE, 0)
            set(Calendar.SECOND, 0)
            set(Calendar.MILLISECOND, 0)
        }
        return cal == hoy
    }

    private fun mapError(e: Exception): Int {
        return when (e) {
            is FirebaseNetworkException -> R.string.auth_error_network
            else -> R.string.auth_error_generic
        }
    }
}