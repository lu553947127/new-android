package com.ktw.bitbit.ui;

import android.Manifest;
import android.annotation.SuppressLint;
import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.os.CountDownTimer;
import android.text.TextUtils;
import android.util.Log;
import android.view.WindowManager;

import com.ktw.bitbit.FLYAppConfig;
import com.ktw.bitbit.FLYAppConstant;
import com.ktw.bitbit.BuildConfig;
import com.ktw.bitbit.FLYApplication;
import com.ktw.bitbit.R;
import com.ktw.bitbit.FLYReporter;
import com.ktw.bitbit.bean.ConfigBean;
import com.ktw.bitbit.bean.event.MessageLogin;
import com.ktw.bitbit.helper.DialogHelper;
import com.ktw.bitbit.helper.LoginHelper;
import com.ktw.bitbit.ui.account.LoginActivity;
import com.ktw.bitbit.ui.account.LoginHistoryActivity;
import com.ktw.bitbit.ui.base.BaseActivity;
import com.ktw.bitbit.ui.base.CoreManager;
import com.ktw.bitbit.ui.notification.NotificationProxyActivity;
import com.ktw.bitbit.ui.other.PrivacyAgreeActivity;
import com.ktw.bitbit.util.Constants;
import com.ktw.bitbit.util.EventBusHelper;
import com.ktw.bitbit.util.PermissionUtil;
import com.ktw.bitbit.util.PreferenceUtils;
import com.ktw.bitbit.util.TimeUtils;
import com.ktw.bitbit.util.ToastUtil;
import com.ktw.bitbit.util.VersionUtil;
import com.ktw.bitbit.view.PermissionExplainDialog;
import com.ktw.bitbit.view.TipDialog;
import com.ktw.bitbit.view.cjt2325.cameralibrary.util.LogUtil;
import com.xuan.xuanhttplibrary.okhttp.HttpUtils;
import com.xuan.xuanhttplibrary.okhttp.callback.BaseCallback;
import com.xuan.xuanhttplibrary.okhttp.result.ObjectResult;
import com.xuan.xuanhttplibrary.okhttp.result.Result;

import java.util.HashMap;
import java.util.HashSet;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;

import de.greenrobot.event.Subscribe;
import de.greenrobot.event.ThreadMode;
import okhttp3.Call;

/**
 * 启动页
 */
public class FLYSplashActivity extends BaseActivity implements PermissionUtil.OnRequestPermissionsResultCallbacks {
    // 声明一个数组，用来存储所有需要动态申请的权限
    private static final int REQUEST_CODE = 0;
    private final Map<String, Integer> permissionsMap = new LinkedHashMap<>();
    // 配置是否成功
    private boolean mConfigReady = false;
    // 复用请求权限的说明对话框，
    private PermissionExplainDialog permissionExplainDialog;

//    private ImageView ivAd;
//    private TextView tvSkip;
//    private CustomVideoView videoAd;
    //广告地址
    private String adSourceUrl;
    //广告类型
    private int adSourceType;
    //广告加载完毕
    private boolean isFinishAd = false;
    //广告展示时间s
    private int second = 10;
    private CountDownTimer countDownTimert;

    public FLYSplashActivity() {
        // 这个页面不需要已经获取config, 也不需要已经登录，
        noConfigRequired();
        noLoginRequired();

        // 手机状态
        permissionsMap.put(Manifest.permission.READ_PHONE_STATE, R.string.permission_phone_status);
        // 照相
        permissionsMap.put(Manifest.permission.CAMERA, R.string.permission_photo);
        // 麦克风
        permissionsMap.put(Manifest.permission.RECORD_AUDIO, R.string.permission_microphone);
        // 存储权限
        permissionsMap.put(Manifest.permission.READ_EXTERNAL_STORAGE, R.string.permission_storage);
        permissionsMap.put(Manifest.permission.WRITE_EXTERNAL_STORAGE, R.string.permission_storage);
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        Intent intent = getIntent();
        if (NotificationProxyActivity.processIntent(intent)) {
            // 如果是通知点击进来的，带上参数转发给NotificationProxyActivity处理，
            intent.setClass(this, NotificationProxyActivity.class);
            startActivity(intent);
            finish();
            return;
        }
        if ((getIntent().getFlags() & Intent.FLAG_ACTIVITY_BROUGHT_TO_FRONT) != 0) {
            finish();
            return;
        }
        // 如果不是任务栈第一个页面，就直接结束，显示上一个页面，
        // 主要是部分设备上Jitsi_pre页面退后台再回来会打开这个启动页flg=0x10200000，此时应该结束启动页，回到Jitsi_pre,
        if (!isTaskRoot()) {
            finish();
            return;
        }

        getWindow().setFlags(WindowManager.LayoutParams.FLAG_LAYOUT_NO_LIMITS, WindowManager.LayoutParams.FLAG_LAYOUT_NO_LIMITS);
//        setContentView(R.layout.activity_splash);
        setContentView(R.layout.acitivty_new_splash);

        adSourceUrl = PreferenceUtils.getString(FLYSplashActivity.this, Constants.KEY_SPLASH_AD_PATH);
        adSourceType = PreferenceUtils.getInt(FLYSplashActivity.this, Constants.KEY_SPLASH_AD_TYPE);

        LogUtil.d("获取本地缓存视频路径 = " + adSourceUrl);

//        ivAd = findViewById(R.id.ivAd);
//        tvSkip = findViewById(R.id.tvSkip);
//        videoAd = findViewById(R.id.videoAd);
//        videoAd.setOnClickListener(new View.OnClickListener() {
//            @Override
//            public void onClick(View view) {
//                videoAd.pause();
//                countDownTimert.cancel();
//                isFinishAd = true;
//                WebView2Activity.start(FLYSplashActivity.this
//                        , PreferenceUtils.getString(FLYSplashActivity.this, Constants.KEY_SPLASH_AD_WEBSITE)
//                        , PreferenceUtils.getString(FLYSplashActivity.this, Constants.KEY_SPLASH_AD_TITLE)
//                , FLYSplashActivity.REQUEST_CODE);
//            }
//        });
//        ivAd.setOnClickListener(new View.OnClickListener() {
//            @Override
//            public void onClick(View view) {
//                videoAd.pause();
//                countDownTimert.cancel();
//                isFinishAd = true;
//                WebView2Activity.start(FLYSplashActivity.this
//                        , PreferenceUtils.getString(FLYSplashActivity.this, Constants.KEY_SPLASH_AD_WEBSITE)
//                        , PreferenceUtils.getString(FLYSplashActivity.this, Constants.KEY_SPLASH_AD_TITLE)
//                        , FLYSplashActivity.REQUEST_CODE);
//
//            }
//        });
        if (TextUtils.isEmpty(adSourceUrl)) {
            isFinishAd = true;
        }
        // 初始化配置
        initConfig();
        // 同时请求定位以外的权限，
        requestPermissions();

        EventBusHelper.register(this);

    }

//    private void processAd() {
//        if (!TextUtils.isEmpty(adSourceUrl)) {
//            setTimer();
//            if (adSourceType == FLYMainActivity.AD_TYPE_IMAGE) {
//                ivAd.setVisibility(View.VISIBLE);
//                ivAd.requestFocus();
//                Glide.with(FLYSplashActivity.this)
//                        .load(Uri.fromFile(new File(adSourceUrl)))
//                        .into(ivAd);
//            } else if (adSourceType == FLYMainActivity.AD_TYPE_VIDEO) {
//                videoAd.setVisibility(View.VISIBLE);
//                videoAd.requestFocus();
//                videoAd.setVideoURI(Uri.fromFile(new File(adSourceUrl)));
//                videoAd.start();
//            }
//        }
//    }

//    private void setTimer() {
//        tvSkip.setText(getString(R.string.skip_second, second + ""));
//        tvSkip.setVisibility(View.VISIBLE);
//        tvSkip.setOnClickListener(new View.OnClickListener() {
//            @Override
//            public void onClick(View view) {
//                isFinishAd = true;
//                videoAd.pause();
//                countDownTimert.cancel();
//                ready();
//            }
//        });
//        countDownTimert = new CountDownTimer(second * 1000, 1000) {
//            @Override
//            public void onTick(long millisUntilFinished) {
//                tvSkip.setText(getString(R.string.skip_second, (millisUntilFinished / 1000) + 1 + ""));
//            }
//
//            @Override
//            public void onFinish() {
//                isFinishAd = true;
//                videoAd.pause();
//                countDownTimert.cancel();
//                ready();
//            }
//        };
//        countDownTimert.start();
//    }


    @Override
    protected void onRestart() {
        super.onRestart();
        // 请求权限过程中离开了回来就再请求吧，
        ready();
    }

    /**
     * 配置参数初始化
     */
    private void initConfig() {
        getConfig();
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
                                ToastUtil.showToast(FLYSplashActivity.this, R.string.tip_get_config_failed);
                            }
                            // 获取网络配置失败，使用已经保存了的配置，
                            configBean = coreManager.readConfigBean();
                        } else {
                            Log.e("zq", "获取网络配置成功，使用服务端返回的配置并更新本地配置");
                            configBean = result.getData();
                            if (!TextUtils.isEmpty(configBean.getAddress())) {
                                PreferenceUtils.putString(FLYSplashActivity.this, FLYAppConstant.EXTRA_CLUSTER_AREA, configBean.getAddress());
                            }
                            coreManager.saveConfigBean(configBean);
                            FLYApplication.IS_OPEN_CLUSTER = configBean.getIsOpenCluster() == 1 ? true : false;
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
            if (configBean == null) {
                // 不可到达，本地assets一定要提供默认config,
                DialogHelper.tip(this, getString(R.string.tip_get_config_failed));
                return;
            }
            coreManager.saveConfigBean(configBean);
        }

        // todo 定位权限放到应用内请求，class：LoginActivity、SelectAreaActivity、NearPersonActivity、MapPickerActivity
/*
        if (!coreManager.getConfig().disableLocationServer) {// 定位
            permissionsMap.put(Manifest.permission.ACCESS_COARSE_LOCATION, R.string.permission_location);
            permissionsMap.put(Manifest.permission.ACCESS_FINE_LOCATION, R.string.permission_location);
        }
*/
        // 配置完毕
        mConfigReady = true;
        // 如果没有androidDisable字段就不判断，
        // 当前版本没被禁用才继续打开，
        if (TextUtils.isEmpty(configBean.getAndroidDisable()) || !blockVersion(configBean.getAndroidDisable(), configBean.getAndroidAppUrl())) {
            // 进入主界面
            ready();
        }
    }

    /**
     * 如果当前版本被禁用，就自杀，
     *
     * @param disabledVersion 禁用该版本以下的版本，
     * @param appUrl          版本被禁用时打开的地址，
     * @return 返回是否被禁用，
     */
    private boolean blockVersion(String disabledVersion, String appUrl) {
        String currentVersion = BuildConfig.VERSION_NAME;
        if (VersionUtil.compare(currentVersion, disabledVersion) > 0) {
            // 当前版本大于被禁用版本，
            return false;
        } else {
            // 通知一下，
            TipDialog tipDialog = new TipDialog(this);
            tipDialog.setmConfirmOnClickListener(getString(R.string.tip_version_disabled), () -> {

            });
            tipDialog.setOnDismissListener(dialog -> {
                try {
                    Intent i = new Intent(Intent.ACTION_VIEW);
                    i.setData(Uri.parse(appUrl));
                    startActivity(i);
                } catch (Exception e) {
                    // 弹出浏览器失败的话无视，
                    // 比如没有浏览器的情况，
                    // 比如appUrl不合法的情况，
                }
                // 自杀，
                finish();
                FLYApplication.getInstance().destory();
            });
            tipDialog.show();
            return true;
        }
    }

    private void ready() {
        if (!mConfigReady) {// 配置失败
            return;
        }
        // 检查 || 请求权限
        boolean hasAll = requestPermissions();
        if (hasAll) {// 已获得所有权限
            if (isFinishAd) {
                //广告显示完后，下一步
                jump();
            } else {
                //获取所有权限后，显示广告
//                processAd();
            }

        }
    }

    @SuppressLint("NewApi")
    private void jump() {
        if (isDestroyed()) {
            return;
        }
        int userStatus = LoginHelper.prepareUser(mContext, coreManager);
        Intent intent = new Intent();
        switch (userStatus) {
            case LoginHelper.STATUS_USER_FULL:
            case LoginHelper.STATUS_USER_NO_UPDATE:
            case LoginHelper.STATUS_USER_TOKEN_OVERDUE:
                boolean login = PreferenceUtils.getBoolean(this, Constants.LOGIN_CONFLICT, false);
                if (login) {// 登录冲突，退出app再次进入，跳转至历史登录界面
                    intent.setClass(mContext, LoginHistoryActivity.class);
                } else {
                    intent.setClass(mContext, FLYMainActivity.class);
                }
                break;
            case LoginHelper.STATUS_USER_SIMPLE_TELPHONE:
                intent.setClass(mContext, LoginHistoryActivity.class);
                break;
            case LoginHelper.STATUS_NO_USER:
            default:
                stay();
                return;// must return
        }
        startActivity(intent);
        finish();
    }

    // 第一次进入，显示登录、注册按钮
    private void stay() {
        // 因为启动页有时会替换，无法做到按钮与图片的完美适配，干脆直接进入到登录页面
        startActivity(new Intent(mContext, LoginActivity.class));
        finish();
    }

    private boolean requestPermissions() {
        if (mConfigReady && !TextUtils.isEmpty(coreManager.getConfig().privacyPolicyPrefix) &&
                !PreferenceUtils.getBoolean(this, Constants.PRIVACY_AGREE_STATUS, false)) {
            // 先同意隐私政策，
            PrivacyAgreeActivity.start(this);
            return false;
        } else {
            // 请求定位以外的权限，
            return requestPermissions(permissionsMap.keySet().toArray(new String[]{}));
        }
    }

    private boolean requestPermissions(String... permissions) {
        List<String> deniedPermission = PermissionUtil.getDeniedPermissions(this, permissions);
        if (deniedPermission != null) {
            PermissionExplainDialog tip = getPermissionExplainDialog();
            tip.setPermissions(deniedPermission.toArray(new String[0]));
            tip.setOnConfirmListener(() -> {
                PermissionUtil.requestPermissions(this, FLYSplashActivity.REQUEST_CODE, permissions);
            });
            tip.show();
            return false;
        }
        return true;
    }

    private PermissionExplainDialog getPermissionExplainDialog() {
        if (permissionExplainDialog == null) {
            permissionExplainDialog = new PermissionExplainDialog(this);
        }
        return permissionExplainDialog;
    }

    @Subscribe(threadMode = ThreadMode.MainThread)
    public void helloEventBus(MessageLogin message) {
        finish();
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (requestCode == REQUEST_CODE) {// 设置 手动开启权限 返回 再次判断是否获取全部权限
            ready();
        }
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, String[] permissions, int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
        PermissionUtil.onRequestPermissionsResult(requestCode, permissions, grantResults, this);
    }

    @Override
    public void onPermissionsGranted(int requestCode, List<String> perms, boolean isAllGranted) {
        if (isAllGranted) {// 请求权限返回 已全部授权
            ready();
        }
    }

    @Override
    public void onPermissionsDenied(int requestCode, List<String> perms, boolean isAllDenied) {
        if (perms != null && perms.size() > 0) {
            // 用set去重，有的复数权限
            Set<String> tipSet = new HashSet<>();
            for (int i = 0; i < perms.size(); i++) {
                tipSet.add(getString(permissionsMap.get(perms.get(i))));
            }
            String tipS = TextUtils.join(", ", tipSet);
            boolean onceAgain = PermissionUtil.deniedRequestPermissionsAgain(this, perms.toArray(new String[perms.size()]));
            TipDialog mTipDialog = new TipDialog(this);
            if (onceAgain) {// 部分 || 所有权限被拒绝且选择了（选择了不再询问 || 部分机型默认为不在询问）
                mTipDialog.setmConfirmOnClickListener(getString(R.string.tip_reject_permission_place_holder, tipS), new TipDialog.ConfirmOnClickListener() {
                    @Override
                    public void confirm() {
                        PermissionUtil.startApplicationDetailsSettings(FLYSplashActivity.this, REQUEST_CODE);
                    }
                });
            } else {// 部分 || 所有权限被拒绝
                mTipDialog.setTip(getString(R.string.tip_permission_reboot_place_holder, tipS));
            }
            mTipDialog.show();
        }
    }

    @Override
    protected void onDestroy() {
//        if (adSourceType > 0) {
//            if (adSourceType == FLYMainActivity.AD_TYPE_VIDEO && videoAd != null) {
//                videoAd.pause();
//                videoAd.suspend();
//                videoAd.setOnErrorListener(null);
//                videoAd.setOnPreparedListener(null);
//                videoAd.setOnCompletionListener(null);
//            }
//            if (countDownTimert != null) {
//                countDownTimert.onFinish();
//            }
//        }
        super.onDestroy();
    }
}
