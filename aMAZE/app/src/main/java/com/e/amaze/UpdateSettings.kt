package com.e.amaze

import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.telephony.mbms.StreamingServiceInfo
import android.view.View
import android.widget.Spinner
import android.widget.TextView
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.database.ktx.database
import com.google.firebase.ktx.Firebase

class UpdateSettings : AppCompatActivity() {

    lateinit var settingName:String
    lateinit var settingValue:String
    lateinit var databaseIndex:String
    private val database = Firebase.database.getReferenceFromUrl("https://amaze-id1.firebaseio.com/").database

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_update_settings)

        settingName = intent.getStringExtra("Name")
        settingValue = intent.getStringExtra("Value")
        databaseIndex = intent.getStringExtra("dbIndex")

        val settingNameView: TextView = findViewById(R.id.textViewUpdateName)
        settingNameView.setText(settingName.toString())
        val settingValueView: TextView = findViewById(R.id.EditTextUpdateValue)
        settingValueView.setText(settingValue.toString())

    }

    fun updateSetting(view: View) {
        val settingValueView: TextView = findViewById(R.id.EditTextUpdateValue)
        val updateVal = settingValueView.text

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
}