package com.ktw.bitbit.adapter;

import android.content.Context;
import android.text.TextUtils;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import com.ktw.bitbit.R;
import com.ktw.bitbit.bean.QuestionBean;
import com.ktw.bitbit.util.CommonAdapter;
import com.ktw.bitbit.util.CommonViewHolder;

import java.util.List;

public class QuestionChildListAdapter extends CommonAdapter<QuestionBean> {

    public QuestionChildListAdapter(Context context, List<QuestionBean> data) {
        super(context, data);
    }

    @Override
    public View getView(int position, View convertView, ViewGroup parent) {
        CommonViewHolder viewHolder = CommonViewHolder.get(mContext, convertView, parent,
                R.layout.item_child_question, position);

        TextView tvName = viewHolder.getView(R.id.tvName);

        QuestionBean dataBean = data.get(position);
        if (dataBean != null) {
            if (!TextUtils.isEmpty(dataBean.getTitle())) {
                tvName.setText(dataBean.getTitle());
            }
        }
        return viewHolder.getConvertView();
    }
}
