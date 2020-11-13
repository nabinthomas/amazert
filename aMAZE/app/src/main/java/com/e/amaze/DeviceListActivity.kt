package com.e.amaze

import android.content.Intent
import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.graphics.drawable.BitmapDrawable
import android.os.Bundle
import android.view.Menu
import android.view.MenuItem
import android.view.View
import androidx.appcompat.app.AppCompatActivity
import androidx.appcompat.widget.Toolbar
import androidx.lifecycle.Observer
import androidx.lifecycle.ViewModelProvider
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.google.firebase.auth.FirebaseAuth
import java.io.InputStream
import java.net.URL


class DeviceListActivity : AppCompatActivity(){

    private lateinit var deviceViewModel: DeviceViewModel

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_device_list)

        val toolbar: Toolbar = findViewById<Toolbar>(R.id.toolbar)
        setSupportActionBar(toolbar)

//        val actionbar = supportActionBar
//        actionbar!!.title = MyApplication.toolbarName
        //set back button
//        actionbar.setDisplayHomeAsUpEnabled(true)
//        actionbar.setDisplayHomeAsUpEnabled(true)

        val recyclerView = findViewById<RecyclerView>(R.id.recyclerview_device)
        val adapter = DeviceListAdapter(this)
        recyclerView.adapter = adapter
        recyclerView.layoutManager = LinearLayoutManager(this)

        deviceViewModel = ViewModelProvider(this).get(DeviceViewModel::class.java)

        deviceViewModel.allDevices.observe(this, Observer<List<String>> { devices ->
            // Update the cached copy of the products in the adapter.
            devices?.let { adapter.setItems(it) }

        })

        val fab: View = findViewById(R.id.fab)
        fab.setOnClickListener { view ->
            val intent = Intent(this, AddDevice::class.java)
            var uid = FirebaseAuth.getInstance().currentUser?.uid
            var user = FirebaseAuth.getInstance().currentUser?.email
            intent.putExtra("Name", user)
            intent.putExtra("Uid", uid)
            startActivity(intent)
        }

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