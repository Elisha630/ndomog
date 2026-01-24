package com.ndomog.inventory.di

import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import com.ndomog.inventory.data.repository.AuthRepository
import com.ndomog.inventory.presentation.auth.AuthViewModel

class ViewModelFactory(
    private val authRepository: AuthRepository
) : ViewModelProvider.Factory {

    @Suppress("UNCHECKED_CAST")
    override fun <T : ViewModel> create(modelClass: Class<T>): T {
        return when {
            modelClass.isAssignableFrom(AuthViewModel::class.java) -> {
                AuthViewModel(authRepository) as T
            }
            // Add other ViewModels here
            else -> throw IllegalArgumentException("Unknown ViewModel class: ${modelClass.name}")
        }
    }
}
