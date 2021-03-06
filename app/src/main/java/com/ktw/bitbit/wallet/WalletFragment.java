package com.ktw.bitbit.wallet;

import android.content.res.ColorStateList;
import android.graphics.drawable.Drawable;
import android.os.Bundle;
import android.view.View;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.core.graphics.drawable.DrawableCompat;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.ktw.bitbit.R;
import com.ktw.bitbit.sp.UserSp;
import com.ktw.bitbit.ui.base.EasyFragment;
import com.ktw.bitbit.util.SkinUtils;
import com.ktw.bitbit.util.ToastUtil;
import com.ktw.bitbit.view.MergerStatus;
import com.ktw.bitbit.wallet.adapter.WalletAdapter;
import com.ktw.bitbit.wallet.bean.CurrencyBean;
import com.ktw.bitbit.wallet.bean.WalletListBean;
import com.scwang.smartrefresh.layout.SmartRefreshLayout;
import com.scwang.smartrefresh.layout.api.RefreshLayout;
import com.scwang.smartrefresh.layout.listener.OnRefreshLoadMoreListener;
import com.xuan.xuanhttplibrary.okhttp.HttpUtils;
import com.xuan.xuanhttplibrary.okhttp.callback.BaseCallback;
import com.xuan.xuanhttplibrary.okhttp.result.ObjectResult;

import org.jetbrains.annotations.NotNull;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import okhttp3.Call;

public class WalletFragment extends EasyFragment {

    private TextView mCoinTv, mWithdrawTv;
    private MergerStatus mToolBar;
    private View mView;
    private SmartRefreshLayout mRefreshLayout;
    private RecyclerView mRv;

    private TextView mAllPriceTv;

    private WalletAdapter mWalletAdapter;

    @Override
    protected int inflateLayoutId() {
        return R.layout.fragment_wallet_layout;
    }

    @Override
    protected void onActivityCreated(Bundle savedInstanceState, boolean createView) {
        if (createView) {
            initView();
            requestData();
        }
    }

    private void initView() {

        mCoinTv = findViewById(R.id.tv_coin);
        mWithdrawTv = findViewById(R.id.tv_withdaw);
        mToolBar = findViewById(R.id.tool_bar);
        mView = findViewById(R.id.view_1);
        mAllPriceTv = findViewById(R.id.tv_all_price);

        mRefreshLayout = findViewById(R.id.refresh_layout);
        mRv = findViewById(R.id.recycler_view);

        ColorStateList tabColor = SkinUtils.getSkin(getActivity()).getWalletColorState();

        // ???????????????????????????????????????
        Drawable drawable = mCoinTv.getCompoundDrawables()[0];
        drawable = DrawableCompat.wrap(drawable);
        DrawableCompat.setTintList(drawable, tabColor);
        // ?????????getDrawable?????????Drawable???????????????setCompoundDrawables??????????????????
        mCoinTv.setCompoundDrawablesWithIntrinsicBounds(drawable, null, null, null);

        findViewById(R.id.view_1).setBackgroundTintList(tabColor);

        Drawable drawable1 = mWithdrawTv.getCompoundDrawables()[0];
        drawable1 = DrawableCompat.wrap(drawable1);
        DrawableCompat.setTintList(drawable1, tabColor);
        // ?????????getDrawable?????????Drawable???????????????setCompoundDrawables??????????????????
        mWithdrawTv.setCompoundDrawablesWithIntrinsicBounds(drawable1, null, null, null);

        mWithdrawTv.setBackgroundTintList(tabColor);
        mCoinTv.setBackgroundTintList(tabColor);
        mView.setBackgroundTintList(tabColor);

        initRv();

        mCoinTv.setOnClickListener(v -> {
            if (mWalletAdapter.getData().size() == 0) {
                return;
            }
            CoinActivity.actionStart(getActivity(), mWalletAdapter.getData(),null);
        });
        mWithdrawTv.setOnClickListener(v -> {
            if (mWalletAdapter.getData().size() == 0) {
                return;
            }
            WithdrawActivity.actionStart(getActivity(), mWalletAdapter.getData(), null);
        });
    }

    private void initRv() {
        mRefreshLayout.setEnableLoadMore(false);
        mRefreshLayout.setOnRefreshLoadMoreListener(new OnRefreshLoadMoreListener() {
            @Override
            public void onLoadMore(@NonNull @NotNull RefreshLayout refreshLayout) {
                refreshLayout.finishLoadMore();
            }

            @Override
            public void onRefresh(@NonNull @NotNull RefreshLayout refreshLayout) {
                refreshLayout.finishRefresh();
                requestData();
            }
        });

        mRv.setLayoutManager(new LinearLayoutManager(getActivity()));
        mWalletAdapter = new WalletAdapter(new ArrayList<>());
        mRv.setAdapter(mWalletAdapter);

    }

    private void requestData() {
        Map<String, String> params = new HashMap<>();
        params.put("userId", UserSp.getInstance(getActivity()).getUserId(""));
        HttpUtils.post().url(Apis.USER_ASSET)
                .params(params)
                .build()
                .execute(new BaseCallback<WalletListBean>(WalletListBean.class) {

                    @Override
                    public void onResponse(ObjectResult<WalletListBean> result) {
                        if (result == null) {
                            return;
                        }
                        if (result.getResultCode() != 1) {
                            ToastUtil.showToast(getActivity(), result.getMsg());
                            return;
                        }

                        WalletListBean data = result.getData();

                        if (data == null) {
                            return;
                        }

                        mAllPriceTv.setText(data.getSum());

                        List<CurrencyBean> list = data.getList();

                        if (list == null) {
                            return;
                        }
                        mWalletAdapter.getData().clear();
                        mWalletAdapter.getData().addAll(list);
                        mWalletAdapter.notifyDataSetChanged();
                    }

                    @Override
                    public void onError(Call call, Exception e) {
                        if (getContext() != null) {
                            ToastUtil.showNetError(requireContext());
                        }
                    }
                });
    }
}