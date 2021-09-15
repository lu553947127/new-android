package com.ktw.fly.ui.me.emot;

import android.content.Context;
import android.text.TextUtils;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import com.ktw.fly.bean.MyCollectEmotPackageBean;
import com.makeramen.roundedimageview.RoundedImageView;
import com.ktw.fly.R;
import com.ktw.fly.helper.AvatarHelper;
import com.ktw.fly.util.CommonAdapter;
import com.ktw.fly.util.CommonViewHolder;

import java.util.List;

public class MyEmotAdapter extends CommonAdapter<MyCollectEmotPackageBean> {

    MyEmotAdapter(Context context, List<MyCollectEmotPackageBean> data) {
        super(context, data);
    }

    @Override
    public View getView(int position, View convertView, ViewGroup parent) {
        CommonViewHolder viewHolder = CommonViewHolder.get(mContext, convertView, parent,
                R.layout.item_my_emot_set, position);
        RoundedImageView ivEmot = viewHolder.getView(R.id.ivEmot);
        TextView tvEmotName = viewHolder.getView(R.id.tvEmotName);
        TextView tvRemove = viewHolder.getView(R.id.tvRemove);
        MyCollectEmotPackageBean myEmotBean = data.get(position);
        MyCollectEmotPackageBean.FaceBean faceBean =  myEmotBean.getFace();
        if (faceBean != null) {
            if (!TextUtils.isEmpty(faceBean.getPath().get(0))) {
                AvatarHelper.getInstance().displayUrl(faceBean.getPath().get(0), ivEmot);
            }
            if (!TextUtils.isEmpty(faceBean.getName())) {
                tvEmotName.setText(faceBean.getName());
            }
            tvRemove.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View view) {
                    if (delMyEmotPackageInterface != null) {
                        delMyEmotPackageInterface.delEmot(position);
                    }
                }
            });

        }
        return viewHolder.getConvertView();
    }

    public interface IDelMyEmotPackageInterface {
        void delEmot(int position);
    }

    public IDelMyEmotPackageInterface delMyEmotPackageInterface;

    public void setDelMyEmotPackageInterface(IDelMyEmotPackageInterface delMyEmotPackageInterface) {
        this.delMyEmotPackageInterface = delMyEmotPackageInterface;
    }
}
