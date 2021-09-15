package com.ktw.fly.view;

import android.content.Context;
import android.graphics.Color;
import android.os.Handler;
import android.util.AttributeSet;

import androidx.appcompat.widget.AppCompatTextView;

import javax.annotation.Nullable;

/**
 * Created by Harvey on 2/6/21.
 **/

public class VerCodeTextView extends AppCompatTextView implements Runnable {

    private Context context;
    public int recLen = 60;
    private Handler handler;

    public VerCodeTextView(Context context) {
        this(context, null);
    }

    public VerCodeTextView(Context context, @Nullable AttributeSet attrs) {
        this(context, attrs, 0);
    }

    public VerCodeTextView(Context context, @Nullable AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
        this.context = context;
    }


    public String getText() {
        return super.getText().toString().trim();
    }

    public void setText(String text) {
        super.setText(text);
    }

    public void setHint(String hint) {
        super.setHint(hint);
    }


    @Override
    public void run() {
        if (recLen > 1) {
            recLen--;
            setText(recLen + "S");
            handler.postDelayed(this, 1000);
        } else {
            setBtnVerCodeFos();
        }
    }

    public void setBtnVerCodeFos() {
        if (handler != null) {
            handler.removeCallbacks(this);
        }
        setTextColor(Color.parseColor("#111111"));
        setText("重新发送");
        setEnabled(true);
        recLen = 60;
    }

    public void btnVerCodeNoFos() {
//
        setTextColor(Color.parseColor("#737E80"));
        if (handler == null) {
            handler = new Handler();
        }
        setEnabled(false);
        handler.postDelayed(VerCodeTextView.this, 0);
    }

    public void destroy() {
        if (handler != null) {
            handler.removeCallbacks(this);
            handler = null;
        }
    }
}
