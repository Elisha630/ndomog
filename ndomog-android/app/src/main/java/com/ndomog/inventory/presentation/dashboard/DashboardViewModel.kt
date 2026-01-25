package com.ndomog.inventory.presentation.dashboard

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.ndomog.inventory.data.models.Item
import com.ndomog.inventory.data.repository.ItemRepository
import com.ndomog.inventory.data.repository.SyncRepository
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch

class DashboardViewModel(
    private val itemRepository: ItemRepository,
    private val syncRepository: SyncRepository
) : ViewModel() {

    private val _items = MutableStateFlow<List<Item>>(emptyList())
    val items: StateFlow<List<Item>> = _items.asStateFlow()

    private val _isLoading = MutableStateFlow(false)
    val isLoading: StateFlow<Boolean> = _isLoading.asStateFlow()

    private val _error = MutableStateFlow<String?>(null)
    val error: StateFlow<String?> = _error.asStateFlow()

    init {
        loadItems()
        // Observe items from the local database
        viewModelScope.launch {
            itemRepository.observeItems().collect {
                _items.value = it
            }
        }
    }

    fun loadItems(isOnline: Boolean = true) {
        viewModelScope.launch {
            _isLoading.value = true
            _error.value = null
            itemRepository.loadItems(isOnline)
                .onSuccess { (items, fromCache) ->
                    // The _items flow is already updated by the observeItems collector
                    // No need to set _items.value here directly unless we want to show a loading state specific to the initial load
                }
                .onFailure {
                    _error.value = it.message ?: "Failed to load items"
                }
            _isLoading.value = false
        }
    }

    fun addItem(item: Item, isOnline: Boolean) {
        viewModelScope.launch {
            try {
                itemRepository.addItem(item, isOnline)
                _error.value = null
            } catch (e: Exception) {
                _error.value = e.message ?: "Failed to add item"
            }
        }
    }

    fun updateItem(item: Item, isOnline: Boolean) {
        viewModelScope.launch {
            try {
                itemRepository.updateItem(item, isOnline)
                _error.value = null
            } catch (e: Exception) {
                _error.value = e.message ?: "Failed to update item"
            }
        }
    }

    fun updateQuantity(id: String, quantity: Int, isOnline: Boolean) {
        viewModelScope.launch {
            try {
                itemRepository.updateQuantity(id, quantity, isOnline)
                _error.value = null
            } catch (e: Exception) {
                _error.value = e.message ?: "Failed to update quantity"
            }
        }
    }

    fun deleteItem(id: String, userId: String, isOnline: Boolean) {
        viewModelScope.launch {
            try {
                itemRepository.deleteItem(id, userId, isOnline)
                _error.value = null
            } catch (e: Exception) {
                _error.value = e.message ?: "Failed to delete item"
            }
        }
    }

    fun syncData(): StateFlow<Boolean> {
        val syncLoading = MutableStateFlow(false)
        viewModelScope.launch {
            syncLoading.value = true
            try {
                syncRepository.syncPendingActions()
                _error.value = null
            } catch (e: Exception) {
                _error.value = e.message ?: "Failed to sync data"
            } finally {
                syncLoading.value = false
            }
        }
        return syncLoading
    }
}
