package com.ndomog.inventory.data.local

import androidx.room.TypeConverter
import com.ndomog.inventory.data.models.ActionType

class Converters {
    @TypeConverter
    fun fromActionType(value: ActionType): String {
        return value.name
    }

    @TypeConverter
    fun toActionType(value: String): ActionType {
        return ActionType.valueOf(value)
    }
}
