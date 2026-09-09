package com.mrdiy.careers.ui.auth

import android.os.Bundle
import androidx.`annotation`.CheckResult
import androidx.navigation.ActionOnlyNavDirections
import androidx.navigation.NavDirections
import com.mrdiy.careers.R
import kotlin.Int
import kotlin.String

public class RegisterFragmentDirections private constructor() {
  private data class ActionRegisterFragmentToEmailVerification(
    public val email: String = "",
  ) : NavDirections {
    public override val actionId: Int = R.id.action_registerFragment_to_emailVerification

    public override val arguments: Bundle
      get() {
        val result = Bundle()
        result.putString("email", this.email)
        return result
      }
  }

  public companion object {
    @CheckResult
    public fun actionRegisterFragmentToEmailVerification(email: String = ""): NavDirections = ActionRegisterFragmentToEmailVerification(email)

    @CheckResult
    public fun actionRegisterFragmentToWelcomeFragment(): NavDirections = ActionOnlyNavDirections(R.id.action_registerFragment_to_welcomeFragment)
  }
}
