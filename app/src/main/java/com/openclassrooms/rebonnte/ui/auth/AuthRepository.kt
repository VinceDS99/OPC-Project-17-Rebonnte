package com.openclassrooms.rebonnte.ui.auth

import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.FirebaseUser
import com.openclassrooms.rebonnte.core.AppException
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.suspendCancellableCoroutine
import kotlinx.coroutines.withContext
import javax.inject.Inject
import kotlin.coroutines.resume

class AuthRepository @Inject constructor() : AuthRepositoryInterface {

    private val auth = FirebaseAuth.getInstance()

    override val currentUserEmail: String?
        get() = auth.currentUser?.email

    override fun isLoggedIn(): Boolean = auth.currentUser != null

    override suspend fun signIn(email: String, password: String): Result<FirebaseUser> =
        withContext(Dispatchers.IO) {
            try {
                suspendCancellableCoroutine { cont ->
                    auth.signInWithEmailAndPassword(email, password)
                        .addOnSuccessListener { cont.resume(Result.success(it.user!!)) }
                        .addOnFailureListener { e ->
                            cont.resume(
                                Result.failure(
                                    AppException.AuthException(
                                        e.localizedMessage ?: "Erreur de connexion", e
                                    )
                                )
                            )
                        }
                }
            } catch (e: Exception) {
                Result.failure(AppException.AuthException("Erreur inattendue lors de la connexion", e))
            }
        }

    override suspend fun register(
        email: String,
        password: String
    ): Result<FirebaseUser> = withContext(Dispatchers.IO) {
        suspendCancellableCoroutine { cont ->
            auth.createUserWithEmailAndPassword(email, password)
                .addOnSuccessListener { cont.resume(Result.success(it.user!!)) }
                .addOnFailureListener { cont.resume(Result.failure(it)) }
        }
    }

    override fun signOut() = auth.signOut()
}