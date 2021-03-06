package com.e.amaze

import android.content.Intent
import android.graphics.drawable.BitmapDrawable
import android.os.Bundle
import android.view.Menu
import android.view.MenuItem
import android.widget.TextView
import androidx.appcompat.app.AppCompatActivity
import androidx.appcompat.widget.Toolbar
import androidx.lifecycle.LiveData
import androidx.lifecycle.Observer
import androidx.lifecycle.ViewModelProvider
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.google.firebase.database.DataSnapshot

class DeviceSettingActivity : AppCompatActivity() {

    private lateinit var settingsViewModel: DeviceSettingsViewModel

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_device_setting)

        val toolbar: Toolbar = findViewById<Toolbar>(R.id.toolbar)
        setSupportActionBar(toolbar)

        var devName = intent.getStringExtra("Name")

        var devText = findViewById<TextView>(R.id.textView9)

        devText.text = devName

        val recyclerView = findViewById<RecyclerView>(R.id.recyclerview)
        val adapter = SettingsAdapter(this)
        recyclerView.adapter = adapter
        recyclerView.layoutManager = LinearLayoutManager(this)

        settingsViewModel = ViewModelProvider(this).get(DeviceSettingsViewModel::class.java)

        val liveData: LiveData<DataSnapshot?> = settingsViewModel.getDataSnapshotLiveData()
        liveData.observe(this,
            Observer { products ->
                // Update the cached copy of the products in the adapter.
                products?.let { adapter.setItems(it) }
            })
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
}