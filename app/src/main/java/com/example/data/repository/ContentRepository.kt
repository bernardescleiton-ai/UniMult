package com.example.data.repository

import com.example.data.remote.XtreamCodesClient
import com.example.domain.model.AccountInfo
import com.example.domain.model.Category
import com.example.domain.model.EpisodeItem
import com.example.domain.model.LiveChannel
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

  // Reactive StateFlows observed by the UI
  private val _liveChannels = MutableStateFlow<List<LiveChannel>>(emptyList())
  val liveChannels: StateFlow<List<LiveChannel>> = _liveChannels.asStateFlow()

  private val _liveCategories = MutableStateFlow<List<Category>>(emptyList())
  val liveCategories: StateFlow<List<Category>> = _liveCategories.asStateFlow()

  private val _vodMovies = MutableStateFlow<List<VodItem>>(emptyList())
  val vodMovies: StateFlow<List<VodItem>> = _vodMovies.asStateFlow()

  private val _vodCategories = MutableStateFlow<List<Category>>(emptyList())
  val vodCategories: StateFlow<List<Category>> = _vodCategories.asStateFlow()

  private val _series = MutableStateFlow<List<SeriesItem>>(emptyList())
  val series: StateFlow<List<SeriesItem>> = _series.asStateFlow()

  private val _seriesCategories = MutableStateFlow<List<Category>>(emptyList())
  val seriesCategories: StateFlow<List<Category>> = _seriesCategories.asStateFlow()

  private val _kidsContent = MutableStateFlow<List<VodItem>>(emptyList())
  val kidsContent: StateFlow<List<VodItem>> = _kidsContent.asStateFlow()

  private val _animeContent = MutableStateFlow<List<VodItem>>(emptyList())
  val animeContent: StateFlow<List<VodItem>> = _animeContent.asStateFlow()

  private val _sportsChannels = MutableStateFlow<List<LiveChannel>>(emptyList())
  val sportsChannels: StateFlow<List<LiveChannel>> = _sportsChannels.asStateFlow()

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
      if (newServer == null) {
        publishEmptyContent()
      } else {
        // If cached for this server, publish cached first
        publishFromCache(newServer.id)
      }
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
      val err = authResult.exceptionOrNull()?.message ?: "Falha ao autenticar no servidor."
      _errorMessage.value = err
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

    // Publish all contents reactively to StateFlows
    publishFromCache(server.id)
    _isLoading.value = false
  }

  private fun publishFromCache(serverId: String) {
    val live = liveChannelsCache[serverId] ?: emptyList()
    val liveCats = liveCategoriesCache[serverId] ?: emptyList()
    val vod = vodMoviesCache[serverId] ?: emptyList()
    val vodCats = vodCategoriesCache[serverId] ?: emptyList()
    val ser = seriesCache[serverId] ?: emptyList()
    val serCats = seriesCategoriesCache[serverId] ?: emptyList()

    _liveChannels.value = live
    _liveCategories.value = liveCats
    _vodMovies.value = vod
    _vodCategories.value = vodCats
    _series.value = ser
    _seriesCategories.value = serCats

    // Filter Kids
    val kidsCats = vodCats.filter {
      it.name.contains("kid", ignoreCase = true) ||
      it.name.contains("infantil", ignoreCase = true) ||
      it.name.contains("desenho", ignoreCase = true)
    }.map { it.id }.toSet()
    _kidsContent.value = vod.filter {
      kidsCats.contains(it.categoryId) ||
      it.name.contains("kid", ignoreCase = true) ||
      it.name.contains("infantil", ignoreCase = true) ||
      it.genre?.contains("infantil", ignoreCase = true) == true
    }

    // Filter Anime
    val animeCats = vodCats.filter {
      it.name.contains("anime", ignoreCase = true) ||
      it.name.contains("animação", ignoreCase = true) ||
      it.name.contains("animacao", ignoreCase = true)
    }.map { it.id }.toSet()
    _animeContent.value = vod.filter {
      animeCats.contains(it.categoryId) ||
      it.name.contains("anime", ignoreCase = true) ||
      it.genre?.contains("anime", ignoreCase = true) == true
    }

    // Filter Sports
    val sportsCats = liveCats.filter {
      it.name.contains("esporte", ignoreCase = true) ||
      it.name.contains("sport", ignoreCase = true) ||
      it.name.contains("futebol", ignoreCase = true) ||
      it.name.contains("premiere", ignoreCase = true)
    }.map { it.id }.toSet()
    _sportsChannels.value = live.filter {
      sportsCats.contains(it.categoryId) ||
      it.name.contains("sport", ignoreCase = true) ||
      it.name.contains("espn", ignoreCase = true) ||
      it.name.contains("premiere", ignoreCase = true) ||
      it.name.contains("combate", ignoreCase = true) ||
      it.name.contains("futebol", ignoreCase = true)
    }
  }

  private fun publishEmptyContent() {
    _liveChannels.value = emptyList()
    _liveCategories.value = emptyList()
    _vodMovies.value = emptyList()
    _vodCategories.value = emptyList()
    _series.value = emptyList()
    _seriesCategories.value = emptyList()
    _kidsContent.value = emptyList()
    _animeContent.value = emptyList()
    _sportsChannels.value = emptyList()
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

  fun getKidsContent(serverId: String?): List<VodItem> = _kidsContent.value

  fun getAnimeContent(serverId: String?): List<VodItem> = _animeContent.value

  fun getSportsChannels(serverId: String?): List<LiveChannel> = _sportsChannels.value

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
    if (currentServerId == serverId) {
      publishEmptyContent()
    }
  }
}
