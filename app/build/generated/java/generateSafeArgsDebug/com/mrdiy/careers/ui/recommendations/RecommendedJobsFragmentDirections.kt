package com.mrdiy.careers.ui.recommendations

import android.os.Bundle
import androidx.`annotation`.CheckResult
import androidx.navigation.ActionOnlyNavDirections
import androidx.navigation.NavDirections
import com.mrdiy.careers.R
import kotlin.Int
import kotlin.String

public class RecommendedJobsFragmentDirections private constructor() {
  private data class ActionRecommendationsToJobDetail(
    public val jobId: String,
  ) : NavDirections {
    public override val actionId: Int = R.id.action_recommendations_to_jobDetail

    public override val arguments: Bundle
      get() {
        val result = Bundle()
        result.putString("jobId", this.jobId)
        return result
      }
  }

  public companion object {
    @CheckResult
    public fun actionRecommendationsToResumeUpload(): NavDirections = ActionOnlyNavDirections(R.id.action_recommendations_to_resumeUpload)

    @CheckResult
    public fun actionRecommendationsToJobDetail(jobId: String): NavDirections = ActionRecommendationsToJobDetail(jobId)
  }
}
