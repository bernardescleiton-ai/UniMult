package com.example

import com.example.security.CredentialCipher
import com.example.security.UrlSanitizer
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertNotEquals
import org.junit.Assert.assertTrue
import org.junit.Test

class UniMultUnitTest {

  @Test
  fun testUrlSanitizer_addsHttpSchemeWhenMissing() {
    val result = UrlSanitizer.sanitizeBaseUrl("servidor.exemplo.com:8080")
    assertTrue(result.isSuccess)
    assertEquals("http://servidor.exemplo.com:8080", result.getOrThrow())
  }

  @Test
  fun testUrlSanitizer_trimsTrailingSlashAndPlayerApiPath() {
    val result = UrlSanitizer.sanitizeBaseUrl("https://iptv.servidor.com:8080/player_api.php")
    assertTrue(result.isSuccess)
    assertEquals("https://iptv.servidor.com:8080", result.getOrThrow())

    val withSlash = UrlSanitizer.sanitizeBaseUrl("http://iptv.servidor.com:2095/")
    assertTrue(withSlash.isSuccess)
    assertEquals("http://iptv.servidor.com:2095", withSlash.getOrThrow())
  }

  @Test
  fun testUrlSanitizer_detectsHttps() {
    assertTrue(UrlSanitizer.isHttps("https://secure.server.com"))
    assertFalse(UrlSanitizer.isHttps("http://insecure.server.com:8080"))
  }

  @Test
  fun testUrlSanitizer_rejectsEmptyOrBlankInput() {
    val emptyResult = UrlSanitizer.sanitizeBaseUrl("   ")
    assertTrue(emptyResult.isFailure)
  }

  @Test
  fun testCredentialCipher_encryptAndDecryptSymmetry() {
    val originalPassword = "SecretPassword!123"
    val encrypted = CredentialCipher.encrypt(originalPassword)

    assertNotEquals(originalPassword, encrypted)
    val decrypted = CredentialCipher.decrypt(encrypted)
    assertEquals(originalPassword, decrypted)
  }

  @Test
  fun testCredentialCipher_handlesEmptyString() {
    val encrypted = CredentialCipher.encrypt("")
    assertEquals("", encrypted)
    val decrypted = CredentialCipher.decrypt("")
    assertEquals("", decrypted)
  }
}
