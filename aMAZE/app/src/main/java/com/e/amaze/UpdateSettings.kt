package com.e.amaze

import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.telephony.mbms.StreamingServiceInfo
import android.util.Log
import android.view.View
import android.widget.Spinner
import android.widget.Switch
import android.widget.TextView
import androidx.core.view.isVisible
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.database.ktx.database
import com.google.firebase.ktx.Firebase

class UpdateSettings : AppCompatActivity() {

    lateinit var settingName:String
    lateinit var settingValue:String
    lateinit var databaseIndex:String
    lateinit var displayName:String

    lateinit var settingNameView: TextView
    lateinit var settingValueView: TextView
    lateinit var switchCtl: Switch

    private val database = Firebase.database.getReferenceFromUrl("https://amaze-id1.firebaseio.com/").database

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_update_settings)

        settingName = intent.getStringExtra("Name")
        displayName = intent.getStringExtra("DisplayName")
        settingValue = intent.getStringExtra("Value")
        databaseIndex = intent.getStringExtra("dbIndex")

        settingNameView = findViewById(R.id.textViewUpdateName)
        settingNameView.setText(displayName.toString())
        settingValueView = findViewById(R.id.EditTextUpdateValue)
        settingValueView.setText(settingValue.toString())
        switchCtl = findViewById(R.id.switch1)

        updateUIControls()
    }

    fun updateUIControls() {
        when(settingName){
            "wireless.wifinet0.disabled" -> {
                switchCtl.visibility = View.VISIBLE
                switchCtl.isChecked = settingValue.toString() != "1"
                settingValueView.visibility = View.INVISIBLE
            }
            else -> {
                Log.d("ERROR", "Invalid Key")
                switchCtl.visibility = View.INVISIBLE
                settingValueView.visibility = View.VISIBLE
            }
        }

    }

    fun getUpdateVal():String {
        var updateVal:String = ""
        if (switchCtl.visibility === View.VISIBLE){
            if(switchCtl.isChecked) {
                updateVal = "0"
            }else{
                updateVal = "1"
            }
        }else {
            updateVal = settingValueView.text.toString()
        }
        return updateVal
    }

    fun updateSetting(view: View) {
        val settingValueView: TextView = findViewById(R.id.EditTextUpdateValue)
        //val updateVal = settingValueView.text
        val updateVal = getUpdateVal()

        val userId = FirebaseAuth.getInstance().currentUser?.uid
        var deviceId: String = MyApplication.Companion.register.deviceId
        if (MyApplication.Companion.register.deviceId === "") {
            deviceId = "532e8c40-18cd-11eb-a4ca-dca6328f80c0" }
        val settingPath = "users/$userId/$deviceId/settings/$databaseIndex/name"
        val valPath = "users/$userId/$deviceId/settings/$databaseIndex/value"

        val nameRef = database.getReference(settingPath.toString())
        nameRef.setValue(settingName)
        val valRef = database.getReference(valPath.toString())
        valRef.setValue(updateVal.toString())

        // Navigate back to Settings page
        onBackPressed()
    }

    fun resetSettingValue(view: View) {
        settingValueView.setText(settingValue.toString())
    }

}