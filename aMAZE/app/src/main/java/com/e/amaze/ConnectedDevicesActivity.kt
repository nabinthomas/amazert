package com.e.amaze

import android.os.Bundle
import android.widget.TextView
import androidx.appcompat.app.AppCompatActivity
import androidx.lifecycle.LiveData
import androidx.lifecycle.Observer
import androidx.lifecycle.ViewModelProvider
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.google.firebase.database.DataSnapshot
import org.w3c.dom.Text

class ConnectedDevicesActivity : AppCompatActivity() {

    private lateinit var statusViewModel: DeviceStatusViewModel
    lateinit var deviceStatusView:TextView
    lateinit var powerOnTimeView:TextView
    lateinit var hBeatView:TextView

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_device_status)
        deviceStatusView = findViewById(R.id.textView15)
        powerOnTimeView = findViewById(R.id.textView16)
        hBeatView = findViewById(R.id.textView17)

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
                deviceStatusView.text = MyApplication.Companion.DeviceStatus
                powerOnTimeView.text = MyApplication.Companion.PowerOnTime
                hBeatView.text = MyApplication.Companion.HbTime
            })
    }
}
