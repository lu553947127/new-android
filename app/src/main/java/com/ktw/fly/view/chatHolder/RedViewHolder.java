package com.ktw.fly.view.chatHolder;

import android.content.Intent;
import android.os.Bundle;
import android.text.TextUtils;
import android.view.View;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import com.ktw.fly.R;
import com.ktw.fly.bean.message.ChatMessage;
import com.ktw.fly.bean.message.MucRoomMember;
import com.ktw.fly.bean.redpacket.EventRedReceived;
import com.ktw.fly.bean.redpacket.OpenRedpacket;
import com.ktw.fly.bean.redpacket.RedDialogBean;
import com.ktw.fly.db.dao.ChatMessageDao;
import com.ktw.fly.ui.base.CoreManager;
import com.ktw.fly.ui.me.redpacket.RedDetailsActivity;
import com.ktw.fly.util.HtmlUtils;
import com.ktw.fly.util.StringUtils;
import com.ktw.fly.util.ToastUtil;
import com.ktw.fly.view.NoDoubleClickListener;
import com.ktw.fly.view.redDialog.RedDialog;
import com.xuan.xuanhttplibrary.okhttp.HttpUtils;
import com.xuan.xuanhttplibrary.okhttp.callback.BaseCallback;
import com.xuan.xuanhttplibrary.okhttp.result.ObjectResult;

import java.util.HashMap;

import de.greenrobot.event.EventBus;
import okhttp3.Call;

class RedViewHolder extends AChatHolderInterface {

    TextView mTvContent;
    TextView mTvType;

    boolean isKeyRed;
    private RedDialog mRedDialog;
    private ImageView iv_image;

    @Override
    public int itemLayoutId(boolean isMysend) {
        return isMysend ? R.layout.chat_from_item_redpacket : R.layout.chat_to_item_redpacket;
    }

    @Override
    public void initView(View view) {
        mTvContent = view.findViewById(R.id.chat_text);
        mTvType = view.findViewById(R.id.tv_type);
        mRootView = view.findViewById(R.id.chat_warp_view);
        iv_image = view.findViewById(R.id.iv_image);

    }

    @Override
    public void fillData(ChatMessage message) {
        String s = StringUtils.replaceSpecialChar(message.getContent());
        CharSequence charSequence = HtmlUtils.transform200SpanString(s, true);
        if (mdata.getFileSize() == 2) {// 已领取
            mRootView.setAlpha(0.6f);
            mTvType.setText("");
            mTvContent.setText(getString(R.string.redemption_of_red_envelope));
        } else {
            mTvType.setText(getString(isKeyRed ? R.string.chat_kl_red : R.string.chat_red_new));
            mRootView.setAlpha(1f);
            mTvContent.setText(charSequence);
        }

        isKeyRed = "3".equals(message.getFilePath());

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
                            Intent intent = new Intent(mContext, RedDetailsActivity.class);
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
                            Intent intent = new Intent(mContext, RedDetailsActivity.class);
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
                                EventBus.getDefault().post(new EventRedReceived(openRedpacket));
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
