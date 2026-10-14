package com.example.ui.components

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.ui.theme.AccentGold

enum class AppNavSection(val label: String) {
  SERVIDORES("⚙️ SERVIDOR"),
  DESTAQUES("DESTAQUES"),
  TV("TV"),
  FILMES("FILMES"),
  SERIES("SÉRIES"),
  JOGOS("JOGOS"),
  KIDS("KIDS"),
  ANIME("ANIME"),
  EXPLORAR("EXPLORAR")
}

@Composable
fun CategoryTabsRow(
  selectedSection: AppNavSection,
  onSectionSelected: (AppNavSection) -> Unit,
  modifier: Modifier = Modifier
) {
  val scrollState = rememberScrollState()

  Row(
    modifier = modifier
      .fillMaxWidth()
      .horizontalScroll(scrollState)
      .padding(horizontal = 16.dp, vertical = 6.dp),
    horizontalArrangement = Arrangement.spacedBy(12.dp),
    verticalAlignment = Alignment.CenterVertically
  ) {
    AppNavSection.values().forEach { section ->
      val isSelected = section == selectedSection

      val isServerTab = section == AppNavSection.SERVIDORES
      val tabBg = when {
        isSelected -> Color(0x33FFD54F)
        isServerTab -> Color(0x33E50914)
        else -> Color.Transparent
      }
      val tabBorderColor = when {
        isSelected -> AccentGold
        isServerTab -> Color(0x66FFD54F)
        else -> Color.Transparent
      }

      Box(
        modifier = Modifier
          .clip(RoundedCornerShape(20.dp))
          .background(tabBg)
          .border(
            width = if (isSelected || isServerTab) 1.5.dp else 0.dp,
            color = tabBorderColor,
            shape = RoundedCornerShape(20.dp)
          )
          .clickable { onSectionSelected(section) }
          .padding(horizontal = 16.dp, vertical = 6.dp)
          .testTag("tab_${section.name.lowercase()}"),
        contentAlignment = Alignment.Center
      ) {
        if (section == AppNavSection.KIDS) {
          // Playful colorful text as shown in screenshot "KIDS"
          Row {
            Text("K", color = Color(0xFFFF5252), fontWeight = FontWeight.ExtraBold, fontSize = 15.sp)
            Text("I", color = Color(0xFFFFEB3B), fontWeight = FontWeight.ExtraBold, fontSize = 15.sp)
            Text("D", color = Color(0xFF69F0AE), fontWeight = FontWeight.ExtraBold, fontSize = 15.sp)
            Text("S", color = Color(0xFF40C4FF), fontWeight = FontWeight.ExtraBold, fontSize = 15.sp)
          }
        } else {
          Text(
            text = section.label,
            color = if (isSelected) Color.White else Color.White.copy(alpha = 0.75f),
            fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Medium,
            fontSize = 15.sp,
            letterSpacing = 0.5.sp
          )
        }
      }
    }
  }
}
