package com.ktw.bitbit.wallet;

import android.content.ClipboardManager;
import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.appcompat.widget.LinearLayoutCompat;

import com.bumptech.glide.Glide;
import com.ktw.bitbit.R;
import com.ktw.bitbit.ui.base.BaseActivity;
import com.ktw.bitbit.util.ToastUtil;
import com.ktw.bitbit.wallet.bean.CoinBean;

public class RecordDetailActivity extends BaseActivity {

    public static void actionStart(Context context, CoinBean coinBean) {
        Intent intent = new Intent(context, RecordDetailActivity.class);
        intent.putExtra("data", coinBean);
        context.startActivity(intent);
    }

    private ImageView mImgIv;
    private TextView mNumberTv, mUnitTv, mTypeTv, mStatusTv, mWalletAddressTv, mFeeTv, mArriveTv, mReviewTv, mReviewTimeTv, mCoinAddressTv, mTxidTv, mTradeTimeTv;
    private LinearLayoutCompat mCoinAddressLayout, mWalletAddressLayout, mFeeLayout, mArriveLayout, mReviewLayout, mReviewTimeLayout;
    private View mView1, mView2, mView3, mView4;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_record_detail_layout);
        setTitleString(R.string.detail);
        initViewId();
        initBundle();
    }

    private void initBundle() {
        CoinBean coinBean = (CoinBean) getIntent().getSerializableExtra("data");
        if (coinBean.getType() == 1) {
            mWalletAddressLayout.setVisibility(View.GONE);
            mFeeLayout.setVisibility(View.GONE);
            mArriveLayout.setVisibility(View.GONE);
            mReviewLayout.setVisibility(View.GONE);
            mReviewTimeLayout.setVisibility(View.GONE);
            mView1.setVisibility(View.GONE);
            mView2.setVisibility(View.GONE);
            mView3.setVisibility(View.GONE);
            mView4.setVisibility(View.GONE);
        } else {
            mCoinAddressLayout.setVisibility(View.GONE);
        }
        //??????
        Glide.with(this).load(coinBean.getPath()).into(mImgIv);
        //??????
        mNumberTv.setText(coinBean.getType() == 1 ? ("+" + coinBean.getAmount()) : ("-" + coinBean.getSum()) + " " + coinBean.getCoinName());
        //??????
        mTypeTv.setText(coinBean.getTypeName());
        //??????
        if (coinBean.getTradeStatus() == 1) {
            //trading
            mStatusTv.setText(coinBean.getType() == 1 ? R.string.coinding : R.string.withdrawding);
        } else if (coinBean.getTradeStatus() == 2) {
            mStatusTv.setText(R.string.over);
        } else {
            mStatusTv.setText(R.string.fail);
        }

        //??????hash
        mTxidTv.setText(coinBean.getTxId());
        //????????????
        mWalletAddressTv.setText(coinBean.getToAddress());
        //????????????
        mCoinAddressTv.setText(coinBean.getFromAddress());
        //?????????
        mFeeTv.setText(coinBean.getFee() + "");
        //????????????
        mArriveTv.setText(coinBean.getAmount() + "");
        //????????????
        mReviewTimeTv.setText(coinBean.getUpdateRealTime());
        //????????????
        mReviewTv.setText(coinBean.getRemark());
        //????????????
        mTradeTimeTv.setText(coinBean.getTime());
        mUnitTv.setText(coinBean.getCoinName());

        mCoinAddressTv.setOnClickListener(v -> {
            if (mCoinAddressTv.getText().length() == 0) {
                return;
            }
            //??????UID
            ClipboardManager cm = (ClipboardManager) getSystemService(Context.CLIPBOARD_SERVICE);
            cm.setText(mCoinAddressTv.getText().toString());
            ToastUtil.showToast(this, getString(R.string.z_tv_copy_success));
        });

        mTxidTv.setOnClickListener(v -> {
            if (mTxidTv.getText().length() == 0) {
                return;
            }
            //??????UID
            ClipboardManager cm = (ClipboardManager) getSystemService(Context.CLIPBOARD_SERVICE);
            cm.setText(mTxidTv.getText().toString());
            ToastUtil.showToast(this, getString(R.string.z_tv_copy_success));
        });

        mWalletAddressTv.setOnClickListener(v -> {
            if (mWalletAddressTv.getText().length() == 0) {
                return;
            }
            //??????UID
            ClipboardManager cm = (ClipboardManager) getSystemService(Context.CLIPBOARD_SERVICE);
            cm.setText(mWalletAddressTv.getText().toString());
            ToastUtil.showToast(this, getString(R.string.z_tv_copy_success));
        });


    }

    private void initViewId() {
        mImgIv = findViewById(R.id.iv_img);
        mNumberTv = findViewById(R.id.tv_number);
        mUnitTv = findViewById(R.id.tv_unit);
        mTypeTv = findViewById(R.id.tv_type);
        mStatusTv = findViewById(R.id.tv_status);
        mWalletAddressTv = findViewById(R.id.tv_wallet_address);
        mFeeTv = findViewById(R.id.tv_fee);
        mArriveTv = findViewById(R.id.tv_arrive);
        mReviewTv = findViewById(R.id.tv_review);
        mReviewTimeTv = findViewById(R.id.tv_review_time);
        mCoinAddressTv = findViewById(R.id.tv_coin_address);
        mTxidTv = findViewById(R.id.tv_hash);
        mTradeTimeTv = findViewById(R.id.tv_trade_time);

        mWalletAddressLayout = findViewById(R.id.ll_wallet_address);
        mCoinAddressLayout = findViewById(R.id.ll_coin_address);

        mReviewTimeLayout = findViewById(R.id.ll_review_time);
        mReviewLayout = findViewById(R.id.ll_review);
        mArriveLayout = findViewById(R.id.ll_arrive);
        mFeeLayout = findViewById(R.id.ll_fee);

        mView1 = findViewById(R.id.view_1);
        mView2 = findViewById(R.id.view_2);
        mView3 = findViewById(R.id.view_3);
        mView4 = findViewById(R.id.view_4);
    }
}