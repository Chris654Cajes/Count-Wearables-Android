package com.countwearables.app.data.repository

import android.content.Context
import com.countwearables.app.data.local.AppDatabase
import com.countwearables.app.data.local.dao.ClothingItemDao
import com.countwearables.app.data.model.ClothingItem
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.withContext

class ClothingRepository(context: Context) {
    
    private val database: AppDatabase = AppDatabase.getInstance(context)
    private val clothingItemDao: ClothingItemDao = database.clothingItemDao()
    
    suspend fun addClothingItem(item: ClothingItem): Result<Long> = withContext(Dispatchers.IO) {
        try {
            val id = clothingItemDao.insert(item)
            Result.success(id)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }
    
    suspend fun updateClothingItem(item: ClothingItem): Result<Unit> = withContext(Dispatchers.IO) {
        try {
            clothingItemDao.update(item)
            Result.success(Unit)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }
    
    suspend fun deleteClothingItem(item: ClothingItem): Result<Unit> = withContext(Dispatchers.IO) {
        try {
            clothingItemDao.delete(item)
            Result.success(Unit)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }
    
    fun getClothingItemById(itemId: Long): Flow<ClothingItem?> = clothingItemDao.getItemById(itemId)
    
    fun getAllClothingItemsForUser(userId: Long): Flow<List<ClothingItem>> = clothingItemDao.getAllItemsForUser(userId)

    fun getFavorites(userId: Long): Flow<List<ClothingItem>> = clothingItemDao.getFavoritesForUser(userId)

    fun getLaundryQueue(userId: Long): Flow<List<ClothingItem>> = clothingItemDao.getItemsByLaundryStatus(userId, ClothingItem.LAUNDRY_DIRTY)
    
    fun searchAndFilter(
        userId: Long,
        nameQuery: String = "",
        category: String = "",
        size: String = "",
        color: String = "",
        season: String = "",
        laundryStatus: String = ""
    ): Flow<List<ClothingItem>> = clothingItemDao.searchAndFilter(userId, nameQuery, category, size, color, season, laundryStatus)

    suspend fun markItemAsWorn(itemId: Long) = withContext(Dispatchers.IO) {
        val item = clothingItemDao.getItemByIdSuspend(itemId)
        item?.let {
            val updated = it.copy(
                lastWornDate = System.currentTimeMillis(),
                wearCount = it.wearCount + 1
            )
            clothingItemDao.update(updated)
        }
    }

    suspend fun updateLaundryStatus(itemId: Long, status: String) = withContext(Dispatchers.IO) {
        val item = clothingItemDao.getItemByIdSuspend(itemId)
        item?.let {
            val updated = it.copy(laundryStatus = status)
            clothingItemDao.update(updated)
        }
    }

    suspend fun toggleFavorite(itemId: Long) = withContext(Dispatchers.IO) {
        val item = clothingItemDao.getItemByIdSuspend(itemId)
        item?.let {
            val updated = it.copy(isFavorite = !it.isFavorite)
            clothingItemDao.update(updated)
        }
    }

    fun getRecentlyAdded(userId: Long, limit: Int = 5) = clothingItemDao.getRecentlyAddedItems(userId, limit)
    
    fun getRecentlyWorn(userId: Long, limit: Int = 5) = clothingItemDao.getRecentlyWornItems(userId, limit)
    
    fun getMostWorn(userId: Long, limit: Int = 5) = clothingItemDao.getMostWornItems(userId, limit)

    fun getItemCount(userId: Long) = clothingItemDao.getItemCountForUser(userId)
    
    fun getTotalQuantity(userId: Long) = clothingItemDao.getTotalQuantityForUser(userId)

    fun getCategories(userId: Long) = clothingItemDao.getCategoriesForUser(userId)

    suspend fun findSimilarItems(userId: Long, item: ClothingItem): List<ClothingItem> = withContext(Dispatchers.IO) {
        val allItems = clothingItemDao.getAllItemsForUser(userId).first()
        allItems.filter { existing ->
            existing.id != item.id && (
                (existing.name.equals(item.name, true) && existing.brand.equals(item.brand, true)) ||
                (existing.name.equals(item.name, true) && existing.color.equals(item.color, true)) ||
                (existing.brand.equals(item.brand, true) && existing.category == item.category)
            )
        }
    }
}
