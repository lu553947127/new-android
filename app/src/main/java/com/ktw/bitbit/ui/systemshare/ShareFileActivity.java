package com.ktw.bitbit.ui.systemshare;

import android.content.Context;
import android.content.Intent;
import android.content.res.ColorStateList;
import android.os.AsyncTask;
import android.os.Bundle;
import android.text.Editable;
import android.text.TextUtils;
import android.text.TextWatcher;
import android.util.Log;
import android.view.View;
import android.widget.CheckBox;
import android.widget.CompoundButton;
import android.widget.EditText;
import android.widget.FrameLayout;
import android.widget.ImageView;
import android.widget.RelativeLayout;
import android.widget.TextView;
import android.widget.Toast;

import androidx.core.view.ViewCompat;

import com.alibaba.fastjson.JSON;
import com.ktw.bitbit.FLYAppConstant;
import com.ktw.bitbit.R;
import com.ktw.bitbit.bean.Area;
import com.ktw.bitbit.bean.UploadFileResult;
import com.ktw.bitbit.helper.AvatarHelper;
import com.ktw.bitbit.helper.DialogHelper;
import com.ktw.bitbit.helper.ImageLoadHelper;
import com.ktw.bitbit.helper.LoginHelper;
import com.ktw.bitbit.helper.UploadService;
import com.ktw.bitbit.ui.account.LoginHistoryActivity;
import com.ktw.bitbit.ui.base.BaseActivity;
import com.ktw.bitbit.ui.circle.range.AtSeeCircleActivity;
import com.ktw.bitbit.ui.circle.range.SeeCircleActivity;
import com.ktw.bitbit.ui.circle.util.SendTextFilter;
import com.ktw.bitbit.ui.map.MapPickerActivity;
import com.ktw.bitbit.ui.tool.ButtonColorChange;
import com.ktw.bitbit.util.DeviceInfoUtil;
import com.ktw.bitbit.util.LogUtils;
import com.ktw.bitbit.util.SkinUtils;
import com.ktw.bitbit.util.ToastUtil;
import com.ktw.bitbit.util.log.FileUtils;
import com.ktw.bitbit.view.SelectFileDialog;
import com.ktw.bitbit.view.SelectionFrame;
import com.ktw.bitbit.view.TipDialog;
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

/**
 * ????????????
 */
public class ShareFileActivity extends BaseActivity implements View.OnClickListener {
    private static final int REQUEST_CODE_SELECT_LOCATE = 3;  // ??????
    private static final int REQUEST_CODE_SELECT_TYPE = 4;    // ????????????
    private static final int REQUEST_CODE_SELECT_REMIND = 5;  // ????????????
    private static boolean isBoolBan = false;
    private EditText mTextEdit;
    // ????????????
    private TextView mTVLocation;
    // ????????????
    private TextView mTVSee;
    // ????????????
    private TextView mTVAt;
    // File item
    private ImageView mAddFileIv;
    private RelativeLayout mSendFileRl;
    private ImageView mFileIv;
    private TextView mFileNameTv;
    // data
    private String mFilePath;
    private SelectionFrame mSelectionFrame;
    // ???????????? || ???????????? ?????? ?????????????????????????????????
    private String str1;
    private String str2;
    private String str3;
    // ???????????????
    private int visible = 1;
    // ???????????? || ????????????
    private String lookPeople;
    // ????????????
    private String atlookPeople;
    // ??????????????????
    private double latitude;
    private double longitude;
    private String address;
    private String mFileData;
    private CheckBox checkBox;
    // Video Item
    private FrameLayout mFloatLayout;
    private ImageView mImageView;
    private ImageView mIconImageView;
    private TextView mVideoTextTv;

    public static void start(Context ctx, Intent intent) {
        intent.setClass(ctx, ShareFileActivity.class);
        ctx.startActivity(intent);
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_send_file);
        initActionBar();
        initView();
        initEvent();

        String text = ShareUtil.parseText(getIntent());
        if (!TextUtils.isEmpty(text)) {
            mTextEdit.setText(text);
        }
        File file = ShareUtil.getFileFromStream(this, getIntent());
        if (file != null) {
            putFile(file);
        }
    }

    private void initActionBar() {
        getSupportActionBar().hide();
        findViewById(R.id.iv_title_left).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                isExitNoPublish();
            }
        });
        TextView tvTitle = (TextView) findViewById(R.id.tv_title_center);
        tvTitle.setText(getString(R.string.public_a_file));
    }

    private void initView() {
        checkBox = findViewById(R.id.cb_ban);
        checkBox.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
                isBoolBan = isChecked;
                checkBox.setChecked(isBoolBan);
                if (isBoolBan) {
                    ButtonColorChange.checkChange(mContext, checkBox);
                } else {
                    checkBox.setButtonDrawable(getResources().getDrawable(R.mipmap.prohibit_icon));
                }
            }
        });
        RelativeLayout rl_ban = findViewById(R.id.rl_ban);
        rl_ban.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                isBoolBan = !isBoolBan;
                Log.e("zx", "onClick: rl_ban  " + isBoolBan);
                checkBox.setChecked(isBoolBan);
                if (isBoolBan) {
                    ButtonColorChange.checkChange(mContext, checkBox);
                } else {
                    checkBox.setButtonDrawable(getResources().getDrawable(R.mipmap.prohibit_icon));
                }
            }
        });
        mTextEdit = (EditText) findViewById(R.id.text_edit);
        // ??????EditText???ScrollView???????????????
        mTextEdit.setOnTouchListener((v, event) -> {
            v.getParent().requestDisallowInterceptTouchEvent(true);
            return false;
        });
        // ?????????EditText?????????????????????600????????????????????????
        mTextEdit.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {

            }

            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {

            }

            @Override
            public void afterTextChanged(Editable s) {
                if (mTextEdit.getText().toString().trim().length() >= 10000) {
                    Toast.makeText(mContext, getString(R.string.tip_edit_max_input_length, 10000), Toast.LENGTH_SHORT).show();
                }
            }
        });
        mTextEdit.setHint(getString(R.string.add_msg_mind));
        // ????????????
        mTVLocation = (TextView) findViewById(R.id.tv_location);
        // ????????????
        mTVSee = (TextView) findViewById(R.id.tv_see);
        // ????????????
        mTVAt = (TextView) findViewById(R.id.tv_at);

        TextView tv_title_right = (TextView) findViewById(R.id.tv_title_right);
        tv_title_right.setText(getResources().getString(R.string.circle_release));
        tv_title_right.setBackground(mContext.getResources().getDrawable(R.drawable.bg_btn_grey_circle));
        ViewCompat.setBackgroundTintList(tv_title_right, ColorStateList.valueOf(SkinUtils.getSkin(this).getAccentColor()));
        tv_title_right.setTextColor(getResources().getColor(R.color.white));
        tv_title_right.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (TextUtils.isEmpty(mFilePath)) {
                    Toast.makeText(mContext, getString(R.string.add_file), Toast.LENGTH_SHORT).show();
                    return;
                }
                new UploadTask().execute();
            }
        });
        mSendFileRl = findViewById(R.id.send_file_rl);
        mFileIv = findViewById(R.id.file_img);
        mFileNameTv = findViewById(R.id.file_name);

        mFloatLayout = findViewById(R.id.float_layout);
        mImageView = (ImageView) findViewById(R.id.image_view);
        mIconImageView = (ImageView) findViewById(R.id.icon_image_view);
        mIconImageView.setBackgroundResource(R.mipmap.send_file);
        mVideoTextTv = (TextView) findViewById(R.id.text_tv);
        mVideoTextTv.setText(R.string.circle_select_file);
    }

    private void initEvent() {
        if (coreManager.getConfig().disableLocationServer) {
            findViewById(R.id.rl_location).setVisibility(View.GONE);
        } else {
            findViewById(R.id.rl_location).setOnClickListener(this);
        }
        findViewById(R.id.rl_see).setOnClickListener(this);
        findViewById(R.id.rl_at).setOnClickListener(this);

        mFloatLayout.setOnClickListener(v -> {
            SelectFileDialog dialog = new SelectFileDialog(mContext, new SelectFileDialog.OptionFileListener() {
                @Override
                public void option(List<File> files) {
                    SelectFileDialog dialog = new SelectFileDialog(ShareFileActivity.this, new SelectFileDialog.OptionFileListener() {
                        @Override
                        public void option(List<File> files) {
                            if (files != null && files.size() > 0) {
                                File file = files.get(0);// ?????????????????????????????????????????????
                                putFile(file);
                            }
                        }

                        @Override
                        public void intent() {
                        }
                    });
                    dialog.maxOpt = 1;// ??????????????????????????????
                    dialog.show();
                }

                @Override
                public void intent() {
                    Intent intent = new Intent(Intent.ACTION_OPEN_DOCUMENT);
                    intent.setType("*/*");//???????????????????????????????????????????????????????????????????????????
                    intent.addCategory(Intent.CATEGORY_OPENABLE);
                    startActivityForResult(intent, FileUtils.REQUEST_CODE_SELECT_FILE);
                }
            });
            dialog.maxOpt = 1;// ??????????????????????????????
            dialog.show();
        });
    }

    private void putFile(File file) {
        mFilePath = file.getPath();

        int index = mFilePath.lastIndexOf(".");
        if (index != -1) {
            String type = mFilePath.substring(index + 1).toLowerCase();
            if (type.equals("png") || type.equals("jpg")) {
                ImageLoadHelper.showImageWithError(
                        mContext,
                        mFilePath,
                        R.drawable.image_download_fail_icon,
                        mFileIv
                );
            } else {
                AvatarHelper.getInstance().fillFileView(type, mFileIv);
            }
        }
        mFileNameTv.setText(file.getName());
        mSendFileRl.setVisibility(View.VISIBLE);
    }

    @Override
    public void onClick(View v) {
        switch (v.getId()) {
            case R.id.rl_location:
                // ????????????
                Intent intent1 = new Intent(this, MapPickerActivity.class);
                startActivityForResult(intent1, REQUEST_CODE_SELECT_LOCATE);
                break;
            case R.id.rl_see:
                // ????????????
                Intent intent2 = new Intent(this, SeeCircleActivity.class);
                intent2.putExtra("THIS_CIRCLE_TYPE", visible - 1);
                intent2.putExtra("THIS_CIRCLE_PERSON_RECOVER1", str1);
                intent2.putExtra("THIS_CIRCLE_PERSON_RECOVER2", str2);
                intent2.putExtra("THIS_CIRCLE_PERSON_RECOVER3", str3);
                startActivityForResult(intent2, REQUEST_CODE_SELECT_TYPE);
                break;
            case R.id.rl_at:
                // ????????????
                if (visible == 2) {
                    final TipDialog tipDialog = new TipDialog(this);
                    tipDialog.setmConfirmOnClickListener(getString(R.string.tip_private_cannot_use_this), new TipDialog.ConfirmOnClickListener() {
                        @Override
                        public void confirm() {
                            tipDialog.dismiss();
                        }
                    });
                    tipDialog.show();
                } else {
                    Intent intent3 = new Intent(this, AtSeeCircleActivity.class);
                    intent3.putExtra("REMIND_TYPE", visible);
                    intent3.putExtra("REMIND_PERSON", lookPeople);
                    intent3.putExtra("REMIND_SELECT_PERSON", atlookPeople);
                    startActivityForResult(intent3, REQUEST_CODE_SELECT_REMIND);
                }
                break;
        }
    }

    @Override
    public void onBackPressed() {
        isExitNoPublish();
    }

    private void isExitNoPublish() {
        if (!TextUtils.isEmpty(mFilePath)) {
            mSelectionFrame = new SelectionFrame(mContext);
            mSelectionFrame.setSomething(getString(R.string.app_name), getString(R.string.tip_has_file_no_public), new SelectionFrame.OnSelectionFrameClickListener() {
                @Override
                public void cancelClick() {

                }

                @Override
                public void confirmClick() {
                    finish();
                }
            });
            mSelectionFrame.show();
        } else {
            finish();
        }
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        if (requestCode == 1 && resultCode == RESULT_OK && data != null) {

        } else if (resultCode == RESULT_OK && requestCode == FileUtils.REQUEST_CODE_SELECT_FILE) {
            mFilePath = FileUtils.getPath(mContext, data.getData());
            if (mFilePath == null) {
                ToastUtil.showToast(mContext, R.string.tip_file_not_supported);
                return;
            }

            int index = mFilePath.lastIndexOf(".");
            if (index != -1) {
                String type = mFilePath.substring(index + 1).toLowerCase();
                if (type.equals("png") || type.equals("jpg")) {
                    ImageLoadHelper.showImageWithError(
                            mContext,
                            mFilePath,
                            R.drawable.image_download_fail_icon,
                            mFileIv
                    );
                } else {
                    AvatarHelper.getInstance().fillFileView(type, mFileIv);
                }
            }

            File file = new File(mFilePath);
            mFileNameTv.setText(file.getName());
            mSendFileRl.setVisibility(View.VISIBLE);
        } else if (resultCode == RESULT_OK && requestCode == REQUEST_CODE_SELECT_LOCATE) {
            // ??????????????????
            latitude = data.getDoubleExtra(FLYAppConstant.EXTRA_LATITUDE, 0);
            longitude = data.getDoubleExtra(FLYAppConstant.EXTRA_LONGITUDE, 0);
            address = data.getStringExtra(FLYAppConstant.EXTRA_ADDRESS);
            if (latitude != 0 && longitude != 0 && !TextUtils.isEmpty(address)) {
                Log.e("zq", "??????:" + latitude + "   ?????????" + longitude + "   ?????????" + address);
                mTVLocation.setText(address);
            } else {
                ToastUtil.showToast(mContext, getString(R.string.loc_startlocnotice));
            }
        } else if (resultCode == RESULT_OK && requestCode == REQUEST_CODE_SELECT_TYPE) {
            // ??????????????????
            int mOldVisible = visible;
            visible = data.getIntExtra("THIS_CIRCLE_TYPE", 1);
            // ?????????????????????????????????????????????????????????????????????
            if (mOldVisible != visible
                    || visible == 3 || visible == 4) {
                // ???????????????????????? 3/4 ???????????????????????????????????????????????????????????????
                atlookPeople = "";
                mTVAt.setText("");
            }
            if (visible == 1) {
                mTVSee.setText(R.string.publics);
            } else if (visible == 2) {
                mTVSee.setText(R.string.privates);
                if (!TextUtils.isEmpty(atlookPeople)) {
                    final TipDialog tipDialog = new TipDialog(this);
                    tipDialog.setmConfirmOnClickListener(getString(R.string.tip_private_cannot_notify), new TipDialog.ConfirmOnClickListener() {
                        @Override
                        public void confirm() {
                            tipDialog.dismiss();
                        }
                    });
                    tipDialog.show();
                }
            } else if (visible == 3) {
                lookPeople = data.getStringExtra("THIS_CIRCLE_PERSON");
                String looKenName = data.getStringExtra("THIS_CIRCLE_PERSON_NAME");
                mTVSee.setText(looKenName);
            } else if (visible == 4) {
                lookPeople = data.getStringExtra("THIS_CIRCLE_PERSON");
                String lookName = data.getStringExtra("THIS_CIRCLE_PERSON_NAME");
                mTVSee.setText(getString(R.string.not_allow, lookName));
            }
            str1 = data.getStringExtra("THIS_CIRCLE_PERSON_RECOVER1");
            str2 = data.getStringExtra("THIS_CIRCLE_PERSON_RECOVER2");
            str3 = data.getStringExtra("THIS_CIRCLE_PERSON_RECOVER3");
        } else if (resultCode == RESULT_OK && requestCode == REQUEST_CODE_SELECT_REMIND) {
            // ??????????????????
            atlookPeople = data.getStringExtra("THIS_CIRCLE_REMIND_PERSON");
            String atLookPeopleName = data.getStringExtra("THIS_CIRCLE_REMIND_PERSON_NAME");
            mTVAt.setText(atLookPeopleName);
        }
    }

    public void sendFile() {
        Map<String, String> params = new HashMap<String, String>();
        params.put("access_token", coreManager.getSelfStatus().accessToken);
        // ???????????????1=???????????????2=???????????????3=???????????????4=???????????? 5=???????????????
        params.put("type", "5");
        // ???????????????1??????????????????2??????????????????3??????????????????
        params.put("flag", "3");

        // ?????????????????????1=?????????2=?????????3=???????????????????????????4=????????????
        params.put("visible", String.valueOf(visible));
        if (visible == 3) {
            // ????????????
            params.put("userLook", lookPeople);
        } else if (visible == 4) {
            // ????????????
            params.put("userNotLook", lookPeople);
        }
        // ????????????
        if (!TextUtils.isEmpty(atlookPeople)) {
            params.put("userRemindLook", atlookPeople);
        }
        params.put("isAllowComment", isBoolBan ? String.valueOf(1) : String.valueOf(0));

        // ????????????
        params.put("text", SendTextFilter.filter(mTextEdit.getText().toString()));
        params.put("files", mFileData);

        /**
         * ????????????
         */
        if (!TextUtils.isEmpty(address)) {
            // ??????
            params.put("latitude", String.valueOf(latitude));
            // ??????
            params.put("longitude", String.valueOf(longitude));
            // ??????
            params.put("location", address);
        }

        // ?????????????????????????????????????????????????????????????????????????????????
        Area area = Area.getDefaultCity();
        if (area != null) {
            params.put("cityId", String.valueOf(area.getId()));// ??????Id
        } else {
            params.put("cityId", "0");
        }

        /**
         * ????????????
         */
        // ????????????
        params.put("model", DeviceInfoUtil.getModel());
        // ???????????????????????????
        params.put("osVersion", DeviceInfoUtil.getOsVersion());
        if (!TextUtils.isEmpty(DeviceInfoUtil.getDeviceId(mContext))) {
            // ???????????????
            params.put("serialNumber", DeviceInfoUtil.getDeviceId(mContext));
        }

        DialogHelper.showDefaulteMessageProgressDialog(mContext);

        HttpUtils.post().url(coreManager.getConfig().MSG_ADD_URL)
                .params(params)
                .build()
                .execute(new BaseCallback<String>(String.class) {

                    @Override
                    public void onResponse(ObjectResult<String> result) {
                        DialogHelper.dismissProgressDialog();
                        if (com.xuan.xuanhttplibrary.okhttp.result.Result.checkSuccess(mContext, result)) {
                            Intent intent = new Intent();
                            intent.putExtra(FLYAppConstant.EXTRA_MSG_ID, result.getData());
                            setResult(RESULT_OK, intent);
                            finish();
                        }
                    }

                    @Override
                    public void onError(Call call, Exception e) {
                        DialogHelper.dismissProgressDialog();
                        ToastUtil.showErrorNet(mContext);
                    }
                });
    }

    private class UploadTask extends AsyncTask<Void, Integer, Integer> {
        private String message;

        @Override
        protected void onPreExecute() {
            super.onPreExecute();
            DialogHelper.showDefaulteMessageProgressDialog(mContext);
        }

        /**
         * ?????????????????? <br/>
         * return 1 Token???????????????????????? <br/>
         * return 2 ?????????????????????????????? <br/>
         * return 3 ????????????<br/>
         * return 4 ????????????<br/>
         */
        @Override
        protected Integer doInBackground(Void... params) {
            if (!LoginHelper.isTokenValidation()) {
                return 1;
            }
            if (TextUtils.isEmpty(mFilePath)) {
                return 2;
            }

            Map<String, String> mapParams = new HashMap<String, String>();
            mapParams.put("access_token", coreManager.getSelfStatus().accessToken);
            mapParams.put("userId", coreManager.getSelf().getUserId() + "");
            mapParams.put("validTime", "-1");// ???????????????

            List<String> dataList = new ArrayList<String>();
            dataList.add(mFilePath);

            String result = new UploadService().uploadFile(coreManager.getConfig().UPLOAD_URL, mapParams, dataList);
            if (TextUtils.isEmpty(result)) {
                return 3;
            }

            LogUtils.log("????????????<" + mFilePath + ">?????????" + result);
            UploadFileResult recordResult = JSON.parseObject(result, UploadFileResult.class);
            boolean success = Result.defaultParser(mContext, recordResult, true);
            if (success) {
                if (recordResult.getSuccess() != recordResult.getTotal()) {// ???????????????????????????
                    return 3;
                }
                if (recordResult.getData() != null) {
                    UploadFileResult.Data data = recordResult.getData();
/*
                    if (data.getFiles() != null && data.getFiles().size() > 0) {
                        while (data.getFiles().size() > 1) {// ???????????????????????????????????????????????????????????????????????????
                            data.getFiles().removeItemMessage(data.getFiles().size() - 1);
                        }
                        data.getFiles().get(0).setSize(new File(mFilePath).length());
                        mFileData = JSON.toJSONString(data.getFiles(), UploadFileResult.sImagesFilter);
                    } else {
                        return 3;
                    }
*/
                    if (data.getImages() != null && data.getImages().size() > 0) {
                        data.getImages().get(0).setSize(new File(mFilePath).length());
                        mFileData = JSON.toJSONString(data.getImages());
                    } else if (data.getAudios() != null && data.getAudios().size() > 0) {
                        data.getAudios().get(0).setSize(new File(mFilePath).length());
                        mFileData = JSON.toJSONString(data.getAudios());
                    } else if (data.getVideos() != null && data.getVideos().size() > 0) {
                        data.getVideos().get(0).setSize(new File(mFilePath).length());
                        mFileData = JSON.toJSONString(data.getVideos());
                    } else if (data.getOthers() != null && data.getOthers().size() > 0) {
                        data.getOthers().get(0).setSize(new File(mFilePath).length());
                        mFileData = JSON.toJSONString(data.getOthers());
                    } else {
                        return 3;
                    }

                    return 4;
                } else {// ??????????????????????????????
                    return 3;
                }
            } else {
                return 3;
            }
        }

        @Override
        protected void onPostExecute(Integer result) {
            super.onPostExecute(result);
            if (result == 1) {
                DialogHelper.dismissProgressDialog();
                startActivity(new Intent(mContext, LoginHistoryActivity.class));
            } else if (result == 2) {
                DialogHelper.dismissProgressDialog();
                ToastUtil.showToast(mContext, getString(R.string.alert_not_have_file));
            } else if (result == 3) {
                DialogHelper.dismissProgressDialog();
                ToastUtil.showToast(mContext, R.string.upload_failed);
            } else {
                sendFile();
            }
        }
    }
}
