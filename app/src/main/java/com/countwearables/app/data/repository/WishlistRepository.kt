package com.countwearables.app.data.repository

import android.content.Context
import com.countwearables.app.data.local.AppDatabase
import com.countwearables.app.data.model.ClothingItem
import com.countwearables.app.data.model.WishlistItem
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext

class WishlistRepository(context: Context) {
    private val database = AppDatabase.getInstance(context)
    private val wishlistDao = database.wishlistDao()
    private val clothingItemDao = database.clothingItemDao()

    fun getWishlist(userId: Long) = wishlistDao.getAllForUser(userId)

    suspend fun addToWishlist(item: WishlistItem) = withContext(Dispatchers.IO) {
        wishlistDao.insert(item)
    }

    suspend fun updateWishlistItem(item: WishlistItem) = withContext(Dispatchers.IO) {
        wishlistDao.update(item)
    }

    suspend fun deleteFromWishlist(item: WishlistItem) = withContext(Dispatchers.IO) {
        wishlistDao.delete(item)
    }

    suspend fun convertToWardrobe(wishlistItem: WishlistItem) = withContext(Dispatchers.IO) {
        val clothingItem = ClothingItem(
            userId = wishlistItem.userId,
            name = wishlistItem.name,
            brand = wishlistItem.brand,
            purchasePrice = wishlistItem.desiredPrice,
            notes = wishlistItem.notes,
            imagePath = wishlistItem.imageUrl // Assuming imageUrl can be treated as path or handled by Glide
        )
        clothingItemDao.insert(clothingItem)
        wishlistDao.delete(wishlistItem)
    }
}
