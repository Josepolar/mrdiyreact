package com.mrdiy.careers.ui.auth

import androidx.`annotation`.CheckResult
import androidx.navigation.ActionOnlyNavDirections
import androidx.navigation.NavDirections
import com.mrdiy.careers.R

public class LoginFragmentDirections private constructor() {
  public companion object {
    @CheckResult
    public fun actionLoginFragmentToRegisterFragment(): NavDirections = ActionOnlyNavDirections(R.id.action_loginFragment_to_registerFragment)

    @CheckResult
    public fun actionLoginFragmentToPhoneLoginFragment(): NavDirections = ActionOnlyNavDirections(R.id.action_loginFragment_to_phoneLoginFragment)

    @CheckResult
    public fun actionLoginFragmentToHomeFragment(): NavDirections = ActionOnlyNavDirections(R.id.action_loginFragment_to_homeFragment)

    @CheckResult
    public fun actionLoginFragmentToWelcomeFragment(): NavDirections = ActionOnlyNavDirections(R.id.action_loginFragment_to_welcomeFragment)
  }
}
