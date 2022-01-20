package com.ktw.bitbit.ui.tool;

import android.animation.Animator;
import android.animation.AnimatorListenerAdapter;
import android.animation.ObjectAnimator;
import android.animation.ValueAnimator;
import android.content.ClipboardManager;
import android.content.Context;
import android.content.Intent;
import android.graphics.Bitmap;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.provider.MediaStore;
import android.text.TextUtils;
import android.util.Log;
import android.view.View;
import android.view.Window;
import android.view.WindowManager;
import android.view.animation.DecelerateInterpolator;
import android.webkit.DownloadListener;
import android.webkit.ValueCallback;
import android.webkit.WebChromeClient;
import android.webkit.WebSettings;
import android.webkit.WebView;
import android.webkit.WebViewClient;
import android.widget.ImageView;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.Nullable;

import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.JSONObject;
import com.ktw.bitbit.R;
import com.ktw.bitbit.bean.OrderInfo;
import com.ktw.bitbit.bean.collection.CollectionEvery;
import com.ktw.bitbit.bean.message.ChatMessage;
import com.ktw.bitbit.bean.message.XmppMessage;
import com.ktw.bitbit.db.InternationalizationHelper;
import com.ktw.bitbit.db.dao.ChatMessageDao;
import com.ktw.bitbit.helper.DialogHelper;
import com.ktw.bitbit.helper.ShareSdkHelper;
import com.ktw.bitbit.ui.account.AuthorDialog;
import com.ktw.bitbit.ui.base.BaseActivity;
import com.ktw.bitbit.ui.circle.range.SendShuoshuoActivity;
import com.ktw.bitbit.ui.message.InstantMessageActivity;
import com.ktw.bitbit.util.AppUtils;
import com.ktw.bitbit.util.Constants;
import com.ktw.bitbit.util.JsonUtils;
import com.ktw.bitbit.util.PermissionUtil;
import com.ktw.bitbit.util.TimeUtils;
import com.ktw.bitbit.util.ToastUtil;
import com.ktw.bitbit.view.ComplaintDialog;
import com.ktw.bitbit.view.ExternalOpenDialog;
import com.ktw.bitbit.view.MatchKeyWordEditDialog;
import com.ktw.bitbit.view.ModifyFontSizeDialog;
import com.ktw.bitbit.view.PayDialog;
import com.ktw.bitbit.view.SelectionFrame;
import com.ktw.bitbit.view.WebMoreDialog;
import com.ktw.bitbit.view.window.WindowShowService;
import com.xuan.xuanhttplibrary.okhttp.HttpUtils;
import com.xuan.xuanhttplibrary.okhttp.callback.BaseCallback;
import com.xuan.xuanhttplibrary.okhttp.callback.JsonCallback;
import com.xuan.xuanhttplibrary.okhttp.result.ObjectResult;

import java.net.URLDecoder;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.UUID;

import okhttp3.Call;

/**
 * web
 */
public class WebViewActivity extends BaseActivity {

    public static final String EXTRA_URL = "url";
    public static final String EXTRA_DOWNLOAD_URL = "download_url";

    public static String FLOATING_WINDOW_URL;
    public static boolean IS_FLOATING;
    boolean isReported;
    private TextView mTitleTv;
    private ImageView mTitleLeftIv;
    private ImageView mTitleRightIv;
    private ProgressBar mProgressBar;
    private WebView mWebView;
    private boolean isAnimStart = false;
    private int currentProgress;
    private String mUrl; // 网址URL
    private String mDownloadUrl;// ShareSdk 分享链接进来的应用下载地址(跳转，当本地不存在对应应用时使用)
    private JsSdkInterface jsSdkInterface;
    // js sdk设置的分享数据，
    private String shareBeanContent;
    // 群助手 交互的数据
    private String mShareParams;

    // 5.0及以上使用
    private ValueCallback<Uri[]> uploadMessageAboveL;
    //拍照图片路径
    private final static int FILE_CAMERA_RESULT_CODE = 129;


    public static void start(Context ctx, String url) {
        Intent intent = new Intent(ctx, WebViewActivity.class);
        intent.putExtra(EXTRA_URL, url);
        ctx.startActivity(intent);
    }

    public static void start(Context ctx, String url, String shareParams) {
        Intent intent = new Intent(ctx, WebViewActivity.class);
        intent.putExtra(EXTRA_URL, url);
        intent.putExtra("shareParams", shareParams);
        ctx.startActivity(intent);
    }

    /**
     * 解析出url参数中的键值对
     * 如 "index.jsp?Action=del&id=123"，解析出Action:del,id:123存入map中
     *
     * @param URL url地址
     * @return url请求参数部分
     */
    public static Map<String, String> URLRequest(String URL) {
        Map<String, String> mapRequest = new HashMap<String, String>();

        String[] arrSplit = null;

        String strUrlParam = TruncateUrlPage(URL);
        if (strUrlParam == null) {
            return mapRequest;
        }
        //每个键值为一组 www.2cto.com
        arrSplit = strUrlParam.split("[&]");
        for (String strSplit : arrSplit) {
            String[] arrSplitEqual = null;
            arrSplitEqual = strSplit.split("[=]");

            //解析出键值
            if (arrSplitEqual.length > 1) {
                //正确解析
                mapRequest.put(arrSplitEqual[0], arrSplitEqual[1]);

            } else {
                if (arrSplitEqual[0] != "") {
                    //只有参数没有值，不加入
                    mapRequest.put(arrSplitEqual[0], "");
                }
            }
        }
        Log.d("mapRequestwebweb", "URLRequest: " + mapRequest.get("webAppName"));
        return mapRequest;
    }

    /**
     * 去掉url中的路径，留下请求参数部分
     *
     * @param strURL url地址
     * @return url请求参数部分
     */
    private static String TruncateUrlPage(String strURL) {
        String strAllParam = null;
        String[] arrSplit = null;

        strURL = strURL.trim();

        arrSplit = strURL.split("[?]");
        if (strURL.length() > 1) {
            if (arrSplit.length > 1) {
                if (arrSplit[1] != null) {
                    strAllParam = arrSplit[1];
                }
            }
        }

        return strAllParam;
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_web_view);
        if (getIntent() != null) {
            mUrl = getIntent().getStringExtra(EXTRA_URL);
            mDownloadUrl = getIntent().getStringExtra(EXTRA_DOWNLOAD_URL);
            mShareParams = getIntent().getStringExtra("shareParams");
            initCheck();
            initActionBar();
        }
    }

    @Override
    public void onBackPressed() {
        if (mWebView != null && mWebView.canGoBack()) {
            mWebView.goBack();
        } else {
            finish();
        }
    }

    @Override
    protected void onStop() {
        super.onStop();
        //点击悬浮窗进来之后是不会有弹窗的，返回之后才有，这里判断一下进来的场景
        if (IS_FLOATING) {
            getCurrentUrl();
            startService(new Intent(WebViewActivity.this, WindowShowService.class));
        }
    }

    private void initCheck() {
        Map<String, String> params = new HashMap<>();
        params.put("access_token", coreManager.getSelfStatus().accessToken);
        params.put("webUrl", mUrl);

        HttpUtils.get().url(coreManager.getConfig().URL_CHECK)
                .params(params)
                .build()
                .execute(new BaseCallback<Void>(Void.class) {
                    @Override
                    public void onResponse(ObjectResult<Void> result) {
                        if (result.getResultCode() != 1) {// 网址被多次举报，将网址改为警告地址，不让访问
                            isReported = true;
                        }
                        init();
                    }

                    @Override
                    public void onError(Call call, Exception e) {
                        init();
                    }
                });
    }

    private void initActionBar() {
        getSupportActionBar().hide();
        findViewById(R.id.iv_title_left).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                finish();
            }
        });
        mTitleTv = findViewById(R.id.tv_title_center);
        mTitleLeftIv = findViewById(R.id.iv_title_left);
        mTitleLeftIv.setImageResource(R.drawable.icon_close);
        mTitleRightIv = findViewById(R.id.iv_title_right);
        mTitleRightIv.setImageResource(R.drawable.chat_more);
    }

    private void init() {
        initView();
        initClient();
        initEvent();
        stopService(new Intent(WebViewActivity.this, WindowShowService.class));
    }

    private void initView() {
        mProgressBar = (ProgressBar) findViewById(R.id.progressBar);
        mWebView = (WebView) findViewById(R.id.mWebView);
        /* 设置支持Js */
        mWebView.getSettings().setJavaScriptEnabled(true);
        /* 设置为true表示支持使用js打开新的窗口 */
        mWebView.getSettings().setJavaScriptCanOpenWindowsAutomatically(true);

        /* 设置缓存模式 */
        mWebView.getSettings().setCacheMode(WebSettings.LOAD_DEFAULT);
        mWebView.getSettings().setDomStorageEnabled(true);

        /* 设置为使用webview推荐的窗口 */
        mWebView.getSettings().setUseWideViewPort(true);
        /* 设置为使用屏幕自适配 */
        mWebView.getSettings().setLoadWithOverviewMode(true);
        /* 设置是否允许webview使用缩放的功能,我这里设为false,不允许 */
        mWebView.getSettings().setBuiltInZoomControls(false);
        /* 提高网页渲染的优先级 */
        mWebView.getSettings().setRenderPriority(WebSettings.RenderPriority.HIGH);

        /* HTML5的地理位置服务,设置为true,启用地理定位 */
        mWebView.getSettings().setGeolocationEnabled(true);
        /* 设置可以访问文件 */
        mWebView.getSettings().setAllowFileAccess(true);

        if (isReported) {// 加载警告网址
            mWebView.loadUrl("file:////android_asset/prohibit.html");
        } else {
            int openStatus = openApp(mUrl);
            if (openStatus == 1) {// 该链接为跳转链接，方法内已跳转，直接return
                finish();
            } else if (openStatus == 2) {// 该链接为跳转链接，但本地未安装该应该，加载该应用下载地址
                mWebView.loadUrl(mDownloadUrl);
            } else if (openStatus == 5) {// 该链接为跳转链接，跳转到本地授权

            } else {
                // 服务器要求标识 这是imApp内的浏览器
                Map<String, String> head = new HashMap<>();
                head.put("user-agent", "app-shikuimapp");
                mWebView.loadUrl(mUrl, head);
            }
        }
    }

    private void initClient() {
        mWebView.setWebViewClient(new WebViewClient() {
            @Override
            public void onPageStarted(WebView view, String url, Bitmap favicon) {
                mProgressBar.setVisibility(View.VISIBLE);
                mProgressBar.setAlpha(1.0f);
                super.onPageStarted(view, url, favicon);

                setWebChromeClient();
            }

            @Override
            public boolean shouldOverrideUrlLoading(WebView view, String url) {
                Log.e("xuan", "shouldOverrideUrlLoading: " + url);
                if (url.startsWith("tel:")) {
                    Intent intent = new Intent(Intent.ACTION_VIEW, Uri.parse(url));
                    startActivity(intent);
                    return true;
                }
                int openStatus = openApp(url);
                if (openStatus == 1) {// 该链接为跳转链接，方法内已跳转，直接return
                    return true;
                } else if (openStatus == 2) {// 该链接为跳转链接，但本地未安装该应该，加载该应用下载地址
                    view.loadUrl(mDownloadUrl);
                } else if (openStatus == 5) {// 该链接为跳转链接， 该链接为跳转链接，跳转到本地授权

                } else {
                    // 服务器要求标识 这是imApp内的浏览器
                    Map<String, String> head = new HashMap<>();
                    head.put("user-agent", "app-shikuimapp");
                    view.loadUrl(url, head);
                }
                return true;
            }

            @Override
            public void onReceivedError(WebView view, int errorCode, String description, String failingUrl) {
                super.onReceivedError(view, errorCode, description, failingUrl);
            }
        });


        mWebView.setDownloadListener(new DownloadListener() {
            @Override
            public void onDownloadStart(String url, String userAgent, String contentDisposition, String mimetype, long contentLength) {
                try {
                    // 不处理下载，直接抛出去，
                    Intent i = new Intent(Intent.ACTION_VIEW);
                    i.setData(Uri.parse(url));
                    startActivity(i);
                } catch (Exception ignored) {
                    // 无论如何不要崩溃，比如没有浏览器，
                    ToastUtil.showToast(WebViewActivity.this, R.string.download_error);
                }
            }
        });

        jsSdkInterface = new JsSdkInterface(this, new MyJsSdkListener());
        jsSdkInterface.setShareParams(mShareParams);
        mWebView.addJavascriptInterface(jsSdkInterface, "AndroidWebView");
    }

    private void initEvent() {
        mTitleRightIv.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                WebMoreDialog mWebMoreDialog = new WebMoreDialog(WebViewActivity.this, getCurrentUrl(), new WebMoreDialog.BrowserActionClickListener() {
                    @Override
                    public void floatingWindow() {
                        showFloating();
                    }

                    @Override
                    public void sendToFriend() {
                        forwardToFriend();
                    }

                    @Override
                    public void shareToLifeCircle() {
                        shareMoment();
                    }

                    @Override
                    public void collection() {
                        onCollection(getCurrentUrl());
                    }

                    @Override
                    public void searchContent() {
                        search();
                    }

                    @Override
                    public void copyLink() {
                        ClipboardManager clipboardManager = (ClipboardManager) mContext.getSystemService(Context.CLIPBOARD_SERVICE);
                        clipboardManager.setText(getCurrentUrl());
                        Toast.makeText(WebViewActivity.this, getString(R.string.tip_copied_to_clipboard), Toast.LENGTH_SHORT).show();
                    }

                    @Override
                    public void openOutSide() {
                        ExternalOpenDialog externalOpenDialog = new ExternalOpenDialog(mContext, getCurrentUrl());
                        externalOpenDialog.show();
                    }

                    @Override
                    public void modifyFontSize() {
                        setWebFontSiz();
                    }

                    @Override
                    public void refresh() {
                        mWebView.reload();
                    }

                    @Override
                    public void complaint() {
                        report();
                    }

                    @Override
                    public void shareWechat() {
                        String title = mTitleTv.getText().toString().trim();
                        String url = getCurrentUrl();
                        ShareSdkHelper.shareWechat(
                                WebViewActivity.this, title, url, url
                        );
                    }

                    @Override
                    public void shareWechatMoments() {
                        String title = mTitleTv.getText().toString().trim();
                        String url = getCurrentUrl();
                        ShareSdkHelper.shareWechatMoments(
                                WebViewActivity.this, title, url, url
                        );
                    }
                });
                mWebMoreDialog.show();
            }
        });
    }

    @Override
    protected void onDestroy() {
        if (jsSdkInterface != null) {
            jsSdkInterface.release();
        }
        super.onDestroy();
    }

    /**
     * 根据url跳转至其他app
     */
    private int openApp(String url) {
        if (TextUtils.isEmpty(url)) {
            return 0;
        }
        try {
            // 内部授权
            //  http://192.168.0.141:8080/websiteAuthorh/appAuth.html?appId=sk7c4fd05f92c7460a&callbackUrl=http://192.168.0.141:8080/websiteAuthorh/test.html
            if (url.contains("websiteAuthorh/index.html")) {
                url = URLDecoder.decode(url, "UTF-8");
                String webAppName = URLRequest(url).get("webAppName");
                String webAppsmallImg = URLRequest(url).get("webAppsmallImg");
                String appId = URLRequest(url).get("appId");
                String redirectURL = URLRequest(url).get("callbackUrl");

                Log.d("zx", "openApp: " + webAppName + "," + webAppsmallImg + "," + url);
                AuthorDialog dialog = new AuthorDialog(mContext);
                dialog.setDialogData(webAppName, webAppsmallImg);
                dialog.setmConfirmOnClickListener(new AuthorDialog.ConfirmOnClickListener() {
                    @Override
                    public void confirm() {
                        HttpUtils.get().url(coreManager.getConfig().AUTHOR_CHECK)
                                .params("appId", appId)
                                .params("state", coreManager.getSelfStatus().accessToken)
                                .params("callbackUrl", redirectURL)
                                .build().execute(new JsonCallback() {
                            @Override
                            public void onResponse(String result) {
                                Log.e("onResponse", "onResponse: " + result);
                                JSONObject json = JSON.parseObject(result);
                                int code = json.getIntValue("resultCode");
                                if (1 == code) {
                                    String html = json.getJSONObject("data").getString("callbackUrl") + "?code=" + json.getJSONObject("data").getString("code");
                                    Map<String, String> head = new HashMap<>();
                                    head.put("user-agent", "app-shikuimapp");
                                    mWebView.loadUrl(html, head);
                                }
                            }

                            @Override
                            public void onError(Call call, Exception e) {

                            }
                        });
                    }

                    @Override
                    public void AuthorCancel() {

                    }
                });

                dialog.setCanceledOnTouchOutside(false);
                dialog.show();
                return 5;
            }

            if (!url.startsWith("http") && !url.startsWith("https") && !url.startsWith("ftp")) {
                Uri uri = Uri.parse(url);
                String host = uri.getHost();
                String scheme = uri.getScheme();
                // host 和 scheme 都不能为null
                if (!TextUtils.isEmpty(host) && !TextUtils.isEmpty(scheme)) {
                    Intent intent = new Intent(Intent.ACTION_VIEW, uri);
                    if (AppUtils.isSupportIntent(this, intent)) {
                        startActivity(intent);
                        return 1;
                    } else {
                        return 2;
                    }
                }
            }
        } catch (Exception e) {
            return 3;
        }
        return 4;
    }

    /****************************************************
     * Start
     ***************************************************/
    private String getCurrentUrl() {
        Log.e("TAG_CURRENT_URL", mWebView.getUrl());
        String currentUrl = mWebView.getUrl();
        if (TextUtils.isEmpty(currentUrl)) {
            currentUrl = mUrl;
        }

        FLOATING_WINDOW_URL = currentUrl;

        if (currentUrl.contains("https://view.officeapps.live.com/op/view.aspx?src=")) {
            currentUrl = currentUrl.replace("https://view.officeapps.live.com/op/view.aspx?src=", "");
        }

        return currentUrl;
    }

    /**
     * 浮窗
     */
    private void showFloating() {
        if (Build.VERSION.SDK_INT < Build.VERSION_CODES.M
                || AppUtils.checkAlertWindowsPermission(this)) {
            IS_FLOATING = !IS_FLOATING;
            if (IS_FLOATING) {
                startService(new Intent(WebViewActivity.this, WindowShowService.class));
                finish();
            } else {
                stopService(new Intent(WebViewActivity.this, WindowShowService.class));
            }
        } else {
            SelectionFrame dialog = new SelectionFrame(this);
            dialog.setSomething("悬浮窗权限", "您的手机没有授予悬浮窗权限，请开启后再试", "暂不开启", "现在去开启",
                    new SelectionFrame.OnSelectionFrameClickListener() {
                        @Override
                        public void cancelClick() {

                        }

                        @Override
                        public void confirmClick() {
                            PermissionUtil.startApplicationDetailsSettings(WebViewActivity.this, 0x01);
                        }
                    });
            dialog.show();
        }
    }

    /**
     * 发送给朋友
     */
    private void initChatByUrl(String url) {
        String title = mTitleTv.getText().toString().trim();
        String content = JsonUtils.initJsonContent(title, getCurrentUrl(), url);
        initChatByContent(content, XmppMessage.TYPE_LINK);
    }

    private void initChatByContent(String content, int type) {
        String mLoginUserId = coreManager.getSelf().getUserId();

        ChatMessage message = new ChatMessage();
        message.setType(type);
        if (type == XmppMessage.TYPE_LINK) {
            message.setContent(content);
        } else if (type == XmppMessage.TYPE_SHARE_LINK) {
            message.setObjectId(content);
        } else {
            throw new IllegalStateException("未知类型: " + type);
        }
        message.setPacketId(UUID.randomUUID().toString().replaceAll("-", ""));
        message.setTimeSend(TimeUtils.sk_time_current_time());
        // Todo 将封装好的消息存入10010 号的msg 表内，在跳转至转发->聊天界面(跳转传值均为10010号与msgId)，之后在聊天界面内通过这两个值查询到对用消息，发送
        String mNewUserId = "10010";
        if (ChatMessageDao.getInstance().saveNewSingleChatMessage(mLoginUserId, mNewUserId, message)) {
            Intent intent = new Intent(WebViewActivity.this, InstantMessageActivity.class);
            intent.putExtra("fromUserId", mNewUserId);
            intent.putExtra("messageId", message.getPacketId());
            startActivity(intent);
            finish();
        } else {
            Toast.makeText(mContext, "消息封装失败", Toast.LENGTH_SHORT).show();
        }
    }

    private void forwardToFriend() {
        if (shareBeanContent != null) {
            initChatByContent(shareBeanContent, XmppMessage.TYPE_SHARE_LINK);
        } else {
            selectShareImage();
        }
    }

    private void selectShareImage() {
        String str = mWebView.getUrl();
        if (TextUtils.isEmpty(str)) {
            str = getCurrentUrl();
        }
        HtmlFactory.instance().queryImage(str, new HtmlFactory.DataListener<String>() {// 检索该网页包含的图片

            @Override
            public void onResponse(List<String> data, String title) {
                if (data != null && data.size() > 0) {
                    // 弹窗让用户选择图片 作为链接封面
                    SelectImageDialog dialog = new SelectImageDialog(WebViewActivity.this, getCurrentUrl(), data, url -> initChatByUrl(url));
                    dialog.show();
                } else {
                    initChatByUrl("");
                }
            }

            @Override
            public void onError(String error) {
                initChatByUrl("");
            }
        });
    }

    /**
     * 分享到生活圈
     */
    private void shareMoment() {
        Intent intent = new Intent(WebViewActivity.this, SendShuoshuoActivity.class);
        intent.putExtra(Constants.BROWSER_SHARE_MOMENTS_CONTENT, getCurrentUrl());
        startActivity(intent);
    }

    /**
     * 收藏
     * 链接 当做 文本类型 收藏
     */
    private String collectionParam(String content) {
        com.alibaba.fastjson.JSONArray array = new com.alibaba.fastjson.JSONArray();
        com.alibaba.fastjson.JSONObject json = new com.alibaba.fastjson.JSONObject();
        int type = CollectionEvery.TYPE_TEXT;
        json.put("type", String.valueOf(type));
        String msg = "";
        String collectContent = "";
        msg = content;
        collectContent = content;
        json.put("msg", msg);
        json.put("collectContent", collectContent);
        json.put("collectType", -1);// 与消息无关的收藏
        array.add(json);
        return JSON.toJSONString(array);
    }

    private void onCollection(final String content) {
        Map<String, String> params = new HashMap<>();
        params.put("access_token", coreManager.getSelfStatus().accessToken);
        params.put("emoji", collectionParam(content));

        HttpUtils.get().url(coreManager.getConfig().Collection_ADD)
                .params(params)
                .build()
                .execute(new BaseCallback<Void>(Void.class) {

                    @Override
                    public void onResponse(ObjectResult<Void> result) {
                        if (result.getResultCode() == 1) {
                            Toast.makeText(mContext, InternationalizationHelper.getString("JX_CollectionSuccess"), Toast.LENGTH_SHORT).show();
                        } else if (!TextUtils.isEmpty(result.getResultMsg())) {
                            ToastUtil.showToast(mContext, result.getResultMsg());
                        } else {
                            ToastUtil.showToast(mContext, R.string.tip_server_error);
                        }
                    }

                    @Override
                    public void onError(Call call, Exception e) {
                        ToastUtil.showNetError(mContext);
                    }
                });
    }

    /**
     * 搜索页面内容
     */
    private void search() {
        MatchKeyWordEditDialog matchKeyWordEditDialog = new MatchKeyWordEditDialog(this, mWebView);
        Window window = matchKeyWordEditDialog.getWindow();
        if (window != null) {
            window.setSoftInputMode(WindowManager.LayoutParams.SOFT_INPUT_STATE_VISIBLE);// 软键盘弹起
            matchKeyWordEditDialog.show();
        }
    }

    /**
     * 调整字体
     */
    private void setWebFontSiz() {
        ModifyFontSizeDialog modifyFontSizeDialog = new ModifyFontSizeDialog(this, mWebView);
        modifyFontSizeDialog.show();
    }

    /**
     * 投诉
     */
    private void report() {
        ComplaintDialog complaintDialog = new ComplaintDialog(this, report -> {
            Map<String, String> params = new HashMap<>();
            params.put("access_token", coreManager.getSelfStatus().accessToken);
            params.put("webUrl", getCurrentUrl());
            params.put("reason", String.valueOf(report.getReportId()));
            DialogHelper.showDefaulteMessageProgressDialog(WebViewActivity.this);

            HttpUtils.get().url(coreManager.getConfig().USER_REPORT)
                    .params(params)
                    .build()
                    .execute(new BaseCallback<Void>(Void.class) {

                        @Override
                        public void onResponse(ObjectResult<Void> result) {
                            DialogHelper.dismissProgressDialog();
                            if (result.getResultCode() == 1) {
                                ToastUtil.showToast(WebViewActivity.this, R.string.report_success);
                            }
                        }

                        @Override
                        public void onError(Call call, Exception e) {
                            DialogHelper.dismissProgressDialog();
                        }
                    });
        });
        complaintDialog.show();
    }

    /****************************************************
     * End
     ***************************************************/

    /**
     * progressBar递增动画
     */
    private void startProgressAnimation(int newProgress) {
        ObjectAnimator animator = ObjectAnimator.ofInt(mProgressBar, "progress", currentProgress, newProgress);
        animator.setDuration(300);
        animator.setInterpolator(new DecelerateInterpolator());
        animator.start();
    }

    /**
     * progressBar消失动画
     */
    private void startDismissAnimation(final int progress) {
        ObjectAnimator anim = ObjectAnimator.ofFloat(mProgressBar, "alpha", 1.0f, 0.0f);
        anim.setDuration(1500);  // 动画时长
        anim.setInterpolator(new DecelerateInterpolator());
        // 关键, 添加动画进度监听器
        anim.addUpdateListener(new ValueAnimator.AnimatorUpdateListener() {

            @Override
            public void onAnimationUpdate(ValueAnimator valueAnimator) {
                float fraction = valueAnimator.getAnimatedFraction();      // 0.0f ~ 1.0f
                int offset = 100 - progress;
                mProgressBar.setProgress((int) (progress + offset * fraction));
            }
        });

        anim.addListener(new AnimatorListenerAdapter() {

            @Override
            public void onAnimationEnd(Animator animation) {
                // 动画结束
                mProgressBar.setProgress(0);
                mProgressBar.setVisibility(View.GONE);
                isAnimStart = false;
            }
        });
        anim.start();
    }

    private class MyJsSdkListener implements JsSdkInterface.Listener {

        @Override
        public void onFinishPlay(String path) {
            mWebView.evaluateJavascript("playFinish()", value -> {
            });
        }

        @Override
        public void onUpdateShareData(String shareBeanContent) {
            WebViewActivity.this.shareBeanContent = shareBeanContent;
        }

        @Override
        public void onChooseSKPayInApp(String appId, String prepayId, String sign) {
            DialogHelper.showDefaulteMessageProgressDialog(mContext);
            Map<String, String> params = new HashMap<String, String>();
            params.put("access_token", coreManager.getSelfStatus().accessToken);
            params.put("appId", appId);
            params.put("prepayId", prepayId);
            params.put("sign", sign);

            // 获取订单信息
            HttpUtils.get().url(coreManager.getConfig().PAY_GET_ORDER_INFO)
                    .params(params)
                    .build()
                    .execute(new BaseCallback<OrderInfo>(OrderInfo.class) {

                        @Override
                        public void onResponse(ObjectResult<OrderInfo> result) {
                            DialogHelper.dismissProgressDialog();
                            if (result.getResultCode() == 1 && result.getData() != null) {
                                PayDialog payDialog = new PayDialog(mContext, appId, prepayId, sign, result.getData(), new PayDialog.PayResultListener() {
                                    @Override
                                    public void payResult(String result) {
                                        mWebView.loadUrl("javascript:sk.paySuccess(" + result + ")");
                                    }
                                });
                                payDialog.show();
                            }
                        }

                        @Override
                        public void onError(Call call, Exception e) {
                            DialogHelper.dismissProgressDialog();
                        }
                    });
        }

    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (null == uploadMessageAboveL) {
            return;
        }
        Uri result = null;
        if (requestCode == FILE_CAMERA_RESULT_CODE) {
            if (resultCode == -1) {
                if (null != data && null != data.getData()) {
                    result = data.getData();
                }
                //5.0以上设备的数据处理
                if (uploadMessageAboveL != null && result != null) {
                    uploadMessageAboveL.onReceiveValue(new Uri[]{result});
                    uploadMessageAboveL = null;
                }
            }
        }
    }

    public void setWebChromeClient() {
        if (mWebView == null) {
            return;
        }
        // 获取网页加载进度
        mWebView.setWebChromeClient(new WebChromeClient() {
            @Override
            public void onProgressChanged(WebView view, int newProgress) {
                currentProgress = mProgressBar.getProgress();
                if (newProgress >= 100 && !isAnimStart) {
                    // 防止调用多次动画
                    isAnimStart = true;
                    mProgressBar.setProgress(newProgress);
                    // 开启属性动画让进度条平滑消失
                    startDismissAnimation(mProgressBar.getProgress());
                } else {
                    // 开启属性动画让进度条平滑递增
                    startProgressAnimation(newProgress);
                }
            }

            @Override
            public void onReceivedTitle(WebView view, String title) {
                super.onReceivedTitle(view, title);
                mTitleTv.setText(title);
            }

            @Override
            public void onReceivedIcon(WebView view, Bitmap icon) {
                super.onReceivedIcon(view, icon);
            }

            // For Android >= 5.0
            @Override
            public boolean onShowFileChooser(WebView webView, ValueCallback<Uri[]> filePathCallback, WebChromeClient.FileChooserParams fileChooserParams) {
                uploadMessageAboveL = filePathCallback;
                Intent i = new Intent(Intent.ACTION_PICK, MediaStore.Images.Media.EXTERNAL_CONTENT_URI);
                i.addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION);
                startActivityForResult(Intent.createChooser(i, "选择图片"), FILE_CAMERA_RESULT_CODE);
                return true;
            }
        });
    }
}
