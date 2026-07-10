package com.openclassrooms.rebonnte.ui.aisle

import com.google.firebase.firestore.FirebaseFirestore
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

    fun getAisles(): Flow<List<Aisle>> = callbackFlow {
        val listener = collection.addSnapshotListener { snapshot, error ->
            if (error != null) {

                trySend(emptyList())
                return@addSnapshotListener
            }
            val aisles = snapshot?.documents?.mapNotNull {
                it.toObject(Aisle::class.java)
            } ?: emptyList()
            trySend(aisles)
        }
        awaitClose { listener.remove() }
    }

    suspend fun addAisle(aisle: Aisle) = withContext(Dispatchers.IO) {
        suspendCancellableCoroutine<Unit> { cont ->
            collection.add(aisle)
                .addOnSuccessListener { cont.resume(Unit) }
                .addOnFailureListener { cont.resumeWithException(it) }
        }
    }
}