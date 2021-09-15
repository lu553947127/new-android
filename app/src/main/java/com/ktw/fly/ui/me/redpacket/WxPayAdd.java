package com.ktw.fly.ui.me.redpacket;

import android.annotation.SuppressLint;
import android.os.Bundle;
import android.text.TextUtils;
import android.view.View;
import android.view.WindowManager;
import android.widget.CheckedTextView;
import android.widget.TextView;
import android.widget.Toast;

import com.ktw.fly.BuildConfig;
import com.ktw.fly.R;
import com.ktw.fly.bean.event.EventPaySuccess;
import com.ktw.fly.bean.pay.PayBean;
import com.ktw.fly.bean.redpacket.Balance;
import com.ktw.fly.helper.DialogHelper;
import com.ktw.fly.ui.base.BaseActivity;
import com.ktw.fly.util.Constants;
import com.ktw.fly.util.EventBusHelper;
import com.ktw.fly.util.ToastUtil;
import com.tencent.mm.opensdk.constants.Build;
import com.tencent.mm.opensdk.modelbiz.WXLaunchMiniProgram;
import com.tencent.mm.opensdk.modelpay.PayReq;
import com.tencent.mm.opensdk.openapi.IWXAPI;
import com.tencent.mm.opensdk.openapi.WXAPIFactory;
import com.xuan.xuanhttplibrary.okhttp.HttpUtils;
import com.xuan.xuanhttplibrary.okhttp.callback.BaseCallback;
import com.xuan.xuanhttplibrary.okhttp.result.ObjectResult;
import com.xuan.xuanhttplibrary.okhttp.result.Result;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import de.greenrobot.event.Subscribe;
import de.greenrobot.event.ThreadMode;
import okhttp3.Call;

/**
 * 微信充值
 */
public class WxPayAdd extends BaseActivity {
    private IWXAPI api;

    private List<BigDecimal> mRechargeList = new ArrayList<>();
    private List<CheckedTextView> mRechargeMoneyViewList = new ArrayList<>();

    private TextView mSelectMoneyTv;
    private int mSelectedPosition = 0;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_wx_pay_add);

        api = WXAPIFactory.createWXAPI(this, Constants.VX_APP_ID, false);
        api.registerApp(Constants.VX_APP_ID);

        initActionBar();
        initData();
        initView();

        EventBusHelper.register(this);
    }

    @SuppressWarnings("unused")
    @Subscribe(threadMode = ThreadMode.MainThread)
    public void helloEventBus(EventPaySuccess message) {
        finish();
    }

    private void initActionBar() {
        getSupportActionBar().hide();
        findViewById(R.id.iv_title_left).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                finish();
            }
        });
        TextView tvTitle = (TextView) findViewById(R.id.tv_title_center);
        tvTitle.setText(getString(R.string.recharge));
    }

    private void initData() {
        mRechargeList.add(new BigDecimal("30"));
        mRechargeList.add(new BigDecimal("50"));
        mRechargeList.add(new BigDecimal("100"));
        mRechargeList.add(new BigDecimal("200"));
    }

    @SuppressLint("SetTextI18n")
    private void initView() {
        getWindow().setSoftInputMode(WindowManager.LayoutParams.SOFT_INPUT_STATE_VISIBLE);




        mSelectMoneyTv = findViewById(R.id.select_money_tv);


        findViewById(R.id.recharge_wechat).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (api.getWXAppSupportAPI() < Build.PAY_SUPPORTED_SDK_INT) {
                    Toast.makeText(getApplicationContext(), R.string.tip_no_wechat, Toast.LENGTH_SHORT).show();
                } else {
//                    recharge(getCurrentMoney());
                    createOrder(getCurrentMoney(),2);
                }
            }
        });
        findViewById(R.id.recharge_alipay).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                createOrder(getCurrentMoney(),1);
//                AlipayHelper.recharge(WxPayAdd.this, coreManager, getCurrentMoney());
            }
        });
    }

    private String getCurrentMoney() {
        if (TextUtils.isEmpty(mSelectMoneyTv.getText())) {
            return "0";
        }
        return new BigDecimal(mSelectMoneyTv.getText().toString()).stripTrailingZeros().toPlainString();
    }

    private void recharge(String money) {// 调用服务端接口，由服务端统一下单
        DialogHelper.showDefaulteMessageProgressDialog(this);

        Map<String, String> params = new HashMap<>();
        params.put("access_token", coreManager.getSelfStatus().accessToken);
        params.put("price", money);

        HttpUtils.get().url(coreManager.getConfig().VX_RECHARGE)
                .params(params)
                .build()
                .execute(new BaseCallback<Balance>(Balance.class) {

                    @Override
                    public void onResponse(ObjectResult<Balance> result) {
                        DialogHelper.dismissProgressDialog();
                        if (Result.checkSuccess(mContext, result)) {
                            PayReq req = new PayReq();
                            req.appId = result.getData().getAppId();
                            req.partnerId = result.getData().getPartnerId();
                            req.prepayId = result.getData().getPrepayId();
                            req.packageValue = "Sign=WXPay";
                            req.nonceStr = result.getData().getNonceStr();
                            req.timeStamp = result.getData().getTimeStamp();
                            req.sign = result.getData().getSign();
                            api.sendReq(req);
                        }
                    }

                    @Override
                    public void onError(Call call, Exception e) {
                        DialogHelper.dismissProgressDialog();
                        ToastUtil.showErrorNet(WxPayAdd.this);
                    }
                });
    }

    private void createOrder(String money,int type) {// 调用服务端接口，由服务端统一下单

        if (TextUtils.isEmpty(money) ||"0".equals(money)){
            ToastUtil.showToast(mContext,"请选择充值金额");
            return;
        }
        DialogHelper.showDefaulteMessageProgressDialog(this);
        Map<String, String> params = new HashMap<>();
        params.put("access_token", coreManager.getSelfStatus().accessToken);
        BigDecimal decimal = new BigDecimal(money);
        String decimalMoney = decimal.setScale(2, BigDecimal.ROUND_HALF_UP).toPlainString();
        params.put("price",decimalMoney);
        params.put("payType", String.valueOf(type));// 支付方式 1.支付宝 2.微信

        HttpUtils.get().url(coreManager.getConfig().GET_RECHARGE_ORDER)
                .params(params)
                .build()
                .execute(new BaseCallback<PayBean>(PayBean.class) {

                    @Override
                    public void onResponse(ObjectResult<PayBean> result) {
                        DialogHelper.dismissProgressDialog();
                        PayBean payBean = result.getData();
                        if (result.getResultCode() == 1 && payBean !=null) {
                            initPay(payBean);

                        }else {
                            ToastUtil.showToast(mContext,result.getResultMsg());

                        }
                    }

                    @Override
                    public void onError(Call call, Exception e) {
                        DialogHelper.dismissProgressDialog();
                        ToastUtil.showErrorNet(WxPayAdd.this);
                    }
                });
    }
    private void initPay(PayBean payBean){
        if (payBean!=null &&!TextUtils.isEmpty( payBean.getPath())){
            String appId = BuildConfig.WECHAT_APP_ID; // 填移动应用(App)的 AppId，非小程序的 AppID
            IWXAPI api = WXAPIFactory.createWXAPI(this, appId);
            WXLaunchMiniProgram.Req req = new WXLaunchMiniProgram.Req();
            req.userName = payBean.getUserName(); // 填小程序原始id
            req.path = payBean.getPath(); //拉起小程序页面的可带参路径，不填默认拉起小程序首页，对于小游戏，可以只传入 query 部分，来实现传参效果，如：传入 "?foo=bar"。
            req.miniprogramType = WXLaunchMiniProgram.Req.MINIPTOGRAM_TYPE_RELEASE;// 可选打开 开发版，体验版和正式版
            api.sendReq(req);

        }else {
            if (payBean==null || payBean.getMsg()==null){
                ToastUtil.showToast(this,"充值失败");
            }else {
                ToastUtil.showToast(this,payBean.getMsg());
            }
        }



//        Uri uri =  Uri.parse(bean.getPayMessage());
//        Intent intent = new Intent(Intent.ACTION_VIEW);
//
//
//        intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
//        intent.setData(uri);
//        startActivity(intent);
    }
}
