package com.ktw.fly.ui;

import android.content.Context;
import android.content.Intent;
import android.os.Build;
import android.os.Bundle;
import android.text.TextUtils;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.TextView;

import com.ktw.fly.FLYApplication;
import com.ktw.fly.R;
import com.ktw.fly.helper.LoginHelper;
import com.ktw.fly.sp.UserSp;
import com.ktw.fly.ui.account.LoginActivity;
import com.ktw.fly.ui.account.LoginHistoryActivity;
import com.ktw.fly.ui.backup.SendChatHistoryActivity;
import com.ktw.fly.ui.base.ActionBackActivity;
import com.ktw.fly.ui.base.ActivityStack;
import com.ktw.fly.ui.lock.DeviceLockHelper;
import com.ktw.fly.util.Constants;
import com.ktw.fly.util.PreferenceUtils;

/**
 * 进入到此界面的Activity只可能是4中用户状态 STATUS_USER_TOKEN_OVERDUE
 * 本地Token过期 STATUS_USER_NO_UPDATE
 * 数据不完整
 */
// TODO: 统一继承BaseActivity, 要注意这里不需要登录，
public class FLYUserCheckedActivity extends ActionBackActivity {
    private static final String TAG = "UserCheckedActivity";
    private TextView mTitleTv;
    private TextView mDesTv;
    private Button mLeftBtn;
    private Button mRightBtn;
    // 有什么工作正在进行不能被重新登录覆盖掉了，
    private boolean working;

    public static void start(Context ctx) {
        Log.d(TAG, "start() called with: ctx = [" + ctx + "]");
        Log.w(TAG, "start: 需要重新登录，", new Exception("需要重新登录，"));
        DeviceLockHelper.clearPassword();
        UserSp.getInstance(ctx).clearUserInfo();
        Intent intent = new Intent(ctx, FLYUserCheckedActivity.class);
        // 清空activity栈，
        intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
        ctx.startActivity(intent);
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_user_checked);
        // Api 11之后，点击外部会使得Activity结束，禁止外部点击结束
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.HONEYCOMB) {
            setFinishOnTouchOutside(false);
        }
        initView();

        working = SendChatHistoryActivity.flag;
        if (working) {
            SendChatHistoryActivity.moveToFront(this);
        }
    }

    private void initView() {
        mTitleTv = (TextView) findViewById(R.id.title_tv);
        mDesTv = (TextView) findViewById(R.id.des_tv);
        mLeftBtn = (Button) findViewById(R.id.left_btn);
        mRightBtn = (Button) findViewById(R.id.right_btn);
        // init status

        // 能进入此Activity的只允许三种用户状态
        int status = FLYApplication.getInstance().mUserStatus;
        if (status == LoginHelper.STATUS_USER_TOKEN_OVERDUE) {
            mTitleTv.setText(R.string.overdue_title);
            mDesTv.setText(R.string.token_overdue_des);
        } else if (status == LoginHelper.STATUS_USER_NO_UPDATE) {
            mTitleTv.setText(R.string.overdue_title);
            mDesTv.setText(R.string.deficiency_data_des);
        } else if (status == LoginHelper.STATUS_USER_TOKEN_CHANGE) {
            mTitleTv.setText(R.string.logout_title);
            mDesTv.setText(R.string.logout_des);
        } else {// 其他的状态，一般不会出现，为了容错，加个判断
            loginAgain();
            return;
        }

        mLeftBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                PreferenceUtils.putBoolean(FLYUserCheckedActivity.this, Constants.LOGIN_CONFLICT, true);
                ActivityStack.getInstance().exit();
            }
        });

        mRightBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                loginAgain();
            }
        });
    }

    private void loginAgain() {
        boolean idIsEmpty = TextUtils.isEmpty(UserSp.getInstance(this).getUserId(""));
        if (!idIsEmpty) {
            startActivity(new Intent(this, LoginHistoryActivity.class));
        } else {
            startActivity(new Intent(this, LoginActivity.class));
        }
        ActivityStack.getInstance().exit();
        finish();
    }

    @Override
    protected void onRestart() {
        super.onRestart();
        if (working) {
            loginAgain();
        }
    }

    @Override
    public void onBackPressed() {
        loginAgain();
    }
}
