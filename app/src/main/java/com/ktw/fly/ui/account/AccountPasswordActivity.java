package com.ktw.fly.ui.account;

import android.content.ComponentName;
import android.content.Intent;
import android.os.Bundle;
import android.text.TextUtils;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.RadioButton;
import android.widget.RadioGroup;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.Nullable;
import androidx.fragment.app.DialogFragment;

import com.google.android.material.bottomsheet.BottomSheetDialogFragment;
import com.ktw.fly.FLYAppConfig;
import com.ktw.fly.FLYApplication;
import com.ktw.fly.R;
import com.ktw.fly.bean.AccountUser;
import com.ktw.fly.bean.NavBean;
import com.ktw.fly.helper.DialogHelper;
import com.ktw.fly.helper.LoginHelper;
import com.ktw.fly.helper.PasswordHelper;
import com.ktw.fly.sp.UserSp;
import com.ktw.fly.ui.base.BaseActivity;
import com.ktw.fly.ui.tool.ButtonColorChange;
import com.ktw.fly.util.Constants;
import com.ktw.fly.util.PreferenceUtils;
import com.ktw.fly.util.StringUtils;
import com.ktw.fly.util.secure.LoginPassword;
import com.ktw.fly.view.BottomDialogFragment;
import com.xuan.xuanhttplibrary.okhttp.HttpUtils;
import com.xuan.xuanhttplibrary.okhttp.callback.BaseCallback;
import com.xuan.xuanhttplibrary.okhttp.callback.ListCallback;
import com.xuan.xuanhttplibrary.okhttp.result.ArrayResult;
import com.xuan.xuanhttplibrary.okhttp.result.ObjectResult;
import com.xuan.xuanhttplibrary.okhttp.result.Result;

import java.util.HashMap;
import java.util.Map;

import okhttp3.Call;

import static com.ktw.fly.FLYAppConfig.BROADCASTTEST_ACTION;

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
public class AccountPasswordActivity extends BaseActivity implements View.OnClickListener, RadioGroup.OnCheckedChangeListener {

    private RadioButton rbPhone;
    private RadioButton rbEmail;
    private RadioGroup radioGroup;

    //0：手机 1 ：邮箱  默认 手机验证
    private int verificationType;
    private BottomDialogFragment verificationCodeDialog;


    private EditText mPasswordEdit, mConfigPasswordEdit;

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
                            boolean phoneIsNull = TextUtils.isEmpty(result.getData().phone);
                            boolean mailboxIsMailbox = TextUtils.isEmpty(result.getData().mailbox);
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
        tvTitle.setText(R.string.login_password);

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
                    if (verificationCodeDialog == null) {
                        String password = mPasswordEdit.getText().toString().trim();
                        verificationCodeDialog = BottomDialogFragment.newInstance(coreManager, 1, verificationType, password);
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
