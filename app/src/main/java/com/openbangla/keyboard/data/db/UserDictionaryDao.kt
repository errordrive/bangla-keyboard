package com.openbangla.keyboard.data.db

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import kotlinx.coroutines.flow.Flow

@Dao
interface UserDictionaryDao {
    @Query("SELECT * FROM user_dictionary WHERE locale = :locale ORDER BY frequency DESC")
    fun getWordsByLocale(locale: String): Flow<List<UserDictionaryEntity>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertOrUpdateWord(word: UserDictionaryEntity)

    @Query("UPDATE user_dictionary SET frequency = frequency + 1 WHERE word = :word")
    suspend fun incrementFrequency(word: String)
}
