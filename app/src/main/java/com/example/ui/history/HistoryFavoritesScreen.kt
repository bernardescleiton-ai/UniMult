package com.example.ui.history

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
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.lazy.grid.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.Edit
import androidx.compose.material.icons.filled.History
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.data.local.entity.FavoriteEntity
import com.example.data.local.entity.HistoryEntity
import com.example.ui.components.ContentPosterCard
import com.example.ui.theme.AccentGold
import com.example.ui.theme.RubyPrimary

@Composable
fun HistoryFavoritesScreen(
  historyItems: List<HistoryEntity>,
  favoriteItems: List<FavoriteEntity>,
  serverName: String,
  onClearHistory: () -> Unit,
  onItemClick: (streamId: String, title: String) -> Unit,
  modifier: Modifier = Modifier
) {
  var selectedTab by remember { mutableStateOf("HISTORICO") }

  Row(
    modifier = modifier
      .fillMaxSize()
      .padding(horizontal = 16.dp, vertical = 6.dp)
  ) {
    // Left side vertical tabs from screenshot
    Column(
      modifier = Modifier
        .width(160.dp)
        .fillMaxHeight()
        .clip(RoundedCornerShape(12.dp))
        .background(Color(0x2E000000))
        .border(0.5.dp, Color(0x33FFFFFF), RoundedCornerShape(12.dp))
        .padding(10.dp),
      verticalArrangement = Arrangement.spacedBy(8.dp)
    ) {
      SideTabButton(
        title = "Histórico",
        isSelected = selectedTab == "HISTORICO",
        onClick = { selectedTab = "HISTORICO" }
      )
      SideTabButton(
        title = "Meus Favoritos",
        isSelected = selectedTab == "FAVORITOS",
        onClick = { selectedTab = "FAVORITOS" }
      )
      SideTabButton(
        title = "Minha Lista",
        isSelected = selectedTab == "LISTA",
        onClick = { selectedTab = "LISTA" }
      )
    }

    Spacer(modifier = Modifier.width(14.dp))

    // Right content area
    Column(modifier = Modifier.weight(1f).fillMaxHeight()) {
      // Top row with title, actions, and total count
      Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically
      ) {
        Column {
          Text(
            text = if (selectedTab == "HISTORICO") "Histórico de Reprodução" else "Favoritos",
            color = Color.White,
            fontWeight = FontWeight.Bold,
            fontSize = 16.sp
          )
          Text(
            text = "Vinculado ao servidor: $serverName",
            color = AccentGold,
            fontSize = 11.sp
          )
        }

        Row(verticalAlignment = Alignment.CenterVertically) {
          IconButton(onClick = onClearHistory) {
            Icon(Icons.Default.Delete, contentDescription = "Limpar", tint = Color.White.copy(0.7f))
          }
          Spacer(modifier = Modifier.width(6.dp))
          Text(
            text = "Total ${if (selectedTab == "HISTORICO") historyItems.size else favoriteItems.size}",
            color = Color.White.copy(0.7f),
            fontSize = 12.sp,
            fontWeight = FontWeight.SemiBold
          )
        }
      }

      Spacer(modifier = Modifier.height(10.dp))

      if (selectedTab == "HISTORICO") {
        if (historyItems.isEmpty()) {
          Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
            Text("Nenhum item assistido recentemente neste servidor.", color = Color.White.copy(0.6f))
          }
        } else {
          LazyVerticalGrid(
            columns = GridCells.Adaptive(minSize = 130.dp),
            horizontalArrangement = Arrangement.spacedBy(10.dp),
            verticalArrangement = Arrangement.spacedBy(10.dp),
            modifier = Modifier.fillMaxSize()
          ) {
            items(historyItems, key = { it.id }) { h ->
              ContentPosterCard(
                title = h.title,
                posterUrl = h.posterUrl,
                badgeText = h.episodeInfo ?: "Assistiu",
                onClick = { onItemClick(h.streamId, h.title) },
                modifier = Modifier.height(195.dp)
              )
            }
          }
        }
      } else {
        if (favoriteItems.isEmpty()) {
          Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
            Text("Nenhum favorito salvo neste servidor.", color = Color.White.copy(0.6f))
          }
        } else {
          LazyVerticalGrid(
            columns = GridCells.Adaptive(minSize = 130.dp),
            horizontalArrangement = Arrangement.spacedBy(10.dp),
            verticalArrangement = Arrangement.spacedBy(10.dp),
            modifier = Modifier.fillMaxSize()
          ) {
            items(favoriteItems, key = { it.id }) { fav ->
              ContentPosterCard(
                title = fav.title,
                posterUrl = fav.posterUrl,
                rating = fav.rating,
                badgeText = fav.type,
                onClick = { onItemClick(fav.streamId, fav.title) },
                modifier = Modifier.height(195.dp)
              )
            }
          }
        }
      }
    }
  }
}

@Composable
private fun SideTabButton(
  title: String,
  isSelected: Boolean,
  onClick: () -> Unit
) {
  Box(
    modifier = Modifier
      .fillMaxWidth()
      .height(44.dp)
      .clip(RoundedCornerShape(8.dp))
      .background(if (isSelected) RubyPrimary else Color.Transparent)
      .border(
        width = if (isSelected) 1.dp else 0.dp,
        color = if (isSelected) AccentGold else Color.Transparent,
        shape = RoundedCornerShape(8.dp)
      )
      .clickable { onClick() }
      .padding(horizontal = 12.dp),
    contentAlignment = Alignment.CenterStart
  ) {
    Text(
      text = title,
      color = Color.White,
      fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Medium,
      fontSize = 13.sp
    )
  }
}
