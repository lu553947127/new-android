package com.ktw.bitbit.ui.me;

import android.os.Bundle;
import android.text.Html;
import android.text.TextUtils;
import android.view.View;
import android.widget.ImageView;
import android.widget.TextView;

import com.ktw.bitbit.FLYAppConfig;
import com.ktw.bitbit.FLYApplication;
import com.ktw.bitbit.R;
import com.ktw.bitbit.bean.QuestionBean;
import com.ktw.bitbit.helper.ShareSdkHelper;
import com.ktw.bitbit.ui.base.BaseActivity;
import com.ktw.bitbit.ui.other.PrivacyAgreeActivity;
import com.ktw.bitbit.util.DeviceInfoUtil;
import com.ktw.bitbit.util.UiUtils;
import com.ktw.bitbit.view.ShareDialog;
import com.xuan.xuanhttplibrary.okhttp.HttpUtils;
import com.xuan.xuanhttplibrary.okhttp.callback.BaseCallback;
import com.xuan.xuanhttplibrary.okhttp.result.ObjectResult;
import com.xuan.xuanhttplibrary.okhttp.result.Result;

import java.util.HashMap;
import java.util.Map;

import okhttp3.Call;

public class AboutActivity extends BaseActivity {
    private ShareDialog shareDialog;
    ShareDialog.OnShareDialogClickListener onShareDialogClickListener = new ShareDialog.OnShareDialogClickListener() {
        @Override
        public void tv1Click() {
            ShareSdkHelper.shareWechat(AboutActivity.this, FLYApplication.getContext().getString(R.string.app_name) + AboutActivity.this.getString(R.string.suffix_share_content),
                    FLYApplication.getContext().getString(R.string.app_name) + AboutActivity.this.getString(R.string.suffix_share_content),
                    AboutActivity.this.coreManager.getConfig().website);
        }

        @Override
        public void tv2Click() {
            ShareSdkHelper.shareWechatMoments(AboutActivity.this, FLYApplication.getContext().getString(R.string.app_name) + AboutActivity.this.getString(R.string.suffix_share_content),
                    FLYApplication.getContext().getString(R.string.app_name) + AboutActivity.this.getString(R.string.suffix_share_content),
                    AboutActivity.this.coreManager.getConfig().website);
        }

        @Override
        public void tv3Click() {
            shareDialog.cancel();
        }
    };
    private TextView tvContent;

    public void PrivacyAgree(View view) {
        if (UiUtils.isNormalClick(view) && !TextUtils.isEmpty(coreManager.getConfig().privacyPolicyPrefix)) {
            PrivacyAgreeActivity.startIntent(AboutActivity.this);
        }
    }

    public void Privacy(View view) {
        if (UiUtils.isNormalClick(view) && !TextUtils.isEmpty(coreManager.getConfig().privacyPolicyPrefix)) {
            PrivacyAgreeActivity.startPrivacy(AboutActivity.this);
        }
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_about);
        getSupportActionBar().hide();
        findViewById(R.id.iv_title_left).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                finish();
            }
        });
        TextView tvTitle = (TextView) findViewById(R.id.tv_title_center);
        tvTitle.setText(getString(R.string.about_us));
        ImageView ivRight = (ImageView) findViewById(R.id.iv_title_right);
        ivRight.setImageResource(R.mipmap.share_icon);
        ivRight.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                shareDialog = new ShareDialog(AboutActivity.this, onShareDialogClickListener);
                shareDialog.show();
            }
        });

        TextView versionTv = (TextView) findViewById(R.id.version_tv);
        versionTv.setText(getString(R.string.app_name) + DeviceInfoUtil.getVersionName(mContext));

        TextView tvCompany = findViewById(R.id.company_tv);
        TextView tvCopyright = findViewById(R.id.copy_right_tv);
        tvContent = findViewById(R.id.tvContent);

        tvCompany.setText(coreManager.getConfig().companyName);
        tvCopyright.setText(coreManager.getConfig().copyright);

        if (!FLYAppConfig.isShiku()) {
            tvCompany.setVisibility(View.GONE);
            tvCopyright.setVisibility(View.GONE);
            ivRight.setVisibility(View.GONE);
        }
        getContent();
    }

    public void getContent() {
        Map<String, String> params = new HashMap<>();
        params.put("type", String.valueOf(8));
        HttpUtils.post().url(coreManager.getConfig().GET_QUESTION_ITEM)
                .params(params)
                .build()
                .execute(new BaseCallback<QuestionBean>(QuestionBean.class) {

                    @Override
                    public void onResponse(ObjectResult<QuestionBean> result) {
                        if (Result.checkSuccess(getApplicationContext(), result)) {
                            if (result.getData() != null && !TextUtils.isEmpty(result.getData().getContent())) {
                                tvContent.setText(Html.fromHtml(result.getData().getContent()));
                                tvContent.setVisibility(View.VISIBLE);
                            }
                        }
                    }

                    @Override
                    public void onError(Call call, Exception e) {
                    }
                });
    }
}
