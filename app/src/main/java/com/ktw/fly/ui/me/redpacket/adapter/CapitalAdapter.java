package com.ktw.fly.ui.me.redpacket.adapter;

import android.widget.ImageView;

import com.bumptech.glide.Glide;
import com.chad.library.adapter.base.BaseQuickAdapter;
import com.chad.library.adapter.base.viewholder.BaseViewHolder;
import com.ktw.fly.R;
import com.ktw.fly.bean.Capital;

import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.List;

/**
 * @ProjectName: new-android
 * @Package: com.ktw.fly.ui.me.redpacket.adapter
 * @ClassName: CapitalAdapter
 * @Description: 资产Adapter
 * @Author: XY
 * @CreateDate: 2021/10/9
 * @UpdateUser:
 * @UpdateDate: 2021/10/9
 * @UpdateRemark: 更新说明
 * @Version: 1.0
 */
public class CapitalAdapter extends BaseQuickAdapter<Capital, BaseViewHolder> {

    public CapitalAdapter(int layoutResId, @Nullable List<Capital> data) {
        super(layoutResId, data);
    }

    @Override
    protected void convert(@NotNull BaseViewHolder holder, Capital capital) {
        Glide.with(getContext()).load(capital.getPath()).<ImageView>into(holder.getView(R.id.item_img_iv));
        holder.setText(R.id.item_currency_tv, capital.capitalName);
        holder.setVisible(R.id.item_right_iv,capital.isChoose);
    }
}
