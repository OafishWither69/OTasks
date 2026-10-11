// SPDX-License-Identifier: GPL-3.0-or-later
package com.otasks.app.data.db
import androidx.room.TypeConverter
import java.time.LocalDate

class Converters {
    @TypeConverter fun date(value: Long?): LocalDate? = value?.let(LocalDate::ofEpochDay)

    @TypeConverter fun epoch(value: LocalDate?): Long? = value?.toEpochDay()
}
