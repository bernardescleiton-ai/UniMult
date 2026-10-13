package com.example.ui.livetv

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Fullscreen
import androidx.compose.material.icons.filled.PlayArrow
import androidx.compose.material.icons.filled.Tv
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
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
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.viewinterop.AndroidView
import androidx.media3.ui.PlayerView
import coil.compose.AsyncImage
import com.example.domain.model.LiveChannel
import com.example.player.PlayerManager
import com.example.ui.theme.AccentGold
import com.example.ui.theme.DarkSurface
import com.example.ui.theme.RubyPrimary

import com.example.ui.components.EmptyCatalogState

@Composable
fun LiveTvScreen(
  channels: List<LiveChannel>,
  onPlayFullscreen: (channel: LiveChannel) -> Unit,
  onNavigateToServers: () -> Unit,
  modifier: Modifier = Modifier
) {
  if (channels.isEmpty()) {
    EmptyCatalogState(
      title = "Nenhum Canal Carregado",
      message = "Cadastre e ative seu servidor Xtream Codes para carregar a grade de canais ao vivo em tempo real.",
      onNavigateToServers = onNavigateToServers,
      modifier = modifier
    )
    return
  }

  val context = LocalContext.current
  val playerManager = remember { PlayerManager(context) }
  val playerState by playerManager.uiState.collectAsState()

  var selectedChannel by remember(channels) {
    mutableStateOf(channels.firstOrNull())
  }

  // Auto-play currently selected channel
  LaunchedEffect(selectedChannel) {
    selectedChannel?.let { ch ->
      if (ch.streamUrl.isNotBlank()) {
        playerManager.playStream(
          url = ch.streamUrl,
          title = ch.name,
          subtitle = ch.currentProgram ?: "",
          isLive = true
        )
      }
    }
  }

  DisposableEffect(Unit) {
    onDispose {
      playerManager.release()
    }
  }

  Row(
    modifier = modifier
      .fillMaxSize()
      .padding(horizontal = 16.dp, vertical = 8.dp)
  ) {
    // Left side: Channel List (Matching reference screenshot)
    Column(
      modifier = Modifier
        .weight(0.42f)
        .fillMaxHeight()
        .clip(RoundedCornerShape(12.dp))
        .background(Color(0x2E000000))
        .border(0.5.dp, Color(0x33FFFFFF), RoundedCornerShape(12.dp))
        .padding(10.dp)
    ) {
      Text(
        text = "Lista de Canais",
        color = Color.White,
        fontWeight = FontWeight.Bold,
        fontSize = 16.sp,
        modifier = Modifier.padding(bottom = 8.dp, start = 4.dp)
      )

      LazyColumn(
        modifier = Modifier
          .fillMaxWidth()
          .weight(1f),
        verticalArrangement = Arrangement.spacedBy(6.dp)
      ) {
        items(channels, key = { it.streamId }) { channel ->
          val isCurrent = channel.streamId == selectedChannel?.streamId

          Row(
            modifier = Modifier
              .fillMaxWidth()
              .clip(RoundedCornerShape(8.dp))
              .background(
                if (isCurrent) Color(0x59FF334B) else Color(0x1AFFFFFF)
              )
              .border(
                width = if (isCurrent) 1.dp else 0.dp,
                color = if (isCurrent) AccentGold else Color.Transparent,
                shape = RoundedCornerShape(8.dp)
              )
              .clickable { selectedChannel = channel }
              .padding(horizontal = 10.dp, vertical = 8.dp)
              .testTag("channel_item_${channel.streamId}"),
            verticalAlignment = Alignment.CenterVertically
          ) {
            // Channel logo or icon
            if (!channel.streamIcon.isNullOrBlank()) {
              AsyncImage(
                model = channel.streamIcon,
                contentDescription = null,
                contentScale = ContentScale.Crop,
                modifier = Modifier
                  .size(28.dp)
                  .clip(CircleShape)
              )
              Spacer(modifier = Modifier.width(8.dp))
            } else {
              Box(
                modifier = Modifier
                  .size(28.dp)
                  .clip(CircleShape)
                  .background(Color(0x33FFFFFF)),
                contentAlignment = Alignment.Center
              ) {
                Icon(Icons.Default.Tv, contentDescription = null, tint = Color.White, modifier = Modifier.size(16.dp))
              }
              Spacer(modifier = Modifier.width(8.dp))
            }

            Column(modifier = Modifier.weight(1f)) {
              Text(
                text = "${channel.num.ifBlank { "" }} ${channel.name}",
                color = Color.White,
                fontWeight = if (isCurrent) FontWeight.Bold else FontWeight.Medium,
                fontSize = 13.sp,
                maxLines = 1,
                overflow = TextOverflow.Ellipsis
              )
              if (!channel.currentProgram.isNullOrBlank()) {
                Text(
                  text = channel.currentProgram,
                  color = if (isCurrent) AccentGold else Color.White.copy(0.6f),
                  fontSize = 11.sp,
                  maxLines = 1,
                  overflow = TextOverflow.Ellipsis
                )
              }
            }

            if (isCurrent) {
              Icon(
                imageVector = Icons.Default.PlayArrow,
                contentDescription = "Reproduzindo",
                tint = AccentGold,
                modifier = Modifier.size(18.dp)
              )
            }
          }
        }
      }

      Spacer(modifier = Modifier.height(6.dp))
      Text(
        text = "Pressione [MENU] para pesquisar canais",
        color = Color.White.copy(alpha = 0.5f),
        fontSize = 11.sp,
        modifier = Modifier.padding(start = 4.dp)
      )
    }

    Spacer(modifier = Modifier.width(14.dp))

    // Right side: Live Player Preview & Info
    Column(
      modifier = Modifier
        .weight(0.58f)
        .fillMaxHeight()
    ) {
      Box(
        modifier = Modifier
          .fillMaxWidth()
          .weight(0.75f)
          .clip(RoundedCornerShape(12.dp))
          .background(Color.Black)
          .border(1.dp, Color(0x33FFFFFF), RoundedCornerShape(12.dp))
      ) {
        AndroidView(
          factory = { ctx ->
            PlayerView(ctx).apply {
              player = playerManager.getPlayer()
              useController = false
            }
          },
          modifier = Modifier.fillMaxSize()
        )

        // Overlay status & fullscreen action
        Row(
          modifier = Modifier
            .fillMaxWidth()
            .align(Alignment.TopCenter)
            .padding(10.dp),
          horizontalArrangement = Arrangement.SpaceBetween,
          verticalAlignment = Alignment.CenterVertically
        ) {
          Box(
            modifier = Modifier
              .clip(RoundedCornerShape(4.dp))
              .background(RubyPrimary)
              .padding(horizontal = 8.dp, vertical = 3.dp)
          ) {
            Text("AO VIVO", color = Color.White, fontWeight = FontWeight.ExtraBold, fontSize = 11.sp)
          }

          selectedChannel?.let { ch ->
            IconButton(
              onClick = { onPlayFullscreen(ch) },
              modifier = Modifier
                .size(36.dp)
                .clip(CircleShape)
                .background(Color(0x80000000))
                .testTag("fullscreen_button")
            ) {
              Icon(
                imageVector = Icons.Default.Fullscreen,
                contentDescription = "Tela Cheia",
                tint = Color.White
              )
            }
          }
        }

        if (playerState.isLoading) {
          Box(
            modifier = Modifier
              .fillMaxSize()
              .background(Color(0x66000000)),
            contentAlignment = Alignment.Center
          ) {
            CircularProgressIndicator(color = AccentGold, modifier = Modifier.size(36.dp))
          }
        }

        if (playerState.errorMessage != null) {
          Box(
            modifier = Modifier
              .fillMaxSize()
              .background(Color(0xB3000000))
              .padding(16.dp),
            contentAlignment = Alignment.Center
          ) {
            Text(
              text = playerState.errorMessage!!,
              color = Color.White,
              fontSize = 12.sp,
              fontWeight = FontWeight.Medium
            )
          }
        }
      }

      Spacer(modifier = Modifier.height(10.dp))

      // Current Program Info Banner
      selectedChannel?.let { ch ->
        Box(
          modifier = Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(10.dp))
            .background(Color(0x22FFFFFF))
            .border(0.5.dp, Color(0x33FFFFFF), RoundedCornerShape(10.dp))
            .padding(12.dp)
        ) {
          Column {
            Text(
              text = ch.name,
              color = Color.White,
              fontWeight = FontWeight.Bold,
              fontSize = 16.sp
            )
            Spacer(modifier = Modifier.height(2.dp))
            Text(
              text = ch.currentProgram ?: "Programação ao vivo",
              color = AccentGold,
              fontSize = 13.sp
            )
          }
        }
      }
    }
  }
}
