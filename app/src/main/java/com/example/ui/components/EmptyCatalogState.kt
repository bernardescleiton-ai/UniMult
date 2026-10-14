package com.example.ui.components

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Dns
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.ui.theme.AccentGold
import com.example.ui.theme.RubyPrimary

@Composable
fun EmptyCatalogState(
  title: String = "Nenhum Conteúdo Carregado",
  message: String = "Cadastre e ative seu servidor Xtream Codes para puxar as capas, canais, filmes e séries em tempo real.",
  onNavigateToServers: (() -> Unit)? = null,
  modifier: Modifier = Modifier
) {
  Box(
    modifier = modifier
      .fillMaxWidth()
      .padding(vertical = 32.dp, horizontal = 16.dp)
      .clip(RoundedCornerShape(16.dp))
      .background(Color(0x1FFFFFFF))
      .border(1.dp, Color(0x33FFFFFF), RoundedCornerShape(16.dp))
      .padding(24.dp),
    contentAlignment = Alignment.Center
  ) {
    Column(horizontalAlignment = Alignment.CenterHorizontally) {
      Icon(
        imageVector = Icons.Default.Dns,
        contentDescription = null,
        tint = AccentGold,
        modifier = Modifier.size(54.dp)
      )
      Spacer(modifier = Modifier.height(12.dp))
      Text(
        text = title,
        color = Color.White,
        fontWeight = FontWeight.Bold,
        fontSize = 18.sp,
        textAlign = TextAlign.Center
      )
      Spacer(modifier = Modifier.height(8.dp))
      Text(
        text = message,
        color = Color.White.copy(alpha = 0.75f),
        fontSize = 13.sp,
        textAlign = TextAlign.Center,
        modifier = Modifier.padding(horizontal = 16.dp)
      )

      if (onNavigateToServers != null) {
        Spacer(modifier = Modifier.height(16.dp))
        Button(
          onClick = onNavigateToServers,
          colors = ButtonDefaults.buttonColors(containerColor = RubyPrimary),
          modifier = Modifier
            .fillMaxWidth(0.9f)
            .height(48.dp)
            .testTag("empty_state_go_to_servers")
        ) {
          Icon(Icons.Default.Add, contentDescription = null, tint = Color.White)
          Spacer(modifier = Modifier.width(8.dp))
          Text("Preencher Dados do Servidor (URL, Usuário e Senha)", color = Color.White, fontWeight = FontWeight.Bold, fontSize = 13.sp)
        }
      }
    }
  }
}
