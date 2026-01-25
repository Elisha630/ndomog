package com.ndomog.inventory.data.models

import androidx.room.Entity
import androidx.room.PrimaryKey
import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

@Serializable
@Entity(tableName = "items")
data class Item(
    @PrimaryKey
    val id: String = "",
    val name: String,
    val category: String,
    @SerialName("category_id")
    val categoryId: String? = null,
    val details: String? = null,
    @SerialName("photo_url")
    val photoUrl: String? = null,
    @SerialName("buying_price")
    val buyingPrice: Double = 0.0,
    @SerialName("selling_price")
    val sellingPrice: Double = 0.0,
    val quantity: Int = 0,
    @SerialName("low_stock_threshold")
    val lowStockThreshold: Int = 5,
    @SerialName("is_deleted")
    val isDeleted: Boolean = false,
    @SerialName("created_by")
    val createdBy: String? = null,
    @SerialName("created_at")
    val createdAt: String? = null,
    @SerialName("updated_at")
    val updatedAt: String? = null,
    @SerialName("deleted_at")
    val deletedAt: String? = null,
    @SerialName("deleted_by")
    val deletedBy: String? = null
)

@Serializable
@Entity(tableName = "categories")
data class Category(
    @PrimaryKey
    val id: String = "",
    val name: String,
    @SerialName("created_by")
    val createdBy: String? = null,
    @SerialName("created_at")
    val createdAt: String? = null
)

@Serializable
@Entity(tableName = "profiles")
data class Profile(
    @PrimaryKey
    val id: String,
    val email: String,
    val username: String? = null,
    @SerialName("avatar_url")
    val avatarUrl: String? = null
)

@Entity(tableName = "pending_actions")
data class PendingAction(
    @PrimaryKey(autoGenerate = true)
    val id: Int = 0,
    val type: ActionType,
    val entityId: String,
    val data: String, // JSON string of the data
    val timestamp: Long = System.currentTimeMillis(),
    val synced: Boolean = false
)

enum class ActionType {
    ADD_ITEM,
    UPDATE_ITEM,
    DELETE_ITEM,
    UPDATE_QUANTITY,
    ADD_CATEGORY
}

data class SyncResult(
    val success: Boolean,
    val itemsSynced: Int = 0,
    val actionsSynced: Int = 0,
    val errors: List<String> = emptyList()
)
