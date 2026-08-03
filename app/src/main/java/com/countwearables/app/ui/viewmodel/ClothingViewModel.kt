package com.countwearables.app.ui.viewmodel

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.asLiveData
import androidx.lifecycle.viewModelScope
import com.countwearables.app.data.model.ClothingItem
import com.countwearables.app.data.repository.ClothingRepository
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.flatMapLatest
import kotlinx.coroutines.flow.flowOf
import kotlinx.coroutines.launch

class ClothingViewModel(application: Application) : AndroidViewModel(application) {
    
    private val repository: ClothingRepository = ClothingRepository(application)
    
    private val _itemResult = MutableLiveData<Result<Long>>()
    val itemResult: LiveData<Result<Long>> = _itemResult
    
    private val _deleteResult = MutableLiveData<Result<Unit>>()
    val deleteResult: LiveData<Result<Unit>> = _deleteResult

    private val _clothingItems = MutableLiveData<List<ClothingItem>>()
    val clothingItems: LiveData<List<ClothingItem>> = _clothingItems

    fun loadAllItems(userId: Long) {
        viewModelScope.launch {
            repository.getAllClothingItemsForUser(userId).collect {
                _clothingItems.value = it
            }
        }
    }
    
    fun addItem(item: ClothingItem) {
        viewModelScope.launch {
            _itemResult.value = repository.addClothingItem(item)
        }
    }
    
    fun updateItem(item: ClothingItem) {
        viewModelScope.launch {
            repository.updateClothingItem(item)
        }
    }
    
    fun deleteItem(item: ClothingItem) {
        viewModelScope.launch {
            _deleteResult.value = repository.deleteClothingItem(item)
        }
    }

    private val _currentItemId = MutableStateFlow<Long?>(null)
    
    @OptIn(ExperimentalCoroutinesApi::class)
    val currentItem: LiveData<ClothingItem?> = _currentItemId.flatMapLatest { id ->
        if (id == null) flowOf(null)
        else repository.getClothingItemById(id)
    }.asLiveData()

    fun setItemId(id: Long) {
        _currentItemId.value = id
    }

    fun markAsWorn(itemId: Long) {
        viewModelScope.launch {
            repository.markItemAsWorn(itemId)
        }
    }

    fun toggleFavorite(itemId: Long) {
        viewModelScope.launch {
            repository.toggleFavorite(itemId)
        }
    }

    fun updateLaundryStatus(itemId: Long, status: String) {
        viewModelScope.launch {
            repository.updateLaundryStatus(itemId, status)
        }
    }

    fun searchItems(userId: Long, query: String) {
        viewModelScope.launch {
            repository.searchAndFilter(userId, nameQuery = query).collect {
                _clothingItems.value = it
            }
        }
    }
}
