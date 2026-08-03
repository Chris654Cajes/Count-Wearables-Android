package com.countwearables.app.ui.viewmodel

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.asLiveData
import com.countwearables.app.data.local.AppDatabase
import com.countwearables.app.data.repository.AuthRepository
import com.countwearables.app.data.repository.ClothingRepository

class StatsViewModel(application: Application) : AndroidViewModel(application) {
    private val authRepository = AuthRepository(application)
    private val clothingRepository = ClothingRepository(application)
    private val dao = AppDatabase.getInstance(application).clothingItemDao()
    
    val userId = authRepository.getCurrentUserId()
    
    val totalValue = dao.getTotalWardrobeValue(userId).asLiveData()
    val mostExpensive = dao.getMostExpensiveItem(userId).asLiveData()
    val oldestItem = dao.getOldestItem(userId).asLiveData()
    val categoryDistribution = dao.getCategoryDistribution(userId).asLiveData()
    val mostWorn = dao.getMostWornItems(userId, 5).asLiveData()
}
