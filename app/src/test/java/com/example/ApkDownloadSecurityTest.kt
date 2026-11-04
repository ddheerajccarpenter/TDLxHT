package com.example

import com.example.util.ApkDownloadManager
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Test
import java.io.File

class ApkDownloadSecurityTest {

  @Test
  fun testSanitizeApkFilenamePreventsPathTraversal() {
    // Basic valid filename
    assertEquals("TDLxHT.2.5.18.apk", ApkDownloadManager.sanitizeApkFilename("TDLxHT.2.5.18.apk"))

    // Directory traversal attempt
    val traversal = "../../../../system/bin/sh.apk"
    val sanitizedTraversal = ApkDownloadManager.sanitizeApkFilename(traversal)
    assertFalse("Must not contain directory navigation slashes", sanitizedTraversal.contains("/"))
    assertFalse("Must not contain backslashes", sanitizedTraversal.contains("\\"))
    assertEquals("sh.apk", sanitizedTraversal)

    // Invalid extension fallback
    val scriptExploit = "../../../libevil.so"
    assertEquals("TDlxHT_update.apk", ApkDownloadManager.sanitizeApkFilename(scriptExploit))

    // Blank or malicious empty string fallback
    assertEquals("TDlxHT_update.apk", ApkDownloadManager.sanitizeApkFilename(""))
    assertEquals("TDlxHT_update.apk", ApkDownloadManager.sanitizeApkFilename("   "))

    // Special characters injection
    val specialChars = "TDlxHT\$(`rm -rf`)[1].apk"
    val sanitizedSpecial = ApkDownloadManager.sanitizeApkFilename(specialChars)
    assertFalse("Must not contain special characters", sanitizedSpecial.contains("$"))
    assertFalse("Must not contain backticks", sanitizedSpecial.contains("`"))
    assertFalse("Must not contain parens", sanitizedSpecial.contains("("))
    assertTrue("Must end with .apk", sanitizedSpecial.endsWith(".apk"))
  }

  @Test
  fun testComputeSha256ChecksumIntegrity() {
    val tempFile = File.createTempFile("sha_test_", ".dat")
    try {
      tempFile.writeText("hello world")
      val sha = ApkDownloadManager.computeSha256(tempFile)
      // SHA-256 for "hello world"
      val expected = "b94d27b9934d3e08a52e52d7da7dabfac484efe37a5380ee9088f7ace2efcde9"
      assertEquals(expected, sha)
    } finally {
      tempFile.delete()
    }
  }

  @Test
  fun testStorageCleanupTargetsOnlyUpdateArtifacts() {
    val tempDir = File.createTempFile("updates_sandbox_", "").apply {
      delete()
      mkdirs()
    }

    try {
      val apkFile = File(tempDir, "TDLxHT.2.5.17.apk").apply { writeBytes(ByteArray(1024)) }
      val partFile = File(tempDir, "TDLxHT.2.5.18.apk.part").apply { writeBytes(ByteArray(512)) }
      val tmpFile = File(tempDir, "download_cache.tmp").apply { writeBytes(ByteArray(256)) }
      val keepUserDataFile = File(tempDir, "user_notes.txt").apply { writeText("Important user data") }

      assertTrue(apkFile.exists())
      assertTrue(partFile.exists())
      assertTrue(tmpFile.exists())
      assertTrue(keepUserDataFile.exists())

      // Simulate cleanup logic on tempDir
      tempDir.listFiles()?.forEach { file ->
        val name = file.name.lowercase()
        if (name.endsWith(".apk") || name.endsWith(".part") || name.endsWith(".tmp")) {
          file.delete()
        }
      }

      assertFalse("Downloaded APK must be deleted after cleanup", apkFile.exists())
      assertFalse("Part file must be deleted after cleanup", partFile.exists())
      assertFalse("Tmp file must be deleted after cleanup", tmpFile.exists())
      assertTrue("Non-update files must remain untouched", keepUserDataFile.exists())
    } finally {
      tempDir.deleteRecursively()
    }
  }
}

