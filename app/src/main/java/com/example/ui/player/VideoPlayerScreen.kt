package com.example.ui.player

import androidx.activity.compose.BackHandler
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material.icons.filled.Forward10
import androidx.compose.material.icons.filled.Pause
import androidx.compose.material.icons.filled.PlayArrow
import androidx.compose.material.icons.filled.Refresh
import androidx.compose.material.icons.filled.Replay10
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.Slider
import androidx.compose.material3.SliderDefaults
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.viewinterop.AndroidView
import androidx.media3.ui.PlayerView
import com.example.player.PlayerManager
import com.example.ui.theme.AccentGold
import com.example.ui.theme.RubyPrimary
import kotlinx.coroutines.delay

@Composable
fun VideoPlayerScreen(
  streamUrl: String,
  title: String,
  subtitle: String = "",
  isLive: Boolean = false,
  onBack: () -> Unit,
  onPlaybackProgress: (positionMs: Long, durationMs: Long) -> Unit = { _, _ -> },
  modifier: Modifier = Modifier
) {
  val context = LocalContext.current
  val playerManager = remember { PlayerManager(context) }
  val playerState by playerManager.uiState.collectAsState()

  var showControls by remember { mutableStateOf(true) }

  BackHandler {
    onPlaybackProgress(playerState.currentPositionMs, playerState.durationMs)
    playerManager.release()
    onBack()
  }

  LaunchedEffect(streamUrl) {
    playerManager.playStream(
      url = streamUrl,
      title = title,
      subtitle = subtitle,
      isLive = isLive
    )
  }

  // Auto-hide controls after 5 seconds
  LaunchedEffect(showControls, playerState.isPlaying) {
    if (showControls && playerState.isPlaying) {
      delay(5000)
      showControls = false
    }
  }

  DisposableEffect(Unit) {
    onDispose {
      playerManager.release()
    }
  }

  Box(
    modifier = modifier
      .fillMaxSize()
      .background(Color.Black)
      .clickable(
        interactionSource = remember { MutableInteractionSource() },
        indication = null
      ) {
        showControls = !showControls
      }
      .testTag("video_player_container")
  ) {
    // Media3 ExoPlayer View
    AndroidView(
      factory = { ctx ->
        PlayerView(ctx).apply {
          player = playerManager.getPlayer()
          useController = false
        }
      },
      modifier = Modifier.fillMaxSize()
    )

    // Loading overlay
    if (playerState.isLoading) {
      Box(
        modifier = Modifier.fillMaxSize(),
        contentAlignment = Alignment.Center
      ) {
        CircularProgressIndicator(color = AccentGold, modifier = Modifier.size(48.dp))
      }
    }

    // Error overlay with Retry button
    if (playerState.errorMessage != null) {
      Box(
        modifier = Modifier
          .fillMaxSize()
          .background(Color(0xD9000000))
          .padding(24.dp),
        contentAlignment = Alignment.Center
      ) {
        Column(horizontalAlignment = Alignment.CenterHorizontally) {
          Text(
            text = "Erro na Reprodução",
            color = Color(0xFFEF5350),
            fontWeight = FontWeight.Bold,
            fontSize = 18.sp
          )
          Spacer(modifier = Modifier.height(8.dp))
          Text(
            text = playerState.errorMessage!!,
            color = Color.White.copy(0.9f),
            fontSize = 13.sp
          )
          Spacer(modifier = Modifier.height(16.dp))
          Button(
            onClick = { playerManager.retry() },
            colors = ButtonDefaults.buttonColors(containerColor = RubyPrimary)
          ) {
            Icon(Icons.Default.Refresh, contentDescription = null, tint = Color.White)
            Spacer(modifier = Modifier.width(6.dp))
            Text("Tentar Novamente", color = Color.White)
          }
        }
      }
    }

    // Interactive Media Controls Overlay
    AnimatedVisibility(
      visible = showControls,
      enter = fadeIn(),
      exit = fadeOut(),
      modifier = Modifier.fillMaxSize()
    ) {
      Box(
        modifier = Modifier
          .fillMaxSize()
          .background(Color(0x59000000))
      ) {
        // Top Bar: Back button, Title & Subtitle
        Row(
          modifier = Modifier
            .fillMaxWidth()
            .align(Alignment.TopCenter)
            .background(
              Brush.verticalGradient(
                colors = listOf(Color(0xCC000000), Color.Transparent)
              )
            )
            .padding(16.dp),
          verticalAlignment = Alignment.CenterVertically
        ) {
          IconButton(
            onClick = {
              onPlaybackProgress(playerState.currentPositionMs, playerState.durationMs)
              playerManager.release()
              onBack()
            },
            modifier = Modifier.testTag("player_back_button")
          ) {
            Icon(Icons.Default.ArrowBack, contentDescription = "Voltar", tint = Color.White)
          }

          Spacer(modifier = Modifier.width(8.dp))

          Column {
            Text(
              text = title,
              color = Color.White,
              fontWeight = FontWeight.Bold,
              fontSize = 16.sp
            )
            if (subtitle.isNotBlank()) {
              Text(
                text = subtitle,
                color = AccentGold,
                fontSize = 12.sp
              )
            }
          }

          Spacer(modifier = Modifier.weight(1f))

          if (isLive) {
            Box(
              modifier = Modifier
                .clip(RoundedCornerShape(4.dp))
                .background(RubyPrimary)
                .padding(horizontal = 8.dp, vertical = 3.dp)
            ) {
              Text("AO VIVO", color = Color.White, fontWeight = FontWeight.Bold, fontSize = 11.sp)
            }
          }
        }

        // Center Action Buttons (Rewind, Play/Pause, Fast Forward)
        Row(
          modifier = Modifier
            .align(Alignment.Center)
            .padding(horizontal = 24.dp),
          verticalAlignment = Alignment.CenterVertically,
          horizontalArrangement = Arrangement.spacedBy(28.dp)
        ) {
          if (!isLive) {
            IconButton(
              onClick = { playerManager.seekBackward(10000) },
              modifier = Modifier
                .size(48.dp)
                .clip(CircleShape)
                .background(Color(0x40FFFFFF))
            ) {
              Icon(Icons.Default.Replay10, contentDescription = "Voltar 10s", tint = Color.White, modifier = Modifier.size(28.dp))
            }
          }

          IconButton(
            onClick = { playerManager.togglePlayPause() },
            modifier = Modifier
              .size(64.dp)
              .clip(CircleShape)
              .background(RubyPrimary)
              .testTag("play_pause_button")
          ) {
            Icon(
              imageVector = if (playerState.isPlaying) Icons.Default.Pause else Icons.Default.PlayArrow,
              contentDescription = if (playerState.isPlaying) "Pausar" else "Reproduzir",
              tint = Color.White,
              modifier = Modifier.size(36.dp)
            )
          }

          if (!isLive) {
            IconButton(
              onClick = { playerManager.seekForward(10000) },
              modifier = Modifier
                .size(48.dp)
                .clip(CircleShape)
                .background(Color(0x40FFFFFF))
            ) {
              Icon(Icons.Default.Forward10, contentDescription = "Avançar 10s", tint = Color.White, modifier = Modifier.size(28.dp))
            }
          }
        }

        // Bottom Progress Bar & Time (for VOD/Series)
        if (!isLive && playerState.durationMs > 0) {
          Column(
            modifier = Modifier
              .fillMaxWidth()
              .align(Alignment.BottomCenter)
              .background(
                Brush.verticalGradient(
                  colors = listOf(Color.Transparent, Color(0xCC000000))
                )
              )
              .padding(horizontal = 24.dp, vertical = 12.dp)
          ) {
            Slider(
              value = playerState.currentPositionMs.toFloat(),
              onValueChange = { targetPos ->
                playerManager.seekTo(targetPos.toLong())
              },
              valueRange = 0f..playerState.durationMs.toFloat(),
              colors = SliderDefaults.colors(
                thumbColor = RubyPrimary,
                activeTrackColor = RubyPrimary,
                inactiveTrackColor = Color.White.copy(0.3f)
              ),
              modifier = Modifier.fillMaxWidth()
            )

            Row(
              modifier = Modifier.fillMaxWidth(),
              horizontalArrangement = Arrangement.SpaceBetween
            ) {
              Text(
                text = formatDuration(playerState.currentPositionMs),
                color = Color.White,
                fontSize = 12.sp
              )
              Text(
                text = formatDuration(playerState.durationMs),
                color = Color.White.copy(0.7f),
                fontSize = 12.sp
              )
            }
          }
        }
      }
    }
  }
}

private fun formatDuration(ms: Long): String {
  val totalSec = ms / 1000
  val hours = totalSec / 3600
  val minutes = (totalSec % 3600) / 60
  val seconds = totalSec % 60
  return if (hours > 0) {
    String.format("%02d:%02d:%02d", hours, minutes, seconds)
  } else {
    String.format("%02d:%02d", minutes, seconds)
  }
}
