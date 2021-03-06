package com.ktw.bitbit.video;

import android.animation.AnimatorSet;
import android.animation.ObjectAnimator;
import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.ImageFormat;
import android.graphics.Rect;
import android.graphics.SurfaceTexture;
import android.hardware.Camera;
import android.hardware.SensorManager;
import android.media.MediaPlayer;
import android.net.Uri;
import android.os.Bundle;
import android.text.TextUtils;
import android.util.Log;
import android.view.MotionEvent;
import android.view.OrientationEventListener;
import android.view.Surface;
import android.view.TextureView;
import android.view.View;
import android.widget.ImageView;
import android.widget.RelativeLayout;
import android.widget.Toast;

import com.alibaba.fastjson.JSON;
import com.joe.camera2recorddemo.Entity.SizeInfo;
import com.joe.camera2recorddemo.OpenGL.CameraRecorder;
import com.joe.camera2recorddemo.OpenGL.Filter.Mp4EditFilter;
import com.joe.camera2recorddemo.OpenGL.Renderer;
import com.joe.camera2recorddemo.Utils.MatrixUtils;
import com.ktw.bitbit.FLYAppConstant;
import com.ktw.bitbit.FLYApplication;
import com.ktw.bitbit.R;
import com.ktw.bitbit.FLYReporter;
import com.ktw.bitbit.bean.VideoFile;
import com.ktw.bitbit.bean.event.MessageLocalVideoFile;
import com.ktw.bitbit.bean.event.MessageVideoFile;
import com.ktw.bitbit.helper.DialogHelper;
import com.ktw.bitbit.ui.base.BaseActivity;
import com.ktw.bitbit.ui.me.LocalVideoActivity;
import com.ktw.bitbit.util.CameraUtil;
import com.ktw.bitbit.util.FileUtil;
import com.ktw.bitbit.util.RecorderUtils;
import com.ktw.bitbit.util.ScreenUtil;
import com.ktw.bitbit.view.MyVideoView;
import com.ktw.bitbit.view.cjt2325.cameralibrary.CameraInterface;
import com.ktw.bitbit.view.cjt2325.cameralibrary.CaptureLayout;
import com.ktw.bitbit.view.cjt2325.cameralibrary.FoucsView;
import com.ktw.bitbit.view.cjt2325.cameralibrary.listener.CaptureListener;
import com.ktw.bitbit.view.cjt2325.cameralibrary.listener.ClickListener;
import com.ktw.bitbit.view.cjt2325.cameralibrary.listener.TypeListener;
import com.ktw.bitbit.view.cjt2325.cameralibrary.util.CameraParamUtil;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

import Jni.FFmpegCmd;
import Jni.VideoUitls;
import VideoHandle.OnEditorListener;
import de.greenrobot.event.EventBus;
import me.kareluo.imaging.IMGEditActivity;

/**
 * ???????????????????????????
 * create by TAG
 * update time 2018-11-21 19:43:13
 */

public class VideoRecorderActivity1 extends BaseActivity implements View.OnClickListener, Renderer {
    public static final int REQUEST_IMAGE_EDIT = 1;
    private static final String TAG = "VideoRecorderActivity";
    private static final int REQUEST_CODE_SELECT_VIDEO = 3;
    // ??????????????????
    private static final int mRecordMaxTime = 10 * 1000;
    private static final int mRecordMinTime = 1000;
    public int mCameraWidth, mCameraHeight;
    int handlerTime = 0;
    // ??????
    private TextureView mTextureView;
    private ImageView mPhotoView;
    private MyVideoView mVideoView;
    private RelativeLayout mSetRelativeLayout;
    private CaptureLayout mCaptureLayout;
    private FoucsView mFoucsView;
    // ??????
    private Camera mCamera;
    private Camera.Parameters mParams;
    private float screenProp;
    private int mCurrentCameraState;
    private boolean isTakePhoto;// ????????? ?????? || ??????
    private boolean isRecord;
    // ???????????????bitmap
    private Bitmap mCurrentBitmap;
    // ?????????????????????????????????
    private String mEditedImagePath;
    // ?????????????????????
    private String mCurrentVideoPath;
    // ?????????????????????
    private int mCurrentTime;
    private CameraRecorder mCameraRecord;
    private Mp4EditFilter mFilter;
    FilterPreviewDialog.OnUpdateFilterListener mFilterListener = new FilterPreviewDialog.OnUpdateFilterListener() {
        @Override
        public void select(int type) {
            mFilter.getChooseFilter().setChangeType(type);
        }

        @Override
        public void dismiss() {
        }
    };
    private FilterPreviewDialog mFilterDialog;
    private AlbumOrientationEventListener mAlbumOrientationEventListener;
    // ????????????????????????
    private int mOrientation = 0;

    @Override
    public void onCreate(final Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_video_record);

        initView();
        initRecorder();
        initEvent();

        mTextureView.postDelayed(() -> setFocusViewAnimation(ScreenUtil.getScreenWidth(mContext) / 2, ScreenUtil.getScreenHeight(mContext) / 2), 1000);
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        mAlbumOrientationEventListener.disable();
    }

    private void initView() {
        mAlbumOrientationEventListener = new AlbumOrientationEventListener(this, SensorManager.SENSOR_DELAY_NORMAL);
        if (mAlbumOrientationEventListener.canDetectOrientation()) {
            mAlbumOrientationEventListener.enable();
        } else {
            Log.e("zx", "????????????Orientation");
        }

        mTextureView = findViewById(R.id.mTexture);

        mPhotoView = findViewById(R.id.image_photo);
        mVideoView = findViewById(R.id.video_preview);

        mSetRelativeLayout = findViewById(R.id.set_rl);

        mCaptureLayout = findViewById(R.id.capture_layout);
        mCaptureLayout.setIconSrc(0, R.drawable.ic_sel_local_video);
        mFoucsView = findViewById(R.id.fouce_view);
    }

    private void initRecorder() {
        mFilter = new Mp4EditFilter(getResources());
        mFilterDialog = new FilterPreviewDialog(this, mFilterListener);

        mCameraRecord = new CameraRecorder();
        mCurrentVideoPath = RecorderUtils.getVideoFileByTime();
        mCameraRecord.setOutputPath(mCurrentVideoPath);
        mTextureView.setSurfaceTextureListener(new TextureView.SurfaceTextureListener() {

            @Override
            public void onSurfaceTextureAvailable(SurfaceTexture surface, int width, int height) {
                mCamera = Camera.open(0);
                screenProp = (float) height / (float) width;
                initCamera(screenProp);

                mCameraRecord.setOutputSurface(new Surface(surface));
                Camera.Size videoSize;
                if (mParams.getSupportedVideoSizes() == null) {
                    videoSize = CameraParamUtil.getInstance().getPreviewSize(mParams.getSupportedPreviewSizes(), 600,
                            screenProp);
                } else {
                    videoSize = CameraParamUtil.getInstance().getPreviewSize(mParams.getSupportedVideoSizes(), 600,
                            screenProp);
                }
                SizeInfo sizeInfo;
                if (videoSize.width == videoSize.height) {
                    sizeInfo = new SizeInfo(720, 720);
                } else {
                    sizeInfo = new SizeInfo(videoSize.height, videoSize.width);
                }
                mCameraRecord.setOutputSize(sizeInfo);
                mCameraRecord.setRenderer(VideoRecorderActivity1.this);
                mCameraRecord.setPreviewSize(width, height);
                mCameraRecord.startPreview();
            }

            @Override
            public void onSurfaceTextureSizeChanged(SurfaceTexture surface, int width, int height) {
                mCameraRecord.setPreviewSize(width, height);
            }

            @Override
            public boolean onSurfaceTextureDestroyed(SurfaceTexture surface) {
                if (isRecord) {
                    isRecord = false;
                    try {
                        mCameraRecord.stopRecord();
                    } catch (InterruptedException e) {
                        e.printStackTrace();
                    }
                }
                stopPreview();
                return true;
            }

            @Override
            public void onSurfaceTextureUpdated(SurfaceTexture surface) {

            }
        });
    }

    @Override
    public void create() {
        try {
            mCamera.setPreviewTexture(mCameraRecord.createInputSurfaceTexture());
        } catch (IOException e) {
            e.printStackTrace();
        }
        Camera.Size mSize = mCamera.getParameters().getPreviewSize();
        mCameraWidth = mSize.height;
        mCameraHeight = mSize.width;

        mCamera.startPreview();
        mFilter.create();
    }

    @Override
    public void sizeChanged(int width, int height) {
        mFilter.sizeChanged(width, height);
        MatrixUtils.getMatrix(mFilter.getVertexMatrix(), MatrixUtils.TYPE_CENTERCROP,
                mCameraWidth, mCameraHeight, width, height);
        MatrixUtils.flip(mFilter.getVertexMatrix(), false, true);
    }

    @Override
    public void draw(int texture) {
        mFilter.draw(texture);
    }

    @Override
    public void destroy() {
        mFilter.destroy();
    }

    public void initCamera(float screenProp) {
        if (mCamera != null) {
            mParams = mCamera.getParameters();
            Camera.Size previewSize = CameraParamUtil.getInstance().getPreviewSize(mParams
                    .getSupportedPreviewSizes(), 1000, screenProp);
            Camera.Size pictureSize = CameraParamUtil.getInstance().getPictureSize(mParams
                    .getSupportedPictureSizes(), 1200, screenProp);
            mParams.setPreviewSize(previewSize.width, previewSize.height);
            mParams.setPictureSize(pictureSize.width, pictureSize.height);
            if (CameraParamUtil.getInstance().isSupportedFocusMode(
                    mParams.getSupportedFocusModes(),
                    Camera.Parameters.FOCUS_MODE_AUTO)) {
                mParams.setFocusMode(Camera.Parameters.FOCUS_MODE_AUTO);
            }

            if (CameraParamUtil.getInstance().isSupportedPictureFormats(mParams.getSupportedPictureFormats(),
                    ImageFormat.JPEG)) {
                mParams.setPictureFormat(ImageFormat.JPEG);
                mParams.setJpegQuality(100);
            }
            mCamera.setParameters(mParams);
            mParams = mCamera.getParameters();
        }
    }

    private void initEvent() {
        mCaptureLayout.setDuration(mRecordMaxTime);
        mCaptureLayout.setMinDuration(mRecordMinTime);
        mCaptureLayout.setCaptureLisenter(new CaptureListener() {
            @Override
            public void takePictures() {
                isTakePhoto = true;
                mCamera.takePicture(null, null, new Camera.PictureCallback() {
                    @Override
                    public void onPictureTaken(byte[] data, Camera camera) {
                        mCurrentBitmap = BitmapFactory.decodeByteArray(data, 0, data.length);
                        Camera.CameraInfo info = new Camera.CameraInfo();
                        Camera.getCameraInfo(mCurrentCameraState, info);
                        if (info.facing == Camera.CameraInfo.CAMERA_FACING_BACK) { // ????????????????????????????????????????????????90'
                            mCurrentBitmap = CameraUtil.restoreRotatedImage(info.orientation, mCurrentBitmap);
                        } else {// ?????????????????????????????????????????????270',?????????????????????
                            mCurrentBitmap = CameraUtil.restoreRotatedImage(info.orientation, mCurrentBitmap);
                            mCurrentBitmap = CameraUtil.turnCurrentLayer(mCurrentBitmap, -1, 1);
                        }
                        mCurrentBitmap = CameraUtil.restoreRotatedImage(mOrientation, mCurrentBitmap);
                        playPhoto();
                        // ??????????????????
                        mCamera.startPreview();
                    }
                });
            }

            @Override
            public void recordStart() {
                isTakePhoto = false;
                // ??????????????????
                if (startRecord(mCurrentVideoPath)) {
                    isRecord = true;
                    mCurrentTime = 0;
                }
            }

            @Override
            public void recordShort(long time) {
                mCaptureLayout.setTextWithAnimation(getString(R.string.tip_record_too_short));
                mTextureView.postDelayed(new Runnable() {
                    @Override
                    public void run() {
                        if (stopRecord()) {
                            isRecord = false;
                            mCurrentTime = 0;
                            mCaptureLayout.resetCaptureLayout();
                        }
                    }
                }, mRecordMinTime - time);
            }

            @Override
            public void recordEnd(long time) {
                if (stopRecord()) {
                    isRecord = false;
                    mCurrentTime = (int) (time / 1000);
                    playVideo();
                }
            }

            @Override
            public void recordZoom(float zoom) {
                // ???????????????
            }

            @Override
            public void recordError() {

            }
        });

        //?????? ??????
        mCaptureLayout.setTypeLisenter(new TypeListener() {
            @Override
            public void cancel() {
                reset();
            }

            @Override
            public void confirm() {
                complete();
            }
        });

        mCaptureLayout.setLeftClickListener(new ClickListener() {
            @Override
            public void onClick() {
                finish();
            }
        });

        mCaptureLayout.setMiddleClickListener(new ClickListener() {
            @Override
            public void onClick() {// ??????????????????
                String path = FileUtil.saveBitmap(mCurrentBitmap);
                if (!TextUtils.isEmpty(path)) {
                    mEditedImagePath = FileUtil.createImageFileForEdit().getAbsolutePath();
                    IMGEditActivity.startForResult(VideoRecorderActivity1.this, Uri.fromFile(new File(path)), mEditedImagePath, REQUEST_IMAGE_EDIT);
                } else {
                    DialogHelper.tip(VideoRecorderActivity1.this, "??????????????????");
                }
            }
        });

        mCaptureLayout.setRightClickListener(new ClickListener() {
            @Override
            public void onClick() {// ??????????????????
                Intent intent = new Intent(VideoRecorderActivity1.this, LocalVideoActivity.class);
                intent.putExtra(FLYAppConstant.EXTRA_ACTION, FLYAppConstant.ACTION_SELECT);
                intent.putExtra(FLYAppConstant.EXTRA_MULTI_SELECT, true);
                startActivityForResult(intent, REQUEST_CODE_SELECT_VIDEO);
            }
        });

        findViewById(R.id.iv_swith).setOnClickListener(this);
        findViewById(R.id.iv_filter).setOnClickListener(this);
    }

    @Override
    public void onClick(final View v) {
        switch (v.getId()) {
            case R.id.iv_filter:
                Toast.makeText(this, getString(R.string.tip_photo_filter_not_supported), Toast.LENGTH_SHORT).show();
                mFilterDialog.show();
                break;
            case R.id.iv_swith:
                changeCamera();
                break;
        }
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        if (resultCode == Activity.RESULT_OK) {
            switch (requestCode) {
                case REQUEST_IMAGE_EDIT:
                    // ??????????????????
                    mCurrentBitmap = BitmapFactory.decodeFile(mEditedImagePath);
                    mPhotoView.setImageBitmap(mCurrentBitmap);
                    break;
                case REQUEST_CODE_SELECT_VIDEO:
                    // ??????????????????
                    if (data == null) {
                        return;
                    }
                    String json = data.getStringExtra(FLYAppConstant.EXTRA_VIDEO_LIST);
                    List<VideoFile> fileList = JSON.parseArray(json, VideoFile.class);
                    if (fileList == null || fileList.size() == 0) {
                        // ???????????????????????????????????????
                        FLYReporter.unreachable();
                    } else {
                        for (VideoFile videoFile : fileList) {
                            String filePath = videoFile.getFilePath();
                            if (TextUtils.isEmpty(filePath)) {
                                // ???????????????????????????????????????
                                FLYReporter.unreachable();
                            } else {
                                File file = new File(filePath);
                                if (!file.exists()) {
                                    // ???????????????????????????????????????
                                    FLYReporter.unreachable();
                                } else {
                                    EventBus.getDefault().post(new MessageLocalVideoFile(file));
                                }
                            }
                        }
                        finish();
                    }
                    break;
                default:
                    super.onActivityResult(requestCode, resultCode, data);
            }
        }
    }

    /**
     * ???????????????
     */
    private void changeCamera() {
        if (Camera.getNumberOfCameras() > 1) {
            stopPreview();
            mCurrentCameraState += 1;
            if (mCurrentCameraState > Camera.getNumberOfCameras() - 1)
                mCurrentCameraState = 0;
            mCamera = Camera.open(mCurrentCameraState);
            initCamera(screenProp);// ?????????????????????????????????setParameters
/*
            if (mParams == null) {
                initCamera(screenProp);
            } else {
                // ???????????? java.lang.RuntimeException: setParameters failed
               mCamera.setParameters(mParams);
            }
*/
            mCameraRecord.startPreview();
        }
    }

    /**
     * ?????????????????????
     */
    private void stopPreview() {
        try {
            mCameraRecord.stopPreview();
        } catch (InterruptedException e) {
            e.printStackTrace();
        }
        if (mCamera != null) {
            mCamera.stopPreview();
            mCamera.release();
            mCamera = null;
        }
    }

    /**
     * ????????????
     */
    private boolean startRecord(String path) {
        try {
            Log.e(TAG, "???????????????" + path);
            mCameraRecord.startRecord();
        } catch (IOException e) {
            e.printStackTrace();
            return false;
        }
        return true;
    }

    /**
     * ????????????
     */
    private boolean stopRecord() {
        try {
            mCameraRecord.stopRecord();
            return true;
        } catch (InterruptedException e) {
            e.printStackTrace();
        }
        return false;
    }

    /*
    ????????????
     */
    private void playPhoto() {
        mSetRelativeLayout.setVisibility(View.GONE);
        mPhotoView.setImageBitmap(mCurrentBitmap);
        mPhotoView.setVisibility(View.VISIBLE);

        mCaptureLayout.startAlphaAnimation();
        mCaptureLayout.startTypeBtnAnimator();
    }

    /*
    ????????????
     */
    private void playVideo() {
        mSetRelativeLayout.setVisibility(View.GONE);
        mVideoView.setVisibility(View.VISIBLE);
        mVideoView.setVideoPath(mCurrentVideoPath);
        mVideoView.setOnCompletionListener(new MediaPlayer.OnCompletionListener() {
            @Override
            public void onCompletion(MediaPlayer mp) {// ????????????
                mVideoView.start();
            }
        });
        mVideoView.start();
    }

    private void reset() {
        mSetRelativeLayout.setVisibility(View.VISIBLE);
        if (isTakePhoto) {
            mPhotoView.setVisibility(View.GONE);
        } else {
            mVideoView.stopPlayback();// ???????????? ???????????????
            mVideoView.setVisibility(View.GONE);
        }

        mCaptureLayout.resetCaptureLayout();
    }

    private void complete() {
        if (isTakePhoto) {
            String path = FileUtil.saveBitmap(mCurrentBitmap);
            EventBus.getDefault().post(new MessageEventGpu(path));
            finish();
        } else {
            compress(mCurrentVideoPath);
        }
    }

    private void compress(String path) {
        DialogHelper.showMessageProgressDialog(this, FLYApplication.getContext().getString(R.string.compressed));
        final String out = RecorderUtils.getVideoFileByTime();
        String[] cmds = RecorderUtils.ffmpegComprerssCmd(path, out);
        long duration = VideoUitls.getDuration(path);

        FFmpegCmd.exec(cmds, duration, new OnEditorListener() {
            public void onSuccess() {
                DialogHelper.dismissProgressDialog();
                mCurrentVideoPath = out;
                EventBus.getDefault().post(new MessageVideoFile(mCurrentTime,
                        new File(mCurrentVideoPath).length(), mCurrentVideoPath));
                finish();
            }

            public void onFailure() {
                DialogHelper.dismissProgressDialog();
                finish();
            }

            public void onProgress(float progress) {
            }
        });
    }

    /*****
     * ?????? ????????????
     */
    @Override
    public boolean onTouchEvent(MotionEvent event) {
        switch (event.getAction()) {
            case MotionEvent.ACTION_DOWN:
                if (event.getPointerCount() == 1) {
                    // ?????????????????????
                    setFocusViewAnimation(event.getX(), event.getY());
                }
                break;
            case MotionEvent.ACTION_MOVE:
                break;
            case MotionEvent.ACTION_UP:
                break;
        }
        return true;
    }

    public boolean setFocusViewAnimation(float x, float y) {
        if (y > mCaptureLayout.getTop()) {
            return false;
        }
        mFoucsView.setVisibility(View.VISIBLE);
        if (x < mFoucsView.getWidth() / 2) {
            x = mFoucsView.getWidth() / 2;
        }
        if (x > ScreenUtil.getScreenWidth(this) - mFoucsView.getWidth() / 2) {
            x = ScreenUtil.getScreenWidth(this) - mFoucsView.getWidth() / 2;
        }
        if (y < mFoucsView.getWidth() / 2) {
            y = mFoucsView.getWidth() / 2;
        }
        if (y > mCaptureLayout.getTop() - mFoucsView.getWidth() / 2) {
            y = mCaptureLayout.getTop() - mFoucsView.getWidth() / 2;
        }
        mFoucsView.setX(x - mFoucsView.getWidth() / 2);
        mFoucsView.setY(y - mFoucsView.getHeight() / 2);
        ObjectAnimator scaleX = ObjectAnimator.ofFloat(mFoucsView, "scaleX", 1, 0.6f);
        ObjectAnimator scaleY = ObjectAnimator.ofFloat(mFoucsView, "scaleY", 1, 0.6f);
        ObjectAnimator alpha = ObjectAnimator.ofFloat(mFoucsView, "alpha", 1f, 0.4f, 1f, 0.4f, 1f, 0.4f, 1f);
        AnimatorSet animSet = new AnimatorSet();
        animSet.play(scaleX).with(scaleY).before(alpha);
        animSet.setDuration(400);
        animSet.start();

        handleFocus(x, y);
        return true;
    }

    public void handleFocus(final float x, final float y) {
        if (mCamera == null) {
            return;
        }
        final Camera.Parameters params = mCamera.getParameters();
        Rect focusRect = CameraInterface.calculateTapArea(x, y, 1f, this);
        mCamera.cancelAutoFocus();
        if (params.getMaxNumFocusAreas() > 0) {
            List<Camera.Area> focusAreas = new ArrayList<>();
            focusAreas.add(new Camera.Area(focusRect, 800));
            params.setFocusAreas(focusAreas);
        } else {
            mFoucsView.setVisibility(View.INVISIBLE);
            return;
        }
        final String currentFocusMode = params.getFocusMode();
        try {
            params.setFocusMode(Camera.Parameters.FOCUS_MODE_AUTO);
            mCamera.setParameters(params);
            mCamera.autoFocus((success, camera) -> {
                if (success || handlerTime > 10) {
                    Camera.Parameters params1 = camera.getParameters();
                    params1.setFocusMode(currentFocusMode);
                    camera.setParameters(params1);
                    handlerTime = 0;
                    mFoucsView.setVisibility(View.INVISIBLE);
                } else {
                    handlerTime++;
                    handleFocus(x, y);
                }
            });
        } catch (Exception e) {

        }
    }

    private class AlbumOrientationEventListener extends OrientationEventListener {
        public AlbumOrientationEventListener(Context context) {
            super(context);
        }

        public AlbumOrientationEventListener(Context context, int rate) {
            super(context, rate);
        }

        @Override
        public void onOrientationChanged(int orientation) {
            if (orientation == OrientationEventListener.ORIENTATION_UNKNOWN) {
                return;
            }

            //???????????????????????????
            int newOrientation = ((orientation + 45) / 90 * 90) % 360;

            if (newOrientation != mOrientation) {
                mOrientation = newOrientation;
                Log.e("zx", "onOrientationChanged: " + mOrientation);
                //?????????mOrientation????????????????????????0?????90?????180?????270??????????????

            }
        }
    }
}
