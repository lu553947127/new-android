package com.ktw.fly.wallet.dialog;

import android.os.Bundle;
import android.view.Gravity;
import android.widget.EditText;
import android.widget.TextView;

import com.ktw.fly.R;

public class VerifyBottomDialog extends BaseDialogFragment {

    private int mType;

    public static VerifyBottomDialog newInstance(int type) {
        Bundle args = new Bundle();
        args.putInt("type", type);
        VerifyBottomDialog fragment = new VerifyBottomDialog();
        fragment.setArguments(args);
        return fragment;
    }

    public interface VerifyCallBack {
        void onVerifyCallBackClicked(String pwd, String code);

        void onSendMsgClicked(VerifyBottomDialog dialog);
    }

    private VerifyCallBack mCallBack;

    public VerifyBottomDialog setCallBack(VerifyCallBack mCallBack) {
        this.mCallBack = mCallBack;
        return this;
    }

    private TextView mSendTv;
    private EditText mCodeEt;

    @Override
    protected int getLayoutId() {
        return R.layout.dialog_verify_bottom_layout;
    }

    @Override
    protected int getGravity() {
        return Gravity.BOTTOM;
    }

    @Override
    protected void getThisView() {
        EditText mPwdEt = findView(R.id.et_password);
        mCodeEt = findView(R.id.et_code);
        mSendTv = findView(R.id.tv_send_code);
        findView(R.id.tv_sure)
                .setOnClickListener(v -> {
                    String s = mPwdEt.getText().toString();
                    String s1 = mCodeEt.getText().toString();
                    if (s.length() == 0 || s1.length() == 0) {
                        return;
                    }
                    if (mCallBack != null) {
                        mCallBack.onVerifyCallBackClicked(s, s1);
                        dismiss();
                    }
                });

        mSendTv.setOnClickListener(v -> {
            if (mCallBack != null) {
                mCallBack.onSendMsgClicked(this);
            }
        });
        findView(R.id.iv_cancel)
                .setOnClickListener(v -> dismiss());
    }

    public void onSuccessSend() {
        //发送验证码成功,初始化验证码按钮
    }

    @Override
    protected void initBundle() {
        mType = getArguments().getInt("type");
        if (mType == 1) {
            //邮箱
            mCodeEt.setHint(R.string.tv_input_text_1);
        } else {
            mCodeEt.setHint(R.string.tv_input_text_2);
        }

    }

    @Override
    protected void initLayout() {

    }
}