package com.countwearables.app.data.local.dao

import androidx.room.*
import com.countwearables.app.data.model.WishlistItem
import kotlinx.coroutines.flow.Flow

@Dao
interface WishlistDao {
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insert(item: WishlistItem): Long

    @Update
    suspend fun update(item: WishlistItem)

    @Delete
    suspend fun delete(item: WishlistItem)

    @Query("SELECT * FROM wishlist_items WHERE userId = :userId ORDER BY priority DESC, dateAdded DESC")
    fun getAllForUser(userId: Long): Flow<List<WishlistItem>>

    @Query("SELECT * FROM wishlist_items WHERE id = :itemId LIMIT 1")
    suspend fun getById(itemId: Long): WishlistItem?
}
