package com.ktw.bitbit.ui.account;

import android.annotation.SuppressLint;
import android.content.ComponentName;
import android.content.Intent;
import android.graphics.drawable.Drawable;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.text.TextUtils;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import com.ktw.bitbit.FLYAppConfig;
import com.ktw.bitbit.FLYApplication;
import com.ktw.bitbit.R;
import com.ktw.bitbit.bean.Code;
import com.ktw.bitbit.helper.DialogHelper;
import com.ktw.bitbit.helper.ImageLoadHelper;
import com.ktw.bitbit.helper.LoginHelper;
import com.ktw.bitbit.helper.PasswordHelper;
import com.ktw.bitbit.sp.UserSp;
import com.ktw.bitbit.ui.base.BaseActivity;
import com.ktw.bitbit.ui.tool.ButtonColorChange;
import com.ktw.bitbit.util.Constants;
import com.ktw.bitbit.util.PreferenceUtils;
import com.ktw.bitbit.util.StringUtils;
import com.ktw.bitbit.util.ToastUtil;
import com.ktw.bitbit.util.ViewPiexlUtil;
import com.ktw.bitbit.util.secure.LoginPassword;
import com.xuan.xuanhttplibrary.okhttp.HttpUtils;
import com.xuan.xuanhttplibrary.okhttp.callback.BaseCallback;
import com.xuan.xuanhttplibrary.okhttp.result.ObjectResult;
import com.xuan.xuanhttplibrary.okhttp.result.Result;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;

import okhttp3.Call;

import static com.ktw.bitbit.FLYAppConfig.BROADCASTTEST_ACTION;

/**
 * 忘记密码
 */
public class FindPwdActivity extends BaseActivity implements View.OnClickListener {
    private Button btn_getCode, btn_change;
    private EditText mPhoneNumberEdit;
    private EditText mPasswordEdit, mConfigPasswordEdit, mAuthCodeEdit;
    private TextView tv_prefix;
    private int mobilePrefix = 86;
    // 驗證碼
    private String randcode;
    // 图形验证码
    private EditText mImageCodeEdit;
    private ImageView mImageCodeIv;
    private ImageView mRefreshIv;
    private int reckonTime = 60;

    private int mLoginType;
    @SuppressLint("HandlerLeak")
    private Handler mReckonHandler = new Handler() {
        public void handleMessage(Message msg) {
            if (msg.what == 0x1) {
                btn_getCode.setText("(" + reckonTime + ")");
                reckonTime--;
                if (reckonTime < 0) {
                    mReckonHandler.sendEmptyMessage(0x2);
                } else {
                    mReckonHandler.sendEmptyMessageDelayed(0x1, 1000);
                }
            } else if (msg.what == 0x2) {
                // 60秒结束
                btn_getCode.setText(getString(R.string.send));
                btn_getCode.setEnabled(true);
                reckonTime = 60;
            }
        }
    };
    private EditText mEmailEdit;

    public FindPwdActivity() {
        noLoginRequired();
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_forget_password);
        mLoginType = getIntent().getIntExtra("loginType", -1);
        initAction();
        initView();
    }

    private void initAction() {
        getSupportActionBar().hide();
        findViewById(R.id.iv_title_left).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                finish();
            }
        });
        TextView tvTitle = (TextView) findViewById(R.id.tv_title_center);
        tvTitle.setText(getString(R.string.forget_password));
    }

    private void initView() {
        findViewById(R.id.ll_phone).setVisibility(mLoginType == LoginHelper.LOGIN_PHONE ? View.VISIBLE : View.GONE);
        findViewById(R.id.ll_email).setVisibility(mLoginType == LoginHelper.LOGIN_EMAIL ? View.VISIBLE : View.GONE);

        tv_prefix = (TextView) findViewById(R.id.tv_prefix);
        tv_prefix.setOnClickListener(this);
        mobilePrefix = PreferenceUtils.getInt(this, Constants.AREA_CODE_KEY, mobilePrefix);
        tv_prefix.setText("+" + mobilePrefix);

        btn_getCode = (Button) findViewById(R.id.send_again_btn);
        btn_change = (Button) findViewById(R.id.login_btn);
        ButtonColorChange.colorChange(this, btn_getCode);
        ButtonColorChange.colorChange(this, btn_change);
        btn_getCode.setOnClickListener(this);
        btn_change.setOnClickListener(this);

        mPhoneNumberEdit = (EditText) findViewById(R.id.phone_numer_edit);
        mEmailEdit = findViewById(R.id.email_edit);
        mPasswordEdit = (EditText) findViewById(R.id.password_edit);
        PasswordHelper.bindPasswordEye(mPasswordEdit, findViewById(R.id.tbEye));
        mConfigPasswordEdit = (EditText) findViewById(R.id.confirm_password_edit);
        PasswordHelper.bindPasswordEye(mConfigPasswordEdit, findViewById(R.id.tbEyeConfirm));
        mImageCodeEdit = (EditText) findViewById(R.id.image_tv);
        mAuthCodeEdit = (EditText) findViewById(R.id.auth_code_edit);
        List<EditText> mEditList = new ArrayList<>();
        mEditList.add(mPasswordEdit);
        mEditList.add(mConfigPasswordEdit);
        mEditList.add(mImageCodeEdit);
        mEditList.add(mAuthCodeEdit);
        mEditList.add(mEmailEdit);
        setBound(mEditList);

        mImageCodeIv = (ImageView) findViewById(R.id.image_iv);
        mRefreshIv = (ImageView) findViewById(R.id.image_iv_refresh);
        mRefreshIv.setOnClickListener(this);

        mPhoneNumberEdit.setHint(getString(R.string.hint_input_phone_number));
        mAuthCodeEdit.setHint(getString(R.string.please_input_auth_code));
        mPasswordEdit.setHint(getString(R.string.please_input_new_password));
        mConfigPasswordEdit.setHint(getString(R.string.please_confirm_new_password));
        btn_change.setText(getString(R.string.change_password));

        // 请求图形验证码
        if (!TextUtils.isEmpty(mPhoneNumberEdit.getText().toString())) {
            requestImageCode();
        }

        if (!TextUtils.isEmpty(mEmailEdit.getText().toString())) {
            requestImageCode("_loginMailboxPassword");
        }
        mPhoneNumberEdit.setOnFocusChangeListener(new View.OnFocusChangeListener() {
            @Override
            public void onFocusChange(View v, boolean hasFocus) {
                if (!hasFocus) {
                    // 手机号输入完成后自动刷新验证码，
                    // 只在移开焦点，也就是点击其他EditText时调用，
                    requestImageCode();
                }
            }
        });

        if (!TextUtils.isEmpty(mEmailEdit.getText().toString())) {
            requestImageCode("_loginMailboxPassword");
        }
        mEmailEdit.setOnFocusChangeListener(new View.OnFocusChangeListener() {
            @Override
            public void onFocusChange(View v, boolean hasFocus) {
                if (!hasFocus) {
                    // 邮箱输入完成后自动刷新验证码，
                    // 只在移开焦点，也就是点击其他EditText时调用，
                    requestImageCode("_loginMailboxPassword");
                }
            }
        });
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
            case R.id.image_iv_refresh:
                if (mLoginType == LoginHelper.LOGIN_PHONE) {
                    if (TextUtils.isEmpty(mPhoneNumberEdit.getText().toString())) {
                        ToastUtil.showToast(this, getString(R.string.tip_phone_number_empty_request_verification_code));
                        return;
                    }

                    requestImageCode();
                } else {
                    if (TextUtils.isEmpty(mEmailEdit.getText().toString())) {
                        ToastUtil.showToast(this, getString(R.string.tip_email_empty_request_verification_code));
                        return;
                    }

                    requestImageCode("_loginMailboxPassword");
                }

                break;
            case R.id.send_again_btn:
                // 获取验证码
                if (mLoginType == LoginHelper.LOGIN_PHONE) {
                    sendAgainPhone();
                } else if (mLoginType == LoginHelper.LOGIN_EMAIL) {
                    sendAgainEmail();
                }
                break;
            case R.id.login_btn:
                // 确认修改
                if (nextStep()) {
                    // 如果验证码正确，则可以重置密码
                    if (mLoginType == LoginHelper.LOGIN_PHONE) {
                        resetPassword();
                    } else if (mLoginType == LoginHelper.LOGIN_EMAIL) {
                        resetPasswordEmail();
                    }
                }
                break;
        }
    }


    //邮箱获取验证码
    private void sendAgainEmail() {
        String mEmailStr = mEmailEdit.getText().toString().trim();
        String imagecode = mImageCodeEdit.getText().toString().trim();
        if (TextUtils.isEmpty(mEmailStr) || TextUtils.isEmpty(imagecode)) {
            ToastUtil.showToast(mContext, getString(R.string.tip_email_verification_code_empty));
            return;
        }
        if (!configPassword()) {// 两次密码是否一致
            return;
        }
        requestEmailAuthCode(mEmailStr, mEmailStr + "_loginMailboxPassword", imagecode);
    }

    //手机号获取验证码
    private void sendAgainPhone() {
        String phoneNumber = mPhoneNumberEdit.getText().toString().trim();
        String imagecode = mImageCodeEdit.getText().toString().trim();
        if (TextUtils.isEmpty(phoneNumber) || TextUtils.isEmpty(imagecode)) {
            ToastUtil.showToast(mContext, getString(R.string.tip_phone_number_verification_code_empty));
            return;
        }
        if (!configPassword()) {// 两次密码是否一致
            return;
        }
        verifyTelephone(phoneNumber, imagecode);
    }


    /**
     * 邮箱修改密码
     */
    private void resetPasswordEmail() {
        DialogHelper.showDefaulteMessageProgressDialog(this);
        final String emailStr = mEmailEdit.getText().toString().trim();
        final String password = mPasswordEdit.getText().toString().trim();
        String authCode = mAuthCodeEdit.getText().toString().trim();
        Map<String, String> params = new HashMap<>();
        params.put("mailbox", emailStr);
        params.put("mailboxCode", authCode);
        params.put("areaCode", String.valueOf(mobilePrefix));
        params.put("password", LoginPassword.encodeMd5(password));

        HttpUtils.get().url(coreManager.getConfig().USER_PASSWORD_RESET_EMAIL)
                .params(params)
                .build(true, true)
                .execute(new BaseCallback<Void>(Void.class) {

                    @Override
                    public void onResponse(ObjectResult<Void> result) {
                        DialogHelper.dismissProgressDialog();
                        if (Result.checkSuccess(FindPwdActivity.this, result)) {
                            Toast.makeText(FindPwdActivity.this, getString(R.string.update_sccuess), Toast.LENGTH_SHORT).show();
                            if (coreManager.getSelf() != null
                                    && !TextUtils.isEmpty(coreManager.getSelf().getTelephone())) {
                                UserSp.getInstance(mContext).clearUserInfo();
                                FLYApplication.getInstance().mUserStatus = LoginHelper.STATUS_USER_SIMPLE_TELPHONE;
                                coreManager.logout();
                                LoginHelper.broadcastLogout(mContext);
                                LoginHistoryActivity.start(FindPwdActivity.this);

                                //发送广播  重新拉起app
                                Intent intent = new Intent(BROADCASTTEST_ACTION);
                                intent.setComponent(new ComponentName(FLYAppConfig.sPackageName, FLYAppConfig.sPackageName + ".MyBroadcastReceiver"));
                                sendBroadcast(intent);
                            } else {// 本地连电话都没有，说明之前没有登录过 修改成功后直接跳转至登录界面
                                startActivity(new Intent(FindPwdActivity.this, LoginActivity.class));
                            }
                            finish();
                        }
                    }

                    @Override
                    public void onError(Call call, Exception e) {
                        DialogHelper.dismissProgressDialog();
                        Toast.makeText(FindPwdActivity.this, getString(R.string.error_network), Toast.LENGTH_SHORT).show();
                    }
                });
    }

    /**
     * 修改密码
     */
    private void resetPassword() {
        DialogHelper.showDefaulteMessageProgressDialog(this);
        final String phoneNumber = mPhoneNumberEdit.getText().toString().trim();
        final String password = mPasswordEdit.getText().toString().trim();
        String authCode = mAuthCodeEdit.getText().toString().trim();
        Map<String, String> params = new HashMap<>();
        params.put("telephone", phoneNumber);
        params.put("randcode", authCode);
        params.put("areaCode", String.valueOf(mobilePrefix));
        params.put("newPassword", LoginPassword.encodeMd5(password));

        HttpUtils.get().url(coreManager.getConfig().USER_PASSWORD_RESET)
                .params(params)
                .build(true, true)
                .execute(new BaseCallback<Void>(Void.class) {

                    @Override
                    public void onResponse(ObjectResult<Void> result) {
                        DialogHelper.dismissProgressDialog();
                        if (Result.checkSuccess(FindPwdActivity.this, result)) {
                            Toast.makeText(FindPwdActivity.this, getString(R.string.update_sccuess), Toast.LENGTH_SHORT).show();
                            if (coreManager.getSelf() != null
                                    && !TextUtils.isEmpty(coreManager.getSelf().getTelephone())) {
                                UserSp.getInstance(mContext).clearUserInfo();
                                FLYApplication.getInstance().mUserStatus = LoginHelper.STATUS_USER_SIMPLE_TELPHONE;
                                coreManager.logout();
                                LoginHelper.broadcastLogout(mContext);
                                LoginHistoryActivity.start(FindPwdActivity.this);

                                //发送广播  重新拉起app
                                Intent intent = new Intent(BROADCASTTEST_ACTION);
                                intent.setComponent(new ComponentName(FLYAppConfig.sPackageName, FLYAppConfig.sPackageName + ".MyBroadcastReceiver"));
                                sendBroadcast(intent);
                            } else {// 本地连电话都没有，说明之前没有登录过 修改成功后直接跳转至登录界面
                                startActivity(new Intent(FindPwdActivity.this, LoginActivity.class));
                            }
                            finish();
                        }
                    }

                    @Override
                    public void onError(Call call, Exception e) {
                        DialogHelper.dismissProgressDialog();
                        Toast.makeText(FindPwdActivity.this, getString(R.string.error_network), Toast.LENGTH_SHORT).show();
                    }
                });
    }

    /**
     * 请求图形验证码
     */
    private void requestImageCode() {
        Map<String, String> params = new HashMap<>();
        String account = mobilePrefix + mPhoneNumberEdit.getText().toString().trim();
        params.put("telephone", account);
        String url = HttpUtils.get().url(coreManager.getConfig().USER_GETCODE_IMAGE)
                .params(params)
                .buildUrl();
        ImageLoadHelper.loadBitmapWithoutCache(
                mContext,
                url,
                b -> {
                    mImageCodeIv.setImageBitmap(b);
                }, e -> {
                    Toast.makeText(FindPwdActivity.this, R.string.tip_verification_code_load_failed, Toast.LENGTH_SHORT).show();
                }
        );
    }

    /**
     * 邮箱注册 请求图形验证码
     */
    private void requestImageCode(String key) {
        if (isFinishing()) {
            // 可能是http回调到这里的，可能activity已经销毁，不再继续，
            return;
        }


        if (TextUtils.isEmpty(mEmailEdit.getText().toString())) {
            ToastUtil.showToast(mContext, getString(R.string.tip_no_email_get_v_code));
            return;
        }

        Map<String, String> params = new HashMap<>();
        String account = mEmailEdit.getText().toString().trim();


        params.put("imgKey", account + key);
        String url = HttpUtils.get().url(coreManager.getConfig().USER_GETCODE_IMAGE_EMAIL)
                .params(params)
                .buildUrl();
        ImageLoadHelper.loadBitmapWithoutCache(
                mContext,
                url,
                b -> {
                    mImageCodeIv.setImageBitmap(b);
                }, e -> {
                    Toast.makeText(FindPwdActivity.this, R.string.tip_verification_code_load_failed, Toast.LENGTH_SHORT).show();
                }
        );
    }


    /**
     * 请求邮箱验证码
     */
    private void requestEmailAuthCode(String email, String emailKey, String imageCodeStr) {
        Map<String, String> params = new HashMap<>();
        params.put("email", email);
        params.put("key", emailKey);
        String language = Locale.getDefault().getLanguage();
        params.put("language", language);

        params.put("imgCode", imageCodeStr);
        params.put("isRegister", String.valueOf(0));
        params.put("version", "1");


        DialogHelper.showDefaulteMessageProgressDialog(this);
        HttpUtils.get().url(coreManager.getConfig().SEND_EMAIL_AUTH_CODE)
                .params(params)
                .build(true, true)
                .execute(new BaseCallback<Code>(Code.class) {
                    @Override
                    public void onResponse(ObjectResult<Code> result) {
                        DialogHelper.dismissProgressDialog();
                        if (Result.checkSuccess(mContext, result)) {
                            Toast.makeText(FindPwdActivity.this, R.string.verification_code_send_success, Toast.LENGTH_SHORT).show();
                            btn_getCode.setEnabled(false);
                            // 开始计时
                            mReckonHandler.sendEmptyMessage(0x1);

                            if (result.getData() != null && result.getData().getCode() != null) {
                                // 得到验证码
                                randcode = result.getData().getCode();
                            }
                        }
                    }

                    @Override
                    public void onError(Call call, Exception e) {
                        DialogHelper.dismissProgressDialog();
                        Toast.makeText(FindPwdActivity.this, getString(R.string.error_network), Toast.LENGTH_SHORT).show();
                    }
                });
    }


    /**
     * 请求验证码
     */
    private void verifyTelephone(String phoneNumber, String imageCode) {
        DialogHelper.showDefaulteMessageProgressDialog(this);
        Map<String, String> params = new HashMap<>();
        String language = Locale.getDefault().getLanguage();
        params.put("language", language);
        params.put("areaCode", String.valueOf(mobilePrefix));
        params.put("telephone", phoneNumber);
        params.put("imgCode", imageCode);
        params.put("isRegister", String.valueOf(0));
        params.put("version", "1");

        /**
         * 只判断中国手机号格式
         */
        if (!StringUtils.isMobileNumber(phoneNumber) && mobilePrefix == 86) {
            Toast.makeText(this, getString(R.string.Input_11_phoneNumber), Toast.LENGTH_SHORT).show();
            return;
        }

        HttpUtils.get().url(coreManager.getConfig().SEND_AUTH_CODE)
                .params(params)
                .build(true, true)
                .execute(new BaseCallback<Code>(Code.class) {
                    @Override
                    public void onResponse(ObjectResult<Code> result) {
                        DialogHelper.dismissProgressDialog();
                        if (Result.checkSuccess(mContext, result)) {
                            Toast.makeText(FindPwdActivity.this, R.string.verification_code_send_success, Toast.LENGTH_SHORT).show();
                            btn_getCode.setEnabled(false);
                            // 开始计时
                            mReckonHandler.sendEmptyMessage(0x1);
                            if (result.getData() != null && result.getData().getCode() != null) {
                                // 得到验证码
                                randcode = result.getData().getCode();
                            }
                        }
                    }

                    @Override
                    public void onError(Call call, Exception e) {
                        DialogHelper.dismissProgressDialog();
                        Toast.makeText(FindPwdActivity.this, getString(R.string.error_network), Toast.LENGTH_SHORT).show();
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

    /**
     * 验证验证码
     */
    private boolean nextStep() {
        final String phoneNumber = mPhoneNumberEdit.getText().toString().trim();
        final String email = mEmailEdit.getText().toString().trim();

        String authCode = mAuthCodeEdit.getText().toString().trim();

        if (mLoginType == LoginHelper.LOGIN_PHONE) {
            if (TextUtils.isEmpty(phoneNumber)) {
                Toast.makeText(this, getString(R.string.hint_input_phone_number), Toast.LENGTH_SHORT).show();
                return false;
            }

            /**
             * 只判断中国手机号格式
             */
            if (!StringUtils.isMobileNumber(phoneNumber) && mobilePrefix == 86) {
                Toast.makeText(this, getString(R.string.Input_11_phoneNumber), Toast.LENGTH_SHORT).show();
                return false;
            }
        } else if (mLoginType == LoginHelper.LOGIN_EMAIL) {
            if (TextUtils.isEmpty(email)) {
                Toast.makeText(this, getString(R.string.hint_input_email), Toast.LENGTH_SHORT).show();
                return false;
            }
        }

        if (TextUtils.isEmpty(authCode)) {
            Toast.makeText(this, getString(R.string.input_message_code), Toast.LENGTH_SHORT).show();
            return false;
        } else if (authCode.equals(randcode)) {
            // 验证码正确
            return true;
        } else {
            Toast.makeText(this, getString(R.string.msg_code_not_ok), Toast.LENGTH_SHORT).show();
            return false;
        }
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (resultCode != SelectPrefixActivity.RESULT_MOBILE_PREFIX_SUCCESS)
            return;
        mobilePrefix = data.getIntExtra(Constants.MOBILE_PREFIX, 86);
        tv_prefix.setText("+" + mobilePrefix);
        // 图形验证码可能因区号失效，
        // 请求图形验证码
        if (mLoginType==LoginHelper.LOGIN_PHONE){
            if (!TextUtils.isEmpty(mPhoneNumberEdit.getText().toString())) {
                requestImageCode();
            }
        }else if (mLoginType==LoginHelper.LOGIN_EMAIL){
            if (!TextUtils.isEmpty(mEmailEdit.getText().toString())) {
                requestImageCode("_loginMailboxPassword");
            }
        }

    }
}
