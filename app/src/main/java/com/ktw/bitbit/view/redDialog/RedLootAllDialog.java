package com.ktw.bitbit.view.redDialog;

import android.app.Dialog;
import android.content.Context;
import android.os.Bundle;
import android.view.Gravity;
import android.view.Window;
import android.view.WindowManager;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.RelativeLayout;
import android.widget.TextView;

import com.ktw.bitbit.FLYApplication;
import com.ktw.bitbit.R;
import com.ktw.bitbit.bean.redpacket.RedDialogBean;
import com.ktw.bitbit.helper.AvatarHelper;

/**
 * 红包抢光的Dialog
 */
public class RedLootAllDialog extends Dialog {

    private RelativeLayout mRedRl;
    private ImageView mAvatarIv, mCloseIv;
    private TextView mNameTv, mContentTv;
    private RedDialogBean mRedDialogBean;

    private Context mContext;


    private OnClickRedListener mOnClickRedListener;
    private LinearLayout llGet;

    public RedLootAllDialog(Context context, RedDialogBean redDialogBean, OnClickRedListener onClickRedListener) {
        super(context, R.style.MyDialog);
        this.mContext = context;
        this.mRedDialogBean = redDialogBean;
        this.mOnClickRedListener = onClickRedListener;
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.dialog_loot_all_red_packet);
        initView();
        initData();
        initEvent();

        Window window = getWindow();
        assert window != null;
        WindowManager.LayoutParams lp = window.getAttributes();
        window.setAttributes(lp);
        window.setGravity(Gravity.CENTER);
    }

    private void initView() {
        mRedRl = findViewById(R.id.rl_red);
        mAvatarIv = findViewById(R.id.iv_avatar);
        mNameTv = findViewById(R.id.tv_name);
        mContentTv = findViewById(R.id.tv_msg);
        mCloseIv = findViewById(R.id.iv_close);
        mCloseIv = findViewById(R.id.iv_close);
        llGet = findViewById(R.id.ll_get);
    }

    private void initData() {
        AvatarHelper.getInstance().displayAvatar(mRedDialogBean.getUserName(), mRedDialogBean.getUserId(),
                mAvatarIv, true);
        mNameTv.setText(FLYApplication.getContext().getString(R.string.red_someone, mRedDialogBean.getUserName()));
        mContentTv.setText(mRedDialogBean.getWords());

//        Animation animation = AnimationUtils.loadAnimation(mContext, R.anim.anim_red);
//        mRedRl.setAnimation(animation);
    }

    private void initEvent() {
        llGet.setOnClickListener(v -> {
            if (mOnClickRedListener != null) {
                mOnClickRedListener.clickRed();
            }
        });

        mCloseIv.setOnClickListener(v -> {

            dismiss();
        });
    }





    public interface OnClickRedListener {
        void clickRed();
    }
}
