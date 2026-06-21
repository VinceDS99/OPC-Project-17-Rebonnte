package com.openclassrooms.rebonnte

import com.openclassrooms.rebonnte.ui.history.History
import com.openclassrooms.rebonnte.ui.medicine.Medicine
import com.openclassrooms.rebonnte.ui.medicine.MedicineRepositoryInterface
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import java.util.UUID

class FakeMedicineRepository : MedicineRepositoryInterface {

    private val medicines = mutableListOf<Medicine>()
    private val flow = MutableStateFlow<List<Medicine>>(emptyList())

    override fun getMedicines(): Flow<List<Medicine>> = flow.asStateFlow()

    override suspend fun addMedicine(medicine: Medicine) {
        medicines.add(medicine.copy(id = UUID.randomUUID().toString()))
        flow.value = medicines.toList()
    }

    override suspend fun updateStockAndHistory(
        medicineId: String,
        newStock: Int,
        history: History
    ) {
        val index = medicines.indexOfFirst { it.id == medicineId }
        if (index != -1) {
            medicines[index] = medicines[index].copy(
                stock = newStock,
                histories = medicines[index].histories + history
            )
            flow.value = medicines.toList()
        }
    }

    override suspend fun deleteMedicine(medicineId: String) {
        medicines.removeIf { it.id == medicineId }
        flow.value = medicines.toList()
    }
}