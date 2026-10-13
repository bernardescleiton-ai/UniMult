package com.example.data.local.entity

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "servers")
data class ServerProfileEntity(
  @PrimaryKey val id: String, // Unique UUID
  val name: String,
  val baseUrl: String,
  val username: String,
  val passwordHashOrEncrypted: String,
  val isActive: Boolean = false,
  val lastTestedTime: Long = 0L,
  val connectionStatus: String = "IDLE", // IDLE, CONNECTED, ERROR, EXPIRED
  val statusMessage: String? = null,
  val expireDateFormatted: String? = null,
  val maxConnections: String? = null,
  val activeConnections: String? = null,
  val createdAt: Long = System.currentTimeMillis()
)
