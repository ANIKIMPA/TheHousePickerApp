package com.design2net.the_house.network
//
//import android.util.Base64
//import com.design2net.the_house.MyApplication
//import com.design2net.the_house.constants.ApiResponseCodes
//import com.design2net.the_house.constants.Tags
//import com.design2net.the_house.interfaces.ApiResponse
//import kotlinx.coroutines.*
//import okhttp3.Request
//import okhttp3.MultipartBody
//import org.json.JSONException
//import org.json.JSONObject
//
//class ApiCall(val requestCode: Int, val action:String, val data: JSONObject? = null, val listener: ApiResponse) {
//    suspend fun execute() {
//        coroutineScope {
//            async {
//                val requestDataValues = JSONObject()
//
//                requestDataValues.put("action", action)
//                requestDataValues.put("api_token", MyApplication.session.getString(Tags.AUTH_TOKEN, null) ?: "")
//                requestDataValues.put("device_id", MyApplication.getDeviceUUID())
//                requestDataValues.put("params", data)
//
//                val requestBody = MultipartBody.Builder()
//                        .setType(MultipartBody.FORM)
//                        .addFormDataPart("data", requestDataValues.toString())
//                        .build()
//
//                MyApplication.log(requestDataValues.toString(), tag = "APICALL")
//
//                val request = Request.Builder()
//                        .url("https://oril-t.design2net.com/app-api/")
//                        .post(requestBody)
//                        .header("Authorization", "Basic " + Base64.encodeToString("generic:123456".toByteArray(), Base64.NO_WRAP))
//                        .build()
//
//                val response = MyApplication.okHttpClient.newCall(request).execute()
//
//                val responseText = response.body!!.string()
//
//                MyApplication.log("Code ${response.code}", tag = "APICALL")
//                MyApplication.log(responseText, tag = "APICALL")
//                response.close()
//
//                GlobalScope.launch(Dispatchers.Main) {
//                    try {
//                        if(response.code == 401) {
//                            listener.onApiError(requestCode, ApiResponseCodes.UNAUTHORIZED,"UNAUTHORIZED")
//                            return@launch
//                        }
//                        if(response.code != 200) {
//                            listener.onApiError(requestCode, ApiResponseCodes.ERROR, JSONObject(responseText).getString("error"))
//                            return@launch
//                        }
//
//                        val apiResponse = JSONObject(responseText)
//
//                        listener.onApiResponse(requestCode, apiResponse)
//                        val actionResponse = apiResponse.getJSONObject("action_response")
//
//                        if(! actionResponse.getBoolean("success")) {
//                            listener.onApiActionError(requestCode, actionResponse.getString("error"))
//                            return@launch
//                        }
//                        listener.onApiActionResponse(requestCode, actionResponse)
//                    }
//                    catch (e: JSONException) {
//                        listener.onApiActionError(requestCode, "La informaciÃ³n recibida no tiene el formato correcto")
//                        e.printStackTrace()
//                    }
//                    catch (e: Exception) {
//                        listener.onApiActionError(requestCode, "Hubo un error recibiendo la informaciÃ³n")
//                        e.printStackTrace()
//                    }
//                }
//            }
//        }
//    }
//}