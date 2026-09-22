package com.mrdiy.careers

import com.mrdiy.careers.data.auth.*
import com.mrdiy.careers.data.repository.ResumeFiles
import org.junit.Assert.*
import org.junit.Test

class AccountAndResumeTest {
    @Test fun termsRequiredEvenWithOtherwiseValidInput() {
        assertNotNull(AccountRules.registrationError("Real Name", "person@example.com", "secret123", false))
        assertNull(AccountRules.registrationError("Real Name", "person@example.com", "secret123", true))
        assertNotNull(AccountRules.registrationError("Real Name", "x", "secret123", true))
    }
    @Test fun absentAndExpiredSessionsAreNotAuthenticated() {
        assertFalse(AccountRules.sessionUsable(null, 200, 100))
        assertFalse(AccountRules.sessionUsable("id", 99, 100))
        assertTrue(AccountRules.sessionUsable("id", 200, 100))
    }
    @Test fun greetingUsesFirstNameAndHasSafeFallback() {
        assertEquals("Hello, Maria", AccountRules.greeting(" Maria Cruz "))
        assertEquals("Hello", AccountRules.greeting(""))
    }
    @Test fun experienceIncludesFourNineAndTenPlus() {
        assertEquals(0, AccountRules.experience("0"))
        assertEquals(4, AccountRules.experience("4"))
        assertEquals(9, AccountRules.experience("9"))
        assertEquals(10, AccountRules.experience("10+"))
    }
    @Test fun rapidSubmissionsAreSerialized() {
        val gate = RequestGate()
        assertTrue(gate.begin()); assertFalse(gate.begin()); gate.end(); assertTrue(gate.begin())
    }
    @Test fun pdfSignatureAndExtensionMustAgree() {
        assertEquals("pdf", ResumeFiles.extension("resume.pdf", "application/pdf", "%PDF-1.4".toByteArray()))
    }
    @Test(expected = IllegalArgumentException::class) fun rejectsRenamedExecutable() {
        ResumeFiles.extension("resume.pdf", "application/pdf", "MZfake".toByteArray())
    }
    @Test(expected = IllegalArgumentException::class) fun rejectsOversizeStreamWithoutDescriptor() {
        ResumeFiles.readBounded(ByteArray(ResumeFiles.MAX_BYTES + 1).inputStream())
    }
    @Test fun privatePathsRejectOtherUsersAndTraversal() {
        assertTrue(ResumeFiles.ownedPath("alice/resume.pdf", "alice"))
        listOf("bob/resume.pdf", "alice/../bob.pdf", "alice/%2e%2e", "https://example.com").forEach {
            assertFalse(ResumeFiles.ownedPath(it, "alice"))
        }
    }
}
