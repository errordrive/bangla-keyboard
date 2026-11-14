package com.openbangla.keyboard.data.db

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import kotlinx.coroutines.flow.Flow

@Dao
interface StickerDao {
    @Query("SELECT * FROM stickers WHERE isDownloaded = 1")
    fun getDownloadedStickers(): Flow<List<StickerEntity>>

    @Query("SELECT * FROM stickers WHERE category = :category")
    fun getStickersByCategory(category: String): Flow<List<StickerEntity>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertAll(stickers: List<StickerEntity>)

    @Query("UPDATE stickers SET isDownloaded = :downloaded WHERE packId = :packId")
    suspend fun updateDownloadStatus(packId: String, downloaded: Boolean)
}
