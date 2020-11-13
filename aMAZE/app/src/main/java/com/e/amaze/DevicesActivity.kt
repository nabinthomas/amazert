package com.e.amaze

import android.content.Intent
import android.graphics.drawable.BitmapDrawable
import android.os.Bundle
import android.util.Log
import android.view.Menu
import android.view.MenuItem
import android.view.View
import android.widget.TextView
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.appcompat.widget.Toolbar
import com.firebase.ui.auth.AuthUI
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.database.ktx.database
import com.google.firebase.ktx.Firebase
import java.security.SecureRandom
import javax.crypto.Cipher
import javax.crypto.SecretKeyFactory
import javax.crypto.spec.IvParameterSpec
import javax.crypto.spec.PBEKeySpec
import javax.crypto.spec.SecretKeySpec

class DevicesActivity : AppCompatActivity() {

    private val TAG = "DEVICES_ACTIVITY"
    private val database = Firebase.database.getReferenceFromUrl("https://amaze-id1.firebaseio.com/").database
    private lateinit var deviceViewModel: DeviceViewModel
    private lateinit var devName: String

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_devices)

        val toolbar: Toolbar = findViewById<Toolbar>(R.id.toolbar)
        setSupportActionBar(toolbar)

        devName = intent.getStringExtra("Name")

        var devText = findViewById<TextView>(R.id.textView9)

        devText.text = devName
    }

    override fun onCreateOptionsMenu(menu: Menu): Boolean{
        // Inflate the menu; this adds items to the action bar if it is present.
        menuInflater.inflate(R.menu.menu, menu)

        val userProf = menu.findItem(R.id.miProfile)

        userProf.setIcon(BitmapDrawable(resources, MyApplication.Companion.bitmap))
        return true
    }

    fun onProfileAction(item: MenuItem) {
        val intent = Intent(this, UserProfile::class.java)
        startActivity(intent)
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

        val intent = Intent(this, MainActivity::class.java)
        startActivity(intent)
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
        val cipherText = MyApplication.Companion.symEnc.encryptString("1234")
        Log.d(TAG, "CIPHERText Combo: " + cipherText)

        val x = "ACRRaw=="
        val bx = java.util.Base64.getDecoder().decode(x)
        val iiv = "u4+C5I/3kmaAEo3h5L/EVg=="
        val biiv = java.util.Base64.getDecoder().decode(iiv)
        val digest = "9+vCN1DDxsAJPXhi0YjU5Q=="
        val bdigest = java.util.Base64.getDecoder().decode(digest)
        Log.d(TAG, "Digest size: " + bdigest.size)

        val (sIv, sDig, sCip) = MyApplication.Companion.symEnc.segregateSettingData("u4+C5I/3kmaAEo3h5L/EVg==9+vCN1DDxsAJPXhi0YjU5Q==ACRRaw==")
        Log.d(TAG, "sIV: " + String(java.util.Base64.getEncoder().encode(sIv.iv)))
        Log.d(TAG, "sDIGEST: " + String(java.util.Base64.getEncoder().encode(sDig)))
        Log.d(TAG, "sCIPHER: " + String(java.util.Base64.getEncoder().encode(sCip)))

        val decText = MyApplication.Companion.symEnc.decryptCipherText(sCip+sDig, sIv)

        Log.d(TAG, "Decrypted text#:: --" + decText.toString()+ "--")
    }

    fun launchStatusActivity(view: View) {
        val intent = Intent(this, ConnectedDevicesActivity::class.java)
        startActivity(intent)
    }
}

