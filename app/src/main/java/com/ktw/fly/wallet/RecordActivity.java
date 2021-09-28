package com.ktw.fly.wallet;

import android.os.Bundle;
import android.view.View;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.chad.library.adapter.base.BaseQuickAdapter;
import com.chad.library.adapter.base.listener.OnItemClickListener;
import com.ktw.fly.R;
import com.ktw.fly.sp.UserSp;
import com.ktw.fly.ui.base.BaseActivity;
import com.ktw.fly.util.ToastUtil;
import com.ktw.fly.wallet.adapter.WalletRecordAdapter;
import com.ktw.fly.wallet.bean.CoinBean;
import com.ktw.fly.wallet.bean.CurrencyBean;
import com.ktw.fly.wallet.bean.WalletListBean;
import com.scwang.smartrefresh.layout.SmartRefreshLayout;
import com.scwang.smartrefresh.layout.api.RefreshLayout;
import com.scwang.smartrefresh.layout.listener.OnRefreshListener;
import com.xuan.xuanhttplibrary.okhttp.HttpUtils;
import com.xuan.xuanhttplibrary.okhttp.callback.BaseCallback;
import com.xuan.xuanhttplibrary.okhttp.callback.ListCallback;
import com.xuan.xuanhttplibrary.okhttp.result.ArrayResult;
import com.xuan.xuanhttplibrary.okhttp.result.ObjectResult;

import org.jetbrains.annotations.NotNull;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

import okhttp3.Call;

/**
 * 充提币记录
 */
public class RecordActivity extends BaseActivity {

    private SmartRefreshLayout mRefreshLayout;
    private RecyclerView mRv;

    private WalletRecordAdapter mAdapter;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_record_layout);
        getSupportActionBar().hide();
        findViewById(R.id.iv_cancel)
                .setOnClickListener(v -> finish());
        mRefreshLayout = findViewById(R.id.refresh_layout);
        mRv = findViewById(R.id.recycler_view);
        initListView();
        requestData();
    }

    private void initListView() {
        mRefreshLayout.setEnableLoadMore(false);
        mRefreshLayout.setOnRefreshListener(new OnRefreshListener() {
            @Override
            public void onRefresh(@NonNull @NotNull RefreshLayout refreshLayout) {
                refreshLayout.finishRefresh();
                requestData();
            }
        });
        mAdapter = new WalletRecordAdapter();
        mRv.setLayoutManager(new LinearLayoutManager(this));
        mRv.setAdapter(mAdapter);
        mAdapter.setOnItemClickListener(new OnItemClickListener() {
            @Override
            public void onItemClick(@NonNull @NotNull BaseQuickAdapter<?, ?> adapter, @NonNull @NotNull View view, int position) {
                RecordDetailActivity.actionStart(RecordActivity.this, mAdapter.getItem(position));
            }
        });
    }

    private void requestData() {
        Map<String, String> params = new HashMap<>();
        params.put("userId", UserSp.getInstance(this).getUserId(""));
        HttpUtils.post().url(Apis.COIN_WITHDRAW)
                .params(params)
                .build()
                .execute(new ListCallback<CoinBean>(CoinBean.class) {


                    @Override
                    public void onResponse(ArrayResult<CoinBean> result) {
                        if (result == null) {
                            return;
                        }
                        if (result.getResultCode() != 1) {
                            ToastUtil.showToast(RecordActivity.this, result.getMsg());
                            return;
                        }

                        List<CoinBean> data = result.getData();
                        if (data == null) {
                            return;
                        }
                        mAdapter.setNewInstance(data);
                    }

                    @Override
                    public void onError(Call call, Exception e) {
                        ToastUtil.showNetError(RecordActivity.this);
                    }
                });
    }
}