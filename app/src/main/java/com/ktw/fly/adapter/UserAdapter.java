package com.ktw.fly.adapter;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.ImageView;
import android.widget.TextView;

import com.baidu.mapapi.model.LatLng;
import com.baidu.mapapi.utils.DistanceUtil;
import com.ktw.fly.FLYApplication;
import com.ktw.fly.R;
import com.ktw.fly.bean.User;
import com.ktw.fly.helper.AvatarHelper;
import com.ktw.fly.util.ViewHolder;

import java.text.DecimalFormat;
import java.util.List;

public class UserAdapter extends BaseAdapter {
    private List<User> mUsers;
    private Context mContext;

    public UserAdapter(List<User> users, Context context) {
        mUsers = users;
        mContext = context;
    }

    @Override
    public int getCount() {
        return mUsers.size();
    }

    @Override
    public Object getItem(int position) {
        return position;
    }

    @Override
    public long getItemId(int position) {
        return position;
    }

    @Override
    public View getView(final int position, View convertView, ViewGroup parent) {
        if (convertView == null) {
            convertView = LayoutInflater.from(mContext).inflate(R.layout.row_user, parent, false);
        }
        ImageView avatar_img = ViewHolder.get(convertView, R.id.avatar_img);
        TextView nick_name_tv = ViewHolder.get(convertView, R.id.nick_name_tv);
        TextView des_tv = ViewHolder.get(convertView, R.id.des_tv);

        final User user = mUsers.get(position);
        // 设置头像
        AvatarHelper.getInstance().displayAvatar(user.getNickName(), user.getUserId(), avatar_img, true);

        double latitude = 0;
        double longitude = 0;
        double latitude_end = 0;
        double longitude_end = 0;
        if (user != null && user.getLoc() != null) {
            latitude = user.getLoc().getLat();
            longitude = user.getLoc().getLng();
        }

        if (FLYApplication.getInstance().getBdLocationHelper().getLatitude() != 0 && FLYApplication.getInstance().getBdLocationHelper().getLongitude() != 0) {
            latitude_end = FLYApplication.getInstance().getBdLocationHelper().getLatitude();
            longitude_end = FLYApplication.getInstance().getBdLocationHelper().getLongitude();
        }
        if (latitude != 0 && longitude != 0 && latitude_end != 0 && longitude_end != 0) {
            LatLng point_start = new LatLng(latitude, longitude);
            LatLng point_end = new LatLng(latitude_end, longitude_end);
            double distance = DistanceUtil.getDistance(point_start, point_end);
            DecimalFormat df = new DecimalFormat(".##");
            String value = df.format(distance);
            if (value.equals(".0")) {
                value = value.replaceAll("\\.", "");
            }
            des_tv.setText(mContext.getString(R.string.instance_person) + " " + value + " " + mContext.getString(R.string.instance_));
        } else {
            des_tv.setText(R.string.this_friend_not_open_position);
        }

        nick_name_tv.setText(user.getNickName());

        return convertView;
    }
}
