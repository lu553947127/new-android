package com.ktw.fly.ui.me;

import android.os.Bundle;
import android.view.View;
import android.webkit.WebView;
import android.widget.ImageView;
import android.widget.TextView;

import com.ktw.fly.R;
import com.ktw.fly.ui.base.BaseActivity;
import com.ktw.fly.ui.tool.WebView2Activity;
import com.ktw.fly.ui.tool.WebViewActivity;

public class ThirdServiceActivity extends BaseActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_third_service);
        getSupportActionBar().hide();
        findViewById(R.id.iv_title_left).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                finish();
            }
        });
        TextView tvTitle = (TextView) findViewById(R.id.tv_title_center);
        tvTitle.setText("第三方服务");

        findViewById(R.id.tv_meituan).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                WebViewActivity.start(mContext,"http://i.meituan.com/");
            }
        });

        findViewById(R.id.tv_pinduoduo).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                WebViewActivity.start(mContext,"https://m.pinduoduo.com/home/");
            }
        });

        findViewById(R.id.tv_jd).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                WebViewActivity.start(mContext,"https://m.jd.com/");
            }
        });

    }

}
