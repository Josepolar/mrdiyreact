package com.mrdiy.careers.ui.profile

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.core.view.isVisible
import androidx.fragment.app.FragmentManager
import com.google.android.material.bottomsheet.BottomSheetDialogFragment
import com.mrdiy.careers.databinding.DialogAddExperienceBinding
import com.mrdiy.careers.model.WorkExperience

class AddExperienceBottomSheet : BottomSheetDialogFragment() {

    private var _binding: DialogAddExperienceBinding? = null
    private val binding get() = _binding!!

    private var existing: WorkExperience? = null
    private var onSaved: ((WorkExperience) -> Unit)? = null

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = DialogAddExperienceBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        // Pre-fill if editing
        existing?.let { exp ->
            binding.etExpRole.setText(exp.role)
            binding.etExpCompany.setText(exp.company)
            binding.etExpStartYear.setText(exp.startYear)
            binding.etExpEndYear.setText(exp.endYear)
            binding.switchIsCurrent.isChecked = exp.isCurrent
            binding.tilExpEndYear.isVisible = !exp.isCurrent
            binding.tvSheetTitle.text = "Edit Experience"
        }

        // Toggle end year visibility based on "Currently working here"
        binding.switchIsCurrent.setOnCheckedChangeListener { _, isChecked ->
            binding.tilExpEndYear.isVisible = !isChecked
        }

        binding.btnSaveExp.setOnClickListener {
            if (validate()) {
                val exp = WorkExperience(
                    id        = existing?.id ?: System.currentTimeMillis().toString(),
                    role      = binding.etExpRole.text?.trim().toString(),
                    company   = binding.etExpCompany.text?.trim().toString(),
                    startYear = binding.etExpStartYear.text?.trim().toString(),
                    endYear   = if (binding.switchIsCurrent.isChecked) "Present"
                    else binding.etExpEndYear.text?.trim().toString(),
                    isCurrent = binding.switchIsCurrent.isChecked
                )
                onSaved?.invoke(exp)
                dismiss()
            }
        }

        binding.btnCancelExp.setOnClickListener { dismiss() }
    }

    private fun validate(): Boolean {
        var valid = true

        if (binding.etExpRole.text.isNullOrBlank()) {
            binding.etExpRole.error = "Role is required"
            valid = false
        }
        if (binding.etExpCompany.text.isNullOrBlank()) {
            binding.etExpCompany.error = "Company is required"
            valid = false
        }
        if (binding.etExpStartYear.text.isNullOrBlank()) {
            binding.etExpStartYear.error = "Start year is required"
            valid = false
        } else {
            val year = binding.etExpStartYear.text.toString().toIntOrNull()
            if (year == null || year < 1950 || year > 2099) {
                binding.etExpStartYear.error = "Enter a valid year (e.g. 2020)"
                valid = false
            }
        }
        if (!binding.switchIsCurrent.isChecked) {
            if (binding.etExpEndYear.text.isNullOrBlank()) {
                binding.etExpEndYear.error = "End year is required"
                valid = false
            } else {
                val endYear = binding.etExpEndYear.text.toString().toIntOrNull()
                val startYear = binding.etExpStartYear.text.toString().toIntOrNull() ?: 0
                if (endYear == null || endYear < startYear) {
                    binding.etExpEndYear.error = "Must be ≥ start year"
                    valid = false
                }
            }
        }
        return valid
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }

    companion object {
        private const val TAG = "AddExperienceBottomSheet"

        fun show(
            fragmentManager: FragmentManager,
            existing: WorkExperience?,
            onSaved: (WorkExperience) -> Unit
        ) {
            AddExperienceBottomSheet().apply {
                this.existing = existing
                this.onSaved  = onSaved
            }.show(fragmentManager, TAG)
        }
    }
}