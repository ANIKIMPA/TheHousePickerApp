package com.design2net.the_house.network

import android.util.Log
import com.design2net.the_house.MyApplication
import com.design2net.the_house.interfaces.ApiResponseListener
import okhttp3.*
import org.json.JSONException
import org.json.JSONObject
import java.io.IOException

class OkHttpRequest(private var url: String, private val mListener: ApiResponseListener) : Callback {
    private var requestCode: Int = 0
    private var mItem: Any? = null

    companion object {
        var internetListener: ConnectivityReceiver.ConnectivityReceiverListener? = null
    }

    fun makeGetRequest(requestCode: Int, action: String): okhttp3.Call {
        this.requestCode = requestCode

        val request = Request.Builder()
                .url(url + action)
                .build()

        val call = MyApplication.okHttpClient.newCall(request)
        call.enqueue(this)
        return call
    }

    fun makePostRequest(requestCode: Int, action: String, parameters: HashMap<String, String>, mItem: Any? = null): okhttp3.Call {

        parameters["token"] = MyApplication.getAuth().token

        Log.i("NIOVAN", "Parameters: $parameters, to: $url$action.html")
        this.requestCode = requestCode

        val builder = FormBody.Builder()
        val it = parameters.entries.iterator()
        while (it.hasNext()) {
            val pair = it.next() as Map.Entry<*, *>
            builder.add(pair.key.toString(), pair.value.toString())
        }

        this.mItem = mItem

        val formBody = builder.build()
        val request = Request.Builder()
                .url("$url$action.html")
                .post(formBody)
                .build()

        val call = MyApplication.okHttpClient.newCall(request)
        call.enqueue(this)
        return call
    }

    override fun onFailure(call: okhttp3.Call, e: IOException) {
        e.printStackTrace()
        Log.e("Niovan", "Failed to execute request: " + e.message.toString())
        internetListener?.onNetworkConnectionChanged(false)
    }

    override fun onResponse(call: okhttp3.Call, response: Response) {
        if (response.isSuccessful) {
            try {
                val responseText = response.body!!.string()
                Log.i("NIOVAN", "Response: $responseText")
                val myResponse = JSONObject(responseText)

                mListener.onApiResponse(requestCode, myResponse, mItem)
            } catch (e: JSONException) {
                e.printStackTrace()
                Log.e("NIOVAN", e.printStackTrace().toString())
            }
        }
        else {
            val responseText = response.body!!.string()
            Log.i("NIOVAN", "Response: $responseText")
            Log.e("NIOVAN", "Request not successful $response")
        }
    }
}