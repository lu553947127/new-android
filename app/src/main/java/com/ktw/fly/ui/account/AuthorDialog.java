package com.ktw.fly.ui.account;

import android.app.Dialog;
import android.content.Context;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.TextView;

import com.ktw.fly.FLYApplication;
import com.ktw.fly.R;
import com.ktw.fly.helper.AvatarHelper;
import com.ktw.fly.view.CircleImageView;

public class AuthorDialog extends Dialog {

    private ConfirmOnClickListener mConfirmOnClickListener;
    private CircleImageView mIvIcon;
    private TextView mTvName;
    private String urlImagview;
    private String name;

    public AuthorDialog(Context context) {
        super(context, R.style.MyDialog);
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.dialog_author);
        initView();
    }

    private void initView() {
        TextView title = findViewById(R.id.title_tv);
        title.setText(FLYApplication.getContext().getString(R.string.centent_bar, FLYApplication.getContext().getString(R.string.app_name)));
        mIvIcon = findViewById(R.id.iv_author_icon);
        mTvName = findViewById(R.id.tv_author_title);
        AvatarHelper.getInstance().displayUrl(urlImagview, mIvIcon);
        mTvName.setText(name);
        initEvent();
    }

    public void setDialogData(String name, String url) {// 单独设置提示语，不对点击事件做处理
        Log.d("xuan", "setDialogData: " + name + "," + url);
        this.name = name;
        this.urlImagview = url;
    }

    /**
     * 点确定或者返回键取消对话框都调用这个回调，
     */
    public void setmConfirmOnClickListener(ConfirmOnClickListener mConfirmOnClickListener) {
        this.mConfirmOnClickListener = mConfirmOnClickListener;
    }

    private void initEvent() {
        findViewById(R.id.tv_positive).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                dismiss();
                if (mConfirmOnClickListener != null) {
                    mConfirmOnClickListener.confirm();
                }
            }
        });

        findViewById(R.id.tv_negative).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                dismiss();
                if (mConfirmOnClickListener != null) {
                    mConfirmOnClickListener.AuthorCancel();
                }
            }
        });
    }

    public interface ConfirmOnClickListener {
        void confirm();

        void AuthorCancel();
    }
}
