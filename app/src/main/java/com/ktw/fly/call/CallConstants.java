package com.ktw.fly.call;

import com.ktw.fly.FLYAppConfig;

/**
 * Created by Administrator on 2018/1/22 0022.
 */

public class CallConstants {
    public static final String AUDIO_OR_VIDEO_OR_MEET = "Audio_Or_Video_Or_Meet";
    // 刷新、关闭悬浮窗
    public static final String REFRESH_FLOATING = FLYAppConfig.sPackageName + "Refresh_Floating";
    public static final String CLOSE_FLOATING = FLYAppConfig.sPackageName + "Close_Floating";
    // 通话、会议
    public static final int Audio = 1;
    public static final int Video = 2;
    public static final int Audio_Meet = 3;
    public static final int Video_Meet = 4;
    public static final int Talk_Meet = 6;
}
