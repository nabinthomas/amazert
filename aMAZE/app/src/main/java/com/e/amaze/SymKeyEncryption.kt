package com.e.amaze

import android.util.Log
import java.security.MessageDigest
import java.security.SecureRandom
import java.util.*
import javax.crypto.Cipher
import javax.crypto.SecretKeyFactory
import javax.crypto.spec.IvParameterSpec
import javax.crypto.spec.PBEKeySpec
import javax.crypto.spec.SecretKeySpec

class SymKeyEncryption {
    // https://developer.android.com/guide/topics/security/cryptography
    // https://www.raywenderlich.com/778533-encryption-tutorial-for-android-getting-started
    // https://medium.com/@lucideus/secure-derivation-of-keys-in-android-pbkdf2-lucideus-371452cc29f7

    private val TAG = "SymmetricKeyEncrypter: "
    private val KeyLength = 256
    private val IterationCount = 100000
    private val saltStr:String = "salt_"
    private val registrationId:String =  "6650ed14-1a7e-11eb-92da-dca6328f80c0"
    private lateinit var SymmetricKey:SecretKeySpec
    private lateinit var EncCipher:Cipher
    private lateinit var DecryptCipher:Cipher
    private val digest = MessageDigest.getInstance("SHA-256")

    // Initialize Symmetric Key
    init {
        val password = registrationId.toCharArray()
        val salt = saltStr.toByteArray()

        //Generate PBKDF2 Key
        val pbKeySpec = PBEKeySpec(password, salt, IterationCount, KeyLength) // 1
        val secretKeyFactory = SecretKeyFactory.getInstance(("PBKDF2WithHmacSHA256")) // 2
        val keyBytes = secretKeyFactory.generateSecret(pbKeySpec).encoded // 3
        SymmetricKey = SecretKeySpec(keyBytes, "AES") // 4
        Log.d(TAG, "Key: ${SymmetricKey.toString()}")

    }

    private fun generateIV():IvParameterSpec{
        // Generate IV
        val ivRandom = SecureRandom() //not caching previous seeded instance of SecureRandom
        val iv = ByteArray(16)
        ivRandom.nextBytes(iv)
        val ivSpec = IvParameterSpec(iv) // 2
        return ivSpec
    }

    fun encryptString(plaintext: String) : Triple<ByteArray, IvParameterSpec, ByteArray>{
        // Generate IV
        val ivSpec = generateIV()

        EncCipher = Cipher.getInstance("AES/GCM/NoPadding")
        EncCipher.init(Cipher.ENCRYPT_MODE, SymmetricKey, ivSpec)

        val dataToEncrypt = plaintext.toByteArray()
        val cipherText = EncCipher.doFinal(dataToEncrypt)

        Log.d(TAG, "Generated CipherText: $cipherText")
        return Triple (cipherText, ivSpec, getMessageDigest(plaintext))
    }

    fun decryptCipherText(cipherText: ByteArray, ivSpec:IvParameterSpec) : String{
        DecryptCipher = Cipher.getInstance("AES/GCM/NoPadding")
        DecryptCipher.init(Cipher.DECRYPT_MODE, SymmetricKey, ivSpec)

        val plainTextByteArray:ByteArray = DecryptCipher.doFinal(cipherText)

        Log.d(TAG, "DECRYPTED String: --" + plainTextByteArray.toString(Charsets.UTF_8) + "--  String: " + plainTextByteArray.contentToString())
        return plainTextByteArray.toString(Charsets.UTF_8)
    }

    private fun getMessageDigest(plaintext: String):ByteArray {
        return digest.digest(plaintext.toByteArray(Charsets.UTF_8))
    }

    fun segregateSettingData(valueString: String): Triple<IvParameterSpec, ByteArray, ByteArray>{
        val ivBase64: ByteArray = Arrays.copyOfRange(valueString.toByteArray(),0, 24)
        val iv = Base64.getDecoder().decode(ivBase64)
        val ivSpec = IvParameterSpec(iv)

        val digestBase64: ByteArray = Arrays.copyOfRange(valueString.toByteArray(),24,48)
        val digest = Base64.getDecoder().decode(digestBase64)

        val cipherTextBase64: ByteArray = Arrays.copyOfRange(valueString.toByteArray(),48,valueString.length)
        val cipherText = Base64.getDecoder().decode(cipherTextBase64)

        return Triple(ivSpec, digest, cipherText)
    }
}