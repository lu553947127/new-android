package com.ktw.fly.ui.other;


import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.net.Uri;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.TextView;

import com.bumptech.glide.Glide;
import com.example.qrcode.utils.BitmapUtil;
import com.example.qrcode.utils.CommonUtils;
import com.ktw.fly.FLYAppConfig;
import com.ktw.fly.R;
import com.ktw.fly.FLYReporter;
import com.ktw.fly.helper.ImageLoadHelper;
import com.ktw.fly.ui.base.BaseActivity;
import com.ktw.fly.util.AsyncUtils;
import com.ktw.fly.util.DisplayUtil;
import com.ktw.fly.util.FileUtil;
import com.ktw.fly.util.ScreenUtil;
import com.watermark.androidwm_light.WatermarkBuilder;
import com.watermark.androidwm_light.bean.WatermarkImage;

import java.io.File;

public class AppQrCodeActivity extends BaseActivity {
    private ImageView mAppLogoIv,mAppQrCodeIv;
    private TextView mAppNameTv;
    private Button mSaveQrCodeBtn,mShareQrCodeBtn;



    private int sizePix;
    private Bitmap bitmapAva;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_common_qr_code);

        sizePix = ScreenUtil.getScreenWidth(mContext) - 200;
        initActionBar();
        initView();
    }

    private void initActionBar() {
        getSupportActionBar().hide();
        findViewById(R.id.iv_title_left).setOnClickListener(v -> finish());
        TextView tvTitle = (TextView) findViewById(R.id.tv_title_center);
        tvTitle.setText(getString(R.string.qrcode));


    }

    private void initView() {
        mAppLogoIv = findViewById(R.id.iv_app_logo);
        mAppQrCodeIv = findViewById(R.id.iv_qr_code);
        mAppNameTv = findViewById(R.id.tv_app_name);
        mSaveQrCodeBtn = findViewById(R.id.btn_save_qr);
        mShareQrCodeBtn = findViewById(R.id.btn_share);
        mSaveQrCodeBtn.setOnClickListener(v -> {
            saveImageToGallery();

        });
        mShareQrCodeBtn.setOnClickListener(v -> {
            shareSingleImage();
        });


        mAppNameTv.setText(R.string.app_name);
        Glide.with(this).load(R.mipmap.ic_aa_logo).into(mAppLogoIv);


        String str = coreManager.getConfig().website + "?action=switchApp&domain=" + FLYAppConfig.HOST;


        // 生成二维码
        Bitmap bitmap = CommonUtils.createQRCode(str, sizePix, sizePix);

        // 此bitmap只为无头像的二维码
        mAppQrCodeIv.setImageBitmap(bitmap);
        // 将二维码和头像拼成一张bitmap
        drawQrCode(R.mipmap.ic_aa_logo);
    }


    private void drawQrCode(int resourceId) {
        AsyncUtils.doAsync(this, t -> {
            FLYReporter.post("二维码头像加载失败", t);
        }, c -> {
            try {
                bitmapAva = ImageLoadHelper.getBitmapCenterCrop(mContext, resourceId, DisplayUtil.dip2px(this, 40), DisplayUtil.dip2px(this, 40));
            } catch (Exception e) {// 抛出Exception，基本为url无效导致的，自己生成bitmap
                final BitmapFactory.Options options = new BitmapFactory.Options();

                bitmapAva = BitmapFactory.decodeResource(getResources(),R.mipmap.ic_aa_logo);
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
                        .create(this, mAppQrCodeIv)
                        .loadWatermarkImage(watermarkImage)
                        .setTileMode(false)
                        .getWatermark()
                        .setToImageView(mAppQrCodeIv);
            }
        });
    }

    //分享一张图片  
    public void shareSingleImage() {
        String imagePath = FileUtil.saveBitmap(WatermarkBuilder.create(this, mAppQrCodeIv)
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

    public void saveImageToGallery() {
        FileUtil.saveImageToGallery2(mContext, WatermarkBuilder.create(this, mAppQrCodeIv)
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