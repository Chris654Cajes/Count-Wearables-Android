package com.countwearables.app.data.repository

import android.content.Context
import com.countwearables.app.data.local.AppDatabase
import com.countwearables.app.data.model.ClothingItem
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext

/**
 * Repository for clothing item operations.
 * Handles CRUD operations and search/filter functionality.
 */
class ClothingRepository(context: Context) {
    
    private val database: AppDatabase = AppDatabase.getInstance(context)
    
    /**
     * Add a new clothing item
     */
    suspend fun addClothingItem(item: ClothingItem): Result<Long> = withContext(Dispatchers.IO) {
        try {
            if (item.name.isBlank()) {
                Result.failure(IllegalArgumentException("Item name cannot be empty"))
            } else {
                val id = database.insertClothingItem(item)
                if (id > 0) Result.success(id) else Result.failure(Exception("Failed to add item"))
            }
        } catch (e: Exception) {
            Result.failure(e)
        }
    }
    
    /**
     * Update an existing clothing item
     */
    suspend fun updateClothingItem(item: ClothingItem): Result<Long> = withContext(Dispatchers.IO) {
        try {
            if (item.name.isBlank()) {
                Result.failure(IllegalArgumentException("Item name cannot be empty"))
            } else {
                val rows = database.updateClothingItem(item).toLong()
                Result.success(rows)
            }
        } catch (e: Exception) {
            Result.failure(e)
        }
    }
    
    /**
     * Delete a clothing item
     */
    suspend fun deleteClothingItem(itemId: Long): Result<Unit> = withContext(Dispatchers.IO) {
        try {
            database.deleteClothingItem(itemId)
            Result.success(Unit)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }
    
    /**
     * Get a clothing item by ID
     */
    suspend fun getClothingItemById(itemId: Long): ClothingItem? = withContext(Dispatchers.IO) {
        database.getClothingItemById(itemId)
    }
    
    /**
     * Get all clothing items for a user
     */
    suspend fun getAllClothingItemsForUser(userId: Long): List<ClothingItem> = withContext(Dispatchers.IO) {
        database.getAllClothingItemsForUser(userId)
    }
    
    /**
     * Search clothing items by name or category
     */
    suspend fun searchClothingItems(userId: Long, query: String): List<ClothingItem> = withContext(Dispatchers.IO) {
        database.searchClothingItems(userId, query)
    }
    
    /**
     * Filter clothing items by category
     */
    suspend fun filterByCategory(userId: Long, category: String): List<ClothingItem> = withContext(Dispatchers.IO) {
        database.filterByCategory(userId, category)
    }
    
    /**
     * Filter clothing items by size
     */
    suspend fun filterBySize(userId: Long, size: String): List<ClothingItem> = withContext(Dispatchers.IO) {
        database.filterBySize(userId, size)
    }
    
    /**
     * Filter clothing items by color
     */
    suspend fun filterByColor(userId: Long, color: String): List<ClothingItem> = withContext(Dispatchers.IO) {
        database.filterByColor(userId, color)
    }
    
    /**
     * Get count of clothing items for a user
     */
    suspend fun getClothingItemCountForUser(userId: Long): Int = withContext(Dispatchers.IO) {
        database.getClothingItemCountForUser(userId)
    }
    
    /**
     * Get total quantity of all items for a user
     */
    suspend fun getTotalQuantityForUser(userId: Long): Int = withContext(Dispatchers.IO) {
        database.getTotalQuantityForUser(userId)
    }
}