package com.openclassrooms.rebonnte.ui.medicine

import android.util.Log
import com.google.firebase.firestore.FieldValue
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.Query
import com.openclassrooms.rebonnte.core.AppException
import com.openclassrooms.rebonnte.core.NetworkMonitor
import com.openclassrooms.rebonnte.ui.history.History
import kotlinx.coroutines.CancellationException
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.TimeoutCancellationException
import kotlinx.coroutines.channels.awaitClose
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.callbackFlow
import kotlinx.coroutines.suspendCancellableCoroutine
import kotlinx.coroutines.withContext
import kotlinx.coroutines.withTimeout
import javax.inject.Inject
import kotlin.coroutines.resume
import kotlin.coroutines.resumeWithException

class MedicineRepository @Inject constructor(
    private val networkMonitor: NetworkMonitor
) : MedicineRepositoryInterface {

    private val collection = FirebaseFirestore.getInstance().collection("medicines")
    private val tag = "MedicineRepository"
    private val writeTimeoutMs = 15_000L

    override fun getMedicines(
        params: MedicineQueryParams
    ): Flow<List<Medicine>> = callbackFlow {

        val query: Query = when {
            params.nameFilter.isNotBlank() -> {
                val filterLower = params.nameFilter.lowercase()
                // Recherche par préfixe insensible à la casse via le champ "nameLower"
                collection
                    .orderBy("nameLower")
                    .startAt(filterLower)
                    .endAt(filterLower + "\uf8ff")
                    .limit(params.pageSize.toLong())
            }
            params.sortField == MedicineSortField.NAME -> {
                collection.orderBy("nameLower").limit(params.pageSize.toLong())
            }
            params.sortField == MedicineSortField.STOCK -> {
                collection.orderBy("stock").limit(params.pageSize.toLong())
            }
            else -> collection.limit(params.pageSize.toLong())
        }

        val listener = query.addSnapshotListener { snapshot, error ->
            if (error != null) {
                Log.w(tag, "getMedicines listener error", error)
                trySend(emptyList())
                return@addSnapshotListener
            }
            val medicines = snapshot?.documents?.mapNotNull { doc ->
                try {
                    doc.toObject(Medicine::class.java)?.copy(id = doc.id)
                } catch (e: Exception) {
                    Log.e(tag, "Erreur désérialisation document ${doc.id}", e)
                    null
                }
            } ?: emptyList()
            trySend(medicines)
        }

        awaitClose {
            Log.d(tag, "Listener Firestore libéré")
            listener.remove()
        }
    }

    override suspend fun addMedicine(medicine: Medicine) = withContext(Dispatchers.IO) {

        if (!networkMonitor.isOnline()) {
            throw AppException.NetworkException(
                "Pas de connexion internet. Vérifiez votre réseau et réessayez."
            )
        }
        try {
            withTimeout(writeTimeoutMs) {
                suspendCancellableCoroutine<Unit> { cont ->
                    val medicineToSave = medicine.copy(nameLower = medicine.name.lowercase())
                    collection.add(medicineToSave)
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
            }
        } catch (e: TimeoutCancellationException) {
            Log.e(tag, "Timeout ajout médicament '${medicine.name}'", e)
            throw AppException.NetworkException(
                "Aucune réponse du serveur. Vérifiez votre connexion et réessayez.", e
            )
        } catch (e: CancellationException) {
            throw e
        } catch (e: AppException.MedicineException) {
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
        if (!networkMonitor.isOnline()) {
            throw AppException.NetworkException(
                "Pas de connexion internet. Vérifiez votre réseau et réessayez."
            )
        }
        try {
            withTimeout(writeTimeoutMs) {
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
            }
        } catch (e: TimeoutCancellationException) {
            Log.e(tag, "Timeout mise à jour stock (id=$medicineId)", e)
            throw AppException.NetworkException(
                "Aucune réponse du serveur. Vérifiez votre connexion et réessayez.", e
            )
        } catch (e: CancellationException) {
            throw e
        } catch (e: AppException.MedicineException) {
            throw e
        } catch (e: Exception) {
            throw AppException.MedicineException("Erreur inattendue lors de la mise à jour du stock", e)
        }
    }

    override suspend fun deleteMedicine(medicineId: String) = withContext(Dispatchers.IO) {
        if (!networkMonitor.isOnline()) {
            throw AppException.NetworkException(
                "Pas de connexion internet. Vérifiez votre réseau et réessayez."
            )
        }
        try {
            withTimeout(writeTimeoutMs) {
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
            }
        } catch (e: TimeoutCancellationException) {
            Log.e(tag, "Timeout suppression médicament (id=$medicineId)", e)
            throw AppException.NetworkException(
                "Aucune réponse du serveur. Vérifiez votre connexion et réessayez.", e
            )
        } catch (e: CancellationException) {
            throw e
        } catch (e: AppException.MedicineException) {
            throw e
        } catch (e: Exception) {
            throw AppException.MedicineException("Erreur inattendue lors de la suppression", e)
        }
    }
}