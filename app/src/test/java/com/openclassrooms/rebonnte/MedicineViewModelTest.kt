package com.openclassrooms.rebonnte

import androidx.arch.core.executor.testing.InstantTaskExecutorRule
import com.openclassrooms.rebonnte.ui.medicine.Medicine
import com.openclassrooms.rebonnte.ui.medicine.MedicineViewModel
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.launch
import kotlinx.coroutines.test.StandardTestDispatcher
import kotlinx.coroutines.test.TestScope
import kotlinx.coroutines.test.resetMain
import kotlinx.coroutines.test.runTest
import kotlinx.coroutines.test.setMain
import org.junit.After
import org.junit.Assert.assertEquals
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

    /**
     * S'abonne à viewModel.medicines dans backgroundScope (fourni par TestScope).
     * OBLIGATOIRE : medicines utilise SharingStarted.WhileSubscribed(5000), qui
     * ne démarre la collecte du Repository QUE si un abonné actif existe.
     * Sans cet appel, medicines.value reste bloqué sur emptyList() (sa valeur
     * initiale) quoi qu'on ajoute au repository — backgroundScope se termine
     * automatiquement à la fin du test, pas besoin d'annuler manuellement.
     */
    private fun TestScope.subscribeToMedicines() {
        backgroundScope.launch {
            viewModel.medicines.collect { }
        }
    }

    private suspend fun addTestMedicine(
        name: String = "Doliprane 1000mg",
        stock: Int = 10,
        nameAisle: String = "Rayon A"
    ) {
        viewModel.addMedicine(name = name, stock = stock, nameAisle = nameAisle)
        testDispatcher.scheduler.advanceUntilIdle()
    }

    // ─── T-01 : Ajout d'un médicament ─────────────────────────────────────────
    @Test
    fun `addMedicine ajoute un medicament a la liste`() = runTest {
        subscribeToMedicines()
        addTestMedicine()

        assertEquals(1, viewModel.medicines.value.size)
        assertEquals("Doliprane 1000mg", viewModel.medicines.value.first().name)
        assertEquals(10, viewModel.medicines.value.first().stock)
        assertEquals("Rayon A", viewModel.medicines.value.first().nameAisle)
    }

    // T-02 : Modification du stock
    @Test
    fun `updateStock met a jour le stock et cree une entree historique`() = runTest {
        subscribeToMedicines()
        addTestMedicine()

        val medicine = viewModel.medicines.value.first()
        val newStock = medicine.stock + 1

        viewModel.updateStock(medicine, newStock, "test@mail.com")
        testDispatcher.scheduler.advanceUntilIdle()

        val updated = viewModel.medicines.value.first()
        assertEquals(newStock, updated.stock)
        assertEquals(1, updated.histories.size)
        assertEquals("test@mail.com", updated.histories.first().userId)
        assert(updated.histories.first().details.contains("increased"))
    }

    // T-03 : Suppression d'un médicament
    @Test
    fun `deleteMedicine supprime le medicament de la liste`() = runTest {
        subscribeToMedicines()
        addTestMedicine()

        val medicine = viewModel.medicines.value.first()
        viewModel.deleteMedicine(medicine)
        testDispatcher.scheduler.advanceUntilIdle()

        assertEquals(0, viewModel.medicines.value.size)
    }

    // T-04 : Filtrage par nom
    @Test
    fun `filterByName retourne uniquement les medicaments correspondants`() = runTest {
        subscribeToMedicines()
        repository.addMedicine(
            Medicine(id = "1", name = "Doliprane", stock = 10, nameAisle = "Rayon A")
        )
        repository.addMedicine(
            Medicine(id = "2", name = "Ibuprofène", stock = 5, nameAisle = "Rayon A")
        )
        testDispatcher.scheduler.advanceUntilIdle()

        viewModel.filterByName("Dolip")
        testDispatcher.scheduler.advanceUntilIdle()

        assertEquals(1, viewModel.medicines.value.size)
        assertEquals("Doliprane", viewModel.medicines.value.first().name)
    }

    @Test
    fun `filterByName avec chaine vide restaure toute la liste`() = runTest {
        subscribeToMedicines()
        repository.addMedicine(
            Medicine(id = "1", name = "Doliprane", stock = 10, nameAisle = "Rayon A")
        )
        repository.addMedicine(
            Medicine(id = "2", name = "Ibuprofène", stock = 5, nameAisle = "Rayon A")
        )
        testDispatcher.scheduler.advanceUntilIdle()

        viewModel.filterByName("Dolip")
        testDispatcher.scheduler.advanceUntilIdle()
        viewModel.filterByName("")
        testDispatcher.scheduler.advanceUntilIdle()

        assertEquals(2, viewModel.medicines.value.size)
    }
}