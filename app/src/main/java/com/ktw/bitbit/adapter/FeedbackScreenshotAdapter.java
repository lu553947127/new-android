package com.ktw.bitbit.adapter;

import android.content.Context;
import android.os.Build;
import android.text.TextUtils;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;

import com.ktw.bitbit.R;
import com.ktw.bitbit.helper.AvatarHelper;
import com.ktw.bitbit.util.CommonAdapter;
import com.ktw.bitbit.util.CommonViewHolder;
import com.makeramen.roundedimageview.RoundedImageView;

import java.util.List;

public class FeedbackScreenshotAdapter extends CommonAdapter<String> {

    public FeedbackScreenshotAdapter(Context context, List<String> data) {
        super(context, data);
    }

    @Override
    public View getView(int position, View convertView, ViewGroup parent) {
        CommonViewHolder viewHolder = CommonViewHolder.get(mContext, convertView, parent,
                R.layout.item_question_screenshot, position);
        RoundedImageView ivImg = viewHolder.getView(R.id.ivImg);
        ImageView ivDel = viewHolder.getView(R.id.ivDel);
        String imagePath = data.get(position);
        if (position == 0) {
            ivImg.setBackground(null);
            AvatarHelper.getInstance().displayResourceId(R.mipmap.ic_add_img, ivImg);
            ivDel.setVisibility(View.GONE);
        } else {
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
                ivImg.setBackground(mContext.getDrawable(R.mipmap.default_error));
            }
            if (!TextUtils.isEmpty(imagePath)) {
                AvatarHelper.getInstance().displayUrl(imagePath, ivImg);
            }
            ivDel.setVisibility(View.VISIBLE);
            ivDel.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View view) {
                    if (delImgListener != null) {
                        delImgListener.delImg(position);
                    }
                }
            });
        }
        return viewHolder.getConvertView();
    }

    public interface IDelImgListener {
        void delImg(int position);
    }

    IDelImgListener delImgListener;

    public void setDelImgListener(IDelImgListener delImgListener) {
        this.delImgListener = delImgListener;
    }
}
