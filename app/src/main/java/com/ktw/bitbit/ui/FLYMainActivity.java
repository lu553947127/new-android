package com.ktw.bitbit.ui;

import android.Manifest;
import android.app.Activity;
import android.app.ActivityManager;
import android.app.NotificationManager;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.pm.PackageInfo;
import android.content.pm.PackageManager;
import android.content.res.ColorStateList;
import android.graphics.drawable.Drawable;
import android.os.Bundle;
import android.os.Handler;
import android.os.IBinder;
import android.os.Looper;
import android.os.Process;
import android.text.TextUtils;
import android.util.Log;
import android.view.KeyEvent;
import android.view.View;
import android.view.inputmethod.InputMethodManager;
import android.widget.FrameLayout;
import android.widget.RadioButton;
import android.widget.RadioGroup;
import android.widget.TextView;

import androidx.core.content.ContextCompat;
import androidx.core.graphics.drawable.DrawableCompat;
import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentManager;
import androidx.fragment.app.FragmentTransaction;

import com.alibaba.fastjson.JSON;
import com.bumptech.glide.Glide;
import com.example.qrcode.Constant;
import com.example.qrcode.ScannerActivity;
import com.example.qrcode.utils.NetUtil;
import com.fanjun.keeplive.KeepLive;
import com.fanjun.keeplive.config.KeepLiveService;
import com.google.android.gms.common.ConnectionResult;
import com.google.android.gms.common.GoogleApiAvailability;
import com.ktw.bitbit.FLYAppConfig;
import com.ktw.bitbit.FLYAppConstant;
import com.ktw.bitbit.BuildConfig;
import com.ktw.bitbit.FLYApplication;
import com.ktw.bitbit.R;
import com.ktw.bitbit.FLYReporter;
import com.ktw.bitbit.bean.AllGroupBean;
import com.ktw.bitbit.bean.AutoAnswerBean;
import com.ktw.bitbit.bean.ConfigBean;
import com.ktw.bitbit.bean.Contact;
import com.ktw.bitbit.bean.Contacts;
import com.ktw.bitbit.bean.Friend;
import com.ktw.bitbit.bean.MyCollectEmotPackageBean;
import com.ktw.bitbit.bean.NavBean;
import com.ktw.bitbit.bean.UploadingFile;
import com.ktw.bitbit.bean.User;
import com.ktw.bitbit.bean.collection.Collectiion;
import com.ktw.bitbit.bean.event.EventCreateGroupFriend;
import com.ktw.bitbit.bean.event.EventSendVerifyMsg;
import com.ktw.bitbit.bean.event.MessageContactEvent;
import com.ktw.bitbit.bean.event.MessageEventBG;
import com.ktw.bitbit.bean.event.MessageEventHongdian;
import com.ktw.bitbit.bean.event.MessageLogin;
import com.ktw.bitbit.bean.event.MessageSendChat;
import com.ktw.bitbit.bean.message.ChatMessage;
import com.ktw.bitbit.bean.message.MucRoom;
import com.ktw.bitbit.bean.message.XmppMessage;
import com.ktw.bitbit.broadcast.MsgBroadcast;
import com.ktw.bitbit.broadcast.MucgroupUpdateUtil;
import com.ktw.bitbit.broadcast.OtherBroadcast;
import com.ktw.bitbit.broadcast.TimeChangeReceiver;
import com.ktw.bitbit.broadcast.UpdateUnReadReceiver;
import com.ktw.bitbit.broadcast.UserLogInOutReceiver;
import com.ktw.bitbit.call.AudioOrVideoController;
import com.ktw.bitbit.call.CallConstants;
import com.ktw.bitbit.call.Jitsi_connecting_second;
import com.ktw.bitbit.call.MessageEventCancelOrHangUp;
import com.ktw.bitbit.call.MessageEventInitiateMeeting;
import com.ktw.bitbit.db.dao.ChatMessageDao;
import com.ktw.bitbit.db.dao.ContactDao;
import com.ktw.bitbit.db.dao.FriendDao;
import com.ktw.bitbit.db.dao.MyZanDao;
import com.ktw.bitbit.db.dao.NewFriendDao;
import com.ktw.bitbit.db.dao.OnCompleteListener2;
import com.ktw.bitbit.db.dao.UploadingFileDao;
import com.ktw.bitbit.db.dao.UserDao;
import com.ktw.bitbit.db.dao.login.MachineDao;
import com.ktw.bitbit.downloader.UpdateManger;
import com.ktw.bitbit.fragment.FindFragment;
import com.ktw.bitbit.fragment.FriendFragment;
import com.ktw.bitbit.fragment.MeFragment;
import com.ktw.bitbit.fragment.MessageFragment;
import com.ktw.bitbit.fragment.Nav1Fragment;
import com.ktw.bitbit.fragment.Nav2Fragment;
import com.ktw.bitbit.wallet.Apis;
import com.ktw.bitbit.wallet.WalletFragment;
import com.ktw.bitbit.helper.DialogHelper;
import com.ktw.bitbit.helper.LoginHelper;
import com.ktw.bitbit.helper.LoginSecureHelper;
import com.ktw.bitbit.helper.PrivacySettingHelper;
import com.ktw.bitbit.map.MapHelper;
import com.ktw.bitbit.pay.PaymentReceiptMoneyActivity;
import com.ktw.bitbit.pay.ReceiptPayMoneyActivity;
import com.ktw.bitbit.socket.SocketException;
import com.ktw.bitbit.ui.backup.ReceiveChatHistoryActivity;
import com.ktw.bitbit.ui.base.BaseActivity;
import com.ktw.bitbit.ui.base.CoreManager;
import com.ktw.bitbit.ui.lock.DeviceLockActivity;
import com.ktw.bitbit.ui.lock.DeviceLockHelper;
import com.ktw.bitbit.ui.login.WebLoginActivity;
import com.ktw.bitbit.ui.me.emot.MyEmotBean;
import com.ktw.bitbit.ui.message.MucChatActivity;
import com.ktw.bitbit.ui.other.BasicInfoActivity;
import com.ktw.bitbit.ui.tool.WebViewActivity;
import com.ktw.bitbit.util.AppUtils;
import com.ktw.bitbit.util.AsyncUtils;
import com.ktw.bitbit.util.Constants;
import com.ktw.bitbit.util.ContactsUtil;
import com.ktw.bitbit.util.DeviceInfoUtil;
import com.ktw.bitbit.util.DisplayUtil;
import com.ktw.bitbit.util.FileUtil;
import com.ktw.bitbit.util.HttpUtil;
import com.ktw.bitbit.util.JsonUtils;
import com.ktw.bitbit.util.PermissionUtil;
import com.ktw.bitbit.util.PreferenceUtils;
import com.ktw.bitbit.util.SkinUtils;
import com.ktw.bitbit.util.TimeUtils;
import com.ktw.bitbit.util.ToastUtil;
import com.ktw.bitbit.util.UiUtils;
import com.ktw.bitbit.util.UpgradeManager;
import com.ktw.bitbit.util.download.AndroidDownloadManager;
import com.ktw.bitbit.util.download.AndroidDownloadManagerListener;
import com.ktw.bitbit.util.log.LogUtils;
import com.ktw.bitbit.view.PermissionExplainDialog;
import com.ktw.bitbit.view.SelectionFrame;
import com.ktw.bitbit.view.VerifyDialog;
import com.ktw.bitbit.view.cjt2325.cameralibrary.util.LogUtil;
import com.ktw.bitbit.wallet.bean.ApkUpdateBean;
import com.ktw.bitbit.xmpp.CoreService;
import com.ktw.bitbit.xmpp.ListenerManager;
import com.ktw.bitbit.xmpp.listener.ChatMessageListener;
import com.xuan.xuanhttplibrary.okhttp.HttpUtils;
import com.xuan.xuanhttplibrary.okhttp.callback.BaseCallback;
import com.xuan.xuanhttplibrary.okhttp.callback.ListCallback;
import com.xuan.xuanhttplibrary.okhttp.result.ArrayResult;
import com.xuan.xuanhttplibrary.okhttp.result.ObjectResult;
import com.xuan.xuanhttplibrary.okhttp.result.Result;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.UUID;

import de.greenrobot.event.EventBus;
import de.greenrobot.event.Subscribe;
import de.greenrobot.event.ThreadMode;
import fm.jiecao.jcvideoplayer_lib.JCVideoPlayer;
import me.leolin.shortcutbadger.ShortcutBadger;
import okhttp3.Call;

/**
 * ?????????
 */
public class FLYMainActivity extends BaseActivity implements PermissionUtil.OnRequestPermissionsResultCallbacks {
    // ????????????
    public static final String APP_ID = BuildConfig.XIAOMI_APP_ID;
    public static final String APP_KEY = BuildConfig.XIAOMI_APP_KEY;
    // ???????????????initView??????
    // ?????????????????????????????????????????????????????????true
    public static boolean isInitView = false;
    /**
     * ??????????????????
     */
    Handler mHandler = new Handler();
    private UpdateUnReadReceiver mUpdateUnReadReceiver = null;
    private UserLogInOutReceiver mUserLogInOutReceiver = null;
    private TimeChangeReceiver timeChangeReceiver = null;
    private ActivityManager mActivityManager;
    // ?????????????????????????????????????????????????????????????????????????????????????????????????????????????????????????????????????????????????????????????????????????????????????????????????????????
    // ?????????????????????????????????????????????????????????????????????????????????????????????????????????????????????????????????????????????????????????????????????????????????????????????????????????
    private int mLastFragmentId;// ????????????
    private RadioGroup mRadioGroup;
    private RadioButton
            mRbTab1 //??????
            , mWalletTab //??????
            , mRbTab2 //??????
            , mRbFindTab//??????
            , mRbTab4 //??????

            , mRbNav1Tab //???????????????
            , mRbNav2Tab; //???????????????
    private FrameLayout flNav1Point //????????????????????????
            , flNav2Point //????????????????????????
            , flFindPoint; //?????????????????????
    private TextView mTvMessageNum;// ??????????????????????????????
    private TextView mTvNewFriendNum;// ?????????????????????????????????
    private TextView mTvCircleNum;// ???????????????????????????
    private int numMessage = 0;// ????????????????????????
    private int numCircle = 0; // ???????????????????????????
    private String mUserId;// ??????????????? UserID
    private My_BroadcastReceiver my_broadcastReceiver;
    private int mCurrtTabId;
    private boolean isCreate;
    /**
     * ????????????????????????????????????
     */
    private boolean isConflict;
    //??????
    public static final int AD_TYPE_IMAGE = 1;
    //??????
    public static final int AD_TYPE_VIDEO = 2;
    //???????????????
    private AndroidDownloadManager androidDownloadManager;

    public FLYMainActivity() {
        noLoginRequired();
    }

    public static void start(Context ctx) {
        Intent intent = new Intent(ctx, FLYMainActivity.class);
        ctx.startActivity(intent);
    }

    /**
     * ????????????????????????
     * ??????MainActivity??????Fragment?????????
     */
    public static void requestQrCodeScan(Activity ctx) {
        Intent intent = new Intent(ctx, ScannerActivity.class);
        // ?????????????????????
        intent.putExtra(Constant.EXTRA_SCANNER_FRAME_WIDTH, DisplayUtil.dip2px(ctx, 200));
        // ?????????????????????
        intent.putExtra(Constant.EXTRA_SCANNER_FRAME_HEIGHT, DisplayUtil.dip2px(ctx, 200));
        // ?????????????????????????????????
        intent.putExtra(Constant.EXTRA_SCANNER_FRAME_TOP_PADDING, DisplayUtil.dip2px(ctx, 100));
        // ?????????????????????
        intent.putExtra(Constant.EXTRA_IS_ENABLE_SCAN_FROM_PIC, true);
        ctx.startActivityForResult(intent, 888);
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        // ?????????????????? | ?????????????????? | Activity????????????????????? | ??????????????????
/*
        getWindow().addFlags(
                WindowManager.LayoutParams.FLAG_DISMISS_KEYGUARD
                        | WindowManager.LayoutParams.FLAG_SHOW_WHEN_LOCKED
                        | WindowManager.LayoutParams.FLAG_TURN_SCREEN_ON
                        | WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON);
*/
        setContentView(R.layout.activity_main);
        // ????????????
        if (PrivacySettingHelper.getPrivacySettings(this).getIsKeepalive() == 1) {
            initKeepLive();
        }
        initLog();

        mUserId = coreManager.getSelf().getUserId();
        initView();// ???????????????
        initBroadcast();// ???????????????
        initDatas();// ?????????????????????

        // ??????????????????Control
        AudioOrVideoController.init(mContext, coreManager);

        AsyncUtils.doAsync(this, mainActivityAsyncContext -> {
            // ??????app????????????????????????????????????????????????????????????????????????
            List<UploadingFile> uploadingFiles = UploadingFileDao.getInstance().getAllUploadingFiles(coreManager.getSelf().getUserId());
            for (int i = uploadingFiles.size() - 1; i >= 0; i--) {
                ChatMessageDao.getInstance().updateMessageState(coreManager.getSelf().getUserId(), uploadingFiles.get(i).getToUserId(),
                        uploadingFiles.get(i).getMsgId(), ChatMessageListener.MESSAGE_SEND_FAILED);
            }
        });

        UpdateManger.checkUpdate(this, coreManager.getConfig().androidAppUrl,
                coreManager.getConfig().androidVersion, coreManager.getConfig().androidExplain);

        EventBus.getDefault().post(new MessageLogin());
        // ????????????
        showDeviceLock();

        initMap();

        // ??????????????????????????????ios?????????
        setSwipeBackEnable(false);
        getConfig();


//        if (!StringUtil.isBlank(coreManager.getConfig().androidAppUrl) &&
//                !StringUtil.isBlank(coreManager.getConfig().androidVersion)) {
//            checkVersion(coreManager.getConfig().androidAppUrl, coreManager.getConfig().androidVersion);
//        }

    }

    private void checkVersion(String apkUrl, String versionName) {
        PackageManager mPackageManager = mContext.getPackageManager();
        PackageInfo packageInfo = null;
        try {
            packageInfo = mPackageManager.getPackageInfo(mContext.getPackageName(), 0);
        } catch (PackageManager.NameNotFoundException e) {
            FLYReporter.unreachable(e);
            return;
        }
//        String mCurrentVersionCode = packageInfo.versionName.replaceAll("\\.", "");
        int mCurrentVersionCode = packageInfo.versionCode;
        if (mCurrentVersionCode < Integer.parseInt(versionName)) {
            //?????????????????????????????????
            SelectionFrame mSF = new SelectionFrame(this);
            mSF.setSomething(null, "???????????????????????????????????????", new SelectionFrame.OnSelectionFrameClickListener() {
                @Override
                public void cancelClick() {

                }

                @Override
                public void confirmClick() {
                    new UpgradeManager(FLYMainActivity.this, apkUrl, versionName).downloadApk();

                }
            });
            mSF.show();
        }
    }

    @Override
    protected void onNewIntent(Intent intent) {
        super.onNewIntent(intent);
        Log.e(TAG, "onNewIntent1");
        if (isInitView) {
            Log.e(TAG, "onNewIntent2");
            // ????????????????????????????????????????????????
            setStatusBarColor();
            FragmentManager fm = getSupportFragmentManager();
            List<Fragment> lf = fm.getFragments();
            for (Fragment f : lf) {
                fm.beginTransaction().remove(f).commitNowAllowingStateLoss();
            }
            initView();
        }
        FLYMainActivity.isInitView = false;
    }

    //    @Override
//    protected void onRestart() {
//        super.onRestart();
//        // ??????????????????????????????????????????????????????
//        MsgBroadcast.broadcastMsgUiUpdate(mContext);
//    }
    //    @Override
    protected void onRestart() {
        super.onRestart();
        // ??????????????????????????????????????????????????????
        MsgBroadcast.broadcastMsgUiUpdate(mContext);

        if (NetUtil.isGprsOrWifiConnected(mContext)) {
            //?????????????????????????????????????????????????????????
            ChatMessage message = new ChatMessage();
            message.setType(XmppMessage.TYPE_TEXT);
            message.setContent("???????????????????????????????????????????????????????????????");
            message.setType(XmppMessage.TYPE_REPLAY);
            message.setReSendCount(0);//???????????????????????????????????????????????????????????????????????????
            message.setFromUserId(coreManager.getSelf().getUserId());
            message.setToUserId(Friend.ID_SYSTEM_CONNECT);
            message.setTimeSend(TimeUtils.sk_time_current_time());
            coreManager.sendChatMessage(Friend.ID_SYSTEM_CONNECT, message);
        }
    }

    @Override
    public boolean onKeyDown(int keyCode, KeyEvent event) {
        if (keyCode == KeyEvent.KEYCODE_BACK) {
            if (!JCVideoPlayer.backPress()) {
                // ??????JCVideoPlayer.backPress()
                // true : ??????????????????????????????
                // false: ??????????????????????????????
                moveTaskToBack(true);
            }
            return true;
        }
        return super.onKeyDown(keyCode, event);
    }

    @Override
    protected void onDestroy() {
        // XMPP???????????? ????????????disconnect ?????????????????????????????????????????????????????? ??????????????????
        coreManager.disconnect();

        unregisterReceiver(mUpdateUnReadReceiver);
        unregisterReceiver(mUserLogInOutReceiver);
        unregisterReceiver(my_broadcastReceiver);
        unregisterReceiver(timeChangeReceiver);
        EventBus.getDefault().unregister(this);

        Glide.get(this).clearMemory();
        new Thread(new Runnable() {
            @Override
            public void run() {
                Glide.get(getApplicationContext()).clearDiskCache();
            }
        });
        if (androidDownloadManager != null) {
            androidDownloadManager.unregister(FLYApplication.getInstance());
        }
        super.onDestroy();
    }

    private void initKeepLive() {
        //??????????????????
        KeepLive.startWork(getApplication(), KeepLive.RunMode.ENERGY,
                //??????????????????????????????socket???????????????????????????????????????????????????????????????????????????
                new KeepLiveService() {
                    /**
                     * ?????????
                     * ?????????????????????????????????????????????????????????????????????
                     */
                    @Override
                    public void onWorking() {
                        Log.e("xuan", "onWorking: ");
                    }

                    /**
                     * ????????????
                     * ???????????????????????????????????????????????????????????????????????????onWorking?????????????????????????????????broadcast
                     */
                    @Override
                    public void onStop() {
                        Log.e("xuan", "onStop: ");
                    }
                }
        );
    }

    private void initLog() {
        String dir = FileUtil.getSaveDirectory("IMLogs");
        LogUtils.setLogDir(dir);
        LogUtils.setLogLevel(LogUtils.LogLevel.WARN);
    }

    private void initView() {
        getSupportActionBar().hide();
        mRadioGroup = (RadioGroup) findViewById(R.id.main_rg);
        mRbTab1 = (RadioButton) findViewById(R.id.rb_tab_1);
        mWalletTab = (RadioButton) findViewById(R.id.rb_tab_wallet);
        mRbTab2 = (RadioButton) findViewById(R.id.rb_tab_2);
        mRbFindTab = (RadioButton) findViewById(R.id.rb_tab_3);
        mRbTab4 = (RadioButton) findViewById(R.id.rb_tab_4);
        mRbNav1Tab = (RadioButton) findViewById(R.id.rb_tab_nav1);
        mRbNav2Tab = (RadioButton) findViewById(R.id.rb_tab_nav2);

        flNav1Point = findViewById(R.id.flNav1Point);
        flNav2Point = findViewById(R.id.flNav2Point);
        flFindPoint = findViewById(R.id.flFindPoint);

        mTvMessageNum = (TextView) findViewById(R.id.main_tab_one_tv);
        mTvNewFriendNum = (TextView) findViewById(R.id.main_tab_two_tv);
        Friend newFriend = FriendDao.getInstance().getFriend(coreManager.getSelf().getUserId(), Friend.ID_NEW_FRIEND_MESSAGE);
        if (newFriend != null) {
            updateNewFriendMsgNum(newFriend.getUnReadNum());
        }

        mTvCircleNum = (TextView) findViewById(R.id.main_tab_three_tv);

        mRadioGroup.setOnCheckedChangeListener((group, checkedId) -> {
            hideInput();
            if (checkedId > 0 && mCurrtTabId != checkedId) {
                mCurrtTabId = checkedId;

                changeFragment(checkedId);

                if (checkedId == R.id.rb_tab_1) {
                    updateNumData();
                }
                JCVideoPlayer.releaseAllVideos();
            }
        });

        isCreate = false;
        //  ????????????bug
        mRbTab1.toggle();
//        mWalletTab.toggle();
        // initFragment();

        // ????????????
        ColorStateList tabColor = SkinUtils.getSkin(this).getMainTabColorState();
        for (RadioButton radioButton : Arrays.asList(mRbTab1, mRbTab2, mWalletTab, mRbNav1Tab, mRbNav2Tab, mRbFindTab, mRbTab4)) {
            // ???????????????????????????????????????
            Drawable drawable = radioButton.getCompoundDrawables()[1];
            drawable = DrawableCompat.wrap(drawable);
            DrawableCompat.setTintList(drawable, tabColor);
            // ?????????getDrawable?????????Drawable???????????????setCompoundDrawables??????????????????
            radioButton.setCompoundDrawablesWithIntrinsicBounds(null, drawable, null, null);
            radioButton.setTextColor(tabColor);
        }

        // ?????????????????????????????????
        checkNotifyStatus();
    }

    private void initBroadcast() {
        EventBus.getDefault().register(this);

        // ??????????????????????????????
        IntentFilter filter = new IntentFilter();
        filter.addAction(MsgBroadcast.ACTION_MSG_NUM_UPDATE);
        filter.addAction(MsgBroadcast.ACTION_MSG_NUM_UPDATE_NEW_FRIEND);
        filter.addAction(MsgBroadcast.ACTION_MSG_NUM_RESET);
        mUpdateUnReadReceiver = new UpdateUnReadReceiver(this);
        registerReceiver(mUpdateUnReadReceiver, filter);

        // ??????????????????????????????
        mUserLogInOutReceiver = new UserLogInOutReceiver(this);
        registerReceiver(mUserLogInOutReceiver, LoginHelper.getLogInOutActionFilter());

        // ???????????????????????? ?????????????????????????????????????????????????????????
        filter = new IntentFilter();
        // ??????????????????????????????????????????????????????????????????????????????????????????????????????(?????????????????????????????????????????????????????????)???????????????
        filter.addAction(Constants.UPDATE_ROOM);
        filter.addAction(com.ktw.bitbit.broadcast.OtherBroadcast.SYNC_CLEAN_CHAT_HISTORY);
        filter.addAction(com.ktw.bitbit.broadcast.OtherBroadcast.SYNC_SELF_DATE);
        filter.addAction(com.ktw.bitbit.broadcast.OtherBroadcast.CollectionRefresh);
        filter.addAction(com.ktw.bitbit.broadcast.OtherBroadcast.SEND_MULTI_NOTIFY);  // ??????????????????
        my_broadcastReceiver = new My_BroadcastReceiver();
        registerReceiver(my_broadcastReceiver, filter);

        // ???????????????????????????
        filter = new IntentFilter();
        filter.addAction(Intent.ACTION_TIME_CHANGED);
        filter.addAction(Intent.ACTION_DATE_CHANGED);
        filter.addAction(Intent.ACTION_TIMEZONE_CHANGED);
        timeChangeReceiver = new TimeChangeReceiver(this);
        registerReceiver(timeChangeReceiver, filter);
    }

    private void initDatas() {
        // ???????????????????????????????????????????????????
        User loginUser = coreManager.getSelf();
        if (!LoginHelper.isUserValidation(loginUser)) {
            LoginHelper.prepareUser(this, coreManager);
        }
        LoginSecureHelper.autoLogin(this, coreManager, t -> {
            if (t instanceof LoginSecureHelper.LoginTokenOvertimeException) {
                FLYApplication.getInstance().mUserStatus = LoginHelper.STATUS_USER_TOKEN_OVERDUE;
                loginOut();
            }
        }, () -> {
            // ??????????????????????????????????????????accessToken???????????????
            loginRequired();
            initCore();
            CoreManager.initLocalCollectionEmoji();
            CoreManager.updateMyBalance();
            initOther();// ??????????????????
            checkTime();
            // ?????????????????????
            if ((coreManager.getConfig().isSupportAddress
                    && !coreManager.getConfig().registerUsername)) {
                addressBookOperation();
            }
            login();
            updateSelfData();

            //?????????????????????
            getNavData();

            initEmotData();

            getAd();

            getAutoAnswerList();

        });

        mUserId = loginUser.getUserId();
        FriendDao.getInstance().checkSystemFriend(mUserId); // ?????? ???????????????

        // ???????????????????????????
        updateNumData();

    }

    private void getAd() {
        Map<String, String> params = new HashMap<>();
        params.put("code", "2");
        HttpUtils.get().url(FLYAppConfig.API_APP_SOURCE)
                .params(params)
                .build(true, true)
                .execute(new ListCallback<NavBean>(NavBean.class) {

                    @Override
                    public void onResponse(ArrayResult<NavBean> result) {

                        if (result.getResultCode() == 1 && result.getData() != null && result.getData().size() > 0) {
                            NavBean navBean = result.getData().get(0);
                            if (navBean != null && !TextUtils.isEmpty(navBean.getImgUrl())) {
                                LogUtil.d("???????????????????????????" + navBean.getImgUrl());
                                //??????????????????????????????????????????????????????
                                String upUrl = PreferenceUtils.getString(FLYApplication.getInstance(), Constants.KEY_SPLASH_AD_URL);
                                if (!navBean.getImgUrl().equalsIgnoreCase(upUrl)) {
                                    //????????????????????????
                                    new Thread(new Runnable() {
                                        @Override
                                        public void run() {
                                            LogUtil.d("??????????????????" + (Looper.getMainLooper() == Looper.myLooper() ? 1 : 0));
                                            if (AndroidDownloadManager.getFileSuffixByUrl(navBean.getImgUrl()).equalsIgnoreCase(".mp4")) {
                                                //????????????
                                                downloadAdSource(AD_TYPE_VIDEO, navBean);
                                            } else {
                                                //????????????
                                                downloadAdSource(AD_TYPE_IMAGE, navBean);
                                            }
                                        }
                                    }).start();

                                }
                            }
                        }
                    }

                    @Override
                    public void onError(Call call, Exception e) {
                    }
                });

    }

    private void initEmotData() {
        new Handler().postDelayed(new Runnable() {
            @Override
            public void run() {
                FLYApplication.emotMap.clear();
                FLYApplication.singleEmotList.clear();
                getEmotPackageList();
                getSingleEmotPackageList();
            }
        }, 2000);
    }

    private void getSingleEmotPackageList() {
        Map<String, String> params = new HashMap<>();
        params.put("userId", coreManager.getSelf().getUserId());
        params.put("type", 1 + "");
        HttpUtils.get().url(coreManager.getConfig().API_FACE_COLLECT_LIST)
                .params(params)
                .build()
                .execute(new ListCallback<MyCollectEmotPackageBean>(MyCollectEmotPackageBean.class) {
                    @Override
                    public void onResponse(ArrayResult<MyCollectEmotPackageBean> result) {
                        if (Result.checkSuccess(mContext, result)) {
                            if (result.getData() != null && result.getData().size() > 0) {
                                for (MyCollectEmotPackageBean item : result.getData()) {
                                    MyEmotBean myEmotBean = new MyEmotBean();
                                    myEmotBean.setId(item.getId());
                                    myEmotBean.setUrl(item.getFace().getPath().get(0));
                                    FLYApplication.singleEmotList.add(myEmotBean);
                                }
                            }
                        }
                    }

                    @Override
                    public void onError(Call call, Exception e) {
                    }
                });
    }

    private void getEmotPackageList() {
        Map<String, String> params = new HashMap<>();
        params.put("userId", coreManager.getSelf().getUserId());
        params.put("type", 0 + "");
        HttpUtils.get().url(coreManager.getConfig().API_FACE_COLLECT_LIST)
                .params(params)
                .build()
                .execute(new ListCallback<MyCollectEmotPackageBean>(MyCollectEmotPackageBean.class) {
                    @Override
                    public void onResponse(ArrayResult<MyCollectEmotPackageBean> result) {
                        DialogHelper.dismissProgressDialog();
                        if (Result.checkSuccess(mContext, result)) {
                            if (result.getData() != null && result.getData().size() > 0) {
                                for (MyCollectEmotPackageBean myEmotBean : result.getData()) {
                                    FLYApplication.emotMap.put(myEmotBean.getFace().getName(), myEmotBean.getFace().getPath());
                                }
                            }
                        }

                    }

                    @Override
                    public void onError(Call call, Exception e) {
                    }
                });

    }

    private void getNavData() {
        Map<String, String> params = new HashMap<>();
        params.put("code", "0");
        HttpUtils.get().url(FLYAppConfig.API_APP_SOURCE)
                .params(params)
                .build(true, true)
                .execute(new ListCallback<NavBean>(NavBean.class) {

                    @Override
                    public void onResponse(ArrayResult<NavBean> result) {
                        if (result.getResultCode() == 1 && result.getData() != null && result.getData().size() > 0) {

                            runOnUiThread(new Runnable() {
                                @Override
                                public void run() {
                                    showNav(result.getData());
                                }
                            });
                        }

                    }

                    @Override
                    public void onError(Call call, Exception e) {
                    }
                });
    }

    /**
     * ??????????????????????????????????????????????????????1??????????????????????????????1????????????2??????????????????????????????????????????????????????
     *
     * @param data
     */
    private void showNav(List<NavBean> data) {
        switch (data.size()) {
            case 2: {
                mRbFindTab.setVisibility(View.GONE);
                flFindPoint.setVisibility(View.GONE);

                NavBean navBean2 = data.get(1);
                if (navBean2 != null) {
                    mRbNav2Tab.setVisibility(View.VISIBLE);
                    mRbNav2Tab.setText(navBean2.getDesc());
                    flNav2Point.setVisibility(View.VISIBLE);
                    Nav2Fragment.homeUrl = navBean2.getLink();
                    Nav2Fragment.title = navBean2.getDesc();
                }
            }
            case 1: {
                NavBean navBean1 = data.get(0);
                if (navBean1 != null) {
                    mRbNav1Tab.setVisibility(View.VISIBLE);
                    mRbNav1Tab.setText(navBean1.getDesc());
                    flNav1Point.setVisibility(View.VISIBLE);
                    Nav1Fragment.homeUrl = navBean1.getLink();
                    Nav1Fragment.title = navBean1.getDesc();
                }
            }
        }
    }

    private void showDeviceLock() {
        if (DeviceLockHelper.isLocked()) {
            // ?????????????????????
            DeviceLockActivity.start(this);
        } else {
            Log.e("DeviceLock", "???????????????????????????????????????");
        }
    }

    private void initMap() {
        // ?????????????????????????????????
        // ???????????????????????????????????????????????????
        String area = PreferenceUtils.getString(this, FLYAppConstant.EXTRA_CLUSTER_AREA);
        if (TextUtils.equals(area, "CN")) {
            MapHelper.setMapType(MapHelper.MapType.BAIDU);
        } else {
            MapHelper.setMapType(MapHelper.MapType.GOOGLE);
        }
    }

    /**
     * ??????Fragment
     */
    private void changeFragment(int checkedId) {
        if (mLastFragmentId == checkedId) {
            return;
        }

        FragmentTransaction transaction = getSupportFragmentManager().beginTransaction();

        Fragment fragment = getSupportFragmentManager().findFragmentByTag(String.valueOf(checkedId));
        if (fragment == null) {
            switch (checkedId) {
//                case R.id.rb_tab_wallet:
//                    //??????
//                    fragment = new WalletFragment();
//                    break;
                case R.id.rb_tab_1:
                    //??????
                    fragment = new MessageFragment();
                    break;
                case R.id.rb_tab_2:
                    //?????????
                    fragment = new FriendFragment();
                    break;
                case R.id.rb_tab_3:
                    //????????? ?????????dapp
//                    if (coreManager.getConfig().newUi) { // ??????????????????ui??????????????????????????????
//                        fragment = new SquareFragment();
//                    } else {
//                        fragment = new DiscoverFragment();
//                    }
                    fragment = new FindFragment();
//                    fragment = new DappsFragment();
                    break;
                case R.id.rb_tab_4:
                    fragment = new MeFragment();
                    break;
//                case R.id.rb_tab_nav1:
//                    fragment = new Nav1Fragment();
//                    break;
//                case R.id.rb_tab_nav2:
//                    fragment = new Nav2Fragment();
//                    break;
            }
        }

        // fragment = null;
        assert fragment != null;

        if (!fragment.isAdded()) {// ????????? add
            transaction.add(R.id.main_content, fragment, String.valueOf(checkedId));
        }

        Fragment lastFragment = getSupportFragmentManager().findFragmentByTag(String.valueOf(mLastFragmentId));

        if (lastFragment != null) {
            transaction.hide(lastFragment);
        }
        // ??????????????????last???current???????????????fragment???????????????hide???show,
        transaction.show(fragment);

        // transaction.setTransition(FragmentTransaction.TRANSIT_FRAGMENT_OPEN);// ????????????
        transaction.commitNowAllowingStateLoss();

        // getSupportFragmentManager().executePendingTransactions();

        mLastFragmentId = checkedId;
        setStatusBarColor();
//        if (checkedId == R.id.rb_tab_4) {
//            setStatusBarLight(false);
//        } else {
//
//        }
    }

    /**
     * OPPO?????????App????????????????????????????????????????????????????????????
     * OPPO?????????App??????????????????StartActivity??????????????????????????????????????? ????????????-????????????-??????????????? ??????App??????????????????
     * <p>
     * ??????????????????????????????????????????????????????????????????????????????????????????????????????????????????????????????????????????????????????????????????????????????????????????????????????????????????????????????
     */
    private void checkNotifyStatus() {
        int launchCount = PreferenceUtils.getInt(this, Constants.APP_LAUNCH_COUNT, 0);// ??????app???????????????
        Log.e("zq", "??????app?????????:" + launchCount);
        if (launchCount == 1) {
            String tip = "";
            if (!AppUtils.isNotificationEnabled(this)) {
                tip = getString(R.string.title_notification) + "\n" + getString(R.string.content_notification);
            }
            if (DeviceInfoUtil.isOppoRom()) {// ??????Rom???OPPO???????????????????????????????????????
                tip += getString(R.string.open_auto_launcher);
            }
            if (!TextUtils.isEmpty(tip)) {
                SelectionFrame dialog = new SelectionFrame(this);
                dialog.setSomething(null, tip, new SelectionFrame.OnSelectionFrameClickListener() {
                    @Override
                    public void cancelClick() {

                    }

                    @Override
                    public void confirmClick() {
                        PermissionUtil.startApplicationDetailsSettings(FLYMainActivity.this, 0x001);
                    }
                });
                dialog.show();
            }
        } else if (launchCount == 2) {
            if (DeviceInfoUtil.isMiuiRom() || DeviceInfoUtil.isMeizuRom()) {
                SelectionFrame dialog = new SelectionFrame(this);
                dialog.setSomething(getString(R.string.open_screen_lock_show),
                        getString(R.string.open_screen_lock_show_for_audio), new SelectionFrame.OnSelectionFrameClickListener() {
                            @Override
                            public void cancelClick() {

                            }

                            @Override
                            public void confirmClick() {
                                PermissionUtil.startApplicationDetailsSettings(FLYMainActivity.this, 0x001);
                            }
                        });
                dialog.show();
            }
        }
    }

    private void initOther() {
        Log.d(TAG, "initOther() called");

        // ????????????????????????????????????????????????ID?????????????????????????????????
        // ?????????????????????????????????????????????????????????????????????

        //noinspection ConstantConditions
        AsyncUtils.doAsync(this, t -> {
            FLYReporter.post("?????????????????????", t);
        }, mainActivityAsyncContext -> {
//            if (coreManager.getConfig().enableGoogleFcm && googleAvailable()) {
//                if (HttpUtil.testGoogle()) {// ?????????????????????????????? ??????????????????
//                    FirebaseMessageService.init(MainActivity.this);
//                } else {// ????????????????????????????????????????????????????????????????????????????????????????????????
//                    selectPush();
//                }
//            } else {
//                selectPush();
//            }
//            updateRegisterRationId();

        });
    }

    private void updateRegisterRationId() {
        HttpUtils.post()
                .url(CoreManager.requireConfig(FLYApplication.getInstance()).configJg)
                .params("regId", FLYApplication.registrationID)
                .params("access_token", CoreManager.requireSelfStatus(this).accessToken)
                // devicesId???????????????????????????????????????????????????????????????????????????????????????
                .params("deviceId", "4")
                .build()
                .execute(new BaseCallback<Void>(Void.class) {
                    @Override
                    public void onResponse(ObjectResult<Void> result) {

                    }

                    @Override
                    public void onError(Call call, Exception e) {
                        FLYReporter.post("??????JPUSH??????Id?????????", e);
                    }

                });
    }

    private boolean googleAvailable() {
        boolean isGoogleAvailability = true;
        GoogleApiAvailability googleApiAvailability = GoogleApiAvailability.getInstance();
        int resultCode = googleApiAvailability.isGooglePlayServicesAvailable(this);
        if (resultCode != ConnectionResult.SUCCESS) {
            // ????????????????????????????????????
            // ????????????????????????????????????
            // if (googleApiAvailability.isUserResolvableError(resultCode)) {
            //     googleApiAvailability.getErrorDialog(this, resultCode, 2404).show();
            // }
            // ?????????????????????????????????
            isGoogleAvailability = false;
        }
        return isGoogleAvailability;
    }

    @SuppressWarnings({"PointlessBooleanExpression", "ConstantConditions"})
    private void selectPush() {
        // ??????Rom????????????
//        if (DeviceInfoUtil.isEmuiRom()) {
//            Log.e(TAG, "???????????????: ???????????????");
//            // ???????????? ????????????
//            HuaweiClient client = new HuaweiClient(this);
//            client.clientConnect();
//        } else if (DeviceInfoUtil.isMeizuRom()) {
//            Log.e(TAG, "???????????????: ???????????????");
//            MeizuPushMsgReceiver.init(this);
//        } else if (PushManager.isSupportPush(this)) {
//            Log.e(TAG, "???????????????: OPPO?????????");
//            OppoPushMessageService.init(this);
//        } else if (DeviceInfoUtil.isVivoRom()) {
//            Log.e(TAG, "???????????????: VIVO?????????");
//            VivoPushMessageReceiver.init(this);
//        } else if (true || DeviceInfoUtil.isMiuiRom()) {
//            Log.e(TAG, "???????????????: ???????????????");
//            if (shouldInit()) {
//                // ?????????????????????
//                MiPushClient.registerPush(this, APP_ID, APP_KEY);
//            }
//        }
    }

    public void checkTime() {
        Map<String, String> params = new HashMap<>();
        params.put("access_token", coreManager.getSelfStatus().accessToken);

        HttpUtils.get().url(coreManager.getConfig().GET_CURRENT_TIME)
                .params(params)
                .build()
                .execute(new BaseCallback<Void>(Void.class) {

                    @Override
                    public void onResponse(ObjectResult<Void> result) {
                        // ?????????config????????????????????????????????????????????????????????????
                        // ???ios???????????????????????????????????????
                        TimeUtils.responseTime(result.getCurrentTime());
                    }

                    @Override
                    public void onError(Call call, Exception e) {
                        // ??????????????????
                        Log.e("TimeUtils", "??????????????????", e);
                    }
                });
    }

    public void cancelUserCheckIfExist() {
        Log.d(TAG, "cancelUserCheckIfExist() called");
    }

    /* ?????????????????????????????????????????????????????????Fragment???????????????????????????????????????????????????????????? */
    public void removeNeedUserFragment() {
        mRadioGroup.clearCheck();
        mLastFragmentId = -1;
        isCreate = true;
    }

    /**
     * ????????????
     */
    public void login() {
        Log.d(TAG, "login() called");
        User user = coreManager.getSelf();

        Intent startIntent = CoreService.getIntent(FLYMainActivity.this, user.getUserId(), user.getPassword(), user.getNickName());
        ContextCompat.startForegroundService(FLYMainActivity.this, startIntent);

        mUserId = user.getUserId();
        numMessage = FriendDao.getInstance().getMsgUnReadNumTotal(mUserId);
        numCircle = MyZanDao.getInstance().getZanSize(coreManager.getSelf().getUserId());
        updateNumData();
        if (isCreate) {
            mRbTab1.toggle();
//            mWalletTab.toggle();
        }
    }

    public void loginOut() {
        coreManager.logout();
        removeNeedUserFragment();
        cancelUserCheckIfExist();
        if (FLYApplication.getInstance().mUserStatus == LoginHelper.STATUS_USER_TOKEN_OVERDUE) {
            FLYUserCheckedActivity.start(FLYApplication.getContext());
        }
        finish();
    }

    public void conflict() {
        Log.d(TAG, "conflict() called");
        isConflict = true;// ????????????

        coreManager.logout();
        removeNeedUserFragment();
        cancelUserCheckIfExist();
        FLYApplication.getInstance().mUserStatus = LoginHelper.STATUS_USER_TOKEN_CHANGE;
        FLYUserCheckedActivity.start(this);
        if (mActivityManager == null) {
            mActivityManager = (ActivityManager) getSystemService(Context.ACTIVITY_SERVICE);
        }
        mActivityManager.moveTaskToFront(getTaskId(), ActivityManager.MOVE_TASK_NO_USER_ACTION);
        finish();
    }

    public void need_update() {
        Log.d(TAG, "need_update() called");
        removeNeedUserFragment();
        cancelUserCheckIfExist();
        // ???????????????
        FLYUserCheckedActivity.start(this);
    }

    public void login_give_up() {
        Log.d(TAG, "login_give_up() called");
        removeNeedUserFragment();
        cancelUserCheckIfExist();
        FLYApplication.getInstance().mUserStatus = LoginHelper.STATUS_USER_NO_UPDATE;
    }

    @Subscribe(threadMode = ThreadMode.MainThread)
    public void helloEventBus(MessageSendChat message) {
        if (!message.isGroup) {
            coreManager.sendChatMessage(message.toUserId, message.chat);
        } else {
            coreManager.sendMucChatMessage(message.toUserId, message.chat);
        }
    }

    // ?????????????????????????????????
    @Subscribe(threadMode = ThreadMode.MainThread)
    public void helloEventBus(MessageEventHongdian message) {
        numCircle = message.number;
        UiUtils.updateNum(mTvCircleNum, numCircle);
    }

    // ??????????????????????????????IM,????????????????????????
    @Subscribe(threadMode = ThreadMode.MainThread)
    public void helloEventBus(MessageContactEvent mMessageEvent) {
        List<Contact> mNewContactList = ContactDao.getInstance().getContactsByToUserId(coreManager.getSelf().getUserId(),
                mMessageEvent.message);
        if (mNewContactList != null && mNewContactList.size() > 0) {
            updateContactUI(mNewContactList);
        }
    }

    /**
     * ????????????????????????????????????XMPP???????????????
     * copy by AudioOrVideoController
     */
    @Subscribe(threadMode = ThreadMode.MainThread)
    public void helloEventBus(final MessageEventCancelOrHangUp event) {
        String mLoginUserId = coreManager.getSelf().getUserId();
        ChatMessage message = new ChatMessage();
        if (event.type == 103) {          // ?????? ????????????
            message.setType(XmppMessage.TYPE_NO_CONNECT_VOICE);
        } else if (event.type == 104) {// ?????? ????????????
            message.setType(XmppMessage.TYPE_END_CONNECT_VOICE);
        } else if (event.type == 113) {// ?????? ????????????
            message.setType(XmppMessage.TYPE_NO_CONNECT_VIDEO);
        } else if (event.type == 114) {// ?????? ????????????
            message.setType(XmppMessage.TYPE_END_CONNECT_VIDEO);
        }
        message.setMySend(true);
        message.setFromUserId(mLoginUserId);
        message.setFromUserName(coreManager.getSelf().getNickName());
        message.setToUserId(event.toUserId);
        message.setContent(event.content);
        message.setTimeLen(event.callTimeLen);
        message.setTimeSend(TimeUtils.sk_time_current_time());
        message.setPacketId(UUID.randomUUID().toString().replaceAll("-", ""));

        if (ChatMessageDao.getInstance().saveNewSingleChatMessage(mLoginUserId, event.toUserId, message)) {
            ListenerManager.getInstance().notifyNewMesssage(mLoginUserId, message.getFromUserId(), message, false);
        }

        coreManager.sendChatMessage(event.toUserId, message);
        MsgBroadcast.broadcastMsgUiUpdate(mContext);   // ??????????????????
    }

    @Subscribe(threadMode = ThreadMode.MainThread)
    public void helloEventBus(MessageEventInitiateMeeting message) {
        String mLoginUserId = coreManager.getSelf().getUserId();
        String mLoginNickName = coreManager.getSelf().getNickName();

        Jitsi_connecting_second.start(this, mLoginUserId, mLoginUserId, message.type);

        for (int i = 0; i < message.list.size(); i++) {
            ChatMessage mMeetingMessage = new ChatMessage();
            int type;
            String str;
            if (message.type == CallConstants.Audio_Meet) {
                type = XmppMessage.TYPE_IS_MU_CONNECT_VOICE;
                str = getString(R.string.tip_invite_voice_meeting);
            } else if (message.type == CallConstants.Video_Meet) {
                type = XmppMessage.TYPE_IS_MU_CONNECT_VIDEO;
                str = getString(R.string.tip_invite_video_meeting);
            } else {
                type = XmppMessage.TYPE_IS_MU_CONNECT_TALK;
                str = getString(R.string.tip_invite_talk_meeting);
            }
            mMeetingMessage.setType(type);
            mMeetingMessage.setContent(str);
            mMeetingMessage.setFromUserId(mLoginUserId);
            mMeetingMessage.setFromUserName(mLoginNickName);
            mMeetingMessage.setObjectId(mLoginUserId);
            mMeetingMessage.setTimeSend(TimeUtils.sk_time_current_time());
            mMeetingMessage.setToUserId(message.list.get(i));
            mMeetingMessage.setPacketId(UUID.randomUUID().toString().replaceAll("-", ""));
            coreManager.sendChatMessage(message.list.get(i), mMeetingMessage);
            // ??????????????????????????????
/*
            ChatMessageDao.getInstance().saveNewSingleChatMessage(mLoginUserId, message.list.get(i), mMeetingMessage);
            FriendDao.getInstance().updateFriendContent(mLoginUserId, message.list.get(i), str, type, TimeUtils.sk_time_current_time());
*/
        }
    }

    /*
    ??????????????? || ??????????????? ??????????????????????????????????????? ???????????????????????????
     */
    @Subscribe(threadMode = ThreadMode.MainThread)
    public void helloEventBus(EventSendVerifyMsg eventSendVerifyMsg) {
        String mLoginUserId = coreManager.getSelf().getUserId();
        String mLoginUserName = coreManager.getSelf().getNickName();
        ChatMessage message = new ChatMessage();
        message.setType(XmppMessage.TYPE_GROUP_VERIFY);
        message.setFromUserId(mLoginUserId);
        message.setToUserId(eventSendVerifyMsg.getCreateUserId());
        message.setFromUserName(mLoginUserName);
        message.setIsEncrypt(0);
        String s = JsonUtils.initJsonContent(mLoginUserId, mLoginUserName, eventSendVerifyMsg.getGroupJid(), "1", eventSendVerifyMsg.getReason());
        message.setObjectId(s);
        message.setPacketId(UUID.randomUUID().toString().replaceAll("-", ""));
        message.setTimeSend(TimeUtils.sk_time_current_time());
        if (coreManager.isLogin()) {
            coreManager.sendChatMessage(eventSendVerifyMsg.getCreateUserId(), message);
        }
    }

    @Subscribe(threadMode = ThreadMode.MainThread)
    public void helloEventBus(MessageEventBG mMessageEventBG) {
        if (mMessageEventBG.flag) {// ???????????????
            // ????????????
            showDeviceLock();
            // ?????????????????????
            NotificationManager mNotificationManager = (NotificationManager) this.getSystemService(Context.NOTIFICATION_SERVICE);
            if (mNotificationManager != null) {
                mNotificationManager.cancelAll();
            }

            if (isConflict) {// ????????????????????????????????????
                isConflict = false;// Reset Status
                Log.e("zq", "????????????????????????????????????");
                return;
            }

            if (!coreManager.isServiceReady()) {
                // ?????????????????????????????????CoreService????????????????????????????????????ta
                Log.e("zq", "CoreService?????????????????????");
                coreManager.relogin();
            } else {
                if (!coreManager.isLogin()) {// XMPP?????????
                    Log.e("zq", "XMPP????????????????????????");
                    coreManager.autoReconnect(FLYMainActivity.this);
                }
            }
        } else {
            MachineDao.getInstance().resetMachineStatus();

            FLYApplication.getInstance().appBackstage(false);
        }
    }

    /*
    ??????????????? || ??????????????? ???????????? ????????????????????????
    */
    @Subscribe(threadMode = ThreadMode.MainThread)
    public void helloEventBus(EventCreateGroupFriend eventCreateGroupFriend) {
        String mLoginUserId = coreManager.getSelf().getUserId();
        String mLoginUserName = coreManager.getSelf().getNickName();
        MucRoom room = eventCreateGroupFriend.getMucRoom();

        FLYApplication.getInstance().saveGroupPartStatus(room.getJid(), room.getShowRead(), room.getAllowSendCard(),
                room.getAllowConference(), room.getAllowSpeakCourse(), room.getTalkTime());

        Friend friend = new Friend();
        friend.setOwnerId(mLoginUserId);
        friend.setUserId(room.getJid());
        friend.setNickName(room.getName());
        friend.setDescription(room.getDesc());
        friend.setRoomId(room.getId());
        friend.setRoomCreateUserId(room.getUserId());
        friend.setChatRecordTimeOut(room.getChatRecordTimeOut());// ?????????????????? -1/0 ??????
        friend.setContent(mLoginUserName + " " + getString(R.string.Message_Object_Group_Chat));
        friend.setTimeSend(TimeUtils.sk_time_current_time());
        friend.setRoomFlag(1);
        friend.setStatus(Friend.STATUS_FRIEND);
        FriendDao.getInstance().createOrUpdateFriend(friend);

        // ??????socket?????????????????????
        coreManager.joinMucChat(room.getJid(), 0);
    }

    private boolean shouldInit() {
        ActivityManager activityManager = ((ActivityManager) getSystemService(Context.ACTIVITY_SERVICE));
        List<ActivityManager.RunningAppProcessInfo> processes = activityManager.getRunningAppProcesses();
        String mainProcessName = getPackageName();
        int myPid = Process.myPid();
        for (ActivityManager.RunningAppProcessInfo info : processes) {
            if (info.pid == myPid && mainProcessName.equals(info.processName)) {
                return true;
            }
        }
        return false;
    }

    /**
     * ???????????????
     */
    public void hideInput() {
        InputMethodManager imm = (InputMethodManager) getSystemService(Context.INPUT_METHOD_SERVICE);
        IBinder token = getWindow().getDecorView().getWindowToken();
        if (imm != null && imm.isActive() && token != null) {
            imm.hideSoftInputFromWindow(token, InputMethodManager.HIDE_NOT_ALWAYS);
        }
    }

    /**
     * ???????????????????????????
     */
    private void addressBookOperation() {
        boolean isReadContacts = PermissionUtil.checkSelfPermissions(this, new String[]{Manifest.permission.READ_CONTACTS});
        if (isReadContacts) {
            try {
                uploadAddressBook();
            } catch (Exception e) {
                String message = getString(R.string.tip_read_contacts_failed);
                ToastUtil.showToast(this, message);
                FLYReporter.post(message, e);
                ContactsUtil.cleanLocalCache(this, coreManager.getSelf().getUserId());
            }
        } else {
            String[] permissions = new String[]{Manifest.permission.READ_CONTACTS};
            if (!PermissionUtil.deniedRequestPermissionsAgain(this, permissions)) {
                PermissionExplainDialog tip = new PermissionExplainDialog(this);
                tip.setPermissions(permissions);
                tip.setOnConfirmListener(() -> {
                    PermissionUtil.requestPermissions(this, 0x01, permissions);
                });
                tip.show();
            } else {
                PermissionUtil.requestPermissions(this, 0x01, permissions);
            }
        }
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, String[] permissions, int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
        PermissionUtil.onRequestPermissionsResult(requestCode, permissions, grantResults, this);
    }

    @Override
    public void onPermissionsGranted(int requestCode, List<String> perms, boolean isAllGranted) {
        if (isAllGranted) {// ?????????
            try {
                uploadAddressBook();
            } catch (Exception e) {
                String message = getString(R.string.tip_read_contacts_failed);
                ToastUtil.showToast(this, message);
                FLYReporter.post(message, e);
                ContactsUtil.cleanLocalCache(this, coreManager.getSelf().getUserId());
            }
        }
    }

    @Override
    public void onPermissionsDenied(int requestCode, List<String> perms, boolean isAllDenied) {

    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        switch (requestCode) {
            case 888:
                if (resultCode == Activity.RESULT_OK) {
                    if (data == null || data.getExtras() == null) {
                        return;
                    }
                    String result = data.getExtras().getString(Constant.EXTRA_RESULT_CONTENT);
                    Log.e("zq", "????????????????????????" + result);
                    if (TextUtils.isEmpty(result)) {
                        return;
                    }
                    if (PaymentReceiptMoneyActivity.checkQrCode(result)) {
                        // ?????????19??? && ????????? ???????????????????????? ??????????????????
                        Intent intent = new Intent(mContext, PaymentReceiptMoneyActivity.class);
                        intent.putExtra("PAYMENT_ORDER", result);
                        startActivity(intent);
                    } else if (result.contains("userId")
                            && result.contains("userName")) {
                        // ???????????????????????? ??????????????????
                        Intent intent = new Intent(mContext, ReceiptPayMoneyActivity.class);
                        intent.putExtra("RECEIPT_ORDER", result);
                        startActivity(intent);
                    } else if (ReceiveChatHistoryActivity.checkQrCode(result)) {
                        // ?????????????????????????????????????????????????????????????????????????????????
                        ReceiveChatHistoryActivity.start(this, result);
                    } else if (WebLoginActivity.checkQrCode(result)) {
                        // ????????????????????????????????????????????????????????????
                        WebLoginActivity.start(this, result);
                    } else {
                        if (result.contains("shikuId")) {
                            // ?????????
                            Map<String, String> map = WebViewActivity.URLRequest(result);
                            String action = map.get("action");
                            String userId = map.get("shikuId");
                            if (TextUtils.equals(action, "group")) {
                                getRoomInfo(userId);
                            } else if (TextUtils.equals(action, "user")) {
                                getUserInfo(userId);
                            } else {
                                FLYReporter.post("????????????????????????<" + result + ">");
                                ToastUtil.showToast(this, R.string.unrecognized);
                            }
                        } else if (!result.contains("shikuId")
                                && HttpUtil.isURL(result)) {
                            // ????????????  ???????????????
                            Intent intent = new Intent(this, WebViewActivity.class);
                            intent.putExtra(WebViewActivity.EXTRA_URL, result);
                            startActivity(intent);
                        } else {
                            FLYReporter.post("????????????????????????<" + result + ">");
                            ToastUtil.showToast(this, R.string.unrecognized);
                        }
                    }
                }
                break;
            default:
                super.onActivityResult(requestCode, resultCode, data);
        }
    }

    /**
     * ?????????????????????userId
     */
    private void getUserInfo(String account) {
        Map<String, String> params = new HashMap<>();
        params.put("access_token", CoreManager.requireSelfStatus(FLYApplication.getInstance()).accessToken);
        params.put("account", account);

        HttpUtils.get().url(CoreManager.requireConfig(FLYApplication.getInstance()).USER_GET_URL_ACCOUNT)
                .params(params)
                .build()
                .execute(new BaseCallback<User>(User.class) {
                    @Override
                    public void onResponse(ObjectResult<User> result) {
                        if (result.getResultCode() == 1 && result.getData() != null) {
                            User user = result.getData();
                            BasicInfoActivity.start(mContext, user.getUserId(), BasicInfoActivity.FROM_ADD_TYPE_QRCODE);
                        } else {
                            ToastUtil.showErrorData(FLYApplication.getInstance());
                        }
                    }

                    @Override
                    public void onError(Call call, Exception e) {
                        ToastUtil.showNetError(FLYApplication.getInstance());
                    }
                });
    }

    /**
     * ??????????????????
     */
    private void getRoomInfo(String roomId) {
        Friend friend = FriendDao.getInstance().getMucFriendByRoomId(coreManager.getSelf().getUserId(), roomId);
        if (friend != null) {
            if (friend.getGroupStatus() == 0) {
                interMucChat(friend.getUserId(), friend.getNickName());
                return;
            } else {// ????????????????????? || ?????????????????? || ????????????????????????
                FriendDao.getInstance().deleteFriend(coreManager.getSelf().getUserId(), friend.getUserId());
                ChatMessageDao.getInstance().deleteMessageTable(coreManager.getSelf().getUserId(), friend.getUserId());
            }
        }

        Map<String, String> params = new HashMap<>();
        params.put("access_token", coreManager.getSelfStatus().accessToken);
        params.put("roomId", roomId);

        HttpUtils.get().url(coreManager.getConfig().ROOM_GET)
                .params(params)
                .build()
                .execute(new BaseCallback<MucRoom>(MucRoom.class) {

                    @Override
                    public void onResponse(ObjectResult<MucRoom> result) {
                        if (result.getResultCode() == 1 && result.getData() != null) {
                            final MucRoom mucRoom = result.getData();
                            if (mucRoom.getIsNeedVerify() == 1) {
                                VerifyDialog verifyDialog = new VerifyDialog(FLYMainActivity.this);
                                verifyDialog.setVerifyClickListener(FLYApplication.getInstance().getString(R.string.tip_reason_invite_friends), new VerifyDialog.VerifyClickListener() {
                                    @Override
                                    public void cancel() {

                                    }

                                    @Override
                                    public void send(String str) {
                                        EventBus.getDefault().post(new EventSendVerifyMsg(mucRoom.getUserId(), mucRoom.getJid(), str));
                                    }
                                });
                                verifyDialog.show();
                                return;
                            }
                            joinRoom(mucRoom, coreManager.getSelf().getUserId());
                        } else {
                            ToastUtil.showErrorData(FLYMainActivity.this);
                        }
                    }

                    @Override
                    public void onError(Call call, Exception e) {
                        ToastUtil.showNetError(FLYMainActivity.this);
                    }
                });
    }

    /**
     * ????????????
     */
    private void joinRoom(final MucRoom room, final String loginUserId) {
        DialogHelper.showDefaulteMessageProgressDialog(FLYMainActivity.this);
        Map<String, String> params = new HashMap<>();
        params.put("access_token", coreManager.getSelfStatus().accessToken);
        params.put("roomId", room.getId());
        if (room.getUserId().equals(loginUserId))
            params.put("type", "1");
        else
            params.put("type", "2");

        FLYApplication.mRoomKeyLastCreate = room.getJid();

        HttpUtils.get().url(coreManager.getConfig().ROOM_JOIN)
                .params(params)
                .build()
                .execute(new BaseCallback<Void>(Void.class) {

                    @Override
                    public void onResponse(ObjectResult<Void> result) {
                        DialogHelper.dismissProgressDialog();
                        if (Result.checkSuccess(FLYMainActivity.this, result)) {
                            EventBus.getDefault().post(new EventCreateGroupFriend(room));
                            mHandler.postDelayed(new Runnable() {
                                @Override
                                public void run() {// ???500ms?????????????????????????????????????????????????????????????????????
                                    interMucChat(room.getJid(), room.getName());
                                }
                            }, 500);
                        } else {
                            FLYApplication.mRoomKeyLastCreate = "compatible";
                        }
                    }

                    @Override
                    public void onError(Call call, Exception e) {
                        DialogHelper.dismissProgressDialog();
                        ToastUtil.showErrorNet(FLYMainActivity.this);
                        FLYApplication.mRoomKeyLastCreate = "compatible";
                    }
                });
    }

    /**
     * ????????????
     */
    private void interMucChat(String roomJid, String roomName) {
        Intent intent = new Intent(FLYMainActivity.this, MucChatActivity.class);
        intent.putExtra(FLYAppConstant.EXTRA_USER_ID, roomJid);
        intent.putExtra(FLYAppConstant.EXTRA_NICK_NAME, roomName);
        intent.putExtra(FLYAppConstant.EXTRA_IS_GROUP_CHAT, true);
        startActivity(intent);

        MucgroupUpdateUtil.broadcastUpdateUi(FLYMainActivity.this);
    }

    private void uploadAddressBook() {
        List<Contacts> mNewAdditionContacts = ContactsUtil.getNewAdditionContacts(this, coreManager.getSelf().getUserId());
        /**
         * ????????????
         * [{"name":"15768779999","telephone":"8615768779999"},{"name":"?????????","telephone":"8615720966659"},
         * {"name":"zas","telephone":"8613000000000"},{"name":"????????????","telephone":"864007883333"},]
         * ???????????????
         * [{\"toTelephone\":\"15217009762\",\"toRemarkName\":\"????????????????????????\"},{\"toTelephone\":\"15217009762\",\"toRemarkName\":\"????????????????????????\"}]
         */
        if (mNewAdditionContacts.size() <= 0) {
            return;
        }

        String step1 = JSON.toJSONString(mNewAdditionContacts);
        String step2 = step1.replaceAll("name", "toRemarkName");
        String contactsListStr = step2.replaceAll("telephone", "toTelephone");
        Log.e("contact", "????????????????????????" + contactsListStr);

        Map<String, String> params = new HashMap<>();
        params.put("access_token", coreManager.getSelfStatus().accessToken);
        params.put("uploadJsonStr", contactsListStr);

        HttpUtils.post().url(coreManager.getConfig().ADDRESSBOOK_UPLOAD)
                .params(params)
                .build()
                .execute(new ListCallback<Contact>(Contact.class) {

                    @Override
                    public void onResponse(ArrayResult<Contact> result) {
                        if (result.getResultCode() == 1 && result.getData() != null) {
                            List<Contact> mContactList = result.getData();
                            for (int i = 0; i < mContactList.size(); i++) {
                                Contact contact = mContactList.get(i);
                                if (ContactDao.getInstance().createContact(contact)) {
                                    if (contact.getStatus() == 1) {// ???????????????????????????????????????????????????
                                        NewFriendDao.getInstance().addFriendOperating(contact.getToUserId(), contact.getToUserName(), contact.getToRemarkName());
                                    }
                                }
                            }

                            if (mContactList.size() > 0) {// ????????????????????????  ????????????contacts id
                                updateContactUI(mContactList);
                            }
                        }
                    }

                    @Override
                    public void onError(Call call, Exception e) {

                    }
                });
    }

    private void updateRoom() {
        HashMap<String, String> params = new HashMap<>();
        params.put("access_token", coreManager.getSelfStatus().accessToken);
        params.put("type", "0");
        params.put("pageIndex", "0");
        params.put("pageSize", "1000");// ????????????????????????

        HttpUtils.get().url(coreManager.getConfig().ROOM_LIST_HIS)
                .params(params)
                .build()
                .execute(new ListCallback<MucRoom>(MucRoom.class) {
                    @Override
                    public void onResponse(ArrayResult<MucRoom> result) {
                        if (result.getResultCode() == 1) {
                            FriendDao.getInstance().addRooms(mHandler, coreManager.getSelf().getUserId(), result.getData(), new OnCompleteListener2() {
                                @Override
                                public void onLoading(int progressRate, int sum) {

                                }

                                @Override
                                public void onCompleted() {
                                    if (coreManager.isLogin()) {
                                        coreManager.batchMucChat();
                                    }
                                    MsgBroadcast.broadcastMsgUiUpdate(FLYMainActivity.this);
                                }
                            });
                        }
                    }

                    @Override
                    public void onError(Call call, Exception e) {

                    }
                });
    }

    /*
    ?????? ??????
     */
    public void msg_num_update(int operation, int count) {
        numMessage = (operation == MsgBroadcast.NUM_ADD) ? numMessage + count : numMessage - count;
        updateNumData();
    }

    public void msg_num_reset() {
        numMessage = FriendDao.getInstance().getMsgUnReadNumTotal(mUserId);
        numCircle = MyZanDao.getInstance().getZanSize(coreManager.getSelf().getUserId());
        updateNumData();
    }

    public void updateNumData() {
        numMessage = FriendDao.getInstance().getMsgUnReadNumTotal(mUserId);
        numCircle = MyZanDao.getInstance().getZanSize(coreManager.getSelf().getUserId());

        ShortcutBadger.applyCount(this, numMessage);

        UiUtils.updateNum(mTvMessageNum, numMessage);
        UiUtils.updateNum(mTvCircleNum, numCircle);
    }

    /*
    ?????????
     */
    public void updateNewFriendMsgNum(int msgNum) {
        int mNewContactsNumber = PreferenceUtils.getInt(this, Constants.NEW_CONTACTS_NUMBER + coreManager.getSelf().getUserId(),
                0);
        int totalNumber = msgNum + mNewContactsNumber;

        if (totalNumber == 0) {
            mTvNewFriendNum.setText("");
            mTvNewFriendNum.setVisibility(View.INVISIBLE);
        } else {
            mTvNewFriendNum.setText(totalNumber + "");
            mTvNewFriendNum.setVisibility(View.VISIBLE);
        }
    }

    private void updateContactUI(List<Contact> mContactList) {
        String mLoginUserId = coreManager.getSelf().getUserId();
        int mContactsNumber = PreferenceUtils.getInt(FLYMainActivity.this, Constants.NEW_CONTACTS_NUMBER + mLoginUserId, 0);
        int mTotalContactsNumber = mContactsNumber + mContactList.size();
        PreferenceUtils.putInt(FLYMainActivity.this, Constants.NEW_CONTACTS_NUMBER + mLoginUserId, mTotalContactsNumber);
        Friend newFriend = FriendDao.getInstance().getFriend(coreManager.getSelf().getUserId(), Friend.ID_NEW_FRIEND_MESSAGE);
        updateNewFriendMsgNum(newFriend.getUnReadNum());

        List<String> mNewContactsIds = new ArrayList<>();
        for (int i = 0; i < mContactList.size(); i++) {
            mNewContactsIds.add(mContactList.get(i).getToUserId());
        }
        String mContactsIds = PreferenceUtils.getString(FLYMainActivity.this, Constants.NEW_CONTACTS_IDS + mLoginUserId);
        List<String> ids = JSON.parseArray(mContactsIds, String.class);
        if (ids != null && ids.size() > 0) {
            mNewContactsIds.addAll(ids);
        }
        PreferenceUtils.putString(FLYMainActivity.this, Constants.NEW_CONTACTS_IDS + mLoginUserId, JSON.toJSONString(mNewContactsIds));
    }

    // ???????????????????????????????????????????????????
    private void emptyServerMessage(String friendId) {
        Map<String, String> params = new HashMap<>();
        params.put("access_token", coreManager.getSelfStatus().accessToken);
        params.put("type", String.valueOf(0));// 0 ???????????? 1 ????????????
        params.put("toUserId", friendId);

        HttpUtils.get().url(coreManager.getConfig().EMPTY_SERVER_MESSAGE)
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

    private void updateSelfData() {
        Map<String, String> params = new HashMap<>();
        params.put("access_token", coreManager.getSelfStatus().accessToken);

        HttpUtils.get().url(coreManager.getConfig().USER_GET_URL)
                .params(params)
                .build()
                .execute(new BaseCallback<User>(User.class) {
                    @Override
                    public void onResponse(ObjectResult<User> result) {
                        if (result.getResultCode() == 1 && result.getData() != null) {
                            User user = result.getData();
                            boolean updateSuccess = UserDao.getInstance().updateByUser(user);
                            // ????????????????????????
                            if (updateSuccess) {
                                // ?????????????????????User?????????
                                coreManager.setSelf(user);
                                // ??????MeFragment??????
                                sendBroadcast(new Intent(OtherBroadcast.SYNC_SELF_DATE_NOTIFY));
                            }
                        }
                    }

                    @Override
                    public void onError(Call call, Exception e) {

                    }
                });
    }

    public void notifyCollectionList() {
        Map<String, String> params = new HashMap<>();
        params.put("access_token", coreManager.getSelfStatus().accessToken);
        params.put("userId", coreManager.getSelf().getUserId());

        HttpUtils.get().url(coreManager.getConfig().Collection_LIST)
                .params(params)
                .build()
                .execute(new ListCallback<Collectiion>(Collectiion.class) {
                    @Override
                    public void onResponse(ArrayResult<Collectiion> result) {
                        if (Result.checkSuccess(mContext, result)) {
                            FLYApplication.mCollection = result.getData();
                            Collectiion collection = new Collectiion();
                            collection.setType(7);
                            FLYApplication.mCollection.add(0, collection);
                            // ????????????????????????
                            sendBroadcast(new Intent(OtherBroadcast.CollectionRefresh_ChatFace));
                        }

                    }

                    @Override
                    public void onError(Call call, Exception e) {
                        ToastUtil.showNetError(FLYApplication.getContext());
                    }
                });
    }

    private class My_BroadcastReceiver extends BroadcastReceiver {
        @Override
        public void onReceive(Context context, Intent intent) {
            String action = intent.getAction();
            if (TextUtils.isEmpty(action)) {
                return;
            }

            if (action.equals(Constants.UPDATE_ROOM)) {
                updateRoom();
            } else if (action.equals(SocketException.FINISH_CONNECT_EXCEPTION)) {
                coreManager.autoReconnect(FLYMainActivity.this);
            } else if (action.equals(OtherBroadcast.SYNC_CLEAN_CHAT_HISTORY)) {
                String friendId = intent.getStringExtra(FLYAppConstant.EXTRA_USER_ID);
                emptyServerMessage(friendId);

                FriendDao.getInstance().resetFriendMessage(coreManager.getSelf().getUserId(), friendId);
                ChatMessageDao.getInstance().deleteMessageTable(coreManager.getSelf().getUserId(), friendId);
                sendBroadcast(new Intent(Constants.CHAT_HISTORY_EMPTY));// ??????????????????
                MsgBroadcast.broadcastMsgUiUpdate(mContext);
            } else if (action.equals(OtherBroadcast.SYNC_SELF_DATE)) {
                updateSelfData();
            } else if (action.equals(OtherBroadcast.CollectionRefresh)) {
                notifyCollectionList();
            } else if (action.equals(OtherBroadcast.SEND_MULTI_NOTIFY)) {
                mRbTab4.setChecked(false);
                mRbTab1.setChecked(true);
            }
        }
    }

    public void downloadAdSource(int sourceType, NavBean navBean) {
        androidDownloadManager = new AndroidDownloadManager(FLYApplication.getInstance(), navBean.getImgUrl())
                .setListener(new AndroidDownloadManagerListener() {
                    @Override
                    public void onPrepare() {
                        LogUtil.d("???????????????");
                    }

                    @Override
                    public void onSuccess(String path) {
                        PreferenceUtils.putInt(FLYApplication.getInstance(), Constants.KEY_SPLASH_AD_TYPE, sourceType);
                        PreferenceUtils.putString(FLYApplication.getInstance(), Constants.KEY_SPLASH_AD_PATH, path);
                        PreferenceUtils.putString(FLYApplication.getInstance(), Constants.KEY_SPLASH_AD_URL, navBean.getImgUrl());
                        PreferenceUtils.putString(FLYApplication.getInstance(), Constants.KEY_SPLASH_AD_TITLE, navBean.getDesc());
                        PreferenceUtils.putString(FLYApplication.getInstance(), Constants.KEY_SPLASH_AD_WEBSITE, navBean.getLink());
                        LogUtil.d("???????????????????????????????????? = " + path);
                    }

                    @Override
                    public void onFailed(Throwable throwable) {
                        LogUtil.d("???????????? = " + throwable.getMessage());
                    }
                });
        androidDownloadManager.download();

    }

    private void getConfig() {
        String mConfigApi = FLYAppConfig.readConfigUrl(mContext);

        Map<String, String> params = new HashMap<>();
        FLYReporter.putUserData("configUrl", mConfigApi);
        HttpUtils.get().url(mConfigApi)
                .params(params)
                .build(true, true)
                .execute(new BaseCallback<ConfigBean>(ConfigBean.class) {
                    @Override
                    public void onResponse(ObjectResult<ConfigBean> result) {
                        if (result != null) {
                            TimeUtils.responseTime(result.getCurrentTime());
                        }
                        ConfigBean configBean;
                        if (result == null || result.getData() == null || result.getResultCode() != Result.CODE_SUCCESS) {
                            Log.e("zq", "?????????????????????????????????????????????????????????");
                            if (BuildConfig.DEBUG) {
                                ToastUtil.showToast(FLYMainActivity.this, R.string.tip_get_config_failed);
                            }
                            // ????????????????????????????????????????????????????????????
                            configBean = coreManager.readConfigBean();
                        } else {
                            Log.e("zq", "??????????????????????????????????????????????????????????????????????????????");
                            configBean = result.getData();
                            if (!TextUtils.isEmpty(configBean.getAddress())) {
                                PreferenceUtils.putString(FLYMainActivity.this, FLYAppConstant.EXTRA_CLUSTER_AREA, configBean.getAddress());
                            }
                            coreManager.saveConfigBean(configBean);
                            FLYApplication.IS_OPEN_CLUSTER = configBean.getIsOpenCluster() == 1;
                        }
                        setConfig(configBean);
                    }

                    @Override
                    public void onError(Call call, Exception e) {
                        Log.e("zq", "?????????????????????????????????????????????????????????");
                        // ToastUtil.showToast(SplashActivity.this, R.string.tip_get_config_failed);
                        // ????????????????????????????????????????????????????????????
                        ConfigBean configBean = coreManager.readConfigBean();
                        setConfig(configBean);
                    }
                });
    }

    private void setConfig(ConfigBean configBean) {
        if (configBean == null) {
            if (BuildConfig.DEBUG) {
                ToastUtil.showToast(this, R.string.tip_get_config_failed);
            }
            // ????????????????????????????????????????????????????????????????????????????????????????????????
            configBean = CoreManager.getDefaultConfig(this);
            coreManager.saveConfigBean(configBean);
        }
        //????????????
//        checkUpdate();

        //????????????????????????
        isAlreadyGroup();
    }

    /**
     * ???????????????????????????(???????????????????????????????????????????????????)
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
                            }
                        }
                    }

                    @Override
                    public void onError(Call call, Exception e) {
                    }
                });
    }

    private void checkUpdate() {
        Map<String, String> params = new HashMap<>();
        params.put("type", "android");
        HttpUtils.post().url(Apis.DOWNLOAD_URL)
                .params(params)
                .build()
                .execute(new BaseCallback<ApkUpdateBean>(ApkUpdateBean.class) {

                    @Override
                    public void onResponse(ObjectResult<ApkUpdateBean> result) {
                        if (result == null) {
                            return;
                        }
                        if (result.getResultCode() != 1) {
                            ToastUtil.showToast(FLYMainActivity.this, result.getMsg());
                            return;
                        }

                        ApkUpdateBean updateVersionBean = result.getData();
                        if (null == updateVersionBean) {
                            return;
                        }
                        String versionNum = updateVersionBean.getVersionNo();
                        if (TextUtils.isEmpty(versionNum)) {
                            return;
                        }

                        try {
                            String isForceUpdate = updateVersionBean.getIsUpdates();
                            //S:??????   F
                            if ("S".equalsIgnoreCase(isForceUpdate)) {
//                                UpdateManger.checkUpdate(
//                                        FLYMainActivity.this,
//                                        updateVersionBean.getDownloadAddress(),
//                                        updateVersionBean.getVersionNo(),
//                                        updateVersionBean.getContent());
                                checkVersion(updateVersionBean.getDownloadAddress(), updateVersionBean.getVersionNo());
                            }


                        } catch (Exception e) {

                        }

                    }

                    @Override
                    public void onError(Call call, Exception e) {

                    }
                });
    }

    /**
     * ??????????????????????????????????????????
     *
     * @param roomId
     */
    private void isJoinGroup(String roomId) {
        Map<String, String> params = new HashMap<>();
        params.put("roomId", roomId);
        params.put("userId", coreManager.getSelf().getUserId());

        HttpUtils.get().url(coreManager.getConfig().IS_JOIN_GROUP)
                .params(params)
                .build()
                .execute(new BaseCallback<Void>(Void.class) {

                    @Override
                    public void onResponse(ObjectResult<Void> result) {
                        if (result == null) {
                            return;
                        }
                        //?????????????????? ????????????????????????????????????
                        if (result.getResultCode() == 1){
                            getRoomInfo(roomId);
                        }
                    }

                    @Override
                    public void onError(Call call, Exception e) {

                    }
                });
    }

    /**
     * ?????????????????????
     */
    private void allGroup() {
        HttpUtils.post().url(coreManager.getConfig().ALL_GROUP)
                .build()
                .execute(new BaseCallback<AllGroupBean>(AllGroupBean.class) {

                    @Override
                    public void onResponse(ObjectResult<AllGroupBean> result) {
                        if (result == null) {
                            return;
                        }

                        if (result.getData().getUserSize() == result.getData().getMaxUserSize()){
                            return;
                        }
                        isJoinGroup(result.getData().getId());
                    }

                    @Override
                    public void onError(Call call, Exception e) {

                    }
                });
    }

    /**
     * ????????????????????????????????????
     *
     */
    private void isAlreadyGroup() {
        Map<String, String> params = new HashMap<>();
        params.put("userId", coreManager.getSelf().getUserId());

        HttpUtils.get().url(coreManager.getConfig().IS_ALREADY_GROUP)
                .params(params)
                .build()
                .execute(new BaseCallback<Void>(Void.class) {

                    @Override
                    public void onResponse(ObjectResult<Void> result) {
                        if (result == null) {
                            return;
                        }
                        //??????????????? ??????????????????????????????
                        if (result.getResultCode() == 1){
                            allGroup();
                        }
                    }

                    @Override
                    public void onError(Call call, Exception e) {

                    }
                });
    }
}