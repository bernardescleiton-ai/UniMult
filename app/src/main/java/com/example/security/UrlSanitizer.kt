package com.example.security

import java.net.URI

object UrlSanitizer {
  /**
   * Normalizes and validates server Base URL:
   * - Automatically prepends http:// if scheme is missing
   * - Strips trailing slashes and API subpaths (like /player_api.php)
   * - Preserves host and custom ports (e.g., http://myserver.com:8080)
   * - Returns sanitized base URL or null if invalid
   */
  fun sanitizeBaseUrl(input: String): Result<String> {
    val trimmed = input.trim()
    if (trimmed.isBlank()) {
      return Result.failure(IllegalArgumentException("O endereço do servidor não pode estar vazio."))
    }

    val withScheme = if (!trimmed.startsWith("http://", ignoreCase = true) &&
      !trimmed.startsWith("https://", ignoreCase = true)
    ) {
      "http://$trimmed"
    } else {
      trimmed
    }

    return try {
      val uri = URI(withScheme)
      val host = uri.host
      if (host.isNullOrBlank()) {
        return Result.failure(IllegalArgumentException("Endereço/host inválido. Verifique o formato do servidor."))
      }

      val scheme = uri.scheme?.lowercase() ?: "http"
      val port = uri.port
      val portPart = if (port != -1 && port != 80 && port != 443) {
        ":$port"
      } else if (port == 80 && scheme == "https") {
        ":80"
      } else {
        ""
      }

      // Check if user accidentally pasted /player_api.php into the base URL
      var path = uri.path ?: ""
      if (path.endsWith("/player_api.php", ignoreCase = true)) {
        path = path.substringBeforeLast("/player_api.php")
      }
      path = path.trimEnd('/')

      val normalized = "$scheme://$host$portPart$path"
      Result.success(normalized)
    } catch (e: Exception) {
      Result.failure(IllegalArgumentException("Formato de URL inválido: ${e.message}"))
    }
  }

  fun isHttps(url: String): Boolean {
    return url.startsWith("https://", ignoreCase = true)
  }
}
