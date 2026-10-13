package com.example.data.repository

import com.example.data.local.dao.ServerProfileDao
import com.example.data.local.entity.ServerProfileEntity
import com.example.data.remote.XtreamCodesClient
import com.example.domain.model.AccountInfo
import com.example.domain.model.ConnectionStatus
import com.example.domain.model.ServerProfile
import com.example.security.CredentialCipher
import com.example.security.UrlSanitizer
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map
import java.util.UUID

class ServerRepository(
  private val serverProfileDao: ServerProfileDao,
  private val xtreamCodesClient: XtreamCodesClient
) {

  val allServers: Flow<List<ServerProfile>> = serverProfileDao.getAllServers().map { list ->
    list.map { it.toDomain() }
  }

  val activeServer: Flow<ServerProfile?> = serverProfileDao.getActiveServer().map { entity ->
    entity?.toDomain()
  }

  suspend fun getActiveServerSync(): ServerProfile? {
    return serverProfileDao.getActiveServerSync()?.toDomain()
  }

  suspend fun saveServer(
    id: String? = null,
    name: String,
    rawUrl: String,
    username: String,
    password: String,
    makeActive: Boolean = false
  ): Result<ServerProfile> {
    val sanitizedUrlResult = UrlSanitizer.sanitizeBaseUrl(rawUrl)
    if (sanitizedUrlResult.isFailure) {
      return Result.failure(sanitizedUrlResult.exceptionOrNull()!!)
    }
    val baseUrl = sanitizedUrlResult.getOrThrow()

    if (username.isBlank()) {
      return Result.failure(IllegalArgumentException("Nome de usuário não pode estar em branco."))
    }
    if (password.isBlank()) {
      return Result.failure(IllegalArgumentException("Senha não pode estar em branco."))
    }

    val serverId = id ?: UUID.randomUUID().toString()
    val encryptedPass = CredentialCipher.encrypt(password)

    val existing = if (id != null) serverProfileDao.getServerById(id) else null
    val entity = ServerProfileEntity(
      id = serverId,
      name = name.ifBlank { "Servidor $serverId" },
      baseUrl = baseUrl,
      username = username.trim(),
      passwordHashOrEncrypted = encryptedPass,
      isActive = if (makeActive) true else (existing?.isActive ?: false),
      lastTestedTime = existing?.lastTestedTime ?: 0L,
      connectionStatus = existing?.connectionStatus ?: ConnectionStatus.IDLE.name,
      statusMessage = existing?.statusMessage,
      expireDateFormatted = existing?.expireDateFormatted,
      maxConnections = existing?.maxConnections,
      activeConnections = existing?.activeConnections,
      createdAt = existing?.createdAt ?: System.currentTimeMillis()
    )

    serverProfileDao.insertServer(entity)
    if (makeActive) {
      serverProfileDao.setActiveServer(serverId)
    }

    return Result.success(entity.toDomain())
  }

  suspend fun testConnection(server: ServerProfile): Result<AccountInfo> {
    val result = xtreamCodesClient.authenticate(server)
    val now = System.currentTimeMillis()

    val currentEntity = serverProfileDao.getServerById(server.id)
    if (currentEntity != null) {
      if (result.isSuccess) {
        val account = result.getOrThrow()
        val updated = currentEntity.copy(
          lastTestedTime = now,
          connectionStatus = ConnectionStatus.ONLINE.name,
          statusMessage = "Conexão estabelecida com sucesso",
          expireDateFormatted = account.expDateFormatted,
          maxConnections = account.maxConnections,
          activeConnections = account.activeConnections
        )
        serverProfileDao.updateServer(updated)
      } else {
        val errorMsg = result.exceptionOrNull()?.message ?: "Falha na conexão"
        val updated = currentEntity.copy(
          lastTestedTime = now,
          connectionStatus = ConnectionStatus.ERROR.name,
          statusMessage = errorMsg
        )
        serverProfileDao.updateServer(updated)
      }
    }
    return result
  }

  suspend fun setActiveServer(serverId: String) {
    serverProfileDao.setActiveServer(serverId)
  }

  suspend fun deleteServer(serverId: String) {
    serverProfileDao.deleteServerById(serverId)
  }

  private fun ServerProfileEntity.toDomain(): ServerProfile {
    val decryptedPass = CredentialCipher.decrypt(passwordHashOrEncrypted)
    val status = try {
      ConnectionStatus.valueOf(connectionStatus)
    } catch (e: Exception) {
      ConnectionStatus.IDLE
    }

    return ServerProfile(
      id = id,
      name = name,
      baseUrl = baseUrl,
      username = username,
      password = decryptedPass,
      isActive = isActive,
      lastTestedTime = lastTestedTime,
      connectionStatus = status,
      statusMessage = statusMessage,
      expireDateFormatted = expireDateFormatted,
      maxConnections = maxConnections,
      activeConnections = activeConnections,
      createdAt = createdAt
    )
  }
}
