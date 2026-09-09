package com.mrdiy.careers.ui.auth

import android.content.Context
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ArrayAdapter
import android.widget.Toast
import androidx.fragment.app.Fragment
import androidx.fragment.app.activityViewModels
import androidx.lifecycle.lifecycleScope
import androidx.navigation.fragment.findNavController
import com.mrdiy.careers.R
import com.mrdiy.careers.data.auth.SupabaseProvider
import com.mrdiy.careers.databinding.FragmentWelcomeBinding
import com.mrdiy.careers.model.UserProfile
import com.mrdiy.careers.ui.profile.ProfileViewModel
import io.github.jan.supabase.auth.auth
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext

class WelcomeFragment : Fragment() {

    private var _binding: FragmentWelcomeBinding? = null
    private val binding get() = _binding!!

    private val viewModel: ProfileViewModel by activityViewModels()

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = FragmentWelcomeBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        setupDropdowns()
        setupClickListeners()
    }

private fun setupDropdowns() {
        val experiences = listOf("0", "1", "2", "3", "5", "6", "7", "8", "10+")
        val expAdapter = ArrayAdapter(requireContext(), android.R.layout.simple_dropdown_item_1line, experiences)
        binding.actvExperience.setAdapter(expAdapter)
    }

    private fun getUserIdFromPrefs(): String {
        val prefs = requireContext().getSharedPreferences("auth_prefs", Context.MODE_PRIVATE)
        return prefs.getString("user_id", "") ?: ""
    }

    private fun setupClickListeners() {
        binding.btnContinue.setOnClickListener {
            val fullName = binding.etFullName.text?.trim().toString()
            val location = binding.etLocation.text?.trim().toString()
            val position = binding.etPosition.text?.trim().toString()
            val experience = binding.actvExperience.text?.trim().toString()

            if (fullName.isEmpty()) {
                binding.tilFullName.error = "Please enter your name"
                return@setOnClickListener
            }

            if (position.isEmpty()) {
                binding.tilPosition.error = "Please enter your desired position"
                return@setOnClickListener
            }

            lifecycleScope.launch {
                val prefs = requireContext().getSharedPreferences("auth_prefs", Context.MODE_PRIVATE)
                val email = prefs.getString("user_email", "") ?: ""
                val phone = prefs.getString("user_phone", "") ?: ""
                val uid = withContext(Dispatchers.IO) {
                    try {
                        SupabaseProvider.client.auth.currentUserOrNull()?.id ?: getUserIdFromPrefs()
                    } catch (e: Exception) {
                        getUserIdFromPrefs()
                    }
                }

                if (uid.isEmpty()) {
                    Toast.makeText(requireContext(), "Please log in first", Toast.LENGTH_SHORT).show()
                    return@launch
                }

                saveUserProfile(fullName, location, position, experience, uid, email, phone)
            }
        }
    }

    private fun saveUserProfile(fullName: String, location: String, position: String, experience: String, userId: String, email: String, phone: String) {
        binding.btnContinue.isEnabled = false
        binding.progressBar.visibility = View.VISIBLE

        val profile = UserProfile(
            id = userId,
            fullName = fullName,
            email = email,
            phone = phone,
            location = location,
            desiredPosition = position,
            yearsOfExperience = experience.toIntOrNull() ?: 0
        )

        viewModel.saveProfile(profile) { success ->
            binding.progressBar.visibility = View.GONE
            binding.btnContinue.isEnabled = true

            if (success == true) {
                Toast.makeText(requireContext(), "Profile saved!", Toast.LENGTH_SHORT).show()
                navigateToHome()
            } else {
                Toast.makeText(requireContext(), "Failed to save profile. Please try again.", Toast.LENGTH_SHORT).show()
            }
        }
    }

    private fun navigateToHome() {
        findNavController().navigate(R.id.action_welcomeFragment_to_homeFragment)
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}