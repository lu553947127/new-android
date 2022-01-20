package com.ktw.bitbit.downloader;

import android.view.View;

import java.lang.ref.Reference;
import java.lang.ref.WeakReference;

/**
 * Wrapper for Android {@link View View}. Keeps weak reference of
 * View to prevent memory leaks.
 */
public class ViewAware {
    protected Reference<View> viewRef;
    private boolean emptyView;

    public ViewAware(View view) {
        if (view == null) {
            emptyView = true;
        } else {
            this.viewRef = new WeakReference<View>(view);
        }
    }

    public View getWrappedView() {
        if (emptyView) {
            return null;
        }
        return viewRef.get();
    }

    public boolean isCollected() {
        if (emptyView) {
            return false;
        }
        return viewRef.get() == null;
    }

    public int getId() {
        if (emptyView) {
            return super.hashCode();
        }
        View view = viewRef.get();
        return view == null ? super.hashCode() : view.hashCode();
    }

}
