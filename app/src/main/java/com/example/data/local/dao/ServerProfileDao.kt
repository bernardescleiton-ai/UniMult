package com.example.data.local.dao

import androidx.room.Dao
import androidx.room.Delete
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import androidx.room.Transaction
import androidx.room.Update
import com.example.data.local.entity.ServerProfileEntity
import kotlinx.coroutines.flow.Flow

@Dao
interface ServerProfileDao {
  @Query("SELECT * FROM servers ORDER BY createdAt DESC")
  fun getAllServers(): Flow<List<ServerProfileEntity>>

  @Query("SELECT * FROM servers WHERE id = :id LIMIT 1")
  suspend fun getServerById(id: String): ServerProfileEntity?

  @Query("SELECT * FROM servers WHERE isActive = 1 LIMIT 1")
  fun getActiveServer(): Flow<ServerProfileEntity?>

  @Query("SELECT * FROM servers WHERE isActive = 1 LIMIT 1")
  suspend fun getActiveServerSync(): ServerProfileEntity?

  @Insert(onConflict = OnConflictStrategy.REPLACE)
  suspend fun insertServer(server: ServerProfileEntity)

  @Update
  suspend fun updateServer(server: ServerProfileEntity)

  @Delete
  suspend fun deleteServer(server: ServerProfileEntity)

  @Query("DELETE FROM servers WHERE id = :id")
  suspend fun deleteServerById(id: String)

  @Query("UPDATE servers SET isActive = 0")
  suspend fun deactivateAllServers()

  @Query("UPDATE servers SET isActive = 1 WHERE id = :id")
  suspend fun activateServer(id: String)

  @Transaction
  suspend fun setActiveServer(id: String) {
    deactivateAllServers()
    activateServer(id)
  }
}
