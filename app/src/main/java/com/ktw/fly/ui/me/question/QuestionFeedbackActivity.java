package com.ktw.fly.ui.me.question;

import android.app.Activity;
import android.content.Intent;
import android.content.res.ColorStateList;
import android.os.Bundle;
import android.text.TextUtils;
import android.util.Log;
import android.view.View;
import android.widget.AdapterView;
import android.widget.Button;
import android.widget.GridView;
import android.widget.TextView;

import androidx.annotation.Nullable;
import androidx.core.view.ViewCompat;

import com.alibaba.fastjson.JSON;
import com.ktw.fly.R;
import com.ktw.fly.adapter.FeedbackScreenshotAdapter;
import com.ktw.fly.bean.UploadFileResult;
import com.ktw.fly.helper.DialogHelper;
import com.ktw.fly.helper.UploadService;
import com.ktw.fly.ui.base.BaseActivity;
import com.ktw.fly.ui.me.emot.SingleEmotPreviewActivity;
import com.ktw.fly.util.SkinUtils;
import com.ktw.fly.util.ToastUtil;
import com.ktw.fly.view.ClearEditText;
import com.ktw.fly.view.photopicker.PhotoPickerActivity;
import com.ktw.fly.view.photopicker.SelectModel;
import com.ktw.fly.view.photopicker.intent.PhotoPickerIntent;
import com.xuan.xuanhttplibrary.okhttp.HttpUtils;
import com.xuan.xuanhttplibrary.okhttp.callback.BaseCallback;
import com.xuan.xuanhttplibrary.okhttp.result.ObjectResult;
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
 * 意见反馈
 */
public class QuestionFeedbackActivity extends BaseActivity {

    //0是投诉 1是建议
    public static final int TYPE_COMPLAINT = 0;
    public static final int TYPE_PROPOSAL = 1;


    private static final int REQUEST_CODE_PICK_PHOTO = 2;
    private final static int IMAGE_UPLOAD_MAX_COUNT = 9;

    private List<String> dataList = new ArrayList<>();
    private GridView gv_screenshot;
    private FeedbackScreenshotAdapter adapter;
    private TextView tv_screenshot_num;
    private ClearEditText edt_remark;
    private ClearEditText edt_phone;
    private Button btn_submit;
    private String phone;
    private String remark;
    private int type;
    private TextView tvTitle;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_question_feedback);
        getSupportActionBar().hide();
        findViewById(R.id.iv_title_left).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                finish();
            }
        });
        tvTitle = (TextView) findViewById(R.id.tv_title_center);

        initView();
        initData();
    }

    private void initData() {
        Intent intent = getIntent();
        if (intent != null) {
            type = intent.getIntExtra("type", TYPE_COMPLAINT);
            if (type == TYPE_COMPLAINT) {
                tvTitle.setText(R.string.complaints_suggestions);
            } else {
                tvTitle.setText(R.string.question_feedback);
            }
        }
    }

    private void initView() {
        tv_screenshot_num = findViewById(R.id.tv_screenshot_num);
        gv_screenshot = findViewById(R.id.gv_screenshot);
        edt_remark = findViewById(R.id.edt_remark);
        edt_phone = findViewById(R.id.edt_phone);
        btn_submit = findViewById(R.id.btn_submit);
        dataList.add("");
        adapter = new FeedbackScreenshotAdapter(this, dataList);
        adapter.setDelImgListener(new FeedbackScreenshotAdapter.IDelImgListener() {
            @Override
            public void delImg(int position) {
                dataList.remove(position);
                adapter.notifyDataSetChanged();
            }
        });
        gv_screenshot.setAdapter(adapter);

        gv_screenshot.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> adapterView, View view, int position, long l) {
                if (position == 0) {
                    if (dataList.size() - 1 == IMAGE_UPLOAD_MAX_COUNT) {
                        ToastUtil.showToast(mContext, "最多只能上传9张图片");
                        return;
                    }
                    //选择图片
                    PhotoPickerIntent intent = new PhotoPickerIntent(QuestionFeedbackActivity.this);
                    intent.setMaxTotal(IMAGE_UPLOAD_MAX_COUNT - (dataList.size() - 1));
                    intent.setSelectModel(SelectModel.MULTI);
                    startActivityForResult(intent, REQUEST_CODE_PICK_PHOTO);
                } else {
                    String imagePath = dataList.get(position);
                    if (!TextUtils.isEmpty(imagePath)) {
                        SingleEmotPreviewActivity.start(QuestionFeedbackActivity.this, imagePath);
                    }
                }
            }
        });
        btn_submit.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                phone = edt_phone.getText().toString().trim();
                if (TextUtils.isEmpty(phone)) {
                    ToastUtil.showToast(mContext, "请输入联系电话");
                    return;
                }
                if (dataList.size() - 1 == 0) {
                    ToastUtil.showToast(mContext, "请选择截取的图片");
                    return;
                }
                remark = edt_remark.getText().toString().trim();
                //压缩图片
                album(dataList.subList(1, dataList.size()));
            }
        });
        ViewCompat.setBackgroundTintList(btn_submit, ColorStateList.valueOf(SkinUtils.getSkin(this).getAccentColor()));
        refreshCount();
    }

    public void refreshCount() {
        String countContent = String.format(getString(R.string.feedback_img_num), dataList.size() - 1, IMAGE_UPLOAD_MAX_COUNT);
        tv_screenshot_num.setText(countContent);
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (resultCode == Activity.RESULT_OK) {
            if (requestCode == REQUEST_CODE_PICK_PHOTO) {
                if (data != null) {
                    List<String> filePathList = data.getStringArrayListExtra(PhotoPickerActivity.EXTRA_RESULT);
                    if (filePathList != null && filePathList.size() > 0) {
                        dataList.addAll(filePathList);
                        refreshCount();
                        adapter.notifyDataSetChanged();
                    }
                } else {
                    ToastUtil.showToast(this, R.string.c_photo_album_failed);
                }
            }
        }
    }

    // 多张图片压缩 相册
    private void album(List<String> stringArrayListExtra) {
        DialogHelper.showDefaulteMessageProgressDialog(mContext);
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
                        DialogHelper.dismissProgressDialog();
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
                                StringBuffer sb = new StringBuffer();
                                for (int i = 0; i < recordResult.getData().getImages().size(); i++) {
                                    sb.append(recordResult.getData().getImages().get(i).getOriginalUrl()).append(",");
                                }
                                String images = sb.toString();
                                if (!TextUtils.isEmpty(images)) {
                                    images = images.substring(0, images.length() - 1);
                                    submit(images);
                                }
                            }

                        }
                    }
                });

            }
        }).start();

    }

    private void submit(String imgs) {
        Map<String, String> params = new HashMap<>();
        params.put("phone", phone);
        params.put("imgs", imgs);
        params.put("content", remark);
        params.put("type", String.valueOf(type));
        HttpUtils.post().url(coreManager.getConfig().QUESTION_FEEDBACK)
                .params(params)
                .build()
                .execute(new BaseCallback<Void>(Void.class) {

                    @Override
                    public void onResponse(ObjectResult<Void> result) {
                        DialogHelper.dismissProgressDialog();
                        if (Result.checkSuccess(getApplicationContext(), result)) {
                            ToastUtil.showToast(mContext, "提交成功");
                            finish();
                        }
                    }

                    @Override
                    public void onError(Call call, Exception e) {
                        DialogHelper.dismissProgressDialog();
                        ToastUtil.showLongToast(mContext, "提交失败，请重试");
                    }
                });
    }


}
