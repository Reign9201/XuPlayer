package com.yancy.xu.xuplayer.db

import androidx.room.TypeConverter
import java.util.*

class LiveSourceConverters {
    @TypeConverter
    fun fromSet(strings: Set<String?>?): String? {
        if (strings.isNullOrEmpty()) {
            return null
        }
        val sb = StringBuilder()
        var delim = ""
        for (s in strings) {
            sb.append(delim)
            sb.append(s)
            delim = ","
        }
        return sb.toString()
    }

    @TypeConverter
    fun toSet(string: String?): Set<String>? {
        if (string.isNullOrEmpty()) {
            return null
        }
        val array = string.split(",".toRegex()).dropLastWhile { it.isEmpty() }.toTypedArray()
        return HashSet(listOf(*array))
    }
}