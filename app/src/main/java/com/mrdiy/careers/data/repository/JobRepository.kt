package com.mrdiy.careers.data.repository

import com.mrdiy.careers.model.Job

object JobRepository {
    private val sampleJobs = listOf(
        Job(
            id = "1",
            title = "Store Supervisor",
            company = "MR.D.I.Y. Philippines",
            branch = "SM Marikina",
            location = "Marikina",
            jobType = "Full-time",
            category = "Retail",
            level = "Mid-level",
            salaryMin = 25000,
            salaryMax = 32000,
            postedAgo = "2d ago",
            aboutRole = "Lead daily store operations and drive sales performance across all product categories.",
            responsibilities = listOf(
                "Oversee day-to-day store operations and staff scheduling",
                "Monitor sales targets and implement action plans",
                "Conduct product training and performance coaching",
                "Handle customer escalations and ensure satisfaction",
                "Maintain inventory accuracy and visual merchandising"
            ),
            requirements = listOf(
                "Bachelor's degree in Business or related field",
                "Minimum 2 years supervisory experience in retail",
                "Strong leadership and communication skills",
                "Proficient in MS Office and POS systems"
            ),
            benefits = listOf(
                "SSS, PhilHealth, Pag-IBIG contributions",
                "13th month pay & performance bonuses",
                "Employee discount on all products",
                "Career growth & promotion opportunities"
            )
        ),
        Job(
            id = "2",
            title = "Sales Associate",
            company = "MR.D.I.Y. Philippines",
            branch = "Choice Market Ortigas",
            location = "Ortigas, Pasig City",
            jobType = "Full-time",
            category = "Retail",
            level = "Entry Level",
            salaryMin = 17000,
            salaryMax = 22000,
            postedAgo = "3d ago",
            aboutRole = "Assist customers in finding products and ensure a pleasant shopping experience.",
            responsibilities = listOf(
                "Assist customers on the sales floor",
                "Process transactions at POS"
            ),
            requirements = listOf("High school diploma or equivalent", "Good communication skills"),
            benefits = listOf("SSS, PhilHealth, Pag-IBIG", "13th month pay")
        ),
        Job(
            id = "3",
            title = "Warehouse Operations Lead",
            company = "MR.D.I.Y. Philippines",
            branch = "Pasig Hub",
            location = "Pasig",
            jobType = "Full-time",
            category = "Logistics",
            level = "Mid-level",
            salaryMin = 30000,
            salaryMax = 38000,
            postedAgo = "5d ago",
            aboutRole = "Oversee incoming and outgoing shipments and manage warehouse team.",
            responsibilities = listOf(
                "Manage warehouse team and daily operations",
                "Ensure accurate inventory records"
            ),
            requirements = listOf(
                "3+ years warehouse experience",
                "Forklift certification is a plus"
            ),
            benefits = listOf("SSS, PhilHealth, Pag-IBIG", "13th month pay", "Meal allowance")
        ),
        Job(
            id = "4",
            title = "HR Recruitment Officer",
            company = "MR.D.I.Y. Philippines",
            branch = "Head Office, Eastwood Global Plaza Building",
            location = "Eastwood City, Quezon City",
            jobType = "Full-time",
            category = "Corporate",
            level = "Mid-level",
            salaryMin = 28000,
            salaryMax = 35000,
            postedAgo = "1w ago",
            aboutRole = "Manage end-to-end recruitment for store and corporate positions.",
            responsibilities = listOf(
                "Source and screen candidates",
                "Conduct initial interviews",
                "Coordinate onboarding"
            ),
            requirements = listOf(
                "Bachelor's degree in HR or Psychology",
                "2+ years recruitment experience"
            ),
            benefits = listOf("SSS, PhilHealth, Pag-IBIG", "13th month pay", "HMO coverage")
        )
    )

    fun getJobById(id: String): Job? = sampleJobs.find { it.id == id }

    fun getJobs(): List<Job> = sampleJobs
}