package com.countwearables.app.ui.viewmodel

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.viewModelScope
import com.countwearables.app.data.model.ClothingItem
import com.countwearables.app.data.repository.ClothingRepository
import kotlinx.coroutines.launch

/**
 * ViewModel for clothing item operations.
 * Handles CRUD operations and search/filter functionality.
 */
class ClothingViewModel(application: Application) : AndroidViewModel(application) {
    
    private val repository: ClothingRepository = ClothingRepository(application)
    
    private val _clothingItems = MutableLiveData<List<ClothingItem>>()
    val clothingItems: LiveData<List<ClothingItem>> = _clothingItems
    
    private val _itemResult = MutableLiveData<Result<Long>>()
    val itemResult: LiveData<Result<Long>> = _itemResult
    
    private val _deleteResult = MutableLiveData<Result<Unit>>()
    val deleteResult: LiveData<Result<Unit>> = _deleteResult
    
    private val _isLoading = MutableLiveData<Boolean>()
    val isLoading: LiveData<Boolean> = _isLoading
    
    private val _currentItem = MutableLiveData<ClothingItem?>()
    val currentItem: LiveData<ClothingItem?> = _currentItem
    
    /**
     * Load all clothing items for a user
     */
    fun loadAllItems(userId: Long) {
        _isLoading.value = true
        viewModelScope.launch {
            val items = repository.getAllClothingItemsForUser(userId)
            _isLoading.value = false
            _clothingItems.value = items
        }
    }
    
    /**
     * Add a new clothing item
     */
    fun addItem(item: ClothingItem) {
        _isLoading.value = true
        viewModelScope.launch {
            val result = repository.addClothingItem(item)
            _isLoading.value = false
            _itemResult.value = result
            if (result.isSuccess) {
                // Reload items
                loadAllItems(item.userId)
            }
        }
    }
    
    /**
     * Update an existing clothing item
     */
    fun updateItem(item: ClothingItem) {
        _isLoading.value = true
        viewModelScope.launch {
            val result = repository.updateClothingItem(item)
            _isLoading.value = false
            if (result.isSuccess) {
                // Reload items
                loadAllItems(item.userId)
            }
        }
    }
    
    /**
     * Delete a clothing item
     */
    fun deleteItem(itemId: Long, userId: Long) {
        _isLoading.value = true
        viewModelScope.launch {
            val result = repository.deleteClothingItem(itemId)
            _isLoading.value = false
            _deleteResult.value = result
            if (result.isSuccess) {
                // Reload items
                loadAllItems(userId)
            }
        }
    }
    
    /**
     * Search clothing items by name or category
     */
    fun searchItems(userId: Long, query: String) {
        _isLoading.value = true
        viewModelScope.launch {
            if (query.isBlank()) {
                // If query is blank, load all items
                loadAllItems(userId)
            } else {
                val items = repository.searchClothingItems(userId, query)
                _isLoading.value = false
                _clothingItems.value = items
            }
        }
    }
    
    /**
     * Filter items by category
     */
    fun filterByCategory(userId: Long, category: String) {
        _isLoading.value = true
        viewModelScope.launch {
            if (category.isEmpty() || category == "All") {
                loadAllItems(userId)
            } else {
                val items = repository.filterByCategory(userId, category)
                _isLoading.value = false
                _clothingItems.value = items
            }
        }
    }
    
    /**
     * Filter items by size
     */
    fun filterBySize(userId: Long, size: String) {
        _isLoading.value = true
        viewModelScope.launch {
            if (size.isEmpty() || size == "All") {
                loadAllItems(userId)
            } else {
                val items = repository.filterBySize(userId, size)
                _isLoading.value = false
                _clothingItems.value = items
            }
        }
    }
    
    /**
     * Filter items by color
     */
    fun filterByColor(userId: Long, color: String) {
        _isLoading.value = true
        viewModelScope.launch {
            if (color.isEmpty()) {
                loadAllItems(userId)
            } else {
                val items = repository.filterByColor(userId, color)
                _isLoading.value = false
                _clothingItems.value = items
            }
        }
    }
    
    /**
     * Get a single item by ID
     */
    fun getItemById(itemId: Long) {
        _isLoading.value = true
        viewModelScope.launch {
            val item = repository.getClothingItemById(itemId)
            _isLoading.value = false
            _currentItem.value = item
        }
    }
    
    /**
     * Set the current item (for editing)
     */
    fun setCurrentItem(item: ClothingItem?) {
        _currentItem.value = item
    }
    
    /**
     * Clear current item
     */
    fun clearCurrentItem() {
        _currentItem.value = null
    }
    
    /**
     * Clear search results and reload all items
     */
    fun clearSearch(userId: Long) {
        loadAllItems(userId)
    }
    
    /**
     * Get total quantity of all items for a user
     */
    suspend fun getTotalQuantityForUser(userId: Long): Int {
        return repository.getTotalQuantityForUser(userId)
    }
    
    /**
     * Get count of clothing items for a user
     */
    suspend fun getClothingItemCountForUser(userId: Long): Int {
        return repository.getClothingItemCountForUser(userId)
    }
}
