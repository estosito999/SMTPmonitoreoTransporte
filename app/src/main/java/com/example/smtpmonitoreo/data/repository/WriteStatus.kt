package com.example.smtpmonitoreo.data.repository

/** Resultado de una escritura: confirmada por el servidor o guardada localmente en espera de conexión. */
enum class WriteStatus { SYNCED, QUEUED }