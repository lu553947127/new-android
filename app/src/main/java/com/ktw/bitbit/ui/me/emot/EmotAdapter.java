package com.ktw.bitbit.ui.me.emot;

import android.content.Context;
import android.text.TextUtils;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import com.ktw.bitbit.util.CommonAdapter;
import com.makeramen.roundedimageview.RoundedImageView;
import com.ktw.bitbit.R;
import com.ktw.bitbit.helper.AvatarHelper;
import com.ktw.bitbit.util.CommonViewHolder;

import java.util.List;

public class EmotAdapter extends CommonAdapter<EmotBean> {

    boolean isEmotPackageDetail = false;

    public void setEmotPackageDetail(boolean emotPackageDetail) {
        isEmotPackageDetail = emotPackageDetail;
    }

    EmotAdapter(Context context, List<EmotBean> data) {
        super(context, data);
    }

    @Override
    public View getView(int position, View convertView, ViewGroup parent) {
        CommonViewHolder viewHolder = CommonViewHolder.get(mContext, convertView, parent,
                R.layout.item_emot_image, position);
        RoundedImageView ivEmot = viewHolder.getView(R.id.ivEmot);
        TextView tvEmotName = viewHolder.getView(R.id.tvEmotName);
        EmotBean emotBean = data.get(position);
        if (emotBean != null) {
            if (emotBean.getPath() != null && emotBean.getPath().size() > 0 && !TextUtils.isEmpty(emotBean.getPath().get(0))) {
                AvatarHelper.getInstance().displayUrl(emotBean.getPath().get(0), ivEmot);
            }
            if (!TextUtils.isEmpty(emotBean.getName())) {
                tvEmotName.setText(emotBean.getName());
            }
            if (isEmotPackageDetail) {
                tvEmotName.setVisibility(View.GONE);
            }
        }
        return viewHolder.getConvertView();
    }

}
