package com.mrdiy.careers.ui.resume

import android.app.Application
import android.net.Uri
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.viewModelScope
import com.mrdiy.careers.data.auth.SupabaseProvider
import com.mrdiy.careers.data.repository.ProfileRepository
import com.mrdiy.careers.data.repository.ResumeRepository
import com.mrdiy.careers.model.ResumeParseResult
import com.mrdiy.careers.model.UserProfile
import io.github.jan.supabase.storage.storage
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import kotlinx.coroutines.suspendCancellableCoroutine
import kotlin.coroutines.resume
import java.time.Instant

sealed class ResumeUploadState {
    object Idle : ResumeUploadState()
    data class FileSelected(val fileName: String) : ResumeUploadState()
    object ExtractingText : ResumeUploadState()
    object UploadingFile : ResumeUploadState()
    object ParsingWithAI : ResumeUploadState()
    data class ParseSuccess(val profile: UserProfile, val result: ResumeParseResult) : ResumeUploadState()
    data class Error(val message: String) : ResumeUploadState()
}

class ResumeUploadViewModel(application: Application) : AndroidViewModel(application) {

    private val resumeRepo = ResumeRepository(application)
    private val profileRepo = ProfileRepository(application)
    private val context = application.applicationContext

    private val _state = MutableLiveData<ResumeUploadState>(ResumeUploadState.Idle)
    val state: LiveData<ResumeUploadState> = _state

    fun processResume(uri: Uri, fileName: String) {
        viewModelScope.launch {
            _state.value = ResumeUploadState.FileSelected(fileName)
            val size = try { context.contentResolver.openAssetFileDescriptor(uri, "r")?.use { it.length } ?: -1L }
                catch (_: Exception) {
                    _state.value = ResumeUploadState.Error("Could not open the selected file. Please choose it again.")
                    return@launch
                }
            if (size > 10 * 1024 * 1024) {
                _state.value = ResumeUploadState.Error("Please choose a resume smaller than 10 MB.")
                return@launch
            }
            _state.value = ResumeUploadState.ExtractingText

            val rawText = try {
                resumeRepo.extractTextFromUri(uri)
            } catch (e: Exception) {
                _state.value = ResumeUploadState.Error("Could not read this resume. Please choose a text-based PDF or .txt file.")
                return@launch
            }

            if (rawText.isBlank()) {
                _state.value = ResumeUploadState.Error(
                    "Could not extract text from this file. " +
                    "Please upload a text-based PDF or .txt file."
                )
                return@launch
            }

            _state.value = ResumeUploadState.ParsingWithAI

            val parsedProfile = resumeRepo.parseResume(rawText)

            val parseResult = ResumeParseResult(
                success = true,
                skills = parsedProfile.skills,
                education = parsedProfile.education,
                workExperiences = parsedProfile.workExperiences,
                yearsOfExperience = parsedProfile.yearsOfExperience,
                summary = "We've successfully extracted skills and experience from your resume."
            )

            val userId = profileRepo.getCurrentUserId()
            if (userId.isEmpty()) {
                _state.value = ResumeUploadState.Error("You must be logged in to upload a resume.")
                return@launch
            }

            _state.value = ResumeUploadState.UploadingFile

            // Step 1: Upload PDF to Supabase Storage — this is the ONLY place
            //         we need the storage RLS policy to allow authenticated inserts
            val resumeUrl = try {
                uploadResumeToStorage(uri, fileName, userId)
            } catch (e: Exception) {
                _state.value = ResumeUploadState.Error("Upload failed, please try again.")
                return@launch
            }

            // Step 2: Save ONLY resume fields (resume_url, resume_file_name)
            //         to the profiles table via a PARTIAL update.
            //         Personal info (full_name, email, phone, etc.) is NEVER touched.
            val (saveSuccess, saveError) = suspendCancellableCoroutine<Pair<Boolean, String?>> { cont ->
                profileRepo.saveResumeFields(userId, fileName, resumeUrl, parsedProfile, rawText) { success, error ->
                    if (cont.isActive) cont.resume(Pair(success, error))
                }
            }

            if (!saveSuccess) {
                _state.value = ResumeUploadState.Error("Upload failed, please try again.")
                return@launch
            }

            // Build a lightweight display object (does NOT go to database)
            val displayProfile = UserProfile(
                id = userId,
                resumeUrl = resumeUrl,
                resumeName = fileName,
                resumeUploadedAt = Instant.now().toString(),
                skills = parsedProfile.skills
            )

            _state.value = ResumeUploadState.ParseSuccess(displayProfile, parseResult)
        }
    }

    private suspend fun uploadResumeToStorage(uri: Uri, fileName: String, userId: String): String {
        return withContext(Dispatchers.IO) {
            val client = SupabaseProvider.client
            val bucketName = "resumes"
            val extension = if (context.contentResolver.getType(uri) == "application/pdf") "pdf" else "txt"
            val filePath = "$userId/resume-${java.util.UUID.randomUUID()}.$extension"

            val inputStream = context.contentResolver.openInputStream(uri)
                ?: throw Exception("Could not open file")

            val bytes = inputStream.use { it.readBytes() }
            require(bytes.size <= 10 * 1024 * 1024) { "Resume must be smaller than 10 MB." }

            client.storage.from(bucketName).upload(filePath, bytes) {
                upsert = true
            }

            client.storage.from(bucketName).publicUrl(filePath)
        }
    }
}
