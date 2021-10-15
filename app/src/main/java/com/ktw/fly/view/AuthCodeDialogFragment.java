package com.ktw.fly.view;

import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;
import android.text.TextUtils;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.DialogFragment;

import com.ktw.fly.R;
import com.ktw.fly.bean.Code;
import com.ktw.fly.helper.DialogHelper;
import com.ktw.fly.helper.ImageLoadHelper;
import com.ktw.fly.helper.LoginHelper;
import com.ktw.fly.ui.account.FindPwdActivity;
import com.ktw.fly.ui.base.CoreManager;
import com.ktw.fly.ui.tool.ButtonColorChange;
import com.ktw.fly.util.Constants;
import com.ktw.fly.util.PreferenceUtils;
import com.ktw.fly.util.ToastUtil;
import com.xuan.xuanhttplibrary.okhttp.HttpUtils;
import com.xuan.xuanhttplibrary.okhttp.callback.BaseCallback;
import com.xuan.xuanhttplibrary.okhttp.result.ObjectResult;
import com.xuan.xuanhttplibrary.okhttp.result.Result;

import java.util.HashMap;
import java.util.Map;

import okhttp3.Call;

/**
 * @ProjectName: new-android
 * @Package: com.ktw.fly.view
 * @ClassName: AuthCodeDialogFragment
 * @Description: 图形验证码Dialog
 * @Author: XY
 * @CreateDate: 2021/9/24
 * @UpdateUser:
 * @UpdateDate: 2021/9/24
 * @UpdateRemark: 更新说明
 * @Version: 1.0
 */
public class AuthCodeDialogFragment extends DialogFragment implements View.OnClickListener {


    private int verificationType;
    private String account;
    private ImageView imageCodeIv;

    private static CoreManager sCoreManager;
    private EditText codeEdit;

    private int mobilePrefix = 86;
    private int passwordType;

    public static AuthCodeDialogFragment newInstance(CoreManager coreManager,int passwordType, int verificationType, String account) {
        sCoreManager = coreManager;
        Bundle bundle = new Bundle();
        bundle.putInt("verification_type", verificationType);
        bundle.putString("account", account);
        bundle.putInt("passwordType", passwordType);
        AuthCodeDialogFragment dialogFragment = new AuthCodeDialogFragment();
        dialogFragment.setArguments(bundle);
        return dialogFragment;
    }

    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        setStyle(STYLE_NO_TITLE, R.style.AuthCodeDialogStyle);
        super.onCreate(savedInstanceState);

        verificationType = getArguments().getInt("verification_type", -1);
        passwordType = getArguments().getInt("passwordType", -1);
        account = getArguments().getString("account", "");
        mobilePrefix = PreferenceUtils.getInt(getContext(), Constants.AREA_CODE_KEY, mobilePrefix);

        if (passwordType==1){
            if (verificationType == LoginHelper.LOGIN_PHONE) {
                requestImageCode();
            } else if (verificationType == LoginHelper.LOGIN_EMAIL) {
                requestImageCodeEmail("_loginMailboxPassword");
            }
        }else{
            if (verificationType == LoginHelper.LOGIN_PHONE) {
                requestImageCode("_capitalSendPhone");
            } else if (verificationType == LoginHelper.LOGIN_EMAIL) {
                requestImageCodeEmail("_capitalSendMailboxCode");
            }
        }

    }

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.dialog_auth_code, container, false);
        initView(view);
        return view;
    }

    private void initView(View view) {
        imageCodeIv = view.findViewById(R.id.image_iv);
        Button btnConfirm = view.findViewById(R.id.btn_confirm);
        Button btnCancel = view.findViewById(R.id.btn_cancel);
        ButtonColorChange.rechargeChange(getContext(), btnConfirm, R.drawable.bg_auth_code_confirm);
        btnConfirm.setOnClickListener(this);
        btnCancel.setOnClickListener(this);
        codeEdit = view.findViewById(R.id.et_code);
    }

    @Override
    public void onClick(View v) {
        switch (v.getId()) {
            case R.id.btn_confirm:
                String imgCode = codeEdit.getText().toString().trim();
                if (TextUtils.isEmpty(imgCode)) {
                    Toast.makeText(getContext(), R.string.hint_input_graphics_auth_code, Toast.LENGTH_SHORT).show();
                    return;
                }
                Intent intent = new Intent();
                intent.putExtra("imgCode", imgCode);
                getTargetFragment().onActivityResult(200, 200, intent);
                dismiss();
                break;
            case R.id.btn_cancel:
                dismiss();
                break;
        }
    }


    /**
     * 请求图形验证码
     */
    private void requestImageCode() {
        Map<String, String> params = new HashMap<>();
        params.put("telephone", mobilePrefix + account);
        String url = HttpUtils.get().url(sCoreManager.getConfig().USER_GETCODE_IMAGE)
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

    /**
     * 邮箱注册 请求图形验证码
     */
    private void requestImageCode(String key) {

        Map<String, String> params = new HashMap<>();

        params.put("imgKey",mobilePrefix + account + key);
        String url = HttpUtils.get().url(sCoreManager.getConfig().USER_GETCODE_IMAGE_EMAIL)
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


    /**
     * 邮箱注册 请求图形验证码
     */
    private void requestImageCodeEmail(String key) {
        
        Map<String, String> params = new HashMap<>();

        params.put("imgKey", account + key);
        String url = HttpUtils.get().url(sCoreManager.getConfig().USER_GETCODE_IMAGE_EMAIL)
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
