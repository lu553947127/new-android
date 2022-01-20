package com.ktw.bitbit.ui.me;

import android.content.Context;
import android.os.Bundle;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ListView;
import android.widget.TextView;

import com.ktw.bitbit.R;
import com.ktw.bitbit.bean.WithdrawalRecordBean;
import com.ktw.bitbit.helper.DialogHelper;
import com.ktw.bitbit.ui.base.BaseActivity;
import com.ktw.bitbit.util.CommonAdapter;
import com.ktw.bitbit.util.CommonViewHolder;
import com.ktw.bitbit.util.ToastUtil;
import com.xuan.xuanhttplibrary.okhttp.HttpUtils;
import com.xuan.xuanhttplibrary.okhttp.callback.ListCallback;
import com.xuan.xuanhttplibrary.okhttp.result.ArrayResult;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.List;

import okhttp3.Call;

/**
 * Created by wzw on 2016/9/26.
 */
public class WithdrawalsRecordActivity extends BaseActivity {
    private static final String TAG = "WithdrawalsRecordActivity";
    private ListView mListView;
    private WithdrawalsRecordAdapter withdrawalsRecordAdapter;
    List<WithdrawalRecordBean> recordBeanList = new ArrayList<>();

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.a_activity_switch_language);
        initView();
        initData();
    }

    private void initData() {
        getRecordList();
    }

    protected void initView() {
        getSupportActionBar().hide();
        findViewById(R.id.iv_title_left).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                finish();
            }
        });
        TextView tvTitle = (TextView) findViewById(R.id.tv_title_center);
        tvTitle.setText("提现记录");

        initUI();
    }

    void initUI() {
        mListView = (ListView) findViewById(R.id.lg_lv);
        withdrawalsRecordAdapter = new WithdrawalsRecordAdapter(this, recordBeanList);
        mListView.setAdapter(withdrawalsRecordAdapter);
    }

    class WithdrawalsRecordAdapter extends CommonAdapter<WithdrawalRecordBean> {

        WithdrawalsRecordAdapter(Context context, List<WithdrawalRecordBean> data) {
            super(context, data);
        }

        @Override
        public View getView(int position, View convertView, ViewGroup parent) {
            CommonViewHolder viewHolder = CommonViewHolder.get(mContext, convertView, parent,
                    R.layout.item_withdrawals_record, position);

            try {
                WithdrawalRecordBean withdrawalRecordBean = data.get(position);
                if (withdrawalRecordBean != null) {
                    TextView tvPlatform = viewHolder.getView(R.id.tvPlatform);
                    TextView tvAmount = viewHolder.getView(R.id.tvAmount);
                    TextView tvFailedReason = viewHolder.getView(R.id.tvFailedReason);
                    TextView tvDate = viewHolder.getView(R.id.tvDate);
                    TextView tvStatus = viewHolder.getView(R.id.tvStatus);

                    tvPlatform.setText("平台：" + withdrawalRecordBean.getPlatformName());
                    tvAmount.setText("金额：¥ " + withdrawalRecordBean.getAmount());

                    SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd HH:mm");
                    tvDate.setText(sdf.format(new Date(withdrawalRecordBean.getCreateTime())));

                    if (withdrawalRecordBean.getStatus() == 0) {
                        //申请中
                        tvStatus.setText("申请中");
                        tvStatus.setTextColor(getResources().getColor(R.color.app_skin_blue));
                        tvFailedReason.setVisibility(View.GONE);
                    } else if (withdrawalRecordBean.getStatus() == 1) {
                        //通过
                        tvStatus.setText("成功");
                        tvStatus.setTextColor(getResources().getColor(R.color.app_skin_blue));
                        tvFailedReason.setVisibility(View.GONE);
                    } else {
                        //拒绝
                        tvStatus.setText("失败");
                        tvStatus.setTextColor(getResources().getColor(R.color.color_role5));
                        tvFailedReason.setVisibility(View.VISIBLE);
                        tvFailedReason.setText("失败原因："+withdrawalRecordBean.getRefuseReason());
                    }

                }
            } catch (Exception e) {
                e.printStackTrace();
            }

            return viewHolder.getConvertView();
        }


        public void setDatas(List<WithdrawalRecordBean> datas) {
            this.data = datas;
            notifyDataSetChanged();
        }
    }

    private void getRecordList() {
        DialogHelper.showDefaulteMessageProgressDialog(this);
        HashMap<String, String> params = new HashMap<String, String>();
        params.put("userId", coreManager.getSelf().getUserId());
        HttpUtils.get().url(coreManager.getConfig().API_WITHDRAWL_LIST)
                .params(params)
                .build()
                .execute(new ListCallback<WithdrawalRecordBean>(WithdrawalRecordBean.class) {

                    @Override
                    public void onResponse(ArrayResult<WithdrawalRecordBean> result) {
                        DialogHelper.dismissProgressDialog();
                        if (result.getResultCode() == 1 && result.getData() != null && result.getData().size() > 0) {
                            recordBeanList = result.getData();
                            runOnUiThread(new Runnable() {
                                @Override
                                public void run() {
                                    withdrawalsRecordAdapter.setDatas(recordBeanList);
                                }
                            });

                        }
                    }

                    @Override
                    public void onError(Call call, Exception e) {
                        DialogHelper.dismissProgressDialog();
                        ToastUtil.showErrorNet(WithdrawalsRecordActivity.this);
                    }
                });
    }
}
