package com.example

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.BackHandler
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.animation.AnimatedContent
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.togetherWith
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.safeDrawingPadding
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.Scaffold
import androidx.compose.material3.SnackbarHost
import androidx.compose.material3.SnackbarHostState
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp
import androidx.lifecycle.viewmodel.compose.viewModel
import com.example.data.local.AppDatabase
import com.example.data.remote.XtreamCodesClient
import com.example.data.repository.ContentRepository
import com.example.data.repository.FavoriteRepository
import com.example.data.repository.HistoryRepository
import com.example.data.repository.ServerRepository
import com.example.ui.ActivePlayback
import com.example.ui.MainViewModel
import com.example.ui.components.AppNavSection
import com.example.ui.components.CategoryTabsRow
import com.example.ui.components.SearchModalDialog
import com.example.ui.components.TopHeaderBar
import com.example.ui.history.HistoryFavoritesScreen
import com.example.ui.home.HomeScreen
import com.example.ui.livetv.LiveTvScreen
import com.example.ui.matches.MatchesScreen
import com.example.ui.player.VideoPlayerScreen
import com.example.ui.profile.UserProfileScreen
import com.example.ui.servers.ServersScreen
import com.example.ui.servers.ServersViewModel
import com.example.ui.theme.AccentGold
import com.example.ui.theme.BurgundyDeep
import com.example.ui.theme.BurgundyGlow
import com.example.ui.theme.BurgundyMedium
import com.example.ui.theme.MyApplicationTheme
import com.example.ui.vod.ExploreScreen
import com.example.ui.vod.VodScreen

class MainActivity : ComponentActivity() {
  override fun onCreate(savedInstanceState: Bundle?) {
    super.onCreate(savedInstanceState)
    enableEdgeToEdge()

    // Initialize dependencies
    val database = AppDatabase.getDatabase(applicationContext)
    val xtreamClient = XtreamCodesClient()
    val serverRepo = ServerRepository(database.serverProfileDao(), xtreamClient)
    val contentRepo = ContentRepository(xtreamClient)
    val historyRepo = HistoryRepository(database.historyDao())
    val favoriteRepo = FavoriteRepository(database.favoriteDao())

    setContent {
      MyApplicationTheme {
        UniMultApp(
          serverRepo = serverRepo,
          contentRepo = contentRepo,
          historyRepo = historyRepo,
          favoriteRepo = favoriteRepo
        )
      }
    }
  }
}

@Composable
fun UniMultApp(
  serverRepo: ServerRepository,
  contentRepo: ContentRepository,
  historyRepo: HistoryRepository,
  favoriteRepo: FavoriteRepository
) {
  val mainViewModel = remember {
    MainViewModel(serverRepo, contentRepo, historyRepo, favoriteRepo)
  }
  val serversViewModel = remember {
    ServersViewModel(serverRepo, contentRepo)
  }

  val activeServer by mainViewModel.activeServer.collectAsState()
  val allServers by mainViewModel.allServers.collectAsState()
  val selectedSection by mainViewModel.selectedSection.collectAsState()
  val activePlayback by mainViewModel.activePlayback.collectAsState()
  val isLoading by mainViewModel.isLoading.collectAsState()
  val accountInfo by mainViewModel.accountInfo.collectAsState()

  val historyList by mainViewModel.historyList.collectAsState()
  val favoritesList by mainViewModel.favoritesList.collectAsState()

  val testStatus by serversViewModel.testStatus.collectAsState()
  val userMessage by serversViewModel.userMessage.collectAsState()

  val snackbarHostState = remember { SnackbarHostState() }
  var showSearchModal by remember { mutableStateOf(false) }
  var showProfileScreen by remember { mutableStateOf(false) }
  var showHistoryScreen by remember { mutableStateOf(false) }

  // Show user messages in snackbar
  LaunchedEffect(userMessage) {
    userMessage?.let { msg ->
      snackbarHostState.showSnackbar(msg)
      serversViewModel.clearUserMessage()
    }
  }

  // Handle hardware / gesture back button
  BackHandler(enabled = activePlayback != null || showProfileScreen || showHistoryScreen || selectedSection != AppNavSection.DESTAQUES) {
    when {
      activePlayback != null -> mainViewModel.stopPlayback()
      showProfileScreen -> showProfileScreen = false
      showHistoryScreen -> showHistoryScreen = false
      selectedSection != AppNavSection.DESTAQUES -> mainViewModel.setSection(AppNavSection.DESTAQUES)
    }
  }

  // Fullscreen video player has precedence
  if (activePlayback != null) {
    val playback = activePlayback!!
    VideoPlayerScreen(
      streamUrl = playback.streamUrl,
      title = playback.title,
      subtitle = playback.subtitle,
      isLive = playback.isLive,
      onBack = { mainViewModel.stopPlayback() },
      onPlaybackProgress = { pos, dur ->
        mainViewModel.updatePlaybackProgress(pos, dur)
      }
    )
    return
  }

  Scaffold(
    snackbarHost = { SnackbarHost(snackbarHostState) },
    containerColor = Color.Transparent,
    modifier = Modifier.fillMaxSize()
  ) { innerPadding ->
    // Atmospheric dark wine cinema gradient background
    Box(
      modifier = Modifier
        .fillMaxSize()
        .background(
          Brush.verticalGradient(
            colors = listOf(
              BurgundyGlow.copy(alpha = 0.85f),
              BurgundyMedium,
              BurgundyDeep
            )
          )
        )
        .safeDrawingPadding()
    ) {
      Column(modifier = Modifier.fillMaxSize()) {
        // Top Header Bar (brand logo, search, history, servers, user profile, clock)
        TopHeaderBar(
          activeServer = activeServer,
          onSearchClick = { showSearchModal = true },
          onFilterClick = {
            showProfileScreen = false
            showHistoryScreen = false
            mainViewModel.setSection(AppNavSection.EXPLORAR)
          },
          onHistoryClick = {
            showProfileScreen = false
            showHistoryScreen = !showHistoryScreen
          },
          onProfileClick = {
            showHistoryScreen = false
            showProfileScreen = !showProfileScreen
          },
          onServersClick = {
            showProfileScreen = false
            showHistoryScreen = false
            mainViewModel.setSection(AppNavSection.SERVIDORES)
          }
        )

        // Top Category navigation tabs
        CategoryTabsRow(
          selectedSection = selectedSection,
          onSectionSelected = { sec ->
            showProfileScreen = false
            showHistoryScreen = false
            mainViewModel.setSection(sec)
          }
        )

        // Loading indicator bar
        if (isLoading) {
          Box(
            modifier = Modifier
              .fillMaxSize()
              .background(Color(0x33000000)),
            contentAlignment = Alignment.Center
          ) {
            CircularProgressIndicator(color = AccentGold)
          }
        }

        // Main content router
        Box(modifier = Modifier.weight(1f)) {
          when {
            showProfileScreen -> {
              UserProfileScreen(
                activeServer = activeServer,
                accountInfo = accountInfo,
                onNavigateToServers = {
                  showProfileScreen = false
                  mainViewModel.setSection(AppNavSection.SERVIDORES)
                },
                onNavigateToHistory = {
                  showProfileScreen = false
                  showHistoryScreen = true
                },
                onNavigateToFavorites = {
                  showProfileScreen = false
                  showHistoryScreen = true
                }
              )
            }
            showHistoryScreen -> {
              HistoryFavoritesScreen(
                historyItems = historyList,
                favoriteItems = favoritesList,
                serverName = activeServer?.name ?: "Padrão / Demonstração",
                onClearHistory = { mainViewModel.clearHistory() },
                onItemClick = { streamId, title ->
                  val movie = mainViewModel.getMovies().firstOrNull { it.streamId == streamId }
                  val ch = mainViewModel.getLiveChannels().firstOrNull { it.streamId == streamId }
                  val streamUrl = movie?.streamUrl ?: ch?.streamUrl ?: ""
                  if (streamUrl.isNotBlank()) {
                    mainViewModel.startPlayback(
                      ActivePlayback(
                        streamUrl = streamUrl,
                        title = title,
                        streamId = streamId,
                        posterUrl = movie?.streamIcon ?: ch?.streamIcon
                      )
                    )
                  }
                }
              )
            }
            else -> {
              AnimatedContent(
                targetState = selectedSection,
                transitionSpec = { fadeIn() togetherWith fadeOut() },
                label = "nav_section_animation"
              ) { section ->
                when (section) {
                  AppNavSection.TV -> {
                    LiveTvScreen(
                      channels = mainViewModel.getLiveChannels(),
                      onPlayFullscreen = { ch ->
                        mainViewModel.startPlayback(
                          ActivePlayback(
                            streamUrl = ch.streamUrl,
                            title = ch.name,
                            subtitle = ch.currentProgram ?: "",
                            isLive = true,
                            streamId = ch.streamId,
                            posterUrl = ch.streamIcon
                          )
                        )
                      },
                      onNavigateToServers = { mainViewModel.setSection(AppNavSection.SERVIDORES) }
                    )
                  }
                  AppNavSection.JOGOS -> {
                    MatchesScreen(
                      sportsChannels = mainViewModel.getSportsChannels(),
                      onChannelClick = { ch ->
                        mainViewModel.startPlayback(
                          ActivePlayback(
                            streamUrl = ch.streamUrl,
                            title = ch.name,
                            subtitle = "Transmissão Esportiva",
                            isLive = true,
                            streamId = ch.streamId,
                            posterUrl = ch.streamIcon
                          )
                        )
                      },
                      onNavigateToServers = { mainViewModel.setSection(AppNavSection.SERVIDORES) }
                    )
                  }
                  AppNavSection.DESTAQUES -> {
                    HomeScreen(
                      highlights = mainViewModel.getMovies(),
                      onItemClick = { vod ->
                        mainViewModel.startPlayback(
                          ActivePlayback(
                            streamUrl = vod.streamUrl,
                            title = vod.name,
                            subtitle = vod.year ?: "",
                            streamId = vod.streamId,
                            posterUrl = vod.streamIcon
                          )
                        )
                      },
                      onNavigateToServers = { mainViewModel.setSection(AppNavSection.SERVIDORES) }
                    )
                  }
                  AppNavSection.FILMES -> {
                    VodScreen(
                      sectionTitle = "Filmes",
                      items = mainViewModel.getMovies(),
                      categories = mainViewModel.getVodCategories(),
                      onVodClick = { vod ->
                        mainViewModel.startPlayback(
                          ActivePlayback(
                            streamUrl = vod.streamUrl,
                            title = vod.name,
                            subtitle = vod.year ?: "",
                            streamId = vod.streamId,
                            posterUrl = vod.streamIcon
                          )
                        )
                      },
                      onNavigateToServers = { mainViewModel.setSection(AppNavSection.SERVIDORES) }
                    )
                  }
                  AppNavSection.SERIES -> {
                    VodScreen(
                      sectionTitle = "Séries",
                      items = emptyList(),
                      seriesItems = mainViewModel.getSeries(),
                      categories = mainViewModel.getSeriesCategories(),
                      onVodClick = {},
                      onSeriesClick = { series ->
                        // Episode stream will be requested or played
                        val stream = series.cover ?: ""
                        mainViewModel.startPlayback(
                          ActivePlayback(
                            streamUrl = stream,
                            title = series.name,
                            subtitle = series.genre ?: "Série",
                            streamId = series.seriesId,
                            posterUrl = series.cover
                          )
                        )
                      },
                      onNavigateToServers = { mainViewModel.setSection(AppNavSection.SERVIDORES) }
                    )
                  }
                  AppNavSection.KIDS -> {
                    VodScreen(
                      sectionTitle = "KIDS",
                      items = mainViewModel.getKids(),
                      onVodClick = { vod ->
                        mainViewModel.startPlayback(
                          ActivePlayback(
                            streamUrl = vod.streamUrl,
                            title = vod.name,
                            subtitle = "KIDS",
                            streamId = vod.streamId,
                            posterUrl = vod.streamIcon
                          )
                        )
                      },
                      onNavigateToServers = { mainViewModel.setSection(AppNavSection.SERVIDORES) }
                    )
                  }
                  AppNavSection.ANIME -> {
                    VodScreen(
                      sectionTitle = "ANIME",
                      items = mainViewModel.getAnime(),
                      onVodClick = { vod ->
                        mainViewModel.startPlayback(
                          ActivePlayback(
                            streamUrl = vod.streamUrl,
                            title = vod.name,
                            subtitle = "ANIME",
                            streamId = vod.streamId,
                            posterUrl = vod.streamIcon
                          )
                        )
                      },
                      onNavigateToServers = { mainViewModel.setSection(AppNavSection.SERVIDORES) }
                    )
                  }
                  AppNavSection.EXPLORAR -> {
                    ExploreScreen(
                      allItems = mainViewModel.getMovies(),
                      onVodClick = { vod ->
                        mainViewModel.startPlayback(
                          ActivePlayback(
                            streamUrl = vod.streamUrl,
                            title = vod.name,
                            subtitle = vod.year ?: "",
                            streamId = vod.streamId,
                            posterUrl = vod.streamIcon
                          )
                        )
                      },
                      onNavigateToServers = { mainViewModel.setSection(AppNavSection.SERVIDORES) }
                    )
                  }
                  AppNavSection.SERVIDORES -> {
                    ServersScreen(
                      servers = allServers,
                      activeServer = activeServer,
                      testStatus = testStatus,
                      onSelectActiveServer = { serversViewModel.selectActiveServer(it) },
                      onDeleteServer = { id, name -> serversViewModel.deleteServer(id, name) },
                      onTestServer = { serversViewModel.testExistingServer(it) },
                      onDraftTest = { url, user, pass -> serversViewModel.testDraftServer(url, user, pass) },
                      onSaveServer = { id, name, url, user, pass, makeActive ->
                        serversViewModel.saveServer(
                          id = id,
                          name = name,
                          rawUrl = url,
                          username = user,
                          password = pass,
                          makeActive = makeActive,
                          onSuccess = { serversViewModel.resetTestStatus() }
                        )
                      }
                    )
                  }
                }
              }
            }
          }
        }
      }
    }

    // Quick Search Modal Dialog
    if (showSearchModal) {
      SearchModalDialog(
        allMovies = mainViewModel.getMovies(),
        allChannels = mainViewModel.getLiveChannels(),
        onSelectMovie = { movie ->
          showSearchModal = false
          mainViewModel.startPlayback(
            ActivePlayback(
              streamUrl = movie.streamUrl,
              title = movie.name,
              subtitle = movie.year ?: "",
              streamId = movie.streamId,
              posterUrl = movie.streamIcon
            )
          )
        },
        onSelectChannel = { channel ->
          showSearchModal = false
          mainViewModel.startPlayback(
            ActivePlayback(
              streamUrl = channel.streamUrl,
              title = channel.name,
              subtitle = channel.currentProgram ?: "",
              isLive = true,
              streamId = channel.streamId,
              posterUrl = channel.streamIcon
            )
          )
        },
        onDismiss = { showSearchModal = false }
      )
    }
  }
}
