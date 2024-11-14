package com.example.myapplication.LoginRegister

import android.app.Application
import android.content.Context
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.example.myapplication.LoginRegister.session.Prefs
import com.example.myapplication.LoginRegister.session.UserSession
import com.example.myapplication.network.LoginResponseDto
import com.example.myapplication.network.RetrofitBuilder
import com.example.myapplication.network.UserRemoteDataSource

import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.launch


sealed class ApiResponse<out T> {
    data class Success<T>(val data: T) : ApiResponse<T>()
    data class Error(val message: String) : ApiResponse<Nothing>()
    object Loading : ApiResponse<Nothing>()
}



sealed class LoginUiState {
    object Initial : LoginUiState()
    object Loading : LoginUiState()
    data class Success(val user: LoginResponseDto) : LoginUiState()
    data class Error(val message: String) : LoginUiState()
}

class UserSessionViewModel(application: Application) : AndroidViewModel(application) {


    private val _uiState = MutableStateFlow<LoginUiState>(LoginUiState.Initial)
    val uiState: StateFlow<LoginUiState> = _uiState
    private var currentUser: LoginResponseDto? = null

    private val prefs = Prefs(application)


    val userremotedat :UserRemoteDataSource = UserRemoteDataSource(RetrofitBuilder)

    fun login(email: String, password: String) {

        viewModelScope.launch {

            _uiState.value = LoginUiState.Loading
            when (val response = userremotedat.loginUser(email, password)) {
                is ApiResponse.Success -> {
                    prefs.token = response.data.token
                    prefs.user = UserSession("Name", "LastName", "AvatarURL") // Usa los datos recibidos si existen

                    currentUser = response.data
                    _uiState.value = LoginUiState.Success(response.data)
                }
                is ApiResponse.Error -> {
                    _uiState.value = LoginUiState.Error(response.message)
                }
                is ApiResponse.Loading -> {
                    _uiState.value = LoginUiState.Loading
                }
            }
        }


    }


    fun logout() {
        viewModelScope.launch {
            _uiState.value = LoginUiState.Loading

            prefs.token?.let { token ->
                when (userremotedat.logout(token)) {
                    is ApiResponse.Success -> {
                        currentUser = null
                        _uiState.value = LoginUiState.Initial
                    }
                    is ApiResponse.Error -> {
                        // Aún así cerramos la sesión localmente
                        currentUser = null
                        _uiState.value = LoginUiState.Initial
                    }
                    is ApiResponse.Loading -> {
                        _uiState.value = LoginUiState.Loading
                    }
                }
            } ?: run {
                // Si no hay usuario actual, simplemente regresamos al estado inicial
                _uiState.value = LoginUiState.Initial
            }
        }
    }


}