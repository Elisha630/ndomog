package com.ndomog.inventory.utils

import android.content.Context
import androidx.datastore.preferences.core.booleanPreferencesKey
import androidx.datastore.preferences.core.stringPreferencesKey
import androidx.datastore.preferences.core.edit
import androidx.datastore.preferences.preferencesDataStore
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map

private val Context.dataStore by preferencesDataStore(name = "pin_preferences")

class PinPreferences(private val context: Context) {
    companion object {
        private val PIN_ENABLED_KEY = booleanPreferencesKey("pin_enabled")
        private val PIN_HASH_KEY = stringPreferencesKey("pin_hash")
        private val BIOMETRIC_ENABLED_KEY = booleanPreferencesKey("biometric_enabled")
    }

    val isPinEnabled: Flow<Boolean> = context.dataStore.data
        .map { preferences ->
            preferences[PIN_ENABLED_KEY] ?: false
        }

    val pinHash: Flow<String?> = context.dataStore.data
        .map { preferences ->
            preferences[PIN_HASH_KEY]
        }

    val isBiometricEnabled: Flow<Boolean> = context.dataStore.data
        .map { preferences ->
            preferences[BIOMETRIC_ENABLED_KEY] ?: false
        }

    suspend fun setPinEnabled(enabled: Boolean) {
        context.dataStore.edit { preferences ->
            preferences[PIN_ENABLED_KEY] = enabled
        }
    }

    suspend fun setPinHash(hash: String) {
        context.dataStore.edit { preferences ->
            preferences[PIN_HASH_KEY] = hash
        }
    }

    suspend fun setBiometricEnabled(enabled: Boolean) {
        context.dataStore.edit { preferences ->
            preferences[BIOMETRIC_ENABLED_KEY] = enabled
        }
    }

    suspend fun clear() {
        context.dataStore.edit { preferences ->
            preferences.clear()
        }
    }
}
