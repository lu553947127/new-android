package com.ktw.fly.view.shape;

import android.content.Context;
import android.util.AttributeSet;
import android.view.MotionEvent;
import android.widget.LinearLayout;

/**
 * 用于需要圆角矩形框背景的LinearLayout的情况,减少直接使用LinearLayout时引入的shape资源文件
 */
public class RoundLinearLayout extends LinearLayout {
    private RoundViewDelegate delegate;
    private boolean intercept = false;

    public RoundLinearLayout(Context context) {
        this(context, null);
    }

    public RoundLinearLayout(Context context, AttributeSet attrs) {
        super(context, attrs);
        delegate = new RoundViewDelegate(this, context, attrs);
    }

    /**
     * use delegate to set attr
     */
    public RoundViewDelegate getDelegate() {
        return delegate;
    }

    @Override
    protected void onMeasure(int widthMeasureSpec, int heightMeasureSpec) {
        if (delegate.isWidthHeightEqual() && getWidth() > 0 && getHeight() > 0) {
            int max = Math.max(getWidth(), getHeight());
            int measureSpec = MeasureSpec.makeMeasureSpec(max, MeasureSpec.EXACTLY);
            super.onMeasure(measureSpec, measureSpec);
            return;
        }

        super.onMeasure(widthMeasureSpec, heightMeasureSpec);
    }

    @Override
    protected void onLayout(boolean changed, int left, int top, int right, int bottom) {
        super.onLayout(changed, left, top, right, bottom);
        if (delegate.isRadiusHalfHeight()) {
            delegate.setCornerRadius(getHeight() / 2);
        } else {
            delegate.setBgSelector();
        }
    }

    @Override
    public boolean onInterceptTouchEvent(MotionEvent ev) {
        if (intercept) {
            return intercept;
        }
        return super.onInterceptTouchEvent(ev);
    }

    public void setIntercept(boolean intercept) {
        this.intercept = intercept;
    }
}
