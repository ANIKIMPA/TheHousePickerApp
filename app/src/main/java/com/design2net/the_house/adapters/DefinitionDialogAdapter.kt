package com.design2net.the_house.adapters

import android.support.v7.widget.RecyclerView
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import com.design2net.the_house.R
import com.design2net.the_house.models.DefinitionsBin
import kotlinx.android.synthetic.main.row_definition.view.*

class DefinitionDialogAdapter (
        private val mValues: ArrayList<DefinitionsBin>, private val mListener: DefinitionsBinListener)
    : RecyclerView.Adapter<DefinitionDialogAdapter.ViewHolder>() {

    interface DefinitionsBinListener {
        fun onBinSelected(position: Int)
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        val view = LayoutInflater.from(parent.context)
                .inflate(R.layout.row_definition, parent, false)
        return ViewHolder(view)
    }

    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        holder.setData(position)
    }

    override fun getItemCount(): Int = mValues.size

    inner class ViewHolder(private val mView: View) : RecyclerView.ViewHolder(mView) {

        fun setData(position: Int) {
            val item = mValues[position]

            mView.txtViewDefinition.text = item.name

            mView.imgViewDefinition.setImageResource(
                when(item.bin_id) {
                    "A" -> R.drawable.ic_bin_azul_cerrado_44dp
                    "V" -> R.drawable.ic_bin_verde_cerrado_44dp
                    "F" -> R.drawable.ic_freezer_cerrado_44dp
                    "IS" -> R.drawable.ic_articulos_limpieza_cerrado_44dp
                    "N" -> R.drawable.ic_fridge_cerrado_44dp
                    else -> R.drawable.ic_bin_verde_abierto_44dp
                }
            )

            mView.setOnClickListener { mListener.onBinSelected(position) }
        }

    }
}
