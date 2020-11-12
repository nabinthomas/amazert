package com.e.amaze

import android.content.Context
import android.content.Intent
import android.os.Bundle
import android.util.Log
import android.view.Menu
import android.view.View
import android.widget.ArrayAdapter
import android.widget.EditText
import android.widget.Spinner
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.appcompat.widget.Toolbar
import androidx.lifecycle.LiveData
import com.firebase.ui.auth.AuthUI
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.database.DataSnapshot
import com.google.firebase.database.DatabaseError
import com.google.firebase.database.IgnoreExtraProperties
import com.google.firebase.database.ValueEventListener
import com.google.firebase.database.ktx.database
import com.google.firebase.database.ktx.getValue
import com.google.firebase.ktx.Firebase
import com.google.gson.Gson
import com.google.gson.reflect.TypeToken
import java.io.BufferedReader
import java.io.InputStream
import java.io.InputStreamReader


class dbio : AppCompatActivity() {

    companion object {
        private const val TAG = "FireBase_IO"
    }
    private val database = Firebase.database.getReferenceFromUrl("https://amaze-id1.firebaseio.com/").database

    enum class SETTINGS (val description:String) {
        WIFISSID("wireless.wifinet0.ssid"),
        WIFISTATE("Wifi.state"),
        POWERSTATE("Power State"),
        MAXSETTINGS("END-OF-SEtTINGS")
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_dbio)

        val toolbar: Toolbar = findViewById<Toolbar>(R.id.toolbar)
        setSupportActionBar(toolbar)

        //Register listener
        readCurrentDB()

        val power_spinner: Spinner = findViewById(R.id.power_spinner)
        // Create an ArrayAdapter using the string array and a default spinner layout
        ArrayAdapter.createFromResource(
            this,
            R.array.powerstate_array,
            android.R.layout.simple_spinner_item
        ).also { adapter ->
            // Specify the layout to use when the list of choices appears
            adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item)
            // Apply the adapter to the spinner
            power_spinner.adapter = adapter
        }

        val wifi_spinner: Spinner = findViewById(R.id.wifi_spinner)
        // Create an ArrayAdapter using the string array and a default spinner layout
        ArrayAdapter.createFromResource(
            this,
            R.array.wifistate_array,
            android.R.layout.simple_spinner_item
        ).also { adapter ->
            // Specify the layout to use when the list of choices appears
            adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item)
            // Apply the adapter to the spinner
            wifi_spinner.adapter = adapter
        }

        // View the connected DB URL
        Log.d("FBase","In registerAppDb "+database.reference.repo)
        Toast.makeText(baseContext, "Connected to DB: "+database.reference.repo.toString(), Toast.LENGTH_LONG).show()

    }

    override fun onCreateOptionsMenu(menu: Menu): Boolean{
        // Inflate the menu; this adds items to the action bar if it is present.
        menuInflater.inflate(R.menu.menu, menu)
        return true
    }

    override fun onSupportNavigateUp(): Boolean {
        onBackPressed()
        return true
    }

    fun testDbRules(view: View) {
        var userId = FirebaseAuth.getInstance().currentUser?.uid
        val deviceId = "532e8c40-18cd-11eb-a4ca-dca6328f80c0"

        val editTextUid = findViewById<EditText>(R.id.uid)
        val uidVal = editTextUid.text.toString()
        if (uidVal.isNotBlank() ) userId = uidVal.toString() else userId = "NoUser"

        val wifiSpinner = findViewById<View>(R.id.wifi_spinner) as Spinner
        val wifiVal = wifiSpinner.selectedItem.toString()

        val settingsPath = "users/$userId/$deviceId/settings/0/name"
        val valPath = "users/$userId/$deviceId/settings/0/value"

        val myRef = database.getReference(settingsPath.toString())
        myRef.setValue("WifiState")
        val myRef1 = database.getReference(valPath.toString())
        myRef1.setValue(wifiVal)

        /*
        var userId = FirebaseAuth.getInstance().currentUser?.uid
        var deviceId = "Unique_DeviceUID"

        val powerSpinner = findViewById<View>(R.id.power_spinner) as Spinner
        val powerVal = powerSpinner.selectedItem.toString()

        val wifiSpinner = findViewById<View>(R.id.wifi_spinner) as Spinner
        val wifiVal = wifiSpinner.selectedItem.toString()

        val editTextUid = findViewById<EditText>(R.id.uid)
        val uidVal = editTextUid.text.toString()
        if (uidVal.isNotBlank() ) userId = uidVal.toString()

        val refPrefix = userId.plus("/users/$userId/$deviceId/")
        val username = FirebaseAuth.getInstance().currentUser?.displayName
        val userEmail = FirebaseAuth.getInstance().currentUser?.email
        val devRef = database.getReference(refPrefix)
        val map: HashMap<String, String?> = hashMapOf(
            "Username" to username,
            "UserEmail" to userEmail,
            "Wifi" to "$wifiVal",
            "PowerState" to "$powerVal",
            "UID" to "$userId"
        )
        devRef.setValue(map)
         */
    }

    fun updateUI(key:String,  value: String){
        when(key){
            "0" -> {
                Log.d("SWITCH", "IN with 0 - ${value.toString()} ")
                val hostnameTextEdit = findViewById<View>(R.id.editTextHostname) as EditText
                hostnameTextEdit.setText(value.toString())
            }
            "1" -> {//val ssa = value as settingItem
                Log.d("SWITCH", "IN with 1 - ${value.toString()}   ")
                val wifiSsidTextEdit = findViewById<View>(R.id.editTextSSID) as EditText
                wifiSsidTextEdit.setText(value.toString())
            }
            "2" -> {
                Log.d("SWITCH", "IN with 2 - ${value.toString()}   -${value.toInt()}")
                val wifiSpinner = findViewById<View>(R.id.wifi_spinner) as Spinner
                var pos: Int = 1
                if (value.toString() == "0") {
                    pos = 0
                }
                wifiSpinner.setSelection(pos)
            }
            "3" -> {
                Log.d("SWITCH", "IN with 3 - ${value.toString()}")
                val powerSpinner = findViewById<View>(R.id.power_spinner) as Spinner
                var pos: Int = 1
                if (value.toString() == "0") {
                    pos = 0
                }
                powerSpinner.setSelection(pos)
            }

            else -> Log.d("ERROR", "Invalid Key")
        }
    }

     fun readCurrentDB(){
         val userId = FirebaseAuth.getInstance().currentUser?.uid
         val deviceId = "532e8c40-18cd-11eb-a4ca-dca6328f80c0"
         val settingsPath = "users/$userId/$deviceId/settings"
         val rootRef = database.getReference(settingsPath.toString())
         val itemList:MutableList<SettingItem> = ArrayList()

         val valueEventListener = object : ValueEventListener {
            override fun onDataChange(dataSnapshot: DataSnapshot) {
                for (ds in dataSnapshot.children) {
                    val dbValue = ds.getValue<Comment>()
                    if (dbValue != null) {
                        Log.d("OUT...Val: ", dbValue.name.toString() + "  " +   dbValue.value)
                        if (!dbValue.value.isNullOrBlank()) {
                            updateUI(ds.key.toString(), dbValue.value.toString())
                        }
                    }

                    val dbValue1 = ds.getValue<SettingItem>()
                    if (dbValue1 != null) {
                        itemList.add(dbValue1)
                    }
                }
                return
            }

            override fun onCancelled(databaseError: DatabaseError) {
                Log.d(TAG, databaseError.getMessage()) //Don't ignore errors!
            }
        }
        //rootRef.addListenerForSingleValueEvent(valueEventListener)
         rootRef.addValueEventListener(valueEventListener)

     }

    fun readFeatureList() {
        val mContext: Context = applicationContext
        val iStream: InputStream = mContext.getAssets().open("features.json")
        val response = BufferedReader(
            InputStreamReader(iStream, "UTF-8")
        ).use { it.readText() }
        Log.d("JSON read response... [ ", "$response"+"  ]'")

        val itemType = object : TypeToken<List<item>>() {}.type
        var out: List<item> = Gson().fromJson(response, itemType)

        out.forEachIndexed { idx, ite -> Log.i("data", "> Item $idx:\n${ite.Name} ${ite.Description} ${ite.Input}") }
    }

    fun readDb(view: View) {
        val userId = FirebaseAuth.getInstance().currentUser?.uid
        val deviceId = "device-$userId"
        val refPrefix = userId.plus("/device")
        val myRef = database.getReference(refPrefix.toString())
        val childRef = myRef.child(deviceId.toString())
        Log.d(TAG, "IN read DB.... $refPrefix...")

        // addListenerForSingleValueEvent

        childRef.addValueEventListener(object : ValueEventListener {
            override fun onDataChange(dataSnapshot: DataSnapshot) {
                Log.d(TAG, "IN onDataChange DB cb......")

                // This method is called once with the initial value and again
                // whenever data at this location is updated.
                var vPower: String = ""
                var vWifi: String = ""

                var hashMap : HashMap<String, String> = HashMap<String, String> ()

                for (postSnapshot in dataSnapshot.children) {
                    postSnapshot.key?.toString()?.let { hashMap.put(it,
                        postSnapshot.value as String
                    ) }
                }
                var uName:String = ""
                if (hashMap.isNotEmpty()) {
                    uName = hashMap["Username"].toString()
                    val uEmail = hashMap["UserEmail"]
                    vWifi = hashMap["Wifi"] as String
                    vPower = hashMap["PowerState"] as String
                    val uUid = hashMap["UID"]
                    val uDvm = hashMap["daivame"]
                }
                Log.d("readDB", "Username: ".plus(uName).plus(" vPower: ").plus(vPower))
            }

            override fun onCancelled(error: DatabaseError) {
                // Failed to read value
                Log.w(TAG, "Failed to read value.", error.toException())
            }
        })
    }

    fun logoutCurrentUser(view: View) {
        val userName = FirebaseAuth.getInstance().currentUser
        Log.i("SANS", "LOGGING OUT User: $userName")
        AuthUI.getInstance().signOut(this)

        val intent = Intent(this, MainActivity::class.java)
        startActivity(intent)
    }

    fun updateHostname(view: View) {
        val userId = FirebaseAuth.getInstance().currentUser?.uid
        var deviceId: String = MyApplication.Companion.register.deviceId
        if (MyApplication.Companion.register.deviceId === "") {
            deviceId = "532e8c40-18cd-11eb-a4ca-dca6328f80c0" }

        val settingsPath = "users/$userId/$deviceId/settings/0/name"
        val valPath = "users/$userId/$deviceId/settings/0/value"

        val hostnameTextEdit = findViewById<View>(R.id.editTextHostname) as EditText
        val hostnameVal = hostnameTextEdit.text

        val nameRef = database.getReference(settingsPath.toString())
        nameRef.setValue("system.@system[0].hostname")

        val valRef = database.getReference(valPath.toString())
        valRef.setValue(hostnameVal.toString())

    }

    fun updateWifiSsid(view: View) {
        val userId = FirebaseAuth.getInstance().currentUser?.uid
        var deviceId: String = MyApplication.Companion.register.deviceId
        if (MyApplication.Companion.register.deviceId === "") {
            deviceId = "532e8c40-18cd-11eb-a4ca-dca6328f80c0" }

        val settingsPath = "users/$userId/$deviceId/settings/1/name"
        val valPath = "users/$userId/$deviceId/settings/1/value"

        val wifiSsidTextEdit = findViewById<View>(R.id.editTextSSID) as EditText
        val wifiSSIDVal = wifiSsidTextEdit.text

        val nameRef = database.getReference(settingsPath.toString())
        nameRef.setValue("wireless.wifinet0.ssid")

        val valRef = database.getReference(valPath.toString())
        valRef.setValue(wifiSSIDVal.toString())

    }

    fun updateWifiState(view: View) {
        val userId = FirebaseAuth.getInstance().currentUser?.uid
        var deviceId: String = MyApplication.Companion.register.deviceId
        if (MyApplication.Companion.register.deviceId === "") {
            deviceId = "532e8c40-18cd-11eb-a4ca-dca6328f80c0" }
        //val refPrefix = userId.plus("/device/device-$userId")
        val settingsPath = "users/$userId/$deviceId/settings/2/name"
        val valPath = "users/$userId/$deviceId/settings/2/value"

        val wifiSpinner = findViewById<View>(R.id.wifi_spinner) as Spinner
        var wifiVal:String = "1"
        if (wifiSpinner.selectedItem.toString() == "ON") {
            wifiVal = "0"
        }
        val myRef = database.getReference(settingsPath.toString())
        myRef.setValue("wireless.wifinet0.disabled")
        val myRef1 = database.getReference(valPath.toString())
        myRef1.setValue(wifiVal)
    }

    fun updatePowerState(view: View) {
        val userId = FirebaseAuth.getInstance().currentUser?.uid
        var deviceId: String = MyApplication.Companion.register.deviceId
        if (MyApplication.Companion.register.deviceId === "") {
            deviceId = "532e8c40-18cd-11eb-a4ca-dca6328f80c0" }
        val settingsPath = "users/$userId/$deviceId/settings/3/name"
        val valPath = "users/$userId/$deviceId/settings/3/value"

        val powerSpinner = findViewById<View>(R.id.power_spinner) as Spinner
        val powerVal = powerSpinner.selectedItem.toString()

        val myRef = database.getReference(settingsPath.toString())
        myRef.setValue("PowerState")
        val myRef1 = database.getReference(valPath.toString())
        myRef1.setValue(powerVal)
    }
}

//class sett(val setting: List<item>)
class item(val Name: String ,val Description: String, val Input: String)

@IgnoreExtraProperties
data class Comment(
    var name: String? = "",
    var value: String? = ""
)