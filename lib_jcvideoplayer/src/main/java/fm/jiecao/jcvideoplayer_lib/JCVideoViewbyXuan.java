package fm.jiecao.jcvideoplayer_lib;

import android.content.Context;
import android.media.AudioManager;
import android.text.TextUtils;
import android.util.AttributeSet;
import android.util.Log;
import android.view.Gravity;
import android.view.ViewGroup;
import android.view.WindowManager;
import android.widget.FrameLayout;


/**
 * Created by xuan on 2018-11-26 12:08:11.
 * <p>
 * 使用基于jcv视频播放器改造而成，用于短视频模块视频播放预览
 */
public class JCVideoViewbyXuan extends FrameLayout implements OnJcvdListener {

    public int mCurrState;
    public boolean loop = true;
    public String mCurrUrl = "";
    public boolean isForceFullScreenPlay;
    protected AudioManager mAudioManager;
    private OnJcvdListener mListener;
    private AudioManager.OnAudioFocusChangeListener onAudioFocusChangeListener = new AudioManager.OnAudioFocusChangeListener() {

        @Override
        public void onAudioFocusChange(int focusChange) {
            if (focusChange == AudioManager.AUDIOFOCUS_LOSS) {
                VideotillManager.instance().releaseVideo();
                JCMediaManager.instance().releaseMediaPlayer();
            } else if (focusChange == AudioManager.AUDIOFOCUS_LOSS_TRANSIENT) {
                try {
                    if (JCMediaManager.instance().mediaPlayer != null && JCMediaManager.instance().mediaPlayer.isPlaying()) {
                        JCMediaManager.instance().mediaPlayer.pause();
                    }
                } catch (Exception e) {
                }
            }
        }
    };

    public JCVideoViewbyXuan(Context context) {
        super(context);
        init(context);
    }

    public JCVideoViewbyXuan(Context context, AttributeSet attrs) {
        super(context, attrs);
        init(context);
    }

    private void init(Context context) {
        mAudioManager = (AudioManager) getContext().getSystemService(Context.AUDIO_SERVICE);
        mAudioManager.requestAudioFocus(onAudioFocusChangeListener, AudioManager.STREAM_MUSIC, AudioManager.AUDIOFOCUS_GAIN_TRANSIENT);
    }

    public void setForceFullScreenPlay(boolean forceFullScreenPlay) {
        isForceFullScreenPlay = forceFullScreenPlay;
    }

    @Override
    protected void onMeasure(int widthMeasureSpec, int heightMeasureSpec) {
        if (isForceFullScreenPlay) {
            int width = getDefaultSize(0, widthMeasureSpec);
            int height = getDefaultSize(0, heightMeasureSpec);
            setMeasuredDimension(width, height);
        } else {
            super.onMeasure(widthMeasureSpec, heightMeasureSpec);
        }
        //  setMeasuredDimension();
    }

    private void prepare() {
        VideotillManager.instance().releaseVideo();

        // 移除 管理器中的 textureView
        JCMediaManager.savedSurfaceTexture = null;
        if (JCMediaManager.textureView != null && JCMediaManager.textureView.getParent() != null) {
            ((ViewGroup) JCMediaManager.textureView.getParent()).removeView(JCMediaManager.textureView);
        }

        // 创建一个新的 textureView
        JCMediaManager.textureView = new JCResizeTextureView(getContext());
        JCMediaManager.textureView.setSurfaceTextureListener(JCMediaManager.instance());

        // 放到容器中
        LayoutParams layoutParams = new LayoutParams(LayoutParams.MATCH_PARENT, LayoutParams.MATCH_PARENT, Gravity.CENTER);
        this.addView(JCMediaManager.textureView, layoutParams);

        // 当播放时才有焦点
        JCUtils.scanForActivity(getContext()).getWindow().addFlags(WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON);
        JCMediaManager.CURRENT_PLAYING_URL = this.mCurrUrl;
        JCMediaManager.CURRENT_PLING_LOOP = loop;
        JCMediaManager.MAP_HEADER_DATA = null;
        JCMediaManager.addOnJcvdListener(this);
        VideotillManager.instance().addVideoPlay(this);

        mCurrState = JCVideoPlayer.CURRENT_STATE_PREPARING;
    }

    /**
     * 播放 或 继续播放
     *
     * @param url
     */
    public void play(String url) {
        Log.e("xuan", "play: " + url + "    state :" + mCurrState);
        if (mCurrState == JCVideoPlayer.CURRENT_STATE_NORMAL) {
            if (TextUtils.isEmpty(url)) {
                return;
            }
            this.mCurrUrl = url;
            prepare(); //   !JCUtils.isWifiConnected(getContext()) 判断网络
        } else if (mCurrState == JCVideoPlayer.CURRENT_STATE_PAUSE) {
            JCMediaManager.instance().mediaPlayer.start(); // 开始
            if (mListener != null) {
                mListener.onPrepared();
            }
            mCurrState = JCVideoPlayer.CURRENT_STATE_PLAYING;
        }
    }

    /**
     * 暂停播放
     */
    public void pause() {
        Log.e("xuan", "pause: " + mCurrState);
        if (mCurrState == JCVideoPlayer.CURRENT_STATE_PLAYING) {
            if (JCMediaManager.instance().mediaPlayer.isPlaying()) {
                JCMediaManager.instance().mediaPlayer.pause(); // 暂停
            }
            mCurrState = JCVideoPlayer.CURRENT_STATE_PAUSE;

            if (mListener != null) {
                mListener.onPause();
            }
        }
    }

    /**
     * 停止播放
     */
    public void stop() {
        Log.e("xuan", "stop: " + mCurrState);
        mCurrState = JCVideoPlayer.CURRENT_STATE_NORMAL;
        JCMediaManager.instance().releaseMediaPlayer();
        JCMediaManager.removeOnJcdvListener(this);
        // 加上这句，避免循环播放video的时候，内存不断飙升。
        Runtime.getRuntime().gc();
    }

    public void reset() {
        Log.e("xuan", "reset: ");
        JCMediaManager.instance().releaseMediaPlayer();
        mCurrState = JCVideoPlayer.CURRENT_STATE_NORMAL;

        // 清理缓存变量
        this.removeView(JCMediaManager.textureView);

        mAudioManager = (AudioManager) getContext().getSystemService(Context.AUDIO_SERVICE);
        mAudioManager.abandonAudioFocus(onAudioFocusChangeListener);

        JCMediaManager.textureView = null;
        JCMediaManager.savedSurfaceTexture = null;

        // 加上这句，避免循环播放video的时候，内存不断飙升。
        Runtime.getRuntime().gc();

        if (mListener != null) {
            mListener.onReset();
        }
    }

    /**
     * 修改系统音乐音量
     *
     * @param volume +10 or -10
     */
    public void changeVolume(int volume) {
        if (mCurrState == JCVideoPlayer.CURRENT_STATE_PLAYING && mAudioManager != null) {
            //            int max = mAudioManager.getStreamMaxVolume(AudioManager.STREAM_MUSIC);
            int curr = mAudioManager.getStreamVolume(AudioManager.STREAM_MUSIC);
            mAudioManager.setStreamVolume(AudioManager.STREAM_MUSIC, curr + volume, 0);
        }
    }

    /**
     * 修改当前播放进度
     */
    public void changeProgress(int pro) {
        // 得到视频长度
        int duration = getDuration();
        // 将长度转换成时间
        String totalTime = JCUtils.stringForTime(duration);

        // 跳转到一个位置 毫秒
        JCMediaManager.instance().mediaPlayer.seekTo(1000);

        // 保存一个url的进度 or 清除进度
        JCUtils.saveProgress(getContext(), mCurrUrl, 0);
        JCUtils.clearSavedProgress(getContext(), mCurrUrl);
    }

    public void seekTo(int msec) {
        // 跳转到一个位置 毫秒
        JCMediaManager.instance().mediaPlayer.seekTo(msec);
    }

    /**
     * 加载完成开始播放的回掉
     */
    @Override
    public void onPrepared() {
        Log.e("xuan", "开始播放: " + mCurrUrl);
        mCurrState = JCVideoPlayer.CURRENT_STATE_PLAYING;
        if (mListener != null) {
            mListener.onPrepared();
        }
    }

    // 播放完成的回调
    @Override
    public void onCompletion() {
        Log.e("xuan", "播放完成: state = " + mCurrState);
        mCurrState = JCVideoPlayer.CURRENT_STATE_NORMAL;
        if (mListener != null) {
            mListener.onCompletion();
        }

        if (loop) {
            mCurrState = JCVideoPlayer.CURRENT_STATE_PAUSE;
            play(this.mCurrUrl);
        }
    }

    @Override
    public void onError() {
        Log.e("xuan", "播放出错: " + mCurrUrl);
        JCMediaManager.instance().releaseMediaPlayer();
        mCurrState = JCVideoPlayer.CURRENT_STATE_NORMAL;
        if (mListener != null) {
            mListener.onError();
        }
    }

    @Override
    public void onPause() {

    }

    @Override
    public void onReset() {

    }

    public int getCurrentProgress() {
        int position = 0;
        if (mCurrState == JCVideoPlayer.CURRENT_STATE_PLAYING || mCurrState == JCVideoPlayer.CURRENT_STATE_PAUSE) {
            try {
                position = JCMediaManager.instance().mediaPlayer.getCurrentPosition();
            } catch (IllegalStateException e) {
                e.printStackTrace();
                return position;
            }
        }
        return position;
    }

    public int getDuration() {
        int duration = 0;
        try {
            duration = JCMediaManager.instance().mediaPlayer.getDuration();
        } catch (IllegalStateException e) {
            e.printStackTrace();
            return duration;
        }
        return duration;
    }

    public void addOnJcvdListener(OnJcvdListener listener) {
        this.mListener = listener;
    }

    public boolean isPlaying() {
        return mCurrState == JCVideoPlayer.CURRENT_STATE_PLAYING;
    }

}
