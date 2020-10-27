package com.example.fireamaze

//import android.R
import android.app.Activity
import android.os.Bundle
import android.util.Log
import android.view.View
import android.widget.AdapterView
import android.widget.ArrayAdapter
import android.widget.EditText
import android.widget.Spinner
import androidx.appcompat.app.AppCompatActivity
import com.firebase.ui.auth.AuthUI
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.database.DataSnapshot
import com.google.firebase.database.DatabaseError
import com.google.firebase.database.ValueEventListener
import com.google.firebase.database.ktx.database
import com.google.firebase.ktx.Firebase


class firebaseio : AppCompatActivity() {
    companion object {
        private const val TAG = "SANS"
    }
    private val database = Firebase.database

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_firebaseio)

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

        // Update Settings once
        //readCurrentDB()
    }

    override fun onSupportNavigateUp(): Boolean {
        onBackPressed()
        return true
    }

    fun updateCompleteDB(view: View) {

        val powerSpinner = findViewById<View>(R.id.power_spinner) as Spinner
        val powerVal = powerSpinner.selectedItem.toString()

        val wifiSpinner = findViewById<View>(R.id.wifi_spinner) as Spinner
        val wifiVal = wifiSpinner.selectedItem.toString()

        var userId = FirebaseAuth.getInstance().currentUser?.uid

        val editTextUid = findViewById<EditText>(R.id.uid)
        val uidVal = editTextUid.text.toString()
        if (uidVal.isNotBlank() ) userId = uidVal.toString()

        val refPrefix = userId.plus("/device/device-$userId")
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
        onBackPressed()
    }

    fun updateWifiState(view: View) {
        val userId = FirebaseAuth.getInstance().currentUser?.uid
        val refPrefix = userId.plus("/device/device-$userId")

        val wifiSpinner = findViewById<View>(R.id.wifi_spinner) as Spinner
        val wifiVal = wifiSpinner.selectedItem.toString()

        val updWifiState:Map<String, String> = mapOf(Pair("$refPrefix/WifiState", "$wifiVal"))
        Firebase.database.reference.updateChildren(updWifiState)
    }

    fun updatePowerState(view: View) {
        val userId = FirebaseAuth.getInstance().currentUser?.uid
        val refPrefix = userId.plus("/device/device-$userId")

        val powerSpinner = findViewById<View>(R.id.power_spinner) as Spinner
        val powerVal = powerSpinner.selectedItem.toString()

        val updPowerState:Map<String, String> = mapOf(Pair("$refPrefix/PowerState", "$powerVal"))
        Firebase.database.reference.updateChildren(updPowerState)
    }
}