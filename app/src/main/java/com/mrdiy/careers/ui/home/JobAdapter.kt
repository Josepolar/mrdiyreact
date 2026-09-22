package com.mrdiy.careers.ui.home

import androidx.core.view.isVisible
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.core.content.ContextCompat
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.RecyclerView
import com.mrdiy.careers.R
import com.mrdiy.careers.databinding.ItemJobCardBinding
import com.mrdiy.careers.model.Job

class JobAdapter(
    private var jobs: List<Job>,
    private val onJobClick: (Job) -> Unit,
    private val onSaveClick: ((Job) -> Unit)? = null
) : RecyclerView.Adapter<JobAdapter.JobViewHolder>() {

    private var savedJobIds: Set<String> = emptySet()

    // ───────── Update Jobs ─────────

    fun updateList(newJobs: List<Job>) {

        val diff = DiffUtil.calculateDiff(
            object : DiffUtil.Callback() {

                override fun getOldListSize() =
                    jobs.size

                override fun getNewListSize() =
                    newJobs.size

                override fun areItemsTheSame(
                    oldItemPosition: Int,
                    newItemPosition: Int
                ): Boolean {

                    return jobs[
                        oldItemPosition
                    ].id ==
                            newJobs[
                                newItemPosition
                            ].id
                }

                override fun areContentsTheSame(
                    oldItemPosition: Int,
                    newItemPosition: Int
                ): Boolean {

                    return jobs[
                        oldItemPosition
                    ] ==
                            newJobs[
                                newItemPosition
                            ]
                }
            }
        )

        jobs = newJobs

        diff.dispatchUpdatesTo(this)
    }

    // ───────── Saved Jobs ─────────

    fun updateSavedJobs(
        newSavedIds: Set<String>
    ) {

        val changedIds =
            savedJobIds
                .symmetricDifference(
                    newSavedIds
                )

        savedJobIds = newSavedIds

        jobs.forEachIndexed { index, job ->

            if (
                job.id in changedIds
            ) {

                notifyItemChanged(
                    index,
                    PAYLOAD_SAVE_STATE
                )
            }
        }
    }

    // ───────── ViewHolder ─────────

    inner class JobViewHolder(
        private val binding:
        ItemJobCardBinding
    ) : RecyclerView.ViewHolder(
        binding.root
    ) {

        fun bind(job: Job) {

            binding.tvJobTitle.text =
                job.title

            binding.tvCompanyBranch.text =
                "${job.company} · ${job.branch}"

            binding.tvPostedTime.text =
                job.postedAgo

            binding.chipJobType.visibility = if (job.jobType.isNotBlank() && job.jobType != "Not specified") View.VISIBLE else View.GONE
            binding.chipCategory.visibility = if (job.category.isNotBlank() && job.category != "Not specified") View.VISIBLE else View.GONE
            binding.chipJobType.text =
                job.jobType

            binding.chipLocation.text =
                job.location

            binding.chipCategory.text =
                job.category

            // Salary display
            val monthlyMin = job.getMonthlySalary()
            val monthlyMax = job.getMonthlySalaryMax()

            binding.tvSalary.text =
                when {
                    job.salaryMin == 0 && job.salaryMax == 0 ->
                        "Salary not disclosed"

                    monthlyMin == monthlyMax ->
                        "₱${"%,d".format(monthlyMin)}/mo"

                    else ->
                        "₱${"%,d".format(monthlyMin)} – ₱${"%,d".format(monthlyMax)}/mo"
                }

            bindSaveButton(job)

            binding.jobCard
                .setOnClickListener {

                    onJobClick(job)
                }
        }

        fun bindSaveButton(
            job: Job
        ) {

            val isSaved =
                savedJobIds.contains(
                    job.id
                )

            binding.btnSaveJob
                .setImageResource(

                    if (isSaved)

                        R.drawable
                            .ic_bookmark_filled

                    else

                        R.drawable
                            .ic_bookmark_outline
                )

            binding.btnSaveJob
                .setColorFilter(

                    ContextCompat
                        .getColor(

                            binding.root.context,

                            if (isSaved)

                                R.color
                                    .orange_primary

                            else

                                R.color
                                    .text_muted
                        )
                )

            binding.btnSaveJob
                .setOnClickListener {

                    onSaveClick
                        ?.invoke(job)
                }
        }
    }

    // ───────── Adapter Overrides ─────────

    override fun onCreateViewHolder(
        parent: ViewGroup,
        viewType: Int
    ): JobViewHolder {

        val binding =
            ItemJobCardBinding
                .inflate(

                    LayoutInflater
                        .from(
                            parent.context
                        ),

                    parent,

                    false
                )

        return JobViewHolder(
            binding
        )
    }

    override fun onBindViewHolder(
        holder: JobViewHolder,
        position: Int
    ) {

        holder.bind(
            jobs[position]
        )
    }

    override fun onBindViewHolder(
        holder: JobViewHolder,
        position: Int,
        payloads: List<Any>
    ) {

        if (
            payloads.contains(
                PAYLOAD_SAVE_STATE
            )
        ) {

            holder.bindSaveButton(
                jobs[position]
            )

        } else {

            super.onBindViewHolder(
                holder,
                position,
                payloads
            )
        }
    }

    override fun getItemCount() =
        jobs.size

    // ───────── Helpers ─────────

    private fun <T> Set<T>
            .symmetricDifference(
        other: Set<T>
    ): Set<T> {

        return (this - other) +
                (other - this)
    }

    companion object {

        private const val
                PAYLOAD_SAVE_STATE =
            "save_state"
    }
}
