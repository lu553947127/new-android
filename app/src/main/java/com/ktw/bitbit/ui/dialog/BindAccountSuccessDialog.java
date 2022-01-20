package com.ktw.bitbit.ui.dialog;

import android.app.Dialog;
import android.content.Context;
import android.os.Bundle;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;

import com.ktw.bitbit.R;

/**
 * 绑定账号成功
 */
public class BindAccountSuccessDialog extends Dialog {


    public BindAccountSuccessDialog(@NonNull Context context) {
        super(context, R.style.MyDialog);
    }

    public BindAccountSuccessDialog(@NonNull Context context, int themeResId) {
        super(context, themeResId);
    }

    protected BindAccountSuccessDialog(@NonNull Context context, boolean cancelable, @Nullable OnCancelListener cancelListener) {
        super(context, cancelable, cancelListener);
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.dialog_bind_account_success);

    }



}
