package com.ktw.bitbit.ui.account;

import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.os.Handler;
import android.text.TextUtils;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.TextView;

import com.ktw.bitbit.FLYAppConstant;
import com.ktw.bitbit.FLYApplication;
import com.ktw.bitbit.R;
import com.ktw.bitbit.bean.LoginRegisterResult;
import com.ktw.bitbit.bean.User;
import com.ktw.bitbit.bean.event.MessageLogin;
import com.ktw.bitbit.db.dao.UserDao;
import com.ktw.bitbit.helper.AvatarHelper;
import com.ktw.bitbit.helper.DialogHelper;
import com.ktw.bitbit.helper.LoginHelper;
import com.ktw.bitbit.helper.LoginSecureHelper;
import com.ktw.bitbit.helper.PasswordHelper;
import com.ktw.bitbit.helper.PrivacySettingHelper;
import com.ktw.bitbit.sp.UserSp;
import com.ktw.bitbit.ui.base.ActivityStack;
import com.ktw.bitbit.ui.base.BaseActivity;
import com.ktw.bitbit.ui.tool.ButtonColorChange;
import com.ktw.bitbit.util.Constants;
import com.ktw.bitbit.util.DeviceInfoUtil;
import com.ktw.bitbit.util.EventBusHelper;
import com.ktw.bitbit.util.Md5Util;
import com.ktw.bitbit.util.PreferenceUtils;
import com.ktw.bitbit.util.ToastUtil;
import com.xuan.xuanhttplibrary.okhttp.HttpUtils;
import com.xuan.xuanhttplibrary.okhttp.callback.BaseCallback;
import com.xuan.xuanhttplibrary.okhttp.result.ObjectResult;
import com.xuan.xuanhttplibrary.okhttp.result.Result;

import java.util.HashMap;

import de.greenrobot.event.Subscribe;
import de.greenrobot.event.ThreadMode;
import okhttp3.Call;

/**
 * 历史登陆界面
 */

public class LoginHistoryActivity extends BaseActivity implements View.OnClickListener {
    private ImageView mAvatarImgView;
    private TextView mNickNameTv;
    private EditText mPasswordEdit;
    private int mobilePrefix = 86;
    private User mLastLoginUser;

    public LoginHistoryActivity() {
        noLoginRequired();
    }

    public static void start(Context ctx) {
        Intent intent = new Intent(ctx, LoginHistoryActivity.class);
        // 清空activity栈，
        // 重建期间白屏，暂且放弃，
        ctx.startActivity(intent);
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_login_history);
        PreferenceUtils.putBoolean(this, Constants.LOGIN_CONFLICT, false);// 重置登录冲突记录
        String userId = UserSp.getInstance(this).getUserId("");
        mLastLoginUser = UserDao.getInstance().getUserByUserId(userId);
        if (!LoginHelper.isUserValidation(mLastLoginUser)) {
            Intent intent = new Intent(this, LoginActivity.class);
            startActivity(intent);
            finish();
            return;
        }
        initActionBar();
        initView();
        EventBusHelper.register(this);
    }

    @Override
    public void onBackPressed() {
        ActivityStack.getInstance().exit();
    }

    private void initActionBar() {
        getSupportActionBar().hide();
        findViewById(R.id.iv_title_left).setVisibility(View.GONE);
        TextView tv1 = (TextView) findViewById(R.id.tv_title_left);
        TextView tv2 = (TextView) findViewById(R.id.tv_title_right);
        tv1.setText(R.string.app_name);
        tv2.setText(R.string.switch_account);
        tv2.setOnClickListener(v -> {
            Intent intent = new Intent(LoginHistoryActivity.this, LoginActivity.class);
            startActivity(intent);
        });
    }

    private void initView() {
        mAvatarImgView = (ImageView) findViewById(R.id.avatar_img);
        mNickNameTv = (TextView) findViewById(R.id.nick_name_tv);
        mPasswordEdit = (EditText) findViewById(R.id.password_edit);
        PasswordHelper.bindPasswordEye(mPasswordEdit, findViewById(R.id.tbEye));
        mobilePrefix = PreferenceUtils.getInt(this, Constants.AREA_CODE_KEY, mobilePrefix);
        Button loginBtn, registerBtn, forgetPasswordBtn;
        loginBtn = (Button) findViewById(R.id.login_btn);
        ButtonColorChange.colorChange(this, loginBtn);
        loginBtn.setOnClickListener(this);
        registerBtn = (Button) findViewById(R.id.register_account_btn);
        registerBtn.setOnClickListener(this);
        if (coreManager.getConfig().isOpenRegister) {
            registerBtn.setVisibility(View.VISIBLE);
        } else {
            registerBtn.setVisibility(View.GONE);
        }
        forgetPasswordBtn = (Button) findViewById(R.id.forget_password_btn);
        if (coreManager.getConfig().registerUsername) {
            forgetPasswordBtn.setVisibility(View.GONE);
        } else {
            forgetPasswordBtn.setOnClickListener(this);
        }
        loginBtn.setText(getString(R.string.login));
        registerBtn.setText(getString(R.string.register_account));
        forgetPasswordBtn.setText(getString(R.string.forget_password));

        AvatarHelper.getInstance().displayRoundAvatar(mLastLoginUser.getNickName(), mLastLoginUser.getUserId(), mAvatarImgView, true);
        mNickNameTv.setText(mLastLoginUser.getNickName());
        if (PreferenceUtils.getBoolean(this, "iSpassword")) {
            mPasswordEdit.setText(PreferenceUtils.getString(this, "password"));
        }
    }

    @Override
    public void onClick(View v) {
        switch (v.getId()) {
            case R.id.login_btn:
                login();
                break;
            case R.id.register_account_btn:
                startActivity(new Intent(LoginHistoryActivity.this, RegisterActivity.class));
                break;
            case R.id.forget_password_btn:
                startActivity(new Intent(this, FindPwdActivity.class));
                break;
        }
    }

    @Subscribe(threadMode = ThreadMode.MainThread)
    public void helloEventBus(MessageLogin message) {
        finish();
    }

    private void login() {
        PreferenceUtils.putInt(this, Constants.AREA_CODE_KEY, mobilePrefix);
        String password = mPasswordEdit.getText().toString().trim();
        if (TextUtils.isEmpty(password)) {
            return;
        }
        final String digestPwd = new String(Md5Util.toMD5(password));

        DialogHelper.showDefaulteMessageProgressDialog(this);
        HashMap<String, String> params = new HashMap<>();
        String account = mLastLoginUser.getTelephoneNoAreaCode();
        params.put("xmppVersion", "1");
        // 附加信息
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

        if (account.contains("@")) {  //邮箱登录
            LoginSecureHelper.secureLoginEmail(
                    this, coreManager, String.valueOf(mobilePrefix), account, password,
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
                            CheckAuthLoginRunnable authLogin = new CheckAuthLoginRunnable(result.getData().getAuthKey(), account, digestPwd);
                            waitAuth(authLogin);
                            return;
                        }
                        afterLogin(result, account, digestPwd);
                    });
        } else {//手机号登录
            LoginSecureHelper.secureLogin(
                    this, coreManager, String.valueOf(mobilePrefix), account, password,
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
                            CheckAuthLoginRunnable authLogin = new CheckAuthLoginRunnable(result.getData().getAuthKey(), account, digestPwd);
                            waitAuth(authLogin);
                            return;
                        }
                        afterLogin(result, account, digestPwd);
                    });
        }


    }

    private void afterLogin(ObjectResult<LoginRegisterResult> result, String phoneNumber, String digestPwd) {
        boolean success = LoginHelper.setLoginUser(mContext, coreManager, phoneNumber, digestPwd, result);
        if (success) {
            LoginRegisterResult.Settings settings = result.getData().getSettings();
            FLYApplication.getInstance().initPayPassword(result.getData().getUserId(), result.getData().getPayPassword());
            PrivacySettingHelper.setPrivacySettings(LoginHistoryActivity.this, settings);
            FLYApplication.getInstance().initMulti();

            // 登陆成功
            DataDownloadActivity.start(mContext, result.getData().getIsupdate());
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
