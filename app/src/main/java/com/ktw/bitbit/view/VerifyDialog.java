package com.ktw.bitbit.view;

import android.app.Dialog;
import android.content.Context;
import android.os.Bundle;
import android.text.InputFilter;
import android.text.TextUtils;
import android.view.View;
import android.view.WindowManager;
import android.widget.EditText;
import android.widget.TextView;

import androidx.annotation.StringRes;

import com.ktw.bitbit.R;
import com.ktw.bitbit.util.ScreenUtil;

public class VerifyDialog extends Dialog {
    private TextView mTipTv;
    private EditText mVerifyEdit;
    private TextView mCancel;
    private TextView mSend;

    private String mTip;
    private String mHint;
    private String mText;
    private VerifyClickListener mVerifyClickListener;
    @StringRes
    private Integer ok;
    private Integer inputType;
    private int mInputLength;

    public VerifyDialog(Context context) {
        super(context, R.style.MyDialog);
    }

    public void setVerifyClickListener(String tip, VerifyClickListener verifyClickListener) {
        this.mTip = tip;
        this.mVerifyClickListener = verifyClickListener;
    }

    public void setVerifyClickListener(String tip, String hint, VerifyClickListener verifyClickListener) {
        this.mTip = tip;
        this.mHint = hint;
        this.mVerifyClickListener = verifyClickListener;
    }

    public void setVerifyClickListener(String tip, String hint, String text, int inputLength, VerifyClickListener verifyClickListener) {
        this.mTip = tip;
        this.mHint = hint;
        this.mText = text;
        this.mInputLength = inputLength;
        this.mVerifyClickListener = verifyClickListener;
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.verify_dialog);
        setCanceledOnTouchOutside(false);
        initView();
    }

    private void initView() {
        mTipTv = (TextView) findViewById(R.id.tip_tv);
        if (!TextUtils.isEmpty(mTip)) {
            mTipTv.setText(mTip);
        }
        mVerifyEdit = (EditText) findViewById(R.id.verify_et);
        if (!TextUtils.isEmpty(mHint)) {
            mVerifyEdit.setHint(mHint);
        }
        if (!TextUtils.isEmpty(mText)) {
            mVerifyEdit.setText(mText);
        }
        if (mInputLength != 0) {
            mVerifyEdit.setFilters(new InputFilter[]{new InputFilter.LengthFilter(mInputLength)});
        }
        mCancel = (TextView) findViewById(R.id.cancel);
        mSend = (TextView) findViewById(R.id.send);
        if (ok != null) {
            mSend.setText(ok);
        }
        if (inputType != null) {
            mSend.setInputType(inputType);
        }
        WindowManager.LayoutParams lp = getWindow().getAttributes();
        lp.width = (int) (ScreenUtil.getScreenWidth(getContext()) * 0.8);
        initEvent();
    }

    private void initEvent() {
        mCancel.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                dismiss();
                if (mVerifyClickListener != null) {
                    mVerifyClickListener.cancel();
                }
            }
        });

        mSend.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                dismiss();
                String str = mVerifyEdit.getText().toString();
                if (mVerifyClickListener != null) {
                    mVerifyClickListener.send(str);
                }
            }
        });
    }

    public void setOkButton(@StringRes int ok) {
        this.ok = ok;
        if (mSend != null) {
            mSend.setText(ok);
        }
    }

    public void setInputType(int inputType) {
        this.inputType = inputType;
        if (mSend != null) {
            mSend.setInputType(inputType);
        }
    }

    public interface VerifyClickListener {
        void cancel();

        void send(String str);
    }
}
