package com.ktw.fly.pay;

import android.content.Intent;
import android.os.Bundle;
import android.text.TextUtils;
import android.view.View;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import com.alibaba.fastjson.JSON;
import com.ktw.fly.FLYAppConstant;
import com.ktw.fly.R;
import com.ktw.fly.bean.Friend;
import com.ktw.fly.bean.Transfer;
import com.ktw.fly.bean.TransferReceive;
import com.ktw.fly.bean.event.EventTransfer;
import com.ktw.fly.bean.message.ChatMessage;
import com.ktw.fly.db.dao.FriendDao;
import com.ktw.fly.ui.base.BaseActivity;
import com.ktw.fly.ui.base.CoreManager;
import com.ktw.fly.ui.me.redpacket.WxPayBlance;
import com.ktw.fly.ui.tool.ButtonColorChange;
import com.ktw.fly.util.TimeUtils;
import com.xuan.xuanhttplibrary.okhttp.HttpUtils;
import com.xuan.xuanhttplibrary.okhttp.callback.BaseCallback;
import com.xuan.xuanhttplibrary.okhttp.result.ObjectResult;

import java.util.HashMap;

import de.greenrobot.event.EventBus;
import okhttp3.Call;

/**
 * 转账详情
 */
public class TransferMoneyDetailActivity extends BaseActivity {
    public static final String TRANSFER_DETAIL = "transfer_detail";
    public static final int EVENT_REISSUE_TRANSFER = 10001;// 重发转账消息
    public static final int EVENT_SURE_RECEIPT = 10002; // 确认领取

    private String mMsgId;
    private Transfer mTransfer;

    private boolean isMySend;// 转账人为我
    private String mToUserName;// 收账人昵称

    private ImageView mTransferStatusIv;
    private TextView mTransferTips1Tv, mTransferTips2Tv, mTransferTips3Tv;
    private TextView mTransferMoneyTv;
    private Button mTransferSureBtn;
    private TextView mTransferTime1Tv, mTransferTime2Tv;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_transfer_money_detail);
        mMsgId = getIntent().getStringExtra(FLYAppConstant.EXTRA_MESSAGE_ID);
        String detail = getIntent().getStringExtra(TRANSFER_DETAIL);
        mTransfer = JSON.parseObject(detail, Transfer.class);
        if (mTransfer == null) {
            return;
        }
        isMySend = TextUtils.equals(mTransfer.getUserId(), coreManager.getSelf().getUserId());
        if (isMySend) {
            Friend friend = FriendDao.getInstance().getFriend(coreManager.getSelf().getUserId(), mTransfer.getToUserId());
            mToUserName = TextUtils.isEmpty(friend.getRemarkName()) ? friend.getNickName() : friend.getRemarkName();
        }
        initActionBar();
        initView();
        initData();
        initEvent();
    }

    private void initActionBar() {
        getSupportActionBar().hide();
        findViewById(R.id.iv_title_left).setOnClickListener(v -> finish());
    }

    private void initView() {
        mTransferStatusIv = findViewById(R.id.ts_status_iv);
        mTransferMoneyTv = findViewById(R.id.ts_money);
        mTransferTips1Tv = findViewById(R.id.ts_tip1_tv);
        mTransferTips2Tv = findViewById(R.id.ts_tip2_tv);
        mTransferTips3Tv = findViewById(R.id.ts_tip3_tv);
        mTransferTime1Tv = findViewById(R.id.ts_time1_tv);

        mTransferSureBtn = findViewById(R.id.ts_sure_btn);
        ButtonColorChange.colorChange(this, mTransferSureBtn);
        mTransferTime2Tv = findViewById(R.id.ts_time2_tv);
    }

    private void initData() {
        mTransferSureBtn.setVisibility(View.GONE);
        mTransferMoneyTv.setText("￥" + String.valueOf(mTransfer.getMoney()));
        mTransferTime1Tv.setText(getString(R.string.transfer_time, TimeUtils.f_long_2_str(mTransfer.getCreateTime() * 1000)));

        if (mTransfer.getStatus() == 1) {// 待领取
            mTransferStatusIv.setImageResource(R.drawable.ic_ts_status2);
            if (isMySend) {
                mTransferTips1Tv.setText(getString(R.string.transfer_wait_receive1, mToUserName));
                mTransferTips2Tv.setText(getString(R.string.transfer_receive_status1));
                mTransferTips3Tv.setText(getString(R.string.transfer_receive_click_status1));
            } else {
                mTransferSureBtn.setVisibility(View.VISIBLE);
                mTransferTips1Tv.setText(getString(R.string.transfer_push_receive1));
                mTransferTips2Tv.setText(getString(R.string.transfer_push_receive2));
            }
        } else if (mTransfer.getStatus() == 2) {// 已收钱
            mTransferStatusIv.setImageResource(R.drawable.ic_ts_status1);
            if (isMySend) {
                mTransferTips1Tv.setText(getString(R.string.transfer_wait_receive2, mToUserName));
                mTransferTips2Tv.setText(getString(R.string.transfer_receive_status2));
                mTransferTips3Tv.setVisibility(View.GONE);
            } else {
                mTransferTips1Tv.setText(getString(R.string.transfer_push_receive3));
                mTransferTips2Tv.setVisibility(View.GONE);
                mTransferTips3Tv.setText(getString(R.string.transfer_receive_click_status2));
            }
            mTransferTime2Tv.setText(getString(R.string.transfer_receive_time, TimeUtils.f_long_2_str(mTransfer.getReceiptTime() * 1000)));
        } else {// 已退回
            mTransferStatusIv.setImageResource(R.drawable.ic_ts_status3);
            mTransferTips1Tv.setText(getString(R.string.transfer_wait_receive3));
            if (isMySend) {
                mTransferTips2Tv.setText(getString(R.string.transfer_receive_status3));
                mTransferTips3Tv.setText(getString(R.string.transfer_receive_click_status2));
            }
            mTransferTime2Tv.setText(getString(R.string.transfer_out_time, TimeUtils.f_long_2_str(mTransfer.getOutTime() * 1000)));
        }
    }

    private void initEvent() {
        mTransferTips3Tv.setOnClickListener(v -> {
            if (mTransfer.getStatus() == 1) {
                // 通知到聊天界面刷新ui
                ChatMessage chatMessage = new ChatMessage();
                chatMessage.setType(EVENT_REISSUE_TRANSFER);
                chatMessage.setPacketId(mMsgId);
                EventBus.getDefault().post(new EventTransfer(chatMessage));
                finish();
                return;
            }
            // 查看零钱
            startActivity(new Intent(mContext, WxPayBlance.class));
        });

        mTransferSureBtn.setOnClickListener(v -> acceptTransfer(coreManager.getSelfStatus().accessToken, mTransfer.getId()));
    }

    // 接受转账
    private void acceptTransfer(String token, String redId) {
        HashMap<String, String> params = new HashMap<String, String>();
        params.put("access_token", token);
        params.put("id", redId);

        HttpUtils.get().url(CoreManager.requireConfig(mContext).SKTRANSFER_RECEIVE_TRANSFER)
                .params(params)
                .build()
                .execute(new BaseCallback<TransferReceive>(TransferReceive.class) {

                    @Override
                    public void onResponse(ObjectResult<TransferReceive> result) {
                        if (result.getResultCode() == 1 && result.getData() != null) {
                            TransferReceive transferReceive = result.getData();
                            mTransfer.setStatus(2);
                            mTransfer.setReceiptTime(transferReceive.getTime());
                            mTransferTips1Tv.setVisibility(View.GONE);
                            initData();

                            // 通知到聊天界面刷新ui
                            ChatMessage chatMessage = new ChatMessage();
                            chatMessage.setType(EVENT_SURE_RECEIPT);
                            chatMessage.setPacketId(mMsgId);
                            EventBus.getDefault().post(new EventTransfer(chatMessage));
                        } else {
                            Toast.makeText(TransferMoneyDetailActivity.this, result.getResultMsg(), Toast.LENGTH_SHORT).show();
                        }
                    }

                    @Override
                    public void onError(Call call, Exception e) {

                    }
                });
    }
}
