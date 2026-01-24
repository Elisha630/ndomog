package com.ndomog.inventory.data.repository

import com.ndomog.inventory.data.local.ItemDao
import com.ndomog.inventory.data.local.PendingActionDao
import com.ndomog.inventory.data.models.ActionType
import com.ndomog.inventory.data.models.Item
import com.ndomog.inventory.data.models.SyncResult
import com.ndomog.inventory.data.remote.SupabaseClient
import io.github.jan.supabase.postgrest.from
import kotlinx.serialization.json.Json
import timber.log.Timber

class SyncRepository(
    private val itemDao: ItemDao,
    private val pendingActionDao: PendingActionDao
) {
    private val supabase = SupabaseClient.client
    private val json = Json { ignoreUnknownKeys = true }

    suspend fun syncPendingActions(): SyncResult {
        val errors = mutableListOf<String>()
        var actionsSynced = 0

        try {
            val pendingActions = pendingActionDao.getPendingActions()

            if (pendingActions.isEmpty()) {
                return SyncResult(success = true, actionsSynced = 0)
            }

            Timber.d("Syncing ${pendingActions.size} pending actions")

            for (action in pendingActions) {
                try {
                    when (action.type) {
                        ActionType.ADD_ITEM -> {
                            val item = json.decodeFromString<Item>(action.data)
                            supabase.from("items").insert(item)
                        }
                        
                        ActionType.UPDATE_ITEM -> {
                            val item = json.decodeFromString<Item>(action.data)
                            supabase.from("items").update(item) {
                                filter {
                                    eq("id", action.entityId)
                                }
                            }
                        }
                        
                        ActionType.UPDATE_QUANTITY -> {
                            val data = json.decodeFromString<Map<String, Int>>(action.data)
                            supabase.from("items").update(data) {
                                filter {
                                    eq("id", action.entityId)
                                }
                            }
                        }
                        
                        ActionType.DELETE_ITEM -> {
                            val data = json.decodeFromString<Map<String, Any>>(action.data)
                            supabase.from("items").update(
                                mapOf(
                                    "is_deleted" to true,
                                    "deleted_at" to data["deleted_at"],
                                    "deleted_by" to data["deleted_by"]
                                )
                            ) {
                                filter {
                                    eq("id", action.entityId)
                                }
                            }
                        }
                        
                        ActionType.ADD_CATEGORY -> {
                            // Handle category addition
                            val categoryData = json.decodeFromString<Map<String, String>>(action.data)
                            supabase.from("categories").insert(categoryData)
                        }
                    }

                    // Mark as synced
                    pendingActionDao.markActionSynced(action.id)
                    actionsSynced++
                    
                } catch (e: Exception) {
                    Timber.e(e, "Failed to sync action ${action.id}")
                    errors.add("Failed to sync ${action.type}: ${e.message}")
                }
            }

            // Clean up synced actions
            pendingActionDao.deleteSyncedActions()

            // Refresh local cache
            val items = supabase.from("items")
                .select {
                    filter {
                        eq("is_deleted", false)
                    }
                }
                .decodeList<Item>()
            itemDao.insertItems(items)

            return SyncResult(
                success = errors.isEmpty(),
                actionsSynced = actionsSynced,
                itemsSynced = items.size,
                errors = errors
            )

        } catch (e: Exception) {
            Timber.e(e, "Sync failed")
            errors.add(e.message ?: "Unknown error")
            return SyncResult(
                success = false,
                actionsSynced = actionsSynced,
                errors = errors
            )
        }
    }

    suspend fun getPendingActionsCount(): Int {
        return pendingActionDao.getPendingActions().size
    }
}
