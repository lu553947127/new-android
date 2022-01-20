package com.ktw.bitbit.view;

import android.app.Dialog;
import android.content.Context;
import android.os.Bundle;
import android.view.Gravity;
import android.view.View;
import android.view.Window;
import android.view.WindowManager;
import android.widget.LinearLayout;

import androidx.annotation.NonNull;

import com.ktw.bitbit.R;
import com.ktw.bitbit.util.ScreenUtil;

public class GroupVideoChatToolDialog extends Dialog implements View.OnClickListener {
    private OnVideoChatToolDialogClickListener clickListener;
    private Context VContext;
    private LinearLayout tv_video, tv_voice, tv_talk, tv_cancle;

    public GroupVideoChatToolDialog(@NonNull Context context, OnVideoChatToolDialogClickListener onVideoChatToolDialogClickListener) {
        super(context, R.style.BottomDialog);
        this.VContext = context;
        this.clickListener = onVideoChatToolDialogClickListener;
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.dialog_chat_tool);
        setCanceledOnTouchOutside(true);
        initView();
    }

    private void initView() {
        tv_video = (LinearLayout) findViewById(R.id.tv1);
        tv_voice = (LinearLayout) findViewById(R.id.tv2);
        tv_talk = (LinearLayout) findViewById(R.id.tv3);
        tv_cancle = (LinearLayout) findViewById(R.id.tv4);

        tv_video.setOnClickListener(this);
        tv_voice.setOnClickListener(this);
        tv_talk.setOnClickListener(this);
        tv_cancle.setOnClickListener(this);


        Window o = getWindow();
        WindowManager.LayoutParams lp = o.getAttributes();
        // x/y坐标
        // lp.x = 100;
        // lp.y = 100;
        lp.width = ScreenUtil.getScreenWidth(getContext());
        o.setAttributes(lp);
        this.getWindow().setGravity(Gravity.BOTTOM);
        this.getWindow().setWindowAnimations(R.style.BottomDialog_Animation);
    }

    @Override
    public void onClick(View v) {
        dismiss();
        switch (v.getId()) {
            case R.id.tv1:
                clickListener.videoClick();
                break;
            case R.id.tv2:
                clickListener.voiceClick();
                break;
            case R.id.tv3:
                clickListener.talkClick();
                break;
            case R.id.tv4:
                clickListener.cancleClick();
                break;

        }
    }

    public interface OnVideoChatToolDialogClickListener {
        void videoClick();

        void voiceClick();

        void talkClick();

        void cancleClick();

    }
}
