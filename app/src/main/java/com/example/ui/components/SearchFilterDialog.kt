package com.example.ui.components

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.PlayArrow
import androidx.compose.material.icons.filled.Search
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.OutlinedTextFieldDefaults
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
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
import com.example.domain.model.LiveChannel
import com.example.domain.model.VodItem
import com.example.ui.theme.AccentGold
import com.example.ui.theme.DarkSurface
import com.example.ui.theme.RubyPrimary

@Composable
fun SearchModalDialog(
  allMovies: List<VodItem>,
  allChannels: List<LiveChannel>,
  onSelectMovie: (VodItem) -> Unit,
  onSelectChannel: (LiveChannel) -> Unit,
  onDismiss: () -> Unit
) {
  var searchQuery by remember { mutableStateOf("") }

  val filteredMovies = remember(searchQuery, allMovies) {
    if (searchQuery.isBlank()) emptyList()
    else allMovies.filter { it.name.contains(searchQuery, ignoreCase = true) }
  }

  val filteredChannels = remember(searchQuery, allChannels) {
    if (searchQuery.isBlank()) emptyList()
    else allChannels.filter { it.name.contains(searchQuery, ignoreCase = true) }
  }

  AlertDialog(
    onDismissRequest = onDismiss,
    containerColor = DarkSurface,
    title = {
      Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically
      ) {
        Text("Pesquisar Conteúdo", color = Color.White, fontWeight = FontWeight.Bold, fontSize = 18.sp)
        IconButton(onClick = onDismiss) {
          Icon(Icons.Default.Close, contentDescription = "Fechar", tint = Color.White.copy(0.7f))
        }
      }
    },
    text = {
      Column(modifier = Modifier.fillMaxWidth()) {
        OutlinedTextField(
          value = searchQuery,
          onValueChange = { searchQuery = it },
          placeholder = { Text("Digite o nome do filme, canal ou série...") },
          leadingIcon = { Icon(Icons.Default.Search, contentDescription = null, tint = AccentGold) },
          colors = OutlinedTextFieldDefaults.colors(
            focusedTextColor = Color.White,
            unfocusedTextColor = Color.White,
            focusedBorderColor = AccentGold,
            unfocusedBorderColor = Color.White.copy(0.3f),
            cursorColor = AccentGold
          ),
          singleLine = true,
          modifier = Modifier
            .fillMaxWidth()
            .testTag("search_input_field")
        )

        Spacer(modifier = Modifier.height(12.dp))

        if (searchQuery.isNotBlank() && filteredMovies.isEmpty() && filteredChannels.isEmpty()) {
          Box(
            modifier = Modifier
              .fillMaxWidth()
              .height(100.dp),
            contentAlignment = Alignment.Center
          ) {
            Text("Nenhum resultado encontrado.", color = Color.White.copy(0.6f))
          }
        }

        LazyColumn(
          modifier = Modifier
            .fillMaxWidth()
            .height(280.dp),
          verticalArrangement = Arrangement.spacedBy(8.dp)
        ) {
          if (filteredChannels.isNotEmpty()) {
            item {
              Text("Canais ao Vivo (${filteredChannels.size})", color = AccentGold, fontWeight = FontWeight.Bold, fontSize = 12.sp)
            }
            items(filteredChannels) { ch ->
              SearchResultRow(title = ch.name, subtitle = "Canal Ao Vivo", onClick = { onSelectChannel(ch) })
            }
          }

          if (filteredMovies.isNotEmpty()) {
            item {
              Spacer(modifier = Modifier.height(6.dp))
              Text("Filmes & Séries (${filteredMovies.size})", color = AccentGold, fontWeight = FontWeight.Bold, fontSize = 12.sp)
            }
            items(filteredMovies) { m ->
              SearchResultRow(title = m.name, subtitle = "Ano: ${m.year ?: "N/D"}", onClick = { onSelectMovie(m) })
            }
          }
        }
      }
    },
    confirmButton = {
      TextButton(onClick = onDismiss) {
        Text("Fechar", color = Color.White.copy(0.8f))
      }
    }
  )
}

@Composable
private fun SearchResultRow(
  title: String,
  subtitle: String,
  onClick: () -> Unit
) {
  Row(
    modifier = Modifier
      .fillMaxWidth()
      .clip(RoundedCornerShape(8.dp))
      .background(Color(0x22FFFFFF))
      .clickable { onClick() }
      .padding(10.dp),
    verticalAlignment = Alignment.CenterVertically,
    horizontalArrangement = Arrangement.SpaceBetween
  ) {
    Column(modifier = Modifier.weight(1f)) {
      Text(title, color = Color.White, fontWeight = FontWeight.SemiBold, fontSize = 13.sp)
      Text(subtitle, color = Color.White.copy(0.6f), fontSize = 11.sp)
    }
    Icon(Icons.Default.PlayArrow, contentDescription = null, tint = RubyPrimary, modifier = Modifier.size(20.dp))
  }
}
