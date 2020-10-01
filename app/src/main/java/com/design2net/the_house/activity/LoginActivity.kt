package com.design2net.the_house.activity

import android.annotation.SuppressLint
import android.content.Intent
import android.os.Bundle
import android.support.v7.app.AppCompatActivity
import android.view.View
import android.widget.Toast
import com.design2net.the_house.BuildConfig
import com.design2net.the_house.MyApplication
import com.design2net.the_house.R
import com.design2net.the_house.UpdateApp
import com.design2net.the_house.interfaces.ApiResponseListener
import com.design2net.the_house.network.OkHttpRequest
import com.design2net.the_house.network.RequestCode
import kotlinx.android.synthetic.main.activity_login.*
import org.json.JSONObject

class LoginActivity : AppCompatActivity(), ApiResponseListener {

    private lateinit var client: OkHttpRequest

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_login)

        client = OkHttpRequest(getString(R.string.url_picker), this)

        btnLogin.setOnClickListener { authenticateUser() }

        loading.visibility = View.VISIBLE
        checkForUpdateApk()
    }

    private fun checkForUpdateApk() {
        client.makeGetRequest(RequestCode.CHECK_APP.code, "check-update.html")
    }

    private fun authenticateUser() {
        val username: String = edtUsername.text.toString().trim()
        val password: String = edtPassword.text.toString().trim()

        when {
            username.isEmpty() -> Toast.makeText(this, getString(R.string.required_username), Toast.LENGTH_SHORT).show()
            password.isEmpty() -> Toast.makeText(this, getString(R.string.required_password), Toast.LENGTH_SHORT).show()
            else -> {
                loading.visibility = View.VISIBLE
                client.makeGetRequest(RequestCode.LOGIN.code,
                        "login.html?username=$username&password=$password")
            }
        }
    }

    override fun onApiResponse(requestCode: Int, response: JSONObject, item: Any?) {
        when (requestCode) {
            RequestCode.LOGIN.code -> processLoginResponse(response)
            RequestCode.CHECK_APP.code -> processUpdateApp(response)
        }
    }

    private fun processUpdateApp(response: JSONObject) {
        runOnUiThread {
            val contenidoObj = response.getJSONObject("content")
            val serverVersion = contenidoObj.getInt("version_code")
            if (serverVersion > BuildConfig.VERSION_CODE) {
                UpdateApp(this).execute()
                return@runOnUiThread
            }

            if (MyApplication.isUserLoggedIn())
                goToMainActivity()
            else {
                edtUsername.visibility = View.VISIBLE
                edtPassword.visibility = View.VISIBLE
                btnLogin.visibility = View.VISIBLE
                loading.visibility = View.GONE
            }
        }
    }

    @SuppressLint("CommitPrefEdits")
    private fun processLoginResponse(response: JSONObject) {
        runOnUiThread {
            if (response.has("success") && response.getBoolean("success")) {
                val userInfo = response.getJSONObject("user_info")

                with(MyApplication.session.edit()) {
                    putString("locations", userInfo.getString("locations"))
                    putString("name", userInfo.getString("name"))
                    putString("position", userInfo.getString("position"))
                    putString("username", userInfo.getString("username"))
                    putString("levels", userInfo.getString("level"))
                    putString("token", response.getString("token"))
                    apply()
                }

                goToMainActivity()
            } else {
                loading.visibility = View.GONE
                Toast.makeText(this, getString(R.string.login_failed), Toast.LENGTH_SHORT).show()
            }
        }
    }

    private fun goToMainActivity() {
        startActivity(Intent(this, MainActivity::class.java))
        finish()
    }
}
