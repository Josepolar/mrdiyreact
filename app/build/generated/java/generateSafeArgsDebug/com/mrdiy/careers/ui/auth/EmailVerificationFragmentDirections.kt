package com.mrdiy.careers.ui.auth

import androidx.`annotation`.CheckResult
import androidx.navigation.ActionOnlyNavDirections
import androidx.navigation.NavDirections
import com.mrdiy.careers.R

public class EmailVerificationFragmentDirections private constructor() {
  public companion object {
    @CheckResult
    public fun actionEmailVerificationFragmentToLoginFragment(): NavDirections = ActionOnlyNavDirections(R.id.action_emailVerificationFragment_to_loginFragment)
  }
}
