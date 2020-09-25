package com.design2net.the_house.adapters

import android.annotation.SuppressLint
import android.os.Build
import android.support.v7.widget.RecyclerView
import android.text.Html
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import com.design2net.the_house.R
import com.design2net.the_house.models.Chat
import kotlinx.android.synthetic.main.row_chat.view.*

class ChatAdapter(private val mMessages: ArrayList<Chat>, private val mListener: ChatRecyclerViewListener) : RecyclerView.Adapter<ChatAdapter.ViewHolder>() {

    override fun onCreateViewHolder(parent: ViewGroup, p1: Int): ViewHolder {
        val view = LayoutInflater.from(parent.context)
                .inflate(R.layout.row_chat, parent, false)

        return ViewHolder(view)
    }

    override fun getItemCount(): Int = mMessages.size

    override fun onBindViewHolder(viewHolder: ViewHolder, position: Int) {
        viewHolder.setData(position)
    }

    inner class ViewHolder(private val mView: View) : RecyclerView.ViewHolder(mView)  {
        @SuppressLint("SetTextI18n")
        fun setData(position: Int) {
            val item = mMessages[position]

            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.N) {
                mView.txtViewMessage.text = Html.fromHtml("<b>${item.from_user}:</b> ${item.message}", Html.FROM_HTML_MODE_COMPACT)
            } else {
                mView.txtViewMessage.text = Html.fromHtml("<b>${item.from_user}:</b> ${item.message}")
            }

            mView.txtViewDateTime.text = item.date_sent

            if (item.is_from_employee) {
                mView.imgViewEmployee.visibility = View.VISIBLE
                mView.imgViewCustomer.visibility = View.GONE
                mView.bubbleChat.setBackgroundResource(R.drawable.bg_chat_bubble_green)
            } else {
                mView.imgViewCustomer.visibility = View.VISIBLE
                mView.imgViewEmployee.visibility = View.GONE
                mView.bubbleChat.setBackgroundResource(R.drawable.bg_chat_bubble_gray)
            }

            mView.txtViewVerSustitutos.visibility = if (item.sustitutos.isEmpty()) View.GONE else View.VISIBLE
            mView.txtViewVerSustitutos.setOnClickListener {
                mListener.onVerSustitutosClick(item.sku, item.sustitutos)
            }
        }
    }

    interface ChatRecyclerViewListener {
        fun onVerSustitutosClick(sku: String, sustitutosSkus: String)
    }
}