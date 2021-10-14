package com.ktw.fly.ui.me.redpacket;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.view.WindowManager;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.chad.library.adapter.base.BaseQuickAdapter;
import com.chad.library.adapter.base.listener.OnItemClickListener;
import com.j256.ormlite.stmt.query.In;
import com.ktw.fly.FLYAppConstant;
import com.ktw.fly.R;
import com.ktw.fly.bean.Capital;
import com.ktw.fly.bean.message.ChatMessage;
import com.ktw.fly.bean.message.XmppMessage;
import com.ktw.fly.bean.redpacket.RedPacket;
import com.ktw.fly.helper.DialogHelper;
import com.ktw.fly.ui.base.BaseActivity;
import com.ktw.fly.ui.me.redpacket.adapter.CapitalAdapter;
import com.ktw.fly.ui.message.ChatActivity;
import com.ktw.fly.util.ToastUtil;
import com.ktw.fly.wallet.bean.CurrencyBean;
import com.xuan.xuanhttplibrary.okhttp.HttpUtils;
import com.xuan.xuanhttplibrary.okhttp.callback.BaseCallback;
import com.xuan.xuanhttplibrary.okhttp.callback.ListCallback;
import com.xuan.xuanhttplibrary.okhttp.result.ArrayResult;
import com.xuan.xuanhttplibrary.okhttp.result.ObjectResult;
import com.xuan.xuanhttplibrary.okhttp.result.Result;

import java.util.List;

import okhttp3.Call;

import static com.ktw.fly.ui.me.redpacket.SendRedPacketActivity.REQUEST_CODE_CAPITAL;

/**
 * @ProjectName: new-android
 * @Package: com.ktw.fly.ui.me.redpacket
 * @ClassName: PropertyListActivity
 * @Description: 发红包 选择资产类型
 * @Author: XY
 * @CreateDate: 2021/9/22
 * @UpdateUser:
 * @UpdateDate: 2021/9/22
 * @UpdateRemark: 更新说明
 * @Version: 1.0
 */
public class PropertyListActivity extends BaseActivity {




    private RecyclerView rvList;
    private CapitalAdapter capitalAdapter;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_property_list);

        initActionBar();
        initView();
        initData();
    }

    private void initView() {
        rvList = findViewById(R.id.rv_list);
        capitalAdapter = new CapitalAdapter(R.layout.item_capital_layout, null);
        rvList.setLayoutManager(new LinearLayoutManager(this));
        rvList.setAdapter(capitalAdapter);

        capitalAdapter.setOnItemClickListener(new OnItemClickListener() {
            @Override
            public void onItemClick(@NonNull BaseQuickAdapter<?, ?> adapter, @NonNull View view, int position) {
                Capital data = capitalAdapter.getData().get(position);
                data.isChoose = !data.isChoose;
                capitalAdapter.notifyDataSetChanged();
                Intent intent = new Intent();
                intent.putExtra("capital", data);
                setResult(REQUEST_CODE_CAPITAL, intent);
                PropertyListActivity.this.finish();
            }
        });
    }

    private void initData() {
        getCapital();
    }

    /**
     * 获取资金类型
     */
    private void getCapital() {
        HttpUtils.get().url(coreManager.getConfig().GET_CAPITAL_TYPE)
                .build()
                .execute(new ListCallback<Capital>(Capital.class) {
                    @Override
                    public void onResponse(ArrayResult<Capital> result) {
                        DialogHelper.dismissProgressDialog();
                        if (Result.checkSuccess(mContext, result)) {
                            if (result == null) {
                                return;
                            }
                            List<Capital> list = result.getData();
                            if (list == null) {
                                return;
                            }
                            capitalAdapter.setNewInstance(list);
                        }
                    }

                    @Override
                    public void onError(Call call, Exception e) {
                        DialogHelper.dismissProgressDialog();
                        Toast.makeText(getApplicationContext(), getString(R.string.error_network), Toast.LENGTH_SHORT).show();
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

}
