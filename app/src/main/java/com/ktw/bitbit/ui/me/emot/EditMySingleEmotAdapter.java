package com.ktw.bitbit.ui.me.emot;

import android.content.Context;
import android.os.Build;
import android.text.TextUtils;
import android.view.View;
import android.view.ViewGroup;
import android.widget.CheckBox;
import android.widget.CompoundButton;

import com.makeramen.roundedimageview.RoundedImageView;
import com.ktw.bitbit.R;
import com.ktw.bitbit.helper.AvatarHelper;
import com.ktw.bitbit.util.CommonAdapter;
import com.ktw.bitbit.util.CommonViewHolder;

import java.util.List;

public class EditMySingleEmotAdapter extends CommonAdapter<MyEmotBean> {

    EditMySingleEmotAdapter(Context context, List<MyEmotBean> data) {
        super(context, data);
    }

    @Override
    public View getView(int position, View convertView, ViewGroup parent) {
        CommonViewHolder viewHolder = CommonViewHolder.get(mContext, convertView, parent,
                R.layout.item_edit_single_emot_image, position);
        RoundedImageView ivEmot = viewHolder.getView(R.id.ivEmot);
        CheckBox cbCheck = viewHolder.getView(R.id.cbCheck);
        MyEmotBean myEmotBean = data.get(position);
        if (myEmotBean != null) {
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
                ivEmot.setBackground(mContext.getDrawable(R.mipmap.default_error));
            }
            if (!TextUtils.isEmpty(myEmotBean.getUrl())) {
                AvatarHelper.getInstance().displayUrl(myEmotBean.getUrl(), ivEmot);
            }
            cbCheck.setChecked(false);
            cbCheck.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
                @Override
                public void onCheckedChanged(CompoundButton compoundButton, boolean b) {
                    if (checkEmotListener != null) {
                        checkEmotListener.checkEmot(position, b);
                    }
                }
            });
        }
        return viewHolder.getConvertView();
    }


    public interface ICheckEmotListener {
        void checkEmot(int position, boolean isCheck);
    }

    public ICheckEmotListener checkEmotListener;

    public void setCheckEmotListener(ICheckEmotListener checkEmotListener) {
        this.checkEmotListener = checkEmotListener;
    }
}
