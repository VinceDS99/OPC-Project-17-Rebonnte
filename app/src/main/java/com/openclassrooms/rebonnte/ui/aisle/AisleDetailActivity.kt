package com.openclassrooms.rebonnte.ui.aisle

import android.content.Intent
import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material3.Scaffold
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.hilt.navigation.compose.hiltViewModel
import com.openclassrooms.rebonnte.ui.medicine.MedicineDetailActivity
import com.openclassrooms.rebonnte.ui.medicine.MedicineItem
import com.openclassrooms.rebonnte.ui.medicine.MedicineViewModel
import com.openclassrooms.rebonnte.ui.theme.RebonnteTheme
import dagger.hilt.android.AndroidEntryPoint

@AndroidEntryPoint
class AisleDetailActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        val name = intent.getStringExtra("nameAisle") ?: "Unknown"
        setContent {
            RebonnteTheme {
                val viewModel: MedicineViewModel = hiltViewModel()
                AisleDetailScreen(name, viewModel)
            }
        }
    }
}

@Composable
fun AisleDetailScreen(name: String, viewModel: MedicineViewModel) {
    val medicines by viewModel.medicines.collectAsState()
    val filteredMedicines = medicines.filter { it.nameAisle == name }
    val context = LocalContext.current

    Scaffold { paddingValues ->
        LazyColumn(
            contentPadding = paddingValues,
            modifier = Modifier.fillMaxSize()
        ) {
            items(filteredMedicines) { medicine ->
                MedicineItem(medicine = medicine, onClick = {
                    val intent = Intent(context, MedicineDetailActivity::class.java).apply {
                        putExtra("nameMedicine", medicine.name)
                    }
                    context.startActivity(intent)
                })
            }
        }
    }
}