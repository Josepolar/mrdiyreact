package com.mrdiy.careers.ui.profile

import androidx.`annotation`.CheckResult
import androidx.navigation.ActionOnlyNavDirections
import androidx.navigation.NavDirections
import com.mrdiy.careers.R

public class ProfileFragmentDirections private constructor() {
  public companion object {
    @CheckResult
    public fun actionProfileToEditProfile(): NavDirections = ActionOnlyNavDirections(R.id.action_profile_to_editProfile)

    @CheckResult
    public fun actionProfileToResumeUpload(): NavDirections = ActionOnlyNavDirections(R.id.action_profile_to_resumeUpload)

    @CheckResult
    public fun actionProfileFragmentToLoginFragment(): NavDirections = ActionOnlyNavDirections(R.id.action_profileFragment_to_loginFragment)

    @CheckResult
    public fun actionProfileToSavedJobs(): NavDirections = ActionOnlyNavDirections(R.id.action_profile_to_savedJobs)

    @CheckResult
    public fun actionProfileToSettings(): NavDirections = ActionOnlyNavDirections(R.id.action_profile_to_settings)
  }
}
