package com.ktw.fly.ui.me.emot;

import android.content.Context;
import android.os.Build;
import android.text.TextUtils;
import android.view.View;
import android.view.ViewGroup;

import com.ktw.fly.R;
import com.ktw.fly.bean.MyCollectEmotPackageBean;
import com.ktw.fly.helper.AvatarHelper;
import com.ktw.fly.util.CommonAdapter;
import com.ktw.fly.util.CommonViewHolder;
import com.makeramen.roundedimageview.RoundedImageView;

import java.util.List;

public class MySingleEmotAdapter extends CommonAdapter<MyCollectEmotPackageBean> {

    MySingleEmotAdapter(Context context, List<MyCollectEmotPackageBean> data) {
        super(context, data);
    }

    @Override
    public View getView(int position, View convertView, ViewGroup parent) {
        CommonViewHolder viewHolder = CommonViewHolder.get(mContext, convertView, parent,
                R.layout.item_single_emot_image, position);
        RoundedImageView ivEmot = viewHolder.getView(R.id.ivEmot);
        MyCollectEmotPackageBean myEmotBean = data.get(position);
        if (myEmotBean != null) {
            if (position == 0) {
                ivEmot.setBackground(null);
                AvatarHelper.getInstance().displayResourceId(R.mipmap.ic_add_img, ivEmot);
            } else {
                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
                    ivEmot.setBackground(mContext.getDrawable(R.mipmap.default_error));
                }
                if (!TextUtils.isEmpty(myEmotBean.getFace().getPath().get(0))) {
                    AvatarHelper.getInstance().displayUrl(myEmotBean.getFace().getPath().get(0), ivEmot);
                }
            }
        }
        return viewHolder.getConvertView();
    }

}
