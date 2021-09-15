package com.ktw.fly.fragment;


import android.content.ActivityNotFoundException;
import android.content.Intent;
import android.net.Uri;
import android.net.http.SslError;
import android.os.Build;
import android.os.Bundle;
import android.util.Log;
import android.view.KeyEvent;
import android.view.View;
import android.webkit.CookieManager;
import android.webkit.CookieSyncManager;
import android.webkit.SslErrorHandler;
import android.webkit.WebChromeClient;
import android.webkit.WebSettings;
import android.webkit.WebView;
import android.webkit.WebViewClient;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;

import com.ktw.fly.R;
import com.ktw.fly.ui.base.EasyFragment;
import com.ktw.fly.util.UiUtils;
import com.ktw.fly.view.SkinTextView;

/**
 * 导航2
 */
public class Nav2Fragment extends EasyFragment implements View.OnClickListener {

    private WebView mWebView;
    private ProgressBar mLoadBar;
    private WebSettings mWs;
    public static String homeUrl;
    public static String title;
    private TextView mTvTitle;

    public Nav2Fragment
            () {
    }

    @Override
    protected int inflateLayoutId() {
        return R.layout.fragment_venice;
    }

    @Override
    protected void onActivityCreated(Bundle savedInstanceState, boolean createView) {
        if (createView) {
            initView();
        }
    }

    @Override
    public void onResume() {
        super.onResume();
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
    }

    private void initActionBar() {
        findViewById(R.id.iv_title_left).setVisibility(View.GONE);
        mTvTitle = (TextView) findViewById(R.id.tv_title_center);
        mTvTitle.setText(title);

        SkinTextView mTvLeftTitle = findViewById(R.id.tv_title_left);
        mTvLeftTitle.setText("主页");
        mTvLeftTitle.setOnClickListener(view -> mWebView.loadUrl(homeUrl));

        SkinTextView mTvRightTitle = findViewById(R.id.tv_title_right);
        mTvRightTitle.setText("刷新");
        mTvRightTitle.setOnClickListener(view -> mWebView.reload());
    }

    private void initView() {

        initActionBar();

        mWebView = findViewById(R.id.wv_web);
        mLoadBar = findViewById(R.id.pb_load_bar);

//        ivHome = findViewById(R.id.ivHome);
//        ivHome.setOnClickListener(new View.OnClickListener() {
//            @Override
//            public void onClick(View view) {
//                mWebView.loadUrl(homeUrl);
//            }
//        });

        mWs = mWebView.getSettings();
        // 设置可以支持缩放
        mWs.setSupportZoom(true);
        // 设置出现缩放工具
        mWs.setBuiltInZoomControls(true);
        //设置可在大视野范围内上下左右拖动，并且可以任意比例缩放
        mWs.setUseWideViewPort(true);
        //设置默认加载的可视范围是大视野范围
        mWs.setLoadWithOverviewMode(true);
        //自适应屏幕
        mWs.setLayoutAlgorithm(WebSettings.LayoutAlgorithm.SINGLE_COLUMN);
        //支持js
        mWs.setJavaScriptEnabled(true);

        mWebView.setWebChromeClient(new WebChromeClient() {
            @Override
            public void onProgressChanged(WebView webView, int progress) {
                mLoadBar.setProgress(progress);
            }
        });

        mWebView.getSettings().setDomStorageEnabled(true);

        mWebView.setWebViewClient(new WebViewClient() {

            @Override
            public void onReceivedSslError(WebView view, SslErrorHandler handler, SslError error) {
                handler.proceed();
            }

            @Override
            public void onPageFinished(WebView webView, String url) {
                mLoadBar.setVisibility(View.GONE);

                CookieManager cookieManager = CookieManager.getInstance();
                cookieManager.setAcceptCookie(true);
                String endCookie = cookieManager.getCookie(url);
                Log.i("XWEBVIEW", "onPageFinished: endCookie : " + endCookie);
                if (Build.VERSION.SDK_INT < Build.VERSION_CODES.LOLLIPOP) {
                    CookieSyncManager.getInstance().sync();//同步cookie
                } else {
                    CookieManager.getInstance().flush();
                }

//                if (homeUrl.equalsIgnoreCase(url)) {
//                    LogUtil.d("回到首页");
//                    ivHome.setVisibility(View.GONE);
//                } else {
//                    if (ivHome.getVisibility() == View.GONE) {
//                        ivHome.setVisibility(View.VISIBLE);
//                    }
//                }
            }

            @Override
            public boolean shouldOverrideUrlLoading(WebView view, String url) {
                //返回值是true的时候控制去WebView打开，
                // 为false调用系统浏览器或第三方浏览器
                if (url.startsWith("http") || url.startsWith("https") || url.startsWith("ftp")) {
                    return false;
                } else {
                    try {
                        Intent intent = new Intent();
                        intent.setAction(Intent.ACTION_VIEW);
                        intent.setData(Uri.parse(url));
                        view.getContext().startActivity(intent);
                    } catch (ActivityNotFoundException e) {
                        Toast.makeText(view.getContext(), "手机还没有安装支持打开此网页的应用！", Toast.LENGTH_SHORT).show();
                    }
                    return true;
                }
            }
        });


        mWebView.loadUrl(homeUrl);

    }


    @Override
    public void onClick(View v) {
        if (!UiUtils.isNormalClick(v)) {
            return;
        }
        int id = v.getId();
        switch (id) {

        }
    }

    @Override
    public void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);

    }

    public boolean onKeyDown(int keyCode, KeyEvent event) {
        if (keyCode == KeyEvent.KEYCODE_BACK && mWebView.canGoBack()) {
            mWebView.goBack(); //goBack()表示返回WebView的上一页面
            return true;
        }
        return false;
    }

}
