package com.ktw.bitbit.util;

import android.content.Context;
import android.os.Handler;
import android.os.Message;
import android.view.WindowManager;
import android.widget.Toast;

import com.ktw.bitbit.R;

import java.lang.reflect.Field;

/**
 *
 */
public class ToastUtil {

    public static void showErrorNet(Context context) {
        showToast(context, R.string.net_exception);
    }

    public static void showToast(Context context, int res) {
        if (context == null) {
            return;
        }
        realShowToast(context, context.getString(res), Toast.LENGTH_SHORT);
    }

    public static void showErrorData(Context context) {
        if (context == null) {
            return;
        }
        realShowToast(context, context.getString(R.string.data_exception), Toast.LENGTH_SHORT);
    }


    public static void showNetError(Context context) {
        if (context == null) {
            return;
        }
        realShowToast(context, context.getString(R.string.net_exception), Toast.LENGTH_SHORT);
    }

    public static void showToast(Context context, String message) {
        if (context == null) {
            return;
        }
        realShowToast(context, message, Toast.LENGTH_SHORT);
    }

    public static void showLongToast(Context context, String message) {
        if (context == null) {
            return;
        }
        realShowToast(context, message, Toast.LENGTH_LONG);
    }

    public static void showLongToast(Context context, int res) {
        if (context == null) {
            return;
        }
        realShowToast(context, context.getString(res), Toast.LENGTH_LONG);
    }

    public static void showUnkownError(Context ctx, Throwable t) {
        String message = "null";
        if (t != null) {
            message = t.getMessage();
        }
        showToast(ctx, ctx.getString(R.string.tip_unkown_error_place_holder, message));
    }

    private static void realShowToast(Context context, int res, int duration) {
        realShowToast(context, context.getString(res), duration);
    }

    private static void realShowToast(Context context, CharSequence text, int duration) {
        // ??????7???toast????????????ui????????????????????????hook??????try,
        hookToast(Toast.makeText(context, text, duration)).show();
    }

    @SuppressWarnings("JavaReflectionMemberAccess")
    private static Toast hookToast(Toast toast) {
        Class<Toast> cToast = Toast.class;
        try {
            //TN???private???
            Field fTn = cToast.getDeclaredField("mTN");
            fTn.setAccessible(true);

            //??????tn??????
            Object oTn = fTn.get(toast);
            //??????TN???class????????????????????????Field.getType()?????????
            Class<?> cTn = oTn.getClass();
            Field fHandle = cTn.getDeclaredField("mHandler");

            //??????set->mHandler
            fHandle.setAccessible(true);
            fHandle.set(oTn, new HandlerProxy((Handler) fHandle.get(oTn)));
        } catch (NoSuchFieldException | IllegalAccessException ignored) {
        }
        return toast;
    }

    private static class HandlerProxy extends Handler {

        private Handler mHandler;

        HandlerProxy(Handler handler) {
            this.mHandler = handler;
        }

        @Override
        public void handleMessage(Message msg) {
            try {
                mHandler.handleMessage(msg);
            } catch (WindowManager.BadTokenException ignored) {
                // ??????8?????????handleShow????????????????????????try catch?????????????????????????????????try catch???????????????
            }
        }
    }
}
