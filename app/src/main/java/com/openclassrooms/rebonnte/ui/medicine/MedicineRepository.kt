package com.openclassrooms.rebonnte.ui.medicine

import com.google.firebase.firestore.FieldValue
import com.google.firebase.firestore.FirebaseFirestore
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

    override fun getMedicines(): Flow<List<Medicine>> = callbackFlow {
        val listener = collection.addSnapshotListener { snapshot, error ->
            if (error != null) { close(error); return@addSnapshotListener }
            val medicines = snapshot?.documents?.mapNotNull { doc ->
                doc.toObject(Medicine::class.java)?.copy(id = doc.id)
            } ?: emptyList()
            trySend(medicines)
        }
        awaitClose { listener.remove() }
    }

    override suspend fun addMedicine(medicine: Medicine) = withContext(Dispatchers.IO) {
        suspendCancellableCoroutine<Unit> { cont ->
            collection.add(medicine)
                .addOnSuccessListener { cont.resume(Unit) }
                .addOnFailureListener { cont.resumeWithException(it) }
        }
    }

    override suspend fun updateStockAndHistory(
        medicineId: String,
        newStock: Int,
        history: History
    ) = withContext(Dispatchers.IO) {
        suspendCancellableCoroutine<Unit> { cont ->
            collection.document(medicineId)
                .update(mapOf("stock" to newStock, "histories" to FieldValue.arrayUnion(history)))
                .addOnSuccessListener { cont.resume(Unit) }
                .addOnFailureListener { cont.resumeWithException(it) }
        }
    }

    override suspend fun deleteMedicine(medicineId: String) = withContext(Dispatchers.IO) {
        suspendCancellableCoroutine<Unit> { cont ->
            collection.document(medicineId)
                .delete()
                .addOnSuccessListener { cont.resume(Unit) }
                .addOnFailureListener { cont.resumeWithException(it) }
        }
    }
}
