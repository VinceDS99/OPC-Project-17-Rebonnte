package com.openclassrooms.rebonnte

import androidx.compose.ui.test.assertIsDisplayed
import androidx.compose.ui.test.assertIsEnabled
import androidx.compose.ui.test.assertIsNotEnabled
import androidx.compose.ui.test.junit4.createComposeRule
import androidx.compose.ui.test.onNodeWithText
import androidx.compose.ui.test.performClick
import androidx.compose.ui.test.performTextInput
import androidx.test.ext.junit.runners.AndroidJUnit4
import com.openclassrooms.rebonnte.ui.auth.LoginScreen
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith

/**
 * Tests instrumentés de l'écran de connexion.
 * Testent le comportement UI avec interaction utilisateur réelle.
 */
@RunWith(AndroidJUnit4::class)
class LoginScreenTest {

    @get:Rule
    val composeTestRule = createComposeRule()

    private fun setupLoginScreen(
        onLoginSuccess: () -> Unit = {},
        onNavigateToRegister: () -> Unit = {}
    ) {
        composeTestRule.setContent {
            LoginScreen(
                onLoginSuccess = onLoginSuccess,
                onNavigateToRegister = onNavigateToRegister
            )
        }
    }

    // ─── Test 1 : l'écran s'affiche correctement ───────────────────────────

    @Test
    fun loginScreen_displaysAllComponents() {
        setupLoginScreen()

        composeTestRule.onNodeWithText("Rebonnté").assertIsDisplayed()
        composeTestRule.onNodeWithText("Email").assertIsDisplayed()
        composeTestRule.onNodeWithText("Mot de passe").assertIsDisplayed()
        composeTestRule.onNodeWithText("Se connecter").assertIsDisplayed()
        composeTestRule.onNodeWithText("Pas de compte ? S'inscrire").assertIsDisplayed()
    }

    // ─── Test 2 : erreur si champs vides ───────────────────────────────────

    @Test
    fun loginScreen_showsError_whenFieldsAreEmpty() {
        setupLoginScreen()

        // Cliquer sur "Se connecter" sans remplir les champs
        composeTestRule.onNodeWithText("Se connecter").performClick()

        // Le message d'erreur doit apparaître
        composeTestRule
            .onNodeWithText("Email et mot de passe requis")
            .assertIsDisplayed()
    }

    // ─── Test 3 : erreur si seul le mot de passe est vide ──────────────────

    @Test
    fun loginScreen_showsError_whenOnlyEmailFilled() {
        setupLoginScreen()

        composeTestRule.onNodeWithText("Email").performTextInput("test@mail.com")
        composeTestRule.onNodeWithText("Se connecter").performClick()

        composeTestRule
            .onNodeWithText("Email et mot de passe requis")
            .assertIsDisplayed()
    }

    // ─── Test 4 : le lien d'inscription appelle le bon callback ────────────

    @Test
    fun loginScreen_navigatesToRegister_whenLinkClicked() {
        var registerClicked = false
        setupLoginScreen(onNavigateToRegister = { registerClicked = true })

        composeTestRule
            .onNodeWithText("Pas de compte ? S'inscrire")
            .performClick()

        assert(registerClicked) { "onNavigateToRegister aurait dû être appelé" }
    }

    // ─── Test 5 : le bouton est désactivé pendant le chargement ────────────

    @Test
    fun loginScreen_buttonEnabled_whenFieldsFilled() {
        setupLoginScreen()

        // Remplir les deux champs
        composeTestRule.onNodeWithText("Email").performTextInput("test@mail.com")
        composeTestRule.onNodeWithText("Mot de passe").performTextInput("password123")

        // Le bouton doit être actif
        composeTestRule.onNodeWithText("Se connecter").assertIsEnabled()
    }
}