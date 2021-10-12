package com.ktw.fly.wallet.adapter;

import android.net.Uri;
import android.widget.ImageView;

import com.bumptech.glide.Glide;
import com.bumptech.glide.load.model.GlideUrl;
import com.chad.library.adapter.base.BaseQuickAdapter;
import com.chad.library.adapter.base.viewholder.BaseViewHolder;
import com.ktw.fly.R;
import com.ktw.fly.ui.FLYSplashActivity;
import com.ktw.fly.wallet.bean.DappBean;

import org.jetbrains.annotations.NotNull;

import java.io.File;

public class DappAdapter extends BaseQuickAdapter<DappBean, BaseViewHolder> {

    private int mType  ;

    public DappAdapter(int type) {
        super(type == 1 ? R.layout.item_dapp_layout : R.layout.item_dapp_new_layout);
        mType = type;
    }

    @Override
    protected void convert(@NotNull BaseViewHolder baseViewHolder, DappBean dappBean) {
        if (mType == 1) {
            Glide.with(getContext())
                    .load(dappBean.getImg())
                    .into((ImageView) baseViewHolder.getView(R.id.iv_bg));
            baseViewHolder.setText(R.id.tv_title, dappBean.getTitle());
        } else {
            Glide.with(getContext())
                    .load(dappBean.getImg())
                    .into((ImageView) baseViewHolder.getView(R.id.iv_bg));
            baseViewHolder.setText(R.id.tv_title, dappBean.getTitle());
            baseViewHolder.setText(R.id.tv_title, dappBean.getContent());
        }
    }
}