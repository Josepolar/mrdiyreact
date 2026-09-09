package com.mrdiy.careers.ui.auth

import android.os.Bundle
import androidx.lifecycle.SavedStateHandle
import androidx.navigation.NavArgs
import java.lang.IllegalArgumentException
import kotlin.String
import kotlin.jvm.JvmStatic

public data class EmailVerificationFragmentArgs(
  public val email: String = "",
) : NavArgs {
  public fun toBundle(): Bundle {
    val result = Bundle()
    result.putString("email", this.email)
    return result
  }

  public fun toSavedStateHandle(): SavedStateHandle {
    val result = SavedStateHandle()
    result.set("email", this.email)
    return result
  }

  public companion object {
    @JvmStatic
    public fun fromBundle(bundle: Bundle): EmailVerificationFragmentArgs {
      bundle.setClassLoader(EmailVerificationFragmentArgs::class.java.classLoader)
      val __email : String?
      if (bundle.containsKey("email")) {
        __email = bundle.getString("email")
        if (__email == null) {
          throw IllegalArgumentException("Argument \"email\" is marked as non-null but was passed a null value.")
        }
      } else {
        __email = ""
      }
      return EmailVerificationFragmentArgs(__email)
    }

    @JvmStatic
    public fun fromSavedStateHandle(savedStateHandle: SavedStateHandle): EmailVerificationFragmentArgs {
      val __email : String?
      if (savedStateHandle.contains("email")) {
        __email = savedStateHandle["email"]
        if (__email == null) {
          throw IllegalArgumentException("Argument \"email\" is marked as non-null but was passed a null value")
        }
      } else {
        __email = ""
      }
      return EmailVerificationFragmentArgs(__email)
    }
  }
}
