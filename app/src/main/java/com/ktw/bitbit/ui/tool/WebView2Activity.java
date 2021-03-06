package com.ktw.bitbit.ui.tool;

import android.app.Activity;
import android.content.ActivityNotFoundException;
import android.content.Intent;
import android.net.Uri;
import android.net.http.SslError;
import android.os.Build;
import android.os.Bundle;
import android.util.Log;
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

import com.ktw.bitbit.R;
import com.ktw.bitbit.ui.base.BaseActivity;
import com.ktw.bitbit.view.SkinTextView;

/**
 * web
 */
public class WebView2Activity extends BaseActivity {

    public static final String EXTRA_URL = "url";
    public static final String EXTRA_TITLE = "title";

    public static void start(Activity activity, String url, String title, int requestCode) {
        Intent intent = new Intent(activity, WebView2Activity.class);
        intent.putExtra(EXTRA_URL, url);
        intent.putExtra(EXTRA_TITLE, title);
        activity.startActivityForResult(intent, requestCode);
    }

    public WebView2Activity(){
        noConfigRequired();
        noLoginRequired();
    }

    private WebView mWebView;
    private ProgressBar mLoadBar;
    private WebSettings mWs;
    public static String homeUrl;
    public static String title;
    private TextView mTvTitle;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_web_view2);
        if (getIntent() != null) {
            homeUrl = getIntent().getStringExtra(EXTRA_URL);
            title = getIntent().getStringExtra(EXTRA_TITLE);
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
        getSupportActionBar().hide();
        mTvTitle = (TextView) findViewById(R.id.tv_title_center);
        mTvTitle.setText(title);
        mTvTitle.setTextColor(getResources().getColor(R.color.black_2));
        findViewById(R.id.iv_title_left).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                finish();
            }
        });
        SkinTextView mTvRightTitle = findViewById(R.id.tv_title_right);
        mTvRightTitle.setText("??????");
        mTvRightTitle.setTextColor(getResources().getColor(R.color.black_2));
        mTvRightTitle.setOnClickListener(view -> mWebView.reload());
    }

    private void initView() {

        initActionBar();

        mWebView = findViewById(R.id.mWebView);
        mLoadBar = findViewById(R.id.progressBar);

        mWs = mWebView.getSettings();
        // ????????????????????????
        mWs.setSupportZoom(true);
        // ????????????????????????
        mWs.setBuiltInZoomControls(true);
        //?????????????????????????????????????????????????????????????????????????????????
        mWs.setUseWideViewPort(true);
        //???????????????????????????????????????????????????
        mWs.setLoadWithOverviewMode(true);
        //???????????????
        mWs.setLayoutAlgorithm(WebSettings.LayoutAlgorithm.SINGLE_COLUMN);
        //??????js
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
                    CookieSyncManager.getInstance().sync();//??????cookie
                } else {
                    CookieManager.getInstance().flush();
                }

            }

            @Override
            public boolean shouldOverrideUrlLoading(WebView view, String url) {
                //????????????true??????????????????WebView?????????
                // ???false??????????????????????????????????????????
                if (url.startsWith("http") || url.startsWith("https") || url.startsWith("ftp")) {
                    return false;
                } else {
                    try {
                        Intent intent = new Intent();
                        intent.setAction(Intent.ACTION_VIEW);
                        intent.setData(Uri.parse(url));
                        view.getContext().startActivity(intent);
                    } catch (ActivityNotFoundException e) {
                        Toast.makeText(view.getContext(), "??????????????????????????????????????????????????????", Toast.LENGTH_SHORT).show();
                    }
                    return true;
                }
            }
        });

        mWebView.loadUrl(homeUrl);

    }


    @Override
    public void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);

    }

//    @Override
//    public boolean onKeyDown(int keyCode, KeyEvent event) {
//        if (keyCode == KeyEvent.KEYCODE_BACK && mWebView.canGoBack()) {
//            mWebView.goBack(); //goBack()????????????WebView???????????????
//            return true;
//        }
//        return false;
//    }

    @Override
    public void onBackPressed() {
        if (mWebView != null && mWebView.canGoBack()) {
            mWebView.goBack();
        } else {
            finish();
        }
    }
}
