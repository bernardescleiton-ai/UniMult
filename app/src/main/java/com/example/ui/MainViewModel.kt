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

data class ActiveWebPlayback(
  val url: String,
  val title: String
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

  private val _selectedSection = MutableStateFlow(AppNavSection.SERVIDORES)
  val selectedSection: StateFlow<AppNavSection> = _selectedSection.asStateFlow()

  private val _activePlayback = MutableStateFlow<ActivePlayback?>(null)
  val activePlayback: StateFlow<ActivePlayback?> = _activePlayback.asStateFlow()

  private val _activeWebPlayback = MutableStateFlow<ActiveWebPlayback?>(null)
  val activeWebPlayback: StateFlow<ActiveWebPlayback?> = _activeWebPlayback.asStateFlow()

  val isLoading: StateFlow<Boolean> = contentRepository.isLoading
  val errorMessage: StateFlow<String?> = contentRepository.errorMessage
  val accountInfo: StateFlow<AccountInfo?> = contentRepository.accountInfo

  // Reactive StateFlows from ContentRepository
  val liveChannels: StateFlow<List<LiveChannel>> = contentRepository.liveChannels
  val liveCategories: StateFlow<List<Category>> = contentRepository.liveCategories
  val vodMovies: StateFlow<List<VodItem>> = contentRepository.vodMovies
  val vodCategories: StateFlow<List<Category>> = contentRepository.vodCategories
  val series: StateFlow<List<SeriesItem>> = contentRepository.series
  val seriesCategories: StateFlow<List<Category>> = contentRepository.seriesCategories
  val kidsContent: StateFlow<List<VodItem>> = contentRepository.kidsContent
  val animeContent: StateFlow<List<VodItem>> = contentRepository.animeContent
  val sportsChannels: StateFlow<List<LiveChannel>> = contentRepository.sportsChannels

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
          _selectedSection.value = AppNavSection.SERVIDORES
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

  fun startWebPlayback(url: String, title: String) {
    _activeWebPlayback.value = ActiveWebPlayback(url, title)
  }

  fun stopWebPlayback() {
    _activeWebPlayback.value = null
  }

  fun clearHistory() {
    activeServer.value?.let { server ->
      viewModelScope.launch {
        historyRepository.clearHistory(server.id)
      }
    }
  }

  fun reloadContent() {
    activeServer.value?.let { server ->
      viewModelScope.launch {
        contentRepository.loadServerContent(server)
      }
    }
  }
}
