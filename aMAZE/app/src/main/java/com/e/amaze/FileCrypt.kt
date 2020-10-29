package com.e.amaze

import android.content.Context
import androidx.security.crypto.EncryptedFile
import androidx.security.crypto.MasterKeys
import java.io.ByteArrayOutputStream
import java.io.File
import java.nio.charset.StandardCharsets

class FileCrypt {

    fun encryptFile(context: Context, fName: String, string: String) {
        val mainKey = MasterKeys.getOrCreate(MasterKeys.AES256_GCM_SPEC)

        val file = File(fName)

        val encryptedFile = EncryptedFile.Builder(
            file,
            context,
            mainKey,
            EncryptedFile.FileEncryptionScheme.AES256_GCM_HKDF_4KB
        ).build()

//        encryptedFile.openFileOutput().use { outputStream ->
            //write data to your encrypted file
//        }

        val fileContent = string
            .toByteArray(StandardCharsets.UTF_8)
        encryptedFile.openFileOutput().apply {
            write(fileContent)
            flush()
            close()
        }
    }

    fun decryptFile(context: Context, fName: String): String {
        val mainKey = MasterKeys.getOrCreate(MasterKeys.AES256_GCM_SPEC)

        val file = File(fName)

        val encryptedFile = EncryptedFile.Builder(
            file,
            context,
            mainKey,
            EncryptedFile.FileEncryptionScheme.AES256_GCM_HKDF_4KB
        ).build()

        val inputStream = encryptedFile.openFileInput()
        val byteArrayOutputStream = ByteArrayOutputStream()
        var nextByte: Int = inputStream.read()
        while (nextByte != -1) {
            byteArrayOutputStream.write(nextByte)
            nextByte = inputStream.read()
        }

        val plaintext: ByteArray = byteArrayOutputStream.toByteArray()

        return plaintext.toString()
    }
}