package com.ktw.fly.ui.me.redpacket;

import android.util.Log;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import androidx.annotation.Nullable;
import androidx.recyclerview.widget.RecyclerView;

import com.ktw.fly.R;
import com.ktw.fly.bean.redpacket.ConsumeRecordItem;
import com.ktw.fly.helper.DialogHelper;
import com.ktw.fly.ui.base.BaseListActivity;
import com.ktw.fly.ui.mucfile.XfileUtils;
import com.ktw.fly.util.ToastUtil;
import com.xuan.xuanhttplibrary.okhttp.HttpUtils;
import com.xuan.xuanhttplibrary.okhttp.callback.BaseCallback;
import com.xuan.xuanhttplibrary.okhttp.result.ObjectResult;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

import okhttp3.Call;

import static com.ktw.fly.FLYAppConstant.LIVE_GIVE;
import static com.ktw.fly.FLYAppConstant.LIVE_RECEIVE;
import static com.ktw.fly.FLYAppConstant.PUT_RAISE_CASH;
import static com.ktw.fly.FLYAppConstant.RECEIVE_PAYMENTCODE;
import static com.ktw.fly.FLYAppConstant.RECEIVE_QRCODE;
import static com.ktw.fly.FLYAppConstant.RECEIVE_REDPACKET;
import static com.ktw.fly.FLYAppConstant.RECEIVE_TRANSFER;
import static com.ktw.fly.FLYAppConstant.REFUND_REDPACKET;
import static com.ktw.fly.FLYAppConstant.REFUND_TRANSFER;
import static com.ktw.fly.FLYAppConstant.SDKTRANSFR_PAY;
import static com.ktw.fly.FLYAppConstant.SEND_PAYMENTCODE;
import static com.ktw.fly.FLYAppConstant.SEND_QRCODE;
import static com.ktw.fly.FLYAppConstant.SEND_REDPACKET;
import static com.ktw.fly.FLYAppConstant.SEND_TRANSFER;
import static com.ktw.fly.FLYAppConstant.SYSTEM_HANDCASH;
import static com.ktw.fly.FLYAppConstant.SYSTEM_RECHARGE;
import static com.ktw.fly.FLYAppConstant.USER_RECHARGE;

/**
 * Created by wzw on 2016/9/26.
 */
public class MyConsumeRecord extends BaseListActivity<MyConsumeRecord.MyConsumeHolder> {
    private static final String TAG = "MyConsumeRecord";
    List<ConsumeRecordItem.PageDataEntity> datas = new ArrayList<>();

    @Nullable
    @Override
    protected Integer getMiddleDivider() {
        return R.drawable.divider_consume_record;
    }

    @Override
    public void initView() {
        super.initView();
        getSupportActionBar().hide();
        findViewById(R.id.iv_title_left).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                finish();
            }
        });
        TextView tvTitle = (TextView) findViewById(R.id.tv_title_center);
        tvTitle.setText(getResources().getString(R.string.bill));
    }

    @Override
    public void initDatas(int pager) {
        HashMap<String, String> params = new HashMap<String, String>();
        params.put("access_token", coreManager.getSelfStatus().accessToken);
        // 如果是下拉刷新就重新加载第一页
        params.put("pageIndex", pager + "");
        params.put("pageSize", "30");
        HttpUtils.get().url(coreManager.getConfig().CONSUMERECORD_GET)
                .params(params)
                .build()
                .execute(new BaseCallback<ConsumeRecordItem>(ConsumeRecordItem.class) {

                    @Override
                    public void onResponse(ObjectResult<ConsumeRecordItem> result) {
                        if (result.getData().getPageData() != null) {
                            if (pager == 0) {
                                datas.clear();
                            }
                            for (ConsumeRecordItem.PageDataEntity data : result.getData().getPageData()) {
                                final double money = data.getMoney();
                                boolean isZero = Double.toString(money).equals("0.0");
                                Log.d(TAG, "bool : " + isZero + " \t" + money);
                                if (!isZero) {
                                    datas.add(data);
                                }
                            }
                            if (result.getData().getPageData().size() != 30) {
                                more = false;
                            } else {
                                more = true;
                            }
                        } else {
                            more = false;
                        }
                        runOnUiThread(new Runnable() {
                            @Override
                            public void run() {
                                update(datas);
                            }
                        });
                    }

                    @Override
                    public void onError(Call call, Exception e) {
                        DialogHelper.dismissProgressDialog();
                        ToastUtil.showErrorNet(MyConsumeRecord.this);
                    }
                });
    }

    @Override
    public MyConsumeHolder initHolder(ViewGroup parent) {
        View v = mInflater.inflate(R.layout.consumerecord_item, parent, false);
        MyConsumeHolder holder = new MyConsumeHolder(v);
        return holder;
    }

    @Override
    public void fillData(MyConsumeHolder holder, int position) {
        ConsumeRecordItem.PageDataEntity info = datas.get(position);
        if (info != null) {
            long time = Long.valueOf(info.getTime());
            String StrTime = XfileUtils.fromatTime(time * 1000, "MM-dd HH:mm");
            holder.nameTv.setText(info.getDesc());
            holder.timeTv.setText(StrTime);
            switch (info.getType()) {
                case PUT_RAISE_CASH:
                case SEND_REDPACKET:
                case SEND_TRANSFER:
                case SEND_PAYMENTCODE:
                case LIVE_GIVE:
                case SEND_QRCODE:
                case SYSTEM_HANDCASH:
                case SDKTRANSFR_PAY:
                    holder.moneyTv.setTextColor(getResources().getColor(R.color.records_of_consumption));
                    holder.moneyTv.setText("-" + XfileUtils.fromatFloat(info.getMoney()));
                    break;
                case USER_RECHARGE:
                case SYSTEM_RECHARGE:
                case RECEIVE_REDPACKET:
                case REFUND_REDPACKET:
                case RECEIVE_TRANSFER:
                case REFUND_TRANSFER:
                case RECEIVE_QRCODE:
                case RECEIVE_PAYMENTCODE:
                case LIVE_RECEIVE:
                    holder.moneyTv.setTextColor(getResources().getColor(R.color.ji_jian_lan));
                    holder.moneyTv.setText("+" + XfileUtils.fromatFloat(info.getMoney()));
                    break;
            }
        }
    }

    class MyConsumeHolder extends RecyclerView.ViewHolder {
        private TextView nameTv, timeTv, moneyTv;

        MyConsumeHolder(View itemView) {
            super(itemView);
            nameTv = (TextView) itemView.findViewById(R.id.textview_name);
            timeTv = (TextView) itemView.findViewById(R.id.textview_time);
            moneyTv = (TextView) itemView.findViewById(R.id.textview_money);
        }
    }
}
