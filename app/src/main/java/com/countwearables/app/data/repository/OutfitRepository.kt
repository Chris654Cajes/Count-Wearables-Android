package com.countwearables.app.data.repository

import android.content.Context
import com.countwearables.app.data.local.AppDatabase
import com.countwearables.app.data.model.Outfit
import com.countwearables.app.data.model.OutfitItemCrossRef
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext

class OutfitRepository(context: Context) {
    private val database = AppDatabase.getInstance(context)
    private val outfitDao = database.outfitDao()

    fun getAllOutfits(userId: Long) = outfitDao.getAllOutfitsForUser(userId)

    suspend fun saveOutfit(outfit: Outfit, itemIds: List<Long>) = withContext(Dispatchers.IO) {
        val outfitId = if (outfit.id == 0L) {
            outfitDao.insertOutfit(outfit)
        } else {
            outfitDao.updateOutfit(outfit)
            outfitDao.deleteItemsForOutfit(outfit.id)
            outfit.id
        }

        val crossRefs = itemIds.map { OutfitItemCrossRef(outfitId, it) }
        outfitDao.insertOutfitItems(crossRefs)
    }

    suspend fun deleteOutfit(outfit: Outfit) = withContext(Dispatchers.IO) {
        outfitDao.deleteOutfit(outfit)
    }

    fun getItemsForOutfit(outfitId: Long) = outfitDao.getItemsForOutfit(outfitId)
}
