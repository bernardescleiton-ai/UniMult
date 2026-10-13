package com.example.player

import android.content.Context
import androidx.media3.common.MediaItem
import androidx.media3.common.PlaybackException
import androidx.media3.common.Player
import androidx.media3.datasource.DefaultDataSource
import androidx.media3.datasource.DefaultHttpDataSource
import androidx.media3.datasource.HttpDataSource
import androidx.media3.exoplayer.ExoPlayer
import androidx.media3.exoplayer.source.DefaultMediaSourceFactory
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.Job
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.isActive
import kotlinx.coroutines.launch

data class PlayerUiState(
  val isPlaying: Boolean = false,
  val isLoading: Boolean = false,
  val currentPositionMs: Long = 0L,
  val durationMs: Long = 0L,
  val errorMessage: String? = null,
  val title: String = "",
  val subtitle: String = "",
  val isLive: Boolean = false
)

class PlayerManager(private val context: Context) {

  private var exoPlayer: ExoPlayer? = null
  private val scope = CoroutineScope(Dispatchers.Main)
  private var progressJob: Job? = null

  private val _uiState = MutableStateFlow(PlayerUiState())
  val uiState: StateFlow<PlayerUiState> = _uiState.asStateFlow()

  fun getPlayer(): ExoPlayer {
    if (exoPlayer == null) {
      val httpDataSourceFactory = DefaultHttpDataSource.Factory()
        .setUserAgent("Mozilla/5.0 (Windows NT 10.0; Win64; x64) AppleWebKit/537.36 (KHTML, like Gecko) Chrome/120.0.0.0 Safari/537.36")
        .setAllowCrossProtocolRedirects(true)
        .setConnectTimeoutMs(15000)
        .setReadTimeoutMs(20000)

      val dataSourceFactory = DefaultDataSource.Factory(context, httpDataSourceFactory)
      val mediaSourceFactory = DefaultMediaSourceFactory(dataSourceFactory)

      val player = ExoPlayer.Builder(context)
        .setMediaSourceFactory(mediaSourceFactory)
        .build().apply {
          playWhenReady = true
          addListener(object : Player.Listener {
            override fun onPlaybackStateChanged(playbackState: Int) {
              when (playbackState) {
                Player.STATE_BUFFERING -> {
                  _uiState.value = _uiState.value.copy(isLoading = true, errorMessage = null)
                }
                Player.STATE_READY -> {
                  val dur = if (duration > 0) duration else 0L
                  _uiState.value = _uiState.value.copy(
                    isLoading = false,
                    durationMs = dur,
                    errorMessage = null
                  )
                }
                Player.STATE_ENDED -> {
                  _uiState.value = _uiState.value.copy(isLoading = false, isPlaying = false)
                }
                Player.STATE_IDLE -> {
                  _uiState.value = _uiState.value.copy(isLoading = false)
                }
              }
            }

            override fun onIsPlayingChanged(isPlaying: Boolean) {
              _uiState.value = _uiState.value.copy(isPlaying = isPlaying)
            }

            override fun onPlayerError(error: PlaybackException) {
              val cause = error.cause
              val is403 = (cause is HttpDataSource.InvalidResponseCodeException && cause.responseCode == 403) ||
                error.errorCode == PlaybackException.ERROR_CODE_IO_BAD_HTTP_STATUS

              val friendlyMsg = if (is403) {
                "Acesso restrito ou fluxo indisponível (HTTP 403). Conecte seu servidor Xtream para assistir."
              } else when (error.errorCode) {
                PlaybackException.ERROR_CODE_IO_NETWORK_CONNECTION_FAILED -> "Falha de rede ou servidor inacessível."
                PlaybackException.ERROR_CODE_DECODING_FAILED -> "Formato/codec de vídeo incompatível."
                else -> error.localizedMessage ?: "Erro ao reproduzir fluxo de vídeo."
              }
              _uiState.value = _uiState.value.copy(isLoading = false, errorMessage = friendlyMsg)
            }
          })
        }
      exoPlayer = player
      startProgressTracking()
    }
    return exoPlayer!!
  }

  fun playStream(url: String, title: String, subtitle: String = "", isLive: Boolean = false) {
    if (url.isBlank()) {
      _uiState.value = _uiState.value.copy(
        isLoading = false,
        errorMessage = "Endereço do canal ou vídeo não especificado."
      )
      return
    }

    val player = getPlayer()
    _uiState.value = PlayerUiState(
      isLoading = true,
      title = title,
      subtitle = subtitle,
      isLive = isLive,
      errorMessage = null
    )

    try {
      val mediaItem = MediaItem.fromUri(url)
      player.setMediaItem(mediaItem)
      player.prepare()
      player.play()
    } catch (e: Exception) {
      _uiState.value = _uiState.value.copy(
        isLoading = false,
        errorMessage = "Falha ao carregar endereço de mídia: ${e.message}"
      )
    }
  }

  fun togglePlayPause() {
    exoPlayer?.let {
      if (it.isPlaying) {
        it.pause()
      } else {
        it.play()
      }
    }
  }

  fun seekTo(positionMs: Long) {
    exoPlayer?.seekTo(positionMs)
  }

  fun seekForward(deltaMs: Long = 10000L) {
    exoPlayer?.let {
      val target = (it.currentPosition + deltaMs).coerceAtMost(it.duration)
      it.seekTo(target)
    }
  }

  fun seekBackward(deltaMs: Long = 10000L) {
    exoPlayer?.let {
      val target = (it.currentPosition - deltaMs).coerceAtLeast(0L)
      it.seekTo(target)
    }
  }

  fun retry() {
    exoPlayer?.let {
      _uiState.value = _uiState.value.copy(isLoading = true, errorMessage = null)
      it.prepare()
      it.play()
    }
  }

  private fun startProgressTracking() {
    progressJob?.cancel()
    progressJob = scope.launch {
      while (isActive) {
        exoPlayer?.let { player ->
          if (player.isPlaying) {
            _uiState.value = _uiState.value.copy(
              currentPositionMs = player.currentPosition,
              durationMs = if (player.duration > 0) player.duration else 0L
            )
          }
        }
        delay(1000)
      }
    }
  }

  fun release() {
    progressJob?.cancel()
    exoPlayer?.stop()
    exoPlayer?.release()
    exoPlayer = null
  }
}
