package com.mrdiy.careers.ui.profile

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import com.mrdiy.careers.data.repository.ProfileRepository
import com.mrdiy.careers.model.UserProfile

class ProfileViewModel(application: Application) : AndroidViewModel(application) {

    private val repository = ProfileRepository(application)

    private val _profile = MutableLiveData<UserProfile>()
    val profile: LiveData<UserProfile> get() = _profile

    private val _isSaving = MutableLiveData(false)
    val isSaving: LiveData<Boolean> get() = _isSaving

    private val _profileCompletion = MutableLiveData(0)
    val profileCompletion: LiveData<Int> get() = _profileCompletion

    init { loadProfileFromDb() }

    fun loadProfileFromDb() {
        val userId = repository.getCurrentUserId()
        if (_profile.value?.id != userId) _profile.value = UserProfile(id = userId)
        repository.loadProfile(userId) { loaded ->
            if (userId != repository.getCurrentUserId()) return@loadProfile
            val profile = loaded ?: repository.loadProfileFromPrefs()
            _profile.postValue(profile)
            calculateProfileCompletion(profile)
        }
    }

    private fun calculateProfileCompletion(profile: UserProfile) {
        var completed = 0
        val total = 10

        if (profile.fullName.isNotBlank()) completed++
        if (profile.email.isNotBlank()) completed++
        if (profile.phone.isNotBlank()) completed++
        if (profile.location.isNotBlank()) completed++
        if (profile.desiredPosition.isNotBlank()) completed++
        if (profile.about.isNotBlank()) completed++
        if (profile.skills.isNotEmpty()) completed++
        if (profile.workExperiences.isNotEmpty()) completed++
        if (profile.education.isNotEmpty()) completed++
        if (profile.resumeUrl.isNotBlank()) completed++

        _profileCompletion.value = (completed * 100) / total
    }

    fun currentProfile(): UserProfile = _profile.value?.takeIf { it.id == repository.getCurrentUserId() } ?: UserProfile()

    fun saveProfile(profile: UserProfile, onComplete: ((Boolean) -> Unit)? = null) {
        _isSaving.value = true
        repository.saveProfile(profile) { success ->
            if (profile.id.isNotBlank() && profile.id != repository.getCurrentUserId()) return@saveProfile
            _isSaving.value = false
            if (success) {
                _profile.value = profile
                calculateProfileCompletion(profile)
                loadProfileFromDb()
            }
            onComplete?.invoke(success)
        }
    }

    fun refreshProfile() = loadProfileFromDb()
    fun clearProfile() { _profile.value = UserProfile(); _profileCompletion.value = 0 }
}
