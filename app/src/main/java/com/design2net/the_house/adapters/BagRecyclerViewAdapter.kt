package com.design2net.the_house.adapters

import android.annotation.SuppressLint
import android.support.v7.widget.RecyclerView
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import com.design2net.the_house.R
import com.design2net.the_house.models.Bag
import kotlinx.android.synthetic.main.column_bag.view.*
import java.util.ArrayList

class BagsRecyclerViewAdapter(private val mListaBags: ArrayList<Bag>, private val mOnBagListener: OnBagListener) : RecyclerView.Adapter<BagsRecyclerViewAdapter.ViewHolder>() {

    override fun onCreateViewHolder(viewGroup: ViewGroup, i: Int): ViewHolder {
        val vista = LayoutInflater.from(viewGroup.context).inflate(R.layout.column_bag, viewGroup, false)

        return ViewHolder(vista, mOnBagListener)
    }

    interface OnBagListener {
        fun onBagClick(position: Int)
        fun onBagLongClick(position: Int)
    }

    override fun onBindViewHolder(viewHolder: ViewHolder, position: Int) {
        viewHolder.setData(position)
    }

    override fun getItemCount(): Int {
        return mListaBags.size
    }

    inner class ViewHolder(mView: View, private var onBagListener: OnBagListener) : RecyclerView.ViewHolder(mView) {

        @SuppressLint("SetTextI18n")
        fun setData(position: Int) {
            val mBag = mListaBags[position]

            itemView.txtViewId.text = "BAG #" + mBag.id
            itemView.txtViewTotalProductos.text = "(" + mBag.checkQty.toString() + ") Items"

            if (mBag == Bag.selected) {
                when(mBag.type) {
                    "Papel" -> itemView.imgViewUrl.setImageResource(R.drawable.ic_funda_abierta_44dp)
                    else -> itemView.imgViewUrl.setImageResource(R.drawable.ic_bolsa_abierta_44dp)
                }
                itemView.layoutContainerBag.setBackgroundResource(R.color.verde_claro)
            }
            else {
                when(mBag.type) {
                    "Papel" -> itemView.imgViewUrl.setImageResource(R.drawable.ic_funda_cerrada_44dp)
                    else -> itemView.imgViewUrl.setImageResource(R.drawable.ic_bolsa_cerrada_44dp)
                }
                itemView.layoutContainerBag.setBackgroundResource(R.color.trasparent)
            }


            itemView.setOnClickListener {
                if (position != RecyclerView.NO_POSITION) {
                    onBagListener.onBagClick(position)
                }
            }

            itemView.setOnLongClickListener {
                if (position != RecyclerView.NO_POSITION) {
                    onBagListener.onBagLongClick(position)
                }
                true
            }
        }
    }
}