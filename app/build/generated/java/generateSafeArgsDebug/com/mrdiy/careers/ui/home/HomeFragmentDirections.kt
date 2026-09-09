package com.mrdiy.careers.ui.home

import android.os.Bundle
import androidx.`annotation`.CheckResult
import androidx.navigation.NavDirections
import com.mrdiy.careers.R
import kotlin.Int
import kotlin.String

public class HomeFragmentDirections private constructor() {
  private data class ActionHomeToJobDetail(
    public val jobId: String,
  ) : NavDirections {
    public override val actionId: Int = R.id.action_home_to_jobDetail

    public override val arguments: Bundle
      get() {
        val result = Bundle()
        result.putString("jobId", this.jobId)
        return result
      }
  }

  public companion object {
    @CheckResult
    public fun actionHomeToJobDetail(jobId: String): NavDirections = ActionHomeToJobDetail(jobId)
  }
}
