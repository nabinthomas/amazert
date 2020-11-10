package com.e.amaze

import android.content.Context
import android.content.res.AssetManager
import android.os.AsyncTask
import android.os.Bundle
import android.util.Log
import android.view.View
import android.widget.Button
import android.widget.EditText
import android.widget.ProgressBar
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.database.ktx.database
import com.google.firebase.ktx.Firebase
import com.google.gson.Gson
import com.jcraft.jsch.ChannelExec
import com.jcraft.jsch.ChannelSftp
import com.jcraft.jsch.ChannelSftp.OVERWRITE
import com.jcraft.jsch.JSch
import kotlinx.android.synthetic.main.add_device.*
import java.io.BufferedReader
import java.io.ByteArrayOutputStream
import java.io.InputStream
import java.io.InputStreamReader
import java.util.*


class AddDevice : AppCompatActivity() {

    private lateinit var uName: String
    private lateinit var uId: String
    private val database = Firebase.database.getReferenceFromUrl("https://amaze-id1.firebaseio.com/").database
    private lateinit var mContext: Context
    private lateinit var deviceName:EditText
    private lateinit var progressBar: ProgressBar

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.add_device)
    //    SshTask().execute()

        uName = intent.getStringExtra("Name")
        uId = intent.getStringExtra("Uid")
        mContext = this.applicationContext
        deviceName = findViewById<EditText>(R.id.editTextDeviceName)

        progressBar = findViewById<ProgressBar>(R.id.progressBar1)
        val addButton: Button = findViewById<Button>(R.id.addButton)
        val doneButton: Button = findViewById<Button>(R.id.doneButton)

        progressBar.visibility = View.INVISIBLE
        doneButton.visibility = View.INVISIBLE
    }

    fun launchAddDeviceHandling(view: View) {

        val userName = findViewById<EditText>(R.id.editTextUsername)
        val deviceName = findViewById<EditText>(R.id.editTextDeviceName)
        val deviceIP = findViewById<EditText>(R.id.editTextDeviceIP)
        val passwd = findViewById<EditText>(R.id.editTextPassword)
        val deviceDetails = findViewById<EditText>(R.id.deviceDetails)
        var output:String = "None"

        var devFile = deviceName.text.toString()
        //Log.d("ADD", "Existing file name "+ MyApplication.Companion.dev_name + " new file name $devFile")
        for (i in MyApplication.Companion.deviceList.value?.indices!!) {

            if (devFile.trim().toString() == MyApplication.Companion.deviceList.value!![i].trim().toString()) {
                Log.d("ADD", "Same name used")
                Toast.makeText(
                    this,
                    "Device with same name '" + deviceName.text.toString() + "' already exist, please try a different device name",
                    Toast.LENGTH_LONG
                ).show()
                return
            }
        }

        addButton.visibility = View.VISIBLE
        doneButton.visibility = View.INVISIBLE
        progressBar.visibility = View.VISIBLE

        class SshPostTask : AsyncTask<Void, Void, String>() {
            override fun doInBackground(vararg p0: Void?): String {
                output = executeRemotePostCommand(userName.text.toString(), passwd.text.toString(), deviceIP.text.toString())
                print(output)
                return output
            }

            override fun onPostExecute(result: String) {
                deviceDetails.setText("Cloud registration done.. started device service")
                print("Cloud registration done.. started device service")
                progressBar.visibility = View.GONE
                addButton.visibility = View.INVISIBLE
                doneButton.visibility = View.VISIBLE
            }
        }

        class SftpGetTask : AsyncTask<Void, Void, String>() {

            override fun doInBackground(vararg p0: Void?): String {

                output = executeGetCommand(userName.text.toString(), passwd.text.toString(), deviceIP.text.toString(), 22)
                print(output)
                return output
            }

            override fun onPostExecute(result: String) {
                //deviceDetails.setText(output.toString())
                deviceDetails.setText(result)
                Log.d("ADD","Sftp Get Done")
                registerDeviceToCloud()
                deviceDetails.setText("Device registration done Successfully")

                //mContext = MyApplication.Companion.context
//                var fPath = mContext.applicationInfo.dataDir
//                Log.d("ADD", "PATH $fPath")

//                var device = deviceName.text.toString()
//                var fName = "$fPath/$device.dev"
//                mContext?.let { FileCrypt().encryptFile(it, fName, result) }

//                MyApplication.Companion.dev_name = "$device.dev"
//                Log.d("ADD", "Dev reg file created "+ MyApplication.Companion.dev_name)

                SshPostTask().execute()
            }
        }
        class SshTask : AsyncTask<Void, Void, String>() {
            override fun doInBackground(vararg p0: Void?): String {
                deviceDetails.setText("Secure Device software installation started ...")
                output = executeRemoteCommand(userName.text.toString(), passwd.text.toString(), deviceIP.text.toString())
                print(output)
                return output
            }

            override fun onPostExecute(result: String) {
                //deviceDetails.setText(output.toString())
                deviceDetails.setText("Secure Device installation done")
                //deviceDetails.setText(Uri.parse("android.resource://com.e.amaze/" + R.raw.amazert).toString())
                SftpGetTask().execute()
            }
        }

        class SftpTask : AsyncTask<Void, Void, String>() {
            override fun doInBackground(vararg p0: Void?): String {
                var am: AssetManager = getAssets()
                var inputStream: InputStream = am.open("amazert.pkg")

                deviceDetails.setText("Starting secure copy of device software package")

                output = executeCopyCommand(userName.text.toString(), passwd.text.toString(), deviceIP.text.toString(), 22, inputStream)
                print(output)
                return output
            }

            override fun onPostExecute(result: String) {
                //deviceDetails.setText(output.toString())
                deviceDetails.setText("Secure copy of package done")
                print("Sftp Done, start Ssh ...")
                SshTask().execute()
            }
        }

        class SshInitTask : AsyncTask<Void, Void, String>() {
            override fun doInBackground(vararg p0: Void?): String {
                deviceDetails.setText("Installing supporting software packages")
                output = executeRemoteInitCommand(userName.text.toString(), passwd.text.toString(), deviceIP.text.toString())
                print(output)
                return output
            }

            override fun onPostExecute(result: String) {
                deviceDetails.setText("Support software installation done")
                print("Ssh Init Done, start sftp ...")
                SftpTask().execute()
            }
        }

        //SftpTask().execute()
        //SshTask().execute()
        SshInitTask().execute()

    }

    fun launchAddDeviceDone(view: View) {
        onBackPressed()
    }

    private fun registerDeviceToCloud() {
        var userId = FirebaseAuth.getInstance().currentUser?.uid
        //var deviceId = "532e8c40-18cd-11eb-a4ca-dca6328f80c0" // Unique_DeviceUID
        var deviceId = MyApplication.Companion.register.deviceId

        val refPrefix = "/users/$userId/$deviceId/identifier/email"
        val devRef = database.getReference(refPrefix)
        devRef.setValue(FirebaseAuth.getInstance().currentUser?.email)
    }

    fun executeRemoteInitCommand(username: String,
                                 password: String,
                                 hostname: String,
                                 port: Int = 22): String {
        val jsch = JSch()
        val session = jsch.getSession(username, hostname, port)
        session.setPassword(password)

        // Avoid asking for key confirmation.
        val properties = Properties()
        properties.put("StrictHostKeyChecking", "no")
        session.setConfig(properties)

        session.connect()

        // Create SSH Channel.
        val sshChannel = session.openChannel("exec") as ChannelExec
        val outputStream = ByteArrayOutputStream()
        sshChannel.outputStream = outputStream

        // Execute command.
        sshChannel.setCommand("opkg update && opkg install openssh-sftp-server")
        //sshChannel.setCommand("uname -a")
        sshChannel.connect()

        // Sleep needed in order to wait long enough to get result back.
        Thread.sleep(1_000)
        sshChannel.disconnect()

        session.disconnect()

        return outputStream.toString()
    }

    fun executeRemotePostCommand(username: String,
                                 password: String,
                                 hostname: String,
                                 port: Int = 22): String {
        val jsch = JSch()
        val session = jsch.getSession(username, hostname, port)
        session.setPassword(password)

        // Avoid asking for key confirmation.
        val properties = Properties()
        properties.put("StrictHostKeyChecking", "no")
        session.setConfig(properties)

        session.connect()

        // Create SSH Channel.
        val sshChannel = session.openChannel("exec") as ChannelExec
        val outputStream = ByteArrayOutputStream()
        sshChannel.outputStream = outputStream

        // Execute command.
        sshChannel.setCommand("/etc/init.d/amazert start")
        sshChannel.connect()

        // Sleep needed in order to wait long enough to get result back.
        Thread.sleep(1_000)
        sshChannel.disconnect()

        session.disconnect()

        return outputStream.toString()
    }

    fun executeRemoteCommand(username: String,
                             password: String,
                             hostname: String,
                             port: Int = 22): String {
        val jsch = JSch()
        val session = jsch.getSession(username, hostname, port)
        session.setPassword(password)

        // Avoid asking for key confirmation.
        val properties = Properties()
        properties.put("StrictHostKeyChecking", "no")
        session.setConfig(properties)

        session.connect()

        // Create SSH Channel.
        val sshChannel = session.openChannel("exec") as ChannelExec
        //val sshChannel = session.openChannel("shell") as ChannelShell
        val outputStream = ByteArrayOutputStream()
        sshChannel.outputStream = outputStream

        sshChannel.connect(300000)
        // Execute command.
        //TODO - get email id from previous context
        val cmd =
            "cd /tmp && tar -xvzf amazert.tar.gz && cd /tmp/amazert && chmod 744 install.sh && ./install.sh && python3 init.py $uName $uId"
        Log.d("CMD","cmd "+ cmd)
        sshChannel.setCommand(cmd)
        //sshChannel.setCommand("uname -a")
        sshChannel.connect()

        // Sleep needed in order to wait long enough to get result back.
        Thread.sleep(45_000)
        sshChannel.disconnect()

        session.disconnect()

        return outputStream.toString()
    }

    fun executeCopyCommand(username: String,
                           password: String,
                           hostname: String,
                           port: Int = 22, inputStream: InputStream): String {
        val jsch = JSch()
        val session = jsch.getSession(username, hostname, port)
        session.setPassword(password)

        // Avoid asking for key confirmation.
        val properties = Properties()
        properties.put("StrictHostKeyChecking", "no")
        session.setConfig(properties)

        session.connect()

        // Create SSH Channel.
        val sftpChannel = session.openChannel("sftp") as ChannelSftp
        //val outputStream = ByteArrayOutputStream()
        //sftpChannel.outputStream = outputStream

        sftpChannel.connect(10000)
        //sftpChannel.put("I:/demo/myOutFile.txt", "/tmp/myOutFile.zip", 1 )
        sftpChannel.put(inputStream, "/tmp/amazert.tar.gz", OVERWRITE)

        // Sleep needed in order to wait long enough to get result back.
        Thread.sleep(1_000)
        sftpChannel.disconnect()

        session.disconnect()

        //return outputStream.toString()
        return "Sftp Done"
    }

    fun executeGetCommand(username: String,
                          password: String,
                          hostname: String,
                          port: Int = 22): String {
        val jsch = JSch()
        val session = jsch.getSession(username, hostname, port)
        session.setPassword(password)

        // Avoid asking for key confirmation.
        val properties = Properties()
        properties.put("StrictHostKeyChecking", "no")
        session.setConfig(properties)

        session.connect()

        // Create SSH Channel.
        val sftpChannel = session.openChannel("sftp") as ChannelSftp

        sftpChannel.connect(10000)

        val remoteFile = "/etc/amazert.json"

        val iStream: InputStream = sftpChannel.get(remoteFile)

        val response = BufferedReader(
            InputStreamReader(iStream, "UTF-8")
        ).use { it.readText() }

        var fPath = mContext.applicationInfo.dataDir
        Log.d("ADD", "PATH $fPath")

        var device = deviceName.text.toString()
        var fName = "$fPath/$device.dev"
        mContext?.let { FileCrypt().encryptFile(it, fName, response) }

        //MyApplication.Companion.deviceList.value?.plus(device)

        var deviceList: MutableList <String> = ArrayList()
        for (i in MyApplication.Companion.deviceList.value?.indices!!) {

            deviceList.add(MyApplication.Companion.deviceList.value!![i].trim().toString())
        }

        deviceList.add(device.toString())

        //MyApplication.Companion.deviceList.value = deviceList
        MyApplication.Companion.deviceList.postValue(deviceList)

        MyApplication.Companion.dev_name = "$device.dev"
        Log.d("ADD", "Dev reg file created "+ MyApplication.Companion.dev_name)

        val registerDev = Gson().fromJson(response,Register::class.java)

        MyApplication.Companion.register.deviceId = registerDev.deviceId
        MyApplication.Companion.register.email = registerDev.email
        MyApplication.Companion.register.registrationId = registerDev.registrationId
        MyApplication.Companion.register.uid = registerDev.uid

        // set local file
        //val tFile = FileOutputStream("amazert.json")
        //val tFile = FileOutputStream(File("amazert.json"))

        // read local file contents
        //var c: Int
        //while (iStream.read().also { c = it } != -1) {
        //    tFile.write(c)
        //}

        iStream.close()
        //tFile.close()

        // Sleep needed in order to wait long enough to get result back.
        Thread.sleep(1_000)
        sftpChannel.disconnect()

        session.disconnect()

        //return outputStream.toString()
        return registerDev.toString()

    }

}