// SPDX-License-Identifier: GPL-3.0-or-later
package com.otasks.app.data.repository

sealed interface WriteResult<out T> {
    data class Success<T>(
        val value: T,
    ) : WriteResult<T>

    data class Invalid(
        val reason: Reason,
    ) : WriteResult<Nothing>
}

enum class Reason { PAST_PLAN, INVALID_SCOPE, INVALID_VALUE, NOT_FOUND, NOT_VISIBLE }
