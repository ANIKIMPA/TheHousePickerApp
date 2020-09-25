package com.design2net.the_house.activity

import android.annotation.SuppressLint
import android.app.Activity
import android.content.Context
import android.content.Intent
import android.content.IntentFilter
import android.net.ConnectivityManager
import android.os.Bundle
import android.support.design.widget.Snackbar
import android.support.v7.app.AppCompatActivity
import android.util.Log
import android.view.inputmethod.InputMethodManager
import android.widget.EditText
import com.design2net.the_house.BuildConfig
import com.design2net.the_house.R
import com.design2net.the_house.network.ConnectivityReceiver
import com.design2net.the_house.MyApplication
import com.design2net.the_house.network.OkHttpRequest
import com.design2net.the_house.network.RequestCode


@SuppressLint("Registered")
abstract class BaseActivity : AppCompatActivity(), ConnectivityReceiver.ConnectivityReceiverListener {
    private var mSnackBar: Snackbar? = null
    protected var mNotificationsQty = 0

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        if (ConnectivityReceiver.connectivityReceiverListener == null)
            registerReceiver(ConnectivityReceiver(),
                    IntentFilter(ConnectivityManager.CONNECTIVITY_ACTION))

        OkHttpRequest.internetListener = this
    }

    private fun showMessage(isConnected: Boolean) {
        if (!isConnected) {
            val messageToUser = "No internet connection."

            mSnackBar = Snackbar.make(findViewById(R.id.rootLayout), messageToUser, Snackbar.LENGTH_LONG)
            mSnackBar?.duration = Snackbar.LENGTH_INDEFINITE
            mSnackBar?.setAction("Retry")
            {
                onRetryClick()
                showMessage(ConnectivityReceiver.isConnected(applicationContext))
            }
            mSnackBar?.show()
        }
    }

    abstract fun onRetryClick()

    override fun onResume() {
        super.onResume()

        ConnectivityReceiver.connectivityReceiverListener = this

        if (!MyApplication.isUserLoggedIn())
            finish()
    }

    override fun onNetworkConnectionChanged(isConnected: Boolean) {
        showMessage(isConnected)
    }

    protected fun getVersionCode(): String {
        val versionCode = BuildConfig.VERSION_CODE
        return String.format("%.2f", versionCode.toDouble())
    }

    protected fun log(text: String) {
        Log.i("NIOVAN", text)
    }

    protected fun converTodp(pixels: Int): Int {
        val scale = this.resources.displayMetrics.density
        return (pixels * scale + 0.5f).toInt()
    }

    protected fun hideKeyboard(editText: EditText) {
        val imm = getSystemService(Context.INPUT_METHOD_SERVICE) as InputMethodManager
        imm.hideSoftInputFromWindow(editText.windowToken, 0)
    }

    protected fun requestLogOut(request: OkHttpRequest) {
        val params = hashMapOf("token" to MyApplication.getAuth().token)
        request.makePostRequest(RequestCode.LOGOUT.code, "logout", params)
    }

    protected fun logout(activity: Activity) {
        MyApplication.logOut()
        startActivity(Intent(activity, LoginActivity::class.java))
        activity.finish()
    }
}