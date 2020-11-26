package com.e.amaze

import android.content.Context
import android.util.Log
import android.view.Gravity
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Button
import android.widget.TextView
import android.widget.Toast
import androidx.recyclerview.widget.RecyclerView
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.database.DataSnapshot
import com.google.firebase.database.ktx.getValue
import com.google.gson.Gson
import com.google.gson.reflect.TypeToken


class StatusAdapter(
    val context: Context //,
) : RecyclerView.Adapter<StatusAdapter.StatusViewHolder>() {
    private val TAG = "StatusAdapter"
    private val inflater: LayoutInflater = LayoutInflater.from(context)
    private var items = emptyList<String>() // Cached copy of items
    private var pkgName: String = context.packageName
    private var clientMACList: MutableList <String> = java.util.ArrayList()
    private val database = MyApplication.Companion.projectDatabase

    inner class StatusViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        val statusNameView: TextView = itemView.findViewById(R.id.textViewDeviceStatus)
        val macBanButton:Button = itemView.findViewById(R.id.button7)
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): StatusViewHolder {
        val itemView = inflater.inflate(R.layout.recyclerview_status, parent, false)
        return StatusViewHolder(itemView)
    }

    override fun onBindViewHolder(holder: StatusViewHolder, position: Int) {
        val current = items[position]
        holder.statusNameView.text = current
        if (MyApplication.Companion.macBannedList.contains(current.toString())){
            holder.macBanButton.setText("ALLOW")
        } else {
            holder.macBanButton.setText("BLOCK")
        }

        holder.macBanButton.setOnClickListener{
            if (holder.macBanButton.text == "BLOCK") {
                //handleMacBanOperation(holder)
                blockMac(holder)
            } else {
                handleMacAllowOperation(holder)
            }

            val splitList = MyApplication.Companion.macBannedList.toString().split("'", " ")
        }
    }

    private fun handleMacAllowOperation(holder: StatusViewHolder) {
        /*
        var banMacValue:String = ""
        if (MyApplication.Companion.macBannedList != "")    {
            if (MyApplication.Companion.macBannedList.contains(holder.statusNameView.text.toString())) {
                Toast.makeText(
                    context,
                    "MAC Address already in BAN list: " + MyApplication.Companion.macBannedList.toString(),
                    Toast.LENGTH_SHORT
                ).show()
            } else {
                banMacValue =
                    MyApplication.Companion.macBannedList.plus(" " + holder.statusNameView.text.toString())
                MyApplication.Companion.macBannedList = banMacValue
            }
        } else {
            banMacValue = holder.statusNameView.text.toString()
            MyApplication.Companion.macBannedList = banMacValue
        }

        if (banMacValue != "" ) {
            // Encrypt value using SymKey Enc
            var encryptedValue = MyApplication.Companion.symEnc.encryptString(banMacValue)
            val userId = FirebaseAuth.getInstance().currentUser?.uid
            var deviceId: String = MyApplication.Companion.register.deviceId
            if (MyApplication.Companion.register.deviceId === "") {
                deviceId = "532e8c40-18cd-11eb-a4ca-dca6328f80c0"
            }
            var databaseIndex = MyApplication.Companion.macIndex
            var settingPath = "users/$userId/$deviceId/settings/$databaseIndex/name"
            var valPath = "users/$userId/$deviceId/settings/$databaseIndex/value"

            var nameRef = database.getReference(settingPath.toString())
            nameRef.setValue("wireless.wifinet0.maclist")
            var valRef = database.getReference(valPath.toString())
            valRef.setValue(encryptedValue)

            encryptedValue = MyApplication.Companion.symEnc.encryptString("Deny")
            databaseIndex = MyApplication.Companion.macDisableIndex
            settingPath = "users/$userId/$deviceId/settings/$databaseIndex/name"
            valPath = "users/$userId/$deviceId/settings/$databaseIndex/value"

            nameRef = database.getReference(settingPath.toString())
            nameRef.setValue("wireless.wifinet0.disabled")
            valRef = database.getReference(valPath.toString())
            valRef.setValue(encryptedValue)

            Toast.makeText(
                context,
                "MAC Address added to BAN list" ,
                Toast.LENGTH_SHORT
            ).show()
        }
         */
        Toast.makeText(
            context,
            "TO BE IMPL..." ,
            Toast.LENGTH_SHORT
        ).show()
    }

    private fun blockMac(holder: StatusViewHolder ) {
        val userId = FirebaseAuth.getInstance().currentUser?.uid
        var deviceId: String = MyApplication.Companion.register.deviceId
        if (MyApplication.Companion.register.deviceId === "") {
            deviceId = "532e8c40-18cd-11eb-a4ca-dca6328f80c0" }

        val dbCommandPath = "users/$userId/$deviceId/request"
        val actionPath = dbCommandPath.plus("/action")
        val cmdPath = dbCommandPath.plus("/command")
        val actionRef = database.getReference(actionPath)
        val cmdRef = database.getReference(cmdPath)

        val blockCmd:String = "[\"banclient.sh\", \"" + holder.statusNameView.text.toString() + "\", \"3600000\"]"
Log.d(TAG, "BLOCK cmd --" + blockCmd.toString() + "--")
        val encryptedValue = MyApplication.Companion.symEnc.encryptString(blockCmd)

        actionRef.setValue("command")
        cmdRef.setValue(encryptedValue)
        Toast.makeText(
            context,
            "MAC Address disconnected for 1 hour",
            Toast.LENGTH_SHORT
        ).show()
        /*
        val location = IntArray(2)
        findViewById<TextView>(R.id.textView12).getLocationOnScreen(location)
        val toast = Toast.makeText(applicationContext,"Device REBOOT triggered",Toast.LENGTH_SHORT)
        toast.setGravity(Gravity.TOP or Gravity.LEFT, location [0]+200, location[1])
        toast.show()
        */
    }
    private fun handleMacBanOperation(holder: StatusViewHolder) {
        var banMacValue:String = ""
        if (MyApplication.Companion.macBannedList != "")    {
            if (MyApplication.Companion.macBannedList.contains(holder.statusNameView.text.toString())) {
                Toast.makeText(
                    context,
                    "MAC Address already in BAN list: " + MyApplication.Companion.macBannedList.toString(),
                    Toast.LENGTH_SHORT
                ).show()
            } else {
                banMacValue =
                    MyApplication.Companion.macBannedList.plus(" '" + holder.statusNameView.text.toString()) + "'"
                MyApplication.Companion.macBannedList = banMacValue
            }
        } else {
            banMacValue = "'" + holder.statusNameView.text.toString() + "'"
            MyApplication.Companion.macBannedList = banMacValue
        }

        if (banMacValue != "" ) {
            // Encrypt value using SymKey Enc
            var encryptedValue = MyApplication.Companion.symEnc.encryptString(banMacValue)
            val userId = FirebaseAuth.getInstance().currentUser?.uid
            var deviceId: String = MyApplication.Companion.register.deviceId
            if (MyApplication.Companion.register.deviceId === "") {
                deviceId = "532e8c40-18cd-11eb-a4ca-dca6328f80c0"
            }
            var databaseIndex = MyApplication.Companion.macIndex
            var settingPath = "users/$userId/$deviceId/settings/$databaseIndex/name"
            var valPath = "users/$userId/$deviceId/settings/$databaseIndex/value"

            var nameRef = database.getReference(settingPath.toString())
            nameRef.setValue("wireless.wifinet0.maclist")
            var valRef = database.getReference(valPath.toString())
            valRef.setValue(encryptedValue)

            encryptedValue = MyApplication.Companion.symEnc.encryptString("Deny")
            databaseIndex = MyApplication.Companion.macDisableIndex
            settingPath = "users/$userId/$deviceId/settings/$databaseIndex/name"
            valPath = "users/$userId/$deviceId/settings/$databaseIndex/value"

            nameRef = database.getReference(settingPath.toString())
            nameRef.setValue("wireless.wifinet0.disabled")
            valRef = database.getReference(valPath.toString())
            valRef.setValue(encryptedValue)

            Toast.makeText(
                context,
                "MAC Address added to BAN list" ,
                Toast.LENGTH_SHORT
            ).show()

            holder.macBanButton.setText("ALLOW")
        }
    }


    internal fun setItems(snapshot: DataSnapshot) {
        for (ds in snapshot.children) {
            processDeviceStatus(ds)
        }
        this.items = clientMACList
        notifyDataSetChanged()
    }

    override fun getItemCount() = items.size


    private fun processDeviceStatus(ds:DataSnapshot){
        val key = ds.key
        val value = ds.value

        Log.d(TAG, " Status Item: " + key)

        val dbValue = ds.value as HashMap<String, Any>
        val statusKey = dbValue["name"]
        val statusValue = dbValue["value"]

        when(statusKey){
            "wifi.clients" -> {
                val splitList = statusValue.toString().split("{", "}")
                val count = splitList.size - 3
                var x:Int = 2
                clientMACList.clear()

                while (x < count ) {
                    var clientMac = splitList[x].split("=")[0]
                    val tmp = clientMac.split(",")
                    if (tmp.size == 2 ) {
                        clientMac = tmp[1].toString()
                    }
                    clientMACList.add(clientMac)
                    x+=2
                    Log.d(TAG, "Clients: ITEM: " + clientMac.toString())
                }
            }
            "amazert.heartbeat.time" -> {
                Log.d(TAG, "RCVD $statusKey  == $statusValue")
                MyApplication.Companion.HbTime = statusValue.toString()
            }
            "amazert.poweron.time" -> {
                Log.d(TAG, "RCVD $statusKey == $statusValue")
                MyApplication.Companion.PowerOnTime = statusValue.toString()
            }
            "amazert.status" -> {
                Log.d(TAG, "RCVD $statusKey == $statusValue")
                MyApplication.Companion.DeviceStatus = statusValue.toString()
            }
            "dhcp.leases" -> {
                Log.d(TAG, "RCVD $statusKey == $statusValue")
                //var DhcpLeasesType = object :TypeToken<StatusDhcpLeases>() {}.type
                //var dhcpLeases: StatusDhcpLeases = Gson().fromJson(statusValue.toString(), DhcpLeasesType)
                //Log.d(TAG, dhcpLeases.dhcp_leases.toString())
            }
            else -> {
                Log.d(TAG, "WARNING: New Status Item [$statusKey]? ADD support!")
            }
        }
        //Log.d(TAG, "MAC - " + ClientList[0].MAC + " " + ClientList.size.toString())
    }
}