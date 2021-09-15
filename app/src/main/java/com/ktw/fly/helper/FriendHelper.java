package com.ktw.fly.helper;

import android.text.TextUtils;

import com.ktw.fly.FLYApplication;
import com.ktw.fly.bean.AttentionUser;
import com.ktw.fly.bean.Friend;
import com.ktw.fly.bean.User;
import com.ktw.fly.broadcast.CardcastUiUpdateUtil;
import com.ktw.fly.broadcast.MsgBroadcast;
import com.ktw.fly.db.dao.ChatMessageDao;
import com.ktw.fly.db.dao.CircleMessageDao;
import com.ktw.fly.db.dao.FriendDao;
import com.ktw.fly.sp.TableVersionSp;
import com.ktw.fly.util.TimeUtils;

/**
 * @author Dean Tao
 */
public class FriendHelper {

    public static boolean updateFriendRelationship(String loginUserId, User user) {// 更新两者的关系，因为本地数据可能不正确
        AttentionUser attentionUser = user.getFriends();
        String friendId = user.getUserId();
        boolean changed = false;
        Friend friend = FriendDao.getInstance().getFriend(loginUserId, friendId);// 本地好友
        if (attentionUser == null) {// 服务器上不存在关系
            changed = true;
            if (friend != null) {// 服务器上不存在关系，本地却有
                if (friend.getStatus() != Friend.STATUS_23) {
                    // 从好友表中改变状态
                    // 只改变状态，和被删除一致，避免在其他页面被查询出来，
                    FriendDao.getInstance().updateFriendStatus(loginUserId, friendId, Friend.STATUS_23);
                }
            } else {
                // 现在服务器上不存在的情况本地也需要有，才能在一些页面获取到数据，
                // 因为陌生人支持设置备注了，
                friend = new Friend();
                friend.setOwnerId(loginUserId);
                friend.setUserId(user.getUserId());
                friend.setRoomFlag(0);// 0朋友 1群组
                friend.setStatus(Friend.STATUS_UNKNOW);
                friend.setVersion(TableVersionSp.getInstance(FLYApplication.getInstance()).getFriendTableVersion(loginUserId));// 更新版本
                FriendDao.getInstance().createOrUpdateFriend(friend);
            }
        } else {// 服务器上存在关系
            if (friend == null) {// 本地不存在关系，那么就要插入一条好友记录
                friend = new Friend();
                friend.setOwnerId(attentionUser.getUserId());
                friend.setUserId(attentionUser.getToUserId());
                friend.setNickName(attentionUser.getToNickName());
                friend.setRemarkName(attentionUser.getRemarkName());
                friend.setDescribe(attentionUser.getDescribe());
                friend.setTimeCreate(attentionUser.getCreateTime());
                friend.setTimeSend(TimeUtils.sk_time_current_time());
                friend.setRoomFlag(0);// 0朋友 1群组
                friend.setCompanyId(attentionUser.getCompanyId());// 公司
                int status = (attentionUser.getBlacklist() == 0) ? attentionUser.getStatus() : Friend.STATUS_BLACKLIST;
                friend.setStatus(status);
                friend.setVersion(TableVersionSp.getInstance(FLYApplication.getInstance()).getFriendTableVersion(loginUserId));// 更新版本
                FriendDao.getInstance().createOrUpdateFriend(friend);

                if (status == Friend.STATUS_ATTENTION) {// 如果是关注（理论上不可能）
                } else if (status == Friend.STATUS_FRIEND) {   // 如果是好友
                    addFriendExtraOperation(loginUserId, friendId);
                }
                changed = true;
            } else {
                if (!TextUtils.equals(attentionUser.getRemarkName(), friend.getRemarkName())
                        || !TextUtils.equals(attentionUser.getDescribe(), friend.getDescribe())) {
                    FriendDao.getInstance().updateRemarkNameAndDescribe(loginUserId,
                            attentionUser.getToUserId(), user.getFriends().getRemarkName(),
                            user.getFriends().getDescribe());
                    changed = true;
                }
                int status = attentionUser.getBlacklist() == 0 ? attentionUser.getStatus() : Friend.STATUS_BLACKLIST;
                if (status == friend.getStatus()) {
                    // do no thing
                } else {
                    FriendDao.getInstance().updateFriendStatus(loginUserId, friendId, status);
                    if (status == Friend.STATUS_BLACKLIST) {// 如果之前在黑名单中，现在是STATUS_ATTENTION或者STATUS_FRIEND
                        if (friend.getStatus() == Friend.STATUS_ATTENTION) {
                        } else if (friend.getStatus() == Friend.STATUS_FRIEND) {
                            addFriendExtraOperation(loginUserId, friendId);
                        }
                    } else if (status == Friend.STATUS_ATTENTION) {// 如果之前是关注，现在是黑名单或者好友
                        if (friend.getStatus() == Friend.STATUS_BLACKLIST) {
                            addBlacklistExtraOperation(loginUserId, friendId);
                        } else if (friend.getStatus() == Friend.STATUS_FRIEND) {
                            addFriendExtraOperation(loginUserId, friendId);
                        }
                    } else if (status == Friend.STATUS_FRIEND) {
                        if (friend.getStatus() == Friend.STATUS_BLACKLIST) {
                            addBlacklistExtraOperation(loginUserId, friendId);
                        } else if (friend.getStatus() == Friend.STATUS_ATTENTION) {// 本来是好友，现在变成关注
                            // 消息表中删除
                            ChatMessageDao.getInstance().deleteMessageTable(loginUserId, friendId);
                            // 2、更新消息界面（消息界面可能之前存在和该用户的聊天记录，要删除掉）
                            MsgBroadcast.broadcastMsgUiUpdate(FLYApplication.getInstance());
                            // 3、更新主界面未读数量（消息界面可能之前存在和该用户的聊天记录，要删除掉，未读数量可能改变）
                            MsgBroadcast.broadcastMsgNumReset(FLYApplication.getInstance());
                        }
                    }
                    changed = true;
                }
            }
        }
        return changed;
    }

    /**
     * 我将对方拉黑
     * todo Friend表与NewFriendMessage表内的status已在外面更新(updateFriendRelationship方法内好像没有去改变status)，其实可以统一到这里来做，
     */
    public static void addBlacklistExtraOperation(String loginUserId, String friendId) {
        // 1、消息表中删除
        // 与ios端统一，拉黑被拉黑不删除聊天记录
        // ChatMessageDao.getInstance().deleteMessageTable(loginUserId, friendId);
        // 2、更新消息界面（消息界面可能之前存在和该用户的聊天记录，要删除掉）
        MsgBroadcast.broadcastMsgUiUpdate(FLYApplication.getInstance());
        // 3、更新主界面未读数量（消息界面可能之前存在和该用户的聊天记录，要删除掉，未读数量可能改变）
        MsgBroadcast.broadcastMsgNumReset(FLYApplication.getInstance());
        // 4、可能正在看通讯录，那么通讯录也要更新
        CardcastUiUpdateUtil.broadcastUpdateUi(FLYApplication.getInstance());
    }

    /**
     * 对方将我拉黑，不删除好友，仅改变Friend表status
     * todo NewFriendMessage表内的status已在外面更新，其实可以统一到这里来做，
     */
    public static void beBlacklistExtraOperation(String loginUserId, String friendId) {
        // 从好友表中改变状态
        FriendDao.getInstance().updateFriendStatus(loginUserId, friendId, Friend.STATUS_23);
        FriendDao.getInstance().updateFriendContent(loginUserId, friendId, null, 0, 0);

        FriendDao.getInstance().updateRemarkName(loginUserId, friendId, "");

        // 消息表中删除
        // 与ios端统一，拉黑被拉黑不删除聊天记录
        // ChatMessageDao.getInstance().deleteMessageTable(loginUserId, friendId);
        // 更新消息界面（消息界面可能之前存在和该用户的聊天记录，要删除掉）
        MsgBroadcast.broadcastMsgUiUpdate(FLYApplication.getInstance());
        // 更新主界面未读数量（消息界面可能之前存在和该用户的聊天记录，要删除掉，未读数量可能改变）
        MsgBroadcast.broadcastMsgNumReset(FLYApplication.getInstance());
        // 可能正在看通讯录，那么通讯录也要更新
        CardcastUiUpdateUtil.broadcastUpdateUi(FLYApplication.getInstance());
    }

    /**
     * 对方删除我|| 我删除对方 || 取消关注，不删除好友，仅改变Friend表status
     * todo NewFriendMessage表内的status已在外面更新，其实可以统一到这里来做，不过这个方法就要分裂为两个方法
     */
    public static void removeAttentionOrFriend(String ownerId, String friendId) {
        // 从好友表中改变状态
        FriendDao.getInstance().updateFriendStatus(ownerId, friendId, Friend.STATUS_23);
        FriendDao.getInstance().updateFriendContent(ownerId, friendId, null, 0, 0);

        FriendDao.getInstance().updateRemarkName(ownerId, friendId, "");

        // 消息表中删除
        ChatMessageDao.getInstance().deleteMessageTable(ownerId, friendId);
        // 更新消息界面（消息界面可能之前存在和该用户的聊天记录，要删除掉）
        MsgBroadcast.broadcastMsgUiUpdate(FLYApplication.getInstance());
        // 更新主界面未读数量（消息界面可能之前存在和该用户的聊天记录，要删除掉，未读数量可能改变）
        MsgBroadcast.broadcastMsgNumReset(FLYApplication.getInstance());
        // 可能正在看通讯录，那么通讯录也要更新
        CardcastUiUpdateUtil.broadcastUpdateUi(FLYApplication.getInstance());
    }

    /**
     * 好友被后台删除了
     *
     * @param ownerId
     * @param friendId
     */
    public static void friendAccountRemoved(String ownerId, String friendId) {
        // 从好友表中改变状态
        FriendDao.getInstance().deleteFriend(ownerId, friendId);
        // 消息表中删除
        ChatMessageDao.getInstance().deleteMessageTable(ownerId, friendId);
        // 商务圈消息表删除
        CircleMessageDao.getInstance().deleteMessage(ownerId, friendId);
        // 更新消息界面（消息界面可能之前存在和该用户的聊天记录，要删除掉）
        MsgBroadcast.broadcastMsgUiUpdate(FLYApplication.getInstance());
        // 更新主界面未读数量（消息界面可能之前存在和该用户的聊天记录，要删除掉，未读数量可能改变）
        MsgBroadcast.broadcastMsgNumReset(FLYApplication.getInstance());
        // 可能正在看通讯录，那么通讯录也要更新
        CardcastUiUpdateUtil.broadcastUpdateUi(FLYApplication.getInstance());
    }

    /**
     * todo 下面三个方法一模一样，仅第二个方法做了公众好的判断，且都被调用了，先不管，后面在统一
     */
    /**
     * 在本地数据库表中出入一条好友记录，调用ascensionNewFriend方法之后额外需要做的操作
     */
    public static void addFriendExtraOperation(String loginUserId, String friendId) {
        // 插入一条系统提示消息
        FriendDao.getInstance().addNewFriendInMsgTable(loginUserId, friendId);
        // 更新Message Ui
        MsgBroadcast.broadcastMsgUiUpdate(FLYApplication.getInstance());
        // 更新Main Ui message 未读数量
        MsgBroadcast.broadcastMsgNumUpdate(FLYApplication.getInstance(), true, 1);
    }

    /**
     * 在本地数据库表中出入一条好友记录，调用ascensionNewFriend方法之后额外需要做的操作
     */
    public static void addFriendExtraOperation(String loginUserId, String friendId, int userType) {
        if (userType != 2) {
            // 非公众号，插入一条系统提示消息
            FriendDao.getInstance().addNewFriendInMsgTable(loginUserId, friendId);
        }
        // 更新Message Ui
        MsgBroadcast.broadcastMsgUiUpdate(FLYApplication.getInstance());
        // 更新Main Ui message 未读数量
        MsgBroadcast.broadcastMsgNumUpdate(FLYApplication.getInstance(), true, 1);
    }

    /**
     * 在本地数据库表中出入一条好友记录，调用ascensionNewFriend方法之后额外需要做的操作
     */
    public static void beAddFriendExtraOperation(String loginUserId, String friendId) {
        FriendDao.getInstance().addNewFriendInMsgTable(loginUserId, friendId);
        // 更新Message Ui
        MsgBroadcast.broadcastMsgUiUpdate(FLYApplication.getInstance());
        // 更新Main Ui message 未读数量
        MsgBroadcast.broadcastMsgNumUpdate(FLYApplication.getInstance(), true, 1);
    }
}
