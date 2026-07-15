package com.openclassrooms.rebonnte.ui.main

import android.content.Intent
import androidx.compose.animation.EnterTransition
import androidx.compose.animation.ExitTransition
import androidx.compose.foundation.layout.padding
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material3.FloatingActionButton
import androidx.compose.material3.Icon
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Snackbar
import androidx.compose.material3.SnackbarDuration
import androidx.compose.material3.SnackbarHost
import androidx.compose.material3.SnackbarHostState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.stringResource
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.navigation.NavGraph.Companion.findStartDestination
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.currentBackStackEntryAsState
import androidx.navigation.compose.rememberNavController
import com.openclassrooms.rebonnte.R
import com.openclassrooms.rebonnte.ui.aisle.AisleScreen
import com.openclassrooms.rebonnte.ui.aisle.AisleViewModel
import com.openclassrooms.rebonnte.ui.main.components.RebonnteBottomBar
import com.openclassrooms.rebonnte.ui.main.components.RebonnteTopBar
import com.openclassrooms.rebonnte.ui.medicine.AddMedicineActivity
import com.openclassrooms.rebonnte.ui.medicine.MedicineScreen
import com.openclassrooms.rebonnte.ui.medicine.MedicineViewModel
import com.openclassrooms.rebonnte.ui.theme.RebonnteTheme
import kotlinx.coroutines.launch

@Composable
fun MainScreen(initialTab: String = "aisle", onLogout: () -> Unit) {
    val navController = rememberNavController()
    val medicineViewModel: MedicineViewModel = hiltViewModel()
    val aisleViewModel: AisleViewModel = hiltViewModel()
    val navBackStackEntry by navController.currentBackStackEntryAsState()
    val currentRoute = navBackStackEntry?.destination?.route
    val context = LocalContext.current

    val aisleError by aisleViewModel.error.collectAsState()
    val medicineError by medicineViewModel.error.collectAsState()
    val snackbarHostState = remember { SnackbarHostState() }
    val coroutineScope = rememberCoroutineScope()

    // Navigue vers l'onglet demandé
    LaunchedEffect(initialTab) {
        navController.navigate(initialTab) {
            popUpTo(navController.graph.findStartDestination().id) { saveState = true }
            launchSingleTop = true
            restoreState = true
        }
    }

    // Erreur d'ajout de rayon (FAB sur l'onglet Rayons), ex: timeout réseau.
    LaunchedEffect(aisleError) {
        val message = aisleError ?: return@LaunchedEffect
        coroutineScope.launch {
            snackbarHostState.showSnackbar(message = message, duration = SnackbarDuration.Long)
            aisleViewModel.clearError()
        }
    }

    // Erreur de chargement de la liste des médicaments (listener Firestore).
    LaunchedEffect(medicineError) {
        val message = medicineError ?: return@LaunchedEffect
        coroutineScope.launch {
            snackbarHostState.showSnackbar(message = message, duration = SnackbarDuration.Long)
            medicineViewModel.clearError()
        }
    }

    RebonnteTheme {
        Scaffold(
            snackbarHost = {
                SnackbarHost(hostState = snackbarHostState) { data ->
                    Snackbar(snackbarData = data)
                }
            },
            topBar = {
                RebonnteTopBar(
                    currentRoute = currentRoute,
                    onLogout = onLogout,
                    onSortByNone = { medicineViewModel.sortByNone() },
                    onSortByName = { medicineViewModel.sortByName() },
                    onSortByStock = { medicineViewModel.sortByStock() },
                    onSearchQueryChange = { medicineViewModel.filterByName(it) }
                )
            },
            bottomBar = {
                RebonnteBottomBar(
                    currentRoute = currentRoute,
                    navController = navController
                )
            },
            floatingActionButton = {
                FloatingActionButton(
                    onClick = {
                        when (currentRoute) {
                            "medicine" -> context.startActivity(
                                Intent(context, AddMedicineActivity::class.java)
                            )
                            "aisle" -> aisleViewModel.addRandomAisle()
                        }
                    }
                ) {
                    Icon(Icons.Default.Add, contentDescription = stringResource(R.string.fab_add))
                }
            }
        ) { paddingValues ->
            NavHost(
                modifier = Modifier.padding(paddingValues),
                navController = navController,
                startDestination = "aisle",
                enterTransition = { EnterTransition.None },
                exitTransition = { ExitTransition.None },
                popEnterTransition = { EnterTransition.None },
                popExitTransition = { ExitTransition.None }
            ) {
                composable("aisle") { AisleScreen(aisleViewModel) }
                composable("medicine") { MedicineScreen(medicineViewModel) }
            }
        }
    }
}