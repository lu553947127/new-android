package com.ktw.bitbit.wxapi;

import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.text.TextUtils;
import android.widget.Toast;

import com.ktw.bitbit.R;
import com.ktw.bitbit.bean.User;
import com.ktw.bitbit.bean.WXUploadResult;
import com.ktw.bitbit.bean.event.EventNotifyByTag;
import com.ktw.bitbit.helper.DialogHelper;
import com.ktw.bitbit.helper.PaySecureHelper;
import com.ktw.bitbit.ui.account.LoginActivity;
import com.ktw.bitbit.ui.account.SwitchLoginActivity;
import com.ktw.bitbit.ui.base.BaseActivity;
import com.ktw.bitbit.ui.me.redpacket.QuXianActivity;
import com.ktw.bitbit.util.Constants;
import com.ktw.bitbit.util.LogUtils;
import com.ktw.bitbit.util.ToastUtil;
import com.tencent.mm.opensdk.constants.ConstantsAPI;
import com.tencent.mm.opensdk.modelbase.BaseReq;
import com.tencent.mm.opensdk.modelbase.BaseResp;
import com.tencent.mm.opensdk.modelbiz.WXLaunchMiniProgram;
import com.tencent.mm.opensdk.modelmsg.SendAuth;
import com.tencent.mm.opensdk.openapi.IWXAPI;
import com.tencent.mm.opensdk.openapi.IWXAPIEventHandler;
import com.tencent.mm.opensdk.openapi.WXAPIFactory;
import com.xuan.xuanhttplibrary.okhttp.HttpUtils;
import com.xuan.xuanhttplibrary.okhttp.callback.BaseCallback;
import com.xuan.xuanhttplibrary.okhttp.result.ObjectResult;
import com.xuan.xuanhttplibrary.okhttp.result.Result;

import java.util.HashMap;
import java.util.Map;
import java.util.Objects;

import de.greenrobot.event.EventBus;
import okhttp3.Call;

public class WXEntryActivity extends BaseActivity implements IWXAPIEventHandler {

    private static Context mContext;
    private IWXAPI api;

    public WXEntryActivity() {
        // 微信登录的回调是到这里，所以可能没有登录，
        noLoginRequired();
    }

    public static void wxLogin(Context ctx) {
        mContext = ctx;
        IWXAPI api = WXAPIFactory.createWXAPI(ctx, Constants.VX_APP_ID, false);
        api.registerApp(Constants.VX_APP_ID);
        SendAuth.Req req = new SendAuth.Req();
        req.scope = "snsapi_userinfo";
        req.state = "login";
        api.sendReq(req);
    }

    public static void wxBand(Context ctx) {
        IWXAPI api = WXAPIFactory.createWXAPI(ctx, Constants.VX_APP_ID, false);
        api.registerApp(Constants.VX_APP_ID);
        SendAuth.Req req = new SendAuth.Req();
        req.scope = "snsapi_userinfo";
        req.state = "band";
        api.sendReq(req);
    }

    @Override
    public void finish() {
        setResult(RESULT_OK);
        super.finish();
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_vx_result);

        api = WXAPIFactory.createWXAPI(WXEntryActivity.this, Constants.VX_APP_ID, false);
        api.handleIntent(getIntent(), this);
    }

    // 微信发送请求到第三方应用时，会回调到该方法
    @Override
    protected void onNewIntent(Intent intent) {
        super.onNewIntent(intent);
        setIntent(intent);
        api.handleIntent(intent, this);
    }

    @Override
    public void onReq(BaseReq req) {

    }

    // 第三方应用发送到微信的请求处理后的响应结果，会回调到该方法
    @Override
    public void onResp(BaseResp resp) {
        LogUtils.log(TAG, resp);

        if (resp.getType()== ConstantsAPI.COMMAND_LAUNCH_WX_MINIPROGRAM){
            WXLaunchMiniProgram.Resp launchMiniProResp = (WXLaunchMiniProgram.Resp) resp;
            String extraData =launchMiniProResp.extMsg;

            finish();
        }else {
            switch (resp.errCode) {
                case BaseResp.ErrCode.ERR_OK:
                    if (resp instanceof SendAuth.Resp) {
                        SendAuth.Resp mSendAuthResp = ((SendAuth.Resp) resp);
                        if (Objects.equals(mSendAuthResp.state, "login")) {
                            getOpenId(mSendAuthResp.code);
                        } else if (Objects.equals(mSendAuthResp.state, "band")) {
                            bandAccount(mSendAuthResp.code);
                        } else {
                            updateCodeToService(mSendAuthResp.code);
                        }
                    } else if (resp.getType() == ConstantsAPI.COMMAND_SENDMESSAGE_TO_WX) {
                        Toast.makeText(this, R.string.share_succes, Toast.LENGTH_SHORT).show();
                        finish();
                    } else {
                        finish();
                    }
                    break;
                case BaseResp.ErrCode.ERR_USER_CANCEL:
                    if (resp.getType() == ConstantsAPI.COMMAND_SENDMESSAGE_TO_WX) {
                        Toast.makeText(this, R.string.share_cancel, Toast.LENGTH_SHORT).show();
                    }
                    finish();
                    break;
                case BaseResp.ErrCode.ERR_AUTH_DENIED:
                    if (resp.getType() == ConstantsAPI.COMMAND_SENDMESSAGE_TO_WX) {
                        Toast.makeText(this, R.string.share_failed, Toast.LENGTH_SHORT).show();
                    }
                    finish();
                    break;
                default:
                    finish();
                    break;
            }

        }

    }

    private void getOpenId(String code) {
        DialogHelper.showDefaulteMessageProgressDialog(this);
        Map<String, String> params = new HashMap<>();
        params.put("code", code);

        HttpUtils.get().url(coreManager.getConfig().VX_GET_OPEN_ID)
                .params(params)
                .build(true, true)
                .execute(new BaseCallback<WXUploadResult>(WXUploadResult.class) {

                    @Override
                    public void onResponse(ObjectResult<WXUploadResult> result) {
                        DialogHelper.dismissProgressDialog();
                        if (Result.checkSuccess(getApplicationContext(), result)) {
                            String openId = result.getData().getOpenid();
                            if (TextUtils.isEmpty(openId)) {
                                ToastUtil.showToast(getApplicationContext(), R.string.tip_server_error);
                            } else {
                                if (mContext.getClass().equals(LoginActivity.class)) {
                                    LoginActivity.bindThird(WXEntryActivity.this, result.getData());
                                } else if (mContext.getClass().equals(SwitchLoginActivity.class)) {
                                    SwitchLoginActivity.bindThird(WXEntryActivity.this, result.getData());
                                }
                            }
                        }
                        finish();
                    }

                    @Override
                    public void onError(Call call, Exception e) {
                        DialogHelper.dismissProgressDialog();
                        finish();
                    }
                });
    }

    private void bandAccount(String code) {
        DialogHelper.showDefaulteMessageProgressDialog(this);
        Map<String, String> params = new HashMap<>();
        params.put("code", code);

        HttpUtils.get().url(coreManager.getConfig().VX_GET_OPEN_ID)
                .params(params)
                .build(true, true)
                .execute(new BaseCallback<WXUploadResult>(WXUploadResult.class) {

                    @Override
                    public void onResponse(ObjectResult<WXUploadResult> result) {
                        DialogHelper.dismissProgressDialog();
                        if (Result.checkSuccess(getApplicationContext(), result)) {
                            String openId = result.getData().getOpenid();
                            if (TextUtils.isEmpty(openId)) {
                                Toast.makeText(WXEntryActivity.this, getString(R.string.tip_bind_server_success), Toast.LENGTH_SHORT).show();
                                EventBus.getDefault().post(new EventUpdateBandAccount("result", "err"));
                                finish();
                            } else {
                                bandOpenId(openId);
                            }
                        } else {
                            Toast.makeText(WXEntryActivity.this, getString(R.string.tip_bind_server_failed), Toast.LENGTH_SHORT).show();
                            EventBus.getDefault().post(new EventUpdateBandAccount("result", "err"));
                            finish();
                        }
                    }

                    @Override
                    public void onError(Call call, Exception e) {
                        DialogHelper.dismissProgressDialog();
                        finish();
                    }
                });
    }

    private void bandOpenId(String openId) {
        User user = coreManager.getSelf();
        DialogHelper.showDefaulteMessageProgressDialog(this);

        Map<String, String> params = new HashMap<>();
        params.put("access_token", coreManager.getSelfStatus().accessToken);
        params.put("telephone", user.getTelephone());
        params.put("type", "2");
        params.put("loginInfo", openId);
        params.put("password", user.getPassword());

        HttpUtils.get().url(coreManager.getConfig().USER_THIRD_BIND)
                .params(params)
                .build()
                .execute(new BaseCallback<Void>(Void.class) {

                    @Override
                    public void onResponse(ObjectResult<Void> result) {
                        DialogHelper.dismissProgressDialog();
                        if (result.getResultCode() == 1) {
                            Toast.makeText(WXEntryActivity.this, "绑定服务器成功", Toast.LENGTH_SHORT).show();
                            EventBus.getDefault().post(new EventUpdateBandAccount("result", "ok"));
                            finish();
                        } else {
                            Toast.makeText(WXEntryActivity.this, "绑定服务器失败", Toast.LENGTH_SHORT).show();
                            EventBus.getDefault().post(new EventUpdateBandAccount("result", "err"));
                            finish();
                        }
                    }

                    @Override
                    public void onError(Call call, Exception e) {
                        DialogHelper.dismissProgressDialog();
                        finish();
                    }
                });
    }

    private void updateCodeToService(String code) {
        DialogHelper.showDefaulteMessageProgressDialog(this);
        // amount单位是元，
        String money = QuXianActivity.amount;
        PaySecureHelper.inputPayPassword(this, getString(R.string.withdraw), money, password -> {

            Map<String, String> params = new HashMap<>();
            params.put("access_token", coreManager.getSelfStatus().accessToken);
            params.put("code", code);

            PaySecureHelper.generateParam(
                    this, password, params,
                    "" + code,
                    t -> {
                        DialogHelper.dismissProgressDialog();
                        ToastUtil.showToast(this, this.getString(R.string.tip_pay_secure_place_holder, t.getMessage()));
                    }, (p, pCode) -> {
                        HttpUtils.get().url(coreManager.getConfig().VX_UPLOAD_CODE)
                                .params(p)
                                .build()
                                .execute(new BaseCallback<WXUploadResult>(WXUploadResult.class) {

                                    @Override
                                    public void onResponse(ObjectResult<WXUploadResult> result) {
                                        DialogHelper.dismissProgressDialog();
                                        if (result.getResultCode() == 1 && result.getData() != null) {
                                            WXUploadResult wxUploadResult = result.getData();
                                            transfer(password);
                                        } else {
                                            Toast.makeText(WXEntryActivity.this, "绑定服务器失败", Toast.LENGTH_SHORT).show();
                                            finish();
                                        }
                                    }

                                    @Override
                                    public void onError(Call call, Exception e) {
                                        DialogHelper.dismissProgressDialog();
                                        finish();
                                    }
                                });
                    });
        }).setOnDismissListener(dialog1 -> {
            finish();
        });
    }

    private void transfer(final String password) {
        // amount单位是元，
        String money = QuXianActivity.amount;
        DialogHelper.showDefaulteMessageProgressDialog(WXEntryActivity.this);

        final Map<String, String> params = new HashMap<>();
        params.put("amount", money);

        PaySecureHelper.generateParam(
                this, password, params,
                "" + money,
                t -> {
                    DialogHelper.dismissProgressDialog();
                    ToastUtil.showToast(this, this.getString(R.string.tip_pay_secure_place_holder, t.getMessage()));
                }, (p, code) -> {
                    HttpUtils.post().url(coreManager.getConfig().VX_TRANSFER_PAY)
                            .params(p)
                            .build()
                            .execute(new BaseCallback<WXUploadResult>(WXUploadResult.class) {

                                @Override
                                public void onResponse(ObjectResult<WXUploadResult> result) {
                                    DialogHelper.dismissProgressDialog();
                                    if (result.getResultCode() == 1 && result.getData() != null) {
                                        EventBus.getDefault().post(new EventNotifyByTag(EventNotifyByTag.Withdraw));
                                        ToastUtil.showToast(WXEntryActivity.this, R.string.tip_withdraw_success);
                                    } else {
                                        ToastUtil.showToast(WXEntryActivity.this, result.getResultMsg());
                                    }
                                    finish();
                                }

                                @Override
                                public void onError(Call call, Exception e) {
                                    DialogHelper.dismissProgressDialog();
                                    finish();
                                    ToastUtil.showErrorData(WXEntryActivity.this);
                                }
                            });
                });
    }
}