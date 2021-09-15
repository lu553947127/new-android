package com.ktw.fly.ui.me.emot;

import android.content.Context;
import android.text.TextUtils;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import com.ktw.fly.R;
import com.ktw.fly.helper.AvatarHelper;
import com.ktw.fly.util.CommonAdapter;
import com.ktw.fly.util.CommonViewHolder;
import com.makeramen.roundedimageview.RoundedImageView;

import java.util.List;

public class EmotDetailAdapter extends CommonAdapter<String> {

    EmotDetailAdapter(Context context, List<String> data) {
        super(context, data);
    }

    @Override
    public View getView(int position, View convertView, ViewGroup parent) {
        CommonViewHolder viewHolder = CommonViewHolder.get(mContext, convertView, parent,
                R.layout.item_emot_image, position);
        RoundedImageView ivEmot = viewHolder.getView(R.id.ivEmot);
        TextView tvEmotName = viewHolder.getView(R.id.tvEmotName);
        String path = data.get(position);
        if(!TextUtils.isEmpty(path)){
            AvatarHelper.getInstance().displayUrl(path, ivEmot);
        }
        tvEmotName.setVisibility(View.GONE);
        return viewHolder.getConvertView();
    }

}
