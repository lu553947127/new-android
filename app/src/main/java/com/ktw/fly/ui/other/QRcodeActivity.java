package com.ktw.fly.ui.other;

import android.content.Intent;
import android.graphics.Bitmap;
import android.net.Uri;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.ImageView;
import android.widget.TextView;

import com.example.qrcode.utils.BitmapUtil;
import com.example.qrcode.utils.CommonUtils;
import com.ktw.fly.R;
import com.ktw.fly.FLYReporter;
import com.ktw.fly.bean.Friend;
import com.ktw.fly.db.dao.FriendDao;
import com.ktw.fly.helper.AvatarHelper;
import com.ktw.fly.helper.ImageLoadHelper;
import com.ktw.fly.ui.base.BaseActivity;
import com.ktw.fly.util.AsyncUtils;
import com.ktw.fly.util.AvatarUtil;
import com.ktw.fly.util.DisplayUtil;
import com.ktw.fly.util.FileUtil;
import com.ktw.fly.util.ScreenUtil;
import com.ktw.fly.util.SkinUtils;
import com.ktw.fly.view.MessageAvatar;
import com.watermark.androidwm_light.WatermarkBuilder;
import com.watermark.androidwm_light.bean.WatermarkImage;

import java.io.File;
import java.util.ArrayList;
import java.util.List;

/**
 * Created by Administrator on 2017/9/14 0014.
 * 二维码类
 */
public class QRcodeActivity extends BaseActivity {
    private ImageView qrcode;
    private TextView tv_name;
    private ImageView iv_remarks;
    private ImageView avatar;
    private MessageAvatar avatar_group;
    private boolean isgroup;
    private String userId;// 因为有变动，userId为通讯号
    private String userAvatar;// 真正的userId
    private String roomJid;
    private Friend friend;
    private String nickName;
    private int sex;

    private int sizePix;
    private Bitmap bitmapAva;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_im_code_image);
        if (getIntent() != null) {
            isgroup = getIntent().getBooleanExtra("isgroup", false);
            userId = getIntent().getStringExtra("userid");
            userAvatar = getIntent().getStringExtra("userAvatar");
            nickName = getIntent().getStringExtra("nickName");
            sex = getIntent().getIntExtra("sex", 3);
            if (isgroup) {
                roomJid = getIntent().getStringExtra("roomJid");
                friend = FriendDao.getInstance().getFriend(coreManager.getSelf().getUserId(), roomJid);
            }
        }
        sizePix = ScreenUtil.getScreenWidth(mContext) - 200;
        initActionBar();
        initView();
    }

    private void initActionBar() {
        getSupportActionBar().hide();
        findViewById(R.id.iv_title_left).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                finish();
            }
        });
        TextView tvTitle = (TextView) findViewById(R.id.tv_title_center);
        tvTitle.setText(getString(R.string.qrcode));
        ImageView mIvTitleRight = (ImageView) findViewById(R.id.iv_title_right);
        mIvTitleRight.setImageResource(R.mipmap.download_icon);
        mIvTitleRight.setVisibility(View.GONE);
        mIvTitleRight.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                FileUtil.saveImageToGallery2(mContext, getBitmap(QRcodeActivity.this.getWindow().getDecorView()));
            }
        });
    }

    private void initView() {
        qrcode = (ImageView) findViewById(R.id.qrcode);
        avatar = (ImageView) findViewById(R.id.avatar_img);
        avatar_group = findViewById(R.id.avatar_group);
        tv_name = findViewById(R.id.tv_name);
        iv_remarks = findViewById(R.id.iv_remarks);

        if (isgroup) {
            avatar_group.setVisibility(View.VISIBLE);
        } else {
            avatar.setVisibility(View.VISIBLE);
            iv_remarks.setImageResource(sex == 0 ? R.mipmap.basic_famale : R.mipmap.basic_male);
            iv_remarks.setVisibility(View.VISIBLE);
        }
        tv_name.setText(nickName);
        String str = coreManager.getConfig().website + "?action=" + (isgroup ? "group" : "user") + "&shikuId=" + userId;
        Log.e("zq", "二维码链接：" + str);

        // 生成二维码
        Bitmap bitmap = CommonUtils.createQRCode(str, sizePix, sizePix);
        // 显示 二维码 与 头像
        if (isgroup) {// 群组头像
            avatar_group.fillData(friend);
        } else {// 用户头像
            AvatarHelper.getInstance().displayAvatar(nickName, userAvatar, avatar, false);
        }
        // 此bitmap只为无头像的二维码
        qrcode.setImageBitmap(bitmap);
        // 将二维码和头像拼成一张bitmap
        drawQRcode(isgroup ? AvatarHelper.getGroupAvatarUrl(roomJid, false) : AvatarHelper.getAvatarUrl(userAvatar, false));
    }

    /**
     * @param url：头像url地址
     */
    private void drawQRcode(String url) {
        AsyncUtils.doAsync(this, t -> {
            FLYReporter.post("二维码头像加载失败", t);
        }, c -> {
            try {
                bitmapAva = ImageLoadHelper.getBitmapCenterCrop(mContext, url, DisplayUtil.dip2px(this, 40), DisplayUtil.dip2px(this, 40));
            } catch (Exception e) {// 抛出Exception，基本为url无效导致的，自己生成bitmap
                if (isgroup) {
                    if (friend != null) {
                        bitmapAva = AvatarHelper.getInstance().getBitmapCode(friend);
                    }
                } else {
                    List<Object> bitmapList = new ArrayList();
                    bitmapList.add(nickName);
                    bitmapAva = AvatarUtil.getBuilder(this)
                            .setShape(AvatarUtil.Shape.CIRCLE)
                            .setList(bitmapList)
                            .setTextSize(DisplayUtil.dip2px(this, 40))
                            .setTextColor(R.color.white)
                            .setTextBgColor(SkinUtils.getSkin(this).getAccentColor())
                            .setBitmapSize(DisplayUtil.dip2px(this, 40), DisplayUtil.dip2px(this, 40))
                            .create();
                }
            }
            // 将bitmapAva裁剪为圆形
            Bitmap bitmap = BitmapUtil.getCircleBitmap(bitmapAva);
            if (bitmap != null) {
                WatermarkImage watermarkImage = new WatermarkImage(bitmap)
                        .setImageAlpha(255)
                        .setPositionX(0.4)
                        .setPositionY(0.4)
                        .setRotation(0)
                        .setSize(0.2);

                WatermarkBuilder
                        .create(this, qrcode)
                        .loadWatermarkImage(watermarkImage)
                        .setTileMode(false)
                        .getWatermark()
                        .setToImageView(qrcode);
            } else {
                // todo 走到这里的话，基本上是未从缓存内得到群头像导致的，理论上不太可能
            }
        });
    }

    //分享一张图片  
    public void shareSingleImage(View view) {
        String imagePath = FileUtil.saveImageToGalleryString(mContext, userId, WatermarkBuilder.create(this, qrcode)
                .getWatermark().getOutputImage());
        Log.e("zx", "shareSingleImage: " + imagePath);
        Uri imageUri = null; //imagePath--本地的文件路径
        try {
            imageUri = Uri.fromFile(new File(imagePath));
            Log.d("share", "uri:" + imageUri);
        } catch (Exception e) {
            e.printStackTrace();
        }
        Intent shareIntent = new Intent();
        shareIntent.setAction(Intent.ACTION_SEND);
        shareIntent.putExtra(Intent.EXTRA_STREAM, imageUri);
        shareIntent.setType("image/*");
        startActivity(Intent.createChooser(shareIntent, getString(R.string.toShare)));
    }

    public void saveImageToGallery(View view) {
        FileUtil.saveImageToGallery2(mContext, WatermarkBuilder.create(this, qrcode)
                .getWatermark().getOutputImage());
    }

    /**
     * 获取这个view的缓存bitmap,
     */
    private Bitmap getBitmap(View view) {
        view.setDrawingCacheEnabled(true);
        view.buildDrawingCache();
        Bitmap result = Bitmap.createBitmap(view.getDrawingCache());
        view.destroyDrawingCache();
        view.setDrawingCacheEnabled(false);
        return result;
    }
}