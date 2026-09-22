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
        val experiences = (0..10).map(Int::toString)
        val expAdapter = ArrayAdapter(requireContext(), android.R.layout.simple_dropdown_item_1line, experiences)
        binding.actvExperience.setAdapter(expAdapter)
        val auth = com.mrdiy.careers.data.auth.AuthManager(requireContext())
        val cached = com.mrdiy.careers.data.repository.ProfileRepository(requireContext()).loadFromPrefs()
        val registeredName = cached.fullName.ifBlank { auth.getCurrentUserName() }
        if (binding.etFullName.text.isNullOrBlank()) binding.etFullName.setText(registeredName)
    }


    private fun setupClickListeners() {
        binding.btnContinue.setOnClickListener {
            if (!binding.btnContinue.isEnabled) return@setOnClickListener
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

            viewLifecycleOwner.lifecycleScope.launch {
                val user = SupabaseProvider.client.auth.currentUserOrNull()
                val email = user?.email.orEmpty()
                val phone = user?.userMetadata?.get("phone")?.let { (it as? kotlinx.serialization.json.JsonPrimitive)?.content }.orEmpty()
                val uid = com.mrdiy.careers.data.auth.AuthManager(requireContext()).getCurrentUserId().orEmpty()
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

        val profile = viewModel.currentProfile().copy(
            id = userId,
            fullName = fullName,
            email = email,
            phone = phone,
            location = location,
            desiredPosition = position,
            yearsOfExperience = com.mrdiy.careers.data.auth.AccountRules.experience(experience) ?: 0
        )

        viewModel.saveProfile(profile) { success ->
            if (_binding == null || !isAdded) return@saveProfile
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
