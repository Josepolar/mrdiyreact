package com.mrdiy.careers.data.search

import com.mrdiy.careers.model.Job

object MetroManilaScope {
    private val allowedTerms = listOf(
        "metro manila", "ncr", "manila", "quezon", "makati", "pasig", "taguig",
        "mandaluyong", "marikina", "paranaque", "parañaque", "las pinas", "las piñas",
        "muntinlupa", "caloocan", "navotas", "malabon", "valenzuela", "san juan",
        "pasay", "pateros", "cavite", "laguna", "bulacan", "rizal"
    )

    fun contains(job: Job): Boolean = contains(job.location) || contains(job.branch)

    fun contains(location: String): Boolean {
        val normalized = location.lowercase().replace("-", " ").trim()
        return normalized.isNotBlank() && allowedTerms.any(normalized::contains)
    }
}
