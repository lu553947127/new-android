package com.ktw.fly;

import android.app.Activity;
import android.app.Application;
import android.content.Context;
import android.graphics.Bitmap;
import android.os.Build;
import android.os.Bundle;
import android.os.Environment;
import android.os.Looper;
import android.os.StrictMode;
import android.util.Log;
import android.util.LruCache;

import androidx.appcompat.app.AppCompatDelegate;
import androidx.multidex.MultiDex;

import com.baidu.mapapi.SDKInitializer;
import com.danikula.videocache.HttpProxyCacheServer;
import com.google.gson.Gson;
import com.ktw.fly.bean.AutoAnswerBean;
import com.ktw.fly.bean.PrivacySetting;
import com.ktw.fly.bean.collection.Collectiion;
import com.ktw.fly.bean.event.MessageEventBG;
import com.ktw.fly.db.SQLiteHelper;
import com.ktw.fly.helper.PrivacySettingHelper;
import com.ktw.fly.map.MapHelper;
import com.ktw.fly.socket.EMConnectionManager;
import com.ktw.fly.ui.base.CoreManager;
import com.ktw.fly.ui.me.emot.MyEmotBean;
import com.ktw.fly.ui.tool.MyFileNameGenerator;
import com.ktw.fly.util.AppUtils;
import com.ktw.fly.util.AsyncUtils;
import com.ktw.fly.util.Constants;
import com.ktw.fly.util.LocaleHelper;
import com.ktw.fly.util.PreferenceUtils;
import com.ktw.fly.util.ScreenShotListenManager;
import com.wanjian.cockroach.Cockroach;
import com.wanjian.cockroach.ExceptionHandler;

import java.io.File;
import java.lang.reflect.Field;
import java.lang.reflect.Method;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import cn.jpush.android.api.JPushInterface;
import de.greenrobot.event.EventBus;

public class FLYApplication extends Application {
    public static final String TAG = BuildConfig.APPLICATION_ID;
    public static boolean IS_OPEN_CLUSTER = false;// 服务器是否开启集群 如开启，在登录、自动登录时需要传area，在发起音视频通话(单人)时会要调接口获取通话地址
    public static boolean IS_OPEN_RECEIPT = true;
    // 是否支持多端登录
    public static boolean IS_SUPPORT_MULTI_LOGIN;

    public static String registrationID; //JPush

    public static String IsRingId = "Empty";// 当前聊天对象的id/jid 用于控制消息来时是否响铃通知
    public static String mRoomKeyLastCreate = "compatible";// 本地建群时的jid(给个初始值坐下兼容) 用于防止收到服务端的907消息时本地也在建群而造成群组重复
    public static List<Collectiion> mCollection = new ArrayList<>();
    private static FLYApplication INSTANCE = null;
    private static Context context;
    /* 文件缓存的目录 */
    public String mAppDir01;
    public String mPicturesDir01;
    public String mVoicesDir01;
    public String mVideosDir01;
    public String mFilesDir01;
    public int mActivityCount = 0;
    /* 文件缓存的目录 */
    public String mAppDir;
    public String mPicturesDir;
    public String mVoicesDir;
    public String mVideosDir;
    public String mFilesDir;
    public int mUserStatus;
    public boolean mUserStatusChecked = true;
    /*********************
     * 百度地图定位服务
     ************************/
    private FLYBdLocationHelper mFLYBdLocationHelper;
    private LruCache<String, Bitmap> mMemoryCache;
    // 抖音模块缓存
    private HttpProxyCacheServer proxy;
    //添加的表情包
    public static Map<String, List<String>> emotMap = new HashMap<>();
    //添加的自定义表情
    public static List<MyEmotBean> singleEmotList = new ArrayList<>();
    //机器人问题集合
    public static AutoAnswerBean autoAnswerBean = new AutoAnswerBean();

    public static FLYApplication getInstance() {
        return INSTANCE;
    }

    public static Context getContext() {
        return context;
    }

    public static HttpProxyCacheServer getProxy(Context context) {
        FLYApplication app = (FLYApplication) context.getApplicationContext();
        return app.proxy == null ? (app.proxy = app.newProxy()) : app.proxy;
    }

    @Override
    protected void attachBaseContext(Context base) {
        super.attachBaseContext(base);
        MultiDex.install(this);
    }

    @Override
    public void onCreate() {
        super.onCreate();
        INSTANCE = this;
        context = getApplicationContext();

        installCockroach();

        SDKInitializer.initialize(getApplicationContext());
        if (FLYAppConfig.DEBUG) {
            Log.d(FLYAppConfig.TAG, "MyApplication onCreate");
        }

        initMulti();

        // 在7.0的设备上，不开启该模式访问相机或裁剪居然会抛出FileUriExposedException异常，记录一下
        StrictMode.setThreadPolicy(new StrictMode.ThreadPolicy.Builder().detectAll().penaltyLog().build());
        StrictMode.setVmPolicy(new StrictMode.VmPolicy.Builder().detectAll().penaltyLog().build());

        // 初始化数据库
        SQLiteHelper.copyDatabaseFile(this);
        // 初始化定位
        getBdLocationHelper();
        // 初始化App目录
        initAppDir();
        initAppDirsecond();
        // 初始化图片加载 缓存
        initLruCache();

        // 判断前后台切换
        getAppBackground();
        // 监听屏幕截图
        ListeningScreenshots();

        int launchCount = PreferenceUtils.getInt(this, Constants.APP_LAUNCH_COUNT, 0);// 记录app启动的次数
        PreferenceUtils.putInt(this, Constants.APP_LAUNCH_COUNT, ++launchCount);

        initMap();

        initLanguage();

        initReporter();

        disableAPIDialog();

        disableWatchdog();

        initJpush();

//        initDefaultAnswer();
    }

    private void initDefaultAnswer() {
        String jsonStr = "{\n" +
                "    \"answer\": \"----漫游IM---\\n欢迎来到漫游im\\n 您有什么想问的呢：\\n\\n    【1】  新用户如何建群？\\n\\n    【2】  提现需要多久能到账？\\n    【3】  充值需要多久可以到账？\\n    【4】  可以多端登陆吗？\\n\\n----漫游IM----\\n请不要相信任何刷单兼职信息\\n\",\n" +
                "    \"endAnswer\": \"----漫游IM---\\n欢迎来到漫游im\\n 您有什么想问的呢：\\n\",\n" +
                "    \"startAnswer\": \"----漫游IM---\\n欢迎来到漫游im\\n 您有什么想问的呢：\\n\",\n" +
                "    \"autoAnswerList\": [\n" +
                "        {\n" +
                "            \"answer\": \"点击消息界面左上角+号创建\",\n" +
                "            \"id\": \"5fffedf7a017f167b994842a\",\n" +
                "            \"issue\": \"新用户如何建群？\\n\",\n" +
                "            \"sort\": \"1\",\n" +
                "            \"updateTime\": 1611502785\n" +
                "        },\n" +
                "        {\n" +
                "            \"answer\": \"正常一分钟内可以到账\",\n" +
                "            \"id\": \"5fffee40a017f167b994842b\",\n" +
                "            \"issue\": \"提现需要多久能到账？\",\n" +
                "            \"sort\": \"2\",\n" +
                "            \"updateTime\": 1611502830\n" +
                "        },\n" +
                "        {\n" +
                "            \"answer\": \"充值都是秒到账\",\n" +
                "            \"id\": \"5fffee72a017f167b994842d\",\n" +
                "            \"issue\": \"充值需要多久可以到账？\",\n" +
                "            \"sort\": \"3\",\n" +
                "            \"updateTime\": 1611502852\n" +
                "        },\n" +
                "        {\n" +
                "            \"answer\": \"安卓和苹果可以同时登陆\",\n" +
                "            \"id\": \"60028c16a017f15eb21788a8\",\n" +
                "            \"issue\": \"可以多端登陆吗？\",\n" +
                "            \"sort\": \"4\",\n" +
                "            \"updateTime\": 1611502893\n" +
                "        }\n" +
                "    ]\n" +
                "}";
        autoAnswerBean = new Gson().fromJson(jsonStr, AutoAnswerBean.class);

    }

    private void initJpush() {
        //极光推送 设置开启日志,发布时请关闭日志（该接口需在init接口之前调用，避免出现部分日志没打印的情况）
        JPushInterface.setDebugMode(true);
        //初始化推送服务
        if (INSTANCE != null) {
            JPushInterface.init(INSTANCE);
            registrationID = JPushInterface.getRegistrationID(INSTANCE);
            Log.e("JPush", "JPush registrationID=" + registrationID);
        }
    }


    private void installCockroach() {
        final Thread.UncaughtExceptionHandler sysExcepHandler = Thread.getDefaultUncaughtExceptionHandler();
        Cockroach.install(this, new ExceptionHandler() {
            @Override
            protected void onUncaughtExceptionHappened(Thread thread, Throwable throwable) {
                Log.e("AndroidRuntime", "--->onUncaughtExceptionHappened:" + thread + "<---", throwable);
            }

            @Override
            protected void onBandageExceptionHappened(Throwable throwable) {
                throwable.printStackTrace();//打印警告级别log，该throwable可能是最开始的bug导致的，无需关心
            }

            @Override
            protected void onEnterSafeMode() {
            }

            @Override
            protected void onMayBeBlackScreen(Throwable e) {
                Thread thread = Looper.getMainLooper().getThread();
                Log.e("AndroidRuntime", "--->onUncaughtExceptionHappened:" + thread + "<---", e);
                //黑屏时建议直接杀死app
                sysExcepHandler.uncaughtException(thread, new RuntimeException("black screen"));
            }
        });
    }

    /**
     * 有个watchdog负责监控垃圾对象回收，
     * 在oppo上总是超时导致崩溃，直接禁用，
     * https://www.jianshu.com/p/89e2719be9c7
     */
    @SuppressWarnings({"unchecked", "ConstantConditions"})
    private void disableWatchdog() {
        try {
            Class clazz = Class.forName("java.lang.Daemons$FinalizerWatchdogDaemon");
            Method method = clazz.getSuperclass().getDeclaredMethod("stop");
            method.setAccessible(true);
            Field field = clazz.getDeclaredField("INSTANCE");
            field.setAccessible(true);
            method.invoke(field.get(null));
        } catch (Throwable e) {
            e.printStackTrace();
        }
    }

    /**
     * 安卓9以上的hide api警告对话框，
     * 各种库大量使用hide api, 无法都解决掉，
     * 用hide api解决hide api警告，感觉算漏洞，以后可能失效，
     * <p>
     * 反射 禁止弹窗
     */
    @SuppressWarnings("all")
    private void disableAPIDialog() {
        if (Build.VERSION.SDK_INT < 28) return;
        try {
            Class clazz = Class.forName("android.app.ActivityThread");
            Method currentActivityThread = clazz.getDeclaredMethod("currentActivityThread");
            currentActivityThread.setAccessible(true);
            Object activityThread = currentActivityThread.invoke(null);
            Field mHiddenApiWarningShown = clazz.getDeclaredField("mHiddenApiWarningShown");
            mHiddenApiWarningShown.setAccessible(true);
            mHiddenApiWarningShown.setBoolean(activityThread, true);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    private void initReporter() {
        FLYReporter.init(this);
    }

    private void initLanguage() {
        // 应用程序里设置的语言，否则程序杀死后重启又会是系统语言，
        LocaleHelper.setLocale(this, LocaleHelper.getLanguage(this));
    }

    private void initMap() {
        MapHelper.initContext(this);
        // 默认为百度地图，
        PrivacySetting privacySetting = PrivacySettingHelper.getPrivacySettings(this);
        boolean isGoogleMap = privacySetting.getIsUseGoogleMap() == 1;
        if (isGoogleMap) {
            MapHelper.setMapType(MapHelper.MapType.GOOGLE);
        } else {
            MapHelper.setMapType(MapHelper.MapType.BAIDU);
        }
    }

    private void ListeningScreenshots() {
        ScreenShotListenManager manager = ScreenShotListenManager.newInstance(this);
        manager.setListener(new ScreenShotListenManager.OnScreenShotListener() {
            @Override
            public void onShot(String imagePath) {
                PreferenceUtils.putString(getApplicationContext(), Constants.SCREEN_SHOTS, imagePath);
            }
        });
        manager.startListen();
    }

    private void getAppBackground() {
        registerActivityLifecycleCallbacks(new ActivityLifecycleCallbacks() {

            @Override
            public void onActivityCreated(Activity activity, Bundle savedInstanceState) {

            }

            @Override
            public void onActivityStarted(Activity activity) {
                if (mActivityCount == 0) {
                    Log.e(TAG, "程序已到前台,检查XMPP是否验证");
                    EventBus.getDefault().post(new MessageEventBG(true));
                }
                mActivityCount++;
                Log.e(TAG, "onActivityStarted-->" + mActivityCount);
            }

            @Override
            public void onActivityResumed(Activity activity) {

            }

            @Override
            public void onActivityPaused(Activity activity) {

            }

            @Override
            public void onActivityStopped(Activity activity) {
                mActivityCount--;
                Log.e(TAG, "onActivityStopped-->" + mActivityCount);
                if (!AppUtils.isAppForeground(getContext())) {// 在app启动时，当启动页stop，而MainActivity还未start时，又会回调到该方法内，所以需要判断到底是不是真的处于后台
                    appBackstage(true);
                }
            }

            @Override
            public void onActivitySaveInstanceState(Activity activity, Bundle outState) {

            }

            @Override
            public void onActivityDestroyed(Activity activity) {

            }
        });
    }

    public void appBackstage(boolean isBack) {
        AsyncUtils.doAsync(this, c -> {
            CoreManager.appBackstage(getApplicationContext(), isBack);
        });
    }

    public void initMulti() {
        // 只能在登录的时候修改，所以不能放到 setPrivacySettings 内
        PrivacySetting privacySetting = PrivacySettingHelper.getPrivacySettings(this);
        boolean isSupport = privacySetting.getMultipleDevices() == 1;
        if (isSupport) {
            IS_SUPPORT_MULTI_LOGIN = true;
            EMConnectionManager.CURRENT_DEVICE = "android";
        } else {
            IS_SUPPORT_MULTI_LOGIN = false;
            EMConnectionManager.CURRENT_DEVICE = "youjob";
        }
    }

    /*
    保存某群组的部分属性
     */
    public void saveGroupPartStatus(String groupJid, int mGroupShowRead, int mGroupAllowSecretlyChat,
                                    int mGroupAllowConference, int mGroupAllowSendCourse, long mGroupTalkTime) {
        // 是否显示群消息已读人数
        PreferenceUtils.putBoolean(this, Constants.IS_SHOW_READ + groupJid, mGroupShowRead == 1);
        // 是否允许普通成员私聊
        PreferenceUtils.putBoolean(this, Constants.IS_SEND_CARD + groupJid, mGroupAllowSecretlyChat == 1);
        // 是否允许普通成员发起会议
        PreferenceUtils.putBoolean(this, Constants.IS_ALLOW_NORMAL_CONFERENCE + groupJid, mGroupAllowConference == 1);
        // 是否允许普通成员发送课程
        PreferenceUtils.putBoolean(this, Constants.IS_ALLOW_NORMAL_SEND_COURSE + groupJid, mGroupAllowSendCourse == 1);
        // 是否开启了全体禁言
        PreferenceUtils.putBoolean(this, Constants.GROUP_ALL_SHUP_UP + groupJid, mGroupTalkTime > 0);
    }

    /**
     * 初始化支付密码设置状态，
     * 登录接口返回支付密码是否设置，在这里保存起来，
     *
     * @param payPassword 支付密码是否已经设置，
     */
    public void initPayPassword(String userId, int payPassword) {
        Log.d(TAG, "initPayPassword() called with: userId = [" + userId + "], payPassword = [" + payPassword + "]");
        // 和initPrivateSettingStatus中的其他变量保存方式统一，
        PreferenceUtils.putBoolean(this, Constants.IS_PAY_PASSWORD_SET + userId, payPassword == 1);
    }

    public FLYBdLocationHelper getBdLocationHelper() {
        if (mFLYBdLocationHelper == null) {
            mFLYBdLocationHelper = new FLYBdLocationHelper(this);
        }
        return mFLYBdLocationHelper;
    }

    // 意义不明，
    private void initAppDirsecond() {
        File innerFile = new File(getFilesDir(), "external");
        File file = getExternalFilesDir(null);
        if (file != null && !file.exists()) {
            file.mkdirs();
        }
        if (file == null) {
            // 不能为空，
            file = innerFile;
        }
        mAppDir01 = file.getAbsolutePath();

        file = getExternalFilesDir(Environment.DIRECTORY_PICTURES);
        if (file != null && !file.exists()) {
            file.mkdirs();
        }
        if (file == null) {
            // 不能为空，
            file = new File(innerFile, Environment.DIRECTORY_PICTURES);
        }
        mPicturesDir01 = file.getAbsolutePath();

        file = getExternalFilesDir(Environment.DIRECTORY_MUSIC);
        if (file != null && !file.exists()) {
            file.mkdirs();
        }
        if (file == null) {
            // 不能为空，
            file = new File(innerFile, Environment.DIRECTORY_MUSIC);
        }
        mVoicesDir01 = file.getAbsolutePath();

        file = getExternalFilesDir(Environment.DIRECTORY_MOVIES);
        if (file != null && !file.exists()) {
            file.mkdirs();
        }
        if (file == null) {
            // 不能为空，
            file = new File(innerFile, Environment.DIRECTORY_MOVIES);
        }
        mVideosDir01 = file.getAbsolutePath();

        file = getExternalFilesDir(Environment.DIRECTORY_DOWNLOADS);
        if (file != null && !file.exists()) {
            file.mkdirs();
        }
        if (file == null) {
            // 不能为空，
            file = new File(innerFile, Environment.DIRECTORY_DOWNLOADS);
        }
        mFilesDir01 = file.getAbsolutePath();
    }

    private void initAppDir() {
        File innerFile = new File(getFilesDir(), "external");
        File file = getExternalFilesDir(null);
        if (file != null && !file.exists()) {
            file.mkdirs();
        }
        if (file == null) {
            // 不能为空，
            file = innerFile;
        }
        mAppDir = file.getAbsolutePath();

        file = getExternalFilesDir(Environment.DIRECTORY_PICTURES);
        if (file != null && !file.exists()) {
            file.mkdirs();
        }
        if (file == null) {
            // 不能为空，
            file = new File(innerFile, Environment.DIRECTORY_PICTURES);
        }
        mPicturesDir = file.getAbsolutePath();

        file = getExternalFilesDir(Environment.DIRECTORY_MUSIC);
        if (file != null && !file.exists()) {
            file.mkdirs();
        }
        if (file == null) {
            // 不能为空，
            file = new File(innerFile, Environment.DIRECTORY_MUSIC);
        }
        mVoicesDir = file.getAbsolutePath();

        file = getExternalFilesDir(Environment.DIRECTORY_MOVIES);
        if (file != null && !file.exists()) {
            file.mkdirs();
        }
        if (file == null) {
            // 不能为空，
            file = new File(innerFile, Environment.DIRECTORY_MOVIES);
        }
        mVideosDir = file.getAbsolutePath();

        file = getExternalFilesDir(Environment.DIRECTORY_DOWNLOADS);
        if (file != null && !file.exists()) {
            file.mkdirs();
        }
        if (file == null) {
            // 不能为空，
            file = new File(innerFile, Environment.DIRECTORY_DOWNLOADS);
        }
        mFilesDir = file.getAbsolutePath();
    }

    /**
     * 在程序内部关闭时，调用此方法
     */
    public void destory() {
        if (FLYAppConfig.DEBUG) {
            Log.d(FLYAppConfig.TAG, "MyApplication destory");
        }
        // 结束百度定位
        if (mFLYBdLocationHelper != null) {
            mFLYBdLocationHelper.release();
        }
        android.os.Process.killProcess(android.os.Process.myPid());
    }

    public void destoryRestart() {
        if (FLYAppConfig.DEBUG) {
            Log.d(FLYAppConfig.TAG, "MyApplication destory");
        }
        // 结束百度定位
        if (mFLYBdLocationHelper != null) {
            mFLYBdLocationHelper.release();
        }
    }

    /***********************
     * 保存其他用户坐标信息
     ***************/

    @Override
    public void onTerminate() {
        super.onTerminate();
    }

    public void initLruCache() {
        // Get max available VM memory, exceeding this amount will throw an
        // OutOfMemory exception. Stored in kilobytes as LruCache takes an
        // int in its constructor.
        final int maxMemory = (int) (Runtime.getRuntime().maxMemory() / 1024);
        // Use 1/8th of the available memory for this memory cache.
        final int cacheSize = maxMemory / 8;
        mMemoryCache = new LruCache<String, Bitmap>(cacheSize) {
            @Override
            protected int sizeOf(String key, Bitmap bitmap) {
                // The cache size will be measured in kilobytes rather than
                // number of items.
                return bitmap.getByteCount() / 1024;
            }
        };
    }

    public void addBitmapToMemoryCache(String key, Bitmap bitmap) {
        if (getBitmapFromMemCache(key) == null) {
            mMemoryCache.put(key, bitmap);
        }
    }

    public Bitmap getBitmapFromMemCache(String key) {
        return mMemoryCache.get(key);
    }

    private HttpProxyCacheServer newProxy() {
        return new HttpProxyCacheServer.Builder(this)
                .maxCacheSize(1024 * 1024 * 1024)       // 1 Gb for cache
                .fileNameGenerator(new MyFileNameGenerator()).build();
    }


}
