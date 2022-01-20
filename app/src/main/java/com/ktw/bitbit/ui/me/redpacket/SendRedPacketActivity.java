package com.ktw.bitbit.ui.me.redpacket;

import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.text.TextUtils;
import android.view.View;
import android.view.WindowManager;
import android.view.inputmethod.InputMethodManager;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.RelativeLayout;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.Nullable;

import com.ktw.bitbit.FLYAppConstant;
import com.ktw.bitbit.R;
import com.ktw.bitbit.bean.Capital;
import com.ktw.bitbit.bean.message.ChatMessage;
import com.ktw.bitbit.bean.message.XmppMessage;
import com.ktw.bitbit.bean.redpacket.RedPacket;
import com.ktw.bitbit.bean.redpacket.RedPacketResult;
import com.ktw.bitbit.helper.DialogHelper;
import com.ktw.bitbit.helper.PaySecureHelper;
import com.ktw.bitbit.helper.RedPacketHelper;
import com.ktw.bitbit.ui.base.BaseActivity;
import com.ktw.bitbit.ui.message.ChatActivity;
import com.ktw.bitbit.util.InputChangeListener;
import com.ktw.bitbit.util.ToastUtil;
import com.ktw.bitbit.util.secure.Money;
import com.xuan.xuanhttplibrary.okhttp.HttpUtils;
import com.xuan.xuanhttplibrary.okhttp.callback.BaseCallback;
import com.xuan.xuanhttplibrary.okhttp.result.ObjectResult;
import com.xuan.xuanhttplibrary.okhttp.result.Result;

import java.util.HashMap;
import java.util.Map;

import okhttp3.Call;

/**
 * Created by 魏正旺 on 2016/9/9.
 */
public class SendRedPacketActivity extends BaseActivity implements View.OnClickListener {

    public static final int REQUEST_CODE_CAPITAL = 200;     // 资金类型返回


    private String toUserId;

    private TextView capitalText;

    //选择发送红包的资产类型ID
    private String capitalId;
    private String capitalName;
    private EditText amountEdit;
    private TextView virtualCoinsNumberText;
    private boolean isGroupChat;

    private TextView curRedText;
    private TextView alterRedText;

    private int curRedPackerType;
    private EditText wordEdit;
    private EditText greetingEdit;
    private EditText redPacketCountEdit;
    private RelativeLayout numberLayout;
    private Capital capital;
    //朋友的 用户昵称
    private String toUserNickName;
    //当前用户ID
    private String userId;
    //当前用户昵称
    private String nickName;
    private LinearLayout redTypeLayout;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_redpacket);

        toUserId = getIntent().getStringExtra(FLYAppConstant.EXTRA_USER_ID);
        toUserNickName = getIntent().getStringExtra(FLYAppConstant.EXTRA_NICK_NAME);
        userId = coreManager.getSelf().getUserId();
        nickName = coreManager.getSelf().getNickName();

        isGroupChat = getIntent().getBooleanExtra(FLYAppConstant.EXTRA_IS_GROUP_CHAT, false);
        //默认资产类型ID 和名称
        this.capitalId = "19";
        this.capitalName = "ASDT";

        if (isGroupChat) { //群组默认拼手气类型
            curRedPackerType = FLYAppConstant.LUCK_RED_PACKER;
        } else {    //单聊默认普通类型
            curRedPackerType = FLYAppConstant.COMMON_RED_PACKER;
        }

        initActionBar();
        initView();

        getCapitalByUser();
    }


    private void initActionBar() {
        getSupportActionBar().hide();
        getWindow().setSoftInputMode(WindowManager.LayoutParams.SOFT_INPUT_STATE_VISIBLE);
        findViewById(R.id.tv_title_left).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                finish();
            }
        });
    }

    /**
     * 初始化布局
     */
    private void initView() {
        findViewById(R.id.rl_property).setOnClickListener(this);
        capitalText = findViewById(R.id.tv_capital);
        virtualCoinsNumberText = findViewById(R.id.tv_virtual_coins_number);
        amountEdit = findViewById(R.id.et_amount);
        redPacketCountEdit = findViewById(R.id.et_red_packet_count);

        ImageView ivRedDetail = findViewById(R.id.iv_red_detail);
        numberLayout = findViewById(R.id.rl_number);

        curRedText = findViewById(R.id.tv_cur_red);
        alterRedText = findViewById(R.id.tv_alter_red);

        wordEdit = findViewById(R.id.et_word);
        greetingEdit = findViewById(R.id.et_greeting);
        redTypeLayout = findViewById(R.id.ll_red_type);

        Button sendRedBtn = findViewById(R.id.btn_sendRed);

        ivRedDetail.setOnClickListener(this);

        alterRedText.setOnClickListener(this);
        sendRedBtn.setOnClickListener(this);

        String capital = String.format(getResources().getString(R.string.red_available_balance), "0");
        amountEdit.setHint(capital);
        if (isGroupChat) {
            numberLayout.setVisibility(View.VISIBLE);
            virtualCoinsNumberText.setText(R.string.red_number_of_issued_virtual_coins);
        } else {
            numberLayout.setVisibility(View.GONE);
            virtualCoinsNumberText.setText(R.string.single_red_packer);
        }
        setCurRedPacketStr(curRedPackerType);

        sendRedBtn.setAlpha(0.6f);
        InputChangeListener inputChangeListenerPt = new InputChangeListener(amountEdit, sendRedBtn);
        amountEdit.addTextChangedListener(inputChangeListenerPt);
    }

    @Override
    public void onClick(View v) {
        Intent intent;
        switch (v.getId()) {
            case R.id.rl_property://资产选择
                intent = new Intent(this, PropertyListActivity.class);
                startActivityForResult(intent, REQUEST_CODE_CAPITAL);
                break;
            case R.id.iv_red_detail: //红包记录详情
                intent = new Intent(this, RedPacketListActivity.class);
                startActivity(intent);
                break;
            case R.id.tv_alter_red:
                if (curRedPackerType == FLYAppConstant.COMMON_RED_PACKER) {
                    curRedPackerType = FLYAppConstant.LUCK_RED_PACKER;
                } else {
                    curRedPackerType = FLYAppConstant.COMMON_RED_PACKER;
                }
                setCurRedPacketStr(curRedPackerType);
                break;
            case R.id.btn_sendRed: //发送红包
                sendRedParam();
                break;
        }
    }


    /**
     * 发送红包参数
     */
    private void sendRedParam() {
        String money, greeting, redPackerCount;

        if (TextUtils.isEmpty(capitalId)) {
            Toast.makeText(getApplicationContext(), getString(R.string.choose_capital_type), Toast.LENGTH_SHORT).show();
            return;
        }
        //资金
        money = amountEdit.getText().toString();
        //祝福语
        greeting = greetingEdit.getText().toString();
        //口令
//        String word = wordEdit.getText().toString();

        if (TextUtils.isEmpty(greeting)) {
            greeting = greetingEdit.getHint().toString().substring(4);
        }

        String count = redPacketCountEdit.getText().toString();

        if (curRedPackerType == FLYAppConstant.LUCK_RED_PACKER) { //拼手气红包
            if (TextUtils.isEmpty(count)||Integer.parseInt(count)<=0){
                Toast.makeText(getApplicationContext(), getString(R.string.input_red_packet_number), Toast.LENGTH_SHORT).show();
                return;
            }
            redPackerCount = count;
        } else { //普通红包
            redPackerCount = TextUtils.isEmpty(count) ? String.valueOf(1) : count;
        }

        if (TextUtils.isEmpty(money)) {
            ToastUtil.showToast(mContext, getString(R.string.input_gift_count));
        } else if (Double.parseDouble(money) > 500 || Double.parseDouble(money) <= 0) {
            ToastUtil.showToast(mContext, getString(R.string.recharge_money_count));
        } else {
            money = Money.fromYuan(money);
            final String finalMoney = money;
            final String finalGreeting = greeting;
            final String finalRedPackerCount = redPackerCount;

            PaySecureHelper.inputPayPassword(this, getString(R.string.send_red_packet_amount), money, capitalName, password -> {

                RedPacketHelper.checkCapitalPassword(this, coreManager, userId, password,
                        error -> {
                            Toast.makeText(this, error.getMessage(), Toast.LENGTH_SHORT).show();
                        },
                        success -> {
                            sendRed(curRedPackerType, finalMoney, finalRedPackerCount, finalGreeting);
                        });
            });
        }
    }

    private void sendRed(int redPackerType, String pMoney, String redPackerCount, String greeting) {


        if (!coreManager.isLogin()) {
            return;
        }
        DialogHelper.showDefaulteMessageProgressDialog(mContext);

        String money = Money.fromYuan(pMoney);
        Map<String, String> params = new HashMap<>();
        params.put("user_id", userId);
        params.put("user_name", nickName);
        params.put("type", isGroupChat ? "2" : "1");
        params.put("capital_type", capitalId);
        params.put("red_envelopes_type", String.valueOf(redPackerType));
        params.put("red_envelope_capital", money);
        params.put("red_envelope_count", redPackerCount);
        params.put("red_envelope_pwd", "copyUserRedId_QZ");
        params.put("red_envelope_name", greeting);
        params.put("capital_count", money);


        RedPacketHelper.sendRedPacket(this, coreManager, params,
                error -> {
                    DialogHelper.dismissProgressDialog();
                },
                success -> {
                    DialogHelper.dismissProgressDialog();
                    RedPacketResult redPacket = success.getData();

                    ChatMessage message = new ChatMessage();
                    message.setType(XmppMessage.TYPE_RED);
                    message.setFromUserId(coreManager.getSelf().getUserId());
                    message.setFromUserName(coreManager.getSelf().getNickName());
                    message.setContent(greeting); // 祝福语
                    message.setFilePath(String.valueOf(redPackerType)); // 用FilePath来储存红包类型
//                    message.setFileSize(redPacket.getStatus()); //用filesize来储存红包状态
                    message.setObjectId(redPacket.toJson()); // 红包数据
                    Intent intent = new Intent();
                    intent.putExtra(FLYAppConstant.EXTRA_CHAT_MESSAGE, message.toJsonString());
                    setResult(ChatActivity.REQUEST_CODE_SEND_RED, intent);
                    finish();
                });
    }


    private void hideKeyboard() {
        InputMethodManager imm = (InputMethodManager) this.getSystemService(Context.INPUT_METHOD_SERVICE);
        if (imm != null && imm.isActive() && this.getCurrentFocus() != null) {
            if (this.getCurrentFocus().getWindowToken() != null) {
                imm.hideSoftInputFromWindow(this.getCurrentFocus().getWindowToken(), InputMethodManager.HIDE_NOT_ALWAYS);
            }
        }
    }

    /**
     * 发送红包
     *
     * @param type
     * @param pMoney
     * @param count
     * @param words
     * @param payPassword
     */
    public void sendRed(final String type, String pMoney, String count, final String words, String payPassword) {
        if (!coreManager.isLogin()) {
            return;
        }
        DialogHelper.showDefaulteMessageProgressDialog(mContext);

        String money = Money.fromYuan(pMoney);
        Map<String, String> params = new HashMap<>();
        params.put("type", type);
        params.put("moneyStr", money);
        params.put("count", count);
        params.put("greetings", words);
        params.put("toUserId", toUserId);
        PaySecureHelper.generateParam(
                mContext, payPassword, params,
                "" + type + money + count + words + toUserId,
                t -> {
                    DialogHelper.dismissProgressDialog();
                    ToastUtil.showToast(mContext, mContext.getString(R.string.tip_pay_secure_place_holder, t.getMessage()));
                }, (p, code) -> {
                    HttpUtils.get().url(coreManager.getConfig().REDPACKET_SEND)
                            .params(p)
                            .build()
                            .execute(new BaseCallback<RedPacket>(RedPacket.class) {

                                @Override
                                public void onResponse(ObjectResult<RedPacket> result) {
                                    DialogHelper.dismissProgressDialog();
                                    if (Result.checkSuccess(mContext, result)) {
                                        RedPacket redPacket = result.getData();
                                        String objectId = redPacket.getId();
                                        ChatMessage message = new ChatMessage();
                                        message.setType(XmppMessage.TYPE_RED);
                                        message.setFromUserId(coreManager.getSelf().getUserId());
                                        message.setFromUserName(coreManager.getSelf().getNickName());
                                        message.setContent(words); // 祝福语
                                        message.setFilePath(type); // 用FilePath来储存红包类型
                                        message.setFileSize(redPacket.getStatus()); //用filesize来储存红包状态
                                        message.setObjectId(objectId); // 红包id
                                        Intent intent = new Intent();
                                        intent.putExtra(FLYAppConstant.EXTRA_CHAT_MESSAGE, message.toJsonString());
//                                        setResult(viewPager.getCurrentItem() == 0 ? ChatActivity.REQUEST_CODE_SEND_RED_PT : ChatActivity.REQUEST_CODE_SEND_RED_KL, intent);
                                        finish();
                                    }
                                }

                                @Override
                                public void onError(Call call, Exception e) {
                                    DialogHelper.dismissProgressDialog();
                                }
                            });
                }
        );
    }


    @Override
    protected void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (requestCode == REQUEST_CODE_CAPITAL && resultCode == REQUEST_CODE_CAPITAL && data != null) {  //选择资产返回的资产数据
            capital = data.getParcelableExtra("capital");
            capitalText.setText(capital.capitalName);

            this.capitalName = capital.capitalName;
            this.capitalId = capital.capitalId;
            getCapitalByUser();
        }
    }


    /**
     * 根据资金类型返回可用金额
     */
    private void getCapitalByUser() {

        DialogHelper.showDefaulteMessageProgressDialog(this);

        Map<String, String> params = new HashMap<>();
        params.put("userId", coreManager.getSelf().getUserId());
        params.put("capitalId", capitalId);
        HttpUtils.post().url(coreManager.getConfig().GET_CAPITAL_BY_USER)
                .params(params)
                .build()
                .execute(new BaseCallback<Capital>(Capital.class) {
                    @Override
                    public void onResponse(ObjectResult<Capital> result) {
                        DialogHelper.dismissProgressDialog();
                        if (Result.checkSuccess(getBaseContext(), result)) {
                            String capital = String.format(getResources().getString(R.string.red_available_balance),
                                    result.getData().capital);
                            amountEdit.setHint(capital);
                        }
                    }

                    @Override
                    public void onError(Call call, Exception e) {
                        DialogHelper.dismissProgressDialog();
                        Toast.makeText(getApplicationContext(), getString(R.string.error_network), Toast.LENGTH_SHORT).show();
                    }
                });


    }


    private void setCurRedPacketStr(int type) {
        String commonRedPackerStr;
        String luckRedPackerStr;
        if (type == FLYAppConstant.COMMON_RED_PACKER) {
            commonRedPackerStr = String.format(
                    getResources().getString(R.string.red_cur_type),
                    getResources().getString(R.string.red_common_packer));
            luckRedPackerStr = String.format(
                    getResources().getString(R.string.red_alter_type),
                    getResources().getString(R.string.red_luck_packer));

            if (isGroupChat){
                numberLayout.setVisibility(View.VISIBLE);
                redTypeLayout.setVisibility(View.VISIBLE);
            }else {
                numberLayout.setVisibility(View.GONE);
                redTypeLayout.setVisibility(View.GONE);
            }

        } else {
            commonRedPackerStr = String.format(
                    getResources().getString(R.string.red_cur_type),
                    getResources().getString(R.string.red_luck_packer));
            luckRedPackerStr = String.format(
                    getResources().getString(R.string.red_alter_type),
                    getResources().getString(R.string.red_common_packer));
            numberLayout.setVisibility(View.VISIBLE);
        }

        curRedText.setText(commonRedPackerStr);
        alterRedText.setText(luckRedPackerStr);

        if ( curRedPackerType == FLYAppConstant.COMMON_RED_PACKER){
            virtualCoinsNumberText.setText(R.string.single_red_packer);
        }else {
            virtualCoinsNumberText.setText(R.string.red_number_of_issued_virtual_coins);
        }
    }
}
