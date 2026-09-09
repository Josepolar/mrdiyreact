package com.mrdiy.careers.ui.resume

import androidx.`annotation`.CheckResult
import androidx.navigation.ActionOnlyNavDirections
import androidx.navigation.NavDirections
import com.mrdiy.careers.R

public class ResumeUploadFragmentDirections private constructor() {
  public companion object {
    @CheckResult
    public fun actionResumeUploadToRecommendations(): NavDirections = ActionOnlyNavDirections(R.id.action_resumeUpload_to_recommendations)
  }
}
