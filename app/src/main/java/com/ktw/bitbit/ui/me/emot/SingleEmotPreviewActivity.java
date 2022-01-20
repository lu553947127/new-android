package com.ktw.bitbit.ui.me.emot;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.os.Bundle;
import android.text.TextUtils;
import android.widget.Toast;

import com.bumptech.glide.Glide;
import com.bumptech.glide.load.engine.DiskCacheStrategy;
import com.ktw.bitbit.R;
import com.ktw.bitbit.broadcast.OtherBroadcast;
import com.ktw.bitbit.helper.AvatarHelper;
import com.ktw.bitbit.ui.base.BaseActivity;
import com.ktw.bitbit.view.ZoomImageView;

/**
 * 单张表情预览
 */
public class SingleEmotPreviewActivity extends BaseActivity {

    public final static String SINGLE_EMOT_URL = "emot_url";

    private String mImagePath;

    private ZoomImageView mImageView;

    private My_BroadcastReceiver my_broadcastReceiver = new My_BroadcastReceiver();


    public static void start(Context ctx, String url) {
        Intent intent = new Intent(ctx, SingleEmotPreviewActivity.class);
        intent.putExtra(SINGLE_EMOT_URL, url);
        ctx.startActivity(intent);
    }

    @SuppressWarnings("unused")
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_single_image_preview);

        if (getIntent() != null) {
            mImagePath = getIntent().getStringExtra(SINGLE_EMOT_URL);
        }

        initView();
        register();
    }

    public void doBack() {
        finish();
        overridePendingTransition(0, 0);// 关闭过场动画
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

    @Override
    protected void onDestroy() {
        super.onDestroy();
    }

    private void initView() {
        getSupportActionBar().hide();
        mImageView = findViewById(R.id.image_view);
        if (TextUtils.isEmpty(mImagePath)) {
            Toast.makeText(mContext, R.string.image_not_found, Toast.LENGTH_SHORT).show();
            return;
        }
        if (mImagePath.endsWith(".gif")) {
            Glide.with(SingleEmotPreviewActivity.this).load(mImagePath).asGif().diskCacheStrategy(DiskCacheStrategy.SOURCE).into(mImageView);
        } else {
            AvatarHelper.getInstance().displayUrl(mImagePath, mImageView);
        }
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
    }

    private void register() {
        IntentFilter filter = new IntentFilter();
        filter.addAction(OtherBroadcast.singledown);
        registerReceiver(my_broadcastReceiver, filter);
    }


    class My_BroadcastReceiver extends BroadcastReceiver {
        @Override
        public void onReceive(Context context, Intent intent) {
            if (intent.getAction().equals(OtherBroadcast.singledown)) {
                // 轻触屏幕，退出预览
                doBack();
            }
        }

    }
}