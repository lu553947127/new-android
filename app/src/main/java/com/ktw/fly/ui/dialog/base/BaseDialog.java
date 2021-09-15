package com.ktw.fly.ui.dialog.base;

import android.app.Activity;
import android.app.AlertDialog;
import android.view.View;
import android.view.Window;

import com.ktw.fly.R;

/**
 * Created by Administrator on 2016/5/3.
 */
public abstract class BaseDialog {
    protected View mView;
    protected Activity mActivity;
    protected int RID;
    protected AlertDialog mDialog;
    protected boolean mCancleable = true;

    protected void initView() {
        if (RID != 0)
            mView = mActivity.getLayoutInflater().inflate(RID, null);
    }

    public BaseDialog show() {
        mDialog = new AlertDialog.Builder(mActivity).setView(mView).create();
        Window window = mDialog.getWindow();
        window.setBackgroundDrawableResource(R.drawable.dialog_style_bg);
        mDialog.setCancelable(mCancleable);//点击空白的地方取消
        mDialog.show();
        return this;
    }

    public <T> T $(int rid) {
        return (T) mView.findViewById(rid);
    }

    public String getString(int rstring) {
        return mActivity.getString(rstring);
    }
}
