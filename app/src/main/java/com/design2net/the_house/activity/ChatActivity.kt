package com.design2net.the_house.activity

import android.app.Activity
import android.os.Bundle
import android.support.v7.widget.LinearLayoutManager
import android.view.View
import com.design2net.the_house.R
import com.design2net.the_house.adapters.ChatAdapter
import com.design2net.the_house.fragments.SustitutoDialogFragment
import com.design2net.the_house.interfaces.ApiResponseListener
import com.design2net.the_house.models.Chat
import com.design2net.the_house.network.OkHttpRequest
import com.design2net.the_house.network.RequestCode
import kotlinx.android.synthetic.main.activity_chat.*
import kotlinx.android.synthetic.main.toolbar.*
import org.json.JSONObject

class ChatActivity : BaseActivity(), ApiResponseListener, ChatAdapter.ChatRecyclerViewListener, SustitutoDialogFragment.SustitutoListener {

    private var client: OkHttpRequest? = null
    private var mOrderNumber = ""
    private val mMessages = ArrayList<Chat>()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_chat)

        backArrow.visibility = View.VISIBLE
        toolbarLogo.setOnClickListener { onBackPressed() }

        client = OkHttpRequest(getString(R.string.url_picker), this)

        mOrderNumber = intent.getStringExtra("order_number")

        initRecyclerView()
        requestChat()

        imgBtnSend.setOnClickListener {
            requestSendMessage(edtMessage.text.toString())
        }
    }

    override fun onBackPressed() {
        setResult(Activity.RESULT_OK)
        super.onBackPressed()
    }

    private fun requestSendMessage(message: String) {
        edtMessage.setText("")
        val params = hashMapOf("order_number" to mOrderNumber, "message" to message)
        client!!.makePostRequest(RequestCode.SEND_MESSAGE.code, "send-message", params)
    }

    private fun initRecyclerView() {
        with(recyclerViewChat) {
            layoutManager = LinearLayoutManager(context)
            adapter = ChatAdapter(mMessages, this@ChatActivity)
        }
    }

    private fun requestChat() {
        val params = hashMapOf("order_number" to mOrderNumber)
        client!!.makePostRequest(RequestCode.GET_CHAT.code, "get-chat", params)
    }

    override fun onRetryClick() {
        TODO("Not yet implemented")
    }

    override fun onVerSustitutosClick(sku: String, sustitutosSkus: String) {
        showSustitutoDialog(sku, sustitutosSkus)
    }

    override fun onApiResponse(requestCode: Int, response: JSONObject, item: Any?) {
        when(requestCode) {
            RequestCode.GET_CHAT.code -> processGetChat(response)
            RequestCode.SEND_MESSAGE.code -> processMessageSent(response)
        }
    }

    private fun processMessageSent(response: JSONObject) {
        runOnUiThread {
            val jsonMessage = response.getJSONObject("message")

            val orderNumber = jsonMessage.getString("order_number")
            val sku = jsonMessage.getString("sku")
            val message = jsonMessage.getString("message")
            val sustitutos = jsonMessage.getString("sustitutos")
            val dateSent = jsonMessage.getString("date_sent")
            val fromUser = jsonMessage.getString("from_user")
            val isFromEmployee = jsonMessage.getBoolean("is_from_employee")

            mMessages.add(Chat(orderNumber, sku, message, sustitutos, dateSent, fromUser, isFromEmployee))

            recyclerViewChat.adapter!!.notifyItemInserted(mMessages.size - 1)
            recyclerViewChat.scrollToPosition(mMessages.size - 1)
        }
    }

    private fun processGetChat(response: JSONObject) {
        runOnUiThread {
            val jsonChat = response.getJSONArray("chat")

            for (idx in 0 until jsonChat.length()) {
                val msgObj = jsonChat.getJSONObject(idx)

                val orderNumber = msgObj.getString("order_number")
                val sku = msgObj.getString("sku")
                val message = msgObj.getString("message")
                val sustitutos = msgObj.getString("sustitutos")
                val dateSent = msgObj.getString("date_sent")
                val fromUser = msgObj.getString("from_user")
                val isFromEmployee = msgObj.getBoolean("is_from_employee")

                mMessages.add(Chat(orderNumber, sku, message, sustitutos, dateSent, fromUser, isFromEmployee))

                mNotificationsQty = response.getInt("notifications_qty")
            }

            updateData()
        }
    }

    private fun updateData() {
        recyclerViewChat.adapter!!.notifyDataSetChanged()
        recyclerViewChat.scrollToPosition(mMessages.size - 1)
    }

    private fun showSustitutoDialog(sku: String, sustitutosSkus: String) {
        val bundle = Bundle()
        bundle.putString("orderNumber", mOrderNumber)
        bundle.putString("sku", sku)

        val dialogFragment = SustitutoDialogFragment()
        dialogFragment.arguments = bundle
        dialogFragment.show(supportFragmentManager, "SustitutoDialogFragment")
    }

    override fun onSustitutosSent(response: JSONObject) {}
}
