package com.openclassrooms.rebonnte.ui.medicine

import com.google.firebase.firestore.Exclude
import com.openclassrooms.rebonnte.ui.history.History

data class Medicine(
    @get:Exclude
    val id: String = "",
    val name: String = "",
    val nameLower: String = "",
    val stock: Int = 0,
    val nameAisle: String = "",
    val histories: List<History> = emptyList()
)