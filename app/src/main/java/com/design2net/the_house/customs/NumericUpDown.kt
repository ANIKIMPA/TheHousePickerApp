package com.design2net.the_house.customs

import android.annotation.SuppressLint
import android.content.Context
import android.graphics.Typeface
import android.support.v4.content.ContextCompat
import android.util.AttributeSet
import android.view.View
import android.widget.ImageButton
import android.widget.LinearLayout
import android.widget.TextView
import com.design2net.the_house.R

class NumericUpDown @SuppressLint("SetTextI18n")
constructor(context: Context, attrs: AttributeSet?) : LinearLayout(context, attrs), View.OnClickListener {

    private val txtValue: TextView
    private val upButton: ImageButton
    private val downButton: ImageButton
    private var minValue: Int = 0
    private var maxValue: Int = 99

    var value: Int = 0
        set(value) {
            field = value
            txtValue.text = value.toString()
        }

    init {
        inflate(context, R.layout.numeric_up_down, this)

        // Asignar el valor.
        txtValue = findViewById(R.id.txtValue)
        txtValue.text = value.toString()

        // Boton de arriba
        upButton = findViewById(R.id.upButton)
        upButton.setOnClickListener(this)

        // Boton de arriba
        downButton = findViewById(R.id.downButton)
        downButton.setOnClickListener(this)

        @SuppressLint("CustomViewStyleable", "Recycle") val attributes = context.obtainStyledAttributes(attrs, R.styleable.numericUpDown)
        txtValue.text = attributes.getString(R.styleable.numericUpDown_value)
        txtValue.setTextColor(attributes.getColor(R.styleable.numericUpDown_textColor, resources.getColor(R.color.txt_app)))
        attributes.recycle()
    }

    constructor(context: Context) : this(context, null) {}

    @SuppressLint("SetTextI18n")
    fun incrementValue() {
        if (value < this.maxValue) {
            value++
            this.txtValue.text = value.toString()
        }
    }

    @SuppressLint("SetTextI18n")
    fun decrementValue() {
        if (value > minValue) {
            value--
            this.txtValue.text = value.toString()
        }
    }

    fun setMinValue(minValue: Int) {
        this.minValue = minValue
    }

    fun setMaxValue(maxValue: Int) {
        this.maxValue = maxValue
    }

    override fun onClick(v: View) {
        if (v.id == R.id.downButton) {
            decrementValue()
        } else if (v.id == R.id.upButton) {
            incrementValue()
        }
    }

    fun setTextColor(color: Int) {
        txtValue.setTextColor(ContextCompat.getColor(context, color))
    }

    fun setTextBold() {
        txtValue.setTypeface(null, Typeface.BOLD)
    }
}