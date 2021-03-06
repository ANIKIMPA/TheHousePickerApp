package com.design2net.the_house.adapters

import android.annotation.SuppressLint
import android.support.v7.widget.RecyclerView
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import com.design2net.the_house.R
import kotlinx.android.synthetic.main.row_aisle.view.*

class AislesRecyclerViewAdapter (private val mValues: ArrayList<String>)
: RecyclerView.Adapter<AislesRecyclerViewAdapter.ViewHolder>() {

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        val view = LayoutInflater.from(parent.context)
                .inflate(R.layout.row_aisle, parent, false)
        return ViewHolder(view)
    }

    override fun onBindViewHolder(viewHolder: ViewHolder, position: Int) {
        viewHolder.setData(position)
    }

    override fun getItemCount(): Int = mValues.size

    inner class ViewHolder(private val mView: View) : RecyclerView.ViewHolder(mView) {

        @SuppressLint("SetTextI18n")
        fun setData(position: Int) {
            val item = mValues[position]
            mView.txtViewAisle.text = item
        }
    }
}