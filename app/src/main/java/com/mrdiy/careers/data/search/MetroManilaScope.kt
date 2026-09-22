package com.mrdiy.careers.data.search

import com.mrdiy.careers.model.Job

object MetroManilaScope {
    val metroTerms = listOf(
        "metro manila", "ncr", "manila", "quezon city", "makati", "pasig", "taguig",
        "mandaluyong", "marikina", "paranaque", "parañaque", "las pinas", "las piñas",
        "muntinlupa", "caloocan", "navotas", "malabon", "valenzuela", "san juan",
        "pasay", "pateros"
    )
    val surroundingTerms = listOf("cavite", "laguna", "bulacan", "rizal")
    val allowedTerms = metroTerms + surroundingTerms

    fun contains(job: Job): Boolean = contains(job.location) || contains(job.branch)

    fun contains(location: String): Boolean = matches(location, allowedTerms)
    fun isMetro(location: String): Boolean = matches(location, metroTerms)
    fun matches(location: String, configuredTerms: List<String>): Boolean {
        val normalized = location.lowercase().replace("-", " ").trim()
        return normalized.isNotBlank() && configuredTerms.any {
            Regex("(?<![a-z])${Regex.escape(it)}(?![a-z])").containsMatchIn(normalized)
        }
    }
}
