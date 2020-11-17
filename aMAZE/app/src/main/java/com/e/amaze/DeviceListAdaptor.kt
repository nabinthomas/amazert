package com.e.amaze

import android.content.Context
import android.content.Intent
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import android.widget.Toast
import androidx.recyclerview.widget.RecyclerView
import com.google.gson.Gson
import com.google.gson.JsonSyntaxException

class DeviceListAdapter internal constructor(
    val context: Context
) : RecyclerView.Adapter<DeviceListAdapter.DeviceViewHolder>() {

        private val inflater: LayoutInflater = LayoutInflater.from(context)
        private var items = emptyList<String>() // Cached copy of items
        private var pkgName: String = context.packageName

        inner class DeviceViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
            val deviceView: TextView = itemView.findViewById(R.id.rdevice_name)
        }

        override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): DeviceViewHolder {
            val itemView = inflater.inflate(R.layout.recyclerview_device, parent, false)
            return DeviceViewHolder(itemView)
        }

        override fun onBindViewHolder(holder: DeviceViewHolder, position: Int) {
            val current = items[position]
            holder.deviceView.text = current

            holder.deviceView.setOnClickListener{
                var fPath = context.applicationInfo.dataDir

                var fName = fPath + "/" + current.toString() + ".dev"
                var data:String = ""
                Log.d("DEVLIST","Device name " + fName)
                data = FileCrypt().decryptFile(context, fName)
                Log.d("DEVLIST", "DATA    $data")

                try {
                    val registerDev = Gson().fromJson(data, Register::class.java)

                    MyApplication.Companion.register.deviceId = registerDev.deviceId
                    MyApplication.Companion.register.email = registerDev.email
                    MyApplication.Companion.register.registrationId = registerDev.registrationId
                    MyApplication.Companion.register.uid = registerDev.uid

                    Log.d(
                        "DEVLIST",
                        "Dev id: " + MyApplication.Companion.register.deviceId + " email: " + MyApplication.Companion.register.email + " uid: " +  MyApplication.Companion.register.uid
                    )
                } catch (e: JsonSyntaxException){
                    Log.d("DEVLIST", "Error in parsing Json")
                }

                val intent = Intent(context, DevicesActivity::class.java)
                intent.putExtra("Name", current)
                context.startActivity(intent)
            }

            holder.deviceView.setOnLongClickListener{

                Toast.makeText(context, "Long press",Toast.LENGTH_LONG).show()

                return@setOnLongClickListener true
            }
        }

        internal fun setItems(items: List<String>) {
            this.items = items
            notifyDataSetChanged()
        }

        override fun getItemCount() = items.size
}