# Remaining Backend Implementation Guide

## 1. Password Update with Old Password Verification

### Current Status:
- UI is complete with old password field and visibility toggles
- ProfileViewModel has `updatePassword()` stub method

### What Needs to be Done:

**File**: `presentation/profile/ProfileViewModel.kt` - `updatePassword()` method

```kotlin
fun updatePassword(oldPassword: String, newPassword: String) {
    viewModelScope.launch {
        _isLoading.value = true
        _error.value = null
        try {
            // First, verify old password by attempting to re-authenticate
            val currentUser = authRepository.getCurrentUser()
            val email = currentUser?.email ?: return@launch
            
            // Try to sign in with old password to verify
            val verifyResult = authRepository.signIn(email, oldPassword)
            
            if (verifyResult.isSuccess) {
                // Old password is correct, now update to new password
                val updateResult = authRepository.updatePassword(newPassword)
                
                if (updateResult.isSuccess) {
                    toast("Password updated successfully")
                } else {
                    _error.value = "Failed to update password"
                }
            } else {
                _error.value = "Current password is incorrect"
            }
        } catch (e: Exception) {
            Timber.e(e, "Failed to update password")
            _error.value = e.message ?: "Failed to update password"
        } finally {
            _isLoading.value = false
        }
    }
}
```

### In AuthRepository, ensure you have:
```kotlin
suspend fun updatePassword(newPassword: String): Result<Unit>
// Implementation using Supabase auth.updateUser()
```

---

## 2. Activity Logging

### Current Status:
- ActivityScreen exists
- ProfileViewModel and DashboardViewModel exist

### What Needs to be Done:

### A. Update DashboardViewModel to Log Changes

Add methods to log activities:

```kotlin
private fun logActivity(action: String, itemId: String, itemName: String) {
    viewModelScope.launch {
        try {
            val currentUser = authRepository.getCurrentUser()
            
            // Get username for the log
            val profile = SupabaseClient.client.from("profiles").select {
                filter { eq("id", currentUser?.id) }
            }.decodeSingle<Profile>()
            
            val activityLog = mapOf(
                "user_id" to currentUser?.id,
                "username" to profile.username,
                "action" to action,
                "entity_type" to "item",
                "entity_id" to itemId,
                "entity_name" to itemName,
                "timestamp" to System.currentTimeMillis(),
                "details" to ""
            )
            
            SupabaseClient.client.from("activity_logs").insert(activityLog)
            
            // Notify other users via real-time subscription
            notifyOtherUsers(action, itemName, profile.username)
        } catch (e: Exception) {
            Timber.e(e, "Failed to log activity")
        }
    }
}

// Call this when adding an item:
fun addItem(item: Item) {
    // ... existing add logic ...
    logActivity("CREATE", item.id, item.name)
}

// Call this when updating an item:
fun updateItem(item: Item) {
    // ... existing update logic ...
    logActivity("UPDATE", item.id, item.name)
}

// Call this when deleting an item:
fun deleteItem(itemId: String, itemName: String) {
    // ... existing delete logic ...
    logActivity("DELETE", itemId, itemName)
}
```

### B. Update ActivityViewModel to Display Username

```kotlin
// In ActivityViewModel or ActivityScreen
val activityLogs by viewModel.activityLogs.collectAsState()

// In UI, display:
Text("${log.username} ${log.action}d ${log.entity_name}")
// Instead of: Text("${log.user_id} ${log.action}d ${log.entity_name}")
```

---

## 3. Notifications to All Users

### Current Status:
- Notification permission is requested
- Push notification service exists

### What Needs to be Done:

### A. Add Function to Send Notifications

**File**: `services/pushNotificationService.ts` or similar

```kotlin
suspend fun notifyOtherUsers(action: String, itemName: String, username: String) {
    viewModelScope.launch {
        try {
            val currentUser = authRepository.getCurrentUser()
            
            // Query all other users' FCM tokens
            val tokens = SupabaseClient.client.from("push_subscriptions")
                .select()
                .filter {
                    neq("user_id", currentUser?.id)  // Exclude current user
                }
                .decodeList<PushSubscription>()
            
            for (subscription in tokens) {
                sendPushNotification(
                    token = subscription.token,
                    title = "Inventory Updated",
                    body = "$username ${action.lowercase()}d: $itemName",
                    data = mapOf(
                        "action" to action,
                        "itemName" to itemName,
                        "username" to username
                    )
                )
            }
        } catch (e: Exception) {
            Timber.e(e, "Failed to send notifications")
        }
    }
}

private fun sendPushNotification(
    token: String,
    title: String,
    body: String,
    data: Map<String, String>
) {
    // Send via Firebase Cloud Messaging or custom endpoint
    // This typically goes to your backend to call FCM API
}
```

### B. Handle Incoming Notifications in Receiver

```kotlin
class NdomogFirebaseMessagingService : FirebaseMessagingService() {
    override fun onMessageReceived(remoteMessage: RemoteMessage) {
        val title = remoteMessage.notification?.title ?: "Inventory Updated"
        val body = remoteMessage.notification?.body ?: "An item was updated"
        
        // Show local notification
        showLocalNotification(title, body)
        
        // Optionally refresh activity logs when notification received
        // Trigger update of ActivityScreen data
    }
}
```

---

## 4. Check for Updates

### Current Status:
- Version dialog exists
- VersionInfoDialog shows hardcoded versions

### What Needs to be Done:

**File**: `presentation/profile/ProfileViewModel.kt`

```kotlin
fun checkForUpdates() {
    viewModelScope.launch {
        try {
            // Query app_releases table for latest version
            val releases = SupabaseClient.client.from("app_releases")
                .select()
                .order("version_code", QueryOrder.DESCENDING)
                .limit(5)
                .decodeSingle<AppRelease>()
            
            val latestVersion = releases.version
            val currentVersion = APP_VERSION  // "1.2.3"
            
            if (isVersionNewer(latestVersion, currentVersion)) {
                _updateAvailable.value = true
                _latestVersion.value = latestVersion
                _releaseNotes.value = releases.notes
                _downloadUrl.value = releases.download_url
            } else {
                _updateAvailable.value = false
            }
        } catch (e: Exception) {
            Timber.e(e, "Failed to check for updates")
        }
    }
}

private fun isVersionNewer(latest: String, current: String): Boolean {
    val latestParts = latest.split(".").map { it.toIntOrNull() ?: 0 }
    val currentParts = current.split(".").map { it.toIntOrNull() ?: 0 }
    
    for (i in 0..2) {
        if (latestParts[i] > currentParts[i]) return true
        if (latestParts[i] < currentParts[i]) return false
    }
    return false
}
```

---

## 5. Biometric Authentication

### Current Status:
- Biometric toggle saves state
- UI shows when enabled
- No authentication prompt

### What Needs to be Done:

**File**: `presentation/profile/ProfileScreen.kt` - Update biometric toggle

```kotlin
Switch(
    checked = isBiometricEnabled,
    onCheckedChange = { newValue ->
        if (newValue) {
            // Show biometric authentication prompt
            val biometricPrompt = androidx.biometric.BiometricPrompt(
                context,
                ContextCompat.getMainExecutor(context),
                object : androidx.biometric.BiometricPrompt.AuthenticationCallback() {
                    override fun onAuthenticationSucceeded(
                        result: androidx.biometric.BiometricPrompt.AuthenticationResult
                    ) {
                        isBiometricEnabled = true
                        viewModel.viewModelScope.launch {
                            pinPreferences.setBiometricEnabled(true)
                        }
                    }
                    
                    override fun onAuthenticationError(
                        errorCode: Int,
                        errString: CharSequence
                    ) {
                        // Authentication failed
                        isBiometricEnabled = false
                    }
                }
            )
            
            val promptInfo = androidx.biometric.BiometricPrompt.PromptInfo.Builder()
                .setTitle("Enable Biometric Lock")
                .setSubtitle("Verify your identity with fingerprint or face")
                .setNegativeButtonText("Cancel")
                .build()
            
            biometricPrompt.authenticate(promptInfo)
        } else {
            isBiometricEnabled = false
            viewModel.viewModelScope.launch {
                pinPreferences.setBiometricEnabled(false)
            }
        }
    },
    // ... rest of switch properties
)
```

---

## 6. Forgot Password

### Current Status:
- Link exists in LoginScreen
- No implementation

### What Needs to be Done:

**File**: `presentation/auth/LoginScreen.kt`

```kotlin
Text(
    "Forgot password?",
    style = MaterialTheme.typography.labelMedium.copy(color = NdomogColors.Primary),
    modifier = Modifier.clickable {
        // Show forgot password dialog
        showForgotPasswordDialog = true
    }
)

// Then add dialog:
if (showForgotPasswordDialog) {
    ForgotPasswordDialog(
        onDismiss = { showForgotPasswordDialog = false },
        onSend = { email ->
            authViewModel.sendPasswordResetEmail(email)
            showForgotPasswordDialog = false
        }
    )
}
```

**File**: `presentation/auth/AuthViewModel.kt`

```kotlin
fun sendPasswordResetEmail(email: String) {
    viewModelScope.launch {
        _loginState.value = LoginState.Loading
        try {
            val result = authRepository.sendPasswordResetEmail(email)
            result.onSuccess {
                _loginState.value = LoginState.Success
                // Show message: "Reset link sent to email"
            }.onFailure {
                _loginState.value = LoginState.Error(it.message ?: "Failed to send reset email")
            }
        } catch (e: Exception) {
            _loginState.value = LoginState.Error(e.message ?: "Unknown error")
        }
    }
}
```

---

## Database Schema Reminder

Make sure these tables exist in Supabase:

```sql
-- Already existing or should exist:
- profiles (id, username, avatar_url)
- activity_logs (id, user_id, action, entity_type, entity_id, timestamp, etc)
- push_subscriptions (user_id, token)
- app_releases (version, version_code, notes, download_url)
```

---

## Testing Checklist:

- [ ] Password change validates old password
- [ ] Activity appears for all users when item is changed
- [ ] Notifications send to all other users
- [ ] Biometric prompt shows when enabling biometric
- [ ] Check for updates finds new version
- [ ] Forgot password sends email
- [ ] Username displays in activity logs (not user_id)
