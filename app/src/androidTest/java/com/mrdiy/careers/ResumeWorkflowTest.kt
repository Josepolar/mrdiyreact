package com.mrdiy.careers

import android.content.Context
import androidx.test.core.app.ApplicationProvider
import androidx.test.ext.junit.runners.AndroidJUnit4
import com.mrdiy.careers.data.repository.ResumeRepository
import com.tom_roush.pdfbox.android.PDFBoxResourceLoader
import com.tom_roush.pdfbox.pdmodel.PDDocument
import com.tom_roush.pdfbox.pdmodel.PDPage
import com.tom_roush.pdfbox.pdmodel.PDPageContentStream
import com.tom_roush.pdfbox.pdmodel.font.PDType1Font
import kotlinx.coroutines.runBlocking
import org.junit.Assert.*
import org.junit.Test
import org.junit.runner.RunWith
import java.io.ByteArrayOutputStream

@RunWith(AndroidJUnit4::class)
class ResumeWorkflowTest {
    @Test fun actualPdfCanBeReadAndSkillsExtracted() = runBlocking {
        val context = ApplicationProvider.getApplicationContext<Context>()
        PDFBoxResourceLoader.init(context)
        val bytes = ByteArrayOutputStream()
        PDDocument().use { document ->
            val page = PDPage(); document.addPage(page)
            PDPageContentStream(document, page).use {
                it.beginText(); it.setFont(PDType1Font.HELVETICA, 12f)
                it.newLineAtOffset(30f, 700f); it.showText("Customer service and inventory management"); it.endText()
            }
            document.save(bytes)
        }
        val repository = ResumeRepository(context)
        val text = repository.extractText(bytes.toByteArray(), "pdf")
        assertTrue(text.contains("Customer service"))
        assertTrue(repository.parseResume(text).skills.isNotEmpty())
    }
}
