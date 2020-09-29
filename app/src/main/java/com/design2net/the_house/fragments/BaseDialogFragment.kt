package com.design2net.the_house.fragments

import android.annotation.SuppressLint
import android.content.Context
import android.graphics.Color
import android.support.v4.app.DialogFragment
import android.util.Log
import android.view.Gravity
import android.view.inputmethod.InputMethodManager
import android.widget.EditText
import android.widget.LinearLayout
import android.widget.TextView
import android.widget.Toast
import com.design2net.the_house.R

abstract class BaseDialogFragment:  DialogFragment(){

    protected fun converTodp(pixels: Int): Int {
        val scale = this.resources.displayMetrics.density
        return (pixels * scale + 0.5f).toInt()
    }

    protected fun hideKeyboard(editText: EditText) {
        if (activity != null) {
            val imm = activity!!.getSystemService(Context.INPUT_METHOD_SERVICE) as InputMethodManager
            imm.hideSoftInputFromWindow(editText.windowToken, 0)
        }
    }

    protected fun showKeyBoard() {
        val imm = activity!!.getSystemService(Context.INPUT_METHOD_SERVICE) as InputMethodManager
        imm.toggleSoftInput(InputMethodManager.SHOW_FORCED, InputMethodManager.HIDE_IMPLICIT_ONLY)
    }

    protected fun log(text: String) {
        Log.i("NOVAN", text)
    }

    @SuppressLint("ShowToast")
    protected fun errorToast(context: Context, text: String, duration: Int): Toast {
        val toast = Toast.makeText(context, text, duration)
        toast.setGravity(Gravity.CENTER_VERTICAL, 0, 0)
        val toastContentView = toast.view as LinearLayout
        toastContentView.setBackgroundResource(R.drawable.spinner_background)
        toastContentView.setPadding(converTodp(10), converTodp(10), converTodp(10), converTodp(10))
        val textView = ((toast.view as LinearLayout)).getChildAt(0) as TextView
        textView.setTextColor(Color.RED)
        return toast
    }
}