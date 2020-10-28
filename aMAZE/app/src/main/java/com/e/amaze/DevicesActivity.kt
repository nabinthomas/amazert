package com.e.amaze

import android.app.Activity
import android.content.Intent
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.util.Log
import android.view.View
import android.widget.Toast
import com.firebase.ui.auth.AuthUI
import com.firebase.ui.auth.IdpResponse
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.database.ktx.database
import com.google.firebase.ktx.Firebase

class DevicesActivity : AppCompatActivity() {

    private val database = Firebase.database

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_devices)
    }

    fun launchAddDevice(view: View) {
        val intent = Intent(this, AddDevice::class.java)
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
}

