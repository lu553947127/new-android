package com.ktw.fly.view.chatHolder;

import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.os.Looper;
import android.text.TextUtils;
import android.view.View;
import android.widget.TextView;
import android.widget.Toast;

import com.google.gson.Gson;
import com.ktw.fly.R;
import com.ktw.fly.bean.message.ChatMessage;
import com.ktw.fly.bean.message.MucRoomMember;
import com.ktw.fly.bean.redpacket.EventRedReceived;
import com.ktw.fly.bean.redpacket.OpenRedpacket;
import com.ktw.fly.bean.redpacket.RedDialogBean;
import com.ktw.fly.bean.redpacket.RedPacketResult;
import com.ktw.fly.db.dao.ChatMessageDao;
import com.ktw.fly.helper.RedPacketHelper;
import com.ktw.fly.ui.base.CoreManager;
import com.ktw.fly.ui.me.redpacket.RedDetailsActivity;
import com.ktw.fly.ui.me.redpacket.RedDetailsAuldActivity;
import com.ktw.fly.util.HtmlUtils;
import com.ktw.fly.util.LogUtils;
import com.ktw.fly.util.StringUtils;
import com.ktw.fly.util.ToastUtil;
import com.ktw.fly.view.NoDoubleClickListener;
import com.ktw.fly.view.redDialog.RedDialog;
import com.xuan.xuanhttplibrary.okhttp.HttpUtils;
import com.xuan.xuanhttplibrary.okhttp.callback.BaseCallback;
import com.xuan.xuanhttplibrary.okhttp.result.ObjectResult;
import com.xuan.xuanhttplibrary.okhttp.result.Result;

import java.util.HashMap;
import java.util.Map;

import de.greenrobot.event.EventBus;
import okhttp3.Call;

import static com.xuan.xuanhttplibrary.okhttp.result.Result.CODE_AUTH_RED_PACKET_GAIN;

class RedViewHolder extends AChatHolderInterface {

    TextView mTvContent;
    TextView mTvType;

    boolean isKeyRed;
    private RedDialog mRedDialog;

    @Override
    public int itemLayoutId(boolean isMysend) {
        return isMysend ? R.layout.chat_from_redpacket_item : R.layout.chat_to_redpacket_item;
    }

    @Override
    public void initView(View view) {
        mTvContent = view.findViewById(R.id.chat_text);
        mTvType = view.findViewById(R.id.tv_type);
        mRootView = view.findViewById(R.id.chat_warp_view);
    }

    @Override
    public void fillData(ChatMessage message) {
        String s = StringUtils.replaceSpecialChar(message.getContent());
        CharSequence charSequence = HtmlUtils.transform200SpanString(s, true);
        isKeyRed = "1".equals(message.getFilePath());

        if (mdata.getFileSize() == 2) {// 已领取
            mRootView.setAlpha(0.4f);
            mTvType.setText(getString(isKeyRed ? R.string.red_common_packer : R.string.red_luck_packer));
            mTvContent.setText(charSequence);
        } else {
            mTvType.setText(getString(isKeyRed ? R.string.red_common_packer : R.string.red_luck_packer));
            mRootView.setAlpha(1f);
            mTvContent.setText(charSequence);
        }


        mRootView.setOnClickListener(new NoDoubleClickListener() {
            @Override
            public void onNoDoubleClick(View view) {
                RedViewHolder.super.onClick(view);
            }
        });
    }

    @Override
    public boolean isOnClick() {
        return false; // 红包消息点击后回去请求接口，所以要做一个多重点击替换
    }

    @Override
    protected void onRootClick(View v) {
        clickRedpacket();
    }

    /*
    // 点击红包
    public void clickRedpacket() {
        if (selfGroupRole != null && MucRoomMember.disallowPublicAction(selfGroupRole)) {
            ToastUtil.showToast(mContext, getString(R.string.tip_action_disallow_place_holder, getString(MucRoomMember.getRoleName(selfGroupRole))));
            return;
        }
        final String token = CoreManager.requireSelfStatus(mContext).accessToken;
        final String redId = mdata.getObjectId();

        HashMap<String, String> params = new HashMap<>();
        params.put("access_token", token);
        params.put("id", redId);

        HttpUtils.get().url(CoreManager.requireConfig(mContext).RENDPACKET_GET)
                .params(params)
                .build()
                .execute(new BaseCallback<OpenRedpacket>(OpenRedpacket.class) {

                    @Override
                    public void onResponse(ObjectResult<OpenRedpacket> result) {
                        if (result.getData() != null) {
                            // 当resultCode==1时，表示可领取
                            // 当resultCode==0时，表示红包已过期、红包已退回、红包已领完
                            int resultCode = result.getResultCode();
                            OpenRedpacket openRedpacket = result.getData();
                            Bundle bundle = new Bundle();
                            Intent intent = new Intent(mContext, RedDetailsAuldActivity.class);
                            bundle.putSerializable("openRedpacket", openRedpacket);
                            bundle.putInt("redAction", 0);
                            if (!TextUtils.isEmpty(result.getResultMsg())) //resultMsg不为空表示红包已过期
                            {
                                bundle.putInt("timeOut", 1);
                            } else {
                                bundle.putInt("timeOut", 0);
                            }

                            bundle.putBoolean("isGroup", isGounp);
                            bundle.putString("mToUserId", mToUserId);
                            intent.putExtras(bundle);

                            // 红包不可领取, 或者我发的单聊红包直接跳转
                            if (resultCode != 1 || (!isGounp && isMysend)) {
                                mContext.startActivity(intent);
                            } else {
                                // 在群里面我领取过的红包直接跳转
                                if (isGounp && mdata.getFileSize() != 1) {
                                    mContext.startActivity(intent);
                                } else {
                                    if (mdata.getFilePath().equals("3")) {
                                        // 口令红包编辑输入框
                                        changeBottomViewInputText(mdata.getContent());
                                    } else {
                                        RedDialogBean redDialogBean = new RedDialogBean(openRedpacket.getPacket().getUserId(), openRedpacket.getPacket().getUserName(),
                                                openRedpacket.getPacket().getGreetings(), openRedpacket.getPacket().getId());
                                        mRedDialog = new RedDialog(mContext, redDialogBean, () -> openRedPacket(token, redId));
                                        mRedDialog.show();
                                    }
                                }
                            }
                        } else {
                            Toast.makeText(mContext, result.getResultMsg(), Toast.LENGTH_SHORT).show();
                        }

                    }

                    @Override
                    public void onError(Call call, Exception e) {
                    }
                });
    }
   */
    // 点击红包
    public void clickRedpacket() {
        if (selfGroupRole != null && MucRoomMember.disallowPublicAction(selfGroupRole)) {
            ToastUtil.showToast(mContext, getString(R.string.tip_action_disallow_place_holder, getString(MucRoomMember.getRoleName(selfGroupRole))));
            return;
        }

        final String userId = CoreManager.getSelf(mContext).getUserId();

        if (userId.equals(mdata.getFromUserId())) {  //自己的发的红包
            if (isGounp) {//群组
                gainRedPacket(mContext, userId);
            } else { //单聊
                rushRedPacket(mdata, false);
            }
        } else { //别人发的红包
            gainRedPacket(mContext, userId);
        }
    }

    /**
     * 验证是否抢过红包
     *
     * @param context
     * @param userId
     */
    private void gainRedPacket(Context context, String userId) {

        final RedPacketResult redPacket = new Gson().fromJson(mdata.getObjectId(), RedPacketResult.class);

        Map<String, String> params = new HashMap<>();
        params.put("redIdS", redPacket.redId);
        params.put("userId", userId);
        RedPacketHelper.gainRedPacket(context, params,
                error -> {
                },
                result -> {
                    if (Result.checkSuccess(context, result, false)) {  //未抢过当前红包

                        RedDialogBean redDialogBean =
                                new RedDialogBean(redPacket.userId, redPacket.userName, redPacket.redEnvelopeName, redPacket.redId);

                        mRedDialog = new RedDialog(mContext, redDialogBean,
                                () -> {
                                    rushRedPacket(mdata, true);
                                    mRedDialog.dismiss();
                                });
                        mRedDialog.show();

                    } else if (result.getResultCode() == CODE_AUTH_RED_PACKET_GAIN) { //已抢过红包
                        rushRedPacket(mdata, false);
                    }
                });
    }


    /**
     * 抢红包
     */
    public void rushRedPacket(final ChatMessage message, boolean isGrab) {
        HashMap<String, String> params = new HashMap<String, String>();

        final RedPacketResult redPacket = new Gson().fromJson(message.getObjectId(), RedPacketResult.class);

        String userId = CoreManager.getSelf(mContext).getUserId();
        String receiveName = CoreManager.getSelf(mContext).getNickName();
        params.put("redId", redPacket.redId);
        params.put("userId", userId);
        params.put("receiveName", receiveName);
        params.put("currencyId", redPacket.currencyId);
        params.put("currencyName", redPacket.currencyName);
        params.put("type", redPacket.type);
        RedPacketHelper.rushRedPacket(mContext, params,
                error -> {
                },
                result -> {
                    if (isGrab) {  //第一次抢红包
                        mdata.setFileSize(2);
                        ChatMessageDao.getInstance().updateChatMessageReceiptStatus(userId, mToUserId, mdata.getPacketId());
                        fillData(mdata);
                        // 更新余额
                        CoreManager.updateMyBalance();

                        if (isGounp) {
                            EventBus.getDefault().post(new EventRedReceived(result));
                        } else {
                            if (!TextUtils.equals(userId, result.redUser.userId)) {
                                EventBus.getDefault().post(new EventRedReceived(result));
                            }
                        }
                    }
                    Bundle bundle = new Bundle();
                    Intent intent = new Intent(mContext, RedDetailsActivity.class);
                    bundle.putParcelable("openRedpacket", result);
                    bundle.putInt("redAction", 0);
                    bundle.putBoolean("isGroup", isGounp);
                    bundle.putString("mToUserId", mToUserId);
                    bundle.putSerializable("redPacket", redPacket);
                    intent.putExtras(bundle);
                    mContext.startActivity(intent);

                });
    }


    // 打开红包
    public void openRedPacket(final String token, String redId) {
        HashMap<String, String> params = new HashMap<String, String>();
        params.put("access_token", token);
        params.put("id", redId);

        HttpUtils.get().url(CoreManager.requireConfig(mContext).REDPACKET_OPEN)
                .params(params)
                .build()
                .execute(new BaseCallback<OpenRedpacket>(OpenRedpacket.class) {

                    @Override
                    public void onResponse(ObjectResult<OpenRedpacket> result) {
                        if (mRedDialog != null) {
                            mRedDialog.dismiss();
                        }
                        if (result.getData() != null) {
                            mdata.setFileSize(2);
                            ChatMessageDao.getInstance().updateChatMessageReceiptStatus(mLoginUserId, mToUserId, mdata.getPacketId());
                            fillData(mdata);

                            OpenRedpacket openRedpacket = result.getData();
                            Bundle bundle = new Bundle();
                            Intent intent = new Intent(mContext, RedDetailsAuldActivity.class);
                            bundle.putSerializable("openRedpacket", openRedpacket);
                            bundle.putInt("redAction", 1);
                            bundle.putInt("timeOut", 0);

                            bundle.putBoolean("isGroup", isGounp);
                            bundle.putString("mToUserId", mToUserId);
                            intent.putExtras(bundle);
                            mContext.startActivity(intent);
                            // 更新余额
                            CoreManager.updateMyBalance();

                            if (!TextUtils.equals(mLoginUserId, openRedpacket.getPacket().getUserId())
                                    && !isGounp) {
//                                EventBus.getDefault().post(new EventRedReceived(openRedpacket));
                            }
                        } else {
                            Toast.makeText(mContext, result.getResultMsg(), Toast.LENGTH_SHORT).show();
                        }
                    }

                    @Override
                    public void onError(Call call, Exception e) {
                        if (mRedDialog != null) {
                            mRedDialog.dismiss();
                        }
                    }
                });
    }

    // 通知更新输入框
    private void changeBottomViewInputText(String text) {
        mHolderListener.onChangeInputText(text);
    }

    @Override
    public boolean enableSendRead() {
        return true;
    }
}
