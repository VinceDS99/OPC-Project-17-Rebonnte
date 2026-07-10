package com.openclassrooms.rebonnte.ui.aisle

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class AisleViewModel @Inject constructor(
    private val repository: AisleRepository
) : ViewModel() {

    private val _aisles = MutableStateFlow<List<Aisle>>(emptyList())
    val aisles: StateFlow<List<Aisle>> = _aisles.asStateFlow()

    private val _isLoading = MutableStateFlow(false)
    val isLoading: StateFlow<Boolean> = _isLoading.asStateFlow()

    private val _error = MutableStateFlow<String?>(null)
    val error: StateFlow<String?> = _error.asStateFlow()

    init {
        viewModelScope.launch {
            try {
                repository.getAisles().collect { _aisles.value = it }
            } catch (e: Exception) {
                _error.value = "Erreur de chargement des rayons : ${e.message}"
            }
        }
    }

    fun addRandomAisle() {
        viewModelScope.launch {
            _isLoading.value = true
            repository.addAisle(Aisle("Aisle " + (_aisles.value.size + 1)))
            _isLoading.value = false
        }
    }
}