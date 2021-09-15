package com.ktw.fly.ui.groupchat;

import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.text.TextUtils;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;

import com.ktw.fly.R;
import com.ktw.fly.ui.base.BaseActivity;
import com.ktw.fly.ui.nearby.PublicNumberSearchActivity;
import com.ktw.fly.ui.tool.ButtonColorChange;

public class RoomSearchActivity extends BaseActivity {
    private EditText mKeyWordEdit;
    private Button mSearchBtn;

    public static void start(Context ctx) {
        Intent intent = new Intent(ctx, PublicNumberSearchActivity.class);
        ctx.startActivity(intent);
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_room_search);
        getSupportActionBar().hide();
        findViewById(R.id.iv_title_left).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                finish();
            }
        });
        TextView tvTitle = (TextView) findViewById(R.id.tv_title_center);
        tvTitle.setText(R.string.search_group);
        initView();
    }

    private void initView() {
        mKeyWordEdit = (EditText) findViewById(R.id.keyword_edit);
        // 获取焦点，键盘弹出
        mKeyWordEdit.requestFocus();

        mSearchBtn = (Button) findViewById(R.id.search_btn);
        ButtonColorChange.colorChange(this, mSearchBtn);

        mSearchBtn.setOnClickListener(v -> {
            if (!TextUtils.isEmpty(mKeyWordEdit.getText().toString())) {
                RoomSearchResultActivity.start(mContext, mKeyWordEdit.getText().toString());
            }
        });
    }
}
