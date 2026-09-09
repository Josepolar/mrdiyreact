package com.mrdiy.careers.model

data class SavedJob(
    val id: String,
    val userId: String,
    val jobId: String,
    val savedAt: String
)

data class SavedJobWithJob(
    val savedJob: SavedJob,
    val job: Job
)