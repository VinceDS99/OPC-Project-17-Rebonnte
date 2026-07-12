package com.openclassrooms.rebonnte.ui.aisle

import android.util.Log
import com.google.firebase.firestore.FirebaseFirestore
import com.openclassrooms.rebonnte.core.AppException
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.channels.awaitClose
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.callbackFlow
import kotlinx.coroutines.suspendCancellableCoroutine
import kotlinx.coroutines.withContext
import javax.inject.Inject
import kotlin.coroutines.resume
import kotlin.coroutines.resumeWithException

class AisleRepository @Inject constructor() {

    private val collection = FirebaseFirestore.getInstance().collection("aisles")
    private val tag = "AisleRepository"

    fun getAisles(): Flow<List<Aisle>> = callbackFlow {
        val listener = collection
            .orderBy("name") // tri alphabétique des rayons côté Firestore
            .addSnapshotListener { snapshot, error ->
                if (error != null) {
                    Log.w(tag, "error", error)
                    trySend(emptyList())
                    return@addSnapshotListener
                }
                val aisles = snapshot?.documents?.mapNotNull { doc ->
                    try {
                        doc.toObject(Aisle::class.java)
                    } catch (e: Exception) {
                        Log.e(tag, "Erreur rayon ${doc.id}", e)
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
        try {
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
        } catch (e: AppException) {
            throw e
        } catch (e: Exception) {
            throw AppException.AisleException("Erreur inattendue lors de l'ajout du rayon", e)
        }
    }
}