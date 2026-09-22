package com.mrdiy.careers.ui.jobs

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import android.widget.Toast
import androidx.core.view.isVisible
import androidx.lifecycle.lifecycleScope
import androidx.navigation.fragment.findNavController
import com.mrdiy.careers.R
import com.mrdiy.careers.data.repository.PhpJobRepository
import com.mrdiy.careers.data.repository.JobRepository
import com.mrdiy.careers.data.repository.ApplicationsRepository
import com.mrdiy.careers.data.repository.SavedJobsRepository
import com.mrdiy.careers.databinding.FragmentJobDetailBinding
import com.mrdiy.careers.model.Job
import com.mrdiy.careers.ui.base.BaseFragment
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext

class JobDetailFragment : BaseFragment() {

    private var _binding: FragmentJobDetailBinding? = null
    private val binding get() = _binding!!

    private val phpJobRepository = PhpJobRepository()
    private var applicationsRepository: ApplicationsRepository? = null
    private var savedJobsRepository: SavedJobsRepository? = null
    private var currentJob: Job? = null
    private var isJobSaved = false

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = FragmentJobDetailBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        setName("JobDetailFragment")

        try {
            savedJobsRepository = SavedJobsRepository(requireContext())
            applicationsRepository = ApplicationsRepository(requireContext())
        } catch (e: Exception) {
            e.printStackTrace()
        }

        val jobId = arguments?.getString("jobId") ?: ""

        if (jobId.isEmpty()) {
            Toast.makeText(requireContext(), "Invalid job ID", Toast.LENGTH_SHORT).show()
            findNavController().navigateUp()
            return
        }

        showLoading(true)

        viewLifecycleOwner.lifecycleScope.launch(Dispatchers.IO) {
            try {
                val result = phpJobRepository.getJobById(jobId)
                withContext(Dispatchers.Main) {
                    if (!isAdded || _binding == null) return@withContext
                    showLoading(false)

                    result.onSuccess { job ->
                        currentJob = job
                        checkIfJobSaved(job.id)
                        displayJob(job)
                    }.onFailure { e ->
                        val fallbackJob = JobRepository.getJobById(jobId)
                        if (fallbackJob != null) {
                            currentJob = fallbackJob
                            checkIfJobSaved(fallbackJob.id)
                            displayJob(fallbackJob)
                        } else {
                            Toast.makeText(requireContext(), "We could not load this job. Please try again.", Toast.LENGTH_SHORT).show()
                            findNavController().navigateUp()
                        }
                    }
                }
            } catch (e: Exception) {
                withContext(Dispatchers.Main) {
                    if (!isAdded || _binding == null) return@withContext
                    showLoading(false)
                    val fallbackJob = JobRepository.getJobById(jobId)
                    if (fallbackJob != null) {
                        currentJob = fallbackJob
                        checkIfJobSaved(fallbackJob.id)
                        displayJob(fallbackJob)
                    } else {
                        Toast.makeText(requireContext(), "We could not load this job. Please try again.", Toast.LENGTH_SHORT).show()
                        try {
                            findNavController().navigateUp()
                        } catch (ignored: Exception) {}
                    }
                }
            }
        }

        binding.toolbar.setNavigationOnClickListener { findNavController().navigateUp() }

        binding.btnApply.setOnClickListener {
            currentJob?.let { job ->
                binding.btnApply.isEnabled = false
                viewLifecycleOwner.lifecycleScope.launch(Dispatchers.IO) {
                    val result = applicationsRepository?.apply(job)
                        ?: Result.failure(IllegalStateException("Application service unavailable."))
                    withContext(Dispatchers.Main) {
                        if (!isAdded || _binding == null) return@withContext
                        binding.btnApply.isEnabled = true
                        result.onSuccess {
                            Toast.makeText(requireContext(), "Application submitted.", Toast.LENGTH_SHORT).show()
                        }.onFailure { error ->
                            Toast.makeText(
                                requireContext(),
                                error.message ?: "Application failed. Please try again.",
                                Toast.LENGTH_LONG
                            ).show()
                        }
                    }
                }
            }
        }

        binding.btnSave.setOnClickListener {
            currentJob?.let { job ->
                toggleSaveJob(job)
            }
        }
    }

    private fun checkIfJobSaved(jobId: String) {
        if (savedJobsRepository == null) {
            isJobSaved = false
            updateSaveButtonIcon()
            return
        }
        viewLifecycleOwner.lifecycleScope.launch(Dispatchers.IO) {
            try {
                val saved = savedJobsRepository?.isJobSaved(jobId) ?: false
                withContext(Dispatchers.Main) {
                    if (!isAdded || _binding == null) return@withContext
                    isJobSaved = saved
                    updateSaveButtonIcon()
                }
            } catch (e: Exception) {
                e.printStackTrace()
                withContext(Dispatchers.Main) {
                    isJobSaved = false
                    updateSaveButtonIcon()
                }
            }
        }
    }

    private fun toggleSaveJob(job: Job) {
        viewLifecycleOwner.lifecycleScope.launch(Dispatchers.IO) {
            if (isJobSaved) {
                savedJobsRepository?.unsaveJob(job.id)
            } else {
                savedJobsRepository?.saveJob(job)
            }
            withContext(Dispatchers.Main) {
                if (!isAdded || _binding == null) return@withContext
                isJobSaved = !isJobSaved
                updateSaveButtonIcon()
                val message = if (isJobSaved) "Job saved!" else "Job removed from saved"
                Toast.makeText(requireContext(), message, Toast.LENGTH_SHORT).show()
            }
        }
    }

    private fun updateSaveButtonIcon() {
        binding.btnSave.setIconResource(
            if (isJobSaved) R.drawable.ic_bookmark_filled else R.drawable.ic_bookmark_outline
        )
    }

    private fun showLoading(show: Boolean) {
        binding.toolbar.isVisible = !show
    }

    private fun displayJob(job: Job) {
        try {
            binding.btnApply.isEnabled = true
            binding.tvJdTitle.text      = job.title
            binding.tvJdCompanyLoc.text = "${job.company} · ${job.branch}, ${job.location}"

            val monthlyMin = job.getMonthlySalary()
            val monthlyMax = job.getMonthlySalaryMax()
            binding.tvSalaryRange.text = if (job.salaryMin == 0 && job.salaryMax == 0) {
                "Salary not disclosed"
            } else if (monthlyMin == monthlyMax) {
                "₱${"%,d".format(monthlyMin)}/mo"
            } else {
                "₱${"%,d".format(monthlyMin)} – ₱${"%,d".format(monthlyMax)}/mo"
            }

            binding.tvAboutRole.text = job.aboutRole.ifEmpty { "No description provided." }

            addBullets(binding.responsibilitiesContainer, job.responsibilities)
            addBullets(binding.requirementsContainer, job.requirements)
            addBullets(binding.benefitsContainer, job.benefits)
        } catch (e: Exception) {
            e.printStackTrace()
            Toast.makeText(requireContext(), "Error displaying job details", Toast.LENGTH_SHORT).show()
        }
    }

    private fun addBullets(container: ViewGroup, items: List<String>) {
        container.removeAllViews()
        if (items.isEmpty()) {
            val row = LayoutInflater.from(requireContext())
                .inflate(R.layout.item_bullet_point, container, false)
            row.findViewById<TextView>(R.id.tv_bullet_text).text = "Information not available"
            container.addView(row)
        } else {
            items.forEach { text ->
                val row = LayoutInflater.from(requireContext())
                    .inflate(R.layout.item_bullet_point, container, false)
                row.findViewById<TextView>(R.id.tv_bullet_text).text = text
                container.addView(row)
            }
        }
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}