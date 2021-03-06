package com.ktw.bitbit.call;

import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.content.ServiceConnection;
import android.media.MediaPlayer;
import android.os.Build;
import android.os.Bundle;
import android.os.CountDownTimer;
import android.os.IBinder;
import android.text.TextUtils;
import android.util.DisplayMetrics;
import android.util.Log;
import android.view.View;
import android.view.WindowManager;
import android.widget.FrameLayout;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.Nullable;

import com.facebook.react.modules.core.PermissionListener;
import com.ktw.bitbit.R;
import com.ktw.bitbit.FLYReporter;
import com.ktw.bitbit.bean.Friend;
import com.ktw.bitbit.bean.VideoFile;
import com.ktw.bitbit.bean.event.EventNotifyByTag;
import com.ktw.bitbit.bean.message.ChatMessage;
import com.ktw.bitbit.bean.message.XmppMessage;
import com.ktw.bitbit.db.dao.FriendDao;
import com.ktw.bitbit.db.dao.VideoFileDao;
import com.ktw.bitbit.helper.AvatarHelper;
import com.ktw.bitbit.helper.CutoutHelper;
import com.ktw.bitbit.helper.DialogHelper;
import com.ktw.bitbit.ui.base.BaseActivity;
import com.ktw.bitbit.util.AppUtils;
import com.ktw.bitbit.util.HttpUtil;
import com.ktw.bitbit.util.PermissionUtil;
import com.ktw.bitbit.util.PreferenceUtils;
import com.ktw.bitbit.util.TimeUtils;
import com.ktw.bitbit.view.SelectionFrame;
import com.ktw.bitbit.view.TipDialog;

import org.jitsi.meet.sdk.JitsiMeetActivityDelegate;
import org.jitsi.meet.sdk.JitsiMeetActivityInterface;
import org.jitsi.meet.sdk.JitsiMeetConferenceOptions;
import org.jitsi.meet.sdk.JitsiMeetView;
import org.jitsi.meet.sdk.JitsiMeetViewListener;

import java.io.File;
import java.net.MalformedURLException;
import java.net.URL;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

import de.greenrobot.event.EventBus;
import de.greenrobot.event.Subscribe;
import de.greenrobot.event.ThreadMode;
import io.jsonwebtoken.Jwts;

/**
 * 2018-2-27 ??????????????????????????????
 */
public class Jitsi_connecting_second extends BaseActivity implements JitsiMeetActivityInterface {
    private static final String TAG = "Jitsi_connecting_second";
    // ????????????
    private static final int RECORD_REQUEST_CODE = 0x01;
    // ???????????????????????????
    public static String time = null;
    private String mLocalHostJitsi = "https://meet.jit.si/";// ????????????
    private String mLocalHost/* = "https://meet.youjob.co/"*/;  // ????????????,???????????????
    // ????????????(?????????????????????????????????????????????????????????)
    private int mCallType;
    // ???????????????????????????userId???????????????jid,
    private String fromUserId;
    // ????????????????????????????????????userId, ???????????????jid,
    private String toUserId;
    private long startTime = System.currentTimeMillis();// ??????????????????
    private long stopTime; // ??????????????????
    private FrameLayout mFrameLayout;
    private JitsiMeetView mJitsiMeetView;
    private ImageView ivChange;
    // ???????????????
    private ImageView mFloatingView;
    // ??????
    private LinearLayout mRecordLL;
    private ImageView mRecordIv;
    private TextView mRecordTv;
    // ?????????????????????????????????android 5.0,??????????????????
    private boolean isApi21HangUp;
    // private MediaProjection mediaProjection;
    private RecordService recordService;
    private ServiceConnection connection = new ServiceConnection() {
        @Override
        public void onServiceConnected(ComponentName className, IBinder service) {
            DisplayMetrics metrics = new DisplayMetrics();
            getWindowManager().getDefaultDisplay().getMetrics(metrics);
            RecordService.RecordBinder binder = (RecordService.RecordBinder) service;
            recordService = binder.getRecordService();
            recordService.setConfig(metrics.widthPixels, metrics.heightPixels, metrics.densityDpi);
        }

        @Override
        public void onServiceDisconnected(ComponentName arg0) {
        }
    };
    private SimpleDateFormat mSimpleDateFormat = new SimpleDateFormat("mm:ss");
    CountDownTimer mCountDownTimer = new CountDownTimer(18000000, 1000) {// ?????????????????????????????????????????????????????????????????????????????????????????????
        @Override
        public void onTick(long millisUntilFinished) {
            time = formatTime();
            Jitsi_connecting_second.this.sendBroadcast(new Intent(CallConstants.REFRESH_FLOATING));
        }

        @Override
        public void onFinish() {// 12????????????Finish

        }
    };
    private boolean isOldVersion = true;// ????????????????????????????????? "?????????" ??????????????????????????????????????????????????????????????????????????????ping???????????????
    private boolean isEndCallOpposite;// ???????????????????????????
    private int mPingReceiveFailCount;// ????????????????????? "?????????" ???????????????
    // ??????3???????????????????????? "?????????" ??????
    CountDownTimer mCallingCountDownTimer = new CountDownTimer(3000, 1000) {
        @Override
        public void onTick(long millisUntilFinished) {

        }

        @Override
        public void onFinish() {// ????????????
            if (isFinishing()) {
                // ??????activity???????????????????????????ping,
                return;
            }
            if (!HttpUtil.isGprsOrWifiConnected(Jitsi_connecting_second.this)) {
                TipDialog tipDialog = new TipDialog(Jitsi_connecting_second.this);
                tipDialog.setmConfirmOnClickListener(getString(R.string.check_network), () -> {
                    leaveJitsi();
                });

                tipDialog.show();
                return;
            }
            if (mCallType == 1 || mCallType == 2 || mCallType == 5 || mCallType == 6) {// ?????????????????????
                if (isEndCallOpposite) {// ???????????????????????? "?????????" ??????
                    // ???????????????????????????Count??????3?????????????????????????????????????????????????????????????????? "?????????" ?????????count+1
                    int maxCount = 10;
                    if (mCallType == 5 || mCallType == 6) {
                        // ?????????ping???????????????
                        maxCount = 4;
                    }
                    if (mPingReceiveFailCount == maxCount) {
                        if (isOldVersion) {
                            return;
                        }
                        Log.e(TAG, "true-->" + TimeUtils.sk_time_current_time());
                        if (!isDestroyed()) {
                            stopTime = System.currentTimeMillis();
                            overCall((int) (stopTime - startTime) / 1000);
                            Toast.makeText(Jitsi_connecting_second.this, getString(R.string.tip_opposite_offline_auto__end_call), Toast.LENGTH_SHORT).show();
                            leaveJitsi();
/*
                            TipDialog tipDialog = new TipDialog(Jitsi_connecting_second.this);
                            tipDialog.setmConfirmOnClickListener(getString(R.string.tip_opposite_offline_end_call), () -> {
                                stopTime = System.currentTimeMillis();
                                overCall((int) (stopTime - startTime) / 1000);
                                leaveJitsi();
                            });
                            tipDialog.show();
*/
                        }
                    } else {
                        mPingReceiveFailCount++;
                        Log.e(TAG, "true-->" + mPingReceiveFailCount + "???" + TimeUtils.sk_time_current_time());
                        sendCallingMessage();
                    }
                } else {
                    Log.e(TAG, "false-->" + TimeUtils.sk_time_current_time());
                    sendCallingMessage();
                }
            }
        }
    };

    public static void start(Context ctx, String fromuserid, String touserid, int type) {
        start(ctx, fromuserid, touserid, type, null);
    }

    public static void start(Context ctx, String fromuserid, String touserid, int type, @Nullable String meetUrl) {
        if (type == CallConstants.Talk_Meet) {
            Intent intent = new Intent(ctx, JitsiTalk.class);
            intent.putExtra("type", type);
            intent.putExtra("fromuserid", fromuserid);
            intent.putExtra("touserid", touserid);
            if (!TextUtils.isEmpty(meetUrl)) {
                intent.putExtra("meetUrl", meetUrl);
            }
            ctx.startActivity(intent);
            return;
        }
        Intent intent = new Intent(ctx, Jitsi_connecting_second.class);
        intent.putExtra("type", type);
        intent.putExtra("fromuserid", fromuserid);
        intent.putExtra("touserid", touserid);
        if (!TextUtils.isEmpty(meetUrl)) {
            intent.putExtra("meetUrl", meetUrl);
        }
        ctx.startActivity(intent);
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        CutoutHelper.setWindowOut(getWindow());
        super.onCreate(savedInstanceState);
        // ?????????????????? | ?????????????????? | Activity????????????????????? | ??????????????????
        getWindow().addFlags(
                WindowManager.LayoutParams.FLAG_DISMISS_KEYGUARD
                        | WindowManager.LayoutParams.FLAG_SHOW_WHEN_LOCKED
                        | WindowManager.LayoutParams.FLAG_TURN_SCREEN_ON
                        | WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON);

        setContentView(R.layout.jitsiconnecting);
        initData();
        initView();
        initEvent();
        EventBus.getDefault().register(this);
        JitsiMeetActivityDelegate.onHostResume(this);
        setSwipeBackEnable(false);
    }

    @Override
    public void onCoreReady() {
        super.onCoreReady();
        sendCallingMessage();// ??????????????????????????????????????????????????????????????????????????????????????????????????????????????? "?????????" ???????????????
    }

    private void initData() {
        mCallType = getIntent().getIntExtra("type", 0);
        fromUserId = getIntent().getStringExtra("fromuserid");
        toUserId = getIntent().getStringExtra("touserid");

        JitsistateMachine.isInCalling = true;
        if (mCallType == 1 || mCallType == 2) {
            JitsistateMachine.callingOpposite = toUserId;
        } else {
            // ??????????????????????????????id,???????????????????????????????????????????????????
            JitsistateMachine.callingOpposite = fromUserId;
        }

        if (mCallType == 1 || mCallType == 2) {// ??????
            mLocalHost = getIntent().getStringExtra("meetUrl");
            if (TextUtils.isEmpty(mLocalHost)) {
                mLocalHost = coreManager.getConfig().JitsiServer;
            }
        } else {
            mLocalHost = coreManager.getConfig().JitsiServer;
        }

        if (TextUtils.isEmpty(mLocalHost)) {
            DialogHelper.tip(mContext, getString(R.string.tip_meet_server_empty));
            finish();
        }

        // mCallingCountDownTimer.start();
    }

    private void leaveJitsi() {
        Log.e(TAG, "leaveJitsi() called ");
        finish();
    }

    /**
     * startWithAudioMuted:??????????????????
     * startWithVideoMuted:??????????????????
     */
    private void initView() {
        CutoutHelper.initCutoutHolderTop(getWindow(), findViewById(R.id.vCutoutHolder));
        if (mCallType == 1 || mCallType == 2) {
            ivChange = findViewById(R.id.ivChange);
            if (mCallType == 1) {
                ivChange.setImageResource(R.mipmap.call_change_to_video);
            }
            ivChange.setVisibility(View.VISIBLE);
            ivChange.setOnClickListener(v -> {
                toggleCallType();
                // ?????????????????????????????????
                sendToggleCallType();
            });
        }
        mFrameLayout = (FrameLayout) findViewById(R.id.jitsi_view);
        mJitsiMeetView = new JitsiMeetView(this);
        mFrameLayout.addView(mJitsiMeetView);

        mFloatingView = (ImageView) findViewById(R.id.open_floating);

        mRecordLL = (LinearLayout) findViewById(R.id.record_ll);
        mRecordIv = (ImageView) findViewById(R.id.record_iv);
        mRecordTv = (TextView) findViewById(R.id.record_tv);
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {// 5.0??????????????????root????????????
            Intent intent = new Intent(this, RecordService.class);
            bindService(intent, connection, BIND_AUTO_CREATE);
            mRecordLL.setVisibility(View.VISIBLE);
        }
        // TODO ????????????????????????
        mRecordLL.setVisibility(View.GONE);

        // ??????????????????
        JitsiMeetConferenceOptions.Builder options = new JitsiMeetConferenceOptions.Builder()
                .setWelcomePageEnabled(false);
//TODO        mJitsiMeetView.setPictureInPictureEnabled(false);
        if (mCallType == 1 || mCallType == 3) {
            options.setVideoMuted(true);
        }
        try {
            options.setServerURL(new URL(mLocalHost));
        } catch (MalformedURLException e) {
            throw new IllegalStateException("jitsi????????????: " + mLocalHost);
        }
        if (mCallType == 3) {// ????????????????????????????????????????????????????????????????????????
            options.setRoom("audio" + fromUserId);
        } else {
            options.setRoom(fromUserId);
        }
        loadJwt(options);
        // ????????????
        mJitsiMeetView.join(options.build());
    }

    private void sendToggleCallType() {
        ChatMessage chatMessage = new ChatMessage();
        chatMessage.setType(XmppMessage.TYPE_CHANGE_VIDEO_ENABLE);
        // mCallType????????????????????????
        // content???1??????????????????????????????0???????????????????????????
        if (mCallType == 1) {
            chatMessage.setContent(String.valueOf(0));
        } else if (mCallType == 2) {
            chatMessage.setContent(String.valueOf(1));
        } else {
            FLYReporter.unreachable();
            return;
        }

        chatMessage.setFromUserId(coreManager.getSelf().getUserId());
        chatMessage.setFromUserName(coreManager.getSelf().getNickName());
        chatMessage.setToUserId(toUserId);
        chatMessage.setTimeSend(TimeUtils.sk_time_current_time());
        chatMessage.setPacketId(UUID.randomUUID().toString().replaceAll("-", ""));
        coreManager.sendChatMessage(toUserId, chatMessage);
    }

    private void toggleCallType() {
        toggleCallType(mCallType == 1);
    }

    /**
     * @param videoEnable ????????????????????????????????????????????????true??????????????????????????????false???????????????????????????
     */
    private void toggleCallType(boolean videoEnable) {
        if (videoEnable) {
            mCallType = 2;
            mJitsiMeetView.setVideoEnable();
            ivChange.setImageResource(R.mipmap.call_change_to_voice);
        } else {
            mCallType = 1;
            mJitsiMeetView.setVideoMuted();
            ivChange.setImageResource(R.mipmap.call_change_to_video);
        }
    }

    @SuppressWarnings("unchecked")
    private void loadJwt(JitsiMeetConferenceOptions.Builder options) {
        try {
            Map<String, String> user = new HashMap<>();
            user.put("avatar", AvatarHelper.getAvatarUrl(coreManager.getSelf().getUserId(), false));
            user.put("name", coreManager.getSelf().getNickName());
            Map<String, Object> context = new HashMap<>();
            context.put("user", user);
            Map<String, Object> payload = new HashMap<>();
            payload.put("context", context);
            String jwt = Jwts.builder().addClaims(payload)
                    .compact();
            options.setToken(jwt);
        } catch (Exception e) {
            Log.e(TAG, "loadJwt: ????????????????????????", e);
        }
    }

    private void initEvent() {
        ImageView iv = findViewById(R.id.ysq_iv);
        Friend friend = FriendDao.getInstance().getFriend(coreManager.getSelf().getUserId(), fromUserId);
        if (friend != null && friend.getRoomFlag() != 0) {
            iv.setVisibility(View.VISIBLE);
            // ???????????????????????????????????????
            iv.setOnClickListener(v -> {
                JitsiInviteActivity.start(this, mCallType, fromUserId);
            });
        }

        mJitsiMeetView.setListener(new JitsiMeetViewListener() {

            @Override
            public void onConferenceWillJoin(Map<String, Object> map) {
                Log.e("jitsi", "??????????????????");
            }

            @Override
            public void onConferenceJoined(Map<String, Object> map) {
                Log.e(TAG, "??????????????????????????????????????????????????????");
                // ?????????runOnUiThread??????onConferenceWillJoin???????????????????????????????????????????????????????????????
                runOnUiThread(new Runnable() {
                    @Override
                    public void run() {
                        mFloatingView.setVisibility(View.VISIBLE);
                    }
                });
                // ?????????????????????????????????
                startTime = System.currentTimeMillis();
                // ????????????
                mCountDownTimer.start();
            }

            @Override
            public void onConferenceTerminated(Map<String, Object> map) {
                Log.e(TAG, "5");
                // ??????????????????
                if (!isApi21HangUp) {
                    stopTime = System.currentTimeMillis();
                    overCall((int) (stopTime - startTime) / 1000);
                }

                Log.e(TAG, "6");
                Jitsi_connecting_second.this.sendBroadcast(new Intent(CallConstants.CLOSE_FLOATING));
                finish();
            }
        });

        mFloatingView.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (AppUtils.checkAlertWindowsPermission(Jitsi_connecting_second.this)) { // ????????????????????????
                    // nonRoot = false??? ??????activity???task???????????????activity????????????activity????????????????????????
                    // nonRoot = true ??? ?????????????????????
                    // ????????????????????????task??????activity????????????????????????????????????home???
                    moveTaskToBack(true);
                    // ???????????????
                    Intent intent = new Intent(getApplicationContext(), JitsiFloatService.class);
                    startService(intent);
                } else { // ????????????????????????
                    SelectionFrame selectionFrame = new SelectionFrame(Jitsi_connecting_second.this);
                    selectionFrame.setSomething(null, getString(R.string.av_no_float), new SelectionFrame.OnSelectionFrameClickListener() {
                        @Override
                        public void cancelClick() {
                            hideBottomUIMenu();
                        }

                        @Override
                        public void confirmClick() {
                            PermissionUtil.startApplicationDetailsSettings(Jitsi_connecting_second.this, 0x01);
                            hideBottomUIMenu();
                        }
                    });
                    selectionFrame.show();
                }
            }
        });

        mRecordLL.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
/*
                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
                    if (recordService.isRunning()) {
                        if (recordService.stopRecord()) {
                            mRecordIv.setImageResource(R.drawable.recording);
                            mRecordTv.setText(getString(R.string.screen_record));
                            saveScreenRecordFile();// ?????????????????????????????????
                        }
                    } else {
                        // ??????????????????
                        MediaProjectionManager projectionManager = (MediaProjectionManager) getSystemService(MEDIA_PROJECTION_SERVICE);
                        if (projectionManager != null) {
                            Intent captureIntent = projectionManager.createScreenCaptureIntent();
                            startActivityForResult(captureIntent, RECORD_REQUEST_CODE);
                        }
                    }
                }
*/
            }
        });
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (requestCode == RECORD_REQUEST_CODE && resultCode == RESULT_OK) {
/*
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
                MediaProjectionManager projectionManager = (MediaProjectionManager) getSystemService(MEDIA_PROJECTION_SERVICE);
                if (projectionManager != null) {
                    mediaProjection = projectionManager.getMediaProjection(resultCode, data);
                    recordService.setMediaProject(mediaProjection);
                    // ????????????
                    recordService.startRecord();

                    mRecordIv.setImageResource(R.drawable.stoped);
                    mRecordTv.setText(getString(R.string.stop));
                }
            }
*/
        } else {
            JitsiMeetActivityDelegate.onActivityResult(
                    this, requestCode, resultCode, data);
        }
    }

    public void sendCallingMessage() {
        isEndCallOpposite = true;

        ChatMessage chatMessage = new ChatMessage();
        chatMessage.setType(XmppMessage.TYPE_IN_CALLING);

        chatMessage.setFromUserId(coreManager.getSelf().getUserId());
        chatMessage.setFromUserName(coreManager.getSelf().getNickName());
        chatMessage.setToUserId(toUserId);
        chatMessage.setTimeSend(TimeUtils.sk_time_current_time());
        chatMessage.setPacketId(UUID.randomUUID().toString().replaceAll("-", ""));
        coreManager.sendChatMessage(toUserId, chatMessage);

        mCallingCountDownTimer.start();// ??????????????????
    }

    @Subscribe(threadMode = ThreadMode.MainThread)
    public void helloEventBus(final EventNotifyByTag message) {
        if (message.tag.equals(EventNotifyByTag.Interrupt)) {
            sendBroadcast(new Intent(CallConstants.CLOSE_FLOATING));
            leaveJitsi();
        }
    }

    @Subscribe(threadMode = ThreadMode.MainThread)
    public void helloEventBus(final MessageCallTypeChange message) {
        if (message.chatMessage.getType() == XmppMessage.TYPE_CHANGE_VIDEO_ENABLE) {
            if (message.chatMessage.getFromUserId().equals(toUserId)) {
                toggleCallType(TextUtils.equals(message.chatMessage.getContent(), "1"));
            }
        }
    }

    @Subscribe(threadMode = ThreadMode.MainThread)
    public void helloEventBus(final MessageCallingEvent message) {
        if (message.chatMessage.getType() == XmppMessage.TYPE_IN_CALLING) {
            if (message.chatMessage.getFromUserId().equals(toUserId)) {
                isOldVersion = false;
                // ?????? "?????????" ????????????????????????????????????????????????????????????
                Log.e(TAG, "MessageCallingEvent-->" + TimeUtils.sk_time_current_time());
                mPingReceiveFailCount = 0;// ???count??????0
                isEndCallOpposite = false;
            }
        }
    }

    // ????????????
    @Subscribe(threadMode = ThreadMode.MainThread)
    public void helloEventBus(final MessageHangUpPhone message) {
        if (message.chatMessage.getFromUserId().equals(fromUserId)
                || message.chatMessage.getFromUserId().equals(toUserId)) {// ?????????????????????????????? ???????????????
            if (Build.VERSION.SDK_INT == Build.VERSION_CODES.LOLLIPOP) {
                isApi21HangUp = true;
                TipDialog tip = new TipDialog(Jitsi_connecting_second.this);
                tip.setmConfirmOnClickListener(getString(R.string.av_hand_hang), new TipDialog.ConfirmOnClickListener() {
                    @Override
                    public void confirm() {
                        hideBottomUIMenu();
                    }
                });
                tip.show();
                return;
            }

            // ???????????????
            sendBroadcast(new Intent(CallConstants.CLOSE_FLOATING));
            leaveJitsi();
        }
    }

    /*******************************************
     * Method
     ******************************************/
    // ???????????????XMPP??????
    private void overCall(int time) {
        if (mCallType == 1) {
            EventBus.getDefault().post(new MessageEventCancelOrHangUp(104, toUserId,
                    getString(R.string.sip_canceled) + getString(R.string.voice_chat),
                    time));
        } else if (mCallType == 2) {
            EventBus.getDefault().post(new MessageEventCancelOrHangUp(114, toUserId,
                    getString(R.string.sip_canceled) + getString(R.string.voice_chat),
                    time));
        } else if (mCallType == 5) {
            EventBus.getDefault().post(new MessageEventCancelOrHangUp(134, toUserId,
                    getString(R.string.sip_canceled) + getString(R.string.name_talk),
                    time));
        }
    }

    private String formatTime() {
        Date date = new Date(new Date().getTime() - startTime);
        return mSimpleDateFormat.format(date);
    }

    // ??????????????????
    private void hideBottomUIMenu() {
        View v = this.getWindow().getDecorView();
        v.setSystemUiVisibility(View.GONE);
    }

    /*******************************************
     * ??????????????????????????????
     ******************************************/
    public void saveScreenRecordFile() {
        // ??????????????????
        String imNewestScreenRecord = PreferenceUtils.getString(getApplicationContext(), "IMScreenRecord");
        File file = new File(imNewestScreenRecord);
        if (file.exists() && file.getName().trim().toLowerCase().endsWith(".mp4")) {
            VideoFile videoFile = new VideoFile();
            videoFile.setCreateTime(TimeUtils.f_long_2_str(getScreenRecordFileCreateTime(file.getName())));
            videoFile.setFileLength(getScreenRecordFileTimeLen(file.getPath()));
            videoFile.setFileSize(file.length());
            videoFile.setFilePath(file.getPath());
            videoFile.setOwnerId(coreManager.getSelf().getUserId());
            VideoFileDao.getInstance().addVideoFile(videoFile);
        }
    }

    private long getScreenRecordFileCreateTime(String srf) {
        int dot = srf.lastIndexOf('.');
        return Long.parseLong(srf.substring(0, dot));
    }

    private long getScreenRecordFileTimeLen(String srf) {
        long duration;
        MediaPlayer player = new MediaPlayer();
        try {
            player.setDataSource(srf);
            player.prepare();
            duration = player.getDuration() / 1000;
        } catch (Exception e) {
            duration = 10;
            e.printStackTrace();
        }
        player.release();
        return duration;
    }

    /*******************************************
     * ????????????
     ******************************************/
    @Override
    public void onBackPressed() {
        // ?????????????????????????????????????????????finish???
    }

    @Override
    public void onNewIntent(Intent intent) {
        super.onNewIntent(intent);
        JitsiMeetActivityDelegate.onNewIntent(intent);
    }

    @Override
    protected void onResume() {
        super.onResume();
        if (JitsistateMachine.isFloating) {
            sendBroadcast(new Intent(CallConstants.CLOSE_FLOATING));
        }
    }

    @Override
    protected void onPause() {
        super.onPause();
    }

    @Override
    protected void onDestroy() {
        // ??????????????????
        JitsiMeetActivityDelegate.onHostPause(this);
        JitsistateMachine.reset();

        mCallingCountDownTimer.cancel();

        JitsiMeetActivityDelegate.onBackPressed();
        mJitsiMeetView.dispose();
        JitsiMeetActivityDelegate.onHostDestroy(this);

        EventBus.getDefault().unregister(this);

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
            if (connection != null) {
                // 1.????????????????????????????????????????????????????????????????????????????????????????????????????????????????????????????????????
                // 2.??????????????????
                if (recordService.isRunning()) {
                    recordService.stopRecord();
                    saveScreenRecordFile();
                }
                unbindService(connection);
            }
        }

        if (mCountDownTimer != null) {
            mCountDownTimer.cancel();
        }
        Log.e(TAG, "onDestory");
        super.onDestroy();
    }

    @Override
    public void onRequestPermissionsResult(
            final int requestCode,
            final String[] permissions,
            final int[] grantResults) {
        JitsiMeetActivityDelegate.onRequestPermissionsResult(requestCode, permissions, grantResults);
    }

    @Override
    public void requestPermissions(String[] permissions, int requestCode, PermissionListener listener) {
        JitsiMeetActivityDelegate.requestPermissions(this, permissions, requestCode, listener);
    }

    @Override
    public void onPointerCaptureChanged(boolean hasCapture) {
        Log.d(TAG, "onPointerCaptureChanged() called with: hasCapture = [" + hasCapture + "]");
    }
}
