package com.ktw.fly.ui.dialog;

import android.app.Dialog;
import android.content.Context;
import android.os.Bundle;
import android.view.View;
import android.view.WindowManager;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;

import com.ktw.fly.R;
import com.ktw.fly.helper.ImageLoadHelper;
import com.ktw.fly.ui.base.CoreManager;
import com.ktw.fly.ui.tool.ButtonColorChange;
import com.ktw.fly.util.Constants;
import com.ktw.fly.util.PreferenceUtils;
import com.ktw.fly.util.ScreenUtil;
import com.xuan.xuanhttplibrary.okhttp.HttpUtils;

import java.util.HashMap;
import java.util.Map;

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
