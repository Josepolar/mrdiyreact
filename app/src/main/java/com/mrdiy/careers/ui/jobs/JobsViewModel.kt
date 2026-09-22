package com.mrdiy.careers.ui.jobs

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import com.mrdiy.careers.data.repository.PublishedJobsRepository
import androidx.lifecycle.viewModelScope
import kotlinx.coroutines.launch
import kotlinx.coroutines.delay
import com.mrdiy.careers.model.Job
import com.mrdiy.careers.data.search.UniversalJobSearchEngine

class JobsViewModel : ViewModel() {

    private var allJobs: List<Job> = emptyList()
    val errorMessage = MutableLiveData<String?>()
    private var refreshing = false
    private val repository = PublishedJobsRepository()

    private val _filteredJobs = MutableLiveData<List<Job>>()
    val filteredJobs: LiveData<List<Job>> get() = _filteredJobs

    private var currentSearchQuery: String? = null
    private var currentCategory: String? = null
    private var currentLocation: String? = null
    private var minSalary: Int = 0
    private var maxSalary: Int = 100000

    init {
        viewModelScope.launch {
            while (true) {
                refresh()
                delay(60_000)
            }
        }
    }

    fun reload() { viewModelScope.launch { refresh() } }

    private suspend fun refresh() {
        if (refreshing) return
        refreshing = true
        try {
        repository.fetchOpenJobs().onSuccess {
            allJobs = it
            errorMessage.value = null
            applyFilters()
        }.onFailure {
            allJobs = emptyList()
            applyFilters()
            errorMessage.value = "Unable to load published jobs. Please retry."
        }
        } finally { refreshing = false }
    }

    fun onSearchQuery(query: String?) {
        currentSearchQuery = query
        applyFilters()
    }

    fun onCategoryFilter(category: String?) {
        currentCategory = category
        applyFilters()
    }

    fun onLocationFilter(location: String?) {
        currentLocation = location
        applyFilters()
    }

    fun onSalaryFilter(min: Int, max: Int) {
        minSalary = min
        maxSalary = max
        applyFilters()
    }

    fun clearFilters() {
        currentSearchQuery = null
        currentCategory = null
        currentLocation = null
        minSalary = 0
        maxSalary = 100000
        applyFilters()
    }

    private fun applyFilters() {
        var filtered = allJobs

        // Search Query
        currentSearchQuery?.let { query ->
            if (query.isNotBlank()) {
                filtered = UniversalJobSearchEngine.search(query, filtered).map { it.job }
            }
        }

        // Category
        currentCategory?.let { category ->
            filtered = filtered.filter { it.category.equals(category, ignoreCase = true) }
        }

        // Location
        currentLocation?.let { location ->
            filtered = filtered.filter { it.location.equals(location, ignoreCase = true) }
        }

        // Salary
        filtered = filtered.filter {
            it.getMonthlySalaryMax() >= minSalary && it.getMonthlySalary() <= maxSalary
        }

        _filteredJobs.value = filtered
    }
}
