package com.countwearables.app.ui.viewmodel

import android.app.Application
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.countwearables.app.data.model.User
import com.countwearables.app.data.repository.AuthRepository
import kotlinx.coroutines.launch

/**
 * ViewModel for authentication operations.
 * Handles user registration, login, and session management.
 */
class AuthViewModel(application: Application) : AndroidViewModel(application) {
    
    private val repository: AuthRepository = AuthRepository(application)
    
    private val _loginResult = MutableLiveData<Result<User>>()
    val loginResult: LiveData<Result<User>> = _loginResult
    
    private val _registerResult = MutableLiveData<Result<User>>()
    val registerResult: LiveData<Result<User>> = _registerResult
    
    private val _currentUser = MutableLiveData<User?>()
    val currentUser: LiveData<User?> = _currentUser
    
    private val _isLoading = MutableLiveData<Boolean>()
    val isLoading: LiveData<Boolean> = _isLoading
    
    init {
        // Check if user is already logged in
        if (repository.isLoggedIn()) {
            _currentUser.value = repository.getCurrentUser()
        }
    }
    
    /**
     * Login with username and password
     */
    fun login(username: String, password: String) {
        _isLoading.value = true
        viewModelScope.launch {
            val result = repository.login(username, password)
            _isLoading.value = false
            if (result.isSuccess) {
                _currentUser.value = result.getOrNull()
            }
            _loginResult.value = result
        }
    }
    
    /**
     * Register a new user
     */
    fun register(username: String, password: String, confirmPassword: String) {
        _isLoading.value = true
        viewModelScope.launch {
            // Validate passwords match
            if (password != confirmPassword) {
                _isLoading.value = false
                _registerResult.value = Result.failure(IllegalArgumentException("Passwords do not match"))
                return@launch
            }
            val result = repository.registerUser(username, password)
            _isLoading.value = false
            _registerResult.value = result
        }
    }
    
    /**
     * Logout current user
     */
    fun logout() {
        repository.logout()
        _currentUser.value = null
    }
    
    /**
     * Check if user is logged in
     */
    fun isLoggedIn(): Boolean {
        return repository.isLoggedIn()
    }
    
    /**
     * Get current user ID
     */
    fun getCurrentUserId(): Long {
        return repository.getCurrentUserId()
    }
    
    /**
     * Check if username is taken
     */
    fun checkUsername(username: String) {
        _isLoading.value = true
        viewModelScope.launch {
            val isTaken = repository.isUsernameTaken(username)
            _isLoading.value = false
            // Could expose this as LiveData if needed
        }
    }
}