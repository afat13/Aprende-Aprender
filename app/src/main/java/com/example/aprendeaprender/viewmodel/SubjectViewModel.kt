package com.example.aprendeaprender.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.aprendeaprender.R
import com.example.aprendeaprender.data.repository.SubjectRepository
import com.google.firebase.FirebaseNetworkException
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch

class SubjectViewModel(
    private val repository: SubjectRepository
) : ViewModel() {

    private val _createUiState = MutableStateFlow(CreateSubjectUiState())
    val createUiState: StateFlow<CreateSubjectUiState> = _createUiState.asStateFlow()

    private val _listUiState = MutableStateFlow(SubjectListUiState())
    val listUiState: StateFlow<SubjectListUiState> = _listUiState.asStateFlow()

    // ── Crear materia ──

    fun onAsignaturaChange(value: String) {
        _createUiState.update { it.copy(asignatura = value, mensajeErrorResId = null) }
    }

    fun onInstructorChange(value: String) {
        _createUiState.update { it.copy(instructor = value, mensajeErrorResId = null) }
    }

    fun onTemaChange(index: Int, value: String) {
        _createUiState.update { state ->
            val temas = state.temas.toMutableList()
            if (index in temas.indices) {
                temas[index] = value
            }
            state.copy(temas = temas, mensajeErrorResId = null)
        }
    }

    fun agregarTema() {
        _createUiState.update { state ->
            val temas = state.temas.toMutableList()
            temas.add("")
            state.copy(temas = temas)
        }
    }

    fun eliminarTema(index: Int) {
        _createUiState.update { state ->
            val temas = state.temas.toMutableList()
            if (temas.size > 1 && index in temas.indices) {
                temas.removeAt(index)
            }
            state.copy(temas = temas)
        }
    }

    fun inscribirMateria() {
        val state = _createUiState.value

        when {
            state.asignatura.isBlank() -> {
                _createUiState.update {
                    it.copy(mensajeErrorResId = R.string.subject_error_empty_name)
                }
                return
            }
            state.instructor.isBlank() -> {
                _createUiState.update {
                    it.copy(mensajeErrorResId = R.string.subject_error_empty_instructor)
                }
                return
            }
        }

        _createUiState.update { it.copy(cargando = true, mensajeErrorResId = null) }

        viewModelScope.launch {
            try {
                repository.createSubject(
                    asignatura = state.asignatura,
                    instructor = state.instructor,
                    temas = state.temas
                )
                _createUiState.update {
                    it.copy(cargando = false, materiaCreada = true)
                }
            } catch (e: Exception) {
            android.util.Log.e("SUBJECT", "Error inscribiendo materia", e)
            _createUiState.update {
                it.copy(
                    cargando = false,
                    mensajeErrorResId = mapSubjectError(e)
                )
            }
        }
        }
    }

    fun resetCreateForm() {
        _createUiState.update { CreateSubjectUiState() }
    }

    // ── Listar materias ──

    fun cargarMaterias() {
        _listUiState.update { it.copy(cargando = true, mensajeErrorResId = null) }

        viewModelScope.launch {
            try {
                val subjects = repository.getMySubjects()
                _listUiState.update {
                    it.copy(cargando = false, subjects = subjects)
                }
            } catch (e: Exception) {
                _listUiState.update {
                    it.copy(
                        cargando = false,
                        mensajeErrorResId = mapSubjectError(e)
                    )
                }
            }
        }
    }

    fun eliminarMateria(subjectId: String) {
        viewModelScope.launch {
            try {
                repository.deleteSubject(subjectId)
                cargarMaterias()
            } catch (_: Exception) {
                // silencioso por ahora
            }
        }
    }

    private fun mapSubjectError(e: Exception): Int {
        return when (e) {
            is FirebaseNetworkException -> R.string.auth_error_network
            else -> R.string.auth_error_generic
        }
    }
}