package com.ndomog.inventory.data.repository

import com.ndomog.inventory.data.local.ItemDao
import com.ndomog.inventory.data.local.PendingActionDao
import com.ndomog.inventory.data.models.ActionType
import com.ndomog.inventory.data.models.Item
import com.ndomog.inventory.data.models.PendingAction
import com.ndomog.inventory.data.remote.SupabaseClient
import io.github.jan.supabase.postgrest.from
import io.github.jan.supabase.postgrest.query.Columns
import kotlinx.coroutines.flow.Flow
import kotlinx.serialization.encodeToString
import kotlinx.serialization.json.Json
import timber.log.Timber

class ItemRepository(
    private val itemDao: ItemDao,
    private val pendingActionDao: PendingActionDao
) {
    private val supabase = SupabaseClient.client

    // Observe all items from local database
    fun observeItems(): Flow<List<Item>> = itemDao.getAllItems()

    // Load items - tries online first, falls back to cache
    suspend fun loadItems(isOnline: Boolean): Result<Pair<List<Item>, Boolean>> {
        return try {
            if (isOnline) {
                // Fetch from Supabase
                val items = supabase.from("items")
                    .select {
                        filter {
                            eq("is_deleted", false)
                        }
                    }
                    .decodeList<Item>()

                // Cache locally
                itemDao.insertItems(items)
                Result.success(Pair(items, false))
            } else {
                // Load from cache
                val cachedItems = itemDao.getItems()
                Result.success(Pair(cachedItems, true))
            }
        } catch (e: Exception) {
            Timber.e(e, "Error loading items, falling back to cache")
            val cachedItems = itemDao.getItems()
            Result.success(Pair(cachedItems, true))
        }
    }

    // Get single item
    suspend fun getItem(id: String): Item? = itemDao.getItemById(id)

    // Add item - queues for sync if offline
    suspend fun addItem(item: Item, isOnline: Boolean) {
        // Save locally first
        itemDao.insertItem(item)

        if (isOnline) {
            try {
                // Sync to Supabase immediately
                supabase.from("items").insert(item)
            } catch (e: Exception) {
                Timber.e(e, "Failed to sync item online, queuing for later")
                queueAction(ActionType.ADD_ITEM, item.id, Json.encodeToString(item))
            }
        } else {
            // Queue for sync when back online
            queueAction(ActionType.ADD_ITEM, item.id, Json.encodeToString(item))
        }
    }

    // Update item
    suspend fun updateItem(item: Item, isOnline: Boolean) {
        itemDao.updateItem(item)

        if (isOnline) {
            try {
                supabase.from("items").update(item) {
                    filter {
                        eq("id", item.id)
                    }
                }
            } catch (e: Exception) {
                Timber.e(e, "Failed to update item online, queuing for later")
                queueAction(ActionType.UPDATE_ITEM, item.id, Json.encodeToString(item))
            }
        } else {
            queueAction(ActionType.UPDATE_ITEM, item.id, Json.encodeToString(item))
        }
    }

    // Update quantity
    suspend fun updateQuantity(id: String, quantity: Int, isOnline: Boolean) {
        itemDao.updateQuantity(id, quantity)

        if (isOnline) {
            try {
                supabase.from("items").update(mapOf("quantity" to quantity)) {
                    filter {
                        eq("id", id)
                    }
                }
            } catch (e: Exception) {
                Timber.e(e, "Failed to update quantity online, queuing for later")
                queueAction(ActionType.UPDATE_QUANTITY, id, Json.encodeToString(mapOf("quantity" to quantity)))
            }
        } else {
            queueAction(ActionType.UPDATE_QUANTITY, id, Json.encodeToString(mapOf("quantity" to quantity)))
        }
    }

    // Soft delete item
    suspend fun deleteItem(id: String, userId: String, isOnline: Boolean) {
        val now = java.time.Instant.now().toString()
        itemDao.softDelete(id, now, userId)

        if (isOnline) {
            try {
                supabase.from("items").update(
                    mapOf(
                        "is_deleted" to true,
                        "deleted_at" to now,
                        "deleted_by" to userId
                    )
                ) {
                    filter {
                        eq("id", id)
                    }
                }
            } catch (e: Exception) {
                Timber.e(e, "Failed to delete item online, queuing for later")
                queueAction(ActionType.DELETE_ITEM, id, Json.encodeToString(mapOf("deleted_at" to now, "deleted_by" to userId)))
            }
        } else {
            queueAction(ActionType.DELETE_ITEM, id, Json.encodeToString(mapOf("deleted_at" to now, "deleted_by" to userId)))
        }
    }

    private suspend fun queueAction(type: ActionType, entityId: String, data: String) {
        pendingActionDao.insertAction(
            PendingAction(
                type = type,
                entityId = entityId,
                data = data
            )
        )
    }
}
