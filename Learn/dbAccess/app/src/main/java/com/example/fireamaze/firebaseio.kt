package com.example.fireamaze

import android.os.Bundle
import android.util.Log
import android.view.View
import android.widget.EditText
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

    }

    override fun onSupportNavigateUp(): Boolean {
        onBackPressed()
        return true
    }

    fun writeDb(view: View) {
        var userId = FirebaseAuth.getInstance().currentUser?.uid
        val editTextView = findViewById<EditText>(R.id.editTextPower)
        val powerVal = editTextView.text.toString()

        val wifiTextView = findViewById<EditText>(R.id.editTextWifi)
        val wifiVal = editTextView.text.toString()

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
            "UID" to "$userId",
            "daivame" to "Eeshwaraaa"
        )
        devRef.setValue(map)


    }

    fun readDb(view: View) {

        val userId = FirebaseAuth.getInstance().currentUser?.uid
        val refPrefix = userId.plus("/device/device-$userId")
        val myRef = database.getReference(refPrefix)
        val childRef = myRef.child(userId.toString())
        Log.d(TAG, "IN read DB.... $refPrefix...")



        childRef.addValueEventListener(object : ValueEventListener {
            override fun onDataChange(dataSnapshot: DataSnapshot) {
                Log.d(TAG, "IN onDataChange DB cb......")

                // This method is called once with the initial value and again
                // whenever data at this location is updated.
                var vPower: String = ""
                var vWifi: String = ""

                var hashMap : HashMap<String, String> = HashMap<String, String> ()


                for (postSnapshot in dataSnapshot.children) {
                    Log.d(TAG, "IN read DB, VAL: ${postSnapshot.toString()}")

                    postSnapshot.key?.toString()?.let { hashMap.put(it,
                        postSnapshot.value as String
                    ) }
                    Log.i("SANS", postSnapshot.key.toString().plus(postSnapshot.value.toString()) )
                }

                if (hashMap.isNotEmpty()) {
                    val uName = hashMap["Username"]
                    val uEmail = hashMap["UserEmail"]
                    vWifi = hashMap["Wifi"] as String
                    vPower = hashMap["PowerState"] as String
                    val uUid = hashMap["UID"]
                    val uDvm = hashMap["daivame"]
                }
/*
                val value = dataSnapshot.getValue<String>()
 */
                val editPowerTextView = findViewById<EditText>(R.id.editTextPower)
                editPowerTextView.setText(vPower)

                val wifiTextView = findViewById<EditText>(R.id.editTextWifi)
                wifiTextView.setText(vWifi)

                Log.d(TAG, "Wifi Value is: $vWifi")
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
}