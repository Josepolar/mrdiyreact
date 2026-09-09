package com.mrdiy.careers.model

import kotlinx.serialization.Serializable

@Serializable
data class Education(
    val degree: String = "",
    val school: String = "",
    val year: String = ""
)
