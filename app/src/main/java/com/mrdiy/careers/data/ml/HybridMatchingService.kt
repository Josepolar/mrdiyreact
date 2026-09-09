package com.mrdiy.careers.data.ml

import com.mrdiy.careers.model.Job
import com.mrdiy.careers.model.JobMatchResult
import com.mrdiy.careers.model.UserProfile

/**
 * Service that combines local ML matching with optional AI enhancements.
 */
object HybridMatchingService {

    /**
     * Matches jobs to a user profile.
     * @param profile The user profile to match.
     * @param jobs The list of jobs to compare against.
     * @param useAI If true, would normally use an AI API (e.g., Claude) to enhance results.
     */
    fun matchJobsToProfile(profile: UserProfile, jobs: List<Job>, useAI: Boolean = false): List<JobMatchResult> {
        // For now, we use our local JobMatchingEngine for all matching.
        return JobMatchingEngine.matchJobs(profile, jobs)
    }

    /**
     * Generates a personalized description of why these jobs were recommended.
     */
    fun generateRecommendedDescription(profile: UserProfile): String {
        if (profile.skills.isEmpty()) {
            return "Complete your profile to get more personalized job recommendations."
        }
        
        val topSkills = profile.skills.take(3).joinToString(", ")
        return "Based on your expertise in $topSkills, we've identified these roles that align with your career profile."
    }
}
