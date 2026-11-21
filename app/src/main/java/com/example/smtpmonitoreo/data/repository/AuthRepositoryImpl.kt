package com.example.smtpmonitoreo.data.repository


import com.example.smtpmonitoreo.data.model.AccountStatus
import com.example.smtpmonitoreo.data.model.UserAccount
import com.example.smtpmonitoreo.data.model.UserRole
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.FirebaseUser
import com.google.firebase.firestore.FirebaseFirestore
import kotlinx.coroutines.CancellationException
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import kotlinx.coroutines.tasks.await

private const val USERS_COLLECTION = "users"

class AuthRepositoryImpl(
    private val auth: FirebaseAuth,
    private val db: FirebaseFirestore
) : AuthRepository {

    private val scope = CoroutineScope(SupervisorJob() + Dispatchers.Default)

    private val _session = MutableStateFlow<SessionState>(SessionState.Loading)
    override val session: StateFlow<SessionState> = _session.asStateFlow()

    private val _currentUser = MutableStateFlow<UserAccount?>(null)
    override val currentUserFlow: StateFlow<UserAccount?> = _currentUser.asStateFlow()

    init {
        restoreSession()
    }

    override fun getCurrentUser(): UserAccount? = _currentUser.value

    // ---------- Inicio de sesión ----------

    override suspend fun login(email: String, password: String): Result<UserAccount> = attempt {
        val result = auth.signInWithEmailAndPassword(email.trim(), password).await()
        val fbUser = result.user ?: throw IllegalStateException("No se pudo iniciar sesión.")
        try {
            loadAndCheck(fbUser).also { publish(it) }
        } catch (e: Exception) {
            auth.signOut() // no dejar una sesión de Firebase a medias
            throw e
        }
    }

    // ---------- Registro: siempre como Pasajero ----------

    override suspend fun register(email: String, password: String, fullName: String): Result<UserAccount> =
        attempt {
            val result = auth.createUserWithEmailAndPassword(email.trim(), password).await()
            val fbUser = result.user ?: throw IllegalStateException("No se pudo crear la cuenta.")
            val account = UserAccount(
                uid = fbUser.uid,
                email = email.trim(),
                fullName = fullName.trim(),
                role = UserRole.PASAJERO
            )
            try {
                db.collection(USERS_COLLECTION).document(fbUser.uid).set(account.toDocument()).await()
            } catch (e: Exception) {
                auth.signOut() // el próximo inicio de sesión completará el perfil
                throw e
            }
            publish(account)
            account
        }

    override suspend fun logout() {
        auth.signOut()
        publish(null)
    }
    override suspend fun refreshProfile() {
        val fbUser = auth.currentUser ?: return // sesión de Firebase real
        try {
            publish(loadAndCheck(fbUser))
        } catch (e: AccountSuspendedException) {
            publish(null)
        } catch (e: CancellationException) {
            throw e
        } catch (e: Exception) {
            // Sin conexión: se conserva el perfil que ya estaba cargado
        }
    }



    // ---------- Internos ----------

    /** Restaura la sesión guardada por Firebase, con el rol real leído de Firestore. */
    private fun restoreSession() {
        val fbUser = auth.currentUser
        if (fbUser == null) {
            publish(null)
            return
        }
        scope.launch {
            attempt { loadAndCheck(fbUser) }
                .onSuccess { publish(it) }
                .onFailure { publish(null) }
        }
    }

    private suspend fun loadAndCheck(fbUser: FirebaseUser): UserAccount {
        val profile = loadProfile(fbUser)
        if (profile.status == AccountStatus.SUSPENDIDO) {
            auth.signOut()
            throw AccountSuspendedException()
        }
        return profile
    }

    /** El rol sale únicamente del documento users/{uid}. */
    private suspend fun loadProfile(fbUser: FirebaseUser): UserAccount {
        val ref = db.collection(USERS_COLLECTION).document(fbUser.uid)
        val snapshot = ref.get().await()
        val fallbackName = fbUser.email.orEmpty().substringBefore("@")

        if (!snapshot.exists()) {
            // Cuenta sin perfil (p. ej. creada desde la consola): rol de menor privilegio.
            val account = UserAccount(
                uid = fbUser.uid,
                email = fbUser.email.orEmpty(),
                fullName = fbUser.displayName ?: fallbackName,
                role = UserRole.PASAJERO
            )
            ref.set(account.toDocument()).await()
            return account
        }

        return UserAccount(
            uid = fbUser.uid,
            email = snapshot.getString("email") ?: fbUser.email.orEmpty(),
            fullName = snapshot.getString("fullName") ?: fallbackName,
            role = UserRole.fromString(snapshot.getString("role")),
            phone = snapshot.getString("phone").orEmpty(),
            assignedVehicleId = snapshot.getString("assignedVehicleId"),
            status = AccountStatus.entries.firstOrNull { it.name == snapshot.getString("status") }
                ?: AccountStatus.ACTIVO,
            createdAt = snapshot.getLong("createdAt") ?: System.currentTimeMillis()
        )
    }

    private fun publish(user: UserAccount?) {
        _currentUser.value = user
        _session.value = if (user == null) SessionState.SignedOut else SessionState.SignedIn(user)
    }

    private fun UserAccount.toDocument(): Map<String, Any> = mapOf(
        "uid" to uid,
        "email" to email,
        "fullName" to fullName,
        "role" to role.id,
        "status" to status.name,
        "phone" to phone,
        "createdAt" to createdAt
    )



    /** Ejecuta el bloque y traduce cualquier error a un mensaje para el usuario. */
    private inline fun <T> attempt(block: () -> T): Result<T> =
        try {
            Result.success(block())
        } catch (e: CancellationException) {
            throw e
        } catch (e: Exception) {
            Result.failure(AuthException(AuthErrors.toUserMessage(e), e))
        }
}