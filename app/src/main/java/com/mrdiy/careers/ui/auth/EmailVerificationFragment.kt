package com.mrdiy.careers.ui.auth

import android.content.Intent
import android.os.Bundle
import android.os.CountDownTimer
import androidx.lifecycle.lifecycleScope
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.view.animation.AnimationUtils
import android.widget.Toast
import androidx.core.view.isVisible
import androidx.fragment.app.Fragment
import androidx.navigation.fragment.findNavController
import androidx.navigation.fragment.navArgs
import com.mrdiy.careers.R
import com.mrdiy.careers.data.auth.SupabaseProvider
import com.mrdiy.careers.databinding.FragmentEmailVerificationBinding
import io.github.jan.supabase.auth.OtpType
import io.github.jan.supabase.auth.auth
import kotlinx.serialization.json.jsonPrimitive
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext

class EmailVerificationFragment : Fragment() {

    private var _binding: FragmentEmailVerificationBinding? = null
    private val binding get() = _binding!!

    private val args: EmailVerificationFragmentArgs by navArgs()

    private var resendTimer: CountDownTimer? = null
    private val RESEND_COOLDOWN_MS = 60_000L

    private var isEmailVerified = false

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = FragmentEmailVerificationBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        binding.tvEmail.text = args.email
        startPulseAnimation()
        setupClickListeners()
        checkIfEmailVerified()
    }

    override fun onResume() {
        super.onResume()
        if (!isEmailVerified) {
            checkIfEmailVerified()
        }
    }

    private fun checkIfEmailVerified() {
        binding.progressBar.isVisible = true
        binding.btnRefreshStatus.isEnabled = false

        viewLifecycleOwner.lifecycleScope.launch(Dispatchers.IO) {
            try {
                val auth = SupabaseProvider.client.auth
                auth.refreshCurrentSession()
                delay(500)
                val user = auth.currentUserOrNull()

                if (user != null) {
                    val emailConfirmed = user.appMetadata?.get("email_confirmed")?.jsonPrimitive?.content == "true"

                    if (emailConfirmed) {
                        isEmailVerified = true
                        withContext(Dispatchers.Main) {
                            if (!isAdded || _binding == null) return@withContext
                            binding.progressBar.isVisible = false
                            Toast.makeText(requireContext(), "Email verified! Redirecting...", Toast.LENGTH_SHORT).show()
                            navigateToLogin()
                        }
                    } else {
                        withContext(Dispatchers.Main) {
                            if (!isAdded || _binding == null) return@withContext
                            binding.progressBar.isVisible = false
                            binding.btnRefreshStatus.isEnabled = true
                        }
                    }
                } else {
                    withContext(Dispatchers.Main) {
                        if (!isAdded || _binding == null) return@withContext
                        binding.progressBar.isVisible = false
                        binding.btnRefreshStatus.isEnabled = true
                    }
                }
            } catch (e: Exception) {
                withContext(Dispatchers.Main) {
                    if (!isAdded || _binding == null) return@withContext
                    binding.progressBar.isVisible = false
                    binding.btnRefreshStatus.isEnabled = true
                }
            }
        }
    }

    private fun navigateToLogin() {
        findNavController().navigate(
            R.id.action_emailVerificationFragment_to_loginFragment
        )
    }

    override fun onDestroyView() {
        resendTimer?.cancel()
        super.onDestroyView()
        _binding = null
    }

    private fun setupClickListeners() {
        binding.ivBack.setOnClickListener {
            findNavController().navigateUp()
        }

        binding.btnOpenEmail.setOnClickListener {
            openEmailApp()
        }

        binding.tvResend.setOnClickListener {
            resendVerificationEmail()
        }

        binding.btnRefreshStatus.setOnClickListener {
            checkIfEmailVerified()
        }
    }

    private fun resendVerificationEmail() {
        val email = args.email
        if (email.isBlank()) return

        binding.tvResend.isEnabled = false

        viewLifecycleOwner.lifecycleScope.launch(Dispatchers.IO) {
            try {
                SupabaseProvider.client.auth.resendEmail(OtpType.Email.SIGNUP, email)

                withContext(Dispatchers.Main) {
                    if (!isAdded || _binding == null) return@withContext
                    Toast.makeText(
                        requireContext(),
                        "Verification email resent to $email",
                        Toast.LENGTH_SHORT
                    ).show()
                    startResendCooldown()
                }
            } catch (e: Exception) {
                withContext(Dispatchers.Main) {
                    if (!isAdded || _binding == null) return@withContext
                    Toast.makeText(
                        requireContext(),
                        "Could not resend - please wait a moment and try again.",
                        Toast.LENGTH_LONG
                    ).show()
                    binding.tvResend.isEnabled = true
                }
            }
        }
    }

    private fun startResendCooldown() {
        binding.tvResend.isVisible = false
        binding.tvResendTimer.isVisible = true

        resendTimer?.cancel()
        resendTimer = object : CountDownTimer(RESEND_COOLDOWN_MS, 1_000) {
            override fun onTick(millisUntilFinished: Long) {
                val secs = millisUntilFinished / 1_000
                binding.tvResendTimer.text = "Resend in 0:${secs.toString().padStart(2, '0')}"
            }

            override fun onFinish() {
                binding.tvResendTimer.isVisible = false
                binding.tvResend.isVisible = true
                binding.tvResend.isEnabled = true
            }
        }.start()
    }

    private fun openEmailApp() {
        try {
            val intent = Intent(Intent.ACTION_MAIN).apply {
                addCategory(Intent.CATEGORY_APP_EMAIL)
                addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
            }
            startActivity(intent)
        } catch (e: Exception) {
            Toast.makeText(
                requireContext(),
                "No email app found - please open your email manually.",
                Toast.LENGTH_LONG
            ).show()
        }
    }

    private fun startPulseAnimation() {
        val pulse = AnimationUtils.loadAnimation(requireContext(), R.anim.pulse_scale)
        binding.vPulseDot.startAnimation(pulse)
    }
}