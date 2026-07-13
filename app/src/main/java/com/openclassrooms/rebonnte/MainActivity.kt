package com.openclassrooms.rebonnte

import android.content.Intent
import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.viewModels
import androidx.compose.runtime.mutableStateOf
import com.openclassrooms.rebonnte.ui.auth.LoginActivity
import com.openclassrooms.rebonnte.ui.auth.SessionViewModel
import com.openclassrooms.rebonnte.ui.main.MainScreen
import dagger.hilt.android.AndroidEntryPoint

@AndroidEntryPoint
class MainActivity : ComponentActivity() {

    private val sessionViewModel: SessionViewModel by viewModels()

    // État Compose lu par MainScreen — modifiable depuis onNewIntent
    private val selectedTab = mutableStateOf("aisle")

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        if (!sessionViewModel.isLoggedIn()) {
            navigateToLogin()
            return
        }

        selectedTab.value = intent.getStringExtra("selectedTab") ?: "aisle"

        setContent {
            MainScreen(
                initialTab = selectedTab.value,
                onLogout = {
                    sessionViewModel.signOut()
                    navigateToLogin()
                }
            )
        }
    }

    override fun onNewIntent(intent: Intent) {
        super.onNewIntent(intent)
        setIntent(intent)
        selectedTab.value = intent.getStringExtra("selectedTab") ?: selectedTab.value
    }

    private fun navigateToLogin() {
        val loginIntent = Intent(this, LoginActivity::class.java)
        loginIntent.flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK
        startActivity(loginIntent)
        finish()
    }
}