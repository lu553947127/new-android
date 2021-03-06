package com.ktw.bitbit.ui.message;

import android.content.Intent;
import android.text.TextUtils;
import android.util.Log;

import com.ktw.bitbit.FLYApplication;
import com.ktw.bitbit.bean.Friend;
import com.ktw.bitbit.bean.Label;
import com.ktw.bitbit.bean.PrivacySetting;
import com.ktw.bitbit.bean.SyncBean;
import com.ktw.bitbit.bean.User;
import com.ktw.bitbit.bean.message.ChatMessage;
import com.ktw.bitbit.bean.message.MucRoom;
import com.ktw.bitbit.bean.message.XmppMessage;
import com.ktw.bitbit.broadcast.CardcastUiUpdateUtil;
import com.ktw.bitbit.broadcast.MsgBroadcast;
import com.ktw.bitbit.broadcast.MucgroupUpdateUtil;
import com.ktw.bitbit.broadcast.OtherBroadcast;
import com.ktw.bitbit.db.dao.ChatMessageDao;
import com.ktw.bitbit.db.dao.FriendDao;
import com.ktw.bitbit.db.dao.LabelDao;
import com.ktw.bitbit.db.dao.RoomMemberDao;
import com.ktw.bitbit.helper.FriendHelper;
import com.ktw.bitbit.helper.LoginHelper;
import com.ktw.bitbit.helper.PrivacySettingHelper;
import com.ktw.bitbit.ui.account.RegisterUserBasicInfoActivity;
import com.ktw.bitbit.ui.base.CoreManager;
import com.ktw.bitbit.util.TimeUtils;
import com.ktw.bitbit.xmpp.CoreService;
import com.xuan.xuanhttplibrary.okhttp.HttpUtils;
import com.xuan.xuanhttplibrary.okhttp.callback.BaseCallback;
import com.xuan.xuanhttplibrary.okhttp.callback.ListCallback;
import com.xuan.xuanhttplibrary.okhttp.result.ArrayResult;
import com.xuan.xuanhttplibrary.okhttp.result.ObjectResult;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

import okhttp3.Call;

public class HandleSyncMoreLogin {
    private static final String TAG = "sync";
    private static final String SYNC_LOGIN_PASSWORD = "sync_login_password";
    private static final String SYNC_PAY_PASSWORD = "sync_pay_password";
    private static final String SYNC_PRIVATE_SETTINGS = "sync_private_settings";
    private static final String SYNC_LABEL = "sync_label";

    public static void distributionService(SyncBean syncBean, CoreService coreService) {
        if (syncBean.getTag().equals("label")) {
            if (RegisterUserBasicInfoActivity.isRegisteredSyncCount > 0) {
                RegisterUserBasicInfoActivity.isRegisteredSyncCount--;
                return;// ???????????????????????????????????????????????????????????????????????????off..??????????????????tag???label?????????????????????
            }
            handleLabelUpdate();
        } else if (syncBean.getTag().equals("friend")) {
            handleUserUpdate(syncBean.getFriendId());
        } else if (syncBean.getTag().equals("room")) {
            handleGroupUpdate(syncBean.getFriendId(), coreService);
        }
    }

    public static void distributionChatMessage(ChatMessage chatMessage, CoreService coreService, boolean isDelay) {
        if (chatMessage.getType() == XmppMessage.TYPE_SYNC_OTHER) {
            if (!TextUtils.isEmpty(chatMessage.getObjectId())) {
                if (chatMessage.getObjectId().equals(SYNC_LOGIN_PASSWORD)) {
                    if (!isDelay) {
                        handleChangeLoginPassword();
                    }
                } else if (chatMessage.getObjectId().equals(SYNC_PAY_PASSWORD)) {
                    handleSetPayPassword();
                } else if (chatMessage.getObjectId().equals(SYNC_PRIVATE_SETTINGS)) {
                    handlePrivateSettingsUpdate();
                } else if (chatMessage.getObjectId().equals(SYNC_LABEL)) {
                    if (RegisterUserBasicInfoActivity.isRegisteredSyncCount > 0) {
                        RegisterUserBasicInfoActivity.isRegisteredSyncCount--;
                        return;// ??????????????????????????????????????????????????????????????????????????????????????????????????????????????????
                    }
                    handleLabelUpdate();
                }
            }
        } else if (chatMessage.getType() == XmppMessage.TYPE_SYNC_FRIEND) {
            handleUserUpdate(chatMessage.getToUserId());
        } else if (chatMessage.getType() == XmppMessage.TYPE_SYNC_GROUP) {
            handleGroupUpdate(chatMessage.getToUserId(), coreService);
        }
    }

    private static void handleChangeLoginPassword() {
        Log.e(TAG, "??????????????????--->????????????????????????????????????");
        FLYApplication.getInstance().mUserStatus = LoginHelper.STATUS_USER_TOKEN_OVERDUE;
        LoginHelper.broadcastLogout(FLYApplication.getContext());
    }

    private static void handleSetPayPassword() {
        Log.e(TAG, "??????????????????--->??????????????????????????????????????????");
        Map<String, String> params = new HashMap<>();
        params.put("access_token", CoreManager.requireSelfStatus(FLYApplication.getContext()).accessToken);
        params.put("userId", CoreManager.requireSelf(FLYApplication.getContext()).getUserId());

        HttpUtils.get().url(CoreManager.requireConfig(FLYApplication.getContext()).USER_GET_URL)
                .params(params)
                .build()
                .execute(new BaseCallback<User>(User.class) {
                    @Override
                    public void onResponse(ObjectResult<User> result) {
                        if (result.getResultCode() == 1 && result.getData() != null) {
                            Log.e(TAG, "??????????????????--->???????????????????????????");
                            User user = result.getData();
                            FLYApplication.getInstance().initPayPassword(user.getUserId(),
                                    TextUtils.equals(user.getPayPassword(), "1") ? 1 : 0);
                        } else {
                            Log.e(TAG, "??????????????????--->????????????????????????????????????????????????");
                        }
                    }

                    @Override
                    public void onError(Call call, Exception e) {
                        Log.e(TAG, "??????????????????--->????????????????????????????????????????????????");
                    }
                });
    }

    private static void handlePrivateSettingsUpdate() {
        Log.e(TAG, "??????????????????--->??????????????????");
        Map<String, String> params = new HashMap<>();
        params.put("access_token", CoreManager.requireSelfStatus(FLYApplication.getContext()).accessToken);
        params.put("userId", CoreManager.requireSelf(FLYApplication.getContext()).getUserId());

        HttpUtils.get().url(CoreManager.requireConfig(FLYApplication.getContext()).USER_GET_PRIVACY_SETTING)
                .params(params)
                .build()
                .execute(new BaseCallback<PrivacySetting>(PrivacySetting.class) {

                    @Override
                    public void onResponse(ObjectResult<PrivacySetting> result) {
                        if (result.getResultCode() == 1 && result.getData() != null) {
                            Log.e(TAG, "??????????????????--->??????????????????");
                            PrivacySetting settings = result.getData();
                            PrivacySettingHelper.setPrivacySettings(FLYApplication.getContext(), settings);
                        } else {
                            Log.e(TAG, "??????????????????--->??????????????????");
                        }
                    }

                    @Override
                    public void onError(Call call, Exception e) {
                        Log.e(TAG, "??????????????????--->??????????????????");
                    }
                });
    }

    private static void handleLabelUpdate() {
        Log.e(TAG, "??????????????????--->????????????");
        Map<String, String> params = new HashMap<>();
        params.put("access_token", CoreManager.requireSelfStatus(FLYApplication.getContext()).accessToken);

        HttpUtils.get().url(CoreManager.requireConfig(FLYApplication.getContext()).FRIENDGROUP_LIST)
                .params(params)
                .build()
                .execute(new ListCallback<Label>(Label.class) {
                    @Override
                    public void onResponse(ArrayResult<Label> result) {
                        if (result.getResultCode() == 1 && result.getData() != null) {
                            Log.e(TAG, "??????????????????--->??????????????????");
                            List<Label> labelList = result.getData();
                            LabelDao.getInstance().refreshLabel(CoreManager.requireSelf(FLYApplication.getContext()).getUserId(), labelList);
                        } else {
                            Log.e(TAG, "??????????????????--->??????????????????");
                        }
                    }

                    @Override
                    public void onError(Call call, Exception e) {
                        Log.e(TAG, "??????????????????--->??????????????????");
                    }
                });
    }

    private static void handleUserUpdate(String userId) {
        if (TextUtils.equals(userId, CoreManager.requireSelf(FLYApplication.getContext()).getUserId())) {
            handleSelfUpdate();
        } else {
            handleFriendUpdate(userId);
        }
    }

    private static void handleSelfUpdate() {
        Log.e(TAG, "??????????????????--->?????????????????????");
        FLYApplication.getContext().sendBroadcast(new Intent(OtherBroadcast.SYNC_SELF_DATE));
    }

    private static void handleFriendUpdate(String userId) {
        Log.e(TAG, "??????????????????--->?????????????????????");
        Map<String, String> params = new HashMap<>();
        params.put("access_token", CoreManager.requireSelfStatus(FLYApplication.getContext()).accessToken);
        params.put("userId", userId);

        HttpUtils.get().url(CoreManager.requireConfig(FLYApplication.getContext()).USER_GET_URL)
                .params(params)
                .build()
                .execute(new BaseCallback<User>(User.class) {
                    @Override
                    public void onResponse(ObjectResult<User> result) {
                        if (result.getResultCode() == 1 && result.getData() != null) {
                            Log.e(TAG, "??????????????????--->???????????????????????????");
                            User user = result.getData();
                            // ?????????????????? ?????????????????????
                            if (FriendHelper.updateFriendRelationship(CoreManager.requireSelf(FLYApplication.getContext()).getUserId(), user)) {
                                CardcastUiUpdateUtil.broadcastUpdateUi(FLYApplication.getContext());
                            }
                            if (user.getFriends() != null) {// ??????????????????????????? && ????????????????????????...
                                boolean isTopOld = false;
                                boolean isTopNew = false;
                                Friend friend = FriendDao.getInstance().getFriend(CoreManager.requireSelf(FLYApplication.getContext()).getUserId(), userId);
                                if (friend != null) {
                                    isTopOld = friend.getTopTime() > 0;
                                    isTopNew = user.getFriends().getOpenTopChatTime() > 0;
                                }

                                FriendDao.getInstance().updateFriendPartStatus(userId, user);
                                if (!TextUtils.equals(String.valueOf(isTopOld), String.valueOf(isTopNew))) {
                                    // ??????????????????????????????????????????
                                    MsgBroadcast.broadcastMsgUiUpdate(FLYApplication.getContext());
                                }
                            }
                        } else {
                            Log.e(TAG, "??????????????????--->???????????????????????????");
                        }
                    }

                    @Override
                    public void onError(Call call, Exception e) {
                        Log.e(TAG, "??????????????????--->???????????????????????????");
                    }
                });
    }

    private static void handleGroupUpdate(String roomId, CoreService coreService) {
        Log.e(TAG, "??????????????????--->?????????????????????");
        String loginUserId = CoreManager.requireSelf(FLYApplication.getContext()).getUserId();
        Friend friend = FriendDao.getInstance().getMucFriendByRoomId(loginUserId, roomId);

        Map<String, String> params = new HashMap<>();
        params.put("access_token", CoreManager.requireSelfStatus(FLYApplication.getContext()).accessToken);
        params.put("roomId", roomId);

        HttpUtils.get().url(CoreManager.requireConfig(FLYApplication.getContext()).ROOM_GET_ROOM)
                .params(params)
                .build()
                .execute(new BaseCallback<MucRoom>(MucRoom.class) {

                             @Override
                             public void onResponse(ObjectResult<MucRoom> result) {// ???????????????room/get??????????????????????????????????????????????????????????????????
                                 Log.e(TAG, "??????????????????--->???????????????????????????");
                                 if (result.getResultCode() == 1 && result.getData() != null) {
                                     final MucRoom mucRoom = result.getData();
                                     if (mucRoom.getMember() == null) {// ???????????????
                                         if (friend != null) {
                                             delete(loginUserId, friend, coreService);
                                         }
                                     } else {
                                         if (friend == null) {// ??????
                                             create(loginUserId, mucRoom, coreService);
                                         } else {// ??????
                                             update(friend, mucRoom);
                                         }
                                     }
                                 } else {// ?????????????????????
                                     delete(loginUserId, friend, coreService);
                                 }
                             }

                             @Override
                             public void onError(Call call, Exception e) {
                                 Log.e(TAG, "??????????????????--->???????????????????????????");
                             }
                         }
                );
    }

    private static void create(String userId, MucRoom mucRoom, CoreService coreService) {
        Friend friend = new Friend();
        friend.setOwnerId(userId);
        friend.setUserId(mucRoom.getJid());
        friend.setNickName(mucRoom.getName());
        friend.setDescription(mucRoom.getDesc());
        friend.setRoomId(mucRoom.getId());
        friend.setContent("");
        friend.setTimeSend(mucRoom.getMember().getCreateTime());
        friend.setRoomFlag(1);
        friend.setStatus(Friend.STATUS_FRIEND);
        friend.setGroupStatus(0);

        if (mucRoom.getMember() != null) {
            friend.setOfflineNoPushMsg(mucRoom.getMember().getOfflineNoPushMsg());
            friend.setTopTime(mucRoom.getMember().getOpenTopChatTime());
        }

        FriendDao.getInstance().createOrUpdateFriend(friend);

        // ??????smack?????????????????????
        coreService.joinMucChat(mucRoom.getJid(), TimeUtils.sk_time_current_time() - mucRoom.getMember().getCreateTime());
        MsgBroadcast.broadcastMsgUiUpdate(FLYApplication.getContext());
        MucgroupUpdateUtil.broadcastUpdateUi(FLYApplication.getContext());
    }

    private static void delete(String userId, Friend friend, CoreService coreService) {
        if (friend != null) {
            coreService.exitMucChat(friend.getUserId());
            FriendDao.getInstance().deleteFriend(userId, friend.getUserId());
            // ??????????????????
            ChatMessageDao.getInstance().deleteMessageTable(userId, friend.getUserId());
            RoomMemberDao.getInstance().deleteRoomMemberTable(friend.getUserId());
            // ??????????????????
            MsgBroadcast.broadcastMsgNumReset(FLYApplication.getContext());
            MsgBroadcast.broadcastMsgUiUpdate(FLYApplication.getContext());
            MucgroupUpdateUtil.broadcastUpdateUi(FLYApplication.getContext());
        }
    }

    /**
     * update ?????????????????????????????????????????????
     */
    private static void update(Friend friend, MucRoom mucRoom) {
        if (mucRoom.getMember() != null) {
            FriendDao.getInstance().updateOfflineNoPushMsgStatus(mucRoom.getJid(), mucRoom.getMember().getOfflineNoPushMsg());
            if (mucRoom.getMember().getOpenTopChatTime() > 0) {
                FriendDao.getInstance().updateTopFriend(mucRoom.getJid(), mucRoom.getMember().getOpenTopChatTime());
            } else {
                FriendDao.getInstance().resetTopFriend(mucRoom.getJid());
            }

            boolean isTopOld = friend.getTopTime() > 0;
            boolean isTopNew = mucRoom.getMember().getOpenTopChatTime() > 0;
            if (!TextUtils.equals(String.valueOf(isTopOld), String.valueOf(isTopNew))) {
                // ??????????????????????????????????????????
                MsgBroadcast.broadcastMsgUiUpdate(FLYApplication.getContext());
            }
        }
    }
}
