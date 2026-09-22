package com.mrdiy.careers.data.ml

/** Skills from retail, store operations, logistics and office roles as well as IT. */
object ResumeSkills {
    private val vocabulary = listOf(
        "customer service", "cash handling", "cashier", "point of sale", "POS",
        "inventory", "stock management", "merchandising", "sales", "retail",
        "leadership", "teamwork", "communication", "supervision", "staff scheduling",
        "warehouse", "logistics", "forklift", "picking", "packing", "delivery",
        "recruitment", "payroll", "human resources", "accounting", "bookkeeping",
        "Excel", "Microsoft Office", "data entry", "administration", "training",
        "Kotlin", "Java", "Android", "Python", "JavaScript", "React", "SQL",
        "Git", "Docker", "AWS", "Firebase", "machine learning", "data analysis",
        "HTML", "CSS", "Node", "Flutter"
    )

    fun extract(text: String): List<String> = vocabulary.filter { skill ->
        Regex("(?<![\\p{L}\\p{N}])${Regex.escape(skill)}(?![\\p{L}\\p{N}])", RegexOption.IGNORE_CASE)
            .containsMatchIn(text)
    }
}
