package com.example.ui.components

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
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
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Movie
import androidx.compose.material.icons.filled.PlayArrow
import androidx.compose.material.icons.filled.Star
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.focus.onFocusChanged
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
import com.example.ui.theme.AccentGold
import com.example.ui.theme.RubyPrimary

@Composable
fun ContentPosterCard(
  title: String,
  posterUrl: String?,
  rating: String? = null,
  year: String? = null,
  badgeText: String? = null,
  onClick: () -> Unit,
  modifier: Modifier = Modifier
) {
  val context = LocalContext.current
  var isFocused by remember { mutableStateOf(false) }

  Card(
    shape = RoundedCornerShape(10.dp),
    colors = CardDefaults.cardColors(
      containerColor = if (isFocused) Color(0x4DFF334B) else Color(0x22FFFFFF)
    ),
    elevation = CardDefaults.cardElevation(defaultElevation = if (isFocused) 8.dp else 4.dp),
    modifier = modifier
      .onFocusChanged { isFocused = it.isFocused }
      .clip(RoundedCornerShape(10.dp))
      .border(
        width = if (isFocused) 2.dp else 0.5.dp,
        color = if (isFocused) AccentGold else Color(0x33FFFFFF),
        shape = RoundedCornerShape(10.dp)
      )
      .clickable { onClick() }
      .testTag("content_card_${title.take(15)}")
  ) {
    Box(modifier = Modifier.fillMaxSize()) {
      if (!posterUrl.isNullOrBlank()) {
        AsyncImage(
          model = ImageRequest.Builder(context)
            .data(posterUrl)
            .crossfade(true)
            .build(),
          contentDescription = title,
          contentScale = ContentScale.Crop,
          modifier = Modifier.fillMaxSize()
        )
      } else {
        Box(
          modifier = Modifier.fillMaxSize(),
          contentAlignment = Alignment.Center
        ) {
          Icon(
            imageVector = Icons.Default.Movie,
            contentDescription = null,
            tint = Color.White.copy(alpha = 0.35f),
            modifier = Modifier.size(38.dp)
          )
        }
      }

      // Bottom gradient overlay for title legibility
      Box(
        modifier = Modifier
          .fillMaxWidth()
          .height(70.dp)
          .align(Alignment.BottomCenter)
          .background(
            Brush.verticalGradient(
              colors = listOf(Color.Transparent, Color(0xCC000000), Color(0xF0000000))
            )
          )
      )

      // Rating badge top-start if present
      if (!rating.isNullOrBlank()) {
        Box(
          modifier = Modifier
            .padding(6.dp)
            .align(Alignment.TopStart)
            .clip(RoundedCornerShape(4.dp))
            .background(Color(0xD9000000))
            .padding(horizontal = 6.dp, vertical = 2.dp)
        ) {
          Row(verticalAlignment = Alignment.CenterVertically) {
            Icon(
              imageVector = Icons.Default.Star,
              contentDescription = null,
              tint = AccentGold,
              modifier = Modifier.size(12.dp)
            )
            Spacer(modifier = Modifier.width(3.dp))
            Text(
              text = rating,
              color = Color.White,
              fontSize = 11.sp,
              fontWeight = FontWeight.Bold
            )
          }
        }
      }

      // Special badge top-end (e.g. 2026, Dublado, CC)
      if (!badgeText.isNullOrBlank()) {
        Box(
          modifier = Modifier
            .padding(6.dp)
            .align(Alignment.TopEnd)
            .clip(RoundedCornerShape(4.dp))
            .background(RubyPrimary)
            .padding(horizontal = 6.dp, vertical = 2.dp)
        ) {
          Text(
            text = badgeText,
            color = Color.White,
            fontSize = 10.sp,
            fontWeight = FontWeight.ExtraBold
          )
        }
      }

      // Title & metadata at bottom
      Column(
        modifier = Modifier
          .align(Alignment.BottomStart)
          .padding(8.dp)
      ) {
        Text(
          text = title,
          color = Color.White,
          fontSize = 12.sp,
          fontWeight = FontWeight.Bold,
          maxLines = 1,
          overflow = TextOverflow.Ellipsis
        )
        if (!year.isNullOrBlank()) {
          Text(
            text = year,
            color = Color.White.copy(alpha = 0.7f),
            fontSize = 10.sp
          )
        }
      }
    }
  }
}
