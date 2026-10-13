package com.example.ui

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.data.local.entity.FavoriteEntity
import com.example.data.local.entity.HistoryEntity
import com.example.data.repository.ContentRepository
import com.example.data.repository.FavoriteRepository
import com.example.data.repository.HistoryRepository
import com.example.data.repository.ServerRepository
import com.example.domain.model.AccountInfo
import com.example.domain.model.Category
import com.example.domain.model.LiveChannel
import com.example.domain.model.MatchGuideItem
import com.example.domain.model.SeriesItem
import com.example.domain.model.ServerProfile
import com.example.domain.model.VodItem
import com.example.ui.components.AppNavSection
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch

data class ActivePlayback(
  val streamUrl: String,
  val title: String,
  val subtitle: String = "",
  val isLive: Boolean = false,
  val streamId: String = "",
  val posterUrl: String? = null
)

class MainViewModel(
  private val serverRepository: ServerRepository,
  private val contentRepository: ContentRepository,
  private val historyRepository: HistoryRepository,
  private val favoriteRepository: FavoriteRepository
) : ViewModel() {

  val allServers: StateFlow<List<ServerProfile>> = serverRepository.allServers
    .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

  val activeServer: StateFlow<ServerProfile?> = serverRepository.activeServer
    .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), null)

  private val _selectedSection = MutableStateFlow(AppNavSection.DESTAQUES)
  val selectedSection: StateFlow<AppNavSection> = _selectedSection.asStateFlow()

  private val _activePlayback = MutableStateFlow<ActivePlayback?>(null)
  val activePlayback: StateFlow<ActivePlayback?> = _activePlayback.asStateFlow()

  val isLoading: StateFlow<Boolean> = contentRepository.isLoading
  val errorMessage: StateFlow<String?> = contentRepository.errorMessage
  val accountInfo: StateFlow<AccountInfo?> = contentRepository.accountInfo

  private val _historyList = MutableStateFlow<List<HistoryEntity>>(emptyList())
  val historyList: StateFlow<List<HistoryEntity>> = _historyList.asStateFlow()

  private val _favoritesList = MutableStateFlow<List<FavoriteEntity>>(emptyList())
  val favoritesList: StateFlow<List<FavoriteEntity>> = _favoritesList.asStateFlow()

  init {
    viewModelScope.launch {
      activeServer.collect { server ->
        contentRepository.switchServer(server)
        if (server != null) {
          launch {
            historyRepository.getHistoryForServer(server.id).collect {
              _historyList.value = it
            }
          }
          launch {
            favoriteRepository.getFavoritesForServer(server.id).collect {
              _favoritesList.value = it
            }
          }
        } else {
          _historyList.value = emptyList()
          _favoritesList.value = emptyList()
        }
      }
    }
  }

  fun setSection(section: AppNavSection) {
    _selectedSection.value = section
  }

  fun startPlayback(playback: ActivePlayback) {
    _activePlayback.value = playback
    // Record history if active server exists
    activeServer.value?.let { server ->
      viewModelScope.launch {
        historyRepository.recordPlayback(
          serverId = server.id,
          streamId = playback.streamId,
          title = playback.title,
          type = if (playback.isLive) "LIVE" else "VOD",
          posterUrl = playback.posterUrl,
          positionMs = 0L,
          durationMs = 0L,
          episodeInfo = playback.subtitle
        )
      }
    }
  }

  fun updatePlaybackProgress(positionMs: Long, durationMs: Long) {
    val current = _activePlayback.value ?: return
    activeServer.value?.let { server ->
      viewModelScope.launch {
        historyRepository.recordPlayback(
          serverId = server.id,
          streamId = current.streamId,
          title = current.title,
          type = if (current.isLive) "LIVE" else "VOD",
          posterUrl = current.posterUrl,
          positionMs = positionMs,
          durationMs = durationMs,
          episodeInfo = current.subtitle
        )
      }
    }
  }

  fun stopPlayback() {
    _activePlayback.value = null
  }

  fun clearHistory() {
    activeServer.value?.let { server ->
      viewModelScope.launch {
        historyRepository.clearHistory(server.id)
      }
    }
  }

  fun getLiveChannels(): List<LiveChannel> = contentRepository.getLiveChannels(activeServer.value?.id)
  fun getLiveCategories(): List<Category> = contentRepository.getLiveCategories(activeServer.value?.id)
  fun getMovies(categoryId: String? = null): List<VodItem> = contentRepository.getVodMovies(activeServer.value?.id, categoryId)
  fun getVodCategories(): List<Category> = contentRepository.getVodCategories(activeServer.value?.id)
  fun getSeries(categoryId: String? = null): List<SeriesItem> = contentRepository.getSeries(activeServer.value?.id, categoryId)
  fun getSeriesCategories(): List<Category> = contentRepository.getSeriesCategories(activeServer.value?.id)
  fun getKids(): List<VodItem> = contentRepository.getKidsContent(activeServer.value?.id)
  fun getAnime(): List<VodItem> = contentRepository.getAnimeContent(activeServer.value?.id)
  fun getSportsChannels(): List<LiveChannel> = contentRepository.getSportsChannels(activeServer.value?.id)

  fun reloadContent() {
    activeServer.value?.let { server ->
      viewModelScope.launch {
        contentRepository.loadServerContent(server)
      }
    }
  }
}
