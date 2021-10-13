package com.ktw.fly.helper;

import android.text.InputType;
import android.text.method.DigitsKeyListener;
import android.widget.EditText;
import android.widget.ToggleButton;

import com.ktw.fly.R;

public class PasswordHelper {
    private static final int INVISIBLE_TYPE = InputType.TYPE_CLASS_TEXT | InputType.TYPE_TEXT_VARIATION_PASSWORD;
    private static final int VISIBLE_TYPE = InputType.TYPE_CLASS_TEXT | InputType.TYPE_TEXT_VARIATION_VISIBLE_PASSWORD;

    private PasswordHelper() {
    }

    public static void bindPasswordEye(EditText et, ToggleButton eye) {
        // checked 为 true表示眼睛睁开可以看见密码的情况，
        et.setInputType(INVISIBLE_TYPE);
//        et.setKeyListener(DigitsKeyListener.getInstance(et.getContext().getString(R.string.digits_password)));
        eye.setChecked(false);
        eye.setOnCheckedChangeListener((buttonView, isChecked) -> {
            if (isChecked) {
                et.setInputType(VISIBLE_TYPE);
//                et.setKeyListener(DigitsKeyListener.getInstance(et.getContext().getString(R.string.digits_password)));
            } else {
                et.setInputType(INVISIBLE_TYPE);
//                et.setKeyListener(DigitsKeyListener.getInstance(et.getContext().getString(R.string.digits_password)));
            }
        });
    }
}
