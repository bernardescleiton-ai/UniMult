package com.example.data.local.entity

import androidx.room.Entity
import androidx.room.Index
import androidx.room.PrimaryKey

@Entity(
  tableName = "watch_history",
  indices = [Index(value = ["serverId", "streamId"], unique = true)]
)
data class HistoryEntity(
  @PrimaryKey(autoGenerate = true) val id: Long = 0,
  val serverId: String, // Server isolation
  val streamId: String,
  val title: String,
  val type: String, // LIVE, VOD, SERIES
  val posterUrl: String?,
  val lastPlaybackPositionMs: Long = 0L,
  val durationMs: Long = 0L,
  val episodeInfo: String? = null,
  val watchedAt: Long = System.currentTimeMillis()
)
