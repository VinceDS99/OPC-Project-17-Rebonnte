package com.openclassrooms.rebonnte

import androidx.compose.ui.test.assertIsDisplayed
import androidx.compose.ui.test.junit4.createComposeRule
import androidx.compose.ui.test.onAllNodesWithContentDescription
import androidx.compose.ui.test.onNodeWithText
import androidx.test.ext.junit.runners.AndroidJUnit4
import com.openclassrooms.rebonnte.ui.aisle.Aisle
import com.openclassrooms.rebonnte.ui.aisle.AisleScreen
import com.openclassrooms.rebonnte.ui.aisle.AisleViewModel
import com.openclassrooms.rebonnte.ui.theme.RebonnteTheme
import io.mockk.every
import io.mockk.mockk
import kotlinx.coroutines.flow.MutableStateFlow
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith

@RunWith(AndroidJUnit4::class)
class AisleScreenTest {

    @get:Rule
    val composeTestRule = createComposeRule()

    private val testAisles = listOf(
        Aisle("Rayon Analgésiques"),
        Aisle("Rayon Antibiotiques"),
        Aisle("Rayon Vitamines")
    )

    private fun setupScreen(aisles: List<Aisle> = testAisles) {
        val viewModel = mockk<AisleViewModel>(relaxed = true)
        every { viewModel.aisles } returns MutableStateFlow(aisles)
        every { viewModel.isLoading } returns MutableStateFlow(false)

        composeTestRule.setContent {
            RebonnteTheme {
                AisleScreen(viewModel = viewModel)
            }
        }
    }

    @Test
    fun aisleScreen_displaysAllAisles() {
        setupScreen()

        composeTestRule.onNodeWithText("Rayon Analgésiques").assertIsDisplayed()
        composeTestRule.onNodeWithText("Rayon Antibiotiques").assertIsDisplayed()
        composeTestRule.onNodeWithText("Rayon Vitamines").assertIsDisplayed()
    }

}