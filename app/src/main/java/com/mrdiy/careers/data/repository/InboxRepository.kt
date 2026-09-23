package com.mrdiy.careers.data.repository

import android.content.Context
import com.mrdiy.careers.data.auth.AuthManager
import com.mrdiy.careers.data.auth.SupabaseProvider
import com.mrdiy.careers.model.Alert
import com.mrdiy.careers.model.AlertType
import com.mrdiy.careers.model.Message
import io.github.jan.supabase.postgrest.postgrest
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext

class InboxRepository(private val context: Context) {
    private val userId = AuthManager(context).getCurrentUserId()

    @kotlinx.serialization.Serializable
    data class AlertRow(val id: String = "", val user_id: String = "", val type: String = "JOB_MATCH", val title: String = "", val message: String = "", val time_ago: String = "", val is_read: Boolean = false, val job_id: String? = null)
    @kotlinx.serialization.Serializable
    data class MessageRow(val id: String = "", val user_id: String = "", val sender_name: String = "", val sender_avatar: String = "", val last_message: String = "", val time_ago: String = "", val is_read: Boolean = false, val unread_count: Int = 0, val job_id: String? = null)

    suspend fun alerts(): Result<List<Alert>> = withContext(Dispatchers.IO) { runCatching {
        if (userId.isNullOrBlank()) return@runCatching emptyList()
        SupabaseProvider.client.postgrest["alerts"].select { filter { eq("user_id", userId!!) } }
            .decodeList<AlertRow>().map { Alert(it.id, runCatching { AlertType.valueOf(it.type) }.getOrDefault(AlertType.JOB_MATCH), it.title, it.message, it.time_ago, it.is_read, it.job_id) }
    }.recoverCatching { error ->
        if (!error.message.orEmpty().contains("PGRST205") && !error.message.orEmpty().contains("Could not find the table")) throw error
        ApplicationsRepository(context).getApplications().getOrThrow()
            .filter { !it.status.equals("Pending", true) && !it.status.equals("submitted", true) }
            .map { Alert("application-${it.id}-${it.status}", AlertType.APPLICATION_UPDATE,
                "Application ${it.status}", it.job_title, it.applied_at, jobId = it.job_id) }
    } }

    suspend fun messages(): Result<List<Message>> = withContext(Dispatchers.IO) { runCatching {
        if (userId.isNullOrBlank()) return@runCatching emptyList()
        SupabaseProvider.client.postgrest["messages"].select { filter { eq("user_id", userId!!) } }
            .decodeList<MessageRow>().map { Message(it.id, it.sender_name, it.sender_avatar, it.last_message, it.time_ago, it.is_read, it.unread_count, it.job_id) }
    }.recoverCatching { error ->
        // Messages are optional in older Supabase projects. An absent table or
        // pre-migration schema means there are simply no messages to display;
        // it should not make the Messages screen unusable.
        val message = error.message.orEmpty()
        if (message.contains("PGRST205") || message.contains("Could not find the table")) {
            emptyList()
        } else {
            throw error
        }
    } }
}
