package com.ndomog.inventory.presentation.profile

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.ndomog.inventory.data.repository.AuthRepository
import com.ndomog.inventory.data.local.ProfileDao
import com.ndomog.inventory.data.models.Profile
import com.ndomog.inventory.data.remote.SupabaseClient
import com.ndomog.inventory.services.AppRelease
import com.ndomog.inventory.services.AppReleaseService
import io.github.jan.supabase.postgrest.from
import io.github.jan.supabase.gotrue.auth
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import timber.log.Timber

class ProfileViewModel(
    private val authRepository: AuthRepository,
    private val profileDao: ProfileDao
) : ViewModel() {

    private val _userEmail = MutableStateFlow<String?>(null)
    val userEmail: StateFlow<String?> = _userEmail.asStateFlow()

    private val _username = MutableStateFlow<String?>(null)
    val username: StateFlow<String?> = _username.asStateFlow()

    private val _avatarUrl = MutableStateFlow<String?>(null)
    val avatarUrl: StateFlow<String?> = _avatarUrl.asStateFlow()

    private val _isLoading = MutableStateFlow(false)
    val isLoading: StateFlow<Boolean> = _isLoading.asStateFlow()

    private val _error = MutableStateFlow<String?>(null)
    val error: StateFlow<String?> = _error.asStateFlow()
    
    private val _isAdmin = MutableStateFlow(false)
    val isAdmin: StateFlow<Boolean> = _isAdmin.asStateFlow()
    
    private val _isLoggedOut = MutableStateFlow(false)
    val isLoggedOut: StateFlow<Boolean> = _isLoggedOut.asStateFlow()

    private val _updateAvailable = MutableStateFlow(false)
    val updateAvailable: StateFlow<Boolean> = _updateAvailable.asStateFlow()

    private val _latestRelease = MutableStateFlow<AppRelease?>(null)
    val latestRelease: StateFlow<AppRelease?> = _latestRelease.asStateFlow()

    private val appReleaseService = AppReleaseService(viewModelScope)

    init {
        loadUserProfile()
    }

    private fun loadUserProfile() {
        viewModelScope.launch {
            _isLoading.value = true
            _error.value = null
            try {
                val currentUser = authRepository.getCurrentUser()
                _userEmail.value = currentUser?.email
                currentUser?.id?.let { userId ->
                    // Try to load from local DB first
                    var profile = profileDao.getProfileById(userId)
                    if (profile != null) {
                        _username.value = profile.username
                        _avatarUrl.value = profile.avatarUrl
                    }

                    // Then fetch from remote (Supabase)
                    try {
                        val profiles = SupabaseClient.client.from("profiles").select {
                            filter {
                                eq("id", userId)
                            }
                        }.decodeList<Profile>()
                        
                        if (profiles.isNotEmpty()) {
                            val remoteProfile = profiles[0]
                            // Update values from remote
                            _username.value = remoteProfile.username
                            _avatarUrl.value = remoteProfile.avatarUrl
                            // Cache the latest
                            profileDao.insertProfile(remoteProfile)
                        }
                        
                        // Check if user is admin
                        checkAdminStatus(userId)
                    } catch (e: Exception) {
                        Timber.e(e, "Failed to load remote profile")
                        // Continue with locally cached profile if remote fetch fails
                    }
                }
            } catch (e: Exception) {
                Timber.e(e, "Failed to load profile")
                _error.value = e.message ?: "Failed to load profile"
            } finally {
                _isLoading.value = false
            }
        }
    }
    
    private suspend fun checkAdminStatus(userId: String) {
        try {
            // Check user_roles table for admin role
            val roles = SupabaseClient.client.from("user_roles").select {
                filter {
                    eq("user_id", userId)
                    eq("role", "admin")
                }
            }.decodeList<Map<String, Any>>()
            
            _isAdmin.value = roles.isNotEmpty()
        } catch (e: Exception) {
            Timber.e(e, "Failed to check admin status")
            _isAdmin.value = false
        }
    }

    fun updateUsername(newUsername: String) {
        viewModelScope.launch {
            _isLoading.value = true
            _error.value = null
            try {
                val currentUser = authRepository.getCurrentUser()
                currentUser?.id?.let { userId ->
                    SupabaseClient.client.from("profiles")
                        .update(mapOf("username" to newUsername)) {
                            filter {
                                eq("id", userId)
                            }
                        }
                    _username.value = newUsername
                    profileDao.insertProfile(Profile(id = userId, email = _userEmail.value ?: "", username = newUsername, avatarUrl = _avatarUrl.value))
                }
            } catch (e: Exception) {
                Timber.e(e, "Failed to update username")
                _error.value = e.message ?: "Failed to update username"
            } finally {
                _isLoading.value = false
            }
        }
    }
    
    fun updateAvatar(newAvatarUrl: String) {
        viewModelScope.launch {
            _isLoading.value = true
            _error.value = null
            try {
                val currentUser = authRepository.getCurrentUser()
                currentUser?.id?.let { userId ->
                    SupabaseClient.client.from("profiles")
                        .update(mapOf("avatar_url" to newAvatarUrl)) {
                            filter {
                                eq("id", userId)
                            }
                        }
                    _avatarUrl.value = newAvatarUrl
                    profileDao.insertProfile(Profile(id = userId, email = _userEmail.value ?: "", username = _username.value, avatarUrl = newAvatarUrl))
                }
            } catch (e: Exception) {
                Timber.e(e, "Failed to update avatar")
                _error.value = e.message ?: "Failed to update avatar"
            } finally {
                _isLoading.value = false
            }
        }
    }
    
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
                        _error.value = "Password updated successfully"
                    } else {
                        _error.value = updateResult.exceptionOrNull()?.message ?: "Failed to update password"
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

    fun logout() {
        viewModelScope.launch {
            _isLoading.value = true
            try {
                authRepository.signOut()
                _isLoggedOut.value = true
            } catch (e: Exception) {
                _error.value = e.message ?: "Failed to log out"
            } finally {
                _isLoading.value = false
            }
        }
    }

    fun checkForUpdates() {
        appReleaseService.checkForUpdates(
            onUpdateAvailable = { release ->
                _updateAvailable.value = true
                _latestRelease.value = release
                _error.value = null
            },
            onError = { errorMessage ->
                _updateAvailable.value = false
                _error.value = errorMessage
            }
        )
    }
}
