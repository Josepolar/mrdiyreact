package com.mrdiy.careers.ui.home

import android.app.Application
import android.util.Log
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.viewModelScope
import com.mrdiy.careers.data.repository.JobRepository
import com.mrdiy.careers.data.repository.PhpJobRepository
import com.mrdiy.careers.data.repository.SavedJobsRepository
import com.mrdiy.careers.model.Job
import com.mrdiy.careers.data.search.UniversalJobSearchEngine
import kotlinx.coroutines.Job as KJob
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch

class HomeViewModel(application: Application) : AndroidViewModel(application) {

    private val savedJobsRepository = SavedJobsRepository(application)
    private val phpJobRepository = PhpJobRepository()

    private val fallbackJobs: List<Job> = JobRepository.getJobs()
    private var allJobs: List<Job> = fallbackJobs

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
        Log.d("HomeViewModel", "Init started, setting hardcoded jobs first")
        _filteredJobs.value = allJobs
        Log.d("HomeViewModel", "Hardcoded jobs set: ${allJobs.size} jobs")
        loadSavedJobIds()
        fetchJobsFromApi()
        startAutoRefresh()
    }

    fun fetchJobsFromApi() {
        viewModelScope.launch {
            _isLoading.value = true
            _errorMessage.value = null

            phpJobRepository.fetchOpenJobs()
                .onSuccess { apiJobs ->
                    Log.d("HomeViewModel", "API returned ${apiJobs.size} jobs")
                    allJobs = apiJobs.ifEmpty { fallbackJobs }
                    _isLoading.value = false
                    applyFilters()
                }
                .onFailure { e ->
                    Log.e("HomeViewModel", "API failed: ${e.message}")
                    allJobs = fallbackJobs
                    _isLoading.value = false
                    _errorMessage.value = "Live jobs are unavailable. Showing available jobs instead."
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
                        allJobs = apiJobs.ifEmpty { fallbackJobs }
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

    fun getAppliedCount(): Int = 0

    fun getSavedCount(): Int = _savedCount.value ?: 0

    private fun applyFilters() {
        var filtered = allJobs

        if (!currentSearchQuery.isNullOrEmpty()) {
            filtered = UniversalJobSearchEngine.search(currentSearchQuery!!, filtered).map { it.job }
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