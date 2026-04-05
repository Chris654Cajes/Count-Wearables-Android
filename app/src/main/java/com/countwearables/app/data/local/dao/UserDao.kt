package com.countwearables.app.data.local.dao

import androidx.room.*
import com.countwearables.app.data.model.User
import kotlinx.coroutines.flow.Flow

/**
 * Data Access Object for User entity.
 * Provides methods for user authentication and management.
 */
@Dao
interface UserDao {
    
    /**
     * Insert a new user into the database
     * @param user The user to insert
     * @return The row ID of the inserted user
     */
    @Insert(onConflict = OnConflictStrategy.ABORT)
    suspend fun insert(user: User): Long
    
    /**
     * Get a user by their username
     * @param username The username to search for
     * @return Flow of User or null if not found
     */
    @Query("SELECT * FROM users WHERE username = :username LIMIT 1")
    fun getUserByUsername(username: String): Flow<User?>
    
    /**
     * Get a user by their ID
     * @param userId The user ID to search for
     * @return Flow of User or null if not found
     */
    @Query("SELECT * FROM users WHERE id = :userId LIMIT 1")
    fun getUserById(userId: Long): Flow<User?>
    
    /**
     * Validate user credentials for login
     * @param username The username to check
     * @param password The password to verify
     * @return The User if credentials are valid, null otherwise
     */
    @Query("SELECT * FROM users WHERE username = :username AND password = :password LIMIT 1")
    suspend fun validateCredentials(username: String, password: String): User?
    
    /**
     * Check if a username already exists
     * @param username The username to check
     * @return The count of users with that username
     */
    @Query("SELECT COUNT(*) FROM users WHERE username = :username")
    suspend fun isUsernameTaken(username: String): Int
    
    /**
     * Delete a user from the database
     * @param user The user to delete
     */
    @Delete
    suspend fun delete(user: User)
    
    /**
     * Delete a user by ID
     * @param userId The ID of the user to delete
     */
    @Query("DELETE FROM users WHERE id = :userId")
    suspend fun deleteById(userId: Long)
    
    /**
     * Get all users (for debugging purposes)
     * @return Flow of list of all users
     */
    @Query("SELECT * FROM users ORDER BY createdAt DESC")
    fun getAllUsers(): Flow<List<User>>
}