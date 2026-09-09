package com.mrdiy.careers.ui.settings

import android.content.Context
import android.os.Bundle
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
            Toast.makeText(requireContext(), "Change Password feature coming soon!", Toast.LENGTH_SHORT).show()
        }

        binding.btnNotificationSettings.setOnClickListener {
            Toast.makeText(requireContext(), "Notification Settings coming soon!", Toast.LENGTH_SHORT).show()
        }

        binding.btnPrivacy.setOnClickListener {
            Toast.makeText(requireContext(), "Privacy Settings coming soon!", Toast.LENGTH_SHORT).show()
        }

        binding.btnResumeSettings.setOnClickListener {
            Toast.makeText(requireContext(), "Resume Settings coming soon!", Toast.LENGTH_SHORT).show()
        }

        binding.btnHelp.setOnClickListener {
            Toast.makeText(requireContext(), "Help and Support coming soon!", Toast.LENGTH_SHORT).show()
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

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}