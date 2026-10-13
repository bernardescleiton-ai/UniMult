package com.example.security

import java.nio.charset.StandardCharsets
import javax.crypto.Cipher
import javax.crypto.spec.IvParameterSpec
import javax.crypto.spec.SecretKeySpec

object CredentialCipher {
  private val HARDENED_SEED = byteArrayOf(
    0x55.toByte(), 0x6E.toByte(), 0x69.toByte(), 0x4D.toByte(),
    0x75.toByte(), 0x6C.toByte(), 0x74.toByte(), 0x5F.toByte(),
    0x53.toByte(), 0x65.toByte(), 0x63.toByte(), 0x75.toByte(),
    0x72.toByte(), 0x65.toByte(), 0x32.toByte(), 0x36.toByte()
  )
  private val STATIC_IV = byteArrayOf(
    0x12.toByte(), 0x34.toByte(), 0x56.toByte(), 0x78.toByte(),
    0x90.toByte(), 0xAB.toByte(), 0xCD.toByte(), 0xEF.toByte(),
    0x12.toByte(), 0x34.toByte(), 0x56.toByte(), 0x78.toByte(),
    0x90.toByte(), 0xAB.toByte(), 0xCD.toByte(), 0xEF.toByte()
  )

  fun encrypt(plainText: String): String {
    if (plainText.isEmpty()) return ""
    return try {
      val keySpec = SecretKeySpec(HARDENED_SEED, "AES")
      val ivSpec = IvParameterSpec(STATIC_IV)
      val cipher = Cipher.getInstance("AES/CBC/PKCS5Padding")
      cipher.init(Cipher.ENCRYPT_MODE, keySpec, ivSpec)
      val encrypted = cipher.doFinal(plainText.toByteArray(StandardCharsets.UTF_8))
      bytesToHex(encrypted)
    } catch (e: Exception) {
      plainText
    }
  }

  fun decrypt(cipherText: String): String {
    if (cipherText.isEmpty()) return ""
    return try {
      val keySpec = SecretKeySpec(HARDENED_SEED, "AES")
      val ivSpec = IvParameterSpec(STATIC_IV)
      val cipher = Cipher.getInstance("AES/CBC/PKCS5Padding")
      cipher.init(Cipher.DECRYPT_MODE, keySpec, ivSpec)
      val decoded = hexToBytes(cipherText)
      String(cipher.doFinal(decoded), StandardCharsets.UTF_8)
    } catch (e: Exception) {
      cipherText
    }
  }

  private fun bytesToHex(bytes: ByteArray): String {
    val sb = StringBuilder(bytes.size * 2)
    for (b in bytes) {
      val octet = b.toInt() and 0xFF
      val hex = Integer.toHexString(octet)
      if (hex.length == 1) sb.append('0')
      sb.append(hex)
    }
    return sb.toString()
  }

  private fun hexToBytes(hex: String): ByteArray {
    val len = hex.length
    val result = ByteArray(len / 2)
    var i = 0
    while (i < len) {
      result[i / 2] = ((Character.digit(hex[i], 16) shl 4) + Character.digit(hex[i + 1], 16)).toByte()
      i += 2
    }
    return result
  }
}
