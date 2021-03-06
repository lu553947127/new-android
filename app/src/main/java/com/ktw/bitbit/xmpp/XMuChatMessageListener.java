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
     * ??????????????????????????????(??????)
     */
    private void saveGroupMessage(ChatMessage chatMessage, String roomJid, boolean isDelay) {
        String packetId = chatMessage.getPacketId();

        if (TextUtils.equals(chatMessage.getFromUserId(), mLoginUserId)
                && chatMessage.getType() == XmppMessage.TYPE_READ
                && TextUtils.isEmpty(chatMessage.getFromUserName())) {
            chatMessage.setFromUserName(CoreManager.requireSelf(mService).getNickName());
        }

        ChatMessageDao.getInstance().decryptDES(chatMessage);// ??????
        int type = chatMessage.getType();
        chatMessage.setGroup(true);
        chatMessage.setMessageState(ChatMessageListener.MESSAGE_SEND_SUCCESS);

        Log.e("msg", "??????????????????" + chatMessage.toString());

        // ??????????????????
        if (isDelay) {
            if (chatMessage.isExpired()) {// ???????????????????????????????????????????????????Return ????????????
                Log.e("msg_muc", "// ???????????????????????????????????????????????????Return ????????????");
                chatMessage.setIsExpired(1);
                ChatMessageDao.getInstance().saveNewSingleChatMessage(mLoginUserId, roomJid, chatMessage);
                return;
            }
            // ???????????? ???????????????????????????????????????????????????100???????????????100?????????????????????????????????endTime????????????????????????????????????
            // ???????????? ?????????????????????????????????msgId?????????????????????startMsgId ??????????????????100???
            MsgRoamTask mLastMsgRoamTask = MsgRoamTaskDao.getInstance().getFriendLastMsgRoamTask(mLoginUserId, roomJid); // ?????????????????????????????????
            if (mLastMsgRoamTask == null) {
            } else if (mLastMsgRoamTask.getEndTime() == 0) {// ???????????????EndTime?????? ???????????????????????????
                MsgRoamTaskDao.getInstance().updateMsgRoamTaskEndTime(mLoginUserId, roomJid, mLastMsgRoamTask.getTaskId(), chatMessage.getTimeSend());
            } else if (packetId.equals(mLastMsgRoamTask.getStartMsgId())) {
                MsgRoamTaskDao.getInstance().deleteMsgRoamTask(mLoginUserId, roomJid, mLastMsgRoamTask.getTaskId());
            }
        }

        boolean isShieldGroupMsg = PreferenceUtils.getBoolean(FLYApplication.getContext(), Constants.SHIELD_GROUP_MSG + roomJid + mLoginUserId, false);
        if (isShieldGroupMsg) {// ?????????
            return;
        }

        if (type == XmppMessage.TYPE_TEXT
                && !TextUtils.isEmpty(chatMessage.getObjectId())) {// ?????????@??????
            Friend friend = FriendDao.getInstance().getFriend(mLoginUserId, roomJid);
            if (friend != null) {
                if (friend.getIsAtMe() == 0
                        && !TextUtils.equals(FLYApplication.IsRingId, roomJid)) {// ?????????@?????? && ?????????????????????????????????????????????????????????
                    if (chatMessage.getObjectId().equals(roomJid)) {// @????????????
                        FriendDao.getInstance().updateAtMeStatus(roomJid, 2);
                    } else if (chatMessage.getObjectId().contains(mLoginUserId)) {// @???
                        FriendDao.getInstance().updateAtMeStatus(roomJid, 1);
                    }
                }
            }
        }

        // ?????????
        if (type == XmppMessage.TYPE_READ) {
            packetId = chatMessage.getContent();
            ChatMessage chat = ChatMessageDao.getInstance().findMsgById(mLoginUserId, roomJid, packetId);
            if (chat != null) {
                String fromUserId = chatMessage.getFromUserId();
                boolean repeat = ChatMessageDao.getInstance().checkRepeatRead(mLoginUserId, roomJid, fromUserId, packetId);
                if (!repeat) {
                    int count = chat.getReadPersons();// ????????????+1
                    chat.setReadPersons(count + 1);
                    // ??????????????????
                    chat.setReadTime(chatMessage.getTimeSend());
                    // ??????????????????
                    ChatMessageDao.getInstance().updateMessageRead(mLoginUserId, roomJid, chat);
                    // ???????????????
                    ChatMessageDao.getInstance().saveNewSingleChatMessage(mLoginUserId, roomJid, chatMessage);
                    // ????????????
                    MsgBroadcast.broadcastMsgReadUpdate(FLYApplication.getInstance(), packetId);
                }
            }
            return;
        }

        // ???????????????????????????
        if (type == XmppMessage.TYPE_83) {
            String fromUserId = chatMessage.getFromUserId();// ???????????????
            String toUserId = chatMessage.getToUserId();// ???????????????
            String fromName;
            String toName;
            if (TextUtils.equals(fromUserId, mLoginUserId) && TextUtils.equals(toUserId, mLoginUserId)) {// ??????????????????????????????
                fromName = FLYApplication.getContext().getString(R.string.you);
                toName = FLYApplication.getContext().getString(R.string.self);
            } else if (TextUtils.equals(toUserId, mLoginUserId)) {// xx?????????????????????
                fromName = chatMessage.getFromUserName();
                toName = FLYApplication.getContext().getString(R.string.you);
            } else if (TextUtils.equals(fromUserId, mLoginUserId)) {//????????????xx?????????
                fromName = FLYApplication.getContext().getString(R.string.you);
                // toName = RoomMemberDao.getInstance().getRoomMemberName(chatMessage.getObjectId(), chatMessage.getFromUserId());
                toName = chatMessage.getToUserName();
            } else {// xx?????????xx?????????
                fromName = chatMessage.getFromUserName();
                // toName = RoomMemberDao.getInstance().getRoomMemberName(chatMessage.getObjectId(), chatMessage.getFromUserId());
                toName = chatMessage.getToUserName();
            }

            String hasBennReceived = "";
            if (chatMessage.getFileSize() == 1) {// ??????????????????
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

            // ????????????????????????????????? ??????????????????????????????????????????type???id?????????????????????
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

        // ?????????????????????130-139
        if (type >= XmppMessage.TYPE_IS_MU_CONNECT_TALK && type <= XmppMessage.TYPE_TALK_KICK) {
            chatTalk(chatMessage);
            return;
        }

        // ????????????
        if (type == XmppMessage.TYPE_BACK) {
            // ?????????????????????
            packetId = chatMessage.getContent();
            if (chatMessage.getFromUserId().equals(mLoginUserId)) {// ????????????????????????
                ChatMessageDao.getInstance().updateMessageBack(mLoginUserId, roomJid, packetId, FLYApplication.getContext().getString(R.string.you));
            } else {
                ChatMessageDao.getInstance().updateMessageBack(mLoginUserId, roomJid, packetId, chatMessage.getFromUserName(), chatMessage.getFromUserId());
            }

            Intent intent = new Intent();
            intent.putExtra("packetId", packetId);
            intent.setAction(com.ktw.bitbit.broadcast.OtherBroadcast.MSG_BACK);
            mService.sendBroadcast(intent);

            // ??????UI??????
            ChatMessage chat = ChatMessageDao.getInstance().getLastChatMessage(mLoginUserId, roomJid);
            if (chat != null) {
                if (chat.getPacketId().equals(packetId)) {
                    // ??????????????????????????????????????????????????????
                    if (chatMessage.getFromUserId().equals(mLoginUserId)) {// ????????????????????????
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
            if (ChatMessageDao.getInstance().hasSameMessage(mLoginUserId, chatMessage.getObjectId(), chatMessage.getPacketId())) {// ?????????????????????????????????????????????
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
            if (ChatMessageDao.getInstance().hasSameMessage(mLoginUserId, chatMessage.getObjectId(), chatMessage.getPacketId())) {// ?????????????????????????????????????????????
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
            Log.e("msg_muc", "????????????????????????????????????????????????");
            chatMessage.setUpload(true);
            chatMessage.setUploadSchedule(100);
        }
        if (ChatMessageDao.getInstance().saveNewSingleChatMessage(mLoginUserId, roomJid, chatMessage)) {
            Friend friend = FriendDao.getInstance().getFriend(mLoginUserId, roomJid);
            if (friend != null) {// friend == null ?????????????????????????????????
                if (friend.getOfflineNoPushMsg() == 0) {

                    mService.notificationMessage(chatMessage, true);// ??????????????????????????????????????????

                    if (!roomJid.equals(FLYApplication.IsRingId)
                            && !chatMessage.getFromUserId().equals(mLoginUserId)) {// ?????????????????????????????????????????????????????? && ???????????????????????????
                        Log.e("msg", "??????????????????");
/*
                        if (!MessageFragment.foreground) {
                            // ????????????????????????
                            NoticeVoicePlayer.getInstance().start();
                        }
*/
                        // ???ios????????????
                        NoticeVoicePlayer.getInstance().start();
                    }
                } else {
                    Log.e("msg", "??????????????????????????????????????????????????????");
                }
            }

            ListenerManager.getInstance().notifyNewMesssage(mLoginUserId, roomJid, chatMessage, true);
        }
    }

    private void chatTalk(ChatMessage chatMessage) {
        if (TextUtils.equals(chatMessage.getFromUserId(), mLoginUserId)) {
            // TODO: ???????????????????????????
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
            if (toUserId.equals(mLoginUserId)) {// ?????????????????????????????????fromUserName??????
                String xF = getName(friend, fromUserId);
                if (!TextUtils.isEmpty(xF)) {
                    fromUserName = xF;
                }
            } else {// ???????????????????????????fromUserName???toUserName???????????????
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
        ?????????
         */
        if (type == XmppMessage.TYPE_MUCFILE_DEL || type == XmppMessage.TYPE_MUCFILE_ADD) {
            String str;
            if (type == XmppMessage.TYPE_MUCFILE_DEL) {
                // str = chatMessage.getFromUserName() + " ?????????????????? " + chatMessage.getFilePath();
                str = fromUserName + " " + FLYApplication.getInstance().getString(R.string.message_file_delete) + ":" + chatMessage.getFilePath();
            } else {
                // str = chatMessage.getFromUserName() + " ?????????????????? " + chatMessage.getFilePath();
                str = fromUserName + " " + FLYApplication.getInstance().getString(R.string.message_file_upload) + ":" + chatMessage.getFilePath();
            }
            // ???????????????????????????????????????
            chatMessage.setContent(str);
            if (ChatMessageDao.getInstance().saveNewSingleChatMessage(mLoginUserId, friend.getUserId(), chatMessage)) {
                ListenerManager.getInstance().notifyNewMesssage(mLoginUserId, friend.getUserId(), chatMessage, true);
            }
            return;
        }

        /*
        ?????????
         */
        if (type >= XmppMessage.TYPE_CHANGE_SHOW_READ && type <= XmppMessage.TYPE_GROUP_TRANSFER) {
            if (type == XmppMessage.TYPE_GROUP_VERIFY) {
                // 916??????????????????
                // ????????????????????????????????????????????????????????????????????? ???/??? ???????????????????????????????????????????????????
                // ????????????????????????????????????????????????????????????????????????????????????????????????????????????????????????
                if (!TextUtils.isEmpty(chatMessage.getContent()) &&
                        (chatMessage.getContent().equals("0") || chatMessage.getContent().equals("1"))) {// ?????????
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
                } else {//  ???????????????????????? ????????????????????? ?????????????????????????????? ????????????
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
        ??????????????????
         */
        if (type == XmppMessage.TYPE_CHANGE_NICK_NAME) { // ??????????????????
            String content = chatMessage.getContent();
            if (!TextUtils.isEmpty(toUserId) && toUserId.equals(mLoginUserId)) {// ??????????????????
                if (!TextUtils.isEmpty(content)) {
                    friend.setRoomMyNickName(content);
                    FriendDao.getInstance().updateRoomMyNickName(friend.getUserId(), content);
                    ListenerManager.getInstance().notifyNickNameChanged(friend.getUserId(), toUserId, content);
                    ChatMessageDao.getInstance().updateNickName(mLoginUserId, friend.getUserId(), toUserId, content);
                }
                // ??????????????????????????????????????????
                chatMessage.setContent(toUserName + " " + FLYApplication.getInstance().getString(R.string.message_object_update_nickname) + "???" + content + "???");
                if (ChatMessageDao.getInstance().saveNewSingleChatMessage(mLoginUserId, friend.getUserId(), chatMessage)) {
                    ListenerManager.getInstance().notifyNewMesssage(mLoginUserId, friend.getUserId(), chatMessage, true);
                }
            } else {  // ????????????????????????????????????????????????
                chatMessage.setContent(toUserName + " " + FLYApplication.getInstance().getString(R.string.message_object_update_nickname) + "???" + content + "???");
                if (ChatMessageDao.getInstance().saveNewSingleChatMessage(mLoginUserId, friend.getUserId(), chatMessage)) {
                    ListenerManager.getInstance().notifyNewMesssage(mLoginUserId, friend.getUserId(), chatMessage, true);
                }
                ListenerManager.getInstance().notifyNickNameChanged(friend.getUserId(), toUserId, content);
                ChatMessageDao.getInstance().updateNickName(mLoginUserId, friend.getUserId(), toUserId, content);
            }
        } else if (type == XmppMessage.TYPE_CHANGE_ROOM_NAME) {
            // ?????????????????????????????????
            String content = chatMessage.getContent();
            FriendDao.getInstance().updateMucFriendRoomName(friend.getUserId(), content);
            ListenerManager.getInstance().notifyNickNameChanged(friend.getUserId(), "ROOMNAMECHANGE", content);

            chatMessage.setContent(fromUserName + " " + FLYApplication.getInstance().getString(R.string.Message_Object_Update_RoomName) + content);
            if (ChatMessageDao.getInstance().saveNewSingleChatMessage(mLoginUserId, friend.getUserId(), chatMessage)) {
                ListenerManager.getInstance().notifyNewMesssage(mLoginUserId, friend.getUserId(), chatMessage, true);
            }
        } else if (type == XmppMessage.TYPE_DELETE_ROOM) {// ??????????????????
            if (fromUserId.equals(toUserId)) {
                // ????????????
                FriendDao.getInstance().deleteFriend(mLoginUserId, chatMessage.getObjectId());
                // ??????????????????
                ChatMessageDao.getInstance().deleteMessageTable(mLoginUserId, chatMessage.getObjectId());
                RoomMemberDao.getInstance().deleteRoomMemberTable(chatMessage.getObjectId());
                // ??????????????????
                MsgBroadcast.broadcastMsgNumReset(mService);
                MsgBroadcast.broadcastMsgUiUpdate(FLYApplication.getInstance());
                MucgroupUpdateUtil.broadcastUpdateUi(mService);
            } else {
                mService.exitMucChat(chatMessage.getObjectId());
                // 2 ????????????????????????  ???????????????
                FriendDao.getInstance().updateFriendGroupStatus(mLoginUserId, friend.getUserId(), 2);
                chatMessage.setContent(FLYApplication.getContext().getString(R.string.tip_disbanded));
                if (ChatMessageDao.getInstance().saveNewSingleChatMessage(mLoginUserId, chatMessage.getObjectId(), chatMessage)) {
                    ListenerManager.getInstance().notifyNewMesssage(mLoginUserId, chatMessage.getObjectId(), chatMessage, true);
                }
            }
            ListenerManager.getInstance().notifyDeleteMucRoom(chatMessage.getObjectId());
        } else if (type == XmppMessage.TYPE_DELETE_MEMBER) {
            // ?????? ?????? || ??????
            if (toUserId.equals(mLoginUserId)) { // ????????????????????????
                // Todo ????????????????????????XChatListener???????????????????????????????????????????????????????????????????????????????????????????????????????????????
                return;
            } else {
                // ??????????????? || ?????????
                if (fromUserId.equals(toUserId)) {
                    chatMessage.setContent(toUserName + " " + FLYApplication.getInstance().getString(R.string.quit_group));
                } else {
                    chatMessage.setContent(toUserName + " " + FLYApplication.getInstance().getString(R.string.kicked_out_group));
                }
                // ??????RoomMemberDao?????????????????????
                operatingRoomMemberDao(1, friend.getRoomId(), toUserId, null);
                MsgBroadcast.broadcastMsgRoomUpdateGetRoomStatus(FLYApplication.getContext());
            }

            if (ChatMessageDao.getInstance().saveNewSingleChatMessage(mLoginUserId, friend.getUserId(), chatMessage)) {
                ListenerManager.getInstance().notifyNewMesssage(mLoginUserId, friend.getUserId(), chatMessage, true);
            }
        } else if (type == XmppMessage.TYPE_NEW_NOTICE
                || type == XmppMessage.TYPE_EDIT_GROUP_NOTICE) { // ???????????? || ??????
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
        } else if (type == XmppMessage.TYPE_GAG) {// ??????
            long time = Long.parseLong(chatMessage.getContent());
            if (toUserId != null && toUserId.equals(mLoginUserId)) {
                // Todo ????????????????????????XChatListener???????????????????????????????????????????????????????????????????????????????????????????????????????????????
            }

            // ??????????????????????????????????????????3s?????????
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
                // ????????????
                desc = fromUserName + " " + FLYApplication.getInstance().getString(R.string.Message_Object_Group_Chat);
            } else {
                // ???????????????
                desc = fromUserName + " " + FLYApplication.getInstance().getString(R.string.message_object_inter_friend) + toUserName;

                // String roomId = jsonObject.getString("fileName");
                String roomId = chatMessage.getFilePath();
                if (!toUserId.equals(mLoginUserId)) {// ????????????????????????????????????RoomMemberDao???????????????????????????????????????????????????????????????????????????????????????????????????????????????
                    operatingRoomMemberDao2(0, roomId, chatMessage.getToUserId(), toUserName, fromUserId, fromUserName);
                }
            }

            // Todo ????????????????????????XChatListener???????????????????????????????????????????????????????????????????????????????????????????????????????????????

            // ???????????????
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
                //???????????????????????????????????????????????????????????????????????????????????????????????????????????????
                //????????????????????????????????????????????????????????????????????????????????????
                //????????????
                // ??????????????????????????????????????????
                RoomMemberDao.getInstance().deleteRoomMember(friend.getRoomId(), toUserId);
                Intent intent = new Intent(OtherBroadcast.SYNC_GROUP_YINSHENREN);
                mService.sendBroadcast(intent);
                return;
            }
            int tipContent = -1;
            int role = RoomMember.ROLE_MEMBER;
            switch (chatMessage.getContent()) {
                case "1": // 1:???????????????
                    tipContent = R.string.tip_set_invisible_place_holder;
                    role = RoomMember.ROLE_INVISIBLE;
                    break;
                case "-1": // -1:???????????????
                    tipContent = R.string.tip_cancel_invisible_place_holder;
                    break;
                case "2": // 2??????????????????
                    tipContent = R.string.tip_set_guardian_place_holder;
                    role = RoomMember.ROLE_GUARDIAN;
                    break;
                case "0": // 0??????????????????
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
        if (mRoomMember != null && mRoomMember.getRole() == 1) {// ???????????? Name?????????????????????
            RoomMember member = RoomMemberDao.getInstance().getSingleRoomMember(friend.getRoomId(), userId);
            if (member != null && !TextUtils.equals(member.getUserName(), member.getCardName())) {
                // ???userName???cardName??????????????????????????????????????????????????????
                return member.getCardName();
            } else {
                Friend mFriend = FriendDao.getInstance().getFriend(mLoginUserId, userId);
                if (mFriend != null && !TextUtils.isEmpty(mFriend.getRemarkName())) {
                    return mFriend.getRemarkName();
                }
            }
        } else {// ????????? ????????????
            Friend mFriend = FriendDao.getInstance().getFriend(mLoginUserId, userId);
            if (mFriend != null && !TextUtils.isEmpty(mFriend.getRemarkName())) {
                return mFriend.getRemarkName();
            }
        }
        return null;
    }

    // ??????????????????
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

    // ??????????????????
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
