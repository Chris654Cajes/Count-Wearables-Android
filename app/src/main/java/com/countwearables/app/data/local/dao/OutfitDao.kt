package com.countwearables.app.data.local.dao

import androidx.room.*
import com.countwearables.app.data.model.ClothingItem
import com.countwearables.app.data.model.Outfit
import com.countwearables.app.data.model.OutfitItemCrossRef
import kotlinx.coroutines.flow.Flow

@Dao
interface OutfitDao {
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertOutfit(outfit: Outfit): Long

    @Update
    suspend fun updateOutfit(outfit: Outfit)

    @Delete
    suspend fun deleteOutfit(outfit: Outfit)

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertOutfitItems(crossRefs: List<OutfitItemCrossRef>)

    @Query("DELETE FROM outfit_item_cross_ref WHERE outfitId = :outfitId")
    suspend fun deleteItemsForOutfit(outfitId: Long)

    @Transaction
    @Query("SELECT * FROM outfits WHERE userId = :userId ORDER BY createdAt DESC")
    fun getAllOutfitsForUser(userId: Long): Flow<List<Outfit>>

    @Transaction
    @Query("SELECT * FROM outfits WHERE id = :outfitId LIMIT 1")
    suspend fun getOutfitById(outfitId: Long): Outfit?

    @Transaction
    @Query("""
        SELECT clothes.* FROM clothes
        INNER JOIN outfit_item_cross_ref ON clothes.id = outfit_item_cross_ref.clothingItemId
        WHERE outfit_item_cross_ref.outfitId = :outfitId
    """)
    fun getItemsForOutfit(outfitId: Long): Flow<List<ClothingItem>>
}
