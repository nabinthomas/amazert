package com.e.amaze

import android.content.Intent
import android.graphics.drawable.BitmapDrawable
import android.os.Bundle
import android.util.Log
import android.view.Gravity
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

    fun rebootDevice(view: View) {
        val userId = FirebaseAuth.getInstance().currentUser?.uid
        var deviceId: String = MyApplication.Companion.register.deviceId
        if (MyApplication.Companion.register.deviceId === "") {
            deviceId = "532e8c40-18cd-11eb-a4ca-dca6328f80c0" }

        val dbCommandPath = "users/$userId/$deviceId/request"
        val actionPath = dbCommandPath.plus("/action")
        val cmdPath = dbCommandPath.plus("/command")
        val actionRef = database.getReference(actionPath)
        val cmdRef = database.getReference(cmdPath)

        val rebootCommand:String = "[reboot, now]"
        val encryptedValue = MyApplication.Companion.symEnc.encryptString(rebootCommand)

        actionRef.setValue("command")
        cmdRef.setValue(encryptedValue)
        //Toast.makeText(baseContext, "Device REBOOT triggered", Toast.LENGTH_LONG).show()

        val location = IntArray(2)
        findViewById<TextView>(R.id.textView12).getLocationOnScreen(location)
        val toast = Toast.makeText(applicationContext,"Device REBOOT triggered",Toast.LENGTH_SHORT)
        toast.setGravity(Gravity.TOP or Gravity.LEFT, location [0]+200, location[1])
        toast.show()
    }
}

