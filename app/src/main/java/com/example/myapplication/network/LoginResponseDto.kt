package com.example.myapplication.network

import com.squareup.moshi.Json
import com.squareup.moshi.JsonClass


@JsonClass(generateAdapter = true)
data class LoginResponseDto(
    @Json(name = "token")
    val token: String
)
