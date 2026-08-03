package com.countwearables.app.data.local.dao

import androidx.room.*
import com.countwearables.app.data.model.ClothingItem
import kotlinx.coroutines.flow.Flow

@Dao
interface ClothingItemDao {
    
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insert(item: ClothingItem): Long
    
    @Update
    suspend fun update(item: ClothingItem)
    
    @Delete
    suspend fun delete(item: ClothingItem)
    
    @Query("DELETE FROM clothes WHERE id = :itemId")
    suspend fun deleteById(itemId: Long)
    
    @Query("SELECT * FROM clothes WHERE id = :itemId LIMIT 1")
    fun getItemById(itemId: Long): Flow<ClothingItem?>

    @Query("SELECT * FROM clothes WHERE id = :itemId LIMIT 1")
    suspend fun getItemByIdSuspend(itemId: Long): ClothingItem?
    
    @Query("SELECT * FROM clothes WHERE userId = :userId ORDER BY dateAdded DESC")
    fun getAllItemsForUser(userId: Long): Flow<List<ClothingItem>>

    @Query("SELECT * FROM clothes WHERE userId = :userId AND isFavorite = 1 ORDER BY dateAdded DESC")
    fun getFavoritesForUser(userId: Long): Flow<List<ClothingItem>>

    @Query("SELECT * FROM clothes WHERE userId = :userId AND laundryStatus = :status ORDER BY dateAdded DESC")
    fun getItemsByLaundryStatus(userId: Long, status: String): Flow<List<ClothingItem>>

    @Query("SELECT * FROM clothes WHERE userId = :userId AND season = :season ORDER BY dateAdded DESC")
    fun getItemsBySeason(userId: Long, season: String): Flow<List<ClothingItem>>
    
    @Query("SELECT COUNT(*) FROM clothes WHERE userId = :userId")
    fun getItemCountForUser(userId: Long): Flow<Int>
    
    @Query("""
        SELECT * FROM clothes 
        WHERE userId = :userId 
        AND (name LIKE '%' || :query || '%' OR brand LIKE '%' || :query || '%' OR category LIKE '%' || :query || '%')
        ORDER BY dateAdded DESC
    """)
    fun search(userId: Long, query: String): Flow<List<ClothingItem>>
    
    @Query("""
        SELECT * FROM clothes 
        WHERE userId = :userId 
        AND (:nameQuery = '' OR name LIKE '%' || :nameQuery || '%')
        AND (:category = '' OR category = :category)
        AND (:size = '' OR size = :size)
        AND (:color = '' OR color LIKE '%' || :color || '%')
        AND (:season = '' OR season = :season)
        AND (:laundryStatus = '' OR laundryStatus = :laundryStatus)
        ORDER BY dateAdded DESC
    """)
    fun searchAndFilter(
        userId: Long,
        nameQuery: String,
        category: String,
        size: String,
        color: String,
        season: String,
        laundryStatus: String
    ): Flow<List<ClothingItem>>
    
    @Query("SELECT DISTINCT category FROM clothes WHERE userId = :userId ORDER BY category")
    fun getCategoriesForUser(userId: Long): Flow<List<String>>
    
    @Query("SELECT SUM(quantity) FROM clothes WHERE userId = :userId")
    fun getTotalQuantityForUser(userId: Long): Flow<Int?>

    // Analytics queries
    @Query("SELECT * FROM clothes WHERE userId = :userId ORDER BY wearCount DESC LIMIT :limit")
    fun getMostWornItems(userId: Long, limit: Int): Flow<List<ClothingItem>>

    @Query("SELECT * FROM clothes WHERE userId = :userId ORDER BY dateAdded DESC LIMIT :limit")
    fun getRecentlyAddedItems(userId: Long, limit: Int): Flow<List<ClothingItem>>

    @Query("SELECT * FROM clothes WHERE userId = :userId AND lastWornDate IS NOT NULL ORDER BY lastWornDate DESC LIMIT :limit")
    fun getRecentlyWornItems(userId: Long, limit: Int): Flow<List<ClothingItem>>

    @Query("SELECT SUM(purchasePrice) FROM clothes WHERE userId = :userId")
    fun getTotalWardrobeValue(userId: Long): Flow<Double?>

    @Query("SELECT * FROM clothes WHERE userId = :userId ORDER BY purchasePrice DESC LIMIT 1")
    fun getMostExpensiveItem(userId: Long): Flow<ClothingItem?>

    @Query("SELECT * FROM clothes WHERE userId = :userId ORDER BY dateAdded ASC LIMIT 1")
    fun getOldestItem(userId: Long): Flow<ClothingItem?>

    @Query("SELECT category, COUNT(*) as count FROM clothes WHERE userId = :userId GROUP BY category")
    fun getCategoryDistribution(userId: Long): Flow<List<CategoryCount>>
}

data class CategoryCount(
    val category: String,
    val count: Int
)

