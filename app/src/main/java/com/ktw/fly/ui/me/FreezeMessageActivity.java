package com.ktw.fly.ui.me;

import android.content.ComponentName;
import android.content.Intent;
import android.content.res.ColorStateList;
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

import androidx.core.view.ViewCompat;

import com.ktw.fly.FLYAppConfig;
import com.ktw.fly.FLYApplication;
import com.ktw.fly.R;
import com.ktw.fly.bean.Code;
import com.ktw.fly.db.SQLiteHelper;
import com.ktw.fly.helper.DialogHelper;
import com.ktw.fly.helper.ImageLoadHelper;
import com.ktw.fly.helper.LoginHelper;
import com.ktw.fly.sp.UserSp;
import com.ktw.fly.ui.base.BaseActivity;
import com.ktw.fly.util.SkinUtils;
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
 * 冻结、解冻账号 Step 2
 * Created by Harvey on 2/6/21.
 **/
public class FreezeMessageActivity extends BaseActivity implements View.OnClickListener {
    private int mType;
    private TextView mTitleTv, mPhoneTv;
    private EditText mSmsCodeEdt, mImageCodeEdit;
    private Button mConfirmBtn;
    public static final String INTENT_PHONE = "phone";
    public static final String INTENT_REAL_NAME = "realName";
    public static final String INTENT_ID_CARD = "idCard";
    private Button mGetCodeBtn;
    private String mPhoneNum, mRealName, mIdCard;

    //验证码
    private String randcode;
    // 图形验证码
    private ImageView mImageCodeIv;
    private ImageView mRefreshIv;
    private int reckonTime = 60;
    private Handler mReckonHandler = new Handler() {
        public void handleMessage(Message msg) {
            if (msg.what == 0x1) {
                mGetCodeBtn.setText(reckonTime + "S");
                reckonTime--;
                if (reckonTime < 0) {
                    mReckonHandler.sendEmptyMessage(0x2);
                } else {
                    mReckonHandler.sendEmptyMessageDelayed(0x1, 1000);
                }
            } else if (msg.what == 0x2) {
                // 60秒结束
                mGetCodeBtn.setText(getString(R.string.send));
                mGetCodeBtn.setEnabled(true);
                reckonTime = 60;
            }
        }
    };

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_freeze_message);
        initActionBar();
        initView();
        initData();

    }

    private void initView() {

        mPhoneTv = findViewById(R.id.tv_phone);
        mSmsCodeEdt = findViewById(R.id.edt_msg_code);
        mGetCodeBtn = findViewById(R.id.ver_code_view);
        mConfirmBtn = findViewById(R.id.btn_confirm);
        mImageCodeEdit = findViewById(R.id.image_edt);
        mImageCodeIv = findViewById(R.id.image_iv);
        mRefreshIv = findViewById(R.id.image_iv_refresh);

        mGetCodeBtn.setOnClickListener(this);
        mConfirmBtn.setOnClickListener(this);
        mRefreshIv.setOnClickListener(this);

        ViewCompat.setBackgroundTintList(mConfirmBtn, ColorStateList.valueOf(SkinUtils.getSkin(this).getAccentColor()));
        ViewCompat.setBackgroundTintList(mGetCodeBtn, ColorStateList.valueOf(SkinUtils.getSkin(this).getAccentColor()));
    }

    private void initData() {
        mPhoneNum = getIntent().getStringExtra(INTENT_PHONE);
        mRealName = getIntent().getStringExtra(INTENT_REAL_NAME);
        mIdCard = getIntent().getStringExtra(INTENT_ID_CARD);
        mType = getIntent().getIntExtra(AccountOperateActivity.TYPE_OPERATE, 0);
        if (mType == AccountOperateActivity.OPERATE_FREEZE_ACCOUNT) {
            mTitleTv.setText(R.string.operate_freeze_title);
            mConfirmBtn.setText(R.string.operate_freeze_title);

        } else if (mType == AccountOperateActivity.OPERATE_UNFREEZE_ACCOUNT) {
            mTitleTv.setText(R.string.operate_unfreeze_title);
            mConfirmBtn.setText(R.string.operate_unfreeze_title);
        }


        mPhoneTv.setText(mPhoneNum);

        requestImageCode();

    }

    private void initActionBar() {
        getSupportActionBar().hide();
        findViewById(R.id.iv_title_left).setOnClickListener(v -> finish());
        mTitleTv = findViewById(R.id.tv_title_center);
    }

    @Override
    public void onClick(View view) {
        switch (view.getId()) {
            case R.id.image_iv_refresh:
                requestImageCode();
                break;
            case R.id.ver_code_view:
                // 获取验证码
                String imagecode = mImageCodeEdit.getText().toString().trim();
                if (TextUtils.isEmpty(mPhoneNum) || TextUtils.isEmpty(imagecode)) {
                    ToastUtil.showToast(mContext, getString(R.string.tip_phone_number_verification_code_empty));
                    return;
                }
                verifyTelephone(mPhoneNum, imagecode);
                break;
            case R.id.btn_confirm:
                if (nextStep()) {
                    if (mType == AccountOperateActivity.OPERATE_FREEZE_ACCOUNT) {
                        freezeAccount();
                    } else if (mType == AccountOperateActivity.OPERATE_UNFREEZE_ACCOUNT) {
                        unfreezeAccount();
                    }
                }
                break;
        }
    }


    /**
     * 请求验证码
     */
    private void verifyTelephone(String phoneNumber, String imageCode) {
        DialogHelper.showDefaulteMessageProgressDialog(this);
        Map<String, String> params = new HashMap<>();
        String language = Locale.getDefault().getLanguage();
        params.put("language", language);
        params.put("areaCode", String.valueOf(86));
        params.put("telephone", phoneNumber);
        params.put("imgCode", imageCode);
        params.put("isRegister", String.valueOf(0));
        params.put("version", "1");

        HttpUtils.get().url(coreManager.getConfig().SEND_AUTH_CODE)
                .params(params)
                .build(true, true)
                .execute(new BaseCallback<Code>(Code.class) {
                    @Override
                    public void onResponse(ObjectResult<Code> result) {
                        DialogHelper.dismissProgressDialog();
                        if (result.getResultCode() == 1) {
                            mGetCodeBtn.setEnabled(false);
                            // 开始计时
                            mReckonHandler.sendEmptyMessage(0x1);
                            if (result.getData() != null && result.getData().getCode() != null) {
                                // 得到验证码
                                randcode = result.getData().getCode();
                            }
                            ToastUtil.showToast(mContext, R.string.verification_code_send_success);
                        } else {
                            requestImageCode();
                            ToastUtil.showToast(mContext, result.getResultMsg());
                        }
                    }

                    @Override
                    public void onError(Call call, Exception e) {
                        DialogHelper.dismissProgressDialog();
                        Toast.makeText(FreezeMessageActivity.this, getString(R.string.error_network), Toast.LENGTH_SHORT).show();
                    }
                });
    }

    /**
     * 验证验证码
     */
    private boolean nextStep() {
        String authCode = mSmsCodeEdt.getText().toString().trim();
        if (TextUtils.isEmpty(authCode)) {
            Toast.makeText(this, getString(R.string.input_message_code), Toast.LENGTH_SHORT).show();
            return false;
        }
        if (!TextUtils.isEmpty(randcode)) {
            if (authCode.equals(randcode)) {
                // 验证码正确
                return true;
            } else {
                // 验证码错误
                Toast.makeText(this, getString(R.string.msg_code_not_ok), Toast.LENGTH_SHORT).show();
                return false;
            }
        } else {
            return true;
        }
    }

    /**
     * 请求图形验证码
     */
    private void requestImageCode() {
        Map<String, String> params = new HashMap<>();
        params.put("telephone", 86 + mPhoneNum);
        String url = HttpUtils.get().url(coreManager.getConfig().USER_GETCODE_IMAGE)
                .params(params)
                .buildUrl();
        ImageLoadHelper.loadBitmapWithoutCache(
                mContext,
                url,
                b -> {
                    mImageCodeIv.setImageBitmap(b);
                }, e -> {
                    Toast.makeText(FreezeMessageActivity.this, R.string.tip_verification_code_load_failed, Toast.LENGTH_SHORT).show();
                }
        );
    }

    /**
     * 冻结账号
     */
    private void freezeAccount() {
        DialogHelper.showDefaulteMessageProgressDialog(this);
        Map<String, String> params = new HashMap<>();
        params.put("phone", mPhoneNum);
        params.put("name", mRealName);
        params.put("idCard", mIdCard);
        params.put("type", String.valueOf(1));
        HttpUtils.post().url(coreManager.getConfig().FREEZE_ACCOUNT)
                .params(params)
                .build()
                .execute(new BaseCallback<Void>(Void.class) {

                    @Override
                    public void onResponse(ObjectResult<Void> result) {
                        DialogHelper.dismissProgressDialog();
                        if (Result.checkSuccess(getApplicationContext(), result)) {
                            ToastUtil.showToast(mContext, "冻结成功");

                            FLYApplication.getInstance().mUserStatus = LoginHelper.STATUS_NO_USER;
                            coreManager.logout();

                            UserSp.getInstance(mContext).clearUserInfo();

                            //发送广播  重新拉起app
                            Intent intent = new Intent(FLYAppConfig.BROADCASTTEST_ACTION);
                            intent.setComponent(new ComponentName(FLYAppConfig.sPackageName, FLYAppConfig.BroadcastReceiverClass));
                            sendBroadcast(intent);
                        }
                    }

                    @Override
                    public void onError(Call call, Exception e) {
                        DialogHelper.dismissProgressDialog();
                        ToastUtil.showLongToast(mContext, "操作失败，请重试");
                    }
                });
    }

    /**
     * 解冻账号
     */
    private void unfreezeAccount() {
        DialogHelper.showDefaulteMessageProgressDialog(this);
        Map<String, String> params = new HashMap<>();
        params.put("phone", mPhoneNum);
        params.put("name", mRealName);
        params.put("idCard", mIdCard);
        params.put("type", String.valueOf(0));
        HttpUtils.post().url(coreManager.getConfig().FREEZE_ACCOUNT)
                .params(params)
                .build()
                .execute(new BaseCallback<Void>(Void.class) {

                    @Override
                    public void onResponse(ObjectResult<Void> result) {
                        DialogHelper.dismissProgressDialog();
                        if (Result.checkSuccess(getApplicationContext(), result)) {
                            ToastUtil.showToast(mContext, "解冻成功");
                        }
                    }

                    @Override
                    public void onError(Call call, Exception e) {
                        DialogHelper.dismissProgressDialog();
                        ToastUtil.showLongToast(mContext, "操作失败，请重试");
                    }
                });
    }
}
