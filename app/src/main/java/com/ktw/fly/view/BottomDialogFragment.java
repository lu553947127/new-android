package com.ktw.fly.view;

import android.annotation.SuppressLint;
import android.content.ComponentName;
import android.content.Intent;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.text.TextUtils;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.EditText;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.DialogFragment;

import com.google.android.material.bottomsheet.BottomSheetDialogFragment;
import com.ktw.fly.FLYAppConfig;
import com.ktw.fly.FLYApplication;
import com.ktw.fly.R;
import com.ktw.fly.bean.Code;
import com.ktw.fly.bean.LoginRegisterResult;
import com.ktw.fly.bean.User;
import com.ktw.fly.helper.DialogHelper;
import com.ktw.fly.helper.ImageLoadHelper;
import com.ktw.fly.helper.LoginHelper;
import com.ktw.fly.helper.LoginSecureHelper;
import com.ktw.fly.sp.UserSp;
import com.ktw.fly.ui.account.FindPwdActivity;
import com.ktw.fly.ui.account.LoginActivity;
import com.ktw.fly.ui.account.LoginHistoryActivity;
import com.ktw.fly.ui.base.CoreManager;
import com.ktw.fly.ui.tool.ButtonColorChange;
import com.ktw.fly.util.Constants;
import com.ktw.fly.util.PreferenceUtils;
import com.ktw.fly.util.StringUtils;
import com.ktw.fly.util.ToastUtil;
import com.ktw.fly.util.log.LogUtils;
import com.ktw.fly.util.secure.LoginPassword;
import com.ktw.fly.util.secure.MAC;
import com.ktw.fly.util.secure.MD5;
import com.ktw.fly.util.secure.Parameter;
import com.xuan.xuanhttplibrary.okhttp.HttpUtils;
import com.xuan.xuanhttplibrary.okhttp.callback.BaseCallback;
import com.xuan.xuanhttplibrary.okhttp.result.ObjectResult;
import com.xuan.xuanhttplibrary.okhttp.result.Result;

import java.util.HashMap;
import java.util.Locale;
import java.util.Map;

import okhttp3.Call;

import static com.ktw.fly.FLYAppConfig.BROADCASTTEST_ACTION;

/**
 * @ProjectName: new-android
 * @Package: com.ktw.fly.view
 * @ClassName: BottomDialogFragment
 * @Description: 底部弹出框Dialog
 * @Author: XY
 * @CreateDate: 2021/9/23
 * @UpdateUser:
 * @UpdateDate: 2021/9/23
 * @UpdateRemark: 更新说明
 * @Version: 1.0
 */
public class BottomDialogFragment extends BottomSheetDialogFragment implements View.OnClickListener {

    private int reckonTime = 120;

    @SuppressLint("HandlerLeak")
    private Handler mReckonHandler = new Handler() {
        public void handleMessage(Message msg) {
            if (msg.what == 0x1) {
                btnGetCode.setText(reckonTime + " " + "S");
                reckonTime--;
                if (reckonTime < 0) {
                    mReckonHandler.sendEmptyMessage(0x2);
                } else {
                    mReckonHandler.sendEmptyMessageDelayed(0x1, 1000);
                }
            } else if (msg.what == 0x2) {
                // 120秒结束
                btnGetCode.setText(getString(R.string.send));
                btnGetCode.setEnabled(true);
                reckonTime = 120;
            }
        }
    };


    private Button btnGetCode;
    private int verificationType;
    private String account;
    private AuthCodeDialogFragment authCodeDialog;

    private static CoreManager sCoreManager;

    // 驗證碼
    private String randcode;
    private TextView tvBingAccount;
    private EditText mAuthCodeEdit;

    private int mobilePrefix = 86;
    private String password;
    private int passwordType;

    /**
     * @param coreManager
     * @param passwordType     密码类型  1：账号密码 2 资金密码
     * @param verificationType 验证类型
     * @param password
     * @return
     */
    public static BottomDialogFragment newInstance(CoreManager coreManager, int passwordType, int verificationType, String password) {
        sCoreManager = coreManager;
        String account = coreManager.getSelf().getTelephone();
        Bundle bundle = new Bundle();
        bundle.putInt("verification_type", verificationType);
        bundle.putString("account", account);
        bundle.putString("password", password);
        bundle.putInt("passwordType", passwordType);
        BottomDialogFragment dialogFragment = new BottomDialogFragment();
        dialogFragment.setArguments(bundle);
        return dialogFragment;
    }

    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        setStyle(STYLE_NO_TITLE, R.style.BottomSheetDialog);
        super.onCreate(savedInstanceState);
        verificationType = getArguments().getInt("verification_type", -1);
        passwordType = getArguments().getInt("passwordType", -1);
        account = getArguments().getString("account", "");
        password = getArguments().getString("password", "");

        mobilePrefix = PreferenceUtils.getInt(getContext(), Constants.AREA_CODE_KEY, mobilePrefix);

        LogUtils.e("USER_ID", sCoreManager.getSelf().getUserId());
    }

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.dialog_password_verification, container, false);

        initView(view);
        return view;
    }

    private void initView(View view) {
        LinearLayout llCode = view.findViewById(R.id.ll_code);
        btnGetCode = view.findViewById(R.id.btn_get_code);
        TextView tvHintEmailCode = view.findViewById(R.id.tv_hint_email_code);
        Button btnConfirm = view.findViewById(R.id.btn_confirm);
        btnConfirm.setOnClickListener(this);

        ButtonColorChange.colorDrawableStroke(getContext(), llCode);
        ButtonColorChange.colorChange(getContext(), btnGetCode);
        ButtonColorChange.textChange(getContext(), tvHintEmailCode);
        ButtonColorChange.colorChange(getContext(), btnConfirm);

        btnGetCode.setOnClickListener(this);

        TextView tvBing = view.findViewById(R.id.tv_bind);
        tvBingAccount = view.findViewById(R.id.tv_bind_account);
        tvBing.setText(verificationType == LoginHelper.LOGIN_PHONE ? R.string.binding_phone : R.string.binding_email);
        tvBingAccount.setText(verificationType == LoginHelper.LOGIN_PHONE ? changePhoneNumber(account) : hideEmail(account));


        mAuthCodeEdit = view.findViewById(R.id.et_auth_code);

        authCodeDialog = AuthCodeDialogFragment.newInstance(sCoreManager, passwordType, verificationType, account);
    }


    @Override
    public void onClick(View v) {
        switch (v.getId()) {
            case R.id.btn_get_code:
                Bundle bundle = new Bundle();
                bundle.putInt("verification_type", verificationType);
                bundle.putInt("passwordType", passwordType);
                bundle.putString("account", account);
                authCodeDialog.setArguments(bundle);
                authCodeDialog.setTargetFragment(this, 200);
                authCodeDialog.show(getFragmentManager(), AuthCodeDialogFragment.class.getSimpleName());
                break;
            case R.id.btn_confirm:
                // 确认修改
                if (nextStep()) {
                    // 如果验证码正确，则可以重置密码
                    if (passwordType == 1) {
                        if (verificationType == LoginHelper.LOGIN_PHONE) {
                            resetPassword();
                        } else if (verificationType == LoginHelper.LOGIN_EMAIL) {
                            resetPasswordEmail();
                        }
                    } else {
                        resetPasswordCapital();
                    }
                }
                break;
        }
    }

    /**
     * 修改资金密码
     */
    private void resetPasswordCapital() {
        if (verificationType == LoginHelper.LOGIN_PHONE) {
            verifyCodePhone(t -> {
                DialogHelper.dismissProgressDialog();
                Toast.makeText(getContext(), getString(R.string.error_network), Toast.LENGTH_SHORT).show();
            }, success -> {
                setPasswordCapital();
            });
        } else if (verificationType == LoginHelper.LOGIN_EMAIL) {
            verifyCodeEmail(t -> {
                DialogHelper.dismissProgressDialog();
                Toast.makeText(getContext(), getString(R.string.error_network), Toast.LENGTH_SHORT).show();
            }, success -> {
                setPasswordCapital();
            });
        }

    }


    private void setPasswordCapital() {
        User user = sCoreManager.getSelf();
        String userId = user.getUserId();
        Map<String, String> params = new HashMap<>();
        params.put("userId", userId);
        params.put("pwd", LoginPassword.encodeMd5(password));

        HttpUtils.post().url(sCoreManager.getConfig().RESET_CAPITAL_PASSWORD)
                .params(params)
                .build()
                .execute(new BaseCallback<Void>(Void.class) {
                    @Override
                    public void onResponse(ObjectResult<Void> result) {
                        DialogHelper.dismissProgressDialog();
                        if (Result.checkSuccess(getContext(), result)) {
                            PreferenceUtils.putBoolean(getContext(), Constants.IS_CAPITAL_PASSWORD_SET + userId, true);
                            Toast.makeText(getContext(), getString(R.string.reset_capital_password), Toast.LENGTH_SHORT).show();
                            dismiss();
                            getActivity().finish();
                        } else {
                            Toast.makeText(getContext(), result.getMsg(), Toast.LENGTH_SHORT).show();
                        }
                    }

                    @Override
                    public void onError(Call call, Exception e) {
                        DialogHelper.dismissProgressDialog();
                        Toast.makeText(getContext(), getString(R.string.error_network), Toast.LENGTH_SHORT).show();
                    }
                });
    }


    /**
     * 验证邮箱验证码
     */
    private void verifyCodeEmail(LoginSecureHelper.Function<Throwable> onError,
                                 LoginSecureHelper.Function<ObjectResult<Void>> onSuccess) {

        Map<String, String> params = new HashMap<>();

        DialogHelper.showDefaulteMessageProgressDialog(getActivity());

        String authCode = mAuthCodeEdit.getText().toString().trim();

        params.put("mailbox", account);
        params.put("mailboxCode", authCode);
        HttpUtils.post().url(sCoreManager.getConfig().VERIFY_CODE_CAPITAL_EMAIL)
                .params(params)
                .build()
                .execute(new BaseCallback<Void>(Void.class) {
                    @Override
                    public void onResponse(ObjectResult<Void> result) {
                        if (Result.checkSuccess(getContext(), result)) {
                            onSuccess.apply(result);
                        }
                    }

                    @Override
                    public void onError(Call call, Exception e) {
                        onError.apply(e);
                    }
                });
    }

    /**
     * 验证手机号验证码
     */
    private void verifyCodePhone(LoginSecureHelper.Function<Throwable> onError,
                                 LoginSecureHelper.Function<ObjectResult<Void>> onSuccess) {

        DialogHelper.showDefaulteMessageProgressDialog(getActivity());


        String authCode = mAuthCodeEdit.getText().toString().trim();

        Map<String, String> params = new HashMap<>();
        params.put("phone", mobilePrefix + account);
        params.put("verificationCode", authCode);
        HttpUtils.get().url(sCoreManager.getConfig().VERIFY_CODE_CAPITAL_PHONE)
                .params(params)
                .build()
                .execute(new BaseCallback<Void>(Void.class) {
                    @Override
                    public void onResponse(ObjectResult<Void> result) {
                        if (Result.checkSuccess(getContext(), result)) {
                            onSuccess.apply(result);
                        }
                    }

                    @Override
                    public void onError(Call call, Exception e) {
                        onError.apply(e);
                    }
                });
    }


    /**
     * 验证验证码
     */
    private boolean nextStep() {

        String account = tvBingAccount.getText().toString().trim();

        String authCode = mAuthCodeEdit.getText().toString().trim();

        if (verificationType == LoginHelper.LOGIN_PHONE) {
            if (TextUtils.isEmpty(account)) {
                Toast.makeText(getContext(), getString(R.string.hint_input_phone_number), Toast.LENGTH_SHORT).show();
                return false;
            }

            /**
             * 只判断中国手机号格式
             */
            if (!StringUtils.isMobileNumber(account) && mobilePrefix == 86) {
                Toast.makeText(getContext(), getString(R.string.Input_11_phoneNumber), Toast.LENGTH_SHORT).show();
                return false;
            }
        } else if (verificationType == LoginHelper.LOGIN_EMAIL) {
            if (TextUtils.isEmpty(account)) {
                Toast.makeText(getContext(), getString(R.string.hint_input_email), Toast.LENGTH_SHORT).show();
                return false;
            }
        }
        if (TextUtils.isEmpty(authCode)) {
            Toast.makeText(getContext(), getString(R.string.please_input_auth_code), Toast.LENGTH_SHORT).show();
            return false;
        } else if (authCode.equals(randcode)) {
            // 验证码正确
            return true;
        } else {
            Toast.makeText(getContext(), getString(R.string.please_input_auth_code), Toast.LENGTH_SHORT).show();
            return false;
        }
    }


    /**
     * 修改密码
     */
    private void resetPassword() {
        DialogHelper.showDefaulteMessageProgressDialog(getActivity());

        String authCode = mAuthCodeEdit.getText().toString().trim();
        Map<String, String> params = new HashMap<>();
        params.put("telephone", account);
        params.put("randcode", authCode);
        params.put("areaCode", String.valueOf(mobilePrefix));
        params.put("newPassword", LoginPassword.encodeMd5(password));

        HttpUtils.get().url(sCoreManager.getConfig().USER_PASSWORD_RESET)
                .params(params)
                .build(true, true)
                .execute(new BaseCallback<Void>(Void.class) {

                    @Override
                    public void onResponse(ObjectResult<Void> result) {
                        DialogHelper.dismissProgressDialog();
                        if (Result.checkSuccess(getContext(), result)) {
                            Toast.makeText(getContext(), getString(R.string.update_sccuess), Toast.LENGTH_SHORT).show();
                            if (sCoreManager.getSelf() != null
                                    && !TextUtils.isEmpty(sCoreManager.getSelf().getTelephone())) {
                                UserSp.getInstance(getContext()).clearUserInfo();
                                FLYApplication.getInstance().mUserStatus = LoginHelper.STATUS_USER_SIMPLE_TELPHONE;
                                sCoreManager.logout();
                                LoginHelper.broadcastLogout(getContext());
                                LoginHistoryActivity.start(getActivity());

                                //发送广播  重新拉起app
                                Intent intent = new Intent(BROADCASTTEST_ACTION);
                                intent.setComponent(new ComponentName(FLYAppConfig.sPackageName, FLYAppConfig.sPackageName + ".MyBroadcastReceiver"));
                                getContext().sendBroadcast(intent);
                            } else {// 本地连电话都没有，说明之前没有登录过 修改成功后直接跳转至登录界面
                                startActivity(new Intent(getActivity(), LoginActivity.class));
                            }
                            getActivity().finish();
                        }
                    }

                    @Override
                    public void onError(Call call, Exception e) {
                        DialogHelper.dismissProgressDialog();
                        Toast.makeText(getContext(), getString(R.string.error_network), Toast.LENGTH_SHORT).show();
                    }
                });
    }

    /**
     * 邮箱修改密码
     */
    private void resetPasswordEmail() {
        DialogHelper.showDefaulteMessageProgressDialog(getActivity());

        String authCode = mAuthCodeEdit.getText().toString().trim();
        Map<String, String> params = new HashMap<>();
        params.put("mailbox", account);
        params.put("mailboxCode", authCode);
        params.put("areaCode", String.valueOf(mobilePrefix));
        params.put("password", LoginPassword.encodeMd5(password));

        HttpUtils.get().url(sCoreManager.getConfig().USER_PASSWORD_RESET_EMAIL)
                .params(params)
                .build(true, true)
                .execute(new BaseCallback<Void>(Void.class) {

                    @Override
                    public void onResponse(ObjectResult<Void> result) {
                        DialogHelper.dismissProgressDialog();
                        if (Result.checkSuccess(getContext(), result)) {
                            Toast.makeText(getContext(), getString(R.string.update_sccuess), Toast.LENGTH_SHORT).show();
                            if (sCoreManager.getSelf() != null
                                    && !TextUtils.isEmpty(sCoreManager.getSelf().getTelephone())) {
                                UserSp.getInstance(getContext()).clearUserInfo();
                                FLYApplication.getInstance().mUserStatus = LoginHelper.STATUS_USER_SIMPLE_TELPHONE;
                                sCoreManager.logout();
                                LoginHelper.broadcastLogout(getContext());
                                LoginHistoryActivity.start(getContext());

                                //发送广播  重新拉起app
                                Intent intent = new Intent(BROADCASTTEST_ACTION);
                                intent.setComponent(new ComponentName(FLYAppConfig.sPackageName, FLYAppConfig.sPackageName + ".MyBroadcastReceiver"));
                                getContext().sendBroadcast(intent);
                            } else {// 本地连电话都没有，说明之前没有登录过 修改成功后直接跳转至登录界面
                                startActivity(new Intent(getActivity(), LoginActivity.class));
                            }
                            getActivity().finish();
                        }
                    }

                    @Override
                    public void onError(Call call, Exception e) {
                        DialogHelper.dismissProgressDialog();
                        Toast.makeText(getContext(), getString(R.string.error_network), Toast.LENGTH_SHORT).show();
                    }
                });
    }


    /**
     * 手机号中间变星号
     */
    private String changePhoneNumber(String s) {
        if (s.length() > 4) {
            String ss = s.substring(0, 3);
            String es = s.substring(s.length() - 4, s.length());
            return ss + "****" + es;
        } else {
            return s;
        }
    }

    /**
     * 邮箱只显示@前面的首位跟末位
     */
    private String hideEmail(String email) {
        String hiddenEmail = email.replaceAll("(\\w?)(\\w+)(\\w)(@\\w+\\.[a-z]+(\\.[a-z]+)?)", "$1****$3$4");
        return hiddenEmail;
    }

    @Override
    public void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        String imageCodeStr = data.getStringExtra("imgCode");

        if (passwordType == 1) {
            if (verificationType == LoginHelper.LOGIN_PHONE) {
                verifyTelephone(account, imageCodeStr);
            } else if (verificationType == LoginHelper.LOGIN_EMAIL) {
                requestEmailAuthCode(account, account + "_loginMailboxPassword", imageCodeStr);
            }
        } else {

            if (verificationType == LoginHelper.LOGIN_PHONE) {
                requestCapitalAuthCodePhone(account, account + "_capitalSendPhone", imageCodeStr);
            } else if (verificationType == LoginHelper.LOGIN_EMAIL) {
                requestEmailAuthCode(account, account + "_capitalSendMailboxCode", imageCodeStr);
            }

        }

    }

    /**
     * 获取修改资金 手机验证码
     *
     * @param account
     * @param key
     * @param imageCodeStr
     */
    private void requestCapitalAuthCodePhone(String account, String key, String imageCodeStr) {
        DialogHelper.showDefaulteMessageProgressDialog(getActivity());
        Map<String, String> params = new HashMap<>();
        String language = Locale.getDefault().getLanguage();
        params.put("telephone", account);
        params.put("language", language);
        params.put("areaCode", String.valueOf(mobilePrefix));
        params.put("key", mobilePrefix + key);
        params.put("imgCode", imageCodeStr);
        /**
         * 只判断中国手机号格式
         */
        if (!StringUtils.isMobileNumber(account) && mobilePrefix == 86) {
            Toast.makeText(getContext(), getString(R.string.Input_11_phoneNumber), Toast.LENGTH_SHORT).show();
            return;
        }
        HttpUtils.get().url(sCoreManager.getConfig().SEND_PHONE_AUTH_CODE_CAPITAL)
                .params(params)
                .build(true, true)
                .execute(new BaseCallback<Code>(Code.class) {
                    @Override
                    public void onResponse(ObjectResult<Code> result) {
                        DialogHelper.dismissProgressDialog();
                        if (Result.checkSuccess(getActivity(), result)) {
                            Toast.makeText(getActivity(), R.string.verification_code_send_success, Toast.LENGTH_SHORT).show();
                            btnGetCode.setEnabled(false);
                            // 开始计时
                            mReckonHandler.sendEmptyMessage(0x1);
                            if (result.getData() != null && result.getData().getCode() != null) {
                                // 得到验证码
                                randcode = result.getData().getCode();
                            }
                        }
                    }

                    @Override
                    public void onError(Call call, Exception e) {
                        DialogHelper.dismissProgressDialog();
                        Toast.makeText(getActivity(), getString(R.string.error_network), Toast.LENGTH_SHORT).show();
                    }
                });
    }

    /**
     * 邮箱 请求图形验证码
     */
    private void requestEmailAuthCode(String email, String emailKey, String imageCodeStr) {

        Map<String, String> params = new HashMap<>();
        params.put("email", email);
        params.put("key", emailKey);
        String language = Locale.getDefault().getLanguage();
        params.put("language", language);
        params.put("imgCode", imageCodeStr);
        params.put("isRegister", String.valueOf(0));
        params.put("version", "1");

        DialogHelper.showDefaulteMessageProgressDialog(getActivity());

        HttpUtils.get().url(sCoreManager.getConfig().SEND_EMAIL_AUTH_CODE)
                .params(params)
                .build(true, true)
                .execute(new BaseCallback<Code>(Code.class) {
                    @Override
                    public void onResponse(ObjectResult<Code> result) {
                        DialogHelper.dismissProgressDialog();
                        if (Result.checkSuccess(getContext(), result)) {
                            Toast.makeText(getContext(), R.string.verification_code_send_success, Toast.LENGTH_SHORT).show();
                            btnGetCode.setEnabled(false);
//                             开始倒计时
                            mReckonHandler.sendEmptyMessage(0x1);

                            if (result.getData() != null && result.getData().getCode() != null) {
                                // 得到验证码
                                randcode = result.getData().getCode();
                            }
                        }
                    }

                    @Override
                    public void onError(Call call, Exception e) {
                        DialogHelper.dismissProgressDialog();
                        Toast.makeText(getContext(), getString(R.string.error_network), Toast.LENGTH_SHORT).show();
                    }
                });

    }

    /**
     * 请求验证码
     */
    private void verifyTelephone(String phoneNumber, String imageCode) {
        DialogHelper.showDefaulteMessageProgressDialog(getActivity());
        Map<String, String> params = new HashMap<>();
        String language = Locale.getDefault().getLanguage();
        params.put("language", language);
        params.put("areaCode", String.valueOf(mobilePrefix));
        params.put("telephone", phoneNumber);
        params.put("imgCode", imageCode);
        params.put("isRegister", String.valueOf(0));
        params.put("version", "1");

        /**
         * 只判断中国手机号格式
         */
        if (!StringUtils.isMobileNumber(phoneNumber) && mobilePrefix == 86) {
            Toast.makeText(getContext(), getString(R.string.Input_11_phoneNumber), Toast.LENGTH_SHORT).show();
            return;
        }

        HttpUtils.get().url(sCoreManager.getConfig().SEND_AUTH_CODE)
                .params(params)
                .build(true, true)
                .execute(new BaseCallback<Code>(Code.class) {
                    @Override
                    public void onResponse(ObjectResult<Code> result) {
                        DialogHelper.dismissProgressDialog();
                        if (Result.checkSuccess(getActivity(), result)) {
                            Toast.makeText(getActivity(), R.string.verification_code_send_success, Toast.LENGTH_SHORT).show();
                            btnGetCode.setEnabled(false);
                            // 开始计时
                            mReckonHandler.sendEmptyMessage(0x1);
                            if (result.getData() != null && result.getData().getCode() != null) {
                                // 得到验证码
                                randcode = result.getData().getCode();
                            }
                        }
                    }

                    @Override
                    public void onError(Call call, Exception e) {
                        DialogHelper.dismissProgressDialog();
                        Toast.makeText(getActivity(), getString(R.string.error_network), Toast.LENGTH_SHORT).show();
                    }
                });
    }
}
