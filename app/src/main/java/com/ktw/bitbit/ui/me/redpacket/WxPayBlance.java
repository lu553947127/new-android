package com.ktw.bitbit.ui.me.redpacket;

import android.content.Intent;
import android.os.Bundle;
import android.text.TextUtils;
import android.view.View;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.annotation.Nullable;

import com.ktw.bitbit.R;
import com.ktw.bitbit.bean.event.EventNotifyByTag;
import com.ktw.bitbit.bean.redpacket.Balance;
import com.ktw.bitbit.ui.base.BaseActivity;
import com.ktw.bitbit.ui.tool.ButtonColorChange;
import com.ktw.bitbit.util.EventBusHelper;
import com.ktw.bitbit.util.SkinUtils;
import com.ktw.bitbit.util.ToastUtil;
import com.xuan.xuanhttplibrary.okhttp.HttpUtils;
import com.xuan.xuanhttplibrary.okhttp.callback.BaseCallback;
import com.xuan.xuanhttplibrary.okhttp.result.ObjectResult;

import java.text.DecimalFormat;
import java.util.HashMap;

import de.greenrobot.event.Subscribe;
import de.greenrobot.event.ThreadMode;
import okhttp3.Call;

public class WxPayBlance extends BaseActivity {
    private int REQUEST_CODE = 1106;

    public static final String RSA_PRIVATE = "";
    private static final int SDK_PAY_FLAG = 1;
    private static final int SDK_AUTH_FLAG = 2;
    private TextView mBalanceTv;
    private TextView mRechargeTv;
    private TextView mWithdrawTv;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_wx_pay_blance);
        initActionBar();
        initView();
        EventBusHelper.register(this);
    }

    @Override
    protected void onResume() {
        // todo 提现之后回到该界面，服务端待微信响应之后才会更新余额，此时调用刷新余额的方法获取到的可能还是之前的余额，另加一个EventBus来刷新吧
        super.onResume();
        initData();
    }

    private void initActionBar() {
        getSupportActionBar().hide();
        findViewById(R.id.iv_title_left).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                finish();
            }
        });
        TextView mTvTitle = (TextView) findViewById(R.id.tv_title_center);
        mTvTitle.setText(getString(R.string.my_purse));
        ImageView mImageView = findViewById(R.id.iv_title_right);
        mImageView.setImageDrawable(getResources().getDrawable(R.mipmap.navigation));
        mImageView.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                // 访问接口 获取记录
                Intent intent = new Intent(WxPayBlance.this, PaymentCenterActivity.class);
                startActivity(intent);
            }
        });
    }

    private void initView() {
        mBalanceTv = (TextView) findViewById(R.id.myblance);
        mRechargeTv = (TextView) findViewById(R.id.chongzhi);
        mWithdrawTv = (TextView) findViewById(R.id.quxian);
        ButtonColorChange.rechargeChange(this, mWithdrawTv, R.drawable.recharge_icon);
        ButtonColorChange.rechargeChange(this, mRechargeTv, R.drawable.chongzhi_icon);
        mWithdrawTv.setTextColor(SkinUtils.getSkin(this).getAccentColor());

        mRechargeTv.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Intent intent = new Intent(WxPayBlance.this, WxPayAdd.class);
                startActivityForResult(intent,REQUEST_CODE);
            }
        });

        mWithdrawTv.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Intent intent = new Intent(WxPayBlance.this, QuXianActivity.class);
                startActivity(intent);
            }
        });
        mWithdrawTv.setVisibility(View.GONE);

        findViewById(R.id.tvPayPassword).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(WxPayBlance.this, ChangePayPasswordActivity.class);
                startActivity(intent);
            }
        });
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (requestCode== REQUEST_CODE && resultCode == RESULT_OK){
            initData();
        }
    }

    private void initData() {
        HashMap<String, String> params = new HashMap<String, String>();
        params.put("access_token", coreManager.getSelfStatus().accessToken);

        HttpUtils.get().url(coreManager.getConfig().RECHARGE_GET)
                .params(params)
                .build()
                .execute(new BaseCallback<Balance>(Balance.class) {

                    @Override
                    public void onResponse(ObjectResult<Balance> result) {
                        if (result.getResultCode() == 1 && result.getData() != null) {
                            DecimalFormat decimalFormat = new DecimalFormat("0.00");
                            Balance balance = result.getData();
                            coreManager.getSelf().setBalance(Double.parseDouble(decimalFormat.format(balance.getBalance())));
                            mBalanceTv.setText("￥" + decimalFormat.format(Double.parseDouble(decimalFormat.format(balance.getBalance()))));
                        } else {
                            ToastUtil.showErrorData(WxPayBlance.this);
                        }
                    }

                    @Override
                    public void onError(Call call, Exception e) {
                        ToastUtil.showNetError(WxPayBlance.this);
                    }
                });
    }

    @Subscribe(threadMode = ThreadMode.MainThread)
    public void helloEventBus(EventNotifyByTag message) {
        if (TextUtils.equals(message.tag, EventNotifyByTag.Withdraw)) {
            initData();
        }
    }
}
