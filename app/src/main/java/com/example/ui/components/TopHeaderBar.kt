package com.example.ui.components

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.AccountCircle
import androidx.compose.material.icons.filled.Dns
import androidx.compose.material.icons.filled.FilterList
import androidx.compose.material.icons.filled.History
import androidx.compose.material.icons.filled.Notifications
import androidx.compose.material.icons.filled.Search
import androidx.compose.material.icons.filled.Tv
import androidx.compose.material3.Badge
import androidx.compose.material3.BadgedBox
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.domain.model.ServerProfile
import com.example.ui.theme.AccentGold
import com.example.ui.theme.RubyPrimary
import kotlinx.coroutines.delay
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

@Composable
fun TopHeaderBar(
  activeServer: ServerProfile?,
  onSearchClick: () -> Unit,
  onFilterClick: () -> Unit,
  onHistoryClick: () -> Unit,
  onProfileClick: () -> Unit,
  onServersClick: () -> Unit,
  modifier: Modifier = Modifier
) {
  var currentTime by remember { mutableStateOf(getFormattedTime()) }

  LaunchedEffect(Unit) {
    while (true) {
      delay(30000)
      currentTime = getFormattedTime()
    }
  }

  Row(
    modifier = modifier
      .fillMaxWidth()
      .padding(horizontal = 16.dp, vertical = 8.dp),
    verticalAlignment = Alignment.CenterVertically,
    horizontalArrangement = Arrangement.SpaceBetween
  ) {
    // Brand pill logo (UniMult) styled like the reference screenshots
    Box(
      modifier = Modifier
        .clip(RoundedCornerShape(20.dp))
        .background(
          Brush.horizontalGradient(
            listOf(
              Color(0x80FFFFFF),
              Color(0x30FFFFFF)
            )
          )
        )
        .border(1.dp, Color(0x40FFFFFF), RoundedCornerShape(20.dp))
        .clickable { onServersClick() }
        .padding(horizontal = 12.dp, vertical = 6.dp)
        .testTag("brand_logo_pill")
    ) {
      Row(verticalAlignment = Alignment.CenterVertically) {
        Box(
          modifier = Modifier
            .size(24.dp)
            .clip(CircleShape)
            .background(RubyPrimary),
          contentAlignment = Alignment.Center
        ) {
          Icon(
            imageVector = Icons.Default.Tv,
            contentDescription = "UniMult Logo",
            tint = Color.White,
            modifier = Modifier.size(16.dp)
          )
        }
        Spacer(modifier = Modifier.width(8.dp))
        Text(
          text = "UniMult",
          color = Color.White,
          fontWeight = FontWeight.Bold,
          fontSize = 15.sp
        )
        if (activeServer != null) {
          Spacer(modifier = Modifier.width(6.dp))
          Text(
            text = "• ${activeServer.name}",
            color = AccentGold,
            fontSize = 12.sp,
            fontWeight = FontWeight.Medium
          )
        }
      }
    }

    // Right Action Icons
    Row(
      verticalAlignment = Alignment.CenterVertically,
      horizontalArrangement = Arrangement.spacedBy(4.dp)
    ) {
      IconButton(
        onClick = onSearchClick,
        modifier = Modifier.testTag("search_button")
      ) {
        Icon(
          imageVector = Icons.Default.Search,
          contentDescription = "Pesquisar",
          tint = Color.White.copy(alpha = 0.9f)
        )
      }

      IconButton(
        onClick = onFilterClick,
        modifier = Modifier.testTag("filter_button")
      ) {
        Icon(
          imageVector = Icons.Default.FilterList,
          contentDescription = "Filtro",
          tint = Color.White.copy(alpha = 0.9f)
        )
      }

      IconButton(
        onClick = onHistoryClick,
        modifier = Modifier.testTag("history_button")
      ) {
        Icon(
          imageVector = Icons.Default.History,
          contentDescription = "Histórico e Favoritos",
          tint = Color.White.copy(alpha = 0.9f)
        )
      }

      // Multi-Server icon button
      IconButton(
        onClick = onServersClick,
        modifier = Modifier.testTag("servers_management_button")
      ) {
        BadgedBox(
          badge = {
            if (activeServer != null) {
              Box(
                modifier = Modifier
                  .size(8.dp)
                  .clip(CircleShape)
                  .background(Color(0xFF4CAF50))
              )
            }
          }
        ) {
          Icon(
            imageVector = Icons.Default.Dns,
            contentDescription = "Gerenciar Servidores",
            tint = if (activeServer != null) AccentGold else Color.White
          )
        }
      }

      // User Center Profile Icon with VIP badge
      Box(
        modifier = Modifier
          .clickable { onProfileClick() }
          .padding(4.dp)
          .testTag("user_profile_button"),
        contentAlignment = Alignment.Center
      ) {
        Icon(
          imageVector = Icons.Default.AccountCircle,
          contentDescription = "Perfil da Conta",
          tint = Color.White,
          modifier = Modifier.size(28.dp)
        )
        Box(
          modifier = Modifier
            .align(Alignment.BottomEnd)
            .size(10.dp)
            .clip(CircleShape)
            .background(AccentGold)
        )
      }

      Spacer(modifier = Modifier.width(6.dp))

      // Digital Clock from screenshots
      Text(
        text = currentTime,
        color = Color.White.copy(alpha = 0.95f),
        fontSize = 14.sp,
        fontWeight = FontWeight.SemiBold,
        modifier = Modifier.padding(start = 4.dp)
      )
    }
  }
}

private fun getFormattedTime(): String {
  val sdf = SimpleDateFormat("HH:mm", Locale.getDefault())
  return sdf.format(Date())
}
