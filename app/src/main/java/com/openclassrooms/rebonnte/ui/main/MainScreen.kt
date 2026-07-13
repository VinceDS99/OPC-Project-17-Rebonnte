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
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
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

@Composable
fun MainScreen(initialTab: String = "aisle", onLogout: () -> Unit) {
    val navController = rememberNavController()
    val medicineViewModel: MedicineViewModel = hiltViewModel()
    val aisleViewModel: AisleViewModel = hiltViewModel()
    val navBackStackEntry by navController.currentBackStackEntryAsState()
    val currentRoute = navBackStackEntry?.destination?.route
    val context = LocalContext.current

    // Navigue vers l'onglet demandé (ex: retour depuis MedicineDetailActivity
    // après suppression, ou onNewIntent). S'exécute aussi au premier lancement.
    LaunchedEffect(initialTab) {
        navController.navigate(initialTab) {
            popUpTo(navController.graph.findStartDestination().id) { saveState = true }
            launchSingleTop = true
            restoreState = true
        }
    }

    RebonnteTheme {
        Scaffold(
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