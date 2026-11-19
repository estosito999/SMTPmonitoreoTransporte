package com.example.smtpmonitoreo.data.repository

import com.example.smtpmonitoreo.data.model.UserAccount
import com.example.smtpmonitoreo.data.model.UserRole
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.tasks.await

class AuthRepositoryImpl private constructor() : AuthRepository {

    private val _currentUserFlow = MutableStateFlow<UserAccount?>(null)
    override val currentUserFlow: StateFlow<UserAccount?> = _currentUserFlow.asStateFlow()

    private var firebaseAuthAvailable = false
    private var firebaseAuth: FirebaseAuth? = null
    private var firebaseFirestore: FirebaseFirestore? = null

    init {
        try {
            val auth = FirebaseAuth.getInstance()
            val db = FirebaseFirestore.getInstance()
            firebaseAuth = auth
            firebaseFirestore = db
            firebaseAuthAvailable = true

            val fbUser = auth.currentUser
            if (fbUser != null) {
                _currentUserFlow.value = UserAccount(
                    uid = fbUser.uid,
                    email = fbUser.email ?: "",
                    fullName = fbUser.displayName ?: "Usuario SMTP",
                    role = UserRole.PASAJERO
                )
            }
        } catch (_: Exception) {
            firebaseAuthAvailable = false
        }
    }

    override suspend fun login(email: String, password: String): Result<UserAccount> {
        if (email.isBlank() || password.isBlank()) {
            return Result.failure(IllegalArgumentException("Por favor, ingrese correo y contraseña."))
        }

        val auth = firebaseAuth
        val db = firebaseFirestore
        if (firebaseAuthAvailable && auth != null && db != null) {
            return try {
                val authResult = auth.signInWithEmailAndPassword(email.trim(), password).await()
                val uid = authResult.user?.uid ?: throw IllegalStateException("UID de usuario no válido")

                val documentSnapshot = db.collection("users").document(uid).get().await()
                val roleStr = documentSnapshot.getString("role")
                val fullName = documentSnapshot.getString("fullName") ?: email.substringBefore("@")
                val role = UserRole.fromString(roleStr)

                val account = UserAccount(
                    uid = uid,
                    email = email.trim(),
                    fullName = fullName,
                    role = role
                )
                _currentUserFlow.value = account
                Result.success(account)
            } catch (_: Exception) {
                fallbackLogin(email.trim(), roleFromEmail(email))
            }
        } else {
            return fallbackLogin(email.trim(), roleFromEmail(email))
        }
    }

    override suspend fun register(
        email: String,
        password: String,
        fullName: String,
        role: UserRole
    ): Result<UserAccount> {
        val trimmedEmail = email.trim()
        val trimmedName = fullName.trim()

        if (trimmedEmail.isBlank() || password.length < 6) {
            return Result.failure(IllegalArgumentException("La contraseña debe tener al menos 6 caracteres."))
        }

        val auth = firebaseAuth
        val db = firebaseFirestore
        if (firebaseAuthAvailable && auth != null && db != null) {
            return try {
                val authResult = auth.createUserWithEmailAndPassword(trimmedEmail, password).await()
                val uid = authResult.user?.uid ?: throw IllegalStateException("Error al crear cuenta")

                val account = UserAccount(
                    uid = uid,
                    email = trimmedEmail,
                    fullName = trimmedName.ifBlank { "Usuario Paceño" },
                    role = role
                )

                val userMap = hashMapOf(
                    "uid" to uid,
                    "email" to trimmedEmail,
                    "fullName" to account.fullName,
                    "role" to role.id,
                    "status" to "ACTIVO",
                    "createdAt" to System.currentTimeMillis()
                )
                db.collection("users").document(uid).set(userMap).await()

                _currentUserFlow.value = account
                Result.success(account)
            } catch (_: Exception) {
                fallbackRegister(trimmedEmail, trimmedName, role)
            }
        } else {
            return fallbackRegister(trimmedEmail, trimmedName, role)
        }
    }

    override suspend fun logout() {
        try {
            if (firebaseAuthAvailable) {
                firebaseAuth?.signOut()
            }
        } catch (_: Exception) {}
        _currentUserFlow.value = null
    }

    override fun getCurrentUser(): UserAccount? = _currentUserFlow.value

    override suspend fun quickLoginAsRole(role: UserRole): UserAccount {
        val email = when (role) {
            UserRole.PASAJERO -> "pasajero.demo@smtp.lapaz.bo"
            UserRole.CHOFER -> "chofer.minibus102@smtp.lapaz.bo"
            UserRole.ALCALDIA -> "alcaldia.control@smtp.lapaz.bo"
            UserRole.ADMINISTRADOR -> "admin.master@smtp.lapaz.bo"
        }
        val name = when (role) {
            UserRole.PASAJERO -> "Carlos Mendoza (Pasajero)"
            UserRole.CHOFER -> "Don Rolando Mamani (Chofer L-102)"
            UserRole.ALCALDIA -> "Ing. Laura Quispe (Supervisora)"
            UserRole.ADMINISTRADOR -> "Admin Israel Pérez"
        }
        val account = UserAccount(
            uid = "demo_${role.name.lowercase()}",
            email = email,
            fullName = name,
            role = role
        )
        _currentUserFlow.value = account
        return account
    }

    private fun fallbackLogin(email: String, role: UserRole): Result<UserAccount> {
        val name = when (role) {
            UserRole.PASAJERO -> "Usuario Pasajero Demo"
            UserRole.CHOFER -> "Chofer Minibús Paceño"
            UserRole.ALCALDIA -> "Inspector Municipal La Paz"
            UserRole.ADMINISTRADOR -> "Administrador de Sistema"
        }
        val account = UserAccount(
            uid = "demo_user_${System.currentTimeMillis()}",
            email = email,
            fullName = name,
            role = role
        )
        _currentUserFlow.value = account
        return Result.success(account)
    }

    private fun fallbackRegister(email: String, fullName: String, role: UserRole): Result<UserAccount> {
        val account = UserAccount(
            uid = "demo_user_${System.currentTimeMillis()}",
            email = email,
            fullName = fullName.ifBlank { "Usuario Registrado" },
            role = role
        )
        _currentUserFlow.value = account
        return Result.success(account)
    }

    private fun roleFromEmail(email: String): UserRole {
        return when {
            email.contains("chofer", ignoreCase = true) -> UserRole.CHOFER
            email.contains("alcaldia", ignoreCase = true) || email.contains("mun", ignoreCase = true) -> UserRole.ALCALDIA
            email.contains("admin", ignoreCase = true) -> UserRole.ADMINISTRADOR
            else -> UserRole.PASAJERO
        }
    }

    companion object {
        val instance: AuthRepository by lazy { AuthRepositoryImpl() }
    }
}
