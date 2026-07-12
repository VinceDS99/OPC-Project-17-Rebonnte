package com.openclassrooms.rebonnte

import androidx.compose.ui.test.assertIsDisplayed
import androidx.compose.ui.test.junit4.createComposeRule
import androidx.compose.ui.test.onNodeWithText
import androidx.compose.ui.test.performClick
import androidx.compose.ui.test.performTextInput
import androidx.test.ext.junit.runners.AndroidJUnit4
import com.openclassrooms.rebonnte.ui.auth.RegisterScreen
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith

@RunWith(AndroidJUnit4::class)
class RegisterScreenTest {

    @get:Rule
    val composeTestRule = createComposeRule()

    private fun setupRegisterScreen(
        onRegisterSuccess: () -> Unit = {},
        onNavigateBack: () -> Unit = {}
    ) {
        composeTestRule.setContent {
            RegisterScreen(
                onRegisterSuccess = onRegisterSuccess,
                onNavigateBack = onNavigateBack
            )
        }
    }

    // ─── Test 1 : l'écran s'affiche correctement ───────────────────────────

    @Test
    fun registerScreen_displaysAllComponents() {
        setupRegisterScreen()

        composeTestRule.onNodeWithText("Créer un compte").assertIsDisplayed()
        composeTestRule.onNodeWithText("Email").assertIsDisplayed()
        composeTestRule.onNodeWithText("Mot de passe (6 caractères min.)").assertIsDisplayed()
        composeTestRule.onNodeWithText("Créer le compte").assertIsDisplayed()
        composeTestRule.onNodeWithText("Retour à la connexion").assertIsDisplayed()
    }

    // ─── Test 2 : erreur si mot de passe trop court ────────────────────────

    @Test
    fun registerScreen_showsError_whenPasswordTooShort() {
        setupRegisterScreen()

        composeTestRule.onNodeWithText("Email").performTextInput("test@mail.com")
        composeTestRule.onNodeWithText("Mot de passe (6 caractères min.)").performTextInput("abc")
        composeTestRule.onNodeWithText("Créer le compte").performClick()

        composeTestRule
            .onNodeWithText("Email valide et mot de passe de 6 caractères min. requis")
            .assertIsDisplayed()
    }

    // ─── Test 3 : erreur si champs vides ───────────────────────────────────

    @Test
    fun registerScreen_showsError_whenFieldsEmpty() {
        setupRegisterScreen()

        composeTestRule.onNodeWithText("Créer le compte").performClick()

        composeTestRule
            .onNodeWithText("Email valide et mot de passe de 6 caractères min. requis")
            .assertIsDisplayed()
    }

    // ─── Test 4 : retour login appelle le bon callback ─────────────────────

    @Test
    fun registerScreen_navigatesBack_whenBackLinkClicked() {
        var backClicked = false
        setupRegisterScreen(onNavigateBack = { backClicked = true })

        composeTestRule.onNodeWithText("Retour à la connexion").performClick()

        assert(backClicked) { "onNavigateBack aurait dû être appelé" }
    }
}