package com.e.amaze

import android.app.Activity
import android.content.Intent
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.util.Log
import android.view.View
import android.widget.Button
import android.widget.Toast
import com.firebase.ui.auth.AuthUI
import com.firebase.ui.auth.IdpResponse
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.database.ktx.database
import com.google.firebase.ktx.Firebase

class DevicesActivity : AppCompatActivity() {

    private val database = Firebase.database.getReferenceFromUrl("https://amaze-id1.firebaseio.com/").database

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_devices)
/*
        val button = findViewById<Button>(R.id.button7)
        button.setOnClickListener {
            registerDevice("c55d78f3-18de-11eb-ae63-dca6328f819e")
        }
 */
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

    fun toastItUp(view: View) {
        val intent = Intent(this, DeviceSettingActivity::class.java)
        startActivity(intent)
    }
}

