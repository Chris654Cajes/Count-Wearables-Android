package com.countwearables.app.data.local.dao

import androidx.room.*
import com.countwearables.app.data.model.User
import kotlinx.coroutines.flow.Flow

@Dao
interface UserDao {
    @Insert(onConflict = OnConflictStrategy.ABORT)
    suspend fun insert(user: User): Long
    
    @Query("SELECT * FROM users WHERE username = :username LIMIT 1")
    fun getUserByUsername(username: String): Flow<User?>
    
    @Query("SELECT * FROM users WHERE id = :userId LIMIT 1")
    fun getUserById(userId: Long): Flow<User?>
    
    @Query("SELECT * FROM users WHERE username = :username AND password = :password LIMIT 1")
    suspend fun validateCredentials(username: String, password: String): User?
    
    @Query("SELECT COUNT(*) FROM users WHERE username = :username")
    suspend fun isUsernameTaken(username: String): Int
    
    @Delete
    suspend fun delete(user: User)
}
