package com.ktw.bitbit.ui.account;

import android.content.Intent;
import android.os.Bundle;
import android.text.TextUtils;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;

import com.ktw.bitbit.R;
import com.ktw.bitbit.ui.base.BaseActivity;
import com.ktw.bitbit.ui.tool.ButtonColorChange;
import com.ktw.bitbit.util.StringUtils;
import com.ktw.bitbit.util.secure.LoginPassword;
import com.ktw.bitbit.view.TipDialog;

/**
 * 注册-2.填写密码
 */
public class RegisterPasswordActivity extends BaseActivity {

    private EditText mPasswordEdit;
    private EditText mConfirmPasswordEdit;
    private Button mNextStepBtn;
    private String mobilePrefix;
    private String mPhoneNum;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_register_password);
        if (getIntent() != null) {
            mobilePrefix = getIntent().getStringExtra(RegisterActivity.EXTRA_AUTH_CODE);
            mPhoneNum = getIntent().getStringExtra(RegisterActivity.EXTRA_PHONE_NUMBER);
        }
        getSupportActionBar().hide();
        findViewById(R.id.iv_title_left).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                finish();
            }
        });
        TextView tvTitle = (TextView) findViewById(R.id.tv_title_center);
        tvTitle.setText(getString(R.string.input_password));
        initView();
    }

    private void initView() {
        mPasswordEdit = (EditText) findViewById(R.id.password_edit);
        mConfirmPasswordEdit = (EditText) findViewById(R.id.confirm_password_edit);
        mNextStepBtn = (Button) findViewById(R.id.next_step_btn);
//        mNextStepBtn.setBackgroundColor(SkinUtils.getSkin(this).getAccentColor());
        ButtonColorChange.colorChange(this, mNextStepBtn);
        mPasswordEdit.setHint(getString(R.string.input_password));
        mConfirmPasswordEdit.setHint(getString(R.string.please_confirm_password));
        mNextStepBtn.setText(getString(R.string.next_step));

        mNextStepBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                nextStep();
            }
        });
    }

    private void nextStep() {
        final String password = mPasswordEdit.getText().toString().trim();
        String confirmPassword = mConfirmPasswordEdit.getText().toString().trim();
        if (TextUtils.isEmpty(password) || password.length() < 6) {
            mPasswordEdit.requestFocus();
            mPasswordEdit.setError(StringUtils.editTextHtmlErrorTip(this, R.string.password_empty_error));
            return;
        }
        if (TextUtils.isEmpty(confirmPassword) || confirmPassword.length() < 6) {
            mConfirmPasswordEdit.requestFocus();
            mConfirmPasswordEdit.setError(StringUtils.editTextHtmlErrorTip(this, R.string.confirm_password_empty_error));
            return;
        }
        if (!confirmPassword.equals(password)) {
            mConfirmPasswordEdit.requestFocus();
            mConfirmPasswordEdit.setError(StringUtils.editTextHtmlErrorTip(this, R.string.password_confirm_password_not_match));
            return;
        }

        Intent intent = new Intent();
        intent.setClass(this, RegisterUserBasicInfoActivity.class);
        intent.putExtra(RegisterActivity.EXTRA_PHONE_NUMBER, mPhoneNum);
        intent.putExtra(RegisterActivity.EXTRA_PASSWORD, LoginPassword.encodeMd5(password));
        intent.putExtra(RegisterActivity.EXTRA_AUTH_CODE, mobilePrefix);
        startActivity(intent);
        finish();
    }

    private void doBack() {
        TipDialog tipDialog = new TipDialog(this);
        tipDialog.setmConfirmOnClickListener(getString(R.string.cancel_register_prompt), new TipDialog.ConfirmOnClickListener() {
            @Override
            public void confirm() {
                finish();
            }
        });
        tipDialog.show();
    }
}
