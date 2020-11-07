package com.e.amaze

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData

class DeviceViewModel (application: Application) : AndroidViewModel(application) {
    var allDevices: MutableLiveData<List<String>>

    init {
        allDevices = MyApplication.Companion.deviceList
    }
}