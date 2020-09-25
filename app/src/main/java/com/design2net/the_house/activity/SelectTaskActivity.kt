package com.design2net.the_house.activity

import android.annotation.SuppressLint
import android.content.Intent
import android.os.Bundle
import android.support.v7.widget.Toolbar
import android.view.Menu
import android.view.MenuItem
import android.view.View
import com.design2net.the_house.MyApplication
import com.design2net.the_house.R
import com.design2net.the_house.interfaces.ApiResponseListener
import com.design2net.the_house.network.OkHttpRequest
import com.design2net.the_house.network.RequestCode
import kotlinx.android.synthetic.main.activity_select_task.*
import kotlinx.android.synthetic.main.footer.*
import kotlinx.android.synthetic.main.toolbar.*
import org.json.JSONObject
import java.util.*

class SelectTaskActivity : BaseActivity(), ApiResponseListener {

    private lateinit var client: OkHttpRequest
    private var locationId: String = ""
    private var tienda: String = ""

    @SuppressLint("SetTextI18n")
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_select_task)

        locationId = intent.getStringExtra("location")
        tienda = intent.getStringExtra("tienda")

        val levels = MyApplication.getAuth().levels.split(",")

        if (!levels.contains(MyApplication.PICKER_LEVEL)) {
            goToOrdenesActivity(getString(R.string.url_checker))
            finish()
        } else if (!levels.contains(MyApplication.CHECKER_LEVEL)) {
            goToOrdenesActivity(getString(R.string.url_picker))
            finish()
        }

        client = OkHttpRequest(getString(R.string.url_checker), this)

        val toolbar = findViewById<Toolbar>(R.id.toolbar)
        setSupportActionBar(toolbar)


        backArrow.visibility = View.VISIBLE

        txtViewTitle.text = tienda.toUpperCase(Locale.ROOT)
        txtViewUsuario.text = "User: " + MyApplication.getAuth().fullName + " (" + MyApplication.getAuth().username + ")"
        txtViewVersion.text = "V: " + getVersionCode()

        toolbarLogo.setOnClickListener { finish() }
        btnPicker.setOnClickListener  { goToOrdenesActivity(getString(R.string.url_picker)) }
        btnChecker.setOnClickListener { goToOrdenesActivity(getString(R.string.url_checker)) }
    }

    override fun onCreateOptionsMenu(menu: Menu): Boolean {
        menuInflater.inflate(R.menu.option_menu, menu)

        return super.onCreateOptionsMenu(menu)
    }

    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        val id = item.itemId

        if (id == R.id.log_out) {
            requestLogOut(client)
        }

        return super.onOptionsItemSelected(item)
    }

    private fun goToOrdenesActivity(url: String) {
        val intent = Intent(applicationContext, OrdenesActivity::class.java)
        intent.putExtra("location", locationId)
        intent.putExtra("tienda", tienda)
        intent.putExtra("url", url)
        startActivity(intent)
    }

    override fun onApiResponse(requestCode: Int, response: JSONObject, item: Any?) {
        if (requestCode == RequestCode.LOGOUT.code) {
            logout(this)
        }
    }

    override fun onRetryClick() {  }
}
