package com.ktw.fly.wallet.dialog;

import android.os.Bundle;

import com.ktw.fly.R;

public class VerifyCenterDialog extends BaseDialogFragment {

    public interface VerifyCallBack {
        void onCodeSendClicked(int type);
    }

    private VerifyCallBack mCallBack;

    public static VerifyCenterDialog newInstance() {

        Bundle args = new Bundle();

        VerifyCenterDialog fragment = new VerifyCenterDialog();
        fragment.setArguments(args);
        return fragment;
    }

    public VerifyCenterDialog setCallBack(VerifyCallBack mCallBack) {
        this.mCallBack = mCallBack;
        return this;
    }

    @Override
    protected int getLayoutId() {
        return R.layout.dialog_verify_center_layout;
    }

    @Override
    protected void getThisView() {
        findView(R.id.iv_cancel)
                .setOnClickListener(v -> dismiss());
        findView(R.id.tv_email)
                .setOnClickListener(v -> {
                    //邮箱验证
                    if (mCallBack!=null) {
                        mCallBack.onCodeSendClicked(2);
                        dismiss();
                    }
                });
        findView(R.id.tv_phone)
                .setOnClickListener(v -> {
                    //手机验证
                    if (mCallBack!=null) {
                        mCallBack.onCodeSendClicked(1);
                        dismiss();
                    }
                });
    }

    @Override
    protected void initBundle() {

    }

    @Override
    protected void initLayout() {

    }
}