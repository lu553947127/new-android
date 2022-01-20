package com.ktw.bitbit.ui.share;

import android.content.Intent;
import android.os.Bundle;
import android.text.TextUtils;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;

import com.ktw.bitbit.FLYAppConstant;
import com.ktw.bitbit.FLYApplication;
import com.ktw.bitbit.R;
import com.ktw.bitbit.bean.PrivacySetting;
import com.ktw.bitbit.helper.DialogHelper;
import com.ktw.bitbit.helper.LoginHelper;
import com.ktw.bitbit.helper.LoginSecureHelper;
import com.ktw.bitbit.helper.PrivacySettingHelper;
import com.ktw.bitbit.ui.account.DataDownloadActivity;
import com.ktw.bitbit.ui.account.SelectPrefixActivity;
import com.ktw.bitbit.ui.base.BaseActivity;
import com.ktw.bitbit.util.Constants;
import com.ktw.bitbit.util.DeviceInfoUtil;
import com.ktw.bitbit.util.PreferenceUtils;
import com.ktw.bitbit.util.ToastUtil;
import com.ktw.bitbit.util.secure.LoginPassword;
import com.xuan.xuanhttplibrary.okhttp.result.Result;

import java.util.HashMap;
import java.util.Map;

/**
 * 分享 登录
 * 本地未登录需要先登录在分享或授权
 */
public class ShareLoginActivity extends BaseActivity implements View.OnClickListener {
    private EditText mPhoneNumberEdit;
    private TextView tv_prefix;
    private int mobilePrefix = 86;
    private EditText mPasswordEdit;

    public ShareLoginActivity() {
        noLoginRequired();
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_login);
        initActionBar();
        initView();
    }

    @Override
    protected void onResume() {
        super.onResume();
        if (!FLYApplication.getInstance().getBdLocationHelper().isLocationUpdate()) {
            FLYApplication.getInstance().getBdLocationHelper().requestLocation();
        }
    }

    private void initActionBar() {
        getSupportActionBar().hide();
        findViewById(R.id.iv_title_left).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                finish();
            }
        });
        TextView tvTitle = (TextView) findViewById(R.id.tv_title_center);
        tvTitle.setText(getString(R.string.login));
    }

    private void initView() {
        mPhoneNumberEdit = (EditText) findViewById(R.id.phone_numer_edit);
        mPhoneNumberEdit.setHint(getString(R.string.hint_input_phone_number));

        tv_prefix = (TextView) findViewById(R.id.tv_prefix);
        tv_prefix.setOnClickListener(this);
        mobilePrefix = PreferenceUtils.getInt(this, Constants.AREA_CODE_KEY, mobilePrefix);
        tv_prefix.setText("+" + mobilePrefix);

        mPasswordEdit = (EditText) findViewById(R.id.password_edit);

        Button loginBtn = (Button) findViewById(R.id.login_btn);
        loginBtn.setOnClickListener(this);
        loginBtn.setText(getString(R.string.login));

        findViewById(R.id.forget_password_btn).setVisibility(View.GONE);
        findViewById(R.id.register_account_btn).setVisibility(View.GONE);
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
                login();
                break;
        }
    }

    private void login() {
        PreferenceUtils.putInt(this, Constants.AREA_CODE_KEY, mobilePrefix);
        final String phoneNumber = mPhoneNumberEdit.getText().toString().trim();
        String password = mPasswordEdit.getText().toString().trim();
        if (TextUtils.isEmpty(phoneNumber)) {
            Toast.makeText(mContext, getString(R.string.hint_input_phone_number), Toast.LENGTH_SHORT).show();
            return;
        }
        if (TextUtils.isEmpty(password)) {
            Toast.makeText(mContext, getString(R.string.input_pass_word), Toast.LENGTH_SHORT).show();
            return;
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
                this, coreManager, String.valueOf(mobilePrefix), phoneNumber, password,
                params,
                t -> {
                    DialogHelper.dismissProgressDialog();
                    ToastUtil.showToast(this, this.getString(R.string.tip_login_secure_place_holder, t.getMessage()));
                }, result -> {
                    DialogHelper.dismissProgressDialog();
                    if (result == null) {
                        ToastUtil.showErrorData(mContext);
                        return;
                    }
                    boolean success = false;
                    if (result.getResultCode() == Result.CODE_SUCCESS) {
                        success = LoginHelper.setLoginUser(mContext, coreManager, phoneNumber, digestPwd, result);// 设置登陆用户信息
                    }
                    if (success) {
                        PrivacySetting settings = result.getData().getSettings();
                        FLYApplication.getInstance().initPayPassword(result.getData().getUserId(), result.getData().getPayPassword());
                        PrivacySettingHelper.setPrivacySettings(ShareLoginActivity.this, settings);
                        FLYApplication.getInstance().initMulti();

                        // startActivity(new Intent(mContext, DataDownloadActivity.class));
                        DataDownloadActivity.start(mContext, result.getData().getIsupdate());
                        finish();
                    } else {
                        // 登录失败
                        String message = TextUtils.isEmpty(result.getResultMsg()) ? getString(R.string.Password_Filed) : result.getResultMsg();
                        ToastUtil.showToast(mContext, message);
                    }
                });
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        if (resultCode != SelectPrefixActivity.RESULT_MOBILE_PREFIX_SUCCESS)
            return;
        mobilePrefix = data.getIntExtra(Constants.MOBILE_PREFIX, 86);
        tv_prefix.setText("+" + mobilePrefix);
    }
}
