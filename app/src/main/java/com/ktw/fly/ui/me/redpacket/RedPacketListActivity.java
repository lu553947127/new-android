package com.ktw.fly.ui.me.redpacket;

import android.os.Bundle;
import android.view.View;
import android.view.WindowManager;

import com.ktw.fly.R;
import com.ktw.fly.ui.base.BaseActivity;

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
public class RedPacketListActivity extends BaseActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_red_packet_list);

        initActionBar();
    }

    private void initActionBar() {
        getSupportActionBar().hide();
        getWindow().setSoftInputMode(WindowManager.LayoutParams.SOFT_INPUT_STATE_VISIBLE);
        findViewById(R.id.tv_title_left).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                finish();
            }
        });
    }

}
