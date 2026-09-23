package com.mrdiy.careers.data.repository

import java.io.InputStream

object ResumeFiles {
    const val MAX_BYTES = 10 * 1024 * 1024
    // The deployed Supabase project uses the existing `resumes` bucket.
    // Keep ownership enforced by the first path segment (the authenticated UID).
    const val BUCKET = "resumes"
    fun readBounded(input: InputStream): ByteArray {
        val output = java.io.ByteArrayOutputStream()
        val buffer = ByteArray(8192)
        while (true) {
            val count = input.read(buffer)
            if (count < 0) break
            require(output.size() + count <= MAX_BYTES)
            output.write(buffer, 0, count)
        }
        return output.toByteArray()
    }
    fun extension(name: String, mime: String?, bytes: ByteArray): String {
        require(bytes.isNotEmpty())
        val pdf = bytes.take(5).toByteArray().toString(Charsets.US_ASCII) == "%PDF-"
        if (name.endsWith(".pdf", true) && pdf && mime in listOf(null, "application/pdf", "application/octet-stream")) return "pdf"
        if (name.endsWith(".txt", true) && !pdf && !bytes.contains(0.toByte()) && mime in listOf(null, "text/plain", "application/octet-stream")) return "txt"
        throw IllegalArgumentException("Unsupported file")
    }
    fun ownedPath(path: String, uid: String): Boolean = uid.isNotBlank() &&
        path.startsWith("$uid/") && path.split('/').size == 2 &&
        !path.contains("..") && !path.contains('\\') && !path.contains('%')
}
