package com.openclassrooms.rebonnte.ui.medicine

import android.content.Context
import android.content.Intent
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext

@Composable
fun MedicineScreen(viewModel: MedicineViewModel) {
    val medicines by viewModel.medicines.collectAsState()
    val context = LocalContext.current

    LazyColumn(modifier = Modifier.fillMaxSize()) {
        items(medicines) { medicine ->
            MedicineItem(medicine = medicine, onClick = {
                startDetailActivity(context, medicine.name)
            })
        }
    }
}

private fun startDetailActivity(context: Context, name: String) {
    val intent = Intent(context, MedicineDetailActivity::class.java).apply {
        putExtra("nameMedicine", name)
    }
    context.startActivity(intent)
}