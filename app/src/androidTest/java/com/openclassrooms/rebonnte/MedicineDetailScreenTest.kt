package com.openclassrooms.rebonnte

import androidx.compose.ui.test.junit4.createComposeRule
import androidx.compose.ui.test.onNodeWithText
import androidx.test.ext.junit.runners.AndroidJUnit4
import com.openclassrooms.rebonnte.ui.history.History
import com.openclassrooms.rebonnte.ui.medicine.Medicine
import com.openclassrooms.rebonnte.ui.medicine.MedicineDetailContent
import com.openclassrooms.rebonnte.ui.theme.RebonnteTheme
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith
import androidx.compose.ui.test.assertIsDisplayed
import androidx.compose.ui.test.assertIsNotEnabled
import androidx.compose.ui.test.onAllNodesWithText
import androidx.compose.ui.test.onNodeWithContentDescription
import androidx.compose.ui.test.performClick

/**
 * Tests instrumentés du CONTENU visuel (MedicineDetailContent),
 * sans ViewModel ni Hilt — testable directement via ses paramètres.
 */
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

    private fun setupContent(
        medicine: Medicine = testMedicine,
        isLoading: Boolean = false,
        onIncrement: () -> Unit = {},
        onDecrement: () -> Unit = {},
        onDeleteClick: () -> Unit = {}
    ) {
        composeTestRule.setContent {
            RebonnteTheme {
                MedicineDetailContent(
                    medicine = medicine,
                    isLoading = isLoading,
                    onIncrement = onIncrement,
                    onDecrement = onDecrement,
                    onDeleteClick = onDeleteClick
                )
            }
        }
    }


    @Test
    fun medicineDetail_displaysCorrectInformation() {
        setupContent()
        
        composeTestRule
            .onAllNodesWithText("Doliprane 1000mg")[0]
            .assertIsDisplayed()

        composeTestRule.onNodeWithText("Rayon Analgésiques").assertIsDisplayed()
        composeTestRule.onNodeWithText("10").assertIsDisplayed()
    }

    @Test
    fun medicineDetail_displaysHistory() {
        setupContent()

        composeTestRule.onNodeWithText("Historique").assertIsDisplayed()
        composeTestRule.onNodeWithText("Utilisateur : test@mail.com").assertIsDisplayed()
        composeTestRule.onNodeWithText("Détails : Stock increased from 9 to 10").assertIsDisplayed()
    }



    @Test
    fun medicineDetail_decrementDisabled_whenStockIsZero() {
        setupContent(medicine = testMedicine.copy(stock = 0))

        composeTestRule
            .onNodeWithContentDescription("Diminuer le stock")
            .assertIsNotEnabled()
    }
}