package com.example.domain.model

data class ServerProfile(
  val id: String,
  val name: String,
  val baseUrl: String,
  val username: String,
  val password: String, // Plaintext when in domain memory, encrypted in storage
  val isActive: Boolean = false,
  val lastTestedTime: Long = 0L,
  val connectionStatus: ConnectionStatus = ConnectionStatus.IDLE,
  val statusMessage: String? = null,
  val expireDateFormatted: String? = null,
  val maxConnections: String? = null,
  val activeConnections: String? = null,
  val createdAt: Long = System.currentTimeMillis()
)

enum class ConnectionStatus {
  IDLE,
  CONNECTING,
  ONLINE,
  OFFLINE,
  EXPIRED,
  ERROR
}
