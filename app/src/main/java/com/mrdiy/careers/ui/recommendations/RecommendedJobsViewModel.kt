package com.mrdiy.careers.ui.recommendations

import android.app.Application
import android.util.Log
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.viewModelScope
import com.mrdiy.careers.data.repository.PhpJobRepository
import com.mrdiy.careers.data.repository.JobRepository
import com.mrdiy.careers.data.repository.SavedJobsRepository
import com.mrdiy.careers.data.repository.ProfileRepository
import com.mrdiy.careers.data.ml.HybridMatchingService
import com.mrdiy.careers.model.Job
import kotlinx.coroutines.Job as KJob
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch

class RecommendedJobsViewModel(application: Application) : AndroidViewModel(application) {

    private val savedJobsRepository = SavedJobsRepository(application)
    private val phpJobRepository = PhpJobRepository()
    private val profileRepository = ProfileRepository(application)

    private var allJobs: List<Job> = emptyList()
    private val fallbackJobs = JobRepository.getJobs()

    private val _filteredJobs = MutableLiveData<List<Job>>()
    val filteredJobs: LiveData<List<Job>> get() = _filteredJobs

    private val _savedJobIds = MutableLiveData<Set<String>>()
    val savedJobIds: LiveData<Set<String>> get() = _savedJobIds

    private val _savedCount = MutableLiveData<Int>()
    val savedCount: LiveData<Int> get() = _savedCount

    private val _isLoading = MutableLiveData<Boolean>(false)
    val isLoading: LiveData<Boolean> get() = _isLoading

    private val _errorMessage = MutableLiveData<String?>()
    val errorMessage: LiveData<String?> get() = _errorMessage

    private var currentSearchQuery: String? = null
    private var currentJobTypeFilter: String? = null
    private var currentLocationFilter: String? = null

    private var autoRefreshJob: KJob? = null
    private var searchJob: KJob? = null

    init {
        _filteredJobs.value = emptyList()
        loadSavedJobIds()
        fetchJobsFromApi()
        startAutoRefresh()
    }

    fun loadRecommendations() {
        fetchJobsFromApi()
    }

    fun fetchJobsFromApi() {
        viewModelScope.launch {
            _isLoading.value = true
            _errorMessage.value = null

            phpJobRepository.fetchOpenJobs()
                .onSuccess { apiJobs ->
                    Log.d("RecommendedJobsVM", "API returned ${apiJobs.size} jobs")
                    allJobs = rankJobs(apiJobs.ifEmpty { fallbackJobs })
                    _isLoading.value = false
                    applyFilters()
                }
                .onFailure { e ->
                    Log.e("RecommendedJobsVM", "API failed: ${e.message}")
                    _isLoading.value = false
                    allJobs = rankJobs(fallbackJobs)
                    _errorMessage.value = "Live recommendations are unavailable. Showing available jobs instead."
                    applyFilters()
                }
        }
    }

    private fun startAutoRefresh() {
        autoRefreshJob?.cancel()
        autoRefreshJob = viewModelScope.launch {
            while (true) {
                delay(60_000)
                phpJobRepository.fetchOpenJobs()
                    .onSuccess { apiJobs ->
                        allJobs = rankJobs(apiJobs.ifEmpty { fallbackJobs })
                        applyFilters()
                        loadSavedJobIds()
                    }
            }
        }
    }

    fun loadSavedJobIds() {
        viewModelScope.launch {
            val ids = savedJobsRepository.getSavedJobIds()
            _savedJobIds.value = ids
            _savedCount.value = ids.size
        }
    }

    fun onSearchQuery(query: String?) {
        currentSearchQuery = query
        searchJob?.cancel()
        searchJob = viewModelScope.launch {
            delay(250)
            applyFilters()
        }
    }

    fun onJobTypeFilter(jobType: String?) {
        currentJobTypeFilter = jobType
        applyFilters()
    }

    fun onLocationFilter(location: String?) {
        currentLocationFilter = location
        applyFilters()
    }

    fun clearFilters() {
        currentSearchQuery = null
        currentJobTypeFilter = null
        currentLocationFilter = null
        applyFilters()
    }

    fun saveJob(job: Job) {
        viewModelScope.launch {
            savedJobsRepository.saveJob(job)
            loadSavedJobIds()
        }
    }

    fun unsaveJob(job: Job) {
        viewModelScope.launch {
            savedJobsRepository.unsaveJob(job.id)
            loadSavedJobIds()
        }
    }

    fun toggleSaveJob(job: Job) {
        viewModelScope.launch {
            val isSaved = savedJobsRepository.isJobSaved(job.id)
            if (isSaved) unsaveJob(job) else saveJob(job)
        }
    }

    fun getSavedCount(): Int = _savedCount.value ?: 0

    private fun rankJobs(jobs: List<Job>): List<Job> {
        val profile = profileRepository.loadFromPrefs()
        if (profile.skills.isEmpty() && profile.about.isBlank() && profile.desiredPosition.isBlank()) {
            return jobs
        }
        return HybridMatchingService.matchJobsToProfile(profile, jobs)
            .map { it.job }
    }

    private fun applyFilters() {
        var filtered = allJobs

        if (!currentSearchQuery.isNullOrEmpty()) {
            filtered = filtered.filter { job ->
                job.title.contains(currentSearchQuery!!, ignoreCase = true) ||
                job.company.contains(currentSearchQuery!!, ignoreCase = true) ||
                job.branch.contains(currentSearchQuery!!, ignoreCase = true)
            }
        }

        if (!currentJobTypeFilter.isNullOrEmpty()) {
            filtered = filtered.filter { job ->
                job.jobType.equals(currentJobTypeFilter, ignoreCase = true)
            }
        }

        if (!currentLocationFilter.isNullOrEmpty()) {
            filtered = filtered.filter { job ->
                job.location.contains(currentLocationFilter!!, ignoreCase = true)
            }
        }

        _filteredJobs.value = filtered
    }

    override fun onCleared() {
        super.onCleared()
        autoRefreshJob?.cancel()
        searchJob?.cancel()
    }
}