package com.e.amaze

import android.content.Intent
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.view.View

class DevicesActivity : AppCompatActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_devices)
    }

    fun launchAddDevice(view: View) {
        val intent = Intent(this, AddDevice::class.java)
        startActivity(intent)
    }
}

