package com.mrdiy.careers

import com.mrdiy.careers.data.repository.*
import com.mrdiy.careers.data.search.MetroManilaScope
import kotlinx.coroutines.runBlocking
import kotlinx.serialization.json.JsonPrimitive
import org.junit.Assert.*
import org.junit.Test

class GeographicScopeTest {
    @Test fun metroFilterIncludesCityNamesWithoutMetroPrefix() {
        listOf("Makati", "Quezon City", "Taguig", "Parañaque").forEach { assertTrue(MetroManilaScope.isMetro(it)) }
        listOf("Quezon Province", "Cebu", "Davao").forEach { assertFalse(MetroManilaScope.contains(it)) }
    }
    @Test fun publishedJobsAreScopedBeforeEveryConsumer() = runBlocking {
        val jobs = listOf("Makati", "Cavite", "Cebu", "Davao").mapIndexed { index, location ->
            PublishedJobRow(JsonPrimitive(index), location = location, status = "published")
        }
        assertEquals(listOf("Makati", "Cavite"), PublishedJobsRepository { jobs }.fetchOpenJobs().getOrThrow().map { it.location })
        assertEquals(listOf("Makati"), PublishedJobsRepository(allowedLocations = listOf("makati")) { jobs }
            .fetchOpenJobs().getOrThrow().map { it.location })
    }
}
