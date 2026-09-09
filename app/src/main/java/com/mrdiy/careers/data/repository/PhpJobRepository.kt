package com.mrdiy.careers.data.repository

import com.mrdiy.careers.model.Job
import com.mrdiy.careers.network.ApiJob
import com.mrdiy.careers.network.RetrofitClient
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import java.util.concurrent.TimeUnit

class PhpJobRepository {

    private val api = RetrofitClient.jobApiService

    suspend fun fetchOpenJobs(): Result<List<Job>> = withContext(Dispatchers.IO) {
        try {
            val response = api.getJobs(status = "Open", page = 1, limit = 50)
            if (response.success) {
                Result.success(response.jobs.map { it.toJob() })
            } else {
                Result.failure(Exception("API returned success=false"))
            }
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    suspend fun fetchAllJobs(page: Int = 1, limit: Int = 20): Result<List<Job>> = withContext(Dispatchers.IO) {
        try {
            val response = api.getJobs(status = "All", page = page, limit = limit)
            if (response.success) {
                Result.success(response.jobs.map { it.toJob() })
            } else {
                Result.failure(Exception("API returned success=false"))
            }
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    suspend fun searchJobs(query: String, location: String = ""): Result<List<Job>> = withContext(Dispatchers.IO) {
        try {
            val response = api.searchJobs(query = query, location = location, status = "Open")
            if (response.success) {
                Result.success(response.jobs.map { it.toJob() })
            } else {
                Result.failure(Exception("API returned success=false"))
            }
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    suspend fun getJobById(id: String): Result<Job> = withContext(Dispatchers.IO) {
        try {
            val response = api.getJobDetails(id = id)
            if (response.success && response.job != null) {
                Result.success(response.job.toJob())
            } else {
                Result.failure(Exception(response.message ?: "Job not found"))
            }
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    private fun ApiJob.toJob(): Job {
        val (salaryMin, salaryMax) = parseSalary(salary)
        return Job(
            id = id,
            title = title,
            company = "MR.D.I.Y. Philippines",
            branch = location,
            location = location,
            jobType = "Full-time",
            category = "Retail",
            level = "Entry Level",
            salaryMin = salaryMin,
            salaryMax = salaryMax,
            salaryType = salary_type.ifBlank { "monthly" },
            postedAgo = posted_ago,
            aboutRole = description,
            responsibilities = emptyList(),
            requirements = emptyList(),
            benefits = emptyList()
        )
    }

    private fun parseSalary(salary: String): Pair<Int, Int> {
        return try {
            val cleaned = salary.replace(",", "").replace(" ", "")
            val numbers = cleaned.filter { it.isDigit() || it == '-' }.split("-")
            when {
                numbers.size == 2 -> {
                    val min = numbers[0].toIntOrNull() ?: 0
                    val max = numbers[1].toIntOrNull() ?: min
                    Pair(min, max)
                }
                numbers.size == 1 && numbers[0].isNotEmpty() -> {
                    val value = numbers[0].toIntOrNull() ?: 0
                    Pair(value, value)
                }
                else -> Pair(0, 0)
            }
        } catch (e: Exception) {
            Pair(0, 0)
        }
    }
}