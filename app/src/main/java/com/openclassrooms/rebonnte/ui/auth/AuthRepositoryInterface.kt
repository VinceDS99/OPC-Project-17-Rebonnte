package com.openclassrooms.rebonnte.ui.auth

import com.google.firebase.auth.FirebaseUser

interface AuthRepositoryInterface {
    val currentUserEmail: String?
    fun isLoggedIn(): Boolean
    suspend fun signIn(email: String, password: String): Result<FirebaseUser>
    suspend fun register(email: String, password: String): Result<FirebaseUser>
    fun signOut()
}