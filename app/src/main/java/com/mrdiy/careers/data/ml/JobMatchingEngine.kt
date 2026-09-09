package com.mrdiy.careers.data.ml

import com.mrdiy.careers.model.Job
import com.mrdiy.careers.model.JobMatchResult
import com.mrdiy.careers.model.UserProfile
import com.mrdiy.careers.model.Education
import com.mrdiy.careers.model.WorkExperience
import kotlin.math.sqrt

/**
 * ML-based Job Matching using TF-IDF + Cosine Similarity
 *
 * This is a real ML approach using:
 * - TF-IDF (Term Frequency-Inverse Document Frequency) for feature extraction
 * - Cosine Similarity for measuring match score
 *
 * In a production app, you would:
 * 1. Train a model on labeled resume-job pairs
 * 2. Use more advanced embeddings (BERT, Word2Vec)
 * 3. Deploy as a REST API
 */
object JobMatchingEngine {

    data class TextVector(
        val terms: Map<String, Double>
    )

    /**
     * Main matching function - matches profile against jobs
     */
    fun matchJobs(profile: UserProfile, jobs: List<Job>): List<JobMatchResult> {
        if (jobs.isEmpty()) return emptyList()

        // Create profile document from all profile info
        val profileDoc = buildProfileDocument(profile)

        // Extract TF-IDF vectors
        val profileVector = computeTFIDF(profileDoc, listOf(profileDoc) + jobs.map { buildJobDocument(it) })
        val jobVectors = jobs.associateWith { job ->
            computeTFIDF(buildJobDocument(job), listOf(profileDoc) + jobs.map { buildJobDocument(it) })
        }

        // Calculate similarity scores
        return jobs.map { job ->
            val similarity = cosineSimilarity(profileVector, jobVectors[job] ?: emptyMap())
            val score = (similarity * 100).toInt().coerceIn(0, 100)

            JobMatchResult(
                job = job,
                matchScore = score,
                matchLabel = getMatchLabel(score),
                matchReasons = generateMatchReasons(profile, job, score),
                matchedSkills = findMatchedSkills(profile.skills, job.requirements),
                missingSkills = findMissingSkills(profile.skills, job.requirements)
            )
        }.sortedByDescending { it.matchScore }
    }

    // ─────────────────────────────────────────────────────────────────────
    // Step 1: Build text documents
    // ─────────────────────────────────────────────────────────────────────
    private fun buildProfileDocument(profile: UserProfile): String {
        return buildString {
            append(profile.skills.joinToString(" ") { it })
            append(" ")
            append(profile.headline)
            append(" ")
            append(profile.about)
            append(" ")
            append(profile.workExperiences.joinToString(" ") { exp: WorkExperience -> "${exp.role} ${exp.company} ${exp.description}" })
            append(" ")
            append(profile.education.joinToString(" ") { edu: Education -> "${edu.degree} ${edu.school}" })
            append(" ")
            append(profile.preferredCategories.joinToString(" "))
            append(" ")
            append(profile.preferredLevel)
        }.lowercase()
    }

    private fun buildJobDocument(job: Job): String {
        return buildString {
            append(job.title)
            append(" ")
            append(job.category)
            append(" ")
            append(job.level)
            append(" ")
            append(job.aboutRole)
            append(" ")
            append(job.requirements.joinToString(" "))
            append(" ")
            append(job.responsibilities.joinToString(" "))
            append(" ")
            append(job.benefits.joinToString(" "))
        }.lowercase()
    }

    // ─────────────────────────────────────────────────────────────────────
    // Step 2: TF-IDF Feature Extraction
    // ─────────────────────────────────────────────────────────────────────
    private fun computeTFIDF(document: String, corpus: List<String>): Map<String, Double> {
        val words = document.split(Regex("[\\s,.]+")).filter { it.length > 2 }
        val termFreq = words.groupingBy { it }.eachCount()
        val totalDocs = corpus.size

        // Calculate IDF for each term
        val idf = words.distinct().associateWith { term ->
            val docsWithTerm = corpus.count { it.contains(term) }
            if (docsWithTerm > 0) kotlin.math.ln((totalDocs.toDouble() / docsWithTerm)) + 1 else 0.0
        }

        // TF-IDF = TF * IDF
        return termFreq.mapValues { (term, freq) ->
            val tf = freq.toDouble() / words.size
            tf * (idf[term] ?: 0.0)
        }
    }

    // ─────────────────────────────────────────────────────────────────────
    // Step 3: Cosine Similarity (ML metric)
    // ─────────────────────────────────────────────────────────────────────
    private fun cosineSimilarity(vec1: Map<String, Double>, vec2: Map<String, Double>): Double {
        if (vec1.isEmpty() || vec2.isEmpty()) return 0.0

        val commonKeys = vec1.keys.intersect(vec2.keys)
        val dotProduct = commonKeys.sumOf { (vec1[it] ?: 0.0) * (vec2[it] ?: 0.0) }
        
        val mag1 = sqrt(vec1.values.sumOf { it * it })
        val mag2 = sqrt(vec2.values.sumOf { it * it })

        return if (mag1 > 0 && mag2 > 0) dotProduct / (mag1 * mag2) else 0.0
    }

    // ─────────────────────────────────────────────────────────────────────
    // Step 4: Generate match label
    // ─────────────────────────────────────────────────────────────────────
    private fun getMatchLabel(score: Int): String = when {
        score >= 85 -> "Excellent Match"
        score >= 65 -> "Good Match"
        score >= 45 -> "Fair Match"
        else -> "Low Match"
    }

    // ─────────────────────────────────────────────────────────────────────
    // Step 5: Generate reasons
    // ─────────────────────────────────────────────────────────────────────
    private fun generateMatchReasons(profile: UserProfile, job: Job, score: Int): List<String> {
        val reasons = mutableListOf<String>()

        // Check skill match
        val matchedSkills = profile.skills.map { it.lowercase() }
        val jobReqs = job.requirements.joinToString(" ").lowercase()
        val skillMatches = matchedSkills.filter { jobReqs.contains(it) }
        if (skillMatches.isNotEmpty()) {
            reasons.add("Matches ${skillMatches.size} required skills")
        }

        // Check experience level
        if (profile.yearsOfExperience > 0) {
            reasons.add("${profile.yearsOfExperience} years of experience")
        }

        // Check category preference
        if (profile.preferredCategories.any { it.equals(job.category, ignoreCase = true) }) {
            reasons.add("Matches preferred category")
        }

        // Check level
        if (profile.preferredLevel.isNotEmpty() && profile.preferredLevel.equals(job.level, ignoreCase = true)) {
            reasons.add("Matches preferred level")
        }

        return reasons.ifEmpty { listOf("Based on overall profile fit") }
    }

    private fun findMatchedSkills(userSkills: List<String>, jobRequirements: List<String>): List<String> {
        val reqText = jobRequirements.joinToString(" ").lowercase()
        return userSkills.filter { reqText.contains(it.lowercase()) }
    }

    private fun findMissingSkills(userSkills: List<String>, jobRequirements: List<String>): List<String> {
        val userSkillsLower = userSkills.map { it.lowercase() }
        val reqText = jobRequirements.joinToString(" ").lowercase()

        // Simple keyword extraction from requirements
        val reqKeywords = reqText.split(Regex("[,\\s]+"))
            .filter { it.length > 3 }
            .filter { !it.contains("year") && !it.contains("experience") }
            .map { it.lowercase() }
            .distinct()

        return reqKeywords.filter { req ->
            userSkillsLower.none { userSkill -> req.contains(userSkill) || userSkill.contains(req) }
        }.take(3)
    }
}