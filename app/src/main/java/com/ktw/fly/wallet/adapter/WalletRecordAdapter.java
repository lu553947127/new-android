package com.ktw.fly.wallet.adapter;

import androidx.core.content.ContextCompat;

import com.chad.library.adapter.base.BaseQuickAdapter;
import com.chad.library.adapter.base.viewholder.BaseViewHolder;
import com.ktw.fly.R;
import com.ktw.fly.wallet.bean.CoinBean;

import org.jetbrains.annotations.NotNull;

public class WalletRecordAdapter extends BaseQuickAdapter<CoinBean, BaseViewHolder> {

    public WalletRecordAdapter() {
        super(R.layout.item_wallet_record_layout);
    }

    @Override
    protected void convert(@NotNull BaseViewHolder baseViewHolder, CoinBean coinBean) {
        baseViewHolder.setText(R.id.item_type_tv, coinBean.getTypeName());
        baseViewHolder.setText(R.id.item_time_tv, coinBean.getTime());
        if (coinBean.getTradeStatus() == 1) {
            //trading
            baseViewHolder.setText(R.id.item_status_tv, coinBean.getType() == 1 ? R.string.coinding : R.string.withdrawding);
//            baseViewHolder.setTextColor(R.id.item_status_tv, ContextCompat.getColor(getContext(), R.color.wallet_type_reading));
        } else if (coinBean.getTradeStatus() == 2) {
            baseViewHolder.setText(R.id.item_status_tv, R.string.over);
            baseViewHolder.setTextColor(R.id.item_status_tv, ContextCompat.getColor(getContext(), R.color.wallet_type_success));
        } else {
            baseViewHolder.setText(R.id.item_status_tv, R.string.fail);
//            baseViewHolder.setTextColor(R.id.item_status_tv, ContextCompat.getColor(getContext(), R.color.wallet_type_fail));
        }
        baseViewHolder.setText(R.id.item_number_tv, coinBean.getType() == 1 ? ("+" + coinBean.getAmount() + " " + coinBean.getCoinName()) : ("-" + coinBean.getSum()) + " " + coinBean.getCoinName());
    }
}