package com.e.amaze

import android.content.Context
import android.util.Log
import androidx.security.crypto.EncryptedFile
import androidx.security.crypto.MasterKeys
import java.io.ByteArrayOutputStream
import java.io.File
import java.nio.charset.Charset


class FileCrypt {

    fun encryptFile(context: Context, fName: String, string: String) {
        val mainKey = MasterKeys.getOrCreate(MasterKeys.AES256_GCM_SPEC)

        val file = File(fName)

        Log.d("ENC", "file $fName")

        val encryptedFile = EncryptedFile.Builder(
            file,
            context,
            mainKey,
            EncryptedFile.FileEncryptionScheme.AES256_GCM_HKDF_4KB
        ).build()

        Log.d("ENC", "content $string")

        encryptedFile.openFileOutput().use { outputStream ->
            // Write data to your encrypted file
            //outputStream.write(string.toByteArray())
            //outputStream.write(string.getBytes(Charset.forName("UTF-8")));
            outputStream.write(string.toByteArray(Charset.forName("UTF-8")))
            outputStream.flush()
            outputStream.close()
        }
 /*
        val fileContent = string
            .toByteArray(StandardCharsets.UTF_8)
        encryptedFile.openFileOutput().apply {
            write(fileContent)
            flush()
            close()
        }

  */
    }

    fun decryptFile(context: Context, fName: String): String {
        val mainKey = MasterKeys.getOrCreate(MasterKeys.AES256_GCM_SPEC)

        val file = File(fName)

        Log.d("DEC", "file $fName")

        val encryptedFile = EncryptedFile.Builder(
            file,
            context,
            mainKey,
            EncryptedFile.FileEncryptionScheme.AES256_GCM_HKDF_4KB
        ).build()

//        var string: String = String()
//        encryptedFile.openFileInput().bufferedReader().useLines { lines ->
//            string += lines
//        }

//        var string: String = String()
        encryptedFile.openFileInput().use { inputStream ->
            // Read data from your encrypted file

            val result = ByteArrayOutputStream()
            val buffer = ByteArray(1024)
            var length: Int
            while (inputStream.read(buffer).also { length = it } != -1) {
                result.write(buffer, 0, length)
            }

            Log.d("DEC","dec text: $result")
            return result.toString("UTF-8")
        }

        /*
        val inputStream = encryptedFile.openFileInput()
        val byteArrayOutputStream = ByteArrayOutputStream()
        var nextByte: Int = inputStream.read()
        while (nextByte != -1) {
            byteArrayOutputStream.write(nextByte)
            nextByte = inputStream.read()
        }

        val plaintext: ByteArray = byteArrayOutputStream.toByteArray()

        Log.d("DEC","dec text: $plaintext")

        return plaintext.toString()

         */
  //      Log.d("DEC","dec text: $string")
  //      return string
        return ""
    }
}