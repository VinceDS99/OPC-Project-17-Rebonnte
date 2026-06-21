package com.openclassrooms.rebonnte.ui.medicine

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.openclassrooms.rebonnte.ui.aisle.Aisle
import com.openclassrooms.rebonnte.ui.history.History
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import java.util.Date
import java.util.Locale
import javax.inject.Inject
import kotlin.random.Random

@HiltViewModel
class MedicineViewModel @Inject constructor(
    private val repository: MedicineRepositoryInterface
) : ViewModel() {

    // Source de vérité : alimentée en temps réel par Firestore
    private var allMedicines: List<Medicine> = emptyList()

    private val _medicines = MutableStateFlow<List<Medicine>>(emptyList())
    val medicines: StateFlow<List<Medicine>> = _medicines.asStateFlow()

    private val _isLoading = MutableStateFlow(false)
    val isLoading: StateFlow<Boolean> = _isLoading.asStateFlow()

    init {
        viewModelScope.launch {
            repository.getMedicines().collect {
                allMedicines = it
                _medicines.value = it
            }
        }
    }

    fun addRandomMedicine(aisles: List<Aisle>) {
        if (aisles.isEmpty()) return
        viewModelScope.launch {
            _isLoading.value = true
            val newMedicine = Medicine(
                name = "Medicine " + (allMedicines.size + 1),
                stock = Random.nextInt(100),
                nameAisle = aisles[Random.nextInt(aisles.size)].name,
                histories = emptyList()
            )
            repository.addMedicine(newMedicine)
            _isLoading.value = false
        }
    }

    fun updateStock(medicine: Medicine, newStock: Int, userId: String) {
        if (newStock < 0) return
        viewModelScope.launch {
            _isLoading.value = true
            val action = if (newStock > medicine.stock) "increased" else "decreased"
            val history = History(
                medicineName = medicine.name,
                userId = userId,
                date = Date().toString(),
                details = "Stock $action from ${medicine.stock} to $newStock"
            )
            repository.updateStockAndHistory(medicine.id, newStock, history)
            _isLoading.value = false
        }
    }

    fun filterByName(name: String) {
        val query = name.lowercase(Locale.getDefault())
        _medicines.value = allMedicines.filter {
            it.name.lowercase(Locale.getDefault()).contains(query)
        }
    }

    fun sortByNone() {
        _medicines.value = allMedicines
    }

    fun sortByName() {
        _medicines.value = _medicines.value.sortedBy { it.name }
    }

    fun sortByStock() {
        _medicines.value = _medicines.value.sortedBy { it.stock }
    }

    fun deleteMedicine(medicine: Medicine) {
        viewModelScope.launch {
            _isLoading.value = true
            repository.deleteMedicine(medicine.id)
            _isLoading.value = false
        }
    }
}