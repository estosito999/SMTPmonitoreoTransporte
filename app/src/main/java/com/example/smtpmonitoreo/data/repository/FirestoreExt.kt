package com.example.smtpmonitoreo.data.repository

import com.example.smtpmonitoreo.data.model.UserAccount
import com.google.firebase.firestore.DocumentSnapshot
import com.google.firebase.firestore.Query
import kotlinx.coroutines.CancellationException
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.channels.awaitClose
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.callbackFlow
import kotlinx.coroutines.flow.catch
import kotlinx.coroutines.flow.flatMapLatest
import kotlinx.coroutines.flow.flowOf
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.stateIn
import com.google.android.gms.tasks.Task
import kotlinx.coroutines.tasks.await
import kotlinx.coroutines.withTimeoutOrNull

/** Escucha una consulta de Firestore en tiempo real. */
internal fun Query.snapshotFlow(): Flow<List<DocumentSnapshot>> = callbackFlow {
    val registration = addSnapshotListener { snapshot, error ->
        if (error != null) {
            close(error)
        } else if (snapshot != null) {
            trySend(snapshot.documents)
        }
    }
    awaitClose { registration.remove() }
}

/**
 * Convierte la consulta que corresponde al usuario actual en un StateFlow de modelos.
 * Sin sesión (o sin permiso para ese rol, queryFor = null) emite una lista vacía.
 */
@OptIn(ExperimentalCoroutinesApi::class)
internal fun <T : Any> Flow<UserAccount?>.listenTo(
    scope: CoroutineScope,
    queryFor: (UserAccount) -> Query?,
    map: (DocumentSnapshot) -> T?
): StateFlow<List<T>> = flatMapLatest { user ->
    val query = user?.let(queryFor)
    if (query == null) {
        flowOf(emptyList<T>())
    } else {
        query.snapshotFlow()
            .map { docs -> docs.mapNotNull(map) }
            .catch { emit(emptyList<T>()) }
    }
}.stateIn(scope, SharingStarted.WhileSubscribed(5_000), emptyList())

/** Ejecuta el bloque y traduce cualquier error de Firebase a un mensaje para el usuario. */
/** Punto de enganche para reportar errores inesperados de las escrituras (lo configura SmtpApp). */
internal object WriteErrorReporter {
    @Volatile
    var onError: ((Exception) -> Unit)? = null
}

/** Ejecuta el bloque y traduce cualquier error de Firebase a un mensaje para el usuario. */
internal inline fun <T> resultOf(block: () -> T): Result<T> =
    try {
        Result.success(block())
    } catch (e: CancellationException) {
        throw e
    } catch (e: Exception) {
        WriteErrorReporter.onError?.invoke(e)
        Result.failure(AuthException(AuthErrors.toUserMessage(e), e))
    }
/**
 * Espera la confirmación del servidor, pero sin colgarse sin conexión: Firestore guarda la escritura
 * en disco y la envía sola al reconectar. Los errores reales (permisos, etc.) se siguen propagando.
 */
internal suspend fun <T> Task<T>.awaitOrQueue(timeoutMs: Long = 4_000L): WriteStatus {
    val confirmed = withTimeoutOrNull(timeoutMs) {
        await()
        true
    }
    return if (confirmed == true) WriteStatus.SYNCED else WriteStatus.QUEUED
}