package com.github.kr328.ibr.adapters

import android.content.Context
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.BaseAdapter
import com.github.kr328.ibr.databinding.AdapterAppItemBinding

class AppItemAdapter(private val context: Context,
                     private val appInfoData: List<Item>) : BaseAdapter() {
    data class Item(val name: String, val packageName: String)

    override fun getItem(position: Int): Any {
        return appInfoData[position]
    }

    override fun getItemId(position: Int): Long {
        return appInfoData[position].packageName.hashCode().toLong()
    }

    override fun getCount(): Int {
        return appInfoData.size
    }

    override fun getView(position: Int, convertView: View?, parent: ViewGroup?): View {
        return (convertView
                ?: AdapterAppItemBinding.inflate(LayoutInflater.from(context), parent, false).root).also {
            val current = appInfoData[position]
            AdapterAppItemBinding.bind(it).apply {
                adapterAppItemIcon.setImageDrawable(context.packageManager.getApplicationIcon(current.packageName))
                adapterAppItemName.text = current.name
                adapterAppItemDescription.text = current.packageName
            }
        }
    }
}