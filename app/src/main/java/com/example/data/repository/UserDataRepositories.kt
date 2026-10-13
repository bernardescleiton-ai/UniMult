package com.example.data.repository

import com.example.data.local.dao.FavoriteDao
import com.example.data.local.dao.HistoryDao
import com.example.data.local.entity.FavoriteEntity
import com.example.data.local.entity.HistoryEntity
import kotlinx.coroutines.flow.Flow

class HistoryRepository(private val historyDao: HistoryDao) {

  fun getHistoryForServer(serverId: String): Flow<List<HistoryEntity>> {
    return historyDao.getHistoryForServer(serverId)
  }

  suspend fun recordPlayback(
    serverId: String,
    streamId: String,
    title: String,
    type: String,
    posterUrl: String?,
    positionMs: Long,
    durationMs: Long,
    episodeInfo: String? = null
  ) {
    val entity = HistoryEntity(
      serverId = serverId,
      streamId = streamId,
      title = title,
      type = type,
      posterUrl = posterUrl,
      lastPlaybackPositionMs = positionMs,
      durationMs = durationMs,
      episodeInfo = episodeInfo,
      watchedAt = System.currentTimeMillis()
    )
    historyDao.insertOrUpdateHistory(entity)
  }

  suspend fun deleteHistory(id: Long) {
    historyDao.deleteHistoryItem(id)
  }

  suspend fun clearHistory(serverId: String) {
    historyDao.clearHistoryForServer(serverId)
  }
}

class FavoriteRepository(private val favoriteDao: FavoriteDao) {

  fun getFavoritesForServer(serverId: String): Flow<List<FavoriteEntity>> {
    return favoriteDao.getFavoritesForServer(serverId)
  }

  fun isFavorite(serverId: String, streamId: String): Flow<Boolean> {
    return favoriteDao.isFavorite(serverId, streamId)
  }

  suspend fun toggleFavorite(
    serverId: String,
    streamId: String,
    title: String,
    type: String,
    posterUrl: String?,
    rating: String? = null,
    isCurrentlyFav: Boolean
  ) {
    if (isCurrentlyFav) {
      favoriteDao.removeFavorite(serverId, streamId)
    } else {
      val entity = FavoriteEntity(
        serverId = serverId,
        streamId = streamId,
        title = title,
        type = type,
        posterUrl = posterUrl,
        rating = rating,
        addedAt = System.currentTimeMillis()
      )
      favoriteDao.addFavorite(entity)
    }
  }
}
