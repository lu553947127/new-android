package com.ktw.fly.wallet;

import android.content.Context;
import android.content.Intent;
import android.content.res.ColorStateList;
import android.graphics.drawable.Drawable;
import android.os.Bundle;
import android.text.TextUtils;
import android.view.View;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.core.graphics.drawable.DrawableCompat;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.bumptech.glide.Glide;
import com.chad.library.adapter.base.BaseQuickAdapter;
import com.chad.library.adapter.base.listener.OnItemClickListener;
import com.ktw.fly.R;
import com.ktw.fly.sp.UserSp;
import com.ktw.fly.ui.base.BaseActivity;
import com.ktw.fly.util.SkinUtils;
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

public class WalletDetailActivity extends BaseActivity {

    public static void actionStart(Context c, CurrencyBean currencyBean) {
        Intent intent = new Intent(c, WalletDetailActivity.class);
        intent.putExtra("data", currencyBean);
        c.startActivity(intent);
    }

    private SmartRefreshLayout mRefreshLayout;
    private RecyclerView mRv;
    private TextView mCoinTv, mWithdrawTv, mPriceTv;
    private ImageView mHeaderIv;
    private CurrencyBean mBean;
    private WalletRecordAdapter mAdapter;
    private TextView tv1;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_wallet_detail_layout);
        getSupportActionBar().hide();
        initView();
        initData();
        initListView();
        requestData();
    }

    private void initData() {
        mBean = (CurrencyBean) getIntent().getSerializableExtra("data");
        if (mBean == null) {
            return;
        }
        //图标
        Glide.with(this).load(mBean.getPath()).into(mHeaderIv);
        mPriceTv.setText(mBean.getFreezeAssets());
        TextView mTitle = findViewById(R.id.tv_title);
        mTitle.setText(mBean.getCurrencyName() + getString(R.string.string_1));
    }

    private void initView() {
        mRefreshLayout = findViewById(R.id.refresh_layout);
        mRv = findViewById(R.id.recycler_view);
        mCoinTv = findViewById(R.id.tv_coin);
        mWithdrawTv = findViewById(R.id.tv_withdaw);
        mPriceTv = findViewById(R.id.tv_all_price);
        mHeaderIv = findViewById(R.id.iv_icon);
        tv1 = findViewById(R.id.tv_1);

        findViewById(R.id.iv_go_back)
                .setOnClickListener(v -> finish());

        mCoinTv.setOnClickListener(v -> {
            if (mBean == null) {
                return;
            }

            requestData(1);
        });
        mWithdrawTv.setOnClickListener(v -> {
            if (mBean == null) {
                return;
            }
            requestData(2);
        });

        initTitleColor();
    }

    private void initTitleColor() {
        ColorStateList tabColor = SkinUtils.getSkin(this).getWalletColorState();
        // 图标着色，兼容性解决方案，
        Drawable drawable = mCoinTv.getCompoundDrawables()[0];
        drawable = DrawableCompat.wrap(drawable);
        DrawableCompat.setTintList(drawable, tabColor);
        // 如果是getDrawable拿到的Drawable不能直接调setCompoundDrawables，没有宽高，
        mCoinTv.setCompoundDrawablesWithIntrinsicBounds(drawable, null, null, null);


        Drawable drawable1 = mWithdrawTv.getCompoundDrawables()[0];
        drawable1 = DrawableCompat.wrap(drawable1);
        DrawableCompat.setTintList(drawable1, tabColor);
        // 如果是getDrawable拿到的Drawable不能直接调setCompoundDrawables，没有宽高，
        mWithdrawTv.setCompoundDrawablesWithIntrinsicBounds(drawable1, null, null, null);

        Drawable drawable2 = tv1.getCompoundDrawables()[0];
        drawable2 = DrawableCompat.wrap(drawable2);
        DrawableCompat.setTintList(drawable2, tabColor);
        // 如果是getDrawable拿到的Drawable不能直接调setCompoundDrawables，没有宽高，
        tv1.setCompoundDrawablesWithIntrinsicBounds(drawable2, null, null, null);
        View mView = findViewById(R.id.view_1);
        mView.setBackgroundTintList(tabColor);

//        mWithdrawTv.setBackgroundTintList(tabColor);
//        mCoinTv.setBackgroundTintList(tabColor);
//        tv1.setBackgroundTintList(tabColor);
    }

    private void initListView() {
        mRefreshLayout.setEnableLoadMore(false);
        mRefreshLayout.setOnRefreshListener(refreshLayout -> {
            refreshLayout.finishRefresh();
            requestData();
        });
        mAdapter = new WalletRecordAdapter();
        mRv.setLayoutManager(new LinearLayoutManager(this));
        mRv.setAdapter(mAdapter);
        mAdapter.setOnItemClickListener((adapter, view, position) -> RecordDetailActivity.actionStart(WalletDetailActivity.this, mAdapter.getItem(position)));
    }


    private void requestData(int type) {
        Map<String, String> params = new HashMap<>();
        params.put("userId", UserSp.getInstance(this).getUserId(""));
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
                            ToastUtil.showToast(WalletDetailActivity.this, result.getMsg());
                            return;
                        }

                        WalletListBean data = result.getData();

                        if (data == null) {
                            return;
                        }


                        List<CurrencyBean> list = data.getList();

                        if (list == null) {
                            return;
                        }
                        if (type == 1) {
                            CoinActivity.actionStart(WalletDetailActivity.this, list);
                        } else {
                            WithdrawActivity.actionStart(WalletDetailActivity.this, list);
                        }

                    }

                    @Override
                    public void onError(Call call, Exception e) {
                    }
                });
    }

    private void requestData() {
        if (mBean == null) {
            return;
        }
        Map<String, String> params = new HashMap<>();
        params.put("userId", UserSp.getInstance(this).getUserId(""));
        params.put("cionkey", mBean.getF01());
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
                            ToastUtil.showToast(WalletDetailActivity.this, result.getMsg());
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
                        ToastUtil.showNetError(WalletDetailActivity.this);
                    }
                });
    }
}