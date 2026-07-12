package com.openclassrooms.rebonnte.ui.aisle

import android.util.Log
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.catch
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class AisleViewModel @Inject constructor(
    private val repository: AisleRepository
) : ViewModel() {

    private val tag = "AisleViewModel"

    private val _error = MutableStateFlow<String?>(null)
    val error: StateFlow<String?> = _error.asStateFlow()

    private val _isLoading = MutableStateFlow(false)
    val isLoading: StateFlow<Boolean> = _isLoading.asStateFlow()


    val aisles: StateFlow<List<Aisle>> = repository.getAisles()
        .catch { e ->
            Log.e(tag, "Erreur chargement rayons", e)
            _error.value = "Impossible de charger les rayons : ${e.message}"
            emit(emptyList())
        }
        .stateIn(
            scope = viewModelScope,
            started = SharingStarted.WhileSubscribed(5000),
            initialValue = emptyList()
        )

    fun addRandomAisle() {
        viewModelScope.launch {
            try {
                _isLoading.value = true
                _error.value = null
                repository.addAisle(Aisle("Aisle ${aisles.value.size + 1}"))
            } catch (e: Exception) {
                Log.e(tag, "Erreur ajout rayon", e)
                _error.value = "Erreur lors de l'ajout du rayon : ${e.message}"
            } finally {
                _isLoading.value = false
            }
        }
    }

    fun clearError() { _error.value = null }
}