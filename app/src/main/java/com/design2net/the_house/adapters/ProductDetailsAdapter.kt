package com.design2net.the_house.adapters

import android.annotation.SuppressLint
import android.support.v7.widget.RecyclerView
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import com.design2net.the_house.R


import com.design2net.the_house.models.Bag
import kotlinx.android.synthetic.main.numeric_up_down.view.*
import kotlinx.android.synthetic.main.row_product_details.view.*


class ProductDetailsAdapter(
        private val mValues: List<Bag>, private val mListener: OnBagListener)
    : RecyclerView.Adapter<ProductDetailsAdapter.ViewHolder>() {

    interface OnBagListener {
        fun onDeleteClick(position: Int)
        fun onNumericUpButtonClick(position: Int)
        fun onNumericDownButtonClick(position: Int)
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        val view = LayoutInflater.from(parent.context)
                .inflate(R.layout.row_product_details, parent, false)
        return ViewHolder(view, mListener)
    }

    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        holder.setData(position)
    }

    override fun getItemCount(): Int = mValues.size

    inner class ViewHolder(private val mView: View, private val listener: OnBagListener ) : RecyclerView.ViewHolder(mView) {

        init {
            mView.numericUpDown.setMinValue(0)
        }

        @SuppressLint("SetTextI18n")
        fun setData(position: Int) {
            val item = mValues[position]
            val cuerrentValue = item.currentQty
            mView.numericUpDown.value = cuerrentValue
            mView.txtViewBagId.text = "Bag #" + item.id

            if (position != RecyclerView.NO_POSITION) {
                mView.imgBtn_delete.setOnClickListener {
                    listener.onDeleteClick(position)
                }

                mView.numericUpDown.upButton.setOnClickListener {
                    listener.onNumericUpButtonClick(position)
                }

                mView.numericUpDown.downButton.setOnClickListener {
                    if (mView.numericUpDown.value.toInt() > 0)
                        listener.onNumericDownButtonClick(position)
                }
            }
        }

        override fun toString(): String {
            return super.toString() + " '" + mView.txtViewBagId.text + "'"
        }
    }
}
