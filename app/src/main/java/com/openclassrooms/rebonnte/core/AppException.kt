package com.openclassrooms.rebonnte.core

/**
 * Exceptions du projet.
 * Permet de distinguer les erreurs selon leur origine.
 */

sealed class AppException(
    message: String,
    cause: Throwable? = null
) : Exception(message, cause) {

    class MedicineException(message: String, cause: Throwable? = null) :
        AppException(message, cause)

    class AisleException(message: String, cause: Throwable? = null) :
        AppException(message, cause)

    class AuthException(message: String, cause: Throwable? = null) :
        AppException(message, cause)

    class NetworkException(message: String, cause: Throwable? = null) :
        AppException(message, cause)
}