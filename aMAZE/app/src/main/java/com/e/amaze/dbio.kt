package com.e.amaze

import android.content.Intent
import android.os.Bundle
import android.util.Log
import android.view.View
import android.widget.ArrayAdapter
import android.widget.EditText
import android.widget.Spinner
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import com.firebase.ui.auth.AuthUI
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.database.DataSnapshot
import com.google.firebase.database.DatabaseError
import com.google.firebase.database.ValueEventListener
import com.google.firebase.database.ktx.database
import com.google.firebase.ktx.Firebase

class dbio : AppCompatActivity() {

    companion object {
        private const val TAG = "FireBase_IO"
    }
    private val database = Firebase.database

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_dbio)

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

    private fun readCurrentDB(){
        val userId = FirebaseAuth.getInstance().currentUser?.uid
        val deviceId = "device-$userId"
        val refPrefix = userId.plus("/device/$deviceId")
        val myRef = database.getReference(refPrefix)
        val childRef = myRef.child(userId.toString())

        childRef.addListenerForSingleValueEvent(object : ValueEventListener {
            override fun onDataChange(dataSnapshot: DataSnapshot) {
                Log.d(TAG, "IN onDataChange DB cb......")

                // This method is called once with the initial value and again
                // whenever data at this location is updated.
                var vPower: String = ""
                var vWifi: String = ""

                var hashMap : HashMap<String, String> = HashMap<String, String> ()


                for (postSnapshot in dataSnapshot.children) {
                    Log.d(TAG, "IN read DB, Key: ${postSnapshot.key.toString()}")
                    Log.d(TAG, "IN read DB, Value: ${postSnapshot.value.toString()}")


                    postSnapshot.key?.toString()?.let { hashMap.put(it,
                        postSnapshot.value as String
                    ) }
                    Log.i("SANS", postSnapshot.key.toString().plus(postSnapshot.value.toString()) )
                }
            }

            override fun onCancelled(error: DatabaseError) {
                // Failed to read value
                Log.w(TAG, "Failed to read value.", error.toException())
            }
        })
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
                    //Log.d("SANS", "-----------read DB, Key: ${postSnapshot.key.toString()}")

                    postSnapshot.key?.toString()?.let { hashMap.put(it,
                        postSnapshot.value as String
                    ) }
                    //Log.i("SANS", postSnapshot.key.toString().plus(postSnapshot.value.toString()) )
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

    fun updateWifiState(view: View) {
        val userId = FirebaseAuth.getInstance().currentUser?.uid
        val deviceId = "532e8c40-18cd-11eb-a4ca-dca6328f80c0"
        //val refPrefix = userId.plus("/device/device-$userId")
        val settingsPath = "users/$userId/$deviceId/settings/0/name"
        val valPath = "users/$userId/$deviceId/settings/0/value"

        val wifiSpinner = findViewById<View>(R.id.wifi_spinner) as Spinner
        val wifiVal = wifiSpinner.selectedItem.toString()

        val myRef = database.getReference(settingsPath.toString())
        myRef.setValue("WifiState")
        val myRef1 = database.getReference(valPath.toString())
        myRef1.setValue(wifiVal)
    }

    fun updatePowerState(view: View) {
        val userId = FirebaseAuth.getInstance().currentUser?.uid
        val deviceId = "532e8c40-18cd-11eb-a4ca-dca6328f80c0"
        //val refPrefix = userId.plus("/device/device-$userId")
        val settingsPath = "users/$userId/$deviceId/settings/1/name"
        val valPath = "users/$userId/$deviceId/settings/1/value"

        val powerSpinner = findViewById<View>(R.id.power_spinner) as Spinner
        val powerVal = powerSpinner.selectedItem.toString()

        val myRef = database.getReference(settingsPath.toString())
        myRef.setValue("PowerState")
        val myRef1 = database.getReference(valPath.toString())
        myRef1.setValue(powerVal)
    }
}