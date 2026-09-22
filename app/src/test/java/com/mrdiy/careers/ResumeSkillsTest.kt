package com.mrdiy.careers

import com.mrdiy.careers.data.ml.ResumeSkills
import org.junit.Assert.*
import org.junit.Test

class ResumeSkillsTest {
    @Test fun extractsRetailAndWarehouseSkills() {
        val skills = ResumeSkills.extract("Customer service, cash handling, POS and inventory. Forklift trained.")
        assertTrue(skills.containsAll(listOf("customer service", "cash handling", "POS", "inventory", "forklift")))
    }
    @Test fun doesNotTreatSubstringsAsSkills() {
        val skills = ResumeSkills.extract("JavaScript developer working on digital repositories")
        assertTrue("JavaScript" in skills)
        assertFalse("Java" in skills)
        assertFalse("Git" in skills)
        assertFalse("POS" in skills)
    }
    @Test fun emptyResumeHasNoExtractedSkills() { assertTrue(ResumeSkills.extract("").isEmpty()) }
}
