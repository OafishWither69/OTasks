// SPDX-License-Identifier: GPL-3.0-or-later
package com.otasks.app.data.repository

import androidx.room.withTransaction
import com.otasks.app.data.db.OTasksDatabase

private class RejectedWrite(
    val result: WriteResult.Invalid,
) : RuntimeException()

// H1/D6: a rejected mutation must roll back its history materialization too.
internal suspend fun <T> OTasksDatabase.writeTransaction(block: suspend () -> WriteResult<T>): WriteResult<T> =
    try {
        withTransaction {
            block().also { if (it is WriteResult.Invalid) throw RejectedWrite(it) }
        }
    } catch (rejected: RejectedWrite) {
        rejected.result
    }
