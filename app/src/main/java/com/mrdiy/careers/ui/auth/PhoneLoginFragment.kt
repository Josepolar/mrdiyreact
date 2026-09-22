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
                val normalized = if (phone.startsWith("+")) phone else "+63$phone"
                binding.btnSendOtp.isEnabled = false
                authManager.requestPhoneOtp(normalized) { success, message ->
                    binding.btnSendOtp.isEnabled = true
                    Toast.makeText(requireContext(), message, Toast.LENGTH_SHORT).show()
                    if (success) binding.otpSection.isVisible = true
                }
            }
        }

        binding.btnVerify.setOnClickListener {
            val otp = binding.etOtp.text?.trim().toString()
            if (otp.length == 6) {
                val phone = binding.etPhone.text?.trim().toString()
                val normalized = if (phone.startsWith("+")) phone else "+63$phone"
                binding.btnVerify.isEnabled = false
                authManager.verifyPhoneOtp(normalized, otp) { success, message ->
                    binding.btnVerify.isEnabled = true
                    Toast.makeText(requireContext(), message, Toast.LENGTH_SHORT).show()
                    if (success) navigateToHome()
                }
            } else {
                binding.tilOtp.error = "Enter 6-digit code"
            }
        }

        binding.tvResend.setOnClickListener {
            binding.btnSendOtp.performClick()
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