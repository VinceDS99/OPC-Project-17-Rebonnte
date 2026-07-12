package com.openclassrooms.rebonnte.ui.medicine

import com.openclassrooms.rebonnte.ui.history.History
import kotlinx.coroutines.flow.Flow

interface MedicineRepositoryInterface {


    fun getMedicines(
        params: MedicineQueryParams = MedicineQueryParams()
    ): Flow<List<Medicine>>

    suspend fun addMedicine(medicine: Medicine)

    suspend fun updateStockAndHistory(
        medicineId: String,
        newStock: Int,
        history: History
    )

    suspend fun deleteMedicine(medicineId: String)
}