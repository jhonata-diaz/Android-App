package com.example.myapplication.network


import retrofit2.http.Body
import retrofit2.http.POST

interface IApiService {

    @POST("login")
    suspend fun loginUser(@Body loginRequestDto: LoginRequestDto): LoginResponseDto
}