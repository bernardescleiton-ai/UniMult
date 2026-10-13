package com.example.data.local.dao

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import com.example.data.local.entity.FavoriteEntity
import kotlinx.coroutines.flow.Flow

@Dao
interface FavoriteDao {
  @Query("SELECT * FROM favorites WHERE serverId = :serverId ORDER BY addedAt DESC")
  fun getFavoritesForServer(serverId: String): Flow<List<FavoriteEntity>>

  @Query("SELECT EXISTS(SELECT 1 FROM favorites WHERE serverId = :serverId AND streamId = :streamId)")
  fun isFavorite(serverId: String, streamId: String): Flow<Boolean>

  @Insert(onConflict = OnConflictStrategy.REPLACE)
  suspend fun addFavorite(favorite: FavoriteEntity)

  @Query("DELETE FROM favorites WHERE serverId = :serverId AND streamId = :streamId")
  suspend fun removeFavorite(serverId: String, streamId: String)

  @Query("DELETE FROM favorites WHERE serverId = :serverId")
  suspend fun clearFavoritesForServer(serverId: String)
}
