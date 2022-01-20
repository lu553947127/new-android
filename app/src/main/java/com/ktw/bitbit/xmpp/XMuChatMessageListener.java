package com.ktw.bitbit.xmpp;

import android.annotation.SuppressLint;
import android.content.Intent;
import android.text.TextUtils;
import android.util.Log;

import com.ktw.bitbit.FLYApplication;
import com.ktw.bitbit.R;
import com.ktw.bitbit.FLYReporter;
import com.ktw.bitbit.audio.NoticeVoicePlayer;
import com.ktw.bitbit.bean.Friend;
import com.ktw.bitbit.bean.MsgRoamTask;
import com.ktw.bitbit.bean.RoomMember;
import com.ktw.bitbit.bean.event.EventNewNotice;
import com.ktw.bitbit.bean.message.ChatMessage;
import com.ktw.bitbit.bean.message.XmppMessage;
import com.ktw.bitbit.broadcast.MsgBroadcast;
import com.ktw.bitbit.broadcast.MucgroupUpdateUtil;
import com.ktw.bitbit.broadcast.OtherBroadcast;
import com.ktw.bitbit.call.talk.MessageTalkJoinEvent;
import com.ktw.bitbit.call.talk.MessageTalkLeftEvent;
import com.ktw.bitbit.call.talk.MessageTalkOnlineEvent;
import com.ktw.bitbit.call.talk.MessageTalkReleaseEvent;
import com.ktw.bitbit.call.talk.MessageTalkRequestEvent;
import com.ktw.bitbit.db.dao.ChatMessageDao;
import com.ktw.bitbit.db.dao.FriendDao;
import com.ktw.bitbit.db.dao.MsgRoamTaskDao;
import com.ktw.bitbit.db.dao.RoomMemberDao;
import com.ktw.bitbit.ui.base.CoreManager;
import com.ktw.bitbit.ui.mucfile.XfileUtils;
import com.ktw.bitbit.util.Constants;
import com.ktw.bitbit.util.DateFormatUtil;
import com.ktw.bitbit.util.PreferenceUtils;
import com.ktw.bitbit.xmpp.listener.ChatMessageListener;

import org.json.JSONException;

import java.util.concurrent.TimeUnit;

import de.greenrobot.event.EventBus;

/**
 * Created by Administrator on 2017/11/24.
 */

public class XMuChatMessageListener {
    private CoreService mService;
    private String mLoginUserId;

    public XMuChatMessageListener(CoreService coreService) {
        mService = coreService;
        mLoginUserId = CoreManager.requireSelf(mService).getUserId();
    }

    public void onReceMessage(com.ktw.bitbit.socket.msg.ChatMessage message, ChatMessage chatMessage, boolean isDelay) {
        String roomJid;
        if (!TextUtils.isEmpty(message.getMessageHead().getTo())) {
            roomJid = message.getMessageHead().getTo();
        } else {
            roomJid = chatMessage.getToUserId();
        }
        saveGroupMessage(chatMessage, roomJid, isDelay);
    }

    /**
     * 保存接收到的聊天信息(群聊)
     */
    private void saveGroupMessage(ChatMessage chatMessage, String roomJid, boolean isDelay) {
        String packetId = chatMessage.getPacketId();

        if (TextUtils.equals(chatMessage.getFromUserId(), mLoginUserId)
                && chatMessage.getType() == XmppMessage.TYPE_READ
                && TextUtils.isEmpty(chatMessage.getFromUserName())) {
            chatMessage.setFromUserName(CoreManager.requireSelf(mService).getNickName());
        }

        ChatMessageDao.getInstance().decryptDES(chatMessage);// 解密
        int type = chatMessage.getType();
        chatMessage.setGroup(true);
        chatMessage.setMessageState(ChatMessageListener.MESSAGE_SEND_SUCCESS);

        Log.e("msg", "收到群聊消息" + chatMessage.toString());

        // 生成漫游任务
        if (isDelay) {
            if (chatMessage.isExpired()) {// 该条消息为过期消息，存入本地后直接Return ，不通知
                Log.e("msg_muc", "// 该条消息为过期消息，存入本地后直接Return ，不通知");
                chatMessage.setIsExpired(1);
                ChatMessageDao.getInstance().saveNewSingleChatMessage(mLoginUserId, roomJid, chatMessage);
                return;
            }
            // 离线消息 判断当前群组的实际离线消息是否大于100条，如大于100条，为之前创建的任务的endTime字段赋值，反之则删除任务
            // 判断条件 离线消息内有一条消息的msgId等于当前任务的startMsgId 离线消息小于100条
            MsgRoamTask mLastMsgRoamTask = MsgRoamTaskDao.getInstance().getFriendLastMsgRoamTask(mLoginUserId, roomJid); // 获取该群组最后一个任务
            if (mLastMsgRoamTask == null) {
            } else if (mLastMsgRoamTask.getEndTime() == 0) {// 为该任务的EndTime赋值 理论上只会赋值一次
                MsgRoamTaskDao.getInstance().updateMsgRoamTaskEndTime(mLoginUserId, roomJid, mLastMsgRoamTask.getTaskId(), chatMessage.getTimeSend());
            } else if (packetId.equals(mLastMsgRoamTask.getStartMsgId())) {
                MsgRoamTaskDao.getInstance().deleteMsgRoamTask(mLoginUserId, roomJid, mLastMsgRoamTask.getTaskId());
            }
        }

        boolean isShieldGroupMsg = PreferenceUtils.getBoolean(FLYApplication.getContext(), Constants.SHIELD_GROUP_MSG + roomJid + mLoginUserId, false);
        if (isShieldGroupMsg) {// 已屏蔽
            return;
        }

        if (type == XmppMessage.TYPE_TEXT
                && !TextUtils.isEmpty(chatMessage.getObjectId())) {// 判断为@消息
            Friend friend = FriendDao.getInstance().getFriend(mLoginUserId, roomJid);
            if (friend != null) {
                if (friend.getIsAtMe() == 0
                        && !TextUtils.equals(FLYApplication.IsRingId, roomJid)) {// 本地无@通知 && 收到该条消息时不处于当前群组的聊天界面
                    if (chatMessage.getObjectId().equals(roomJid)) {// @全体成员
                        FriendDao.getInstance().updateAtMeStatus(roomJid, 2);
                    } else if (chatMessage.getObjectId().contains(mLoginUserId)) {// @我
                        FriendDao.getInstance().updateAtMeStatus(roomJid, 1);
                    }
                }
            }
        }

        // 群已读
        if (type == XmppMessage.TYPE_READ) {
            packetId = chatMessage.getContent();
            ChatMessage chat = ChatMessageDao.getInstance().findMsgById(mLoginUserId, roomJid, packetId);
            if (chat != null) {
                String fromUserId = chatMessage.getFromUserId();
                boolean repeat = ChatMessageDao.getInstance().checkRepeatRead(mLoginUserId, roomJid, fromUserId, packetId);
                if (!repeat) {
                    int count = chat.getReadPersons();// 查看人数+1
                    chat.setReadPersons(count + 1);
                    // 覆盖最后时间
                    chat.setReadTime(chatMessage.getTimeSend());
                    // 更新消息数据
                    ChatMessageDao.getInstance().updateMessageRead(mLoginUserId, roomJid, chat);
                    // 保存新消息
                    ChatMessageDao.getInstance().saveNewSingleChatMessage(mLoginUserId, roomJid, chatMessage);
                    // 通知刷新
                    MsgBroadcast.broadcastMsgReadUpdate(FLYApplication.getInstance(), packetId);
                }
            }
            return;
        }

        // 某个成员领取了红包
        if (type == XmppMessage.TYPE_83) {
            String fromUserId = chatMessage.getFromUserId();// 红包领取方
            String toUserId = chatMessage.getToUserId();// 红包发送方
            String fromName;
            String toName;
            if (TextUtils.equals(fromUserId, mLoginUserId) && TextUtils.equals(toUserId, mLoginUserId)) {// 自己领取了自己的红包
                fromName = FLYApplication.getContext().getString(R.string.you);
                toName = FLYApplication.getContext().getString(R.string.self);
            } else if (TextUtils.equals(toUserId, mLoginUserId)) {// xx领取了你的红包
                fromName = chatMessage.getFromUserName();
                toName = FLYApplication.getContext().getString(R.string.you);
            } else if (TextUtils.equals(fromUserId, mLoginUserId)) {//你领取了xx的红包
                fromName = FLYApplication.getContext().getString(R.string.you);
                // toName = RoomMemberDao.getInstance().getRoomMemberName(chatMessage.getObjectId(), chatMessage.getFromUserId());
                toName = chatMessage.getToUserName();
            } else {// xx领取了xx的红包
                fromName = chatMessage.getFromUserName();
                // toName = RoomMemberDao.getInstance().getRoomMemberName(chatMessage.getObjectId(), chatMessage.getFromUserId());
                toName = chatMessage.getToUserName();
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
            fromUserId = chatMessage.getObjectId();
            if (ChatMessageDao.getInstance().saveNewSingleChatMessage(mLoginUserId, fromUserId, chatMessage)) {
                ListenerManager.getInstance().notifyNewMesssage(mLoginUserId, fromUserId, chatMessage, true);
            }
            return;
        }

        // 对讲机相关消息130-139
        if (type >= XmppMessage.TYPE_IS_MU_CONNECT_TALK && type <= XmppMessage.TYPE_TALK_KICK) {
            chatTalk(chatMessage);
            return;
        }

        // 消息撤回
        if (type == XmppMessage.TYPE_BACK) {
            // 本地数据库处理
            packetId = chatMessage.getContent();
            if (chatMessage.getFromUserId().equals(mLoginUserId)) {// 自己发的不用处理
                ChatMessageDao.getInstance().updateMessageBack(mLoginUserId, roomJid, packetId, FLYApplication.getContext().getString(R.string.you));
            } else {
                ChatMessageDao.getInstance().updateMessageBack(mLoginUserId, roomJid, packetId, chatMessage.getFromUserName(), chatMessage.getFromUserId());
            }

            Intent intent = new Intent();
            intent.putExtra("packetId", packetId);
            intent.setAction(com.ktw.bitbit.broadcast.OtherBroadcast.MSG_BACK);
            mService.sendBroadcast(intent);

            // 更新UI界面
            ChatMessage chat = ChatMessageDao.getInstance().getLastChatMessage(mLoginUserId, roomJid);
            if (chat != null) {
                if (chat.getPacketId().equals(packetId)) {
                    // 要撤回的消息正是朋友表的最后一条消息
                    if (chatMessage.getFromUserId().equals(mLoginUserId)) {// 自己发的不用处理
                        FriendDao.getInstance().updateFriendContent(mLoginUserId, roomJid,
                                FLYApplication.getContext().getString(R.string.you) + " " + FLYApplication.getInstance().getString(R.string.other_with_draw), XmppMessage.TYPE_TEXT, chatMessage.getTimeSend());
                    } else {
                        FriendDao.getInstance().updateFriendContent(mLoginUserId, roomJid,
                                chatMessage.getFromUserName() + " " + FLYApplication.getInstance().getString(R.string.other_with_draw), XmppMessage.TYPE_TEXT, chatMessage.getTimeSend());
                    }
                    MsgBroadcast.broadcastMsgUiUpdate(FLYApplication.getInstance());
                }
            }
            return;
        }

        if ((type >= XmppMessage.TYPE_MUCFILE_ADD && type <= XmppMessage.TYPE_MUCFILE_DOWN)
                || (type >= XmppMessage.TYPE_CHANGE_NICK_NAME && type <= XmppMessage.NEW_MEMBER)
                || type == XmppMessage.TYPE_SEND_MANAGER
                || type == XmppMessage.TYPE_EDIT_GROUP_NOTICE
                || (type >= XmppMessage.TYPE_CHANGE_SHOW_READ && type <= XmppMessage.TYPE_GROUP_TRANSFER)
                || type == XmppMessage.TYPE_UPDATE_ROLE) {
            if (TextUtils.isEmpty(chatMessage.getObjectId())) {
                Log.e("msg_muc", "Return 4");
                return;
            }
            if (ChatMessageDao.getInstance().hasSameMessage(mLoginUserId, chatMessage.getObjectId(), chatMessage.getPacketId())) {// 本地已经保存了这条消息，不处理
                Log.e("msg_muc", "Return 5");
                return;
            }
            Friend friend = FriendDao.getInstance().getFriend(mLoginUserId, chatMessage.getObjectId());
            if (friend != null) {
                chatGroup(chatMessage.getToUserName(), chatMessage, friend);
            }
            return;
        }

        if (type == XmppMessage.TYPE_GROUP_UPDATE_MSG_AUTO_DESTROY_TIME) {
            if (TextUtils.isEmpty(chatMessage.getObjectId())) {
                Log.e("msg_muc", "Return 4");
                return;
            }
            if (ChatMessageDao.getInstance().hasSameMessage(mLoginUserId, chatMessage.getObjectId(), chatMessage.getPacketId())) {// 本地已经保存了这条消息，不处理
                Log.e("msg_muc", "Return 5");
                return;
            }
            FriendDao.getInstance().updateChatRecordTimeOut(chatMessage.getObjectId(), Double.parseDouble(chatMessage.getContent()));
            chatMessage.setType(XmppMessage.TYPE_TIP);
            chatMessage.setContent(FLYApplication.getContext().getString(R.string.tip_group_owner_update_msg_auto_destroy_time, DateFormatUtil.timeStr(Double.parseDouble(chatMessage.getContent()))));
            if (ChatMessageDao.getInstance().saveNewSingleChatMessage(mLoginUserId, chatMessage.getObjectId(), chatMessage)) {
                ListenerManager.getInstance().notifyNewMesssage(mLoginUserId, chatMessage.getObjectId(), chatMessage, true);
            }
        }

        if (chatMessage.getFromUserId().equals(mLoginUserId) &&
                (chatMessage.getType() == XmppMessage.TYPE_IMAGE || chatMessage.getType() == XmppMessage.TYPE_VIDEO || chatMessage.getType() == XmppMessage.TYPE_FILE)) {
            Log.e("msg_muc", "多点登录，需要显示上传进度的消息");
            chatMessage.setUpload(true);
            chatMessage.setUploadSchedule(100);
        }
        if (ChatMessageDao.getInstance().saveNewSingleChatMessage(mLoginUserId, roomJid, chatMessage)) {
            Friend friend = FriendDao.getInstance().getFriend(mLoginUserId, roomJid);
            if (friend != null) {// friend == null 为直播间消息，直接跳过
                if (friend.getOfflineNoPushMsg() == 0) {

                    mService.notificationMessage(chatMessage, true);// 消息已存入本地，调用本地通知

                    if (!roomJid.equals(FLYApplication.IsRingId)
                            && !chatMessage.getFromUserId().equals(mLoginUserId)) {// 收到该消息时不处于与发送方的聊天界面 && 不是自己发送的消息
                        Log.e("msg", "群组铃声通知");
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
                    Log.e("msg", "已针对该群组开启了消息免打扰，不通知");
                }
            }

            ListenerManager.getInstance().notifyNewMesssage(mLoginUserId, roomJid, chatMessage, true);
        }
    }

    private void chatTalk(ChatMessage chatMessage) {
        if (TextUtils.equals(chatMessage.getFromUserId(), mLoginUserId)) {
            // TODO: 多点登录没有考虑，
            return;
        }
        switch (chatMessage.getType()) {
            case XmppMessage.TYPE_TALK_REQUEST:
                EventBus.getDefault().post(new MessageTalkRequestEvent(chatMessage));
                break;
            case XmppMessage.TYPE_TALK_RELEASE:
                EventBus.getDefault().post(new MessageTalkReleaseEvent(chatMessage));
                break;
            case XmppMessage.TYPE_TALK_JOIN:
                EventBus.getDefault().post(new MessageTalkJoinEvent(chatMessage));
                break;
            case XmppMessage.TYPE_TALK_LEFT:
                EventBus.getDefault().post(new MessageTalkLeftEvent(chatMessage));
                break;
            case XmppMessage.TYPE_TALK_ONLINE:
                EventBus.getDefault().post(new MessageTalkOnlineEvent(chatMessage));
                break;
        }
    }

    @SuppressLint("StringFormatMatches")
    private void chatGroup(String toUserName, ChatMessage chatMessage, Friend friend) {
        int type = chatMessage.getType();
        String fromUserId = chatMessage.getFromUserId();
        String fromUserName = chatMessage.getFromUserName();
        String toUserId = chatMessage.getToUserId();

        if (!TextUtils.isEmpty(toUserId)) {
            if (toUserId.equals(mLoginUserId)) {// 针对我的操作，只需要为fromUserName赋值
                String xF = getName(friend, fromUserId);
                if (!TextUtils.isEmpty(xF)) {
                    fromUserName = xF;
                }
            } else {// 针对其他人的操作，fromUserName与toUserName都需要赋值
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
        chatMessage.setGroup(true);
        chatMessage.setType(XmppMessage.TYPE_TIP);

        /*
        群文件
         */
        if (type == XmppMessage.TYPE_MUCFILE_DEL || type == XmppMessage.TYPE_MUCFILE_ADD) {
            String str;
            if (type == XmppMessage.TYPE_MUCFILE_DEL) {
                // str = chatMessage.getFromUserName() + " 删除了群文件 " + chatMessage.getFilePath();
                str = fromUserName + " " + FLYApplication.getInstance().getString(R.string.message_file_delete) + ":" + chatMessage.getFilePath();
            } else {
                // str = chatMessage.getFromUserName() + " 上传了群文件 " + chatMessage.getFilePath();
                str = fromUserName + " " + FLYApplication.getInstance().getString(R.string.message_file_upload) + ":" + chatMessage.getFilePath();
            }
            // 更新聊天记录表最后一条消息
            chatMessage.setContent(str);
            if (ChatMessageDao.getInstance().saveNewSingleChatMessage(mLoginUserId, friend.getUserId(), chatMessage)) {
                ListenerManager.getInstance().notifyNewMesssage(mLoginUserId, friend.getUserId(), chatMessage, true);
            }
            return;
        }

        /*
        群管理
         */
        if (type >= XmppMessage.TYPE_CHANGE_SHOW_READ && type <= XmppMessage.TYPE_GROUP_TRANSFER) {
            if (type == XmppMessage.TYPE_GROUP_VERIFY) {
                // 916协议分为两种
                // 第一种为服务端发送，触发条件为群主在群组信息内 开/关 进群验证按钮，群组内每个人都能收到
                // 第二种为邀请、申请加入该群组，由邀请人或加入方发送给群主的消息，只有群主可以收到
                if (!TextUtils.isEmpty(chatMessage.getContent()) &&
                        (chatMessage.getContent().equals("0") || chatMessage.getContent().equals("1"))) {// 第一种
                    PreferenceUtils.putBoolean(FLYApplication.getContext(),
                            Constants.IS_NEED_OWNER_ALLOW_NORMAL_INVITE_FRIEND + chatMessage.getObjectId(), chatMessage.getContent().equals("1"));
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
                    MsgBroadcast.broadcastMsgRoomUpdateGetRoomStatus(FLYApplication.getContext());
                } else if (type == XmppMessage.TYPE_GROUP_SEND_CARD) {
                    PreferenceUtils.putBoolean(FLYApplication.getContext(),
                            Constants.IS_SEND_CARD + chatMessage.getObjectId(), chatMessage.getContent().equals("1"));
                    if (chatMessage.getContent().equals("1")) {
                        chatMessage.setContent(FLYApplication.getContext().getString(R.string.tip_owner_enable_chat_privately));
                    } else {
                        chatMessage.setContent(FLYApplication.getContext().getString(R.string.tip_owner_disable_chat_privately));
                    }
                    MsgBroadcast.broadcastMsgRoomUpdateGetRoomStatus(FLYApplication.getContext());
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
                        MsgBroadcast.broadcastMsgRoomUpdateInvite(FLYApplication.getContext(), 1);
                    } else {
                        chatMessage.setContent(FLYApplication.getContext().getString(R.string.tip_owner_disable_invite));
                        MsgBroadcast.broadcastMsgRoomUpdateInvite(FLYApplication.getContext(), 0);
                    }
                } else if (type == XmppMessage.TYPE_GROUP_ALLOW_NORMAL_UPLOAD) {
                    PreferenceUtils.putBoolean(FLYApplication.getContext(),
                            Constants.IS_ALLOW_NORMAL_SEND_UPLOAD + chatMessage.getObjectId(), !chatMessage.getContent().equals("0"));
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
            return;
        }

        /*
        群内其它设置
         */
        if (type == XmppMessage.TYPE_CHANGE_NICK_NAME) { // 修改群内昵称
            String content = chatMessage.getContent();
            if (!TextUtils.isEmpty(toUserId) && toUserId.equals(mLoginUserId)) {// 我修改了昵称
                if (!TextUtils.isEmpty(content)) {
                    friend.setRoomMyNickName(content);
                    FriendDao.getInstance().updateRoomMyNickName(friend.getUserId(), content);
                    ListenerManager.getInstance().notifyNickNameChanged(friend.getUserId(), toUserId, content);
                    ChatMessageDao.getInstance().updateNickName(mLoginUserId, friend.getUserId(), toUserId, content);
                }
                // 自己改了昵称也要留一条消息，
                chatMessage.setContent(toUserName + " " + FLYApplication.getInstance().getString(R.string.message_object_update_nickname) + "‘" + content + "’");
                if (ChatMessageDao.getInstance().saveNewSingleChatMessage(mLoginUserId, friend.getUserId(), chatMessage)) {
                    ListenerManager.getInstance().notifyNewMesssage(mLoginUserId, friend.getUserId(), chatMessage, true);
                }
            } else {  // 其他人修改了昵称，通知下就可以了
                chatMessage.setContent(toUserName + " " + FLYApplication.getInstance().getString(R.string.message_object_update_nickname) + "‘" + content + "’");
                if (ChatMessageDao.getInstance().saveNewSingleChatMessage(mLoginUserId, friend.getUserId(), chatMessage)) {
                    ListenerManager.getInstance().notifyNewMesssage(mLoginUserId, friend.getUserId(), chatMessage, true);
                }
                ListenerManager.getInstance().notifyNickNameChanged(friend.getUserId(), toUserId, content);
                ChatMessageDao.getInstance().updateNickName(mLoginUserId, friend.getUserId(), toUserId, content);
            }
        } else if (type == XmppMessage.TYPE_CHANGE_ROOM_NAME) {
            // 修改房间名、更新朋友表
            String content = chatMessage.getContent();
            FriendDao.getInstance().updateMucFriendRoomName(friend.getUserId(), content);
            ListenerManager.getInstance().notifyNickNameChanged(friend.getUserId(), "ROOMNAMECHANGE", content);

            chatMessage.setContent(fromUserName + " " + FLYApplication.getInstance().getString(R.string.Message_Object_Update_RoomName) + content);
            if (ChatMessageDao.getInstance().saveNewSingleChatMessage(mLoginUserId, friend.getUserId(), chatMessage)) {
                ListenerManager.getInstance().notifyNewMesssage(mLoginUserId, friend.getUserId(), chatMessage, true);
            }
        } else if (type == XmppMessage.TYPE_DELETE_ROOM) {// 群主解散该群
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
                chatMessage.setContent(FLYApplication.getContext().getString(R.string.tip_disbanded));
                if (ChatMessageDao.getInstance().saveNewSingleChatMessage(mLoginUserId, chatMessage.getObjectId(), chatMessage)) {
                    ListenerManager.getInstance().notifyNewMesssage(mLoginUserId, chatMessage.getObjectId(), chatMessage, true);
                }
            }
            ListenerManager.getInstance().notifyDeleteMucRoom(chatMessage.getObjectId());
        } else if (type == XmppMessage.TYPE_DELETE_MEMBER) {
            // 群组 退出 || 踢人
            if (toUserId.equals(mLoginUserId)) { // 该操作为针对我的
                // Todo 针对自己消息的在XChatListener内已经处理了，为了防止加群后拉群组离线消息又拉到该条消息，针对自己的不处理
                return;
            } else {
                // 其他人退出 || 被踢出
                if (fromUserId.equals(toUserId)) {
                    chatMessage.setContent(toUserName + " " + FLYApplication.getInstance().getString(R.string.quit_group));
                } else {
                    chatMessage.setContent(toUserName + " " + FLYApplication.getInstance().getString(R.string.kicked_out_group));
                }
                // 更新RoomMemberDao、更新群聊界面
                operatingRoomMemberDao(1, friend.getRoomId(), toUserId, null);
                MsgBroadcast.broadcastMsgRoomUpdateGetRoomStatus(FLYApplication.getContext());
            }

            if (ChatMessageDao.getInstance().saveNewSingleChatMessage(mLoginUserId, friend.getUserId(), chatMessage)) {
                ListenerManager.getInstance().notifyNewMesssage(mLoginUserId, friend.getUserId(), chatMessage, true);
            }
        } else if (type == XmppMessage.TYPE_NEW_NOTICE
                || type == XmppMessage.TYPE_EDIT_GROUP_NOTICE) { // 发布公告 || 编辑
            EventBus.getDefault().post(new EventNewNotice(chatMessage));
            String content = chatMessage.getContent();
            if (type == XmppMessage.TYPE_NEW_NOTICE) {
                chatMessage.setContent(fromUserName + " " + FLYApplication.getInstance().getString(R.string.Message_Object_Add_NewAdv) + content);
            } else {
                chatMessage.setContent(fromUserName + " " + FLYApplication.getInstance().getString(R.string.edit_group_notice) + content);
            }
            if (ChatMessageDao.getInstance().saveNewSingleChatMessage(mLoginUserId, friend.getUserId(), chatMessage)) {
                ListenerManager.getInstance().notifyNewMesssage(mLoginUserId, friend.getUserId(), chatMessage, true);
            }
        } else if (type == XmppMessage.TYPE_GAG) {// 禁言
            long time = Long.parseLong(chatMessage.getContent());
            if (toUserId != null && toUserId.equals(mLoginUserId)) {
                // Todo 针对自己消息的在XChatListener内已经处理了，为了防止加群后拉群组离线消息又拉到该条消息，针对自己的不处理
            }

            // 为防止其他用户接收不及时，给3s的误差
            if (time > (System.currentTimeMillis() / 1000) + 3) {
                String formatTime = XfileUtils.fromatTime((time * 1000), "MM-dd HH:mm");
                chatMessage.setContent(fromUserName + " " + FLYApplication.getInstance().getString(R.string.message_object_yes) + toUserName +
                        FLYApplication.getInstance().getString(R.string.Message_Object_Set_Gag_With_Time) + formatTime);
            } else {
                chatMessage.setContent(toUserName + FLYApplication.getContext().getString(R.string.tip_been_cancel_ban_place_holder, fromUserName));
            }

            if (ChatMessageDao.getInstance().saveNewSingleChatMessage(mLoginUserId, friend.getUserId(), chatMessage)) {
                ListenerManager.getInstance().notifyNewMesssage(mLoginUserId, friend.getUserId(), chatMessage, true);
            }
        } else if (type == XmppMessage.NEW_MEMBER) {
            String desc = "";
            if (chatMessage.getFromUserId().equals(toUserId)) {
                // 主动加入
                desc = fromUserName + " " + FLYApplication.getInstance().getString(R.string.Message_Object_Group_Chat);
            } else {
                // 被邀请加入
                desc = fromUserName + " " + FLYApplication.getInstance().getString(R.string.message_object_inter_friend) + toUserName;

                // String roomId = jsonObject.getString("fileName");
                String roomId = chatMessage.getFilePath();
                if (!toUserId.equals(mLoginUserId)) {// 被邀请人为自己时不能更新RoomMemberDao，如更新了，在群聊界面判断出该表有人而不会在去调用接口获取该群真实的人数了
                    operatingRoomMemberDao2(0, roomId, chatMessage.getToUserId(), toUserName, fromUserId, fromUserName);
                }
            }

            // Todo 针对自己消息的在XChatListener内已经处理了，为了防止加群后拉群组离线消息又拉到该条消息，针对自己的不处理

            // 更新数据库
            chatMessage.setContent(desc);
            if (ChatMessageDao.getInstance().saveNewSingleChatMessage(mLoginUserId, chatMessage.getObjectId(), chatMessage)) {
                ListenerManager.getInstance().notifyNewMesssage(mLoginUserId, chatMessage.getObjectId(), chatMessage, true);
                MsgBroadcast.broadcastMsgRoomUpdateGetRoomStatus(FLYApplication.getContext());
            }
        } else if (type == XmppMessage.TYPE_SEND_MANAGER) {
            String content = chatMessage.getContent();
            int role;
            if (content.equals("1")) {
                role = 2;
                chatMessage.setContent(fromUserName + " " + FLYApplication.getInstance().getString(R.string.setting) + toUserName + " " + FLYApplication.getInstance().getString(R.string.message_admin));
            } else {
                role = 3;
                chatMessage.setContent(fromUserName + " " + FLYApplication.getInstance().getString(R.string.sip_canceled) + toUserName + " " + FLYApplication.getInstance().getString(R.string.message_admin));
            }

            RoomMemberDao.getInstance().updateRoomMemberRole(friend.getRoomId(), toUserId, role);
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
            String currentUserId = CoreManager.getInstance(FLYApplication.getInstance()).getSelf().getUserId();
            if (!toUserId.equals(currentUserId) && !fromUserId.equals(currentUserId)) {
                //如果设置隐身人的消息，只是更新本地群成员的状态，那么走到这里就可以了，退出
                //如果是我设置的隐身人，或者被设置为隐身人，就走后台的流程
                //删除隐人
                // 发送广播去界面更新群成员列表
                RoomMemberDao.getInstance().deleteRoomMember(friend.getRoomId(), toUserId);
                Intent intent = new Intent(OtherBroadcast.SYNC_GROUP_YINSHENREN);
                mService.sendBroadcast(intent);
                return;
            }
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

            chatMessage.setContent(FLYApplication.getContext().getString(tipContent, chatMessage.getFromUserName(), toUserName));

            RoomMemberDao.getInstance().updateRoomMemberRole(friend.getRoomId(), toUserId, role);

            chatMessage.setType(XmppMessage.TYPE_TIP);
            // chatMessage.setPacketId(UUID.randomUUID().toString().replaceAll("-", ""));
            if (ChatMessageDao.getInstance().saveNewSingleChatMessage(mLoginUserId, friend.getUserId(), chatMessage)) {
                ListenerManager.getInstance().notifyNewMesssage(mLoginUserId, friend.getUserId(), chatMessage, true);
                MsgBroadcast.broadcastMsgRoleChanged(FLYApplication.getContext());
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
}
