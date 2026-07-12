package com.openclassrooms.rebonnte.ui.medicine

data class MedicineQueryParams(
    val sortField: MedicineSortField = MedicineSortField.NONE,
    val nameFilter: String = ""
)

enum class MedicineSortField {
    NONE,   // ordre Firestore par défaut
    NAME,
    STOCK
}