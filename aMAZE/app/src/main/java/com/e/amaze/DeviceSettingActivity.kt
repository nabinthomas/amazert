package com.e.amaze

import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity
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
}