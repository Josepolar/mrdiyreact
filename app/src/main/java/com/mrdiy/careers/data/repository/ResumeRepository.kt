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

    suspend fun extractText(bytes: ByteArray, extension: String): String = withContext(Dispatchers.IO) {
        if (extension == "pdf") {
            PDFBoxResourceLoader.init(context)
            PDDocument.load(bytes).use { document ->
                require(!document.isEncrypted)
                require(document.numberOfPages <= 100)
                PDFTextStripper().getText(document).take(200_000)
            }
        } else bytes.toString(Charsets.UTF_8).take(200_000)
    }

    suspend fun parseResume(resumeText: String): UserProfile = withContext(Dispatchers.Default) {
        parseResumeLocally(resumeText)
    }

    private fun parseResumeLocally(text: String): UserProfile {
        val lines = text.lines().map { it.trim() }.filter { it.isNotEmpty() }
        val name = lines.firstOrNull() ?: ""
        val email = lines.find { it.contains("@") }?.extractEmail() ?: ""
        val phone = lines.find { it.contains(Regex("\\d{10,}")) }?.extractPhone() ?: ""

        val skills = com.mrdiy.careers.data.ml.ResumeSkills.extract(text)

        return UserProfile(
            fullName = name,
            firstName = name.split(" ").firstOrNull() ?: "",
            lastName = name.split(" ").drop(1).joinToString(" ").ifEmpty { name },
            email = email,
            phone = phone,
            skills = skills,
            resumeText = text
        )
    }

    private fun String.extractEmail(): String {
        return Regex("[a-zA-Z0-9._%+-]+@[a-zA-Z0-9.-]+\\.[a-zA-Z]{2,}").find(this)?.value ?: ""
    }

    private fun String.extractPhone(): String {
        return Regex("\\d{10,12}").find(this)?.value?.take(11) ?: ""
    }

}
