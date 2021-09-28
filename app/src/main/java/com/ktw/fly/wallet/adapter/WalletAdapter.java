package com.ktw.fly.wallet.adapter;

import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.bumptech.glide.Glide;
import com.bumptech.glide.Priority;
import com.bumptech.glide.load.model.GlideUrl;
import com.ktw.fly.R;
import com.ktw.fly.wallet.bean.CurrencyBean;

import org.jetbrains.annotations.NotNull;

import java.io.File;
import java.util.List;

public class WalletAdapter extends RecyclerView.Adapter {

    private List<CurrencyBean> mData;

    public WalletAdapter(List<CurrencyBean> mData) {
        this.mData = mData;
    }

    public void addData(CurrencyBean bean) {
        mData.add(bean);
        notifyDataSetChanged();
    }

    public List<CurrencyBean> getData() {
        return mData;
    }

    @NonNull
    @NotNull
    @Override
    public RecyclerView.ViewHolder onCreateViewHolder(@NonNull @NotNull ViewGroup parent, int viewType) {
        View inflate = LayoutInflater.from(parent.getContext()).inflate(R.layout.item_wallet_layout, parent, false);
        return new WalletViewHolder(inflate);
    }

    @Override
    public void onBindViewHolder(@NonNull @NotNull RecyclerView.ViewHolder holder, int position) {
        WalletViewHolder viewHolder = (WalletViewHolder) holder;
        CurrencyBean currencyBean = mData.get(position);
        Glide.with(viewHolder.itemView.getContext()).load(currencyBean.getPath()).into(viewHolder.mImgIv);
        viewHolder.mTitleTv.setText(currencyBean.getCurrencyName());
        viewHolder.mNumberTv.setText(currencyBean.getSumAsstes());
        viewHolder.mPriceTv.setText(currencyBean.getUsdt());
    }

    public class WalletViewHolder extends RecyclerView.ViewHolder {

        public TextView mTitleTv, mNumberTv, mPriceTv;
        public ImageView mImgIv;

        public WalletViewHolder(@NonNull @NotNull View itemView) {
            super(itemView);
            mTitleTv = itemView.findViewById(R.id.item_currency_tv);
            mNumberTv = itemView.findViewById(R.id.item_number_tv);
            mPriceTv = itemView.findViewById(R.id.item_price_tv);
            mImgIv = itemView.findViewById(R.id.item_img_iv);
        }

        @Override
        public String toString() {
            return "WalletViewHolder{" +
                    "mTitleTv=" + mTitleTv +
                    ", mNumberTv=" + mNumberTv +
                    ", mPriceTv=" + mPriceTv +
                    ", mImgIv=" + mImgIv +
                    '}';
        }
    }


    @Override
    public int getItemCount() {
        return mData == null ? 0 : mData.size();
    }

    public void clear() {
        if (mData != null) {
            mData.clear();
        }
        notifyDataSetChanged();
    }

}