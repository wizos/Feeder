package com.nononsenseapps.feeder.data

import android.arch.persistence.room.TypeConverter
import org.joda.time.DateTime


class DateTimeConverter {
    companion object {
        @JvmStatic
        @TypeConverter
        fun toDateTime(text: String?): DateTime? =
                try {
                    DateTime.parse(text)
                } catch (t: Throwable) {
                    null
                }

        @JvmStatic
        @TypeConverter
        fun toString(dateTime: DateTime?): String? = dateTime?.toString()
    }

}
