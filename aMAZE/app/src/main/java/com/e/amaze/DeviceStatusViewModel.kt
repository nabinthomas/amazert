package com.e.amaze

import android.app.Application
import android.util.Log
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.database.DataSnapshot
import com.google.firebase.database.DatabaseReference
import com.google.firebase.database.FirebaseDatabase


class DeviceStatusViewModel (application: Application) : AndroidViewModel(application) {
    var userId = FirebaseAuth.getInstance().currentUser?.uid
    private val deviceId = getDeviceId()
    private val statusPath = "/users/$userId/$deviceId/status/"
    private val database =  MyApplication.Companion.projectDatabase
    private val DbREF:DatabaseReference = database.getReference(statusPath)

    var statusLiveData = FirebaseQueryLiveData(DbREF)

    private fun getDeviceId():String{
        var deviceId = MyApplication.Companion.register.deviceId
        if (MyApplication.Companion.register.deviceId === "") {
            deviceId = "532e8c40-18cd-11eb-a4ca-dca6328f80c0" }
        return deviceId.toString()
    }

    init {
        val fireObj:FirebaseQueryLiveData = FirebaseQueryLiveData(DbREF)

        statusLiveData = FirebaseQueryLiveData(DbREF)
    }

    fun getDataSnapshotLiveData(): LiveData<DataSnapshot?> {
        return statusLiveData
    }
}

