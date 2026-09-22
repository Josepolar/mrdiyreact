package com.mrdiy.careers

import com.mrdiy.careers.data.search.UniversalJobSearchEngine
import com.mrdiy.careers.model.Job
import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Test

class UniversalJobSearchEngineTest {
    private val jobs = listOf(
        job("1", "Retail Cashier", "Retail"),
        job("2", "Office Cleaner", "Facilities"),
        job("3", "Warehouse Helper", "Logistics"),
        job("4", "Company Driver", "Logistics"),
        job("5", "Accountant", "Corporate"),
        job("6", "Software Developer", "IT"),
        job("7", "Store Manager", "Retail")
    )

    @Test fun cashierFindsRelatedRetailRole() {
        assertEquals("Retail Cashier", UniversalJobSearchEngine.search("cashier", jobs).first().job.title)
    }

    @Test fun typoIsTolerated() {
        assertEquals("Retail Cashier", UniversalJobSearchEngine.search("casher", jobs).first().job.title)
    }

    @Test fun jobFamiliesCoverNonTechnicalRoles() {
        assertEquals("Office Cleaner", UniversalJobSearchEngine.search("janitor", jobs).first().job.title)
        assertEquals("Warehouse Helper", UniversalJobSearchEngine.search("warehouse helper", jobs).first().job.title)
        assertEquals("Company Driver", UniversalJobSearchEngine.search("driver", jobs).first().job.title)
    }

    @Test fun professionalAndManagementRolesRemainSearchable() {
        assertTrue(UniversalJobSearchEngine.search("accountant", jobs).any { it.job.title == "Accountant" })
        assertTrue(UniversalJobSearchEngine.search("software developer", jobs).any { it.job.title == "Software Developer" })
        assertTrue(UniversalJobSearchEngine.search("store manager", jobs).any { it.job.title == "Store Manager" })
    }

    private fun job(id: String, title: String, category: String) = Job(
        id, title, "MR.D.I.Y.", "Main Branch", "Antipolo", "Full-time", category, "Entry Level",
        15000, 25000, "monthly", "1d ago", "Role description", emptyList(), emptyList(), emptyList()
    )
}
