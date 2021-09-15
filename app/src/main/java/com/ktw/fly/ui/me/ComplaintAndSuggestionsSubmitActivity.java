package com.ktw.fly.ui.me;

import android.app.Activity;
import android.app.AlertDialog;
import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.text.TextUtils;
import android.util.Log;
import android.view.View;
import android.widget.TextView;

import androidx.recyclerview.widget.GridLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.ktw.fly.R;
import com.ktw.fly.helper.ImageLoadHelper;
import com.ktw.fly.ui.base.BaseActivity;
import com.ktw.fly.ui.base.BaseRecAdapter;
import com.ktw.fly.ui.base.BaseRecViewHolder;
import com.ktw.fly.util.ToastUtil;
import com.ktw.fly.video.EasyCameraActivity;
import com.ktw.fly.view.SquareCenterImageView;
import com.ktw.fly.view.photopicker.PhotoPickerActivity;
import com.ktw.fly.view.photopicker.SelectModel;
import com.ktw.fly.view.photopicker.intent.PhotoPickerIntent;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import pl.droidsonroids.gif.GifDrawable;
import top.zibin.luban.Luban;
import top.zibin.luban.OnCompressListener;

/**
 * 提交建议或投诉
 **/
public class ComplaintAndSuggestionsSubmitActivity extends BaseActivity {
    public static final String TYPE = "type";
    public static final int TYPE_COMPLAINT =1106;
    public static final int TYPE_SUGGESTIONS =1108;
    private TextView mTitleTv;
    private GridViewAdapter mAdapter;
    private ArrayList<String> mPhotoList;
    private RecyclerView mRecyclerView;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_complaint_suggestions_submit);
        initActionBar();
        initView();
        initData();
    }

    private void initView() {
        mRecyclerView = findViewById(R.id.rv_img);
        GridLayoutManager layoutManager = new GridLayoutManager(mContext,3);
        mRecyclerView.setLayoutManager(layoutManager);


    }


    private void showSelectPictureDialog() {
        String[] items = new String[]{getString(R.string.photograph), getString(R.string.album)};
        AlertDialog.Builder builder = new AlertDialog.Builder(this).setSingleChoiceItems(items, 0,
                (dialog, which) -> {
                    if (which == 0) {
                        takePhoto();
                    } else {
                        selectPhoto();
                    }
                    dialog.dismiss();
                });
        builder.show();
    }
    // 拍照
    private void takePhoto() {

        Intent intent = new Intent(this, EasyCameraActivity.class);
        startActivity(intent);
    }
    /**
     * 相册
     * 可以多选的图片选择器
     */
    private void selectPhoto() {
        ArrayList<String> imagePaths = new ArrayList<>();
        PhotoPickerIntent intent = new PhotoPickerIntent(this);
        intent.setSelectModel(SelectModel.MULTI);
        // 是否显示拍照， 默认false
        intent.setShowCarema(false);
        // 最多选择照片数量，默认为9
        intent.setMaxTotal(9 - mPhotoList.size());
        // 已选中的照片地址， 用于回显选中状态
        intent.setSelectedPaths(imagePaths);
        // intent.setImageConfig(config);
        startActivityForResult(intent, REQUEST_CODE_PICK_PHOTO);
    }
    private void initData() {
        mPhotoList = new ArrayList<>();
        mAdapter = new GridViewAdapter(mPhotoList);
        mRecyclerView.setAdapter(mAdapter);
        mAdapter.setItemClickListener((adapter, view, position) -> {
            if (mAdapter.getData().size()-1== position){
                showSelectPictureDialog();
            }
        });

    }

    private void initActionBar() {
        getSupportActionBar().hide();
        findViewById(R.id.iv_title_left).setOnClickListener(v -> finish());
        mTitleTv = findViewById(R.id.tv_title_center);

    }
    private static final int REQUEST_CODE_CAPTURE_PHOTO = 1;  // 拍照
    private static final int REQUEST_CODE_PICK_PHOTO = 2;     // 图库
    // 拍照和图库，获得图片的Uri
    private Uri mNewPhotoUri;
    // 单张图片压缩 拍照
    private void photograph(final File file) {
        Log.e("zq", "压缩前图片路径:" + file.getPath() + "压缩前图片大小:" + file.length() / 1024 + "KB");
        // 拍照出来的图片Luban一定支持，
        Luban.with(this)
                .load(file)
                .ignoreBy(100)     // 原图小于100kb 不压缩
                // .putGear(2)     // 设定压缩档次，默认三挡
                // .setTargetDir() // 指定压缩后的图片路径
                .setCompressListener(new OnCompressListener() { //设置回调
                    @Override
                    public void onStart() {
                        Log.e("zq", "开始压缩");
                    }

                    @Override
                    public void onSuccess(File file) {
                        Log.e("zq", "压缩成功，压缩后图片位置:" + file.getPath() + "压缩后图片大小:" + file.length() / 1024 + "KB");
                        mPhotoList.add(mPhotoList.size(),file.getPath());

                        mAdapter.setNewData(mPhotoList);
                    }

                    @Override
                    public void onError(Throwable e) {
                        Log.e("zq", "压缩失败,原图上传");
                        mPhotoList.add(mPhotoList.size(),file.getPath());

                        mAdapter.setNewData(mPhotoList);
                    }
                }).launch();// 启动压缩
    }
    // 多张图片压缩 相册
    private void album(ArrayList<String> stringArrayListExtra, boolean isOriginal) {
        if (isOriginal) {// 原图发送，不压缩
            Log.e("zq", "原图上传，不压缩，选择原文件路径");
            for (int i = 0; i < stringArrayListExtra.size(); i++) {
                mPhotoList.add(mPhotoList.size(),stringArrayListExtra.get(i));
                mAdapter.setNewData(mPhotoList);

            }
            return;
        }

        List<String> list = new ArrayList<>();
        for (int i = 0; i < stringArrayListExtra.size(); i++) {
            // Luban只处理特定后缀的图片，不满足的不处理也不走回调，
            // 只能挑出来不压缩，
            // todo luban支持压缩.gif图，但是压缩之后的.gif图用glide加载与转换为gifDrawable都会出问题，所以,gif图不压缩了
            List<String> lubanSupportFormatList = Arrays.asList("jpg", "jpeg", "png", "webp");
            boolean support = false;
            for (int j = 0; j < lubanSupportFormatList.size(); j++) {
                if (stringArrayListExtra.get(i).endsWith(lubanSupportFormatList.get(j))) {
                    support = true;
                    break;
                }
            }
            if (!support) {
                list.add(stringArrayListExtra.get(i));
            }
        }

        if (list.size() > 0) {
            for (String s : list) {// 不压缩的部分，直接发送
                mPhotoList.add(s);
                mPhotoList.add(mPhotoList.size(),s);
                mAdapter.setNewData(mPhotoList);

            }
        }

        // 移除掉不压缩的图片
        stringArrayListExtra.removeAll(mPhotoList);

        Luban.with(this)
                .load(stringArrayListExtra)
                .ignoreBy(100)// 原图小于100kb 不压缩
                .setCompressListener(new OnCompressListener() {
                    @Override
                    public void onStart() {

                    }

                    @Override
                    public void onSuccess(File file) {
                        mPhotoList.add(mPhotoList.size(),file.getPath());

                        mAdapter.setNewData(mPhotoList);


                    }

                    @Override
                    public void onError(Throwable e) {

                    }
                }).launch();// 启动压缩
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (requestCode == REQUEST_CODE_CAPTURE_PHOTO) {
            // 拍照返回 Todo 已更换拍照方式
            if (resultCode == Activity.RESULT_OK) {
                if (mNewPhotoUri != null) {
                    photograph(new File(mNewPhotoUri.getPath()));
                } else {
                    ToastUtil.showToast(this, R.string.c_take_picture_failed);
                }
            }
        } else if (requestCode == REQUEST_CODE_PICK_PHOTO) {
            // 选择图片返回
            if (resultCode == Activity.RESULT_OK) {
                if (data != null) {
                    boolean isOriginal = data.getBooleanExtra(PhotoPickerActivity.EXTRA_RESULT_ORIGINAL, false);
                    album(data.getStringArrayListExtra(PhotoPickerActivity.EXTRA_RESULT), isOriginal);
                } else {
                    ToastUtil.showToast(this, R.string.c_photo_album_failed);
                }
            }
        }
    }

    private class GridViewAdapter extends BaseRecAdapter<String, ImageViewHolder> {

        public GridViewAdapter(List<String> list) {
            super(list);
        }

        @Override
        public void onHolder(ImageViewHolder holder, String path, int position) {

            if (TextUtils.isEmpty(path)) {
                holder.imageView.setImageResource(R.drawable.send_image);
            } else {
                // 普通的视图
                if (path.endsWith(".gif")) {
                    try {
                        GifDrawable gifDrawable = new GifDrawable(new File(path));
                        holder.imageView.setImageDrawable(gifDrawable);
                    } catch (IOException e) {
                        e.printStackTrace();
                    }
                } else {
                    ImageLoadHelper.showImageWithSizeError(
                            mContext,
                            mPhotoList.get(position),
                            R.drawable.pic_error,
                            150, 150,
                            holder.imageView);
                }
            }


        }


        @Override
        public ImageViewHolder onCreateHolder() {
            return new ImageViewHolder(getViewByRes(R.layout.layout_circle_add_more_item_temp));

        }
    }

    class ImageViewHolder extends BaseRecViewHolder {

        SquareCenterImageView imageView;

        public ImageViewHolder(View itemView) {
            super(itemView);
            imageView = itemView.findViewById(R.id.iv);
        }
    }
}
