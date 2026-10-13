package com.example.ui.servers

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
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.Dns
import androidx.compose.material.icons.filled.Edit
import androidx.compose.material.icons.filled.Refresh
import androidx.compose.material.icons.filled.Security
import androidx.compose.material.icons.filled.Warning
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.FloatingActionButton
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.OutlinedButton
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
import com.example.domain.model.ConnectionStatus
import com.example.domain.model.ServerProfile
import com.example.ui.theme.AccentGold
import com.example.ui.theme.DarkSurface
import com.example.ui.theme.RubyPrimary
import com.example.ui.theme.StatusGreen
import com.example.ui.theme.StatusOrange
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

@Composable
fun ServersScreen(
  servers: List<ServerProfile>,
  activeServer: ServerProfile?,
  testStatus: TestStatus,
  onSelectActiveServer: (ServerProfile) -> Unit,
  onDeleteServer: (id: String, name: String) -> Unit,
  onTestServer: (ServerProfile) -> Unit,
  onDraftTest: (url: String, user: String, pass: String) -> Unit,
  onSaveServer: (id: String?, name: String, url: String, user: String, pass: String, makeActive: Boolean) -> Unit,
  modifier: Modifier = Modifier
) {
  var showAddEditDialog by remember { mutableStateOf(false) }
  var serverToEdit by remember { mutableStateOf<ServerProfile?>(null) }
  var serverToDelete by remember { mutableStateOf<ServerProfile?>(null) }

  Box(modifier = modifier.fillMaxSize()) {
    Column(
      modifier = Modifier
        .fillMaxSize()
        .padding(horizontal = 16.dp, vertical = 8.dp)
    ) {
      Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically
      ) {
        Column {
          Text(
            text = "Gerenciador de Servidores",
            color = Color.White,
            fontWeight = FontWeight.Bold,
            fontSize = 20.sp
          )
          Text(
            text = "Cadastre, teste e alterne livremente entre servidores Xtream Codes.",
            color = Color.White.copy(alpha = 0.7f),
            fontSize = 12.sp
          )
        }

        Button(
          onClick = {
            serverToEdit = null
            showAddEditDialog = true
          },
          colors = ButtonDefaults.buttonColors(containerColor = RubyPrimary),
          modifier = Modifier.testTag("add_server_top_button")
        ) {
          Icon(Icons.Default.Add, contentDescription = null, tint = Color.White, modifier = Modifier.size(18.dp))
          Spacer(modifier = Modifier.width(4.dp))
          Text("Adicionar Servidor", color = Color.White, fontSize = 13.sp)
        }
      }

      Spacer(modifier = Modifier.height(16.dp))

      if (servers.isEmpty()) {
        EmptyServersPlaceholder(onAddClick = {
          serverToEdit = null
          showAddEditDialog = true
        })
      } else {
        LazyColumn(
          modifier = Modifier.fillMaxSize(),
          verticalArrangement = Arrangement.spacedBy(12.dp)
        ) {
          items(servers, key = { it.id }) { server ->
            val isActive = server.id == activeServer?.id

            ServerCardItem(
              server = server,
              isActive = isActive,
              onSelectActive = { onSelectActiveServer(server) },
              onEdit = {
                serverToEdit = server
                showAddEditDialog = true
              },
              onDelete = { serverToDelete = server },
              onTest = { onTestServer(server) }
            )
          }

          item {
            Spacer(modifier = Modifier.height(60.dp))
          }
        }
      }
    }

    // Dialog for adding / editing server
    if (showAddEditDialog) {
      AddEditServerDialog(
        serverToEdit = serverToEdit,
        testStatus = testStatus,
        onDismiss = { showAddEditDialog = false },
        onTestConnection = onDraftTest,
        onSave = { id, name, url, user, pass, makeActive ->
          onSaveServer(id, name, url, user, pass, makeActive)
          showAddEditDialog = false
        }
      )
    }

    // Confirmation dialog for deletion
    serverToDelete?.let { s ->
      AlertDialog(
        onDismissRequest = { serverToDelete = null },
        containerColor = DarkSurface,
        title = {
          Text("Excluir Conexão?", color = Color.White, fontWeight = FontWeight.Bold)
        },
        text = {
          Text(
            "Tem certeza que deseja remover o servidor \"${s.name}\"? Suas credenciais e dados locais serão removidos com segurança.",
            color = Color.White.copy(0.8f)
          )
        },
        confirmButton = {
          Button(
            onClick = {
              onDeleteServer(s.id, s.name)
              serverToDelete = null
            },
            colors = ButtonDefaults.buttonColors(containerColor = Color(0xFFD32F2F))
          ) {
            Text("Sim, Excluir", color = Color.White)
          }
        },
        dismissButton = {
          TextButton(onClick = { serverToDelete = null }) {
            Text("Cancelar", color = Color.White.copy(0.7f))
          }
        }
      )
    }
  }
}

@Composable
private fun ServerCardItem(
  server: ServerProfile,
  isActive: Boolean,
  onSelectActive: () -> Unit,
  onEdit: () -> Unit,
  onDelete: () -> Unit,
  onTest: () -> Unit
) {
  Card(
    shape = RoundedCornerShape(12.dp),
    colors = CardDefaults.cardColors(
      containerColor = if (isActive) Color(0x3DFF334B) else Color(0x22FFFFFF)
    ),
    border = androidx.compose.foundation.BorderStroke(
      width = if (isActive) 1.5.dp else 0.5.dp,
      color = if (isActive) AccentGold else Color(0x33FFFFFF)
    ),
    modifier = Modifier
      .fillMaxWidth()
      .testTag("server_card_${server.id}")
  ) {
    Column(modifier = Modifier.padding(14.dp)) {
      Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically
      ) {
        Row(verticalAlignment = Alignment.CenterVertically) {
          Box(
            modifier = Modifier
              .size(36.dp)
              .clip(CircleShape)
              .background(if (isActive) RubyPrimary else Color(0x33FFFFFF)),
            contentAlignment = Alignment.Center
          ) {
            Icon(
              imageVector = Icons.Default.Dns,
              contentDescription = null,
              tint = Color.White,
              modifier = Modifier.size(20.dp)
            )
          }

          Spacer(modifier = Modifier.width(12.dp))

          Column {
            Row(verticalAlignment = Alignment.CenterVertically) {
              Text(
                text = server.name,
                color = Color.White,
                fontWeight = FontWeight.Bold,
                fontSize = 16.sp
              )
              if (isActive) {
                Spacer(modifier = Modifier.width(8.dp))
                Box(
                  modifier = Modifier
                    .clip(RoundedCornerShape(4.dp))
                    .background(AccentGold)
                    .padding(horizontal = 6.dp, vertical = 2.dp)
                ) {
                  Text(
                    text = "ATIVO",
                    color = Color.Black,
                    fontWeight = FontWeight.ExtraBold,
                    fontSize = 10.sp
                  )
                }
              }
            }
            Text(
              text = "${server.username} @ ${server.baseUrl}",
              color = Color.White.copy(alpha = 0.7f),
              fontSize = 12.sp
            )
          }
        }

        // Action buttons
        Row {
          IconButton(onClick = onTest) {
            Icon(Icons.Default.Refresh, contentDescription = "Testar", tint = AccentGold)
          }
          IconButton(onClick = onEdit) {
            Icon(Icons.Default.Edit, contentDescription = "Editar", tint = Color.White.copy(0.8f))
          }
          IconButton(onClick = onDelete) {
            Icon(Icons.Default.Delete, contentDescription = "Excluir", tint = Color(0xFFEF5350))
          }
        }
      }

      Spacer(modifier = Modifier.height(10.dp))

      // Status pill & expiration info
      Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically
      ) {
        Row(verticalAlignment = Alignment.CenterVertically) {
          val (badgeColor, badgeText) = when (server.connectionStatus) {
            ConnectionStatus.ONLINE -> StatusGreen to "Online"
            ConnectionStatus.ERROR -> Color(0xFFD32F2F) to "Erro"
            ConnectionStatus.EXPIRED -> StatusOrange to "Expirado"
            ConnectionStatus.CONNECTING -> AccentGold to "Conectando"
            ConnectionStatus.IDLE -> Color.Gray to "Não testado"
            ConnectionStatus.OFFLINE -> Color.Gray to "Offline"
          }

          Box(
            modifier = Modifier
              .clip(RoundedCornerShape(4.dp))
              .background(badgeColor.copy(alpha = 0.25f))
              .border(0.5.dp, badgeColor, RoundedCornerShape(4.dp))
              .padding(horizontal = 8.dp, vertical = 3.dp)
          ) {
            Text(badgeText, color = badgeColor, fontSize = 11.sp, fontWeight = FontWeight.Bold)
          }

          if (server.expireDateFormatted != null) {
            Spacer(modifier = Modifier.width(10.dp))
            Text(
              text = "Validade: ${server.expireDateFormatted}",
              color = Color.White.copy(alpha = 0.65f),
              fontSize = 11.sp
            )
          }
        }

        if (!isActive) {
          Button(
            onClick = onSelectActive,
            colors = ButtonDefaults.buttonColors(containerColor = Color(0x40FFFFFF)),
            modifier = Modifier
              .height(34.dp)
              .testTag("activate_server_${server.id}")
          ) {
            Icon(Icons.Default.Check, contentDescription = null, tint = Color.White, modifier = Modifier.size(16.dp))
            Spacer(modifier = Modifier.width(4.dp))
            Text("Conectar", color = Color.White, fontSize = 12.sp)
          }
        }
      }
    }
  }
}

@Composable
private fun EmptyServersPlaceholder(onAddClick: () -> Unit) {
  Box(
    modifier = Modifier
      .fillMaxWidth()
      .height(300.dp)
      .clip(RoundedCornerShape(16.dp))
      .background(Color(0x1AFFFFFF))
      .border(1.dp, Color(0x33FFFFFF), RoundedCornerShape(16.dp)),
    contentAlignment = Alignment.Center
  ) {
    Column(
      horizontalAlignment = Alignment.CenterHorizontally,
      modifier = Modifier.padding(24.dp)
    ) {
      Icon(
        imageVector = Icons.Default.Dns,
        contentDescription = null,
        tint = AccentGold,
        modifier = Modifier.size(54.dp)
      )
      Spacer(modifier = Modifier.height(12.dp))
      Text(
        text = "Nenhum Servidor Cadastrado",
        color = Color.White,
        fontWeight = FontWeight.Bold,
        fontSize = 18.sp
      )
      Spacer(modifier = Modifier.height(6.dp))
      Text(
        text = "Adicione seu primeiro servidor Xtream Codes com a URL base, usuário e senha para carregar seus canais, filmes e séries.",
        color = Color.White.copy(alpha = 0.7f),
        fontSize = 13.sp,
        modifier = Modifier.padding(horizontal = 16.dp)
      )
      Spacer(modifier = Modifier.height(16.dp))
      Button(
        onClick = onAddClick,
        colors = ButtonDefaults.buttonColors(containerColor = RubyPrimary),
        modifier = Modifier.testTag("empty_state_add_server")
      ) {
        Icon(Icons.Default.Add, contentDescription = null, tint = Color.White)
        Spacer(modifier = Modifier.width(6.dp))
        Text("Cadastrar Novo Servidor", color = Color.White, fontWeight = FontWeight.Bold)
      }
    }
  }
}
