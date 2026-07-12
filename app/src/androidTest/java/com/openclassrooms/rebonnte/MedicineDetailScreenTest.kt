package com.openclassrooms.rebonnte

import androidx.compose.ui.test.assertIsDisplayed
import androidx.compose.ui.test.assertIsEnabled
import androidx.compose.ui.test.assertIsNotEnabled
import androidx.compose.ui.test.junit4.createComposeRule
import androidx.compose.ui.test.onNodeWithContentDescription
import androidx.compose.ui.test.onNodeWithText
import androidx.compose.ui.test.performClick
import androidx.test.ext.junit.runners.AndroidJUnit4
import com.openclassrooms.rebonnte.ui.history.History
import com.openclassrooms.rebonnte.ui.medicine.Medicine
import com.openclassrooms.rebonnte.ui.medicine.MedicineDetailScreen
import com.openclassrooms.rebonnte.ui.medicine.MedicineViewModel
import com.openclassrooms.rebonnte.ui.theme.RebonnteTheme
import io.mockk.every
import io.mockk.mockk
import io.mockk.verify
import kotlinx.coroutines.flow.MutableStateFlow
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith

@RunWith(AndroidJUnit4::class)
class MedicineDetailScreenTest {

    @get:Rule
    val composeTestRule = createComposeRule()

    private val testMedicine = Medicine(
        id = "test-id-123",
        name = "Doliprane 1000mg",
        stock = 10,
        nameAisle = "Rayon Analgésiques",
        histories = listOf(
            History(
                medicineName = "Doliprane 1000mg",
                userId = "test@mail.com",
                date = "Thu Jul 10 2026",
                details = "Stock increased from 9 to 10"
            )
        )
    )

    private fun setupScreen(medicine: Medicine = testMedicine): MedicineViewModel {
        val viewModel = mockk<MedicineViewModel>(relaxed = true)
        every { viewModel.medicines } returns MutableStateFlow(listOf(medicine))
        every { viewModel.isLoading } returns MutableStateFlow(false)

        composeTestRule.setContent {
            RebonnteTheme {
                MedicineDetailScreen(
                    name = medicine.name,
                    viewModel = viewModel
                )
            }
        }
        return viewModel
    }

    // ─── Test 1 : les informations du médicament s'affichent ───────────────

    @Test
    fun medicineDetail_displaysCorrectInformation() {
        setupScreen()

        composeTestRule.onNodeWithText("Doliprane 1000mg").assertIsDisplayed()
        composeTestRule.onNodeWithText("Rayon Analgésiques").assertIsDisplayed()
        composeTestRule.onNodeWithText("10").assertIsDisplayed()
    }

    // ─── Test 2 : l'historique s'affiche ───────────────────────────────────

    @Test
    fun medicineDetail_displaysHistory() {
        setupScreen()

        composeTestRule.onNodeWithText("Historique").assertIsDisplayed()
        composeTestRule.onNodeWithText("Utilisateur : test@mail.com").assertIsDisplayed()
        composeTestRule.onNodeWithText("Stock increased from 9 to 10").assertIsDisplayed()
    }

    // ─── Test 3 : cliquer sur + appelle updateStock ────────────────────────

    @Test
    fun medicineDetail_clickIncrement_callsUpdateStock() {
        val viewModel = setupScreen()

        composeTestRule
            .onNodeWithContentDescription("Augmenter le stock")
            .performClick()

        verify { viewModel.updateStock(testMedicine, 11, any()) }
    }

    // ─── Test 4 : cliquer sur - appelle updateStock ────────────────────────

    @Test
    fun medicineDetail_clickDecrement_callsUpdateStock() {
        val viewModel = setupScreen()

        composeTestRule
            .onNodeWithContentDescription("Diminuer le stock")
            .performClick()

        verify { viewModel.updateStock(testMedicine, 9, any()) }
    }

    // ─── Test 5 : bouton - désactivé quand stock = 0 ───────────────────────

    @Test
    fun medicineDetail_decrementDisabled_whenStockIsZero() {
        val medicineWithZeroStock = testMedicine.copy(stock = 0)
        setupScreen(medicineWithZeroStock)

        composeTestRule
            .onNodeWithContentDescription("Diminuer le stock")
            .assertIsNotEnabled()
    }

    // ─── Test 6 : cliquer sur Supprimer ouvre le dialog ───────────────────

    @Test
    fun medicineDetail_clickDelete_showsConfirmationDialog() {
        setupScreen()

        composeTestRule
            .onNodeWithText("Supprimer le médicament")
            .performClick()

        composeTestRule
            .onNodeWithText("Voulez-vous supprimer Doliprane 1000mg ?")
            .assertIsDisplayed()
        composeTestRule.onNodeWithText("Annuler").assertIsDisplayed()
        composeTestRule.onNodeWithText("Supprimer").assertIsDisplayed()
    }

    // ─── Test 7 : annuler le dialog le ferme ──────────────────────────────

    @Test
    fun medicineDetail_cancelDelete_dismissesDialog() {
        setupScreen()

        composeTestRule.onNodeWithText("Supprimer le médicament").performClick()
        composeTestRule.onNodeWithText("Annuler").performClick()

        composeTestRule
            .onNodeWithText("Voulez-vous supprimer Doliprane 1000mg ?")
            .assertDoesNotExist()
    }
}