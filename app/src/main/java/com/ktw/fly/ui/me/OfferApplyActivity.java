package com.ktw.fly.ui.me;

import android.content.Intent;
import android.os.Bundle;
import android.text.TextUtils;
import android.view.View;
import android.widget.ImageView;
import android.widget.TextView;

import com.ktw.fly.FLYAppConfig;
import com.ktw.fly.R;
import com.ktw.fly.bean.User;
import com.ktw.fly.helper.DialogHelper;
import com.ktw.fly.helper.LoginHelper;
import com.ktw.fly.ui.base.BaseActivity;
import com.ktw.fly.util.CodeUtils;
import com.ktw.fly.util.ToastUtil;
import com.ktw.fly.view.ClearEditText;
import com.xuan.xuanhttplibrary.okhttp.HttpUtils;
import com.xuan.xuanhttplibrary.okhttp.callback.BaseCallback;
import com.xuan.xuanhttplibrary.okhttp.result.ObjectResult;
import com.xuan.xuanhttplibrary.okhttp.result.Result;

import java.util.HashMap;
import java.util.Map;

import okhttp3.Call;

/**
 * 优惠申请
 */
public class OfferApplyActivity extends BaseActivity implements View.OnClickListener {

    User mUser;
    private ImageView ivVerifyCode;
    private ClearEditText mBankCardNumberEdt;
    private ClearEditText etAmount;
    private ClearEditText mBankNameEdt;
    private ClearEditText etVerifyCode;
    private ClearEditText mUserNameEdt;
    private ClearEditText etDesc;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        mUser = coreManager.getSelf();
        if (!LoginHelper.isUserValidation(mUser)) {
            return;
        }
        setContentView(R.layout.activity_offer_apply);
        initActionBar();
        initView();
    }

    private void initActionBar() {
        getSupportActionBar().hide();
        findViewById(R.id.iv_title_left).setOnClickListener(v -> finish());
        TextView tvTitle = (TextView) findViewById(R.id.tv_title_center);
        tvTitle.setText("申请提现");
        TextView tvRight = (TextView) findViewById(R.id.tv_title_right);
        tvRight.setText("提现记录");
        tvRight.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                startActivity(new Intent(OfferApplyActivity.this, WithdrawalsRecordActivity.class));
            }
        });
    }

    private void initView() {

        ivVerifyCode = findViewById(R.id.ivVerifyCode);
        ivVerifyCode.setImageBitmap(CodeUtils.getInstance().createBitmap());

        ivVerifyCode.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                ivVerifyCode.setImageBitmap(CodeUtils.getInstance().createBitmap());
            }
        });

        mBankCardNumberEdt = findViewById(R.id.edt_bank_card_number);
        etAmount = findViewById(R.id.etAmount);
        mBankNameEdt = findViewById(R.id.edt_bank_name);
        etVerifyCode = findViewById(R.id.etVerifyCode);
        mUserNameEdt = findViewById(R.id.edt_user_name);
        etDesc = findViewById(R.id.etDesc);
        findViewById(R.id.btnApply).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                submit();
            }
        });
    }

    private void submit() {
        String bankName = mBankNameEdt.getText().toString().trim();
        if (TextUtils.isEmpty(bankName)) {
            ToastUtil.showToast(this, "请输入开户行");
            return;
        }
        String bankCardNumber= mBankCardNumberEdt.getText().toString().trim();
        if (TextUtils.isEmpty(bankCardNumber)) {
            ToastUtil.showToast(this, "请输入银行卡号");
            return;
        }
        String platform = mUserNameEdt.getText().toString().trim();
        if (TextUtils.isEmpty(platform)) {
            ToastUtil.showToast(this, "请输入姓名");
            return;
        }

        String amount = etAmount.getText().toString().trim();
        if (TextUtils.isEmpty(amount)) {
            ToastUtil.showToast(this, "请输入提现金额");
            return;
        }

        String code = etVerifyCode.getText().toString().trim();
        if (TextUtils.isEmpty(code) || !CodeUtils.getInstance().getCode().equalsIgnoreCase(code)) {
            ToastUtil.showToast(this, "验证码为空或者错误");
            return;
        }
        DialogHelper.showDefaulteMessageProgressDialog(this);
        Map<String, String> params = new HashMap<>();
        params.put("platformName", platform);
        params.put("account", bankCardNumber);
        params.put("amount", amount);
        params.put("reason", bankName);
        params.put("verifyCode", code);
        params.put("userId", coreManager.getSelf().getUserId());
        params.put("userName", coreManager.getSelf().getNickName());
        params.put("remark", etDesc.getText().toString().trim());
        HttpUtils.post().url(FLYAppConfig.API_ADD_WITHDRAWL)
                .params(params)
                .build()
                .execute(new BaseCallback<Void>(Void.class) {

                    @Override
                    public void onResponse(ObjectResult<Void> result) {
                        DialogHelper.dismissProgressDialog();
                        if (Result.checkSuccess(getApplicationContext(), result)) {
                            ToastUtil.showLongToast(OfferApplyActivity.this, "提交成功");
                            finish();
                        }
                    }

                    @Override
                    public void onError(Call call, Exception e) {
                        DialogHelper.dismissProgressDialog();
                        ToastUtil.showLongToast(OfferApplyActivity.this, "提交失败，请重试");
                    }
                });
    }

    @Override
    public void onClick(View view) {

    }
}
