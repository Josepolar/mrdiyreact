package com.mrdiy.careers.ui.auth

import androidx.`annotation`.CheckResult
import androidx.navigation.ActionOnlyNavDirections
import androidx.navigation.NavDirections
import com.mrdiy.careers.R

public class WelcomeFragmentDirections private constructor() {
  public companion object {
    @CheckResult
    public fun actionWelcomeFragmentToHomeFragment(): NavDirections = ActionOnlyNavDirections(R.id.action_welcomeFragment_to_homeFragment)
  }
}
