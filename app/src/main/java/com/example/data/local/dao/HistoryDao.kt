package com.example.data.local.dao

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import com.example.data.local.entity.HistoryEntity
import kotlinx.coroutines.flow.Flow

@Dao
interface HistoryDao {
  @Query("SELECT * FROM watch_history WHERE serverId = :serverId ORDER BY watchedAt DESC")
  fun getHistoryForServer(serverId: String): Flow<List<HistoryEntity>>

  @Insert(onConflict = OnConflictStrategy.REPLACE)
  suspend fun insertOrUpdateHistory(history: HistoryEntity)

  @Query("DELETE FROM watch_history WHERE serverId = :serverId")
  suspend fun clearHistoryForServer(serverId: String)

  @Query("DELETE FROM watch_history WHERE id = :id")
  suspend fun deleteHistoryItem(id: Long)
}
