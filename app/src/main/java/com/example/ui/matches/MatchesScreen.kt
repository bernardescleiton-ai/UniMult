package com.example.ui.matches

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
import androidx.compose.material.icons.filled.PlayArrow
import androidx.compose.material.icons.filled.SportsSoccer
import androidx.compose.material.icons.filled.Tv
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import coil.compose.AsyncImage
import com.example.domain.model.LiveChannel
import com.example.ui.components.EmptyCatalogState
import com.example.ui.theme.AccentGold
import com.example.ui.theme.RubyPrimary

@Composable
fun MatchesScreen(
  sportsChannels: List<LiveChannel>,
  onChannelClick: (LiveChannel) -> Unit,
  onNavigateToServers: () -> Unit,
  modifier: Modifier = Modifier
) {
  if (sportsChannels.isEmpty()) {
    EmptyCatalogState(
      title = "Nenhum Canal Esportivo Disponível",
      message = "Conecte seu servidor Xtream Codes para puxar as transmissões ao vivo de esportes, campeonatos e jogos.",
      onNavigateToServers = onNavigateToServers,
      modifier = modifier
    )
    return
  }

  Column(
    modifier = modifier
      .fillMaxSize()
      .padding(horizontal = 16.dp, vertical = 6.dp)
  ) {
    // Header
    Row(
      modifier = Modifier.fillMaxWidth(),
      horizontalArrangement = Arrangement.SpaceBetween,
      verticalAlignment = Alignment.CenterVertically
    ) {
      Row(verticalAlignment = Alignment.CenterVertically) {
        Icon(
          imageVector = Icons.Default.SportsSoccer,
          contentDescription = null,
          tint = Color(0xFF66BB6A),
          modifier = Modifier.size(24.dp)
        )
        Spacer(modifier = Modifier.width(8.dp))
        Text(
          text = "Jogos e Canais de Esportes Ao Vivo",
          color = Color.White,
          fontWeight = FontWeight.Bold,
          fontSize = 18.sp
        )
      }

      Text(
        text = "${sportsChannels.size} canais disponíveis",
        color = Color.White.copy(alpha = 0.7f),
        fontSize = 12.sp
      )
    }

    Spacer(modifier = Modifier.height(12.dp))

    LazyColumn(
      modifier = Modifier.fillMaxSize(),
      verticalArrangement = Arrangement.spacedBy(8.dp)
    ) {
      items(sportsChannels, key = { it.streamId }) { channel ->
        Card(
          shape = RoundedCornerShape(10.dp),
          colors = CardDefaults.cardColors(containerColor = Color(0x2BFFFFFF)),
          border = androidx.compose.foundation.BorderStroke(0.5.dp, Color(0x33FFFFFF)),
          modifier = Modifier
            .fillMaxWidth()
            .clickable { onChannelClick(channel) }
            .testTag("sports_channel_${channel.streamId}")
        ) {
          Row(
            modifier = Modifier
              .fillMaxWidth()
              .padding(horizontal = 16.dp, vertical = 12.dp),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
          ) {
            Row(
              verticalAlignment = Alignment.CenterVertically,
              modifier = Modifier.weight(1f)
            ) {
              if (!channel.streamIcon.isNullOrBlank()) {
                AsyncImage(
                  model = channel.streamIcon,
                  contentDescription = channel.name,
                  contentScale = ContentScale.Crop,
                  modifier = Modifier
                    .size(36.dp)
                    .clip(CircleShape)
                )
              } else {
                Box(
                  modifier = Modifier
                    .size(36.dp)
                    .clip(CircleShape)
                    .background(Color(0x33FFFFFF)),
                  contentAlignment = Alignment.Center
                ) {
                  Icon(Icons.Default.Tv, contentDescription = null, tint = Color.White, modifier = Modifier.size(20.dp))
                }
              }

              Spacer(modifier = Modifier.width(12.dp))

              Column {
                Text(
                  text = channel.name,
                  color = Color.White,
                  fontWeight = FontWeight.Bold,
                  fontSize = 15.sp,
                  maxLines = 1,
                  overflow = TextOverflow.Ellipsis
                )
                if (!channel.currentProgram.isNullOrBlank()) {
                  Text(
                    text = channel.currentProgram,
                    color = AccentGold,
                    fontSize = 12.sp,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis
                  )
                }
              }
            }

            Box(
              modifier = Modifier
                .clip(RoundedCornerShape(6.dp))
                .background(RubyPrimary)
                .clickable { onChannelClick(channel) }
                .padding(horizontal = 12.dp, vertical = 6.dp)
            ) {
              Row(verticalAlignment = Alignment.CenterVertically) {
                Icon(Icons.Default.PlayArrow, contentDescription = null, tint = Color.White, modifier = Modifier.size(16.dp))
                Spacer(modifier = Modifier.width(4.dp))
                Text("Assistir", color = Color.White, fontSize = 11.sp, fontWeight = FontWeight.Bold)
              }
            }
          }
        }
      }
    }
  }
}
