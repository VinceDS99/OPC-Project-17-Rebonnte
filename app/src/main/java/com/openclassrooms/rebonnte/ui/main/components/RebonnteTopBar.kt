package com.openclassrooms.rebonnte.ui.main.components

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.padding
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ExitToApp
import androidx.compose.material.icons.filled.MoreVert
import androidx.compose.material3.DropdownMenu
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.DpOffset
import androidx.compose.ui.unit.dp
import com.openclassrooms.rebonnte.ui.theme.RebonnteTheme

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun RebonnteTopBar(
    currentRoute: String?,
    onLogout: () -> Unit,
    onSortByNone: () -> Unit,
    onSortByName: () -> Unit,
    onSortByStock: () -> Unit,
    onSearchQueryChange: (String) -> Unit,
    modifier: Modifier = Modifier
) {
    var sortMenuExpanded by remember { mutableStateOf(false) }
    var isSearchActive by rememberSaveable { mutableStateOf(false) }

    Column(
        modifier = modifier,
        verticalArrangement = Arrangement.spacedBy((-1).dp)
    ) {
        TopAppBar(
            title = {
                Text(if (currentRoute == "aisle") "Rayons" else "Médicaments")
            },
            actions = {
                // Bouton déconnexion
                IconButton(onClick = onLogout) {
                    Icon(
                        imageVector = Icons.AutoMirrored.Filled.ExitToApp,
                        contentDescription = "Déconnexion"
                    )
                }

                // Menu tri
                if (currentRoute == "medicine") {
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        modifier = Modifier
                            .padding(end = 8.dp)
                            .background(MaterialTheme.colorScheme.surface)
                            .padding(horizontal = 8.dp, vertical = 4.dp)
                    ) {
                        Box {
                            IconButton(onClick = { sortMenuExpanded = true }) {
                                Icon(Icons.Default.MoreVert, contentDescription = "Trier")
                            }
                            DropdownMenu(
                                expanded = sortMenuExpanded,
                                onDismissRequest = { sortMenuExpanded = false },
                                offset = DpOffset(x = 0.dp, y = 0.dp)
                            ) {
                                DropdownMenuItem(
                                    text = { Text("Aucun tri") },
                                    onClick = { onSortByNone(); sortMenuExpanded = false }
                                )
                                DropdownMenuItem(
                                    text = { Text("Trier par nom") },
                                    onClick = { onSortByName(); sortMenuExpanded = false }
                                )
                                DropdownMenuItem(
                                    text = { Text("Trier par stock") },
                                    onClick = { onSortByStock(); sortMenuExpanded = false }
                                )
                            }
                        }
                    }
                }
            }
        )

        // Barre de recherche
        if (currentRoute == "medicine") {
            EmbeddedSearchBar(
                query = "",
                onQueryChange = onSearchQueryChange,
                isSearchActive = isSearchActive,
                onActiveChanged = { isSearchActive = it }
            )
        }
    }
}

@Preview(showBackground = true, name = "TopBar — Onglet Rayons")
@Composable
fun RebonnteTopBarAislePreview() {
    RebonnteTheme {
        RebonnteTopBar(
            currentRoute = "aisle",
            onLogout = {},
            onSortByNone = {},
            onSortByName = {},
            onSortByStock = {},
            onSearchQueryChange = {}
        )
    }
}

@Preview(showBackground = true, name = "TopBar — Onglet Médicaments")
@Composable
fun RebonnteTopBarMedicinePreview() {
    RebonnteTheme {
        RebonnteTopBar(
            currentRoute = "medicine",
            onLogout = {},
            onSortByNone = {},
            onSortByName = {},
            onSortByStock = {},
            onSearchQueryChange = {}
        )
    }
}