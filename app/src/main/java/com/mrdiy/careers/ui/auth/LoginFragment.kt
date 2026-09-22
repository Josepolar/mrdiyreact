package com.mrdiy.careers.ui.auth

import android.content.Context
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.core.view.isVisible
import androidx.fragment.app.Fragment
import androidx.navigation.fragment.findNavController
import com.mrdiy.careers.R
import com.mrdiy.careers.data.auth.AuthManager
import com.mrdiy.careers.data.repository.ProfileRepository
import com.mrdiy.careers.databinding.FragmentLoginBinding

class LoginFragment : Fragment() {

    private var _binding: FragmentLoginBinding? = null
    private val binding get() = _binding!!

    private lateinit var authManager: AuthManager

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = FragmentLoginBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        authManager = AuthManager(requireContext())
        if (findNavController().currentDestination?.id != R.id.loginFragment) return

        if (authManager.isLoggedIn) {
            checkProfileAndNavigate()
            return
        }

        binding.cardFacebook.visibility = View.GONE
        binding.etEmail.setOnFocusChangeListener { _, focused -> if (focused) binding.tilEmail.error = null }
        binding.etPassword.setOnFocusChangeListener { _, focused -> if (focused) binding.tilPassword.error = null }
        setupClickListeners()
    }

private fun checkProfileAndNavigate() {
        if (!isAdded || _binding == null || findNavController().currentDestination?.id != R.id.loginFragment) return
        val authPrefs = requireContext()
            .getSharedPreferences("auth_prefs", Context.MODE_PRIVATE)
        val userId = authPrefs.getString("user_id", "") ?: ""

        if (userId.isEmpty()) {
            findNavController().navigate(R.id.action_loginFragment_to_welcomeFragment)
            return
        }

        // 1️⃣ Fast path – local cache
        val profilePrefs = requireContext()
            .getSharedPreferences("profile_$userId", Context.MODE_PRIVATE)
        if (!profilePrefs.getString("full_name", "").isNullOrEmpty()) {
            navigateToHome()
            return
        }

        // 2️⃣ Supabase fallback – if profile exists with real data, save to cache and go Home
        val repo = ProfileRepository(requireContext())
        repo.loadProfile(userId) { profile ->
            if (!isAdded || _binding == null || findNavController().currentDestination?.id != R.id.loginFragment) return@loadProfile
            if (profile != null && profile.fullName.isNotBlank()) {
                navigateToHome()
            } else {
                findNavController().navigate(R.id.action_loginFragment_to_welcomeFragment)
            }
        }
    }

    private fun setupClickListeners() {
        binding.btnLogin.setOnClickListener {
            val email = binding.etEmail.text?.trim().toString()
            val password = binding.etPassword.text?.toString().orEmpty()

            if (validateInput(email, password)) {
                binding.progressBar.isVisible = true
                binding.btnLogin.isEnabled = false

                authManager.login(email, password) { success, message ->
                    if (!isAdded || _binding == null) return@login
                    binding.progressBar.isVisible = false
                    binding.btnLogin.isEnabled = true

                    Toast.makeText(requireContext(), message, Toast.LENGTH_SHORT).show()

                    if (success) {
                        authManager.saveLoginState(true)
                        checkProfileAndNavigate()
                    }
                }
            }
        }

        binding.tvForgotPassword.setOnClickListener { showForgotPasswordDialog() }

        binding.tvRegister.setOnClickListener {
            findNavController().navigate(R.id.action_loginFragment_to_registerFragment)
        }

        binding.tvPhoneLogin.setOnClickListener {
            findNavController().navigate(R.id.action_loginFragment_to_phoneLoginFragment)
        }

        binding.cardGoogle.setOnClickListener {
            authManager.signInWithGoogle { success, message ->
                Toast.makeText(requireContext(), message, Toast.LENGTH_SHORT).show()
            }
        }

        binding.cardFacebook.setOnClickListener {
            Toast.makeText(requireContext(), "Use Google Sign-In", Toast.LENGTH_SHORT).show()
        }

binding.cardPhone.setOnClickListener {
            findNavController().navigate(R.id.action_loginFragment_to_phoneLoginFragment)
        }
    }

    private fun validateInput(email: String, password: String): Boolean {
        var isValid = true

        if (email.isEmpty()) {
            binding.tilEmail.error = "Email is required"; isValid = false
        } else if (!android.util.Patterns.EMAIL_ADDRESS.matcher(email).matches()) {
            binding.tilEmail.error = "Enter a valid email"; isValid = false
        } else { binding.tilEmail.error = null }

        if (password.isEmpty()) {
            binding.tilPassword.error = "Password is required"; isValid = false
        } else if (password.length < 6) {
            binding.tilPassword.error = "Password must be at least 6 characters"; isValid = false
        } else { binding.tilPassword.error = null }

        return isValid
    }

    private fun showForgotPasswordDialog() {
        val dialogView = layoutInflater.inflate(R.layout.dialog_forgot_password, null)
        val etEmail    = dialogView.findViewById<com.google.android.material.textfield.TextInputEditText>(R.id.etForgotEmail)

        com.google.android.material.dialog.MaterialAlertDialogBuilder(requireContext())
            .setTitle("Reset Password")
            .setView(dialogView)
            .setPositiveButton("Send") { _, _ ->
                val email = etEmail.text?.trim().toString()
                if (email.isNotEmpty() && android.util.Patterns.EMAIL_ADDRESS.matcher(email).matches()) {
                    authManager.resetPassword(email) { _, message ->
                        Toast.makeText(requireContext(), message, Toast.LENGTH_SHORT).show()
                    }
                } else {
                    Toast.makeText(requireContext(), "Enter a valid email", Toast.LENGTH_SHORT).show()
                }
            }
            .setNegativeButton("Cancel", null)
            .show()
    }

    private fun navigateToHome() {
        if (!isAdded || view == null || findNavController().currentDestination?.id != R.id.loginFragment) return
        findNavController().navigate(R.id.action_loginFragment_to_homeFragment)
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}
