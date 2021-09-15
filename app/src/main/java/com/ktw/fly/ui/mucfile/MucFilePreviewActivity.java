package com.ktw.fly.ui.mucfile;

import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.os.Bundle;
import android.view.View;
import android.webkit.WebView;
import android.widget.ImageView;
import android.widget.RelativeLayout;
import android.widget.TextView;

import com.ktw.fly.R;
import com.ktw.fly.helper.ImageLoadHelper;
import com.ktw.fly.ui.base.BaseActivity;
import com.ktw.fly.ui.mucfile.bean.MucFileBean;
import com.ktw.fly.ui.tool.WebViewActivity;
import com.ktw.fly.view.ZoomImageView;

import java.io.File;

import static com.ktw.fly.ui.tool.WebViewActivity.EXTRA_URL;

public class MucFilePreviewActivity extends BaseActivity {
    private RelativeLayout rlImg;
    private RelativeLayout rlOther;
    private ZoomImageView ivType;
    private WebView mWebView;
    private MucFileBean data;
    private RelativeLayout rlOffice;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_muc_file_preview);
        getSupportActionBar().hide();
        findViewById(R.id.iv_title_left).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                finish();
            }
        });
        TextView tvTitle = (TextView) findViewById(R.id.tv_title_center);
        tvTitle.setText(getString(R.string.file_preview));

        data = (MucFileBean) getIntent().getSerializableExtra("data");
        initViews();
        initDatas();
    }

    /**
     * @see XfileUtils#setFileInco(int, ImageView)
     */
    private void initDatas() {
        if (data.getType() == 1) {
            rlImg.setVisibility(View.VISIBLE);

            File file = new File(DownManager.instance().getFileDir(), data.getName());
            if (file.exists()) {
                // 取本地的图片
                Bitmap bitmap = BitmapFactory.decodeFile(file.getAbsolutePath());
                ivType.setImageBitmap(bitmap);
            } else {
                ImageLoadHelper.showImage(
                        MucFilePreviewActivity.this,
                        data.getUrl(),
                        ivType
                );
            }

        } else if (data.getType() == 4 || data.getType() == 5 || data.getType() == 6 || data.getType() == 10) {
            rlOffice.setVisibility(View.VISIBLE);
            String url = "https://view.officeapps.live.com/op/view.aspx?src=" + data.getUrl();
            Intent intent = new Intent(this, WebViewActivity.class);
            intent.putExtra(EXTRA_URL, url);
            startActivity(intent);
            mWebView.getSettings().setJavaScriptEnabled(true);
            mWebView.getSettings().setUseWideViewPort(true);
            mWebView.loadUrl(url);
        } else {
            rlOther.setVisibility(View.VISIBLE);
        }
    }

    private void initViews() {
        rlImg = (RelativeLayout) findViewById(R.id.rl_file_type_img);
        rlOther = (RelativeLayout) findViewById(R.id.rl_file_type_other);
        rlOffice = (RelativeLayout) findViewById(R.id.rl_file_type_office);
        ivType = (ZoomImageView) findViewById(R.id.iv_type_img);
        mWebView = (WebView) findViewById(R.id.web_file_office);
    }
}
