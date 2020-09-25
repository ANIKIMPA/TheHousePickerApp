package com.design2net.the_house.customs;

import android.annotation.SuppressLint;
import android.content.Context;
import android.content.res.TypedArray;
import android.util.AttributeSet;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.widget.LinearLayout;
import android.widget.TextView;

import com.design2net.the_house.R;

public class ButtonTienda extends LinearLayout {

    private TextView textViewTop;
    private TextView textViewBottom;

    public ButtonTienda(Context context, AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, 0, defStyleAttr);

        @SuppressLint("CustomViewStyleable") TypedArray array = getContext().obtainStyledAttributes(attrs,
                R.styleable.texts, 0, defStyleAttr);

        String topText = array.getString(R.styleable.texts_top_text);
        String bottomText = array.getString(R.styleable.texts_bottom_text);

        array.recycle();

        setOrientation(LinearLayout.VERTICAL);
        setGravity(Gravity.CENTER_HORIZONTAL);

        LayoutInflater inflater = (LayoutInflater) context.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
        inflater.inflate(R.layout.button_tienda, this, true);

        // Asignar el boton de arriba a la variable textViewTop.
        textViewTop = (TextView) getChildAt(0);
        textViewTop.setText(topText);

        // Asignar el boton de abajo a la variable textViewBottom.
        textViewBottom = (TextView) getChildAt(1);
        textViewBottom.setText(bottomText);
    }

    public void setTopText(String text) {
        textViewTop.setText(text);
    }

    public void setBottomText(String text) {
        textViewBottom.setText(text);
    }

    public String getBottomText() { return textViewBottom.getText().toString(); }
    public String getTopText() { return textViewTop.getText().toString(); }
}
