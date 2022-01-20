package com.ktw.bitbit.wallet.dapp;

import android.os.Bundle;
import android.view.View;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.GridLayoutManager;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.chad.library.adapter.base.BaseQuickAdapter;
import com.chad.library.adapter.base.listener.OnItemClickListener;
import com.ktw.bitbit.R;
import com.ktw.bitbit.ui.base.EasyFragment;
import com.ktw.bitbit.util.ToastUtil;
import com.ktw.bitbit.wallet.adapter.DappAdapter;
import com.ktw.bitbit.wallet.bean.DappBean;
import com.scwang.smartrefresh.layout.SmartRefreshLayout;
import com.scwang.smartrefresh.layout.api.RefreshLayout;
import com.scwang.smartrefresh.layout.listener.OnRefreshListener;

import org.jetbrains.annotations.NotNull;

public class DappsFragment extends EasyFragment {

    private TextView mNewTv, mAATv, mEthTv;
    private View mNewView, mAAView, mEthView;
    private RecyclerView mDappRv, mHotDappRv, mNewRv;
    private SmartRefreshLayout mRefreshLayout;

    private DappAdapter adapter2;

    @Override
    protected int inflateLayoutId() {
        return R.layout.fragment_dapp_layout;
    }

    @Override
    protected void onActivityCreated(Bundle savedInstanceState, boolean createView) {
        if (createView) {
            initView();
            initListener();
            initTitleLayout(0);
            initRv();
        }
    }

    private void initRv() {
        DappAdapter adapter = new DappAdapter(1);
        mDappRv.setLayoutManager(new GridLayoutManager(getActivity(), 5));
        mDappRv.setAdapter(adapter);
        adapter.addData(new DappBean(R.drawable.ic_test_1, "AA Mall"));
        adapter.addData(new DappBean(R.drawable.ic_test_2, "AA 浏览器"));
        adapter.addData(new DappBean(R.drawable.ic_test_3, "AA Exchange"));
        adapter.addData(new DappBean(R.drawable.ic_test_4, "AA Games"));
        adapter.addData(new DappBean(R.drawable.ic_test_5, "AA BZZ节点"));

        DappAdapter adapter1 = new DappAdapter(1);
        mHotDappRv.setLayoutManager(new GridLayoutManager(getActivity(), 5));
        mHotDappRv.setAdapter(adapter1);
        adapter1.addData(new DappBean(R.drawable.ic_hot_1, "族谱链"));
        adapter1.addData(new DappBean(R.drawable.ic_hot_2, "Uniswap"));
        adapter1.addData(new DappBean(R.drawable.ic_hot_3, "OpenSea"));
        adapter1.addData(new DappBean(R.drawable.ic_hot_4, "Synthetix"));
        adapter1.addData(new DappBean(R.drawable.ic_hot_5, "DeBank"));

        adapter2 = new DappAdapter(2);
        mNewRv.setLayoutManager(new LinearLayoutManager(getActivity()));
        mNewRv.setAdapter(adapter2);
        adapter2.addData(new DappBean(R.drawable.ic_aa_1, "Roolend.Finance", "Roolend.Finance是基于智能合约的去…"));
        adapter2.addData(new DappBean(R.drawable.ic_aa_2, "SubGame跨链桥", "支持模块舆论智能合约间的呼叫，实现…"));

        adapter.setOnItemClickListener(new OnItemClickListener() {
            @Override
            public void onItemClick(@NonNull @NotNull BaseQuickAdapter<?, ?> adapter, @NonNull @NotNull View view, int position) {
                ToastUtil.showToast(getActivity(),"敬请期待");
            }
        });

        adapter1.setOnItemClickListener(new OnItemClickListener() {
            @Override
            public void onItemClick(@NonNull @NotNull BaseQuickAdapter<?, ?> adapter, @NonNull @NotNull View view, int position) {
                ToastUtil.showToast(getActivity(),"敬请期待");
            }
        });

        adapter2.setOnItemClickListener(new OnItemClickListener() {
            @Override
            public void onItemClick(@NonNull @NotNull BaseQuickAdapter<?, ?> adapter, @NonNull @NotNull View view, int position) {
                ToastUtil.showToast(getActivity(),"敬请期待");
            }
        });
    }

    private void initListener() {
        mNewTv.setOnClickListener(v -> {
            initTitleLayout(0);
        });
        mAATv.setOnClickListener(v -> {
            initTitleLayout(1);
        });
        mEthTv.setOnClickListener(v -> {
            initTitleLayout(2);
        });
    }

    private void initTitleLayout(int position) {
        mNewView.setVisibility(position == 0 ? View.VISIBLE : View.GONE);
        mAAView.setVisibility(position == 1 ? View.VISIBLE : View.GONE);
        mEthView.setVisibility(position == 2 ? View.VISIBLE : View.GONE);

        if (adapter2 == null) {
            return;
        }

        adapter2.getData().clear();
        adapter2.notifyDataSetChanged();
        if (position == 0) {
            adapter2.addData(new DappBean(R.drawable.ic_aa_1, "Roolend.Finance", "Roolend.Finance是基于智能合约的去…"));
            adapter2.addData(new DappBean(R.drawable.ic_aa_2, "SubGame跨链桥", "支持模块舆论智能合约间的呼叫，实现…"));
        } else if (position == 1) {
            adapter2.addData(new DappBean(R.drawable.ic_test_1, "AA MegeMall", "去中心化+可追溯源性"));
            adapter2.addData(new DappBean(R.drawable.ic_test_2, "AA浏览器", "实时交易查询"));
        } else {
            adapter2.addData(new DappBean(R.drawable.ic_eth_1, "Commpound", "轻松借出或者借入多种代币"));
            adapter2.addData(new DappBean(R.drawable.ic_hot_4, "Synthetix", "去中心化合成资产发行交易平台"));

        }
    }

    private void initView() {
        mNewTv = findViewById(R.id.tv_new);
        mAATv = findViewById(R.id.tv_aa);
        mEthTv = findViewById(R.id.tv_eth);

        mNewView = findViewById(R.id.view_new);
        mAAView = findViewById(R.id.view_aa);
        mEthView = findViewById(R.id.view_eth);

        mNewRv = findViewById(R.id.new_rv);
        mHotDappRv = findViewById(R.id.hot_dapps_rv);
        mDappRv = findViewById(R.id.my_dapp_rv);

        mRefreshLayout = findViewById(R.id.refresh_layout);
        mRefreshLayout.setOnRefreshListener(new OnRefreshListener() {
            @Override
            public void onRefresh(@NonNull @NotNull RefreshLayout refreshLayout) {
                refreshLayout.finishRefresh();
            }
        });
        mRefreshLayout.setEnableLoadMore(false);
    }
}