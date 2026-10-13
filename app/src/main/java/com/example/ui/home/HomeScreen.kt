package com.example.ui.home

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
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
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import coil.compose.AsyncImage
import coil.request.ImageRequest
import com.example.domain.model.VodItem
import com.example.ui.components.ContentPosterCard
import com.example.ui.components.EmptyCatalogState
import com.example.ui.theme.AccentGold
import com.example.ui.theme.RubyPrimary

@Composable
fun HomeScreen(
  highlights: List<VodItem>,
  onItemClick: (VodItem) -> Unit,
  onNavigateToServers: () -> Unit,
  modifier: Modifier = Modifier
) {
  if (highlights.isEmpty()) {
    EmptyCatalogState(
      title = "Nenhum Conteúdo Carregado",
      message = "Cadastre seu servidor Xtream Codes na aba SERVIDORES para carregar as capas oficiais, filmes e séries em tempo real.",
      onNavigateToServers = onNavigateToServers,
      modifier = modifier
    )
    return
  }

  val featuredItem = highlights.firstOrNull()
  val gridItems = if (highlights.size > 1) highlights.drop(1) else highlights
  val context = LocalContext.current

  Column(
    modifier = modifier
      .fillMaxSize()
      .padding(horizontal = 16.dp, vertical = 6.dp)
  ) {
    // Featured Hero Banner if present
    featuredItem?.let { hero ->
      Box(
        modifier = Modifier
          .fillMaxWidth()
          .height(170.dp)
          .clip(RoundedCornerShape(12.dp))
          .border(0.5.dp, Color(0x33FFFFFF), RoundedCornerShape(12.dp))
          .clickable { onItemClick(hero) }
          .testTag("featured_hero_banner")
      ) {
        AsyncImage(
          model = ImageRequest.Builder(context)
            .data(hero.streamIcon)
            .crossfade(true)
            .build(),
          contentDescription = hero.name,
          contentScale = ContentScale.Crop,
          modifier = Modifier.fillMaxSize()
        )

        // Gradient overlay
        Box(
          modifier = Modifier
            .fillMaxSize()
            .background(
              Brush.horizontalGradient(
                listOf(Color(0xE6000000), Color(0x66000000), Color.Transparent)
              )
            )
        )

        Column(
          modifier = Modifier
            .align(Alignment.CenterStart)
            .padding(16.dp)
            .fillMaxWidth(0.65f)
        ) {
          Box(
            modifier = Modifier
              .clip(RoundedCornerShape(4.dp))
              .background(RubyPrimary)
              .padding(horizontal = 8.dp, vertical = 2.dp)
          ) {
            Text("SUPER DESTAQUE", color = Color.White, fontSize = 10.sp, fontWeight = FontWeight.Bold)
          }

          Spacer(modifier = Modifier.height(6.dp))

          Text(
            text = hero.name,
            color = Color.White,
            fontSize = 18.sp,
            fontWeight = FontWeight.ExtraBold,
            maxLines = 1,
            overflow = TextOverflow.Ellipsis
          )

          if (!hero.plot.isNullOrBlank()) {
            Spacer(modifier = Modifier.height(4.dp))
            Text(
              text = hero.plot,
              color = Color.White.copy(0.8f),
              fontSize = 11.sp,
              maxLines = 2,
              overflow = TextOverflow.Ellipsis
            )
          }
        }
      }

      Spacer(modifier = Modifier.height(12.dp))
    }

    Text(
      text = "Catálogo do Servidor",
      color = Color.White,
      fontWeight = FontWeight.Bold,
      fontSize = 15.sp,
      modifier = Modifier.padding(bottom = 8.dp)
    )

    LazyVerticalGrid(
      columns = GridCells.Adaptive(minSize = 135.dp),
      horizontalArrangement = Arrangement.spacedBy(10.dp),
      verticalArrangement = Arrangement.spacedBy(10.dp),
      modifier = Modifier.fillMaxSize()
    ) {
      items(gridItems, key = { it.streamId }) { item ->
        ContentPosterCard(
          title = item.name,
          posterUrl = item.streamIcon,
          rating = item.rating,
          year = item.year,
          onClick = { onItemClick(item) },
          modifier = Modifier.height(205.dp)
        )
      }
    }
  }
}
