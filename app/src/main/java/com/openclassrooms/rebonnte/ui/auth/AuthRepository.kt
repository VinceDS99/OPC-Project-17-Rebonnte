package com.openclassrooms.rebonnte.ui.auth

import com.google.firebase.FirebaseNetworkException
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.FirebaseAuthInvalidCredentialsException
import com.google.firebase.auth.FirebaseAuthInvalidUserException
import com.google.firebase.auth.FirebaseAuthUserCollisionException
import com.google.firebase.auth.FirebaseAuthWeakPasswordException
import com.google.firebase.auth.FirebaseUser
import com.openclassrooms.rebonnte.core.AppException
import com.openclassrooms.rebonnte.core.NetworkMonitor
import kotlinx.coroutines.CancellationException
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.TimeoutCancellationException
import kotlinx.coroutines.suspendCancellableCoroutine
import kotlinx.coroutines.withContext
import kotlinx.coroutines.withTimeout
import javax.inject.Inject
import kotlin.coroutines.resume

class AuthRepository @Inject constructor(
    private val networkMonitor: NetworkMonitor
) : AuthRepositoryInterface {

    private val auth = FirebaseAuth.getInstance()
    private val authTimeoutMs = 15_000L

    override val currentUserEmail: String?
        get() = auth.currentUser?.email

    override fun isLoggedIn(): Boolean = auth.currentUser != null

    override suspend fun signIn(
        email: String,
        password: String
    ): Result<FirebaseUser> = withContext(Dispatchers.IO) {
        if (!networkMonitor.isOnline()) {
            return@withContext Result.failure(
                AppException.NetworkException("Pas de connexion internet. Vérifiez votre réseau.")
            )
        }
        try {
            withTimeout(authTimeoutMs) {
                suspendCancellableCoroutine { cont ->
                    auth.signInWithEmailAndPassword(email, password)
                        .addOnSuccessListener { authResult ->
                            val user = authResult.user
                            if (user != null) {
                                cont.resume(Result.success(user))
                            } else {
                                cont.resume(
                                    Result.failure(
                                        AppException.AuthException("Utilisateur introuvable après connexion")
                                    )
                                )
                            }
                        }
                        .addOnFailureListener { e ->
                            cont.resume(
                                Result.failure(AppException.AuthException(mapAuthErrorMessage(e), e))
                            )
                        }
                }
            }
        } catch (e: TimeoutCancellationException) {
            Result.failure(
                AppException.NetworkException("Aucune réponse du serveur. Vérifiez votre connexion.", e)
            )
        } catch (e: CancellationException) {
            throw e
        } catch (e: Exception) {
            Result.failure(AppException.AuthException("Erreur inattendue lors de la connexion", e))
        }
    }

    override suspend fun register(
        email: String,
        password: String
    ): Result<FirebaseUser> = withContext(Dispatchers.IO) {
        if (!networkMonitor.isOnline()) {
            return@withContext Result.failure(
                AppException.NetworkException("Pas de connexion internet. Vérifiez votre réseau.")
            )
        }
        try {
            withTimeout(authTimeoutMs) {
                suspendCancellableCoroutine { cont ->
                    auth.createUserWithEmailAndPassword(email, password)
                        .addOnSuccessListener { authResult ->
                            val user = authResult.user
                            if (user != null) {
                                cont.resume(Result.success(user))
                            } else {
                                cont.resume(
                                    Result.failure(
                                        AppException.AuthException("Utilisateur introuvable après inscription")
                                    )
                                )
                            }
                        }
                        .addOnFailureListener { e ->
                            cont.resume(
                                Result.failure(AppException.AuthException(mapAuthErrorMessage(e), e))
                            )
                        }
                }
            }
        } catch (e: TimeoutCancellationException) {
            Result.failure(
                AppException.NetworkException("Aucune réponse du serveur. Vérifiez votre connexion.", e)
            )
        } catch (e: CancellationException) {
            throw e
        } catch (e: Exception) {
            Result.failure(AppException.AuthException("Erreur inattendue lors de l'inscription", e))
        }
    }

    override fun signOut() = auth.signOut()

    private fun mapAuthErrorMessage(e: Exception): String = when (e) {
        is FirebaseNetworkException ->
            "Pas de connexion internet. Vérifiez votre réseau et réessayez."
        is FirebaseAuthInvalidCredentialsException ->
            "Email ou mot de passe incorrect."
        is FirebaseAuthInvalidUserException ->
            "Aucun compte ne correspond à cet email."
        is FirebaseAuthUserCollisionException ->
            "Un compte existe déjà avec cet email."
        is FirebaseAuthWeakPasswordException ->
            "Mot de passe trop faible (6 caractères minimum)."
        else ->
            "Erreur d'authentification. Veuillez réessayer."
    }
}