package com.openclassrooms.rebonnte.ui.aisle

import android.util.Log
import com.google.firebase.firestore.FirebaseFirestore
import com.openclassrooms.rebonnte.core.AppException
import com.openclassrooms.rebonnte.core.NetworkMonitor
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

class AisleRepository @Inject constructor(
    private val networkMonitor: NetworkMonitor
) {

    private val collection = FirebaseFirestore.getInstance().collection("aisles")
    private val tag = "AisleRepository"
    private val writeTimeoutMs = 15_000L

    fun getAisles(): Flow<List<Aisle>> = callbackFlow {
        val listener = collection
            .orderBy("name") // tri alphabétique des rayons côté Firestore
            .addSnapshotListener { snapshot, error ->
                if (error != null) {
                    Log.w(tag, "getAisles listener error", error)
                    trySend(emptyList())
                    return@addSnapshotListener
                }
                val aisles = snapshot?.documents?.mapNotNull { doc ->
                    try {
                        doc.toObject(Aisle::class.java)
                    } catch (e: Exception) {
                        Log.e(tag, "Erreur désérialisation rayon ${doc.id}", e)
                        null
                    }
                } ?: emptyList()
                trySend(aisles)
            }
        awaitClose {
            Log.d(tag, "Listener aisles remove")
            listener.remove()
        }
    }

    suspend fun addAisle(aisle: Aisle) = withContext(Dispatchers.IO) {
        if (!networkMonitor.isOnline()) {
            throw AppException.NetworkException(
                "Pas de connexion internet. Vérifiez votre réseau et réessayez."
            )
        }
        try {
            withTimeout(writeTimeoutMs) {
                suspendCancellableCoroutine<Unit> { cont ->
                    collection.add(aisle)
                        .addOnSuccessListener {
                            Log.d(tag, "Rayon ajouté : ${aisle.name}")
                            cont.resume(Unit)
                        }
                        .addOnFailureListener { e ->
                            Log.e(tag, "Erreur ajout rayon", e)
                            cont.resumeWithException(
                                AppException.AisleException(
                                    "Impossible d'ajouter le rayon '${aisle.name}'", e
                                )
                            )
                        }
                }
            }
        } catch (e: TimeoutCancellationException) {
            Log.e(tag, "Timeout ajout rayon '${aisle.name}'", e)
            throw AppException.NetworkException(
                "Aucune réponse du serveur. Vérifiez votre connexion et réessayez.", e
            )
        } catch (e: CancellationException) {
            throw e
        } catch (e: AppException.AisleException) {
            throw e
        } catch (e: Exception) {
            throw AppException.AisleException("Erreur inattendue lors de l'ajout du rayon", e)
        }
    }
}