package com.e.amaze

import android.content.Intent
import android.graphics.drawable.BitmapDrawable
import android.os.Bundle
import android.util.Log
import android.view.Gravity
import android.view.Menu
import android.view.MenuItem
import android.view.View
import android.view.View.OnFocusChangeListener
import android.widget.Switch
import android.widget.TextView
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.appcompat.widget.Toolbar
import com.google.firebase.auth.FirebaseAuth


class UpdateSettings : AppCompatActivity() {

    lateinit var settingName:String
    lateinit var settingValue:String
    lateinit var databaseIndex:String
    lateinit var displayName:String
    lateinit var inputOptions:String


    lateinit var settingNameView: TextView
    lateinit var settingValueView: TextView
    lateinit var switchCtl: Switch
    //private val symEnc:SymKeyEncryption = SymKeyEncryption()
    private val TAG = "UpdateSettings"

    private val database = MyApplication.Companion.projectDatabase

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_update_settings)

        val toolbar: Toolbar = findViewById<Toolbar>(R.id.toolbar)
        setSupportActionBar(toolbar)

        settingName = intent.getStringExtra("Name")
        displayName = intent.getStringExtra("DisplayName")
        settingValue = intent.getStringExtra("Value")
        inputOptions = intent.getStringExtra("inputOptions")

        databaseIndex = intent.getStringExtra("dbIndex")

        settingNameView = findViewById(R.id.textViewUpdateName)
        settingNameView.setText(displayName.toString())
        settingValueView = findViewById(R.id.EditTextUpdateValue)
        settingValueView.setText(settingValue.toString())
        switchCtl = findViewById(R.id.switch1)

        updateUIControls()

        settingValueView.onFocusChangeListener = object : OnFocusChangeListener {
            override fun onFocusChange(view: View, hasFocus: Boolean) {
                if (!hasFocus) {
                    val location = IntArray(2)
                    settingValueView.getLocationOnScreen(location)
                    val toast = Toast.makeText(applicationContext,inputOptions,Toast.LENGTH_LONG)
                    toast.setGravity(Gravity.TOP or Gravity.LEFT, location[0], location[1]-150)
                    toast.show()
                }
            }
        }
        settingValueView.setOnClickListener(){
            val location = IntArray(2)
            settingValueView.getLocationOnScreen(location)
            val toast = Toast.makeText(applicationContext,inputOptions,Toast.LENGTH_LONG)
            toast.setGravity(Gravity.TOP or Gravity.LEFT, location [0], location[1]-150)
            toast.show()
        }

        switchCtl.setOnClickListener(){
            val location = IntArray(2)
            switchCtl.getLocationOnScreen(location)
            val toast = Toast.makeText(applicationContext,inputOptions,Toast.LENGTH_SHORT)
            toast.setGravity(Gravity.TOP or Gravity.LEFT, location [0]+100, location[1]-150)
            toast.show()
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
        val updateVal = getUpdateVal()
        // Encrypt value using SymKey Enc
        val encryptedValue = MyApplication.Companion.symEnc.encryptString(updateVal)
        Log.d(TAG, "CIPHERText Combo: " + encryptedValue)

        val userId = FirebaseAuth.getInstance().currentUser?.uid
        var deviceId: String = MyApplication.Companion.register.deviceId
        if (MyApplication.Companion.register.deviceId === "") {
            deviceId = "532e8c40-18cd-11eb-a4ca-dca6328f80c0" }
        val settingPath = "users/$userId/$deviceId/settings/$databaseIndex/name"
        val valPath = "users/$userId/$deviceId/settings/$databaseIndex/value"

        val nameRef = database.getReference(settingPath.toString())
        nameRef.setValue(settingName)
        val valRef = database.getReference(valPath.toString())
        valRef.setValue(encryptedValue)

        // Navigate back to Settings page
        onBackPressed()
    }

    fun resetSettingValue(view: View) {
        settingValueView.setText(settingValue.toString())
        updateUIControls()
    }
}