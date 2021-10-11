package com.ktw.fly.wallet;

import android.content.Intent;
import android.content.res.ColorStateList;
import android.graphics.drawable.Drawable;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.view.WindowManager;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.core.graphics.drawable.DrawableCompat;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.ktw.fly.R;
import com.ktw.fly.bean.circle.PublicMessage;
import com.ktw.fly.sp.UserSp;
import com.ktw.fly.ui.base.EasyFragment;
import com.ktw.fly.util.SkinUtils;
import com.ktw.fly.util.ToastUtil;
import com.ktw.fly.view.MergerStatus;
import com.ktw.fly.wallet.CoinActivity;
import com.ktw.fly.wallet.adapter.WalletAdapter;
import com.ktw.fly.wallet.bean.CurrencyBean;
import com.ktw.fly.wallet.bean.WalletListBean;
import com.scwang.smartrefresh.layout.SmartRefreshLayout;
import com.scwang.smartrefresh.layout.api.RefreshLayout;
import com.scwang.smartrefresh.layout.listener.OnRefreshLoadMoreListener;
import com.xuan.xuanhttplibrary.okhttp.HttpUtils;
import com.xuan.xuanhttplibrary.okhttp.callback.BaseCallback;
import com.xuan.xuanhttplibrary.okhttp.callback.ListCallback;
import com.xuan.xuanhttplibrary.okhttp.result.ArrayResult;
import com.xuan.xuanhttplibrary.okhttp.result.ObjectResult;
import com.xuan.xuanhttplibrary.okhttp.result.Result;

import org.jetbrains.annotations.NotNull;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import okhttp3.Call;

public class WalletFragment extends EasyFragment {

    private TextView mCoinTv, mWithDawTv;
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
        mWithDawTv = findViewById(R.id.tv_withdaw);
        mToolBar = findViewById(R.id.tool_bar);
        mView = findViewById(R.id.view_1);
        mAllPriceTv = findViewById(R.id.tv_all_price);

        mRefreshLayout = findViewById(R.id.refresh_layout);
        mRv = findViewById(R.id.recycler_view);

        ColorStateList tabColor = SkinUtils.getSkin(getActivity()).getWalletColorState();

        // 图标着色，兼容性解决方案，
        Drawable drawable = mCoinTv.getCompoundDrawables()[0];
        drawable = DrawableCompat.wrap(drawable);
        DrawableCompat.setTintList(drawable, tabColor);
        // 如果是getDrawable拿到的Drawable不能直接调setCompoundDrawables，没有宽高，
        mCoinTv.setCompoundDrawablesWithIntrinsicBounds(drawable, null, null, null);

        Drawable drawable1 = mWithDawTv.getCompoundDrawables()[0];
        drawable1 = DrawableCompat.wrap(drawable1);
        DrawableCompat.setTintList(drawable1, tabColor);
        // 如果是getDrawable拿到的Drawable不能直接调setCompoundDrawables，没有宽高，
        mWithDawTv.setCompoundDrawablesWithIntrinsicBounds(drawable1, null, null, null);

        mView.setBackgroundTintList(tabColor);
        mToolBar.setBackgroundTintList(tabColor);
        mWithDawTv.setBackgroundTintList(tabColor);
        mCoinTv.setBackgroundTintList(tabColor);

        initRv();

        mCoinTv.setOnClickListener(v -> {
            if (mWalletAdapter.getData().size()==0) {
                return;
            }
            CoinActivity.actionStart(getActivity(),mWalletAdapter.getData());
        });
        mWithDawTv.setOnClickListener(v -> {
            if (mWalletAdapter.getData().size()==0) {
                return;
            }
            WithdrawActivity.actionStart(getActivity(),mWalletAdapter.getData());
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