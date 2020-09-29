package com.design2net.the_house.adapters

import android.annotation.SuppressLint
import android.support.v7.widget.RecyclerView
import android.text.Html
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import com.design2net.the_house.R
import com.design2net.the_house.models.Producto
import kotlinx.android.synthetic.main.row_chat.view.*
import kotlinx.android.synthetic.main.row_picked_upc.view.*

class PickedItemsRecyclerViewAdapter(private val mValues: ArrayList<Producto>)
    : RecyclerView.Adapter<PickedItemsRecyclerViewAdapter.ViewHolder>() {

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        val view = LayoutInflater.from(parent.context)
                .inflate(R.layout.row_picked_upc, parent, false)
        return ViewHolder(view)
    }

    override fun onBindViewHolder(viewHolder: ViewHolder, position: Int) {
        viewHolder.setData(position)
    }

    override fun getItemCount(): Int = mValues.size

    inner class ViewHolder(private val mView: View )
        : RecyclerView.ViewHolder(mView) {

        @SuppressLint("SetTextI18n", "NewApi")
        fun setData(position: Int) {
            val item = mValues[position]

            mView.txtViewPickedUpc.text = Html.fromHtml("<b>UPC:</b> ${item.upcStr}", Html.FROM_HTML_MODE_COMPACT)
            mView.txtViewPickedQty.text = Html.fromHtml("<b>QTY:</b> ${item.pickQty}", Html.FROM_HTML_MODE_COMPACT)
            mView.txtViewPickerUsuario.text = Html.fromHtml("<b>USUARIO:</b> ${item.pickerUser}", Html.FROM_HTML_MODE_COMPACT)
            mView.txtViewPickedFecha.text = Html.fromHtml("<b>FECHA:</b> ${item.dateTimePicked}", Html.FROM_HTML_MODE_COMPACT)
        }
    }
}