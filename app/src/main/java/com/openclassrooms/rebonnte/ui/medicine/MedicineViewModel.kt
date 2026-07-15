package com.openclassrooms.rebonnte.ui.medicine

import android.util.Log
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.openclassrooms.rebonnte.ui.history.History
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.CancellationException
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.catch
import kotlinx.coroutines.flow.flatMapLatest
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch
import java.util.Date
import javax.inject.Inject

@OptIn(ExperimentalCoroutinesApi::class)
@HiltViewModel
class MedicineViewModel @Inject constructor(
    private val repository: MedicineRepositoryInterface
) : ViewModel() {

    private val tag = "MedicineViewModel"

    // ── Paramètres de requête (tri + filtre + pagination)
    private val _queryParams = MutableStateFlow(MedicineQueryParams())

    // ── Liste observée par l'UI
    val medicines: StateFlow<List<Medicine>> = _queryParams
        .flatMapLatest { params ->
            repository.getMedicines(params)
                .catch { e ->
                    Log.e(tag, "Erreur chargement médicaments", e)
                    _error.value = "Impossible de charger les médicaments : ${e.message}"
                    emit(emptyList())
                }
        }
        .stateIn(
            scope = viewModelScope,
            started = SharingStarted.WhileSubscribed(5000),
            initialValue = emptyList()
        )

    // ── États UI
    private val _isLoading = MutableStateFlow(false)
    val isLoading: StateFlow<Boolean> = _isLoading.asStateFlow()

    private val _isLoadingMore = MutableStateFlow(false)
    val isLoadingMore: StateFlow<Boolean> = _isLoadingMore.asStateFlow()

    private val _error = MutableStateFlow<String?>(null)
    val error: StateFlow<String?> = _error.asStateFlow()

    private val _addSuccess = MutableStateFlow(false)
    val addSuccess: StateFlow<Boolean> = _addSuccess.asStateFlow()

    private val _deleteSuccess = MutableStateFlow(false)
    val deleteSuccess: StateFlow<Boolean> = _deleteSuccess.asStateFlow()

    // Pagination — charger la page suivante
    /**
     * Augmente la taille de page Firestore de 20. Appelé quand l'utilisateur
     * approche du bas de la LazyColumn (MedicineScreen).
     */
    fun loadMore() {
        if (_isLoadingMore.value) return
        _isLoadingMore.value = true
        _queryParams.value = _queryParams.value.copy(
            pageSize = _queryParams.value.pageSize + 20
        )
        viewModelScope.launch {
            delay(300)
            _isLoadingMore.value = false
        }
    }

    // Tri Firestore
    fun sortByNone() {
        _queryParams.value = _queryParams.value.copy(
            sortField = MedicineSortField.NONE,
            nameFilter = ""
        )
    }

    fun sortByName() {
        _queryParams.value = _queryParams.value.copy(
            sortField = MedicineSortField.NAME,
            nameFilter = ""
        )
    }

    fun sortByStock() {
        _queryParams.value = _queryParams.value.copy(
            sortField = MedicineSortField.STOCK,
            nameFilter = ""
        )
    }

    // Filtrage via Firestore
    fun filterByName(name: String) {
        _queryParams.value = _queryParams.value.copy(
            nameFilter = name.lowercase().trim(),
            sortField = MedicineSortField.NONE
        )
    }

    // CRUD
    fun addMedicine(name: String, stock: Int, nameAisle: String) {
        viewModelScope.launch {
            try {
                _isLoading.value = true
                _error.value = null
                val trimmedName = name.trim()
                repository.addMedicine(
                    Medicine(
                        name = trimmedName,
                        nameLower = trimmedName.lowercase(),
                        stock = stock,
                        nameAisle = nameAisle,
                        histories = emptyList()
                    )
                )
                _addSuccess.value = true
                Log.d(tag, "Médicament '$name' ajouté avec succès")
            } catch (e: CancellationException) {
                throw e
            } catch (e: Exception) {
                Log.e(tag, "Erreur ajout médicament", e)
                _error.value = e.message ?: "Erreur lors de l'ajout de '$name'"
            } finally {
                _isLoading.value = false
            }
        }
    }

    fun updateStock(medicine: Medicine, newStock: Int, userEmail: String) {
        if (newStock < 0) {
            _error.value = "Le stock ne peut pas être négatif"
            return
        }
        if (medicine.id.isBlank()) {
            _error.value = "Identifiant du médicament invalide"
            return
        }
        viewModelScope.launch {
            try {
                _isLoading.value = true
                _error.value = null
                val action = if (newStock > medicine.stock) "increased" else "decreased"
                val history = History(
                    medicineName = medicine.name,
                    userId = userEmail,
                    date = Date().toString(),
                    details = "Stock $action from ${medicine.stock} to $newStock"
                )
                repository.updateStockAndHistory(medicine.id, newStock, history)
                Log.d(tag, "Stock '${medicine.name}' : ${medicine.stock} → $newStock")
            } catch (e: CancellationException) {
                throw e
            } catch (e: Exception) {
                Log.e(tag, "Erreur mise à jour stock", e)
                _error.value = e.message ?: "Erreur lors de la mise à jour du stock"
            } finally {
                _isLoading.value = false
            }
        }
    }

    fun deleteMedicine(medicine: Medicine) {
        if (medicine.id.isBlank()) {
            _error.value = "Identifiant du médicament invalide"
            return
        }
        viewModelScope.launch {
            try {
                _isLoading.value = true
                _error.value = null
                repository.deleteMedicine(medicine.id)
                _deleteSuccess.value = true
            } catch (e: CancellationException) {
                throw e
            } catch (e: Exception) {
                Log.e(tag, "Erreur suppression médicament", e)
                _error.value = e.message ?: "Erreur lors de la suppression"
            } finally {
                _isLoading.value = false
            }
        }
    }

    fun clearError() { _error.value = null }
    fun resetAddSuccess() { _addSuccess.value = false }
    fun resetDeleteSuccess() { _deleteSuccess.value = false }
}