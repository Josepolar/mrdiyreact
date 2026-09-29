# Legacy API networking

Project creator and developer: **PolarDredd**.

`JobApiService.kt` defines the PHP API contracts and response models. `RetrofitClient.kt` builds the Retrofit client. `data/repository/PhpJobRepository.kt` uses these contracts.

Current applicant job screens read published Supabase jobs through `PublishedJobsRepository`. Before modifying this folder, trace current callers so a legacy API change is not mistaken for a change to the active job feed. Keep HTTP logs free of applicant data and credentials.
