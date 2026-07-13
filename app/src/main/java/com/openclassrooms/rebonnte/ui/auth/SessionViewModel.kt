package com.openclassrooms.rebonnte.ui.auth

import androidx.lifecycle.ViewModel
import dagger.hilt.android.lifecycle.HiltViewModel
import javax.inject.Inject

@HiltViewModel
class SessionViewModel @Inject constructor(
    private val authRepository: AuthRepositoryInterface
) : ViewModel() {

    val currentUserEmail: String
        get() = authRepository.currentUserEmail ?: "unknown"

    fun isLoggedIn(): Boolean = authRepository.isLoggedIn()

    fun signOut() = authRepository.signOut()
}