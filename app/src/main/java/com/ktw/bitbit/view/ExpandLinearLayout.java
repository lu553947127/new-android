package com.ktw.bitbit.view;

import android.content.Context;
import android.util.AttributeSet;
import android.view.View;
import android.widget.ImageView;
import android.widget.LinearLayout;

import androidx.annotation.Nullable;

/**
 * @ProjectName: new-android
 * @Package: com.ktw.fly.view
 * @ClassName: ExpandLinearLayout
 * @Description: 可扩展的线性布局
 * @Author: XY
 * @CreateDate: 2021/9/18
 * @UpdateUser:
 * @UpdateDate: 2021/9/18
 * @UpdateRemark: 更新说明
 * @Version: 1.0
 */
public class ExpandLinearLayout extends LinearLayout {
    /**
     * 折叠按钮图标
     */
    private ImageView mExpandBtn;

    /**
     * 是否折叠的临界高度
     */
    private int mLimitHeight = 100;

    /**
     * 是否展开了
     */
    private boolean mIsExpand = false;

    /**
     * 是否支持展开折叠功能
     */
    private boolean mSupportExpand = true;

    public ExpandLinearLayout(Context context) {
        super(context);
        init();
    }

    public ExpandLinearLayout(Context context, @Nullable AttributeSet attrs) {
        super(context, attrs);
        init();
    }

    public ExpandLinearLayout(Context context, @Nullable AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
        init();
    }

    /**
     * 初始化
     */
    public void init() {
        setOrientation(VERTICAL);
    }

    /**
     * 重写onMeasure动态控制控件的高度
     *
     * @param widthMeasureSpec
     * @param heightMeasureSpec
     */
    @Override
    protected void onMeasure(int widthMeasureSpec, int heightMeasureSpec) {
        super.onMeasure(widthMeasureSpec, heightMeasureSpec);

        //获取自身的测量高度
        int measureHeight = getMeasuredHeight();

        //设置折叠按钮是否显示
        setExpandBtnVisiable(measureHeight <= mLimitHeight ? View.GONE : View.VISIBLE);

        //如果不支持折叠或者需要展开或者高度不到折叠高度,就保持默认测量
        if (!mSupportExpand || mIsExpand || measureHeight <= mLimitHeight) {
            return;
        }

        //重新设置高度测量参数
        heightMeasureSpec = MeasureSpec.makeMeasureSpec(mLimitHeight, MeasureSpec.EXACTLY);

        //重新测量
        super.onMeasure(widthMeasureSpec, heightMeasureSpec);
    }

    /**
     * 绑定展开折叠按钮图标
     *
     * @param view
     * @param expandRes
     * @param foldRes
     */
    public void bindExpandButton(ImageView view, final int expandRes, final int foldRes) {
        mExpandBtn = view;
        if (view == null) {
            return;
        }
        view.setOnClickListener(new OnClickListener() {
            @Override
            public void onClick(View v) {
                setIsExpand(!mIsExpand);
                mExpandBtn.setImageResource(mIsExpand ? expandRes : foldRes);
            }
        });
        setExpandBtnVisiable(GONE);
    }

    /**
     * 获取临界折叠高度
     *
     * @return
     */
    public int getLimitHeight() {
        return mLimitHeight;
    }

    /**
     * 设置临界折叠高度
     *
     * @param limitHeight
     */
    public void setLimitHeight(int limitHeight) {
        this.mLimitHeight = limitHeight;
    }

    /**
     * 获取是否展开了
     *
     * @return
     */
    public boolean isIsExpand() {
        return mIsExpand;
    }

    /**
     * 设置是否展开
     *
     * @param isExpand
     */
    public void setIsExpand(boolean isExpand) {
        this.mIsExpand = isExpand;
        requestLayout();
    }

    /**
     * 获取当前是否支持折叠展开功能
     *
     * @return
     */
    public boolean isSupportExpand() {
        return mSupportExpand;
    }

    /**
     * 设置折叠还是展开
     *
     * @param supportExpand
     */
    public void setSupportExpand(boolean supportExpand) {
        this.mSupportExpand = supportExpand;
        setExpandBtnVisiable(GONE);
    }

    /**
     * 设置折叠图标是否显示
     *
     * @param visiable
     */
    private void setExpandBtnVisiable(int visiable) {
        if (mExpandBtn != null) {
            if (!mSupportExpand) {
                mExpandBtn.setVisibility(GONE);
            } else {
                mExpandBtn.setVisibility(visiable);
            }
        }
    }

}
