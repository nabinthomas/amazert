package com.e.amaze

import android.util.Log
import android.view.View
import java.security.SecureRandom
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
    private val IterationCount = 10000
    private val saltStr:String = "salt_"
    private val registrationId:String =  "7544723b-ebaf-40dd-bb91-c0589a231a17"
    private lateinit var SymmetricKey:SecretKeySpec
    private lateinit var IvSpec:IvParameterSpec
    private lateinit var EncCipher:Cipher
    private lateinit var DecryptCipher:Cipher

    // Initialize Symmetric Key
    init {
        val password = registrationId.toCharArray()
        val salt = saltStr.toByteArray()

        //Generate PBKDF2 Key
        val pbKeySpec = PBEKeySpec(password, salt, IterationCount, KeyLength) // 1
        val secretKeyFactory = SecretKeyFactory.getInstance(("PBKDF2WithHmacSHA256")) // 2
        val keyBytes = secretKeyFactory.generateSecret(pbKeySpec).encoded // 3
        SymmetricKey = SecretKeySpec(keyBytes, "AES") // 4
        Log.d(TAG, "Key: $SymmetricKey")

        // Generate IV
        val ivRandom = SecureRandom() //not caching previous seeded instance of SecureRandom
        val iv = ByteArray(16)
        ivRandom.nextBytes(iv)
        val IvSpec = IvParameterSpec(iv) // 2

        EncCipher = Cipher.getInstance("AES/GCM/NoPadding")
        EncCipher.init(Cipher.ENCRYPT_MODE, SymmetricKey, IvSpec)

        DecryptCipher = Cipher.getInstance("AES/GCM/NoPadding")
        DecryptCipher.init(Cipher.DECRYPT_MODE, SymmetricKey, IvSpec)
    }

    fun encryptString(plaintext: String) : ByteArray{
        val dataToEncrypt = plaintext.toByteArray()
        val cipherText = EncCipher.doFinal(dataToEncrypt)

        Log.d(TAG, "Generated CipherText: $cipherText")
        return cipherText
    }

    fun decryptCipherText(cipherText: ByteArray) : String{
        val plainTextByteArray:ByteArray = DecryptCipher.doFinal(cipherText)

        Log.d(TAG, "DECRYPTED String: --" + plainTextByteArray.toString(Charsets.UTF_8) + "--  String: " + plainTextByteArray.contentToString())
        return plainTextByteArray.toString(Charsets.UTF_8)
    }

}