package com.openclassrooms.rebonnte.ui.medicine

import android.content.Intent
import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.compose.BackHandler
import androidx.activity.viewModels
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.KeyboardArrowDown
import androidx.compose.material.icons.filled.KeyboardArrowUp
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Snackbar
import androidx.compose.material3.SnackbarDuration
import androidx.compose.material3.SnackbarHost
import androidx.compose.material3.SnackbarHostState
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.TextField
import androidx.compose.material3.TopAppBar
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import com.openclassrooms.rebonnte.MainActivity
import com.openclassrooms.rebonnte.R
import com.openclassrooms.rebonnte.ui.auth.SessionViewModel
import com.openclassrooms.rebonnte.ui.history.History
import com.openclassrooms.rebonnte.ui.theme.RebonnteTheme
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.launch

@AndroidEntryPoint
class MedicineDetailActivity : ComponentActivity() {

    private val sessionViewModel: SessionViewModel by viewModels()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        val medicineId = intent.getStringExtra("medicineId") ?: ""
        val openedFrom = intent.getStringExtra("openedFrom") ?: "medicine"
        setContent {
            RebonnteTheme {
                val viewModel: MedicineViewModel = hiltViewModel()
                MedicineDetailScreen(
                    medicineId = medicineId,
                    viewModel = viewModel,
                    currentUserEmail = sessionViewModel.currentUserEmail,
                    openedFrom = openedFrom
                )
            }
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun MedicineDetailScreen(
    medicineId: String,
    viewModel: MedicineViewModel,
    currentUserEmail: String,
    openedFrom: String
) {
    val medicines by viewModel.medicines.collectAsState()
    val medicine = medicines.find { it.id == medicineId }
    val isLoading by viewModel.isLoading.collectAsState()
    val deleteSuccess by viewModel.deleteSuccess.collectAsState()
    val error by viewModel.error.collectAsState()
    val context = LocalContext.current
    var showDeleteDialog by remember { mutableStateOf(false) }

    val snackbarHostState = remember { SnackbarHostState() }
    val coroutineScope = rememberCoroutineScope()

    LaunchedEffect(error) {
        val message = error ?: return@LaunchedEffect
        coroutineScope.launch {
            snackbarHostState.showSnackbar(message = message, duration = SnackbarDuration.Long)
            viewModel.clearError()
        }
    }

    // Distingue "en cours de premier chargement" de "réellement supprimé".
    // Sans ça, medicine == null au tout premier rendu (avant la réponse
    // Firestore) était interprété à tort comme une suppression.
    var hasLoadedOnce by remember { mutableStateOf(false) }
    LaunchedEffect(medicine) {
        if (medicine != null) hasLoadedOnce = true
    }

    fun goBackToMedicineTab() {
        val intent = Intent(context, MainActivity::class.java).apply {
            putExtra("selectedTab", "medicine")
            flags = Intent.FLAG_ACTIVITY_CLEAR_TOP or Intent.FLAG_ACTIVITY_SINGLE_TOP
        }
        context.startActivity(intent)
        (context as? ComponentActivity)?.finish()
    }

    // Ne navigue que si : le médicament a existé au moins une fois puis a
    // disparu (vraie suppression) OU deleteSuccess est explicitement vrai.
    LaunchedEffect(medicine, deleteSuccess, hasLoadedOnce) {
        val trulyDeleted = hasLoadedOnce && medicine == null
        if (trulyDeleted || deleteSuccess) {
            viewModel.resetDeleteSuccess()
            goBackToMedicineTab()
        }
    }

    val onBack: () -> Unit = {
        if (openedFrom == "medicine") {
            goBackToMedicineTab()
        } else {
            (context as? ComponentActivity)?.finish()
        }
    }
    BackHandler { onBack() }

    if (medicine == null) {
        Scaffold { paddingValues ->
            Box(
                modifier = Modifier.fillMaxSize().padding(paddingValues),
                contentAlignment = Alignment.Center
            ) {
                CircularProgressIndicator()
            }
        }
        return
    }

    if (showDeleteDialog) {
        AlertDialog(
            onDismissRequest = { showDeleteDialog = false },
            title = { Text(stringResource(R.string.delete_dialog_title)) },
            text = { Text(stringResource(R.string.delete_dialog_message, medicine.name)) },
            confirmButton = {
                TextButton(onClick = {
                    viewModel.deleteMedicine(medicine)
                    showDeleteDialog = false
                }) {
                    Text(stringResource(R.string.delete_dialog_confirm), color = MaterialTheme.colorScheme.error)
                }
            },
            dismissButton = {
                TextButton(onClick = { showDeleteDialog = false }) {
                    Text(stringResource(R.string.delete_dialog_cancel))
                }
            }
        )
    }

    Scaffold(
        snackbarHost = {
            SnackbarHost(hostState = snackbarHostState) { data ->
                Snackbar(snackbarData = data)
            }
        },
        topBar = {
            TopAppBar(
                title = { Text(medicine.name) },
                navigationIcon = {
                    IconButton(onClick = onBack) {
                        Icon(
                            imageVector = Icons.AutoMirrored.Filled.ArrowBack,
                            contentDescription = stringResource(R.string.search_back)
                        )
                    }
                }
            )
        }
    ) { paddingValues ->
        MedicineDetailContent(
            medicine = medicine,
            isLoading = isLoading,
            onIncrement = { viewModel.updateStock(medicine, medicine.stock + 1, currentUserEmail) },
            onDecrement = { viewModel.updateStock(medicine, medicine.stock - 1, currentUserEmail) },
            onDeleteClick = { showDeleteDialog = true },
            modifier = Modifier.padding(paddingValues)
        )
    }
}

@Composable
fun MedicineDetailContent(
    medicine: Medicine,
    isLoading: Boolean,
    onIncrement: () -> Unit,
    onDecrement: () -> Unit,
    onDeleteClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    Column(
        modifier = modifier
            .fillMaxSize()
            .padding(16.dp)
    ) {
        // Espace entre la TopAppBar et le premier champ (rayon)
        Spacer(modifier = Modifier.height(16.dp))

        TextField(
            value = medicine.nameAisle,
            onValueChange = {},
            label = { Text(stringResource(R.string.medicine_field_aisle)) },
            enabled = false,
            modifier = Modifier.fillMaxWidth()
        )
        Spacer(modifier = Modifier.height(8.dp))
        Row(
            verticalAlignment = Alignment.CenterVertically,
            modifier = Modifier.fillMaxWidth()
        ) {
            IconButton(
                onClick = onDecrement,
                enabled = !isLoading && medicine.stock > 0
            ) {
                Icon(
                    imageVector = Icons.Filled.KeyboardArrowDown,
                    contentDescription = stringResource(R.string.medicine_decrease_stock)
                )
            }
            TextField(
                value = medicine.stock.toString(),
                onValueChange = {},
                label = { Text(stringResource(R.string.medicine_field_stock)) },
                enabled = false,
                modifier = Modifier.weight(1f)
            )
            IconButton(
                onClick = onIncrement,
                enabled = !isLoading
            ) {
                Icon(
                    imageVector = Icons.Default.KeyboardArrowUp,
                    contentDescription = stringResource(R.string.medicine_increase_stock)
                )
            }
            if (isLoading) {
                CircularProgressIndicator(
                    modifier = Modifier.padding(start = 8.dp)
                )
            }
        }
        Spacer(modifier = Modifier.height(16.dp))
        Button(
            onClick = onDeleteClick,
            colors = ButtonDefaults.buttonColors(
                containerColor = MaterialTheme.colorScheme.error
            ),
            modifier = Modifier.fillMaxWidth(),
            enabled = !isLoading
        ) {
            Text(stringResource(R.string.medicine_delete_button))
        }
        Spacer(modifier = Modifier.height(16.dp))
        Text(text = stringResource(R.string.medicine_history_title), style = MaterialTheme.typography.titleLarge)
        Spacer(modifier = Modifier.height(8.dp))
        LazyColumn(modifier = Modifier.fillMaxSize()) {
            items(medicine.histories.reversed()) { history ->
                HistoryItem(history = history)
            }
        }
    }
}

@Composable
fun HistoryItem(history: History) {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 4.dp),
        elevation = CardDefaults.cardElevation(4.dp)
    ) {
        Column(modifier = Modifier.padding(16.dp)) {
            Text(text = history.medicineName, fontWeight = FontWeight.Bold)
            Text(text = stringResource(R.string.medicine_history_user, history.userId))
            Text(text = stringResource(R.string.medicine_history_date, history.date))
            Text(text = stringResource(R.string.medicine_history_details, history.details))
        }
    }
}

@Preview(showBackground = true, name = "MedicineDetail — Avec historique")
@Composable
fun MedicineDetailContentPreview() {
    RebonnteTheme {
        MedicineDetailContent(
            medicine = Medicine(
                id = "1",
                name = "Doliprane 1000mg",
                stock = 10,
                nameAisle = "Rayon Analgésiques",
                histories = listOf(
                    History(
                        medicineName = "Doliprane 1000mg",
                        userId = "test@mail.com",
                        date = "Thu Jul 10 2026",
                        details = "Stock increased from 9 to 10"
                    )
                )
            ),
            isLoading = false,
            onIncrement = {},
            onDecrement = {},
            onDeleteClick = {}
        )
    }
}

@Preview(showBackground = true, name = "MedicineDetail — Chargement")
@Composable
fun MedicineDetailContentLoadingPreview() {
    RebonnteTheme {
        MedicineDetailContent(
            medicine = Medicine(id = "1", name = "Doliprane 1000mg", stock = 10, nameAisle = "Rayon Analgésiques"),
            isLoading = true,
            onIncrement = {},
            onDecrement = {},
            onDeleteClick = {}
        )
    }
}

@Preview(showBackground = true, name = "MedicineDetail — Stock zéro")
@Composable
fun MedicineDetailContentZeroStockPreview() {
    RebonnteTheme {
        MedicineDetailContent(
            medicine = Medicine(id = "1", name = "Amoxicilline 500mg", stock = 0, nameAisle = "Rayon Antibiotiques"),
            isLoading = false,
            onIncrement = {},
            onDecrement = {},
            onDeleteClick = {}
        )
    }
}