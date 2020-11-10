package com.e.amaze

import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity
import androidx.lifecycle.LiveData
import androidx.lifecycle.Observer
import androidx.lifecycle.ViewModelProvider
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.google.firebase.database.DataSnapshot

class DeviceStatusActivity : AppCompatActivity() {

    private lateinit var statusViewModel: DeviceStatusViewModel

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_device_status)

        val recyclerView = findViewById<RecyclerView>(R.id.recyclerview_status)
        val adapter = StatusAdapter(this)
        recyclerView.adapter = adapter
        recyclerView.layoutManager = LinearLayoutManager(this)

        statusViewModel = ViewModelProvider(this).get(DeviceStatusViewModel::class.java)

        val liveData: LiveData<DataSnapshot?> = statusViewModel.getDataSnapshotLiveData()
        liveData.observe(this,
            Observer { products ->
                // Update the cached copy of the products in the adapter.
                products?.let { adapter.setItems(it) }
            })
    }
}
