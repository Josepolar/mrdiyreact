package com.mrdiy.careers.ui.jobs

import android.os.Bundle
import androidx.lifecycle.SavedStateHandle
import androidx.navigation.NavArgs
import java.lang.IllegalArgumentException
import kotlin.String
import kotlin.jvm.JvmStatic

public data class JobDetailFragmentArgs(
  public val jobId: String,
) : NavArgs {
  public fun toBundle(): Bundle {
    val result = Bundle()
    result.putString("jobId", this.jobId)
    return result
  }

  public fun toSavedStateHandle(): SavedStateHandle {
    val result = SavedStateHandle()
    result.set("jobId", this.jobId)
    return result
  }

  public companion object {
    @JvmStatic
    public fun fromBundle(bundle: Bundle): JobDetailFragmentArgs {
      bundle.setClassLoader(JobDetailFragmentArgs::class.java.classLoader)
      val __jobId : String?
      if (bundle.containsKey("jobId")) {
        __jobId = bundle.getString("jobId")
        if (__jobId == null) {
          throw IllegalArgumentException("Argument \"jobId\" is marked as non-null but was passed a null value.")
        }
      } else {
        throw IllegalArgumentException("Required argument \"jobId\" is missing and does not have an android:defaultValue")
      }
      return JobDetailFragmentArgs(__jobId)
    }

    @JvmStatic
    public fun fromSavedStateHandle(savedStateHandle: SavedStateHandle): JobDetailFragmentArgs {
      val __jobId : String?
      if (savedStateHandle.contains("jobId")) {
        __jobId = savedStateHandle["jobId"]
        if (__jobId == null) {
          throw IllegalArgumentException("Argument \"jobId\" is marked as non-null but was passed a null value")
        }
      } else {
        throw IllegalArgumentException("Required argument \"jobId\" is missing and does not have an android:defaultValue")
      }
      return JobDetailFragmentArgs(__jobId)
    }
  }
}
