package com.mrdiy.careers.data.search

import com.mrdiy.careers.model.Job
import com.mrdiy.careers.model.UserProfile
import kotlin.math.max

/** Explainable, occupation-agnostic retrieval and ranking for the client fallback/search path. */
object UniversalJobSearchEngine {
    data class QueryIntent(
        val raw: String,
        val normalizedTerms: Set<String>,
        val occupationFamily: String?,
        val locationTerms: Set<String>,
        val employmentType: String?
    )

    data class RankedJob(val job: Job, val score: Int, val reasons: List<String>)

    private val families = mapOf(
        "cashier" to setOf("cashier", "checkout", "counter staff", "retail associate", "sales associate", "store crew", "customer service"),
        "cleaning" to setOf("janitor", "cleaner", "custodian", "utility", "housekeeping", "maintenance helper"),
        "warehouse" to setOf("warehouse", "stock clerk", "inventory", "warehouse helper", "logistics"),
        "driver" to setOf("driver", "delivery", "rider", "courier", "company driver"),
        "sales" to setOf("sales", "sales associate", "promodiser", "merchandiser", "retail associate"),
        "accounting" to setOf("accountant", "accounting", "bookkeeper", "finance"),
        "technology" to setOf("developer", "programmer", "software engineer", "web developer", "react", "backend", "frontend", "data analyst", "it support"),
        "management" to setOf("supervisor", "manager", "operations manager", "store manager", "executive"),
        "healthcare" to setOf("nurse", "caregiver", "medical", "clinic"),
        "education" to setOf("teacher", "tutor", "instructor")
    )

    private val typoAliases = mapOf(
        "casher" to "cashier", "janetor" to "janitor", "wearhouse" to "warehouse",
        "developper" to "developer", "saleslady" to "sales associate", "warehouseman" to "warehouse"
    )

    fun parse(query: String): QueryIntent {
        val rawTerms = query.trim().lowercase().split(Regex("[^a-z0-9+]+"))
            .filter { it.isNotBlank() }
        val normalized = rawTerms.map { typoAliases[it] ?: it }.toSet()
        val family = families.entries.firstOrNull { (_, aliases) ->
            normalized.any { term -> aliases.any { alias -> alias.contains(term) || term.contains(alias) } }
        }?.key
        val employment = when {
            normalized.contains("part") && normalized.contains("time") -> "part-time"
            normalized.contains("full") && normalized.contains("time") -> "full-time"
            normalized.contains("remote") -> "remote"
            else -> null
        }
        val locations = normalized.filterNot { it in setOf("near", "me", "no", "experience", "part", "time", "full", "remote") }.toSet()
        return QueryIntent(query, normalized, family, locations, employment)
    }

    fun search(query: String, jobs: List<Job>, profile: UserProfile? = null): List<RankedJob> {
        val intent = parse(query)
        if (query.isBlank()) return jobs.map { RankedJob(it, 0, listOf("Available job")) }
        return jobs.mapNotNull { job -> score(intent, job, profile) }
            .sortedWith(compareByDescending<RankedJob> { it.score }.thenBy { it.job.title })
    }

    private fun score(intent: QueryIntent, job: Job, profile: UserProfile?): RankedJob? {
        val title = job.title.lowercase()
        val document = listOf(job.title, job.category, job.level, job.location, job.branch, job.aboutRole, job.requirements.joinToString(" ")).joinToString(" ").lowercase()
        val exact = if (intent.raw.trim().lowercase() in title) 100 else 0
        val termMatches = intent.normalizedTerms.count { document.contains(it) }
        val familyAliases = intent.occupationFamily?.let { families[it].orEmpty() }.orEmpty()
        val familyMatch = if (familyAliases.any { document.contains(it) }) 35 else 0
        val locationMatch = if (intent.locationTerms.any { document.contains(it) }) 20 else 0
        val typeMatch = if (intent.employmentType == null || job.jobType.lowercase().contains(intent.employmentType)) 10 else -20
        val relevance = (exact + termMatches * 12 + familyMatch + locationMatch + typeMatch).coerceIn(0, 100)
        val compatibility = profile?.let { profileScore(it, job) } ?: 0
        val finalScore = (relevance * 0.75 + compatibility * 0.25).toInt()
        if (relevance == 0) return null
        val reasons = buildList {
            if (exact > 0) add("Exact title match")
            if (familyMatch > 0) add("Related ${intent.occupationFamily} role")
            if (locationMatch > 0) add("Matches location")
            if (compatibility > 0) add("Fits your profile")
        }.ifEmpty { listOf("Related job") }
        return RankedJob(job, finalScore, reasons)
    }

    private fun profileScore(profile: UserProfile, job: Job): Int {
        val profileText = listOf(profile.desiredPosition, profile.headline, profile.about, profile.skills.joinToString(" ")).joinToString(" ").lowercase()
        val jobText = listOf(job.title, job.category, job.aboutRole, job.requirements.joinToString(" ")).joinToString(" ").lowercase()
        val hits = profileText.split(Regex("[^a-z0-9]+"))
            .filter { it.length > 2 }
            .count { jobText.contains(it) }
        return (hits * 15).coerceIn(0, 100)
    }
}
