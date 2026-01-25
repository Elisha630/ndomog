package com.ndomog.inventory.data.remote

import com.ndomog.inventory.BuildConfig
import io.github.jan.supabase.createSupabaseClient
import io.github.jan.supabase.gotrue.Auth
import io.github.jan.supabase.gotrue.FlowType
import io.github.jan.supabase.postgrest.Postgrest
import io.github.jan.supabase.realtime.Realtime
import io.github.jan.supabase.storage.Storage

import kotlin.time.Duration.Companion.seconds

object SupabaseClient {
    val client = createSupabaseClient(
        supabaseUrl = BuildConfig.SUPABASE_URL,
        supabaseKey = BuildConfig.SUPABASE_KEY
    ) {
        install(Auth) {
            flowType = FlowType.PKCE
            scheme = "app"
            host = "supabase.com"
            // Enable session auto-refresh and persistence
            alwaysAutoRefresh = true
            autoLoadFromStorage = true
            autoSaveToStorage = true
        }
        install(Postgrest)
        install(Realtime)
        install(Storage) {
            transferTimeout = 90.seconds
        }
    }
}
