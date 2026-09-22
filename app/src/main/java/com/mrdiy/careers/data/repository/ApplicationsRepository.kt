package com.mrdiy.careers.data.repository

import android.content.Context
import com.mrdiy.careers.data.auth.AuthManager
import com.mrdiy.careers.data.auth.SupabaseProvider
import com.mrdiy.careers.model.Job
import io.github.jan.supabase.postgrest.postgrest
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import java.time.Instant
import java.util.UUID

class ApplicationsRepository(context: Context) {

    private val authManager = AuthManager(context)
    private val table = "job_applications"

    data class ApplicationRow(
        val id: String,
        val user_id: String,
        val job_id: String,
        val job_title: String,
        val status: String = "submitted",
        val applied_at: String
    )

    suspend fun apply(job: Job): Result<Unit> = withContext(Dispatchers.IO) {
        val userId = authManager.getCurrentUserId()
            ?: return@withContext Result.failure(IllegalStateException("Please sign in before applying."))
        if (userId == "debug-demo-user") {
            return@withContext Result.failure(IllegalStateException("Sign in with a real account before applying."))
        }

        try {
            val existing = SupabaseProvider.client.postgrest[table].select {
                filter {
                    eq("user_id", userId)
                    eq("job_id", job.id)
                }
            }.decodeList<ApplicationRow>()

            if (existing.isNotEmpty()) {
                Result.failure(IllegalStateException("You have already applied for this job."))
            } else {
                SupabaseProvider.client.postgrest[table].insert(
                    ApplicationRow(
                        id = UUID.randomUUID().toString(),
                        user_id = userId,
                        job_id = job.id,
                        job_title = job.title,
                        applied_at = Instant.now().toString()
                    )
                )
                Result.success(Unit)
            }
        } catch (error: Exception) {
            val message = error.message.orEmpty()
            if ("invalid input syntax for type uuid" in message) {
                Result.failure(IllegalStateException("The applications table needs its job_id column migrated to text."))
            } else {
                Result.failure(IllegalStateException("Application could not be submitted. Please try again."))
            }
        }
    }
}
