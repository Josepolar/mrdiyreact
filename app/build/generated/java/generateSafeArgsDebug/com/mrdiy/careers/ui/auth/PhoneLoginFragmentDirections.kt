package com.mrdiy.careers.ui.auth

import androidx.`annotation`.CheckResult
import androidx.navigation.ActionOnlyNavDirections
import androidx.navigation.NavDirections
import com.mrdiy.careers.R

public class PhoneLoginFragmentDirections private constructor() {
  public companion object {
    @CheckResult
    public fun actionPhoneLoginFragmentToHomeFragment(): NavDirections = ActionOnlyNavDirections(R.id.action_phoneLoginFragment_to_homeFragment)
  }
}
