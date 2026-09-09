package com.mrdiy.careers.data.repository

import android.content.Context
import android.net.Uri
import com.tom_roush.pdfbox.android.PDFBoxResourceLoader
import com.tom_roush.pdfbox.pdmodel.PDDocument
import com.tom_roush.pdfbox.text.PDFTextStripper
import com.mrdiy.careers.model.UserProfile
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import org.json.JSONArray
import org.json.JSONObject
import java.io.BufferedReader
import java.io.InputStreamReader

class ResumeRepository(private val context: Context) {

    private val prefs = context.getSharedPreferences("mrdiy_profile", Context.MODE_PRIVATE)

    suspend fun extractTextFromUri(uri: Uri): String = withContext(Dispatchers.IO) {
        val mimeType = context.contentResolver.getType(uri) ?: ""
        return@withContext when {
            mimeType == "application/pdf" -> extractPdfText(uri)
            mimeType.startsWith("text/") -> extractPlainText(uri)
            else -> extractPlainText(uri)
        }
    }

    private fun extractPdfText(uri: Uri): String {
        PDFBoxResourceLoader.init(context)
        return try {
            context.contentResolver.openInputStream(uri)?.use { inputStream ->
                PDDocument.load(inputStream).use { document ->
                    PDFTextStripper().getText(document)
                }
            } ?: ""
        } catch (e: Exception) {
            try {
                context.contentResolver.openInputStream(uri)?.use { stream ->
                    val reader = BufferedReader(InputStreamReader(stream, Charsets.ISO_8859_1))
                    val raw = reader.readText()
                    Regex("[\\x20-\\x7E]{4,}").findAll(raw)
                        .map { it.value }
                        .filter { it.length > 10 }
                        .joinToString(" ")
                } ?: ""
            } catch (e2: Exception) {
                ""
            }
        }
    }

    private fun extractPlainText(uri: Uri): String {
        return context.contentResolver.openInputStream(uri)?.use {
            it.bufferedReader().readText()
        } ?: ""
    }

    suspend fun parseResume(resumeText: String): UserProfile = withContext(Dispatchers.Default) {
        parseResumeLocally(resumeText)
    }

    private fun parseResumeLocally(text: String): UserProfile {
        val lines = text.lines().map { it.trim() }.filter { it.isNotEmpty() }
        val name = lines.firstOrNull() ?: ""
        val email = lines.find { it.contains("@") }?.extractEmail() ?: ""
        val phone = lines.find { it.contains(Regex("\\d{10,}")) }?.extractPhone() ?: ""

        val lowerText = text.lowercase()
        val skillKeywords = listOf("kotlin", "java", "android", "python", "javascript", "react", "sql", "git", "docker", "aws", "firebase", "machine learning", "data analysis", "html", "css", "node", "flutter")
        val skills = skillKeywords.filter { lowerText.contains(it) }

        return UserProfile(
            fullName = name,
            firstName = name.split(" ").firstOrNull() ?: "",
            lastName = name.split(" ").drop(1).joinToString(" ").ifEmpty { name },
            email = email,
            phone = phone,
            skills = skills
        )
    }

    private fun String.extractEmail(): String {
        return Regex("[a-zA-Z0-9._%+-]+@[a-zA-Z0-9.-]+\\.[a-zA-Z]{2,}").find(this)?.value ?: ""
    }

    private fun String.extractPhone(): String {
        return Regex("\\d{10,12}").find(this)?.value?.take(11) ?: ""
    }

    fun saveProfile(profile: UserProfile) {
        val fullName = if (profile.fullName.isNotEmpty()) profile.fullName
        else "${profile.firstName} ${profile.lastName}"
        prefs.edit().apply {
            putString("first_name", profile.firstName)
            putString("last_name", profile.lastName)
            putString("full_name", fullName)
            putString("email", profile.email)
            putString("phone", profile.phone)
            putString("location", profile.location)
            putString("desired_position", profile.desiredPosition)
            putString("headline", profile.headline)
            putString("about", profile.about)
            putString("photo_path", profile.photoPath)
            putInt("years_exp", profile.yearsOfExperience)
            putString("preferred_level", profile.preferredLevel)
            putString("skills", JSONArray(profile.skills).toString())
            putString("preferred_categories", JSONArray(profile.preferredCategories).toString())
            putString("education", JSONArray(profile.education.map { edu ->
                JSONObject().apply {
                    put("degree", edu.degree); put("school", edu.school); put("year", edu.year)
                }
            }).toString())
            putString("work_exp", JSONArray(profile.workExperiences.map { exp ->
                JSONObject().apply {
                    put("id", exp.id); put("role", exp.role); put("company", exp.company)
                    put("startYear", exp.startYear); put("endYear", exp.endYear); put("isCurrent", exp.isCurrent)
                }
            }).toString())
            apply()
        }
    }

    fun loadProfile(): UserProfile {
        val firstName = prefs.getString("first_name", "") ?: ""
        val lastName = prefs.getString("last_name", "") ?: ""
        return UserProfile(
            firstName = firstName,
            lastName = lastName,
            fullName = prefs.getString("full_name", "$firstName $lastName".trim()) ?: "",
            email = prefs.getString("email", "") ?: "",
            phone = prefs.getString("phone", "") ?: "",
            location = prefs.getString("location", "") ?: "",
            desiredPosition = prefs.getString("desired_position", "") ?: "",
            headline = prefs.getString("headline", "") ?: "",
            about = prefs.getString("about", "") ?: "",
            photoPath = prefs.getString("photo_path", "") ?: "",
            yearsOfExperience = prefs.getInt("years_exp", 0),
            preferredLevel = prefs.getString("preferred_level", "") ?: "",
            skills = parseJsonStringList(prefs.getString("skills", "[]") ?: "[]"),
            preferredCategories = parseJsonStringList(
                prefs.getString("preferred_categories", "[]") ?: "[]"
            ),
            workExperiences = parseWorkExperiences(prefs.getString("work_exp", "[]") ?: "[]"),
            education = parseEducation(prefs.getString("education", "[]") ?: "[]")
        )
    }

    private fun parseWorkExperiences(json: String): List<com.mrdiy.careers.model.WorkExperience> {
        return try {
            val arr = JSONArray(json)
            (0 until arr.length()).map { i ->
                val obj = arr.getJSONObject(i)
                com.mrdiy.careers.model.WorkExperience(
                    id = obj.optString("id", System.currentTimeMillis().toString()),
                    role = obj.optString("role", ""),
                    company = obj.optString("company", ""),
                    startYear = obj.optString("startYear", ""),
                    endYear = obj.optString("endYear", "Present"),
                    isCurrent = obj.optBoolean("isCurrent", true)
                )
            }
        } catch (e: Exception) { emptyList() }
    }

    private fun parseEducation(json: String): List<com.mrdiy.careers.model.Education> {
        return try {
            val arr = JSONArray(json)
            (0 until arr.length()).map { i ->
                val obj = arr.getJSONObject(i)
                com.mrdiy.careers.model.Education(
                    degree = obj.optString("degree", ""),
                    school = obj.optString("school", ""),
                    year = obj.optString("year", "")
                )
            }
        } catch (e: Exception) { emptyList() }
    }

    fun hasResume(): Boolean = prefs.getString("resume_text", "").isNullOrBlank().not()

    private fun parseJsonStringList(json: String): List<String> = try {
        val arr = JSONArray(json)
        (0 until arr.length()).map { arr.getString(it) }
    } catch (e: Exception) { emptyList() }
}