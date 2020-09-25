package com.design2net.the_house.interfaces

import org.json.JSONObject

interface ApiResponseListener {
    fun onApiResponse(requestCode: Int, response: JSONObject, item: Any?)
}