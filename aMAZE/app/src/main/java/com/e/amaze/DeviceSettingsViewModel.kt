package com.e.amaze

import android.app.Application
import android.util.Log
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.LiveData
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.database.DataSnapshot
import com.google.firebase.database.DatabaseReference
import com.google.firebase.database.FirebaseDatabase


class DeviceSettingsViewModel (application: Application) : AndroidViewModel(application) {
    var userId = FirebaseAuth.getInstance().currentUser?.uid
    private val deviceId = getDeviceId()
    private val settingsPath = "/users/$userId/$deviceId/settings/"
    private val database =  FirebaseDatabase.getInstance().getReferenceFromUrl("https://amaze-id1.firebaseio.com").database
    private val DbREF:DatabaseReference = database.getReference(settingsPath)

    var settingLiveData = FirebaseQueryLiveData(DbREF)

    private fun getDeviceId():String{
        var deviceId = MyApplication.Companion.register.deviceId
        if (MyApplication.Companion.register.deviceId === "") {
            deviceId = "532e8c40-18cd-11eb-a4ca-dca6328f80c0" }
        return deviceId.toString()
    }

    init {
        val fireObj:FirebaseQueryLiveData = FirebaseQueryLiveData(DbREF)

        settingLiveData = FirebaseQueryLiveData(DbREF)
    }


    fun getDataSnapshotLiveData(): LiveData<DataSnapshot?> {
        return settingLiveData
    }}