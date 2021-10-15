package com.ktw.fly.ui.me.redpacket;

import android.annotation.SuppressLint;
import android.content.Intent;
import android.os.Bundle;
import android.text.TextUtils;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.ListView;
import android.widget.TextView;

import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.bumptech.glide.Glide;
import com.chad.library.adapter.base.BaseQuickAdapter;
import com.chad.library.adapter.base.viewholder.BaseViewHolder;
import com.ktw.fly.R;
import com.ktw.fly.bean.Capital;
import com.ktw.fly.bean.Friend;
import com.ktw.fly.bean.RoomMember;
import com.ktw.fly.bean.redpacket.OpenRedpacket;
import com.ktw.fly.bean.redpacket.RedPacketResult;
import com.ktw.fly.bean.redpacket.RushRedPacket;
import com.ktw.fly.db.dao.FriendDao;
import com.ktw.fly.db.dao.RoomMemberDao;
import com.ktw.fly.helper.AvatarHelper;
import com.ktw.fly.helper.DialogHelper;
import com.ktw.fly.ui.base.BaseActivity;
import com.ktw.fly.ui.base.CoreManager;
import com.ktw.fly.ui.me.redpacket.adapter.CapitalAdapter;
import com.xuan.xuanhttplibrary.okhttp.HttpUtils;
import com.xuan.xuanhttplibrary.okhttp.callback.BaseCallback;
import com.xuan.xuanhttplibrary.okhttp.result.ObjectResult;

import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.text.DecimalFormat;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import okhttp3.Call;

/**
 * modify by zq
 * <p>
 * 红包详情
 */
public class RedDetailsActivity extends BaseActivity {

    private ImageView red_head_iv;
    private TextView red_nickname_tv;

    private TextView red_money_tv;
    private TextView red_money_bit_tv;

    private RecyclerView red_details_lsv;

    private RushRedPacket openRedpacket;

    private RedPacketResult redPacket;
    private TextView red_resultmsg_tv;

    //红包是否已经抢完  true 抢完了
    private boolean isNull;
    private TextView red_greeting_tv;
    private LinearLayout ll_money;

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_redpacket_details);
        Bundle bundle = getIntent().getExtras();
        openRedpacket = bundle.getParcelable("openRedpacket");
        isNull = bundle.getBoolean("null", false);
        redPacket = (RedPacketResult) bundle.getSerializable("redPacket");
        initView();
    }

    @SuppressLint("StringFormatInvalid")
    private void initView() {
        getSupportActionBar().hide();

        findViewById(R.id.red_back_tv).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                RedDetailsActivity.this.finish();
            }
        });


        red_head_iv = findViewById(R.id.red_head_iv);
        red_nickname_tv = findViewById(R.id.red_nickname_tv);

        red_money_tv = findViewById(R.id.get_money_tv);
        red_money_bit_tv = findViewById(R.id.get_money_bit_tv);
        red_details_lsv = findViewById(R.id.red_details_lsv);
        red_resultmsg_tv = findViewById(R.id.red_resultmsg_tv);
        red_greeting_tv = findViewById(R.id.red_greeting_tv);
        ll_money = findViewById(R.id.ll_money);


        if (isNull) {
            red_greeting_tv.setVisibility(View.VISIBLE);
            ll_money.setVisibility(View.GONE);
            red_greeting_tv.setText(openRedpacket.redUser.redEnvelopeName);
        } else {
            red_greeting_tv.setVisibility(View.GONE);
            ll_money.setVisibility(View.VISIBLE);
            red_money_tv.setText(formatMoney(openRedpacket.receiveUser.receiveCapital));
            red_money_bit_tv.setText(openRedpacket.receiveUser.currencyName);
        }

        AvatarHelper.getInstance().displayAvatar(redPacket.userName, redPacket.userId,
                red_head_iv, true);

        red_nickname_tv.setText(getString(R.string.someone_s_red_packet, redPacket.userName));


        if (openRedpacket.redCount.status == 0) {
            red_resultmsg_tv.setText(getString(R.string.example_red_packet_remain, openRedpacket.redCount.receivedRedEnvelopeCount,
                    openRedpacket.redCount.redEnvelopeCount, openRedpacket.redCount.receivedRedEnvelopeCapital, openRedpacket.redCapital.capitalCount));
        } else if (openRedpacket.redCount.status == 1) {
            String time = cal(Integer.valueOf(openRedpacket.redCount.time));

            red_resultmsg_tv.setText(getString(R.string.example_red_packet_loot_all,
                    openRedpacket.redCount.redEnvelopeCount, time));
        } else {
            red_resultmsg_tv.setText(getString(R.string.red_back_expires));
        }


        if (openRedpacket.redList != null && openRedpacket.redList.size() > 0) {
            RedDetailsAdapter capitalAdapter = new RedDetailsAdapter(R.layout.reditem_layout, openRedpacket.redList);
            red_details_lsv.setLayoutManager(new LinearLayoutManager(this));
            red_details_lsv.setAdapter(capitalAdapter);
        }

    }


    private String formatMoney(String money) {
        try {
            if (money.contains(".")) {
                for (int i = 0; i < money.length(); i++) {
                    int indMinPrice = money.indexOf(".");
                    String subMinPrice = money.substring(indMinPrice);
                    if (subMinPrice.length() - 1 == 1) {
                        return money + "0";
                    } else {
                        return money;
                    }
                }
            } else {
                return money + ".00";
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
        return "0.00";
    }


    public static String cal(int second) {
        int h = 0;
        int d = 0;
        int s = 0;
        int temp = second % 3600;
        if (second > 3600) {
            h = second / 3600;
            if (temp != 0) {
                if (temp > 60) {
                    d = temp / 60;
                    if (temp % 60 != 0) {
                        s = temp % 60;
                    }
                } else {
                    s = temp;
                }
            }
        } else {
            d = second / 60;
            if (second % 60 != 0) {
                s = second % 60;
            }
        }
        if (h > 0) {
            return h + "时" + d + "分" + s + "秒";
        } else {
            if (d > 0) {
                return d + "分" + s + "秒";
            } else {
                return s + "秒";
            }
        }
    }


    private class RedDetailsAdapter extends BaseQuickAdapter<RushRedPacket.Red, BaseViewHolder> {

        public RedDetailsAdapter(int layoutResId, @Nullable List<RushRedPacket.Red> data) {
            super(layoutResId, data);
        }

        @Override
        protected void convert(@NotNull BaseViewHolder holder, RushRedPacket.Red item) {
            AvatarHelper.getInstance().displayAvatar(item.receiveName, item.receiveId, holder.getView(R.id.red_head_iv), true);
            holder.setText(R.id.username_tv, item.receiveName);
            holder.setText(R.id.opentime_tv, item.receiveTime);
            holder.setText(R.id.money_tv, formatMoney(item.receiveCapital) + " " + item.currencyId);
            holder.setVisible(R.id.best_lucky_ll, item.status == 0);

            if (holder.getLayoutPosition() != 0) {
                holder.setGone(R.id.line1, true);
            }

        }
    }
}
