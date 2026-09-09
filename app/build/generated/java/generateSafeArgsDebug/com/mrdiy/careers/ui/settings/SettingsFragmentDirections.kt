package com.mrdiy.careers.ui.settings

import androidx.`annotation`.CheckResult
import androidx.navigation.ActionOnlyNavDirections
import androidx.navigation.NavDirections
import com.mrdiy.careers.R

public class SettingsFragmentDirections private constructor() {
  public companion object {
    @CheckResult
    public fun actionSettingsToLogin(): NavDirections = ActionOnlyNavDirections(R.id.action_settings_to_login)
  }
}
