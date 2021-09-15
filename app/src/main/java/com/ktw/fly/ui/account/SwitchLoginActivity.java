package com.ktw.fly.ui.account;

import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.text.TextUtils;
import android.util.Log;
import android.view.View;
import android.view.inputmethod.InputMethodManager;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import com.alibaba.fastjson.JSON;
import com.ktw.fly.FLYAppConstant;
import com.ktw.fly.FLYApplication;
import com.ktw.fly.R;
import com.ktw.fly.bean.Code;
import com.ktw.fly.bean.LoginRegisterResult;
import com.ktw.fly.bean.User;
import com.ktw.fly.bean.WXUploadResult;
import com.ktw.fly.helper.DialogHelper;
import com.ktw.fly.helper.ImageLoadHelper;
import com.ktw.fly.helper.LoginHelper;
import com.ktw.fly.helper.LoginSecureHelper;
import com.ktw.fly.helper.PrivacySettingHelper;
import com.ktw.fly.helper.UsernameHelper;
import com.ktw.fly.ui.FLYMainActivity;
import com.ktw.fly.ui.base.BaseActivity;
import com.ktw.fly.util.Constants;
import com.ktw.fly.util.DeviceInfoUtil;
import com.ktw.fly.util.PreferenceUtils;
import com.ktw.fly.util.ToastUtil;
import com.xuan.xuanhttplibrary.okhttp.HttpUtils;
import com.xuan.xuanhttplibrary.okhttp.callback.BaseCallback;
import com.xuan.xuanhttplibrary.okhttp.result.ObjectResult;
import com.xuan.xuanhttplibrary.okhttp.result.Result;

import java.util.HashMap;
import java.util.Locale;
import java.util.Map;

import okhttp3.Call;

public class SwitchLoginActivity extends BaseActivity implements View.OnClickListener {
    private EditText auth_code_edit;
    private Button loginBtn;
    private User mLastLoginUser;
    private TextView tv_prefix;
    private int mobilePrefix = 86;
    private int mOldLoginStatus;
    private Button mSendAgainBtn;
    private int reckonTime = 60;
    private String mRandCode;
    private EditText mImageCodeEdit;
    private ImageView mImageCodeIv;
    private ImageView mRefreshIv;
    private TextView mNickNameTv;
    private ImageView mAvatarImgView;
    private String mImageCodeStr;
    private EditText mPhoneNumberEdit;
    private String thirdToken;

    private Handler mReckonHandler = new Handler() {
        public void handleMessage(Message msg) {
            if (msg.what == 0x1) {
                mSendAgainBtn.setText(reckonTime + " " + "S");
                reckonTime--;
                if (reckonTime < 0) {
                    mReckonHandler.sendEmptyMessage(0x2);
                } else {
                    mReckonHandler.sendEmptyMessageDelayed(0x1, 1000);
                }
            } else if (msg.what == 0x2) {
                // 60秒结束
                mSendAgainBtn.setText(getString(R.string.send));
                mSendAgainBtn.setEnabled(true);
                reckonTime = 60;
            }
        }
    };
    private String phone;
    private boolean third;

    public SwitchLoginActivity() {
        noLoginRequired();
    }

    public static void bindThird(Context ctx, WXUploadResult thirdToken) {
        Intent intent = new Intent(ctx, SwitchLoginActivity.class);
        intent.putExtra("thirdTokenLogin", JSON.toJSONString(thirdToken));
        ctx.startActivity(intent);
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_switch);
        PreferenceUtils.putBoolean(this, Constants.LOGIN_CONFLICT, false);// 重置登录冲突记录

        mOldLoginStatus = FLYApplication.getInstance().mUserStatus;
        thirdToken = getIntent().getStringExtra("thirdTokenLogin");
        initActionBar();
        initView();
        if (!TextUtils.isEmpty(thirdToken)) {
            // 第三方进来直接登录，
            // 清空手机号以标记是第三方登录，
            mPhoneNumberEdit.setText("");
            login(true);
        }
    }

    private void initActionBar() {
        getSupportActionBar().hide();
        findViewById(R.id.iv_title_left).setOnClickListener(v -> finish());
        TextView tvTitle = (TextView) findViewById(R.id.tv_title_center);
        tvTitle.setText(getString(R.string.verification_code) + getString(R.string.login));
        TextView tvRight = (TextView) findViewById(R.id.tv_title_right);
        tvRight.setVisibility(View.GONE);
    }

    private void initView() {
        mPhoneNumberEdit = findViewById(R.id.phone_numer_edit);
        tv_prefix = (TextView) findViewById(R.id.tv_prefix);
        if (coreManager.getConfig().registerUsername) {
            tv_prefix.setVisibility(View.GONE);
        } else {
            tv_prefix.setOnClickListener(this);
        }
        mImageCodeEdit = (EditText) findViewById(R.id.image_tv);
        mImageCodeIv = (ImageView) findViewById(R.id.image_iv);
        mRefreshIv = (ImageView) findViewById(R.id.image_iv_refresh);
        mSendAgainBtn = (Button) findViewById(R.id.send_again_btn);
        auth_code_edit = findViewById(R.id.auth_code_edit);
        loginBtn = (Button) findViewById(R.id.login_btn);
        loginBtn.setOnClickListener(this);

        UsernameHelper.initEditText(mPhoneNumberEdit, coreManager.getConfig().registerUsername);

        findViewById(R.id.main_content).setOnClickListener(this);

        mRefreshIv.setOnClickListener(new View.OnClickListener() {// 刷新图形码
            @Override
            public void onClick(View v) {
                if (TextUtils.isEmpty(mPhoneNumberEdit.getText().toString().trim())) {
                    Toast.makeText(mContext, R.string.tip_phone_number_empty_request_verification_code, Toast.LENGTH_SHORT).show();
                } else {
                    requestImageCode();
                }
            }
        });

        mSendAgainBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                phone = mPhoneNumberEdit.getText().toString().trim();
                mImageCodeStr = mImageCodeEdit.getText().toString().trim();
                if (TextUtils.isEmpty(mImageCodeStr)) {
                    ToastUtil.showToast(mContext, getString(R.string.tip_verification_code_empty));
                    return;
                }
                // 验证手机号是否注册
                verifyPhoneIsRegistered(phone, mImageCodeStr);

            }
        });

        mPhoneNumberEdit.setOnFocusChangeListener(new View.OnFocusChangeListener() {
            @Override
            public void onFocusChange(View v, boolean hasFocus) {
                if (!hasFocus) {
                    // 注册页面手机号输入完成后自动刷新验证码，
                    // 只在移开焦点，也就是点击其他EditText时调用，
                    requestImageCode();
                }
            }
        });
    }

    @Override
    public void onClick(View v) {
        switch (v.getId()) {
            case R.id.tv_prefix:
                // 选择国家区号
                Intent intent = new Intent(this, SelectPrefixActivity.class);
                startActivityForResult(intent, SelectPrefixActivity.REQUEST_MOBILE_PREFIX_LOGIN);
                break;
            case R.id.login_btn:
                if (TextUtils.isEmpty(phone)) {
                    Toast.makeText(mContext, R.string.tip_phone_number_empty, Toast.LENGTH_SHORT).show();
                    return;
                }
                if (TextUtils.isEmpty(auth_code_edit.getText())) {
                    Toast.makeText(mContext, R.string.please_input_auth_code, Toast.LENGTH_SHORT).show();
                    return;
                }
                if (!TextUtils.isEmpty(mRandCode)) {
                    if (mRandCode.equals(auth_code_edit.getText().toString().trim())) {
                        Log.e("zx", "onClick: " + "login_btn");
                        login(false);
                    } else {
                        Toast.makeText(SwitchLoginActivity.this, R.string.auth_code_error, Toast.LENGTH_SHORT).show();
                    }
                } else {
                    login(false);
                }
                break;
            case R.id.register_account_btn:
                startActivity(new Intent(SwitchLoginActivity.this, RegisterActivity.class));
                break;
            case R.id.forget_password_btn:
                startActivity(new Intent(SwitchLoginActivity.this, FindPwdActivity.class));
                break;
            case R.id.switch_account_btn:
                finish();
                break;
            case R.id.main_content:
                // 点击空白区域隐藏软键盘
                InputMethodManager inputManager = (InputMethodManager) getSystemService(INPUT_METHOD_SERVICE);
                if (inputManager != null) {
                    inputManager.hideSoftInputFromWindow(findViewById(R.id.main_content).getWindowToken(), 0); //强制隐藏键盘
                }
                break;
        }
    }

    /**
     * 请求验证码
     */
    private void requestAuthCode(String phoneStr, String imageCodeStr) {
        Map<String, String> params = new HashMap<>();
        String language = Locale.getDefault().getLanguage();
        params.put("language", language);
        params.put("areaCode", String.valueOf(mobilePrefix));
        params.put("telephone", phoneStr);
        Log.e("zx", "requestAuthCode: " + phoneStr);
        params.put("imgCode", imageCodeStr);
        params.put("isRegister", String.valueOf(0));
        params.put("version", "1");

        DialogHelper.showDefaulteMessageProgressDialog(this);
        Log.e("zx", "requestAuthCode: " + imageCodeStr);
        HttpUtils.get().url(coreManager.getConfig().SEND_AUTH_CODE)
                .params(params)
                .build(true, true)
                .execute(new BaseCallback<Code>(Code.class) {

                    @Override
                    public void onResponse(ObjectResult<Code> result) {
                        DialogHelper.dismissProgressDialog();
                        if (result.getResultCode() == 1) {
                            if (result.getData() != null && result.getData().getCode() != null) {
                                Log.e("zx", "onResponse: " + result.getData().getCode());
                                mRandCode = result.getData().getCode();// 记录验证码
                            }
                            mSendAgainBtn.setEnabled(false);
                            // 开始倒计时
                            mReckonHandler.sendEmptyMessage(0x1);
                        } else {
                            if (!TextUtils.isEmpty(result.getResultMsg())) {
                                ToastUtil.showToast(SwitchLoginActivity.this,
                                        result.getResultMsg());
                            } else {
                                ToastUtil.showToast(SwitchLoginActivity.this,
                                        getString(R.string.tip_server_error));
                            }
                        }
                    }

                    @Override
                    public void onError(Call call, Exception e) {
                        DialogHelper.dismissProgressDialog();
                        ToastUtil.showNetError(mContext);
                    }
                });
    }

    /**
     * 验证手机是否注册
     */
    private void verifyPhoneIsRegistered(final String phoneStr, final String imageCodeStr) {
        verifyPhoneNumber(phoneStr, new Runnable() {
            @Override
            public void run() {
                requestAuthCode(phoneStr, imageCodeStr);
            }
        });
    }

    /**
     * 请求图形验证码
     */
    private void requestImageCode() {
        Map<String, String> params = new HashMap<>();
        params.put("telephone", mobilePrefix + mPhoneNumberEdit.getText().toString().trim());
        String url = HttpUtils.get().url(coreManager.getConfig().USER_GETCODE_IMAGE)
                .params(params)
                .buildUrl();
        ImageLoadHelper.loadBitmapWithoutCache(mContext, url, b -> {
            mImageCodeIv.setImageBitmap(b);
        }, e -> {
            Toast.makeText(SwitchLoginActivity.this, R.string.tip_verification_code_load_failed, Toast.LENGTH_SHORT).show();
        });
    }

    private void verifyPhoneNumber(String phoneNumber, final Runnable onSuccess) {
        Map<String, String> params = new HashMap<>();
        params.put("telephone", phoneNumber);
        params.put("areaCode", "" + mobilePrefix);
        params.put("verifyType", "1");
        HttpUtils.get().url(coreManager.getConfig().VERIFY_TELEPHONE)
                .params(params)
                .build(true, true)
                .execute(new BaseCallback<Void>(Void.class) {

                    @Override
                    public void onResponse(ObjectResult<Void> result) {
                        DialogHelper.dismissProgressDialog();
                        if (result == null) {
                            ToastUtil.showToast(SwitchLoginActivity.this,
                                    R.string.data_exception);
                            return;
                        }

                        if (result.getResultCode() == 1) {
                            onSuccess.run();
                        } else {
                            requestImageCode();
                            // 手机号已经被注册
                            if (!TextUtils.isEmpty(result.getResultMsg())) {
                                ToastUtil.showToast(SwitchLoginActivity.this,
                                        result.getResultMsg());
                            } else {
                                ToastUtil.showToast(SwitchLoginActivity.this,
                                        R.string.tip_server_error);
                            }
                        }
                    }

                    @Override
                    public void onError(Call call, Exception e) {
                        DialogHelper.dismissProgressDialog();
                        ToastUtil.showErrorNet(SwitchLoginActivity.this);
                    }
                });
    }

    private void login(boolean third) {
        this.third = third;
        login();
    }

    private void login() {
        PreferenceUtils.putInt(this, Constants.AREA_CODE_KEY, mobilePrefix);
        if (TextUtils.isEmpty(thirdToken)) {
            // 第三方登录的不处理账号密码，
            if (TextUtils.isEmpty(phone)) {
                Toast.makeText(mContext, getString(R.string.please_input_account), Toast.LENGTH_SHORT).show();
                return;
            }
        }
        final String phoneNumber = mPhoneNumberEdit.getText().toString().trim();
//        final String digestPwd = new String(Md5Util.toMD5(code));
        DialogHelper.showDefaulteMessageProgressDialog(this);
        HashMap<String, String> params = new HashMap<>();
        Log.e("zx", "login: " + mRandCode);
        params.put("xmppVersion", "1");
        // 附加信息
        params.put("model", DeviceInfoUtil.getModel());
        params.put("osVersion", DeviceInfoUtil.getOsVersion());
        params.put("serial", DeviceInfoUtil.getDeviceId(mContext));
        params.put("loginType", "1");//验证码登录

        // 地址信息
        double latitude = FLYApplication.getInstance().getBdLocationHelper().getLatitude();
        double longitude = FLYApplication.getInstance().getBdLocationHelper().getLongitude();
        if (latitude != 0)
            params.put("latitude", String.valueOf(latitude));
        if (longitude != 0)
            params.put("longitude", String.valueOf(longitude));

        if (FLYApplication.IS_OPEN_CLUSTER) {// 服务端集群需要
            String area = PreferenceUtils.getString(this, FLYAppConstant.EXTRA_CLUSTER_AREA);
            if (!TextUtils.isEmpty(area)) {
                params.put("area", area);
            }
        }
        LoginSecureHelper.smsLogin(
                this, coreManager, auth_code_edit.getText().toString().trim(), String.valueOf(mobilePrefix), phoneNumber,
                params,
                t -> {
                    DialogHelper.dismissProgressDialog();
                    ToastUtil.showToast(this, this.getString(R.string.tip_login_secure_place_holder, t.getMessage()));
                }, result -> {
                    DialogHelper.dismissProgressDialog();
                    if (!Result.checkSuccess(getApplicationContext(), result)) {
                        return;
                    }
                    if (!TextUtils.isEmpty(result.getData().getAuthKey())) {
                        DialogHelper.showMessageProgressDialog(mContext, getString(R.string.tip_need_auth_login));
                        CheckAuthLoginRunnable authLogin = new CheckAuthLoginRunnable(result.getData().getAuthKey());
                        waitAuth(authLogin);
                        return;
                    }
                    afterLogin(result);
                });
    }

    private void afterLogin(ObjectResult<LoginRegisterResult> result) {
        boolean success = LoginHelper.setLoginUser(mContext, coreManager, phone, result.getData().getPassword(), result);// 设置登陆用户信息
        if (success) {
            LoginRegisterResult.Settings settings = result.getData().getSettings();
            FLYApplication.getInstance().initPayPassword(result.getData().getUserId(), result.getData().getPayPassword());
            PrivacySettingHelper.setPrivacySettings(SwitchLoginActivity.this, settings);
            FLYApplication.getInstance().initMulti();

            // 登陆成功
            LoginRegisterResult.Login login = result.getData().getLogin();
            if (login != null && login.getSerial() != null && login.getSerial().equals(DeviceInfoUtil.getDeviceId(mContext))
                    && mOldLoginStatus != LoginHelper.STATUS_USER_NO_UPDATE && mOldLoginStatus != LoginHelper.STATUS_NO_USER) {
                // 如果Token没变，上次更新也是完整更新，那么直接进入Main程序
                // 其他的登陆地方都需进入DataDownloadActivity，在DataDownloadActivity里发送此广播
                LoginHelper.broadcastLogin(SwitchLoginActivity.this);
                // Intent intent = new Intent(mContext, MainActivity.class);
                Intent intent = new Intent(SwitchLoginActivity.this, FLYMainActivity.class);
                intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
                startActivity(intent);
            } else {
                // 否则，进入数据下载界面
                // startActivity(new Intent(SwitchLoginActivity.this, DataDownloadActivity.class));
                DataDownloadActivity.start(mContext, result.getData().getIsupdate());
            }
            finish();
        } else {
            // 登录失败
            String message = TextUtils.isEmpty(result.getResultMsg()) ? getString(R.string.login_failed) : result.getResultMsg();
            ToastUtil.showToast(mContext, message);
        }
    }

    private void waitAuth(CheckAuthLoginRunnable authLogin) {
        authLogin.waitAuthHandler.postDelayed(authLogin, 3000);
    }

    private class CheckAuthLoginRunnable implements Runnable {
        private Handler waitAuthHandler = new Handler();
        private int waitAuthTimes = 10;

        private String authKey;

        public CheckAuthLoginRunnable(String authKey) {
            this.authKey = authKey;
        }

        @Override
        public void run() {
            HttpUtils.get().url(coreManager.getConfig().CHECK_AUTH_LOGIN)
                    .params("authKey", authKey)
                    .build(true, true)
                    .execute(new BaseCallback<LoginRegisterResult>(LoginRegisterResult.class) {
                        @Override
                        public void onResponse(ObjectResult<LoginRegisterResult> result) {
                            if (Result.checkError(result, Result.CODE_AUTH_LOGIN_SCUESS)) {
                                DialogHelper.dismissProgressDialog();
                                login();
                            } else if (Result.checkError(result, Result.CODE_AUTH_LOGIN_FAILED_1)) {
                                waitAuth(CheckAuthLoginRunnable.this);
                            } else {
                                DialogHelper.dismissProgressDialog();
                                if (!TextUtils.isEmpty(result.getResultMsg())) {
                                    ToastUtil.showToast(mContext, result.getResultMsg());
                                } else {
                                    ToastUtil.showToast(mContext, R.string.tip_server_error);
                                }
                            }
                        }

                        @Override
                        public void onError(Call call, Exception e) {
                            DialogHelper.dismissProgressDialog();
                            ToastUtil.showErrorNet(mContext);
                        }
                    });
        }
    }
}
