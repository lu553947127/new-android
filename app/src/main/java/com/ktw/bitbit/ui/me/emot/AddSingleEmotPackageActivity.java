package com.ktw.bitbit.ui.me.emot;

import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;
import android.text.TextUtils;
import android.util.Log;
import android.view.View;
import android.widget.AdapterView;
import android.widget.TextView;

import androidx.annotation.Nullable;

import com.alibaba.fastjson.JSON;
import com.ktw.bitbit.FLYApplication;
import com.ktw.bitbit.R;
import com.ktw.bitbit.bean.MyCollectEmotPackageBean;
import com.ktw.bitbit.bean.UploadFileResult;
import com.ktw.bitbit.broadcast.OtherBroadcast;
import com.ktw.bitbit.helper.DialogHelper;
import com.ktw.bitbit.helper.UploadService;
import com.ktw.bitbit.ui.base.BaseActivity;
import com.ktw.bitbit.util.ToastUtil;
import com.ktw.bitbit.view.MyGridView;
import com.ktw.bitbit.view.SkinTextView;
import com.ktw.bitbit.view.photopicker.PhotoPickerActivity;
import com.ktw.bitbit.view.photopicker.SelectModel;
import com.ktw.bitbit.view.photopicker.intent.PhotoPickerIntent;
import com.xuan.xuanhttplibrary.okhttp.HttpUtils;
import com.xuan.xuanhttplibrary.okhttp.callback.ListCallback;
import com.xuan.xuanhttplibrary.okhttp.result.ArrayResult;
import com.xuan.xuanhttplibrary.okhttp.result.Result;

import java.io.File;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import okhttp3.Call;
import top.zibin.luban.Luban;
import top.zibin.luban.OnCompressListener;

/**
 * 添加单个个人表情包
 */
public class AddSingleEmotPackageActivity extends BaseActivity {

    private static final int REQUEST_CODE_PICK_PHOTO = 2;

    private final static int IMAGE_UPLOAD_MAX_COUNT = 300;
    private List<MyCollectEmotPackageBean> emotBeanList;
    private MyGridView gvEmot;
    private MySingleEmotAdapter adapter;
    private int uploadSuccessCount;
    private TextView tvTitle;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_add_single_emot_package);
        initActionBar();
        initView();
        initEvent();
    }

    private void initActionBar() {
        getSupportActionBar().hide();
        findViewById(R.id.iv_title_left).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                finish();
            }
        });
        SkinTextView tvEdit = findViewById(R.id.tv_title_right);
        tvEdit.setText("编辑");
        tvEdit.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Intent intent = new Intent(AddSingleEmotPackageActivity.this, EditSingleEmotPackageActivity.class);
                startActivityForResult(intent, EditSingleEmotPackageActivity.REQUEST_CODE_EDIT_EMOT);
            }
        });
        tvTitle = (TextView) findViewById(R.id.tv_title_center);
        setTitle(0);

        emotBeanList = new ArrayList<>();
        emotBeanList.add(new MyCollectEmotPackageBean());//用户加号按钮显示
    }

    public void setTitle(int emotCount) {
        String title = String.format(getString(R.string.tips_1), emotCount, IMAGE_UPLOAD_MAX_COUNT);
        tvTitle.setText(title);
    }

    private void initView() {
        gvEmot = findViewById(R.id.gvEmot);
        adapter = new MySingleEmotAdapter(this, emotBeanList);
        gvEmot.setAdapter(adapter);
    }

    private void initEvent() {
        gvEmot.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> adapterView, View view, int position, long l) {
                if (position == 0) {
                    //选择图片
                    ArrayList<String> imagePaths = new ArrayList<>();
                    PhotoPickerIntent intent = new PhotoPickerIntent(AddSingleEmotPackageActivity.this);
                    intent.setSelectModel(SelectModel.MULTI);
                    // 已选中的照片地址， 用于回显选中状态
                    intent.setSelectedPaths(imagePaths);
                    startActivityForResult(intent, REQUEST_CODE_PICK_PHOTO);
                } else {
                    MyCollectEmotPackageBean myEmotBean = emotBeanList.get(position);
                    if (myEmotBean != null && !TextUtils.isEmpty(myEmotBean.getFace().getPath().get(0))) {
                        SingleEmotPreviewActivity.start(AddSingleEmotPackageActivity.this, myEmotBean.getFace().getPath().get(0));
                    }
                }

            }
        });
        loadMySingleEmot(false);
    }

    private void loadMySingleEmot(boolean isUpdate) {
        DialogHelper.showDefaulteMessageProgressDialog(this);
        Map<String, String> params = new HashMap<>();
        params.put("access_token", coreManager.getSelfStatus().accessToken);
        params.put("userId", coreManager.getSelf().getUserId());
        params.put("type", 1 + "");
        HttpUtils.get().url(coreManager.getConfig().API_FACE_COLLECT_LIST)
                .params(params)
                .build()
                .execute(new ListCallback<MyCollectEmotPackageBean>(MyCollectEmotPackageBean.class) {
                    @Override
                    public void onResponse(ArrayResult<MyCollectEmotPackageBean> result) {
                        DialogHelper.dismissProgressDialog();
                        if (Result.checkSuccess(mContext, result)) {
                            if (result.getData() != null) {
                                List<MyCollectEmotPackageBean> tempList = new ArrayList<>();
                                tempList.add(new MyCollectEmotPackageBean());
                                tempList.addAll(result.getData());
                                emotBeanList = tempList;
                                runOnUiThread(new Runnable() {
                                    @Override
                                    public void run() {
                                        setTitle(emotBeanList.size() - 1);
                                        adapter.setData(emotBeanList);
                                        adapter.notifyDataSetChanged();
                                        if (isUpdate) {
                                            //存入缓存
                                            FLYApplication.singleEmotList.clear();
                                            for (MyCollectEmotPackageBean item : result.getData()) {
                                                MyEmotBean myEmotBean = new MyEmotBean();
                                                myEmotBean.setUrl(item.getFace().getPath().get(0));
                                                FLYApplication.singleEmotList.add(myEmotBean);
                                            }
                                            // 发送广播去界面更新
                                            Intent broadcast = new Intent(OtherBroadcast.SYNC_EMOT_REFRESH);
                                            mContext.sendBroadcast(broadcast);
                                        }
                                    }
                                });
                            }
                        }

                    }

                    @Override
                    public void onError(Call call, Exception e) {
                        DialogHelper.dismissProgressDialog();
                        ToastUtil.showNetError(AddSingleEmotPackageActivity.this);
                    }
                });
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (resultCode == Activity.RESULT_OK) {
            if (requestCode == REQUEST_CODE_PICK_PHOTO) {
                if (data != null) {
                    DialogHelper.showMessageProgressDialog(AddSingleEmotPackageActivity.this, "正在上传……");
                    album(data.getStringArrayListExtra(PhotoPickerActivity.EXTRA_RESULT));
                } else {
                    ToastUtil.showToast(this, R.string.c_photo_album_failed);
                }
            } else if (requestCode == EditSingleEmotPackageActivity.REQUEST_CODE_EDIT_EMOT) {
                loadMySingleEmot(true);
            }
        }
    }

    // 多张图片压缩 相册
    private void album(ArrayList<String> stringArrayListExtra) {
        List<File> tempFileList = new ArrayList<>();
        Luban.with(this)
                .load(stringArrayListExtra)
                .ignoreBy(100)// 原图小于100kb 不压缩
                .setCompressListener(new OnCompressListener() {
                    @Override
                    public void onStart() {
                        Log.e("zq", "开始压缩");
                    }

                    @Override
                    public void onSuccess(File file) {
                        tempFileList.add(file);

                        if (tempFileList.size() == stringArrayListExtra.size()) {
                            //已经压缩完毕
                            uploadImage(tempFileList);
                        }
                    }

                    @Override
                    public void onError(Throwable e) {

                    }
                }).launch();// 启动压缩
    }

    private void uploadImage(List<File> tempFileList) {

        List<String> filePathList = new ArrayList<>();
        for (int i = 0; i < tempFileList.size(); i++) {
            filePathList.add(tempFileList.get(i).getPath());
        }

        new Thread(new Runnable() {
            @Override
            public void run() {
                Map<String, String> params = new HashMap<>();
                params.put("access_token", coreManager.getSelfStatus().accessToken);
                params.put("userId", coreManager.getSelf().getUserId());
                params.put("validTime", "-1");// 文件有效期

                String result = new UploadService().uploadFile(coreManager.getConfig().UPLOAD_URL, params, filePathList);

                runOnUiThread(new Runnable() {


                    @Override
                    public void run() {
                        if (!TextUtils.isEmpty(result)) {
                            UploadFileResult recordResult = JSON.parseObject(result, UploadFileResult.class);
                            if (recordResult != null
                                    && recordResult.getData() != null
                                    && recordResult.getData().getImages() != null
                                    && recordResult.getData().getImages().size() > 0) {
                                uploadSuccessCount = 0;
                                for (int i = 0; i < recordResult.getData().getImages().size(); i++) {
                                    uploadEmotImage(recordResult.getData().getImages().get(i).getOriginalUrl()
                                            , recordResult.getData().getImages().get(i).getOriginalFileName()
                                            , recordResult.getData().getImages().size());
                                }
                            }

                        }
                    }
                });

            }
        }).start();

    }

    /**
     * @param url         图片地址
     * @param fileName    图片文件名
     * @param uploadCount 总共要收藏的图片数量
     */
    private void uploadEmotImage(String url, String fileName, int uploadCount) {
        Map<String, String> params = new HashMap<>();
        params.put("access_token", coreManager.getSelfStatus().accessToken);
        params.put("userId", coreManager.getSelf().getUserId());
        params.put("faceName", fileName);
        params.put("url", url);
        params.put("faceId", "");
        DialogHelper.showDefaulteMessageProgressDialog(this);
        HttpUtils.get().url(coreManager.getConfig().API_FACE_COLLECT_ADD)
                .params(params)
                .build()
                .execute(new ListCallback<EmotBean>(EmotBean.class) {
                    @Override
                    public void onResponse(ArrayResult<EmotBean> result) {
                        DialogHelper.dismissProgressDialog();
                        if (Result.checkSuccess(mContext, result)) {
                            ToastUtil.showToast(mContext, "收藏成功");
                            uploadSuccessCount++;
                            if (uploadSuccessCount == uploadCount) {
                                loadMySingleEmot(true);
                            }
                        } else {
                            ToastUtil.showToast(mContext, result.getResultMsg());
                        }
                    }

                    @Override
                    public void onError(Call call, Exception e) {
                        ToastUtil.showNetError(AddSingleEmotPackageActivity.this);
                        DialogHelper.dismissProgressDialog();
                    }
                });
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        DialogHelper.dismissProgressDialog();
    }
}