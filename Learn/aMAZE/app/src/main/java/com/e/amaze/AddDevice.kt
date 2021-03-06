package com.e.amaze

import android.content.res.AssetManager
import android.os.AsyncTask
import android.os.Bundle
import android.view.View
import android.widget.EditText
import androidx.appcompat.app.AppCompatActivity
import com.jcraft.jsch.ChannelExec
import com.jcraft.jsch.ChannelSftp
import com.jcraft.jsch.ChannelSftp.OVERWRITE
import com.jcraft.jsch.JSch
import java.io.*
import java.util.*


class AddDevice : AppCompatActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.add_device)
    //    SshTask().execute()
    }

    fun launchAddDeviceHandling(view: View) {

        val userName = findViewById<EditText>(R.id.editTextUsername)
        val deviceName = findViewById<EditText>(R.id.editTextDeviceName)
        val deviceIP = findViewById<EditText>(R.id.editTextDeviceIP)
        val passwd = findViewById<EditText>(R.id.editTextPassword)
        val deviceDetails = findViewById<EditText>(R.id.deviceDetails)
        var output:String = "None"

        class SshTask : AsyncTask<Void, Void, String>() {
            override fun doInBackground(vararg p0: Void?): String {
                output = executeRemoteCommand(userName.text.toString(), passwd.text.toString(), deviceIP.text.toString())
                print(output)
                return output
            }

            override fun onPostExecute(result: String) {
                //deviceDetails.setText(output.toString())
                deviceDetails.setText("ssh done")
                //deviceDetails.setText(Uri.parse("android.resource://com.e.amaze/" + R.raw.amazert).toString())
            }
        }

        class SftpTask : AsyncTask<Void, Void, String>() {
            override fun doInBackground(vararg p0: Void?): String {
                var am: AssetManager = getAssets()
                var inputStream: InputStream = am.open("amazert.pkg")

                output = executeCopyCommand(userName.text.toString(), passwd.text.toString(), deviceIP.text.toString(), 22, inputStream)
                print(output)
                return output
            }

            override fun onPostExecute(result: String) {
                //deviceDetails.setText(output.toString())
                deviceDetails.setText("Sftp Done, start Ssh ...")
                print("Sftp Done, start Ssh ...")
                SshTask().execute()
            }
        }

        class SshInitTask : AsyncTask<Void, Void, String>() {
            override fun doInBackground(vararg p0: Void?): String {
                output = executeRemoteInitCommand(userName.text.toString(), passwd.text.toString(), deviceIP.text.toString())
                print(output)
                return output
            }

            override fun onPostExecute(result: String) {
                deviceDetails.setText("Ssh Init Done, start sftp ...")
                print("Ssh Init Done, start sftp ...")
                SftpTask().execute()
            }
        }

        //SftpTask().execute()
        //SshTask().execute()
        SshInitTask().execute()

    }

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
    val outputStream = ByteArrayOutputStream()
    sshChannel.outputStream = outputStream

    sshChannel.connect(10000)
    // Execute command.
    sshChannel.setCommand("cd /tmp && tar -xvzf amazert.tar.gz && cd /tmp/amazert && ./install.sh")
    //sshChannel.setCommand("uname -a")
    sshChannel.connect()

    // Sleep needed in order to wait long enough to get result back.
    Thread.sleep(1_000)
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
