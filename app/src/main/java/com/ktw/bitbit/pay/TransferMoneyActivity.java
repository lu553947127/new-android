package com.ktw.bitbit.pay;

import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.text.Editable;
import android.text.InputType;
import android.text.TextUtils;
import android.text.TextWatcher;
import android.view.View;
import android.view.Window;
import android.view.WindowManager;
import android.view.inputmethod.InputMethodManager;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import com.ktw.bitbit.FLYAppConstant;
import com.ktw.bitbit.R;
import com.ktw.bitbit.bean.Transfer;
import com.ktw.bitbit.bean.event.EventTransfer;
import com.ktw.bitbit.bean.message.ChatMessage;
import com.ktw.bitbit.bean.message.XmppMessage;
import com.ktw.bitbit.helper.AvatarHelper;
import com.ktw.bitbit.helper.DialogHelper;
import com.ktw.bitbit.helper.PaySecureHelper;
import com.ktw.bitbit.ui.base.BaseActivity;
import com.ktw.bitbit.ui.base.CoreManager;
import com.ktw.bitbit.ui.me.redpacket.ChangePayPasswordActivity;
import com.ktw.bitbit.ui.tool.ButtonColorChange;
import com.ktw.bitbit.util.Constants;
import com.ktw.bitbit.util.EditTextUtil;
import com.ktw.bitbit.util.PreferenceUtils;
import com.ktw.bitbit.util.ToastUtil;
import com.ktw.bitbit.util.secure.Money;
import com.ktw.bitbit.view.KeyBoad;
import com.ktw.bitbit.view.VerifyDialog;
import com.xuan.xuanhttplibrary.okhttp.HttpUtils;
import com.xuan.xuanhttplibrary.okhttp.callback.BaseCallback;
import com.xuan.xuanhttplibrary.okhttp.result.ObjectResult;

import java.util.HashMap;
import java.util.Map;

import de.greenrobot.event.EventBus;
import okhttp3.Call;

/**
 * 转账
 */
public class TransferMoneyActivity extends BaseActivity {
    private String mTransferredUserId, mTransferredName;

    private ImageView mTransferredIv;
    private TextView mTransferredTv;

    private String money, words;// 转账金额与转账说明
    private TextView mMoneyTv;
    private TextView mTransferDescTv, mTransferDescClickTv;

    private EditText et_transfer;
    private KeyBoad keyBoad;
    private boolean isUiCreat = false;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_transfer_money);
        mTransferredUserId = getIntent().getStringExtra(FLYAppConstant.EXTRA_USER_ID);
        mTransferredName = getIntent().getStringExtra(FLYAppConstant.EXTRA_NICK_NAME);
        initActionBar();
        initView();
        initEvent();
        initKeyBoad();
        checkHasPayPassword();
    }

    private void checkHasPayPassword() {
        boolean hasPayPassword = PreferenceUtils.getBoolean(this, Constants.IS_PAY_PASSWORD_SET + coreManager.getSelf().getUserId(), true);
        if (!hasPayPassword) {
            ToastUtil.showToast(this, R.string.tip_no_pay_password);
            Intent intent = new Intent(this, ChangePayPasswordActivity.class);
            startActivity(intent);
            finish();
        }
    }

    private void initActionBar() {
        getSupportActionBar().hide();
        findViewById(R.id.iv_title_left).setOnClickListener(view -> finish());
        TextView titleTv = findViewById(R.id.tv_title_center);
        titleTv.setText(getString(R.string.transfer_money));
    }

    private void initView() {
        mTransferredIv = findViewById(R.id.tm_iv);
        mTransferredTv = findViewById(R.id.tm_tv);
        AvatarHelper.getInstance().displayAvatar(mTransferredUserId, mTransferredIv);
        mTransferredTv.setText(mTransferredName);

        mMoneyTv = findViewById(R.id.transfer_je_tv);
        mMoneyTv.setInputType(InputType.TYPE_CLASS_NUMBER | InputType.TYPE_NUMBER_FLAG_DECIMAL);// 允许输入数字与小数点
        mTransferDescTv = findViewById(R.id.transfer_desc_tv);
        mTransferDescClickTv = findViewById(R.id.transfer_edit_desc_tv);

        et_transfer = findViewById(R.id.et_transfer);
        // 禁止输入框复制粘贴
        EditTextUtil.disableCopyAndPaste(et_transfer);
        keyBoad = new KeyBoad(TransferMoneyActivity.this, getWindow().getDecorView(), et_transfer);
    }

    private void initEvent() {
        mTransferDescClickTv.setOnClickListener(v -> {
            VerifyDialog verifyDialog = new VerifyDialog(mContext);
            verifyDialog.setVerifyClickListener(getString(R.string.transfer_money_desc), getString(R.string.transfer_desc_max_length_10),
                    words, 10, new VerifyDialog.VerifyClickListener() {
                        @Override
                        public void cancel() {

                        }

                        @Override
                        public void send(String str) {
                            words = str;
                            if (TextUtils.isEmpty(words)) {
                                mTransferDescTv.setText("");
                                mTransferDescTv.setVisibility(View.GONE);
                                mTransferDescClickTv.setText(getString(R.string.transfer_money_desc));
                            } else {
                                mTransferDescTv.setText(str);
                                mTransferDescTv.setVisibility(View.VISIBLE);
                                mTransferDescClickTv.setText(getString(R.string.transfer_modify));
                            }
                            keyBoad.show();
                        }

                    });
            verifyDialog.setOkButton(R.string.sure);
            keyBoad.dismiss();
            Window window = verifyDialog.getWindow();

            if (window != null) {
                window.setSoftInputMode(WindowManager.LayoutParams.SOFT_INPUT_STATE_VISIBLE); // 软键盘弹起
            }
            verifyDialog.show();
        });

        ButtonColorChange.colorChange(this, findViewById(R.id.transfer_btn));
        findViewById(R.id.transfer_btn).setOnClickListener(v -> {
            money = et_transfer.getText().toString().trim();

            if (TextUtils.isEmpty(money) || Double.parseDouble(money) <= 0) {
                Toast.makeText(mContext, getString(R.string.transfer_input_money), Toast.LENGTH_SHORT).show();
                return;
            }

            money = Money.fromYuan(money);

            PaySecureHelper.inputPayPassword(this, getString(R.string.transfer_money_to_someone, mTransferredName), money, password -> transfer(money, words, password));
        });
    }

    private void initKeyBoad() {
        et_transfer.setFocusable(true);
        et_transfer.setOnFocusChangeListener((v, hasFocus) -> {
            if (keyBoad != null && isUiCreat) {
                keyBoad.refreshKeyboardOutSideTouchable(!hasFocus);
            } else if (isUiCreat) {
                keyBoad.show();
            }
            if (hasFocus) {
                keyBoad.show();
                InputMethodManager im = (InputMethodManager) getSystemService(Context.INPUT_METHOD_SERVICE);
                im.hideSoftInputFromWindow(et_transfer.getWindowToken(), 0);
            }
        });

        et_transfer.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {

            }

            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {

            }

            @Override
            public void afterTextChanged(Editable s) {
                String text = s.toString();
                if (text.startsWith(".")) {
                    et_transfer.setText("0" + text);
                } else if (text.startsWith("0") && !text.contains(".") && text.length() > 1) {
                    et_transfer.setText(text.substring(1, text.length()));
                }

            }
        });

        et_transfer.setOnClickListener(v -> {
            if (keyBoad != null) {
                keyBoad.show();
            }
        });
    }

    @Override
    public void onWindowFocusChanged(boolean hasFocus) {
        super.onWindowFocusChanged(hasFocus);
        isUiCreat = true;
    }

    public void transfer(String money, final String words, String payPassword) {
        if (!coreManager.isLogin()) {
            return;
        }
        Map<String, String> params = new HashMap();
        params.put("toUserId", mTransferredUserId);
        params.put("money", money);
        if (!TextUtils.isEmpty(words)) {
            params.put("remark", words);
        }

        PaySecureHelper.generateParam(
                this, payPassword, params,
                "" + mTransferredUserId + money + (words == null ? "" : words),
                t -> {
                    DialogHelper.dismissProgressDialog();
                    ToastUtil.showToast(this, this.getString(R.string.tip_pay_secure_place_holder, t.getMessage()));
                }, (p, code) -> {
                    HttpUtils.get().url(coreManager.getConfig().SKTRANSFER_SEND_TRANSFER)
                            .params(p)
                            .build()
                            .execute(new BaseCallback<Transfer>(Transfer.class) {

                                @Override
                                public void onResponse(ObjectResult<Transfer> result) {
                                    Transfer transfer = result.getData();
                                    if (result.getResultCode() != 1) {
                                        ToastUtil.showToast(mContext, result.getResultMsg());
                                    } else {
                                        String objectId = transfer.getId();
                                        ChatMessage message = new ChatMessage();
                                        message.setType(XmppMessage.TYPE_TRANSFER);
                                        message.setFromUserId(coreManager.getSelf().getUserId());
                                        message.setFromUserName(coreManager.getSelf().getNickName());
                                        message.setToUserId(mTransferredUserId);
                                        message.setContent(money);// 转账金额
                                        message.setFilePath(words); // 转账说明
                                        message.setObjectId(objectId); // 红包id
                                        CoreManager.updateMyBalance();

                                        EventBus.getDefault().post(new EventTransfer(message));
                                        finish();
                                    }
                                }

                                @Override
                                public void onError(Call call, Exception e) {

                                }
                            });
                });
    }
}
