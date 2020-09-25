package com.design2net.the_house.adapters

import android.annotation.SuppressLint
import android.content.Context
import android.support.v7.widget.RecyclerView
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.AdapterView
import android.widget.ArrayAdapter
    import com.design2net.the_house.models.Bag
import com.design2net.the_house.models.Bin


import kotlinx.android.synthetic.main.row_bin_dialog.view.*
import android.widget.TextView
import android.graphics.Color
import com.design2net.the_house.R


class BinDialogFragmentAdapter(
        private val mBags: List<Bag>,
        private val mBines: List<Bin>,
        private val mListener: BinDialogListener?,
        private val context: Context)
    : RecyclerView.Adapter<BinDialogFragmentAdapter.ViewHolder>() {

    interface BinDialogListener {
        fun onSpinnerItemSelected(spinnerPosition: Int, bagPosition: Int)
        fun onBagClick(position: Int)
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        val view = LayoutInflater.from(parent.context)
                .inflate(R.layout.row_bin_dialog, parent, false)
        return ViewHolder(view)
    }

    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        holder.setData(position)
    }

    override fun getItemCount(): Int = mBags.size

    inner class ViewHolder(private val mView: View) : RecyclerView.ViewHolder(mView) {

        @SuppressLint("SetTextI18n")
        fun setData(pos: Int) {
            val item =  mBags[pos]

            var binPosition = 0
            for (i in 0 until mBines.size) {
                if (mBines[i].binNumber == item.binNumber) {
                    binPosition = i
                    break
                }
            }

            mView.txtViewBagId.text = "Bag #${item.id}"
            mView.setOnClickListener { mListener?.onBagClick(pos) }

            val arrAdapter = object : ArrayAdapter<String>(
                    context, android.R.layout.simple_spinner_item, mBines.map { it.description }) {
                override fun isEnabled(position: Int): Boolean {
                    return position != binPosition
                }

                override fun getDropDownView(position: Int, convertView: View?,
                                             parent: ViewGroup): View {
                    val view = super.getDropDownView(position, convertView, parent)
                    val tv = view as TextView
                    if (position == binPosition) {
                        // Set the hint text color gray
                        tv.setTextColor(Color.GRAY)
                    } else {
                        tv.setTextColor(Color.BLACK)
                    }
                    return view
                }
            }
            arrAdapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item)

            with(mView.spinnerLocation) {
                adapter = arrAdapter
                setSelection(binPosition, true)
                onItemSelectedListener = object : AdapterView.OnItemSelectedListener {
                    override fun onNothingSelected(parent: AdapterView<*>?) {}

                    override fun onItemSelected(parent: AdapterView<*>?, view: View?, position: Int, id: Long) {
                        mListener?.onSpinnerItemSelected(position, pos)
                    }

                }
            }
        }
    }
}
