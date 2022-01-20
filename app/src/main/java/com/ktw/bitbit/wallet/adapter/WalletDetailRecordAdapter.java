package com.ktw.bitbit.wallet.adapter;

import com.chad.library.adapter.base.BaseQuickAdapter;
import com.chad.library.adapter.base.viewholder.BaseViewHolder;
import com.ktw.bitbit.R;
import com.ktw.bitbit.wallet.bean.CoinBean;

import org.jetbrains.annotations.NotNull;

public class WalletDetailRecordAdapter extends BaseQuickAdapter<CoinBean, BaseViewHolder> {

    public WalletDetailRecordAdapter() {
        super(R.layout.item_wallet_record_layout);
    }

    @Override
    protected void convert(@NotNull BaseViewHolder baseViewHolder, CoinBean coinBean) {
        baseViewHolder.setText(R.id.item_type_tv, coinBean.getSuorceName());
        baseViewHolder.setText(R.id.item_time_tv, coinBean.getTimes());
        baseViewHolder.setText(R.id.item_status_tv, coinBean.getType() == 1 ? ("+" + coinBean.getNumber() + " " + coinBean.getCoinName()) : ("-" + coinBean.getNumber()) + " " + coinBean.getCoinName());
        baseViewHolder.setVisible(R.id.item_number_tv, false  );
    }
}