package com.e.amaze

import android.content.Intent
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.util.Log
import android.view.View
import android.widget.Toast
import com.firebase.ui.auth.AuthUI
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.database.ktx.database
import com.google.firebase.ktx.Firebase
import java.security.SecureRandom
import javax.crypto.Cipher
import javax.crypto.KeyGenerator
import javax.crypto.SecretKeyFactory
import javax.crypto.spec.IvParameterSpec
import javax.crypto.spec.PBEKeySpec
import javax.crypto.spec.SecretKeySpec

class DevicesActivity : AppCompatActivity() {

    private val TAG = "DEVICES_ACTIVITY"
    private val database = Firebase.database.getReferenceFromUrl("https://amaze-id1.firebaseio.com/").database
    private lateinit var deviceViewModel: DeviceViewModel

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_devices)
    }

    fun launchAddDevice(view: View) {
        val intent = Intent(this, AddDevice::class.java)
        var uid = FirebaseAuth.getInstance().currentUser?.uid
        var user = FirebaseAuth.getInstance().currentUser?.email
        intent.putExtra("Name", user)
        intent.putExtra("Uid", uid)
        startActivity(intent)
    }

    fun registerAppDb(view: View) {
        var userId = FirebaseAuth.getInstance().currentUser?.uid
        val deviceId = "device-".plus("$userId") // TODO : Pass DeviceId as argument to this call.
        val refPrefix = userId.plus("/device/$deviceId")

        val myRef = database.getReference(refPrefix)
        myRef.setValue("$deviceId")

        // View the connected DB URL
        Log.d("FBase","In registerAppDb "+database.reference.repo)
        Toast.makeText(baseContext, "Connected to DB: "+database.reference.repo.toString(), Toast.LENGTH_LONG).show()
    }

    fun launchDevicesActivity(view: View, user: String) {
        val intent = Intent(this, DevicesActivity::class.java)
        intent.putExtra("Name", user)
        startActivity(intent)
    }

    fun logoutCurrentUser(view: View) {
        val userName = FirebaseAuth.getInstance().currentUser
        Log.i("FBase", "LOGGING OUT User: $userName")
        AuthUI.getInstance().signOut(this)
        onBackPressed()
    }

    fun launchFirebaseIo(view: View) {
        val intent = Intent(this, dbio::class.java)
        startActivity(intent)
    }

    private fun registerDevice(deviceId: String) {
        var userId = FirebaseAuth.getInstance().currentUser?.uid
        var deviceId = "532e8c40-18cd-11eb-a4ca-dca6328f80c0" // Unique_DeviceUID

        val refPrefix = "/users/$userId/$deviceId/identifier/email"
        val devRef = database.getReference(refPrefix)
        devRef.setValue(FirebaseAuth.getInstance().currentUser?.email)
    }

    fun launchDeviceScreen(view: View) {
        val intent = Intent(this, DeviceSettingActivity::class.java)
        startActivity(intent)
    }

    fun launchDeviceSettingActivity(view: View) {
        val intent = Intent(this, DeviceSettingActivity::class.java)
        startActivity(intent)
    }

    fun encryptString(plaintext: String) : ByteArray{
        // https://developer.android.com/guide/topics/security/cryptography
        // https://www.raywenderlich.com/778533-encryption-tutorial-for-android-getting-started
        // https://medium.com/@lucideus/secure-derivation-of-keys-in-android-pbkdf2-lucideus-371452cc29f7
        val saltStr:String = "salt_"
        val registrationId:String =  "7544723b-ebaf-40dd-bb91-c0589a231a17"

        val password = registrationId.toCharArray()
        val salt = saltStr.toByteArray()
        val keyLength = 256
        val iterationCount = 10000

        //Generate PBKDF2 Key
        val pbKeySpec = PBEKeySpec(password, salt, 10000, 256) // 1
        val secretKeyFactory = SecretKeyFactory.getInstance(("PBKDF2WithHmacSHA256")) // 2
        val keyBytes = secretKeyFactory.generateSecret(pbKeySpec).encoded // 3
        val keySpec = SecretKeySpec(keyBytes, "AES") // 4
        Log.d(TAG, "Key: $keySpec")

        // Generate IV
        val ivRandom = SecureRandom() //not caching previous seeded instance of SecureRandom
        val iv = ByteArray(16)
        ivRandom.nextBytes(iv)
        val ivSpec = IvParameterSpec(iv) // 2

        val cipher = Cipher.getInstance("AES/GCM/NoPadding")
        cipher.init(Cipher.ENCRYPT_MODE, keySpec, ivSpec)

        val dataToEncrypt = plaintext.toByteArray()
        val cipherText = cipher.doFinal(dataToEncrypt) // 2
        Log.d(TAG, "Generated CipherText: $cipherText")

        return cipherText
    }

    fun testEncrypt(view: View) {
        val symEnc:SymKeyEncryption = SymKeyEncryption()
        val cipherText = symEnc.encryptString("Hello World")
        val decText = symEnc.decryptCipherText(cipherText)

        Log.d(TAG, "Decrypted text: --" + decText.toString())
        /*
        // https://developer.android.com/guide/topics/security/cryptography
        // https://www.raywenderlich.com/778533-encryption-tutorial-for-android-getting-started
        // https://medium.com/@lucideus/secure-derivation-of-keys-in-android-pbkdf2-lucideus-371452cc29f7
        val saltStr:String = "salt_"
        val registrationId:String =  "7544723b-ebaf-40dd-bb91-c0589a231a17"
        //val clearTextStr = "Message for AES-256-GCM + Scrypt encryption"
        val clearTextStr = "AESGCM @Test Message"

        val password = registrationId.toCharArray()
        val salt = saltStr.toByteArray()
        val keyLength = 256
        val iterationCount = 10000

        //Generate PBKDF2 Key
        val pbKeySpec = PBEKeySpec(password, salt, 10000, 256) // 1

        val secretKeyFactory = SecretKeyFactory.getInstance(("PBKDF2WithHmacSHA256")) // 2
        val keyBytes = secretKeyFactory.generateSecret(pbKeySpec).encoded // 3
        val keySpec = SecretKeySpec(keyBytes, "AES") // 4
        Log.d(TAG, "Key: $keySpec")

        // Generate IV
        val ivRandom = SecureRandom() //not caching previous seeded instance of SecureRandom
        val iv = ByteArray(16)
        ivRandom.nextBytes(iv)
        val ivSpec = IvParameterSpec(iv) // 2

        //val cipher = Cipher.getInstance("AES/CBC/PKCS7Padding")
        val cipher = Cipher.getInstance("AES/GCM/NoPadding")

        //cipher.init(Cipher.ENCRYPT_MODE, keySpec, ivSpec)
        cipher.init(Cipher.ENCRYPT_MODE, keySpec, ivSpec)


        val dataToEncrypt = clearTextStr.toByteArray()
        Log.d(TAG, "I/P ByteArray: $dataToEncrypt")

        val encrypted = cipher.doFinal(dataToEncrypt) // 2

        Log.d(TAG, "CIPHER: $encrypted")


//Decrypt
        //val cipherd = Cipher.getInstance("AES/CBC/PKCS7Padding")
        val cipherd = Cipher.getInstance("AES/GCM/NoPadding")

        //cipherd.init(Cipher.DECRYPT_MODE, keySpec, ivSpec)
        cipherd.init(Cipher.DECRYPT_MODE, keySpec, ivSpec)

        val decrypted:ByteArray = cipherd.doFinal(encrypted)

        Log.d(TAG, "DECRYPTED String: --" + decrypted.toString(Charsets.UTF_8) + "--  String: " + decrypted.contentToString())
*/
    }

    fun launchStatusActivity(view: View) {
        val intent = Intent(this, DeviceStatusActivity::class.java)
        startActivity(intent)
    }

}

