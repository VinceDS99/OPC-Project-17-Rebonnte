package com.openclassrooms.rebonnte.ui.main.components

import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Home
import androidx.compose.material.icons.filled.List
import androidx.compose.material3.Icon
import androidx.compose.material3.NavigationBar
import androidx.compose.material3.NavigationBarItem
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.tooling.preview.Preview
import androidx.navigation.NavController
import androidx.navigation.NavGraph.Companion.findStartDestination
import androidx.navigation.compose.rememberNavController
import com.openclassrooms.rebonnte.ui.theme.RebonnteTheme

@Composable
fun RebonnteBottomBar(
    currentRoute: String?,
    navController: NavController
) {
    NavigationBar {
        NavigationBarItem(
            icon = { Icon(Icons.Default.Home, contentDescription = "Rayons") },
            label = { Text("Rayons") },
            selected = currentRoute == "aisle",
            onClick = {
                navController.navigate("aisle") {
                    popUpTo(navController.graph.findStartDestination().id) { saveState = true }
                    launchSingleTop = true
                    restoreState = true
                }
            }
        )
        NavigationBarItem(
            icon = { Icon(Icons.Default.List, contentDescription = "Médicaments") },
            label = { Text("Médicaments") },
            selected = currentRoute == "medicine",
            onClick = {
                navController.navigate("medicine") {
                    popUpTo(navController.graph.findStartDestination().id) { saveState = true }
                    launchSingleTop = true
                    restoreState = true
                }
            }
        )
    }
}

@Preview(showBackground = true, name = "BottomBar — Rayons sélectionné")
@Composable
fun RebonnteBottomBarAislePreview() {
    RebonnteTheme {
        RebonnteBottomBar(currentRoute = "aisle", navController = rememberNavController())
    }
}

@Preview(showBackground = true, name = "BottomBar — Médicaments sélectionné")
@Composable
fun RebonnteBottomBarMedicinePreview() {
    RebonnteTheme {
        RebonnteBottomBar(currentRoute = "medicine", navController = rememberNavController())
    }
}