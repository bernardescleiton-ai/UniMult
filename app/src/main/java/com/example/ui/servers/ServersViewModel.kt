package com.example.ui.servers

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.data.repository.ContentRepository
import com.example.data.repository.ServerRepository
import com.example.domain.model.AccountInfo
import com.example.domain.model.ConnectionStatus
import com.example.domain.model.ServerProfile
import com.example.security.UrlSanitizer
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch

sealed interface TestStatus {
  object Idle : TestStatus
  object Testing : TestStatus
  data class Success(val accountInfo: AccountInfo) : TestStatus
  data class Error(val message: String) : TestStatus
}

class ServersViewModel(
  private val serverRepository: ServerRepository,
  private val contentRepository: ContentRepository
) : ViewModel() {

  val allServers: StateFlow<List<ServerProfile>> = serverRepository.allServers
    .stateIn(
      scope = viewModelScope,
      started = SharingStarted.WhileSubscribed(5000),
      initialValue = emptyList()
    )

  val activeServer: StateFlow<ServerProfile?> = serverRepository.activeServer
    .stateIn(
      scope = viewModelScope,
      started = SharingStarted.WhileSubscribed(5000),
      initialValue = null
    )

  private val _testStatus = MutableStateFlow<TestStatus>(TestStatus.Idle)
  val testStatus: StateFlow<TestStatus> = _testStatus.asStateFlow()

  private val _isSubmitting = MutableStateFlow(false)
  val isSubmitting: StateFlow<Boolean> = _isSubmitting.asStateFlow()

  private val _userMessage = MutableStateFlow<String?>(null)
  val userMessage: StateFlow<String?> = _userMessage.asStateFlow()

  fun clearUserMessage() {
    _userMessage.value = null
  }

  fun resetTestStatus() {
    _testStatus.value = TestStatus.Idle
  }

  fun testDraftServer(rawUrl: String, username: String, pass: String) {
    viewModelScope.launch {
      val urlRes = UrlSanitizer.sanitizeBaseUrl(rawUrl)
      if (urlRes.isFailure) {
        _testStatus.value = TestStatus.Error(urlRes.exceptionOrNull()?.message ?: "URL inválida")
        return@launch
      }

      val draft = ServerProfile(
        id = "draft_test",
        name = "Teste",
        baseUrl = urlRes.getOrThrow(),
        username = username.trim(),
        password = pass
      )

      _testStatus.value = TestStatus.Testing
      val result = serverRepository.testConnection(draft)
      if (result.isSuccess) {
        _testStatus.value = TestStatus.Success(result.getOrThrow())
      } else {
        _testStatus.value = TestStatus.Error(result.exceptionOrNull()?.message ?: "Falha ao testar conexão")
      }
    }
  }

  fun saveServer(
    id: String? = null,
    name: String,
    rawUrl: String,
    username: String,
    password: String,
    makeActive: Boolean,
    onSuccess: () -> Unit
  ) {
    viewModelScope.launch {
      _isSubmitting.value = true
      val result = serverRepository.saveServer(
        id = id,
        name = name,
        rawUrl = rawUrl,
        username = username,
        password = password,
        makeActive = makeActive
      )
      _isSubmitting.value = false

      if (result.isSuccess) {
        val saved = result.getOrThrow()
        _userMessage.value = "Servidor \"${saved.name}\" salvo com sucesso!"
        if (makeActive) {
          contentRepository.switchServer(saved)
        }
        onSuccess()
      } else {
        _userMessage.value = "Erro ao salvar: ${result.exceptionOrNull()?.message}"
      }
    }
  }

  fun selectActiveServer(server: ServerProfile) {
    viewModelScope.launch {
      serverRepository.setActiveServer(server.id)
      contentRepository.switchServer(server)
      _userMessage.value = "Servidor ativo alterado para: ${server.name}"
    }
  }

  fun deleteServer(serverId: String, serverName: String) {
    viewModelScope.launch {
      contentRepository.clearCacheForServer(serverId)
      serverRepository.deleteServer(serverId)
      _userMessage.value = "Servidor \"$serverName\" excluído."
    }
  }

  fun testExistingServer(server: ServerProfile) {
    viewModelScope.launch {
      _userMessage.value = "Testando conexão com ${server.name}..."
      val result = serverRepository.testConnection(server)
      if (result.isSuccess) {
        _userMessage.value = "Conexão com ${server.name} OK! Válido até: ${result.getOrThrow().expDateFormatted}"
      } else {
        _userMessage.value = "Erro ao conectar em ${server.name}: ${result.exceptionOrNull()?.message}"
      }
    }
  }
}
