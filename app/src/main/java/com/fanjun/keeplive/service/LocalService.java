package com.fanjun.keeplive.service;

import android.app.Notification;
import android.app.Service;
import android.content.BroadcastReceiver;
import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.ServiceConnection;
import android.media.MediaPlayer;
import android.os.Build;
import android.os.Handler;
import android.os.IBinder;
import android.os.PowerManager;
import android.os.RemoteException;

import androidx.core.content.ContextCompat;

import com.fanjun.keeplive.KeepLive;
import com.fanjun.keeplive.config.NotificationUtils;
import com.fanjun.keeplive.receiver.NotificationClickReceiver;
import com.fanjun.keeplive.receiver.OnepxReceiver;
import com.fanjun.keeplive.utils.ServiceUtils;
import com.ktw.bitbit.R;

public final class LocalService extends Service {
    private OnepxReceiver mOnepxReceiver;
    private ScreenStateReceiver screenStateReceiver;
    private boolean isPause = true;//ζ§εΆζε
    private MediaPlayer mediaPlayer;
    private MyBilder mBilder;
    private Handler handler;
    private boolean mIsBoundRemoteService;
    private ServiceConnection connection = new ServiceConnection() {

        @Override
        public void onServiceDisconnected(ComponentName name) {
            if (ServiceUtils.isServiceRunning(getApplicationContext(), "com.fanjun.keeplive.service.LocalService")) {
                Intent remoteService = new Intent(LocalService.this,
                        RemoteService.class);
                ContextCompat.startForegroundService(LocalService.this, remoteService);
                Intent intent = new Intent(LocalService.this, RemoteService.class);
                mIsBoundRemoteService = LocalService.this.bindService(intent, connection,
                        Context.BIND_ABOVE_CLIENT);
            }
            PowerManager pm = (PowerManager) getApplicationContext().getSystemService(Context.POWER_SERVICE);
            boolean isScreenOn = pm.isScreenOn();
            if (isScreenOn) {
                sendBroadcast(new Intent("_ACTION_SCREEN_ON"));
            } else {
                sendBroadcast(new Intent("_ACTION_SCREEN_OFF"));
            }
        }

        @Override
        public void onServiceConnected(ComponentName name, IBinder service) {
            try {
                if (mBilder != null && KeepLive.foregroundNotification != null) {
                    GuardAidl guardAidl = GuardAidl.Stub.asInterface(service);
                    guardAidl.wakeUp(KeepLive.foregroundNotification.getTitle(), KeepLive.foregroundNotification.getDescription(), KeepLive.foregroundNotification.getIconRes());
                }
            } catch (RemoteException e) {
                e.printStackTrace();
            }
        }
    };

    @Override
    public void onCreate() {
        super.onCreate();
        if (mBilder == null) {
            mBilder = new MyBilder();
        }
        PowerManager pm = (PowerManager) getApplicationContext().getSystemService(Context.POWER_SERVICE);
        isPause = pm.isScreenOn();
        if (handler == null) {
            handler = new Handler();
        }
    }

    @Override
    public IBinder onBind(Intent intent) {
        return mBilder;
    }

    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {
        if (KeepLive.useSilenceMusice) {
            //ζ­ζΎζ ε£°ι³δΉ
            if (mediaPlayer == null) {
                mediaPlayer = MediaPlayer.create(this, R.raw.novioce);
                if (mediaPlayer != null) {
                    mediaPlayer.setVolume(0f, 0f);
                    mediaPlayer.setOnCompletionListener(new MediaPlayer.OnCompletionListener() {
                        @Override
                        public void onCompletion(MediaPlayer mediaPlayer) {
                            if (!isPause) {
                                if (KeepLive.runMode == KeepLive.RunMode.ROGUE) {
                                    play();
                                } else {
                                    if (handler != null) {
                                        handler.postDelayed(new Runnable() {
                                            @Override
                                            public void run() {
                                                play();
                                            }
                                        }, 5000);
                                    }
                                }
                            }
                        }
                    });
                    mediaPlayer.setOnErrorListener(new MediaPlayer.OnErrorListener() {
                        @Override
                        public boolean onError(MediaPlayer mp, int what, int extra) {
                            return false;
                        }
                    });
                    play();
                }
            }
        }
        //εη΄ δΏζ΄»
        if (mOnepxReceiver == null) {
            mOnepxReceiver = new OnepxReceiver();
        }
        IntentFilter intentFilter = new IntentFilter();
        intentFilter.addAction("android.intent.action.SCREEN_OFF");
        intentFilter.addAction("android.intent.action.SCREEN_ON");
        registerReceiver(mOnepxReceiver, intentFilter);
        //ε±εΉηΉδΊ?ηΆζηε¬οΌη¨δΊεη¬ζ§εΆι³δΉζ­ζΎ
        if (screenStateReceiver == null) {
            screenStateReceiver = new ScreenStateReceiver();
        }
        IntentFilter intentFilter2 = new IntentFilter();
        intentFilter2.addAction("_ACTION_SCREEN_OFF");
        intentFilter2.addAction("_ACTION_SCREEN_ON");
        registerReceiver(screenStateReceiver, intentFilter2);
        //ε―η¨εε°ζε‘οΌζεδΌεηΊ§
        if (KeepLive.foregroundNotification != null) {
            Intent intent2 = new Intent(getApplicationContext(), NotificationClickReceiver.class);
            intent2.setAction(NotificationClickReceiver.CLICK_NOTIFICATION);
            Notification notification = NotificationUtils.createNotification(this, KeepLive.foregroundNotification.getTitle(), KeepLive.foregroundNotification.getDescription(), KeepLive.foregroundNotification.getIconRes(), intent2);
            startForeground(13691, notification);
        }
        //η»ε?ε?ζ€θΏη¨
        try {
            Intent intent3 = new Intent(this, RemoteService.class);
            mIsBoundRemoteService = this.bindService(intent3, connection, Context.BIND_ABOVE_CLIENT);
        } catch (Exception e) {
        }
        //ιθζε‘ιη₯
        try {
            if (Build.VERSION.SDK_INT < 25) {
                startService(new Intent(this, HideForegroundService.class));
            }
        } catch (Exception e) {
        }
        if (KeepLive.keepLiveService != null) {
            KeepLive.keepLiveService.onWorking();
        }
        return START_STICKY;
    }

    private void play() {
        if (KeepLive.useSilenceMusice) {
            if (mediaPlayer != null && !mediaPlayer.isPlaying()) {
                mediaPlayer.start();
            }
        }
    }

    private void pause() {
        if (KeepLive.useSilenceMusice) {
            if (mediaPlayer != null && mediaPlayer.isPlaying()) {
                mediaPlayer.pause();
            }
        }
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
        if (connection != null) {
            try {
                if (mIsBoundRemoteService) {
                    unbindService(connection);
                }
            } catch (Exception e) {
            }
        }
        try {
            unregisterReceiver(mOnepxReceiver);
            unregisterReceiver(screenStateReceiver);
        } catch (Exception e) {
        }
        if (KeepLive.keepLiveService != null) {
            KeepLive.keepLiveService.onStop();
        }
    }

    private class ScreenStateReceiver extends BroadcastReceiver {
        @Override
        public void onReceive(final Context context, Intent intent) {
            if (intent.getAction().equals("_ACTION_SCREEN_OFF")) {
                isPause = false;
                play();
            } else if (intent.getAction().equals("_ACTION_SCREEN_ON")) {
                isPause = true;
                pause();
            }
        }
    }

    private final class MyBilder extends GuardAidl.Stub {

        @Override
        public void wakeUp(String title, String discription, int iconRes) throws RemoteException {

        }
    }
}
