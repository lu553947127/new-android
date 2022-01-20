package com.ktw.bitbit.view;

import android.app.Dialog;
import android.content.Context;
import android.os.Bundle;
import android.text.TextUtils;
import android.view.Gravity;
import android.view.View;
import android.view.Window;
import android.view.WindowManager;
import android.widget.TextView;
import android.widget.Toast;

import com.ktw.bitbit.FLYApplication;
import com.ktw.bitbit.R;
import com.ktw.bitbit.bean.OrderInfo;
import com.ktw.bitbit.helper.DialogHelper;
import com.ktw.bitbit.helper.PaySecureHelper;
import com.ktw.bitbit.ui.base.CoreManager;
import com.ktw.bitbit.util.ScreenUtil;
import com.ktw.bitbit.util.ToastUtil;
import com.xuan.xuanhttplibrary.okhttp.HttpUtils;
import com.xuan.xuanhttplibrary.okhttp.callback.BaseCallback;
import com.xuan.xuanhttplibrary.okhttp.result.ObjectResult;

import java.util.HashMap;
import java.util.Map;

import okhttp3.Call;

/**
 * js调起的支付dialog
 */
public class PayDialog extends Dialog implements View.OnClickListener {
    private String appId, prepayId, sign;
    private OrderInfo orderInfo;
    private PayResultListener payResultListener;

    private TextView mMoneyTv, mOrderInfoTv;

    private Context context;

    public PayDialog(Context context, String appId, String prepayId, String sign, OrderInfo orderInfo, PayResultListener payResultListener) {
        super(context, R.style.BottomDialog);
        this.context = context;
        this.appId = appId;
        this.prepayId = prepayId;
        this.sign = sign;
        this.orderInfo = orderInfo;
        this.payResultListener = payResultListener;
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.pay_dialog);
        setCanceledOnTouchOutside(true);
        initView();
    }

    private void initView() {
        mMoneyTv = findViewById(R.id.money_tv);
        mOrderInfoTv = findViewById(R.id.order_info_tv);
        mMoneyTv.setText(orderInfo.getMoney());
        mOrderInfoTv.setText(orderInfo.getDesc());

        findViewById(R.id.close_iv).setOnClickListener(this);
        findViewById(R.id.sure_pay_btn).setOnClickListener(this);

        Window o = getWindow();
        WindowManager.LayoutParams lp = o.getAttributes();
        // x/y坐标
        // lp.x = 100;
        // lp.y = 100;
        lp.width = ScreenUtil.getScreenWidth(getContext());
        o.setAttributes(lp);
        this.getWindow().setGravity(Gravity.BOTTOM);
        this.getWindow().setWindowAnimations(R.style.BottomDialog_Animation);
    }

    @Override
    public void onClick(View v) {
        switch (v.getId()) {
            case R.id.close_iv:
                dismiss();
                break;
            case R.id.sure_pay_btn:
                PaySecureHelper.inputPayPassword(context, orderInfo.getDesc(), orderInfo.getMoney(), password -> pay(password));
                break;
        }
    }

    private void pay(String password) {
        DialogHelper.showDefaulteMessageProgressDialog(context);
        Map<String, String> params = new HashMap<String, String>();
        params.put("appId", appId);
        params.put("prepayId", prepayId);
        params.put("sign", sign);
        params.put("money", orderInfo.getMoney());

        PaySecureHelper.generateParam(
                context, password, params,
                "" + appId + prepayId + sign + orderInfo.getMoney(),
                t -> {
                    DialogHelper.dismissProgressDialog();
                    ToastUtil.showToast(context, context.getString(R.string.tip_pay_secure_place_holder, t.getMessage()));
                }, (p, code) -> {
                    // 获取订单信息
                    HttpUtils.get().url(CoreManager.requireConfig(FLYApplication.getContext()).PAY_PASSWORD_PAYMENT)
                            .params(p)
                            .build()
                            .execute(new BaseCallback<String>(String.class) {

                                @Override
                                public void onResponse(ObjectResult<String> result) {
                                    DialogHelper.dismissProgressDialog();
                                    if (result != null && result.getResultCode() == 1) {
                                        payResultListener.payResult(String.valueOf(1));
                                        dismiss();
                                    } else {
                                        if (result != null && !TextUtils.isEmpty(result.getResultMsg())) {
                                            Toast.makeText(context, result.getResultMsg(), Toast.LENGTH_SHORT).show();
                                        }
                                    }
                                }

                                @Override
                                public void onError(Call call, Exception e) {
                                    DialogHelper.dismissProgressDialog();
                                    ToastUtil.showErrorNet(context);
                                }
                            });
                });
    }

    public interface PayResultListener {
        void payResult(String result);
    }
}
