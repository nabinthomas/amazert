package com.e.amaze

import android.content.Context
import android.content.Intent
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import com.google.firebase.database.DataSnapshot
import com.google.firebase.database.ktx.getValue

//class SettingsAdapter internal constructor(
class SettingsAdapter(
    val context: Context //,
) : RecyclerView.Adapter<SettingsAdapter.SettingsViewHolder>() {

    private val inflater: LayoutInflater = LayoutInflater.from(context)
    private var items = emptyList<SettingItem>() // Cached copy of items
    private var pkgName: String = context.packageName

    inner class SettingsViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        val settingNameView: TextView = itemView.findViewById(R.id.textViewName)
        val settingValueView: TextView = itemView.findViewById(R.id.textViewValue)
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): SettingsViewHolder {
        val itemView = inflater.inflate(R.layout.recyclerview_settings, parent, false)
        return SettingsViewHolder(itemView)
    }

    override fun onBindViewHolder(holder: SettingsViewHolder, position: Int) {
        val current = items[position]
        var inputOptions: String = ""
        val filterList = MyApplication.Companion.globalSettingsList.filter { (key, value) -> key.equals(current.name.toString()) }
        if (filterList.isNotEmpty()) {
            holder.settingNameView.text = filterList[0].displayName.toString()
            inputOptions = filterList[0].inputOptions.toString()
        } else {
            holder.settingNameView.text = current.name.toString()
        }

        val (ivSpec, digest, cipher) = MyApplication.Companion.symEnc.segregateSettingData(current.value.toString())
        val decryptedValue = MyApplication.Companion.symEnc.decryptCipherText(cipher + digest, ivSpec)

        if (current.name == "wireless.wifinet0.disabled") {
            if (decryptedValue == "1"){
                holder.settingValueView.text = "OFF"
            } else{
                holder.settingValueView.text = "ON"
            }
        } else {
            holder.settingValueView.text = decryptedValue
        }

        if (current.name == "wireless.wifinet0.maclist") {
            MyApplication.Companion.macIndex = position.toString()
            MyApplication.Companion.macBannedList = decryptedValue.toString()
        }

        if (current.name == "wireless.wifinet0.disabled") {
            MyApplication.Companion.macDisableIndex = position.toString()
        }

        holder.settingNameView.setOnClickListener{
            val intent = Intent(context, UpdateSettings::class.java)
            intent.putExtra("Name", current.name.toString())
            intent.putExtra("DisplayName", holder.settingNameView.text.toString())
            intent.putExtra("Value", decryptedValue)
            intent.putExtra("inputOptions", inputOptions)
            intent.putExtra("dbIndex", position.toString())
            context.startActivity(intent)
        }

        holder.settingValueView.setOnClickListener{
            val intent = Intent(context, UpdateSettings::class.java)
            intent.putExtra("Name", current.name.toString())
            intent.putExtra("DisplayName", holder.settingNameView.text.toString())
            intent.putExtra("Value", decryptedValue)
            intent.putExtra("inputOptions", inputOptions)
            intent.putExtra("dbIndex", position.toString())
            context.startActivity(intent)
        }
    }

    internal fun setItems(snapshot: DataSnapshot) {
        val itemList:MutableList<SettingItem> = ArrayList()
        for (ds in snapshot.children) {
            val dbValue = ds.getValue<SettingItem>()
            if (dbValue != null) {
                itemList.add(dbValue)
            }
        }

        this.items = itemList
        notifyDataSetChanged()
    }

    override fun getItemCount() = items.size
}
