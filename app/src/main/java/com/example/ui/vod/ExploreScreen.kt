package com.example.ui.vod

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
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
import com.example.domain.model.VodItem
import com.example.ui.components.ContentPosterCard
import com.example.ui.theme.AccentGold
import com.example.ui.theme.RubyPrimary

import com.example.ui.components.EmptyCatalogState

@Composable
fun ExploreScreen(
  allItems: List<VodItem>,
  onVodClick: (VodItem) -> Unit,
  onNavigateToServers: () -> Unit,
  modifier: Modifier = Modifier
) {
  if (allItems.isEmpty()) {
    EmptyCatalogState(
      title = "Nenhum Conteúdo para Explorar",
      message = "Cadastre e ative seu servidor Xtream Codes para buscar e filtrar filmes por gênero, ano e país em tempo real.",
      onNavigateToServers = onNavigateToServers,
      modifier = modifier
    )
    return
  }

  var selectedType by remember { mutableStateOf("Filmes") }
  var selectedGenre by remember { mutableStateOf("Todos") }
  var selectedYear by remember { mutableStateOf("Todos") }
  var selectedCountry by remember { mutableStateOf("Todos") }
  var selectedAudio by remember { mutableStateOf("Todos") }
  var selectedRating by remember { mutableStateOf("Todos") }

  val typeOptions = listOf("Filmes", "Séries", "Kids", "Anime")
  val genreOptions = listOf("Todos", "Ação", "Drama", "Aventura", "Crime", "Ficção científica", "Comédia", "Romance", "Animação")
  val yearOptions = listOf("Todos", "2026", "2025", "2024", "2023", "2022", "2021", "2020")
  val countryOptions = listOf("Todos", "Brasil", "Portugal", "Estados Unidos", "Reino Unido", "Japão")
  val audioOptions = listOf("Todos", "Português", "Espanhol", "Inglês")
  val ratingOptions = listOf("Todos", ">=9.0", ">=8.0", ">=7.0", ">=6.0")

  // Filter items based on selections
  val filteredItems = remember(selectedGenre, selectedYear, allItems) {
    allItems.filter { item ->
      val genreMatch = selectedGenre == "Todos" || (item.genre?.contains(selectedGenre, true) == true) || (item.categoryId.contains(selectedGenre, true))
      val yearMatch = selectedYear == "Todos" || (item.year == selectedYear)
      genreMatch && yearMatch
    }.ifEmpty { allItems }
  }

  Column(
    modifier = modifier
      .fillMaxSize()
      .padding(horizontal = 16.dp, vertical = 6.dp)
  ) {
    // Filter rows container
    Column(
      modifier = Modifier
        .fillMaxWidth()
        .clip(RoundedCornerShape(12.dp))
        .background(Color(0x24000000))
        .border(0.5.dp, Color(0x22FFFFFF), RoundedCornerShape(12.dp))
        .padding(10.dp),
      verticalArrangement = Arrangement.spacedBy(6.dp)
    ) {
      FilterRow(label = "Tipo:", options = typeOptions, selected = selectedType, onSelect = { selectedType = it })
      FilterRow(label = "Gênero:", options = genreOptions, selected = selectedGenre, onSelect = { selectedGenre = it })
      FilterRow(label = "Ano:", options = yearOptions, selected = selectedYear, onSelect = { selectedYear = it })
      FilterRow(label = "País:", options = countryOptions, selected = selectedCountry, onSelect = { selectedCountry = it })
      FilterRow(label = "Áudio:", options = audioOptions, selected = selectedAudio, onSelect = { selectedAudio = it })
      FilterRow(label = "Classificação:", options = ratingOptions, selected = selectedRating, onSelect = { selectedRating = it })
    }

    Spacer(modifier = Modifier.height(12.dp))

    // Results grid
    LazyVerticalGrid(
      columns = GridCells.Adaptive(minSize = 130.dp),
      horizontalArrangement = Arrangement.spacedBy(10.dp),
      verticalArrangement = Arrangement.spacedBy(10.dp),
      modifier = Modifier.fillMaxSize()
    ) {
      items(filteredItems, key = { it.streamId }) { vod ->
        ContentPosterCard(
          title = vod.name,
          posterUrl = vod.streamIcon,
          rating = vod.rating,
          year = vod.year,
          onClick = { onVodClick(vod) },
          modifier = Modifier.height(195.dp)
        )
      }
    }
  }
}

@Composable
private fun FilterRow(
  label: String,
  options: List<String>,
  selected: String,
  onSelect: (String) -> Unit
) {
  val scrollState = rememberScrollState()

  Row(
    modifier = Modifier.fillMaxWidth(),
    verticalAlignment = Alignment.CenterVertically
  ) {
    Text(
      text = label,
      color = Color.White.copy(0.7f),
      fontWeight = FontWeight.SemiBold,
      fontSize = 12.sp,
      modifier = Modifier.width(90.dp)
    )

    Row(
      modifier = Modifier
        .horizontalScroll(scrollState)
        .weight(1f),
      horizontalArrangement = Arrangement.spacedBy(6.dp)
    ) {
      options.forEach { opt ->
        val isSel = opt == selected
        Box(
          modifier = Modifier
            .clip(RoundedCornerShape(12.dp))
            .background(if (isSel) RubyPrimary else Color.Transparent)
            .border(
              width = if (isSel) 1.dp else 0.dp,
              color = if (isSel) AccentGold else Color.Transparent,
              shape = RoundedCornerShape(12.dp)
            )
            .clickable { onSelect(opt) }
            .padding(horizontal = 10.dp, vertical = 3.dp),
          contentAlignment = Alignment.Center
        ) {
          Text(
            text = opt,
            color = if (isSel) Color.White else Color.White.copy(0.7f),
            fontWeight = if (isSel) FontWeight.Bold else FontWeight.Normal,
            fontSize = 11.sp
          )
        }
      }
    }
  }
}
