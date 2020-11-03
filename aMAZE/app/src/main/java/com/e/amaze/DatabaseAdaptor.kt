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

//class SettingsAdapter internal constructor(
class SettingsAdapter(
    val context: Context //,
) : RecyclerView.Adapter<SettingsAdapter.SettingsViewHolder>() {

    private val inflater: LayoutInflater = LayoutInflater.from(context)
    private var items = emptyList<SettingItem>() // Cached copy of items
    private var pkgName: String = context.packageName

    inner class SettingsViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        //val settingIndexView: TextView = itemView.findViewById(R.id.textViewIndex)
        val settingNameView: TextView = itemView.findViewById(R.id.textViewName)
        val settingValueView: TextView = itemView.findViewById(R.id.textViewValue)
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): SettingsViewHolder {
        val itemView = inflater.inflate(R.layout.recyclerview_settings, parent, false)
        return SettingsViewHolder(itemView)
    }

    override fun onBindViewHolder(holder: SettingsViewHolder, position: Int) {
        val current = items[position]
        //val current = SettingList[position]
        //holder.settingIndexView.text = position.toString()
        holder.settingNameView.text = current.name.toString()
        holder.settingValueView.text = current.value.toString()
/*
        holder.settingNameView.setOnClickListener{
            val itemPrice = current.price
            val itemCategory = current.category
            val itemStock = current.stock
            val intent = Intent(context, ProductDetailActivity::class.java)
            intent.putExtra("Name", itemName)
            intent.putExtra("Category", itemCategory)
            intent.putExtra("Price", itemPrice.toString())
            intent.putExtra("Stock", itemStock.toString())
            context.startActivity(intent)

             */
       // }
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