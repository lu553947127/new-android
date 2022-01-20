package com.ktw.bitbit.broadcast;

import com.ktw.bitbit.FLYAppConfig;

/**
 * 项目老代码中大量广播都是写死action，统一移到这里，改成包名开头，
 */
public class OtherBroadcast {
    public static final String Read = FLYAppConfig.sPackageName + "Read";
    public static final String NAME_CHANGE = FLYAppConfig.sPackageName + "NAME_CHANGE";
    public static final String TYPE_DELALL = FLYAppConfig.sPackageName + "TYPE_DELALL";
    public static final String SEND_MULTI_NOTIFY = FLYAppConfig.sPackageName + "SEND_MULTI_NOTIFY";
    public static final String CollectionRefresh = FLYAppConfig.sPackageName + "CollectionRefresh";
    public static final String CollectionRefresh_ChatFace = FLYAppConfig.sPackageName + "CollectionRefresh_ChatFace";
    public static final String NO_EXECUTABLE_INTENT = FLYAppConfig.sPackageName + "NO_EXECUTABLE_INTENT";
    public static final String QC_FINISH = FLYAppConfig.sPackageName + "QC_FINISH";
    public static final String longpress = FLYAppConfig.sPackageName + "longpress";
    public static final String IsRead = FLYAppConfig.sPackageName + "IsRead";
    public static final String MULTI_LOGIN_READ_DELETE = FLYAppConfig.sPackageName + "MULTI_LOGIN_READ_DELETE";
    public static final String TYPE_INPUT = FLYAppConfig.sPackageName + "TYPE_INPUT";
    public static final String MSG_BACK = FLYAppConfig.sPackageName + "MSG_BACK";
    public static final String REFRESH_MANAGER = FLYAppConfig.sPackageName + "REFRESH_MANAGER";
    public static final String singledown = FLYAppConfig.sPackageName + "singledown";
    public static final String SYNC_CLEAN_CHAT_HISTORY = FLYAppConfig.sPackageName + "sync_clean_chat_history";
    public static final String SYNC_SELF_DATE = FLYAppConfig.sPackageName + "sync_self_data";
    public static final String SYNC_SELF_DATE_NOTIFY = FLYAppConfig.sPackageName + "sync_self_data_notify";
    public static final String FLOATING_SHOW_HIDE = FLYAppConfig.sPackageName + "floating_show_hide";
    public static final String SYNC_EMOT_REFRESH = FLYAppConfig.sPackageName + "sync_emot_refresh";
    public static final String SYNC_EMOT_PACKAGE_ADD = FLYAppConfig.sPackageName + "sync_emot_package_add";
    public static final String SYNC_EMOT_PACKAGE_REMOVE = FLYAppConfig.sPackageName + "sync_emot_package_remove";
    public static final String SYNC_GROUP_YINSHENREN= FLYAppConfig.sPackageName + "sync_group_yinshenren";
}
