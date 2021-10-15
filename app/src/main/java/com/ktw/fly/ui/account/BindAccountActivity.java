package com.ktw.fly.ui.account;

import android.annotation.SuppressLint;
import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.text.TextUtils;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;

import com.ktw.fly.R;
import com.ktw.fly.bean.Code;
import com.ktw.fly.helper.DialogHelper;
import com.ktw.fly.helper.LoginHelper;
import com.ktw.fly.ui.base.BaseActivity;
import com.ktw.fly.ui.base.CoreManager;
import com.ktw.fly.ui.dialog.BindAccountAuthCodeDialog;
import com.ktw.fly.ui.dialog.BindAccountSuccessDialog;
import com.ktw.fly.ui.tool.ButtonColorChange;
import com.ktw.fly.util.Constants;
import com.ktw.fly.util.PreferenceUtils;
import com.ktw.fly.util.StringUtils;
import com.ktw.fly.util.ToastUtil;
import com.xuan.xuanhttplibrary.okhttp.HttpUtils;
import com.xuan.xuanhttplibrary.okhttp.callback.BaseCallback;
import com.xuan.xuanhttplibrary.okhttp.result.ObjectResult;
import com.xuan.xuanhttplibrary.okhttp.result.Result;

import java.util.HashMap;
import java.util.Locale;
import java.util.Map;

import okhttp3.Call;

/**
 * @ProjectName: new-android
 * @Package: com.ktw.fly.ui.account
 * @ClassName: BingAccountActivity
 * @Description: java类作用描述
 * @Author: XY
 * @CreateDate: 2021/10/14
 * @UpdateUser:
 * @UpdateDate: 2021/10/14
 * @UpdateRemark: 更新说明
 * @Version: 1.0
 */
public class BindAccountActivity extends BaseActivity implements View.OnClickListener {


    private int reckonTime = 120;

    @SuppressLint("HandlerLeak")
    private Handler mReckonHandler = new Handler() {
        public void handleMessage(Message msg) {
            if (msg.what == 0x1) {
                getCodeBtn.setText(reckonTime + " " + "S");
                reckonTime--;
                if (reckonTime < 0) {
                    mReckonHandler.sendEmptyMessage(0x2);
                } else {
                    mReckonHandler.sendEmptyMessageDelayed(0x1, 1000);
                }
            } else if (msg.what == 0x2) {
                // 120秒结束
                getCodeBtn.setText(getString(R.string.send));
                getCodeBtn.setEnabled(true);
                reckonTime = 120;
            }
        }
    };


    private int bindType;
    private EditText accountEdit;
    private Button getCodeBtn;

    // 驗證碼
    private String randcode;

    private int mobilePrefix = 86;
    private BindAccountSuccessDialog bindSuccessDialog;
    private EditText authCodeEdit;

    public static void startActivity(Context context, int bingType) {
        Intent intent = new Intent(context, BindAccountActivity.class);
        intent.putExtra("bind_type", bingType);
        context.startActivity(intent);
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_bind_account);
        bindType = getIntent().getIntExtra("bind_type", -1);
        mobilePrefix = PreferenceUtils.getInt(this, Constants.AREA_CODE_KEY, mobilePrefix);

        initAction(bindType);
        initView();

        bindSuccessDialog = new BindAccountSuccessDialog(this);
    }

    private void initView() {
        LinearLayout llAccount = findViewById(R.id.ll_account);
        LinearLayout llCode = findViewById(R.id.ll_code);
        TextView bingTypeText = findViewById(R.id.tv_bing_type);
        accountEdit = findViewById(R.id.et_account);
        authCodeEdit = findViewById(R.id.et_auth_code);
        getCodeBtn = findViewById(R.id.btn_get_code);
        Button confirmBtn = findViewById(R.id.btn_confirm);

        ButtonColorChange.colorDrawableStroke(this, llAccount);
        ButtonColorChange.colorDrawableStroke(this, llCode);
        ButtonColorChange.colorChange(this, getCodeBtn);
        ButtonColorChange.colorChange(this, confirmBtn);

        bingTypeText.setText(bindType == 0 ? R.string.default_phone_number_prefix : R.string.email_address);
        accountEdit.setHint(bindType == 0 ? R.string.hint_input_bind_phone : R.string.hint_input_email_address);

        getCodeBtn.setOnClickListener(this);
        confirmBtn.setOnClickListener(this);

    }


    private void initAction(int type) {
        getSupportActionBar().hide();
        findViewById(R.id.iv_title_left).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                finish();
            }
        });
        TextView tvTitle = findViewById(R.id.tv_title_center);

        tvTitle.setText(type == 0 ? R.string.bind_phone_account : R.string.bind_email_account);


    }

    @Override
    public void onClick(View v) {
        switch (v.getId()) {
            case R.id.btn_get_code:
                String account = accountEdit.getText().toString();
                if (bindType == 0) {
                    if (TextUtils.isEmpty(account)) {
                        ToastUtil.showToast(this, getString(R.string.hint_input_phone_number));
                        return;
                    }


                    if (!StringUtils.isMobileNumber(account) && mobilePrefix == 86) {
                        Toast.makeText(this, getString(R.string.Input_11_phoneNumber), Toast.LENGTH_SHORT).show();
                        return;
                    }

                    BindAccountAuthCodeDialog dialog = new BindAccountAuthCodeDialog(this);
                    dialog.setAccount(account);
                    dialog.setBindType(bindType);

                    dialog.setOnInputFinishListener(code -> {
                        requestPhoneAuthCode(account, mobilePrefix + account + "_bangDingPhone", code);
                    });
                    dialog.show();
                } else {
                    if (TextUtils.isEmpty(accountEdit.getText().toString())) {
                        ToastUtil.showToast(this, getString(R.string.hint_input_email_address));
                        return;
                    }
                    BindAccountAuthCodeDialog dialog = new BindAccountAuthCodeDialog(this);
                    dialog.setAccount(account);
                    dialog.setBindType(bindType);

                    dialog.setOnInputFinishListener(code -> {
                        requestEmailAuthCode(account, account + "_bangDingMailbox", code);
                    });
                    dialog.show();
                }
                break;
            case R.id.btn_confirm:
                if (nextStep(bindType)){
                    if (bindType == 0) {
                        bindPhone();
                    } else {
                        bindEmail();
                    }
                }
                break;
        }
    }


    /**
     * 请求验证码
     */
    private void requestEmailAuthCode(String account, String key, String imageCodeStr) {
        Map<String, String> params = new HashMap<>();
        params.put("email", account);
        params.put("key", key);
        params.put("imgCode", imageCodeStr);

        HttpUtils.get().url(coreManager.getConfig().SEND_EMAIL_AUTH_CODE)
                .params(params)
                .build(true, true)
                .execute(new BaseCallback<Code>(Code.class) {
                    @Override
                    public void onResponse(ObjectResult<Code> result) {
                        DialogHelper.dismissProgressDialog();
                        if (Result.checkSuccess(getApplication(), result)) {
                            Toast.makeText(getApplication(), R.string.verification_code_send_success, Toast.LENGTH_SHORT).show();
                            getCodeBtn.setEnabled(false);
//                             开始倒计时
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
                        Toast.makeText(getApplication(), getString(R.string.error_network), Toast.LENGTH_SHORT).show();
                    }
                });

    }


    private void requestPhoneAuthCode(String account, String key, String imageCodeStr) {
        Map<String, String> params = new HashMap<>();
        params.put("telephone", account);
        params.put("areaCode", String.valueOf(mobilePrefix));
        params.put("key", key);
        params.put("imgCode", imageCodeStr);

        HttpUtils.get().url(coreManager.getConfig().SEND_PHONE_AUTH_CODE_CAPITAL)
                .params(params)
                .build(true, true)
                .execute(new BaseCallback<Code>(Code.class) {
                    @Override
                    public void onResponse(ObjectResult<Code> result) {
                        DialogHelper.dismissProgressDialog();
                        if (Result.checkSuccess(getApplication(), result)) {
                            Toast.makeText(getApplication(), R.string.verification_code_send_success, Toast.LENGTH_SHORT).show();
                            getCodeBtn.setEnabled(false);
//                             开始倒计时
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
                        Toast.makeText(getApplication(), getString(R.string.error_network), Toast.LENGTH_SHORT).show();
                    }
                });

    }


    /**
     * 验证验证码
     */
    private boolean nextStep(int bindType) {

        String account = accountEdit.getText().toString();
        String authCode = authCodeEdit.getText().toString();
        if (bindType == 0) {
            if (TextUtils.isEmpty(account)) {
                Toast.makeText(this, getString(R.string.hint_input_phone_number), Toast.LENGTH_SHORT).show();
                return false;
            }

            if (!StringUtils.isMobileNumber(account) && mobilePrefix == 86) {
                Toast.makeText(this, getString(R.string.Input_11_phoneNumber), Toast.LENGTH_SHORT).show();
                return false;
            }
        } else if (bindType == 1) {
            if (TextUtils.isEmpty(account)) {
                Toast.makeText(this, getString(R.string.hint_input_email), Toast.LENGTH_SHORT).show();
                return false;
            }
        }

        if (TextUtils.isEmpty(randcode)) {
            Toast.makeText(this, getString(R.string.please_input_auth_code), Toast.LENGTH_SHORT).show();
            return false;
        } else if (authCode.equals(randcode)) {
            // 验证码正确
            return true;
        } else {
            Toast.makeText(this, getString(R.string.auth_code_error), Toast.LENGTH_SHORT).show();
            return false;
        }
    }

    /**
     * 绑定邮箱
     */
    private void bindEmail() {
        String userId = CoreManager.getSelf(this).getUserId();
        String account = accountEdit.getText().toString().trim();
        Map<String, String> params = new HashMap<>();
        params.put("userId", userId);
        params.put("mailbox", account);
        params.put("mailboxCode", randcode);
        HttpUtils.post().url(coreManager.getConfig().BIND_EMAIL)
                .params(params)
                .build()
                .execute(new BaseCallback<Code>(Code.class) {
                    @Override
                    public void onResponse(ObjectResult<Code> result) {
                        DialogHelper.dismissProgressDialog();
                        if (Result.checkSuccess(getApplication(), result)) {
                            bindSuccessDialog.show();
                        }
                    }

                    @Override
                    public void onError(Call call, Exception e) {
                        DialogHelper.dismissProgressDialog();
                        Toast.makeText(getApplication(), getString(R.string.error_network), Toast.LENGTH_SHORT).show();
                    }
                });

    }

    /**
     * 绑定手机
     */
    private void bindPhone() {


        String userId = CoreManager.getSelf(this).getUserId();
        String account = accountEdit.getText().toString().trim();
        Map<String, String> params = new HashMap<>();
        params.put("userId", userId);
        params.put("areaCode", String.valueOf(mobilePrefix));
        params.put("phone", account);
        params.put("verificationCode", randcode);
        HttpUtils.post().url(coreManager.getConfig().BIND_PHONE)
                .params(params)
                .build()
                .execute(new BaseCallback<Code>(Code.class) {
                    @Override
                    public void onResponse(ObjectResult<Code> result) {
                        DialogHelper.dismissProgressDialog();
                        if (Result.checkSuccess(getApplication(), result)) {
                            bindSuccessDialog.show();
                        }
                    }

                    @Override
                    public void onError(Call call, Exception e) {
                        DialogHelper.dismissProgressDialog();
                        Toast.makeText(getApplication(), getString(R.string.error_network), Toast.LENGTH_SHORT).show();
                    }
                });

    }
}
