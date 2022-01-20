package com.ktw.bitbit.wallet.dialog;

import android.graphics.Color;
import android.graphics.drawable.ColorDrawable;
import android.os.Bundle;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.Window;
import android.view.WindowManager;

import androidx.annotation.IdRes;
import androidx.annotation.LayoutRes;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.annotation.StyleRes;
import androidx.fragment.app.DialogFragment;

import com.ktw.bitbit.R;
import com.ktw.bitbit.util.ToastUtil;

public abstract class BaseDialogFragment extends DialogFragment {

    private View mContentView;


    public final String TAG = this.getClass().getSimpleName();

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        mContentView = inflater.inflate(getLayoutId(), container);
        getThisView();
        initBundle();
        initLayout();
        return mContentView;
    }

    protected <T extends View> T findView(@IdRes int id) {
        return mContentView.findViewById(id);
    }

    @LayoutRes
    protected abstract int getLayoutId();

    protected abstract void getThisView();

    protected abstract void initBundle();

    protected abstract void initLayout();

    protected int getGravity() {
        return Gravity.CENTER;
    }

    @StyleRes
    protected int getStyleRes() {
        return R.style.dialog_scale_anim_style;
    }

    @Override
    public void onStart() {
        super.onStart();
        Window window = getDialog().getWindow();
        WindowManager.LayoutParams attributes = window.getAttributes();
        attributes.gravity = getGravity();
        window.setLayout(WindowManager.LayoutParams.MATCH_PARENT, WindowManager.LayoutParams.WRAP_CONTENT);
        window.setBackgroundDrawable(new ColorDrawable(Color.TRANSPARENT));
        window.setWindowAnimations(getStyleRes());
    }

    public void showError(String error) {
        ToastUtil.showToast(mContentView.getContext(),error);
    }


}
