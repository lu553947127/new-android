package com.ktw.fly.ui.me.redpacket;

import android.content.Context;
import android.content.Intent;
import android.content.res.ColorStateList;
import android.os.Bundle;
import android.view.View;
import android.widget.ImageView;
import android.widget.RadioButton;
import android.widget.RadioGroup;
import android.widget.RelativeLayout;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.appcompat.widget.LinearLayoutCompat;
import androidx.drawerlayout.widget.DrawerLayout;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.bumptech.glide.Glide;
import com.chad.library.adapter.base.BaseQuickAdapter;
import com.chad.library.adapter.base.listener.OnItemChildClickListener;
import com.chad.library.adapter.base.listener.OnItemClickListener;
import com.chad.library.adapter.base.viewholder.BaseViewHolder;
import com.ktw.fly.R;
import com.ktw.fly.bean.RedBacketCount;
import com.ktw.fly.bean.SendRedPacketInfo;
import com.ktw.fly.helper.AvatarHelper;
import com.ktw.fly.helper.DialogHelper;
import com.ktw.fly.ui.base.BaseActivity;
import com.ktw.fly.ui.tool.ButtonColorChange;
import com.ktw.fly.util.SkinUtils;
import com.xuan.xuanhttplibrary.okhttp.HttpUtils;
import com.xuan.xuanhttplibrary.okhttp.callback.BaseCallback;
import com.xuan.xuanhttplibrary.okhttp.callback.ListCallback;
import com.xuan.xuanhttplibrary.okhttp.result.ArrayResult;
import com.xuan.xuanhttplibrary.okhttp.result.ObjectResult;
import com.xuan.xuanhttplibrary.okhttp.result.Result;

import org.jetbrains.annotations.NotNull;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import okhttp3.Call;

/**
 * @ProjectName: new-android
 * @Package: com.ktw.fly.ui.me.redpacket
 * @ClassName: RedPacketListActivity
 * @Description: 红包明细列表
 * @Author: XY
 * @CreateDate: 2021/9/18
 * @UpdateUser:
 * @UpdateDate: 2021/9/18
 * @UpdateRemark: 更新说明
 * @Version: 1.0
 */
public class RedPacketListActivity extends BaseActivity implements RadioGroup.OnCheckedChangeListener {

    private LinearLayoutCompat mDrawLayout;

    private DrawerLayout mMainLayout;

    private RecyclerView mListView;
    private CapitalItemAdapter mAdapter;

    private RadioButton rbSendRed;
    private RadioButton rbReceiveRed;
    private TextView redAmountText;
    private TextView redTypeText;

    private RecyclerView redListView;
    private RedPaketItemAdapter sendRedAdapter;
    private ReceiverRedItemAdapter receiverRedAdapter;
    private TextView capitalNameText;

    private int listType;

    //发出红包列表
    private Map<String, List<RedBacketCount.RedBackerList>> sendRedMap;
    //接收红包列表
    private Map<String, List<RedBacketCount.RedBackerList>> receiveRedMap;

    //侧滑资产
    private List<RedBacketCount.CapitalList> sendCapitalList;
    private List<RedBacketCount.CapitalList> receiveCapitalList;

    public static void actionStart(Context context) {
        Intent intent = new Intent(context, RedPacketListActivity.class);
        context.startActivity(intent);
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_red_packet_list);

        initActionBar();

        initView();

        initData();
    }

    private void initData() {
        listType = 1;
        sendRedMap = new HashMap<>();
        receiveRedMap = new HashMap<>();
        sendRedData();
    }


    private void sendRedData() {
        DialogHelper.showDefaulteMessageProgressDialog(this);
        Map<String, String> params = new HashMap<>();
        params.put("userId", coreManager.getSelf().getUserId());
        HttpUtils.post().url(coreManager.getConfig().SEND_RED_PACKET_COUNT)
                .params(params)
                .build()
                .execute(new BaseCallback<RedBacketCount>(RedBacketCount.class) {
                    @Override
                    public void onResponse(ObjectResult<RedBacketCount> result) {
                        receiverData();
                        if (result == null || result.getData() == null) {
                            return;
                        }
                        if (Result.checkSuccess(getApplicationContext(), result)) {
                            sendCapitalList = result.getData().selectRedEnvelopesInfoCountUser;
                            List<RedBacketCount.RedBackerList> redList = result.getData().selectRedEnvelopesInfoCountUserInfo;
                            if (redList == null && redList.size() <= 0) {
                                return;
                            }
                            for (int x = 0; x < sendCapitalList.size(); x++) {
                                List<RedBacketCount.RedBackerList> sendRedList = new ArrayList<>();
                                for (int y = 0; y < redList.size(); y++) {
                                    if (sendCapitalList.get(x).capitalType.equals(redList.get(y).capital_type)) {
                                        sendRedList.add(redList.get(y));
                                    }
                                }
                                sendRedMap.put(sendCapitalList.get(x).capitalType, sendRedList);
                            }
                            mAdapter.setNewInstance(sendCapitalList);

                            initDataLayout(listType, 0);
                        }
                    }

                    @Override
                    public void onError(Call call, Exception e) {
                        Toast.makeText(getApplicationContext(), R.string.net_exception, Toast.LENGTH_SHORT).show();
                        DialogHelper.dismissProgressDialog();
                    }
                });

    }

    private void receiverData() {
        Map<String, String> params = new HashMap<>();
        params.put("userId", coreManager.getSelf().getUserId());
        HttpUtils.post().url(coreManager.getConfig().RECEIVER_RED_PACKET_GET_INFO)
                .params(params)
                .build()
                .execute(new BaseCallback<RedBacketCount>(RedBacketCount.class) {
                    @Override
                    public void onResponse(ObjectResult<RedBacketCount> result) {
                        DialogHelper.dismissProgressDialog();
                        if (Result.checkSuccess(getApplicationContext(), result)) {
                            receiveCapitalList = result.getData().selectRedEnvelopesInfoCountUser;
                            List<RedBacketCount.RedBackerList> redList = result.getData().selectRedEnvelopesInfoCountUserInfo;
                            if (redList == null && redList.size() <= 0) {
                                return;
                            }
                            for (int x = 0; x < receiveCapitalList.size(); x++) {
                                List<RedBacketCount.RedBackerList> receiverRedList = new ArrayList<>();
                                for (int y = 0; y < redList.size(); y++) {
                                    if (receiveCapitalList.get(x).capitalType.equals(redList.get(y).currency_name)) {
                                        receiverRedList.add(redList.get(y));
                                    }
                                }
                                receiveRedMap.put(receiveCapitalList.get(x).capitalType, receiverRedList);
                            }
                        }
                    }

                    @Override
                    public void onError(Call call, Exception e) {
                        Toast.makeText(getApplicationContext(), R.string.net_exception, Toast.LENGTH_SHORT).show();
                        DialogHelper.dismissProgressDialog();
                    }
                });

    }

    /**
     * 发出的红包领取记录
     */
    private void getSendRedPacketInfo(String id, RecyclerView rvReceive) {
        Map<String, String> params = new HashMap<>();
        params.put("redId", id);
        HttpUtils.post().url(coreManager.getConfig().SEND_RED_PACKET_GET_INFO)
                .params(params)
                .build()
                .execute(new ListCallback<SendRedPacketInfo>(SendRedPacketInfo.class) {

                    @Override
                    public void onResponse(ArrayResult<SendRedPacketInfo> result) {
                        if (Result.checkSuccess(getApplicationContext(), result)) {
                            ReceiverItemAdapter receiverItemAdapter = new ReceiverItemAdapter();
                            rvReceive.setLayoutManager(new LinearLayoutManager(getApplicationContext()) {
                                @Override
                                public boolean canScrollVertically() {
                                    return false;
                                }
                            });
                            rvReceive.setAdapter(receiverItemAdapter);
                            receiverItemAdapter.setNewInstance(result.getData());
                        }
                    }

                    @Override
                    public void onError(Call call, Exception e) {
                        Toast.makeText(getApplicationContext(), R.string.net_exception, Toast.LENGTH_SHORT).show();
                    }
                });


    }


    private void initView() {
        mMainLayout = findViewById(R.id.draw_layout);

        RadioGroup rgRed = findViewById(R.id.rg_red);

        rgRed.setOnCheckedChangeListener(this);

        rbSendRed = findViewById(R.id.rb_send_red);
        rbReceiveRed = findViewById(R.id.rb_receive_red);
        RelativeLayout rlUser = findViewById(R.id.rl_user);

        ImageView avatarImage = findViewById(R.id.iv_avatar);
        ImageView historyImage = findViewById(R.id.iv_history);
        findViewById(R.id.iv_open).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                mMainLayout.openDrawer(mDrawLayout);
            }
        });

        TextView userNameText = findViewById(R.id.tv_user_name);
        redAmountText = findViewById(R.id.tv_red_amount);
        capitalNameText = findViewById(R.id.tv_capital_name);
        redTypeText = findViewById(R.id.tv_type);
        redListView = findViewById(R.id.rv_list);


        capitalNameText.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                mMainLayout.openDrawer(mDrawLayout);
            }
        });


        userNameText.setText(coreManager.getSelf().getNickName());

        rgRed.setOnCheckedChangeListener(this);

        ButtonColorChange.colorChange(this, rbSendRed);
        ButtonColorChange.changeDrawable(this, rbReceiveRed, R.drawable.red_packet_list_text_bg);
        ButtonColorChange.colorChange(this, rlUser);

        ColorStateList tabColor = SkinUtils.getSkin(this).getButtonColorState();
        historyImage.setImageTintList(tabColor);

        AvatarHelper.getInstance().displayAvatar(coreManager.getSelf().getUserId(), avatarImage);

        initRv();
        sendRedListView();
    }


    private void sendRedListView() {
        sendRedAdapter = new RedPaketItemAdapter();
        redListView.setLayoutManager(new LinearLayoutManager(this) {
            @Override
            public boolean canScrollVertically() {
                return false;
            }
        });
        sendRedAdapter.setOnItemChildClickListener(new OnItemChildClickListener() {
            @Override
            public void onItemChildClick(@NonNull BaseQuickAdapter adapter, @NonNull View view, int position) {
                if (!sendRedAdapter.getData().get(position).extend) {
                    sendRedAdapter.getData().get(position).extend = true;
                } else {
                    sendRedAdapter.getData().get(position).extend = false;
                }
                sendRedAdapter.notifyItemChanged(position);
            }
        });
        redListView.setAdapter(sendRedAdapter);
        sendRedAdapter.setEmptyView(R.layout.red_packet_empty);
    }

    /**
     * 没数据跳不到这里所以无法设置缺省页面
     *
     * @param
     */
    private void receiverRedListView() {
        receiverRedAdapter = new ReceiverRedItemAdapter();
        redListView.setLayoutManager(new LinearLayoutManager(this) {
            @Override
            public boolean canScrollVertically() {
                return false;
            }
        });
        redListView.setAdapter(receiverRedAdapter);
        receiverRedAdapter.setEmptyView(R.layout.red_packet_empty);
    }


    private void initRv() {
        mListView = findViewById(R.id.recycler_view);

        mDrawLayout = findViewById(R.id.dialog_layout);

        findViewById(R.id.iv_cancel)
                .setOnClickListener(v -> mMainLayout.closeDrawer(mDrawLayout));

        mAdapter = new CapitalItemAdapter();
        mListView.setLayoutManager(new LinearLayoutManager(this));
        mListView.setAdapter(mAdapter);

        mAdapter.setOnItemClickListener(new OnItemClickListener() {
            @Override
            public void onItemClick(@NonNull @NotNull BaseQuickAdapter<?, ?> adapter, @NonNull @NotNull View view, int position) {
                mMainLayout.closeDrawer(mDrawLayout);
                initDataLayout(listType, position);
            }
        });
    }

    private void initActionBar() {
        getSupportActionBar().hide();
        findViewById(R.id.tv_title_left).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                finish();
            }
        });
    }

    private void initDataLayout(int listType, int position) {

        RedBacketCount.CapitalList item = mAdapter.getData().get(position);
        if (item == null) {
            return;
        }
        redAmountText.setText(item.capitalCount);
        redTypeText.setText(item.capitalType);
        capitalNameText.setText(item.capitalType);

        for (RedBacketCount.CapitalList data : mAdapter.getData()) {
            data.select = false;
        }
        item = mAdapter.getItem(position);
        item.select = true;
        mAdapter.notifyDataSetChanged();
        try {
            if (listType == 1) {   //发出的红包
                if (sendRedMap.size() <= 0) return;
                sendRedAdapter.setNewInstance(sendRedMap.get(item.capitalType));
            } else {  //收到的红包
                if (receiveRedMap.size() <= 0) return;
                receiverRedAdapter.setNewInstance(receiveRedMap.get(item.capitalType));
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }


    @Override
    public void onCheckedChanged(RadioGroup group, int checkedId) {
        switch (checkedId) {
            case R.id.rb_send_red:
                ButtonColorChange.colorChange(this, rbSendRed);
                ButtonColorChange.changeDrawable(this, rbReceiveRed, R.drawable.red_packet_list_text_bg);
                sendRedListView();
                listType = 1;
                mAdapter.setNewInstance(sendCapitalList);
                initDataLayout(listType, 0);
                break;
            case R.id.rb_receive_red:
                ButtonColorChange.colorChange(this, rbReceiveRed);
                ButtonColorChange.changeDrawable(this, rbSendRed, R.drawable.red_packet_list_text_bg);
                receiverRedListView();
                listType = 2;
                mAdapter.setNewInstance(receiveCapitalList);
                initDataLayout(listType, 0);
                break;
        }
    }


    public class CapitalItemAdapter extends BaseQuickAdapter<RedBacketCount.CapitalList, BaseViewHolder> {

        public CapitalItemAdapter() {
            super(R.layout.item_select_currency_layout);
        }

        @Override
        protected void convert(@NotNull BaseViewHolder baseViewHolder, RedBacketCount.CapitalList item) {
            baseViewHolder.setGone(R.id.item_iv_select, !item.select);
            Glide.with(getContext()).load(item.getPath()).into((ImageView) baseViewHolder.getView(R.id.item_iv_img));
            baseViewHolder.setText(R.id.item_tv_currency, item.capitalType);
        }
    }


    public class RedPaketItemAdapter extends BaseQuickAdapter<RedBacketCount.RedBackerList, BaseViewHolder> {
        public RedPaketItemAdapter() {
            super(R.layout.item_red_packet_count, null);
            addChildClickViewIds(R.id.ll_expan);
        }

        @Override
        protected void convert(@NotNull BaseViewHolder baseViewHolder, RedBacketCount.RedBackerList item) {
            baseViewHolder.setText(R.id.tv_red_packet_type, item.red_envelopes_type);
            baseViewHolder.setText(R.id.red_property_type, item.capital_type);
            baseViewHolder.setText(R.id.number_count, item.capital_count + " " + item.capital_type);
            baseViewHolder.setText(R.id.red_property_send_time, item.create_time);

            if (item.red_envelopes_type.equals("拼手气红包")) {
                baseViewHolder.setVisible(R.id.ll_luck, true);
                baseViewHolder.setVisible(R.id.ll_expan, true);
                baseViewHolder.setGone(R.id.rl_receiver_time, true);

                if (item.extend) {
                    baseViewHolder.setVisible(R.id.rv_receive, true);
                    baseViewHolder.setText(R.id.tv_expan, R.string.weibo_cell_stop);
                    baseViewHolder.setImageResource(R.id.iv_expan, R.mipmap.icon_up);
                    RecyclerView rvReceive = baseViewHolder.getView(R.id.rv_receive);
                    getSendRedPacketInfo(item.id, rvReceive);
                } else {
                    baseViewHolder.setGone(R.id.rv_receive, true);
                    baseViewHolder.setText(R.id.tv_expan, R.string.red_get_the_details);
                    baseViewHolder.setImageResource(R.id.iv_expan, R.mipmap.icon_expan);
                }

                baseViewHolder.setText(R.id.tv_red_get_the_number, item.received_red_envelope_count + " " + getResources().getString(R.string.individual));
                baseViewHolder.setText(R.id.tv_red_get_the_amount, item.received_red_envelope_capital + " " + item.capital_type);
                baseViewHolder.setText(R.id.tv_red_uncollected_number, item.unclaimed_red_envelope_count + " " + getResources().getString(R.string.individual));
                baseViewHolder.setText(R.id.tv_red_unclaimed_amount, item.unclaimed_red_envelope_capital + " " + item.capital_type);
            } else {
                baseViewHolder.setGone(R.id.ll_luck, true);
                baseViewHolder.setGone(R.id.ll_expan, true);
                baseViewHolder.setVisible(R.id.rl_receiver_time, true);
                baseViewHolder.setText(R.id.red_receiver_time, item.end_time);
            }

        }
    }


    public class ReceiverItemAdapter extends BaseQuickAdapter<SendRedPacketInfo, BaseViewHolder> {

        public ReceiverItemAdapter() {
            super(R.layout.item_send_red_packet_receiver);
        }

        @Override
        protected void convert(@NotNull BaseViewHolder baseViewHolder, SendRedPacketInfo item) {
            baseViewHolder.setText(R.id.red_receiver, item.receive_name);
            baseViewHolder.setText(R.id.red_receiver_sum, item.receive_capital + " " + item.currency_name);
            baseViewHolder.setText(R.id.red_receiver_time, item.receive_time);
        }
    }

    public class ReceiverRedItemAdapter extends BaseQuickAdapter<RedBacketCount.RedBackerList, BaseViewHolder> {

        public ReceiverRedItemAdapter() {
            super(R.layout.item_red_packet_receiver, null);
        }

        @Override
        protected void convert(@NotNull BaseViewHolder baseViewHolder, RedBacketCount.RedBackerList item) {
            baseViewHolder.setText(R.id.red_send, item.currency_name);
            baseViewHolder.setText(R.id.red_receiver_sum, item.receive_capital + " " + item.currency_name);
            baseViewHolder.setText(R.id.red_receiver_time, item.receive_time);
        }
    }
}
