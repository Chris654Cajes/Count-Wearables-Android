package com.countwearables.app.data.repository

import android.content.Context
import android.content.SharedPreferences
import com.countwearables.app.data.local.AppDatabase
import com.countwearables.app.data.local.dao.UserDao
import com.countwearables.app.data.model.User
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext

/**
 * Repository for authentication operations using Room.
 */
class AuthRepository(context: Context) {
    
    private val database: AppDatabase = AppDatabase.getInstance(context)
    private val userDao: UserDao = database.userDao()
    private val prefs: SharedPreferences = context.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE)
    
    companion object {
        private const val PREFS_NAME = "auth_prefs"
        private const val KEY_USER_ID = "user_id"
        private const val KEY_USERNAME = "username"
        private const val KEY_IS_LOGGED_IN = "is_logged_in"
    }
    
    suspend fun registerUser(username: String, password: String): Result<User> = withContext(Dispatchers.IO) {
        try {
            if (username.isBlank()) {
                Result.failure(IllegalArgumentException("Username cannot be empty"))
            } else if (password.length < 6) {
                Result.failure(IllegalArgumentException("Password must be at least 6 characters"))
            } else if (userDao.isUsernameTaken(username) > 0) {
                Result.failure(IllegalArgumentException("Username already exists"))
            } else {
                val user = User(username = username, password = password)
                val id = userDao.insert(user)
                if (id > 0L) {
                    Result.success(User(id = id, username = username, password = password))
                } else {
                    Result.failure(Exception("Registration failed"))
                }
            }
        } catch (e: Exception) {
            Result.failure(e)
        }
    }
    
    suspend fun login(username: String, password: String): Result<User> = withContext(Dispatchers.IO) {
        try {
            if (username.isBlank() || password.isBlank()) {
                Result.failure(IllegalArgumentException("Username and password are required"))
            } else {
                val user = userDao.validateCredentials(username, password)
                if (user != null) {
                    saveSession(user)
                    Result.success(user)
                } else {
                    Result.failure(Exception("Invalid credentials"))
                }
            }
        } catch (e: Exception) {
            Result.failure(e)
        }
    }
    
    fun logout() {
        prefs.edit().clear().apply()
    }
    
    fun isLoggedIn(): Boolean {
        return prefs.getBoolean(KEY_IS_LOGGED_IN, false)
    }
    
    fun getCurrentUser(): User? {
        if (!isLoggedIn()) return null
        val userId = prefs.getLong(KEY_USER_ID, -1)
        val username = prefs.getString(KEY_USERNAME, null) ?: return null
        return User(id = userId, username = username, password = "")
    }
    
    fun getCurrentUserId(): Long {
        return prefs.getLong(KEY_USER_ID, -1)
    }
    
    private fun saveSession(user: User) {
        prefs.edit().apply {
            putLong(KEY_USER_ID, user.id)
            putString(KEY_USERNAME, user.username)
            putBoolean(KEY_IS_LOGGED_IN, true)
            apply()
        }
    }
    
    suspend fun isUsernameTaken(username: String): Boolean = withContext(Dispatchers.IO) {
        userDao.isUsernameTaken(username) > 0
    }
}
