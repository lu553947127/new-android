package com.ktw.bitbit.ui.me.capital;

import android.os.Bundle;
import android.text.TextUtils;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.RadioButton;
import android.widget.RadioGroup;
import android.widget.TextView;

import com.ktw.bitbit.R;
import com.ktw.bitbit.bean.AccountUser;
import com.ktw.bitbit.helper.LoginHelper;
import com.ktw.bitbit.helper.PasswordHelper;
import com.ktw.bitbit.ui.base.BaseActivity;
import com.ktw.bitbit.ui.tool.ButtonColorChange;
import com.ktw.bitbit.util.StringUtils;
import com.ktw.bitbit.view.BottomDialogFragment;
import com.xuan.xuanhttplibrary.okhttp.HttpUtils;
import com.xuan.xuanhttplibrary.okhttp.callback.BaseCallback;
import com.xuan.xuanhttplibrary.okhttp.result.ObjectResult;

import java.util.HashMap;
import java.util.Map;

import okhttp3.Call;

/**
 * @ProjectName: new-android
 * @Package: com.ktw.fly.ui.account
 * @ClassName: AccountPasswordActivity
 * @Description: 设置 修改 账号密码
 * @Author: XY
 * @CreateDate: 2021/9/23
 * @UpdateUser:
 * @UpdateDate: 2021/9/23
 * @UpdateRemark: 更新说明
 * @Version: 1.0
 */
public class CapitalPasswordActivity extends BaseActivity implements View.OnClickListener, RadioGroup.OnCheckedChangeListener {

    private RadioButton rbPhone;
    private RadioButton rbEmail;
    private RadioGroup radioGroup;

    //0：手机 1 ：邮箱  默认 手机验证
    private int verificationType;


    private EditText mPasswordEdit, mConfigPasswordEdit;


    private String mailbox;
    private String phone;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_account_password);
        initData();
        initView();
    }

    private void initData() {

        Map<String, String> params = new HashMap<>();
        params.put("userId", coreManager.getSelf().getUserId());
        HttpUtils.get().url(coreManager.getConfig().USER_ACCOUNT_DATA)
                .params(params)
                .build(true, true)
                .execute(new BaseCallback<AccountUser>(AccountUser.class) {


                    @Override
                    public void onResponse(ObjectResult<AccountUser> result) {
                        if (result.getResultCode() == 1 && result.getData() != null) {
                            phone = result.getData().phone;
                            mailbox = result.getData().mailbox;
                            boolean phoneIsNull = TextUtils.isEmpty(phone);
                            boolean mailboxIsMailbox = TextUtils.isEmpty(mailbox);
                            if (!phoneIsNull && !mailboxIsMailbox) {
                                verificationType = LoginHelper.LOGIN_PHONE;
                                radioGroup.setVisibility(View.VISIBLE);
                            } else if (!phoneIsNull) {
                                verificationType = LoginHelper.LOGIN_PHONE;
                            } else if (!mailboxIsMailbox) {
                                verificationType = LoginHelper.LOGIN_EMAIL;
                            }
                        }
                    }

                    @Override
                    public void onError(Call call, Exception e) {
                    }
                });
    }

    private void initView() {
        getSupportActionBar().hide();
        findViewById(R.id.tv_title_left).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                finish();
            }
        });
        TextView tvTitle = findViewById(R.id.tv_title_center);
        tvTitle.setText(R.string.capital_password);

        radioGroup = findViewById(R.id.rg_password);
        radioGroup.setOnCheckedChangeListener(this);

        rbPhone = findViewById(R.id.rb_phone);
        rbEmail = findViewById(R.id.rb_email);
        ButtonColorChange.colorChange(this, rbPhone);
        ButtonColorChange.changeDrawable(this, rbEmail, R.drawable.red_packet_list_text_bg);

        Button btnSetPassword = findViewById(R.id.btn_set_password);
        btnSetPassword.setOnClickListener(this);
        ButtonColorChange.colorChange(this, btnSetPassword);


        mPasswordEdit = findViewById(R.id.password_edit);
        mConfigPasswordEdit = findViewById(R.id.confirm_password_edit);
        PasswordHelper.bindPasswordEye(mPasswordEdit, findViewById(R.id.tbEye));
        PasswordHelper.bindPasswordEye(mConfigPasswordEdit, findViewById(R.id.tb_confirm_Eye));

    }

    @Override
    public void onClick(View v) {
        switch (v.getId()) {
            case R.id.btn_set_password:
                if (configPassword()) {
                    BottomDialogFragment verificationCodeDialog;
                    String password = mPasswordEdit.getText().toString().trim();
                    if (verificationType ==LoginHelper.LOGIN_PHONE ){
                        verificationCodeDialog = BottomDialogFragment.newInstance(coreManager, 2, verificationType,phone, password);
                    }else {
                        verificationCodeDialog = BottomDialogFragment.newInstance(coreManager, 2, verificationType,mailbox, password);
                    }

                    verificationCodeDialog.show(getSupportFragmentManager(), BottomDialogFragment.class.getSimpleName());
                }
                break;
        }
    }


    /**
     * 确认两次输入的密码是否一致
     */
    private boolean configPassword() {
        String password = mPasswordEdit.getText().toString().trim();
        String confirmPassword = mConfigPasswordEdit.getText().toString().trim();
        if (TextUtils.isEmpty(password) || password.length() < 6) {
            mPasswordEdit.requestFocus();
            mPasswordEdit.setError(StringUtils.editTextHtmlErrorTip(this, R.string.password_empty_error));
            return false;
        }
        if (TextUtils.isEmpty(confirmPassword) || confirmPassword.length() < 6) {
            mConfigPasswordEdit.requestFocus();
            mConfigPasswordEdit.setError(StringUtils.editTextHtmlErrorTip(this, R.string.confirm_password_empty_error));
            return false;
        }
        if (confirmPassword.equals(password)) {
            return true;
        } else {
            mConfigPasswordEdit.requestFocus();
            mConfigPasswordEdit.setError(StringUtils.editTextHtmlErrorTip(this, R.string.password_confirm_password_not_match));
            return false;
        }
    }

    @Override
    public void onCheckedChanged(RadioGroup group, int checkedId) {
        switch (checkedId) {
            case R.id.rb_phone:
                ButtonColorChange.colorChange(this, rbPhone);
                ButtonColorChange.changeDrawable(this, rbEmail, R.drawable.red_packet_list_text_bg);
                verificationType = LoginHelper.LOGIN_PHONE;
                break;
            case R.id.rb_email:
                ButtonColorChange.colorChange(this, rbEmail);
                ButtonColorChange.changeDrawable(this, rbPhone, R.drawable.red_packet_list_text_bg);
                verificationType = LoginHelper.LOGIN_EMAIL;
                break;
        }
    }
}
