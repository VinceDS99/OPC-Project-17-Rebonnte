package com.openclassrooms.rebonnte

import androidx.arch.core.executor.testing.InstantTaskExecutorRule
import com.openclassrooms.rebonnte.ui.aisle.Aisle
import com.openclassrooms.rebonnte.ui.medicine.Medicine
import com.openclassrooms.rebonnte.ui.medicine.MedicineViewModel
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.test.StandardTestDispatcher
import kotlinx.coroutines.test.resetMain
import kotlinx.coroutines.test.runTest
import kotlinx.coroutines.test.setMain
import org.junit.After
import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Before
import org.junit.Rule
import org.junit.Test

@OptIn(ExperimentalCoroutinesApi::class)
class MedicineViewModelTest {

    @get:Rule
    val instantTaskExecutorRule = InstantTaskExecutorRule()

    private val testDispatcher = StandardTestDispatcher()
    private lateinit var repository: FakeMedicineRepository
    private lateinit var viewModel: MedicineViewModel

    @Before
    fun setUp() {
        Dispatchers.setMain(testDispatcher)
        repository = FakeMedicineRepository()
        viewModel = MedicineViewModel(repository)
    }

    @After
    fun tearDown() {
        Dispatchers.resetMain()
    }

    // T-01 : Ajout d'un médicament
    @Test
    fun `addRandomMedicine ajoute un medicament a la liste`() = runTest {
        val aisles = listOf(Aisle("Rayon A"))

        viewModel.addRandomMedicine(aisles)
        testDispatcher.scheduler.advanceUntilIdle()

        assertEquals(1, viewModel.medicines.value.size)
    }

    // T-02 : Modification du stock
    @Test
    fun `updateStock met a jour le stock et cree une entree historique`() = runTest {
        val aisles = listOf(Aisle("Rayon A"))
        viewModel.addRandomMedicine(aisles)
        testDispatcher.scheduler.advanceUntilIdle()

        val medicine = viewModel.medicines.value.first()
        val newStock = medicine.stock + 1

        viewModel.updateStock(medicine, newStock, "test-user-id")
        testDispatcher.scheduler.advanceUntilIdle()

        val updated = viewModel.medicines.value.first()
        assertEquals(newStock, updated.stock)
        assertEquals(1, updated.histories.size)
        assertEquals("test-user-id", updated.histories.first().userId)
    }

    // T-03 : Suppression d'un médicament
    @Test
    fun `deleteMedicine supprime le medicament de la liste`() = runTest {
        val aisles = listOf(Aisle("Rayon A"))
        viewModel.addRandomMedicine(aisles)
        testDispatcher.scheduler.advanceUntilIdle()

        val medicine = viewModel.medicines.value.first()
        viewModel.deleteMedicine(medicine)
        testDispatcher.scheduler.advanceUntilIdle()

        assertEquals(0, viewModel.medicines.value.size)
    }

    // T-04 : Filtrage par nom
    @Test
    fun `filterByName retourne uniquement les medicaments correspondants`() = runTest {
        repository.addMedicine(
            Medicine(id = "1", name = "Doliprane", stock = 10, nameAisle = "Rayon A")
        )
        repository.addMedicine(
            Medicine(id = "2", name = "Ibuprofène", stock = 5, nameAisle = "Rayon A")
        )
        testDispatcher.scheduler.advanceUntilIdle()

        viewModel.filterByName("Dolip")

        assertEquals(1, viewModel.medicines.value.size)
        assertEquals("Doliprane", viewModel.medicines.value.first().name)
    }

    @Test
    fun `filterByName avec chaine vide restaure toute la liste`() = runTest {
        repository.addMedicine(
            Medicine(id = "1", name = "Doliprane", stock = 10, nameAisle = "Rayon A")
        )
        repository.addMedicine(
            Medicine(id = "2", name = "Ibuprofène", stock = 5, nameAisle = "Rayon A")
        )
        testDispatcher.scheduler.advanceUntilIdle()

        viewModel.filterByName("Dolip")
        viewModel.filterByName("")

        assertEquals(2, viewModel.medicines.value.size)
    }
}