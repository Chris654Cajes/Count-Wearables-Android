package com.countwearables.app.ui.viewmodel

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.asLiveData
import com.countwearables.app.data.repository.AuthRepository
import com.countwearables.app.data.repository.ClothingRepository
import com.countwearables.app.data.model.ClothingItem

class DashboardViewModel(application: Application) : AndroidViewModel(application) {
    private val authRepository = AuthRepository(application)
    private val clothingRepository = ClothingRepository(application)
    
    val userId = authRepository.getCurrentUserId()
    
    val totalItems = clothingRepository.getItemCount(userId).asLiveData()
    val totalQuantity = clothingRepository.getTotalQuantity(userId).asLiveData()
    val recentlyAdded = clothingRepository.getRecentlyAdded(userId, 5).asLiveData()
    val recentlyWorn = clothingRepository.getRecentlyWorn(userId, 5).asLiveData()
    val laundryQueue = clothingRepository.getLaundryQueue(userId).asLiveData()
    val favorites = clothingRepository.getFavorites(userId).asLiveData()
    
    // For seasonal summary, we might need a more complex query or filter
    // Current season based on month
    private fun getCurrentSeason(): String {
        val month = java.util.Calendar.getInstance().get(java.util.Calendar.MONTH)
        return when (month) {
            in 2..4 -> ClothingItem.SEASON_SPRING
            in 5..7 -> ClothingItem.SEASON_SUMMER
            in 8..10 -> ClothingItem.SEASON_AUTUMN
            else -> ClothingItem.SEASON_WINTER
        }
    }
    
    val currentSeasonItems = clothingRepository.searchAndFilter(
        userId = userId,
        season = getCurrentSeason()
    ).asLiveData()
}
