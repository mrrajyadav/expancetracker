package com.baburamtechnologies.expensetracker.core.security

import android.security.keystore.KeyGenParameterSpec
import android.security.keystore.KeyProperties
import android.util.Base64
import java.security.KeyStore
import java.security.MessageDigest
import java.security.SecureRandom
import java.security.spec.KeySpec
import javax.crypto.Cipher
import javax.crypto.KeyGenerator
import javax.crypto.Mac
import javax.crypto.SecretKeyFactory
import javax.crypto.spec.GCMParameterSpec
import javax.crypto.spec.PBEKeySpec
import javax.crypto.spec.SecretKeySpec
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class CryptoManager @Inject constructor() {

    companion object {
        val SQLITE_HEADER = byteArrayOf(
            0x53, 0x51, 0x4c, 0x69, 0x74, 0x65, 0x20, 0x66,
            0x6f, 0x72, 0x6d, 0x61, 0x74, 0x20, 0x33, 0x00
        )
    }

    private val keystoreAlias = "backup_key_wrapper"
    private val algorithm = "AES/GCM/NoPadding"
    private val keyAlgorithm = "AES"
    private val hmacAlgorithm = "HmacSHA256"
    private val pbkdf2Algorithm = "PBKDF2WithHmacSHA256"
    private val iterations = 310000
    private val keyLength = 256
    private val ivSize = 12
    private val tagLength = 128

    init {
        initKeystore()
    }

    private fun initKeystore() {
        val keyStore = KeyStore.getInstance("AndroidKeyStore")
        keyStore.load(null)
        if (!keyStore.containsAlias(keystoreAlias)) {
            val keyGenerator = KeyGenerator.getInstance(KeyProperties.KEY_ALGORITHM_AES, "AndroidKeyStore")
            val spec = KeyGenParameterSpec.Builder(
                keystoreAlias,
                KeyProperties.PURPOSE_ENCRYPT or KeyProperties.PURPOSE_DECRYPT
            )
                .setBlockModes(KeyProperties.BLOCK_MODE_GCM)
                .setEncryptionPaddings(KeyProperties.ENCRYPTION_PADDING_NONE)
                .setKeySize(256)
                .build()
            keyGenerator.init(spec)
            keyGenerator.generateKey()
        }
    }

    fun generateSalt(size: Int = 16): ByteArray {
        val salt = ByteArray(size)
        SecureRandom().nextBytes(salt)
        return salt
    }

    fun generateIv(): ByteArray {
        val iv = ByteArray(ivSize)
        SecureRandom().nextBytes(iv)
        return iv
    }

    fun generateRandomKey(): ByteArray {
        val key = ByteArray(32) // 256 bits
        SecureRandom().nextBytes(key)
        return key
    }

    fun deriveMasterKey(firebaseUid: String, salt: ByteArray, iterations: Int = 310000): SecretKeySpec {
        val internalSecret = "ET_BACKUP_INTERNAL_SECRET"
        val combinedSecret = internalSecret + firebaseUid
        val factory = SecretKeyFactory.getInstance(pbkdf2Algorithm)
        val spec: KeySpec = PBEKeySpec(combinedSecret.toCharArray(), salt, iterations, keyLength)
        val tmp = factory.generateSecret(spec).encoded
        return SecretKeySpec(tmp, keyAlgorithm)
    }

    fun encrypt(data: ByteArray, key: ByteArray, iv: ByteArray): ByteArray {
        val secretKey = SecretKeySpec(key, keyAlgorithm)
        val cipher = Cipher.getInstance(algorithm)
        val spec = GCMParameterSpec(tagLength, iv)
        cipher.init(Cipher.ENCRYPT_MODE, secretKey, spec)
        return cipher.doFinal(data)
    }

    fun decrypt(encryptedData: ByteArray, key: ByteArray, iv: ByteArray): ByteArray {
        val secretKey = SecretKeySpec(key, keyAlgorithm)
        val cipher = Cipher.getInstance(algorithm)
        val spec = GCMParameterSpec(tagLength, iv)
        cipher.init(Cipher.DECRYPT_MODE, secretKey, spec)
        return cipher.doFinal(encryptedData)
    }

    // Key Wrapping with Keystore for background tasks
    fun wrapKey(keyToWrap: ByteArray): ByteArray {
        val keyStore = KeyStore.getInstance("AndroidKeyStore")
        keyStore.load(null)
        val entry = keyStore.getEntry(keystoreAlias, null) as KeyStore.SecretKeyEntry
        val wrappingKey = entry.secretKey

        val cipher = Cipher.getInstance(algorithm)
        cipher.init(Cipher.ENCRYPT_MODE, wrappingKey)
        val encrypted = cipher.doFinal(keyToWrap)
        
        return cipher.iv + encrypted
    }

    fun unwrapKey(wrappedKey: ByteArray): ByteArray {
        val keyStore = KeyStore.getInstance("AndroidKeyStore")
        keyStore.load(null)
        val entry = keyStore.getEntry(keystoreAlias, null) as KeyStore.SecretKeyEntry
        val wrappingKey = entry.secretKey

        val iv = wrappedKey.copyOfRange(0, ivSize)
        val encrypted = wrappedKey.copyOfRange(ivSize, wrappedKey.size)

        val cipher = Cipher.getInstance(algorithm)
        val spec = GCMParameterSpec(tagLength, iv)
        cipher.init(Cipher.DECRYPT_MODE, wrappingKey, spec)
        return cipher.doFinal(encrypted)
    }

    fun generateMetadataMac(metadataJson: String, key: ByteArray): String {
        val secretKey = SecretKeySpec(key, hmacAlgorithm)
        val mac = Mac.getInstance(hmacAlgorithm)
        mac.init(secretKey)
        val macBytes = mac.doFinal(metadataJson.toByteArray(Charsets.UTF_8))
        return Base64.encodeToString(macBytes, Base64.NO_WRAP)
    }

    fun calculateChecksum(data: ByteArray): String {
        val digest = MessageDigest.getInstance("SHA-256")
        val hash = digest.digest(data)
        return Base64.encodeToString(hash, Base64.NO_WRAP)
    }
}
