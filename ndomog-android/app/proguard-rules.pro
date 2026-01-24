# Ndomog Inventory ProGuard Rules

# Supabase Kotlin SDK
-keep class io.github.jan.supabase.** { *; }
-keep class io.ktor.** { *; }
-dontwarn io.ktor.**

# Kotlin Serialization
-keepattributes *Annotation*, InnerClasses
-dontnote kotlinx.serialization.**
-keep,includedescriptorclasses class com.ndomog.inventory.**$$serializer { *; }
-keepclassmembers class com.ndomog.inventory.** {
    *** Companion;
}
-keepclasseswithmembers class com.ndomog.inventory.** {
    kotlinx.serialization.KSerializer serializer(...);
}

# Keep data classes
-keep class com.ndomog.inventory.data.models.** { *; }

# Room
-keep class * extends androidx.room.RoomDatabase
-keep @androidx.room.Entity class *
-dontwarn androidx.room.paging.**

# Coroutines
-keepnames class kotlinx.coroutines.internal.MainDispatcherFactory {}
-keepnames class kotlinx.coroutines.CoroutineExceptionHandler {}
-keepclassmembers class kotlinx.coroutines.** {
    volatile <fields>;
}

# Compose
-keep class androidx.compose.** { *; }
-dontwarn androidx.compose.**
