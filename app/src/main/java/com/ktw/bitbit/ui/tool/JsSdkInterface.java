package com.ktw.bitbit.ui.tool;

import android.content.Context;
import android.text.TextUtils;
import android.util.Log;
import android.view.View;
import android.webkit.JavascriptInterface;

import androidx.annotation.Nullable;

import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.JSONObject;
import com.ktw.bitbit.FLYReporter;
import com.ktw.bitbit.audio.RecordManager;
import com.ktw.bitbit.audio.RecordStateListener;
import com.ktw.bitbit.audio1.VoiceManager;
import com.ktw.bitbit.bean.SKShareBean;
import com.ktw.bitbit.downloader.DownloadListener;
import com.ktw.bitbit.downloader.Downloader;
import com.ktw.bitbit.downloader.FailReason;

import java.net.URL;

public class JsSdkInterface {
    private static final String TAG = "JsSdkInterface";
    private Context ctx;
    private RecordManager recordManager;
    private String file;
    @Nullable
    private VoiceManager.VoicePlayListener playListener;
    @Nullable
    private Listener listener;
    private String shareParams;

    public JsSdkInterface(Context ctx, @Nullable Listener listener) {
        Log.i(TAG, "JsSdkInterface() called with: ctx = [" + ctx + "]");
        this.ctx = ctx;
        this.listener = listener;
    }

    @JavascriptInterface
    public String getShareParams() {
        return shareParams;
    }

    public void setShareParams(String shareParams) {
        this.shareParams = shareParams;
    }

    @JavascriptInterface
    public void chooseSKPayInApp(String param) {
        Log.i(TAG, "chooseSKPayInApp() called with: param = [" + param + "]");
        // {"appId":"sk3e90a49caad54052","prepayId":"5cdd15ed57dddb2c98d338e9","sign":"5FD83E9970DB1CE81692293DFAE23026"}
        if (!TextUtils.isEmpty(param)) {
            JSONObject jsonObject = JSONObject.parseObject(param);
            String appId = jsonObject.getString("appId");
            String prepayId = jsonObject.getString("prepayId");
            String sign = jsonObject.getString("sign");
            if (listener != null) {
                listener.onChooseSKPayInApp(appId, prepayId, sign);
            }
        }
    }

    @JavascriptInterface
    public void updateShareData(String param) {
        Log.i(TAG, "updateShareData() called with: param = [" + param + "]");
        try {
            // ?????????????????????????????????json?????????string?????????????????????????????????????????????????????????\
            // ?????????????????????json???????????????????????????json????????????????????????
            String content = JSON.parseObject(param, String.class);
            SKShareBean bean = JSON.parseObject(content, SKShareBean.class);
            if (bean == null
                    || TextUtils.isEmpty(bean.getUrl())
                    || TextUtils.isEmpty(bean.getTitle())
                    || TextUtils.isEmpty(bean.getImageUrl())
            ) {
                FLYReporter.post("updateShareData()????????????, param=" + param);
                throw new IllegalStateException();
            }
            if (listener != null) {
                listener.onUpdateShareData(param);
            }
        } catch (Exception ignored) {
        }
    }

    @JavascriptInterface
    public void startRecord() {
        Log.i(TAG, "startRecord() called");
        getRecordManager().startRecord();
    }

    @JavascriptInterface
    public String stopRecord() {
        Log.i(TAG, "stopRecord() called");
        return getRecordManager().stop();
    }

    @JavascriptInterface
    public void playVoice(String url) {
        Log.i(TAG, "playVoice() called");
        try {
            // ????????????url???????????????????????????????????????????????????????????????
            new URL(url);
            Downloader.getInstance().addDownload(url, new MyDownloadListener());
        } catch (Exception ignored) {
            file = url;
        }
        playVoiceFile();
    }

    private void playVoiceFile() {
        if (!TextUtils.isEmpty(file)) {
            if (VoiceManager.instance().getState() == VoiceManager.STATE_PAUSE) {
                VoiceManager.instance().stop();
            }
            if (playListener == null) {
                playListener = new MyPlayListener();
                VoiceManager.instance().addVoicePlayListener(playListener);
            }
            VoiceManager.instance().play(file);
        }
    }

    @JavascriptInterface
    public void pauseVoice() {
        Log.i(TAG, "pauseVoice() called");
        if (!TextUtils.isEmpty(file)) {
            if (VoiceManager.instance().getState() == VoiceManager.STATE_PLAY) {
                VoiceManager.instance().pause();
            } else if (VoiceManager.instance().getState() == VoiceManager.STATE_PAUSE) {
                VoiceManager.instance().play(file);
            }
        }
    }

    @JavascriptInterface
    public void stopVoice() {
        Log.i(TAG, "stopVoice() called");
        if (!TextUtils.isEmpty(file)) {
            if (VoiceManager.instance().getState() == VoiceManager.STATE_PLAY
                    || VoiceManager.instance().getState() == VoiceManager.STATE_PAUSE) {
                VoiceManager.instance().stop();
            }
        }
    }

    public RecordManager getRecordManager() {
        if (recordManager == null) {
            synchronized (this) {
                if (recordManager == null) {
                    recordManager = RecordManager.getInstance();
                    recordManager.setVoiceVolumeListener(new MyRecordStatusListener());
                }
            }
        }
        return recordManager;
    }

    public void release() {
        if (recordManager != null && recordManager.isRunning()) {
            recordManager.cancel();
        }
        if (playListener != null) {
            VoiceManager.instance().removeVoicePlayListener(playListener);
        }
    }

    public interface Listener {
        void onFinishPlay(String path);

        void onUpdateShareData(String shareBeanContent);

        void onChooseSKPayInApp(String appId, String prepayId, String token);
    }

    private class MyRecordStatusListener implements RecordStateListener {

        @Override
        public void onRecordStarting() {

        }

        @Override
        public void onRecordStart() {

        }

        @Override
        public void onRecordFinish(String file) {
            Log.i(TAG, "onRecordFinish() called with: file = [" + file + "]");
            JsSdkInterface.this.file = file;
        }

        @Override
        public void onRecordCancel() {

        }

        @Override
        public void onRecordVolumeChange(int v) {

        }

        @Override
        public void onRecordTimeChange(int seconds) {

        }

        @Override
        public void onRecordError() {

        }

        @Override
        public void onRecordTooShoot() {

        }
    }

    private class MyDownloadListener implements DownloadListener {
        @Override
        public void onStarted(String uri, View view) {

        }

        @Override
        public void onFailed(String uri, FailReason failReason, View view) {

        }

        @Override
        public void onComplete(String uri, String filePath, View view) {
            file = filePath;
            playVoiceFile();
        }

        @Override
        public void onCancelled(String uri, View view) {

        }
    }

    private class MyPlayListener implements VoiceManager.VoicePlayListener {
        @Override
        public void onFinishPlay(String path) {
            Log.i(TAG, "onFinishPlay() called with: path = [" + path + "]");
            if (listener != null) {
                listener.onFinishPlay(path);
            }
        }

        @Override
        public void onStopPlay(String path) {
            Log.i(TAG, "onStopPlay() called with: path = [" + path + "]");
        }

        @Override
        public void onErrorPlay() {
            Log.i(TAG, "onErrorPlay: ");
        }
    }
}
