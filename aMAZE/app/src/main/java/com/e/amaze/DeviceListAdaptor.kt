package com.e.amaze

import android.content.Context
import android.content.Intent
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView

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
                val intent = Intent(context, DeviceSettingActivity::class.java)
                intent.putExtra("Name", current)
                context.startActivity(intent)
            }
        }

        internal fun setItems(items: List<String>) {
            this.items = items
            notifyDataSetChanged()
        }

        override fun getItemCount() = items.size
}