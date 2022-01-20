package com.ktw.bitbit.pay;import android.content.Context;import android.content.Intent;import android.os.Bundle;import android.text.Editable;import android.text.InputType;import android.text.TextUtils;import android.text.TextWatcher;import android.view.View;import android.view.Window;import android.view.WindowManager;import android.view.inputmethod.InputMethodManager;import android.widget.EditText;import android.widget.ImageView;import android.widget.TextView;import android.widget.Toast;import com.alibaba.fastjson.JSON;import com.ktw.bitbit.R;import com.ktw.bitbit.bean.Receipt;import com.ktw.bitbit.helper.AvatarHelper;import com.ktw.bitbit.helper.DialogHelper;import com.ktw.bitbit.helper.PaySecureHelper;import com.ktw.bitbit.ui.base.BaseActivity;import com.ktw.bitbit.ui.me.redpacket.ChangePayPasswordActivity;import com.ktw.bitbit.ui.tool.ButtonColorChange;import com.ktw.bitbit.util.Constants;import com.ktw.bitbit.util.PreferenceUtils;import com.ktw.bitbit.util.ToastUtil;import com.ktw.bitbit.util.secure.Money;import com.ktw.bitbit.view.KeyBoad;import com.ktw.bitbit.view.VerifyDialog;import com.xuan.xuanhttplibrary.okhttp.HttpUtils;import com.xuan.xuanhttplibrary.okhttp.callback.BaseCallback;import com.xuan.xuanhttplibrary.okhttp.result.ObjectResult;import java.util.HashMap;import java.util.Map;import okhttp3.Call;/** * 扫描他人收款码，弹起付款界面 */public class ReceiptPayMoneyActivity extends BaseActivity {    private Receipt mReceipt;    private boolean isFixedMoney;// 收款方是否固定了金额    private ImageView mAvatarIv;    private TextView mNameTv;    private String money, words;    private TextView mMoneyTv;    private TextView mFixedDescTv, mFixedMoneyTv;    private TextView mTransferDescTv, mTransferDescClickTv;    private EditText et_transfer;    private KeyBoad keyBoad;    private boolean isUiCreat = false;    private double mMoney;    @Override    protected void onCreate(Bundle savedInstanceState) {        super.onCreate(savedInstanceState);        setContentView(R.layout.activity_receipt_pay_money);        String receipt_order = getIntent().getStringExtra("RECEIPT_ORDER");        mReceipt = JSON.parseObject(receipt_order, Receipt.class);        if (mReceipt == null) {            return;        }        isFixedMoney = !TextUtils.isEmpty(mReceipt.getMoney());        initActionBar();        initView();        initEvent();        initKeyBoad();        checkHasPayPassword();    }    private void checkHasPayPassword() {        boolean hasPayPassword = PreferenceUtils.getBoolean(this, Constants.IS_PAY_PASSWORD_SET + coreManager.getSelf().getUserId(), true);        if (!hasPayPassword) {            ToastUtil.showToast(this, R.string.tip_no_pay_password);            Intent intent = new Intent(this, ChangePayPasswordActivity.class);            startActivity(intent);            finish();        }    }    private void initActionBar() {        getSupportActionBar().hide();        findViewById(R.id.iv_title_left).setOnClickListener(view -> finish());        TextView titleTv = findViewById(R.id.tv_title_center);        titleTv.setText(getString(R.string.rp_payment));    }    private void initView() {        mAvatarIv = findViewById(R.id.pay_avatar_iv);        mNameTv = findViewById(R.id.pay_name_tv);        AvatarHelper.getInstance().displayAvatar(mReceipt.getUserId(), mAvatarIv);        mNameTv.setText(mReceipt.getUserName());        mMoneyTv = findViewById(R.id.transfer_je_tv);        mMoneyTv.setInputType(InputType.TYPE_CLASS_NUMBER | InputType.TYPE_NUMBER_FLAG_DECIMAL);// 允许输入数字与小数点        mFixedDescTv = findViewById(R.id.fixed_desc_tv);        mFixedMoneyTv = findViewById(R.id.fixed_money_tv);        mTransferDescTv = findViewById(R.id.transfer_desc_tv);        mTransferDescClickTv = findViewById(R.id.transfer_edit_desc_tv);        et_transfer = findViewById(R.id.et_transfer);        keyBoad = new KeyBoad(ReceiptPayMoneyActivity.this, getWindow().getDecorView(), et_transfer);        if (isFixedMoney) {            findViewById(R.id.ll1).setVisibility(View.GONE);            findViewById(R.id.ll2).setVisibility(View.VISIBLE);            mFixedMoneyTv.setText("￥" + mReceipt.getMoney());            if (!TextUtils.isEmpty(mReceipt.getDescription())) {                mFixedDescTv.setText(mReceipt.getDescription());            } else {                mFixedDescTv.setVisibility(View.GONE);            }        }    }    private void initEvent() {        mTransferDescClickTv.setOnClickListener(v -> {            VerifyDialog verifyDialog = new VerifyDialog(mContext);            verifyDialog.setVerifyClickListener(getString(R.string.receipt_add_remake), getString(R.string.transfer_desc_max_length_10),                    words, 10, new VerifyDialog.VerifyClickListener() {                        @Override                        public void cancel() {                        }                        @Override                        public void send(String str) {                            words = str;                            if (TextUtils.isEmpty(words)) {                                mTransferDescTv.setText("");                                mTransferDescTv.setVisibility(View.GONE);                                mTransferDescClickTv.setText(getString(R.string.receipt_add_remake));                            } else {                                mTransferDescTv.setText(str);                                mTransferDescTv.setVisibility(View.VISIBLE);                                mTransferDescClickTv.setText(getString(R.string.transfer_modify));                            }                            if (!isFixedMoney) {                                keyBoad.show();                            }                        }                    });            verifyDialog.setOkButton(R.string.sure);            keyBoad.dismiss();            Window window = verifyDialog.getWindow();            if (window != null) {                window.setSoftInputMode(WindowManager.LayoutParams.SOFT_INPUT_STATE_VISIBLE); // 软键盘弹起            }            verifyDialog.show();        });//        findViewById(R.id.transfer_btn).setBackgroundColor(SkinUtils.getSkin(this).getAccentColor());        ButtonColorChange.colorChange(this, findViewById(R.id.transfer_btn));        findViewById(R.id.transfer_btn).setOnClickListener(v -> {            if (isFixedMoney) {                PaySecureHelper.inputPayPassword(this, getString(R.string.rp_payment), mReceipt.getMoney(), password -> handlerScanReceiptCode(password));            } else {                money = et_transfer.getText().toString().trim();                try {                    mMoney = Double.parseDouble(money);                } catch (NumberFormatException e) {                    Toast.makeText(mContext, getString(R.string.transfer_input_money), Toast.LENGTH_SHORT).show();                }                if (TextUtils.isEmpty(money) || mMoney <= 0) {                    Toast.makeText(mContext, getString(R.string.transfer_input_money), Toast.LENGTH_SHORT).show();                    return;                }                money = Money.fromYuan(money);                keyBoad.dismiss();                PaySecureHelper.inputPayPassword(this, getString(R.string.rp_payment), money, password -> handlerScanReceiptCode(password)).setOnDismissListener(dialog1 -> {                    keyBoad.show();                });            }        });    }    private void initKeyBoad() {        et_transfer.setOnFocusChangeListener(new View.OnFocusChangeListener() {            @Override            public void onFocusChange(View v, boolean hasFocus) {                if (keyBoad != null && isUiCreat) {                    keyBoad.refreshKeyboardOutSideTouchable(!hasFocus);                } else if (isUiCreat) {                    keyBoad.show();                }                if (hasFocus) {                    InputMethodManager im = (InputMethodManager) getSystemService(Context.INPUT_METHOD_SERVICE);                    im.hideSoftInputFromWindow(et_transfer.getWindowToken(), 0);                }            }        });        et_transfer.addTextChangedListener(new TextWatcher() {            @Override            public void beforeTextChanged(CharSequence s, int start, int count, int after) {            }            @Override            public void onTextChanged(CharSequence s, int start, int before, int count) {            }            @Override            public void afterTextChanged(Editable s) {                String text = s.toString();                if (text.startsWith(".")) {                    et_transfer.setText("0" + text);                } else if (text.startsWith("0") && !text.contains(".") && text.length() > 1) {                    et_transfer.setText(text.substring(1, text.length()));                }            }        });        et_transfer.setOnClickListener(v -> {            if (keyBoad != null) {                keyBoad.show();            }        });    }    @Override    public void onWindowFocusChanged(boolean hasFocus) {        super.onWindowFocusChanged(hasFocus);        isUiCreat = true;    }    private void handlerScanReceiptCode(String payPassword) {        DialogHelper.showDefaulteMessageProgressDialog(this);        final String payMoney;        if (isFixedMoney) {            payMoney = mReceipt.getMoney();        } else {            payMoney = money;        }        Map<String, String> params = new HashMap();        params.put("toUserId", mReceipt.getUserId());        params.put("money", payMoney);        PaySecureHelper.generateParam(                this, payPassword, params,                "" + mReceipt.getUserId() + payMoney,                t -> {                    DialogHelper.dismissProgressDialog();                    ToastUtil.showToast(this, getString(R.string.tip_pay_secure_place_holder, t.getMessage()));                }, (p, code) -> {                    HttpUtils.get().url(coreManager.getConfig().PAY_CODE_RECEIPT)                            .params(p)                            .build()                            .execute(new BaseCallback<Void>(Void.class) {                                @Override                                public void onResponse(ObjectResult<Void> result) {                                    DialogHelper.dismissProgressDialog();                                    if (result.getResultCode() == 1) {                                        Toast.makeText(mContext, getString(R.string.success), Toast.LENGTH_SHORT).show();                                        finish();                                    } else {                                        Toast.makeText(mContext, result.getResultMsg(), Toast.LENGTH_SHORT).show();                                    }                                }                                @Override                                public void onError(Call call, Exception e) {                                    DialogHelper.dismissProgressDialog();                                }                            });                });    }}