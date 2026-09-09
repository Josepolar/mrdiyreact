package com.mrdiy.careers.ui.auth

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
import com.mrdiy.careers.databinding.FragmentPhoneLoginBinding

class PhoneLoginFragment : Fragment() {

    private var _binding: FragmentPhoneLoginBinding? = null
    private val binding get() = _binding!!

    private lateinit var authManager: AuthManager

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = FragmentPhoneLoginBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        authManager = AuthManager(requireContext())

        setupClickListeners()
    }

    private fun setupClickListeners() {
        binding.ivBack.setOnClickListener {
            findNavController().navigateUp()
        }

        binding.btnSendOtp.setOnClickListener {
            val phone = binding.etPhone.text?.trim().toString()
            if (validatePhone(phone)) {
                // Demo: just show message
                Toast.makeText(requireContext(), "OTP sent to +63$phone (demo)", Toast.LENGTH_SHORT).show()
                binding.otpSection.isVisible = true
                binding.btnSendOtp.isEnabled = false
            }
        }

        binding.btnVerify.setOnClickListener {
            val otp = binding.etOtp.text?.trim().toString()
            if (otp.length == 6) {
                Toast.makeText(requireContext(), "Phone verified! (demo)", Toast.LENGTH_SHORT).show()
                authManager.saveLoginState(true)
                navigateToHome()
            } else {
                binding.tilOtp.error = "Enter 6-digit code"
            }
        }

        binding.tvResend.setOnClickListener {
            Toast.makeText(requireContext(), "Resend OTP (demo)", Toast.LENGTH_SHORT).show()
        }
    }

    private fun validatePhone(phone: String): Boolean {
        return if (phone.isEmpty()) {
            binding.tilPhone.error = "Phone number is required"
            false
        } else if (phone.length < 10) {
            binding.tilPhone.error = "Enter a valid phone number"
            false
        } else {
            binding.tilPhone.error = null
            true
        }
    }

    private fun navigateToHome() {
        findNavController().navigate(R.id.action_phoneLoginFragment_to_homeFragment)
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}