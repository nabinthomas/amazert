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
    private val TAG = "SymKeyEncryption"
    private val KeyLength = 256
    private val IterationCount = 100000
    private val saltStr:String = "salt_"
    private var registrationId:String =  MyApplication.Companion.register.registrationId
    private lateinit var SymmetricKey:SecretKeySpec
    private lateinit var EncCipher:Cipher
    private lateinit var DecryptCipher:Cipher
    private val digest = MessageDigest.getInstance("SHA-256")

    // Initialize Symmetric Key
    init {
        if (MyApplication.Companion.register.registrationId === "") {
            registrationId = "7544723b-ebaf-40dd-bb91-c0589a231a17"
        }

        val password = registrationId.toCharArray()
        val salt = saltStr.toByteArray()

        //Generate PBKDF2 Key
        val pbKeySpec = PBEKeySpec(password, salt, IterationCount, KeyLength)
        val secretKeyFactory = SecretKeyFactory.getInstance(("PBKDF2WithHmacSHA256"))
        val keyBytes = secretKeyFactory.generateSecret(pbKeySpec).encoded
        SymmetricKey = SecretKeySpec(keyBytes, "AES")
        Log.d(TAG, "Key: ${SymmetricKey.toString()}")

    }

    private fun generateIV():IvParameterSpec{
        // Generate IV
        val ivRandom = SecureRandom() //not caching previous seeded instance of SecureRandom
        val iv = ByteArray(16)
        ivRandom.nextBytes(iv)
        val ivSpec = IvParameterSpec(iv)
        return ivSpec
    }

    fun encryptString(plaintext: String) : String{
        // Generate IV
        val ivSpec = generateIV()

        EncCipher = Cipher.getInstance("AES/GCM/NoPadding")
        EncCipher.init(Cipher.ENCRYPT_MODE, SymmetricKey, ivSpec)

        val dataToEncrypt = plaintext.toByteArray()
        val cipherText = EncCipher.doFinal(dataToEncrypt)

        val digestArray: ByteArray = Arrays.copyOfRange(cipherText,cipherText.size - 16, cipherText.size)
        val cipherTextArray: ByteArray = Arrays.copyOfRange(cipherText, 0, cipherText.size - 16)
        val outDigestString = String(Base64.getEncoder().encode(digestArray))
        val outCipherTextString = String(Base64.getEncoder().encode(cipherTextArray))
        val outIvSpecString = String(Base64.getEncoder().encode(ivSpec.iv))

        //Log.d(TAG, "DIGEST: " + outDigestString)
        //Log.d(TAG, "CIPHER: " + outCipherTextString)
        //Log.d(TAG, "IV: " + outIvSpecString)
        //Log.d(TAG, "OUTPUT::: " + outIvSpecString + outDigestString + outCipherTextString)

        return outIvSpecString + outDigestString + outCipherTextString
    }

    fun decryptCipherText(cipherText: ByteArray, ivSpec:IvParameterSpec) : String{
        DecryptCipher = Cipher.getInstance("AES/GCM/NoPadding")
        DecryptCipher.init(Cipher.DECRYPT_MODE, SymmetricKey, ivSpec)

        var plainTextByteArray: ByteArray = byteArrayOf()
        try {
            plainTextByteArray = DecryptCipher.doFinal(cipherText)
        } catch (e: Exception){
            Log.d(TAG, e.toString() )
            e.printStackTrace()
        }

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