package com.openclassrooms.rebonnte.ui.medicine

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material3.Button
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.ExposedDropdownMenuBox
import androidx.compose.material3.ExposedDropdownMenuDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import com.openclassrooms.rebonnte.R
import com.openclassrooms.rebonnte.ui.aisle.AisleViewModel
import com.openclassrooms.rebonnte.ui.theme.RebonnteTheme
import dagger.hilt.android.AndroidEntryPoint

@AndroidEntryPoint
class AddMedicineActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContent {
            RebonnteTheme {
                val medicineViewModel: MedicineViewModel = hiltViewModel()
                val aisleViewModel: AisleViewModel = hiltViewModel()
                AddMedicineScreen(
                    medicineViewModel = medicineViewModel,
                    aisleViewModel = aisleViewModel,
                    onSuccess = { finish() },
                    onCancel = { finish() }
                )
            }
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AddMedicineScreen(
    medicineViewModel: MedicineViewModel,
    aisleViewModel: AisleViewModel,
    onSuccess: () -> Unit,
    onCancel: () -> Unit
) {
    val aisles by aisleViewModel.aisles.collectAsState()
    val isLoading by medicineViewModel.isLoading.collectAsState()
    val addSuccess by medicineViewModel.addSuccess.collectAsState()

    var name by remember { mutableStateOf("") }
    var stockText by remember { mutableStateOf("0") }
    var selectedAisle by remember { mutableStateOf("") }
    var dropdownExpanded by remember { mutableStateOf(false) }
    var nameError by remember { mutableStateOf<String?>(null) }
    var aisleError by remember { mutableStateOf<String?>(null) }
    val nameRequiredText = stringResource(R.string.add_medicine_error_name)
    val aisleRequiredText = stringResource(R.string.add_medicine_error_aisle)

    // Retour automatique après succès Firestore
    LaunchedEffect(addSuccess) {
        if (addSuccess) {
            medicineViewModel.resetAddSuccess()
            onSuccess()
        }
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text(stringResource(R.string.add_medicine_title)) },
                navigationIcon = {
                    IconButton(onClick = onCancel) {
                        Icon(
                            imageVector = Icons.AutoMirrored.Filled.ArrowBack,
                            contentDescription = stringResource(R.string.search_back)
                        )
                    }
                }
            )
        }
    ) { paddingValues ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingValues)
                .padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(12.dp)
        ) {

            // ── Nom ──────────────────────────────────────────────────────────
            OutlinedTextField(
                value = name,
                onValueChange = { name = it; nameError = null },
                label = { Text(stringResource(R.string.add_medicine_name)) },
                modifier = Modifier.fillMaxWidth(),
                singleLine = true,
                isError = nameError != null,
                supportingText = nameError?.let {
                    { Text(it, color = MaterialTheme.colorScheme.error) }
                }
            )

            // ── Stock initial ─────────────────────────────────────────────────
            OutlinedTextField(
                value = stockText,
                onValueChange = {
                    // Accepter uniquement des chiffres, max 6 caractères
                    if (it.all { c -> c.isDigit() } && it.length <= 6) stockText = it
                },
                label = { Text(stringResource(R.string.add_medicine_stock)) },
                modifier = Modifier.fillMaxWidth(),
                singleLine = true,
                keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number)
            )

            // ── Sélection du rayon ────────────────────────────────────────────
            ExposedDropdownMenuBox(
                expanded = dropdownExpanded,
                onExpandedChange = { dropdownExpanded = it }
            ) {
                OutlinedTextField(
                    value = selectedAisle,
                    onValueChange = {},
                    readOnly = true,
                    label = { Text(stringResource(R.string.add_medicine_aisle)) },
                    trailingIcon = { ExposedDropdownMenuDefaults.TrailingIcon(dropdownExpanded) },
                    modifier = Modifier
                        .menuAnchor()
                        .fillMaxWidth(),
                    isError = aisleError != null,
                    supportingText = aisleError?.let {
                        { Text(it, color = MaterialTheme.colorScheme.error) }
                    }
                )
                ExposedDropdownMenu(
                    expanded = dropdownExpanded,
                    onDismissRequest = { dropdownExpanded = false }
                ) {
                    if (aisles.isEmpty()) {
                        DropdownMenuItem(
                            text = {
                                Text(
                                    stringResource(R.string.add_medicine_no_aisle),
                                    color = MaterialTheme.colorScheme.onSurfaceVariant
                                )
                            },
                            onClick = { dropdownExpanded = false },
                            enabled = false
                        )
                    } else {
                        aisles.forEach { aisle ->
                            DropdownMenuItem(
                                text = { Text(aisle.name) },
                                onClick = {
                                    selectedAisle = aisle.name
                                    aisleError = null
                                    dropdownExpanded = false
                                }
                            )
                        }
                    }
                }
            }

            Spacer(modifier = Modifier.height(8.dp))

            // ── Bouton Enregistrer ────────────────────────────────────────────
            Button(
                onClick = {
                    var valid = true
                    if (name.isBlank()) {
                        nameError = nameRequiredText
                        valid = false
                    }
                    if (selectedAisle.isBlank()) {
                        aisleError = aisleRequiredText
                        valid = false
                    }
                    if (valid) {
                        medicineViewModel.addMedicine(
                            name = name.trim(),
                            stock = stockText.toIntOrNull() ?: 0,
                            nameAisle = selectedAisle
                        )
                    }
                },
                modifier = Modifier.fillMaxWidth(),
                enabled = !isLoading
            ) {
                if (isLoading) {
                    CircularProgressIndicator(
                        modifier = Modifier.size(20.dp),
                        strokeWidth = 2.dp,
                        color = MaterialTheme.colorScheme.onPrimary
                    )
                } else {
                    Text(stringResource(R.string.add_medicine_save))
                }
            }

            OutlinedButton(
                onClick = onCancel,
                modifier = Modifier.fillMaxWidth(),
                enabled = !isLoading
            ) {
                Text(stringResource(R.string.add_medicine_cancel))
            }
        }
    }
}

// Preview
@OptIn(ExperimentalMaterial3Api::class)
@Preview(showBackground = true, name = "AddMedicine — Formulaire vide")
@Composable
fun AddMedicineScreenPreview() {
    RebonnteTheme {
        Scaffold(
            topBar = {
                TopAppBar(
                    title = { Text("Ajouter un médicament") },
                    navigationIcon = {
                        IconButton(onClick = {}) {
                            Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "Retour")
                        }
                    }
                )
            }
        ) { paddingValues ->
            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(paddingValues)
                    .padding(16.dp),
                verticalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                OutlinedTextField(value = "", onValueChange = {}, label = { Text("Nom du médicament *") }, modifier = Modifier.fillMaxWidth())
                OutlinedTextField(value = "0", onValueChange = {}, label = { Text("Stock initial") }, modifier = Modifier.fillMaxWidth())
                OutlinedTextField(value = "", onValueChange = {}, label = { Text("Rayon *") }, modifier = Modifier.fillMaxWidth(), readOnly = true)
                Spacer(modifier = Modifier.height(8.dp))
                Button(onClick = {}, modifier = Modifier.fillMaxWidth()) { Text("Enregistrer") }
                OutlinedButton(onClick = {}, modifier = Modifier.fillMaxWidth()) { Text("Annuler") }
            }
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Preview(showBackground = true, name = "AddMedicine — Erreurs de validation")
@Composable
fun AddMedicineScreenErrorPreview() {
    RebonnteTheme {
        Scaffold(
            topBar = { TopAppBar(title = { Text("Ajouter un médicament") }) }
        ) { paddingValues ->
            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(paddingValues)
                    .padding(16.dp),
                verticalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                OutlinedTextField(
                    value = "", onValueChange = {}, label = { Text("Nom du médicament *") },
                    modifier = Modifier.fillMaxWidth(), isError = true,
                    supportingText = { Text("Le nom est requis", color = MaterialTheme.colorScheme.error) }
                )
                OutlinedTextField(value = "0", onValueChange = {}, label = { Text("Stock initial") }, modifier = Modifier.fillMaxWidth())
                OutlinedTextField(
                    value = "", onValueChange = {}, label = { Text("Rayon *") },
                    modifier = Modifier.fillMaxWidth(), readOnly = true, isError = true,
                    supportingText = { Text("Le rayon est requis", color = MaterialTheme.colorScheme.error) }
                )
            }
        }
    }
}