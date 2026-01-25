package com.ndomog.inventory.services

import com.ndomog.inventory.data.models.ActivityLog
import com.ndomog.inventory.data.remote.SupabaseClient
import io.github.jan.supabase.postgrest.from
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.launch
import timber.log.Timber

/**
 * Service for sending push notifications to other users when items are modified.
 */
class PushNotificationService(private val coroutineScope: CoroutineScope) {
    private val supabase = SupabaseClient.client

    /**
     * Send notification to all other users about an inventory change.
     * 
     * @param action The action performed (CREATE, UPDATE, DELETE, UPDATE_QUANTITY)
     * @param itemName The name of the item that was modified
     * @param username The username of the user who made the change
     * @param currentUserId The ID of the current user (to exclude them from notifications)
     */
    fun notifyOtherUsers(
        action: String,
        itemName: String,
        username: String,
        currentUserId: String
    ) {
        coroutineScope.launch {
            try {
                // Query all other users' FCM tokens
                val tokens = supabase.from("push_subscriptions")
                    .select()
                    .decodeList<PushSubscriptionToken>()

                // Filter out current user and get their tokens
                val recipientTokens = tokens.filter { it.userId != currentUserId }

                if (recipientTokens.isEmpty()) {
                    Timber.d("No recipient tokens found for notification")
                    return@launch
                }

                // Create notification data
                val title = "Inventory Updated"
                val body = "$username ${action.lowercase()}: $itemName"
                val notificationData = mapOf(
                    "action" to action,
                    "itemName" to itemName,
                    "username" to username,
                    "timestamp" to System.currentTimeMillis().toString()
                )

                // Send to each recipient (in production, this would go through Firebase Cloud Messaging)
                for (subscription in recipientTokens) {
                    sendPushNotification(
                        token = subscription.token,
                        title = title,
                        body = body,
                        data = notificationData
                    )
                }

                Timber.d("Sent notifications to ${recipientTokens.size} users")
            } catch (e: Exception) {
                Timber.e(e, "Failed to send push notifications")
            }
        }
    }

    /**
     * Send a push notification to a specific FCM token.
     * In production, this would call your backend server to trigger FCM.
     * 
     * For now, this is a stub that would be called by a backend service.
     */
    private fun sendPushNotification(
        token: String,
        title: String,
        body: String,
        data: Map<String, String>
    ) {
        try {
            // In a real implementation, you would:
            // 1. Call your backend server endpoint
            // 2. Pass the FCM token and notification details
            // 3. Backend calls Firebase Cloud Messaging API
            // 4. FCM sends notification to the user's device
            
            // Example: POST to /api/send-notification with token and payload
            Timber.d("Would send notification to token: $token - Title: $title")
        } catch (e: Exception) {
            Timber.e(e, "Failed to send notification to token: $token")
        }
    }
}

/**
 * Data class for FCM token subscriptions from database
 */
data class PushSubscriptionToken(
    val id: String = "",
    val userId: String,
    val token: String,
    val createdAt: String = "",
    val updatedAt: String = ""
)
