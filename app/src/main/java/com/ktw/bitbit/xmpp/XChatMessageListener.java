package com.ktw.bitbit.xmpp;

import android.annotation.SuppressLint;
import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;
import android.os.Vibrator;
import android.text.TextUtils;
import android.util.Log;

import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.JSONObject;
import com.ktw.bitbit.FLYAppConstant;
import com.ktw.bitbit.FLYApplication;
import com.ktw.bitbit.R;
import com.ktw.bitbit.FLYReporter;
import com.ktw.bitbit.audio.NoticeVoicePlayer;
import com.ktw.bitbit.bean.CodePay;
import com.ktw.bitbit.bean.Contact;
import com.ktw.bitbit.bean.Friend;
import com.ktw.bitbit.bean.MyZan;
import com.ktw.bitbit.bean.RoomMember;
import com.ktw.bitbit.bean.User;
import com.ktw.bitbit.bean.event.EventNewNotice;
import com.ktw.bitbit.bean.event.EventNotifyByTag;
import com.ktw.bitbit.bean.event.EventSyncFriendOperating;
import com.ktw.bitbit.bean.event.EventTransfer;
import com.ktw.bitbit.bean.event.MessageContactEvent;
import com.ktw.bitbit.bean.event.MessageEventHongdian;
import com.ktw.bitbit.bean.message.ChatMessage;
import com.ktw.bitbit.bean.message.NewFriendMessage;
import com.ktw.bitbit.bean.message.XmppMessage;
import com.ktw.bitbit.broadcast.CardcastUiUpdateUtil;
import com.ktw.bitbit.broadcast.MsgBroadcast;
import com.ktw.bitbit.broadcast.MucgroupUpdateUtil;
import com.ktw.bitbit.call.CallConstants;
import com.ktw.bitbit.call.JitsistateMachine;
import com.ktw.bitbit.call.MessageCallTypeChange;
import com.ktw.bitbit.call.MessageCallingEvent;
import com.ktw.bitbit.call.MessageEventMeetingInvited;
import com.ktw.bitbit.call.MessageEventSipEVent;
import com.ktw.bitbit.call.MessageHangUpPhone;
import com.ktw.bitbit.db.dao.ChatMessageDao;
import com.ktw.bitbit.db.dao.ContactDao;
import com.ktw.bitbit.db.dao.FriendDao;
import com.ktw.bitbit.db.dao.MyZanDao;
import com.ktw.bitbit.db.dao.NewFriendDao;
import com.ktw.bitbit.db.dao.RoomMemberDao;
import com.ktw.bitbit.helper.FriendHelper;
import com.ktw.bitbit.pay.EventPaymentSuccess;
import com.ktw.bitbit.pay.EventReceiptSuccess;
import com.ktw.bitbit.ui.base.CoreManager;
import com.ktw.bitbit.ui.circle.MessageEventNotifyDynamic;
import com.ktw.bitbit.ui.login.AuthLoginActivity;
import com.ktw.bitbit.ui.message.ChatActivity;
import com.ktw.bitbit.ui.message.HandleSyncMoreLogin;
import com.ktw.bitbit.ui.mucfile.XfileUtils;
import com.ktw.bitbit.util.Constants;
import com.ktw.bitbit.util.PreferenceUtils;
import com.ktw.bitbit.util.TimeUtils;
import com.ktw.bitbit.xmpp.listener.ChatMessageListener;

import org.json.JSONException;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.UUID;
import java.util.concurrent.TimeUnit;

import de.greenrobot.event.EventBus;

import static android.content.Context.VIBRATOR_SERVICE;

/**
 * Created by Administrator on 2017/11/24.
 */

public class XChatMessageListener {
    private CoreService mService;
    private String mLoginUserId;
    private Map<String, String> mMsgIDMap = new HashMap<>();// 因为多点登录 转发 问题，先用一个简单的方法去重

    public XChatMessageListener(CoreService service) {
        mService = service;
        mLoginUserId = CoreManager.requireSelf(service).getUserId();
        mMsgIDMap = new HashMap<>();
    }

    private Context getContext() {
        return mService;
    }

    public void onReceMessage(ChatMessage chatMessage) {
        newIncomingMessage(chatMessage);
    }

    private void newIncomingMessage(ChatMessage chatMessage) {
        String fromUserId = chatMessage.getFromUserId();
        String toUserName = chatMessage.getToUserName();
        String toUserId = chatMessage.getToUserId();
        Log.e("msg", "收到单聊消息" + chatMessage.toString());

        if (mMsgIDMap.containsKey(chatMessage.getPacketId())) {
            return;
        }
        if (mMsgIDMap.size() > 20) {
            mMsgIDMap.clear();
        }
        mMsgIDMap.put(chatMessage.getPacketId(), chatMessage.getPacketId());

        int type = chatMessage.getType();
        if (type == 0) { // 消息过滤
            return;
        }

        ChatMessageDao.getInstance().decryptDES(chatMessage);// 解密

        if (chatMessage.getType() >= XmppMessage.TYPE_SYNC_OTHER
                && chatMessage.getType() <= XmppMessage.TYPE_SYNC_GROUP) {
            HandleSyncMoreLogin.distributionChatMessage(chatMessage, mService, chatMessage.isDelayMsg());
            return;
        }

        if (chatMessage.getType() == XmppMessage.TYPE_AUTH_LOGIN) {
            AuthLoginActivity.start(mService, chatMessage.getContent());
            return;
        }

        /*
        服务器发的消息 fromId='10005/Server'
         */
        /**
         *  我的设备发送过来的消息
         * 1.fromUserId等于当前登录的id
         * 2.toUserId包含当前登录的id(android给我的设备发消息toUserId传的为当前登录的id，但是ios给我的设备发消息传的为当前登录id+设备名，所以不能用equals)
         * 3.因为服务端发送的消息有可能会符合以上的两个条件(ex:部分群控制消息...)，所以还需要判断下fromId
         */
        if (fromUserId.equals(mLoginUserId)
                && toUserId.contains(mLoginUserId)
                && !chatMessage.getFromId().toLowerCase().contains("server")) {

            if (chatMessage.getType() == 26) {
                String packetId = chatMessage.getContent();
                ChatMessageDao.getInstance().updateMessageRead(mLoginUserId, chatMessage.getFromId(), packetId, true);
                boolean isReadChange = ChatMessageDao.getInstance().updateReadMessage(mLoginUserId, chatMessage.getFromId(), packetId);
                Intent intent = new Intent();
                Bundle bundle = new Bundle();
                bundle.putString("packetId", packetId);
                bundle.putBoolean("isReadChange", isReadChange);
                intent.setAction(com.ktw.bitbit.broadcast.OtherBroadcast.IsRead);
                intent.putExtras(bundle);
                mService.sendBroadcast(intent);
                return;
            }

            if (ChatMessageDao.getInstance().saveNewSingleChatMessage(mLoginUserId, chatMessage.getFromId(), chatMessage)) {
                ListenerManager.getInstance().notifyNewMesssage(mLoginUserId, chatMessage.getFromId(), chatMessage, false);
            }
            return;
        }

        /**
         * 转发消息且我为发送方，该条消息需要换表存储
         */
        boolean isNeedChangeMsgTableSave = false;
        if (fromUserId.equals(mLoginUserId)
                && !chatMessage.getFromId().toLowerCase().contains("server")) {
            isNeedChangeMsgTableSave = true;
            // 多点登录下 其他端发过来的消息肯定是已经上传成功了，这里加上
            chatMessage.setUpload(true);
            chatMessage.setUploadSchedule(100);
        } else {
            // 收到了别人的消息
            Friend friend = FriendDao.getInstance().getFriend(mLoginUserId, chatMessage.getFromUserId());
            if (friend != null && friend.getStatus() != -1 && friend.getOfflineNoPushMsg() == 0) {
                mService.notificationMessage(chatMessage, false);// 调用本地通知
            }
        }

        // 音视频相关消息100-134
        if (type >= XmppMessage.TYPE_IS_CONNECT_VOICE && type <= XmppMessage.TYPE_TALK_KICK) {
            chatAudioVideo(chatMessage);
            return;
        }

        // 朋友圈相关消息 301-304
        if (type >= XmppMessage.DIANZAN && type <= XmppMessage.ATMESEE) {
            chatDiscover(toUserName, chatMessage);
            return;
        }

        // 新朋友相关消息 500-515
        if (type >= XmppMessage.TYPE_SAYHELLO && type <= XmppMessage.TYPE_BACK_DELETE) {
            chatFriend(chatMessage);
            return;
        }

        // 群文件上传、下载、删除 401-403
        if (type >= XmppMessage.TYPE_MUCFILE_ADD && type <= XmppMessage.TYPE_MUCFILE_DOWN) {
            Friend friend = FriendDao.getInstance().getFriend(mLoginUserId, chatMessage.getObjectId());
            chatGroupTipForMe(toUserName, chatMessage, friend);
            return;
        }

        /*
        服务端已修改了发送群控制消息的逻辑(不再遍历群成员之后以单聊的方式发送，而是直接发送到群组内(但是部分协议还是会以单聊的方式发送给个人,ex:邀请群成员...)，
        但服务端代码还未上传(怕影响老版本用户)，所以我们在XMuChatMessageListener内的处理暂时还未用上，且该类中的逻辑不能删除，还需要添加判断本地是否有存在该条消息
        防止服务端代码上传后 单、群聊监听都收到同一条消息重复处理
         */
        if ((type >= XmppMessage.TYPE_CHANGE_NICK_NAME && type <= XmppMessage.NEW_MEMBER)
                || type == XmppMessage.TYPE_SEND_MANAGER
//                || type == XmppMessage.TYPE_UPDATE_ROLE
        ) {
            Friend friend = FriendDao.getInstance().getFriend(mLoginUserId, chatMessage.getObjectId());
            if (ChatMessageDao.getInstance().hasSameMessage(mLoginUserId, chatMessage.getObjectId(), chatMessage.getPacketId())) {
                // 本地已经保存了这条消息，不处理，因为有些协议群组内也发了，单聊也发了
                Log.e("msg", "Return 6");
                return;
            }
            if (friend != null || type == XmppMessage.NEW_MEMBER) {
                if (chatMessage.getFromUserId().equals(mLoginUserId)) {
                    chatGroupTipFromMe(toUserName, chatMessage, friend);
                } else {
                    chatGroupTipForMe(toUserName, chatMessage, friend);
                }
            }
            return;
        }

        // 群组控制消息[2] 915-925
        if (type >= XmppMessage.TYPE_CHANGE_SHOW_READ && type <= XmppMessage.TYPE_GROUP_TRANSFER) {
            boolean isJumpOver = false;
            if (type == XmppMessage.TYPE_GROUP_VERIFY) {// 群验证跳过判断，因为自己发送的群验证消息object为一个json
                isJumpOver = true;
            }
            if (!isJumpOver && ChatMessageDao.getInstance().hasSameMessage(mLoginUserId, chatMessage.getObjectId(), chatMessage.getPacketId())) {
                // 本地已经保存了这条消息，不处理，因为有些协议群组内也发了，单聊也发了
                Log.e("msg", "Return 7");
                return;
            }
            chatGroupTip2(type, chatMessage, toUserName);
            return;
        }

        // 后台操作发送过来的xmpp
        if (type == XmppMessage.TYPE_DISABLE_GROUP) {
            if (chatMessage.getContent().equals("-1")) {// 锁定
                FriendDao.getInstance().updateFriendGroupStatus(mLoginUserId, chatMessage.getObjectId(), 3);// 更新本地群组状态
            } else if (chatMessage.getContent().equals("1")) {// 解锁
                FriendDao.getInstance().updateFriendGroupStatus(mLoginUserId, chatMessage.getObjectId(), 0);// 更新本地群组状态
            }
            mService.sendBroadcast(new Intent(MsgBroadcast.ACTION_DISABLE_GROUP_BY_SERVICE));
            return;
        }

        // 面对面建群有人加入、退出
        if (type == XmppMessage.TYPE_FACE_GROUP_NOTIFY) {
            MsgBroadcast.broadcastFaceGroupNotify(FLYApplication.getContext(), "notify_list");
            return;
        }

        // 表示这是已读回执类型的消息
        if (chatMessage.getType() == XmppMessage.TYPE_READ) {
            String packetId = chatMessage.getContent();

            if (chatMessage.getFromUserId().equals(mLoginUserId)) {// 其他端发送过来的已读
                ChatMessage msgById = ChatMessageDao.getInstance().findMsgById(mLoginUserId, chatMessage.getToUserId(), packetId);
                if (msgById != null && msgById.getIsReadDel()) {// 在其他端已读了该条阅后即焚消息，本端也需要删除
                    if (ChatMessageDao.getInstance().deleteSingleChatMessage(mLoginUserId, chatMessage.getToUserId(), packetId)) {
                        Intent intent = new Intent();
                        Bundle bundle = new Bundle();
                        bundle.putString("MULTI_LOGIN_READ_DELETE_PACKET", packetId);
                        intent.setAction(com.ktw.bitbit.broadcast.OtherBroadcast.MULTI_LOGIN_READ_DELETE);
                        intent.putExtras(bundle);
                        mService.sendBroadcast(intent);
                    }
                }
            } else {
                ChatMessageDao.getInstance().updateMessageRead(mLoginUserId, fromUserId, packetId, true);// 更新状态为已读
                boolean isReadChange = ChatMessageDao.getInstance().updateReadMessage(mLoginUserId, fromUserId, packetId);
                // 发送广播通知聊天页面，将未读的消息修改为已读
                Intent intent = new Intent();
                Bundle bundle = new Bundle();
                bundle.putString("packetId", packetId);
                bundle.putBoolean("isReadChange", isReadChange);
                intent.setAction(com.ktw.bitbit.broadcast.OtherBroadcast.IsRead);
                intent.putExtras(bundle);
                mService.sendBroadcast(intent);
            }
            return;
        }

        if (type == XmppMessage.TYPE_INPUT) {
            Intent intent = new Intent();
            intent.putExtra("fromId", chatMessage.getFromUserId());
            intent.setAction(com.ktw.bitbit.broadcast.OtherBroadcast.TYPE_INPUT);
            mService.sendBroadcast(intent);
            return;
        }

        // 某个用户发过来的撤回消息
        if (type == XmppMessage.TYPE_BACK) {
            backMessage(chatMessage);
            return;
        }

        // 对方领取了红包
        if (type == XmppMessage.TYPE_83) {
            String fromName;
            String toName;
            if (fromUserId.equals(mLoginUserId)) {// 理论上不存在
                fromName = FLYApplication.getContext().getString(R.string.you);
                toName = FLYApplication.getContext().getString(R.string.self);
            } else {
                fromName = chatMessage.getFromUserName();
                toName = FLYApplication.getContext().getString(R.string.you);
            }

            String hasBennReceived = "";
            if (chatMessage.getFileSize() == 1) {// 红包是否领完
                try {
                    String sRedSendTime = chatMessage.getFilePath();
                    long redSendTime = Long.parseLong(sRedSendTime);
                    long betweenTime = chatMessage.getTimeSend() / 1000 - redSendTime;
                    String sBetweenTime;
                    if (betweenTime < TimeUnit.MINUTES.toSeconds(1)) {
                        sBetweenTime = betweenTime + FLYApplication.getContext().getString(R.string.second);
                    } else if (betweenTime < TimeUnit.HOURS.toSeconds(1)) {
                        sBetweenTime = TimeUnit.SECONDS.toMinutes(betweenTime) + FLYApplication.getContext().getString(R.string.minute);
                    } else {
                        sBetweenTime = TimeUnit.SECONDS.toHours(betweenTime) + FLYApplication.getContext().getString(R.string.hour);
                    }
                    hasBennReceived = FLYApplication.getContext().getString(R.string.red_packet_has_received_place_holder, sBetweenTime);
                } catch (Exception e) {
                    hasBennReceived = FLYApplication.getContext().getString(R.string.red_packet_has_received);
                }
            }
            String str = FLYApplication.getContext().getString(R.string.tip_receive_red_packet_place_holder, fromName, toName) + hasBennReceived;

            // 针对红包领取的提示消息 需要做点击事件处理，将红包的type与id存入其他字段内
            chatMessage.setFileSize(XmppMessage.TYPE_83);
            chatMessage.setFilePath(chatMessage.getContent());

            chatMessage.setType(XmppMessage.TYPE_TIP);
            chatMessage.setContent(str);
            if (ChatMessageDao.getInstance().saveNewSingleChatMessage(mLoginUserId, fromUserId, chatMessage)) {
                ListenerManager.getInstance().notifyNewMesssage(mLoginUserId, fromUserId, chatMessage, false);
            }
            return;
        }

        // 红包退回通知，
        if (type == XmppMessage.TYPE_RED_BACK) {
            String str = FLYApplication.getContext().getString(R.string.tip_red_back);
            chatMessage.setType(XmppMessage.TYPE_TIP);
            chatMessage.setContent(str);
            if (!TextUtils.isEmpty(chatMessage.getObjectId())) {// 群组红包退回 通知到群组
                fromUserId = chatMessage.getObjectId();
                if (ChatMessageDao.getInstance().saveNewSingleChatMessage(mLoginUserId, fromUserId, chatMessage)) {
                    ListenerManager.getInstance().notifyNewMesssage(mLoginUserId, fromUserId, chatMessage, true);
                }
            } else {// 单聊红包退回
                if (ChatMessageDao.getInstance().saveNewSingleChatMessage(mLoginUserId, fromUserId, chatMessage)) {
                    ListenerManager.getInstance().notifyNewMesssage(mLoginUserId, fromUserId, chatMessage, false);
                }
            }
            return;
        }

        if (type == XmppMessage.TYPE_TRANSFER_RECEIVE) {
            String str;
            if (type == XmppMessage.TYPE_TRANSFER_RECEIVE) {
                // 更新数据库内转账消息为被领取状态
                List<ChatMessage> chatMessages = ChatMessageDao.getInstance().getAllSameObjectIdMessages(mLoginUserId, fromUserId, chatMessage.getContent());
                for (int i = 0; i < chatMessages.size(); i++) {
                    ChatMessageDao.getInstance().updateChatMessageReceiptStatus(mLoginUserId, fromUserId, chatMessages.get(i).getPacketId());
                }
                // 通知到聊天界面
                EventBus.getDefault().post(new EventTransfer(chatMessage.clone(false)));
                str = FLYApplication.getContext().getString(R.string.transfer_received);
            } else {
                str = FLYApplication.getContext().getString(R.string.transfer_backed);
            }

            chatMessage.setType(XmppMessage.TYPE_TIP);
            chatMessage.setContent(str);
            if (ChatMessageDao.getInstance().saveNewSingleChatMessage(mLoginUserId, fromUserId, chatMessage)) {
                ListenerManager.getInstance().notifyNewMesssage(mLoginUserId, fromUserId, chatMessage, false);
            }
            return;
        }

        // 转账、收付款消息
        if (type >= XmppMessage.TYPE_TRANSFER_BACK && type <= XmppMessage.TYPE_RECEIPT_GET) {
            if (type == XmppMessage.TYPE_PAYMENT_OUT) {// 通知到付款界面
                CodePay codePay = JSON.parseObject(chatMessage.getContent(), CodePay.class);
                EventBus.getDefault().post(new EventPaymentSuccess(codePay.getToUserName()));
            } else if (type == XmppMessage.TYPE_RECEIPT_GET) {// 通知到收款界面
                CodePay codePay = JSON.parseObject(chatMessage.getContent(), CodePay.class);
                EventBus.getDefault().post(new EventReceiptSuccess(codePay.getToUserName()));
            }
            if (ChatMessageDao.getInstance().saveNewSingleChatMessage(mLoginUserId, fromUserId, chatMessage)) {
                ListenerManager.getInstance().notifyNewMesssage(mLoginUserId, fromUserId, chatMessage, false);
            }
            return;
        }

        if (type == XmppMessage.TYPE_SCREENSHOT) {
            chatMessage.setType(XmppMessage.TYPE_TIP);
            chatMessage.setContent(mService.getString(R.string.tip_remote_screenshot));
            // 处理成tip后不return，正常流程处理，
        } else if (type == XmppMessage.TYPE_SYNC_CLEAN_CHAT_HISTORY) {
            Intent intent = new Intent();
            if (isNeedChangeMsgTableSave) {
                intent.putExtra(FLYAppConstant.EXTRA_USER_ID, chatMessage.getToUserId());
            } else {
                intent.putExtra(FLYAppConstant.EXTRA_USER_ID, chatMessage.getFromUserId());

/*
                chatMessage.setType(XmppMessage.TYPE_TIP);
                chatMessage.setFileSize(XmppMessage.TYPE_SYNC_CLEAN_CHAT_HISTORY);
                chatMessage.setContent(mService.getString(R.string.tip_remote_screenshot));
                if (ChatMessageDao.getInstance().saveNewSingleChatMessage(mLoginUserId, chatMessage.getFromUserId(), chatMessage)) {
                    ListenerManager.getInstance().notifyNewMesssage(mLoginUserId, chatMessage.getFromUserId(), chatMessage, false);
                }
*/
            }
            intent.setAction(com.ktw.bitbit.broadcast.OtherBroadcast.SYNC_CLEAN_CHAT_HISTORY);
            mService.sendBroadcast(intent);
            return;
        }

        // 存储消息
        if (chatMessage.isExpired()) {// 该条消息为过期消息(基本可以判断为离线消息)，不进行存库通知
            Log.e("msg", "该条消息为过期消息(基本可以判断为离线消息)，不进行存库通知");
            return;
        }

        // 戳一戳
        if (type == XmppMessage.TYPE_SHAKE) {
            Vibrator vibrator = (Vibrator) FLYApplication.getContext().getSystemService(VIBRATOR_SERVICE);
            long[] pattern = {100, 400, 100, 400};
            vibrator.vibrate(pattern, -1);
        }

        if (isNeedChangeMsgTableSave) {
            Log.e("msg", "转发消息且我为发送方");
            if (ChatMessageDao.getInstance().saveNewSingleChatMessage(mLoginUserId, chatMessage.getToUserId(), chatMessage)) {
                ListenerManager.getInstance().notifyNewMesssage(mLoginUserId, chatMessage.getFromUserId(), chatMessage, false);
            }
            return;
        }

        Friend friend = FriendDao.getInstance().getFriend(mLoginUserId, chatMessage.getFromUserId());
        if (friend != null) {
            Log.e("msg", "朋友发送过来的消息");
            if (friend.getStatus() != -1) {
                saveCurrentMessage(chatMessage);
                if (friend.getOfflineNoPushMsg() == 0) {// 未开启消息免打扰 可通知
                    if (!chatMessage.getFromUserId().equals(FLYApplication.IsRingId)) {// 收到该消息时不处于与发送方的聊天界面 && 非转发消息
                        Log.e("msg", "铃声通知");
/*
                        if (!MessageFragment.foreground) {
                            // 消息页面不响铃，
                            NoticeVoicePlayer.getInstance().start();
                        }
*/
                        // 与ios端统一吧
                        NoticeVoicePlayer.getInstance().start();
                    }
                } else {
                    Log.e("msg", "已针对该好友开启了消息免打扰，不通知");
                }
            }
        } else {
            Log.e("msg", "陌生人发过来的消息");
            FriendDao.getInstance().createNewFriend(chatMessage);
            saveCurrentMessage(chatMessage);
        }
    }

    private void backMessage(ChatMessage chatMessage) {
        // 本地数据库处理
        String packetId = chatMessage.getContent();
        if (TextUtils.isEmpty(packetId)) {
            return;
        }
        if (chatMessage.getFromUserId().equals(mLoginUserId)) {// 其他端撤回
            ChatMessageDao.getInstance().updateMessageBack(mLoginUserId, chatMessage.getToUserId(), packetId, FLYApplication.getContext().getString(R.string.you));
        } else {
            ChatMessageDao.getInstance().updateMessageBack(mLoginUserId, chatMessage.getFromUserId(), packetId, chatMessage.getFromUserName());
        }

        /** 更新聊天界面 */
        Intent intent = new Intent();
        intent.putExtra("packetId", packetId);
        intent.setAction(com.ktw.bitbit.broadcast.OtherBroadcast.MSG_BACK);
        mService.sendBroadcast(intent);

        // 更新UI界面
        if (chatMessage.getFromUserId().equals(mLoginUserId)) {
            ChatMessage chat = ChatMessageDao.getInstance().getLastChatMessage(mLoginUserId, chatMessage.getToUserId());
            if (chat.getPacketId().equals(packetId)) {
                // 要撤回的消息正是朋友表的最后一条消息
                FriendDao.getInstance().updateFriendContent(mLoginUserId, chatMessage.getToUserId(),
                        FLYApplication.getContext().getString(R.string.you) + " " + FLYApplication.getContext().getString(R.string.other_with_draw), XmppMessage.TYPE_TEXT, chat.getTimeSend());
                MsgBroadcast.broadcastMsgUiUpdate(FLYApplication.getInstance());
            }
        } else {
            ChatMessage chat = ChatMessageDao.getInstance().getLastChatMessage(mLoginUserId, chatMessage.getFromUserId());
            if (chat.getPacketId().equals(packetId)) {
                // 要撤回的消息正是朋友表的最后一条消息
                FriendDao.getInstance().updateFriendContent(mLoginUserId, chatMessage.getFromUserId(),
                        chatMessage.getFromUserName() + " " + FLYApplication.getContext().getString(R.string.other_with_draw), XmppMessage.TYPE_TEXT, chat.getTimeSend());
                MsgBroadcast.broadcastMsgUiUpdate(FLYApplication.getInstance());
            }
        }
    }

    private void saveCurrentMessage(ChatMessage chatMessage) {
        // 数据库保存message对象
        if (ChatMessageDao.getInstance().saveNewSingleChatMessage(mLoginUserId, chatMessage.getFromUserId(), chatMessage)) {
            // 成功后去刷新UI
            ListenerManager.getInstance().notifyNewMesssage(mLoginUserId, chatMessage.getFromUserId(), chatMessage, false);
        }
    }

    @SuppressLint("StringFormatMatches")
    private void chatGroupTipFromMe(String toUserName, ChatMessage chatMessage, Friend friend) {
        String toUserId = chatMessage.getToUserId();
        // 我针对其他人的操作，只需要为toUserName重新赋值
        String xT = getName(friend, toUserId);
        if (!TextUtils.isEmpty(xT)) {
            toUserName = xT;
        }

        chatMessage.setGroup(false);
        switch (chatMessage.getType()) {
            case XmppMessage.TYPE_CHANGE_NICK_NAME:
                // 我修改了群内昵称
                String content = chatMessage.getContent();
                if (!TextUtils.isEmpty(content)) {
                    friend.setRoomMyNickName(content);
                    FriendDao.getInstance().updateRoomMyNickName(friend.getUserId(), content);
                    ListenerManager.getInstance().notifyNickNameChanged(friend.getUserId(), mLoginUserId, content);
                    ChatMessageDao.getInstance().updateNickName(mLoginUserId, friend.getUserId(), mLoginUserId, content);

                    chatMessage.setContent(chatMessage.getFromUserName() + " " + FLYApplication.getContext().getString(R.string.message_object_update_nickname) + "‘" + content + "’");
                    chatMessage.setType(XmppMessage.TYPE_TIP);
                    if (ChatMessageDao.getInstance().saveNewSingleChatMessage(mLoginUserId, friend.getUserId(), chatMessage)) {
                        ListenerManager.getInstance().notifyNewMesssage(mLoginUserId, friend.getUserId(), chatMessage, true);
                    }
                }
                break;
            case XmppMessage.TYPE_CHANGE_ROOM_NAME:
                // 更新朋友表
                String groupName = chatMessage.getContent();
                FriendDao.getInstance().updateMucFriendRoomName(friend.getUserId(), groupName);
                ListenerManager.getInstance().notifyNickNameChanged(friend.getUserId(), "ROOMNAMECHANGE", groupName);

                chatMessage.setContent(chatMessage.getFromUserName() + " " + FLYApplication.getContext().getString(R.string.Message_Object_Update_RoomName) + groupName);
                chatMessage.setType(XmppMessage.TYPE_TIP);
                if (ChatMessageDao.getInstance().saveNewSingleChatMessage(mLoginUserId, friend.getUserId(), chatMessage)) {
                    ListenerManager.getInstance().notifyNewMesssage(mLoginUserId, friend.getUserId(), chatMessage, true);
                }
                break;
            case XmppMessage.TYPE_DELETE_ROOM:
                // 我解散了该群组
                mService.exitMucChat(chatMessage.getObjectId());
                FriendDao.getInstance().deleteFriend(mLoginUserId, chatMessage.getObjectId());
                // 消息表中删除
                ChatMessageDao.getInstance().deleteMessageTable(mLoginUserId, chatMessage.getObjectId());
                RoomMemberDao.getInstance().deleteRoomMemberTable(chatMessage.getObjectId());
                // 通知界面更新
                MsgBroadcast.broadcastMsgNumReset(mService);
                MsgBroadcast.broadcastMsgUiUpdate(FLYApplication.getInstance());
                MucgroupUpdateUtil.broadcastUpdateUi(mService);
                break;
            case XmppMessage.TYPE_DELETE_MEMBER:
                if (toUserId.equals(mLoginUserId)) {
                    // 我退出了该群组
                    mService.exitMucChat(chatMessage.getObjectId());
                    FriendDao.getInstance().deleteFriend(mLoginUserId, chatMessage.getObjectId());
                    // 消息表中删除
                    ChatMessageDao.getInstance().deleteMessageTable(mLoginUserId, chatMessage.getObjectId());
                    RoomMemberDao.getInstance().deleteRoomMemberTable(chatMessage.getObjectId());
                    // 通知界面更新
                    MsgBroadcast.broadcastMsgNumReset(mService);
                    MsgBroadcast.broadcastMsgUiUpdate(FLYApplication.getInstance());
                    MucgroupUpdateUtil.broadcastUpdateUi(mService);
                } else {
                    // toUserId被我踢出群组
                    chatMessage.setContent(toUserName + " " + FLYApplication.getContext().getString(R.string.kicked_out_group));
                    chatMessage.setType(XmppMessage.TYPE_TIP);
                    // chatMessage.setPacketId(UUID.randomUUID().toString().replaceAll("-", ""));
                    if (ChatMessageDao.getInstance().saveNewSingleChatMessage(mLoginUserId, friend.getUserId(), chatMessage)) {
                        ListenerManager.getInstance().notifyNewMesssage(mLoginUserId, friend.getUserId(), chatMessage, true);
                    }
                }
                break;
            case XmppMessage.TYPE_NEW_NOTICE:
                // 我发布了公告
                EventBus.getDefault().post(new EventNewNotice(chatMessage));
                String notice = chatMessage.getContent();
                chatMessage.setContent(chatMessage.getFromUserName() + " " + FLYApplication.getContext().getString(R.string.Message_Object_Add_NewAdv) + notice);
                chatMessage.setType(XmppMessage.TYPE_TIP);
                if (ChatMessageDao.getInstance().saveNewSingleChatMessage(mLoginUserId, friend.getUserId(), chatMessage)) {
                    ListenerManager.getInstance().notifyNewMesssage(mLoginUserId, friend.getUserId(), chatMessage, true);
                }
                break;
            case XmppMessage.TYPE_GAG:
                // 我对群组内其他成员禁言了
                long time = Long.parseLong(chatMessage.getContent());
                // 为防止其他用户接收不及时，给3s的误差
                if (time > (System.currentTimeMillis() / 1000) + 3) {
                    String formatTime = XfileUtils.fromatTime((time * 1000), "MM-dd HH:mm");
                    chatMessage.setContent(chatMessage.getFromUserName() + " " + FLYApplication.getContext().getString(R.string.message_object_yes) + toUserName +
                            FLYApplication.getContext().getString(R.string.Message_Object_Set_Gag_With_Time) + formatTime);
                } else {
                    chatMessage.setContent(FLYApplication.getContext().getString(R.string.be_cancel_ban_place_holder, toUserName, chatMessage.getFromUserName()));
                }

                chatMessage.setType(XmppMessage.TYPE_TIP);
                if (ChatMessageDao.getInstance().saveNewSingleChatMessage(mLoginUserId, friend.getUserId(), chatMessage)) {
                    ListenerManager.getInstance().notifyNewMesssage(mLoginUserId, friend.getUserId(), chatMessage, true);
                }
                break;
            case XmppMessage.NEW_MEMBER:
                String desc = chatMessage.getFromUserName() + " " + FLYApplication.getContext().getString(R.string.Message_Object_Group_Chat);
                if (!TextUtils.equals(toUserId, mLoginUserId)) {
                    /**
                     * 以下四种情况会进入该判断内
                     * 1.我在本端创建了该群组
                     * 2.我在本端加入该群组
                     * 3.我在其他端创建了该群组
                     * 4.我在其他端加入了该群组
                     * 5.面对面建群，调加入接口服务端代发了907
                     *
                     * 注：以上四种情况服务端都会通过smack推送Type==907的消息过来
                     */
                    Friend mFriend = FriendDao.getInstance().getFriend(mLoginUserId, chatMessage.getObjectId());
                    if (mFriend != null && mFriend.getGroupStatus() == 1) {// 本地存在该群组，且被踢出了该群组 先将该群组删除在创建(如调用updateGroupStatus直接修改该群组状态，可以会有问题，保险起见还是创建吧)
                        FriendDao.getInstance().deleteFriend(mLoginUserId, friend.getUserId());
                        ChatMessageDao.getInstance().deleteMessageTable(mLoginUserId, friend.getUserId());
                        mFriend = null;
                    }

                    // 将当前群组部分属性存入共享参数内
                    String roomId = chatMessage.getFilePath();

                    /**
                     * 本来只需要判断mFriend是否为空即可，但在第一、二种情况下，在调用接口之后(创建、加入接口)本地也在创建该群组，而当前收到Type==907后
                     * 又会去创建群组，造成群组重复的问题，所以需要坐下兼容
                     */
                    if (mFriend == null
                            && !TextUtils.equals(chatMessage.getObjectId(), FLYApplication.mRoomKeyLastCreate)) {
                        Friend mCreateFriend = new Friend();
                        mCreateFriend.setOwnerId(mLoginUserId);
                        mCreateFriend.setUserId(chatMessage.getObjectId());
                        mCreateFriend.setNickName(chatMessage.getContent());
                        mCreateFriend.setDescription("");
                        mCreateFriend.setRoomId(roomId);
                        mCreateFriend.setContent(desc);
                        mCreateFriend.setTimeSend(chatMessage.getTimeSend());
                        mCreateFriend.setRoomFlag(1);
                        mCreateFriend.setStatus(Friend.STATUS_FRIEND);
                        mCreateFriend.setGroupStatus(0);
                        FriendDao.getInstance().createOrUpdateFriend(mCreateFriend);

                        // 调用smack加入群组的方法
                        mService.joinMucChat(chatMessage.getObjectId(), 0);
                        MsgBroadcast.broadcastFaceGroupNotify(FLYApplication.getContext(), "join_room");
                    }
                } else {
                    // toUserId被我邀请进入群组
                    desc = chatMessage.getFromUserName() + " " + FLYApplication.getContext().getString(R.string.message_object_inter_friend) + toUserName;

                    String roomId = chatMessage.getFilePath();
                    operatingRoomMemberDao2(0, roomId, chatMessage.getToUserId(), toUserName, chatMessage.getFromUserId(), chatMessage.getFromUserName());
                }

                chatMessage.setType(XmppMessage.TYPE_TIP);
                chatMessage.setContent(desc);
                if (ChatMessageDao.getInstance().saveNewSingleChatMessage(mLoginUserId, chatMessage.getObjectId(), chatMessage)) {
                    ListenerManager.getInstance().notifyNewMesssage(mLoginUserId, chatMessage.getObjectId(), chatMessage, true);
                    MsgBroadcast.broadcastMsgRoomUpdateGetRoomStatus(FLYApplication.getInstance());
                }
                break;
            case XmppMessage.TYPE_SEND_MANAGER:
                // 我对该群组成员设置/取消管理员
                String messageType = chatMessage.getContent();
                if (messageType.equals("1")) {
                    chatMessage.setContent(chatMessage.getFromUserName() + " " + getContext().getString(R.string.setting) + toUserName + " " + getContext().getString(R.string.message_admin));
                } else {
                    chatMessage.setContent(chatMessage.getFromUserName() + " " + getContext().getString(R.string.sip_canceled) + toUserName + " " + getContext().getString(R.string.message_admin));
                }
                chatMessage.setType(XmppMessage.TYPE_TIP);
                chatMessage.setPacketId(UUID.randomUUID().toString().replaceAll("-", ""));
                if (ChatMessageDao.getInstance().saveNewSingleChatMessage(mLoginUserId, friend.getUserId(), chatMessage)) {
                    ListenerManager.getInstance().notifyNewMesssage(mLoginUserId, friend.getUserId(), chatMessage, true);
                }
                break;
            case XmppMessage.TYPE_UPDATE_ROLE:
                // 我对该群组成员设置/取消管理员
                int tipContent = -1;
                switch (chatMessage.getContent()) {
                    case "1": // 1:设置隐身人
                        tipContent = R.string.tip_set_invisible_place_holder;
                        break;
                    case "-1": // -1:取消隐身人
                        tipContent = R.string.tip_cancel_invisible_place_holder;
                        break;
                    case "2": // 2：设置监控人
                        tipContent = R.string.tip_set_guardian_place_holder;
                        break;
                    case "0": // 0：取消监控人
                        tipContent = R.string.tip_cancel_guardian_place_holder;
                        break;
                    default:
                        FLYReporter.unreachable();
                        return;
                }
                chatMessage.setContent(getContext().getString(tipContent, chatMessage.getFromUserName(), toUserName));
                chatMessage.setType(XmppMessage.TYPE_TIP);
                chatMessage.setPacketId(UUID.randomUUID().toString().replaceAll("-", ""));
                if (ChatMessageDao.getInstance().saveNewSingleChatMessage(mLoginUserId, friend.getUserId(), chatMessage)) {
                    ListenerManager.getInstance().notifyNewMesssage(mLoginUserId, friend.getUserId(), chatMessage, true);
                }
                break;

        }
    }

    /**
     * 因为多点登录问题，我在其他端做的群组操作，本端也收到了，需要做一样的处理
     * 所以在做处理前需要判断该群组操作是否为自己操作的(fromUserId==当前账号id)
     * 如果为自己操作了走到了另外一个方法，所以本方法内的对自己的操作代码可以注释掉
     * 现在先不管了，反正不会走到对自己的判断内，无影响
     */
    private void chatGroupTipForMe(String toUserName, ChatMessage chatMessage, Friend friend) {
        int type = chatMessage.getType();
        String fromUserId = chatMessage.getFromUserId();
        String fromUserName = chatMessage.getFromUserName();
        String toUserId = chatMessage.getToUserId();

        if (!TextUtils.isEmpty(toUserId)) {
            if (toUserId.equals(mLoginUserId)) {// 其他人针对我的操作，只需要为fromUserName赋值
                String xF = getName(friend, fromUserId);
                if (!TextUtils.isEmpty(xF)) {
                    fromUserName = xF;
                }
            } else {// 其他人针对其他人的操作，fromUserName与toUserName都需要赋值
                String xF = getName(friend, fromUserId);
                if (!TextUtils.isEmpty(xF)) {
                    fromUserName = xF;
                }
                String xT = getName(friend, toUserId);
                if (!TextUtils.isEmpty(xT)) {
                    toUserName = xT;
                }
            }
        }

        chatMessage.setGroup(false);
        if (type == XmppMessage.TYPE_CHANGE_NICK_NAME) {
            // 修改群内昵称
            String content = chatMessage.getContent();
            if (!TextUtils.isEmpty(toUserId) && toUserId.equals(mLoginUserId)) {
                // 我修改了昵称
                if (!TextUtils.isEmpty(content)) {
                    friend.setRoomMyNickName(content);
                    FriendDao.getInstance().updateRoomMyNickName(friend.getUserId(), content);
                    ListenerManager.getInstance().notifyNickNameChanged(friend.getUserId(), toUserId, content);
                    ChatMessageDao.getInstance().updateNickName(mLoginUserId, friend.getUserId(), toUserId, content);
                }
            } else {
                // 其他人修改了昵称，通知下就可以了
                chatMessage.setContent(fromUserName + " " + FLYApplication.getContext().getString(R.string.message_object_update_nickname) + "‘" + content + "’");
                chatMessage.setType(XmppMessage.TYPE_TIP);
                if (ChatMessageDao.getInstance().saveNewSingleChatMessage(mLoginUserId, friend.getUserId(), chatMessage)) {
                    ListenerManager.getInstance().notifyNewMesssage(mLoginUserId, friend.getUserId(), chatMessage, true);
                }
                ListenerManager.getInstance().notifyNickNameChanged(friend.getUserId(), toUserId, content);
                ChatMessageDao.getInstance().updateNickName(mLoginUserId, friend.getUserId(), toUserId, content);
            }
        } else if (type == XmppMessage.TYPE_CHANGE_ROOM_NAME) {
            // 修改房间名
            // 更新朋友表
            String content = chatMessage.getContent();
            FriendDao.getInstance().updateMucFriendRoomName(friend.getUserId(), content);
            ListenerManager.getInstance().notifyNickNameChanged(friend.getUserId(), "ROOMNAMECHANGE", content);

            chatMessage.setContent(fromUserName + " " + FLYApplication.getContext().getString(R.string.Message_Object_Update_RoomName) + content);
            chatMessage.setType(XmppMessage.TYPE_TIP);
            if (ChatMessageDao.getInstance().saveNewSingleChatMessage(mLoginUserId, friend.getUserId(), chatMessage)) {
                ListenerManager.getInstance().notifyNewMesssage(mLoginUserId, friend.getUserId(), chatMessage, true);
            }
        } else if (type == XmppMessage.TYPE_DELETE_ROOM) {
            // 群主解散该群
            if (fromUserId.equals(toUserId)) {
                // 我为群主
                FriendDao.getInstance().deleteFriend(mLoginUserId, chatMessage.getObjectId());
                // 消息表中删除
                ChatMessageDao.getInstance().deleteMessageTable(mLoginUserId, chatMessage.getObjectId());
                RoomMemberDao.getInstance().deleteRoomMemberTable(chatMessage.getObjectId());
                // 通知界面更新
                MsgBroadcast.broadcastMsgNumReset(mService);
                MsgBroadcast.broadcastMsgUiUpdate(FLYApplication.getInstance());
                MucgroupUpdateUtil.broadcastUpdateUi(mService);
            } else {
                mService.exitMucChat(chatMessage.getObjectId());
                // 2 标志该群已被解散  更新朋友表
                FriendDao.getInstance().updateFriendGroupStatus(mLoginUserId, friend.getUserId(), 2);
                chatMessage.setType(XmppMessage.TYPE_TIP);
                chatMessage.setContent(FLYApplication.getContext().getString(R.string.tip_disbanded));
                if (ChatMessageDao.getInstance().saveNewSingleChatMessage(mLoginUserId, chatMessage.getObjectId(), chatMessage)) {
                    ListenerManager.getInstance().notifyNewMesssage(mLoginUserId, chatMessage.getObjectId(), chatMessage, true);
                }
            }
            ListenerManager.getInstance().notifyDeleteMucRoom(chatMessage.getObjectId());
        } else if (type == XmppMessage.TYPE_DELETE_MEMBER) {
            // 群组 退出 || 踢人
            chatMessage.setType(XmppMessage.TYPE_TIP);
            // 退出 || 被踢出群组
            if (toUserId.equals(mLoginUserId)) { // 该操作为针对我的
                if (fromUserId.equals(toUserId)) {
                    // 自己退出了群组
                    mService.exitMucChat(friend.getUserId());
                    // 删除这个房间
                    FriendDao.getInstance().deleteFriend(mLoginUserId, friend.getUserId());
                    RoomMemberDao.getInstance().deleteRoomMemberTable(chatMessage.getObjectId());
                    // 消息表中删除
                    ChatMessageDao.getInstance().deleteMessageTable(mLoginUserId, friend.getUserId());
                    // 通知界面更新
                    MsgBroadcast.broadcastMsgNumReset(mService);
                    MsgBroadcast.broadcastMsgUiUpdate(FLYApplication.getInstance());
                    MucgroupUpdateUtil.broadcastUpdateUi(mService);
                } else {
                    // 被xx踢出了群组
                    mService.exitMucChat(friend.getUserId());
                    // / 1 标志被踢出该群组， 更新朋友表
                    FriendDao.getInstance().updateFriendGroupStatus(mLoginUserId, friend.getUserId(), 1);
                    chatMessage.setContent(FLYApplication.getContext().getString(R.string.tip_been_kick_place_holder, fromUserName));

                    ListenerManager.getInstance().notifyMyBeDelete(friend.getUserId());// 通知群组聊天界面
                }
            } else {
                // 其他人退出 || 被踢出
                if (fromUserId.equals(toUserId)) {
                    chatMessage.setContent(toUserName + " " + FLYApplication.getContext().getString(R.string.quit_group));
                } else {
                    chatMessage.setContent(toUserName + " " + FLYApplication.getContext().getString(R.string.kicked_out_group));
                }
                // 更新RoomMemberDao、更新群聊界面
                operatingRoomMemberDao(1, friend.getRoomId(), toUserId, null);
                MsgBroadcast.broadcastMsgRoomUpdateGetRoomStatus(FLYApplication.getContext());
            }

            if (ChatMessageDao.getInstance().saveNewSingleChatMessage(mLoginUserId, friend.getUserId(), chatMessage)) {
                ListenerManager.getInstance().notifyNewMesssage(mLoginUserId, friend.getUserId(), chatMessage, true);
            }
        } else if (type == XmppMessage.TYPE_NEW_NOTICE) {
            // 发布公告
            EventBus.getDefault().post(new EventNewNotice(chatMessage));
            String content = chatMessage.getContent();
            chatMessage.setContent(fromUserName + " " + FLYApplication.getContext().getString(R.string.Message_Object_Add_NewAdv) + content);
            chatMessage.setType(XmppMessage.TYPE_TIP);
            if (ChatMessageDao.getInstance().saveNewSingleChatMessage(mLoginUserId, friend.getUserId(), chatMessage)) {
                ListenerManager.getInstance().notifyNewMesssage(mLoginUserId, friend.getUserId(), chatMessage, true);
            }
        } else if (type == XmppMessage.TYPE_GAG) {// 群组与直播间禁言
            long time = Long.parseLong(chatMessage.getContent());
            if (toUserId != null && toUserId.equals(mLoginUserId)) {
                // 被禁言了|| 取消禁言 更新RoomTalkTime字段
                FriendDao.getInstance().updateRoomTalkTime(mLoginUserId, friend.getUserId(), (int) time);
                ListenerManager.getInstance().notifyMyVoiceBanned(friend.getUserId(), (int) time);
            }

            // 为防止其他用户接收不及时，给3s的误差
            if (time > (System.currentTimeMillis() / 1000) + 3) {
                String formatTime = XfileUtils.fromatTime((time * 1000), "MM-dd HH:mm");
                chatMessage.setContent(fromUserName + " " + FLYApplication.getContext().getString(R.string.message_object_yes) + toUserName +
                        FLYApplication.getContext().getString(R.string.Message_Object_Set_Gag_With_Time) + formatTime);
            } else {
                chatMessage.setContent(toUserName + FLYApplication.getContext().getString(R.string.tip_been_cancel_ban_place_holder, fromUserName));
            }

            chatMessage.setType(XmppMessage.TYPE_TIP);
            if (ChatMessageDao.getInstance().saveNewSingleChatMessage(mLoginUserId, friend.getUserId(), chatMessage)) {
                ListenerManager.getInstance().notifyNewMesssage(mLoginUserId, friend.getUserId(), chatMessage, true);
            }
        } else if (type == XmppMessage.NEW_MEMBER) {
            String desc = "";
            if (chatMessage.getFromUserId().equals(toUserId)) {
                // 主动加入
                desc = fromUserName + " " + FLYApplication.getContext().getString(R.string.Message_Object_Group_Chat);
            } else {
                // 被邀请加入
                desc = fromUserName + " " + FLYApplication.getContext().getString(R.string.message_object_inter_friend) + toUserName;

                String roomId = chatMessage.getFilePath();
                if (!toUserId.equals(mLoginUserId)) {// 被邀请人为自己时不能更新RoomMemberDao，如更新了，在群聊界面判断出该表有人而不会在去调用接口获取该群真实的人数了
                    operatingRoomMemberDao2(0, roomId, chatMessage.getToUserId(), toUserName, chatMessage.getFromUserId(), chatMessage.getFromUserName());
                }
            }

            if (toUserId.equals(mLoginUserId)) {
                // 其他人邀请我加入该群组 才会进入该方法
                if (friend != null && friend.getGroupStatus() == 1) {// 本地存在该群组，且被踢出了该群组 先将该群组删除在创建(如调用updateGroupStatus直接修改该群组状态，可以会有问题，保险起见还是创建吧)
                    FriendDao.getInstance().deleteFriend(mLoginUserId, friend.getUserId());
                    ChatMessageDao.getInstance().deleteMessageTable(mLoginUserId, friend.getUserId());
                }

                String roomId = chatMessage.getFilePath();

                Friend mCreateFriend = new Friend();
                mCreateFriend.setOwnerId(mLoginUserId);
                mCreateFriend.setUserId(chatMessage.getObjectId());
                mCreateFriend.setNickName(chatMessage.getContent());
                mCreateFriend.setDescription("");
                mCreateFriend.setRoomId(roomId);
                mCreateFriend.setContent(desc);
                mCreateFriend.setTimeSend(chatMessage.getTimeSend());
                mCreateFriend.setRoomFlag(1);
                mCreateFriend.setStatus(Friend.STATUS_FRIEND);
                mCreateFriend.setGroupStatus(0);
                FriendDao.getInstance().createOrUpdateFriend(mCreateFriend);
                // 调用smack加入群组的方法
                // 被邀请加入群组，lastSeconds == 当前时间 - 被邀请时的时间 + 3000[容错]
                mService.joinMucChat(chatMessage.getObjectId(),
                        TimeUtils.sk_time_current_time() - chatMessage.getTimeSend() + 3000);
            }

            // 更新数据库
            chatMessage.setType(XmppMessage.TYPE_TIP);
            chatMessage.setContent(desc);
            if (ChatMessageDao.getInstance().saveNewSingleChatMessage(mLoginUserId, chatMessage.getObjectId(), chatMessage)) {
                ListenerManager.getInstance().notifyNewMesssage(mLoginUserId, chatMessage.getObjectId(), chatMessage, true);
                MsgBroadcast.broadcastMsgRoomUpdate(FLYApplication.getInstance());
                MsgBroadcast.broadcastMsgRoomUpdateGetRoomStatus(FLYApplication.getContext());
            }
        } else if (type == XmppMessage.TYPE_SEND_MANAGER) {
            String content = chatMessage.getContent();
            int role;
            if (content.equals("1")) {
                role = 2;
                chatMessage.setContent(fromUserName + " " + getContext().getString(R.string.setting) + toUserName + " " + getContext().getString(R.string.message_admin));
            } else {
                role = 3;
                chatMessage.setContent(fromUserName + " " + getContext().getString(R.string.sip_canceled) + toUserName + " " + getContext().getString(R.string.message_admin));
            }

            RoomMemberDao.getInstance().updateRoomMemberRole(friend.getRoomId(), toUserId, role);

            chatMessage.setType(XmppMessage.TYPE_TIP);
            // chatMessage.setPacketId(UUID.randomUUID().toString().replaceAll("-", ""));
            if (ChatMessageDao.getInstance().saveNewSingleChatMessage(mLoginUserId, friend.getUserId(), chatMessage)) {
                ListenerManager.getInstance().notifyNewMesssage(mLoginUserId, friend.getUserId(), chatMessage, true);
                Intent intent = new Intent();
                intent.putExtra("roomId", friend.getUserId());
                intent.putExtra("toUserId", chatMessage.getToUserId());
                intent.putExtra("isSet", content.equals("1"));
                intent.setAction(com.ktw.bitbit.broadcast.OtherBroadcast.REFRESH_MANAGER);
                mService.sendBroadcast(intent);
            }
        } else if (type == XmppMessage.TYPE_UPDATE_ROLE) {
            int tipContent = -1;
            int role = RoomMember.ROLE_MEMBER;
            switch (chatMessage.getContent()) {
                case "1": // 1:设置隐身人
                    tipContent = R.string.tip_set_invisible_place_holder;
                    role = RoomMember.ROLE_INVISIBLE;
                    break;
                case "-1": // -1:取消隐身人
                    tipContent = R.string.tip_cancel_invisible_place_holder;
                    break;
                case "2": // 2：设置监控人
                    tipContent = R.string.tip_set_guardian_place_holder;
                    role = RoomMember.ROLE_GUARDIAN;
                    break;
                case "0": // 0：取消监控人
                    tipContent = R.string.tip_cancel_guardian_place_holder;
                    break;
                default:
                    FLYReporter.unreachable();
                    return;
            }
            chatMessage.setContent(getContext().getString(tipContent, chatMessage.getFromUserName(), toUserName));

            RoomMemberDao.getInstance().updateRoomMemberRole(friend.getRoomId(), toUserId, role);

            chatMessage.setType(XmppMessage.TYPE_TIP);
            // chatMessage.setPacketId(UUID.randomUUID().toString().replaceAll("-", ""));
            if (ChatMessageDao.getInstance().saveNewSingleChatMessage(mLoginUserId, friend.getUserId(), chatMessage)) {
                ListenerManager.getInstance().notifyNewMesssage(mLoginUserId, friend.getUserId(), chatMessage, true);
                MsgBroadcast.broadcastMsgRoleChanged(getContext());
            }
        }

        // 某个用户删除了群文件 或者 上传了群文件
        if (type == XmppMessage.TYPE_MUCFILE_DEL || type == XmppMessage.TYPE_MUCFILE_ADD) {
            String roomid = chatMessage.getObjectId();
            String str;
            if (type == XmppMessage.TYPE_MUCFILE_DEL) {
                // str = chatMessage.getFromUserName() + " 删除了群文件 " + chatMessage.getFilePath();
                str = fromUserName + " " + FLYApplication.getContext().getString(R.string.message_file_delete) + ":" + chatMessage.getFilePath();
            } else {
                // str = chatMessage.getFromUserName() + " 上传了群文件 " + chatMessage.getFilePath();
                str = fromUserName + " " + FLYApplication.getContext().getString(R.string.message_file_upload) + ":" + chatMessage.getFilePath();
            }
            // 更新朋友表最后一条消息
            FriendDao.getInstance().updateFriendContent(mLoginUserId, roomid, str, type, TimeUtils.sk_time_current_time());
            FriendDao.getInstance().markUserMessageUnRead(mLoginUserId, roomid); // 加一个小红点
            // 更新聊天记录表最后一条消息
            chatMessage.setContent(str);
            chatMessage.setType(XmppMessage.TYPE_TIP);
            if (ChatMessageDao.getInstance().saveNewSingleChatMessage(mLoginUserId, roomid, chatMessage)) {
                ListenerManager.getInstance().notifyNewMesssage(mLoginUserId, roomid, chatMessage, true);
            }
            // 更新消息界面
            MsgBroadcast.broadcastMsgUiUpdate(FLYApplication.getInstance());
            return;
        }
    }

    // 更新群成员表
    private void operatingRoomMemberDao(int type, String roomId, String userId, String userName) {
        if (type == 0) {
            RoomMember roomMember = new RoomMember();
            roomMember.setRoomId(roomId);
            roomMember.setUserId(userId);
            roomMember.setUserName(userName);
            roomMember.setCardName(userName);
            roomMember.setRole(3);
            roomMember.setCreateTime(0);
            RoomMemberDao.getInstance().saveSingleRoomMember(roomId, roomMember);
        } else {
            RoomMemberDao.getInstance().deleteRoomMember(roomId, userId);
        }
    }

    // 更新群成员表
    private void operatingRoomMemberDao2(int type, String roomId, String userId, String userName, String inviterId, String inviterName) {
        if (type == 0) {
            RoomMember roomMember = new RoomMember();
            roomMember.setRoomId(roomId);
            roomMember.setUserId(userId);
            roomMember.setUserName(userName);
            roomMember.setCardName(userName);
            roomMember.setRole(3);
            roomMember.setCreateTime(0);
            roomMember.setInviterId(inviterId);
            roomMember.setInviterName(inviterName);
            RoomMemberDao.getInstance().saveSingleRoomMember(roomId, roomMember);
        } else {
            RoomMemberDao.getInstance().deleteRoomMember(roomId, userId);
        }
    }

    @SuppressLint("StringFormatMatches")
    private void chatGroupTip2(int type, ChatMessage chatMessage, String toUserName) {
        chatMessage.setType(XmppMessage.TYPE_TIP);
        if (type == XmppMessage.TYPE_GROUP_VERIFY) {
            // 916协议分为两种
            // 第一种为服务端发送，触发条件为群主在群组信息内 开/关 进群验证按钮，群组内每个人都能收到
            // 第二种为邀请、申请加入该群组，由邀请人或加入方发送给群主的消息，只有群主可以收到
            if (!TextUtils.isEmpty(chatMessage.getContent()) &&
                    (chatMessage.getContent().equals("0") || chatMessage.getContent().equals("1"))) {// 第一种
                if (chatMessage.getContent().equals("1")) {
                    chatMessage.setContent(FLYApplication.getContext().getString(R.string.tip_group_enable_verify));
                } else {
                    chatMessage.setContent(mService.getString(R.string.tip_group_disable_verify));
                }
                // chatMessage.setPacketId(UUID.randomUUID().toString().replaceAll("-", ""));
                if (ChatMessageDao.getInstance().saveNewSingleChatMessage(mLoginUserId, chatMessage.getObjectId(), chatMessage)) {
                    ListenerManager.getInstance().notifyNewMesssage(mLoginUserId, chatMessage.getObjectId(), chatMessage, true);
                }
            } else {//  群聊邀请确认消息 我收到该条消息 说明我就是该群的群主 待我审核
                try {
                    org.json.JSONObject json = new org.json.JSONObject(chatMessage.getObjectId());
                    String isInvite = json.getString("isInvite");
                    if (TextUtils.isEmpty(isInvite)) {
                        isInvite = "0";
                    }
                    if (isInvite.equals("0")) {
                        String id = json.getString("userIds");
                        String[] ids = id.split(",");
                        chatMessage.setContent(FLYApplication.getContext().getString(R.string.tip_invite_need_verify_place_holder, chatMessage.getFromUserName(), ids.length));
                    } else {
                        chatMessage.setContent(chatMessage.getFromUserName() + FLYApplication.getContext().getString(R.string.tip_need_verify_place_holder));
                    }
                    String roomJid = json.getString("roomJid");
                    if (ChatMessageDao.getInstance().saveNewSingleChatMessage(mLoginUserId, roomJid, chatMessage)) {
                        ListenerManager.getInstance().notifyNewMesssage(mLoginUserId, roomJid, chatMessage, true);
                    }
                } catch (JSONException e) {
                    e.printStackTrace();
                }
            }
        } else {
            if (type == XmppMessage.TYPE_CHANGE_SHOW_READ) {
                PreferenceUtils.putBoolean(FLYApplication.getContext(),
                        Constants.IS_SHOW_READ + chatMessage.getObjectId(), chatMessage.getContent().equals("1"));
                if (chatMessage.getContent().equals("1")) {
                    chatMessage.setContent(FLYApplication.getContext().getString(R.string.tip_owner_enable_read));
                } else {
                    chatMessage.setContent(FLYApplication.getContext().getString(R.string.tip_owner_disable_read));
                }
            } else if (type == XmppMessage.TYPE_GROUP_LOOK) {
                if (chatMessage.getContent().equals("1")) {
                    chatMessage.setContent(FLYApplication.getContext().getString(R.string.tip_owner_private));
                } else {
                    chatMessage.setContent(FLYApplication.getContext().getString(R.string.tip_owner_public));
                }
            } else if (type == XmppMessage.TYPE_GROUP_SHOW_MEMBER) {
                PreferenceUtils.putBoolean(FLYApplication.getContext(),
                        Constants.IS_SHOW_MEMBER + chatMessage.getObjectId(), chatMessage.getContent().equals("1"));
                if (chatMessage.getContent().equals("1")) {
                    chatMessage.setContent(FLYApplication.getContext().getString(R.string.tip_owner_enable_member));
                } else {
                    chatMessage.setContent(FLYApplication.getContext().getString(R.string.tip_owner_disable_member));
                }
            } else if (type == XmppMessage.TYPE_GROUP_SEND_CARD) {
                PreferenceUtils.putBoolean(FLYApplication.getContext(),
                        Constants.IS_SEND_CARD + chatMessage.getObjectId(), chatMessage.getContent().equals("1"));
                if (chatMessage.getContent().equals("1")) {
                    chatMessage.setContent(FLYApplication.getContext().getString(R.string.tip_owner_enable_chat_privately));
                } else {
                    chatMessage.setContent(FLYApplication.getContext().getString(R.string.tip_owner_disable_chat_privately));
                }
            } else if (type == XmppMessage.TYPE_GROUP_ALL_SHAT_UP) {
                PreferenceUtils.putBoolean(FLYApplication.getContext(),
                        Constants.GROUP_ALL_SHUP_UP + chatMessage.getObjectId(), !chatMessage.getContent().equals("0"));
                if (!chatMessage.getContent().equals("0")) {
                    chatMessage.setContent(FLYApplication.getContext().getString(R.string.tip_now_ban_all));
                } else {
                    chatMessage.setContent(FLYApplication.getContext().getString(R.string.tip_now_disable_ban_all));
                }
                MsgBroadcast.broadcastMsgRoomUpdateGetRoomStatus(FLYApplication.getContext());
            } else if (type == XmppMessage.TYPE_GROUP_ALLOW_NORMAL_INVITE) {
                if (!chatMessage.getContent().equals("0")) {
                    chatMessage.setContent(FLYApplication.getContext().getString(R.string.tip_owner_enable_invite));
                } else {
                    chatMessage.setContent(FLYApplication.getContext().getString(R.string.tip_owner_disable_invite));
                }
            } else if (type == XmppMessage.TYPE_GROUP_ALLOW_NORMAL_UPLOAD) {
                if (!chatMessage.getContent().equals("0")) {
                    chatMessage.setContent(FLYApplication.getContext().getString(R.string.tip_owner_enable_upload));
                } else {
                    chatMessage.setContent(FLYApplication.getContext().getString(R.string.tip_owner_disable_upload));
                }
            } else if (type == XmppMessage.TYPE_GROUP_ALLOW_NORMAL_CONFERENCE) {
                PreferenceUtils.putBoolean(FLYApplication.getContext(),
                        Constants.IS_ALLOW_NORMAL_CONFERENCE + chatMessage.getObjectId(), !chatMessage.getContent().equals("0"));
                if (!chatMessage.getContent().equals("0")) {
                    chatMessage.setContent(FLYApplication.getContext().getString(R.string.tip_owner_enable_meeting));
                } else {
                    chatMessage.setContent(FLYApplication.getContext().getString(R.string.tip_owner_disable_meeting));
                }
            } else if (type == XmppMessage.TYPE_GROUP_ALLOW_NORMAL_SEND_COURSE) {
                PreferenceUtils.putBoolean(FLYApplication.getContext(),
                        Constants.IS_ALLOW_NORMAL_SEND_COURSE + chatMessage.getObjectId(), !chatMessage.getContent().equals("0"));
                if (!chatMessage.getContent().equals("0")) {
                    chatMessage.setContent(FLYApplication.getContext().getString(R.string.tip_owner_enable_cource));
                } else {
                    chatMessage.setContent(FLYApplication.getContext().getString(R.string.tip_owner_disable_cource));
                }
            } else if (type == XmppMessage.TYPE_GROUP_TRANSFER) {
                chatMessage.setContent(FLYApplication.getContext().getString(R.string.tip_new_group_owner_place_holder, toUserName));
                Friend friend = FriendDao.getInstance().getFriend(mLoginUserId, chatMessage.getObjectId());
                if (friend != null) {
                    FriendDao.getInstance().updateRoomCreateUserId(mLoginUserId,
                            chatMessage.getObjectId(), chatMessage.getToUserId());
                    RoomMemberDao.getInstance().updateRoomMemberRole(friend.getRoomId(), chatMessage.getToUserId(), 1);
                }
            }
            if (ChatMessageDao.getInstance().saveNewSingleChatMessage(mLoginUserId, chatMessage.getObjectId(), chatMessage)) {
                ListenerManager.getInstance().notifyNewMesssage(mLoginUserId, chatMessage.getObjectId(), chatMessage, true);
            }
        }
    }

    /**
     * 朋友相关消息逻辑处理
     */
    private void chatFriend(ChatMessage chatMessage) {
        /**
         * 改变
         * 1.因为 新的朋友从消息页面移至通讯录，不需要显示最后一条消息，updateLastChatMessage(...,Friend.ID_NEW_FRIEND_MESSAGE,...)可以注释掉
         * 2.因为多点登录，ex:我在web端删除了某个好友，android端接收到了，需要另外做处理
         */
        Log.e("msg", mLoginUserId + "，" + chatMessage.getFromUserId() + "，" + chatMessage.getToUserId());
        Log.e("msg", chatMessage.getType() + "，" + chatMessage.getPacketId());

        if (chatMessage.getFromUserId().equals(mLoginUserId)) {// 我在其他端做的操作，在android也接收到了
            chatFriendFromMe(chatMessage);
        } else {
            chatFriendForMe(chatMessage);
        }
    }

    /**
     * 自己在其他端做的好友操作，发送过来的新朋友消息，需要另做处理
     * 处理逻辑与发送该条Type消息时所做的操作一样，可将代码复制过来
     */
    private void chatFriendFromMe(ChatMessage chatMessage) {
        String toUserId = chatMessage.getToUserId();
        String toUserName = chatMessage.getToUserName();
        if (TextUtils.isEmpty(toUserName)) {
            toUserName = "NULL";
        }
        switch (chatMessage.getType()) {
            case XmppMessage.TYPE_SAYHELLO:
                // 我与对方打招呼
                NewFriendMessage message = NewFriendMessage.createLocalMessage(CoreManager.requireSelf(mService),
                        XmppMessage.TYPE_SAYHELLO, getContext().getString(R.string.say_hello_default), toUserId, toUserName);
                NewFriendDao.getInstance().createOrUpdateNewFriend(message);
                NewFriendDao.getInstance().changeNewFriendState(toUserId, Friend.STATUS_10);//朋友状态
                ListenerManager.getInstance().notifyNewFriend(mLoginUserId, message, true);

                // 发送打招呼的消息
                ChatMessage sayMessage = new ChatMessage();
                sayMessage.setFromUserId(mLoginUserId);
                sayMessage.setFromUserName(CoreManager.requireSelf(mService).getNickName());
                sayMessage.setContent(getContext().getString(R.string.say_hello_default));
                sayMessage.setType(XmppMessage.TYPE_TEXT); //文本类型
                sayMessage.setMySend(true);
                sayMessage.setSendRead(true);// 新的朋友消息默认为已读
                sayMessage.setMessageState(ChatMessageListener.MESSAGE_SEND_SUCCESS);
                sayMessage.setPacketId(UUID.randomUUID().toString().replaceAll("-", ""));
                sayMessage.setTimeSend(TimeUtils.sk_time_current_time());
                ChatMessageDao.getInstance().saveNewSingleChatMessage(message.getOwnerId(), message.getUserId(), sayMessage);
                break;
            case XmppMessage.TYPE_PASS:
                // 我同意了对方的加好友请求
                NewFriendMessage passMessage = NewFriendMessage.createLocalMessage(CoreManager.requireSelf(mService),
                        XmppMessage.TYPE_PASS, null, toUserId, toUserName);

                NewFriendDao.getInstance().ascensionNewFriend(passMessage, Friend.STATUS_FRIEND);
                FriendHelper.addFriendExtraOperation(mLoginUserId, toUserId);
                FriendDao.getInstance().updateFriendContent(mLoginUserId, toUserId,
                        getContext().getString(R.string.be_friendand_chat), XmppMessage.TYPE_TEXT, TimeUtils.sk_time_current_time());
                NewFriendDao.getInstance().changeNewFriendState(toUserId, Friend.STATUS_12);
                ListenerManager.getInstance().notifyNewFriend(mLoginUserId, passMessage, true);
                break;
            case XmppMessage.TYPE_FEEDBACK:
                // 我发送给对方的回话
                NewFriendMessage feedBackMessage = NewFriendDao.getInstance().getNewFriendById(mLoginUserId, toUserId);
                if (feedBackMessage == null) {
                    feedBackMessage = NewFriendMessage.createLocalMessage(CoreManager.requireSelf(mService),
                            XmppMessage.TYPE_FEEDBACK, chatMessage.getContent(), toUserId, toUserName);
                    NewFriendDao.getInstance().createOrUpdateNewFriend(feedBackMessage);
                }
                if (feedBackMessage.getState() == Friend.STATUS_11 || feedBackMessage.getState() == Friend.STATUS_15) {
                    NewFriendDao.getInstance().changeNewFriendState(feedBackMessage.getUserId(), Friend.STATUS_15);
                } else {
                    NewFriendDao.getInstance().changeNewFriendState(feedBackMessage.getUserId(), Friend.STATUS_14);
                }
                NewFriendDao.getInstance().updateNewFriendContent(feedBackMessage.getUserId(), chatMessage.getContent());

                ChatMessage chatFeedMessage = new ChatMessage();// 本地也保存一份
                chatFeedMessage.setType(XmppMessage.TYPE_TEXT); // 文本类型
                chatFeedMessage.setFromUserId(mLoginUserId);
                chatFeedMessage.setFromUserName(CoreManager.requireSelf(mService).getNickName());
                chatFeedMessage.setContent(chatMessage.getContent());
                chatFeedMessage.setMySend(true);
                chatFeedMessage.setMessageState(ChatMessageListener.MESSAGE_SEND_SUCCESS);
                chatFeedMessage.setPacketId(UUID.randomUUID().toString().replaceAll("-", ""));
                chatFeedMessage.setTimeSend(TimeUtils.sk_time_current_time());
                ChatMessageDao.getInstance().saveNewSingleAnswerMessage(mLoginUserId, toUserId, chatFeedMessage);
                ListenerManager.getInstance().notifyNewFriend(mLoginUserId, feedBackMessage, true);
                break;
            case XmppMessage.TYPE_FRIEND:
                // 对方未开启验证，我直接将对方添加为好友
                NewFriendMessage friendMessage = NewFriendMessage.createLocalMessage(CoreManager.requireSelf(mService),
                        XmppMessage.TYPE_FRIEND, null, toUserId, toUserName);
                NewFriendDao.getInstance().ascensionNewFriend(friendMessage, Friend.STATUS_FRIEND);
                FriendHelper.addFriendExtraOperation(mLoginUserId, toUserId);
                NewFriendDao.getInstance().changeNewFriendState(toUserId, Friend.STATUS_22);
                FriendDao.getInstance().updateFriendContent(mLoginUserId, toUserId,
                        getContext().getString(R.string.Msg_View_Controller_Start_Chat), XmppMessage.TYPE_TEXT, TimeUtils.sk_time_current_time());
                ListenerManager.getInstance().notifyNewFriend(mLoginUserId, friendMessage, true);
                break;
            case XmppMessage.TYPE_BLACK:
                // 我将对方拉黑
                NewFriendMessage blackMessage = NewFriendMessage.createLocalMessage(CoreManager.requireSelf(mService),
                        XmppMessage.TYPE_BLACK, null, toUserId, toUserName);
                FriendDao.getInstance().updateFriendStatus(mLoginUserId, toUserId, Friend.STATUS_BLACKLIST);
                FriendHelper.addBlacklistExtraOperation(blackMessage.getOwnerId(), blackMessage.getUserId());
                NewFriendDao.getInstance().createOrUpdateNewFriend(blackMessage);
                NewFriendDao.getInstance().changeNewFriendState(toUserId, Friend.STATUS_18);
                ListenerManager.getInstance().notifyNewFriend(mLoginUserId, blackMessage, true);
                // 通知聊天界面刷新
                EventBus.getDefault().post(new EventSyncFriendOperating(chatMessage.getToUserId(), chatMessage.getType()));
                break;
            case XmppMessage.TYPE_REFUSED:
                // 我将对方移除黑名单
                NewFriendMessage removeMessage = NewFriendMessage.createLocalMessage(CoreManager.requireSelf(mService),
                        XmppMessage.TYPE_REFUSED, null, toUserId, toUserName);
                NewFriendDao.getInstance().ascensionNewFriend(removeMessage, Friend.STATUS_FRIEND);
                FriendHelper.beAddFriendExtraOperation(removeMessage.getOwnerId(), removeMessage.getUserId());
                NewFriendDao.getInstance().createOrUpdateNewFriend(removeMessage);
                NewFriendDao.getInstance().changeNewFriendState(toUserId, Friend.STATUS_24);
                ListenerManager.getInstance().notifyNewFriend(mLoginUserId, removeMessage, true);
                break;
            case XmppMessage.TYPE_DELALL:
                // 我删除了对方
                NewFriendMessage deleteMessage = NewFriendMessage.createLocalMessage(CoreManager.requireSelf(mService),
                        XmppMessage.TYPE_DELALL, null, chatMessage.getToUserId(), toUserName);
                // 先从朋友表取出该用户，判断为公众号还是好友
                Friend friend = FriendDao.getInstance().getFriend(mLoginUserId, chatMessage.getToUserId());
                if (friend != null && friend.getStatus() == Friend.STATUS_SYSTEM) {
                    deleteMessage.setContent(FLYApplication.getContext().getString(R.string.delete_firend_public) + friend.getNickName());
                } else {
                    deleteMessage.setContent(FLYApplication.getContext().getString(R.string.delete_firend) + toUserName);
                }
                FriendHelper.removeAttentionOrFriend(mLoginUserId, chatMessage.getToUserId());
                NewFriendDao.getInstance().createOrUpdateNewFriend(deleteMessage);
                NewFriendDao.getInstance().changeNewFriendState(chatMessage.getToUserId(), Friend.STATUS_16);
                ListenerManager.getInstance().notifyNewFriend(mLoginUserId, deleteMessage, true);
                // 通知聊天界面刷新
                EventBus.getDefault().post(new EventSyncFriendOperating(chatMessage.getToUserId(), chatMessage.getType()));
                break;
        }
        // 更新通讯录页面
        CardcastUiUpdateUtil.broadcastUpdateUi(mService);
    }

    /**
     * 对方发送过来的新朋友消息，处理逻辑不变
     */
    private void chatFriendForMe(ChatMessage chatMessage) {
        // json:fromUserId fromUserName type  content timeSend
        NewFriendMessage mNewMessage = new NewFriendMessage();
        mNewMessage.parserJsonData(chatMessage);
        mNewMessage.setOwnerId(mLoginUserId);
        mNewMessage.setRead(false);
        mNewMessage.setMySend(false);
        String content = "";
        switch (chatMessage.getType()) {
            case XmppMessage.TYPE_SAYHELLO:
                // 对方发过来的打招呼消息
                NewFriendDao.getInstance().createOrUpdateNewFriend(mNewMessage);
                NewFriendDao.getInstance().changeNewFriendState(mNewMessage.getUserId(), Friend.STATUS_11);
                // FriendDao.getInstance().updateLastChatMessage(mLoginUserId, Friend.ID_NEW_FRIEND_MESSAGE, chatMessage);

                ChatMessage sayHelloMessage = new ChatMessage();
                sayHelloMessage.setType(XmppMessage.TYPE_TEXT); //文本类型
                sayHelloMessage.setFromUserId(chatMessage.getFromUserId());
                sayHelloMessage.setFromUserName(chatMessage.getFromUserName());
                sayHelloMessage.setContent(getContext().getString(R.string.say_hello_default));
                sayHelloMessage.setMySend(false);
                sayHelloMessage.setMessageState(ChatMessageListener.MESSAGE_SEND_SUCCESS);
                sayHelloMessage.setPacketId(chatMessage.getPacketId());
                sayHelloMessage.setTimeSend(chatMessage.getTimeSend());
                ChatMessageDao.getInstance().saveNewSingleChatMessage(mLoginUserId, chatMessage.getFromUserId(), sayHelloMessage);
                ListenerManager.getInstance().notifyNewFriend(mLoginUserId, mNewMessage, true);
                break;
            case XmppMessage.TYPE_PASS:
                // 对方同意加我为好友
                NewFriendDao.getInstance().ascensionNewFriend(mNewMessage, Friend.STATUS_FRIEND);
                FriendHelper.beAddFriendExtraOperation(mNewMessage.getOwnerId(), mNewMessage.getUserId());
                /*content = getString("JXFriendObject_PassGo");
                FriendDao.getInstance().updateLastChatMessage(mLoginUserId, Friend.ID_NEW_FRIEND_MESSAGE, content);*/
                NewFriendDao.getInstance().changeNewFriendState(mNewMessage.getUserId(), Friend.STATUS_13);//添加了xxx
                content = getContext().getString(R.string.be_friendand_chat);
                FriendDao.getInstance().updateLastChatMessage(mLoginUserId, mNewMessage.getUserId(), content);
                ListenerManager.getInstance().notifyNewFriend(mLoginUserId, mNewMessage, true);
                break;
            case XmppMessage.TYPE_FEEDBACK:
                // 对方的回话
                NewFriendMessage feedBackMessage = NewFriendDao.getInstance().getNewFriendById(mNewMessage.getOwnerId(), mNewMessage.getUserId());
                NewFriendDao.getInstance().createOrUpdateNewFriend(mNewMessage);
                if (feedBackMessage.getState() == Friend.STATUS_11 || feedBackMessage.getState() == Friend.STATUS_15) {
                    NewFriendDao.getInstance().changeNewFriendState(mNewMessage.getUserId(), Friend.STATUS_15);
                } else {
                    NewFriendDao.getInstance().changeNewFriendState(mNewMessage.getUserId(), Friend.STATUS_14);
                }
                NewFriendDao.getInstance().updateNewFriendContent(mNewMessage.getUserId(), chatMessage.getContent());

                ChatMessage message = new ChatMessage();
                message.setType(XmppMessage.TYPE_TEXT);// 文本类型
                message.setFromUserId(mNewMessage.getUserId());
                message.setFromUserName(mNewMessage.getNickName());
                message.setContent(mNewMessage.getContent());
                message.setMySend(false);
                message.setMessageState(ChatMessageListener.MESSAGE_SEND_SUCCESS);
                message.setPacketId(chatMessage.getPacketId());
                message.setTimeSend(TimeUtils.sk_time_current_time());
                ChatMessageDao.getInstance().saveNewSingleAnswerMessage(mLoginUserId, mNewMessage.getUserId(), message);
                ListenerManager.getInstance().notifyNewFriend(mLoginUserId, mNewMessage, true);
                break;
            case XmppMessage.TYPE_FRIEND:
                // 我未开启好友验证，对方直接添加我为好友
                NewFriendDao.getInstance().ascensionNewFriend(mNewMessage, Friend.STATUS_FRIEND);
                FriendHelper.beAddFriendExtraOperation(mNewMessage.getOwnerId(), mNewMessage.getUserId());
                NewFriendDao.getInstance().changeNewFriendState(mNewMessage.getUserId(), Friend.STATUS_21);//添加了xxx
                content = getContext().getString(R.string.be_friendand_chat);
                FriendDao.getInstance().updateLastChatMessage(mLoginUserId, mNewMessage.getUserId(), content);
                ListenerManager.getInstance().notifyNewFriend(mLoginUserId, mNewMessage, true);
                break;
            case XmppMessage.TYPE_BLACK:
                // 对方将我拉黑
                NewFriendDao.getInstance().createOrUpdateNewFriend(mNewMessage);// 本地可能没有该NewFriend，需要先创建在修改其status
                NewFriendDao.getInstance().changeNewFriendState(mNewMessage.getUserId(), Friend.STATUS_19);
                FriendHelper.beBlacklistExtraOperation(mNewMessage.getOwnerId(), mNewMessage.getUserId());
               /* content = mNewMessage.getNickName() + " " + getString("JXFriendObject_PulledBlack");
                FriendDao.getInstance().updateLastChatMessage(mLoginUserId, Friend.ID_NEW_FRIEND_MESSAGE, content);*/
                ListenerManager.getInstance().notifyNewFriend(mLoginUserId, mNewMessage, true);
                // 关闭聊天界面
                ChatActivity.callFinish(getContext(), getContext().getString(R.string.be_pulled_black), mNewMessage.getUserId());
                break;
            case XmppMessage.TYPE_REFUSED:
                // 对方将我移出了黑名单
                NewFriendDao.getInstance().ascensionNewFriend(mNewMessage, Friend.STATUS_FRIEND);
                FriendHelper.beAddFriendExtraOperation(mNewMessage.getOwnerId(), mNewMessage.getUserId());
                NewFriendDao.getInstance().changeNewFriendState(mNewMessage.getUserId(), Friend.STATUS_24);//添加了xxx
                content = FLYApplication.getContext().getString(R.string.be_friendand_chat);
                FriendDao.getInstance().updateLastChatMessage(mLoginUserId, mNewMessage.getUserId(), content);
                ListenerManager.getInstance().notifyNewFriend(mLoginUserId, mNewMessage, true);
                break;
            case XmppMessage.TYPE_DELALL:
                // 对方删除了我
                NewFriendDao.getInstance().createOrUpdateNewFriend(mNewMessage);// 本地可能没有该NewFriend，需要先创建在修改其status
                FriendHelper.removeAttentionOrFriend(mNewMessage.getOwnerId(), mNewMessage.getUserId());
                NewFriendDao.getInstance().changeNewFriendState(mNewMessage.getUserId(), Friend.STATUS_17);
                ListenerManager.getInstance().notifyNewFriend(mLoginUserId, mNewMessage, true);
                // 关闭聊天界面
                ChatActivity.callFinish(getContext(), getContext().getString(R.string.delete_firend), mNewMessage.getUserId());
                break;
            case XmppMessage.TYPE_CONTACT_BE_FRIEND:
                // 对方通过 手机联系人 添加我 直接成为好友
                NewFriendDao.getInstance().ascensionNewFriend(mNewMessage, Friend.STATUS_FRIEND);
                FriendHelper.beAddFriendExtraOperation(mNewMessage.getOwnerId(), mNewMessage.getUserId());
                NewFriendDao.getInstance().changeNewFriendState(mNewMessage.getUserId(), Friend.STATUS_25);// 通过手机联系人添加
                content = getContext().getString(R.string.be_friendand_chat);
                FriendDao.getInstance().updateLastChatMessage(mLoginUserId, mNewMessage.getUserId(), content);
                ListenerManager.getInstance().notifyNewFriend(mLoginUserId, mNewMessage, true);
                break;
            case XmppMessage.TYPE_NEW_CONTACT_REGISTER: {
                // 我之前上传给服务端的联系人表内有人注册了，更新 手机联系人
                JSONObject jsonObject = JSONObject.parseObject(chatMessage.getContent());
                Contact contact = new Contact();
                contact.setTelephone(jsonObject.getString("telephone"));
                contact.setToTelephone(jsonObject.getString("toTelephone"));
                String toUserId = jsonObject.getString("toUserId");
                contact.setToUserId(toUserId);
                contact.setToUserName(jsonObject.getString("toUserName"));
                contact.setUserId(jsonObject.getString("userId"));
                if (ContactDao.getInstance().createContact(contact)) {// 本地创建成功 更新未读数量
                    EventBus.getDefault().post(new MessageContactEvent(toUserId));
                }
                break;
            }
            case XmppMessage.TYPE_REMOVE_ACCOUNT: {
                // 用户被后台删除，用于客户端更新本地数据 ，from是系统管理员 ObjectId是被删除人的userId，
                String removedAccountId = chatMessage.getObjectId();
                Friend toUser = FriendDao.getInstance().getFriend(mLoginUserId, removedAccountId);
                if (toUser != null) {
                    mNewMessage.setUserId(removedAccountId);
                    mNewMessage.setNickName(toUser.getNickName());
                    NewFriendDao.getInstance().createOrUpdateNewFriend(mNewMessage);// 本地可能没有该NewFriend，需要先创建在修改其status
                    FriendHelper.friendAccountRemoved(mNewMessage.getOwnerId(), mNewMessage.getUserId());
                    NewFriendDao.getInstance().changeNewFriendState(mNewMessage.getUserId(), Friend.STATUS_26);
                    NewFriendDao.getInstance().updateNewFriendContent(mNewMessage.getUserId(), chatMessage.getContent());
                    ListenerManager.getInstance().notifyNewFriend(mLoginUserId, mNewMessage, true);
                    // 关闭聊天界面
                    ChatActivity.callFinish(getContext(), chatMessage.getContent(), removedAccountId);
                }
                break;
            }
            case XmppMessage.TYPE_BACK_DELETE: {
                // 后台删除了我的一个好友关系，
                JSONObject json = JSON.parseObject(chatMessage.getObjectId());
                String fromUserId = json.getString("fromUserId");
                String fromUserName = json.getString("fromUserName");
                String toUserId = json.getString("toUserId");
                String toUserName = json.getString("toUserName");
                if (TextUtils.equals(fromUserId, mLoginUserId)) {
                    // 我删除别人，
                    mNewMessage.setUserId(toUserId);
                    mNewMessage.setNickName(toUserName);
                    // 先从朋友表取出该用户，判断为公众号还是好友
                    Friend friend = FriendDao.getInstance().getFriend(mLoginUserId, toUserId);
                    if (friend != null && friend.getStatus() == Friend.STATUS_SYSTEM) {
                        mNewMessage.setContent(FLYApplication.getContext().getString(R.string.delete_firend_public) + toUserName);
                    } else {
                        mNewMessage.setContent(FLYApplication.getContext().getString(R.string.delete_firend) + toUserName);
                    }
                    NewFriendDao.getInstance().createOrUpdateNewFriend(mNewMessage);// 本地可能没有该NewFriend，需要先创建在修改其status
                    FriendHelper.removeAttentionOrFriend(mNewMessage.getOwnerId(), mNewMessage.getUserId());
                    NewFriendDao.getInstance().changeNewFriendState(mNewMessage.getUserId(), Friend.STATUS_16);
                    ListenerManager.getInstance().notifyNewFriend(mLoginUserId, mNewMessage, true);
                } else {
                    // 别人删除我，
                    mNewMessage.setUserId(fromUserId);
                    mNewMessage.setNickName(fromUserName);
                    NewFriendDao.getInstance().createOrUpdateNewFriend(mNewMessage);// 本地可能没有该NewFriend，需要先创建在修改其status
                    FriendHelper.removeAttentionOrFriend(mNewMessage.getOwnerId(), mNewMessage.getUserId());
                    NewFriendDao.getInstance().changeNewFriendState(mNewMessage.getUserId(), Friend.STATUS_17);
                    ListenerManager.getInstance().notifyNewFriend(mLoginUserId, mNewMessage, true);
                }
                // 关闭聊天界面
                ChatActivity.callFinish(getContext(), getContext().getString(R.string.delete_firend), mNewMessage.getUserId());
                break;
            }
            case XmppMessage.TYPE_BACK_BLACK: {
                // 后台拉黑了我的好友或者拉黑了我本身，
                JSONObject json = JSON.parseObject(chatMessage.getObjectId());
                String fromUserId = json.getString("fromUserId");
                String fromUserName = json.getString("fromUserName");
                String toUserId = json.getString("toUserId");
                if (TextUtils.equals(fromUserId, mLoginUserId)) {
                    // 我拉黑别人，
                    mNewMessage.setUserId(toUserId);
                    Friend toUser = FriendDao.getInstance().getFriend(mLoginUserId, toUserId);
                    if (toUser == null) {
                        FLYReporter.post("后台拉黑了个不存在的好友，" + toUserId);
                        return;
                    }
                    mNewMessage.setNickName(toUser.getNickName());
                    FriendDao.getInstance().updateFriendStatus(mLoginUserId, toUserId, Friend.STATUS_BLACKLIST);
                    FriendHelper.addBlacklistExtraOperation(mLoginUserId, toUserId);

                    ChatMessage addBlackChatMessage = new ChatMessage();
                    addBlackChatMessage.setContent(getContext().getString(R.string.added_black_list) + " " + toUser.getShowName());
                    addBlackChatMessage.setTimeSend(TimeUtils.sk_time_current_time());
                    FriendDao.getInstance().updateLastChatMessage(mLoginUserId, Friend.ID_NEW_FRIEND_MESSAGE, addBlackChatMessage);

                    NewFriendDao.getInstance().createOrUpdateNewFriend(mNewMessage);
                    NewFriendDao.getInstance().changeNewFriendState(mNewMessage.getUserId(), Friend.STATUS_18);
                    ListenerManager.getInstance().notifyNewFriend(mLoginUserId, mNewMessage, true);
                    // 关闭聊天界面
                    ChatActivity.callFinish(getContext(), chatMessage.getContent(), toUserId);
                } else {
                    // 我被拉黑，
                    mNewMessage.setUserId(fromUserId);
                    mNewMessage.setNickName(fromUserName);
                    NewFriendDao.getInstance().createOrUpdateNewFriend(mNewMessage);// 本地可能没有该NewFriend，需要先创建在修改其status
                    NewFriendDao.getInstance().changeNewFriendState(mNewMessage.getUserId(), Friend.STATUS_19);
                    FriendHelper.beBlacklistExtraOperation(mNewMessage.getOwnerId(), mNewMessage.getUserId());
               /* content = mNewMessage.getNickName() + " " + getString("JXFriendObject_PulledBlack");
                FriendDao.getInstance().updateLastChatMessage(mLoginUserId, Friend.ID_NEW_FRIEND_MESSAGE, content);*/
                    ListenerManager.getInstance().notifyNewFriend(mLoginUserId, mNewMessage, true);
                    // 关闭聊天界面
                    ChatActivity.callFinish(getContext(), getContext().getString(R.string.be_pulled_black), mNewMessage.getUserId());
                }
                break;
            }
            case XmppMessage.TYPE_BACK_REFUSED: {
                // 后台取消拉黑了我的好友或者取消拉黑了我本身，
                JSONObject json = JSON.parseObject(chatMessage.getObjectId());
                String fromUserId = json.getString("fromUserId");
                String fromUserName = json.getString("fromUserName");
                String toUserId = json.getString("toUserId");
                if (TextUtils.equals(fromUserId, mLoginUserId)) {
                    // 取消拉黑了我的黑名单，
                    mNewMessage.setUserId(toUserId);
                    Friend toUser = FriendDao.getInstance().getFriend(mLoginUserId, toUserId);
                    if (toUser == null) {
                        FLYReporter.post("后台取消拉黑了个不存在的好友，" + toUserId);
                    } else {
                        mNewMessage.setNickName(toUser.getNickName());
                    }
                    NewFriendDao.getInstance().ascensionNewFriend(mNewMessage, Friend.STATUS_FRIEND);
                    FriendHelper.beAddFriendExtraOperation(mLoginUserId, toUserId);

                    User self = CoreManager.requireSelf(mService);
                    ChatMessage removeChatMessage = new ChatMessage();
                    removeChatMessage.setContent(self.getNickName() + getContext().getString(R.string.remove_blacklist_succ));
                    removeChatMessage.setTimeSend(TimeUtils.sk_time_current_time());
                    FriendDao.getInstance().updateLastChatMessage(mLoginUserId, Friend.ID_NEW_FRIEND_MESSAGE, removeChatMessage);
                    NewFriendDao.getInstance().createOrUpdateNewFriend(mNewMessage);
                    NewFriendDao.getInstance().changeNewFriendState(toUserId, Friend.STATUS_24);
                    ListenerManager.getInstance().notifyNewFriend(mLoginUserId, mNewMessage, true);
                } else {
                    // 我被取消拉黑，
                    mNewMessage.setUserId(fromUserId);
                    mNewMessage.setNickName(fromUserName);
                    NewFriendDao.getInstance().ascensionNewFriend(mNewMessage, Friend.STATUS_FRIEND);
                    FriendHelper.beAddFriendExtraOperation(mNewMessage.getOwnerId(), mNewMessage.getUserId());
                    NewFriendDao.getInstance().changeNewFriendState(mNewMessage.getUserId(), Friend.STATUS_24);//添加了xxx
                    content = FLYApplication.getContext().getString(R.string.be_friendand_chat);
                    FriendDao.getInstance().updateLastChatMessage(mLoginUserId, mNewMessage.getUserId(), content);
                    ListenerManager.getInstance().notifyNewFriend(mLoginUserId, mNewMessage, true);
                }
                break;
            }
            case XmppMessage.TYPE_NEWSEE:// 对方单向关注了我
            case XmppMessage.TYPE_DELSEE:// 对方取消了对我的单向关注
                // 单向关注 功能已去掉
                break;
            case XmppMessage.TYPE_RECOMMEND:
                // 新推荐好友 好像无此功能
                break;
            default:
                break;
        }
        // 更新通讯录页面
        CardcastUiUpdateUtil.broadcastUpdateUi(mService);
    }

    /**
     * 朋友圈相关消息逻辑处理
     */
    private void chatDiscover(String toUserName, ChatMessage chatMessage) {
        if (MyZanDao.getInstance().hasSameZan(chatMessage.getPacketId())) {
            Log.e("msg", "本地已存在该条赞或评论消息");
            return;
        }
        MyZan zan = new MyZan();
        zan.setFromUserId(chatMessage.getFromUserId());
        zan.setFromUsername(chatMessage.getFromUserName());
        zan.setSendtime(String.valueOf(chatMessage.getTimeSend()));
        zan.setLoginUserId(mLoginUserId);
        zan.setZanbooleanyidu(0);
        zan.setSystemid(chatMessage.getPacketId());
        /**
         * object组成: id,type,content
         *
         * id
         * type:1 文本 2 图片 3 语音 4 视频
         * content:文本内容
         */
        String[] data = chatMessage.getObjectId().split(",");
        zan.setCricleuserid(data[0]);
        zan.setType(Integer.parseInt(data[1]));
        if (Integer.parseInt(data[1]) == 1) {// 文本类型
            zan.setContent(data[2]);
        } else {// 其他类型
            zan.setContenturl(data[2]);
        }

        if (chatMessage.getType() == XmppMessage.DIANZAN) {// 赞
            zan.setHuifu("101");
            if (MyZanDao.getInstance().addZan(zan)) {
                int size = MyZanDao.getInstance().getZanSize(mLoginUserId);
                EventBus.getDefault().post(new MessageEventHongdian(size));
                EventBus.getDefault().post(new MessageEventNotifyDynamic(size));
            } else {
                // 针对该条说说fromUserId已经点赞过一次了，就不重复提醒了，需要Return掉，继续往下走会有提示音
                return;
            }
        } else if (chatMessage.getType() == XmppMessage.R_DIANZAN) {// 取消赞
            // 这样会将评论也删掉，仅删除赞
            // MyZanDao.getInstance().deleteZan(zan.getLoginUserId(), zan.getFromUserId(), zan.getCricleuserid());
            MyZanDao.getInstance().deleteZanOlnyPraise(zan.getLoginUserId(), zan.getFromUserId(), zan.getCricleuserid());
            int size = MyZanDao.getInstance().getZanSize(mLoginUserId);
            EventBus.getDefault().post(new MessageEventHongdian(size));
            EventBus.getDefault().post(new MessageEventNotifyDynamic(size));
            return;
        } else if (chatMessage.getType() == XmppMessage.PINGLUN) {// 评论
            if (chatMessage.getContent() != null) {
                zan.setHuifu(chatMessage.getContent());
            }
            zan.setTousername(toUserName);
            MyZanDao.getInstance().addZan(zan);
            int size = MyZanDao.getInstance().getZanSize(mLoginUserId);
            EventBus.getDefault().post(new MessageEventHongdian(size));
            EventBus.getDefault().post(new MessageEventNotifyDynamic(size));
        } else if (chatMessage.getType() == XmppMessage.ATMESEE) {// 提醒我看
            zan.setHuifu("102");
            MyZanDao.getInstance().addZan(zan);
            int size = MyZanDao.getInstance().getZanSize(mLoginUserId);
            EventBus.getDefault().post(new MessageEventHongdian(size));
            EventBus.getDefault().post(new MessageEventNotifyDynamic(size));
        }

        // 朋友圈消息也要提示，
        NoticeVoicePlayer.getInstance().start();
    }

    /**
     * 音视频相关消息逻辑处理
     */
    private void chatAudioVideo(ChatMessage chatMessage) {
        int type = chatMessage.getType();
        Log.e("AVI", type + "");
        String fromUserId = chatMessage.getFromUserId();
        if (fromUserId.equals(mLoginUserId)) {
            switch (chatMessage.getType()) {
                case XmppMessage.TYPE_IS_CONNECT_VOICE:
                    // 其他端发起语音通话请求，转发给本端，不处理
                    break;
                case XmppMessage.TYPE_CONNECT_VOICE:
                    // 其他端已接听语音通话，本端需要结束当前来电显示界面
                    EventBus.getDefault().post(new MessageHangUpPhone(chatMessage));
                    break;
                case XmppMessage.TYPE_NO_CONNECT_VOICE:
                    // 其他端拒接 || 无响应 语音通话，本端需要结束当前来电显示界面
                    EventBus.getDefault().post(new MessageHangUpPhone(chatMessage));
                    break;
                case XmppMessage.TYPE_END_CONNECT_VOICE:
                    // 其他端结束了语音通话，不处理
                    break;
                case XmppMessage.TYPE_IS_CONNECT_VIDEO:
                    // 其他端发起视频通话请求，转发给本端， 不处理
                    break;
                case XmppMessage.TYPE_CONNECT_VIDEO:
                    // 其他端已接听视频通话，本端需要结束当前来电显示界面
                    EventBus.getDefault().post(new MessageHangUpPhone(chatMessage));
                    break;
                case XmppMessage.TYPE_NO_CONNECT_VIDEO:
                    // 其他端拒接 || 无响应 视频通话，本端需要结束当前来电显示界面
                    EventBus.getDefault().post(new MessageHangUpPhone(chatMessage));
                    break;
                case XmppMessage.TYPE_END_CONNECT_VIDEO:
                    // 其他端结束了视频通话，不处理
                    break;
                case XmppMessage.TYPE_IS_MU_CONNECT_VOICE:
                    // 其他端发起语音会议请求，不处理
                    break;
                case XmppMessage.TYPE_IS_MU_CONNECT_VIDEO:
                    // 其他端发起视频会议请求，不处理
                    break;
                case XmppMessage.TYPE_IS_MU_CONNECT_TALK:
                    // 其他端发起对讲机请求，不处理
                    break;

                case XmppMessage.TYPE_IN_CALLING:
                    // 其他端发送的通话中消息，不处理
                    break;
                case XmppMessage.TYPE_IS_BUSY:
                    // 其他端发送的忙线消息，不处理
                    break;
            }
        } else {
            if (chatMessage.getType() == XmppMessage.TYPE_IN_CALLING
                    || chatMessage.getType() == XmppMessage.TYPE_IS_BUSY) {
                if (chatMessage.getType() == XmppMessage.TYPE_IS_BUSY) {// 延迟两秒发送该通知，防止自己拨号页面还未拉起就收到了
                    Log.e("zq", "收到" + chatMessage.getFromUserName() + "的busy消息");
                    new Handler(Looper.getMainLooper()).postDelayed(new Runnable() {
                        @Override
                        public void run() {
                            Log.e("zq", "发送busy通知给" + chatMessage.getFromUserName());
                            EventBus.getDefault().post(new MessageCallingEvent(chatMessage));
                        }
                    }, 2000);
                    if (ChatMessageDao.getInstance().saveNewSingleChatMessage(mLoginUserId, chatMessage.getFromUserId(), chatMessage)) {
                        ListenerManager.getInstance().notifyNewMesssage(mLoginUserId, fromUserId, chatMessage, false);
                    }
                } else {
                    EventBus.getDefault().post(new MessageCallingEvent(chatMessage));
                }
                return;
            }

            if (chatMessage.getType() == XmppMessage.TYPE_CHANGE_VIDEO_ENABLE) {
                EventBus.getDefault().post(new MessageCallTypeChange(chatMessage));
            }

            /*
            单聊 语音通话
             */
            if (chatMessage.getType() == XmppMessage.TYPE_IS_CONNECT_VOICE) {
                Friend friend = FriendDao.getInstance().getFriend(mLoginUserId, chatMessage.getFromUserId());
                if (friend != null && friend.getStatus() == -1) {
                    // 不处理黑名单来的音视频邀请，
                    return;
                }
                // 对方来电
                if (JitsistateMachine.isInCalling
                        && !TextUtils.isEmpty(JitsistateMachine.callingOpposite)) {
                    if (JitsistateMachine.callingOpposite.equals(chatMessage.getFromUserId())) {
                        // 当前正在通话中且该条消息的发送方与通话对象一致(对方可能异常断开了，且本地的ping机制还未检测出来，自行挂断，
                        // 在发送通知弹起来电界面)
                        EventBus.getDefault().post(new EventNotifyByTag(EventNotifyByTag.Interrupt));
                        new Handler(Looper.getMainLooper()).postDelayed(new Runnable() {
                            @Override
                            public void run() {
                                EventBus.getDefault().post(new MessageEventSipEVent(100, fromUserId, chatMessage));
                            }
                        }, 500);
                    } else {
                        // 当前正在通话中且该条消息的发送方与通话对象不一致 通知发送方忙线中...
                        Log.e("zq", "发送busy消息给" + chatMessage.getFromUserName());
                        mService.sendBusyMessage(chatMessage.getFromUserId(), 0);
                    }
                    return;
                }
                Log.e("AVI", TimeUtils.sk_time_current_time() - chatMessage.getTimeSend() + "");
                if (TimeUtils.sk_time_current_time() - chatMessage.getTimeSend() <= 30 * 1000) {// 当前时间与对方发送邀请的时间间隔在30s以内
                    EventBus.getDefault().post(new MessageEventSipEVent(100, fromUserId, chatMessage));
                } else {
                    Log.e("AVI", "离线消息");
                }
            } else if (chatMessage.getType() == XmppMessage.TYPE_CONNECT_VOICE) {
                // 对方接听语音通话，发送102
                EventBus.getDefault().post(new MessageEventSipEVent(102, null, chatMessage));
            } else if (chatMessage.getType() == XmppMessage.TYPE_NO_CONNECT_VOICE) {
                // 对方拒接 || 无响应
                EventBus.getDefault().post(new MessageEventSipEVent(103, null, chatMessage));
                String content = "";
                chatMessage.setMySend(false);
                if (chatMessage.getTimeLen() == 0) {
                    content = FLYApplication.getContext().getString(R.string.sip_canceled) + FLYApplication.getContext().getString(R.string.voice_chat);
                } else {
                    content = FLYApplication.getContext().getString(R.string.sip_noanswer);
                }
                ChatMessageDao.getInstance().saveNewSingleChatMessage(mLoginUserId, fromUserId, chatMessage);
                ListenerManager.getInstance().notifyNewMesssage(mLoginUserId, fromUserId, chatMessage, false);
                FriendDao.getInstance().updateFriendContent(mLoginUserId, fromUserId, content, XmppMessage.TYPE_NO_CONNECT_VOICE, chatMessage.getTimeSend());
                MsgBroadcast.broadcastMsgUiUpdate(FLYApplication.getInstance());
                new Handler(Looper.getMainLooper()).postDelayed(new Runnable() {
                    @Override
                    public void run() {
                        EventBus.getDefault().post(new MessageHangUpPhone(chatMessage));
                    }
                }, 1000);// 延迟一秒在发送挂断消息，防止当我们离线时，对方发起通话之后又取消了通话，我们30秒内上线，在来点界面拉起时该Event也发送出去了
            } else if (chatMessage.getType() == XmppMessage.TYPE_END_CONNECT_VOICE) {
                // 通话后，对方挂断
                chatMessage.setMySend(false);
                ChatMessageDao.getInstance().saveNewSingleChatMessage(mLoginUserId, fromUserId, chatMessage);
                ListenerManager.getInstance().notifyNewMesssage(mLoginUserId, fromUserId, chatMessage, false);
                FriendDao.getInstance().updateFriendContent(mLoginUserId, fromUserId, FLYApplication.getContext().getString(R.string.finished) + FLYApplication.getContext().getString(R.string.voice_chat) + "," +
                        FLYApplication.getContext().getString(R.string.time_len) + ":" + chatMessage.getTimeLen() + FLYApplication.getContext().getString(R.string.second), XmppMessage.TYPE_END_CONNECT_VOICE, chatMessage.getTimeSend());
                MsgBroadcast.broadcastMsgUiUpdate(FLYApplication.getInstance());
                // 通知通话界面挂断
                EventBus.getDefault().post(new MessageHangUpPhone(chatMessage));
            }

             /*
            单聊  视频通话
             */
            if (type == XmppMessage.TYPE_IS_CONNECT_VIDEO) {
                Friend friend = FriendDao.getInstance().getFriend(mLoginUserId, chatMessage.getFromUserId());
                if (friend != null && friend.getStatus() == -1) {
                    // 不处理黑名单来的音视频邀请，
                    return;
                }
                if (JitsistateMachine.isInCalling
                        && !TextUtils.isEmpty(JitsistateMachine.callingOpposite)) {
                    if (JitsistateMachine.callingOpposite.equals(chatMessage.getFromUserId())) {
                        // 当前正在通话中且该条消息的发送方与通话对象一致(对方可能异常断开了，且本地的ping机制还未检测出来，自行挂断，
                        // 在发送通知弹起来电界面)
                        EventBus.getDefault().post(new EventNotifyByTag(EventNotifyByTag.Interrupt));
                        new Handler(Looper.getMainLooper()).postDelayed(new Runnable() {
                            @Override
                            public void run() {
                                EventBus.getDefault().post(new MessageEventSipEVent(110, fromUserId, chatMessage));
                            }
                        }, 500);
                    } else {
                        // 当前正在通话中且该条消息的发送方与通话对象不一致 通知发送方忙线中...
                        Log.e("zq", "发送busy消息给" + chatMessage.getFromUserName());
                        mService.sendBusyMessage(chatMessage.getFromUserId(), 1);
                    }
                    return;
                }
                Log.e("AVI", TimeUtils.sk_time_current_time() - chatMessage.getTimeSend() + "");
                if (TimeUtils.sk_time_current_time() - chatMessage.getTimeSend() <= 30 * 1000) {// 当前时间与对方发送邀请的时间间隔在30s以内
                    EventBus.getDefault().post(new MessageEventSipEVent(110, fromUserId, chatMessage));
                } else {
                    Log.e("AVI", "离线消息");
                }
            } else if (type == XmppMessage.TYPE_CONNECT_VIDEO) {
                EventBus.getDefault().post(new MessageEventSipEVent(112, null, chatMessage));
            } else if (type == XmppMessage.TYPE_NO_CONNECT_VIDEO) {
                EventBus.getDefault().post(new MessageEventSipEVent(113, null, chatMessage));
                chatMessage.setMySend(false);
                String content = "";
                if (chatMessage.getTimeLen() == 0) {
                    content = FLYApplication.getContext().getString(R.string.sip_canceled) + FLYApplication.getContext().getString(R.string.voice_chat);
                } else {
                    content = FLYApplication.getContext().getString(R.string.sip_noanswer);
                }
                ChatMessageDao.getInstance().saveNewSingleChatMessage(mLoginUserId, fromUserId, chatMessage);
                FriendDao.getInstance().updateFriendContent(mLoginUserId, fromUserId, content, XmppMessage.TYPE_NO_CONNECT_VIDEO, chatMessage.getTimeSend());
                ListenerManager.getInstance().notifyNewMesssage(mLoginUserId, fromUserId, chatMessage, false);
                new Handler(Looper.getMainLooper()).postDelayed(new Runnable() {
                    @Override
                    public void run() {
                        EventBus.getDefault().post(new MessageHangUpPhone(chatMessage));
                    }
                }, 1000);// 延迟一秒在发送挂断消息，防止当我们离线时，对方发起通话之后又取消了通话，我们30秒内上线，在来点界面拉起时该Event也发送出去了
            } else if (type == XmppMessage.TYPE_END_CONNECT_VIDEO) {
                chatMessage.setMySend(false);
                ChatMessageDao.getInstance().saveNewSingleChatMessage(mLoginUserId, fromUserId, chatMessage);
                ListenerManager.getInstance().notifyNewMesssage(mLoginUserId, fromUserId, chatMessage, false);
                FriendDao.getInstance().updateFriendContent(mLoginUserId, fromUserId, FLYApplication.getContext().getString(R.string.finished) + FLYApplication.getContext().getString(R.string.video_call) + "," +
                        FLYApplication.getContext().getString(R.string.time_len) + ":" + chatMessage.getTimeLen() + FLYApplication.getContext().getString(R.string.second), XmppMessage.TYPE_END_CONNECT_VIDEO, chatMessage.getTimeSend());
                EventBus.getDefault().post(new MessageHangUpPhone(chatMessage));
            }

            /**
             群组 音视频会议邀请
             */
            if (type == XmppMessage.TYPE_IS_MU_CONNECT_VOICE) {
                Log.e("AVI", TimeUtils.sk_time_current_time() - chatMessage.getTimeSend() + "");
                if (TimeUtils.sk_time_current_time() - chatMessage.getTimeSend() <= 30 * 1000) {// 当前时间与对方发送邀请的时间间隔在30s以内
                    EventBus.getDefault().post(new MessageEventMeetingInvited(CallConstants.Audio_Meet, chatMessage));
                } else {
                    Log.e("AVI", "离线消息");
                }
                // 音视频会议消息不保存
/*
                if (ChatMessageDao.getInstance().saveNewSingleChatMessage(mLoginUserId, fromUserId, chatMessage)) {
                    ListenerManager.getInstance().notifyNewMesssage(mLoginUserId, fromUserId, chatMessage, false);
                    FriendDao.getInstance().updateFriendContent(mLoginUserId, fromUserId, chatMessage.getContent(), XmppMessage.TYPE_IS_MU_CONNECT_VOICE, chatMessage.getTimeSend());
                }
*/
            } else if (type == XmppMessage.TYPE_IS_MU_CONNECT_VIDEO) {
                Log.e("AVI", TimeUtils.sk_time_current_time() - chatMessage.getTimeSend() + "");
                if (TimeUtils.sk_time_current_time() - chatMessage.getTimeSend() <= 30 * 1000) {// 当前时间与对方发送邀请的时间间隔在30s以内
                    EventBus.getDefault().post(new MessageEventMeetingInvited(CallConstants.Video_Meet, chatMessage));
                } else {
                    Log.e("AVI", "离线消息");
                }
                // 音视频会议消息不保存
/*
                if (ChatMessageDao.getInstance().saveNewSingleChatMessage(mLoginUserId, fromUserId, chatMessage)) {
                    ListenerManager.getInstance().notifyNewMesssage(mLoginUserId, fromUserId, chatMessage, false);
                    FriendDao.getInstance().updateFriendContent(mLoginUserId, fromUserId, chatMessage.getContent(), XmppMessage.TYPE_IS_MU_CONNECT_Video, chatMessage.getTimeSend());
                }
*/
            } else if (type == XmppMessage.TYPE_IS_MU_CONNECT_TALK) {
                Log.e("AVI", TimeUtils.sk_time_current_time() - chatMessage.getTimeSend() + "");
                if (TimeUtils.sk_time_current_time() - chatMessage.getTimeSend() <= 30 * 1000) {// 当前时间与对方发送邀请的时间间隔在30s以内
                    EventBus.getDefault().post(new MessageEventMeetingInvited(CallConstants.Talk_Meet, chatMessage));
                } else {
                    Log.e("AVI", "离线消息");
                }
                // 音视频会议消息不保存
/*
                if (ChatMessageDao.getInstance().saveNewSingleChatMessage(mLoginUserId, fromUserId, chatMessage)) {
                    ListenerManager.getInstance().notifyNewMesssage(mLoginUserId, fromUserId, chatMessage, false);
                    FriendDao.getInstance().updateFriendContent(mLoginUserId, fromUserId, chatMessage.getContent(), XmppMessage.TYPE_IS_MU_CONNECT_Video, chatMessage.getTimeSend());
                }
*/
            }
        }
    }

    private String getName(Friend friend, String userId) {
        if (friend == null) {
            return null;
        }
        RoomMember mRoomMember = RoomMemberDao.getInstance().getSingleRoomMember(friend.getRoomId(), mLoginUserId);
        if (mRoomMember != null && mRoomMember.getRole() == 1) {// 我为群主 Name显示为群内备注
            RoomMember member = RoomMemberDao.getInstance().getSingleRoomMember(friend.getRoomId(), userId);
            if (member != null && !TextUtils.equals(member.getUserName(), member.getCardName())) {
                // 当userName与cardName不一致时，我们认为群主有设置群内备注
                return member.getCardName();
            } else {
                Friend mFriend = FriendDao.getInstance().getFriend(mLoginUserId, userId);
                if (mFriend != null && !TextUtils.isEmpty(mFriend.getRemarkName())) {
                    return mFriend.getRemarkName();
                }
            }
        } else {// 为好友 显示备注
            Friend mFriend = FriendDao.getInstance().getFriend(mLoginUserId, userId);
            if (mFriend != null && !TextUtils.isEmpty(mFriend.getRemarkName())) {
                return mFriend.getRemarkName();
            }
        }
        return null;
    }
}
