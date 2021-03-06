package com.e.amaze

import android.app.Activity
import android.app.Application
import android.content.Context
import android.content.Intent
import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.graphics.drawable.BitmapDrawable
import android.os.AsyncTask
import android.os.Bundle
import android.util.Log
import android.view.View
import androidx.appcompat.app.AppCompatActivity
import androidx.appcompat.content.res.AppCompatResources
import androidx.appcompat.content.res.AppCompatResources.getDrawable
import androidx.core.graphics.drawable.toBitmap
import androidx.lifecycle.MutableLiveData
import com.firebase.ui.auth.AuthUI
import com.firebase.ui.auth.IdpResponse
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.database.ktx.database
import com.google.firebase.ktx.Firebase
import com.google.gson.Gson
import com.google.gson.JsonSyntaxException
import com.google.gson.reflect.TypeToken
import java.io.BufferedReader
import java.io.File
import java.io.InputStream
import java.io.InputStreamReader
import java.net.URL
import java.util.*

class MyApplication : Application() {

    companion object {
        var context: Context? = null
        var register: Register = Register("","","","")
        var dev_name: String = ""
        lateinit var globalSettingsList:List<SettingDetails>
        val projectDatabase = Firebase.database.getReferenceFromUrl("https://amaze-id1.firebaseio.com/").database
        var deviceList: MutableLiveData<List<String>> = MutableLiveData<List<String>>()
        var symEnc:SymKeyEncryption = SymKeyEncryption()
        lateinit var bitmap: Bitmap
        lateinit var HbTime:String
        lateinit var PowerOnTime:String
        lateinit var DeviceStatus:String
        lateinit var macIndex:String
        lateinit var macDisableIndex:String
        var macBannedList:String = ""

        fun  updateFeatureMapping(applicationContext: Context ) {
            val mContext: Context = applicationContext
            val iStream: InputStream = mContext.getAssets().open("settings.json")
            val response = BufferedReader(
                InputStreamReader(iStream, "UTF-8")
                ).use { it.readText() }
            //Log.d("JSON read response... [ ", "$response"+"  ]'")

            val itemType = object : TypeToken<List<SettingDetails>>() {}.type
            var out: List<SettingDetails> = Gson().fromJson(response, itemType)
            globalSettingsList = out
        }
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

        MyApplication.Companion.updateFeatureMapping(applicationContext)
        MyApplication.Companion.bitmap = AppCompatResources.getDrawable(applicationContext, (R.drawable.account))!!
            .toBitmap()

        var deviceName: String? = null
        var data:String = ""

        val listAllFiles = File(fPath).listFiles()

        var deviceList: MutableList <String> = ArrayList()

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
                    deviceList.add(currentFile.getName().substringBefore("."))
                }
            }

            MyApplication.Companion.deviceList.value = deviceList
        }

        Log.d("MAIN", "Dev list " + deviceList.toString())
        Log.d("MAIN", "Dev list Data " + MyApplication.Companion.deviceList.value.toString())
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

                class ProfileDownload :
                    AsyncTask<String?, Void?, Bitmap?>() {

                    protected override fun onPostExecute(bitmap: Bitmap?) {
                        //Populate Ui
                        if (bitmap != null) {
                            MyApplication.Companion.bitmap = bitmap
                        }
                        super.onPostExecute(bitmap)

                        launchDevicesActivity(View(applicationContext), FirebaseAuth.getInstance().currentUser?.displayName.toString())
                    }

                    protected override fun doInBackground(vararg params: String?): Bitmap {
                        // Open URL connection read bitmaps and return form here

                        val photoUrl = FirebaseAuth.getInstance().currentUser?.photoUrl

                        //val myBitmap: Bitmap =  photoUrl.
                        MyApplication.Companion.bitmap = BitmapFactory.decodeStream(URL(photoUrl.toString()).getContent() as InputStream)

                        return MyApplication.Companion.bitmap
                    }

                }

                ProfileDownload().execute()

//                launchDevicesActivity(View(applicationContext), FirebaseAuth.getInstance().currentUser?.displayName.toString())
            } else {
                // Sign in failed. If response is null, the user canceled the
                // sign-in flow using the back button. Otherwise, check
                // the error code and handle the error.
                Log.i("MSG", "Sign in unsuccessful ${response?.error?.errorCode}")
            }
        }
    }

    fun launchDevicesActivity(view: View, user: String) {
        //val intent = Intent(this, DevicesActivity::class.java)
	    val intent = Intent(this, DeviceListActivity::class.java)
        intent.putExtra("Name", user)
        startActivity(intent)
    }

}
