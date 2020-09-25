package com.design2net.the_house.adapters

import android.support.v7.widget.RecyclerView
import android.text.Editable
import android.text.TextWatcher
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import com.design2net.the_house.R

import com.design2net.the_house.models.Producto
import kotlinx.android.synthetic.main.row_bag_details.view.*
import kotlinx.android.synthetic.main.row_bag_details.view.edt_product_checkQty

class BagDetailsAdapter(private val mValues: ArrayList<Producto>, private val mListener: OnProductListener)
    : RecyclerView.Adapter<BagDetailsAdapter.ViewHolder>() {

    interface OnProductListener {
        fun onProductClick(position: Int)
        fun onRemoveProductClick(position: Int)
        fun onTextChange(position: Int, value: Int)
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        val view = LayoutInflater.from(parent.context)
                .inflate(R.layout.row_bag_details, parent, false)
        return ViewHolder(view, mListener)
    }

    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        val item = mValues[position]

        holder.setData(item)
    }

    override fun getItemCount(): Int = mValues.size

    inner class ViewHolder(private val mView: View, private val listener: OnProductListener ) : RecyclerView.ViewHolder(mView) {

        fun setData(item: Producto) {
            mView.txtViewDescription.text = item.description
            mView.edt_product_checkQty.setText(item.checkQty.toString())

            mView.edt_product_checkQty.addTextChangedListener(object : TextWatcher {
                override fun afterTextChanged(p0: Editable?) {
                    val position = adapterPosition
                    if (position != RecyclerView.NO_POSITION) {
                        listener.onTextChange(position, if(mView.edt_product_checkQty.text.toString().isEmpty()) 0 else mView.edt_product_checkQty.text.toString().toInt())
                    }
                }

                override fun beforeTextChanged(p0: CharSequence?, p1: Int, p2: Int, p3: Int) {
                }

                override fun onTextChanged(p0: CharSequence?, p1: Int, p2: Int, p3: Int) {
                }
            })

            mView.imgBtn_remove.setOnClickListener {
                val position = adapterPosition
                if (position != RecyclerView.NO_POSITION) {
                    listener.onRemoveProductClick(position)
                }
            }

            mView.setOnClickListener{
                val position = adapterPosition
                if (position != RecyclerView.NO_POSITION) {
                    listener.onProductClick(position)
                }
            }
        }

        override fun toString(): String {
            return super.toString() + " '" + mView.txtViewDescription.text + "'"
        }
    }
}
