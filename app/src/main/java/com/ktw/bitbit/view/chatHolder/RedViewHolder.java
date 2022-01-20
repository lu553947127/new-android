package com.ktw.bitbit.view.chatHolder;

import android.annotation.SuppressLint;
import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.text.TextUtils;
import android.view.View;
import android.widget.TextView;
import android.widget.Toast;

import com.google.gson.Gson;
import com.ktw.bitbit.R;
import com.ktw.bitbit.bean.message.ChatMessage;
import com.ktw.bitbit.bean.message.MucRoomMember;
import com.ktw.bitbit.bean.redpacket.EventRedReceived;
import com.ktw.bitbit.bean.redpacket.OpenRedpacket;
import com.ktw.bitbit.bean.redpacket.RedDialogBean;
import com.ktw.bitbit.bean.redpacket.RedPacketResult;
import com.ktw.bitbit.bean.redpacket.RushRedPacket;
import com.ktw.bitbit.db.dao.ChatMessageDao;
import com.ktw.bitbit.helper.RedPacketHelper;
import com.ktw.bitbit.ui.base.CoreManager;
import com.ktw.bitbit.ui.me.redpacket.RedDetailsActivity;
import com.ktw.bitbit.ui.me.redpacket.RedDetailsAuldActivity;
import com.ktw.bitbit.util.HtmlUtils;
import com.ktw.bitbit.util.StringUtils;
import com.ktw.bitbit.util.ToastUtil;
import com.ktw.bitbit.view.NoDoubleClickListener;
import com.ktw.bitbit.view.redDialog.RedDialog;
import com.ktw.bitbit.view.redDialog.RedLootAllDialog;
import com.xuan.xuanhttplibrary.okhttp.HttpUtils;
import com.xuan.xuanhttplibrary.okhttp.callback.BaseCallback;
import com.xuan.xuanhttplibrary.okhttp.result.ObjectResult;

import java.util.HashMap;
import java.util.Map;

import de.greenrobot.event.EventBus;
import okhttp3.Call;

class RedViewHolder extends AChatHolderInterface {

    TextView mTvContent;
    TextView mTvType;

    boolean isKeyRed;
    private RedDialog mRedDialog;
    private RedLootAllDialog lootRedDialog;

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

    @SuppressLint("NewApi")
    @Override
    public void fillData(ChatMessage message) {
        String s = StringUtils.replaceSpecialChar(message.getContent());
        CharSequence charSequence = HtmlUtils.transform200SpanString(s, true);
        isKeyRed = "1".equals(message.getFilePath());

        if (mdata.getFileSize() == 2) {// 已领取
            if (mdata.isMySend()) {
                mRootView.setBackgroundResource(R.mipmap.red_backet_send_loot);
            } else {
                mRootView.setBackgroundResource(R.mipmap.red_backet_receive_loot);
            }
            mTvType.setText(getString(isKeyRed ? R.string.red_common_packer : R.string.red_luck_packer));
            mTvType.setTextColor(mContext.getColor(R.color.white));
            mTvContent.setText(charSequence);
        } else {
            if (mdata.isMySend()) {
                mRootView.setBackgroundResource(R.mipmap.red_backet_send);
            } else {
                mRootView.setBackgroundResource(R.mipmap.red_backet_receive);
            }
            mTvType.setText(getString(isKeyRed ? R.string.red_common_packer : R.string.red_luck_packer));
            mTvType.setTextColor(mContext.getColor(R.color.red_backet_loot_text_color));
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

                    if ((result.redUser == 1 && result.redStatus == 2) ||//当前用户没抢过红包 并且红包未抢完
                            (result.redUser == 1 && result.redStatus == 4)) { //当前用户没抢过红包 并且红包没过期

                        RedDialogBean redDialogBean =
                                new RedDialogBean(redPacket.userId, redPacket.userName, redPacket.redEnvelopeName, redPacket.redId);

                        mRedDialog = new RedDialog(mContext, redDialogBean,
                                () -> {
                                    rushRedPacket(mdata, true);
                                    mRedDialog.dismiss();
                                });
                        mRedDialog.show();

                    } else if (result.redUser == 1 && result.redStatus == 1) { //当前用户没抢过并且红包已抢光

                        RedDialogBean redDialogBean =
                                new RedDialogBean(redPacket.userId, redPacket.userName,
                                        getString(R.string.red_packet_loot_all), redPacket.redId);
                        lootRedDialog = new RedLootAllDialog(mContext, redDialogBean, () -> {
                            rushRedPacket(mdata);
                            lootRedDialog.dismiss();
                        });
                        lootRedDialog.show();

                    } else if (result.redUser == 1 && result.redStatus == 3) { //当前用户没抢过并且红包已过期

                        RedDialogBean redDialogBean =
                                new RedDialogBean(redPacket.userId, redPacket.userName,
                                        getString(R.string.red_packet_past), redPacket.redId);
                        lootRedDialog = new RedLootAllDialog(mContext, redDialogBean, () -> {
                            rushRedPacket(mdata);
                            lootRedDialog.dismiss();
                        });
                        lootRedDialog.show();

                    } else if (result.redUser == 0) {

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
                    RushRedPacket rushRedPacket = result.getData();
                    if (isGrab) {  //第一次抢红包
                        mdata.setFileSize(2);
                        ChatMessageDao.getInstance().updateChatMessageReceiptStatus(userId, mToUserId, mdata.getPacketId());
                        fillData(mdata);
                        // 更新余额
                        CoreManager.updateMyBalance();

                        if (isGounp) {
                            EventBus.getDefault().post(new EventRedReceived(rushRedPacket));
                        } else {
                            if (!TextUtils.equals(userId, rushRedPacket.redUser.userId)) {
                                EventBus.getDefault().post(new EventRedReceived(rushRedPacket));
                            }
                        }
                    }
                    Bundle bundle = new Bundle();
                    Intent intent = new Intent(mContext, RedDetailsActivity.class);
                    bundle.putParcelable("openRedpacket", rushRedPacket);
                    bundle.putInt("redAction", 0);
                    if ((userId.equals(mdata.getFromUserId()))) {
                        bundle.putBoolean("null", true);
                    }
                    bundle.putBoolean("isGroup", isGounp);
                    bundle.putString("mToUserId", mToUserId);
                    bundle.putSerializable("redPacket", redPacket);
                    intent.putExtras(bundle);
                    mContext.startActivity(intent);
                });
    }


    public void rushRedPacket(final ChatMessage message) {
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
                    Bundle bundle = new Bundle();
                    Intent intent = new Intent(mContext, RedDetailsActivity.class);
                    bundle.putParcelable("openRedpacket", result.getData());
                    bundle.putInt("redAction", 0);
                    bundle.putBoolean("isGroup", isGounp);
                    bundle.putString("mToUserId", message.getToUserId());
                    bundle.putSerializable("redPacket", redPacket);
                    bundle.putBoolean("null", true);
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
