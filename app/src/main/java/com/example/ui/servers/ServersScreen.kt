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
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.CloudDownload
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.Dns
import androidx.compose.material.icons.filled.Edit
import androidx.compose.material.icons.filled.Error
import androidx.compose.material.icons.filled.Link
import androidx.compose.material.icons.filled.Lock
import androidx.compose.material.icons.filled.Person
import androidx.compose.material.icons.filled.PlayArrow
import androidx.compose.material.icons.filled.Refresh
import androidx.compose.material.icons.filled.Visibility
import androidx.compose.material.icons.filled.VisibilityOff
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.OutlinedTextFieldDefaults
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.compose.ui.text.input.VisualTransformation
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.domain.model.ConnectionStatus
import com.example.domain.model.ServerProfile
import com.example.security.UrlSanitizer
import com.example.ui.theme.AccentGold
import com.example.ui.theme.DarkSurface
import com.example.ui.theme.RubyPrimary
import com.example.ui.theme.StatusGreen
import com.example.ui.theme.StatusOrange
import kotlinx.coroutines.launch

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
  onPlayDirectUrl: (url: String, title: String) -> Unit,
  onOpenWebUrl: (url: String, title: String) -> Unit,
  onNavigateToContent: (() -> Unit)? = null,
  modifier: Modifier = Modifier
) {
  var editingServerId by remember { mutableStateOf<String?>(null) }
  var name by remember { mutableStateOf("") }
  var url by remember { mutableStateOf("") }
  var username by remember { mutableStateOf("") }
  var password by remember { mutableStateOf("") }
  var isPasswordVisible by remember { mutableStateOf(false) }
  var validationError by remember { mutableStateOf<String?>(null) }
  var serverToDelete by remember { mutableStateOf<ServerProfile?>(null) }
  var lastSavedSuccessServer by remember { mutableStateOf<String?>(null) }

  val listState = rememberLazyListState()
  val coroutineScope = rememberCoroutineScope()

  fun resetForm() {
    editingServerId = null
    name = ""
    url = ""
    username = ""
    password = ""
    validationError = null
  }

  fun loadServerForEdit(s: ServerProfile) {
    editingServerId = s.id
    name = s.name
    url = s.baseUrl
    username = s.username
    password = s.password
    validationError = null
    coroutineScope.launch {
      listState.animateScrollToItem(1)
    }
  }

  LazyColumn(
    state = listState,
    modifier = modifier
      .fillMaxSize()
      .padding(horizontal = 16.dp, vertical = 6.dp),
    verticalArrangement = Arrangement.spacedBy(16.dp)
  ) {
    // 1. Cabeçalho Principal da Seção
    item {
      Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically
      ) {
        Column(modifier = Modifier.weight(1f)) {
          Text(
            text = "Gerenciar Servidores",
            color = Color.White,
            fontWeight = FontWeight.ExtraBold,
            fontSize = 20.sp
          )
          Text(
            text = "Cadastre, teste e alterne entre seus servidores Xtream Codes.",
            color = Color.White.copy(alpha = 0.8f),
            fontSize = 13.sp
          )
        }

        if (editingServerId != null) {
          OutlinedButton(
            onClick = { resetForm() },
            colors = ButtonDefaults.outlinedButtonColors(contentColor = AccentGold),
            border = androidx.compose.foundation.BorderStroke(1.dp, AccentGold)
          ) {
            Icon(Icons.Default.Close, contentDescription = null, modifier = Modifier.size(16.dp))
            Spacer(modifier = Modifier.width(4.dp))
            Text("Cancelar Edição", fontSize = 12.sp)
          }
        }
      }
    }

    // Direct URL / Web Link Playback Card
    item {
      var directInputUrl by remember { mutableStateOf("") }
      var directInputTitle by remember { mutableStateOf("") }
      var directUrlError by remember { mutableStateOf<String?>(null) }

      Card(
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(
          containerColor = Color(0xFF14141F)
        ),
        border = androidx.compose.foundation.BorderStroke(1.5.dp, AccentGold),
        modifier = Modifier.fillMaxWidth()
      ) {
        Column(modifier = Modifier.padding(18.dp)) {
          Row(verticalAlignment = Alignment.CenterVertically) {
            Box(
              modifier = Modifier
                .size(36.dp)
                .clip(CircleShape)
                .background(AccentGold),
              contentAlignment = Alignment.Center
            ) {
              Icon(Icons.Default.PlayArrow, contentDescription = null, tint = Color.Black, modifier = Modifier.size(20.dp))
            }
            Spacer(modifier = Modifier.width(10.dp))
            Column {
              Text(
                text = "Reprodução Direta por URL ou Web Link",
                color = Color.White,
                fontWeight = FontWeight.Bold,
                fontSize = 16.sp
              )
              Text(
                text = "Cole qualquer link de vídeo (MP4, HLS, M3U) ou página web",
                color = AccentGold,
                fontSize = 11.sp
              )
            }
          }

          Spacer(modifier = Modifier.height(14.dp))

          OutlinedTextField(
            value = directInputTitle,
            onValueChange = { directInputTitle = it },
            placeholder = { Text("Título / Nome (Opcional)") },
            colors = getVisibleTextFieldColors(),
            shape = RoundedCornerShape(10.dp),
            singleLine = true,
            modifier = Modifier.fillMaxWidth()
          )

          Spacer(modifier = Modifier.height(8.dp))

          OutlinedTextField(
            value = directInputUrl,
            onValueChange = {
              directInputUrl = it
              directUrlError = null
            },
            placeholder = { Text("Ex: https://exemplo.com/video.mp4 ou .m3u8") },
            leadingIcon = {
              Icon(Icons.Default.Link, contentDescription = null, tint = AccentGold)
            },
            keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Uri),
            colors = getVisibleTextFieldColors(),
            shape = RoundedCornerShape(10.dp),
            singleLine = true,
            modifier = Modifier
              .fillMaxWidth()
              .testTag("direct_url_input")
          )

          if (directUrlError != null) {
            Spacer(modifier = Modifier.height(6.dp))
            Text(text = "⚠️ $directUrlError", color = Color(0xFFEF5350), fontSize = 12.sp)
          }

          Spacer(modifier = Modifier.height(14.dp))

          Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(10.dp)
          ) {
            Button(
              onClick = {
                var u = directInputUrl.trim()
                if (u.isBlank()) {
                  directUrlError = "Insira um link/URL válido."
                } else {
                  if (!u.startsWith("http://") && !u.startsWith("https://") && !u.startsWith("rtsp://") && !u.startsWith("rtmp://")) {
                    u = "https://$u"
                  }
                  onPlayDirectUrl(u, directInputTitle.ifBlank { "Link Direto" })
                }
              },
              colors = ButtonDefaults.buttonColors(containerColor = RubyPrimary),
              shape = RoundedCornerShape(10.dp),
              modifier = Modifier
                .weight(1f)
                .height(46.dp)
                .testTag("play_direct_video_button")
            ) {
              Icon(Icons.Default.PlayArrow, contentDescription = null, tint = Color.White)
              Spacer(modifier = Modifier.width(4.dp))
              Text("Reproduzir Vídeo", color = Color.White, fontWeight = FontWeight.Bold, fontSize = 12.sp)
            }
            Button(
              onClick = {
                var u = directInputUrl.trim()
                if (u.isBlank()) {
                  directUrlError = "Insira um link/URL válido."
                } else {
                  if (!u.startsWith("http://") && !u.startsWith("https://")) {
                    u = "https://$u"
                  }
                  onOpenWebUrl(u, directInputTitle.ifBlank { "Página Web" })
                }
              },
              colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF2C3E50)),
              shape = RoundedCornerShape(10.dp),
              modifier = Modifier
                .weight(1f)
                .height(46.dp)
                .testTag("open_web_page_button")
            ) {
              Icon(Icons.Default.Link, contentDescription = null, tint = AccentGold)
              Spacer(modifier = Modifier.width(4.dp))
              Text("Abrir Web", color = Color.White, fontWeight = FontWeight.Bold, fontSize = 12.sp)
            }
          }
        }
      }
    }

    // 2. FORMULÁRIO DE CADASTRO COM CAMPOS DESTACADOS E VISÍVEIS
    item {
      Card(
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(
          containerColor = Color(0xFF1D0A10) // Fundo vinho escuro sólido de alto contraste
        ),
        border = androidx.compose.foundation.BorderStroke(
          width = 1.5.dp,
          color = if (editingServerId != null) AccentGold else Color(0x66E50914)
        ),
        modifier = Modifier.fillMaxWidth()
      ) {
        Column(modifier = Modifier.padding(18.dp)) {
          // Topo do Formulário
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
                  .background(RubyPrimary),
                contentAlignment = Alignment.Center
              ) {
                Icon(
                  imageVector = Icons.Default.Dns,
                  contentDescription = null,
                  tint = Color.White,
                  modifier = Modifier.size(20.dp)
                )
              }
              Spacer(modifier = Modifier.width(10.dp))
              Column {
                Text(
                  text = if (editingServerId != null) "Editar Dados do Servidor" else "Adicionar Dados do Servidor",
                  color = Color.White,
                  fontWeight = FontWeight.Bold,
                  fontSize = 17.sp
                )
                Text(
                  text = "Xtream Codes API v2",
                  color = AccentGold,
                  fontWeight = FontWeight.SemiBold,
                  fontSize = 11.sp
                )
              }
            }

            if (editingServerId != null && activeServer?.id == editingServerId) {
              Box(
                modifier = Modifier
                  .clip(RoundedCornerShape(6.dp))
                  .background(StatusGreen)
                  .padding(horizontal = 8.dp, vertical = 4.dp)
              ) {
                Text("ATIVO", color = Color.White, fontWeight = FontWeight.Bold, fontSize = 10.sp)
              }
            }
          }

          Spacer(modifier = Modifier.height(10.dp))

          Text(
            text = "Informe o nome, endereço (URL), usuário e senha para que o UniMult conecte e puxe os canais, filmes e séries:",
            color = Color.White.copy(alpha = 0.85f),
            fontSize = 13.sp,
            lineHeight = 18.sp
          )

          Spacer(modifier = Modifier.height(16.dp))

          // CAMPO 1: NOME DA CONEXÃO
          Text(
            text = "Nome da Conexão / Servidor",
            color = Color.White,
            fontWeight = FontWeight.SemiBold,
            fontSize = 13.sp,
            modifier = Modifier.padding(bottom = 4.dp, start = 2.dp)
          )
          OutlinedTextField(
            value = name,
            onValueChange = {
              name = it
              validationError = null
            },
            placeholder = { Text("Ex: Meu Servidor Principal") },
            leadingIcon = {
              Icon(Icons.Default.Dns, contentDescription = null, tint = AccentGold)
            },
            colors = getVisibleTextFieldColors(),
            shape = RoundedCornerShape(10.dp),
            singleLine = true,
            modifier = Modifier
              .fillMaxWidth()
              .testTag("input_server_name")
          )

          Spacer(modifier = Modifier.height(12.dp))

          // CAMPO 2: URL / ENDEREÇO DO SERVIDOR
          Text(
            text = "URL / Endereço do Servidor (com porta)",
            color = Color.White,
            fontWeight = FontWeight.SemiBold,
            fontSize = 13.sp,
            modifier = Modifier.padding(bottom = 4.dp, start = 2.dp)
          )
          OutlinedTextField(
            value = url,
            onValueChange = {
              url = it
              validationError = null
            },
            placeholder = { Text("Ex: http://servidor.exemplo.com:8080") },
            leadingIcon = {
              Icon(Icons.Default.Link, contentDescription = null, tint = AccentGold)
            },
            keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Uri),
            colors = getVisibleTextFieldColors(),
            shape = RoundedCornerShape(10.dp),
            singleLine = true,
            modifier = Modifier
              .fillMaxWidth()
              .testTag("input_server_url")
          )

          if (url.isNotBlank() && !UrlSanitizer.isHttps(url)) {
            Text(
              text = "ℹ️ Conexão HTTP compatível com provedores IPTV.",
              color = AccentGold,
              fontSize = 11.sp,
              modifier = Modifier.padding(start = 4.dp, top = 3.dp)
            )
          }

          Spacer(modifier = Modifier.height(12.dp))

          // CAMPO 3: NOME DE USUÁRIO
          Text(
            text = "Nome de Usuário",
            color = Color.White,
            fontWeight = FontWeight.SemiBold,
            fontSize = 13.sp,
            modifier = Modifier.padding(bottom = 4.dp, start = 2.dp)
          )
          OutlinedTextField(
            value = username,
            onValueChange = {
              username = it
              validationError = null
            },
            placeholder = { Text("Digite seu usuário") },
            leadingIcon = {
              Icon(Icons.Default.Person, contentDescription = null, tint = AccentGold)
            },
            colors = getVisibleTextFieldColors(),
            shape = RoundedCornerShape(10.dp),
            singleLine = true,
            modifier = Modifier
              .fillMaxWidth()
              .testTag("input_server_user")
          )

          Spacer(modifier = Modifier.height(12.dp))

          // CAMPO 4: SENHA
          Text(
            text = "Senha de Acesso",
            color = Color.White,
            fontWeight = FontWeight.SemiBold,
            fontSize = 13.sp,
            modifier = Modifier.padding(bottom = 4.dp, start = 2.dp)
          )
          OutlinedTextField(
            value = password,
            onValueChange = {
              password = it
              validationError = null
            },
            placeholder = { Text("Digite sua senha") },
            leadingIcon = {
              Icon(Icons.Default.Lock, contentDescription = null, tint = AccentGold)
            },
            trailingIcon = {
              IconButton(onClick = { isPasswordVisible = !isPasswordVisible }) {
                Icon(
                  imageVector = if (isPasswordVisible) Icons.Default.Visibility else Icons.Default.VisibilityOff,
                  contentDescription = if (isPasswordVisible) "Ocultar senha" else "Mostrar senha",
                  tint = Color.White.copy(0.8f)
                )
              }
            },
            visualTransformation = if (isPasswordVisible) VisualTransformation.None else PasswordVisualTransformation(),
            colors = getVisibleTextFieldColors(),
            shape = RoundedCornerShape(10.dp),
            singleLine = true,
            modifier = Modifier
              .fillMaxWidth()
              .testTag("input_server_password")
          )

          // Painel de Teste de Conexão em Tempo Real
          when (testStatus) {
            is TestStatus.Testing -> {
              Spacer(modifier = Modifier.height(14.dp))
              Row(
                modifier = Modifier
                  .fillMaxWidth()
                  .clip(RoundedCornerShape(8.dp))
                  .background(Color(0x33FFFFFF))
                  .border(1.dp, AccentGold, RoundedCornerShape(8.dp))
                  .padding(12.dp),
                verticalAlignment = Alignment.CenterVertically
              ) {
                CircularProgressIndicator(
                  modifier = Modifier.size(20.dp),
                  color = AccentGold,
                  strokeWidth = 2.dp
                )
                Spacer(modifier = Modifier.width(12.dp))
                Text(
                  text = "Testando comunicação com o servidor e validando credenciais...",
                  color = Color.White,
                  fontSize = 12.sp
                )
              }
            }
            is TestStatus.Success -> {
              Spacer(modifier = Modifier.height(14.dp))
              Row(
                modifier = Modifier
                  .fillMaxWidth()
                  .clip(RoundedCornerShape(8.dp))
                  .background(Color(0x334CAF50))
                  .border(1.dp, StatusGreen, RoundedCornerShape(8.dp))
                  .padding(12.dp),
                verticalAlignment = Alignment.CenterVertically
              ) {
                Icon(Icons.Default.CheckCircle, contentDescription = null, tint = StatusGreen)
                Spacer(modifier = Modifier.width(10.dp))
                Column {
                  Text(
                    text = "Conexão Validada com Sucesso!",
                    color = Color.White,
                    fontWeight = FontWeight.Bold,
                    fontSize = 13.sp
                  )
                  Text(
                    text = "Status: ${testStatus.accountInfo.status} • Expira: ${testStatus.accountInfo.expDateFormatted} • Conexões: ${testStatus.accountInfo.activeConnections}/${testStatus.accountInfo.maxConnections}",
                    color = Color.White.copy(alpha = 0.9f),
                    fontSize = 11.sp
                  )
                }
              }
            }
            is TestStatus.Error -> {
              Spacer(modifier = Modifier.height(14.dp))
              Row(
                modifier = Modifier
                  .fillMaxWidth()
                  .clip(RoundedCornerShape(8.dp))
                  .background(Color(0x33F44336))
                  .border(1.dp, Color(0xFFEF5350), RoundedCornerShape(8.dp))
                  .padding(12.dp),
                verticalAlignment = Alignment.CenterVertically
              ) {
                Icon(Icons.Default.Error, contentDescription = null, tint = Color(0xFFEF5350))
                Spacer(modifier = Modifier.width(10.dp))
                Text(
                  text = testStatus.message,
                  color = Color.White,
                  fontSize = 12.sp
                )
              }
            }
            TestStatus.Idle -> {}
          }

          if (validationError != null) {
            Spacer(modifier = Modifier.height(10.dp))
            Text(
              text = "⚠️ $validationError",
              color = Color(0xFFFF5252),
              fontSize = 12.sp,
              fontWeight = FontWeight.SemiBold
            )
          }

          if (lastSavedSuccessServer != null) {
            Spacer(modifier = Modifier.height(12.dp))
            Row(
              modifier = Modifier
                .fillMaxWidth()
                .clip(RoundedCornerShape(8.dp))
                .background(Color(0x334CAF50))
                .border(1.dp, StatusGreen, RoundedCornerShape(8.dp))
                .padding(10.dp),
              horizontalArrangement = Arrangement.SpaceBetween,
              verticalAlignment = Alignment.CenterVertically
            ) {
              Column(modifier = Modifier.weight(1f)) {
                Text(
                  text = "Servidor \"$lastSavedSuccessServer\" salvo e conectado!",
                  color = Color.White,
                  fontWeight = FontWeight.Bold,
                  fontSize = 12.sp
                )
                Text(
                  text = "Canais, filmes e séries estão sendo carregados.",
                  color = Color.White.copy(alpha = 0.85f),
                  fontSize = 11.sp
                )
              }
              if (onNavigateToContent != null) {
                Button(
                  onClick = onNavigateToContent,
                  colors = ButtonDefaults.buttonColors(containerColor = RubyPrimary),
                  modifier = Modifier.height(34.dp)
                ) {
                  Text("Ver Conteúdos", fontSize = 11.sp)
                }
              }
            }
          }

          Spacer(modifier = Modifier.height(18.dp))

          // BOTÕES DE AÇÃO PRINCIPAIS
          Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(10.dp)
          ) {
            // Botão Testar Conexão
            OutlinedButton(
              onClick = {
                if (url.isBlank() || username.isBlank() || password.isBlank()) {
                  validationError = "Preencha a URL, usuário e senha para testar."
                } else {
                  validationError = null
                  onDraftTest(url, username, password)
                }
              },
              border = androidx.compose.foundation.BorderStroke(1.2.dp, AccentGold),
              modifier = Modifier
                .weight(1f)
                .height(48.dp)
                .testTag("test_server_button")
            ) {
              Icon(Icons.Default.Refresh, contentDescription = null, tint = AccentGold, modifier = Modifier.size(18.dp))
              Spacer(modifier = Modifier.width(6.dp))
              Text("Testar", color = AccentGold, fontWeight = FontWeight.Bold, fontSize = 13.sp)
            }

            // Botão Salvar e Carregar Conteúdos
            Button(
              onClick = {
                if (name.isBlank()) {
                  validationError = "Por favor, defina um nome para a conexão."
                  return@Button
                }
                if (url.isBlank()) {
                  validationError = "Informe o endereço/URL do servidor."
                  return@Button
                }
                if (username.isBlank() || password.isBlank()) {
                  validationError = "Informe o usuário e a senha."
                  return@Button
                }

                val serverName = name.trim()
                onSaveServer(
                  editingServerId,
                  serverName,
                  url.trim(),
                  username.trim(),
                  password,
                  true // Define como ativo para carregar os conteúdos imediatamente
                )
                lastSavedSuccessServer = serverName
                resetForm()
              },
              colors = ButtonDefaults.buttonColors(containerColor = RubyPrimary),
              shape = RoundedCornerShape(10.dp),
              modifier = Modifier
                .weight(1.4f)
                .height(48.dp)
                .testTag("save_and_connect_button")
            ) {
              Icon(Icons.Default.CloudDownload, contentDescription = null, tint = Color.White, modifier = Modifier.size(20.dp))
              Spacer(modifier = Modifier.width(6.dp))
              Text("Salvar e Carregar", color = Color.White, fontWeight = FontWeight.ExtraBold, fontSize = 13.sp)
            }
          }
        }
      }
    }

    // 3. Seção com Servidores Cadastrados
    item {
      Row(
        modifier = Modifier
          .fillMaxWidth()
          .padding(top = 8.dp),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically
      ) {
        Text(
          text = "Servidores Cadastrados (${servers.size})",
          color = Color.White,
          fontWeight = FontWeight.Bold,
          fontSize = 17.sp
        )
        if (activeServer != null) {
          Text(
            text = "Ativo: ${activeServer.name}",
            color = AccentGold,
            fontSize = 12.sp,
            fontWeight = FontWeight.SemiBold
          )
        }
      }
    }

    if (servers.isEmpty()) {
      item {
        Box(
          modifier = Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(12.dp))
            .background(Color(0x1AFFFFFF))
            .padding(20.dp),
          contentAlignment = Alignment.Center
        ) {
          Text(
            text = "Nenhum servidor salvo. Preencha os campos acima para conectar e carregar seus canais, filmes e séries.",
            color = Color.White.copy(0.7f),
            fontSize = 13.sp
          )
        }
      }
    } else {
      items(servers, key = { it.id }) { server ->
        val isActive = server.id == activeServer?.id

        ServerCardItem(
          server = server,
          isActive = isActive,
          onSelectActive = {
            onSelectActiveServer(server)
            lastSavedSuccessServer = server.name
          },
          onEdit = { loadServerForEdit(server) },
          onDelete = { serverToDelete = server },
          onTest = { onTestServer(server) }
        )
      }
    }

    item {
      Spacer(modifier = Modifier.height(40.dp))
    }
  }

  // Diálogo de confirmação para exclusão
  serverToDelete?.let { s ->
    AlertDialog(
      onDismissRequest = { serverToDelete = null },
      containerColor = DarkSurface,
      title = {
        Text("Excluir Conexão?", color = Color.White, fontWeight = FontWeight.Bold)
      },
      text = {
        Text(
          "Tem certeza que deseja remover o servidor \"${s.name}\"? Suas credenciais e dados locais serão apagados com segurança.",
          color = Color.White.copy(0.85f)
        )
      },
      confirmButton = {
        Button(
          onClick = {
            onDeleteServer(s.id, s.name)
            if (editingServerId == s.id) resetForm()
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
      containerColor = if (isActive) Color(0x3BFF334B) else Color(0xFF1B0B10)
    ),
    border = androidx.compose.foundation.BorderStroke(
      width = if (isActive) 1.5.dp else 0.8.dp,
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
                    text = "CONECTADO",
                    color = Color.Black,
                    fontWeight = FontWeight.ExtraBold,
                    fontSize = 10.sp
                  )
                }
              }
            }
            Text(
              text = "${server.username} @ ${server.baseUrl}",
              color = Color.White.copy(alpha = 0.75f),
              fontSize = 12.sp
            )
          }
        }

        Row {
          IconButton(onClick = onTest) {
            Icon(Icons.Default.Refresh, contentDescription = "Testar", tint = AccentGold)
          }
          IconButton(onClick = onEdit) {
            Icon(Icons.Default.Edit, contentDescription = "Editar", tint = Color.White.copy(0.85f))
          }
          IconButton(onClick = onDelete) {
            Icon(Icons.Default.Delete, contentDescription = "Excluir", tint = Color(0xFFEF5350))
          }
        }
      }

      Spacer(modifier = Modifier.height(10.dp))

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
            ConnectionStatus.IDLE -> Color.Gray to "Disponível"
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
              color = Color.White.copy(alpha = 0.7f),
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

/**
 * Cores de alto contraste e preenchimento sólido para os campos de entrada,
 * garantindo que as caixas de preenchimento (inputs) sejam imediatamente visíveis e legíveis.
 */
@Composable
private fun getVisibleTextFieldColors() = OutlinedTextFieldDefaults.colors(
  focusedTextColor = Color.White,
  unfocusedTextColor = Color.White,
  focusedContainerColor = Color(0xFF280C14),
  unfocusedContainerColor = Color(0xFF200910),
  focusedBorderColor = AccentGold,
  unfocusedBorderColor = Color(0x66FFFFFF),
  focusedLabelColor = AccentGold,
  unfocusedLabelColor = Color.White.copy(alpha = 0.8f),
  cursorColor = AccentGold,
  focusedPlaceholderColor = Color.White.copy(alpha = 0.45f),
  unfocusedPlaceholderColor = Color.White.copy(alpha = 0.4f)
)
