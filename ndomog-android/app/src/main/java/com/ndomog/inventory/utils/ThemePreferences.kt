package com.ndomog.inventory.utils

import android.content.Context
import androidx.datastore.preferences.core.booleanPreferencesKey
import androidx.datastore.preferences.core.floatPreferencesKey
import androidx.datastore.preferences.core.edit
import androidx.datastore.preferences.preferencesDataStore
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map

private val Context.dataStore by preferencesDataStore(name = "theme_preferences")

class ThemePreferences(private val context: Context) {
    companion object {
        private val DARK_MODE_KEY = booleanPreferencesKey("dark_mode")
        private val HIGH_CONTRAST_KEY = booleanPreferencesKey("high_contrast")
        private val TEXT_SIZE_KEY = floatPreferencesKey("text_size")
    }

    val isDarkMode: Flow<Boolean> = context.dataStore.data
        .map { preferences ->
            preferences[DARK_MODE_KEY] ?: true  // Default to dark mode
        }

    val isHighContrast: Flow<Boolean> = context.dataStore.data
        .map { preferences ->
            preferences[HIGH_CONTRAST_KEY] ?: false
        }

    val textSize: Flow<Float> = context.dataStore.data
        .map { preferences ->
            preferences[TEXT_SIZE_KEY] ?: 1f
        }

    suspend fun setDarkMode(isDark: Boolean) {
        context.dataStore.edit { preferences ->
            preferences[DARK_MODE_KEY] = isDark
        }
    }

    suspend fun setHighContrast(enabled: Boolean) {
        context.dataStore.edit { preferences ->
            preferences[HIGH_CONTRAST_KEY] = enabled
        }
    }

    suspend fun setTextSize(size: Float) {
        context.dataStore.edit { preferences ->
            preferences[TEXT_SIZE_KEY] = size
        }
    }
}
