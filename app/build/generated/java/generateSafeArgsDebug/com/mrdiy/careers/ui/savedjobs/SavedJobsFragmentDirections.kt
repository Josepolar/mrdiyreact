package com.mrdiy.careers.ui.savedjobs

import android.os.Bundle
import androidx.`annotation`.CheckResult
import androidx.navigation.NavDirections
import com.mrdiy.careers.R
import kotlin.Int
import kotlin.String

public class SavedJobsFragmentDirections private constructor() {
  private data class ActionSavedJobsToJobDetail(
    public val jobId: String,
  ) : NavDirections {
    public override val actionId: Int = R.id.action_savedJobs_to_jobDetail

    public override val arguments: Bundle
      get() {
        val result = Bundle()
        result.putString("jobId", this.jobId)
        return result
      }
  }

  public companion object {
    @CheckResult
    public fun actionSavedJobsToJobDetail(jobId: String): NavDirections = ActionSavedJobsToJobDetail(jobId)
  }
}
