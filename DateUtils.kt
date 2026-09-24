package com.ovijat.bakerystock.utils

import java.text.SimpleDateFormat
import java.util.Calendar
import java.util.Date
import java.util.Locale
import java.util.TimeZone

object DateUtils {
    private val dhakaTimeZone = TimeZone.getTimeZone("Asia/Dhaka")

    fun getStartOfDay(timestamp: Long = System.currentTimeMillis()): Long {
        val cal = Calendar.getInstance(dhakaTimeZone)
        cal.timeInMillis = timestamp
        cal.set(Calendar.HOUR_OF_DAY, 0)
        cal.set(Calendar.MINUTE, 0)
        cal.set(Calendar.SECOND, 0)
        cal.set(Calendar.MILLISECOND, 0)
        return cal.timeInMillis
    }

    fun getStartOfMonth(year: Int, month: Int): Long {
        val cal = Calendar.getInstance(dhakaTimeZone)
        cal.set(Calendar.YEAR, year)
        cal.set(Calendar.MONTH, month)
        cal.set(Calendar.DAY_OF_MONTH, 1)
        cal.set(Calendar.HOUR_OF_DAY, 0)
        cal.set(Calendar.MINUTE, 0)
        cal.set(Calendar.SECOND, 0)
        cal.set(Calendar.MILLISECOND, 0)
        return cal.timeInMillis
    }

    fun getEndOfMonth(year: Int, month: Int): Long {
        val cal = Calendar.getInstance(dhakaTimeZone)
        cal.set(Calendar.YEAR, year)
        cal.set(Calendar.MONTH, month)
        cal.set(Calendar.DAY_OF_MONTH, cal.getActualMaximum(Calendar.DAY_OF_MONTH))
        cal.set(Calendar.HOUR_OF_DAY, 23)
        cal.set(Calendar.MINUTE, 59)
        cal.set(Calendar.SECOND, 59)
        cal.set(Calendar.MILLISECOND, 999)
        return cal.timeInMillis
    }

    fun getDaysInMonth(year: Int, month: Int): Int {
        val cal = Calendar.getInstance(dhakaTimeZone)
        cal.set(Calendar.YEAR, year)
        cal.set(Calendar.MONTH, month)
        return cal.getActualMaximum(Calendar.DAY_OF_MONTH)
    }

    fun formatDate(timestamp: Long): String {
        val sdf = SimpleDateFormat("dd MMM yyyy", Locale.US)
        sdf.timeZone = dhakaTimeZone
        return sdf.format(Date(timestamp))
    }

    fun formatMonthYear(year: Int, month: Int): String {
        val cal = Calendar.getInstance(dhakaTimeZone)
        cal.set(Calendar.YEAR, year)
        cal.set(Calendar.MONTH, month)
        val sdf = SimpleDateFormat("MMMM yyyy", Locale.US)
        sdf.timeZone = dhakaTimeZone
        return sdf.format(cal.time)
    }

    // Format: M2B_YYYYMMDD_HHMMSS_xxxx
    fun generateBatchId(timestamp: Long = System.currentTimeMillis()): String {
        val sdf = SimpleDateFormat("yyyyMMdd_HHmmss", Locale.US)
        sdf.timeZone = dhakaTimeZone
        val randomSuffix = (1000..9999).random()
        return "M2B_${sdf.format(Date(timestamp))}_$randomSuffix"
    }
}