package com.openbangla.keyboard.data.db

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "stickers")
data class StickerEntity(
    @PrimaryKey(autoGenerate = true)
    val id: Long = 0,
    val packId: String,
    val name: String,
    val category: String,
    val filePath: String,
    val isAnimatedGif: Boolean = false,
    val isDownloaded: Boolean = false
)
