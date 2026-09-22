package com.mrdiy.careers.ui.settings

import android.content.Context
import android.content.Intent
import android.os.Bundle
import android.provider.Settings
import android.text.InputType
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.fragment.app.Fragment
import androidx.navigation.fragment.findNavController
import com.mrdiy.careers.R
import com.mrdiy.careers.data.auth.AuthManager
import com.mrdiy.careers.databinding.FragmentSettingsBinding

class SettingsFragment : Fragment() {

    private var _binding: FragmentSettingsBinding? = null
    private val binding get() = _binding!!
    private lateinit var authManager: AuthManager

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = FragmentSettingsBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        authManager = AuthManager(requireContext())
        setupHeader()
        setupMenuItems()
    }

    private fun setupHeader() {
        binding.btnBack.setOnClickListener {
            findNavController().navigateUp()
        }
    }

    private fun setupMenuItems() {
        binding.btnChangePassword.setOnClickListener {
            showChangePasswordDialog()
        }

        binding.btnNotificationSettings.setOnClickListener {
            startActivity(Intent(Settings.ACTION_APP_NOTIFICATION_SETTINGS).apply {
                putExtra(Settings.EXTRA_APP_PACKAGE, requireContext().packageName)
            })
        }

        binding.btnPrivacy.setOnClickListener {
            startActivity(Intent(Intent.ACTION_VIEW, android.net.Uri.parse("https://supabase.com/privacy")))
        }

        binding.btnResumeSettings.setOnClickListener {
            findNavController().navigate(R.id.resumeUploadFragment)
        }

        binding.btnHelp.setOnClickListener {
            startActivity(Intent(Intent.ACTION_SENDTO, android.net.Uri.parse("mailto:support@mrdiy.com")))
        }

        binding.btnAbout.setOnClickListener {
            showAboutDialog()
        }

        binding.btnLogout.setOnClickListener {
            showLogoutConfirmationDialog()
        }
    }

    private fun showLogoutConfirmationDialog() {
        com.google.android.material.dialog.MaterialAlertDialogBuilder(requireContext())
            .setTitle("Logout")
            .setMessage("Are you sure you want to logout?")
            .setPositiveButton("Logout") { _, _ ->
                performLogout()
            }
            .setNegativeButton("Cancel", null)
            .show()
    }

    private fun performLogout() {
        authManager.logout()
        authManager.saveLoginState(false)
        requireContext().getSharedPreferences("auth_prefs", Context.MODE_PRIVATE).edit().clear().apply()
        requireContext().getSharedPreferences("current_user", Context.MODE_PRIVATE).edit().clear().apply()
        findNavController().navigate(R.id.action_settings_to_login)
    }

    private fun showAboutDialog() {
        com.google.android.material.dialog.MaterialAlertDialogBuilder(requireContext())
            .setTitle("About MR.D.I.Y. Careers")
            .setMessage("Version 1.0.0\n\nMR.D.I.Y. Careers - Job Search App\n\nFind your dream job at MR.D.I.Y. Philippines")
            .setPositiveButton("OK", null)
            .show()
    }

    private fun showChangePasswordDialog() {
        val input = com.google.android.material.textfield.TextInputEditText(requireContext()).apply {
            hint = "New password"
            inputType = InputType.TYPE_CLASS_TEXT or InputType.TYPE_TEXT_VARIATION_PASSWORD
        }
        val container = android.widget.FrameLayout(requireContext()).apply {
            setPadding(48, 0, 48, 0)
            addView(input)
        }
        com.google.android.material.dialog.MaterialAlertDialogBuilder(requireContext())
            .setTitle("Change password")
            .setView(container)
            .setNegativeButton("Cancel", null)
            .setPositiveButton("Update") { _, _ ->
                val password = input.text?.toString().orEmpty()
                if (password.length < 6) {
                    Toast.makeText(requireContext(), "Password must be at least 6 characters.", Toast.LENGTH_SHORT).show()
                } else {
                    authManager.updatePassword(password) { _, message ->
                        Toast.makeText(requireContext(), message, Toast.LENGTH_SHORT).show()
                    }
                }
            }
            .show()
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}