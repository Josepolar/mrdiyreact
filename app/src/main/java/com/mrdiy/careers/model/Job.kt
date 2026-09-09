package com.mrdiy.careers.model

data class Job(
    val id: String,
    val title: String,
    val company: String,
    val branch: String,
    val location: String,
    val jobType: String,
    val category: String,
    val level: String,
    val salaryMin: Int,
    val salaryMax: Int,
    val salaryType: String = "monthly",
    val postedAgo: String,
    val aboutRole: String,
    val responsibilities: List<String>,
    val requirements: List<String>,
    val benefits: List<String>,
    val isSaved: Boolean = false
) {
    fun getMonthlySalary(): Int {
        return when (salaryType.lowercase()) {
            "daily" -> salaryMin * 26
            "weekly" -> (salaryMin * 4.33).toInt()
            "yearly" -> salaryMin / 12
            else -> salaryMin
        }
    }

    fun getMonthlySalaryMax(): Int {
        return when (salaryType.lowercase()) {
            "daily" -> salaryMax * 26
            "weekly" -> (salaryMax * 4.33).toInt()
            "yearly" -> salaryMax / 12
            else -> salaryMax
        }
    }
}
