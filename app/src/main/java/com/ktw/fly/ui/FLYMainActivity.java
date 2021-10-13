package com.ktw.fly.ui;

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
import com.facebook.react.shell.MainPackageConfig;
import com.fanjun.keeplive.KeepLive;
import com.fanjun.keeplive.config.KeepLiveService;
import com.google.android.gms.common.ConnectionResult;
import com.google.android.gms.common.GoogleApiAvailability;
import com.ktw.fly.FLYAppConfig;
import com.ktw.fly.FLYAppConstant;
import com.ktw.fly.BuildConfig;
import com.ktw.fly.FLYApplication;
import com.ktw.fly.R;
import com.ktw.fly.FLYReporter;
import com.ktw.fly.bean.AutoAnswerBean;
import com.ktw.fly.bean.ConfigBean;
import com.ktw.fly.bean.Contact;
import com.ktw.fly.bean.Contacts;
import com.ktw.fly.bean.Friend;
import com.ktw.fly.bean.MyCollectEmotPackageBean;
import com.ktw.fly.bean.NavBean;
import com.ktw.fly.bean.UploadingFile;
import com.ktw.fly.bean.User;
import com.ktw.fly.bean.collection.Collectiion;
import com.ktw.fly.bean.event.EventCreateGroupFriend;
import com.ktw.fly.bean.event.EventSendVerifyMsg;
import com.ktw.fly.bean.event.MessageContactEvent;
import com.ktw.fly.bean.event.MessageEventBG;
import com.ktw.fly.bean.event.MessageEventHongdian;
import com.ktw.fly.bean.event.MessageLogin;
import com.ktw.fly.bean.event.MessageSendChat;
import com.ktw.fly.bean.message.ChatMessage;
import com.ktw.fly.bean.message.MucRoom;
import com.ktw.fly.bean.message.XmppMessage;
import com.ktw.fly.broadcast.MsgBroadcast;
import com.ktw.fly.broadcast.MucgroupUpdateUtil;
import com.ktw.fly.broadcast.OtherBroadcast;
import com.ktw.fly.broadcast.TimeChangeReceiver;
import com.ktw.fly.broadcast.UpdateUnReadReceiver;
import com.ktw.fly.broadcast.UserLogInOutReceiver;
import com.ktw.fly.call.AudioOrVideoController;
import com.ktw.fly.call.CallConstants;
import com.ktw.fly.call.Jitsi_connecting_second;
import com.ktw.fly.call.MessageEventCancelOrHangUp;
import com.ktw.fly.call.MessageEventInitiateMeeting;
import com.ktw.fly.db.dao.ChatMessageDao;
import com.ktw.fly.db.dao.ContactDao;
import com.ktw.fly.db.dao.FriendDao;
import com.ktw.fly.db.dao.MyZanDao;
import com.ktw.fly.db.dao.NewFriendDao;
import com.ktw.fly.db.dao.OnCompleteListener2;
import com.ktw.fly.db.dao.UploadingFileDao;
import com.ktw.fly.db.dao.UserDao;
import com.ktw.fly.db.dao.login.MachineDao;
import com.ktw.fly.downloader.UpdateManger;
import com.ktw.fly.fragment.FindFragment;
import com.ktw.fly.fragment.FriendFragment;
import com.ktw.fly.fragment.MeFragment;
import com.ktw.fly.fragment.MessageFragment;
import com.ktw.fly.fragment.Nav1Fragment;
import com.ktw.fly.fragment.Nav2Fragment;
import com.ktw.fly.sp.UserSp;
import com.ktw.fly.wallet.Apis;
import com.ktw.fly.wallet.WalletFragment;
import com.ktw.fly.helper.DialogHelper;
import com.ktw.fly.helper.LoginHelper;
import com.ktw.fly.helper.LoginSecureHelper;
import com.ktw.fly.helper.PrivacySettingHelper;
import com.ktw.fly.map.MapHelper;
import com.ktw.fly.pay.PaymentReceiptMoneyActivity;
import com.ktw.fly.pay.ReceiptPayMoneyActivity;
import com.ktw.fly.socket.SocketException;
import com.ktw.fly.ui.backup.ReceiveChatHistoryActivity;
import com.ktw.fly.ui.base.BaseActivity;
import com.ktw.fly.ui.base.CoreManager;
import com.ktw.fly.ui.lock.DeviceLockActivity;
import com.ktw.fly.ui.lock.DeviceLockHelper;
import com.ktw.fly.ui.login.WebLoginActivity;
import com.ktw.fly.ui.me.emot.MyEmotBean;
import com.ktw.fly.ui.message.MucChatActivity;
import com.ktw.fly.ui.other.BasicInfoActivity;
import com.ktw.fly.ui.tool.WebViewActivity;
import com.ktw.fly.util.AppUtils;
import com.ktw.fly.util.AsyncUtils;
import com.ktw.fly.util.Constants;
import com.ktw.fly.util.ContactsUtil;
import com.ktw.fly.util.DeviceInfoUtil;
import com.ktw.fly.util.DisplayUtil;
import com.ktw.fly.util.FileUtil;
import com.ktw.fly.util.HttpUtil;
import com.ktw.fly.util.JsonUtils;
import com.ktw.fly.util.PermissionUtil;
import com.ktw.fly.util.PreferenceUtils;
import com.ktw.fly.util.SkinUtils;
import com.ktw.fly.util.TimeUtils;
import com.ktw.fly.util.ToastUtil;
import com.ktw.fly.util.UiUtils;
import com.ktw.fly.util.UpgradeManager;
import com.ktw.fly.util.download.AndroidDownloadManager;
import com.ktw.fly.util.download.AndroidDownloadManagerListener;
import com.ktw.fly.util.log.LogUtils;
import com.ktw.fly.view.PermissionExplainDialog;
import com.ktw.fly.view.SelectionFrame;
import com.ktw.fly.view.VerifyDialog;
import com.ktw.fly.view.cjt2325.cameralibrary.util.LogUtil;
import com.ktw.fly.wallet.bean.ApkUpdateBean;
import com.ktw.fly.wallet.bean.CurrencyBean;
import com.ktw.fly.wallet.bean.WalletListBean;
import com.ktw.fly.wallet.dapp.DappsFragment;
import com.ktw.fly.xmpp.CoreService;
import com.ktw.fly.xmpp.ListenerManager;
import com.ktw.fly.xmpp.listener.ChatMessageListener;
import com.xuan.xuanhttplibrary.okhttp.HttpUtils;
import com.xuan.xuanhttplibrary.okhttp.callback.BaseCallback;
import com.xuan.xuanhttplibrary.okhttp.callback.ListCallback;
import com.xuan.xuanhttplibrary.okhttp.result.ArrayResult;
import com.xuan.xuanhttplibrary.okhttp.result.ObjectResult;
import com.xuan.xuanhttplibrary.okhttp.result.Result;

import org.jsoup.helper.StringUtil;

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
 * 主界面
 */
public class FLYMainActivity extends BaseActivity implements PermissionUtil.OnRequestPermissionsResultCallbacks {
    // 小米推送
    public static final String APP_ID = BuildConfig.XIAOMI_APP_ID;
    public static final String APP_KEY = BuildConfig.XIAOMI_APP_KEY;
    // 是否重新走initView方法
    // 当切换语言、修改皮肤之后，将该状态置为true
    public static boolean isInitView = false;
    /**
     * 更新我的群组
     */
    Handler mHandler = new Handler();
    private UpdateUnReadReceiver mUpdateUnReadReceiver = null;
    private UserLogInOutReceiver mUserLogInOutReceiver = null;
    private TimeChangeReceiver timeChangeReceiver = null;
    private ActivityManager mActivityManager;
    // ╔═══════════════════════════════界面组件══════════════════════════════╗
    // ╚═══════════════════════════════界面组件══════════════════════════════╝
    private int mLastFragmentId;// 当前界面
    private RadioGroup mRadioGroup;
    private RadioButton
            mRbTab1 //社交
            , mWalletTab //钱包
            , mRbTab2 //好友
            , mRbFindTab//发现
            , mRbTab4 //我的

            , mRbNav1Tab //第一个导航
            , mRbNav2Tab; //第二个导航
    private FrameLayout flNav1Point //第一个导航的容器
            , flNav2Point //第二个导航的容器
            , flFindPoint; //发现导航的容器
    private TextView mTvMessageNum;// 显示消息界面未读数量
    private TextView mTvNewFriendNum;// 显示通讯录消息未读数量
    private TextView mTvCircleNum;// 显示朋友圈未读数量
    private int numMessage = 0;// 当前未读消息数量
    private int numCircle = 0; // 当前朋友圈未读数量
    private String mUserId;// 当前登陆的 UserID
    private My_BroadcastReceiver my_broadcastReceiver;
    private int mCurrtTabId;
    private boolean isCreate;
    /**
     * 在其他设备登录了，挤下线
     */
    private boolean isConflict;
    //图片
    public static final int AD_TYPE_IMAGE = 1;
    //视频
    public static final int AD_TYPE_VIDEO = 2;
    //下载引导图
    private AndroidDownloadManager androidDownloadManager;

    public FLYMainActivity() {
        noLoginRequired();
    }

    public static void start(Context ctx) {
        Intent intent = new Intent(ctx, FLYMainActivity.class);
        ctx.startActivity(intent);
    }

    /**
     * 发起二维码扫描，
     * 仅供MainActivity下属Fragment调用，
     */
    public static void requestQrCodeScan(Activity ctx) {
        Intent intent = new Intent(ctx, ScannerActivity.class);
        // 设置扫码框的宽
        intent.putExtra(Constant.EXTRA_SCANNER_FRAME_WIDTH, DisplayUtil.dip2px(ctx, 200));
        // 设置扫码框的高
        intent.putExtra(Constant.EXTRA_SCANNER_FRAME_HEIGHT, DisplayUtil.dip2px(ctx, 200));
        // 设置扫码框距顶部的位置
        intent.putExtra(Constant.EXTRA_SCANNER_FRAME_TOP_PADDING, DisplayUtil.dip2px(ctx, 100));
        // 可以从相册获取
        intent.putExtra(Constant.EXTRA_IS_ENABLE_SCAN_FROM_PIC, true);
        ctx.startActivityForResult(intent, 888);
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        // 自动解锁屏幕 | 锁屏也可显示 | Activity启动时点亮屏幕 | 保持屏幕常亮
/*
        getWindow().addFlags(
                WindowManager.LayoutParams.FLAG_DISMISS_KEYGUARD
                        | WindowManager.LayoutParams.FLAG_SHOW_WHEN_LOCKED
                        | WindowManager.LayoutParams.FLAG_TURN_SCREEN_ON
                        | WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON);
*/
        setContentView(R.layout.activity_main);
        // 启动保活
        if (PrivacySettingHelper.getPrivacySettings(this).getIsKeepalive() == 1) {
            initKeepLive();
        }
        initLog();

        mUserId = coreManager.getSelf().getUserId();
        initView();// 初始化控件
        initBroadcast();// 初始化广播
        initDatas();// 初始化一些数据

        // 初始化音视频Control
        AudioOrVideoController.init(mContext, coreManager);

        AsyncUtils.doAsync(this, mainActivityAsyncContext -> {
            // 获取app关闭之前还在上传的消息，将他们的发送状态置为失败
            List<UploadingFile> uploadingFiles = UploadingFileDao.getInstance().getAllUploadingFiles(coreManager.getSelf().getUserId());
            for (int i = uploadingFiles.size() - 1; i >= 0; i--) {
                ChatMessageDao.getInstance().updateMessageState(coreManager.getSelf().getUserId(), uploadingFiles.get(i).getToUserId(),
                        uploadingFiles.get(i).getMsgId(), ChatMessageListener.MESSAGE_SEND_FAILED);
            }
        });

//        UpdateManger.checkUpdate(this, coreManager.getConfig().androidAppUrl, coreManager.getConfig().androidVersion);

        EventBus.getDefault().post(new MessageLogin());
        // 设备锁，
        showDeviceLock();

        initMap();

        // 主页不要侧划返回，和ios统一，
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
            //判断是否已经开启通知栏
            SelectionFrame mSF = new SelectionFrame(this);
            mSF.setSomething(null, "发现新的版本，请及时更新！", new SelectionFrame.OnSelectionFrameClickListener() {
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
            // 皮肤深浅变化时需要改状态栏颜色，
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
//        // 主要针对侧滑返回，刷新消息会话列表，
//        MsgBroadcast.broadcastMsgUiUpdate(mContext);
//    }
    //    @Override
    protected void onRestart() {
        super.onRestart();
        // 主要针对侧滑返回，刷新消息会话列表，
        MsgBroadcast.broadcastMsgUiUpdate(mContext);

        if (NetUtil.isGprsOrWifiConnected(mContext)) {
            //每次重新进入，给系统通知号发送一条消息
            ChatMessage message = new ChatMessage();
            message.setType(XmppMessage.TYPE_TEXT);
            message.setContent("如果这条消息发送失败，就重新登录消息服务器");
            message.setType(XmppMessage.TYPE_REPLAY);
            message.setReSendCount(0);//不重发，如果这条消息发送失败，就重新登录消息服务器
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
                // 调用JCVideoPlayer.backPress()
                // true : 当前正在全屏播放视频
                // false: 当前未在全屏播放视频
                moveTaskToBack(true);
            }
            return true;
        }
        return super.onKeyDown(keyCode, event);
    }

    @Override
    protected void onDestroy() {
        // XMPP断开连接 必须调用disconnect 否则服务端不能立即检测出当前用户离线 导致推送延迟
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
        //启动保活服务
        KeepLive.startWork(getApplication(), KeepLive.RunMode.ENERGY,
                //你需要保活的服务，如socket连接、定时任务等，建议不用匿名内部类的方式在这里写
                new KeepLiveService() {
                    /**
                     * 运行中
                     * 由于服务可能会多次自动启动，该方法可能重复调用
                     */
                    @Override
                    public void onWorking() {
                        Log.e("xuan", "onWorking: ");
                    }

                    /**
                     * 服务终止
                     * 由于服务可能会被多次终止，该方法可能重复调用，需同onWorking配套使用，如注册和注销broadcast
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
        //  修改白屏bug
        mRbTab1.toggle();
//        mWalletTab.toggle();
        // initFragment();

        // 改皮肤，
        ColorStateList tabColor = SkinUtils.getSkin(this).getMainTabColorState();
        for (RadioButton radioButton : Arrays.asList(mRbTab1, mRbTab2, mWalletTab, mRbNav1Tab, mRbNav2Tab, mRbFindTab, mRbTab4)) {
            // 图标着色，兼容性解决方案，
            Drawable drawable = radioButton.getCompoundDrawables()[1];
            drawable = DrawableCompat.wrap(drawable);
            DrawableCompat.setTintList(drawable, tabColor);
            // 如果是getDrawable拿到的Drawable不能直接调setCompoundDrawables，没有宽高，
            radioButton.setCompoundDrawablesWithIntrinsicBounds(null, drawable, null, null);
            radioButton.setTextColor(tabColor);
        }

        // 检查是否开启通知栏权限
        checkNotifyStatus();
    }

    private void initBroadcast() {
        EventBus.getDefault().register(this);

        // 注册未读消息更新广播
        IntentFilter filter = new IntentFilter();
        filter.addAction(MsgBroadcast.ACTION_MSG_NUM_UPDATE);
        filter.addAction(MsgBroadcast.ACTION_MSG_NUM_UPDATE_NEW_FRIEND);
        filter.addAction(MsgBroadcast.ACTION_MSG_NUM_RESET);
        mUpdateUnReadReceiver = new UpdateUnReadReceiver(this);
        registerReceiver(mUpdateUnReadReceiver, filter);

        // 注册用户登录状态广播
        mUserLogInOutReceiver = new UserLogInOutReceiver(this);
        registerReceiver(mUserLogInOutReceiver, LoginHelper.getLogInOutActionFilter());

        // 刷新评论的广播和 关闭主界面的，用于切换语言，更改皮肤用
        filter = new IntentFilter();
        // 当存在阅后即焚文字类型的消息时，当计时器计时结束但聊天界面已经销毁时(即聊天界面收不到该广播，消息也不会销毁)，代替销毁
        filter.addAction(Constants.UPDATE_ROOM);
        filter.addAction(com.ktw.fly.broadcast.OtherBroadcast.SYNC_CLEAN_CHAT_HISTORY);
        filter.addAction(com.ktw.fly.broadcast.OtherBroadcast.SYNC_SELF_DATE);
        filter.addAction(com.ktw.fly.broadcast.OtherBroadcast.CollectionRefresh);
        filter.addAction(com.ktw.fly.broadcast.OtherBroadcast.SEND_MULTI_NOTIFY);  // 群发消息结束
        my_broadcastReceiver = new My_BroadcastReceiver();
        registerReceiver(my_broadcastReceiver, filter);

        // 监听系统时间设置，
        filter = new IntentFilter();
        filter.addAction(Intent.ACTION_TIME_CHANGED);
        filter.addAction(Intent.ACTION_DATE_CHANGED);
        filter.addAction(Intent.ACTION_TIMEZONE_CHANGED);
        timeChangeReceiver = new TimeChangeReceiver(this);
        registerReceiver(timeChangeReceiver, filter);
    }

    private void initDatas() {
        // 检查用户的状态，做不同的初始化工作
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
            // 进入主页后调的接口，都在刷新accessToken后再调用，
            loginRequired();
            initCore();
            CoreManager.initLocalCollectionEmoji();
            CoreManager.updateMyBalance();
            initOther();// 初始化第三方
            checkTime();
            // 上传本地通讯录
            if ((coreManager.getConfig().isSupportAddress
                    && !coreManager.getConfig().registerUsername)) {
                addressBookOperation();
            }
            login();
            updateSelfData();

            //获取导航栏布局
            getNavData();

            initEmotData();

            getAd();

            getAutoAnswerList();

        });

        mUserId = loginUser.getUserId();
        FriendDao.getInstance().checkSystemFriend(mUserId); // 检查 两个公众号

        // 更新所有未读的信息
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
                                LogUtil.d("获取广告视频地址：" + navBean.getImgUrl());
                                //判断上次是否已经获取过相同的广告文件
                                String upUrl = PreferenceUtils.getString(FLYApplication.getInstance(), Constants.KEY_SPLASH_AD_URL);
                                if (!navBean.getImgUrl().equalsIgnoreCase(upUrl)) {
                                    //下载新的广告资源
                                    new Thread(new Runnable() {
                                        @Override
                                        public void run() {
                                            LogUtil.d("当前线程为：" + (Looper.getMainLooper() == Looper.myLooper() ? 1 : 0));
                                            if (AndroidDownloadManager.getFileSuffixByUrl(navBean.getImgUrl()).equalsIgnoreCase(".mp4")) {
                                                //视频广告
                                                downloadAdSource(AD_TYPE_VIDEO, navBean);
                                            } else {
                                                //图片广告
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
     * 导航的接口，没有链接时，只显示发现，1个连接时，显示发现和1个链接，2个链接时，不显示发现，只显示两个链接
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
            // 有开启设备锁，
            DeviceLockActivity.start(this);
        } else {
            Log.e("DeviceLock", "没开启设备锁，不弹出设备锁");
        }
    }

    private void initMap() {
        // 中国大陆只能使用百度，
        // 墙外且有谷歌框架才能使用谷歌地图，
        String area = PreferenceUtils.getString(this, FLYAppConstant.EXTRA_CLUSTER_AREA);
        if (TextUtils.equals(area, "CN")) {
            MapHelper.setMapType(MapHelper.MapType.BAIDU);
        } else {
            MapHelper.setMapType(MapHelper.MapType.GOOGLE);
        }
    }

    /**
     * 切换Fragment
     */
    private void changeFragment(int checkedId) {
        if (mLastFragmentId == checkedId) {
            return;
        }

        FragmentTransaction transaction = getSupportFragmentManager().beginTransaction();

        Fragment fragment = getSupportFragmentManager().findFragmentByTag(String.valueOf(checkedId));
        if (fragment == null) {
            switch (checkedId) {
                case R.id.rb_tab_wallet:
                    //钱包
                    fragment = new WalletFragment();
                    break;
                case R.id.rb_tab_1:
                    //社交
                    fragment = new MessageFragment();
                    break;
                case R.id.rb_tab_2:
                    //通讯录
                    fragment = new FriendFragment();
                    break;
                case R.id.rb_tab_3:
                    //发现页 替换为dapp
//                    if (coreManager.getConfig().newUi) { // 切换新旧两种ui对应不同的发现页面，
//                        fragment = new SquareFragment();
//                    } else {
//                        fragment = new DiscoverFragment();
//                    }
//                    fragment = new FindFragment();
                    fragment = new DappsFragment();
                    break;
                case R.id.rb_tab_4:
                    fragment = new MeFragment();
                    break;
                case R.id.rb_tab_nav1:
                    fragment = new Nav1Fragment();
                    break;
                case R.id.rb_tab_nav2:
                    fragment = new Nav2Fragment();
                    break;
            }
        }

        // fragment = null;
        assert fragment != null;

        if (!fragment.isAdded()) {// 未添加 add
            transaction.add(R.id.main_content, fragment, String.valueOf(checkedId));
        }

        Fragment lastFragment = getSupportFragmentManager().findFragmentByTag(String.valueOf(mLastFragmentId));

        if (lastFragment != null) {
            transaction.hide(lastFragment);
        }
        // 以防万一出现last和current都是同一个fragment的情况，先hide再show,
        transaction.show(fragment);

        // transaction.setTransition(FragmentTransaction.TRANSIT_FRAGMENT_OPEN);// 添加动画
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
     * OPPO手机：App的通知默认是关闭的，需要检查通知是否开启
     * OPPO手机：App后台时，调用StartActivity方法不起做用，需提示用户至 手机管家-权限隐私-自启动管理 内该App的自启动开启
     * <p>
     * 小米与魅族手机需要开启锁屏显示权限，否则在锁屏时收到音视频消息来电界面无法弹起（其他手机待测试，华为手机无该权限设置，锁屏时弹起后直接干掉弹起页面）
     */
    private void checkNotifyStatus() {
        int launchCount = PreferenceUtils.getInt(this, Constants.APP_LAUNCH_COUNT, 0);// 记录app启动的次数
        Log.e("zq", "启动app的次数:" + launchCount);
        if (launchCount == 1) {
            String tip = "";
            if (!AppUtils.isNotificationEnabled(this)) {
                tip = getString(R.string.title_notification) + "\n" + getString(R.string.content_notification);
            }
            if (DeviceInfoUtil.isOppoRom()) {// 如果Rom为OPPO，还需要提醒用户开启自启动
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

        // 服务器端是根据最后调用的上传推送ID接口决定使用什么推送，
        // 也就是在这里最后初始化哪个推送就会用哪个推送，

        //noinspection ConstantConditions
        AsyncUtils.doAsync(this, t -> {
            FLYReporter.post("初始化推送失败", t);
        }, mainActivityAsyncContext -> {
//            if (coreManager.getConfig().enableGoogleFcm && googleAvailable()) {
//                if (HttpUtil.testGoogle()) {// 拥有谷歌服务且能翻墙 使用谷歌推送
//                    FirebaseMessageService.init(MainActivity.this);
//                } else {// 虽然手机内有谷歌服务，但是不能翻墙，还是根据机型判断使用哪种推送
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
                // devicesId后端没有用上，但是沿用旧接口的参数列表带上这个，实际没用，
                .params("deviceId", "4")
                .build()
                .execute(new BaseCallback<Void>(Void.class) {
                    @Override
                    public void onResponse(ObjectResult<Void> result) {

                    }

                    @Override
                    public void onError(Call call, Exception e) {
                        FLYReporter.post("上传JPUSH推送Id失败，", e);
                    }

                });
    }

    private boolean googleAvailable() {
        boolean isGoogleAvailability = true;
        GoogleApiAvailability googleApiAvailability = GoogleApiAvailability.getInstance();
        int resultCode = googleApiAvailability.isGooglePlayServicesAvailable(this);
        if (resultCode != ConnectionResult.SUCCESS) {
            // 存在谷歌框架但是不可用，
            // 官方做法弹个对话框提示，
            // if (googleApiAvailability.isUserResolvableError(resultCode)) {
            //     googleApiAvailability.getErrorDialog(this, resultCode, 2404).show();
            // }
            // 当成没有谷歌框架处理，
            isGoogleAvailability = false;
        }
        return isGoogleAvailability;
    }

    @SuppressWarnings({"PointlessBooleanExpression", "ConstantConditions"})
    private void selectPush() {
        // 判断Rom使用推送
//        if (DeviceInfoUtil.isEmuiRom()) {
//            Log.e(TAG, "初始化推送: 华为推送，");
//            // 华为手机 华为推送
//            HuaweiClient client = new HuaweiClient(this);
//            client.clientConnect();
//        } else if (DeviceInfoUtil.isMeizuRom()) {
//            Log.e(TAG, "初始化推送: 魅族推送，");
//            MeizuPushMsgReceiver.init(this);
//        } else if (PushManager.isSupportPush(this)) {
//            Log.e(TAG, "初始化推送: OPPO推送，");
//            OppoPushMessageService.init(this);
//        } else if (DeviceInfoUtil.isVivoRom()) {
//            Log.e(TAG, "初始化推送: VIVO推送，");
//            VivoPushMessageReceiver.init(this);
//        } else if (true || DeviceInfoUtil.isMiuiRom()) {
//            Log.e(TAG, "初始化推送: 小米推送，");
//            if (shouldInit()) {
//                // 小米推送初始化
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
                        // 误差比config接口大，可能是主页线程做其他操作导致的，
                        // 和ios统一，进入主页时校准时间，
                        TimeUtils.responseTime(result.getCurrentTime());
                    }

                    @Override
                    public void onError(Call call, Exception e) {
                        // 不需要提示，
                        Log.e("TimeUtils", "校准时间失败", e);
                    }
                });
    }

    public void cancelUserCheckIfExist() {
        Log.d(TAG, "cancelUserCheckIfExist() called");
    }

    /* 当注销当前用户时，将那些需要当前用户的Fragment销毁，以后重新登陆后，重新加载为初始状态 */
    public void removeNeedUserFragment() {
        mRadioGroup.clearCheck();
        mLastFragmentId = -1;
        isCreate = true;
    }

    /**
     * 登录方法
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
        isConflict = true;// 标记一下

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
        // 弹出对话框
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

    // 更新发现模块新消息数量
    @Subscribe(threadMode = ThreadMode.MainThread)
    public void helloEventBus(MessageEventHongdian message) {
        numCircle = message.number;
        UiUtils.updateNum(mTvCircleNum, numCircle);
    }

    // 已上传的联系人注册了IM,更新到联系人表内
    @Subscribe(threadMode = ThreadMode.MainThread)
    public void helloEventBus(MessageContactEvent mMessageEvent) {
        List<Contact> mNewContactList = ContactDao.getInstance().getContactsByToUserId(coreManager.getSelf().getUserId(),
                mMessageEvent.message);
        if (mNewContactList != null && mNewContactList.size() > 0) {
            updateContactUI(mNewContactList);
        }
    }

    /**
     * 我方取消、挂断通话后发送XMPP消息给对方
     * copy by AudioOrVideoController
     */
    @Subscribe(threadMode = ThreadMode.MainThread)
    public void helloEventBus(final MessageEventCancelOrHangUp event) {
        String mLoginUserId = coreManager.getSelf().getUserId();
        ChatMessage message = new ChatMessage();
        if (event.type == 103) {          // 取消 语音通话
            message.setType(XmppMessage.TYPE_NO_CONNECT_VOICE);
        } else if (event.type == 104) {// 取消 视频通话
            message.setType(XmppMessage.TYPE_END_CONNECT_VOICE);
        } else if (event.type == 113) {// 挂断 语音通话
            message.setType(XmppMessage.TYPE_NO_CONNECT_VIDEO);
        } else if (event.type == 114) {// 挂断 视频通话
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
        MsgBroadcast.broadcastMsgUiUpdate(mContext);   // 更新消息界面
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
            // 音视频会议消息不保存
/*
            ChatMessageDao.getInstance().saveNewSingleChatMessage(mLoginUserId, message.list.get(i), mMeetingMessage);
            FriendDao.getInstance().updateFriendContent(mLoginUserId, message.list.get(i), str, type, TimeUtils.sk_time_current_time());
*/
        }
    }

    /*
    扫描二维码 || 全部群组内 加入群组时群主开启了群验证 发送入群请求给群主
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
        if (mMessageEventBG.flag) {// 切换到前台
            // 设备锁，
            showDeviceLock();
            // 清除通知栏消息
            NotificationManager mNotificationManager = (NotificationManager) this.getSystemService(Context.NOTIFICATION_SERVICE);
            if (mNotificationManager != null) {
                mNotificationManager.cancelAll();
            }

            if (isConflict) {// 在其他设备登录了，不登录
                isConflict = false;// Reset Status
                Log.e("zq", "在其他设备登录了，不登录");
                return;
            }

            if (!coreManager.isServiceReady()) {
                // 小米手机在后台运行时，CoreService经常被系统杀死，需要兼容ta
                Log.e("zq", "CoreService为空，重新绑定");
                coreManager.relogin();
            } else {
                if (!coreManager.isLogin()) {// XMPP未验证
                    Log.e("zq", "XMPP未验证，重新登录");
                    coreManager.autoReconnect(FLYMainActivity.this);
                }
            }
        } else {
            MachineDao.getInstance().resetMachineStatus();

            FLYApplication.getInstance().appBackstage(false);
        }
    }

    /*
    扫描二维码 || 全部群组内 加入群组 将群组存入朋友表
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
        friend.setChatRecordTimeOut(room.getChatRecordTimeOut());// 消息保存天数 -1/0 永久
        friend.setContent(mLoginUserName + " " + getString(R.string.Message_Object_Group_Chat));
        friend.setTimeSend(TimeUtils.sk_time_current_time());
        friend.setRoomFlag(1);
        friend.setStatus(Friend.STATUS_FRIEND);
        FriendDao.getInstance().createOrUpdateFriend(friend);

        // 调用socket加入群组的方法
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
     * 关闭软键盘
     */
    public void hideInput() {
        InputMethodManager imm = (InputMethodManager) getSystemService(Context.INPUT_METHOD_SERVICE);
        IBinder token = getWindow().getDecorView().getWindowToken();
        if (imm != null && imm.isActive() && token != null) {
            imm.hideSoftInputFromWindow(token, InputMethodManager.HIDE_NOT_ALWAYS);
        }
    }

    /**
     * 手机联系人相关操作
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
        if (isAllGranted) {// 已授权
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
                    Log.e("zq", "二维码扫描结果：" + result);
                    if (TextUtils.isEmpty(result)) {
                        return;
                    }
                    if (PaymentReceiptMoneyActivity.checkQrCode(result)) {
                        // 长度为19且 && 纯数字 扫描他人的付款码 弹起收款界面
                        Intent intent = new Intent(mContext, PaymentReceiptMoneyActivity.class);
                        intent.putExtra("PAYMENT_ORDER", result);
                        startActivity(intent);
                    } else if (result.contains("userId")
                            && result.contains("userName")) {
                        // 扫描他人的收款码 弹起付款界面
                        Intent intent = new Intent(mContext, ReceiptPayMoneyActivity.class);
                        intent.putExtra("RECEIPT_ORDER", result);
                        startActivity(intent);
                    } else if (ReceiveChatHistoryActivity.checkQrCode(result)) {
                        // 扫描他人的发送聊天记录的二维码，弹起接收聊天记录页面，
                        ReceiveChatHistoryActivity.start(this, result);
                    } else if (WebLoginActivity.checkQrCode(result)) {
                        // 扫描其他平台登录的二维码，确认登录页面，
                        WebLoginActivity.start(this, result);
                    } else {
                        if (result.contains("shikuId")) {
                            // 二维码
                            Map<String, String> map = WebViewActivity.URLRequest(result);
                            String action = map.get("action");
                            String userId = map.get("shikuId");
                            if (TextUtils.equals(action, "group")) {
                                getRoomInfo(userId);
                            } else if (TextUtils.equals(action, "user")) {
                                getUserInfo(userId);
                            } else {
                                FLYReporter.post("二维码无法识别，<" + result + ">");
                                ToastUtil.showToast(this, R.string.unrecognized);
                            }
                        } else if (!result.contains("shikuId")
                                && HttpUtil.isURL(result)) {
                            // 非二维码  访问其网页
                            Intent intent = new Intent(this, WebViewActivity.class);
                            intent.putExtra(WebViewActivity.EXTRA_URL, result);
                            startActivity(intent);
                        } else {
                            FLYReporter.post("二维码无法识别，<" + result + ">");
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
     * 通过通讯号获得userId
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
     * 获取房间信息
     */
    private void getRoomInfo(String roomId) {
        Friend friend = FriendDao.getInstance().getMucFriendByRoomId(coreManager.getSelf().getUserId(), roomId);
        if (friend != null) {
            if (friend.getGroupStatus() == 0) {
                interMucChat(friend.getUserId(), friend.getNickName());
                return;
            } else {// 已被踢出该群组 || 群组已被解散 || 群组已被后台锁定
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
     * 加入房间
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
                                public void run() {// 给500ms的时间缓存，防止群组还未创建好就进入群聊天界面
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
     * 进入房间
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
         * 本地生成
         * [{"name":"15768779999","telephone":"8615768779999"},{"name":"好搜卡","telephone":"8615720966659"},
         * {"name":"zas","telephone":"8613000000000"},{"name":"客服助手","telephone":"864007883333"},]
         * 服务端要求
         * [{\"toTelephone\":\"15217009762\",\"toRemarkName\":\"我是电话号码备注\"},{\"toTelephone\":\"15217009762\",\"toRemarkName\":\"我是电话号码备注\"}]
         */
        if (mNewAdditionContacts.size() <= 0) {
            return;
        }

        String step1 = JSON.toJSONString(mNewAdditionContacts);
        String step2 = step1.replaceAll("name", "toRemarkName");
        String contactsListStr = step2.replaceAll("telephone", "toTelephone");
        Log.e("contact", "新添加的联系人：" + contactsListStr);

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
                                    if (contact.getStatus() == 1) {// 服务端自动成为好友，本地也需要添加
                                        NewFriendDao.getInstance().addFriendOperating(contact.getToUserId(), contact.getToUserName(), contact.getToRemarkName());
                                    }
                                }
                            }

                            if (mContactList.size() > 0) {// 显示数量新增数量  记录新增contacts id
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
        params.put("pageSize", "1000");// 给一个尽量大的值

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
    消息 发现
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
    通讯录
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

    // 服务器上与该人的聊天记录也需要删除
    private void emptyServerMessage(String friendId) {
        Map<String, String> params = new HashMap<>();
        params.put("access_token", coreManager.getSelfStatus().accessToken);
        params.put("type", String.valueOf(0));// 0 清空单人 1 清空所有
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
                            // 设置登陆用户信息
                            if (updateSuccess) {
                                // 如果成功，保存User变量，
                                coreManager.setSelf(user);
                                // 通知MeFragment更新
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
                            // 发送广播通知更新
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
                sendBroadcast(new Intent(Constants.CHAT_HISTORY_EMPTY));// 清空聊天界面
                MsgBroadcast.broadcastMsgUiUpdate(mContext);
            } else if (action.equals(OtherBroadcast.SYNC_SELF_DATE)) {
                updateSelfData();
            } else if (action.equals(OtherBroadcast.CollectionRefresh)) {
                notifyCollectionList();
            } else if (action.equals(OtherBroadcast.SEND_MULTI_NOTIFY)) {
                mRbTab4.setChecked(false);
//                mRbTab1.setChecked(true);
                mRbTab1.setChecked(false);
                mWalletTab.setChecked(true);
            }
        }
    }

    public void downloadAdSource(int sourceType, NavBean navBean) {
        androidDownloadManager = new AndroidDownloadManager(FLYApplication.getInstance(), navBean.getImgUrl())
                .setListener(new AndroidDownloadManagerListener() {
                    @Override
                    public void onPrepare() {
                        LogUtil.d("下载中……");
                    }

                    @Override
                    public void onSuccess(String path) {
                        PreferenceUtils.putInt(FLYApplication.getInstance(), Constants.KEY_SPLASH_AD_TYPE, sourceType);
                        PreferenceUtils.putString(FLYApplication.getInstance(), Constants.KEY_SPLASH_AD_PATH, path);
                        PreferenceUtils.putString(FLYApplication.getInstance(), Constants.KEY_SPLASH_AD_URL, navBean.getImgUrl());
                        PreferenceUtils.putString(FLYApplication.getInstance(), Constants.KEY_SPLASH_AD_TITLE, navBean.getDesc());
                        PreferenceUtils.putString(FLYApplication.getInstance(), Constants.KEY_SPLASH_AD_WEBSITE, navBean.getLink());
                        LogUtil.d("已下载成功，存入本地路径 = " + path);
                    }

                    @Override
                    public void onFailed(Throwable throwable) {
                        LogUtil.d("下载失败 = " + throwable.getMessage());
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
                            Log.e("zq", "获取网络配置失败，使用已经保存了的配置");
                            if (BuildConfig.DEBUG) {
                                ToastUtil.showToast(FLYMainActivity.this, R.string.tip_get_config_failed);
                            }
                            // 获取网络配置失败，使用已经保存了的配置，
                            configBean = coreManager.readConfigBean();
                        } else {
                            Log.e("zq", "获取网络配置成功，使用服务端返回的配置并更新本地配置");
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
                        Log.e("zq", "获取网络配置失败，使用已经保存了的配置");
                        // ToastUtil.showToast(SplashActivity.this, R.string.tip_get_config_failed);
                        // 获取网络配置失败，使用已经保存了的配置，
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
            // 如果没有保存配置，也就是第一次使用，就连不上服务器，使用默认配置
            configBean = CoreManager.getDefaultConfig(this);
            coreManager.saveConfigBean(configBean);
        }
        //检查版本
        checkUpdate();

    }

    /**
     * 获取机器人问题列表(方便初始化消息列表，这里先调用一次)
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
                            //S:强更   F
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

}