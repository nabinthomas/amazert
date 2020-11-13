package com.e.amaze

import android.content.Context
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
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

    inner class StatusViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        val statusNameView: TextView = itemView.findViewById(R.id.textViewDeviceStatus)
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): StatusViewHolder {
        val itemView = inflater.inflate(R.layout.recyclerview_status, parent, false)
        return StatusViewHolder(itemView)
    }

    override fun onBindViewHolder(holder: StatusViewHolder, position: Int) {
        val current = items[position]
        holder.statusNameView.text = current
        holder.statusNameView.setOnClickListener{
            /*
            val intent = Intent(context, UpdateSettings::class.java)
            intent.putExtra("Name", current.name.toString())
            intent.putExtra("DisplayName", holder.statusNameView.text.toString())

            intent.putExtra("Value", current.value.toString())
            intent.putExtra("dbIndex", position.toString())
            context.startActivity(intent)
             */
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
        //Log.d(TAG, " VALUE: " + ds.value.toString())

        val dbValue = ds.value as HashMap<String, Any>
        val statusKey = dbValue["name"]
        val statusValue = dbValue["value"]
        //Log.d(TAG, dbValue["name"].toString() )
        //Log.d(TAG, dbValue["value"].toString() )


        when(statusKey){
            "wifi.clients" -> {
                val splitList = statusValue.toString().split("{", "}")
                val count = splitList.size - 3
                var x:Int = 2
                clientMACList.clear()

                while (x < count ) {
                    val clientMac = splitList[x].split("'")[1]
                    val clientVal = "{" + splitList[x+1].toString() + "}"
                    //Log.d(TAG, "INFO: " + x + " " + clientVal)
                    val itemType = object : TypeToken<StatusClientInfo>() {}.type
                    var clientInfo: StatusClientInfo = Gson().fromJson(clientVal, itemType)

                    val client = StatusClientList(clientMac, clientInfo)
                    //MyApplication.Companion.ClientStatus.add(client)
                    clientMACList.add(clientMac)
                    x+=2
                }
                //MyApplication.Companion.ClientMACList.value = clientMACList
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
            else -> {
                Log.d(TAG, "WARNING: New Status Item [$statusKey]? ADD support!")
            }
        }
        //Log.d(TAG, "MAC - " + ClientList[0].MAC + " " + ClientList.size.toString())
    }
}