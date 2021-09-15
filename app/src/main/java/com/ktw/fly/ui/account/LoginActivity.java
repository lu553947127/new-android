package com.ktw.fly.ui.account;

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
import android.widget.TextView;
import android.widget.Toast;

import com.alibaba.fastjson.JSON;
import com.ktw.fly.wxapi.WXEntryActivity;
import com.ktw.fly.FLYAppConfig;
import com.ktw.fly.FLYAppConstant;
import com.ktw.fly.BuildConfig;
import com.ktw.fly.FLYApplication;
import com.ktw.fly.R;
import com.ktw.fly.bean.LoginRegisterResult;
import com.ktw.fly.bean.QQLoginResult;
import com.ktw.fly.bean.WXUploadResult;
import com.ktw.fly.bean.event.MessageLogin;
import com.ktw.fly.helper.DialogHelper;
import com.ktw.fly.helper.LoginHelper;
import com.ktw.fly.helper.LoginSecureHelper;
import com.ktw.fly.helper.PasswordHelper;
import com.ktw.fly.helper.PrivacySettingHelper;
import com.ktw.fly.helper.QQHelper;
import com.ktw.fly.helper.UsernameHelper;
import com.ktw.fly.ui.base.BaseActivity;
import com.ktw.fly.ui.me.SetConfigActivity;
import com.ktw.fly.util.AppUtils;
import com.ktw.fly.util.Constants;
import com.ktw.fly.util.DeviceInfoUtil;
import com.ktw.fly.util.EventBusHelper;
import com.ktw.fly.util.PermissionUtil;
import com.ktw.fly.util.PreferenceUtils;
import com.ktw.fly.util.ToastUtil;
import com.ktw.fly.util.secure.LoginPassword;
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
 * 登陆界面
 *
 * @author Dean Tao
 * @version 1.0
 */
public class LoginActivity extends BaseActivity implements View.OnClickListener {
    public static final String THIRD_TYPE_WECHAT = "2";
    public static final String THIRD_TYPE_QQ = "1";

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
            // 第三方进来直接登录，
            // 清空手机号以标记是第三方登录，
            mPhoneNumberEdit.setText("");
            login(true);
        }
        EventBusHelper.register(this);
    }

    @Override
    protected void onResume() {
        super.onResume();
        // 如果没有保存用户定位信息，那么去地位用户当前位置
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
        checkBox=findViewById(R.id.checkbox_password);
        if (TextUtils.isEmpty(thirdToken)) {
            tvTitle.setText(getString(R.string.login));
        } else {
            // 第三方登录的不要提示登录，而是绑定手机号码，
            tvTitle.setText(getString(R.string.bind_old_account));
        }
        TextView tvRight = (TextView) findViewById(R.id.tv_title_right);
        // 定制包隐藏设置服务器按钮，
        if (!FLYAppConfig.isShiku() || !BuildConfig.DEBUG) {
            // 为方便测试，留个启用方法，adb shell命令运行"setprop log.tag.ShikuServer D"启用，
            if (!Log.isLoggable("ShikuServer", Log.DEBUG)) {
                tvRight.setVisibility(View.GONE);
            }
        }
        // 隐藏开关，方便测试人员调试
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
        mPhoneNumberEdit = (EditText) findViewById(R.id.phone_numer_edit);
        mPasswordEdit = (EditText) findViewById(R.id.password_edit);
        PasswordHelper.bindPasswordEye(mPasswordEdit, findViewById(R.id.tbEye));
        tv_prefix = (TextView) findViewById(R.id.tv_prefix);
        if (coreManager.getConfig().registerUsername) {
            tv_prefix.setVisibility(View.GONE);
        } else {
            tv_prefix.setOnClickListener(this);
        }
        mobilePrefix = PreferenceUtils.getInt(this, Constants.AREA_CODE_KEY, mobilePrefix);
        tv_prefix.setText("+" + mobilePrefix);

        // 登陆账号
        loginBtn = (Button) findViewById(R.id.login_btn);
        // loginBtn.setBackgroundColor(SkinUtils.getSkin(this).getAccentColor());
        loginBtn.setOnClickListener(this);
        // 注册账号
        registerBtn = (Button) findViewById(R.id.register_account_btn);
        if (coreManager.getConfig().isOpenRegister) {
            if (TextUtils.isEmpty(thirdToken)) {
                registerBtn.setOnClickListener(this);
            } else {
                // 第三方登录的不需要这个注册按钮，登录后没有账号直接跳到注册，
                registerBtn.setVisibility(View.GONE);
            }
        } else {
            registerBtn.setVisibility(View.GONE);
        }
        // 忘记密码
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
        registerBtn.setText(getString(R.string.register));
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
            // 开启用户名注册登录的情况隐藏短信登录，
            findViewById(R.id.sms_login_fl).setVisibility(View.GONE);
        }

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
                // 登陆
                if (checkBox.isChecked()){
                    String phoneNumber = mPhoneNumberEdit.getText().toString().trim();
                    String password = mPasswordEdit.getText().toString().trim();
                    PreferenceUtils.putString(this,"password",password);
                    PreferenceUtils.putBoolean(this,"iSpassword",true);
                }else {

                    PreferenceUtils.putBoolean(this,"iSpassword",false);

                }
                login(false);
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
                // 注册
                register();
                break;
            case R.id.forget_password_btn:
                // 忘记密码
                startActivity(new Intent(mContext, FindPwdActivity.class));
                break;
            case R.id.sms_login_btn:
                Intent iS = new Intent(LoginActivity.this, SwitchLoginActivity.class);
                iS.putExtra("thirdTokenLogin", thirdToken);
                startActivity(iS);
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

    private void register() {
        RegisterActivity.registerFromThird(
                this,
                mobilePrefix,
                mPhoneNumberEdit.getText().toString(),
                mPasswordEdit.getText().toString(),
                thirdToken,
                thirdTokenType
        );
    }

    /**
     * @param third 第三方自动登录，
     */
    private void login(boolean third) {
        this.third = third;
        login();
    }

    private void login() {
        PreferenceUtils.putInt(this, Constants.AREA_CODE_KEY, mobilePrefix);
        final String phoneNumber = mPhoneNumberEdit.getText().toString().trim();
        String password = mPasswordEdit.getText().toString().trim();

        if (TextUtils.isEmpty(thirdToken)) {
            // 第三方登录的不处理账号密码，
            if (TextUtils.isEmpty(phoneNumber) && TextUtils.isEmpty(password)) {
                Toast.makeText(mContext, getString(R.string.please_input_account_and_password), Toast.LENGTH_SHORT).show();
                return;
            }

            if (TextUtils.isEmpty(phoneNumber)) {
                Toast.makeText(mContext, getString(R.string.please_input_account), Toast.LENGTH_SHORT).show();
                return;
            }

            if (TextUtils.isEmpty(password)) {
                Toast.makeText(mContext, getString(R.string.input_pass_word), Toast.LENGTH_SHORT).show();
                return;
            }
        }

        // 加密之后的密码
        final String digestPwd = LoginPassword.encodeMd5(password);

        DialogHelper.showDefaulteMessageProgressDialog(this);

        Map<String, String> params = new HashMap<>();
        params.put("xmppVersion", "1");
        // 附加信息+
        params.put("model", DeviceInfoUtil.getModel());
        params.put("osVersion", DeviceInfoUtil.getOsVersion());
        params.put("serial", DeviceInfoUtil.getDeviceId(mContext));
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

        LoginSecureHelper.secureLogin(
                        this, coreManager, String.valueOf(mobilePrefix), phoneNumber, password, thirdToken, thirdTokenType, third,
                        params,
                t -> {
                    DialogHelper.dismissProgressDialog();
                    ToastUtil.showToast(this, this.getString(R.string.tip_login_secure_place_holder, t.getMessage()));
                }, result -> {
                    DialogHelper.dismissProgressDialog();
                    if (!Result.checkSuccess(getApplicationContext(), result)) {
                        if (Result.checkError(result, Result.CODE_THIRD_NO_EXISTS)) {
                            // 如果返回1040306表示这个IM账号不存在，跳到注册页面让用户走注册IM账号并绑定微信，
                            register();
                        } else if (Result.checkError(result, Result.CODE_THIRD_NO_PHONE)) {
                            // 微信没有绑定IM账号，跳到注册，注册页有回来登录老账号的按钮，
                            register();
                            finish();
                        }
                        return;
                    }
                    if (!TextUtils.isEmpty(result.getData().getAuthKey())) {
                        DialogHelper.showMessageProgressDialog(mContext, getString(R.string.tip_need_auth_login));
                        CheckAuthLoginRunnable authLogin = new CheckAuthLoginRunnable(result.getData().getAuthKey(), phoneNumber, digestPwd);
                        waitAuth(authLogin);
                        return;
                    }
                    afterLogin(result, phoneNumber, digestPwd);
                }
        );
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
        } else { //  登录出错 || 用户资料不全
            String message = TextUtils.isEmpty(result.getResultMsg()) ? getString(R.string.tip_incomplete_information) : result.getResultMsg();
            ToastUtil.showToast(mContext, message);
        }
    }

    private void waitAuth(CheckAuthLoginRunnable authLogin) {
        authLogin.waitAuthHandler.postDelayed(authLogin, 3000);
    }

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
