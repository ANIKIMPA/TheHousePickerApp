package com.design2net.the_house

import android.annotation.SuppressLint
import android.app.Application
import android.content.Context
import android.content.SharedPreferences
import com.design2net.the_house.models.Auth
import okhttp3.OkHttpClient
import java.util.concurrent.TimeUnit

class MyApplication : Application() {

    companion object {
        lateinit var session: SharedPreferences
        lateinit var okHttpClient: OkHttpClient

        const val PICKER_LEVEL = "5"
        const val CHECKER_LEVEL = "62"

        fun getAuth(): Auth {
            val username = session.getString("username", null)
            val name = session.getString("name", null)
            val token = session.getString("token", null)
            val position = session.getString("position", "")
            val levels = session.getString("levels", "")
            val locations = session.getString("locations", "CAG1,CAP1,DOR1,GUY1,PON1,TRJ1")

            return Auth(username, name, token, position, levels, locations)
        }

        fun isUserLoggedIn() = session.contains("token")

        @SuppressLint("CommitPrefEdits")
        fun logOut() {
            with(session.edit()) {
                clear()
                apply()
            }
        }
    }

    @SuppressLint("CommitPrefEdits")
    override fun onCreate() {
        super.onCreate()

        session = getSharedPreferences("SESSION", Context.MODE_PRIVATE)

        val builder = OkHttpClient().newBuilder()
                .connectTimeout(30, TimeUnit.SECONDS)
                .readTimeout(30, TimeUnit.SECONDS)
                .writeTimeout(30, TimeUnit.SECONDS)
        okHttpClient = builder.build()
    }
}