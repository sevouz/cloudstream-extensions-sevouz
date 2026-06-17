package com.cncverse

import com.lagradost.cloudstream3.base64DecodeArray
import javax.crypto.Cipher
import javax.crypto.spec.IvParameterSpec
import javax.crypto.spec.SecretKeySpec

object CryptoUtils {
    private val CRICIFY_PROVIDER_SECRET1 by lazy { BuildConfig.CRICIFY_PROVIDER_SECRET1 }
    private val CRICIFY_PROVIDER_SECRET2 by lazy { BuildConfig.CRICIFY_PROVIDER_SECRET2 }

    private val KEYS by lazy {
        mapOf(
            "key1" to parseKeyInfo(CRICIFY_PROVIDER_SECRET1),
            "key2" to parseKeyInfo(CRICIFY_PROVIDER_SECRET2)
        )
    }

    private fun parseKeyInfo(secret: String): KeyInfo {
        val parts = secret.split(":")
        return KeyInfo(key = hexStringToByteArray(parts[0]), iv = hexStringToByteArray(parts[1]))
    }

    private data class KeyInfo(val key: ByteArray, val iv: ByteArray)

    private fun hexStringToByteArray(hex: String): ByteArray {
        val len = hex.length
        val data = ByteArray(len / 2)
        for (i in 0 until len step 2) {
            data[i / 2] = ((Character.digit(hex[i], 16) shl 4) + Character.digit(hex[i + 1], 16)).toByte()
        }
        return data
    }

    fun decryptData(encryptedBase64: String): String? {
        return try {
            val cleanBase64 = encryptedBase64.trim()
                .replace("\n", "").replace("\r", "").replace(" ", "").replace("\t", "")
            val ciphertext = base64DecodeArray(cleanBase64)
            for ((_, keyInfo) in KEYS) {
                val result = tryDecrypt(ciphertext, keyInfo)
                if (result != null) return result
            }
            null
        } catch (e: Exception) {
            e.printStackTrace(); null
        }
    }

    private fun tryDecrypt(ciphertext: ByteArray, keyInfo: KeyInfo): String? {
        return try {
            val cipher = Cipher.getInstance("AES/CBC/PKCS5Padding")
            cipher.init(Cipher.DECRYPT_MODE, SecretKeySpec(keyInfo.key, "AES"), IvParameterSpec(keyInfo.iv))
            val text = String(cipher.doFinal(ciphertext), Charsets.UTF_8)
            if (text.startsWith("{") || text.startsWith("[") || text.contains("http", ignoreCase = true)) text else null
        } catch (e: Exception) { null }
    }
}
