package com.openclassrooms.rebonnte.ui.medicine

import android.util.Log
import com.google.firebase.firestore.FieldValue
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.Query
import com.openclassrooms.rebonnte.core.AppException
import com.openclassrooms.rebonnte.ui.history.History
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.channels.awaitClose
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.callbackFlow
import kotlinx.coroutines.suspendCancellableCoroutine
import kotlinx.coroutines.withContext
import javax.inject.Inject
import kotlin.coroutines.resume
import kotlin.coroutines.resumeWithException

class MedicineRepository @Inject constructor() : MedicineRepositoryInterface {

    private val collection = FirebaseFirestore.getInstance().collection("medicines")
    private val tag = "MedicineRepository"

    override fun getMedicines(
        params: MedicineQueryParams
    ): Flow<List<Medicine>> = callbackFlow {

        // Construction de la requête Firestore selon les params
        val query: Query = when {
            // Filtrage par préfixe (priorité sur le tri)
            params.nameFilter.isNotBlank() -> {
                val filterLower = params.nameFilter.lowercase()
                collection
                    .orderBy("name")
                    .startAt(filterLower)
                    .endAt(filterLower + "\uf8ff")
            }
            // Tri par nom côté serveur
            params.sortField == MedicineSortField.NAME -> {
                collection.orderBy("name")
            }
            // Tri par stock côté serveur
            params.sortField == MedicineSortField.STOCK -> {
                collection.orderBy("stock")
            }
            // Pas de tri — ordre Firestore par défaut
            else -> collection
        }

        val listener = query.addSnapshotListener { snapshot, error ->
            if (error != null) {
                Log.w(tag, "getMedicines listener error", error)
                // On envoie une liste vide plutôt que de crasher
                trySend(emptyList())
                return@addSnapshotListener
            }
            val medicines = snapshot?.documents?.mapNotNull { doc ->
                try {
                    doc.toObject(Medicine::class.java)?.copy(id = doc.id)
                } catch (e: Exception) {
                    Log.e(tag, "Erreur désérialisation document ${doc.id}", e)
                    null // le document malformé est ignoré, les autres passent
                }
            } ?: emptyList()
            trySend(medicines)
        }

        // Libère le listener quand le Flow est annulé (ViewModel détruit)
        awaitClose {
            Log.d(tag, "Listener Firestore libéré")
            listener.remove()
        }
    }

    override suspend fun addMedicine(medicine: Medicine) = withContext(Dispatchers.IO) {
        try {
            suspendCancellableCoroutine<Unit> { cont ->
                collection.add(medicine)
                    .addOnSuccessListener {
                        Log.d(tag, "Médicament ajouté : ${medicine.name}")
                        cont.resume(Unit)
                    }
                    .addOnFailureListener { e ->
                        Log.e(tag, "Erreur ajout médicament", e)
                        cont.resumeWithException(
                            AppException.MedicineException(
                                "Impossible d'ajouter le médicament '${medicine.name}'", e
                            )
                        )
                    }
            }
        } catch (e: AppException) {
            throw e
        } catch (e: Exception) {
            throw AppException.MedicineException("Erreur inattendue lors de l'ajout", e)
        }
    }

    override suspend fun updateStockAndHistory(
        medicineId: String,
        newStock: Int,
        history: History
    ) = withContext(Dispatchers.IO) {
        try {
            suspendCancellableCoroutine<Unit> { cont ->
                collection.document(medicineId)
                    .update(
                        mapOf(
                            "stock" to newStock,
                            "histories" to FieldValue.arrayUnion(history)
                        )
                    )
                    .addOnSuccessListener { cont.resume(Unit) }
                    .addOnFailureListener { e ->
                        cont.resumeWithException(
                            AppException.MedicineException(
                                "Impossible de mettre à jour le stock (id=$medicineId)", e
                            )
                        )
                    }
            }
        } catch (e: AppException) {
            throw e
        } catch (e: Exception) {
            throw AppException.MedicineException("Erreur inattendue lors de la mise à jour du stock", e)
        }
    }

    override suspend fun deleteMedicine(medicineId: String) = withContext(Dispatchers.IO) {
        try {
            suspendCancellableCoroutine<Unit> { cont ->
                collection.document(medicineId)
                    .delete()
                    .addOnSuccessListener { cont.resume(Unit) }
                    .addOnFailureListener { e ->
                        cont.resumeWithException(
                            AppException.MedicineException(
                                "Impossible de supprimer le médicament (id=$medicineId)", e
                            )
                        )
                    }
            }
        } catch (e: AppException) {
            throw e
        } catch (e: Exception) {
            throw AppException.MedicineException("Erreur inattendue lors de la suppression", e)
        }
    }
}