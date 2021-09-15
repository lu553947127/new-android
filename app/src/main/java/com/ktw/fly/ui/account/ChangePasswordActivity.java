package com.ktw.fly.ui.account;

import android.content.ComponentName;
import android.content.Intent;
import android.graphics.drawable.Drawable;
import android.os.Bundle;
import android.text.TextUtils;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;

import com.ktw.fly.FLYAppConfig;
import com.ktw.fly.FLYApplication;
import com.ktw.fly.R;
import com.ktw.fly.bean.User;
import com.ktw.fly.db.dao.UserDao;
import com.ktw.fly.helper.DialogHelper;
import com.ktw.fly.helper.LoginHelper;
import com.ktw.fly.helper.PasswordHelper;
import com.ktw.fly.sp.UserSp;
import com.ktw.fly.ui.base.BaseActivity;
import com.ktw.fly.ui.tool.ButtonColorChange;
import com.ktw.fly.util.Constants;
import com.ktw.fly.util.PreferenceUtils;
import com.ktw.fly.util.StringUtils;
import com.ktw.fly.util.ViewPiexlUtil;
import com.ktw.fly.util.secure.LoginPassword;
import com.xuan.xuanhttplibrary.okhttp.HttpUtils;
import com.xuan.xuanhttplibrary.okhttp.callback.BaseCallback;
import com.xuan.xuanhttplibrary.okhttp.result.ObjectResult;
import com.xuan.xuanhttplibrary.okhttp.result.Result;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import okhttp3.Call;

import static com.ktw.fly.FLYAppConfig.BROADCASTTEST_ACTION;

/**
 * 修改密码
 */
public class ChangePasswordActivity extends BaseActivity implements View.OnClickListener {
    private Button btn_change;
    private EditText mPhoneNumberEdit;
    private EditText mOldPasswordEdit;
    private EditText mPasswordEdit, mConfigPasswordEdit;
    private TextView tv_prefix;
    private int mobilePrefix = 86;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_change_password);
        initView();
    }

    private void initView() {
        getSupportActionBar().hide();
        findViewById(R.id.iv_title_left).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                finish();
            }
        });
        TextView tvTitle = (TextView) findViewById(R.id.tv_title_center);

        tv_prefix = (TextView) findViewById(R.id.tv_prefix);
        tv_prefix.setOnClickListener(this);
        mobilePrefix = PreferenceUtils.getInt(this, Constants.AREA_CODE_KEY, mobilePrefix);
        tv_prefix.setText("+" + mobilePrefix);
        btn_change = (Button) findViewById(R.id.login_btn);
        ButtonColorChange.colorChange(this, btn_change);
        btn_change.setOnClickListener(this);
        mPhoneNumberEdit = (EditText) findViewById(R.id.phone_numer_edit);

        if (coreManager.getSelf() != null && !TextUtils.isEmpty(coreManager.getSelf().getTelephone())) {
            tvTitle.setText(getString(R.string.change_password));
            String telephone = coreManager.getSelf().getTelephoneNoAreaCode();
            mPhoneNumberEdit.setText(telephone);
        } else {
            tvTitle.setText(getString(R.string.forget_password));
            String userId = UserSp.getInstance(this).getUserId("");
            if (!TextUtils.isEmpty(userId)) {
                User mLastLoginUser = UserDao.getInstance().getUserByUserId(userId);
                if (mLastLoginUser != null) {
                    String phoneNumber = mLastLoginUser.getTelephone();
                    int mobilePrefix = PreferenceUtils.getInt(ChangePasswordActivity.this, Constants.AREA_CODE_KEY, -1);
                    String sPrefix = String.valueOf(mobilePrefix);
                    // 删除开头的区号，
                    if (phoneNumber.startsWith(sPrefix)) {
                        phoneNumber = phoneNumber.substring(sPrefix.length());
                    }
                    mPhoneNumberEdit.setText(phoneNumber);
                }
            }
        }

        mOldPasswordEdit = (EditText) findViewById(R.id.old_password_edit);
        PasswordHelper.bindPasswordEye(mOldPasswordEdit, findViewById(R.id.tbEyeOld));
        mPasswordEdit = (EditText) findViewById(R.id.password_edit);
        PasswordHelper.bindPasswordEye(mPasswordEdit, findViewById(R.id.tbEye));
        mConfigPasswordEdit = (EditText) findViewById(R.id.confirm_password_edit);
        PasswordHelper.bindPasswordEye(mConfigPasswordEdit, findViewById(R.id.tbEyeConfirm));
        List<EditText> mEditList = new ArrayList<>();
        mEditList.add(mOldPasswordEdit);
        mEditList.add(mPasswordEdit);
        mEditList.add(mConfigPasswordEdit);
        setBound(mEditList);

        mPhoneNumberEdit.setHint(getString(R.string.hint_input_phone_number));
        mPasswordEdit.setHint(getString(R.string.please_input_new_password));
        mConfigPasswordEdit.setHint(getString(R.string.please_confirm_new_password));
        btn_change.setText(getString(R.string.change_password));
    }

    public void setBound(List<EditText> mEditList) {// 为Edit内的drawableLeft设置大小
        for (int i = 0; i < mEditList.size(); i++) {
            Drawable[] compoundDrawable = mEditList.get(i).getCompoundDrawables();
            Drawable drawable = compoundDrawable[0];
            if (drawable != null) {
                drawable.setBounds(0, 0, ViewPiexlUtil.dp2px(this, 20), ViewPiexlUtil.dp2px(this, 20));
                mEditList.get(i).setCompoundDrawables(drawable, null, null, null);
            }
        }
    }

    @Override
    public void onClick(View view) {
        switch (view.getId()) {
            case R.id.tv_prefix:
                // 选择国家区号
                Intent intent = new Intent(this, SelectPrefixActivity.class);
                startActivityForResult(intent, SelectPrefixActivity.REQUEST_MOBILE_PREFIX_LOGIN);
                break;
            case R.id.login_btn:
                // 确认修改
                if (configPassword()) {
                    changePassword();
                }
                break;
        }
    }

    /**
     * 修改密码
     */
    private void changePassword() {
        DialogHelper.showDefaulteMessageProgressDialog(this);
        final String phoneNumber = mPhoneNumberEdit.getText().toString().trim();
        final String oldPassword = mOldPasswordEdit.getText().toString().trim();
        final String password = mPasswordEdit.getText().toString().trim();
        Map<String, String> params = new HashMap<>();
        params.put("access_token", coreManager.getSelfStatus().accessToken);
        params.put("telephone", phoneNumber);
        params.put("areaCode", String.valueOf(mobilePrefix));
        params.put("oldPassword", LoginPassword.encodeMd5(oldPassword));
        params.put("newPassword", LoginPassword.encodeMd5(password));

        HttpUtils.get().url(coreManager.getConfig().USER_PASSWORD_UPDATE)
                .params(params)
                .build()
                .execute(new BaseCallback<Void>(Void.class) {

                    @Override
                    public void onResponse(ObjectResult<Void> result) {
                        DialogHelper.dismissProgressDialog();
                        if (Result.checkSuccess(ChangePasswordActivity.this, result)) {
                            Toast.makeText(ChangePasswordActivity.this, getString(R.string.update_sccuess), Toast.LENGTH_SHORT).show();
                            if (coreManager.getSelf() != null
                                    && !TextUtils.isEmpty(coreManager.getSelf().getTelephone())) {
                                UserSp.getInstance(mContext).clearUserInfo();
                                FLYApplication.getInstance().mUserStatus = LoginHelper.STATUS_USER_SIMPLE_TELPHONE;
                                coreManager.logout();
                                LoginHelper.broadcastLogout(mContext);
                                LoginHistoryActivity.start(ChangePasswordActivity.this);

                                //发送广播  重新拉起app
                                Intent intent = new Intent(BROADCASTTEST_ACTION);
                                intent.setComponent(new ComponentName(FLYAppConfig.sPackageName, FLYAppConfig.sPackageName + ".MyBroadcastReceiver"));
                                sendBroadcast(intent);
                            } else {// 本地连电话都没有，说明之前没有登录过 修改成功后直接跳转至登录界面
                                startActivity(new Intent(ChangePasswordActivity.this, LoginActivity.class));
                            }
                            finish();
                        }
                    }

                    @Override
                    public void onError(Call call, Exception e) {
                        DialogHelper.dismissProgressDialog();
                        Toast.makeText(ChangePasswordActivity.this, getString(R.string.error_network), Toast.LENGTH_SHORT).show();
                    }
                });
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
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        if (resultCode != SelectPrefixActivity.RESULT_MOBILE_PREFIX_SUCCESS)
            return;
        mobilePrefix = data.getIntExtra(Constants.MOBILE_PREFIX, 86);
        tv_prefix.setText("+" + mobilePrefix);
    }
}
