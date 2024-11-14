package com.example.myapplication.network

import android.util.Log
import com.example.myapplication.LoginRegister.ApiResponse
import kotlinx.coroutines.delay

class UserRemoteDataSource(
    private val retrofitService: RetrofitBuilder
) {
    suspend fun loginUser(email: String, password: String): ApiResponse<LoginResponseDto> {
        return try {
            val requestDto = LoginRequestDto(email, password)
            Log.d("Login", "Request DTO: $requestDto") // Muestra la solicitud
            val response = retrofitService.apiService.loginUser(requestDto)
            Log.d("Login", "Response: $response") // Muestra la respuesta
            ApiResponse.Success(response)
        } catch (e: Exception) {
            ApiResponse.Error(e.message ?: "Unknown error")
        }
    }


     suspend fun logout(token: String): ApiResponse<Boolean> {
        delay(500) // Simulate network delay
        return ApiResponse.Success(true)
    }

}