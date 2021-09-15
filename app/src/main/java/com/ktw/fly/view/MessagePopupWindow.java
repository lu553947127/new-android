package com.ktw.fly.view;

import android.content.Context;
import android.content.res.ColorStateList;
import android.graphics.Color;
import android.graphics.drawable.ColorDrawable;
import android.view.LayoutInflater;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.ViewGroup;
import android.view.WindowManager;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.LinearLayout.LayoutParams;
import android.widget.PopupWindow;
import android.widget.TextView;

import androidx.core.widget.ImageViewCompat;
import androidx.fragment.app.FragmentActivity;

import com.ktw.fly.R;
import com.ktw.fly.ui.base.CoreManager;

public class MessagePopupWindow extends PopupWindow {

    public MessagePopupWindow(
            final FragmentActivity context,
            OnClickListener itemsOnClick,
            CoreManager coreManager
    ) {
        super(context);
        boolean cannotCreateGroup = coreManager.getLimit().cannotCreateGroup();
        boolean cannotSearchFriend = coreManager.getLimit().cannotSearchFriend();
        boolean disableLocationServer = coreManager.getConfig().disableLocationServer;
        boolean enableMpModule = coreManager.getConfig().enableMpModule;
        boolean enablePayModule = coreManager.getConfig().enablePayModule;
        LayoutInflater inflater = (LayoutInflater) context
                .getSystemService(Context.LAYOUT_INFLATER_SERVICE);
        ViewGroup menuView = (ViewGroup) inflater.inflate(R.layout.popu_message, null);

        // 白色背景，黑色文字图标，
        ColorStateList foreground = ColorStateList.valueOf(Color.DKGRAY);
        for (int i = 0; i < menuView.getChildCount(); i++) {
            View child = menuView.getChildAt(i);
            if (child instanceof LinearLayout) {
                LinearLayout layout = (LinearLayout) child;
                // 不允许建群时隐藏发起群聊按钮，
                if (cannotCreateGroup &&
                        (layout.getId() == R.id.create_group
                                || layout.getId() == R.id.face_group)
                ) {
                    layout.setVisibility(View.GONE);
                    continue;
                }
                // 不允许搜索时隐藏添加好友按钮，
                if (cannotSearchFriend && layout.getId() == R.id.add_friends) {
                    layout.setVisibility(View.GONE);
                    continue;
                }
                // 关闭定位功能，隐藏附近的人
//                if (disableLocationServer && layout.getId() == R.id.near_person) {
                if ( layout.getId() == R.id.near_person) {
                    layout.setVisibility(View.GONE);
                    continue;
                }

                // 关闭公众号功能，隐藏搜索公众号，
//                if (!enableMpModule && layout.getId() == R.id.search_public_number) {
                if ( layout.getId() == R.id.search_public_number) {
                    layout.setVisibility(View.GONE);
                    continue;
                }

                // 关闭支付功能，隐藏收付款，
                if (!enablePayModule && layout.getId() == R.id.receipt_payment) {
                    layout.setVisibility(View.GONE);
                    continue;
                }
                layout.setOnClickListener(itemsOnClick);
                for (int j = 0; j < layout.getChildCount(); j++) {
                    View lChild = layout.getChildAt(j);
                    if (lChild instanceof ImageView) {
                        ImageViewCompat.setImageTintList((ImageView) lChild, foreground);
                    } else if (lChild instanceof TextView) {
                        ((TextView) lChild).setTextColor(foreground);
                    }
                }
            }
        }
        //设置SelectPicPopupWindow的View
        this.setContentView(menuView);
        //设置SelectPicPopupWindow弹出窗体的宽
        this.setWidth(LayoutParams.WRAP_CONTENT);
        // this.setWidth(ViewPiexlUtil.dp2px(context,200));
        //设置SelectPicPopupWindow弹出窗体的高
        this.setHeight(LayoutParams.WRAP_CONTENT);
        //设置SelectPicPopupWindow弹出窗体可点击
        this.setFocusable(true);

        this.setOutsideTouchable(true);

        //设置SelectPicPopupWindow弹出窗体动画效果
        this.setAnimationStyle(R.style.Buttom_Popwindow);

        //设置SelectPicPopupWindow弹出窗体的背景
        // 透明背景，
        this.setBackgroundDrawable(new ColorDrawable(0));

        WindowManager.LayoutParams lp = context.getWindow().getAttributes();
        lp.alpha = 0.7f;
        context.getWindow().setAttributes(lp);

        setOnDismissListener(new OnDismissListener() {
            @Override
            public void onDismiss() {
                WindowManager.LayoutParams lp = context.getWindow().getAttributes();
                lp.alpha = 1f;
                context.getWindow().setAttributes(lp);
            }
        });
    }
}
