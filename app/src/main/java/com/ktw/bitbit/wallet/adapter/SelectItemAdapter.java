package com.ktw.bitbit.wallet.adapter;

import android.widget.ImageView;

import com.bumptech.glide.Glide;
import com.chad.library.adapter.base.BaseQuickAdapter;
import com.chad.library.adapter.base.viewholder.BaseViewHolder;
import com.ktw.bitbit.R;
import com.ktw.bitbit.wallet.bean.CurrencyBean;

import org.jetbrains.annotations.NotNull;

public class SelectItemAdapter extends BaseQuickAdapter<CurrencyBean, BaseViewHolder> {

    public SelectItemAdapter() {
        super(R.layout.item_select_currency_layout);
    }

    @Override
    protected void convert(@NotNull BaseViewHolder baseViewHolder, CurrencyBean currencyBean) {
        baseViewHolder.setGone(R.id.item_iv_select, !currencyBean.isSelect());
        Glide.with(getContext()).load(currencyBean.getPath()).into((ImageView) baseViewHolder.getView(R.id.item_iv_img));
        baseViewHolder.setText(R.id.item_tv_currency,currencyBean.getCurrencyName());
    }
}