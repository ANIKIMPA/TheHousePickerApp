package com.design2net.the_house.fragments

import android.content.Context
import android.support.v4.app.DialogFragment
import android.util.Log
import android.view.inputmethod.InputMethodManager
import android.widget.EditText

abstract class BaseDialogFragment:  DialogFragment(){

    protected fun converTodp(pixels: Int): Int {
        val scale = this.resources.displayMetrics.density
        return (pixels * scale + 0.5f).toInt()
    }

    protected fun hideKeyboard(editText: EditText) {
        val imm = activity!!.getSystemService(Context.INPUT_METHOD_SERVICE) as InputMethodManager
        imm.hideSoftInputFromWindow(editText.windowToken, 0)
    }

    protected fun showKeyBoard() {
        val imm = activity!!.getSystemService(Context.INPUT_METHOD_SERVICE) as InputMethodManager
        imm.toggleSoftInput(InputMethodManager.SHOW_FORCED, InputMethodManager.HIDE_IMPLICIT_ONLY)
    }

    protected fun log(text: String) {
        Log.i("NOVAN", text)
    }
}