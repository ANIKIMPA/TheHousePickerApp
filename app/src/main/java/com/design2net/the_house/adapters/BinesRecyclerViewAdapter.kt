package com.design2net.the_house.adapters

import android.support.v7.widget.RecyclerView
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import com.design2net.the_house.R
import com.design2net.the_house.models.Bin
import kotlinx.android.synthetic.main.column_bin.view.*

class BinesRecyclerViewAdapter(private val mValues: ArrayList<Bin>, private val mBinRecyclerViewListener: BinRecyclerViewListener)
    : RecyclerView.Adapter<BinesRecyclerViewAdapter.ViewHolder>() {

    interface BinRecyclerViewListener {
        fun onBinClick(position: Int)
        fun onBinLongClick(position: Int)
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        val view = LayoutInflater.from(parent.context)
                .inflate(R.layout.column_bin, parent, false)
        return ViewHolder(view, mBinRecyclerViewListener)
    }

    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        holder.setData(position)
    }

    override fun getItemCount(): Int = mValues.size

    inner class ViewHolder(private val mView: View, private val recyclerViewListener: BinRecyclerViewListener ) : RecyclerView.ViewHolder(mView) {

        fun setData(position: Int) {
            val item = mValues[position]

                if (item == Bin.selected) {
                    when (item.binId) {
                        "A" -> mView.imgViewBin.setImageResource(R.drawable.ic_bin_azul_abierto_44dp)
                        "V" -> mView.imgViewBin.setImageResource(R.drawable.ic_bin_verde_abierto_44dp)
                        "F" -> mView.imgViewBin.setImageResource(R.drawable.ic_freezer_abierto_44dp)
                        "IS" -> mView.imgViewBin.setImageResource(R.drawable.ic_articulos_limpieza_abierto_44dp)
                        "N" -> mView.imgViewBin.setImageResource(R.drawable.ic_fridge_abierto_44dp)
                    }
                    mView.layoutContainerBin.setBackgroundResource(R.color.verde_claro)
                } else {
                    when (item.binId) {
                        "A" -> mView.imgViewBin.setImageResource(R.drawable.ic_bin_azul_cerrado_44dp)
                        "V" -> mView.imgViewBin.setImageResource(R.drawable.ic_bin_verde_cerrado_44dp)
                        "F" -> mView.imgViewBin.setImageResource(R.drawable.ic_freezer_cerrado_44dp)
                        "IS" -> mView.imgViewBin.setImageResource(R.drawable.ic_articulos_limpieza_cerrado_44dp)
                        "N" -> mView.imgViewBin.setImageResource(R.drawable.ic_fridge_cerrado_44dp)
                    }
                    mView.layoutContainerBin.setBackgroundResource(R.color.trasparent)
                }

            mView.txtViewBin.text = item.description


            mView.setOnClickListener {
                if (position != RecyclerView.NO_POSITION) {
                    recyclerViewListener.onBinClick(position)
                }
            }

            mView.setOnLongClickListener {
                if (position != RecyclerView.NO_POSITION) {
                    recyclerViewListener.onBinLongClick(position)
                }
                true
            }
        }

    }
}