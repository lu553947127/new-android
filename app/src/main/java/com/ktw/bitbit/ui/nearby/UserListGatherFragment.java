package com.ktw.bitbit.ui.nearby;

import android.text.TextUtils;
import android.view.View;
import android.view.ViewGroup;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;

import androidx.recyclerview.widget.RecyclerView;

import com.makeramen.roundedimageview.RoundedImageView;
import com.ktw.bitbit.FLYApplication;
import com.ktw.bitbit.R;
import com.ktw.bitbit.bean.User;
import com.ktw.bitbit.helper.AvatarHelper;
import com.ktw.bitbit.helper.DialogHelper;
import com.ktw.bitbit.ui.base.BaseNearGridFragment;
import com.ktw.bitbit.ui.other.BasicInfoActivity;
import com.ktw.bitbit.util.DisplayUtil;
import com.ktw.bitbit.util.ScreenUtil;
import com.ktw.bitbit.util.TimeUtils;
import com.ktw.bitbit.view.CircleImageView;
import com.xuan.xuanhttplibrary.okhttp.HttpUtils;
import com.xuan.xuanhttplibrary.okhttp.callback.ListCallback;
import com.xuan.xuanhttplibrary.okhttp.result.ArrayResult;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

import okhttp3.Call;

/**
 * 附近的人-列表模式
 */
public class UserListGatherFragment extends BaseNearGridFragment<UserListGatherFragment.UserListGatherHolder> {
    double latitude;
    double longitude;
    private List<User> mUsers = new ArrayList<>();

    private boolean isPullDwonToRefersh;
    private int mPageIndex = 0;

    private String mKeyWord;// 关键字(keyword)
    private int mSex;       // 城市Id(cityId)
    private int mMinAge;    // 行业Id(industryId)
    private int mMaxAge;    // 职能Id(fnId)
    private int mShowTime;  // 日期(days)

    @Override
    public void initDatas(int pager) {
        if (pager == 0) {
            isPullDwonToRefersh = true;
        } else {
            isPullDwonToRefersh = false;
        }

        latitude = FLYApplication.getInstance().getBdLocationHelper().getLatitude();
        longitude = FLYApplication.getInstance().getBdLocationHelper().getLongitude();

        mKeyWord = getActivity().getIntent().getStringExtra("key_word");
        mSex = getActivity().getIntent().getIntExtra("sex", 0);
        mMinAge = getActivity().getIntent().getIntExtra("min_age", 0);
        mMaxAge = getActivity().getIntent().getIntExtra("max_age", 200);
        mShowTime = getActivity().getIntent().getIntExtra("show_time", 0);
        requestData(isPullDwonToRefersh);
    }

    private void requestData(final boolean isPullDwonToRefersh) {
        if (isPullDwonToRefersh) {
            mPageIndex = 0;
        }
        HashMap<String, String> params = new HashMap<String, String>();
        params.put("access_token", coreManager.getSelfStatus().accessToken);
        params.put("pageIndex", String.valueOf(mPageIndex));
        // params.put("pageSize", String.valueOf(AppConfig.PAGE_SIZE));
        params.put("pageSize", "20");
        if (!TextUtils.isEmpty(mKeyWord)) {
            params.put("nickname", mKeyWord);
        }
        if (mSex != 0) {
            params.put("sex", String.valueOf(mSex));
        }

        if (mMinAge != 0) {
            params.put("minAge", String.valueOf(mMinAge));
        }

        if (mMaxAge != 0) {
            params.put("maxAge", String.valueOf(mMaxAge));
        }

        params.put("active", String.valueOf(mShowTime));

        DialogHelper.showDefaulteMessageProgressDialog(getActivity());
        HttpUtils.get().url(coreManager.getConfig().USER_NEAR)
                .params(params)
                .build()
                .execute(new ListCallback<User>(User.class) {
                    @Override
                    public void onResponse(ArrayResult<User> result) {
                        DialogHelper.dismissProgressDialog();
                        mPageIndex++;
                        if (isPullDwonToRefersh) {
                            mUsers.clear();

                        }
                        List<User> datas = result.getData();
                        if (datas != null && datas.size() > 0) {
                            mUsers.addAll(datas);

                        }
                        update(mUsers);
                    }

                    @Override
                    public void onError(Call call, Exception e) {
                        DialogHelper.dismissProgressDialog();
                        Toast.makeText(getActivity(), R.string.check_network, Toast.LENGTH_SHORT).show();
                    }
                });
    }

    @Override
    public UserListGatherHolder initHolder(ViewGroup parent) {
        View v = mInflater.inflate(R.layout.item_nearby_grid, parent, false);
        return new UserListGatherHolder(v);
    }

    @Override
    public void fillData(UserListGatherHolder holder, int position) {
        if (mUsers != null && mUsers.size() > 0) {
            User data = mUsers.get(position);
            AvatarHelper.getInstance().displayRoundAvatar(data.getNickName(), data.getUserId(), holder.ivBgImg, false);
            AvatarHelper.getInstance().displayAvatar(data.getNickName(), data.getUserId(), holder.ivHead, true);
            holder.tvName.setText(data.getNickName());
            String distance = DisplayUtil.getDistance(latitude, longitude, data);
            holder.tvDistance.setText(distance);
            holder.tvTime.setText(TimeUtils.skNearbyTimeString(data.getCreateTime()));
        }
    }

    public void onItemClick(int position) {
        User user = mUsers.get(position);
        String userId = user.getUserId();
        int fromAddType;
        if (user.getNickName().contains(mKeyWord)) {
            fromAddType = BasicInfoActivity.FROM_ADD_TYPE_NAME;
        } else {
            // 昵称不包含关键字的话就是通过手机号搜索出来的，
            fromAddType = BasicInfoActivity.FROM_ADD_TYPE_PHONE;
        }
        BasicInfoActivity.start(requireActivity(), userId, fromAddType);
    }

    class UserListGatherHolder extends RecyclerView.ViewHolder {
        LinearLayout rootView;
        RoundedImageView ivBgImg;
        TextView tvName;
        CircleImageView ivHead;
        TextView tvDistance;
        TextView tvTime;

        UserListGatherHolder(View itemView) {
            super(itemView);
            rootView = (LinearLayout) itemView.findViewById(R.id.ll_nearby_grid_root);
            ivBgImg = (RoundedImageView) itemView.findViewById(R.id.iv_nearby_img);
            ivBgImg.setCornerRadius(ScreenUtil.dip2px(requireContext(), 7), ScreenUtil.dip2px(requireContext(), 7), 0, 0);
            tvName = (TextView) itemView.findViewById(R.id.tv_nearby_name);
            ivHead = (CircleImageView) itemView.findViewById(R.id.iv_nearby_head);
            tvDistance = (TextView) itemView.findViewById(R.id.tv_nearby_distance);
            tvTime = (TextView) itemView.findViewById(R.id.tv_nearby_time);
            rootView.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    onItemClick(getLayoutPosition());
                }
            });
        }
    }
}
