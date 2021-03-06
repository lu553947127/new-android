package com.ktw.bitbit.ui.account;

import android.annotation.SuppressLint;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.os.Bundle;
import android.os.Handler;
import android.text.TextUtils;
import android.util.Log;
import android.view.View;
import android.view.inputmethod.InputMethodManager;
import android.widget.Button;
import android.widget.CheckBox;
import android.widget.EditText;
import android.widget.RadioGroup;
import android.widget.TextView;
import android.widget.Toast;

import com.alibaba.fastjson.JSON;
import com.blankj.utilcode.util.ToastUtils;
import com.ktw.bitbit.BuildConfig;
import com.ktw.bitbit.FLYAppConfig;
import com.ktw.bitbit.FLYAppConstant;
import com.ktw.bitbit.FLYApplication;
import com.ktw.bitbit.R;
import com.ktw.bitbit.bean.FirstLoginBean;
import com.ktw.bitbit.bean.LoginRegisterResult;
import com.ktw.bitbit.bean.QQLoginResult;
import com.ktw.bitbit.bean.User;
import com.ktw.bitbit.bean.WXUploadResult;
import com.ktw.bitbit.bean.event.MessageLogin;
import com.ktw.bitbit.helper.DialogHelper;
import com.ktw.bitbit.helper.LoginHelper;
import com.ktw.bitbit.helper.LoginSecureHelper;
import com.ktw.bitbit.helper.PasswordHelper;
import com.ktw.bitbit.helper.PrivacySettingHelper;
import com.ktw.bitbit.helper.QQHelper;
import com.ktw.bitbit.helper.UsernameHelper;
import com.ktw.bitbit.ui.base.BaseActivity;
import com.ktw.bitbit.ui.me.SetConfigActivity;
import com.ktw.bitbit.util.AppUtils;
import com.ktw.bitbit.util.Constants;
import com.ktw.bitbit.util.DeviceInfoUtil;
import com.ktw.bitbit.util.EventBusHelper;
import com.ktw.bitbit.util.LogUtils;
import com.ktw.bitbit.util.PermissionUtil;
import com.ktw.bitbit.util.PreferenceUtils;
import com.ktw.bitbit.util.TimeUtils;
import com.ktw.bitbit.util.ToastUtil;
import com.ktw.bitbit.util.secure.LoginPassword;
import com.ktw.bitbit.wxapi.WXEntryActivity;
import com.tencent.tauth.Tencent;
import com.xuan.xuanhttplibrary.okhttp.HttpUtils;
import com.xuan.xuanhttplibrary.okhttp.callback.BaseCallback;
import com.xuan.xuanhttplibrary.okhttp.result.ObjectResult;
import com.xuan.xuanhttplibrary.okhttp.result.Result;

import java.util.HashMap;
import java.util.Map;

import de.greenrobot.event.Subscribe;
import de.greenrobot.event.ThreadMode;
import okhttp3.Call;

/**
 * ????????????
 *
 * @author Dean Tao
 * @version 1.0
 */
public class LoginActivity extends BaseActivity implements View.OnClickListener {
    public static final String THIRD_TYPE_WECHAT = "2";
    public static final String THIRD_TYPE_QQ = "1";

    //?????????????????????????????????
    public int mLoginType = 0;

    private EditText mPhoneNumberEdit;
    private EditText mPasswordEdit;
    private TextView tv_prefix;
    private int mobilePrefix = 86;
    private String thirdToken;
    private CheckBox checkBox;
    private String thirdTokenType;
    private BroadcastReceiver broadcastReceiver = new BroadcastReceiver() {
        @Override
        public void onReceive(Context context, Intent intent) {
            finish();
        }
    };
    private Button forgetPasswordBtn, registerBtn, loginBtn;
    private boolean third;
    private EditText mEmailPasswordEdit;
    private EditText mEmailEdit;
    private User mTempData;

    public LoginActivity() {
        noLoginRequired();
    }

    public static void bindThird(Context ctx, String thirdToken, String thirdTokenType, boolean testLogin) {
        Intent intent = new Intent(ctx, LoginActivity.class);
        intent.putExtra("thirdToken", thirdToken);
        intent.putExtra("thirdTokenType", thirdTokenType);
        intent.putExtra("testLogin", testLogin);
        ctx.startActivity(intent);
    }

    public static void bindThird(Context ctx, String thirdToken, String thirdTokenType) {
        bindThird(ctx, thirdToken, thirdTokenType, false);
    }

    public static void bindThird(Context ctx, WXUploadResult thirdToken) {
        bindThird(ctx, JSON.toJSONString(thirdToken), THIRD_TYPE_WECHAT, true);
    }

    public static void bindThird(Context ctx, QQLoginResult thirdToken) {
        bindThird(ctx, JSON.toJSONString(thirdToken), THIRD_TYPE_QQ, true);
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_login);
        PermissionUtil.requestLocationPermissions(this, 0x01);

        thirdToken = getIntent().getStringExtra("thirdToken");
        thirdTokenType = getIntent().getStringExtra("thirdTokenType");
        initActionBar();
        initView();

        IntentFilter filter = new IntentFilter();
        filter.addAction("CHANGE_CONFIG");
        registerReceiver(broadcastReceiver, filter);

        if (!TextUtils.isEmpty(thirdToken) && getIntent().getBooleanExtra("testLogin", false)) {
            // ??????????????????????????????
            // ?????????????????????????????????????????????
            mPhoneNumberEdit.setText("");
            login(true);
        }
        EventBusHelper.register(this);
    }

    @Override
    protected void onResume() {
        super.onResume();
        // ????????????????????????????????????????????????????????????????????????
        if (!FLYApplication.getInstance().getBdLocationHelper().isLocationUpdate()) {
            FLYApplication.getInstance().getBdLocationHelper().requestLocation();
        }
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        unregisterReceiver(broadcastReceiver);
    }

    private void initActionBar() {
        getSupportActionBar().hide();
        findViewById(R.id.iv_title_left).setVisibility(View.GONE);
        TextView tvTitle = (TextView) findViewById(R.id.tv_title_center);
        checkBox = findViewById(R.id.checkbox_password);
        if (TextUtils.isEmpty(thirdToken)) {
            tvTitle.setText(getString(R.string.login));
        } else {
            // ??????????????????????????????????????????????????????????????????
            tvTitle.setText(getString(R.string.bind_old_account));
        }
        TextView tvRight = (TextView) findViewById(R.id.tv_title_right);
        // ???????????????????????????????????????
        if (!FLYAppConfig.isShiku() || !BuildConfig.DEBUG) {
            // ???????????????????????????????????????adb shell????????????"setprop log.tag.ShikuServer D"?????????
            if (!Log.isLoggable("ShikuServer", Log.DEBUG)) {
                tvRight.setVisibility(View.GONE);
            }
        }
        // ???????????????????????????????????????
        tvTitle.setOnLongClickListener(v -> {
            tvRight.setVisibility(View.VISIBLE);
            return false;
        });
        tvRight.setText(R.string.settings_server_address);
        tvRight.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Intent intent = new Intent(LoginActivity.this, SetConfigActivity.class);
                startActivity(intent);
            }
        });
    }

    private void initView() {

        loginType();

        mPhoneNumberEdit = (EditText) findViewById(R.id.phone_numer_edit);
        mPasswordEdit = (EditText) findViewById(R.id.password_edit);
        mEmailEdit = (EditText) findViewById(R.id.email_edit);
        mEmailPasswordEdit = (EditText) findViewById(R.id.email_password_edit);
        PasswordHelper.bindPasswordEye(mPasswordEdit, findViewById(R.id.tbEye));
        PasswordHelper.bindPasswordEye(mEmailPasswordEdit, findViewById(R.id.tb_email_Eye));
        tv_prefix = (TextView) findViewById(R.id.tv_prefix);
        if (coreManager.getConfig().registerUsername) {
            tv_prefix.setVisibility(View.GONE);
        } else {
            tv_prefix.setOnClickListener(this);
        }
        mobilePrefix = PreferenceUtils.getInt(this, Constants.AREA_CODE_KEY, mobilePrefix);
        tv_prefix.setText("+" + mobilePrefix);

        // ????????????
        loginBtn = (Button) findViewById(R.id.login_btn);
        // loginBtn.setBackgroundColor(SkinUtils.getSkin(this).getAccentColor());
        loginBtn.setOnClickListener(this);
        // ????????????
        registerBtn = (Button) findViewById(R.id.register_account_btn);
        if (coreManager.getConfig().isOpenRegister) {
            if (TextUtils.isEmpty(thirdToken)) {
                registerBtn.setOnClickListener(this);
            } else {
                // ??????????????????????????????????????????????????????????????????????????????????????????
                registerBtn.setVisibility(View.GONE);
            }
        } else {
            registerBtn.setVisibility(View.GONE);
        }
        // ????????????
        forgetPasswordBtn = (Button) findViewById(R.id.forget_password_btn);
        if (!TextUtils.isEmpty(thirdToken) || coreManager.getConfig().registerUsername) {
            forgetPasswordBtn.setVisibility(View.GONE);
        }
/*
        forgetPasswordBtn.setTextColor(SkinUtils.getSkin(this).getAccentColor());
*/
        forgetPasswordBtn.setOnClickListener(this);
        UsernameHelper.initEditText(mPhoneNumberEdit, coreManager.getConfig().registerUsername);
        // mPasswordEdit.setHint(InternationalizationHelper.getString("JX_InputPassWord"));
        loginBtn.setText(getString(R.string.login));
        registerBtn.setText(getString(R.string.phone_register));
        forgetPasswordBtn.setText(getString(R.string.forget_password));

        findViewById(R.id.sms_login_btn).setOnClickListener(this);

        if (TextUtils.isEmpty(thirdToken)) {
            findViewById(R.id.wx_login_btn).setOnClickListener(this);
            if (QQHelper.ENABLE) {
                findViewById(R.id.qq_login_btn).setOnClickListener(this);
            } else {
                findViewById(R.id.qq_login_fl).setVisibility(View.GONE);
            }
        } else {
            findViewById(R.id.wx_login_fl).setVisibility(View.GONE);
            findViewById(R.id.qq_login_fl).setVisibility(View.GONE);
        }

        findViewById(R.id.main_content).setOnClickListener(this);

        if (!coreManager.getConfig().thirdLogin) {
            findViewById(R.id.wx_login_fl).setVisibility(View.GONE);
            findViewById(R.id.qq_login_fl).setVisibility(View.GONE);
        }

        if (coreManager.getConfig().registerUsername) {
            // ?????????????????????????????????????????????????????????
            findViewById(R.id.sms_login_fl).setVisibility(View.GONE);
        }

    }

    /**
     * ??????????????????
     */
    private void loginType() {
        RadioGroup rgLogin = findViewById(R.id.rg_login);
        rgLogin.setOnCheckedChangeListener((group, checkedId) -> {
            switch (checkedId) {
                case R.id.rb_phone:
                    findViewById(R.id.ll_phone).setVisibility(View.VISIBLE);
                    findViewById(R.id.ll_email).setVisibility(View.GONE);
                    registerBtn.setText(getString(R.string.phone_register));
                    mLoginType = LoginHelper.LOGIN_PHONE;
                    break;
                case R.id.rb_email:
                    findViewById(R.id.ll_phone).setVisibility(View.GONE);
                    findViewById(R.id.ll_email).setVisibility(View.VISIBLE);
                    registerBtn.setText(getString(R.string.email_register));
                    mLoginType = LoginHelper.LOGIN_EMAIL;
                    break;
            }
        });
    }

    @Override
    public void onClick(View v) {
        switch (v.getId()) {
            case R.id.tv_prefix:
                // ??????????????????
                Intent intent = new Intent(this, SelectPrefixActivity.class);
                startActivityForResult(intent, SelectPrefixActivity.REQUEST_MOBILE_PREFIX_LOGIN);
                break;
            case R.id.login_btn:
                // ??????
                if (checkBox.isChecked()) {
                    String phoneNumber = mPhoneNumberEdit.getText().toString().trim();

                    String password = mLoginType == LoginHelper.LOGIN_PHONE ?
                            mPasswordEdit.getText().toString().trim() :
                            mEmailPasswordEdit.getText().toString().trim();
                    PreferenceUtils.putString(this, "password", password);
                    PreferenceUtils.putBoolean(this, "iSpassword", true);
                } else {
                    PreferenceUtils.putBoolean(this, "iSpassword", false);
                }

                firstLogin();
//                login(false);
                break;
            case R.id.wx_login_btn:
                if (!AppUtils.isAppInstalled(mContext, "com.tencent.mm")) {
                    Toast.makeText(mContext, getString(R.string.tip_no_wx_chat), Toast.LENGTH_SHORT).show();
                } else {
                    WXEntryActivity.wxLogin(this);
                }
                break;
            case R.id.qq_login_btn:
                if (!QQHelper.qqInstalled(mContext)) {
                    Toast.makeText(mContext, getString(R.string.tip_no_qq_chat), Toast.LENGTH_SHORT).show();
                } else {
                    QQHelper.qqLogin(this);
                }
                break;
            case R.id.register_account_btn:
                // ??????
                register();
                break;
            case R.id.forget_password_btn:
                // ????????????
                Intent findIntent = new Intent(mContext, FindPwdActivity.class);
                findIntent.putExtra("loginType", mLoginType);
                startActivity(findIntent);
                break;
            case R.id.sms_login_btn:
                Intent iS = new Intent(LoginActivity.this, SwitchLoginActivity.class);
                iS.putExtra("thirdTokenLogin", thirdToken);
                startActivity(iS);
                break;
            case R.id.main_content:
                // ?????????????????????????????????
                InputMethodManager inputManager = (InputMethodManager) getSystemService(INPUT_METHOD_SERVICE);
                if (inputManager != null) {
                    inputManager.hideSoftInputFromWindow(findViewById(R.id.main_content).getWindowToken(), 0); //??????????????????
                }
                break;
        }
    }

    private void register() {
        RegisterActivity.registerFromThird(
                this,
                mobilePrefix,
                mLoginType == LoginHelper.LOGIN_PHONE ? mPhoneNumberEdit.getText().toString() : mEmailEdit.getText().toString(),
                mLoginType == LoginHelper.LOGIN_PHONE ? mPasswordEdit.getText().toString() : mEmailPasswordEdit.getText().toString(),
                thirdToken,
                thirdTokenType,
                mLoginType
        );
    }

    /**
     * ????????????????????????
     */
    private void firstLogin(){
        final String account = mPhoneNumberEdit.getText().toString().trim();
        final String password = mPasswordEdit.getText().toString().trim();

        if (TextUtils.isEmpty(account) && TextUtils.isEmpty(password)) {
            Toast.makeText(mContext, getString(R.string.please_input_account_and_password), Toast.LENGTH_SHORT).show();
            return;
        }

        if (TextUtils.isEmpty(account)) {
            Toast.makeText(mContext, getString(R.string.please_input_account), Toast.LENGTH_SHORT).show();
            return;
        }

        if (TextUtils.isEmpty(password)) {
            Toast.makeText(mContext, getString(R.string.input_pass_word), Toast.LENGTH_SHORT).show();
            return;
        }

        DialogHelper.showDefaulteMessageProgressDialog(this);

        Map<String, String> params = new HashMap<>();
        params.put("mobile", account);
        params.put("password", password);
        HttpUtils.post().url(coreManager.getConfig().FIRST_LOGIN)
                .params(params)
                .build()
                .execute(new BaseCallback<FirstLoginBean>(FirstLoginBean.class) {

                    @Override
                    public void onResponse(ObjectResult<FirstLoginBean> result) {
                        if (result == null) {
                            return;
                        }

                        if (result.getResultCode() == 1){
                            verifyPhoneNumber(account, result.getData().getNickname());
                        }else {
                            DialogHelper.dismissProgressDialog();
                            ToastUtils.showShort(result.getResultMsg());
                        }
                    }

                    @Override
                    public void onError(Call call, Exception e) {
                        DialogHelper.dismissProgressDialog();
                        ToastUtil.showNetError(LoginActivity.this);
                    }
                });
    }

    /**
     * ?????????????????????????????????
     *
     * @param phoneNumber
     */
    private void verifyPhoneNumber(String phoneNumber, String name) {
        if (!UsernameHelper.verify(this, phoneNumber, coreManager.getConfig().registerUsername, mLoginType)) {
            return;
        }
        Map<String, String> params = new HashMap<>();
        params.put("telephone", phoneNumber);
        params.put("areaCode", "" + mobilePrefix);

        HttpUtils.get().url(coreManager.getConfig().VERIFY_TELEPHONE)
                .params(params)
                .build(true, true)
                .execute(new BaseCallback<Void>(Void.class) {

                    @Override
                    public void onResponse(ObjectResult<Void> result) {
                        DialogHelper.dismissProgressDialog();
                        if (result == null) {
                            ToastUtil.showToast(LoginActivity.this,
                                    R.string.data_exception);
                            return;
                        }

                        if (result.getResultCode() == 1) {
                            registerNew(name);
                        } else {
                            login(false);
                        }
                    }

                    @Override
                    public void onError(Call call, Exception e) {
                        DialogHelper.dismissProgressDialog();
                        ToastUtil.showErrorNet(LoginActivity.this);
                    }
                });
    }

    /**
     * ??????????????????
     *
     * @param name
     */
    private void registerNew(String name) {
        if (mTempData == null) {
            mTempData = new User();
            mTempData.setNickName(name);
            mTempData.setSex(1);
            mTempData.setBirthday(TimeUtils.sk_time_current_time() / 1000);
        }

        final String account = mPhoneNumberEdit.getText().toString().trim();
        final String password = mPasswordEdit.getText().toString().trim();

        DialogHelper.showDefaulteMessageProgressDialog(this);

        Map<String, String> params = new HashMap<>();
        params.put("nickname", name);
        params.put("telephone", account);
        params.put("password", password);

        HttpUtils.get().url(coreManager.getConfig().FIRST_REGISTER)
                .params(params)
                .build(true, true)
                .execute(new BaseCallback<Void>(Void.class) {

                    @Override
                    public void onResponse(ObjectResult<Void> result) {
                        DialogHelper.dismissProgressDialog();
                        if (result == null) {
                            ToastUtil.showToast(LoginActivity.this,
                                    R.string.data_exception);
                            return;
                        }

                        login(false);
                    }

                    @Override
                    public void onError(Call call, Exception e) {
                        DialogHelper.dismissProgressDialog();
                        ToastUtil.showErrorNet(LoginActivity.this);
                    }
                });
    }

    /**
     * @param third ????????????????????????
     */
    private void login(boolean third) {
        this.third = third;
        login();
    }

    private void login() {
        PreferenceUtils.putInt(this, Constants.AREA_CODE_KEY, mobilePrefix);

        final String account = mLoginType == LoginHelper.LOGIN_PHONE ?
                mPhoneNumberEdit.getText().toString().trim() :
                mEmailEdit.getText().toString().trim();

        String password = mLoginType == LoginHelper.LOGIN_PHONE ?
                mPasswordEdit.getText().toString().trim() :
                mEmailPasswordEdit.getText().toString().trim();

        if (TextUtils.isEmpty(thirdToken)) {
            // ??????????????????????????????????????????
            if (TextUtils.isEmpty(account) && TextUtils.isEmpty(password)) {
                Toast.makeText(mContext, getString(R.string.please_input_account_and_password), Toast.LENGTH_SHORT).show();
                return;
            }

            if (TextUtils.isEmpty(account)) {
                Toast.makeText(mContext, getString(R.string.please_input_account), Toast.LENGTH_SHORT).show();
                return;
            }

            if (TextUtils.isEmpty(password)) {
                Toast.makeText(mContext, getString(R.string.input_pass_word), Toast.LENGTH_SHORT).show();
                return;
            }
        }

        // ?????????????????????
        final String digestPwd = LoginPassword.encodeMd5(password);

        DialogHelper.showDefaulteMessageProgressDialog(this);

        Map<String, String> params = new HashMap<>();
        params.put("xmppVersion", "1");
        // ????????????+
        params.put("model", DeviceInfoUtil.getModel());
        params.put("osVersion", DeviceInfoUtil.getOsVersion());
        params.put("serial", DeviceInfoUtil.getDeviceId(mContext));
        // ????????????
        double latitude = FLYApplication.getInstance().getBdLocationHelper().getLatitude();
        double longitude = FLYApplication.getInstance().getBdLocationHelper().getLongitude();
        if (latitude != 0)
            params.put("latitude", String.valueOf(latitude));
        if (longitude != 0)
            params.put("longitude", String.valueOf(longitude));

        if (FLYApplication.IS_OPEN_CLUSTER) {// ?????????????????????
            String area = PreferenceUtils.getString(this, FLYAppConstant.EXTRA_CLUSTER_AREA);
            if (!TextUtils.isEmpty(area)) {
                params.put("area", area);
            }
        }

        if (mLoginType == LoginHelper.LOGIN_PHONE) {
            LoginSecureHelper.secureLogin(
                    this, coreManager,
                    String.valueOf(mobilePrefix),
                    account, password,
                    thirdToken, thirdTokenType,
                    third,
                    params,
                    t -> {
                        DialogHelper.dismissProgressDialog();
                        LogUtils.log(t.getMessage());
                        ToastUtil.showToast(this, this.getString(R.string.tip_login_secure_place_holder, t.getMessage()));
                    }, result -> {
                        DialogHelper.dismissProgressDialog();
                        if (!Result.checkSuccess(getApplicationContext(), result)) {
                            if (Result.checkError(result, Result.CODE_THIRD_NO_EXISTS)) {
                                // ????????????1040306????????????IM??????????????????????????????????????????????????????IM????????????????????????
                                register();
                            } else if (Result.checkError(result, Result.CODE_THIRD_NO_PHONE)) {
                                // ??????????????????IM?????????????????????????????????????????????????????????????????????
                                register();
                                finish();
                            }
                            return;
                        }
                        if (!TextUtils.isEmpty(result.getData().getAuthKey())) {
                            DialogHelper.showMessageProgressDialog(mContext, getString(R.string.tip_need_auth_login));
                            CheckAuthLoginRunnable authLogin = new CheckAuthLoginRunnable(result.getData().getAuthKey(), account, digestPwd);
                            waitAuth(authLogin);
                            return;
                        }
                        afterLogin(result, account, digestPwd);
                    }
            );
        } else {
            LoginSecureHelper.secureEmailLogin(
                    this, coreManager,
                    String.valueOf(mobilePrefix),
                    account, password,
                    thirdToken, thirdTokenType,
                    third,
                    params,
                    t -> {
                        DialogHelper.dismissProgressDialog();
                        ToastUtil.showToast(this, this.getString(R.string.tip_login_secure_place_holder, t.getMessage()));
                    }, result -> {
                        DialogHelper.dismissProgressDialog();
                        if (!Result.checkSuccess(getApplicationContext(), result)) {
                            if (Result.checkError(result, Result.CODE_THIRD_NO_EXISTS)) {
                                // ????????????1040306????????????IM??????????????????????????????????????????????????????IM????????????????????????
                                register();
                            } else if (Result.checkError(result, Result.CODE_THIRD_NO_PHONE)) {
                                // ??????????????????IM?????????????????????????????????????????????????????????????????????
                                register();
                                finish();
                            }
                            return;
                        }
                        if (!TextUtils.isEmpty(result.getData().getAuthKey())) {
                            DialogHelper.showMessageProgressDialog(mContext, getString(R.string.tip_need_auth_login));
                            CheckAuthLoginRunnable authLogin = new CheckAuthLoginRunnable(result.getData().getAuthKey(), account, digestPwd);
                            waitAuth(authLogin);
                            return;
                        }
                        afterLogin(result, account, digestPwd);
                    }
            );
        }
    }

    private void afterLogin(ObjectResult<LoginRegisterResult> result, String phoneNumber, String digestPwd) {
        boolean success = LoginHelper.setLoginUser(mContext, coreManager, phoneNumber, digestPwd, result);
        if (success) {
            LoginRegisterResult.Settings settings = result.getData().getSettings();
            FLYApplication.getInstance().initPayPassword(result.getData().getUserId(), result.getData().getPayPassword());
            PrivacySettingHelper.setPrivacySettings(LoginActivity.this, settings);
            FLYApplication.getInstance().initMulti();

            // startActivity(new Intent(mContext, DataDownloadActivity.class));
            DataDownloadActivity.start(mContext, result.getData().getIsupdate());
            finish();
        } else { //  ???????????? || ??????????????????
            String message = TextUtils.isEmpty(result.getResultMsg()) ? getString(R.string.tip_incomplete_information) : result.getResultMsg();
            ToastUtil.showToast(mContext, message);
        }
    }

    private void waitAuth(CheckAuthLoginRunnable authLogin) {
        authLogin.waitAuthHandler.postDelayed(authLogin, 3000);
    }

    @SuppressLint("MissingSuperCall")
    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        switch (requestCode) {
            case SelectPrefixActivity.REQUEST_MOBILE_PREFIX_LOGIN:
                if (resultCode != SelectPrefixActivity.RESULT_MOBILE_PREFIX_SUCCESS) {
                    return;
                }
                mobilePrefix = data.getIntExtra(Constants.MOBILE_PREFIX, 86);
                tv_prefix.setText("+" + mobilePrefix);
                break;
            case com.tencent.connect.common.Constants.REQUEST_LOGIN:
            case com.tencent.connect.common.Constants.REQUEST_APPBAR:
                Tencent.onActivityResultData(requestCode, resultCode, data, QQHelper.getLoginListener(mContext));
                break;
        }
    }

    @Subscribe(threadMode = ThreadMode.MainThread)
    public void helloEventBus(MessageLogin message) {
        finish();
    }

    private class CheckAuthLoginRunnable implements Runnable {
        private final String phoneNumber;
        private final String digestPwd;
        private Handler waitAuthHandler = new Handler();
        private int waitAuthTimes = 10;
        private String authKey;

        public CheckAuthLoginRunnable(String authKey, String phoneNumber, String digestPwd) {
            this.authKey = authKey;
            this.phoneNumber = phoneNumber;
            this.digestPwd = digestPwd;
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
