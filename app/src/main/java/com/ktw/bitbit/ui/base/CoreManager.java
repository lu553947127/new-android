package com.ktw.bitbit.ui.base;

import android.app.Activity;
import android.content.ComponentName;
import android.content.Context;
import android.content.ServiceConnection;
import android.content.SharedPreferences;
import android.os.IBinder;
import android.text.TextUtils;
import android.util.Log;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.core.content.ContextCompat;

import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.TypeReference;
import com.ktw.bitbit.FLYAppConfig;
import com.ktw.bitbit.FLYApplication;
import com.ktw.bitbit.R;
import com.ktw.bitbit.FLYReporter;
import com.ktw.bitbit.bean.ConfigBean;
import com.ktw.bitbit.bean.User;
import com.ktw.bitbit.bean.UserStatus;
import com.ktw.bitbit.bean.collection.Collectiion;
import com.ktw.bitbit.bean.message.ChatMessage;
import com.ktw.bitbit.bean.message.NewFriendMessage;
import com.ktw.bitbit.bean.message.XmppMessage;
import com.ktw.bitbit.bean.redpacket.Balance;
import com.ktw.bitbit.db.dao.UserDao;
import com.ktw.bitbit.helper.DialogHelper;
import com.ktw.bitbit.helper.LoginHelper;
import com.ktw.bitbit.sp.UserSp;
import com.ktw.bitbit.ui.FLYUserCheckedActivity;
import com.ktw.bitbit.util.Constants;
import com.ktw.bitbit.util.PreferenceUtils;
import com.ktw.bitbit.xmpp.CoreService;
import com.xuan.xuanhttplibrary.okhttp.HttpUtils;
import com.xuan.xuanhttplibrary.okhttp.callback.BaseCallback;
import com.xuan.xuanhttplibrary.okhttp.callback.ListCallback;
import com.xuan.xuanhttplibrary.okhttp.result.ArrayResult;
import com.xuan.xuanhttplibrary.okhttp.result.ObjectResult;

import java.io.IOException;
import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

import okhttp3.Call;

public class CoreManager {
    public static final String KEY_CONFIG_BEAN = "configBean";
    private static final String TAG = "CoreManager";
    private static FLYAppConfig staticConfig = null;
    private static User staticSelf = null;
    private static UserStatus staticSelfStatus = null;
    private static Context mContext;
    private boolean loginRequired;
    private boolean configRequired;
    private User self = null;
    private UserStatus selfStatus = null;
    private Limit limit = new Limit(this);
    private FLYAppConfig config = null;
    private Context ctx;
    @Nullable
    private CoreStatusListener coreStatusListener;
    // ??????Service??????????????????
    // TODO: ?????????????????????????????????
    private Runnable connectedCallback;
    private CoreService mService;
    private boolean isBind = false;
    // ??????????????????????????????
    private ServiceConnection mCoreServiceConnection;

    CoreManager(Context ctx, @Nullable CoreStatusListener coreStatusListener) {
        this.ctx = ctx;
        this.coreStatusListener = coreStatusListener;
    }

    /**
     * ????????????ctx?????????coreManager???????????????coreService,
     */
    public static CoreManager getInstance(Context ctx) {
        CoreManager coreManager = new CoreManager(ctx, null);
        coreManager.init(false, false);
        return coreManager;
    }

    private static SharedPreferences getSharedPreferences(Context ctx) {
        return ctx.getSharedPreferences("core_manager", Context.MODE_PRIVATE);
    }

    @SuppressWarnings("WeakerAccess")
    public static FLYAppConfig requireConfig(Context ctx) {
        mContext = ctx;
        if (staticConfig == null) {
            synchronized (CoreManager.class) {
                if (staticConfig == null) {
                    ConfigBean configBean = readConfigBean(ctx);
                    if (configBean == null) {
                        configBean = getDefaultConfig(ctx);
                    }
                    setStaticConfig(ctx, FLYAppConfig.initConfig(configBean));
                }
            }
        }
        return staticConfig;
    }

    private static ConfigBean readConfigBean(Context ctx) {
        String configBeanJson = getSharedPreferences(ctx)
                .getString(KEY_CONFIG_BEAN, null);
        if (TextUtils.isEmpty(configBeanJson)) {
            return null;
        }
        return JSON.parseObject(configBeanJson, ConfigBean.class);
    }

    // ??????????????????????????????
    public static void initLocalCollectionEmoji() {
        Map<String, String> params = new HashMap<>();
        params.put("access_token", staticSelfStatus.accessToken);
        params.put("userId", CoreManager.requireSelf(FLYApplication.getInstance()).getUserId());

        HttpUtils.get().url(CoreManager.requireConfig(FLYApplication.getInstance()).Collection_LIST)
                .params(params)
                .build()
                .execute(new ListCallback<Collectiion>(Collectiion.class) {
                    @Override
                    public void onResponse(ArrayResult<Collectiion> result) {
                        if (result.getResultCode() == 1 && result.getData() != null) {
                            FLYApplication.mCollection = result.getData();
                            //????????????????????????item
                            Collectiion collection = new Collectiion();
                            collection.setType(7);
                            FLYApplication.mCollection.add(0, collection);
                        }

                    }

                    @Override
                    public void onError(Call call, Exception e) {

                    }
                });
    }

    // ??????????????????
    // ??????????????????onCreate??????????????????updateMyBalance????????????????????????onCreate???????????????
    public static void updateMyBalance() {
        HashMap<String, String> params = new HashMap<String, String>();
        params.put("access_token", staticSelfStatus.accessToken);
        HttpUtils.get().url(CoreManager.requireConfig(FLYApplication.getInstance()).RECHARGE_GET)
                .params(params)
                .build()
                .execute(new BaseCallback<Balance>(Balance.class) {

                    @Override
                    public void onResponse(ObjectResult<Balance> result) {
                        Balance b = result.getData();
                        if (b != null) {
                            staticSelf.setBalance(b.getBalance());
                        }
                    }

                    @Override
                    public void onError(Call call, Exception e) {
                    }
                });
    }

    public static void appBackstage(Context context, boolean isBack) {
        String accessToken = UserSp.getInstance(context).getAccessToken();
        if (TextUtils.isEmpty(accessToken)) {
            // ??????????????????????????????????????????????????????
            return;
        }
        String str;
        User self = staticSelf;
        if (isBack) {
            str = "??????????????????";
            Log.e("appBackstage", str + "?????????--?????????????????????????????????");
            long time = System.currentTimeMillis() / 1000;
            CoreManager.saveOfflineTime(context, self.getUserId(), time);
            UserDao.getInstance().updateUnLineTime(self.getUserId(), time);
            Log.e("appBackstage", str + "?????????--?????????????????????????????????");
        } else {
            str = "XMPP???????????? || ????????????";
        }

        Log.e("appBackstage", str + "?????????--?????????outTime??????");
        Map<String, String> params = new HashMap<>();
        params.put("userId", self.getUserId());

        HttpUtils.get().url(CoreManager.requireConfig(FLYApplication.getInstance()).USER_OUTTIME)
                .params(params)
                .build().execute(new BaseCallback<Void>(Void.class) {
            @Override
            public void onResponse(ObjectResult<Void> result) {

            }

            @Override
            public void onError(Call call, Exception e) {

            }
        });
    }

    public static void saveOfflineTime(Context context, String userId, long outTime) {
        if (Constants.OFFLINE_TIME_IS_FROM_SERVICE) {
            Log.e("appBackstage", "?????????????????????????????????--???" + outTime);
        } else {
            Log.e("appBackstage", "???????????????????????????--???" + outTime);
        }
        PreferenceUtils.putLong(context, Constants.OFFLINE_TIME + userId, outTime);
        if (staticSelf != null) {
            staticSelf.setOfflineTime(outTime);
        }
    }

    @SuppressWarnings("WeakerAccess")
    @NonNull
    public static User requireSelf(Context ctx) {
        if (staticSelf == null) {
            synchronized (CoreManager.class) {
                if (staticSelf == null) {
                    String userId = UserSp.getInstance(ctx).getUserId("");
                    User user = UserDao.getInstance().getUserByUserId(userId);
                    if (user == null) {
                        FLYReporter.post("?????????User?????????");
                        // ??????????????????????????????????????????????????????
                        FLYApplication.getInstance().mUserStatus = LoginHelper.STATUS_USER_TOKEN_CHANGE;
                        // ???????????????
                        FLYUserCheckedActivity.start(ctx);
                        // ?????????????????????????????????null, ????????????????????????????????????????????????????????????finish?????????
                        user = new User();
                    }
                    setStaticSelf(user);
                }
            }
        }
        Log.d(TAG, "requireSelfUser() returned: " + staticSelf);
        return staticSelf;
    }

    @SuppressWarnings("WeakerAccess")
    @Nullable
    public static User getSelf(Context ctx) {
        if (staticSelf == null) {
            synchronized (CoreManager.class) {
                if (staticSelf == null) {
                    String userId = UserSp.getInstance(ctx).getUserId("");
                    setStaticSelf(UserDao.getInstance().getUserByUserId(userId));
                }
            }
        }
        Log.d(TAG, "requireSelfUser() returned: " + staticSelf);
        return staticSelf;
    }

    /**
     * @deprecated ????????????????????????????????????access_token????????????userStatus????????????????????????
     */
    @Deprecated
    @SuppressWarnings("WeakerAccess")
    @NonNull
    public static UserStatus requireSelfStatus(Context ctx) {
        if (staticSelfStatus == null) {
            synchronized (CoreManager.class) {
                if (staticSelfStatus == null) {
                    UserStatus info = new UserStatus();
                    info.accessToken = UserSp.getInstance(ctx).getAccessToken(null);

                    if (TextUtils.isEmpty(info.accessToken)) {
                        FLYReporter.post("?????????accessToken?????????");
                        // ??????????????????????????????????????????????????????
                        FLYApplication.getInstance().mUserStatus = LoginHelper.STATUS_USER_TOKEN_CHANGE;
                        // ???????????????
                        FLYUserCheckedActivity.start(ctx);
                        // ?????????????????????????????????null, ????????????????????????????????????????????????????????????finish?????????
                    }
                    setStaticSelfStatus(info);
                }
            }
        }
        return staticSelfStatus;
    }

    /**
     * @deprecated ????????????????????????????????????access_token????????????userStatus????????????????????????
     */
    @Deprecated
    @SuppressWarnings("WeakerAccess")
    @Nullable
    public static UserStatus getSelfStatus(Context ctx) {
        if (staticSelfStatus == null) {
            synchronized (CoreManager.class) {
                if (staticSelfStatus == null) {
                    UserStatus info = new UserStatus();
                    info.accessToken = UserSp.getInstance(ctx).getAccessToken(null);
                    if (!TextUtils.isEmpty(info.accessToken)) {
                        setStaticSelfStatus(info);
                    }
                }
            }
        }
        return staticSelfStatus;
    }

    private static void setStaticConfig(Context ctx, FLYAppConfig config) {
        staticConfig = config;
        FLYReporter.putUserData("configUrl", FLYAppConfig.readConfigUrl(ctx));
        if (config != null) {
            FLYReporter.putUserData("apiUrl", config.apiUrl);
        }
    }

    /**
     * ????????????????????????????????????????????????
     * ???????????????bugly??????
     */
    private static void setStaticSelf(User self) {
        staticSelf = self;
        if (self != null) {
            FLYReporter.setUserId(self.getTelephone());
            FLYReporter.putUserData("userId", self.getUserId());
            FLYReporter.putUserData("telephone", self.getTelephone());
            // Reporter.putUserData("password", self.getPassword());
            FLYReporter.putUserData("nickName", self.getNickName());
        }
    }

    /**
     * ????????????????????????????????????????????????
     * ???????????????bugly??????
     */
    private static void setStaticSelfStatus(UserStatus selfStatus) {
        staticSelfStatus = selfStatus;
        if (selfStatus != null) {
            FLYReporter.putUserData("accessToken", selfStatus.accessToken);
        }
    }

    /**
     * ????????????????????????config??????????????????????????????config?????????????????????????????????
     */
    @NonNull
    public static ConfigBean getDefaultConfig(Context ctx) {
        try {
            // ??????????????????config?????????????????????assets???????????????config,
            ObjectResult<ConfigBean> result = JSON.parseObject(ctx.getAssets().open("default_config"), new TypeReference<ObjectResult<ConfigBean>>() {
            }.getType());
            if (result == null || result.getData() == null) {
                // ????????????????????????????????????null,
                return new ConfigBean();
            }
            return result.getData();
        } catch (IOException e) {
            // ?????????????????????assets?????????????????????config,
            FLYReporter.unreachable();
            return new ConfigBean();
        }
    }

    // ??????????????????????????????????????????????????????
    private ServiceConnection createCoreServiceConnection() {
        return new ServiceConnection() {
            @Override
            public void onServiceDisconnected(ComponentName name) {
                // ????????????Service?????????????????????????????????????????????????????????
                Log.d(TAG, "onServiceDisconnected() called with: name = [" + name + "]");
                mService = null;
                mCoreServiceConnection = null;
            }

            @Override
            public void onServiceConnected(ComponentName name, IBinder service) {
                Log.d(TAG, "onServiceConnected() called with: name = [" + name + "], service = [" + service + "]");
                mService = ((CoreService.CoreServiceBinder) service).getService();
                mCoreServiceConnection = this;
                // ????????????CoreService???????????????
                if (connectedCallback != null) {
                    connectedCallback.run();
                }
            }
        };
    }

    public ConfigBean readConfigBean() {
        return readConfigBean(ctx);
    }

    public void saveConfigBean(ConfigBean configBean) {
        getSharedPreferences(ctx)
                .edit()
                .putString(KEY_CONFIG_BEAN, JSON.toJSONString(configBean))
                .apply();
        config = FLYAppConfig.initConfig(configBean);
        setStaticConfig(ctx, config);
    }

    public FLYAppConfig getConfig() {
        if (config == null) {
            config = requireConfig(ctx);
        }
        return config;
    }

    public User getSelf() {
        return self;
    }

    /**
     * ????????????????????????????????????????????????
     */
    public void setSelf(User self) {
        this.self = self;
        setStaticSelf(self);
    }

    public UserStatus getSelfStatus() {
        return selfStatus;
    }

    /**
     * ????????????????????????????????????????????????
     */
    public void setSelfStatus(UserStatus selfStatus) {
        this.selfStatus = selfStatus;
        setStaticSelfStatus(selfStatus);
    }

    public Limit getLimit() {
        return limit;
    }

    void init(boolean loginRequired, boolean configRequired) {
        Log.d(TAG, "init() called");
        this.loginRequired = loginRequired;
        this.configRequired = configRequired;
        if (loginRequired) {
            this.self = requireSelf(ctx);
            this.selfStatus = requireSelfStatus(ctx);
        } else {
            this.self = getSelf(ctx);
            this.selfStatus = getSelfStatus(ctx);
        }
        if (configRequired) {
            this.config = requireConfig(ctx);
        }
        // TODO: ????????????User????????????????????????????????????
        if (loginRequired && !isServiceReady()) {
            ContextCompat.startForegroundService(ctx, CoreService.getIntent(ctx, self.getUserId(), self.getPassword(), self.getNickName()));
            connectedCallback = () -> {
                if (coreStatusListener != null) {
                    coreStatusListener.onCoreReady();
                }
                // onCoreReady???????????????????????????????????????????????????
                // ??????????????????????????????
                connectedCallback = null;
            };
            isBind = ctx.bindService(CoreService.getIntent(), createCoreServiceConnection(), Context.BIND_AUTO_CREATE);
        }
    }

    void destroy() {
        if (isBind && mCoreServiceConnection != null) {
            try {
                ctx.unbindService(mCoreServiceConnection);
            } catch (Exception e) {
                // ???????????????????????????????????????IllegalArgumentException???
                FLYReporter.unreachable(e);
            }
        }
    }

    public void logout() {
        if (isServiceReady()) {
            try {
                mService.logout();
                Log.e("zq", "logout??????");
            } catch (Exception e) {
                // ???????????????
                FLYReporter.unreachable(e);
                Log.e("zq", "logout??????1");
            }
        } else {
            Log.e("zq", "logout??????2");
        }
    }

    public boolean isServiceReady() {
        return isBind && mService != null;
    }

    private void requireXmpp() {
        if (!isServiceReady()) {
            throw new IllegalStateException("xmpp???????????????");
        }
    }

    private boolean requireXmppOrReport() {
        boolean ret = isServiceReady();
        if (!ret) {
            FLYReporter.post("xmpp???????????????");
        }
        return ret;
    }

    public void disconnect() {
        Log.d("zx", "disconnect() called");
        if (isServiceReady() && mService.getmConnectionManager() != null
                && mService.getmConnectionManager().getConnection() != null) {
            mService.getmConnectionManager().getConnection().disconnect();
        }
    }

    public boolean isLogin() {
        Log.d(TAG, "isLogin() called");
        return isServiceReady() && mService.isAuthenticated();
    }

    /**
     * ???????????????????????????????????????
     */
    public void relogin() {
        Log.d(TAG, "relogin() called");
        if (!isLogin()) {
            ContextCompat.startForegroundService(ctx, CoreService.getIntent(ctx, self.getUserId(), self.getPassword(), self.getNickName()));
            connectedCallback = null;
            isBind = ctx.bindService(CoreService.getIntent(), createCoreServiceConnection(), Context.BIND_AUTO_CREATE);
        }
    }

    public void batchMucChat() {
        Log.d(TAG, "batchMucChat() called");
        requireXmpp();
        mService.batchJoinMucChat();
    }

    public String createMucRoom(String roomName) {
        Log.d(TAG, "createMucRoom() called with: roomName = [" + roomName + "]");
        requireXmpp();
        return mService.createMucRoom(roomName);
    }

    public void joinMucChat(String mRoomJid, long currentTimeSecond) {
        Log.d(TAG, "joinMucChat() called with: mRoomJid = [" + mRoomJid + "], l = [" + currentTimeSecond + "]");
        requireXmpp();
        mService.joinMucChat(mRoomJid, currentTimeSecond);
    }

    public void exitMucChat(String mRoomJid) {
        Log.d(TAG, "exitMucChat() called with: mRoomJid = [" + mRoomJid + "]");
        if (!requireXmppOrReport()) {
            return;
        }
        mService.exitMucChat(mRoomJid);
    }

    private void requirePackageId(XmppMessage message) {
        if (TextUtils.isEmpty(message.getPacketId())) {
            message.setPacketId(UUID.randomUUID().toString().replaceAll("-", ""));
        }
    }

    public void sendNewFriendMessage(String userId, NewFriendMessage message) {
        Log.d(TAG, "sendNewFriendMessage() called with: userId = [" + userId + "], message = [" + message + "]");
        requireXmpp();
        requirePackageId(message);
        mService.sendNewFriendMessage(userId, message);
    }

    public void sendChatMessage(String toUserId, ChatMessage message) {
        Log.d(TAG, "sendChatMessage() called with: call_toUser = [" + toUserId + "], message = [" + message + "]");
        requireXmpp();
        requirePackageId(message);
        mService.sendChatMessage(toUserId, message);
    }

    public void sendMucChatMessage(String mRoomJid, ChatMessage message) {
        Log.d(TAG, "sendMucChatMessage() called with: mRoomJid = [" + mRoomJid + "], message = [" + message + "]");
        requireXmpp();
        requirePackageId(message);
        mService.sendMucChatMessage(mRoomJid, message);
    }

    /**
     * ????????????
     */
    public void autoReconnect(Activity activity) {
        logout();
        User user = getSelf();
        if (user != null) {
            Log.e("zq", "????????????--->??????????????????");
            ContextCompat.startForegroundService(ctx, CoreService.getIntent(ctx, self.getUserId(), self.getPassword(), self.getNickName()));
        } else {
            Log.e("zq", "????????????--->????????????????????????");
            Toast.makeText(activity, activity.getString(R.string.tip_local_user_null), Toast.LENGTH_SHORT).show();
        }
    }

    /**
     * ???????????? ???????????????
     */
    public void autoReconnectShowProgress(Activity activity) {
        DialogHelper.showMessageProgressDialogAddCancel(activity, ctx.getString(R.string.keep_reconnection), dialog -> dialog.dismiss());

        logout();
        User user = getSelf();
        if (user != null) {
            Log.e("zq", "????????????--->??????????????????");
            ContextCompat.startForegroundService(ctx, CoreService.getIntent(ctx, self.getUserId(), self.getPassword(), self.getNickName()));
        } else {
            DialogHelper.dismissProgressDialog();
            Log.e("zq", "????????????--->????????????????????????");
            Toast.makeText(activity, activity.getString(R.string.tip_local_user_null), Toast.LENGTH_SHORT).show();
        }
    }

    public void reloginX() {
        logout();
        ContextCompat.startForegroundService(ctx, CoreService.getIntent(ctx, self.getUserId(), self.getPassword(), self.getNickName()));
        connectedCallback = null;
        isBind = ctx.bindService(CoreService.getIntent(), createCoreServiceConnection(), Context.BIND_AUTO_CREATE);
    }
}
