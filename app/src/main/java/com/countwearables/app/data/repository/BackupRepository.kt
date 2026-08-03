package com.countwearables.app.data.repository

import android.content.Context
import com.countwearables.app.data.local.AppDatabase
import com.countwearables.app.data.model.ClothingItem
import com.google.gson.Gson
import com.google.gson.reflect.TypeToken
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.withContext
import java.io.File

class BackupRepository(context: Context) {
    private val database = AppDatabase.getInstance(context)
    private val clothingItemDao = database.clothingItemDao()
    private val gson = Gson()

    suspend fun exportToJson(userId: Long, file: File) = withContext(Dispatchers.IO) {
        val items = clothingItemDao.getAllItemsForUser(userId).first()
        val json = gson.toJson(items)
        file.writeText(json)
    }

    suspend fun importFromJson(userId: Long, json: String) = withContext(Dispatchers.IO) {
        val type = object : TypeToken<List<ClothingItem>>() {}.type
        val items: List<ClothingItem> = gson.fromJson(json, type)
        items.forEach { item ->
            clothingItemDao.insert(item.copy(id = 0, userId = userId))
        }
    }
}
