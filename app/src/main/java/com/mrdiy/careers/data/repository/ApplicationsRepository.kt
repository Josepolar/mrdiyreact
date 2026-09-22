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
import kotlinx.coroutines.CancellationException
import kotlinx.serialization.json.*

class ApplicationsRepository(context: Context) {

    private val authManager = AuthManager(context)
    private val table = "applications"
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
            val rows = SupabaseProvider.client.postgrest[table].select {
                filter { eq("user_id", userId) }
            }.decodeList<JsonObject>()
            val ids = rows.mapNotNull { it["job_id"]?.jsonPrimitive?.contentOrNull }.distinct()
            val titles = if (ids.isEmpty()) emptyMap() else SupabaseProvider.client.postgrest["jobs"].select {
                filter { isIn("id", ids) }
            }.decodeList<PublishedJobRow>().associate { it.id.content to it.title.orEmpty() }
            Result.success(rows.map { row ->
                val jobId = row["job_id"]?.jsonPrimitive?.contentOrNull.orEmpty()
                ApplicationRow(
                    id = row["id"]!!.jsonPrimitive.content,
                    user_id = userId, job_id = jobId,
                    job_title = titles[jobId]?.takeIf { it.isNotBlank() } ?: "Job #$jobId",
                    status = row["status"]?.jsonPrimitive?.contentOrNull ?: "Pending",
                    applied_at = row["created_at"]?.jsonPrimitive?.contentOrNull
                        ?: row["applied_at"]?.jsonPrimitive?.contentOrNull.orEmpty()
                )
            })
        } catch (cancelled: CancellationException) {
            throw cancelled
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
            PublishedJobsRepository().getJobById(job.id).getOrThrow()
            val databaseJobId = job.id
            val existing = SupabaseProvider.client.postgrest[table].select {
                filter {
                    eq("user_id", userId)
                    eq("job_id", databaseJobId)
                }
            }.decodeList<JsonObject>()

            if (existing.isNotEmpty()) {
                Result.failure(IllegalStateException("You have already applied for this job."))
            } else {
                SupabaseProvider.client.postgrest[table].insert(buildJsonObject {
                    put("user_id", userId)
                    put("job_id", job.id.toLongOrNull()?.let { JsonPrimitive(it) } ?: JsonPrimitive(job.id))
                    put("status", "Pending")
                })
                Result.success(Unit)
            }
        } catch (cancelled: CancellationException) {
            throw cancelled
        } catch (error: Exception) {
            val message = error.message.orEmpty()
            Log.e("ApplicationsRepo", "Application submission failed: $message", error)
            val safeMessage = when {
                "relation" in message && "does not exist" in message ->
                    "The admin applications table is not configured."
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
