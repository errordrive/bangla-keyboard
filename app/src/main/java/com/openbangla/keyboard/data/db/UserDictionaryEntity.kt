package com.openbangla.keyboard.data.db

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "user_dictionary")
data class UserDictionaryEntity(
    @PrimaryKey
    val word: String,
    val frequency: Int = 1,
    val locale: String = "bn_BD"
)
