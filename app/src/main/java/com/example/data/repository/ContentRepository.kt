package com.example.data.repository

import com.example.data.remote.XtreamCodesClient
import com.example.domain.model.AccountInfo
import com.example.domain.model.Category
import com.example.domain.model.EpisodeItem
import com.example.domain.model.LiveChannel
import com.example.domain.model.MatchGuideItem
import com.example.domain.model.SeriesItem
import com.example.domain.model.ServerProfile
import com.example.domain.model.VodItem
import kotlinx.coroutines.Job
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.sync.Mutex
import kotlinx.coroutines.sync.withLock

class ContentRepository(
  private val xtreamCodesClient: XtreamCodesClient
) {

  private var currentServerId: String? = null
  private val cacheMutex = Mutex()
  private var fetchJob: Job? = null

  // In-memory caches strictly isolated per serverId
  private val liveChannelsCache = mutableMapOf<String, List<LiveChannel>>()
  private val liveCategoriesCache = mutableMapOf<String, List<Category>>()
  private val vodMoviesCache = mutableMapOf<String, List<VodItem>>()
  private val vodCategoriesCache = mutableMapOf<String, List<Category>>()
  private val seriesCache = mutableMapOf<String, List<SeriesItem>>()
  private val seriesCategoriesCache = mutableMapOf<String, List<Category>>()

  private val _isLoading = MutableStateFlow(false)
  val isLoading: StateFlow<Boolean> = _isLoading.asStateFlow()

  private val _errorMessage = MutableStateFlow<String?>(null)
  val errorMessage: StateFlow<String?> = _errorMessage.asStateFlow()

  private val _accountInfo = MutableStateFlow<AccountInfo?>(null)
  val accountInfo: StateFlow<AccountInfo?> = _accountInfo.asStateFlow()

  suspend fun switchServer(newServer: ServerProfile?) {
    cacheMutex.withLock {
      fetchJob?.cancel()
      currentServerId = newServer?.id
      _errorMessage.value = null
      _accountInfo.value = null
    }

    if (newServer != null) {
      loadServerContent(newServer)
    }
  }

  suspend fun loadServerContent(server: ServerProfile) {
    _isLoading.value = true
    _errorMessage.value = null

    val authResult = xtreamCodesClient.authenticate(server)
    if (authResult.isSuccess) {
      _accountInfo.value = authResult.getOrThrow()
    } else {
      _errorMessage.value = authResult.exceptionOrNull()?.message ?: "Falha ao autenticar no servidor."
      _isLoading.value = false
      return
    }

    // 1. Load Live Categories and Channels from Server
    val liveCatRes = xtreamCodesClient.getLiveCategories(server)
    if (liveCatRes.isSuccess) {
      liveCategoriesCache[server.id] = liveCatRes.getOrThrow()
    }
    val liveStreamsRes = xtreamCodesClient.getLiveStreams(server)
    if (liveStreamsRes.isSuccess) {
      liveChannelsCache[server.id] = liveStreamsRes.getOrThrow()
    }

    // 2. Load VOD Categories and Movies from Server
    val vodCatRes = xtreamCodesClient.getVodCategories(server)
    if (vodCatRes.isSuccess) {
      vodCategoriesCache[server.id] = vodCatRes.getOrThrow()
    }
    val vodStreamsRes = xtreamCodesClient.getVodStreams(server)
    if (vodStreamsRes.isSuccess) {
      vodMoviesCache[server.id] = vodStreamsRes.getOrThrow()
    }

    // 3. Load Series Categories and Series from Server
    val seriesCatRes = xtreamCodesClient.getSeriesCategories(server)
    if (seriesCatRes.isSuccess) {
      seriesCategoriesCache[server.id] = seriesCatRes.getOrThrow()
    }
    val seriesStreamsRes = xtreamCodesClient.getSeriesStreams(server)
    if (seriesStreamsRes.isSuccess) {
      seriesCache[server.id] = seriesStreamsRes.getOrThrow()
    }

    _isLoading.value = false
  }

  fun getLiveChannels(serverId: String?): List<LiveChannel> {
    if (serverId == null) return emptyList()
    return liveChannelsCache[serverId] ?: emptyList()
  }

  fun getLiveCategories(serverId: String?): List<Category> {
    if (serverId == null) return emptyList()
    return liveCategoriesCache[serverId] ?: emptyList()
  }

  fun getVodMovies(serverId: String?, categoryId: String? = null): List<VodItem> {
    if (serverId == null) return emptyList()
    val list = vodMoviesCache[serverId] ?: emptyList()
    return if (categoryId != null && categoryId != "Todos" && categoryId != "ALL") {
      list.filter { it.categoryId == categoryId }
    } else {
      list
    }
  }

  fun getVodCategories(serverId: String?): List<Category> {
    if (serverId == null) return emptyList()
    return vodCategoriesCache[serverId] ?: emptyList()
  }

  fun getSeries(serverId: String?, categoryId: String? = null): List<SeriesItem> {
    if (serverId == null) return emptyList()
    val list = seriesCache[serverId] ?: emptyList()
    return if (categoryId != null && categoryId != "Todos" && categoryId != "ALL") {
      list.filter { it.categoryId == categoryId }
    } else {
      list
    }
  }

  fun getSeriesCategories(serverId: String?): List<Category> {
    if (serverId == null) return emptyList()
    return seriesCategoriesCache[serverId] ?: emptyList()
  }

  fun getKidsContent(serverId: String?): List<VodItem> {
    if (serverId == null) return emptyList()
    val allVod = vodMoviesCache[serverId] ?: emptyList()
    val kidsCategories = (vodCategoriesCache[serverId] ?: emptyList())
      .filter { it.name.contains("kid", ignoreCase = true) || it.name.contains("infantil", ignoreCase = true) || it.name.contains("desenho", ignoreCase = true) }
      .map { it.id }
      .toSet()

    val filtered = allVod.filter {
      kidsCategories.contains(it.categoryId) ||
        it.name.contains("kid", ignoreCase = true) ||
        it.name.contains("infantil", ignoreCase = true) ||
        it.genre?.contains("infantil", ignoreCase = true) == true
    }
    return filtered
  }

  fun getAnimeContent(serverId: String?): List<VodItem> {
    if (serverId == null) return emptyList()
    val allVod = vodMoviesCache[serverId] ?: emptyList()
    val animeCategories = (vodCategoriesCache[serverId] ?: emptyList())
      .filter { it.name.contains("anime", ignoreCase = true) || it.name.contains("animação", ignoreCase = true) || it.name.contains("animacao", ignoreCase = true) }
      .map { it.id }
      .toSet()

    val filtered = allVod.filter {
      animeCategories.contains(it.categoryId) ||
        it.name.contains("anime", ignoreCase = true) ||
        it.genre?.contains("anime", ignoreCase = true) == true
    }
    return filtered
  }

  fun getSportsChannels(serverId: String?): List<LiveChannel> {
    if (serverId == null) return emptyList()
    val allChannels = liveChannelsCache[serverId] ?: emptyList()
    val sportsCategories = (liveCategoriesCache[serverId] ?: emptyList())
      .filter { it.name.contains("esporte", ignoreCase = true) || it.name.contains("sport", ignoreCase = true) || it.name.contains("futebol", ignoreCase = true) || it.name.contains("premiere", ignoreCase = true) }
      .map { it.id }
      .toSet()

    return allChannels.filter {
      sportsCategories.contains(it.categoryId) ||
        it.name.contains("sport", ignoreCase = true) ||
        it.name.contains("espn", ignoreCase = true) ||
        it.name.contains("premiere", ignoreCase = true) ||
        it.name.contains("combate", ignoreCase = true) ||
        it.name.contains("futebol", ignoreCase = true)
    }
  }

  suspend fun getSeriesEpisodes(server: ServerProfile?, seriesId: String): List<EpisodeItem> {
    if (server == null) return emptyList()
    val res = xtreamCodesClient.getSeriesEpisodes(server, seriesId)
    return if (res.isSuccess) res.getOrThrow() else emptyList()
  }

  fun clearCacheForServer(serverId: String) {
    liveChannelsCache.remove(serverId)
    liveCategoriesCache.remove(serverId)
    vodMoviesCache.remove(serverId)
    vodCategoriesCache.remove(serverId)
    seriesCache.remove(serverId)
    seriesCategoriesCache.remove(serverId)
  }
}
