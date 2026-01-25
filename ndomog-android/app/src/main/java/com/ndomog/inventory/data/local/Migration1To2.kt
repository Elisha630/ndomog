package com.ndomog.inventory.data.local

import androidx.room.migration.Migration
import androidx.sqlite.db.SupportSQLiteDatabase

val MIGRATION_1_2 = object : Migration(1, 2) {
    override fun migrate(database: SupportSQLiteDatabase) {
        database.execSQL("ALTER TABLE items ADD COLUMN is_deleted INTEGER NOT NULL DEFAULT 0")
        database.execSQL("ALTER TABLE items ADD COLUMN deleted_at TEXT DEFAULT NULL")
        database.execSQL("ALTER TABLE items ADD COLUMN deleted_by TEXT DEFAULT NULL")
    }
}

val MIGRATION_2_3 = object : Migration(2, 3) {
    override fun migrate(database: SupportSQLiteDatabase) {
        // Create activity_logs table
        database.execSQL(
            """
            CREATE TABLE IF NOT EXISTS activity_logs (
                id TEXT PRIMARY KEY NOT NULL,
                user_id TEXT NOT NULL,
                username TEXT NOT NULL,
                action TEXT NOT NULL,
                entity_type TEXT NOT NULL DEFAULT 'item',
                entity_id TEXT NOT NULL,
                entity_name TEXT NOT NULL,
                timestamp INTEGER NOT NULL,
                details TEXT
            )
            """.trimIndent()
        )
    }
}