package com.ktw.bitbit.wallet.utils;

import android.text.Editable;
import android.text.TextUtils;
import android.text.TextWatcher;

public class BaseTextWatcher implements TextWatcher {
    @Override
    public void beforeTextChanged(CharSequence s, int start, int count, int after) {

    }

    @Override
    public void onTextChanged(CharSequence s, int start, int before, int count) {

    }

    @Override
    public void afterTextChanged(Editable s) {
        String text = s.toString();
        int len = s.toString().length();

        if (text.startsWith(".")) {
            //起始为. 插入0成为0.
            s.insert(0, "0");
        } else if (len >= 2 && text.startsWith("0") && !text.contains(".")) {
            //如果起始是0但是没输入 点  则剔除掉0
            s.replace(0, 1, "");
        } else if (text.contains(".")) {
            if (s.length() - 1 - s.toString().indexOf(".") > digits) {
                s.replace(s.length() - 1, s.length(), "");
            } else {
                onTextChangeRightful();
            }
        } else {
            onTextChangeRightful();
        }
        if (TextUtils.isEmpty(text) && hasMax && !text.startsWith(".")) {
            double price = Double.parseDouble(text);
            if (price > maxNumber) {
                s.clear();
                for (Character it: String.valueOf(maxNumber).toCharArray()){
                    s.append(it);
                }
            }
        }
    }

    private boolean hasMax = false;

    private double maxNumber = -1.0;

    private int digits = 4;

    public BaseTextWatcher setDigits(int d) {
        digits = d;
        return this;
    }

    public BaseTextWatcher setMax(double b) {
        hasMax = true;
        maxNumber = b;
        return this;
    }

    public void onTextChangeRightful() {

    }
}
