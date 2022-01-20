package com.ktw.bitbit.adapter;

import android.content.Context;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.ktw.bitbit.R;
import com.ktw.bitbit.bean.CustomerBean;
import com.ktw.bitbit.helper.AvatarHelper;
import com.ktw.bitbit.view.CircleImageView;

import org.jsoup.helper.StringUtil;

import java.util.List;

public class CustomerAdapter extends RecyclerView.Adapter<CustomerAdapter.MyViewHolder> {
    //当前上下文对象
    Context context;
    //RecyclerView填充Item数据的List对象
    List<CustomerBean> datas;

    public CustomerAdapter(Context context, List<CustomerBean> datas) {
        this.context = context;
        this.datas = datas;
    }

    //创建ViewHolder
    @NonNull
    @Override
    public MyViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        //实例化得到Item布局文件的View对象
        View v = View.inflate(context, R.layout.item_customer, null);
        //返回MyViewHolder的对象
        return new MyViewHolder(v);
    }


    //绑定数据
    @Override
    public void onBindViewHolder(@NonNull MyViewHolder holder, int position) {
        CustomerBean customerBean = datas.get(position);
        if (customerBean != null) {
            if (!StringUtil.isBlank(customerBean.getPath())) {
                AvatarHelper.getInstance().displayUrl(customerBean.getPath(), holder.ivAvatar);
            } else {
                holder.ivAvatar.setImageDrawable(context.getResources().getDrawable(R.mipmap.ic_noticy));
            }
            if (!StringUtil.isBlank(customerBean.getName())) {
                holder.tvName.setText(customerBean.getName());
            }
            if (!StringUtil.isBlank(customerBean.getType())) {
                holder.tvType.setText(customerBean.getType());
            }
            holder.itemView.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View view) {
                    if (listener != null) {
                        listener.onItemClick(position);
                    }
                }
            });
        }
    }

    //返回Item的数量
    @Override
    public int getItemCount() {
        return datas.size();
    }

    public void setDatas(List<CustomerBean> datas) {
        this.datas = datas;
        notifyDataSetChanged();
    }

    public interface OnItemClickListener {
        void onItemClick(int position);
    }

    OnItemClickListener listener;

    public OnItemClickListener getListener() {
        return listener;
    }

    public void setListener(OnItemClickListener listener) {
        this.listener = listener;
    }

    //继承RecyclerView.ViewHolder抽象类的自定义ViewHolder
    class MyViewHolder extends RecyclerView.ViewHolder {
        CircleImageView ivAvatar;
        TextView tvName;
        TextView tvType;

        public MyViewHolder(View itemView) {
            super(itemView);
            ivAvatar = itemView.findViewById(R.id.ivAvatar);
            tvName = itemView.findViewById(R.id.tvName);
            tvType = itemView.findViewById(R.id.tvType);
        }
    }
}