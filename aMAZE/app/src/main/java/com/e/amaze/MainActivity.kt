package com.e.amaze

import android.app.Activity
import android.app.Application
import android.content.Context
import android.content.Intent
import android.os.Bundle
import android.util.Log
import android.view.View
import androidx.appcompat.app.AppCompatActivity
import com.firebase.ui.auth.AuthUI
import com.firebase.ui.auth.IdpResponse
import com.google.firebase.auth.FirebaseAuth
import com.google.gson.Gson
import com.google.gson.JsonSyntaxException
import java.io.BufferedReader
import java.io.File
import java.io.InputStream
import java.io.InputStreamReader

class MyApplication : Application() {

    companion object {
        var context: Context? = null
        var register: Register = Register("","","","")
        var dev_name: String = ""
    }
    override fun onCreate() {
        super.onCreate()

        Companion.context = applicationContext;
    }

}

class MainActivity : AppCompatActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        var fPath = this.applicationInfo.dataDir
        Log.d("MAIN", "PATH $fPath")

        var deviceName: String? = null
        var data:String = ""

        val listAllFiles = File(fPath).listFiles()

        if (listAllFiles != null && listAllFiles.isNotEmpty()) {
            Log.d("MAIN", "List files " + listAllFiles.toString())
            for (currentFile in listAllFiles) {
                if (currentFile.name.endsWith(".dev")) {
                    Log.e("MAIN", "Abs path " + currentFile.getAbsolutePath())
                    // File Name
                    Log.e("MAIN", "Get file name " + currentFile.getName())
                    MyApplication.Companion.dev_name = currentFile.getName()
                    //fileList.add(currentFile.absoluteFile)
                    deviceName = currentFile.absoluteFile.toString()
                }
            }
        }

        Log.d("MAIN", "Dev name " + MyApplication.Companion.dev_name)

        if (deviceName != null) {
            Log.d("MAIN","Device name $deviceName")
            data = FileCrypt().decryptFile(this, "$deviceName")
            Log.d("MAIN", "DATA    $data")

            try {
                val registerDev = Gson().fromJson(data, Register::class.java)

                MyApplication.Companion.register.deviceId = registerDev.deviceId
                MyApplication.Companion.register.email = registerDev.email
                MyApplication.Companion.register.registrationId = registerDev.registrationId
                MyApplication.Companion.register.uid = registerDev.uid

                Log.d(
                    "MAIN",
                    "Dev id: " + MyApplication.Companion.register.deviceId + " email: " + MyApplication.Companion.register.email + " uid: " +  MyApplication.Companion.register.uid
                )
            } catch (e: JsonSyntaxException){
                Log.d("MAIN", "Error in parsing Json")
            }
        }
    }

    fun launchSignInFlow(view: View) {
        // Give users the option to sign in / register with their email or Google account.
        // If users choose to register with their email,
        // they will need to create a password as well.
        val providers = arrayListOf(
            AuthUI.IdpConfig.EmailBuilder().build(), AuthUI.IdpConfig.GoogleBuilder().build()

            // This is where you can provide more ways for users to register and
            // sign in.
        )

        // Create and launch the sign-in intent.
        // We listen to the response of this activity with the
        // SIGN_IN_REQUEST_CODE.
        startActivityForResult(
            AuthUI.getInstance()
                .createSignInIntentBuilder()
                .setAvailableProviders(providers)
                .build(),
            1001
        )
    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)
        if (requestCode == 1001) {
            val response = IdpResponse.fromResultIntent(data)
            if (resultCode == Activity.RESULT_OK) {
                // User successfully signed in.
                Log.i("MSG", "Successfully signed in user ${FirebaseAuth.getInstance().currentUser?.displayName}!")
                launchDevicesActivity(View(applicationContext), FirebaseAuth.getInstance().currentUser?.displayName.toString())
            } else {
                // Sign in failed. If response is null, the user canceled the
                // sign-in flow using the back button. Otherwise, check
                // the error code and handle the error.
                Log.i("MSG", "Sign in unsuccessful ${response?.error?.errorCode}")
            }
        }
    }

    fun launchDevicesActivity(view: View, user: String) {
        val intent = Intent(this, DevicesActivity::class.java)
        intent.putExtra("Name", user)
        startActivity(intent)

    }


}