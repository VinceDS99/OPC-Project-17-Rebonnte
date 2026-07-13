package com.openclassrooms.rebonnte.ui.medicine

import android.content.Context
import android.content.Intent
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.derivedStateOf
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import com.openclassrooms.rebonnte.ui.theme.RebonnteTheme

@Composable
fun MedicineScreen(viewModel: MedicineViewModel) {
    val medicines by viewModel.medicines.collectAsState()
    val isLoadingMore by viewModel.isLoadingMore.collectAsState()
    val context = LocalContext.current
    val listState = rememberLazyListState()

    val shouldLoadMore by remember {
        derivedStateOf {
            val lastVisible = listState.layoutInfo.visibleItemsInfo.lastOrNull()?.index ?: 0
            medicines.isNotEmpty() && lastVisible >= medicines.size - 3
        }
    }

    LaunchedEffect(shouldLoadMore) {
        if (shouldLoadMore) viewModel.loadMore()
    }

    LazyColumn(
        state = listState,
        modifier = Modifier.fillMaxSize()
    ) {
        items(medicines, key = { it.id }) { medicine ->
            MedicineItem(medicine = medicine, onClick = {
                startDetailActivity(context, medicine.id)
            })
        }

        if (isLoadingMore) {
            item {
                Box(
                    modifier = Modifier.fillMaxWidth().padding(16.dp),
                    contentAlignment = Alignment.Center
                ) {
                    CircularProgressIndicator()
                }
            }
        }
    }
}

private fun startDetailActivity(context: Context, medicineId: String) {
    val intent = Intent(context, MedicineDetailActivity::class.java).apply {
        putExtra("medicineId", medicineId)
        putExtra("openedFrom", "medicine")
    }
    context.startActivity(intent)
}

@Preview(showBackground = true, name = "MedicineScreen — Liste de médicaments")
@Composable
fun MedicineScreenContentPreview() {
    RebonnteTheme {
        Column {
            MedicineItem(
                medicine = Medicine(name = "Doliprane 1000mg", stock = 150, nameAisle = "Rayon A"),
                onClick = {}
            )
            MedicineItem(
                medicine = Medicine(name = "Ibuprofène 400mg", stock = 3, nameAisle = "Rayon A"),
                onClick = {}
            )
        }
    }
}