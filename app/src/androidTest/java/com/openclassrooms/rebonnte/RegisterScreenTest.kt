package com.openclassrooms.rebonnte

import androidx.compose.ui.test.assertIsDisplayed
import androidx.compose.ui.test.junit4.createComposeRule
import androidx.compose.ui.test.onNodeWithText
import androidx.compose.ui.test.performClick
import androidx.compose.ui.test.performTextInput
import androidx.test.ext.junit.runners.AndroidJUnit4
import com.openclassrooms.rebonnte.ui.auth.AuthUiState
import com.openclassrooms.rebonnte.ui.auth.RegisterScreen
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith

@RunWith(AndroidJUnit4::class)
class RegisterScreenTest {

    @get:Rule
    val composeTestRule = createComposeRule()

    private fun setupRegisterScreen(
        uiState: AuthUiState = AuthUiState.Idle,
        onRegister: (String, String) -> Unit = { _, _ -> },
        onNavigateBack: () -> Unit = {}
    ) {
        composeTestRule.setContent {
            RegisterScreen(
                uiState = uiState,
                onRegister = onRegister,
                onNavigateBack = onNavigateBack
            )
        }
    }

    @Test
    fun registerScreen_displaysAllComponents() {
        setupRegisterScreen()

        composeTestRule.onNodeWithText("Créer un compte").assertIsDisplayed()
        composeTestRule.onNodeWithText("Email").assertIsDisplayed()
        composeTestRule.onNodeWithText("Mot de passe (6 caractères min.)").assertIsDisplayed()
        composeTestRule.onNodeWithText("Créer le compte").assertIsDisplayed()
        composeTestRule.onNodeWithText("Retour à la connexion").assertIsDisplayed()
    }

    @Test
    fun registerScreen_displaysErrorMessage_whenStateIsError() {
        setupRegisterScreen(
            uiState = AuthUiState.Error("Email valide et mot de passe de 6 caractères min. requis")
        )

        composeTestRule
            .onNodeWithText("Email valide et mot de passe de 6 caractères min. requis")
            .assertIsDisplayed()
    }

}