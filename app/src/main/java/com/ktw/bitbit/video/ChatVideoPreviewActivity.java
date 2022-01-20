package com.ktw.bitbit.video;

import android.content.ContentResolver;
import android.content.ContentValues;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.provider.MediaStore;
import android.text.TextUtils;
import android.view.View;
import android.view.WindowManager;
import android.widget.ImageView;
import android.widget.ProgressBar;
import android.widget.RelativeLayout;
import android.widget.SeekBar;
import android.widget.TextView;

import com.danikula.videocache.HttpProxyCacheServer;
import com.ktw.bitbit.FLYAppConstant;
import com.ktw.bitbit.FLYApplication;
import com.ktw.bitbit.R;
import com.ktw.bitbit.helper.ImageLoadHelper;
import com.ktw.bitbit.ui.base.BaseActivity;
import com.ktw.bitbit.util.TimeUtils;
import com.ktw.bitbit.util.ToastUtil;
import com.ktw.bitbit.util.downloadTask;
import com.ktw.bitbit.view.SavaVideoDialog;
import com.ktw.bitbit.view.chatHolder.MessageEventClickFire;

import java.io.File;
import java.util.Timer;
import java.util.TimerTask;

import de.greenrobot.event.EventBus;
import fm.jiecao.jcvideoplayer_lib.JCVideoPlayer;
import fm.jiecao.jcvideoplayer_lib.JCVideoViewbyXuan;
import fm.jiecao.jcvideoplayer_lib.OnJcvdListener;
import fm.jiecao.jcvideoplayer_lib.VideotillManager;

public class ChatVideoPreviewActivity extends BaseActivity implements View.OnClickListener, View.OnLongClickListener {
    private static final int ONECE_TIME = 20;
    private String mVideoPath, mDelPackedID;
    private JCVideoViewbyXuan mVideoView;
    private RelativeLayout rlContol;
    private ImageView ivThumb, ivStart;
    private TextView tvCurrt, tvTotal;
    private SeekBar mSeekBar;
    private Timer DISMISS_CONTROL_VIEW_TIMER;
    private Timer SEEKBAR_VIEW_TIMER;
    private DismissControlViewTimerTask mDismissControlViewTimerTask;
    private ProgressTimerTask mProgressTask;
    private long mCurTimer; // 毫秒
    private long mDuration;  // 总时长

    Handler handler = new Handler(new Handler.Callback() {
        @Override
        public boolean handleMessage(Message msg) {
            if (msg.what == 1) {
                // tvCurrt.setText("00:" + String.format("%02d", mCurTimer / 1000));
                tvCurrt.setText(TimeUtils.timeParse(mCurTimer));
                int pro = (int) (mCurTimer / (float) mDuration * 100);
                mSeekBar.setProgress(pro);
            } else if (msg.what == 2) {
                rlContol.setVisibility(View.INVISIBLE);
            }
            return false;
        }
    });
    SeekBar.OnSeekBarChangeListener seekbarListener = new SeekBar.OnSeekBarChangeListener() {
        @Override
        public void onProgressChanged(SeekBar seekBar, int progress, boolean fromUser) {
        }

        @Override
        public void onStartTrackingTouch(SeekBar seekBar) {
            cancelDismissControlViewTimer();
            cancelProgressTimer();
        }

        @Override
        public void onStopTrackingTouch(SeekBar seekBar) {
            mCurTimer = (long) (seekBar.getProgress() / 100.0 * mDuration);
            try {
                mVideoView.seekTo((int) mCurTimer);
            } catch (Exception e) {
                // 原因不明，但至少拖动失败不能崩溃，
                ToastUtil.showToast(mContext, R.string.tip_seek_failed);
            }
            // tvCurrt.setText("00:" + String.format("%02d", mCurTimer / 1000));
            tvCurrt.setText(TimeUtils.timeParse(mCurTimer));
            if (mVideoView.isPlaying()) {
                startDismissControlViewTimer();
                startProgressTimer();
            }

        }
    };

    private String thumb;
    private ProgressBar mLoadBar;
    OnJcvdListener jcvdListener = new OnJcvdListener() {
        @Override
        public void onPrepared() {
            mDuration = mVideoView.getDuration();

            startProgressTimer();
            startDismissControlViewTimer();
            // tvTotal.setText("00:" + String.format("%02d", mDuration / 1000));
            tvTotal.setText(TimeUtils.timeParse(mDuration));
            if (!TextUtils.isEmpty(thumb)) {
                ivThumb.postDelayed(() -> {
                    ivThumb.setVisibility(View.GONE);
                    mLoadBar.setVisibility(View.GONE);
                    ivStart.setImageResource(fm.jiecao.jcvideoplayer_lib.R.drawable.jc_click_pause_selector);
                }, 300);
            } else {
                ivStart.setImageResource(fm.jiecao.jcvideoplayer_lib.R.drawable.jc_click_pause_selector);
            }
        }

        @Override
        public void onCompletion() {
            mCurTimer = 0;
            ivStart.setImageResource(fm.jiecao.jcvideoplayer_lib.R.drawable.jc_click_play_selector);
            cancelDismissControlViewTimer();
            cancelProgressTimer();
            rlContol.setVisibility(View.VISIBLE);
            ivThumb.setVisibility(View.VISIBLE);
        }

        @Override
        public void onError() {

        }

        @Override
        public void onPause() {
            ivStart.setImageResource(fm.jiecao.jcvideoplayer_lib.R.drawable.jc_click_play_selector);
            cancelDismissControlViewTimer();
            cancelProgressTimer();
            rlContol.setVisibility(View.VISIBLE);
        }

        @Override
        public void onReset() {

        }
    };
    private SavaVideoDialog savaVideoDialog;
    SavaVideoDialog.OnSavaVideoDialogClickListener onSavaVideoDialogClickListener = new SavaVideoDialog.OnSavaVideoDialogClickListener() {
        @Override
        public void tv1Click() {
            File file = new File(mVideoPath);
            ContentResolver localResolver = ChatVideoPreviewActivity.this.getContentResolver();
            ContentValues localContentValue = getVideoContentValues(file, System.currentTimeMillis());
            localResolver.insert(MediaStore.Video.Media.EXTERNAL_CONTENT_URI, localContentValue);
            ToastUtil.showToast(ChatVideoPreviewActivity.this, R.string.tip_video_save_success);
            if (mVideoPath.startsWith("http")) {
                String path = "/storage/emulated/0/Android/data/com.ktw.fly/files/Movies" +
                        mVideoPath.substring(mVideoPath.length() - 6, mVideoPath.length() - 4) + ".mp4";
                File f = new File(path);
                if (!f.exists()) {
                    new downloadTask(mVideoPath, 2, path).start();
                    ContentResolver localContentResolver = ChatVideoPreviewActivity.this.getContentResolver();
                    ContentValues localContentValues = getVideoContentValues(f, System.currentTimeMillis());
                    localContentResolver.insert(MediaStore.Video.Media.EXTERNAL_CONTENT_URI, localContentValues);
                    ToastUtil.showToast(ChatVideoPreviewActivity.this, R.string.tip_video_save_success);
                } else
                    ToastUtil.showToast(ChatVideoPreviewActivity.this, R.string.tip_video_exists);

            }
            savaVideoDialog.dismiss();
        }
    };

    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_video_preview_chat);
        getSupportActionBar().hide();

        ivThumb = findViewById(R.id.iv_thumb);
        mLoadBar = findViewById(R.id.loading);

        if (getIntent() != null) {
            mVideoPath = getIntent().getStringExtra(FLYAppConstant.EXTRA_VIDEO_FILE_PATH);
            // 这个是阅后即焚消息的 packedId
            mDelPackedID = getIntent().getStringExtra("DEL_PACKEDID");
            if (!TextUtils.isEmpty(mDelPackedID)) {
                getWindow().setFlags(WindowManager.LayoutParams.FLAG_SECURE, WindowManager.LayoutParams.FLAG_SECURE);
            }

            thumb = getIntent().getStringExtra(FLYAppConstant.EXTRA_VIDEO_FILE_THUMB);
            if (!TextUtils.isEmpty(thumb)) {
                ImageLoadHelper.showImage(this, thumb, ivThumb);
                mLoadBar.setVisibility(View.VISIBLE);
            }
        }

        initView();
    }

    public void doBack() {
        VideotillManager.instance().releaseVideo();
        cancelProgressTimer();
        cancelDismissControlViewTimer();
        if (!TextUtils.isEmpty(mDelPackedID)) {
            // 发送广播去更新聊天界面，移除该message
            EventBus.getDefault().post(new MessageEventClickFire("delete", mDelPackedID));
        }
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        doBack();
    }

    private void initView() {
        mVideoView = findViewById(R.id.x_video);

        ivStart = findViewById(R.id.iv_start);
        rlContol = findViewById(R.id.rl_control);

        tvTotal = findViewById(R.id.total);
        tvCurrt = findViewById(R.id.current);
        mSeekBar = findViewById(R.id.bottom_seek_progress);

        mVideoView.addOnJcvdListener(jcvdListener);
        mSeekBar.setOnSeekBarChangeListener(seekbarListener);

        mVideoView.setOnClickListener(this);
        ivStart.setOnClickListener(this);
        findViewById(R.id.back_tiny).setOnClickListener(this);

        rlContol.setVisibility(View.INVISIBLE);
        rlContol.setOnLongClickListener(this::onLongClick);
        mVideoView.loop = false;

        if (!TextUtils.isEmpty(thumb)) {
            HttpProxyCacheServer proxy = FLYApplication.getProxy(this);
            mVideoView.play(proxy.getProxyUrl(mVideoPath));
        } else {
            mVideoView.play(mVideoPath);
        }

    }

    @Override
    public void onClick(View v) {
        if (v.getId() == R.id.iv_start) {
            if (mVideoView.isPlaying()) {
                mVideoView.pause();
            } else if (mVideoView.mCurrState == JCVideoPlayer.CURRENT_STATE_ERROR) {
                // ivStart.setImageResource(fm.jiecao.jcvideoplayer_lib.R.drawable.jc_click_error_selector);
            } else {
                mVideoView.play(mVideoPath);
            }
        } else if (v.getId() == R.id.back_tiny) {
            finish();
        } else {
            if (rlContol.getVisibility() == View.VISIBLE) {
                rlContol.setVisibility(View.INVISIBLE);
                cancelDismissControlViewTimer();
            } else {
                startDismissControlViewTimer();
            }
        }
    }

    public void startDismissControlViewTimer() {
        cancelDismissControlViewTimer();

        rlContol.setVisibility(View.VISIBLE);
        DISMISS_CONTROL_VIEW_TIMER = new Timer();
        mDismissControlViewTimerTask = new DismissControlViewTimerTask();
        DISMISS_CONTROL_VIEW_TIMER.schedule(mDismissControlViewTimerTask, 2500);
    }

    public void cancelDismissControlViewTimer() {
        if (DISMISS_CONTROL_VIEW_TIMER != null) {
            DISMISS_CONTROL_VIEW_TIMER.cancel();
            DISMISS_CONTROL_VIEW_TIMER = null;
        }
        if (mDismissControlViewTimerTask != null) {
            mDismissControlViewTimerTask.cancel();
            mDismissControlViewTimerTask = null;
        }
    }

    public void startProgressTimer() {
        cancelProgressTimer();
        SEEKBAR_VIEW_TIMER = new Timer();
        mProgressTask = new ProgressTimerTask();
        SEEKBAR_VIEW_TIMER.schedule(mProgressTask, 0, ONECE_TIME);
    }

    public void cancelProgressTimer() {
        if (SEEKBAR_VIEW_TIMER != null) {
            SEEKBAR_VIEW_TIMER.cancel();
            SEEKBAR_VIEW_TIMER = null;
        }

        if (mProgressTask != null) {
            mProgressTask.cancel();
            mProgressTask = null;
        }
    }

    public ContentValues getVideoContentValues(File paramFile, long paramLong) {
        ContentValues localContentValues = new ContentValues();
        localContentValues.put("title", paramFile.getName());
        localContentValues.put("_display_name", paramFile.getName());
        localContentValues.put("mime_type", "video/mp4");
        localContentValues.put("datetaken", Long.valueOf(paramLong));
        localContentValues.put("date_modified", Long.valueOf(paramLong));
        localContentValues.put("date_added", Long.valueOf(paramLong));
        localContentValues.put("_data", paramFile.getAbsolutePath());
        localContentValues.put("_size", Long.valueOf(paramFile.length()));
        return localContentValues;
    }

    @Override
    public boolean onLongClick(View v) {
        savaVideoDialog = new SavaVideoDialog(ChatVideoPreviewActivity.this, onSavaVideoDialogClickListener);
        savaVideoDialog.show();
        return false;
    }

    public class ProgressTimerTask extends TimerTask {
        @Override
        public void run() {
            mCurTimer += ONECE_TIME;
            handler.sendEmptyMessage(1);
        }
    }

    public class DismissControlViewTimerTask extends TimerTask {
        @Override
        public void run() {
            handler.sendEmptyMessage(2);
        }
    }
}
