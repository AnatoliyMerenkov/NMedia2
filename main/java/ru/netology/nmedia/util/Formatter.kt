package ru.netology.nmedia.util

import android.icu.text.SimpleDateFormat
import android.os.Build
import androidx.annotation.RequiresApi
import java.time.OffsetDateTime
import java.util.*

object Formatter {

    @RequiresApi(Build.VERSION_CODES.N)
    private val POST_DATE_FORMAT = SimpleDateFormat("HH:mm dd.MM.yyyy", Locale.ROOT)

    fun numberToShortFormat(number: Int): String {
        return when {
            number in  1_000..1_099 -> "1K"
            number in  1_100..9_999 && number % 1000 == 0 -> "${number / 1000}K"
            number in  1_100..9_999 && number % 1000 != 0 -> ((number / 100).toDouble() / 10).toString() + "K"
            number in 10_000..999_999 -> "${number / 1000}K"
            number >= 1_000_000 && number % 1000000 == 0 -> "${number / 1000000}M"
            number >= 1_000_000 && number % 1000000 != 0 -> ((number / 100000).toDouble() / 10).toString() + "M"
            else -> number.toString()
        }
    }

    @RequiresApi(Build.VERSION_CODES.O)
    fun dateMilliToPostDateFormat(published: Long): String {
        val secondsAgo = OffsetDateTime.now().toEpochSecond() - published
        return when (secondsAgo) {
            in 0..60 -> "только что"
            in 61..(60 * 60) -> "${minutesToText(secondsAgo)} назад"
            in (60 * 60 + 1)..(24 * 60 * 60) -> "${hoursToText(secondsAgo)} назад"
            else -> milliToDate(published)
        }
    }

    @RequiresApi(Build.VERSION_CODES.N)
    fun milliToDate(published: Long): String {
        val date = Date(published * 1000)
        return POST_DATE_FORMAT.format(date)
    }

    private fun minutesToText(seconds: Long): String {
        val minutes = seconds / 60
        if (minutes in 11..14)
            return "$minutes минут"
        return when(minutes % 10) {
            1L -> "$minutes минуту"
            2L, 3L, 4L -> "$minutes минуты"
            else -> "$minutes минут"
        }
    }

    private fun hoursToText(seconds: Long): String {
        val hours = seconds / (60 * 60)
        if (hours in 11..14)
            return "$hours часов"
        return when(hours % 10) {
            1L -> "$hours час"
            2L, 3L, 4L -> "$hours часа"
            else -> "$hours часов"
        }
    }
}