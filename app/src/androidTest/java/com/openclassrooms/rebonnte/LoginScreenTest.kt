package com.openclassrooms.rebonnte

import androidx.compose.ui.test.assertIsDisplayed
import androidx.compose.ui.test.assertIsEnabled
import androidx.compose.ui.test.junit4.createComposeRule
import androidx.compose.ui.test.onNodeWithText
import androidx.compose.ui.test.performClick
import androidx.compose.ui.test.performTextInput
import androidx.test.ext.junit.runners.AndroidJUnit4
import com.openclassrooms.rebonnte.ui.auth.AuthUiState
import com.openclassrooms.rebonnte.ui.auth.LoginScreen
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith

@RunWith(AndroidJUnit4::class)
class LoginScreenTest {

    @get:Rule
    val composeTestRule = createComposeRule()

    private fun setupLoginScreen(
        uiState: AuthUiState = AuthUiState.Idle,
        onSignIn: (String, String) -> Unit = { _, _ -> },
        onNavigateToRegister: () -> Unit = {}
    ) {
        composeTestRule.setContent {
            LoginScreen(
                uiState = uiState,
                onSignIn = onSignIn,
                onNavigateToRegister = onNavigateToRegister
            )
        }
    }

    @Test
    fun loginScreen_displaysAllComponents() {
        setupLoginScreen()

        composeTestRule.onNodeWithText("Rebonnté").assertIsDisplayed()
        composeTestRule.onNodeWithText("Email").assertIsDisplayed()
        composeTestRule.onNodeWithText("Mot de passe").assertIsDisplayed()
        composeTestRule.onNodeWithText("Se connecter").assertIsDisplayed()
        composeTestRule.onNodeWithText("Pas de compte ? S'inscrire").assertIsDisplayed()
    }

    @Test
    fun loginScreen_displaysErrorMessage_whenStateIsError() {
        setupLoginScreen(uiState = AuthUiState.Error("Email et mot de passe requis"))

        composeTestRule
            .onNodeWithText("Email et mot de passe requis")
            .assertIsDisplayed()
    }

    @Test
    fun loginScreen_navigatesToRegister_whenLinkClicked() {
        var registerClicked = false
        setupLoginScreen(onNavigateToRegister = { registerClicked = true })

        composeTestRule
            .onNodeWithText("Pas de compte ? S'inscrire")
            .performClick()

        assert(registerClicked) { "onNavigateToRegister aurait dû être appelé" }
    }
}