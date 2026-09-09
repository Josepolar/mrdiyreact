package com.mrdiy.careers.data.repository

import android.content.Context
import android.util.Log
import com.mrdiy.careers.data.auth.SupabaseProvider
import com.mrdiy.careers.model.UserProfile
import io.github.jan.supabase.postgrest.from
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import kotlinx.serialization.Serializable
import kotlinx.serialization.SerialName
import kotlinx.serialization.json.Json

// ─────────────────────────────────────────────────────────────────────────────
// WHY THIS CRASH HAPPENED — full explanation
// ─────────────────────────────────────────────────────────────────────────────
//
// The Supabase `profiles` table was created with TEXT columns that default to
// NULL at the DB level.  A brand-new user who has never saved a profile will
// get a row where those columns are NULL, so Postgrest returns:
//
//   { "full_name": null, "location": null, "desired_position": null, ... }
//
// The OLD DTO declared every field as non-nullable String with a Kotlin default:
//
//   @SerialName("full_name") val fullName: String = ""   ← WRONG
//
// kotlinx.serialization DEFAULT VALUE SEMANTICS:
//   • Field absent from JSON  →  use Kotlin default ("")  ✅
//   • Field present, value null  →  crash: cannot assign null to String  ❌
//
// The default `= ""` does NOT protect against an explicit `null` in JSON for a
// non-nullable type. That's what caused:
//
//   "Unexpected JSON token at offset 63: Expected string literal but 'null'
//    literal was found at path: $[0].full_name"
//
// ─────────────────────────────────────────────────────────────────────────────
// THE FIX — two layers, both applied:
//
//  Layer 1 (SupabaseProvider): coerceInputValues = true on the shared Json
//    instance that is passed to the Postgrest plugin.  This makes the parser
//    treat JSON null as "use the Kotlin default" for non-nullable fields.
//    It is a global safety net for every DTO in the project.
//
//  Layer 2 (here): Every DTO field that the DB can legitimately return as null
//    is declared String? and mapped to "" via the Elvis operator in toDomain().
//    This makes the null-contract explicit and survives even if the Json config
//    is accidentally changed later.
//
//  Which approach is "better practice"?
//    Use BOTH. coerceInputValues is a convenient catch-all but it hides the
//    fact that the DB can return nulls. Nullable types make that contract
//    visible in the type system and prevent subtle "silent default" bugs.
// ─────────────────────────────────────────────────────────────────────────────

@Serializable
data class UserProfileRow(
    @SerialName("user_id")           val userId: String           = "",
    @SerialName("full_name")         val fullName: String?         = null,
    val location: String?                                           = null,
    @SerialName("desired_position")  val desiredPosition: String?  = null,
    @SerialName("years_experience")  val yearsExperience: String?  = null,
    val email: String?                                              = null,
    val phone: String?                                              = null,
    val about: String?                                              = null,
    val skills: String?                                             = null,
    val headline: String?                                           = null,
    @SerialName("resume_text")       val resumeText: String?        = null,
    @SerialName("resume_file_name")  val resumeFileName: String?    = null,
    @SerialName("resume_url")         val resumeUrl: String?        = null
)

class ProfileRepository(private val context: Context) {

    private val prefs = context.getSharedPreferences("auth_prefs", Context.MODE_PRIVATE)

    // Reuse the same Json instance that was given to the Postgrest plugin —
    // this guarantees both the client decode path and any manual Json.encode
    // calls use identical settings.
    private val json: Json = SupabaseProvider.supabaseJson

    fun getCurrentUserId(): String = prefs.getString("user_id", "") ?: ""

    // ── Map domain model → Supabase DTO ──────────────────────────────────────
    private fun UserProfile.toRow(uid: String) = UserProfileRow(
        userId            = uid,
        fullName          = if (fullName.isNotBlank()) fullName else "$firstName $lastName".trim(),
        location          = location,
        desiredPosition   = desiredPosition,
        yearsExperience   = yearsOfExperience.toString(),
        email             = email,
        phone             = phone,
        about             = about,
        skills            = skills.joinToString(","),
        headline          = headline,
        resumeText        = null,
        resumeFileName    = resumeName,
        resumeUrl         = resumeUrl
    )

    // ── Map Supabase DTO → domain model ──────────────────────────────────────
    // Every String? field is mapped with ?: "" — nulls are treated as empty.
    private fun UserProfileRow.toDomain(): UserProfile {
        val safeFullName = fullName ?: ""
        val nameParts    = safeFullName.split(" ")
        return UserProfile(
            id                = userId,
            firstName         = nameParts.getOrElse(0) { "" },
            lastName          = nameParts.drop(1).joinToString(" "),
            fullName          = safeFullName,
            email             = email             ?: "",
            phone             = phone             ?: "",
            location          = location          ?: "",
            desiredPosition   = desiredPosition   ?: "",
            headline          = headline          ?: "",
            about             = about             ?: "",
            skills            = (skills ?: "").split(",")
                .map { it.trim() }.filter { it.isNotBlank() },
            yearsOfExperience = yearsExperience?.toIntOrNull() ?: 0,
            resumeUrl         = resumeUrl         ?: "",
            resumeName        = resumeFileName    ?: "",
            resumeUploadedAt  = ""
        )
    }

    // ─────────────────────────────────────────────────────────────────────────
    // SAVE
    // ─────────────────────────────────────────────────────────────────────────
    fun saveProfile(profile: UserProfile, onComplete: ((Boolean) -> Unit)? = null) {
        val uid = profile.id.ifEmpty { getCurrentUserId() }

        if (uid.isEmpty()) {
            Log.e("ProfileRepo", "saveProfile: no user ID — aborting")
            onComplete?.invoke(false)
            return
        }

        saveLocally(uid, profile)

        CoroutineScope(Dispatchers.IO).launch {
            try {
                val client = SupabaseProvider.client
                val row    = profile.toRow(uid)
                client.from("profiles").upsert(row) { onConflict = "user_id" }
                Log.d("ProfileRepo", "Upsert successful uid=$uid")
                withContext(Dispatchers.Main) { onComplete?.invoke(true) }
            } catch (e: Exception) {
                Log.e("ProfileRepo", "saveProfile failed: ${e.message}", e)
                withContext(Dispatchers.Main) { onComplete?.invoke(false) }
            }
        }
    }

    // ─────────────────────────────────────────────────────────────────────────
    // LOAD
    // ─────────────────────────────────────────────────────────────────────────
    fun loadProfile(userId: String = "", onResult: (UserProfile?) -> Unit) {
        val uid = userId.ifEmpty { getCurrentUserId() }

        if (uid.isEmpty()) { onResult(null); return }

        CoroutineScope(Dispatchers.IO).launch {
            try {
                val client  = SupabaseProvider.client
                val profileRow = client.from("profiles")
                    .select { filter { eq("user_id", uid) } }
                    .decodeSingleOrNull<UserProfileRow>()

                if (profileRow != null) {
                    val profile = profileRow.toDomain()
                    if (profile.fullName.isNotBlank()) {
                        saveLocally(uid, profile)
                        withContext(Dispatchers.Main) { onResult(profile) }
                    } else {
                        withContext(Dispatchers.Main) { onResult(loadFromPrefs(uid)) }
                    }
                } else {
                    withContext(Dispatchers.Main) { onResult(loadFromPrefs(uid)) }
                }
            } catch (e: Exception) {
                Log.e("ProfileRepo", "loadProfile failed: ${e.message}", e)
                withContext(Dispatchers.Main) { onResult(loadFromPrefs(uid)) }
            }
        }
    }

    // ─────────────────────────────────────────────────────────────────────────
    // Local cache helpers
    // ─────────────────────────────────────────────────────────────────────────
    private fun saveLocally(uid: String, profile: UserProfile) {
        context.getSharedPreferences("profile_$uid", Context.MODE_PRIVATE).edit().apply {
            putString("user_id",          uid)
            putString("full_name",        profile.fullName.ifBlank { "${profile.firstName} ${profile.lastName}".trim() })
            putString("first_name",       profile.firstName)
            putString("last_name",        profile.lastName)
            putString("email",            profile.email)
            putString("phone",            profile.phone)
            putString("location",         profile.location)
            putString("desired_position", profile.desiredPosition)
            putString("headline",         profile.headline)
            putString("about",            profile.about)
            putString("skills",           profile.skills.joinToString(","))
            putInt   ("years_experience", profile.yearsOfExperience)
            putString("resume_url",        profile.resumeUrl)
            putString("resume_name",       profile.resumeName)
            apply()
        }
    }

    fun loadFromPrefs(userId: String = ""): UserProfile {
        val uid       = userId.ifEmpty { getCurrentUserId() }
        val p         = context.getSharedPreferences("profile_$uid", Context.MODE_PRIVATE)
        val firstName = p.getString("first_name", "") ?: ""
        val lastName  = p.getString("last_name",  "") ?: ""
        val fullName  = p.getString("full_name",  "") ?: "$firstName $lastName".trim()
        val skillsRaw = p.getString("skills",     "") ?: ""
        return UserProfile(
            id                = uid,
            firstName         = firstName,
            lastName          = lastName,
            fullName          = fullName,
            email             = p.getString("email",            "") ?: "",
            phone             = p.getString("phone",            "") ?: "",
            location          = p.getString("location",         "") ?: "",
            desiredPosition   = p.getString("desired_position", "") ?: "",
            headline          = p.getString("headline",         "") ?: "",
            about             = p.getString("about",            "") ?: "",
            skills            = skillsRaw.split(",").map { it.trim() }.filter { it.isNotBlank() },
            yearsOfExperience = p.getInt("years_experience", 0),
            resumeUrl         = p.getString("resume_url",       "") ?: "",
            resumeName        = p.getString("resume_name",      "") ?: "",
            resumeUploadedAt = ""
        )
    }

    fun loadProfileFromPrefs(): UserProfile = loadFromPrefs()

    fun saveResumeFields(
        userId: String,
        resumeName: String,
        resumeUrl: String,
        onComplete: ((Boolean, String?) -> Unit)? = null
    ) {
        if (userId.isEmpty()) {
            onComplete?.invoke(false, "No user ID")
            return
        }
        saveResumeFieldsLocally(userId, resumeName, resumeUrl)
        CoroutineScope(Dispatchers.IO).launch {
            try {
                val client = SupabaseProvider.client
                val existingRow = try {
                    client.from("profiles").select {
                        filter { eq("user_id", userId) }
                    }.decodeSingleOrNull<UserProfileRow>()
                } catch (e: Exception) {
                    null
                }
                if (existingRow != null) {
                    val row = existingRow.copy(
                        resumeUrl = resumeUrl,
                        resumeFileName = resumeName
                    )
                    client.from("profiles").update(row) {
                        filter { eq("user_id", userId) }
                    }
                } else {
                    val row = UserProfileRow(
                        userId = userId,
                        resumeUrl = resumeUrl,
                        resumeFileName = resumeName
                    )
                    client.from("profiles").insert(row)
                }
                withContext(Dispatchers.Main) { onComplete?.invoke(true, null) }
            } catch (e: Exception) {
                Log.e("ProfileRepo", "saveResumeFields failed: ${e.message}", e)
                withContext(Dispatchers.Main) { onComplete?.invoke(false, e.message) }
            }
        }
    }

    private fun saveResumeFieldsLocally(uid: String, resumeName: String, resumeUrl: String) {
        context.getSharedPreferences("profile_$uid", Context.MODE_PRIVATE)
            .edit().apply {
                putString("resume_name", resumeName)
                putString("resume_url", resumeUrl)
                apply()
            }
    }
}