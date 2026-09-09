package com.mrdiy.careers.model

import java.util.Calendar
import kotlinx.serialization.Serializable

@Serializable
data class WorkExperience(
    val id: String = System.currentTimeMillis().toString(),
    val role: String = "",
    val company: String = "",
    val startYear: String = "",
    val endYear: String = "Present",
    val isCurrent: Boolean = true,
    val description: String = ""
) {
    val initials: String get() = company
        .split(" ")
        .take(2)
        .mapNotNull { it.firstOrNull()?.uppercaseChar() }
        .joinToString("")
        .ifEmpty { "?" }

    val displayPeriod: String get() {
        val end = if (isCurrent) "Present" else endYear
        if (startYear.isEmpty()) return end
        val currentYear = Calendar.getInstance().get(Calendar.YEAR)
        val start = startYear.toIntOrNull() ?: currentYear
        val diff = if (isCurrent) currentYear - start
        else (endYear.toIntOrNull() ?: start) - start
        val duration = when {
            diff <= 0 -> "< 1 yr"
            diff == 1 -> "1 yr"
            else      -> "$diff yrs"
        }
        return "$startYear – $end · $duration"
    }
}