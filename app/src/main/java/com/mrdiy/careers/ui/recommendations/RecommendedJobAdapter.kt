package com.mrdiy.careers.ui.recommendations

import android.graphics.Color
import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.core.content.ContextCompat
import androidx.recyclerview.widget.RecyclerView
import com.mrdiy.careers.R
import com.mrdiy.careers.databinding.ItemRecommendedJobCardBinding
import com.mrdiy.careers.model.Job
import com.mrdiy.careers.model.JobMatchResult

class RecommendedJobAdapter(
    private val results: List<JobMatchResult>,
    private val onJobClick: (Job) -> Unit
) : RecyclerView.Adapter<RecommendedJobAdapter.ViewHolder>() {

    inner class ViewHolder(private val binding: ItemRecommendedJobCardBinding) :
        RecyclerView.ViewHolder(binding.root) {

        fun bind(result: JobMatchResult) {
            val job = result.job

            binding.tvJobTitle.text = job.title
            binding.tvCompanyBranch.text = "${job.company} · ${job.branch}"
            val monthlyMin = job.getMonthlySalary()
            val monthlyMax = job.getMonthlySalaryMax()
            binding.tvSalary.text = if (monthlyMin == monthlyMax) {
                "₱${"%,d".format(monthlyMin)}/mo"
            } else {
                "₱${"%,d".format(monthlyMin)} – ₱${"%,d".format(monthlyMax)}/mo"
            }
            binding.chipJobType.text = job.jobType
            binding.chipLocation.text = job.location
            binding.chipCategory.text = job.category

            // ── Match score badge ─────────────────────────────────────────────
            binding.tvMatchScore.text = "${result.matchScore}%"
            binding.tvMatchLabel.text = result.matchLabel

            val (badgeColor, labelColor) = when {
                result.matchScore >= 85 -> Pair(R.color.match_excellent_bg, R.color.match_excellent_text)
                result.matchScore >= 65 -> Pair(R.color.match_good_bg, R.color.match_good_text)
                result.matchScore >= 45 -> Pair(R.color.match_fair_bg, R.color.match_fair_text)
                else -> Pair(R.color.match_low_bg, R.color.match_low_text)
            }
            binding.matchBadge.setCardBackgroundColor(
                ContextCompat.getColor(binding.root.context, badgeColor)
            )
            binding.tvMatchScore.setTextColor(
                ContextCompat.getColor(binding.root.context, labelColor)
            )
            binding.tvMatchLabel.setTextColor(
                ContextCompat.getColor(binding.root.context, labelColor)
            )

            // ── Matched skills chips ──────────────────────────────────────────
            binding.chipGroupMatchedSkills.removeAllViews()
            result.matchedSkills.take(3).forEach { skill ->
                val chip = com.google.android.material.chip.Chip(binding.root.context).apply {
                    text = skill
                    isClickable = false
                    setChipBackgroundColorResource(R.color.chip_skill_matched_bg)
                    setTextColor(ContextCompat.getColor(context, R.color.chip_skill_matched_text))
                    textSize = 11f
                }
                binding.chipGroupMatchedSkills.addView(chip)
            }

            // ── First match reason ────────────────────────────────────────────
            binding.tvMatchReason.text = result.matchReasons.firstOrNull() ?: ""

            binding.jobCard.setOnClickListener { onJobClick(job) }
        }
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int) = ViewHolder(
        ItemRecommendedJobCardBinding.inflate(
            LayoutInflater.from(parent.context), parent, false
        )
    )

    override fun onBindViewHolder(holder: ViewHolder, position: Int) =
        holder.bind(results[position])

    override fun getItemCount() = results.size
}
