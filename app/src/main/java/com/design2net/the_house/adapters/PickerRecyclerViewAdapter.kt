package com.design2net.the_house.adapters

import android.annotation.SuppressLint
import android.support.v7.widget.RecyclerView
import android.text.Html
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import com.design2net.the_house.R
import com.design2net.the_house.models.Producto
import com.squareup.picasso.Picasso
import kotlinx.android.synthetic.main.row_picker.view.*


class PickerRecyclerViewAdapter(private val mValues: ArrayList<Producto>, private val mListener: PickerRecyclerViewListener)
    : RecyclerView.Adapter<PickerRecyclerViewAdapter.ViewHolder>() {

    interface PickerRecyclerViewListener {
        fun onProductoClick(position: Int)
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        val view = LayoutInflater.from(parent.context)
                .inflate(R.layout.row_picker, parent, false)
        return ViewHolder(view, mListener)
    }

    override fun onBindViewHolder(viewHolder: ViewHolder, position: Int) {
        viewHolder.setData(position)
    }

    override fun getItemCount(): Int = mValues.size

    inner class ViewHolder(private val mView: View, private val listener: PickerRecyclerViewListener )
        : RecyclerView.ViewHolder(mView) {

        @SuppressLint("SetTextI18n")
        fun setData(position: Int) {
            val item = mValues[position]

            mView.txtViewTitle.text = item.description
            mView.txtViewUpcProduct.text = "UPC: ${item.upcs.joinToString(", ")}"
            mView.txtViewQty.text = "Qty: ${item.orderQty}"
            mView.txtViewPick.text = "Pick: ${item.pickQty}"
            mView.txtViewPasillo.text = Html.fromHtml("<b>Aisle:</b> ${item.aisle}")

            if (item.netAvailableFlag == 1) {
                mView.imgViewNetAvailableFlag2.visibility = View.VISIBLE
                mView.txtViewNetAvailableFlag.visibility = View.VISIBLE
            } else {
                mView.imgViewNetAvailableFlag2.visibility = View.GONE
                mView.txtViewNetAvailableFlag.visibility = View.GONE
            }

            Picasso.get()
                    .load(item.imageUrl)
                    .into(mView.imgViewProducto)

            mView.setOnClickListener {
                if (position != RecyclerView.NO_POSITION)
                    listener.onProductoClick(position)
            }
        }
    }
}