package com.ktw.fly.util;

import android.content.Context;
import android.text.TextUtils;
import android.util.Log;
import android.widget.TextView;

import com.ktw.fly.FLYApplication;
import com.ktw.fly.R;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Date;

import static com.ktw.fly.util.Constants.KEY_TIME_DIFFERENCE;

@SuppressWarnings("deprecation")
public class TimeUtils {

    // ///s 代表Simple日期格式：yyyy-MM-dd
    // ///f 代表Full日期格式：yyyy-MM-dd hh:mm:ss

    public static final SimpleDateFormat ss_format = new SimpleDateFormat("MM-dd");
    public static final SimpleDateFormat s_format = new SimpleDateFormat("yyyy-MM-dd");
    public static final SimpleDateFormat f_format = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
    public static final SimpleDateFormat sdfNearby = new SimpleDateFormat("MM-dd HH:mm");
    public static final SimpleDateFormat sk_format_1 = new SimpleDateFormat("yyyy-MM-dd HH:mm");
    public static SimpleDateFormat friendly_format1 = new SimpleDateFormat("HH:mm");
    public static SimpleDateFormat friendly_format2 = new SimpleDateFormat("MM-dd HH:mm");
    private static SimpleDateFormat hm_formater = new SimpleDateFormat("HH:mm");

    public static long s_str_2_long(String dateString) {
        try {
            Date d = s_format.parse(dateString);
            return d.getTime();
        } catch (ParseException e) {
            e.printStackTrace();
        }
        return 0;
    }

    public static long f_str_2_long(String dateString) {
        try {
            Date d = f_format.parse(dateString);
            return d.getTime();
        } catch (ParseException e) {
            e.printStackTrace();
        }
        return 0;
    }

    public static String ss_long_2_str(long timestamp) {
        return ss_format.format(new Date(timestamp));
    }

    public static String s_long_2_str(long timestamp) {
        return s_format.format(new Date(timestamp));
    }

    public static String f_long_2_str(long timestamp) {
        return f_format.format(new Date(timestamp));
    }

    /**
     * 获取字符串时间的年份
     *
     * @param dateString 格式为yyyy-MM-ss，或者yyyy-MM-dd HH:mm:ss
     * @return
     */
    public static int getYear(String dateString) {
        try {
            Date d = s_format.parse(dateString);
            return d.getYear() + 1900;// 年份是基于格林威治时间，所以加上1900
        } catch (ParseException e) {
            e.printStackTrace();
        }
        return 0;
    }

    /**
     * 获取字符串时间的月份
     *
     * @param dateString 格式为yyyy-MM-ss，或者yyyy-MM-dd hh:mm:ss
     * @return
     */
    public static int getMonth(String dateString) {
        try {
            Date d = s_format.parse(dateString);
            return d.getMonth();// 月份从0-11
        } catch (ParseException e) {
            // TODO Auto-generated catch block
            e.printStackTrace();
        }
        return 0;
    }

    // /////////////////////以上是通用的，下面为特殊需求的////////////////////////
    // /**
    // * 时间戳转换日期格式
    // *
    // * @param timestamp
    // * 单位秒
    // * @return
    // */
    // public static String getCurrentTime(long timestamp) {
    // SimpleDateFormat f = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
    // return f.format(new Date(timestamp * 1000));
    // }

    /**
     * 获取字符串时间的天
     *
     * @param dateString 格式为yyyy-MM-ss，或者yyyy-MM-dd hh:mm:ss
     * @return
     */
    public static int getDayOfMonth(String dateString) {
        try {
            Date d = s_format.parse(dateString);
            return d.getDate();
        } catch (ParseException e) {
            // TODO Auto-generated catch block
            e.printStackTrace();
        }
        return 0;
    }

    public static int getHours(String timeString) {
        SimpleDateFormat formart = new SimpleDateFormat("HH:mm:ss");
        try {
            Date date = formart.parse(timeString);
            return date.getHours();
        } catch (ParseException e) {
            // TODO Auto-generated catch block
            e.printStackTrace();
        }
        return 0;
    }

    public static int getMinutes(String timeString) {
        SimpleDateFormat formart = new SimpleDateFormat("HH:mm:ss");
        try {
            Date date = formart.parse(timeString);
            return date.getMinutes();
        } catch (ParseException e) {
            // TODO Auto-generated catch block
            e.printStackTrace();
        }
        return 0;
    }

    public static int getSeconds(String timeString) {
        SimpleDateFormat formart = new SimpleDateFormat("HH:mm:ss");
        try {
            Date date = formart.parse(timeString);
            return date.getSeconds();
        } catch (ParseException e) {
            // TODO Auto-generated catch block
            e.printStackTrace();
        }
        return 0;
    }

    public static String getCurrentTime() {
        return f_format.format(new Date(System.currentTimeMillis()));
    }

    /**
     * 在当前时间上加上多少毫秒，返回这个时间
     *
     * @param mask
     * @return
     */
    public static String getCurrentTimeMask(long mask) {
        return f_format.format(new Date(System.currentTimeMillis() + mask));
    }

    /**
     * 获取精简的日期
     *
     * @param time
     * @return
     */
    public static String getSimpleDate(String time) {
        SimpleDateFormat formater = new SimpleDateFormat("yyyy-MM-dd");
        Date date = null;
        try {
            date = f_format.parse(time);
            return formater.format(date);
        } catch (ParseException e) {
            e.printStackTrace();
            return "";
        }
    }

    /**
     * @param time
     * @return
     */
    public static String getSimpleDateTime(String time) {
        SimpleDateFormat formater = new SimpleDateFormat("yy-MM-dd HH:mm");
        Date date = null;
        try {
            date = f_format.parse(time);
            return formater.format(date);
        } catch (ParseException e) {
            e.printStackTrace();
            return "";
        }
    }

    public static String getSimpleTime(String time) {
        SimpleDateFormat formater = new SimpleDateFormat("HH:mm");
        Date date = null;
        try {
            date = f_format.parse(time);
            return formater.format(date);
        } catch (ParseException e) {
            e.printStackTrace();
            return "";
        }
    }

    public static String getChatSimpleDate(String time) {
        SimpleDateFormat formater = new SimpleDateFormat("yy-MM-dd");
        Date date = null;
        try {
            date = f_format.parse(time);
            return formater.format(date);
        } catch (ParseException e) {
            e.printStackTrace();
            return "";
        }
    }

    public static String getTimeHM(String time) {
        SimpleDateFormat formater = new SimpleDateFormat("HH:mm");
        Date date = null;
        try {
            date = f_format.parse(time);
            return formater.format(date);
        } catch (ParseException e) {
            e.printStackTrace();
            return "";
        }
    }

    public static String getTimeMMdd(long time) {
        SimpleDateFormat formater = new SimpleDateFormat("MM-dd");
        return formater.format(time);

    }

    /**
     * 获取好友的时间显示
     *
     * @param time 秒级别的时间戳
     * @return
     */
    public static String getFriendlyTimeDesc(Context context, long time) {
        if (time > 1000000000000L) {
            time = time / 1000;// 现在传入的时间有可能为毫秒，需要转为秒
        }

        String desc = "";
        if (time == 0) {
            return desc;
        }
        Date timeDate = new Date(time * 1000L);
        Date nowDate = new Date();
        long delaySeconds = nowDate.getTime() / 1000 - time;// 相差的秒数

        if (delaySeconds < 10) {// 小于10秒，显示刚刚
            //desc = context.getString(R.string.friendly_time_just_now);// 显示刚刚
            desc = FLYApplication.getInstance().getString(R.string.friendly_time_just_now);
        } else if (delaySeconds <= 60) {// 小于1分钟，显示如“25秒前”
            desc = delaySeconds + FLYApplication.getInstance().getString(R.string.friendly_time_before_seconds);
        } else if (delaySeconds < 60 * 30) {// 小于30分钟，显示如“25分钟前”
            desc = (delaySeconds / 60) + FLYApplication.getInstance().getString(R.string.friendly_time_before_minute);
        } else if (delaySeconds < 60 * 60 * 24) {// 小于1天之内
            if (nowDate.getDay() - timeDate.getDay() == 0) {// 同一天
                desc = friendly_format1.format(timeDate);
            } else {// 前一天
                desc = FLYApplication.getInstance().getString(R.string.friendly_time_yesterday) + " " + friendly_format1.format(timeDate);
            }
        } else if (delaySeconds < 60 * 60 * 24 * 2) {// 小于2天之内
            if (nowDate.getDay() - timeDate.getDay() == 1 || nowDate.getDay() - timeDate.getDay() == -6) {// 昨天
                desc = FLYApplication.getInstance().getString(R.string.friendly_time_yesterday) + " " + friendly_format1.format(timeDate);
            } else {// 前天
                desc = FLYApplication.getInstance().getString(R.string.friendly_time_before_yesterday) + " " + friendly_format1.format(timeDate);
            }
        } else if (delaySeconds < 60 * 60 * 24 * 3) {// 小于三天
            if (nowDate.getDay() - timeDate.getDay() == 2 || nowDate.getDay() - timeDate.getDay() == -5) {// 前天
                desc = FLYApplication.getInstance().getString(R.string.friendly_time_before_yesterday) + " " + friendly_format1.format(timeDate);
            }
            // else 超过前天
        }

        if (TextUtils.isEmpty(desc)) {
            desc = friendly_format2.format(timeDate);
        }
        return desc;
    }

    public static String sk_time_friendly_format2(long time) {
        return friendly_format2.format(new Date(time));
    }

    public static String sk_time_s_long_2_str(long time) {
        return s_long_2_str(time);
    }

    public static String skNearbyTimeString(long timestamp) {
        return sdfNearby.format(new Date(timestamp * 1000));
    }

    public static String sk_time_ss_long_2_str(long time) {
        return ss_long_2_str(time * 1000);
    }

    public static long sk_time_s_str_2_long(String dateString) {
        return s_str_2_long(dateString);
    }

    public static long sk_time_current_time() {
        // 加上与服务器时间差值，
        long timeDifference = PreferenceUtils.getLong(FLYApplication.getContext(), KEY_TIME_DIFFERENCE, 0L);
        return System.currentTimeMillis() + timeDifference;
    }

    public static String sk_time_long_to_hm_str(long time) {
        try {
            return hm_formater.format(new Date(time));
        } catch (Exception e) {
            e.printStackTrace();
        }
        return "";
    }

    public static String sk_time_long_to_chat_time_str(long time) {
        String date1 = sk_time_s_long_2_str(time);
        String date2 = sk_time_s_long_2_str(System.currentTimeMillis());
        if (date1.compareToIgnoreCase(date2) == 0) {// 是同一天
            return sk_time_long_to_hm_str(time);
        } else {
            return long_to_yMdHm_str(time);
        }
    }

    // 日期加小时的字符串
    public static String long_to_yMdHm_str(long time) {
        return sk_format_1.format(new Date(time));
    }

    public static long sk_time_yMdHm_str_to_long(String time) {
        try {
            return sk_format_1.parse(time).getTime();
        } catch (ParseException e) {
            e.printStackTrace();
        }
        return 0;
    }

    public static int yMdHm_getYear(String dateString) {
        try {
            Date d = sk_format_1.parse(dateString);
            return d.getYear() + 1900;// 年份是基于格林威治时间，所以加上1900
        } catch (ParseException e) {
            e.printStackTrace();
        }
        return 0;
    }

    public static int yMdHm_getMonth(String dateString) {
        try {
            Date d = sk_format_1.parse(dateString);
            return d.getMonth();// 月份从0-11
        } catch (ParseException e) {
            e.printStackTrace();
        }
        return 0;
    }

    public static int yMdHm_getDayOfMonth(String dateString) {
        try {
            Date d = sk_format_1.parse(dateString);
            return d.getDate();
        } catch (ParseException e) {
            e.printStackTrace();
        }
        return 0;
    }

    public static int yMdHm_getHours(String timeString) {
        try {
            Date date = sk_format_1.parse(timeString);
            return date.getHours();
        } catch (ParseException e) {
            e.printStackTrace();
        }
        return 0;
    }

    public static int yMdHm_getMinutes(String timeString) {
        try {
            Date date = sk_format_1.parse(timeString);
            return date.getMinutes();
        } catch (ParseException e) {
            e.printStackTrace();
        }
        return 0;
    }

    /**
     * for birthday 秒
     */
    public static long getSpecialBeginTime(TextView textView, long time) {
        long currentTime = System.currentTimeMillis() / 1000;
        if (time > currentTime) {
            time = currentTime;
        }
        textView.setText(sk_time_s_long_2_str_for_birthday(time));
        return time;
    }

    /**
     * for birthday 秒
     */
    public static String sk_time_s_long_2_str_for_birthday(long time) {
        return s_long_2_str(time * 1000);
    }

    /**
     * @param textView
     * @param time     毫秒时间戳
     * @return
     */
    public static long getSpecialEndTime(TextView textView, long time) {
        long currentTime = System.currentTimeMillis();
        if (time == 0 || time > currentTime - 24 * 60 * 60) {
            textView.setText(FLYApplication.getInstance().getString(R.string.to_this_day));
            return 0;
        }
        textView.setText(sk_time_s_long_2_str(time));
        return time;
    }

    public static int sk_time_age(long birthday) {
        int age = (new Date().getYear()) - (new Date(birthday).getYear());
        if (age < 0 || age > 100) {
            return 25;
        }
        return age;
    }

    public static void responseTime(long l) {
        //noinspection UnnecessaryLocalVariable
        long serverTime = l;
        // 接口没给时间会得到0， 或者老服务器时间是秒，差了一千倍也无视，
        if (serverTime < 1552978894754L) {
            return;
        }
        long localTime = System.currentTimeMillis();
        long timeDifference = serverTime - localTime;
        Log.e("TimeUtils", "timeDifference = " + timeDifference);
        PreferenceUtils.putLong(FLYApplication.getContext(), KEY_TIME_DIFFERENCE, timeDifference);
    }

    public static String formattime(long duration) {
        String time = "";
        long minute = duration / 60000;
        long seconds = duration % 60000;
        long second = Math.round((float) seconds / 1000);
        if (minute < 10) {
            time += "0";
        }
        time += minute + ":";
        if (second < 10) {
            time += "0";
        }
        time += second;
        return time;
    }

    //传入的数据为毫秒数
    public static String timeParse(long time) {
        String min = (time / (1000 * 60)) + "";
        String second = (time % (1000 * 60) / 1000) + "";
        if (min.length() < 2) {
            min = 0 + min;
        }
        if (second.length() < 2) {
            second = 0 + second;
        }
        return min + ":" + second;
    }
}
