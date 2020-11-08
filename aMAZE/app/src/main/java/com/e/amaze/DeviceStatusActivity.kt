package com.e.amaze

import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.util.Log
import androidx.security.crypto.EncryptedFile
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.database.DataSnapshot
import com.google.firebase.database.DatabaseError
import com.google.firebase.database.FirebaseDatabase
import com.google.firebase.database.ValueEventListener
import com.google.firebase.database.ktx.getValue
import com.google.gson.Gson
import com.google.gson.reflect.TypeToken

class DeviceStatusActivity : AppCompatActivity() {

    private val database = MyApplication.Companion.projectDatabase
    private val TAG:String = "DeviceStatusActivity"

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_device_status)

        //readCurrentDB()
    }

    private fun readCurrentDB() {
        val userId = FirebaseAuth.getInstance().currentUser?.uid
        val deviceId = "532e8c40-18cd-11eb-a4ca-dca6328f80c0"
        val settingsPath = "users/$userId/$deviceId/status"
        val rootRef = database.getReference(settingsPath.toString())
        val itemList: MutableList<SettingItem> = ArrayList()

        val valueEventListener = object : ValueEventListener {
            override fun onDataChange(dataSnapshot: DataSnapshot) {
                Log.d(TAG, dataSnapshot.toString())
                Log.d(TAG, dataSnapshot.value.toString())
                //Log.d(TAG, dataSnapshot.value.toString().substring(0, 10))
                //Log.d(TAG, dataSnapshot.value.toString().)

                //val sans:Array<String> = dataSnapshot.value.toString() as Array<String>
                //Log.d(TAG, sans[0].toString() + " " + sans[2].toString())


                    for (ds in dataSnapshot.children) {
                        Log.d(TAG, ds.key.toString())
                        Log.d(TAG, ds.value.toString())

                        val dbValue = ds.value as HashMap<String, Any>
                        Log.d(TAG, dbValue["value"].toString() )

                        val itemType = object : TypeToken<StatusItem>() {}.type
                        var out: StatusItem = Gson().fromJson(dbValue["value"].toString(), itemType)
                        Log.d(TAG, out.toString())
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

}