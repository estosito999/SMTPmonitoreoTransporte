package com.example.smtpmonitoreo.data.repository

import com.google.firebase.FirebaseNetworkException
import com.google.firebase.FirebaseTooManyRequestsException
import com.google.firebase.auth.FirebaseAuthInvalidCredentialsException
import com.google.firebase.auth.FirebaseAuthInvalidUserException
import com.google.firebase.auth.FirebaseAuthUserCollisionException
import com.google.firebase.auth.FirebaseAuthWeakPasswordException
import com.google.firebase.firestore.FirebaseFirestoreException

/** Error de autenticación con un mensaje ya listo para mostrar al usuario. */
class AuthException(message: String, cause: Throwable? = null) : Exception(message, cause)

class AccountSuspendedException :
    Exception("Tu cuenta está suspendida. Contacta al administrador.")

object AuthErrors {
    fun toUserMessage(e: Throwable): String = when (e) {
        is AccountSuspendedException -> e.message.orEmpty()
        // WeakPassword hereda de InvalidCredentials: debe ir primero
        is FirebaseAuthWeakPasswordException -> "La contraseña es muy débil. Usa al menos 6 caracteres."
        is FirebaseAuthInvalidCredentialsException -> "Correo o contraseña incorrectos."
        is FirebaseAuthInvalidUserException -> "La cuenta no existe o fue deshabilitada."
        is FirebaseAuthUserCollisionException -> "Ya existe una cuenta con ese correo."
        is FirebaseNetworkException -> "Sin conexión a internet. Verifica tu red e inténtalo de nuevo."
        is FirebaseTooManyRequestsException -> "Demasiados intentos. Espera unos minutos."
        is FirebaseFirestoreException -> when (e.code) {
            FirebaseFirestoreException.Code.PERMISSION_DENIED ->
                "Sin permiso en Firestore. Revisa las reglas de seguridad."
            FirebaseFirestoreException.Code.UNAVAILABLE ->
                "No se pudo conectar con el servidor. Revisa tu conexión."
            else -> "Error del servidor (${e.code})."
        }
        else -> e.localizedMessage ?: "Error de autenticación."
    }
}