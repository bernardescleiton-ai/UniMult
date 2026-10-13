package com.example.data.local.entity

import androidx.room.Entity
import androidx.room.Index
import androidx.room.PrimaryKey

@Entity(
  tableName = "favorites",
  indices = [Index(value = ["serverId", "streamId"], unique = true)]
)
data class FavoriteEntity(
  @PrimaryKey(autoGenerate = true) val id: Long = 0,
  val serverId: String, // Server isolation
  val streamId: String,
  val title: String,
  val type: String, // LIVE, VOD, SERIES
  val posterUrl: String?,
  val rating: String? = null,
  val year: String? = null,
  val addedAt: Long = System.currentTimeMillis()
)
