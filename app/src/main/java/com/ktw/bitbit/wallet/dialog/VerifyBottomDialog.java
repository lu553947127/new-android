package com.ktw.bitbit.wallet.dialog;

import android.os.Bundle;
import android.os.CountDownTimer;
import android.view.Gravity;
import android.widget.EditText;
import android.widget.TextView;

import com.ktw.bitbit.R;
import com.ktw.bitbit.wallet.WithdrawActivity;

import java.util.Objects;

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
        countDownTimer.start();
    }

    @Override
    protected void initBundle() {
        mType = getArguments().getInt("type");
        if (mType == 2) {
            //邮箱
            mCodeEt.setHint(R.string.tv_input_text_1);
        } else {
            mCodeEt.setHint(R.string.tv_input_text_2);
        }
        ((WithdrawActivity) Objects.requireNonNull(getActivity())).sendCode(mType, this);
    }

    @Override
    protected void initLayout() {

    }

    /**
     * 2019/9/13/ 10:50 验证码倒计时
     */
    private CountDownTimer countDownTimer = new CountDownTimer(60000, 1000) {
        @Override
        public void onTick(long millisUntilFinished) {
            long time = millisUntilFinished / 1000;
            mSendTv.setText(getResources().getString(R.string.verify_code_resend) + "(" + time + "s" + ")");
            mSendTv.setEnabled(false);
        }

        @Override
        public void onFinish() {
            mSendTv.setText(getResources().getString(R.string.send));
            mSendTv.setEnabled(true);
        }
    };

    @Override
    public void dismiss() {
        super.dismiss();
        if (countDownTimer != null) {
            countDownTimer.cancel();
        }
    }
}