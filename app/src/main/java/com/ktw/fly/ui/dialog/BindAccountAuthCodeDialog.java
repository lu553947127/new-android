package com.ktw.fly.ui.dialog;

import android.app.Dialog;
import android.content.Context;
import android.os.Bundle;
import android.text.TextUtils;
import android.view.View;
import android.view.WindowManager;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.TextView;
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
 * 绑定账号验证Dialog
 */
public class BindAccountAuthCodeDialog extends Dialog {

    private int mobilePrefix = 86;
    private OnInputFinishListener onInputFinishListener;


    private int bindType;
    private String account;
    private ImageView imageCodeIv;
    private EditText codeEdit;

    public BindAccountAuthCodeDialog(@NonNull Context context) {
        super(context, R.style.MyDialog);
    }

    public BindAccountAuthCodeDialog(@NonNull Context context, int themeResId) {
        super(context, themeResId);
    }

    protected BindAccountAuthCodeDialog(@NonNull Context context, boolean cancelable, @Nullable OnCancelListener cancelListener) {
        super(context, cancelable, cancelListener);
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.dialog_auth_code);
        setCanceledOnTouchOutside(false);

        mobilePrefix = PreferenceUtils.getInt(getContext(), Constants.AREA_CODE_KEY, mobilePrefix);

        initView();
    }

    private void initView() {

        imageCodeIv = findViewById(R.id.image_iv);
        Button btnConfirm = findViewById(R.id.btn_confirm);
        Button btnCancel = findViewById(R.id.btn_cancel);
        ButtonColorChange.rechargeChange(getContext(), btnConfirm, R.drawable.bg_auth_code_confirm);

        codeEdit = findViewById(R.id.et_code);


        WindowManager.LayoutParams lp = getWindow().getAttributes();
        lp.width = (int) (ScreenUtil.getScreenWidth(getContext()) * 0.9);
        getWindow().setSoftInputMode(WindowManager.LayoutParams.SOFT_INPUT_STATE_ALWAYS_VISIBLE);


        btnConfirm.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                String code = codeEdit.getText().toString();
                onInputFinishListener.onInputFinish(code);
                dismiss();
            }
        });
        btnCancel.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                dismiss();
            }
        });


        if (bindType == 0) {
            requestImageCode(mobilePrefix + account + "_bangDingPhone");
        } else if (bindType == 1) {
            requestImageCode(account + "_bangDingMailbox");
        }

    }


    public void setOnInputFinishListener(OnInputFinishListener onInputFinishListener) {
        this.onInputFinishListener = onInputFinishListener;
    }

    public void setAccount(String account) {
        this.account = account;
    }

    public void setBindType(int bindType) {
        this.bindType = bindType;
    }


    public interface OnInputFinishListener {
        void onInputFinish(String code);
    }


    /**
     * 邮箱注册 请求图形验证码
     */
    private void requestImageCode(String key) {

        Map<String, String> params = new HashMap<>();

        params.put("imgKey", key);
        String url = HttpUtils.get().url(CoreManager.requireConfig(getContext()).USER_GETCODE_IMAGE_EMAIL)
                .params(params)
                .buildUrl();
        ImageLoadHelper.loadBitmapWithoutCache(
                getContext(),
                url,
                b -> {
                    imageCodeIv.setImageBitmap(b);
                }, e -> {
                    Toast.makeText(getContext(), R.string.tip_verification_code_load_failed, Toast.LENGTH_SHORT).show();
                }
        );
    }


}
