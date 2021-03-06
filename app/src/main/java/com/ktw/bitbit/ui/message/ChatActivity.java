package com.ktw.bitbit.ui.message;

import android.annotation.SuppressLint;
import android.app.Activity;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.net.Uri;
import android.os.Bundle;
import android.os.CountDownTimer;
import android.os.Environment;
import android.os.Handler;
import android.os.Vibrator;
import android.text.TextUtils;
import android.util.Log;
import android.view.Gravity;
import android.view.MotionEvent;
import android.view.View;
import android.view.WindowManager;
import android.view.animation.Animation;
import android.view.animation.AnimationUtils;
import android.view.inputmethod.InputMethodManager;
import android.widget.AbsListView;
import android.widget.ImageView;
import android.widget.ListView;
import android.widget.TextView;
import android.widget.Toast;

import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.JSONObject;
import com.google.gson.Gson;
import com.ktw.bitbit.FLYAppConstant;
import com.ktw.bitbit.FLYApplication;
import com.ktw.bitbit.FLYReporter;
import com.ktw.bitbit.R;
import com.ktw.bitbit.audio1.VoiceManager;
import com.ktw.bitbit.audio1.VoicePlayer;
import com.ktw.bitbit.bean.AutoAnswerBean;
import com.ktw.bitbit.bean.Contacts;
import com.ktw.bitbit.bean.Friend;
import com.ktw.bitbit.bean.PrivacySetting;
import com.ktw.bitbit.bean.PublicMenu;
import com.ktw.bitbit.bean.User;
import com.ktw.bitbit.bean.VideoFile;
import com.ktw.bitbit.bean.assistant.GroupAssistantDetail;
import com.ktw.bitbit.bean.collection.CollectionEvery;
import com.ktw.bitbit.bean.event.EventClickProblem;
import com.ktw.bitbit.bean.event.EventNotifyByTag;
import com.ktw.bitbit.bean.event.EventSyncFriendOperating;
import com.ktw.bitbit.bean.event.EventTransfer;
import com.ktw.bitbit.bean.event.EventUploadCancel;
import com.ktw.bitbit.bean.event.EventUploadFileRate;
import com.ktw.bitbit.bean.event.MessageEventClickable;
import com.ktw.bitbit.bean.event.MessageEventRequert;
import com.ktw.bitbit.bean.event.MessageLocalVideoFile;
import com.ktw.bitbit.bean.event.MessageUploadChatRecord;
import com.ktw.bitbit.bean.event.MessageVideoFile;
import com.ktw.bitbit.bean.message.ChatMessage;
import com.ktw.bitbit.bean.message.ChatRecord;
import com.ktw.bitbit.bean.message.XmppMessage;
import com.ktw.bitbit.bean.redpacket.EventRedReceived;
import com.ktw.bitbit.bean.redpacket.OpenRedpacket;
import com.ktw.bitbit.bean.redpacket.RedDialogBean;
import com.ktw.bitbit.bean.redpacket.RedPacketResult;
import com.ktw.bitbit.bean.redpacket.RushRedPacket;
import com.ktw.bitbit.broadcast.MsgBroadcast;
import com.ktw.bitbit.call.Jitsi_connecting_second;
import com.ktw.bitbit.call.Jitsi_pre;
import com.ktw.bitbit.call.MessageEventClicAudioVideo;
import com.ktw.bitbit.call.MessageEventSipEVent;
import com.ktw.bitbit.call.MessageEventSipPreview;
import com.ktw.bitbit.db.dao.ChatMessageDao;
import com.ktw.bitbit.db.dao.FriendDao;
import com.ktw.bitbit.db.dao.VideoFileDao;
import com.ktw.bitbit.downloader.Downloader;
import com.ktw.bitbit.helper.AvatarHelper;
import com.ktw.bitbit.helper.DialogHelper;
import com.ktw.bitbit.helper.ImageLoadHelper;
import com.ktw.bitbit.helper.PrivacySettingHelper;
import com.ktw.bitbit.helper.RedPacketHelper;
import com.ktw.bitbit.helper.TrillStatisticsHelper;
import com.ktw.bitbit.helper.UploadEngine;
import com.ktw.bitbit.pay.TransferMoneyActivity;
import com.ktw.bitbit.pay.TransferMoneyDetailActivity;
import com.ktw.bitbit.ui.FLYMainActivity;
import com.ktw.bitbit.ui.base.BaseActivity;
import com.ktw.bitbit.ui.base.CoreManager;
import com.ktw.bitbit.ui.contacts.SendContactsActivity;
import com.ktw.bitbit.ui.dialog.CreateCourseDialog;
import com.ktw.bitbit.ui.map.MapPickerActivity;
import com.ktw.bitbit.ui.me.MyCollection;
import com.ktw.bitbit.ui.me.capital.CapitalPasswordActivity;
import com.ktw.bitbit.ui.me.redpacket.RedDetailsActivity;
import com.ktw.bitbit.ui.me.redpacket.RedDetailsAuldActivity;
import com.ktw.bitbit.ui.me.redpacket.SendRedPacketActivity;
import com.ktw.bitbit.ui.message.single.PersonSettingActivity;
import com.ktw.bitbit.ui.mucfile.XfileUtils;
import com.ktw.bitbit.ui.other.BasicInfoActivity;
import com.ktw.bitbit.util.AsyncUtils;
import com.ktw.bitbit.util.AudioModeManger;
import com.ktw.bitbit.util.BitmapUtil;
import com.ktw.bitbit.util.Constants;
import com.ktw.bitbit.util.HtmlUtils;
import com.ktw.bitbit.util.JsonUtils;
import com.ktw.bitbit.util.PreferenceUtils;
import com.ktw.bitbit.util.RecorderUtils;
import com.ktw.bitbit.util.SmileyParser;
import com.ktw.bitbit.util.StringUtils;
import com.ktw.bitbit.util.TimeUtils;
import com.ktw.bitbit.util.ToastUtil;
import com.ktw.bitbit.util.log.FileUtils;
import com.ktw.bitbit.video.MessageEventGpu;
import com.ktw.bitbit.video.VideoRecorderActivity;
import com.ktw.bitbit.view.ChatBottomView;
import com.ktw.bitbit.view.ChatBottomView.ChatBottomListener;
import com.ktw.bitbit.view.ChatContentView;
import com.ktw.bitbit.view.ChatContentView.MessageEventListener;
import com.ktw.bitbit.view.NoDoubleClickListener;
import com.ktw.bitbit.view.PullDownListView;
import com.ktw.bitbit.view.SelectCardPopupWindow;
import com.ktw.bitbit.view.SelectFileDialog;
import com.ktw.bitbit.view.SelectionFrame;
import com.ktw.bitbit.view.chatHolder.MessageEventClickFire;
import com.ktw.bitbit.view.cjt2325.cameralibrary.util.LogUtil;
import com.ktw.bitbit.view.photopicker.PhotoPickerActivity;
import com.ktw.bitbit.view.photopicker.SelectModel;
import com.ktw.bitbit.view.photopicker.intent.PhotoPickerIntent;
import com.ktw.bitbit.view.redDialog.RedDialog;
import com.ktw.bitbit.view.redDialog.RedLootAllDialog;
import com.ktw.bitbit.xmpp.ListenerManager;
import com.ktw.bitbit.xmpp.listener.ChatMessageListener;
import com.xuan.xuanhttplibrary.okhttp.HttpUtils;
import com.xuan.xuanhttplibrary.okhttp.callback.BaseCallback;
import com.xuan.xuanhttplibrary.okhttp.callback.ListCallback;
import com.xuan.xuanhttplibrary.okhttp.result.ArrayResult;
import com.xuan.xuanhttplibrary.okhttp.result.ObjectResult;
import com.xuan.xuanhttplibrary.okhttp.result.Result;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.Comparator;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.UUID;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import Jni.FFmpegCmd;
import Jni.VideoUitls;
import VideoHandle.OnEditorListener;
import de.greenrobot.event.EventBus;
import de.greenrobot.event.Subscribe;
import de.greenrobot.event.ThreadMode;
import fm.jiecao.jcvideoplayer_lib.JCMediaManager;
import fm.jiecao.jcvideoplayer_lib.JCVideoPlayer;
import fm.jiecao.jcvideoplayer_lib.JVCideoPlayerStandardforchat;
import fm.jiecao.jcvideoplayer_lib.MessageEvent;
import okhttp3.Call;
import pl.droidsonroids.gif.GifDrawable;
import top.zibin.luban.Luban;
import top.zibin.luban.OnCompressListener;

/**
 * ????????????
 */
public class ChatActivity extends BaseActivity implements
        MessageEventListener, ChatBottomListener, ChatMessageListener,
        SelectCardPopupWindow.SendCardS {

    public static final String FRIEND = "friend";
    /*???????????????????????????*/
    public static final int REQUEST_CODE_SEND_RED = 13;     // ?????????
    public static final int REQUEST_CODE_SEND_RED_PT = 10;  // ??????????????????
    public static final int REQUEST_CODE_SEND_RED_KL = 11;  // ??????????????????
    public static final int REQUEST_CODE_SEND_RED_PSQ = 12; // ?????????????????????
    // ??????????????????
    public static final int REQUEST_CODE_SEND_CONTACT = 21;
    /***********************
     * ?????????????????????
     **********************/
    private static final int REQUEST_CODE_CAPTURE_PHOTO = 1;
    private static final int REQUEST_CODE_PICK_PHOTO = 2;
    private static final int REQUEST_CODE_SELECT_VIDEO = 3;
    private static final int REQUEST_CODE_SEND_COLLECTION = 4;// ???????????? ??????
    private static final int REQUEST_CODE_SELECT_Locate = 5;
    private static final int REQUEST_CODE_QUICK_SEND = 6;
    private static final int REQUEST_CODE_SELECT_FILE = 7;
    RefreshBroadcastReceiver receiver = new RefreshBroadcastReceiver();
    /*******************************************
     * ???????????????????????????????????? && ????????????????????????
     ******************************************/
    List<ChatMessage> chatMessages;
    @SuppressWarnings("unused")
    private ChatContentView mChatContentView;
    // ??????????????????
    private List<ChatMessage> mChatMessages;
    private ChatBottomView mChatBottomView;
    private ImageView mChatBgIv;// ????????????
    private AudioModeManger mAudioModeManger;
    // ??????????????????
    private Friend mFriend;
    private String mLoginUserId;
    private String mLoginNickName;
    private boolean isSearch;
    private double mSearchTime;
    // ????????????
    private String instantMessage;

    // ????????????????????????
    private boolean isNotificationComing;
    // ?????????????????????
    private List<Friend> mBlackList;
    private TextView mTvTitleLeft;
    // ?????? || ??????...
    private TextView mTvTitle;
    // ??????????????????
    CountDownTimer time = new CountDownTimer(10000, 1000) {
        @Override
        public void onTick(long millisUntilFinished) {
        }

        @Override
        public void onFinish() {
            String title = TextUtils.isEmpty(mFriend.getRemarkName()) ? mFriend.getNickName() : mFriend.getRemarkName();
            if (coreManager.getConfig().isOpenOnlineStatus) {
                mTvTitle.setText(title + getString(R.string.status_online));
            } else {
                mTvTitle.setText(title);
            }
        }
    };
    // ?????????????????????
    private int isReadDel;
    private String userId;// ??????isDevice==1????????????????????????????????????????????? || ?????????????????????????????????userId??????????????????id?????????ios || pc...;
    private long mMinId = 0;
    private int mPageSize = 20;
    private boolean mHasMoreData = true;
    private UploadEngine.ImFileUploadResponse mUploadResponse = new UploadEngine.ImFileUploadResponse() {

        @Override
        public void onSuccess(String toUserId, ChatMessage message) {
            sendMsg(message);
        }

        @Override
        public void onFailure(String toUserId, ChatMessage message) {
            for (int i = 0; i < mChatMessages.size(); i++) {
                ChatMessage msg = mChatMessages.get(i);
                if (message.get_id() == msg.get_id()) {
                    msg.setMessageState(ChatMessageListener.MESSAGE_SEND_FAILED);
                    ChatMessageDao.getInstance().updateMessageSendState(mLoginUserId, mFriend.getUserId(),
                            message.get_id(), ChatMessageListener.MESSAGE_SEND_FAILED);
                    mChatContentView.notifyDataSetInvalidated(false);
                    break;
                }
            }
        }
    };

    private Uri mNewPhotoUri;
    private HashSet<String> mDelayDelMaps = new HashSet<>();// ??????????????????????????? packedid
    private ChatMessage replayMessage;

    private RedDialog mRedDialog;
    private RedLootAllDialog lootRedDialog;
    private boolean isUserSaveContentMethod = true;

    public static void start(Context ctx, Friend friend) {
        Intent intent = new Intent(ctx, ChatActivity.class);
        intent.setFlags(Intent.FLAG_ACTIVITY_SINGLE_TOP);
        intent.putExtra(ChatActivity.FRIEND, friend);
        ctx.startActivity(intent);
    }

    /**
     * ???????????????????????????
     * ???????????????????????????
     *
     * @param content ???Toast????????????
     */
    public static void callFinish(Context ctx, String content, String toUserId) {
        Intent intent = new Intent();
        intent.putExtra("content", content);
        intent.putExtra("toUserId", toUserId);
        intent.setAction(com.ktw.bitbit.broadcast.OtherBroadcast.TYPE_DELALL);
        ctx.sendBroadcast(intent);
    }

    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.chat);

        LogUtil.e("image", AvatarHelper.getAvatarUrl(CoreManager.getSelf(mContext).getUserId(), true));



        SmileyParser.getInstance(FLYApplication.getContext()).notifyUpdate();
        /*AndroidBug5497Workaround.assistActivity(this);*/
        mLoginUserId = coreManager.getSelf().getUserId();
        mLoginNickName = coreManager.getSelf().getNickName();
        if (getIntent() != null) {
            mFriend = (Friend) getIntent().getSerializableExtra(FLYAppConstant.EXTRA_FRIEND);
            isSearch = getIntent().getBooleanExtra("isserch", false);
            if (isSearch) {
                mSearchTime = getIntent().getDoubleExtra("jilu_id", 0);
            }
            instantMessage = getIntent().getStringExtra("messageId");
            isNotificationComing = getIntent().getBooleanExtra(Constants.IS_NOTIFICATION_BAR_COMING, false);
        }
        if (mFriend == null) {
            ToastUtil.showToast(mContext, getString(R.string.tip_friend_not_found));
            finish();
            return;
        }
        if (mFriend.getIsDevice() == 1) {
            userId = mLoginUserId;
        }
        // mSipManager = SipManager.getInstance();
        mAudioModeManger = new AudioModeManger();
        mAudioModeManger.register(mContext);
        Downloader.getInstance().init(FLYApplication.getInstance().mAppDir + File.separator + mLoginUserId
                + File.separator + Environment.DIRECTORY_MUSIC);
        initView();
        // ???????????????????????????
        ListenerManager.getInstance().addChatMessageListener(this);
        // ??????EventBus
        EventBus.getDefault().register(this);
        // ????????????
        register();

        if (coreManager.getConfig().enableMpModule && mFriend.getUserId().equals(Friend.ID_SYSTEM_MESSAGE)) {
            // ??????????????????????????????status???????????????????????????
            FriendDao.getInstance().updateFriendStatus(mLoginUserId, userId, Friend.STATUS_SYSTEM);
            initSpecialMenu();
        } else {
            // ???????????????????????????????????????
            initFriendState();
        }

        if (mFriend.getUserId().equals(Friend.ID_SYSTEM_NOTIFICATION)) {
            getAutoAnswerList();
        }
    }

    private void initView() {
        mChatMessages = new ArrayList<>();
        mChatBottomView = (ChatBottomView) findViewById(R.id.chat_bottom_view);
        mChatContentView = (ChatContentView) findViewById(R.id.chat_content_view);
        initActionBar();
        mChatBottomView.setChatBottomListener(this);
        mChatBottomView.getmShotsLl().setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                mChatBottomView.getmShotsLl().setVisibility(View.GONE);
                String shots = PreferenceUtils.getString(mContext, Constants.SCREEN_SHOTS, "No_Shots");
                QuickSendPreviewActivity.startForResult(ChatActivity.this, shots, REQUEST_CODE_QUICK_SEND);
            }
        });
        if (mFriend.getIsDevice() == 1) {
            mChatBottomView.setEquipment(true);
            mChatContentView.setChatListType(ChatContentView.ChatListType.DEVICE);
        }

        mChatContentView.setToUserId(mFriend.getUserId());
        mChatContentView.setData(mChatMessages);
        mChatContentView.setChatBottomView(mChatBottomView);// ???????????????????????????????????????
        mChatContentView.setMessageEventListener(this);
        mChatContentView.setRefreshListener(new PullDownListView.RefreshingListener() {
            @Override
            public void onHeaderRefreshing() {
                loadDatas(false);
            }
        });
        // ?????????????????????????????????????????????
        mChatContentView.addOnScrollListener(new AbsListView.OnScrollListener() {
            boolean needSecure = false;

            @Override
            public void onScrollStateChanged(AbsListView view, int scrollState) {
            }

            @Override
            public void onScroll(AbsListView view, int firstVisibleItem, int visibleItemCount, int totalItemCount) {
                if (view instanceof ListView) {
                    int headerCount = ((ListView) view).getHeaderViewsCount();
                    firstVisibleItem -= headerCount;
                    totalItemCount -= headerCount;
                }
                if (firstVisibleItem < 0) {
                    // ?????????header???????????????firstVisibleItem??????0???????????????0????????????
                    firstVisibleItem = 0;
                }
                if (visibleItemCount <= 0) {
                    return;
                }

                List<ChatMessage> visibleList = mChatMessages.subList(firstVisibleItem, Math.min(firstVisibleItem + visibleItemCount, totalItemCount));
                boolean lastSecure = needSecure;
                needSecure = false;
                for (ChatMessage message : visibleList) {
                    if (message.getIsReadDel()) {
                        needSecure = true;
                        break;
                    }
                }
                if (needSecure != lastSecure) {
                    if (needSecure) {
                        getWindow().setFlags(WindowManager.LayoutParams.FLAG_SECURE, WindowManager.LayoutParams.FLAG_SECURE);
                    } else {
                        getWindow().clearFlags(WindowManager.LayoutParams.FLAG_SECURE);
                    }
                }
            }
        });

        // CoreManager.updateMyBalance();

        if (isNotificationComing) {
            Intent intent = new Intent();
            intent.putExtra(FLYAppConstant.EXTRA_FRIEND, mFriend);
            intent.setAction(Constants.NOTIFY_MSG_SUBSCRIPT);
            sendBroadcast(intent);
        } else {
            FriendDao.getInstance().markUserMessageRead(mLoginUserId, mFriend.getUserId());
        }

        loadDatas(true);
        if (mFriend.getDownloadTime() < mFriend.getTimeSend()) {// ????????????????????????????????????
            synchronizeChatHistory();
        }
    }

    private void loadDatas(boolean scrollToBottom) {
        if (mChatMessages.size() > 0) {
            mMinId = mChatMessages.get(0).getTimeSend();
        } else {
            ChatMessage chat = ChatMessageDao.getInstance().getLastChatMessage(mLoginUserId, mFriend.getUserId());
            if (chat != null && chat.getTimeSend() != 0) {
                mMinId = chat.getTimeSend() + 2;
            } else {
                mMinId = TimeUtils.sk_time_current_time();
            }
        }

        List<ChatMessage> chatLists;
        if (isSearch) {// ????????????????????????????????????????????????????????????????????????????????????
            chatLists = ChatMessageDao.getInstance().searchMessagesByTime(mLoginUserId,
                    mFriend.getUserId(), mSearchTime);
        } else {
            chatLists = ChatMessageDao.getInstance().getSingleChatMessages(mLoginUserId,
                    mFriend.getUserId(), mMinId, mPageSize);
        }

        if (chatLists == null || chatLists.size() <= 0) {
            if (!scrollToBottom) {// ????????????
                getNetSingle();
            }
        } else {
            mTvTitle.post(new Runnable() {
                @Override
                public void run() {
                    long currTime = TimeUtils.sk_time_current_time();
                    for (int i = 0; i < chatLists.size(); i++) {
                        ChatMessage message = chatLists.get(i);
                        // ???????????????????????????????????????
                        if (message.getDeleteTime() > 0 && message.getDeleteTime() < currTime / 1000) {
                            ChatMessageDao.getInstance().deleteSingleChatMessage(mLoginUserId, mFriend.getUserId(), message.getPacketId());
                            continue;
                        }
                        mChatMessages.add(0, message);
                    }

                    if (isSearch) {
                        isSearch = false;
                        int position = 0;
                        for (int i = 0; i < mChatMessages.size(); i++) {
                            if (mChatMessages.get(i).getDoubleTimeSend() == mSearchTime) {
                                position = i;
                            }
                        }
                        mChatContentView.notifyDataSetInvalidated(position);// ?????????????????????
                    } else {
                        if (scrollToBottom) {
                            mChatContentView.notifyDataSetInvalidated(scrollToBottom);
                        } else {
                            mChatContentView.notifyDataSetAddedItemsToTop(chatLists.size());
                        }
                    }
                    mChatContentView.headerRefreshingCompleted();
                    if (!mHasMoreData) {
                        mChatContentView.setNeedRefresh(false);
                    }
                }
            });
        }
    }

    protected void onSaveContent() {
        if (isUserSaveContentMethod) {
            if (mChatBottomView == null) {
                return;
            }
            String str = mChatBottomView.getmChatEdit().getText().toString().trim();
            // ?????? ???????????????
            str = str.replaceAll("\\s", "");
            str = str.replaceAll("\\n", "");
            if (TextUtils.isEmpty(str)) {
                if (XfileUtils.isNotEmpty(mChatMessages)) {
                    ChatMessage chat = mChatMessages.get(mChatMessages.size() - 1);
                    if (chat.getType() == XmppMessage.TYPE_TEXT && chat.getIsReadDel()) {
                        FriendDao.getInstance().updateFriendContent(
                                mLoginUserId,
                                mFriend.getUserId(),
                                getString(R.string.tip_click_to_read),
                                chat.getType(),
                                chat.getTimeSend());
                    } else {
                        FriendDao.getInstance().updateFriendContent(
                                mLoginUserId,
                                mFriend.getUserId(),
                                chat.getContent(),
                                chat.getType(),
                                chat.getTimeSend());
                    }
                }
            } else {// [??????]
                FriendDao.getInstance().updateFriendContent(
                        mLoginUserId,
                        mFriend.getUserId(),
                        "&8824" + str,
                        XmppMessage.TYPE_TEXT, TimeUtils.sk_time_current_time());
            }
            PreferenceUtils.putString(mContext, "WAIT_SEND" + mFriend.getUserId() + mLoginUserId, str);
        }
    }

    @Override
    public boolean dispatchTouchEvent(MotionEvent ev) {
        // ???????????????????????????????????????????????????
        if (ev.getActionIndex() > 0) {
            return true;
        }
        try {
            return super.dispatchTouchEvent(ev);
        } catch (IllegalArgumentException ignore) {
            // ????????????ViewPager???bug, ?????????????????????
            // https://stackoverflow.com/a/31306753
            return true;
        }
    }

    private void doBack() {
        if (!TextUtils.isEmpty(instantMessage)) {
            SelectionFrame selectionFrame = new SelectionFrame(this);
            selectionFrame.setSomething(null, getString(R.string.tip_forwarding_quit), new SelectionFrame.OnSelectionFrameClickListener() {
                @Override
                public void cancelClick() {

                }

                @Override
                public void confirmClick() {
                    finish();
                }
            });
            selectionFrame.show();
        } else {
            finish();
        }
    }

    @Override
    public void onBackPressed() {
        if (!JVCideoPlayerStandardforchat.handlerBack()) {
            doBack();
        }
    }

    @Override
    protected boolean onHomeAsUp() {
        doBack();
        return true;
    }

    @Override
    protected void onStart() {
        // TODO Auto-generated method stub
        super.onStart();
        mBlackList = FriendDao.getInstance().getAllBlacklists(mLoginUserId);
        instantChatMessage();
    }

    @Override
    protected void onResume() {
        // TODO Auto-generated method stub
        super.onResume();
        // ??????/?????????????????????
        boolean isSpeaker = PreferenceUtils.getBoolean(mContext,
                Constants.SPEAKER_AUTO_SWITCH + mLoginUserId, true);
        findViewById(R.id.iv_title_center).setVisibility(isSpeaker ? View.GONE : View.VISIBLE);
        mAudioModeManger.setSpeakerPhoneOn(isSpeaker);

        // ??????[??????]
        String draft = PreferenceUtils.getString(mContext, "WAIT_SEND" + mFriend.getUserId() + mLoginUserId, "");
        if (!TextUtils.isEmpty(draft)) {
            String s = StringUtils.replaceSpecialChar(draft);
            CharSequence content = HtmlUtils.transform200SpanString(s, true);
            mChatBottomView.getmChatEdit().setText(content);
            softKeyboardControl(true);
        }
        // ????????????????????????(??????????????????????????????????????? ??????/?????? ????????????????????????onResume??????????????????????????????)
        isReadDel = PreferenceUtils.getInt(mContext, Constants.MESSAGE_READ_FIRE + mFriend.getUserId() + mLoginUserId, 0);
        // ???????????????????????????id
        FLYApplication.IsRingId = mFriend.getUserId();
    }

    @Override
    protected void onPause() {
        super.onPause();
        VoicePlayer.instance().stop();

        // ?????????????????????
        mAudioModeManger.setSpeakerPhoneOn(true);

        if (TextUtils.isEmpty(mChatBottomView.getmChatEdit().getText().toString())) {// ???????????????????????????????????????????????????onPause--onResume???????????????????????????
            PreferenceUtils.putString(mContext, "WAIT_SEND" + mFriend.getUserId() + mLoginUserId, "");
        }
        // ?????????????????????id??????
        FLYApplication.IsRingId = "Empty";
    }

    @Override
    public void finish() {
        onSaveContent();
        MsgBroadcast.broadcastMsgUiUpdate(mContext);
        super.finish();
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        if (mAudioModeManger != null) {
            mAudioModeManger.unregister();
        }
        JCVideoPlayer.releaseAllVideos();
        if (mChatBottomView != null) {
            mChatBottomView.recordCancel();
        }
        ListenerManager.getInstance().removeChatMessageListener(this);
        if (EventBus.getDefault().isRegistered(this)) {
            EventBus.getDefault().unregister(this);
        }
        try {
            unregisterReceiver(receiver);
        } catch (Exception e) {
            // ??????????????????????????????????????????????????????????????????
        }
    }

    /***************************************
     * ChatContentView?????????
     ***************************************/
    @Override
    public void onMyAvatarClick() {
        // ?????????????????????
        mChatBottomView.reset();
        mChatBottomView.postDelayed(new Runnable() {
            @Override
            public void run() {
                Intent intent = new Intent(mContext, BasicInfoActivity.class);
                intent.putExtra(FLYAppConstant.EXTRA_USER_ID, mLoginUserId);
                startActivity(intent);
            }
        }, 100);
    }

    @Override
    public void onFriendAvatarClick(final String friendUserId) {
        // ?????????????????????
        mChatBottomView.reset();
        mChatBottomView.postDelayed(new Runnable() {
            @Override
            public void run() {
                Intent intent = new Intent(mContext, BasicInfoActivity.class);
                intent.putExtra(FLYAppConstant.EXTRA_USER_ID, friendUserId);
                startActivity(intent);
            }
        }, 100);
    }

    @Override
    public void LongAvatarClick(ChatMessage chatMessage) {
    }

    @Override
    public void onNickNameClick(String friendUserId) {
    }

    @Override
    public void onMessageClick(ChatMessage chatMessage) {
    }

    @Override
    public void onMessageLongClick(ChatMessage chatMessage) {
    }

    @Override
    public void onEmptyTouch() {
        mChatBottomView.reset();
    }

    @Override
    public void onTipMessageClick(ChatMessage message) {
        if (message.getFileSize() == XmppMessage.TYPE_83) {
            showRedReceivedDetail(message);
        }
    }


    // ????????????????????????
    private void showRedReceivedDetail(ChatMessage message) {

        String userId = coreManager.getSelf().getUserId();
        if (userId.equals(message.getFromUserId())) { //???????????????
            rushRedPacket(message, false);
        } else { //??????????????????
            gainRedPacket(mContext, message,userId);
        }
    }




    // ????????????????????????
    private void showRedReceivedDetail(String redId) {
        HashMap<String, String> params = new HashMap<>();
        params.put("access_token", CoreManager.requireSelfStatus(mContext).accessToken);
        params.put("id", redId);

        HttpUtils.get().url(CoreManager.requireConfig(mContext).RENDPACKET_GET)
                .params(params)
                .build()
                .execute(new BaseCallback<OpenRedpacket>(OpenRedpacket.class) {

                    @Override
                    public void onResponse(ObjectResult<OpenRedpacket> result) {
                        if (result.getData() != null) {
                            // ???resultCode==1?????????????????????
                            // ???resultCode==0???????????????????????????????????????????????????????????????
                            OpenRedpacket openRedpacket = result.getData();
                            Bundle bundle = new Bundle();
                            Intent intent = new Intent(mContext, RedDetailsAuldActivity.class);
                            bundle.putSerializable("openRedpacket", openRedpacket);
                            bundle.putInt("redAction", 0);
                            if (!TextUtils.isEmpty(result.getResultMsg())) //resultMsg??????????????????????????????
                            {
                                bundle.putInt("timeOut", 1);
                            } else {
                                bundle.putInt("timeOut", 0);
                            }

                            bundle.putBoolean("isGroup", false);
                            bundle.putString("mToUserId", mFriend.getUserId());
                            intent.putExtras(bundle);
                            mContext.startActivity(intent);
                        } else {
                            Toast.makeText(mContext, result.getResultMsg(), Toast.LENGTH_SHORT).show();
                        }
                    }

                    @Override
                    public void onError(Call call, Exception e) {
                    }
                });
    }

    @Override
    public void onReplayClick(ChatMessage message) {
        ChatMessage replayMessage = new ChatMessage(message.getObjectId());
        AsyncUtils.doAsync(this, t -> {
            FLYReporter.post("??????????????????????????????<" + message.getObjectId() + ">", t);
        }, c -> {
            List<ChatMessage> chatMessages = ChatMessageDao.getInstance().searchFromMessage(c.getRef(), mLoginUserId, mFriend.getUserId(), replayMessage);
            if (chatMessages == null) {
                // ??????????????????
                Log.e("Replay", "????????????????????????????????????<" + message.getObjectId() + ">");
                return;
            }
            int index = -1;
            for (int i = 0; i < chatMessages.size(); i++) {
                ChatMessage m = chatMessages.get(i);
                if (TextUtils.equals(m.getPacketId(), replayMessage.getPacketId())) {
                    index = i;
                }
            }
            if (index == -1) {
                FLYReporter.unreachable();
                return;
            }
            int finalIndex = index;
            c.uiThread(r -> {
                mChatMessages = chatMessages;
                mChatContentView.setData(mChatMessages);
                mChatContentView.notifyDataSetInvalidated(finalIndex);
            });
        });
    }

    /**
     * ???????????????????????????
     */
    @Override
    public void onSendAgain(ChatMessage message) {
        if (message.getType() == XmppMessage.TYPE_VOICE || message.getType() == XmppMessage.TYPE_IMAGE
                || message.getType() == XmppMessage.TYPE_VIDEO || message.getType() == XmppMessage.TYPE_FILE
                || message.getType() == XmppMessage.TYPE_LOCATION) {
            if (!message.isUpload()) {
                // ??????????????????????????????????????????????????????????????????????????????????????????????????????[??????????????????]????????????????????????????????????
                ChatMessageDao.getInstance().updateMessageSendState(mLoginUserId, mFriend.getUserId(),
                        message.get_id(), ChatMessageListener.MESSAGE_SEND_ING);
                UploadEngine.uploadImFile(coreManager.getSelfStatus().accessToken, coreManager.getSelf().getUserId(), mFriend.getUserId(), message, mUploadResponse);
            } else {
                if (isAuthenticated()) {
                    return;
                }
                coreManager.sendChatMessage(mFriend.getUserId(), message);
            }
        } else {
            if (isAuthenticated()) {
                return;
            }
            coreManager.sendChatMessage(mFriend.getUserId(), message);
        }
    }

    public void deleteMessage(String msgIdListStr) {
        Map<String, String> params = new HashMap<>();
        params.put("access_token", coreManager.getSelfStatus().accessToken);
        params.put("messageId", msgIdListStr);
        params.put("delete", "1");  // 1???????????? 2-????????????
        params.put("type", "1");    // 1???????????? 2-????????????

        HttpUtils.get().url(coreManager.getConfig().USER_DEL_CHATMESSAGE)
                .params(params)
                .build()
                .execute(new BaseCallback<Void>(Void.class) {

                    @Override
                    public void onResponse(ObjectResult<Void> result) {
                    }

                    @Override
                    public void onError(Call call, Exception e) {
                    }
                });
    }

    /**
     * ????????????
     */
    @Override
    public void onMessageBack(final ChatMessage chatMessage, final int position) {
        DialogHelper.showMessageProgressDialog(this, getString(R.string.message_revocation));
        Map<String, String> params = new HashMap<>();
        params.put("access_token", coreManager.getSelfStatus().accessToken);
        params.put("messageId", chatMessage.getPacketId());
        params.put("delete", "2");  // 1???????????? 2-????????????
        params.put("type", "1");    // 1???????????? 2-????????????

        HttpUtils.get().url(coreManager.getConfig().USER_DEL_CHATMESSAGE)
                .params(params)
                .build()
                .execute(new BaseCallback<Void>(Void.class) {

                    @Override
                    public void onResponse(ObjectResult<Void> result) {
                        DialogHelper.dismissProgressDialog();
                        if (Result.checkSuccess(mContext, result)) {
                            if (chatMessage.getType() == XmppMessage.TYPE_VOICE) {// ???????????????????????????????????????
                                if (VoicePlayer.instance().getVoiceMsgId().equals(chatMessage.getPacketId())) {
                                    VoicePlayer.instance().stop();
                                }
                            } else if (chatMessage.getType() == XmppMessage.TYPE_VIDEO) {
                                JCVideoPlayer.releaseAllVideos();
                            }
                            // ??????????????????
                            ChatMessage message = new ChatMessage();
                            message.setType(XmppMessage.TYPE_BACK);
                            message.setFromUserId(mLoginUserId);
                            message.setFromUserName(coreManager.getSelf().getNickName());
                            message.setToUserId(mFriend.getUserId());
                            message.setContent(chatMessage.getPacketId());
                            message.setTimeSend(TimeUtils.sk_time_current_time());
                            message.setPacketId(UUID.randomUUID().toString().replaceAll("-", ""));
                            coreManager.sendChatMessage(mFriend.getUserId(), message);
                            ChatMessage chat = mChatMessages.get(position);
                            if (ChatMessageDao.getInstance().updateMessageBack(mLoginUserId, mFriend.getUserId(), chat.getPacketId(), getString(R.string.you))) {
                                chat.setType(XmppMessage.TYPE_TIP);
                                chat.setContent(getString(R.string.already_with_draw));
                                mChatContentView.notifyDataSetInvalidated(true);
                            }
                        }
                    }

                    @Override
                    public void onError(Call call, Exception e) {
                        DialogHelper.dismissProgressDialog();
                        ToastUtil.showErrorNet(mContext);
                    }
                });
    }

    @Override
    public void onMessageReplay(ChatMessage chatMessage) {
        replayMessage = chatMessage;
        mChatBottomView.setReplay(chatMessage);
    }

    @Override
    public void cancelReplay() {
        replayMessage = null;
    }

    @Override
    public void onCallListener(int type) {
        if (coreManager.isLogin()) {
            if (type == 103 || type == 104) {
                Log.e("zq", "dialAudioCall");
                dial(1);
            } else if (type == 113 || type == 114) {
                Log.e("zq", "dialVideoCall");
                dial(2);
            }
        } else {
            coreManager.autoReconnectShowProgress(this);
        }
    }

    private void dial(final int type) {
        if (FLYApplication.IS_OPEN_CLUSTER) {// ???????????????????????? meetUrl
            Map<String, String> params = new HashMap<>();
            params.put("access_token", coreManager.getSelfStatus().accessToken);
            String area = PreferenceUtils.getString(this, FLYAppConstant.EXTRA_CLUSTER_AREA);
            if (!TextUtils.isEmpty(area)) {
                params.put("area", area);
            }
            params.put("toUserId", mFriend.getUserId());

            HttpUtils.get().url(coreManager.getConfig().OPEN_MEET)
                    .params(params)
                    .build()
                    .execute(new BaseCallback<String>(String.class) {
                        @Override
                        public void onResponse(ObjectResult<String> result) {
                            if (!TextUtils.isEmpty(result.getData())) {
                                JSONObject jsonObject = JSONObject.parseObject(result.getData());
                                realDial(type, jsonObject.getString("meetUrl"));
                            } else {
                                realDial(type, null);
                            }
                        }

                        @Override
                        public void onError(Call call, Exception e) {// ?????????????????????????????????????????????
                            realDial(type, null);
                        }
                    });
        } else {
            realDial(type, null);
        }
    }

    private void realDial(int type, String meetUrl) {
        ChatMessage message = new ChatMessage();
        if (type == 1) {// ????????????
            message.setType(XmppMessage.TYPE_IS_CONNECT_VOICE);
            message.setContent(getString(R.string.sip_invite) + " " + getString(R.string.sip_invite));
        } else if (type == 2) {// ????????????
            message.setType(XmppMessage.TYPE_IS_CONNECT_VIDEO);
            message.setContent(getString(R.string.sip_invite) + " " + getString(R.string.sip_invite));
        }
        message.setFromUserId(mLoginUserId);
        message.setFromUserName(mLoginNickName);
        message.setToUserId(mFriend.getUserId());
        if (!TextUtils.isEmpty(meetUrl)) {
            message.setFilePath(meetUrl);
        }
        message.setPacketId(UUID.randomUUID().toString().replaceAll("-", ""));
        message.setTimeSend(TimeUtils.sk_time_current_time());
        coreManager.sendChatMessage(mFriend.getUserId(), message);
        Intent intent = new Intent(this, Jitsi_pre.class);
        if (type == 1) {
            intent.putExtra("isvoice", true);
        } else if (type == 2) {
            intent.putExtra("isvoice", false);
        } else {
            intent.putExtra("isTalk", true);
        }
        intent.putExtra("fromuserid", mLoginUserId);
        intent.putExtra("touserid", mFriend.getUserId());
        intent.putExtra("username", mFriend.getNickName());
        if (!TextUtils.isEmpty(meetUrl)) {
            intent.putExtra("meetUrl", meetUrl);
        }
        startActivity(intent);
    }

    /***************************************
     * ChatBottomView?????????
     ***************************************/

    private void softKeyboardControl(boolean isShow) {
        // ???????????????
        InputMethodManager imm = (InputMethodManager) getSystemService(INPUT_METHOD_SERVICE);
        if (imm == null) return;
        if (isShow) {
            mChatBottomView.postDelayed(new Runnable() {
                @Override
                public void run() {// ??????200ms?????????????????????????????????????????????????????????????????????????????????
                    mChatBottomView.getmChatEdit().requestFocus();
                    mChatBottomView.getmChatEdit().setSelection(mChatBottomView.getmChatEdit().getText().toString().length());
                    imm.toggleSoftInput(0, InputMethodManager.SHOW_FORCED);
                }
            }, 200);
        } else {
            imm.toggleSoftInput(0, InputMethodManager.HIDE_NOT_ALWAYS);
        }
    }

    /**
     * ??????????????????????????????
     */
    private void sendMessage(final ChatMessage message) {
        if (interprect()) {// ????????????????????????????????????
            ToastUtil.showToast(this, getString(R.string.tip_remote_in_black));
            // ?????????????????????
            mChatMessages.remove(message);
            mChatContentView.notifyDataSetInvalidated(true);
            return;
        }

        message.setFromUserId(mLoginUserId);
        message.setToUserId(mFriend.getUserId());
        message.setToUserName(mFriend.getNickName());
        PrivacySetting privacySetting = PrivacySettingHelper.getPrivacySettings(this);
        boolean isSupport = privacySetting.getMultipleDevices() == 1;
        if (isSupport) {
            message.setFromId("android");
        } else {
            message.setFromId("youjob");
        }
        if (mFriend.getIsDevice() == 1) {
            message.setToUserId(mLoginUserId);
            message.setToUserName(mFriend.getUserId());
        } else {
            // sz ??????????????????
            if (mFriend.getChatRecordTimeOut() == -1 || mFriend.getChatRecordTimeOut() == 0) {// ??????
                message.setDeleteTime(-1);
            } else {
                long deleteTime = TimeUtils.sk_time_current_time() + (long) (mFriend.getChatRecordTimeOut() * 24 * 60 * 60);
                message.setDeleteTime(deleteTime);
            }
        }

        boolean isEncrypt = privacySetting.getIsEncrypt() == 1;
        if (isEncrypt) {
            message.setIsEncrypt(1);
        } else {
            message.setIsEncrypt(0);
        }

        message.setReSendCount(ChatMessageDao.fillReCount(message.getType()));
        message.setPacketId(UUID.randomUUID().toString().replaceAll("-", ""));
        message.setTimeSend(TimeUtils.sk_time_current_time());

        // ??????????????????????????????
        ChatMessageDao.getInstance().saveNewSingleChatMessage(mLoginUserId, mFriend.getUserId(), message);
        if (message.getType() == XmppMessage.TYPE_VOICE || message.getType() == XmppMessage.TYPE_IMAGE
                || message.getType() == XmppMessage.TYPE_VIDEO || message.getType() == XmppMessage.TYPE_FILE
                || message.getType() == XmppMessage.TYPE_LOCATION) {// ??????????????????????????????????????????????????????
            // ?????????????????????????????????
            if (!message.isUpload()) {// ?????????
                if (mFriend.getIsDevice() == 1) {
                    UploadEngine.uploadImFile(coreManager.getSelfStatus().accessToken, coreManager.getSelf().getUserId(), userId, message, mUploadResponse);
                } else {
                    UploadEngine.uploadImFile(coreManager.getSelfStatus().accessToken, coreManager.getSelf().getUserId(), mFriend.getUserId(), message, mUploadResponse);
                }
            } else {// ????????? ?????????????????????????????????
                sendMsg(message);
            }
        } else {// ????????????????????????
            sendMsg(message);
        }
    }

    private void sendMsg(ChatMessage message) {
        // ???????????????????????????????????????xmpp???????????????
        // ??????????????????????????????
        if (isAuthenticated()) {
            return;
        }
        if (mFriend.getIsDevice() == 1) {
            coreManager.sendChatMessage(userId, message);
        } else {
            coreManager.sendChatMessage(mFriend.getUserId(), message);
        }

        //?????????????????????????????????????????????????????????
        if (mFriend.getUserId().equals(Friend.ID_SYSTEM_NOTIFICATION)) {
            //???????????????????????????
            if (message.getType() == XmppMessage.TYPE_TEXT) {
                String content = getContentByText(message.getContent());
                if (!TextUtils.isEmpty(content)) {
                    makeMessage(content, false);
                } else {
                    if (FLYApplication.autoAnswerBean != null && !TextUtils.isEmpty(FLYApplication.autoAnswerBean.getAnswer())) {
                        makeMessage(FLYApplication.autoAnswerBean.getAnswer(), true);
                    }
                }
            } else {
                //????????????????????????????????????????????????
                if (FLYApplication.autoAnswerBean != null && !TextUtils.isEmpty(FLYApplication.autoAnswerBean.getAnswer())) {
                    makeMessage(FLYApplication.autoAnswerBean.getAnswer(), true);
                }
            }
        }
    }

    /**
     * ???????????????????????????
     */
    @Override
    public void stopVoicePlay() {
        VoicePlayer.instance().stop();
    }

    @Override
    public void sendAt() {
    }

    @Override
    public void sendAtMessage(String text) {
        sendText(text);// ???????????????@?????????????????????????????????
    }

    @Override
    public void sendText(String text) {
        if (TextUtils.isEmpty(text)) {
            return;
        }
        if (isAuthenticated()) {
            return;
        }

        ChatMessage message = new ChatMessage();
        // ????????????
        message.setType(XmppMessage.TYPE_TEXT);
        message.setFromUserId(mLoginUserId);
        message.setFromUserName(mLoginNickName);
        message.setContent(text);
        if (replayMessage != null) {
            message.setType(XmppMessage.TYPE_REPLAY);
            message.setObjectId(replayMessage.toJsonString());
            replayMessage = null;
            mChatBottomView.resetReplay();
        }
        message.setIsReadDel(isReadDel);
        mChatMessages.add(message);
        mChatContentView.notifyDataSetInvalidated(true);

       /* mChatContentView.postDelayed(new Runnable() {
            @Override
            public void run() {
                mChatContentView.scrollToBottom();
            }
        }, 500);*/

        sendMessage(message);
        // ?????????????????????????????????????????????
        for (ChatMessage msg : mChatMessages) {
            if (msg.getType() == XmppMessage.TYPE_RED// ??????
                    && StringUtils.strEquals(msg.getFilePath(), "3")// ????????????
                    && text.equalsIgnoreCase(msg.getContent())// ??????????????????????????????
                    && msg.getFileSize() == 1// ?????????????????????
                    && !msg.isMySend()) {
                // todo ????????????????????????????????????????????????????????????????????????????????????????????????????????????????????????????????????????????????mRedDialog??????
                // todo ?????????????????????????????????
                clickRedPacket(msg);
                // ????????????????????????????????????????????????????????????????????????????????????????????????
                break;
            }
        }
    }

    /**
     * ????????????
     */
    @Deprecated
    public void clickRedPacketAuld(ChatMessage msg) {
        HashMap<String, String> params = new HashMap<>();
        params.put("access_token", coreManager.getSelfStatus().accessToken);
        params.put("id", msg.getObjectId());

        HttpUtils.get().url(CoreManager.requireConfig(mContext).RENDPACKET_GET)
                .params(params)
                .build()
                .execute(new BaseCallback<OpenRedpacket>(OpenRedpacket.class) {
                    @Override
                    public void onResponse(ObjectResult<OpenRedpacket> result) {
                        if (result.getResultCode() == 1) {
                            RedDialogBean redDialogBean = new RedDialogBean(msg.getFromUserId(), msg.getFromUserName(),
                                    msg.getContent(), null);
                            mRedDialog = new RedDialog(mContext, redDialogBean, () -> {
                                // ????????????
                                rushRedPacket(msg,false);
                            });
                            mRedDialog.show();
                        } else {
                            // ?????????????????????????????????????????????
                            msg.setFileSize(2);
                            ChatMessageDao.getInstance().updateChatMessageReceiptStatus(mLoginUserId, mFriend.getUserId(), msg.getPacketId());
                            mChatContentView.notifyDataSetChanged();
                        }

                    }

                    @Override
                    public void onError(Call call, Exception e) {
                    }
                });
    }



    public void clickRedPacket(ChatMessage msg) {
        String userId = CoreManager.getSelf(this).getUserId();
        gainRedPacket(this,msg,userId);
    }

    /**
     * ????????????
     */
    public void openRedPacketAuld(final ChatMessage message) {
        HashMap<String, String> params = new HashMap<String, String>();
        String redId = message.getObjectId();
        params.put("access_token", coreManager.getSelfStatus().accessToken);
        params.put("id", redId);

        HttpUtils.get().url(coreManager.getConfig().REDPACKET_OPEN)
                .params(params)
                .build()
                .execute(new BaseCallback<OpenRedpacket>(OpenRedpacket.class) {

                    @Override
                    public void onResponse(ObjectResult<OpenRedpacket> result) {
                        if (mRedDialog != null) {
                            mRedDialog.dismiss();
                        }
                        if (result.getData() != null) {
                            // ??????????????????????????????,???????????????
                            message.setFileSize(2);
                            ChatMessageDao.getInstance().updateChatMessageReceiptStatus(mLoginUserId, mFriend.getUserId(), message.getPacketId());
                            mChatContentView.notifyDataSetChanged();

                            OpenRedpacket openRedpacket = result.getData();
                            Bundle bundle = new Bundle();
                            Intent intent = new Intent(mContext, RedDetailsAuldActivity.class);
                            bundle.putSerializable("openRedpacket", openRedpacket);
                            bundle.putInt("redAction", 1);
                            bundle.putInt("timeOut", 0);

                            bundle.putBoolean("isGroup", false);
                            bundle.putString("mToUserId", mFriend.getUserId());
                            intent.putExtras(bundle);
                            mContext.startActivity(intent);
                            // ????????????
                            coreManager.updateMyBalance();

                            showReceiverRedLocal(openRedpacket);
                        } else {
                            Toast.makeText(ChatActivity.this, result.getResultMsg(), Toast.LENGTH_SHORT).show();
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

    /*
     * ?????????
    public void rushRedPacket(final ChatMessage message) {
        HashMap<String, String> params = new HashMap<String, String>();
        String redId = message.getObjectId();
        String userId = coreManager.getSelf().getUserId();
        String receiveName = coreManager.getSelf().getNickName();

        params.put("access_token", coreManager.getSelfStatus().accessToken);
        params.put("redId", redId);
        params.put("userId", userId);
        params.put("receiveName", receiveName);
        params.put("currencyId", "currencyId");
        params.put("currencyName", "currencyName");

        HttpUtils.get().url(coreManager.getConfig().RUSH_RED_PACKET)
                .params(params)
                .build()
                .execute(new BaseCallback<OpenRedpacket>(OpenRedpacket.class) {

                    @Override
                    public void onResponse(ObjectResult<OpenRedpacket> result) {
                        if (mRedDialog != null) {
                            mRedDialog.dismiss();
                        }
                        if (result.getData() != null) {
                            // ??????????????????????????????,???????????????
                            message.setFileSize(2);
                            ChatMessageDao.getInstance().updateChatMessageReceiptStatus(mLoginUserId, mFriend.getUserId(), message.getPacketId());
                            mChatContentView.notifyDataSetChanged();

                            OpenRedpacket openRedpacket = result.getData();
                            Bundle bundle = new Bundle();
                            Intent intent = new Intent(mContext, RedDetailsAuldActivity.class);
                            bundle.putSerializable("openRedpacket", openRedpacket);
                            bundle.putInt("redAction", 1);
                            bundle.putInt("timeOut", 0);

                            bundle.putBoolean("isGroup", false);
                            bundle.putString("mToUserId", mFriend.getUserId());
                            intent.putExtras(bundle);
                            mContext.startActivity(intent);
                            // ????????????
                            coreManager.updateMyBalance();

                            showReceiverRedLocal(openRedpacket);
                        } else {
                            Toast.makeText(ChatActivity.this, result.getResultMsg(), Toast.LENGTH_SHORT).show();
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
 */


    /**
     * ????????????????????????
     *
     * @param context
     * @param userId
     */
    private void gainRedPacket(Context context,ChatMessage message, String userId) {

        final RedPacketResult redPacket = new Gson().fromJson(message.getObjectId(), RedPacketResult.class);
        Map<String, String> params = new HashMap<>();
        params.put("redIdS", redPacket.redId);
        params.put("userId", userId);
        RedPacketHelper.gainRedPacket(context, params,
                error -> {
                },
                result -> {

                    if ((result.redUser == 1 && result.redStatus == 2) ||//??????????????????????????? ?????????????????????
                            (result.redUser == 1 && result.redStatus == 4)) { //??????????????????????????? ?????????????????????

                        RedDialogBean redDialogBean =
                                new RedDialogBean(redPacket.userId, redPacket.userName, redPacket.redEnvelopeName, redPacket.redId);

                        mRedDialog = new RedDialog(mContext, redDialogBean,
                                () -> {
                                    rushRedPacket(message, true);
                                    mRedDialog.dismiss();
                                });
                        mRedDialog.show();

                    } else if (result.redUser == 1 && result.redStatus == 1) { //??????????????????????????????????????????
                        RedDialogBean redDialogBean =
                                new RedDialogBean(redPacket.userId, redPacket.userName,
                                        getString(R.string.red_packet_loot_all), redPacket.redId);
                        lootRedDialog = new RedLootAllDialog(mContext, redDialogBean, () -> {
                            rushRedPacket(message);
                            lootRedDialog.dismiss();
                        });
                        lootRedDialog.show();

                    } else if (result.redUser == 1 && result.redStatus == 3) { //??????????????????????????????????????????

                        RedDialogBean redDialogBean =
                                new RedDialogBean(redPacket.userId, redPacket.userName,
                                        getString(R.string.red_packet_past), redPacket.redId);
                        lootRedDialog = new RedLootAllDialog(mContext, redDialogBean, () -> {
                            rushRedPacket(message);
                            lootRedDialog.dismiss();
                        });
                        lootRedDialog.show();

                    } else if (result.redUser == 0) {
                        rushRedPacket(message, false);
                    }


                });
    }

    /**
     * ?????????
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
                    if (isGrab) {  //??????????????????
                        message.setFileSize(2);
                        ChatMessageDao.getInstance().updateChatMessageReceiptStatus(mLoginUserId, mFriend.getUserId(), message.getPacketId());
                        mChatContentView.notifyDataSetChanged();

                        coreManager.updateMyBalance();
//                        showReceiverRedLocal(openRedpacket);
                        // ????????????
                        CoreManager.updateMyBalance();

                    }

                    Bundle bundle = new Bundle();
                    Intent intent = new Intent(mContext, RedDetailsActivity.class);
                    bundle.putParcelable("openRedpacket", result.getData());
                    bundle.putInt("redAction", 0);
                    bundle.putBoolean("isGroup", false);
                    bundle.putString("mToUserId", message.getToUserId());
                    bundle.putSerializable("redPacket", redPacket);
                    if ((userId.equals(message.getFromUserId()))) {
                        bundle.putBoolean("null", true);
                    }
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
                    bundle.putBoolean("isGroup", false);
                    bundle.putString("mToUserId", message.getToUserId());
                    bundle.putSerializable("redPacket", redPacket);
                    bundle.putBoolean("null", true);
                    intent.putExtras(bundle);
                    mContext.startActivity(intent);
                });
    }

    private void showReceiverRedLocal(OpenRedpacket openRedpacket) {
        // ??????????????????????????????
        ChatMessage chatMessage = new ChatMessage();
        chatMessage.setFileSize(XmppMessage.TYPE_83);
        chatMessage.setFilePath(openRedpacket.getPacket().getId());
        chatMessage.setFromUserId(mLoginUserId);
        chatMessage.setFromUserName(mLoginNickName);
        chatMessage.setToUserId(mFriend.getUserId());
        chatMessage.setType(XmppMessage.TYPE_TIP);
        chatMessage.setContent(getString(R.string.red_received_self, openRedpacket.getPacket().getUserName()));
        chatMessage.setPacketId(UUID.randomUUID().toString().replaceAll("-", ""));
        chatMessage.setTimeSend(TimeUtils.sk_time_current_time());
        if (ChatMessageDao.getInstance().saveNewSingleChatMessage(mLoginUserId, mFriend.getUserId(), chatMessage)) {
            mChatMessages.add(chatMessage);
            mChatContentView.notifyDataSetInvalidated(true);
        }
    }

    @Override
    public void sendGif(String text) {
        if (TextUtils.isEmpty(text)) {
            return;
        }
        if (isAuthenticated()) {
            return;
        }
        ChatMessage message = new ChatMessage();
        message.setType(XmppMessage.TYPE_GIF);
        message.setFromUserId(mLoginUserId);
        message.setFromUserName(mLoginNickName);
        message.setContent(text);
        mChatMessages.add(message);
        mChatContentView.notifyDataSetInvalidated(true);
        sendMessage(message);
    }

    @Override
    public void sendCollection(String collection) {
        if (isAuthenticated()) {
            return;
        }
        ChatMessage message = new ChatMessage();
        message.setType(XmppMessage.TYPE_IMAGE);
        message.setFromUserId(mLoginUserId);
        message.setFromUserName(mLoginNickName);
        message.setContent(collection);
        message.setUpload(true);// ?????????????????????????????????
        message.setIsReadDel(isReadDel);
        mChatMessages.add(message);
        mChatContentView.notifyDataSetInvalidated(true);
        sendMessage(message);
    }

    @Override
    public void sendVoice(String filePath, int timeLen, ArrayList<String> stringAudio) {
        if (TextUtils.isEmpty(filePath)) {
            return;
        }
        if (isAuthenticated()) {
            return;
        }
        File file = new File(filePath);
        long fileSize = file.length();
        ChatMessage message = new ChatMessage();
        message.setType(XmppMessage.TYPE_VOICE);
        message.setFromUserId(mLoginUserId);
        message.setObjectId(TextUtils.join(",", stringAudio));
        message.setFromUserName(mLoginNickName);
        message.setContent("");
        message.setFilePath(filePath);
        message.setFileSize((int) fileSize);
        message.setTimeLen(timeLen);
        message.setIsReadDel(isReadDel);
        mChatMessages.add(message);
        mChatContentView.notifyDataSetInvalidated(true);
        sendMessage(message);
    }

    public void sendImage(File file) {
        if (!file.exists()) {
            return;
        }
        if (isAuthenticated()) {
            return;
        }
        long fileSize = file.length();
        ChatMessage message = new ChatMessage();
        message.setType(XmppMessage.TYPE_IMAGE);
        message.setFromUserId(mLoginUserId);
        message.setFromUserName(mLoginNickName);
        message.setContent("");
        String filePath = file.getAbsolutePath();
        message.setFilePath(filePath);
        message.setFileSize((int) fileSize);
        int[] imageParam = BitmapUtil.getImageParamByIntsFile(filePath);
        message.setLocation_x(String.valueOf(imageParam[0]));
        message.setLocation_y(String.valueOf(imageParam[1]));
        message.setIsReadDel(isReadDel);
        mChatMessages.add(message);
        mChatContentView.notifyDataSetInvalidated(true);
        sendMessage(message);
    }

    public void sendVideo(File file) {
        if (!file.exists()) {
            return;
        }
        if (isAuthenticated()) {
            return;
        }
        long fileSize = file.length();
        ChatMessage message = new ChatMessage();
        message.setType(XmppMessage.TYPE_VIDEO);
        message.setFromUserId(mLoginUserId);
        message.setFromUserName(mLoginNickName);
        message.setContent("");
        String filePath = file.getAbsolutePath();
        message.setFilePath(filePath);
        message.setFileSize((int) fileSize);
        message.setIsReadDel(isReadDel);
        mChatMessages.add(message);
        mChatContentView.notifyDataSetInvalidated(true);
        sendMessage(message);
    }

    public void sendFile(File file) {
        if (!file.exists()) {
            return;
        }
        if (isAuthenticated()) {
            return;
        }
        long fileSize = file.length();
        ChatMessage message = new ChatMessage();
        message.setType(XmppMessage.TYPE_FILE);
        message.setFromUserId(mLoginUserId);
        message.setFromUserName(mLoginNickName);
        message.setContent("");
        message.setTimeSend(TimeUtils.sk_time_current_time());
        String filePath = file.getAbsolutePath();
        message.setFilePath(filePath);
        message.setFileSize((int) fileSize);
        mChatMessages.add(message);
        mChatContentView.notifyDataSetInvalidated(true);
        sendMessage(message);
    }

    private void sendContacts(List<Contacts> contactsList) {
        for (Contacts contacts : contactsList) {
            sendText(contacts.getName() + '\n' + contacts.getTelephone());
        }
    }

    public void sendLocate(double latitude, double longitude, String address, String snapshot) {
        if (isAuthenticated()) {
            return;
        }
        ChatMessage message = new ChatMessage();
        message.setType(XmppMessage.TYPE_LOCATION);
        message.setFromUserId(mLoginUserId);
        message.setFromUserName(mLoginNickName);
        // ??????????????????????????????
        message.setContent("");
        message.setFilePath(snapshot);
        message.setLocation_x(latitude + "");
        message.setLocation_y(longitude + "");
        message.setObjectId(address);
        mChatMessages.add(message);
        mChatContentView.notifyDataSetInvalidated(true);
        sendMessage(message);
    }

    @Override
    public void clickPhoto() {
        // ????????????true
        /*MyApplication.GalleyNotBackGround = true;
        CameraUtil.pickImageSimple(this, REQUEST_CODE_PICK_PHOTO);*/
        ArrayList<String> imagePaths = new ArrayList<>();
        PhotoPickerIntent intent = new PhotoPickerIntent(ChatActivity.this);
        intent.setSelectModel(SelectModel.MULTI);
        // ??????????????????????????? ????????????????????????
        intent.setSelectedPaths(imagePaths);
        startActivityForResult(intent, REQUEST_CODE_PICK_PHOTO);
        mChatBottomView.reset();
    }

    @Override
    public void clickCamera() {
       /* mNewPhotoUri = CameraUtil.getOutputMediaFileUri(this, CameraUtil.MEDIA_TYPE_IMAGE);
        CameraUtil.captureImage(this, mNewPhotoUri, REQUEST_CODE_CAPTURE_PHOTO);*/
       /* Intent intent = new Intent(this, EasyCameraActivity.class);
        startActivity(intent);*/
        mChatBottomView.reset();
        Intent intent = new Intent(this, VideoRecorderActivity.class);
        startActivity(intent);
    }

    @Override
    public void clickStartRecord() {
        // ???????????????ui????????????????????????clickCamera?????????
       /* Intent intent = new Intent(this, VideoRecorderActivity.class);
        startActivity(intent);*/
    }

    @Override
    public void clickLocalVideo() {
        // ???????????????ui????????????????????????clickCamera?????????
        /*Intent intent = new Intent(this, LocalVideoActivity.class);
        intent.putExtra(AppConstant.EXTRA_ACTION, AppConstant.ACTION_SELECT);
        intent.putExtra(AppConstant.EXTRA_MULTI_SELECT, true);
        startActivityForResult(intent, REQUEST_CODE_SELECT_VIDEO);*/
    }

    @Override
    public void clickAudio() {
        if (coreManager.isLogin()) {
            dial(1);
        } else {
            coreManager.autoReconnectShowProgress(this);
        }
    }

    @Override
    public void clickVideoChat() {
        if (coreManager.isLogin()) {
            dial(2);
        } else {
            coreManager.autoReconnectShowProgress(this);
        }
    }

    @Override
    public void sendCustomEmot(String name) {
        if (isAuthenticated()) {
            return;
        }
        ChatMessage message = new ChatMessage();
        message.setType(XmppMessage.TYPE_CUSTOM_EMOT);
        message.setFromUserId(mLoginUserId);
        message.setFromUserName(mLoginNickName);
        message.setContent(name);
        message.setUpload(true);// ?????????????????????????????????
        message.setIsReadDel(isReadDel);
        mChatMessages.add(message);
        mChatContentView.notifyDataSetInvalidated(true);
        sendMessage(message);
    }

    @Override
    public void sendEmotPackage(String emot) {
        if (isAuthenticated()) {
            return;
        }
        ChatMessage message = new ChatMessage();
        message.setType(XmppMessage.TYPE_EMOT_PACKAGE);
        message.setFromUserId(mLoginUserId);
        message.setFromUserName(mLoginNickName);
        message.setContent(emot);
        message.setUpload(true);// ?????????????????????????????????
        message.setIsReadDel(isReadDel);
        mChatMessages.add(message);
        mChatContentView.notifyDataSetInvalidated(true);
        sendMessage(message);
    }

    @Override
    public void clickFile() {
        SelectFileDialog dialog = new SelectFileDialog(this, new SelectFileDialog.OptionFileListener() {
            @Override
            public void option(List<File> files) {
                if (files != null && files.size() > 0) {
                    for (int i = 0; i < files.size(); i++) {
                        sendFile(files.get(i));
                    }
                }
            }

            @Override
            public void intent() {
                Intent intent = new Intent(Intent.ACTION_OPEN_DOCUMENT);
                intent.setType("*/*");//???????????????????????????????????????????????????????????????????????????
                intent.addCategory(Intent.CATEGORY_OPENABLE);
                startActivityForResult(intent, REQUEST_CODE_SELECT_FILE);
            }

        });
        dialog.show();
    }

    @Override
    public void clickContact() {
        SendContactsActivity.start(this, REQUEST_CODE_SEND_CONTACT);
    }

    @Override
    public void clickLocation() {
        Intent intent = new Intent(this, MapPickerActivity.class);
        intent.putExtra(FLYAppConstant.EXTRA_FORM_CAHT_ACTIVITY, true);
        startActivityForResult(intent, REQUEST_CODE_SELECT_Locate);
    }

    @Override
    public void clickCard() {
        SelectCardPopupWindow mSelectCardPopupWindow = new SelectCardPopupWindow(this, this);
        mSelectCardPopupWindow.showAtLocation(findViewById(R.id.root_view),
                Gravity.CENTER, 0, 0);
    }

    @Override
    public void clickRedpacket() {

        boolean hasPayPassword = PreferenceUtils.getBoolean(this,
                Constants.IS_CAPITAL_PASSWORD_SET + coreManager.getSelf().getUserId());

        if (!hasPayPassword) {
            String userId = coreManager.getSelf().getUserId();
            RedPacketHelper.detectionCapitalPassword(getApplication(), coreManager, userId,
                    error -> {
                        Intent intent = new Intent(this, CapitalPasswordActivity.class);
                        startActivity(intent);
                    },
                    success -> {
                        Intent intent = new Intent(this, SendRedPacketActivity.class);
                        intent.putExtra(FLYAppConstant.EXTRA_USER_ID, mFriend.getUserId());
                        intent.putExtra(FLYAppConstant.EXTRA_NICK_NAME, mFriend.getNickName());
                        startActivityForResult(intent, REQUEST_CODE_SEND_RED);
                    });
        } else {
            Intent intent = new Intent(this, SendRedPacketActivity.class);
            intent.putExtra(FLYAppConstant.EXTRA_USER_ID, mFriend.getUserId());
            intent.putExtra(FLYAppConstant.EXTRA_NICK_NAME, mFriend.getNickName());
            startActivityForResult(intent, REQUEST_CODE_SEND_RED);
        }
    }

    @Override
    public void clickTransferMoney() {
        Intent intent = new Intent(this, TransferMoneyActivity.class);
        intent.putExtra(FLYAppConstant.EXTRA_USER_ID, mFriend.getUserId());
        intent.putExtra(FLYAppConstant.EXTRA_NICK_NAME, TextUtils.isEmpty(mFriend.getRemarkName()) ? mFriend.getNickName() : mFriend.getRemarkName());
        startActivity(intent);
    }

    @Override
    public void clickCollection() {
        Intent intent = new Intent(this, MyCollection.class);
        intent.putExtra("IS_SEND_COLLECTION", true);
        startActivityForResult(intent, REQUEST_CODE_SEND_COLLECTION);
    }

    private void clickCollectionSend(
            int type,
            String content,
            int timeLen,
            String filePath,
            long fileSize
    ) {
        if (isAuthenticated()) {
            return;
        }

        if (TextUtils.isEmpty(content)) {
            return;
        }
        ChatMessage message = new ChatMessage();
        message.setType(type);
        message.setFromUserId(mLoginUserId);
        message.setFromUserName(mLoginNickName);
        message.setContent(content);
        message.setTimeLen(timeLen);
        message.setFileSize((int) fileSize);
        message.setUpload(true);
        if (!TextUtils.isEmpty(filePath)) {
            message.setFilePath(filePath);
        }
        if (type == XmppMessage.TYPE_VOICE
                || type == XmppMessage.TYPE_IMAGE
                || type == XmppMessage.TYPE_VIDEO) {
            message.setIsReadDel(isReadDel);
        }
        mChatMessages.add(message);
        mChatContentView.notifyDataSetInvalidated(true);
        sendMessage(message);
    }

    private void clickCollectionSend(CollectionEvery collection) {
        // ????????????????????????????????????????????????????????????????????????????????????
        if (!TextUtils.isEmpty(collection.getCollectContent())) {
            sendText(collection.getCollectContent());
        }
        int type = collection.getXmppType();
        if (type == XmppMessage.TYPE_TEXT) {
            // ????????????????????????????????????????????????
            return;
        } else if (type == XmppMessage.TYPE_IMAGE) {
            // ???????????????????????????????????????
            String allUrl = collection.getUrl();
            for (String url : allUrl.split(",")) {
                clickCollectionSend(type, url, collection.getFileLength(), collection.getFileName(), collection.getFileSize());
            }
            return;
        }
        clickCollectionSend(type, collection.getUrl(), collection.getFileLength(), collection.getFileName(), collection.getFileSize());
    }

    @Override
    public void clickShake() {
        if (isAuthenticated()) {
            return;
        }
        ChatMessage message = new ChatMessage();
        message.setType(XmppMessage.TYPE_SHAKE);
        message.setFromUserId(mLoginUserId);
        message.setFromUserName(mLoginNickName);
        message.setContent(getString(R.string.msg_shake));
        mChatMessages.add(message);
        mChatContentView.notifyDataSetInvalidated(true);
        sendMessage(message);
        shake(0);// ???????????????
    }

    @Override
    public void clickGroupAssistant(GroupAssistantDetail groupAssistantDetail) {

    }

    private void shake(int type) {
        Animation shake;
        if (type == 0) {
            shake = AnimationUtils.loadAnimation(this, R.anim.shake_from);
        } else {
            shake = AnimationUtils.loadAnimation(this, R.anim.shake_to);
        }
        mChatContentView.startAnimation(shake);
        mChatBottomView.startAnimation(shake);
        mChatBgIv.startAnimation(shake);
    }

    /**
     * ?????????????????????
     */
    @Override
    public void sendCardS(List<Friend> friends) {
        for (int i = 0; i < friends.size(); i++) {
            sendCard(friends.get(i));
        }
    }

    public void sendCard(Friend friend) {
        if (isAuthenticated()) {
            return;
        }
        ChatMessage message = new ChatMessage();
        message.setType(XmppMessage.TYPE_CARD);
        message.setFromUserId(mLoginUserId);
        message.setFromUserName(mLoginNickName);
        message.setContent(friend.getNickName());
        message.setObjectId(friend.getUserId());
        mChatMessages.add(message);
        mChatContentView.notifyDataSetInvalidated(true);
        sendMessage(message);
    }

    @Override
    public void onInputState() {
        // ??????????????????
        PrivacySetting privacySetting = PrivacySettingHelper.getPrivacySettings(this);
        boolean input = privacySetting.getIsTyping() == 1;
        if (input && coreManager.isLogin()) {
            ChatMessage message = new ChatMessage();
            // ??????????????????
            message.setType(XmppMessage.TYPE_INPUT);
            message.setFromUserId(mLoginUserId);
            message.setFromUserName(mLoginNickName);
            message.setToUserId(mFriend.getUserId());
            message.setTimeSend(TimeUtils.sk_time_current_time());
            message.setPacketId(UUID.randomUUID().toString().replaceAll("-", ""));
            coreManager.sendChatMessage(mFriend.getUserId(), message);
        }
    }

    /**
     * ???????????????
     */
    @Override
    public boolean onNewMessage(String fromUserId, ChatMessage message, boolean isGroupMsg) {
        if (isGroupMsg) {
            return false;
        }

        // ???????????????????????????????????????
//        String titleContent = mTvTitle.getText().toString();
//        if (titleContent.contains(getString(R.string.off_line))) {
//            String changeTitleContent = titleContent.replace(getString(R.string.off_line),
//                    getString(R.string.online));
//            mTvTitle.setText(changeTitleContent);
//        }


        /**
         *  ?????????????????????????????????????????????????????????????????????????????????????????????????????????????????????????????????????????????
         *  ?????????????????????????????????????????????????????????????????????(??????????????????)??????????????????????????????????????????????????????
         *  ??????????????????onNewMessage????????????????????????????????????????????????????????????????????????????????????????????????????????????????????????
         *  ?????????????????????????????????
         *
         */

        if (mChatMessages.size() > 0) {
            if (mChatMessages.get(mChatMessages.size() - 1).getPacketId().equals(message.getPacketId())) {// ?????????????????????msgId==????????????msgId
                Log.e("zq", "????????????????????????");
                return false;
            }
        }

        if (mFriend.getIsDevice() == 1) {// ????????????????????????????????? ????????????????????????????????????????????????????????????
            ChatMessage chatMessage = ChatMessageDao.getInstance().
                    findMsgById(mLoginUserId, mFriend.getUserId(), message.getPacketId());
            if (chatMessage == null) {
                return false;
            }
        }

        /*
         ???????????????????????????????????????????????????????????????????????????????????????????????????????????????????????????????????????????????????????????????????????????
         */
        if (fromUserId.equals(mLoginUserId)
                && !TextUtils.isEmpty(message.getToUserId())
                && message.getToUserId().equals(mFriend.getUserId())) {// ???????????????????????????????????????????????????????????????????????????
            message.setMySend(true);
            message.setMessageState(MESSAGE_SEND_SUCCESS);
            mChatMessages.add(message);
            if (mChatContentView.shouldScrollToBottom()) {
                mChatContentView.notifyDataSetInvalidated(true);
            } else {
                mChatContentView.notifyDataSetChanged();
            }
            if (message.getType() == XmppMessage.TYPE_SHAKE) {// ?????????
                shake(1);
            }
            return true;
        }

        if (mFriend.getUserId().compareToIgnoreCase(fromUserId) == 0) {// ????????????????????????
            mChatMessages.add(message);
            if (mChatContentView.shouldScrollToBottom()) {
                mChatContentView.notifyDataSetInvalidated(true);
            } else {
                // ??????????????????
                Vibrator vibrator = (Vibrator) FLYApplication.getContext().getSystemService(VIBRATOR_SERVICE);
                long[] pattern = {100, 400, 100, 400};
                if (vibrator != null) {
                    vibrator.vibrate(pattern, -1);
                }
                mChatContentView.notifyDataSetChanged();
            }
            if (message.getType() == XmppMessage.TYPE_SHAKE) {// ?????????
                shake(1);
            }
            return true;
        }
        return false;
    }

    @Override
    public void onMessageSendStateChange(int messageState, String msgId) {
        Log.e("zq", messageState + "???" + msgId);
        if (TextUtils.isEmpty(msgId)) {
            return;
        }
        for (int i = 0; i < mChatMessages.size(); i++) {
            ChatMessage msg = mChatMessages.get(i);
            if (msgId.equals(msg.getPacketId())) {
                /**
                 * ??????????????????????????????????????????????????????????????????????????????????????????????????????
                 * ???????????????????????????????????????????????????????????????1???????????????0??????????????????
                 */
                if (msg.getMessageState() == 1) {
                    return;
                }
                msg.setMessageState(messageState);
                //???????????????  ??????????????????  ???????????????????????????????????????????????????
                mChatContentView.notifyDataSetInvalidated(true);
//                mChatContentView.notifyDataSetChanged();
                break;
            }
        }
    }

    /**
     * ?????????com.ktw.fly.ui.me.LocalVideoActivity#helloEventBus(com.ktw.fly.bean.event.MessageVideoFile)
     * ?????????CameraDemoActivity??????????????????activity result, ?????????EventBus,
     */
    @Subscribe(threadMode = ThreadMode.MainThread)
    public void helloEventBus(final EventUploadFileRate message) {
        for (int i = 0; i < mChatMessages.size(); i++) {
            if (mChatMessages.get(i).getPacketId().equals(message.getPacketId())) {
                mChatMessages.get(i).setUploadSchedule(message.getRate());
                // ???????????????setUpload????????????????????????????????????????????????????????????????????????url,????????????????????????
                mChatContentView.notifyDataSetChanged();
                break;
            }
        }
    }

    @Subscribe(threadMode = ThreadMode.MainThread)
    public void helloEventBus(final EventUploadCancel message) {
        for (int i = 0; i < mChatMessages.size(); i++) {
            if (mChatMessages.get(i).getPacketId().equals(message.getPacketId())) {
                mChatMessages.remove(i);
                mChatContentView.notifyDataSetChanged();
                ChatMessageDao.getInstance().deleteSingleChatMessage(mLoginUserId, mFriend.getUserId(), message.getPacketId());
                break;
            }
        }
    }

    @Subscribe(threadMode = ThreadMode.MainThread)
    public void helloEventBus(final MessageVideoFile message) {
        VideoFile videoFile = new VideoFile();
        videoFile.setCreateTime(TimeUtils.f_long_2_str(System.currentTimeMillis()));
        videoFile.setFileLength(message.timelen);
        videoFile.setFileSize(message.length);
        videoFile.setFilePath(message.path);
        videoFile.setOwnerId(coreManager.getSelf().getUserId());
        VideoFileDao.getInstance().addVideoFile(videoFile);
        String filePath = message.path;
        if (TextUtils.isEmpty(filePath)) {
            ToastUtil.showToast(this, R.string.record_failed);
            return;
        }
        File file = new File(filePath);
        if (!file.exists()) {
            ToastUtil.showToast(this, R.string.record_failed);
            return;
        }
        sendVideo(file);
    }

    private void compress(File file) {
        String path = file.getPath();
        DialogHelper.showMessageProgressDialog(this, FLYApplication.getContext().getString(R.string.compressed));
        final String out = RecorderUtils.getVideoFileByTime();
        String[] cmds = RecorderUtils.ffmpegComprerssCmd(path, out);
        long duration = VideoUitls.getDuration(path);

        FFmpegCmd.exec(cmds, duration, new OnEditorListener() {
            public void onSuccess() {
                DialogHelper.dismissProgressDialog();
                File outFile = new File(out);
                runOnUiThread(() -> {
                    if (outFile.exists()) {
                        sendVideo(outFile);
                    } else {
                        sendVideo(file);
                    }
                });
            }

            public void onFailure() {
                DialogHelper.dismissProgressDialog();
                runOnUiThread(() -> {
                    sendVideo(file);
                });
            }

            public void onProgress(float progress) {

            }
        });
    }

    @Subscribe(threadMode = ThreadMode.MainThread)
    public void helloEventBus(final MessageLocalVideoFile message) {
        compress(message.file);
    }

//    @Subscribe(threadMode = ThreadMode.MainThread)
//    public void helloEventBus(EventRedReceived message) {
//        showReceiverRedLocal(message.getOpenRedpacket());
//    }

    @Subscribe(threadMode = ThreadMode.MainThread)
    public void helloEventBus(EventRedReceived message) {
        showReceiverRedLocal(message.getRushRedPacket());
    }


    private void showReceiverRedLocal(RushRedPacket openRedpacket) {
        // ??????????????????????????????
        ChatMessage chatMessage = new ChatMessage();
        chatMessage.setFileSize(XmppMessage.TYPE_83);
        chatMessage.setFilePath(openRedpacket.redUser.redId);
        chatMessage.setFromUserId(mLoginUserId);
        chatMessage.setFromUserName(mLoginNickName);
        chatMessage.setToUserId(mFriend.getUserId());
        chatMessage.setType(XmppMessage.TYPE_TIP);
        chatMessage.setContent(getString(R.string.red_received_self, openRedpacket.redUser.userName));
        chatMessage.setPacketId(UUID.randomUUID().toString().replaceAll("-", ""));
        chatMessage.setTimeSend(TimeUtils.sk_time_current_time());
        if (ChatMessageDao.getInstance().saveNewSingleChatMessage(mLoginUserId, mFriend.getUserId(), chatMessage)) {
            mChatMessages.add(chatMessage);
            mChatContentView.notifyDataSetInvalidated(true);
        }
    }


    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        if (resultCode == Activity.RESULT_OK) {
            switch (requestCode) {
                case REQUEST_CODE_SELECT_FILE: // ???????????????????????????
                    String file_path = FileUtils.getPath(ChatActivity.this, data.getData());
                    Log.e("xuan", "conversionFile: " + file_path);
                    if (file_path == null) {
                        ToastUtil.showToast(mContext, R.string.tip_file_not_supported);
                    } else {
                        sendFile(new File(file_path));
                    }
                    break;
                case REQUEST_CODE_CAPTURE_PHOTO:
                    // ????????????
                    if (mNewPhotoUri != null) {
                        photograph(new File(mNewPhotoUri.getPath()));
                    }
                    break;
                case REQUEST_CODE_PICK_PHOTO:
                    if (data != null) {
                        boolean isOriginal = data.getBooleanExtra(PhotoPickerActivity.EXTRA_RESULT_ORIGINAL, false);
                        album(data.getStringArrayListExtra(PhotoPickerActivity.EXTRA_RESULT), isOriginal);
                    } else {
                        ToastUtil.showToast(this, R.string.c_photo_album_failed);
                    }
                    break;
                case REQUEST_CODE_SELECT_VIDEO: {
                    // ?????????????????????
                    if (data == null) {
                        return;
                    }
                    String json = data.getStringExtra(FLYAppConstant.EXTRA_VIDEO_LIST);
                    List<VideoFile> fileList = JSON.parseArray(json, VideoFile.class);
                    if (fileList == null || fileList.size() == 0) {
                        // ???????????????????????????????????????
                        FLYReporter.unreachable();
                    } else {
                        for (VideoFile videoFile : fileList) {
                            String filePath = videoFile.getFilePath();
                            if (TextUtils.isEmpty(filePath)) {
                                // ???????????????????????????????????????
                                FLYReporter.unreachable();
                            } else {
                                File file = new File(filePath);
                                if (!file.exists()) {
                                    // ???????????????????????????????????????
                                    FLYReporter.unreachable();
                                } else {
                                    sendVideo(file);
                                }
                            }
                        }
                    }
                    break;
                }
                case REQUEST_CODE_SELECT_Locate: // ?????????????????????
                    double latitude = data.getDoubleExtra(FLYAppConstant.EXTRA_LATITUDE, 0);
                    double longitude = data.getDoubleExtra(FLYAppConstant.EXTRA_LONGITUDE, 0);
                    String address = data.getStringExtra(FLYAppConstant.EXTRA_ADDRESS);
                    String snapshot = data.getStringExtra(FLYAppConstant.EXTRA_SNAPSHOT);

                    if (latitude != 0 && longitude != 0 && !TextUtils.isEmpty(address)
                            && !TextUtils.isEmpty(snapshot)) {
                        sendLocate(latitude, longitude, address, snapshot);
                    } else {
                        ToastUtil.showToast(mContext, getString(R.string.loc_startlocnotice));
                    }
                    break;
                case REQUEST_CODE_SEND_COLLECTION: {
                    String json = data.getStringExtra("data");
                    CollectionEvery collection = JSON.parseObject(json, CollectionEvery.class);
                    clickCollectionSend(collection);
                    break;
                }
                case REQUEST_CODE_QUICK_SEND:
                    String image = QuickSendPreviewActivity.parseResult(data);
                    sendImage(new File(image));
                    break;
                case REQUEST_CODE_SEND_CONTACT: {
                    List<Contacts> contactsList = SendContactsActivity.parseResult(data);
                    if (contactsList == null) {
                        ToastUtil.showToast(mContext, R.string.simple_data_error);
                    } else {
                        sendContacts(contactsList);
                    }
                    break;
                }
                default:
                    super.onActivityResult(requestCode, resultCode, data);
            }
        } else {
            switch (requestCode) {
                case REQUEST_CODE_SEND_RED:
                    if (data != null) {
                        ChatMessage chatMessage = new ChatMessage(data.getStringExtra(FLYAppConstant.EXTRA_CHAT_MESSAGE));
                        mChatMessages.add(chatMessage);
                        mChatContentView.notifyDataSetInvalidated(true);
                        sendMessage(chatMessage);
                        // ????????????
                        CoreManager.updateMyBalance();
                    }
                    break;
                default:
                    super.onActivityResult(requestCode, resultCode, data);
            }
        }
    }

    // ?????????????????? ??????
    private void photograph(final File file) {
        Log.e("zq", "?????????????????????:" + file.getPath() + "?????????????????????:" + file.length() / 1024 + "KB");
        // ?????????????????????Luban???????????????
        Luban.with(this)
                .load(file)
                .ignoreBy(100)     // ????????????100kb ?????????
                // .putGear(2)     // ?????????????????????????????????
                // .setTargetDir() // ??????????????????????????????
                .setCompressListener(new OnCompressListener() { // ????????????
                    @Override
                    public void onStart() {
                        Log.e("zq", "????????????");
                    }

                    @Override
                    public void onSuccess(File file) {
                        Log.e("zq", "????????????????????????????????????:" + file.getPath() + "?????????????????????:" + file.length() / 1024 + "KB");
                        sendImage(file);
                    }

                    @Override
                    public void onError(Throwable e) {
                        Log.e("zq", "????????????,????????????");
                        sendImage(file);
                    }
                }).launch();// ????????????
    }

    // ?????????????????? ??????
    private void album(ArrayList<String> stringArrayListExtra, boolean isOriginal) {
        if (isOriginal) {// ????????????????????????
            Log.e("zq", "???????????????????????????????????????");
            for (int i = 0; i < stringArrayListExtra.size(); i++) {
                sendImage(new File(stringArrayListExtra.get(i)));
            }
            Log.e("zq", "???????????????????????????????????????");
            return;
        }

        List<String> list = new ArrayList<>();
        List<File> fileList = new ArrayList<>();
        for (int i = 0; i < stringArrayListExtra.size(); i++) {
            // Luban????????????????????????????????????????????????????????????????????????
            // ???????????????????????????
            // todo luban????????????.gif???????????????????????????.gif??????glide??????????????????gifDrawable????????????????????????,gif???????????????
            List<String> lubanSupportFormatList = Arrays.asList("jpg", "jpeg", "png", "webp");
            boolean support = false;
            for (int j = 0; j < lubanSupportFormatList.size(); j++) {
                if (stringArrayListExtra.get(i).endsWith(lubanSupportFormatList.get(j))) {
                    support = true;
                    break;
                }
            }
            if (!support) {
                list.add(stringArrayListExtra.get(i));
                fileList.add(new File(stringArrayListExtra.get(i)));
            }
        }

        if (fileList.size() > 0) {
            for (File file : fileList) {// ?????????????????????????????????
                sendImage(file);
            }
        }

        stringArrayListExtra.removeAll(list);

        Luban.with(this)
                .load(stringArrayListExtra)
                .ignoreBy(100)// ????????????100kb ?????????
                .setCompressListener(new OnCompressListener() {
                    @Override
                    public void onStart() {
                        Log.e("zq", "????????????");
                    }

                    @Override
                    public void onSuccess(File file) {
                        sendImage(file);
                    }

                    @Override
                    public void onError(Throwable e) {

                    }
                }).launch();// ????????????
    }

    /*******************************************
     * ?????????EventBus??????????????????
     ******************************************/
    @Subscribe(threadMode = ThreadMode.MainThread)
    public void helloEventBus(final MessageEventRequert message) {
        requstImageText(message.url);
    }

    private void requstImageText(String url) {
        HttpUtils.get().url(url).build().execute(new BaseCallback<Void>(Void.class) {

            @Override
            public void onResponse(ObjectResult<Void> result) {

            }

            @Override
            public void onError(Call call, Exception e) {

            }
        });
    }

    @Subscribe(threadMode = ThreadMode.MainThread)
    public void helloEventBus(EventNotifyByTag message) {
        if (TextUtils.equals(message.tag, EventNotifyByTag.Speak)) {
            boolean isSpeaker = PreferenceUtils.getBoolean(FLYApplication.getContext(),
                    Constants.SPEAKER_AUTO_SWITCH + CoreManager.requireSelf(FLYApplication.getContext()).getUserId(), true);
            findViewById(R.id.iv_title_center).setVisibility(isSpeaker ? View.GONE : View.VISIBLE);
            if (VoiceManager.instance().getMediaPlayer().isPlaying()) {
                // ?????????????????????????????????????????????????????????????????????????????????????????????
                if (!isSpeaker) {
                    VoiceManager.instance().earpieceUser();
                }
                mAudioModeManger.setSpeakerPhoneOn(isSpeaker);
                if (!isSpeaker) {
                    mTvTitle.postDelayed(() -> VoiceManager.instance().earpieceUser(), 200);
                }
            } else {
                mAudioModeManger.setSpeakerPhoneOn(isSpeaker);
            }
        }
    }

    @Subscribe(threadMode = ThreadMode.MainThread)
    public void helloEventBus(final MessageEventGpu message) {// ????????????
        photograph(new File(message.event));
    }

    @Subscribe(threadMode = ThreadMode.MainThread)
    public void helloEventBus(final EventTransfer message) {
        mChatContentView.postDelayed(() -> {
            if (message.getChatMessage().getType() == XmppMessage.TYPE_TRANSFER) {// ??????????????????
                mChatMessages.add(message.getChatMessage());
                mChatContentView.notifyDataSetInvalidated(true);
                sendMessage(message.getChatMessage());
            } else if (message.getChatMessage().getType() == XmppMessage.TYPE_TRANSFER_RECEIVE) {// ???????????????
                // ????????????????????????????????????????????????????????????
                String id = message.getChatMessage().getContent();
                for (int i1 = 0; i1 < mChatMessages.size(); i1++) {
                    if (TextUtils.equals(mChatMessages.get(i1).getObjectId(), id)) {
                        mChatMessages.get(i1).setFileSize(2);
                    }
                }
                mChatContentView.notifyDataSetChanged();
            } else {// ?????????????????? || ????????????
                for (int i = 0; i < mChatMessages.size(); i++) {
                    if (TextUtils.equals(mChatMessages.get(i).getPacketId(),
                            message.getChatMessage().getPacketId())) {
                        if (message.getChatMessage().getType() == TransferMoneyDetailActivity.EVENT_REISSUE_TRANSFER) {
                            ChatMessage chatMessage = mChatMessages.get(i).clone(false);
                            mChatMessages.add(chatMessage);
                            mChatContentView.notifyDataSetInvalidated(true);
                            sendMessage(chatMessage);
                        } else {
                            // ????????????????????????????????????????????????????????????
                            String id = mChatMessages.get(i).getObjectId();
                            for (int i1 = 0; i1 < mChatMessages.size(); i1++) {
                                if (TextUtils.equals(mChatMessages.get(i1).getObjectId(), id)) {
                                    mChatMessages.get(i1).setFileSize(2);
                                    ChatMessageDao.getInstance().updateChatMessageReceiptStatus(mLoginUserId, mFriend.getUserId(), mChatMessages.get(i1).getPacketId());
                                }
                            }
                            mChatContentView.notifyDataSetChanged();
                        }
                    }
                }
            }
        }, 50);
    }

    @Subscribe(threadMode = ThreadMode.MainThread)
    public void helloEventBus(final MessageEvent message) {
        Log.e("xuan", "helloEventBus  MessageEvent: " + message.message);
        if (mDelayDelMaps == null || mDelayDelMaps.isEmpty() || mChatMessages == null || mChatMessages.size() == 0) {
            return;
        }

        for (ChatMessage chatMessage : mChatMessages) {
            if (chatMessage.getFilePath().equals(message.message) && mDelayDelMaps.contains(chatMessage.getPacketId())) {
                String packedId = chatMessage.getPacketId();

                if (ChatMessageDao.getInstance().deleteSingleChatMessage(mLoginUserId, mFriend.getUserId(), packedId)) {
                    Log.e("xuan", "???????????? ");
                } else {
                    Log.e("xuan", "???????????? " + packedId);
                }
                mDelayDelMaps.remove(packedId);
                mChatContentView.removeItemMessage(packedId);
                break;
            }
        }
    }

    // ?????????????????????
    @Subscribe(threadMode = ThreadMode.MainThread)
    public void helloEventBus(final MessageEventClickFire message) {
        Log.e("xuan", "helloEventBus: " + message.event + " ,  " + message.packedId);
        if ("delete".equals(message.event)) {
            mDelayDelMaps.remove(message.packedId);
            ChatMessageDao.getInstance().deleteSingleChatMessage(mLoginUserId, mFriend.getUserId(), message.packedId);
            mChatContentView.removeItemMessage(message.packedId);
        } else if ("delay".equals(message.event)) {
            mDelayDelMaps.add(message.packedId);
        }
    }

    @Subscribe(threadMode = ThreadMode.MainThread)
    public void helloEventBus(final MessageEventSipEVent message02) {
        if (message02.number == 102) {
            // ????????????  ??????????????????
            EventBus.getDefault().post(new MessageEventSipPreview(200, mFriend.getUserId(), true, mFriend, message02.message));
        } else if (message02.number == 112) {
            // ????????????  ??????????????????
            EventBus.getDefault().post(new MessageEventSipPreview(201, mFriend.getUserId(), false, mFriend, message02.message));
        } else if (message02.number == 132) {
            // ????????????  ?????????????????????
            EventBus.getDefault().post(new MessageEventSipPreview(202, mFriend.getUserId(), false, mFriend, message02.message));
        }
    }

    // ??????????????? ????????????
    @Subscribe(threadMode = ThreadMode.MainThread)
    public void helloEventBus(final MessageEventClicAudioVideo message) {
        if (message.isauido == 0) {// ????????????
            Jitsi_connecting_second.start(this, message.event.getObjectId(), coreManager.getSelf().getUserId(), 3);
        } else if (message.isauido == 1) {// ????????????
            Jitsi_connecting_second.start(this, message.event.getObjectId(), coreManager.getSelf().getUserId(), 4);
        }
    }

    @Subscribe(threadMode = ThreadMode.MainThread)
    public void helloEventBus(final MessageEventClickable message) {
        if (message.event.isMySend()) {
            shake(0);
        } else {
            shake(1);
        }
    }

    // ??????????????????
    @SuppressLint("StringFormatInvalid")
    @Subscribe(threadMode = ThreadMode.MainThread)
    public void helloEventBus(final EventMoreSelected message) {
        List<ChatMessage> mSelectedMessageList = new ArrayList<>();
        if (message.getToUserId().equals("MoreSelectedCollection") || message.getToUserId().equals("MoreSelectedEmail")) {// ?????? ?????? || ??????
            moreSelected(false, 0);
            return;
        }
        if (message.getToUserId().equals("MoreSelectedDelete")) {// ?????? ??????
            for (int i = 0; i < mChatMessages.size(); i++) {
                if (mChatMessages.get(i).isMoreSelected) {
                    if (ChatMessageDao.getInstance().deleteSingleChatMessage(mLoginUserId, mFriend.getUserId(), mChatMessages.get(i))) {
                        Log.e("more_selected", "????????????");
                    } else {
                        Log.e("more_selected", "????????????");
                    }
                    mSelectedMessageList.add(mChatMessages.get(i));
                }
            }

            String mMsgIdListStr = "";
            for (int i = 0; i < mSelectedMessageList.size(); i++) {
                if (i == mSelectedMessageList.size() - 1) {
                    mMsgIdListStr += mSelectedMessageList.get(i).getPacketId();
                } else {
                    mMsgIdListStr += mSelectedMessageList.get(i).getPacketId() + ",";
                }
            }
            deleteMessage(mMsgIdListStr);// ????????????????????????

            mChatMessages.removeAll(mSelectedMessageList);
        } else {// ?????? ??????
            if (message.isSingleOrMerge()) {// ????????????
                List<String> mStringHistory = new ArrayList<>();
                for (int i = 0; i < mChatMessages.size(); i++) {
                    if (mChatMessages.get(i).isMoreSelected) {
                        String body = mChatMessages.get(i).toJsonString();
                        mStringHistory.add(body);
                    }
                }
                String detail = JSON.toJSONString(mStringHistory);
                ChatMessage chatMessage = new ChatMessage();
                chatMessage.setType(XmppMessage.TYPE_CHAT_HISTORY);
                chatMessage.setFromUserId(mLoginUserId);
                chatMessage.setFromUserName(mLoginNickName);
                chatMessage.setToUserId(message.getToUserId());
                chatMessage.setContent(detail);
                chatMessage.setMySend(true);
                chatMessage.setReSendCount(0);
                chatMessage.setSendRead(false);
                chatMessage.setIsEncrypt(0);
                chatMessage.setIsReadDel(0);
                String s = TextUtils.isEmpty(mFriend.getRemarkName()) ? mFriend.getNickName() : mFriend.getRemarkName();
                chatMessage.setObjectId(getString(R.string.chat_history_place_holder, s, mLoginNickName));
                chatMessage.setTimeSend(TimeUtils.sk_time_current_time());
                chatMessage.setPacketId(UUID.randomUUID().toString().replaceAll("-", ""));
                ChatMessageDao.getInstance().saveNewSingleChatMessage(mLoginUserId, message.getToUserId(), chatMessage);
                if (message.isGroupMsg()) {
                    coreManager.sendMucChatMessage(message.getToUserId(), chatMessage);
                } else {
                    coreManager.sendChatMessage(message.getToUserId(), chatMessage);
                }
                if (message.getToUserId().equals(mFriend.getUserId())) {// ?????????????????????
                    mChatMessages.add(chatMessage);
                }
            } else {// ????????????
                for (int i = 0; i < mChatMessages.size(); i++) {
                    if (mChatMessages.get(i).isMoreSelected) {
                        ChatMessage chatMessage = ChatMessageDao.getInstance().findMsgById(mLoginUserId, mFriend.getUserId(), mChatMessages.get(i).getPacketId());
                        if (chatMessage.getType() == XmppMessage.TYPE_RED) {
                            chatMessage.setType(XmppMessage.TYPE_TEXT);
                            chatMessage.setContent(getString(R.string.msg_red_packet));
                        } else if (chatMessage.getType() >= XmppMessage.TYPE_IS_CONNECT_VOICE
                                && chatMessage.getType() <= XmppMessage.TYPE_EXIT_VOICE) {
                            chatMessage.setType(XmppMessage.TYPE_TEXT);
                            chatMessage.setContent(getString(R.string.msg_video_voice));
                        } else if (chatMessage.getType() == XmppMessage.TYPE_SHAKE) {
                            chatMessage.setType(XmppMessage.TYPE_TEXT);
                            chatMessage.setContent(getString(R.string.msg_shake));
                        } else if (chatMessage.getType() == XmppMessage.TYPE_TRANSFER) {
                            chatMessage.setType(XmppMessage.TYPE_TEXT);
                            chatMessage.setContent(getString(R.string.tip_transfer_money));
                        }
                        chatMessage.setFromUserId(mLoginUserId);
                        chatMessage.setFromUserName(mLoginNickName);
                        chatMessage.setToUserId(message.getToUserId());
                        chatMessage.setUpload(true);
                        chatMessage.setMySend(true);
                        chatMessage.setReSendCount(0);
                        chatMessage.setSendRead(false);
                        chatMessage.setIsEncrypt(0);
                        chatMessage.setTimeSend(TimeUtils.sk_time_current_time());
                        chatMessage.setPacketId(UUID.randomUUID().toString().replaceAll("-", ""));
                        mSelectedMessageList.add(chatMessage);
                    }
                }

                for (int i = 0; i < mSelectedMessageList.size(); i++) {
                    ChatMessageDao.getInstance().saveNewSingleChatMessage(mLoginUserId, message.getToUserId(), mSelectedMessageList.get(i));
                    if (message.isGroupMsg()) {
                        coreManager.sendMucChatMessage(message.getToUserId(), mSelectedMessageList.get(i));
                    } else {
                        coreManager.sendChatMessage(message.getToUserId(), mSelectedMessageList.get(i));
                    }
                    if (message.getToUserId().equals(mFriend.getUserId())) {// ?????????????????????
                        mChatMessages.add(mSelectedMessageList.get(i));
                    }
                }
            }
        }
        moreSelected(false, 0);
    }

    public void moreSelected(boolean isShow, int position) {
        mChatBottomView.showMoreSelectMenu(isShow);
        if (isShow) {
            findViewById(R.id.iv_title_left).setVisibility(View.GONE);
            mTvTitleLeft.setVisibility(View.VISIBLE);
            if (!mChatMessages.get(position).getIsReadDel()) {// ????????????????????????????????????
                mChatMessages.get(position).setMoreSelected(true);
            }
        } else {
            findViewById(R.id.iv_title_left).setVisibility(View.VISIBLE);
            mTvTitleLeft.setVisibility(View.GONE);
            for (int i = 0; i < mChatMessages.size(); i++) {
                mChatMessages.get(i).setMoreSelected(false);
            }
        }
        mChatContentView.setIsShowMoreSelect(isShow);
        mChatContentView.notifyDataSetChanged();
    }

    @Subscribe(threadMode = ThreadMode.MainThread)
    public void helloEventBus(final MessageUploadChatRecord message) {
        try {
            final CreateCourseDialog dialog = new CreateCourseDialog(this, new CreateCourseDialog.CoureseDialogConfirmListener() {
                @Override
                public void onClick(String content) {
                    upLoadChatList(message.chatIds, content);
                }
            });

            dialog.show();
        } catch (Exception e) {
            // ???????????????????????????layout?????????????????????????????????findViewById???????????????
            FLYReporter.unreachable(e);
        }
    }

    @Subscribe(threadMode = ThreadMode.MainThread)
    public void helloEventBus(final EventSyncFriendOperating message) {
        if (TextUtils.equals(message.getToUserId(), mFriend.getUserId())) {
            // attention??????????????????finish??????????????????????????????/??????????????????/?????????????????????????????????????????????????????????????????????????????????????????????status???
            // ????????????onDestroy????????????onSaveConten??????
            isUserSaveContentMethod = false;
            finish();
        }
    }

    private void upLoadChatList(String chatIds, String name) {
        Map<String, String> params = new HashMap<>();
        params.put("access_token", coreManager.getSelfStatus().accessToken);
        params.put("messageIds", chatIds);
        params.put("userId", mLoginUserId);
        params.put("courseName", name);
        params.put("createTime", TimeUtils.sk_time_current_time() + "");
        DialogHelper.showDefaulteMessageProgressDialog(this);
        HttpUtils.get().url(coreManager.getConfig().USER_ADD_COURSE)
                .params(params)
                .build()
                .execute(new BaseCallback<Void>(Void.class) {

                    @Override
                    public void onResponse(ObjectResult<Void> result) {
                        DialogHelper.dismissProgressDialog();
                        ToastUtil.showToast(getApplicationContext(), getString(R.string.tip_create_cource_success));
                    }

                    @Override
                    public void onError(Call call, Exception e) {
                        DialogHelper.dismissProgressDialog();
                        ToastUtil.showErrorNet(mContext);
                    }
                });
    }

    private void register() {
        IntentFilter intentFilter = new IntentFilter();
        intentFilter.addAction(com.ktw.bitbit.broadcast.OtherBroadcast.IsRead);
        intentFilter.addAction("Refresh");
        intentFilter.addAction(com.ktw.bitbit.broadcast.OtherBroadcast.TYPE_INPUT);
        intentFilter.addAction(com.ktw.bitbit.broadcast.OtherBroadcast.MSG_BACK);
        intentFilter.addAction(com.ktw.bitbit.broadcast.OtherBroadcast.NAME_CHANGE);
        intentFilter.addAction(com.ktw.bitbit.broadcast.OtherBroadcast.MULTI_LOGIN_READ_DELETE);
        intentFilter.addAction(Constants.CHAT_MESSAGE_DELETE_ACTION);
        intentFilter.addAction(Constants.SHOW_MORE_SELECT_MENU);
        intentFilter.addAction(com.ktw.bitbit.broadcast.OtherBroadcast.TYPE_DELALL);
        intentFilter.addAction(Constants.CHAT_HISTORY_EMPTY);
        intentFilter.addAction(com.ktw.bitbit.broadcast.OtherBroadcast.QC_FINISH);
        registerReceiver(receiver, intentFilter);
    }

    /*******************************************
     * ?????????ActionBar??????????????????
     ******************************************/
    private void initActionBar() {
        getSupportActionBar().hide();
        findViewById(R.id.iv_title_left).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                doBack();
            }
        });

        mTvTitleLeft = (TextView) findViewById(R.id.tv_title_left);
        mTvTitleLeft.setVisibility(View.GONE);
        mTvTitleLeft.setText(getString(R.string.cancel));
        mTvTitleLeft.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                moreSelected(false, 0);
            }
        });
        mTvTitle = (TextView) findViewById(R.id.tv_title_center);
        String remarkName = mFriend.getRemarkName();
        if (TextUtils.isEmpty(remarkName)) {
            mTvTitle.setText(mFriend.getNickName());
        } else {
            mTvTitle.setText(remarkName);
        }

        ImageView mMore = (ImageView) findViewById(R.id.iv_title_right);
        mMore.setImageResource(R.mipmap.set_icon);
        mMore.setOnClickListener(new NoDoubleClickListener() {
            @Override
            public void onNoDoubleClick(View view) {
                mChatBottomView.reset();
                mChatBottomView.postDelayed(new Runnable() {
                    @Override
                    public void run() {
                        Intent intent = new Intent(ChatActivity.this, PersonSettingActivity.class);
                        intent.putExtra("ChatObjectId", mFriend.getUserId());
                        startActivity(intent);
                    }
                }, 100);
            }
        });

        if ((mFriend.getStatus() != Friend.STATUS_FRIEND || mFriend.getIsDevice() == 1)) {// ?????????/????????? || ???????????? ?????????????????????
            mMore.setVisibility(View.GONE);
        }

        // ??????????????????
        mChatBgIv = findViewById(R.id.chat_bg);
        loadBackdrop();
    }

    public void loadBackdrop() {
        String mChatBgPath = PreferenceUtils.getString(this, Constants.SET_CHAT_BACKGROUND_PATH
                + mFriend.getUserId() + mLoginUserId, "reset");

        String mChatBg = PreferenceUtils.getString(this, Constants.SET_CHAT_BACKGROUND
                + mFriend.getUserId() + mLoginUserId, "reset");

        if (TextUtils.isEmpty(mChatBgPath)
                || mChatBg.equals("reset")) {// ????????????????????????????????????????????????
            mChatBgIv.setImageDrawable(null);
            return;
        }

        File file = new File(mChatBgPath);
        if (file.exists()) {// ????????????
            if (mChatBgPath.toLowerCase().endsWith("gif")) {
                try {
                    GifDrawable gifDrawable = new GifDrawable(file);
                    mChatBgIv.setImageDrawable(gifDrawable);
                } catch (IOException e) {
                    e.printStackTrace();
                }
            } else {
                ImageLoadHelper.showFileWithError(
                        ChatActivity.this,
                        file,
                        R.drawable.fez,
                        mChatBgIv
                );
            }
        } else {// ????????????
            ImageLoadHelper.showImageWithError(
                    ChatActivity.this,
                    mChatBg,
                    R.color.chat_bg,
                    mChatBgIv
            );
        }
    }

    /*******************************************
     * ?????????????????????&&????????????????????????
     ******************************************/
    private void initFriendState() {
        if (mFriend.getIsDevice() == 1) {
            return;
        }

        Map<String, String> params = new HashMap<>();
        params.put("access_token", coreManager.getSelfStatus().accessToken);
        params.put("userId", mFriend.getUserId());

        HttpUtils.get().url(coreManager.getConfig().USER_GET_URL)
                .params(params)
                .build()
                .execute(new BaseCallback<User>(User.class) {
                    @Override
                    public void onResponse(ObjectResult<User> result) {
                        if (result.getResultCode() == 1 && result.getData() != null) {
                            User user = result.getData();
                            if (coreManager.getConfig().enableMpModule && user.getUserType() == 2) {
                                // ?????????,?????????????????????
                                initSpecialMenu();
                                FriendDao.getInstance().updateFriendStatus(mLoginUserId, userId, Friend.STATUS_SYSTEM);
                                return;
                            }

                            String title = TextUtils.isEmpty(mFriend.getRemarkName()) ? mFriend.getNickName() : mFriend.getRemarkName();
                            if (coreManager.getConfig().isOpenOnlineStatus) {
                                switch (user.getOnlinestate()) {
                                    case 0:
                                        title += getString(R.string.status_offline);
                                        break;
                                    case 1:
                                        title += getString(R.string.status_online);

                                        break;
                                }
                            }

                            mTvTitle.setText(title);

                            if (user.getFriends() != null) {// ??????????????????????????? && ????????????????????????...
                                FriendDao.getInstance().updateFriendPartStatus(mFriend.getUserId(), user);
                            }
                        }
                    }

                    @Override
                    public void onError(Call call, Exception e) {
                        ToastUtil.showErrorNet(mContext);
                    }
                });
    }

    private void initSpecialMenu() {
        Map<String, String> params = new HashMap<>();
        params.put("access_token", coreManager.getSelfStatus().accessToken);
        params.put("userId", mFriend.getUserId());

        HttpUtils.get().url(coreManager.getConfig().USER_GET_PUBLIC_MENU)
                .params(params)
                .build()
                .execute(new ListCallback<PublicMenu>(PublicMenu.class) {
                    @Override
                    public void onResponse(ArrayResult<PublicMenu> result) {
                        if (Result.checkSuccess(mContext, result)) {
                            List<PublicMenu> data = result.getData();
                            if (data != null && data.size() > 0) {
                                mChatBottomView.fillRoomMenu(data);
                            }
                        }
                    }

                    @Override
                    public void onError(Call call, Exception e) {

                    }
                });
    }

    public void synchronizeChatHistory() {
        // ??????????????????????????????????????????????????????????????????????????????????????????????????????
        // ?????????????????????????????????????????????
        mChatContentView.setNeedRefresh(false);

        long startTime;
        String chatSyncTimeLen = String.valueOf(PrivacySettingHelper.getPrivacySettings(this).getChatSyncTimeLen());
        if (Double.parseDouble(chatSyncTimeLen) == -2) {// ?????????
            mChatContentView.setNeedRefresh(true);
            FriendDao.getInstance().updateDownloadTime(mLoginUserId, mFriend.getUserId(), mFriend.getTimeSend());
            return;
        }
        if (Double.parseDouble(chatSyncTimeLen) == -1 || Double.parseDouble(chatSyncTimeLen) == 0) {// ?????? ?????? startTime == downloadTime
            startTime = mFriend.getDownloadTime();
        } else {
            long syncTimeLen = (long) (Double.parseDouble(chatSyncTimeLen) * 24 * 60 * 60 * 1000);// ????????????????????????
            if (mFriend.getTimeSend() - mFriend.getDownloadTime() <= syncTimeLen) {// ???????????????????????????
                startTime = mFriend.getDownloadTime();
            } else {// ??????????????????????????????????????????????????????
                startTime = mFriend.getTimeSend() - syncTimeLen;
            }
        }

        Map<String, String> params = new HashMap();
        params.put("access_token", coreManager.getSelfStatus().accessToken);
        params.put("receiver", mFriend.getUserId());
        params.put("startTime", String.valueOf(startTime));// 2010-01-01 00:00:00  ???????????????????????????????????????
        params.put("endTime", String.valueOf(mFriend.getTimeSend()));
        params.put("pageSize", String.valueOf(Constants.MSG_ROMING_PAGE_SIZE));// ???????????????????????? ??????????????????
        // params.put("pageIndex", "0");

        HttpUtils.get().url(coreManager.getConfig().GET_CHAT_MSG)
                .params(params)
                .build()
                .execute(new ListCallback<ChatRecord>(ChatRecord.class) {
                    @Override
                    public void onResponse(ArrayResult<ChatRecord> result) {
                        FriendDao.getInstance().updateDownloadTime(mLoginUserId, mFriend.getUserId(), mFriend.getTimeSend());

                        final List<ChatRecord> chatRecordList = result.getData();
                        if (chatRecordList != null && chatRecordList.size() > 0) {
                            new Thread(() -> {
                                chatMessages = new ArrayList<>();

                                for (int i = 0; i < chatRecordList.size(); i++) {
                                    ChatRecord data = chatRecordList.get(i);
                                    String messageBody = data.getMessage();
                                    messageBody = messageBody.replaceAll("&quot;", "\"");
                                    ChatMessage chatMessage = jsonToMessage(messageBody);

                                    if (!TextUtils.isEmpty(chatMessage.getFromUserId()) &&
                                            chatMessage.getFromUserId().equals(mLoginUserId)) {
                                        chatMessage.setMySend(true);
                                    }

                                    chatMessage.setSendRead(data.getIsRead() > 0); // ???????????????????????????????????????
                                    // ????????????????????????
                                    chatMessage.setUpload(true);
                                    chatMessage.setUploadSchedule(100);
                                    chatMessage.setMessageState(MESSAGE_SEND_SUCCESS);

                                    if (TextUtils.isEmpty(chatMessage.getPacketId())) {
                                        if (!TextUtils.isEmpty(data.getMessageId())) {
                                            chatMessage.setPacketId(data.getMessageId());
                                        } else {
                                            chatMessage.setPacketId(UUID.randomUUID().toString().replaceAll("-", ""));
                                        }
                                    }

                                    if (ChatMessageDao.getInstance().roamingMessageFilter(chatMessage.getType())) {
                                        ChatMessageDao.getInstance().decryptDES(chatMessage);
                                        ChatMessageDao.getInstance().handlerRoamingSpecialMessage(chatMessage);
                                        if (ChatMessageDao.getInstance().saveNewSingleChatMessage(mLoginUserId, mFriend.getUserId(), chatMessage)) {
                                            chatMessages.add(chatMessage);
                                        }
                                    }
                                }

                                mTvTitle.post(() -> {
                                    for (int i = chatMessages.size() - 1; i >= 0; i--) {
                                        mChatMessages.add(chatMessages.get(i));
                                    }
                                    // ????????????????????????????????????????????????????????????mChatMessages????????????
                                    Comparator<ChatMessage> comparator = (c1, c2) -> (int) (c1.getDoubleTimeSend() - c2.getDoubleTimeSend());
                                    Collections.sort(mChatMessages, comparator);
                                    mChatContentView.notifyDataSetInvalidated(true);

                                    mChatContentView.setNeedRefresh(true);
                                });
                            }).start();
                        } else {
                            mChatContentView.setNeedRefresh(true);
                        }
                    }

                    @Override
                    public void onError(Call call, Exception e) {
                        mChatContentView.setNeedRefresh(true);
                        ToastUtil.showErrorData(ChatActivity.this);
                    }
                });
    }

    public void getNetSingle() {
        Map<String, String> params = new HashMap();
        long endTime;
        if (mChatMessages != null && mChatMessages.size() > 0) {// ???????????????????????????????????????????????????????????????timeSend
            endTime = mChatMessages.get(0).getTimeSend();
        } else {// ?????????????????????????????????????????????
            endTime = TimeUtils.sk_time_current_time();
        }

        params.put("access_token", coreManager.getSelfStatus().accessToken);
        params.put("receiver", mFriend.getUserId());
        params.put("startTime", "1262275200000");// 2010-01-01 00:00:00  ???????????????????????????????????????
        params.put("endTime", String.valueOf(endTime));
        params.put("pageSize", String.valueOf(Constants.MSG_ROMING_PAGE_SIZE));
        params.put("pageIndex", "0");

        HttpUtils.get().url(coreManager.getConfig().GET_CHAT_MSG)
                .params(params)
                .build()
                .execute(new ListCallback<ChatRecord>(ChatRecord.class) {
                    @Override
                    public void onResponse(ArrayResult<ChatRecord> result) {
                        List<ChatRecord> chatRecordList = result.getData();

                        if (chatRecordList != null && chatRecordList.size() > 0) {
                            long currTime = TimeUtils.sk_time_current_time();
                            for (int i = 0; i < chatRecordList.size(); i++) {
                                ChatRecord data = chatRecordList.get(i);
                                String messageBody = data.getMessage();
                                // messageBody = messageBody.replaceAll("&quot;", "\"");
                                ChatMessage chatMessage = jsonToMessage(messageBody);

                                // ?????????????????????????????????1?????????????????????????????????????????????????????????????????????????????????
                                if (chatMessage.getDeleteTime() > 1 && chatMessage.getDeleteTime() < currTime / 1000) {
                                    // ??????????????????,??????
                                    continue;
                                }

                                if (!TextUtils.isEmpty(chatMessage.getFromUserId()) &&
                                        chatMessage.getFromUserId().equals(mLoginUserId)) {
                                    chatMessage.setMySend(true);
                                }

                                chatMessage.setSendRead(data.getIsRead() > 0); // ???????????????????????????????????????
                                // ????????????????????????
                                chatMessage.setUpload(true);
                                chatMessage.setUploadSchedule(100);
                                chatMessage.setMessageState(MESSAGE_SEND_SUCCESS);

                                if (TextUtils.isEmpty(chatMessage.getPacketId())) {
                                    if (!TextUtils.isEmpty(data.getMessageId())) {
                                        chatMessage.setPacketId(data.getMessageId());
                                    } else {
                                        chatMessage.setPacketId(UUID.randomUUID().toString().replaceAll("-", ""));
                                    }
                                }

                                if (ChatMessageDao.getInstance().roamingMessageFilter(chatMessage.getType())) {
                                    ChatMessageDao.getInstance().saveRoamingChatMessage(mLoginUserId, mFriend.getUserId(), chatMessage);
                                }
                            }
                            mHasMoreData = chatRecordList.size() >= mPageSize;
                            notifyChatAdapter();
                        } else {
                            mHasMoreData = false;
                            mChatContentView.headerRefreshingCompleted();
                            mChatContentView.setNeedRefresh(false);
                        }
                    }

                    @Override
                    public void onError(Call call, Exception e) {

                    }
                });
    }

    public ChatMessage jsonToMessage(String json) {
        Gson gson = new Gson();
        com.ktw.bitbit.socket.msg.ChatMessage chatMessage = gson.fromJson(json, com.ktw.bitbit.socket.msg.ChatMessage.class);
        return chatMessage.toSkMessage(mLoginUserId);
    }

    private void notifyChatAdapter() {
        if (mChatMessages.size() > 0) {
            mMinId = mChatMessages.get(0).getTimeSend();
        } else {
            ChatMessage chat = ChatMessageDao.getInstance().getLastChatMessage(mLoginUserId, mFriend.getUserId());
            if (chat != null && chat.getTimeSend() != 0) {
                mMinId = chat.getTimeSend() + 2;
            } else {
                mMinId = TimeUtils.sk_time_current_time();
            }
        }
        // ?????????????????????????????? mMinId ?????????????????????????????????????????????????????????????????? mMinId ?????????????????????
        List<ChatMessage> chatLists = ChatMessageDao.getInstance().getSingleChatMessages(mLoginUserId,
                mFriend.getUserId(), mMinId, mPageSize);
        if (chatLists == null || chatLists.size() == 0) {
            mHasMoreData = false;
            mChatContentView.headerRefreshingCompleted();
            mChatContentView.setNeedRefresh(false);
            return;
        }

        for (int i = 0; i < chatLists.size(); i++) {
            ChatMessage message = chatLists.get(i);
            mChatMessages.add(0, message);
        }

        // ??????timeSend????????????
       /* Collections.sort(mChatMessages, new Comparator<ChatMessage>() {
            @Override
            public int compare(ChatMessage o1, ChatMessage o2) {
                return (int) (o1.getDoubleTimeSend() - o2.getDoubleTimeSend());
            }
        });*/

        mChatContentView.notifyDataSetAddedItemsToTop(chatLists.size());
        mChatContentView.headerRefreshingCompleted();
        if (!mHasMoreData) {
            mChatContentView.setNeedRefresh(false);
        }
    }

    /*******************************************
     * ??????&&??????
     ******************************************/
    private void instantChatMessage() {
        if (!TextUtils.isEmpty(instantMessage)) {
            new Handler().postDelayed(new Runnable() {
                @Override
                public void run() {
                    String toUserId = getIntent().getStringExtra("fromUserId");
                    ChatMessage chatMessage = ChatMessageDao.getInstance().findMsgById(mLoginUserId, toUserId, instantMessage);
                    TrillStatisticsHelper.share(mContext, coreManager, chatMessage);
                    chatMessage.setFromUserId(mLoginUserId);
                    chatMessage.setFromUserName(mLoginNickName);
                    chatMessage.setToUserId(mFriend.getUserId());
                    chatMessage.setUpload(true);
                    chatMessage.setMySend(true);
                    chatMessage.setReSendCount(5);
                    chatMessage.setSendRead(false);
                    // ???????????????????????????????????????????????????????????????????????????content??????????????????????????????????????????isEncrypt??????????????????
                    // ?????????????????????????????????????????????????????????????????????????????????????????????????????????
                    chatMessage.setIsEncrypt(0);
                    chatMessage.setTimeSend(TimeUtils.sk_time_current_time());
                    chatMessage.setPacketId(UUID.randomUUID().toString().replaceAll("-", ""));
                    mChatMessages.add(chatMessage);
                    mChatContentView.notifyDataSetInvalidated(true);
                    ChatMessageDao.getInstance().saveNewSingleChatMessage(mLoginUserId, mFriend.getUserId(), chatMessage);
                    coreManager.sendChatMessage(mFriend.getUserId(), chatMessage);
                    instantMessage = null;
                }
            }, 1000);
        }
    }

    public boolean interprect() {
        for (Friend friend : mBlackList) {
            if (friend.getUserId().equals(mFriend.getUserId())) {
                return true;
            }
        }
        return false;
    }

    /*******************************************
     * ????????????&&??????
     ******************************************/
    public boolean isAuthenticated() {
        boolean isLogin = coreManager.isLogin();
        if (!isLogin) {
            coreManager.autoReconnect(this);
        }
        //  ???????????????????????????return???????????????...??????????????????(?????????)
        // ???????????????????????????CoreService?????????????????????????????????????????????????????????????????????
        return !coreManager.isServiceReady();
    }

    /*******************************************
     * ?????????????????????????????????
     ******************************************/
    public class RefreshBroadcastReceiver extends BroadcastReceiver {
        @Override
        public void onReceive(Context context, Intent intent) {
            String action = intent.getAction();
            if (action.equals(com.ktw.bitbit.broadcast.OtherBroadcast.IsRead)) {
                // ????????????????????? ??????
                Bundle bundle = intent.getExtras();
                String packetId = bundle.getString("packetId");
                boolean isReadChange = bundle.getBoolean("isReadChange");
                for (int i = 0; i < mChatMessages.size(); i++) {
                    ChatMessage msg = mChatMessages.get(i);
                    if (msg.getPacketId().equals(packetId)) {
                        msg.setSendRead(true);// ???????????????
                        if (isReadChange) {// ?????????????????? ??????????????????????????????
                            ChatMessage msgById = ChatMessageDao.getInstance().findMsgById(mLoginUserId, mFriend.getUserId(), packetId);
                            if (msgById != null) {
                                if (msg.getType() == XmppMessage.TYPE_VOICE) {
                                    if (!TextUtils.isEmpty(VoicePlayer.instance().getVoiceMsgId())
                                            && packetId.equals(VoicePlayer.instance().getVoiceMsgId())) {// ??????????????????????????????????????????... ??????????????????
                                        VoicePlayer.instance().stop();
                                    }
                                } else if (msg.getType() == XmppMessage.TYPE_VIDEO) {
                                    if (!TextUtils.isEmpty(JCMediaManager.CURRENT_PLAYING_URL)
                                            && msg.getContent().equals(JCMediaManager.CURRENT_PLAYING_URL)) {// ??????????????????????????????????????????... ?????????????????????????????????
                                        JCVideoPlayer.releaseAllVideos();
                                    }
                                }

                                msg.setType(msgById.getType());
                                msg.setContent(msgById.getContent());
                            }
                        }
                        mChatContentView.notifyDataSetInvalidated(false);

                        // ???????????????????????????????????????

                        String title = TextUtils.isEmpty(mFriend.getRemarkName()) ? mFriend.getNickName() : mFriend.getRemarkName();
                        if (coreManager.getConfig().isOpenOnlineStatus) {
                            title += getString(R.string.status_online);
                        }
                        mTvTitle.setText(title);
                        break;
                    }
                }
            } else if (action.equals("Refresh")) {
                Bundle bundle = intent.getExtras();
                String packetId = bundle.getString("packetId");
                String fromId = bundle.getString("fromId");
                int type = bundle.getInt("type");
               /* if (type == XmppMessage.TYPE_INPUT && mFriend.getUserId().equals(fromId)) {
                    // ??????????????????...
                    nameTv.setText(getString("JX_Entering"));
                    time.cancel();
                    time.start();
                }*/
                // ????????????????????????????????????????????????????????????????????????????????????????????????????????????
                for (int i = 0; i < mChatMessages.size(); i++) {
                    ChatMessage msg = mChatMessages.get(i);
                    // ??????packetId???????????????????????????????????????
                    if (msg.getPacketId() == null) {
                        // ??????????????????????????????????????????false???????????????????????????????????????????????????????????????????????????
                        msg.setSendRead(false); // ??????????????????????????????
                        msg.setFromUserId(mFriend.getUserId());
                        msg.setPacketId(packetId);
                        break;
                    }
                }
                mChatContentView.notifyDataSetInvalidated(false);
            } else if (action.equals(com.ktw.bitbit.broadcast.OtherBroadcast.TYPE_INPUT)) {
                String fromId = intent.getStringExtra("fromId");
                if (mFriend.getUserId().equals(fromId)) {
                    // ??????????????????...
                    Log.e("zq", "??????????????????...");
                    mTvTitle.setText(getString(R.string.entering));
                    time.cancel();
                    time.start();
                }
            } else if (action.equals(com.ktw.bitbit.broadcast.OtherBroadcast.MSG_BACK)) {
                String packetId = intent.getStringExtra("packetId");
                if (TextUtils.isEmpty(packetId)) {
                    return;
                }
                for (ChatMessage chatMessage : mChatMessages) {
                    if (packetId.equals(chatMessage.getPacketId())) {
                        if (chatMessage.getType() == XmppMessage.TYPE_VOICE
                                && !TextUtils.isEmpty(VoicePlayer.instance().getVoiceMsgId())
                                && packetId.equals(VoicePlayer.instance().getVoiceMsgId())) {// ?????? && ???????????????msgId????????? ?????????msgId==???????????????msgId
                            // ??????????????????
                            VoicePlayer.instance().stop();
                        }
                        ChatMessage chat = ChatMessageDao.getInstance().findMsgById(mLoginUserId, mFriend.getUserId(), packetId);
                        chatMessage.setType(chat.getType());
                        chatMessage.setContent(chat.getContent());
                        break;
                    }
                }
                mChatContentView.notifyDataSetInvalidated(true);
            } else if (action.equals(com.ktw.bitbit.broadcast.OtherBroadcast.NAME_CHANGE)) {// ???????????????
                mFriend = FriendDao.getInstance().getFriend(mLoginUserId, mFriend.getUserId());
                String title = TextUtils.isEmpty(mFriend.getRemarkName()) ? mFriend.getNickName() : mFriend.getRemarkName();
                if (coreManager.getConfig().isOpenOnlineStatus) {
                    String s = mTvTitle.getText().toString();
                    if (s.contains(getString(R.string.online))) {
                        title += getString(R.string.status_online);
                    } else {
                        title += getString(R.string.status_offline);
                    }
                }
                mTvTitle.setText(title);
            } else if (action.equals(com.ktw.bitbit.broadcast.OtherBroadcast.MULTI_LOGIN_READ_DELETE)) {// ?????? ???????????? ???????????? ??????????????????????????????
                String packet = intent.getStringExtra("MULTI_LOGIN_READ_DELETE_PACKET");
                if (!TextUtils.isEmpty(packet)) {

                    for (int i = 0; i < mChatMessages.size(); i++) {
                        if (mChatMessages.get(i).getPacketId().equals(packet)) {
                            mChatMessages.remove(i);
                            mChatContentView.notifyDataSetInvalidated(true);
                            break;
                        }
                    }
                }
            } else if (action.equals(Constants.CHAT_MESSAGE_DELETE_ACTION)) {

                if (mChatMessages == null || mChatMessages.size() == 0) {
                    return;
                }

                // ??????????????????
                int position = intent.getIntExtra(Constants.CHAT_REMOVE_MESSAGE_POSITION, -1);
                if (position >= 0 && position < mChatMessages.size()) { // ?????????postion
                    ChatMessage message = mChatMessages.get(position);
                    deleteMessage(message.getPacketId());// ????????????????????????
                    if (ChatMessageDao.getInstance().deleteSingleChatMessage(mLoginUserId, mFriend.getUserId(), message)) {
                        mChatMessages.remove(position);
                        mChatContentView.notifyDataSetInvalidated(true);
                        Toast.makeText(mContext, getString(R.string.delete_all_succ), Toast.LENGTH_SHORT).show();
                    } else {
                        Toast.makeText(mContext, R.string.delete_failed, Toast.LENGTH_SHORT).show();
                    }
                }

            } else if (action.equals(Constants.SHOW_MORE_SELECT_MENU)) {// ??????????????????
                int position = intent.getIntExtra(Constants.CHAT_SHOW_MESSAGE_POSITION, 0);
                moreSelected(true, position);
            } else if (action.equals(com.ktw.bitbit.broadcast.OtherBroadcast.TYPE_DELALL)) {
                // attention??????????????????finish??????????????????????????????/??????????????????/?????????????????????????????????????????????????????????????????????????????????????????????status???
                // ????????????onDestroy????????????onSaveConten??????
                isUserSaveContentMethod = false;

                // ????????? || ??????  @see XChatManger 190
                // ????????????????????????xmpp 512,
                String toUserId = intent.getStringExtra("toUserId");
                // ????????????????????????????????????????????????????????????
                if (Objects.equals(mFriend.getUserId(), toUserId)) {
                    String content = intent.getStringExtra("content");
                    if (!TextUtils.isEmpty(content)) {
                        ToastUtil.showToast(mContext, content);
                    }
                    Intent mainIntent = new Intent(mContext, FLYMainActivity.class);
                    startActivity(mainIntent);
                    finish();
                }
            } else if (action.equals(Constants.CHAT_HISTORY_EMPTY)) {// ??????????????????
                mChatMessages.clear();
                mChatContentView.notifyDataSetChanged();
            } else if (action.equals(com.ktw.bitbit.broadcast.OtherBroadcast.QC_FINISH)) {
                int mOperationCode = intent.getIntExtra("Operation_Code", 0);
                if (mOperationCode == 1) {// ???????????????????????? ??????????????????
                    loadBackdrop();
                } else {// ???????????????????????? ??????????????????
                    finish();
                }
            }
        }
    }

    /**
     * ???????????????????????????
     */
    private void getAutoAnswerList() {
        Map<String, String> params = new HashMap<>();
        HttpUtils.get().url(coreManager.getConfig().ROBOT_AUTO_ANSWER)
                .params(params)
                .build()
                .execute(new BaseCallback<AutoAnswerBean>(AutoAnswerBean.class) {

                    @Override
                    public void onResponse(ObjectResult<AutoAnswerBean> result) {
                        if (Result.checkSuccess(mContext, result)) {
                            if (result.getData() != null) {
                                FLYApplication.autoAnswerBean = result.getData();
                                if (FLYApplication.autoAnswerBean != null && !TextUtils.isEmpty(FLYApplication.autoAnswerBean.getAnswer())) {
                                    makeMessage(FLYApplication.autoAnswerBean.getAnswer(), true);
                                }
                            }
                        }
                    }

                    @Override
                    public void onError(Call call, Exception e) {
                    }
                });

    }

    /**
     * ????????????
     *
     * @param msgContent
     */
    private void makeMessage(String msgContent, boolean isSpecial) {
        try {
            ChatMessage chatMessage = new ChatMessage();
            chatMessage.setType(XmppMessage.TYPE_TEXT);
            chatMessage.setDeleteTime(-1);
            chatMessage.setFromUserId(Friend.ID_SYSTEM_NOTIFICATION);
            chatMessage.setFromUserName(mFriend.getRemarkName());
            chatMessage.setToUserId(mLoginUserId);
            chatMessage.setToUserName(mLoginNickName);
            chatMessage.setContent(msgContent);
            chatMessage.setReadTime(TimeUtils.sk_time_current_time());
            chatMessage.setUpload(true);
            chatMessage.setMySend(false);
            chatMessage.setReSendCount(0);
            chatMessage.setSendRead(true);
            chatMessage.setIsEncrypt(0);
            chatMessage.setTimeSend(TimeUtils.sk_time_current_time());
            chatMessage.setPacketId(UUID.randomUUID().toString().replaceAll("-", ""));
            chatMessage.setMessageState(MESSAGE_SEND_SUCCESS);
            //?????????????????????????????????
            if (isSpecial) {
                chatMessage.setObjectId(JsonUtils.initJsonRobotMsg(true));
            }

            //????????????
            ChatMessageDao.getInstance().saveNewSingleChatMessage(mLoginUserId, chatMessage.getFromUserId(), chatMessage);
            //??????????????????
            onNewMessage(Friend.ID_SYSTEM_NOTIFICATION, chatMessage, false);
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }

    /**
     * ???????????????????????????
     *
     * @param key
     * @return
     */
    private String getContentByText(String key) {
        String answer = "";
        try {
            //???????????????
            if (isNumeric(key)) {
                //?????????
                int sort = Integer.parseInt(key);
                List<AutoAnswerBean.AnswerBean> answerBeanList = FLYApplication.autoAnswerBean.getAutoAnswerList();
                if (answerBeanList != null && answerBeanList.size() > 0) {
                    for (AutoAnswerBean.AnswerBean item : answerBeanList) {
                        if (item.getSort() == sort) {
                            answer = item.getAnswer();
                            break;
                        }
                    }
                }
            } else {
                //????????????
                List<AutoAnswerBean.AnswerBean> answerBeanList = FLYApplication.autoAnswerBean.getAutoAnswerList();
                if (answerBeanList != null && answerBeanList.size() > 0) {
                    for (AutoAnswerBean.AnswerBean item : answerBeanList) {
                        if (!TextUtils.isEmpty(item.getIssue()) && item.getIssue().equals(key)) {
                            answer = item.getAnswer();
                            break;
                        }
                    }
                }
            }
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
        return answer;
    }

    /**
     * ??????????????????????????????
     *
     * @param str
     * @return
     */
    public boolean isNumeric(String str) {
        Pattern pattern = Pattern.compile("[0-9]*");
        Matcher isNum = pattern.matcher(str);
        if (!isNum.matches()) {
            return false;
        }
        return true;
    }

    @Subscribe(threadMode = ThreadMode.MainThread)
    public void helloEventBus(final EventClickProblem eventClickProblem) {
        if (eventClickProblem != null) {
            String content = getContentByText(eventClickProblem.sort + "");
            if (!TextUtils.isEmpty(content)) {
                makeMessage(content, false);
            }
        }
    }

    @Override
    protected void onNewIntent(Intent intent) {
        super.onNewIntent(intent);
    }
}
