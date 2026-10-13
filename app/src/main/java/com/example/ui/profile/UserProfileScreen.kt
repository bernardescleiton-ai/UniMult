package com.example.ui.profile

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.horizontalScroll
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
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.CardGiftcard
import androidx.compose.material.icons.filled.Dns
import androidx.compose.material.icons.filled.ExitToApp
import androidx.compose.material.icons.filled.Favorite
import androidx.compose.material.icons.filled.History
import androidx.compose.material.icons.filled.Person
import androidx.compose.material.icons.filled.PhoneAndroid
import androidx.compose.material.icons.filled.Security
import androidx.compose.material.icons.filled.Star
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.domain.model.AccountInfo
import com.example.domain.model.ServerProfile
import com.example.ui.theme.AccentGold
import com.example.ui.theme.DarkSurface
import com.example.ui.theme.RubyPrimary

@Composable
fun UserProfileScreen(
  activeServer: ServerProfile?,
  accountInfo: AccountInfo?,
  onNavigateToServers: () -> Unit,
  onNavigateToHistory: () -> Unit,
  onNavigateToFavorites: () -> Unit,
  modifier: Modifier = Modifier
) {
  val scrollState = rememberScrollState()

  Row(
    modifier = modifier
      .fillMaxSize()
      .padding(horizontal = 16.dp, vertical = 8.dp)
  ) {
    // Left profile card (matching reference screenshot)
    Column(
      modifier = Modifier
        .width(280.dp)
        .fillMaxHeight()
        .clip(RoundedCornerShape(14.dp))
        .background(Color(0x38FFFFFF))
        .border(0.5.dp, Color(0x33FFFFFF), RoundedCornerShape(14.dp))
        .padding(16.dp)
        .verticalScroll(scrollState),
      horizontalAlignment = Alignment.CenterHorizontally
    ) {
      // Avatar with VIP badge
      Box(
        modifier = Modifier.size(72.dp),
        contentAlignment = Alignment.Center
      ) {
        Box(
          modifier = Modifier
            .size(68.dp)
            .clip(CircleShape)
            .background(Color(0xFF888888)),
          contentAlignment = Alignment.Center
        ) {
          Icon(
            imageVector = Icons.Default.Person,
            contentDescription = null,
            tint = Color.White,
            modifier = Modifier.size(44.dp)
          )
        }
        Box(
          modifier = Modifier
            .align(Alignment.TopEnd)
            .size(22.dp)
            .clip(CircleShape)
            .background(AccentGold),
          contentAlignment = Alignment.Center
        ) {
          Icon(
            imageVector = Icons.Default.Star,
            contentDescription = null,
            tint = Color.Black,
            modifier = Modifier.size(14.dp)
          )
        }
      }

      Spacer(modifier = Modifier.height(10.dp))

      Text(
        text = accountInfo?.username ?: activeServer?.username ?: "Sem Servidor Ativo",
        color = Color.White,
        fontWeight = FontWeight.Bold,
        fontSize = 18.sp
      )

      val expText = accountInfo?.expDateFormatted ?: activeServer?.expireDateFormatted ?: "Conecte um servidor"
      Text(
        text = "Seu plano expirará em: $expText",
        color = Color.White.copy(alpha = 0.75f),
        fontSize = 12.sp,
        modifier = Modifier.padding(top = 4.dp, bottom = 12.dp)
      )

      // Info rows
      ProfileInfoPill(label = "Senha", value = "Já foi definida")
      Spacer(modifier = Modifier.height(6.dp))
      ProfileInfoPill(label = "Servidor", value = activeServer?.name ?: "Nenhum")
      Spacer(modifier = Modifier.height(6.dp))
      ProfileInfoPill(label = "Conexões ativas", value = "${accountInfo?.activeConnections ?: "0"} / ${accountInfo?.maxConnections ?: "1"}")

      Spacer(modifier = Modifier.height(16.dp))

      Button(
        onClick = onNavigateToServers,
        colors = ButtonDefaults.buttonColors(containerColor = RubyPrimary),
        modifier = Modifier
          .fillMaxWidth()
          .testTag("manage_servers_from_profile")
      ) {
        Icon(Icons.Default.Dns, contentDescription = null, tint = Color.White, modifier = Modifier.size(16.dp))
        Spacer(modifier = Modifier.width(6.dp))
        Text("Gerenciar Servidores", color = Color.White, fontSize = 12.sp)
      }
    }

    Spacer(modifier = Modifier.width(16.dp))

    // Right action tiles & shortcuts
    Column(
      modifier = Modifier
        .weight(1f)
        .fillMaxHeight()
    ) {
      Text(
        text = "Ações Rápidas",
        color = Color.White,
        fontWeight = FontWeight.Bold,
        fontSize = 16.sp,
        modifier = Modifier.padding(bottom = 10.dp)
      )

      // 4 Action Tiles from screenshot
      Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.spacedBy(10.dp)
      ) {
        ProfileFeatureTile(
          title = "Segurança da conta",
          icon = Icons.Default.Security,
          gradientColors = listOf(Color(0xFF1E88E5), Color(0xFF1565C0)),
          modifier = Modifier.weight(1f)
        )
        ProfileFeatureTile(
          title = "Logar no celular",
          icon = Icons.Default.PhoneAndroid,
          gradientColors = listOf(Color(0xFF43A047), Color(0xFF2E7D32)),
          modifier = Modifier.weight(1f)
        )
        ProfileFeatureTile(
          title = "Histórico",
          icon = Icons.Default.History,
          gradientColors = listOf(Color(0xFFFB8C00), Color(0xFFEF6C00)),
          modifier = Modifier
            .weight(1f)
            .clickable { onNavigateToHistory() }
        )
        ProfileFeatureTile(
          title = "Favoritos",
          icon = Icons.Default.Favorite,
          gradientColors = listOf(Color(0xFFE91E63), Color(0xFFC2185B)),
          modifier = Modifier
            .weight(1f)
            .clickable { onNavigateToFavorites() }
        )
      }

      Spacer(modifier = Modifier.height(20.dp))

      // Server Info Card
      Card(
        shape = RoundedCornerShape(12.dp),
        colors = CardDefaults.cardColors(containerColor = Color(0x22FFFFFF)),
        border = androidx.compose.foundation.BorderStroke(0.5.dp, Color(0x33FFFFFF)),
        modifier = Modifier.fillMaxWidth()
      ) {
        Column(modifier = Modifier.padding(16.dp)) {
          Text(
            text = "Detalhes do Protocolo Xtream Codes",
            color = AccentGold,
            fontWeight = FontWeight.Bold,
            fontSize = 14.sp
          )
          Spacer(modifier = Modifier.height(6.dp))
          Text(
            text = "• Endereço Base: ${activeServer?.baseUrl ?: "Não conectado"}\n" +
              "• Status: ${activeServer?.connectionStatus?.name ?: "IDLE"}\n" +
              "• Armazenamento Seguro: Senha criptografada localmente via Keystore AES\n" +
              "• Isolamento de Cache: Histórico e favoritos vinculados exclusivamente ao ID deste servidor.",
            color = Color.White.copy(alpha = 0.85f),
            fontSize = 12.sp,
            lineHeight = 18.sp
          )
        }
      }
    }
  }
}

@Composable
private fun ProfileInfoPill(label: String, value: String) {
  Box(
    modifier = Modifier
      .fillMaxWidth()
      .clip(RoundedCornerShape(8.dp))
      .background(Color(0x33000000))
      .padding(horizontal = 10.dp, vertical = 6.dp)
  ) {
    Row(
      modifier = Modifier.fillMaxWidth(),
      horizontalArrangement = Arrangement.SpaceBetween
    ) {
      Text(label, color = Color.White.copy(0.6f), fontSize = 11.sp)
      Text(value, color = Color.White, fontWeight = FontWeight.SemiBold, fontSize = 11.sp)
    }
  }
}

@Composable
private fun ProfileFeatureTile(
  title: String,
  icon: androidx.compose.ui.graphics.vector.ImageVector,
  gradientColors: List<Color>,
  modifier: Modifier = Modifier
) {
  Box(
    modifier = modifier
      .height(84.dp)
      .clip(RoundedCornerShape(10.dp))
      .background(Brush.verticalGradient(gradientColors))
      .border(0.5.dp, Color(0x33FFFFFF), RoundedCornerShape(10.dp))
      .padding(10.dp),
    contentAlignment = Alignment.Center
  ) {
    Column(horizontalAlignment = Alignment.CenterHorizontally) {
      Icon(icon, contentDescription = null, tint = Color.White, modifier = Modifier.size(24.dp))
      Spacer(modifier = Modifier.height(6.dp))
      Text(
        text = title,
        color = Color.White,
        fontWeight = FontWeight.Bold,
        fontSize = 11.sp,
        maxLines = 1
      )
    }
  }
}
