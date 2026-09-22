package com.mrdiy.careers.data.repository

import android.content.Context
import android.content.SharedPreferences
import com.mrdiy.careers.data.auth.AuthManager
import com.mrdiy.careers.data.auth.SupabaseProvider
import com.mrdiy.careers.model.Job
import com.mrdiy.careers.model.SavedJob
import io.github.jan.supabase.postgrest.postgrest
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import kotlinx.coroutines.sync.Mutex
import kotlinx.coroutines.sync.withLock
import org.json.JSONArray
import java.time.Instant

class SavedJobsRepository(context: Context) {
    companion object { private val saveLock = Mutex() }

    private val prefs: SharedPreferences = context.getSharedPreferences("saved_jobs", Context.MODE_PRIVATE)
    private val authManager = AuthManager(context)

    private val savedJobsTable = "saved_jobs"

    @kotlinx.serialization.Serializable
    data class SavedJobRow(
        val id: String,
        val user_id: String,
        val job_id: String,
        val saved_at: String
    )

    suspend fun saveJob(job: Job): Boolean = saveLock.withLock { saveJobOnce(job) }
    private suspend fun saveJobOnce(job: Job): Boolean = withContext(Dispatchers.IO) {
        try {
            val userId = authManager.getCurrentUserId() ?: return@withContext false
            val existing = SupabaseProvider.client.postgrest[savedJobsTable].select {
                filter { eq("user_id", userId); eq("job_id", job.id) }
            }.decodeList<SavedJobRow>()
            if (existing.isNotEmpty()) return@withContext true
            val savedAt = kotlinx.datetime.Clock.System.now().toString()

            val savedJob = SavedJobRow(
                id = java.util.UUID.randomUUID().toString(),
                user_id = userId,
                job_id = job.id,
                saved_at = savedAt
            )

            SupabaseProvider.client.postgrest[savedJobsTable].insert(savedJob)

            if (userId == authManager.getCurrentUserId()) addToLocalCache(SavedJob(savedJob.id, userId, job.id, savedAt))

            true
        } catch (e: Exception) {
            com.mrdiy.careers.data.SafeDiagnostics.record("request", e)
            false
        }
    }

    suspend fun unsaveJob(jobId: String): Boolean = withContext(Dispatchers.IO) {
        try {
            val userId = authManager.getCurrentUserId() ?: return@withContext false

            SupabaseProvider.client.postgrest[savedJobsTable]
                .delete {
                    filter {
                        eq("user_id", userId)
                        eq("job_id", jobId)
                    }
                }

            if (userId == authManager.getCurrentUserId()) removeFromLocalCache(jobId)

            true
        } catch (e: Exception) {
            com.mrdiy.careers.data.SafeDiagnostics.record("request", e)
            false
        }
    }

    suspend fun isJobSaved(jobId: String): Boolean = withContext(Dispatchers.IO) {
        val localSaved = getLocallySavedJobIds()
        if (localSaved.contains(jobId)) return@withContext true

        try {
            val userId = authManager.getCurrentUserId() ?: return@withContext false
            val result = SupabaseProvider.client.postgrest[savedJobsTable]
                .select {
                    filter {
                        eq("user_id", userId)
                        eq("job_id", jobId)
                    }
                }

            val count = result.decodeList<SavedJobRow>().size
            count > 0
        } catch (e: Exception) {
            com.mrdiy.careers.data.SafeDiagnostics.record("request", e)
            false
        }
    }

    suspend fun getSavedJobs(): List<Job> = withContext(Dispatchers.IO) {
        try {
            val userId = authManager.getCurrentUserId() ?: return@withContext emptyList()

            val result = SupabaseProvider.client.postgrest[savedJobsTable]
                .select { filter { eq("user_id", userId) } }

            val savedRows = result.decodeList<SavedJobRow>()
            if (userId != authManager.getCurrentUserId()) return@withContext emptyList()
            syncLocalCache(savedRows)

            val allJobs = PublishedJobsRepository().fetchOpenJobs().getOrThrow()
            val savedJobIds = savedRows.map { it.job_id }.toSet()

            allJobs.filter { savedJobIds.contains(it.id) }
        } catch (e: Exception) {
            com.mrdiy.careers.data.SafeDiagnostics.record("request", e)
            throw e
        }
    }

    suspend fun getSavedJobIds(): Set<String> = withContext(Dispatchers.IO) {
        try {
            val userId = authManager.getCurrentUserId() ?: return@withContext emptySet()

            val result = SupabaseProvider.client.postgrest[savedJobsTable]
                .select { filter { eq("user_id", userId) } }

            val savedRows = result.decodeList<SavedJobRow>()
            savedRows.map { it.job_id }.toSet()
        } catch (e: Exception) {
            com.mrdiy.careers.data.SafeDiagnostics.record("request", e)
            getLocallySavedJobIds()
        }
    }

    fun getSavedCount(): Int {
        return try {
            val json = prefs.getString("saved_jobs_list_${authManager.getCurrentUserId()}", "[]") ?: "[]"
            JSONArray(json).length()
        } catch (e: Exception) {
            0
        }
    }

    private fun getLocallySavedJobIds(): Set<String> {
        return try {
            val json = prefs.getString("saved_jobs_list_${authManager.getCurrentUserId()}", "[]") ?: "[]"
            val arr = JSONArray(json)
            (0 until arr.length()).map { arr.getJSONObject(it).getString("job_id") }.toSet()
        } catch (e: Exception) {
            emptySet()
        }
    }

    private fun addToLocalCache(savedJob: SavedJob) {
        try {
            val json = prefs.getString("saved_jobs_list_${authManager.getCurrentUserId()}", "[]") ?: "[]"
            val arr = JSONArray(json)

            val newObj = org.json.JSONObject().apply {
                put("id", savedJob.id)
                put("user_id", savedJob.userId)
                put("job_id", savedJob.jobId)
                put("saved_at", savedJob.savedAt)
            }

            arr.put(newObj)
            prefs.edit().putString("saved_jobs_list_${authManager.getCurrentUserId()}", arr.toString()).apply()
        } catch (e: Exception) {
            com.mrdiy.careers.data.SafeDiagnostics.record("request", e)
        }
    }

    private fun removeFromLocalCache(jobId: String) {
        try {
            val json = prefs.getString("saved_jobs_list_${authManager.getCurrentUserId()}", "[]") ?: "[]"
            val arr = JSONArray(json)
            val newArr = JSONArray()

            for (i in 0 until arr.length()) {
                val obj = arr.getJSONObject(i)
                if (obj.getString("job_id") != jobId) {
                    newArr.put(obj)
                }
            }

            prefs.edit().putString("saved_jobs_list_${authManager.getCurrentUserId()}", newArr.toString()).apply()
        } catch (e: Exception) {
            com.mrdiy.careers.data.SafeDiagnostics.record("request", e)
        }
    }

    private fun syncLocalCache(savedRows: List<SavedJobRow>) {
        try {
            val arr = JSONArray()
            savedRows.forEach { row ->
                arr.put(org.json.JSONObject().apply {
                    put("id", row.id)
                    put("user_id", row.user_id)
                    put("job_id", row.job_id)
                    put("saved_at", row.saved_at)
                })
            }
            prefs.edit().putString("saved_jobs_list_${authManager.getCurrentUserId()}", arr.toString()).apply()
        } catch (e: Exception) {
            com.mrdiy.careers.data.SafeDiagnostics.record("request", e)
        }
    }

    private suspend fun getSavedJobsFromCache(): List<Job> {
        val jobIds = getLocallySavedJobIds()
        return PublishedJobsRepository().fetchOpenJobs().getOrDefault(emptyList()).filter { it.id in jobIds }
    }
}
