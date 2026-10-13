package com.example.ui.servers

import androidx.compose.foundation.background
import androidx.compose.foundation.border
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
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.Dns
import androidx.compose.material.icons.filled.Error
import androidx.compose.material.icons.filled.Lock
import androidx.compose.material.icons.filled.Person
import androidx.compose.material.icons.filled.Visibility
import androidx.compose.material.icons.filled.VisibilityOff
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Checkbox
import androidx.compose.material3.CheckboxDefaults
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
import com.example.domain.model.ServerProfile
import com.example.security.UrlSanitizer
import com.example.ui.theme.AccentGold
import com.example.ui.theme.DarkSurface
import com.example.ui.theme.DarkSurfaceVariant
import com.example.ui.theme.RubyPrimary
import com.example.ui.theme.StatusGreen

@Composable
fun AddEditServerDialog(
  serverToEdit: ServerProfile?,
  testStatus: TestStatus,
  onDismiss: () -> Unit,
  onTestConnection: (url: String, user: String, pass: String) -> Unit,
  onSave: (id: String?, name: String, url: String, user: String, pass: String, makeActive: Boolean) -> Unit
) {
  var name by remember { mutableStateOf(serverToEdit?.name ?: "") }
  var url by remember { mutableStateOf(serverToEdit?.baseUrl ?: "") }
  var username by remember { mutableStateOf(serverToEdit?.username ?: "") }
  var password by remember { mutableStateOf(serverToEdit?.password ?: "") }
  var isPasswordVisible by remember { mutableStateOf(false) }
  var makeActive by remember { mutableStateOf(serverToEdit?.isActive ?: true) }
  var validationError by remember { mutableStateOf<String?>(null) }

  val isEditing = serverToEdit != null
  val scrollState = rememberScrollState()

  AlertDialog(
    onDismissRequest = onDismiss,
    containerColor = DarkSurface,
    title = {
      Row(verticalAlignment = Alignment.CenterVertically) {
        Icon(
          imageVector = Icons.Default.Dns,
          contentDescription = null,
          tint = AccentGold,
          modifier = Modifier.size(24.dp)
        )
        Spacer(modifier = Modifier.width(8.dp))
        Text(
          text = if (isEditing) "Editar Servidor" else "Cadastrar Servidor Xtream",
          color = Color.White,
          fontWeight = FontWeight.Bold,
          fontSize = 18.sp
        )
      }
    },
    text = {
      Column(
        modifier = Modifier
          .fillMaxWidth()
          .verticalScroll(scrollState)
      ) {
        Text(
          text = "Insira os dados fornecidos pelo seu provedor de IPTV compatível com Xtream Codes.",
          color = Color.White.copy(alpha = 0.7f),
          fontSize = 13.sp,
          modifier = Modifier.padding(bottom = 12.dp)
        )

        // Nome da Conexão
        OutlinedTextField(
          value = name,
          onValueChange = { name = it },
          label = { Text("Nome da Conexão (ex: Principal, Backup)") },
          colors = getTextFieldColors(),
          singleLine = true,
          modifier = Modifier
            .fillMaxWidth()
            .testTag("input_server_name")
        )

        Spacer(modifier = Modifier.height(10.dp))

        // URL / Host do Servidor
        OutlinedTextField(
          value = url,
          onValueChange = {
            url = it
            validationError = null
          },
          label = { Text("URL / Host (ex: http://servidor.com:8080)") },
          colors = getTextFieldColors(),
          singleLine = true,
          keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Uri),
          modifier = Modifier
            .fillMaxWidth()
            .testTag("input_server_url")
        )

        if (url.isNotBlank() && !UrlSanitizer.isHttps(url)) {
          Text(
            text = "Aviso de segurança: Servidor utiliza HTTP sem criptografia TLS.",
            color = AccentGold,
            fontSize = 11.sp,
            modifier = Modifier.padding(start = 4.dp, top = 2.dp)
          )
        }

        Spacer(modifier = Modifier.height(10.dp))

        // Nome de Usuário
        OutlinedTextField(
          value = username,
          onValueChange = { username = it },
          label = { Text("Usuário") },
          leadingIcon = {
            Icon(Icons.Default.Person, contentDescription = null, tint = Color.White.copy(0.6f))
          },
          colors = getTextFieldColors(),
          singleLine = true,
          modifier = Modifier
            .fillMaxWidth()
            .testTag("input_server_user")
        )

        Spacer(modifier = Modifier.height(10.dp))

        // Senha com toggle de visibilidade
        OutlinedTextField(
          value = password,
          onValueChange = { password = it },
          label = { Text("Senha") },
          leadingIcon = {
            Icon(Icons.Default.Lock, contentDescription = null, tint = Color.White.copy(0.6f))
          },
          trailingIcon = {
            IconButton(onClick = { isPasswordVisible = !isPasswordVisible }) {
              Icon(
                imageVector = if (isPasswordVisible) Icons.Default.Visibility else Icons.Default.VisibilityOff,
                contentDescription = "Alternar visibilidade",
                tint = Color.White.copy(0.7f)
              )
            }
          },
          visualTransformation = if (isPasswordVisible) VisualTransformation.None else PasswordVisualTransformation(),
          colors = getTextFieldColors(),
          singleLine = true,
          modifier = Modifier
            .fillMaxWidth()
            .testTag("input_server_password")
        )

        Spacer(modifier = Modifier.height(12.dp))

        // Checkbox: Tornar ativo
        Row(
          verticalAlignment = Alignment.CenterVertically,
          modifier = Modifier.fillMaxWidth()
        ) {
          Checkbox(
            checked = makeActive,
            onCheckedChange = { makeActive = it },
            colors = CheckboxDefaults.colors(
              checkedColor = RubyPrimary,
              checkmarkColor = Color.White
            )
          )
          Text(
            text = "Definir como servidor ativo imediatamente",
            color = Color.White,
            fontSize = 13.sp
          )
        }

        // Test status banner
        when (testStatus) {
          is TestStatus.Testing -> {
            Row(
              modifier = Modifier
                .fillMaxWidth()
                .padding(vertical = 8.dp)
                .clip(RoundedCornerShape(8.dp))
                .background(Color(0x33FFFFFF))
                .padding(10.dp),
              verticalAlignment = Alignment.CenterVertically
            ) {
              CircularProgressIndicator(
                modifier = Modifier.size(20.dp),
                color = AccentGold,
                strokeWidth = 2.dp
              )
              Spacer(modifier = Modifier.width(10.dp))
              Text("Testando autenticação e endpoints...", color = Color.White, fontSize = 12.sp)
            }
          }
          is TestStatus.Success -> {
            Row(
              modifier = Modifier
                .fillMaxWidth()
                .padding(vertical = 8.dp)
                .clip(RoundedCornerShape(8.dp))
                .background(Color(0x334CAF50))
                .padding(10.dp),
              verticalAlignment = Alignment.CenterVertically
            ) {
              Icon(Icons.Default.CheckCircle, contentDescription = null, tint = StatusGreen)
              Spacer(modifier = Modifier.width(8.dp))
              Column {
                Text("Conexão bem sucedida!", color = Color.White, fontWeight = FontWeight.Bold, fontSize = 12.sp)
                Text(
                  "Status: ${testStatus.accountInfo.status} • Expira: ${testStatus.accountInfo.expDateFormatted}",
                  color = Color.White.copy(alpha = 0.85f),
                  fontSize = 11.sp
                )
              }
            }
          }
          is TestStatus.Error -> {
            Row(
              modifier = Modifier
                .fillMaxWidth()
                .padding(vertical = 8.dp)
                .clip(RoundedCornerShape(8.dp))
                .background(Color(0x33F44336))
                .padding(10.dp),
              verticalAlignment = Alignment.CenterVertically
            ) {
              Icon(Icons.Default.Error, contentDescription = null, tint = Color(0xFFEF5350))
              Spacer(modifier = Modifier.width(8.dp))
              Text(
                testStatus.message,
                color = Color.White,
                fontSize = 11.sp
              )
            }
          }
          TestStatus.Idle -> {}
        }

        if (validationError != null) {
          Text(
            text = validationError!!,
            color = Color(0xFFEF5350),
            fontSize = 12.sp,
            modifier = Modifier.padding(top = 4.dp)
          )
        }
      }
    },
    confirmButton = {
      Button(
        onClick = {
          if (name.isBlank()) {
            validationError = "Por favor, defina um nome para o servidor."
            return@Button
          }
          if (url.isBlank()) {
            validationError = "Informe o endereço do servidor."
            return@Button
          }
          if (username.isBlank() || password.isBlank()) {
            validationError = "Informe usuário e senha."
            return@Button
          }

          onSave(
            serverToEdit?.id,
            name.trim(),
            url.trim(),
            username.trim(),
            password,
            makeActive
          )
        },
        colors = ButtonDefaults.buttonColors(containerColor = RubyPrimary),
        modifier = Modifier.testTag("save_server_button")
      ) {
        Text("Salvar Conexão", color = Color.White, fontWeight = FontWeight.Bold)
      }
    },
    dismissButton = {
      Row {
        OutlinedButton(
          onClick = {
            if (url.isBlank() || username.isBlank() || password.isBlank()) {
              validationError = "Preencha URL, usuário e senha para testar."
            } else {
              validationError = null
              onTestConnection(url, username, password)
            }
          },
          border = ButtonDefaults.outlinedButtonBorder.copy(
            brush = androidx.compose.ui.graphics.SolidColor(AccentGold)
          ),
          modifier = Modifier.testTag("test_server_button")
        ) {
          Text("Testar", color = AccentGold, fontWeight = FontWeight.SemiBold)
        }

        Spacer(modifier = Modifier.width(8.dp))

        TextButton(onClick = onDismiss) {
          Text("Cancelar", color = Color.White.copy(0.7f))
        }
      }
    }
  )
}

@Composable
private fun getTextFieldColors() = OutlinedTextFieldDefaults.colors(
  focusedTextColor = Color.White,
  unfocusedTextColor = Color.White,
  focusedBorderColor = AccentGold,
  unfocusedBorderColor = Color.White.copy(alpha = 0.3f),
  focusedLabelColor = AccentGold,
  unfocusedLabelColor = Color.White.copy(alpha = 0.6f),
  cursorColor = AccentGold
)
