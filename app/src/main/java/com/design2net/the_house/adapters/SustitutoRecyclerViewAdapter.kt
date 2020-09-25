package com.design2net.the_house.adapters

import android.annotation.SuppressLint
import android.support.v7.widget.RecyclerView
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import com.design2net.the_house.R
import com.design2net.the_house.models.Producto
import com.squareup.picasso.Picasso
import kotlinx.android.synthetic.main.row_sustituto.view.*

class SustitutoRecyclerViewAdapter(private val mValues: ArrayList<Producto>, private val mListener: SustitutoRecyclerViewListener)
    : RecyclerView.Adapter<SustitutoRecyclerViewAdapter.ViewHolder>(){

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        val view = LayoutInflater.from(parent.context)
                .inflate(R.layout.row_sustituto, parent, false)

        return ViewHolder(view, mListener)
    }

    override fun onBindViewHolder(viewHolder: ViewHolder, position: Int) {
        viewHolder.setData(position)
    }

    override fun getItemCount(): Int = mValues.size

    inner class ViewHolder(private val mView: View, private val listener: SustitutoRecyclerViewListener)
        : RecyclerView.ViewHolder(mView) {

        @SuppressLint("SetTextI18n")
        fun setData(position: Int) {
            val item = mValues[position]

            mView.txtViewSustitutoNombre.text = item.description

            Picasso.get()
                    .load(item.imageUrl)
                    .into(mView.imgViewProductoSustituto)

            mView.imgViewEliminarSustituto.setOnClickListener {
                if (position != RecyclerView.NO_POSITION)
                    listener.onSustitutoDelete(position)
            }
        }
    }

    interface SustitutoRecyclerViewListener {
        fun onSustitutoDelete(position: Int)
    }
}