package com.ktw.fly.ui.account;

import android.app.Activity;
import android.app.AlertDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.res.ColorStateList;
import android.net.Uri;
import android.os.Bundle;
import android.text.TextUtils;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.core.widget.ImageViewCompat;

import com.alibaba.fastjson.JSON;
import com.ktw.fly.wxapi.WXHelper;
import com.loopj.android.http.AsyncHttpClient;
import com.loopj.android.http.AsyncHttpResponseHandler;
import com.loopj.android.http.RequestParams;
import com.ktw.fly.FLYApplication;
import com.ktw.fly.R;
import com.ktw.fly.FLYReporter;
import com.ktw.fly.bean.Area;
import com.ktw.fly.bean.User;
import com.ktw.fly.bean.WXUserInfo;
import com.ktw.fly.bean.event.MessageLogin;
import com.ktw.fly.db.dao.AreasDao;
import com.ktw.fly.helper.AvatarHelper;
import com.ktw.fly.helper.DialogHelper;
import com.ktw.fly.helper.ImageLoadHelper;
import com.ktw.fly.helper.LoginHelper;
import com.ktw.fly.helper.LoginSecureHelper;
import com.ktw.fly.helper.PrivacySettingHelper;
import com.ktw.fly.helper.QQHelper;
import com.ktw.fly.map.MapHelper;
import com.ktw.fly.ui.base.BaseActivity;
import com.ktw.fly.ui.tool.ButtonColorChange;
import com.ktw.fly.ui.tool.SelectAreaActivity;
import com.ktw.fly.util.AsyncUtils;
import com.ktw.fly.util.CameraUtil;
import com.ktw.fly.util.DateSelectHelper;
import com.ktw.fly.util.DeviceInfoUtil;
import com.ktw.fly.util.EventBusHelper;
import com.ktw.fly.util.FileUtil;
import com.ktw.fly.util.LogUtils;
import com.ktw.fly.util.SkinUtils;
import com.ktw.fly.util.StringUtils;
import com.ktw.fly.util.TimeUtils;
import com.ktw.fly.util.ToastUtil;
import com.ktw.fly.util.UiUtils;
import com.ktw.fly.view.TipDialog;
import com.xuan.xuanhttplibrary.okhttp.result.Result;

import org.apache.http.Header;

import java.io.File;
import java.io.FileNotFoundException;
import java.util.HashMap;
import java.util.Map;
import java.util.Objects;

import de.greenrobot.event.Subscribe;
import de.greenrobot.event.ThreadMode;
import me.iwf.photopicker.PhotoPicker;

/**
 * 注册-3.基本资料
 */
public class RegisterUserBasicInfoActivity extends BaseActivity implements View.OnClickListener {

    private static final int REQUEST_CODE_CAPTURE_CROP_PHOTO = 1;
    private static final int REQUEST_CODE_PICK_CROP_PHOTO = 2;
    private static final int REQUEST_CODE_CROP_PHOTO = 3;
    public static int isRegisteredSyncCount = 0;
    private ImageView mAvatarImg;
    private EditText mNameEdit;
    private TextView mSexTv;
    private TextView mBirthdayTv;
    private TextView mCityTv;
    private Button mNextStepBtn;
    /* 前面页面传递进来的四个参数，都是必填 */
    private String mobilePrefix;
    private String mAccount;
    private String mPassword;
    private String mSmsCode;
    // 可能empty但不会null,
    private String mInviteCode;
    private String thirdToken;
    private String thirdTokenType;
    // Temp
    private User mTempData;
    // 选择头像的数据
    private File mCurrentFile;
    private boolean isSelectAvatar;
    private Uri mNewPhotoUri;
    private int mLoginType;

    public RegisterUserBasicInfoActivity() {
        noLoginRequired();
    }

    public static void start(
            Context ctx,
            String mobilePrefix,
            String account,
            String password,
            String smsCode,
            String inviteCode,
            String thirdToken,
            String thirdTokenType,
            int loginType) {
        Intent intent = new Intent(ctx, RegisterUserBasicInfoActivity.class);
        intent.putExtra(RegisterActivity.EXTRA_AUTH_CODE, mobilePrefix);
        intent.putExtra(RegisterActivity.EXTRA_PHONE_NUMBER, account);
        intent.putExtra(RegisterActivity.EXTRA_INVITE_CODE, inviteCode);
        intent.putExtra(RegisterActivity.EXTRA_PASSWORD, password);
        intent.putExtra(RegisterActivity.EXTRA_SMS_CODE, smsCode);
        intent.putExtra("thirdToken", thirdToken);
        intent.putExtra("thirdTokenType", thirdTokenType);
        intent.putExtra("loginType", loginType);
        ctx.startActivity(intent);
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_register_user_basic_info);
        if (getIntent() != null) {
            mobilePrefix = getIntent().getStringExtra(RegisterActivity.EXTRA_AUTH_CODE);
            mAccount = getIntent().getStringExtra(RegisterActivity.EXTRA_PHONE_NUMBER);
            mPassword = getIntent().getStringExtra(RegisterActivity.EXTRA_PASSWORD);
            mSmsCode = getIntent().getStringExtra(RegisterActivity.EXTRA_SMS_CODE);
            mInviteCode = getIntent().getStringExtra(RegisterActivity.EXTRA_INVITE_CODE);
            thirdToken = getIntent().getStringExtra("thirdToken");
            thirdTokenType = getIntent().getStringExtra("thirdTokenType");
            mLoginType = getIntent().getIntExtra("loginType", -1);
        }
        initActionBar();
        initView();
        requestLocationCity();

        if (!TextUtils.isEmpty(thirdToken)) {
            if (TextUtils.equals(LoginActivity.THIRD_TYPE_QQ, thirdTokenType)) {
                QQHelper.requestUserInfo(mContext, thirdToken, userInfo -> {
                    mTempData.setNickName(userInfo.getNickname());
                    if (TextUtils.equals("男", userInfo.getGender())) {
                        mTempData.setSex(1);
                    } else {
                        mTempData.setSex(0);
                    }
                    String headImageUrl = userInfo.getFigureurlQq();
                    if (TextUtils.isEmpty(headImageUrl)) {
                        headImageUrl = userInfo.getFigureurlQq2();
                    }
                    if (TextUtils.isEmpty(headImageUrl)) {
                        headImageUrl = userInfo.getFigureurlQq1();
                    }
                    uploadThirdHeadImage(headImageUrl);
                });
            } else if (TextUtils.equals(LoginActivity.THIRD_TYPE_WECHAT, thirdTokenType)) {
                AsyncUtils.doAsync(this, t -> {
                    LogUtils.log(thirdToken);
                    FLYReporter.post("获取微信个人资料失败，", t);
                }, c -> {
                    WXUserInfo userInfo = WXHelper.requestUserInfo(thirdToken);
                    // 微信 0 未知 1男 2女 需要转换为我们自己定义的性别 0 女 1 男
                    if (userInfo.getSex() == 2) {
                        userInfo.setSex(0);
                    }
                    mTempData.setSex(userInfo.getSex());// 未知时默认为女
                    mTempData.setNickName(userInfo.getNickname());
                    // 微信头像直接上传服务器，服务器会压缩失败，导致服务器上只有原图地址，没有缩略图地址
                    c.uiThread(registerUserBasicInfoActivity -> {
                        uploadThirdHeadImage(userInfo.getHeadimgurl());
                    });
                });
            }
        }
        EventBusHelper.register(this);
    }

    private void uploadThirdHeadImage(String headImageUrl) {
        ImageLoadHelper.loadBitmapCenterCropDontAnimate(
                mContext,
                headImageUrl,
                b -> {
                    String path = FileUtil.saveBitmap(b);
                    mCurrentFile = new File(path);
                    if (mCurrentFile.exists()) {
                        isSelectAvatar = true;
                        ImageLoadHelper.showFile(
                                mContext, mCurrentFile, mAvatarImg
                        );
                    } else {
                        mCurrentFile = null;
                        ToastUtil.showToast(mContext, getString(R.string.load_avatar_failed));
                    }
                }, e -> {
                    ToastUtil.showToast(mContext, getString(R.string.load_avatar_failed));
                }
        );
        updateUI();
    }

    @Subscribe(threadMode = ThreadMode.MainThread)
    public void helloEventBus(MessageLogin message) {
        finish();
    }

    private void initActionBar() {
        getSupportActionBar().hide();
        findViewById(R.id.iv_title_left).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                doBack();
            }
        });
        TextView tvTitle = (TextView) findViewById(R.id.tv_title_center);
        tvTitle.setText(R.string.base_info);
    }

    private void initView() {
        mAvatarImg = (ImageView) findViewById(R.id.avatar_img);
        mNameEdit = (EditText) findViewById(R.id.name_edit);
        mSexTv = (TextView) findViewById(R.id.sex_tv);
        mBirthdayTv = (TextView) findViewById(R.id.birthday_tv);
        mCityTv = (TextView) findViewById(R.id.city_tv);
        mNextStepBtn = (Button) findViewById(R.id.next_step_btn);
        ButtonColorChange.colorChange(this, mNextStepBtn);

        mAvatarImg.setOnClickListener(this);
        ImageViewCompat.setImageTintList(mAvatarImg, ColorStateList.valueOf(SkinUtils.getSkin(this).getAccentColor()));
        findViewById(R.id.sex_select_rl).setOnClickListener(this);
        findViewById(R.id.birthday_select_rl).setOnClickListener(this);
//        if (coreManager.getConfig().disableLocationServer) {
//            findViewById(R.id.city_select_rl).setVisibility(View.GONE);
//        } else {
//            findViewById(R.id.city_select_rl).setOnClickListener(this);
//        }
        findViewById(R.id.city_select_rl).setVisibility(View.GONE);
        mNextStepBtn.setOnClickListener(this);

        updateUI();
    }

    private void updateUI() {
        if (mTempData == null) {
            mTempData = new User();
            mTempData.setSex(1);
            mTempData.setBirthday(TimeUtils.sk_time_current_time() / 1000);
        }
        if (!TextUtils.isEmpty(mTempData.getNickName())) {
            mNameEdit.setText(mTempData.getNickName());
        }
        if (mTempData.getSex() == 1) {
            mSexTv.setText(R.string.sex_man);
        } else {
            mSexTv.setText(R.string.sex_woman);
        }
        mBirthdayTv.setText(TimeUtils.sk_time_s_long_2_str_for_birthday(mTempData.getBirthday()));
    }

    @Override
    public void onClick(View v) {
        switch (v.getId()) {
            case R.id.avatar_img:
                showSelectAvatarDialog();
                break;
            case R.id.sex_select_rl:
                showSelectSexDialog();
                break;
            case R.id.birthday_select_rl:
                showSelectBirthdayDialog();
                break;
            case R.id.city_select_rl:
                Intent intent = new Intent(RegisterUserBasicInfoActivity.this, SelectAreaActivity.class);
                intent.putExtra(SelectAreaActivity.EXTRA_AREA_PARENT_ID, Area.AREA_DATA_CHINA_ID);// 直接为中国
                intent.putExtra(SelectAreaActivity.EXTRA_AREA_TYPE, Area.AREA_TYPE_PROVINCE);
                intent.putExtra(SelectAreaActivity.EXTRA_AREA_DEEP, Area.AREA_TYPE_CITY);// 选择的深度为城市级别
                startActivityForResult(intent, 4);
                break;
            case R.id.next_step_btn:
                if (UiUtils.isNormalClick(v)) {
                    if (mLoginType==LoginHelper.LOGIN_PHONE){
                        register();
                    }else {
                        emailRegister();
                    }
                }
                break;
        }
    }

    //邮箱注册
    private void emailRegister() {
        loadPageData();

        if (TextUtils.isEmpty(mTempData.getNickName())) {
            mNameEdit.requestFocus();
            mNameEdit.setError(StringUtils.editTextHtmlErrorTip(this, R.string.name_empty_error));
            return;
        }

        if (!isSelectAvatar) {
            DialogHelper.tip(this, getString(R.string.must_select_avatar_can_register));
            return;
        }

        Map<String, String> params = new HashMap<>();
        // 前面页面传递的信息
        params.put("userType", "0");
        params.put("mailbox", mAccount);
        params.put("password", mPassword);
        params.put("mailboxCode", mSmsCode);
        params.put("smsCode", mSmsCode);
        if (!TextUtils.isEmpty(mInviteCode)) {
            params.put("inviteCode", mInviteCode);
        }
        // 本页面信息
        params.put("nickname", mTempData.getNickName());
        params.put("sex", String.valueOf(mTempData.getSex()));
        params.put("birthday", String.valueOf(mTempData.getBirthday()));
        params.put("xmppVersion", "1");
        params.put("countryId", String.valueOf(mTempData.getCountryId()));
        params.put("provinceId", String.valueOf(mTempData.getProvinceId()));
        params.put("cityId", String.valueOf(mTempData.getCityId()));
        params.put("areaId", String.valueOf(mTempData.getAreaId()));

        params.put("isSmsRegister", String.valueOf(RegisterActivity.isSmsRegister));

        // 附加信息
        params.put("apiVersion", DeviceInfoUtil.getVersionCode(mContext) + "");
        params.put("model", DeviceInfoUtil.getModel());
        params.put("osVersion", DeviceInfoUtil.getOsVersion());
        params.put("serial", DeviceInfoUtil.getDeviceId(mContext));
        // 地址信息
        double latitude = FLYApplication.getInstance().getBdLocationHelper().getLatitude();
        double longitude = FLYApplication.getInstance().getBdLocationHelper().getLongitude();
        String location = FLYApplication.getInstance().getBdLocationHelper().getAddress();
        if (latitude != 0)
            params.put("latitude", String.valueOf(latitude));
        if (longitude != 0)
            params.put("longitude", String.valueOf(longitude));
        if (!TextUtils.isEmpty(location))
            params.put("location", location);
        DialogHelper.showDefaulteMessageProgressDialog(this);

        LoginSecureHelper.secureEmailRegister(
                this, coreManager, thirdToken, thirdTokenType,
                params,
                t -> {
                    DialogHelper.dismissProgressDialog();
                    ToastUtil.showToast(this, this.getString(R.string.tip_login_secure_place_holder, t.getMessage()));
                }, result -> {
                    DialogHelper.dismissProgressDialog();
                    if (!Result.checkSuccess(getApplicationContext(), result)) {
                        if (result == null) {
                            FLYReporter.post("注册失败，result为空");
                        } else {
                            FLYReporter.post("注册失败，" + result.toString());
                        }
                        return;
                    }
                    // 注册成功
                    boolean success = LoginHelper.setLoginUser(RegisterUserBasicInfoActivity.this, coreManager, mAccount, mPassword, result);
                    if (success) {
                        isRegisteredSyncCount = 3;
                        // 新注册的账号没有支付密码，
                        FLYApplication.getInstance().initPayPassword(result.getData().getUserId(), 0);
                        PrivacySettingHelper.setPrivacySettings(RegisterUserBasicInfoActivity.this, result.getData().getSettings());
                        FLYApplication.getInstance().initMulti();
                        if (mCurrentFile != null && mCurrentFile.exists()) {
                            // 选择了头像，那么先上传头像
                            uploadAvatar(result.getData().getIsupdate(), mCurrentFile);
                            return;
                        } else {
                            // 没有选择头像，直接进入程序主页
                            // startActivity(new Intent(RegisterUserBasicInfoActivity.this, DataDownloadActivity.class));
                            DataDownloadActivity.start(mContext, result.getData().getIsupdate());
                            finish();
                        }
                        ToastUtil.showToast(RegisterUserBasicInfoActivity.this, R.string.register_success);
                    } else {
                        // 失败
                        if (TextUtils.isEmpty(result.getResultMsg())) {
                            ToastUtil.showToast(RegisterUserBasicInfoActivity.this, R.string.register_error);
                        } else {
                            ToastUtil.showToast(RegisterUserBasicInfoActivity.this, result.getResultMsg());
                        }
                    }
                });
    }

    private void showSelectAvatarDialog() {
//        String[] items = new String[]{getString(R.string.photograph), "" + getString(R.string.album)};
//        AlertDialog.Builder builder = new AlertDialog.Builder(this).setTitle(R.string.select_avatar).setSingleChoiceItems(items, 0,
//                new DialogInterface.OnClickListener() {
//                    @Override
//                    public void onClick(DialogInterface dialog, int which) {
//                        dialog.dismiss();
//                        if (which == 0) {
//                            takePhoto();
//                        } else {
//                            selectPhoto();
//                        }
//                    }
//                });
//        builder.show();
        PhotoPicker.builder()
                //设置图片选择数量
                .setPhotoCount(1)
                //取消选择时点击图片浏览
                .setPreviewEnabled(false)
                //选择界面第一个显示拍照按钮
                .setShowCamera(true)
                //开启裁剪
                .setCrop(true)
                //设置裁剪比例(X,Y)
                .setCropXY(1, 1)
//                设置裁剪界面标题栏颜色，设置裁剪界面状态栏颜色
//                .setCropColors(R.color.colorPrimary, R.color.colorPrimaryDark)
                .start(this);
//

    }

    private void takePhoto() {
        mNewPhotoUri = CameraUtil.getOutputMediaFileUri(this, CameraUtil.MEDIA_TYPE_IMAGE);
        CameraUtil.captureImage(this, mNewPhotoUri, REQUEST_CODE_CAPTURE_CROP_PHOTO);
    }

    private void selectPhoto() {
        CameraUtil.pickImageSimple(this, REQUEST_CODE_PICK_CROP_PHOTO);
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);

        if (resultCode == RESULT_OK) {
            switch (requestCode) {
                case PhotoPicker.CROP_CODE:
                    String path = data.getStringExtra(PhotoPicker.KEY_CAMEAR_PATH);
                    File file = new File(Objects.requireNonNull(data.getStringExtra(PhotoPicker.KEY_CAMEAR_PATH)));
                    mCurrentFile = new File(path);
                    isSelectAvatar = true;
                    AvatarHelper.getInstance().displayUrl(Uri.parse(path).toString(), mAvatarImg);
                    break;
            }
        }


        if (requestCode == REQUEST_CODE_CAPTURE_CROP_PHOTO) {
            // 拍照返回再去裁减
            if (resultCode == Activity.RESULT_OK) {
                if (mNewPhotoUri != null) {
                    Uri o = mNewPhotoUri;
                    mNewPhotoUri = CameraUtil.getOutputMediaFileUri(this, CameraUtil.MEDIA_TYPE_IMAGE);
                    CameraUtil.cropImage(this, o, mNewPhotoUri, REQUEST_CODE_CROP_PHOTO, 1, 1, 300, 300);
                } else {
                    ToastUtil.showToast(this, R.string.c_photo_album_failed);
                }
            }
        } else if (requestCode == REQUEST_CODE_PICK_CROP_PHOTO) {
            // 选择一张图片,然后立即调用裁减
            if (resultCode == Activity.RESULT_OK) {
                if (data != null && data.getData() != null) {
                    Uri o = data.getData();
                    mNewPhotoUri = CameraUtil.getOutputMediaFileUri(this, CameraUtil.MEDIA_TYPE_IMAGE);
                    CameraUtil.cropImage(this, o, mNewPhotoUri, REQUEST_CODE_CROP_PHOTO, 1, 1, 300, 300);
                } else {
                    ToastUtil.showToast(this, R.string.c_photo_album_failed);
                }
            }
        } else if (requestCode == REQUEST_CODE_CROP_PHOTO) {
            if (resultCode == Activity.RESULT_OK) {
                isSelectAvatar = true;
                if (mNewPhotoUri != null) {
                    mCurrentFile = new File(mNewPhotoUri.getPath());
                    AvatarHelper.getInstance().displayUrl(mNewPhotoUri.toString(), mAvatarImg);
                } else {
                    ToastUtil.showToast(this, R.string.c_crop_failed);
                }
            }
        } else if (requestCode == 4) {
            // 选择城市
            if (resultCode == RESULT_OK && data != null) {
                int countryId = data.getIntExtra(SelectAreaActivity.EXTRA_COUNTRY_ID, 0);
                int provinceId = data.getIntExtra(SelectAreaActivity.EXTRA_PROVINCE_ID, 0);
                int cityId = data.getIntExtra(SelectAreaActivity.EXTRA_CITY_ID, 0);
                int countyId = data.getIntExtra(SelectAreaActivity.EXTRA_COUNTY_ID, 0);

                String province_name = data.getStringExtra(SelectAreaActivity.EXTRA_PROVINCE_NAME);
                String city_name = data.getStringExtra(SelectAreaActivity.EXTRA_CITY_NAME);
                /*String county_name = data.getStringExtra(SelectAreaActivity.EXTRA_COUNTY_ID);*/
                mCityTv.setText(province_name + "-" + city_name);

                mTempData.setCountryId(countryId);
                mTempData.setProvinceId(provinceId);
                mTempData.setCityId(cityId);
                mTempData.setAreaId(countyId);
            }
        }
    }

    private void showSelectSexDialog() {
        String[] sexs = new String[]{getString(R.string.sex_man), getString(R.string.sex_woman)};
        new AlertDialog.Builder(this).setTitle(R.string.select_sex)
                .setSingleChoiceItems(sexs, mTempData.getSex() == 1 ? 0 : 1, new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        if (which == 0) {
                            mTempData.setSex(1);
                            mSexTv.setText(R.string.sex_man);
                        } else {
                            mTempData.setSex(0);
                            mSexTv.setText(R.string.sex_woman);
                        }
                        dialog.dismiss();
                    }
                }).setCancelable(true).create().show();
    }

    @SuppressWarnings("deprecation")
    private void showSelectBirthdayDialog() {
        /*Date date = new Date(mTempData.getBirthday() * 1000);
        DatePickerDialog dialog = new DatePickerDialog(this, new DatePickerDialog.OnDateSetListener() {
            @Override
            public void onDateSet(DatePicker view, int year, int monthOfYear, int dayOfMonth) {
                GregorianCalendar calendar = new GregorianCalendar(year, monthOfYear, dayOfMonth);
                mTempData.setBirthday(TimeUtils.getSpecialBeginTime(mBirthdayTv, calendar.getTime().getTime() / 1000));
            }
        }, date.getYear() + 1900, date.getMonth(), date.getDate());

        DatePicker datePicker = dialog.getDatePicker();
        datePicker.setMaxDate(new Date().getTime());
        dialog.show();*/

        DateSelectHelper dialog = DateSelectHelper.getInstance(RegisterUserBasicInfoActivity.this);
        dialog.setDateMin("1900-1-1");
        dialog.setDateMax(System.currentTimeMillis());
        dialog.setCurrentDate(mTempData.getBirthday() * 1000);
        dialog.setOnDateSetListener(new DateSelectHelper.OnDateResultListener() {
            @Override
            public void onDateSet(long time, String dateFromat) {
                mTempData.setBirthday(time / 1000);
                mBirthdayTv.setText(dateFromat);
            }
        });

        dialog.show();
    }

    private void loadPageData() {
        mTempData.setNickName(mNameEdit.getText().toString().trim());
    }

    private void register() {
        loadPageData();

        if (TextUtils.isEmpty(mTempData.getNickName())) {
            mNameEdit.requestFocus();
            mNameEdit.setError(StringUtils.editTextHtmlErrorTip(this, R.string.name_empty_error));
            return;
        }

        /*if (!StringUtils.isNickName(mTempData.getNickName())) {
            mNameEdit.requestFocus();
            mNameEdit.setError(StringUtils.editTextHtmlErrorTip(this, R.string.nick_name_format_error));
            return;
        }*/

//        if (!coreManager.getConfig().disableLocationServer) {
//            if (mTempData.getCityId() <= 0) {
//                TipDialog tipDialog = new TipDialog(this);
//                tipDialog.setTip(getString(R.string.live_address_empty_error));
//                tipDialog.show();
//                return;
//            }
//        }

        if (!isSelectAvatar) {
            DialogHelper.tip(this, getString(R.string.must_select_avatar_can_register));
            return;
        }

        Map<String, String> params = new HashMap<>();
        // 前面页面传递的信息
        params.put("userType", "0");
        params.put("telephone", mAccount);
        params.put("password", mPassword);
        params.put("smsCode", mSmsCode);
        if (!TextUtils.isEmpty(mInviteCode)) {
            params.put("inviteCode", mInviteCode);
        }
        params.put("areaCode", mobilePrefix);//TODO AreaCode 区号暂时不带
        // 本页面信息
        params.put("nickname", mTempData.getNickName());
        params.put("sex", String.valueOf(mTempData.getSex()));
        params.put("birthday", String.valueOf(mTempData.getBirthday()));
        params.put("xmppVersion", "1");
        params.put("countryId", String.valueOf(mTempData.getCountryId()));
        params.put("provinceId", String.valueOf(mTempData.getProvinceId()));
        params.put("cityId", String.valueOf(mTempData.getCityId()));
        params.put("areaId", String.valueOf(mTempData.getAreaId()));

        params.put("isSmsRegister", String.valueOf(RegisterActivity.isSmsRegister));

        // 附加信息
        params.put("apiVersion", DeviceInfoUtil.getVersionCode(mContext) + "");
        params.put("model", DeviceInfoUtil.getModel());
        params.put("osVersion", DeviceInfoUtil.getOsVersion());
        params.put("serial", DeviceInfoUtil.getDeviceId(mContext));
        // 地址信息
        double latitude = FLYApplication.getInstance().getBdLocationHelper().getLatitude();
        double longitude = FLYApplication.getInstance().getBdLocationHelper().getLongitude();
        String location = FLYApplication.getInstance().getBdLocationHelper().getAddress();
        if (latitude != 0)
            params.put("latitude", String.valueOf(latitude));
        if (longitude != 0)
            params.put("longitude", String.valueOf(longitude));
        if (!TextUtils.isEmpty(location))
            params.put("location", location);
        DialogHelper.showDefaulteMessageProgressDialog(this);

        LoginSecureHelper.secureRegister(
                this, coreManager, thirdToken, thirdTokenType,
                params,
                t -> {
                    DialogHelper.dismissProgressDialog();
                    ToastUtil.showToast(this, this.getString(R.string.tip_login_secure_place_holder, t.getMessage()));
                }, result -> {
                    DialogHelper.dismissProgressDialog();
                    if (!com.xuan.xuanhttplibrary.okhttp.result.Result.checkSuccess(getApplicationContext(), result)) {
                        if (result == null) {
                            FLYReporter.post("注册失败，result为空");
                        } else {
                            FLYReporter.post("注册失败，" + result.toString());
                        }
                        return;
                    }
                    // 注册成功
                    boolean success = LoginHelper.setLoginUser(RegisterUserBasicInfoActivity.this, coreManager, mAccount, mPassword, result);
                    if (success) {
                        isRegisteredSyncCount = 3;
                        // 新注册的账号没有支付密码，
                        FLYApplication.getInstance().initPayPassword(result.getData().getUserId(), 0);
                        PrivacySettingHelper.setPrivacySettings(RegisterUserBasicInfoActivity.this, result.getData().getSettings());
                        FLYApplication.getInstance().initMulti();
                        if (mCurrentFile != null && mCurrentFile.exists()) {
                            // 选择了头像，那么先上传头像
                            uploadAvatar(result.getData().getIsupdate(), mCurrentFile);
                            return;
                        } else {
                            // 没有选择头像，直接进入程序主页
                            // startActivity(new Intent(RegisterUserBasicInfoActivity.this, DataDownloadActivity.class));
                            DataDownloadActivity.start(mContext, result.getData().getIsupdate());
                            finish();
                        }
                        ToastUtil.showToast(RegisterUserBasicInfoActivity.this, R.string.register_success);
                    } else {
                        // 失败
                        if (TextUtils.isEmpty(result.getResultMsg())) {
                            ToastUtil.showToast(RegisterUserBasicInfoActivity.this, R.string.register_error);
                        } else {
                            ToastUtil.showToast(RegisterUserBasicInfoActivity.this, result.getResultMsg());
                        }
                    }
                });
    }

    /**
     * 自动定位...
     */
    private void requestLocationCity() {
        MapHelper.getInstance().requestLatLng(new MapHelper.OnSuccessListener<MapHelper.LatLng>() {
            @Override
            public void onSuccess(MapHelper.LatLng latLng) {
                MapHelper.getInstance().requestCityName(latLng, new MapHelper.OnSuccessListener<String>() {
                    @Override
                    public void onSuccess(String s) {
                        String cityName = FLYApplication.getInstance().getBdLocationHelper().getCityName();
                        Area area = null;
                        if (!TextUtils.isEmpty(cityName)) {
                            area = AreasDao.getInstance().searchByName(cityName);
                        }
                        if (area != null) {
                            Area countryArea = null;
                            Area provinceArea = null;
                            Area cityArea = null;
                            Area countyArea = null;
                            switch (area.getType()) {
                                case Area.AREA_TYPE_COUNTRY:
                                    countryArea = area;
                                    break;
                                case Area.AREA_TYPE_PROVINCE:
                                    provinceArea = area;
                                    break;
                                case Area.AREA_TYPE_CITY:
                                    cityArea = area;
                                    break;
                                case Area.AREA_TYPE_COUNTY:
                                default:
                                    countyArea = area;
                                    break;
                            }
                            if (countyArea != null) {
                                mTempData.setAreaId(countyArea.getId());
                                cityArea = AreasDao.getInstance().getArea(countyArea.getParent_id());
                            }

                            if (cityArea != null) {
                                mTempData.setCityId(cityArea.getId());
                                mCityTv.setText(cityArea.getName());
                                provinceArea = AreasDao.getInstance().getArea(cityArea.getParent_id());
                            }

                            if (provinceArea != null) {
                                mTempData.setProvinceId(provinceArea.getId());
                                countryArea = AreasDao.getInstance().getArea(provinceArea.getParent_id());
                            }

                            if (countryArea != null) {
                                mTempData.setCountryId(countryArea.getId());
                            }
                        } else {
                            Log.e(TAG, "获取地区失败，", new RuntimeException("找不到城市：" + cityName));
                        }
                    }
                }, new MapHelper.OnErrorListener() {
                    @Override
                    public void onError(Throwable t) {
                        Log.e(TAG, "获取城市名称失败，", t);

                    }
                });
            }
        }, new MapHelper.OnErrorListener() {
            @Override
            public void onError(Throwable t) {
                Log.e(TAG, "定位经纬度失败，", t);
            }
        });
    }


    @Override
    public void onBackPressed() {
        doBack();
    }

    @Override
    protected boolean onHomeAsUp() {
        doBack();
        return true;
    }

    private void doBack() {
        TipDialog tipDialog = new TipDialog(this);
        tipDialog.setmConfirmOnClickListener(getString(R.string.cancel_register_prompt), new TipDialog.ConfirmOnClickListener() {
            @Override
            public void confirm() {
                finish();
            }
        });
        tipDialog.show();
    }

    private void uploadAvatar(int isupdate, File file) {
        if (!file.exists()) {
            // 文件不存在
            return;
        }
        // 显示正在上传的ProgressDialog
        DialogHelper.showMessageProgressDialog(this, getString(R.string.upload_avataring));
        RequestParams params = new RequestParams();
        String loginUserId = coreManager.getSelf().getUserId();
        params.put("userId", loginUserId);
        try {
            params.put("file1", file);
        } catch (FileNotFoundException e) {
            e.printStackTrace();
        }

        AsyncHttpClient client = new AsyncHttpClient();
        client.post(coreManager.getConfig().AVATAR_UPLOAD_URL, params, new AsyncHttpResponseHandler() {
            @Override
            public void onSuccess(int arg0, Header[] arg1, byte[] arg2) {
                boolean success = false;
                if (arg0 == 200) {
                    Result result = null;
                    try {
                        result = JSON.parseObject(new String(arg2), Result.class);
                    } catch (Exception e) {
                        e.printStackTrace();
                    }
                    if (result != null && result.getResultCode() == Result.CODE_SUCCESS) {
                        success = true;
                    }
                }

                DialogHelper.dismissProgressDialog();
                if (success) {
                    ToastUtil.showToast(RegisterUserBasicInfoActivity.this, R.string.upload_avatar_success);
                } else {
                    ToastUtil.showToast(RegisterUserBasicInfoActivity.this, R.string.upload_avatar_failed);
                }

                // startActivity(new Intent(RegisterUserBasicInfoActivity.this, DataDownloadActivity.class));
                DataDownloadActivity.start(mContext, isupdate);
                finish();
            }

            @Override
            public void onFailure(int arg0, Header[] arg1, byte[] arg2, Throwable arg3) {
                DialogHelper.dismissProgressDialog();
                ToastUtil.showToast(RegisterUserBasicInfoActivity.this, R.string.upload_avatar_failed);
                // startActivity(new Intent(RegisterUserBasicInfoActivity.this, DataDownloadActivity.class));
                DataDownloadActivity.start(mContext, isupdate);
                finish();
            }
        });
    }
}
