package com.example.ui.vod

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.lazy.grid.items
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
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
import com.example.domain.model.Category
import com.example.domain.model.SeriesItem
import com.example.domain.model.VodItem
import com.example.ui.components.ContentPosterCard
import com.example.ui.components.EmptyCatalogState
import com.example.ui.theme.AccentGold
import com.example.ui.theme.RubyPrimary

@Composable
fun VodScreen(
  sectionTitle: String,
  items: List<VodItem>,
  seriesItems: List<SeriesItem> = emptyList(),
  categories: List<Category> = emptyList(),
  onVodClick: (VodItem) -> Unit,
  onSeriesClick: (SeriesItem) -> Unit = {},
  onNavigateToServers: () -> Unit,
  modifier: Modifier = Modifier
) {
  if (items.isEmpty() && seriesItems.isEmpty()) {
    EmptyCatalogState(
      title = "Nenhum Conteúdo em $sectionTitle",
      message = "Conecte seu servidor Xtream Codes para carregar automaticamente as capas e títulos de $sectionTitle.",
      onNavigateToServers = onNavigateToServers,
      modifier = modifier
    )
    return
  }

  var selectedCategoryId by remember { mutableStateOf("ALL") }

  val filteredVod = remember(selectedCategoryId, items) {
    if (selectedCategoryId == "ALL") items
    else items.filter { it.categoryId == selectedCategoryId }
  }

  val filteredSeries = remember(selectedCategoryId, seriesItems) {
    if (selectedCategoryId == "ALL") seriesItems
    else seriesItems.filter { it.categoryId == selectedCategoryId }
  }

  Column(
    modifier = modifier
      .fillMaxSize()
      .padding(horizontal = 16.dp, vertical = 6.dp)
  ) {
    // Dynamic categories from server
    if (categories.isNotEmpty()) {
      val scrollState = rememberScrollState()
      Row(
        modifier = Modifier
          .fillMaxWidth()
          .horizontalScroll(scrollState)
          .padding(bottom = 10.dp),
        horizontalArrangement = Arrangement.spacedBy(8.dp)
      ) {
        // "Todos" pill
        val isAllSelected = selectedCategoryId == "ALL"
        Box(
          modifier = Modifier
            .clip(RoundedCornerShape(16.dp))
            .background(if (isAllSelected) RubyPrimary else Color(0x2BFFFFFF))
            .border(0.5.dp, if (isAllSelected) AccentGold else Color(0x33FFFFFF), RoundedCornerShape(16.dp))
            .clickable { selectedCategoryId = "ALL" }
            .padding(horizontal = 14.dp, vertical = 6.dp)
            .testTag("cat_all"),
          contentAlignment = Alignment.Center
        ) {
          Text(
            text = "Todos",
            color = Color.White,
            fontWeight = if (isAllSelected) FontWeight.Bold else FontWeight.Normal,
            fontSize = 12.sp
          )
        }

        categories.forEach { cat ->
          val isSelected = cat.id == selectedCategoryId
          Box(
            modifier = Modifier
              .clip(RoundedCornerShape(16.dp))
              .background(if (isSelected) RubyPrimary else Color(0x2BFFFFFF))
            .border(0.5.dp, if (isSelected) AccentGold else Color(0x33FFFFFF), RoundedCornerShape(16.dp))
            .clickable { selectedCategoryId = cat.id }
            .padding(horizontal = 14.dp, vertical = 6.dp)
            .testTag("cat_${cat.id}"),
            contentAlignment = Alignment.Center
          ) {
            Text(
              text = cat.name,
              color = Color.White,
              fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Normal,
              fontSize = 12.sp
            )
          }
        }
      }
    }

    if (filteredVod.isNotEmpty()) {
      LazyVerticalGrid(
        columns = GridCells.Adaptive(minSize = 135.dp),
        horizontalArrangement = Arrangement.spacedBy(10.dp),
        verticalArrangement = Arrangement.spacedBy(10.dp),
        modifier = Modifier.fillMaxSize()
      ) {
        items(filteredVod, key = { it.streamId }) { vod ->
          ContentPosterCard(
            title = vod.name,
            posterUrl = vod.streamIcon,
            rating = vod.rating,
            year = vod.year,
            badgeText = if (vod.year == "2026") "NOVO" else null,
            onClick = { onVodClick(vod) },
            modifier = Modifier.height(205.dp)
          )
        }
      }
    } else if (filteredSeries.isNotEmpty()) {
      LazyVerticalGrid(
        columns = GridCells.Adaptive(minSize = 135.dp),
        horizontalArrangement = Arrangement.spacedBy(10.dp),
        verticalArrangement = Arrangement.spacedBy(10.dp),
        modifier = Modifier.fillMaxSize()
      ) {
        items(filteredSeries, key = { it.seriesId }) { series ->
          ContentPosterCard(
            title = series.name,
            posterUrl = series.cover,
            rating = series.rating,
            year = series.releaseDate,
            badgeText = series.genre?.take(10),
            onClick = { onSeriesClick(series) },
            modifier = Modifier.height(205.dp)
          )
        }
      }
    }
  }
}
