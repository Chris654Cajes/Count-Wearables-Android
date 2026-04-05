package com.countwearables.app.data.local.dao

import androidx.room.*
import com.countwearables.app.data.model.ClothingItem
import kotlinx.coroutines.flow.Flow

/**
 * Data Access Object for ClothingItem entity.
 * Provides CRUD operations and search/filter functionality.
 */
@Dao
interface ClothingItemDao {
    
    /**
     * Insert a new clothing item
     * @param item The item to insert
     * @return The row ID of the inserted item
     */
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insert(item: ClothingItem): Long
    
    /**
     * Update an existing clothing item
     * @param item The item to update
     */
    @Update
    suspend fun update(item: ClothingItem)
    
    /**
     * Delete a clothing item
     * @param item The item to delete
     */
    @Delete
    suspend fun delete(item: ClothingItem)
    
    /**
     * Delete a clothing item by ID
     * @param itemId The ID of the item to delete
     */
    @Query("DELETE FROM clothing_items WHERE id = :itemId")
    suspend fun deleteById(itemId: Long)
    
    /**
     * Get a clothing item by its ID
     * @param itemId The ID of the item
     * @return Flow of ClothingItem or null if not found
     */
    @Query("SELECT * FROM clothing_items WHERE id = :itemId LIMIT 1")
    fun getItemById(itemId: Long): Flow<ClothingItem?>
    
    /**
     * Get all clothing items for a specific user
     * @param userId The user's ID
     * @return Flow of list of clothing items, sorted by date added (newest first)
     */
    @Query("SELECT * FROM clothing_items WHERE userId = :userId ORDER BY dateAdded DESC")
    fun getAllItemsForUser(userId: Long): Flow<List<ClothingItem>>
    
    /**
     * Get count of all clothing items for a user
     * @param userId The user's ID
     * @return Total number of items
     */
    @Query("SELECT COUNT(*) FROM clothing_items WHERE userId = :userId")
    fun getItemCountForUser(userId: Long): Flow<Int>
    
    /**
     * Search clothing items by name for a specific user
     * @param userId The user's ID
     * @param query The search query
     * @return Flow of matching clothing items
     */
    @Query("""
        SELECT * FROM clothing_items 
        WHERE userId = :userId AND name LIKE '%' || :query || '%' 
        ORDER BY dateAdded DESC
    """)
    fun searchByName(userId: Long, query: String): Flow<List<ClothingItem>>
    
    /**
     * Filter clothing items by category for a specific user
     * @param userId The user's ID
     * @param category The category to filter by
     * @return Flow of matching clothing items
     */
    @Query("""
        SELECT * FROM clothing_items 
        WHERE userId = :userId AND category = :category 
        ORDER BY dateAdded DESC
    """)
    fun filterByCategory(userId: Long, category: String): Flow<List<ClothingItem>>
    
    /**
     * Search and filter clothing items by name, category, size, and color
     * @param userId The user's ID
     * @param nameQuery The name search query (can be empty)
     * @param category The category filter (can be empty for all categories)
     * @param size The size filter (can be empty for all sizes)
     * @param color The color filter (can be empty for all colors)
     * @return Flow of matching clothing items
     */
    @Query("""
        SELECT * FROM clothing_items 
        WHERE userId = :userId 
        AND (:nameQuery = '' OR name LIKE '%' || :nameQuery || '%')
        AND (:category = '' OR category = :category)
        AND (:size = '' OR size = :size)
        AND (:color = '' OR color LIKE '%' || :color || '%')
        ORDER BY dateAdded DESC
    """)
    fun searchAndFilter(
        userId: Long,
        nameQuery: String,
        category: String,
        size: String,
        color: String
    ): Flow<List<ClothingItem>>
    
    /**
     * Get all unique categories for a user's wardrobe
     * @param userId The user's ID
     * @return Flow of list of category strings
     */
    @Query("SELECT DISTINCT category FROM clothing_items WHERE userId = :userId ORDER BY category")
    fun getCategoriesForUser(userId: Long): Flow<List<String>>
    
    /**
     * Get all unique sizes for a user's wardrobe
     * @param userId The user's ID
     * @return Flow of list of size strings
     */
    @Query("SELECT DISTINCT size FROM clothing_items WHERE userId = :userId AND size != '' ORDER BY size")
    fun getSizesForUser(userId: Long): Flow<List<String>>
    
    /**
     * Get all unique colors for a user's wardrobe
     * @param userId The user's ID
     * @return Flow of list of color strings
     */
    @Query("SELECT DISTINCT color FROM clothing_items WHERE userId = :userId AND color != '' ORDER BY color")
    fun getColorsForUser(userId: Long): Flow<List<String>>
    
    /**
     * Get total quantity of all items for a user
     * @param userId The user's ID
     * @return Sum of all quantities
     */
    @Query("SELECT SUM(quantity) FROM clothing_items WHERE userId = :userId")
    fun getTotalQuantityForUser(userId: Long): Flow<Int?>
    
    /**
     * Delete all clothing items for a user (cascade on user delete)
     * @param userId The user's ID
     */
    @Query("DELETE FROM clothing_items WHERE userId = :userId")
    suspend fun deleteAllForUser(userId: Long)
}