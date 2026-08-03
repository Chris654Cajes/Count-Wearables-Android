package com.countwearables.app.ui.viewmodel

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.asLiveData
import androidx.lifecycle.viewModelScope
import com.countwearables.app.data.model.Outfit
import com.countwearables.app.data.repository.OutfitRepository
import kotlinx.coroutines.launch

class OutfitViewModel(application: Application) : AndroidViewModel(application) {
    private val repository = OutfitRepository(application)

    fun getOutfits(userId: Long) = repository.getAllOutfits(userId).asLiveData()

    fun saveOutfit(outfit: Outfit, itemIds: List<Long>) {
        viewModelScope.launch {
            repository.saveOutfit(outfit, itemIds)
        }
    }

    fun deleteOutfit(outfit: Outfit) {
        viewModelScope.launch {
            repository.deleteOutfit(outfit)
        }
    }

    fun getItemsForOutfit(outfitId: Long) = repository.getItemsForOutfit(outfitId).asLiveData()
}
