package com.ndomog.inventory.utils

import android.content.Context
import timber.log.Timber

/**
 * Helper class for biometric authentication
 * NOTE: Full biometric support requires androidx.biometric:biometric library
 * For now, this provides placeholder methods that apps can enhance with actual biometric implementation
 */
class BiometricPromptHelper {
    companion object {
        /**
         * Show biometric authentication prompt
         * 
         * @param context Android context
         * @param title Title of the biometric prompt
         * @param subtitle Subtitle of the biometric prompt
         * @param onAuthenticationSucceeded Called when user successfully authenticates
         * @param onAuthenticationError Called when authentication fails
         */
        fun showBiometricPrompt(
            context: Context,
            title: String = "Biometric Authentication",
            subtitle: String = "Verify your identity",
            onAuthenticationSucceeded: () -> Unit = {},
            onAuthenticationError: (String) -> Unit = {}
        ) {
            // TODO: Implement full biometric authentication
            // Requires adding: implementation("androidx.biometric:biometric:1.1.0") to build.gradle
            Timber.d("Biometric prompt would show: $title - $subtitle")
            onAuthenticationSucceeded()
        }

        /**
         * Check if device can perform biometric authentication
         */
        fun canAuthenticateWithBiometrics(context: Context): Boolean {
            // TODO: Implement biometric capability check
            // For now, return false until biometric library is added
            return false
        }
    }
}
