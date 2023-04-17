package ru.netology.nmedia.util

object Formatter {
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
}