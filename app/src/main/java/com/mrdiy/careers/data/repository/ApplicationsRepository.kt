package com.mrdiy.careers.data.repository

import android.content.Context
import android.util.Log
import com.mrdiy.careers.data.auth.AuthManager
import com.mrdiy.careers.data.auth.SupabaseProvider
import com.mrdiy.careers.model.Job
import io.github.jan.supabase.postgrest.postgrest
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import org.json.JSONArray
import java.time.Instant
import java.util.UUID
import java.nio.charset.StandardCharsets

class ApplicationsRepository(context: Context) {

    private val authManager = AuthManager(context)
    private val table = "job_applications"
    private val prefs = context.getSharedPreferences("demo_applications", Context.MODE_PRIVATE)

    data class ApplicationRow(
        val id: String,
        val user_id: String,
        val job_id: String,
        val job_title: String,
        val status: String = "submitted",
        val applied_at: String
    )

    suspend fun getApplications(): Result<List<ApplicationRow>> = withContext(Dispatchers.IO) {
        val userId = authManager.getCurrentUserId()
            ?: return@withContext Result.failure(IllegalStateException("Please sign in first."))
        if (userId == "debug-demo-user") {
            val json = JSONArray(prefs.getString("items", "[]"))
            return@withContext Result.success((0 until json.length()).map { i ->
                val item = json.getJSONObject(i)
                ApplicationRow(item.getString("id"), userId, item.getString("job_id"), item.getString("job_title"), applied_at = item.getString("applied_at"))
            })
        }
        try {
            Result.success(SupabaseProvider.client.postgrest[table].select {
                filter { eq("user_id", userId) }
            }.decodeList())
        } catch (error: Exception) {
            Result.failure(IllegalStateException("Applications could not be loaded."))
        }
    }

    suspend fun apply(job: Job): Result<Unit> = withContext(Dispatchers.IO) {
        val userId = authManager.getCurrentUserId()
            ?: return@withContext Result.failure(IllegalStateException("Please sign in before applying."))
        if (userId == "debug-demo-user") {
            val json = JSONArray(prefs.getString("items", "[]"))
            if ((0 until json.length()).any { json.getJSONObject(it).getString("job_id") == job.id }) {
                return@withContext Result.failure(IllegalStateException("You have already applied for this job."))
            }
            json.put(org.json.JSONObject().apply {
                put("id", UUID.randomUUID().toString())
                put("job_id", job.id)
                put("job_title", job.title)
                put("applied_at", Instant.now().toString())
            })
            prefs.edit().putString("items", json.toString()).apply()
            return@withContext Result.success(Unit)
        }

        try {
            val databaseJobId = UUID.nameUUIDFromBytes(job.id.toByteArray(StandardCharsets.UTF_8)).toString()
            val existing = SupabaseProvider.client.postgrest[table].select {
                filter {
                    eq("user_id", userId)
                    eq("job_id", databaseJobId)
                }
            }.decodeList<ApplicationRow>()

            if (existing.isNotEmpty()) {
                Result.failure(IllegalStateException("You have already applied for this job."))
            } else {
                SupabaseProvider.client.postgrest[table].insert(
                    ApplicationRow(
                        id = UUID.randomUUID().toString(),
                        user_id = userId,
                        job_id = databaseJobId,
                        job_title = job.title,
                        applied_at = Instant.now().toString()
                    )
                )
                Result.success(Unit)
            }
        } catch (error: Exception) {
            val message = error.message.orEmpty()
            Log.e("ApplicationsRepo", "Application submission failed: $message", error)
            val safeMessage = when {
                "relation" in message && "does not exist" in message ->
                    "Run the job_applications SQL migration in Supabase first."
                "row-level security" in message || "violates row-level security" in message ->
                    "Your account is not authorized to submit applications."
                "JWT" in message || "not authenticated" in message ->
                    "Please sign in with a real account before applying."
                else -> "Application could not be submitted. Please try again."
            }
            Result.failure(IllegalStateException(safeMessage))
        }
    }
}
