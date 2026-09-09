package com.mrdiy.careers.network

import retrofit2.http.GET
import retrofit2.http.Query

interface JobApiService {

    @GET("api/jobs.php")
    suspend fun getJobs(
        @Query("status") status: String = "Open",
        @Query("page") page: Int = 1,
        @Query("limit") limit: Int = 20
    ): JobsResponse

    @GET("api/job_details.php")
    suspend fun getJobDetails(
        @Query("id") id: String
    ): JobDetailResponse

    @GET("api/search_jobs.php")
    suspend fun searchJobs(
        @Query("q") query: String = "",
        @Query("location") location: String = "",
        @Query("status") status: String = "Open",
        @Query("page") page: Int = 1,
        @Query("limit") limit: Int = 20
    ): SearchJobsResponse
}

data class JobsResponse(
    val success: Boolean,
    val total: Int,
    val page: Int,
    val limit: Int,
    val jobs: List<ApiJob>
)

data class JobDetailResponse(
    val success: Boolean,
    val job: ApiJob? = null,
    val message: String? = null
)

data class SearchJobsResponse(
    val success: Boolean,
    val query: String,
    val location: String,
    val status: String,
    val total: Int,
    val page: Int,
    val limit: Int,
    val jobs: List<ApiJob>
)

data class ApiJob(
    val id: String,
    val title: String,
    val description: String,
    val location: String,
    val salary: String,
    val salary_type: String = "monthly",
    val status: String,
    val created_at: String,
    val posted_ago: String
)