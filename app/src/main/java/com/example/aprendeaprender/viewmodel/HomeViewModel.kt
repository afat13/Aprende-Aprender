package com.example.aprendeaprender.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.aprendeaprender.data.repository.ProfileRepository
import com.example.aprendeaprender.ui.screens.home.HomeUiState
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch

class HomeViewModel(
    private val profileRepository: ProfileRepository
) : ViewModel() {

    private val _uiState = MutableStateFlow(HomeUiState())
    val uiState: StateFlow<HomeUiState> = _uiState.asStateFlow()

    fun cargarDatosHome() {
        viewModelScope.launch {
            try {
                val profile = profileRepository.getProfile()
                val nombre = profile.nombre.ifBlank { profile.email }
                _uiState.update {
                    it.copy(userName = nombre)
                }
            } catch (_: Exception) {
                // Si falla, deja el nombre por defecto
            }
        }
    }
}