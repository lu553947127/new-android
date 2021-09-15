package com.ktw.fly.view;

import android.graphics.Color;
import android.text.TextPaint;
import android.text.style.ClickableSpan;
import android.view.View;

public class MyClickSpan extends ClickableSpan {

    private int mHighLightColor = Color.BLUE;
    private View.OnClickListener mClickListener;


    public MyClickSpan(View.OnClickListener listener) {
        this.mClickListener = listener;
    }


    public MyClickSpan(int color, View.OnClickListener listener) {
        this.mHighLightColor = color;
        this.mClickListener = listener;
    }


    @Override
    public void onClick(View widget) {

        if (mClickListener != null)

            mClickListener.onClick(widget);

    }


    @Override
    public void updateDrawState(TextPaint ds) {

        ds.setColor(mHighLightColor);

//        ds.setUnderlineText(mUnderLine);

    }

}