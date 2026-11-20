package com.example.smtpmonitoreo.domain

import com.example.smtpmonitoreo.data.model.AccountStatus
import com.example.smtpmonitoreo.data.model.UserAccount
import com.example.smtpmonitoreo.data.model.UserRole

data class AccountDraft(
    val fullName: String = "",
    val email: String = "",
    val role: UserRole = UserRole.PASAJERO
)

object UserAdminRules {
    private val emailPattern = Regex("^[^@\\s]+@[^@\\s]+\\.[^@\\s]+$")

    fun validateNewAccount(draft: AccountDraft, existing: List<UserAccount>): Validation<UserAccount> {
        val name = draft.fullName.trim()
        val email = draft.email.trim()

        if (name.isEmpty()) return Validation.Error("Ingresa el nombre completo.")
        if (!emailPattern.matches(email)) return Validation.Error("Correo electrónico inválido.")
        if (existing.any { it.email.equals(email, ignoreCase = true) }) {
            return Validation.Error("Ya existe una cuenta con ese correo.")
        }

        return Validation.Ok(
            UserAccount(email = email, fullName = name, role = draft.role, status = AccountStatus.ACTIVO)
        )
    }

    /** Devuelve el motivo del rechazo, o null si el cambio de rol está permitido. */
    fun roleChangeError(
        users: List<UserAccount>,
        target: UserAccount,
        newRole: UserRole,
        actorEmail: String?
    ): String? {
        if (target.role == newRole) return null
        if (isSelf(target, actorEmail)) return "No puedes cambiar tu propio rol."
        if (isLastActiveAdmin(users, target)) return "Debe existir al menos un administrador activo."
        return null
    }

    /** Devuelve el motivo del rechazo, o null si se puede suspender (reactivar siempre se puede). */
    fun suspensionError(
        users: List<UserAccount>,
        target: UserAccount,
        actorEmail: String?
    ): String? {
        if (target.status == AccountStatus.SUSPENDIDO) return null
        if (isSelf(target, actorEmail)) return "No puedes suspender tu propia cuenta."
        if (isLastActiveAdmin(users, target)) return "Debe existir al menos un administrador activo."
        return null
    }

    private fun isSelf(target: UserAccount, actorEmail: String?): Boolean =
        actorEmail != null && target.email.equals(actorEmail, ignoreCase = true)

    /** El objetivo es administrador activo y no hay otro administrador activo. */
    private fun isLastActiveAdmin(users: List<UserAccount>, target: UserAccount): Boolean =
        target.role == UserRole.ADMINISTRADOR &&
                target.status == AccountStatus.ACTIVO &&
                users.none {
                    it.uid != target.uid &&
                            it.role == UserRole.ADMINISTRADOR &&
                            it.status == AccountStatus.ACTIVO
                }
}