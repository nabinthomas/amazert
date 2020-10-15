package com.e.amaze

import android.graphics.Color
import android.os.AsyncTask
import android.os.Bundle
import android.view.View
import android.widget.EditText
import android.widget.TextView
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import com.jcraft.jsch.ChannelExec
import com.jcraft.jsch.ChannelSftp
import com.jcraft.jsch.JSch
import java.io.ByteArrayOutputStream
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
                deviceDetails.setText(output.toString())
            }
        }

        class SftpTask : AsyncTask<Void, Void, String>() {
            override fun doInBackground(vararg p0: Void?): String {
                output = executeCopyCommand(userName.text.toString(), passwd.text.toString(), deviceIP.text.toString())
                print(output)
                return output
            }

            override fun onPostExecute(result: String) {
                deviceDetails.setText(output.toString())
                SshTask().execute()
            }
        }

        SftpTask().execute()

    }

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

    // Execute command.
    sshChannel.setCommand("tar -xvzf /tmp/amazert.tar.gz && cd /tmp/amazert && ./install.sh")
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
    val outputStream = ByteArrayOutputStream()
    sftpChannel.outputStream = outputStream

    sftpChannel.connect()
    //sftpChannel.put("I:/demo/myOutFile.txt", "/tmp/myOutFile.zip", 1 )
    sftpChannel.put(R.raw.amazert.toString(), "/tmp/amazert.tar.gz", 1 )

    // Sleep needed in order to wait long enough to get result back.
    Thread.sleep(1_000)
    sftpChannel.disconnect()

    session.disconnect()

    return outputStream.toString()
}