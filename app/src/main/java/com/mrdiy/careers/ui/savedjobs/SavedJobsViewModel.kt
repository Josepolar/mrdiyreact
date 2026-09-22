package com.mrdiy.careers.ui.savedjobs

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.viewModelScope
import com.mrdiy.careers.data.repository.SavedJobsRepository
import com.mrdiy.careers.model.Job
import kotlinx.coroutines.launch

sealed class SavedJobsUiState {
    object Loading : SavedJobsUiState()
    data class Success(val jobs: List<Job>) : SavedJobsUiState()
    data class Error(val message: String) : SavedJobsUiState()
    object Empty : SavedJobsUiState()
}

class SavedJobsViewModel(application: Application) : AndroidViewModel(application) {

    private val repository = SavedJobsRepository(application)

    private val _uiState = MutableLiveData<SavedJobsUiState>()
    val uiState: LiveData<SavedJobsUiState> = _uiState

    private val _savedCount = MutableLiveData<Int>()
    val savedCount: LiveData<Int> = _savedCount

    fun loadSavedJobs() {
        viewModelScope.launch {
            _uiState.value = SavedJobsUiState.Loading
            try {
                val savedJobs = repository.getSavedJobs()
                _savedCount.value = savedJobs.size
                if (savedJobs.isEmpty()) {
                    _uiState.value = SavedJobsUiState.Empty
                } else {
                    _uiState.value = SavedJobsUiState.Success(savedJobs)
                }
            } catch (e: Exception) {
                _uiState.value = SavedJobsUiState.Error("Could not load saved jobs. Please try again.")
            }
        }
    }

    fun unsaveJob(jobId: String) {
        viewModelScope.launch {
            repository.unsaveJob(jobId)
            loadSavedJobs()
        }
    }

    fun refreshCount() {
        _savedCount.value = repository.getSavedCount()
    }
}
