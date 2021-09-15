package com.ktw.fly.ui.me;

import android.content.ComponentName;
import android.content.Intent;
import android.content.res.ColorStateList;
import android.os.Bundle;
import android.widget.Button;
import android.widget.TextView;

import androidx.core.view.ViewCompat;

import com.ktw.fly.FLYAppConfig;
import com.ktw.fly.FLYApplication;
import com.ktw.fly.R;
import com.ktw.fly.db.SQLiteHelper;
import com.ktw.fly.helper.DialogHelper;
import com.ktw.fly.helper.LoginHelper;
import com.ktw.fly.helper.PaySecureHelper;
import com.ktw.fly.sp.UserSp;
import com.ktw.fly.ui.base.BaseActivity;
import com.ktw.fly.ui.me.redpacket.ChangePayPasswordActivity;
import com.ktw.fly.util.Constants;
import com.ktw.fly.util.PreferenceUtils;
import com.ktw.fly.util.SkinUtils;
import com.ktw.fly.util.ToastUtil;
import com.ktw.fly.view.SelectionFrame;
import com.xuan.xuanhttplibrary.okhttp.HttpUtils;
import com.xuan.xuanhttplibrary.okhttp.callback.BaseCallback;
import com.xuan.xuanhttplibrary.okhttp.result.ObjectResult;
import com.xuan.xuanhttplibrary.okhttp.result.Result;

import java.util.HashMap;

import okhttp3.Call;

/**
 * 账号操作
 * Created by Harvey on 2/5/21.
 **/
public class AccountOperateActivity extends BaseActivity {
    public static final String TYPE_OPERATE = "TypeOperate";
    public static final int OPERATE_FREEZE_ACCOUNT = 1106;  //解冻账户
    public static final int OPERATE_UNFREEZE_ACCOUNT = 1107;//解冻账户
    public static final int OPERATE_UNSUBSCRIBE_ACCOUNT = 1108;//注销账户
    private int mOperateType;

    private TextView mTitleTv, mContentTv, mSubContentTv;
    private Button mOperateBtn;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_account_operate);
        initActionBar();
        initView();
        initData();
    }

    private void initView() {

        mContentTv = findViewById(R.id.tv_operation_content);
        mSubContentTv = findViewById(R.id.tv_operation_sub_content);
        mOperateBtn = findViewById(R.id.btn_operate);
        mOperateBtn.setOnClickListener(v -> {
            if (mOperateType == OPERATE_FREEZE_ACCOUNT || mOperateType == OPERATE_UNFREEZE_ACCOUNT) {
                Intent intent = new Intent(this, AccountFreezeActivity.class);
                intent.putExtra(TYPE_OPERATE, mOperateType);
                startActivity(intent);
            } else if (mOperateType == OPERATE_UNSUBSCRIBE_ACCOUNT) {
                showPayPasswordDialog();
            }

        });
        ViewCompat.setBackgroundTintList(mOperateBtn, ColorStateList.valueOf(SkinUtils.getSkin(this).getAccentColor()));
    }

    // 输入支付密码
    private void showPayPasswordDialog() {
        if (hasPayPassword()) {
            PaySecureHelper.inputPayPassword(AccountOperateActivity.this, "注销账号", "", password -> {
                generateParam("", password);
            });
        } else {
            Intent intent = new Intent(AccountOperateActivity.this, ChangePayPasswordActivity.class);
            startActivity(intent);
        }
    }

    private void initData() {
        mOperateType = getIntent().getIntExtra(TYPE_OPERATE, 0);
        switch (mOperateType) {
            case OPERATE_FREEZE_ACCOUNT:
                mTitleTv.setText(R.string.operate_freeze_title);
                mContentTv.setText(R.string.operate_freeze_content);
                mSubContentTv.setText(R.string.operate_freeze_sub_content);
                mOperateBtn.setText(R.string.operate_freeze);
                break;
            case OPERATE_UNFREEZE_ACCOUNT:
                mTitleTv.setText(R.string.operate_unfreeze_title);
                mContentTv.setText(R.string.operate_unfreeze_content);
                mSubContentTv.setText(R.string.operate_unfreeze_sub_content);
                mOperateBtn.setText(R.string.operate_unfreeze);
                break;
            case OPERATE_UNSUBSCRIBE_ACCOUNT:
                mTitleTv.setText(R.string.operate_unsubscribe_title);
                mContentTv.setText(R.string.operate_unsubscribe_content);
                mSubContentTv.setText(R.string.operate_unsubscribe_sub_content);
                mOperateBtn.setText(R.string.operate_unsubscribe);
                break;
        }
    }

    private void initActionBar() {
        getSupportActionBar().hide();
        findViewById(R.id.iv_title_left).setOnClickListener(v -> finish());
        mTitleTv = findViewById(R.id.tv_title_center);
    }

    /**
     * 注销账号
     */
    public void cancelAccount() {
        DialogHelper.showDefaulteMessageProgressDialog(this);
        HttpUtils.post().url(coreManager.getConfig().CANCEL_ACCOUNT)
                .build()
                .execute(new BaseCallback<Void>(Void.class) {

                    @Override
                    public void onResponse(ObjectResult<Void> result) {
                        DialogHelper.dismissProgressDialog();
                        if (Result.checkSuccess(getApplicationContext(), result)) {
                            FLYApplication.getInstance().mUserStatus = LoginHelper.STATUS_NO_USER;
                            coreManager.logout();

                            SQLiteHelper.getInstance(mContext).clearCacheByUserId(coreManager.getSelf().getUserId());
                            UserSp.getInstance(mContext).clearUserInfo();

                            //发送广播  重新拉起app
                            Intent intent = new Intent(FLYAppConfig.BROADCASTTEST_ACTION);
                            intent.setComponent(new ComponentName(FLYAppConfig.sPackageName, FLYAppConfig.BroadcastReceiverClass));
                            sendBroadcast(intent);

                        } else {
                            ToastUtil.showToast(getApplicationContext(), result.getResultMsg());
                        }
                    }

                    @Override
                    public void onError(Call call, Exception e) {
                        DialogHelper.dismissProgressDialog();
                        ToastUtil.showLongToast(AccountOperateActivity.this, "失败，请重试");
                    }
                });
    }

    private boolean hasPayPassword() {
        return PreferenceUtils.getBoolean(this, Constants.IS_PAY_PASSWORD_SET + coreManager.getSelf().getUserId(), false);
    }

    private void generateParam(String money, String password) {
        HashMap<String, String> paramsMap = new HashMap<>();
        paramsMap.put("moneyStr", money);
        PaySecureHelper.generateParam(this, password, paramsMap, money, error -> {
            ToastUtil.showToast(this, error.getMessage());
        }, (resultParamsMap, bytes) -> {
            showConfirmDialog();
        });
    }

    private void showConfirmDialog() {
        SelectionFrame mSF = new SelectionFrame(this);
        mSF.setSomething("注销账号", "账号一旦注销，将彻底删除数据，无法恢复", new SelectionFrame.OnSelectionFrameClickListener() {
            @Override
            public void cancelClick() {

            }

            @Override
            public void confirmClick() {
                cancelAccount();
            }
        });
        mSF.show();
    }

}