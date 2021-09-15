package com.ktw.fly.db;

import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.text.TextUtils;

import java.util.ArrayList;
import java.util.List;

/**
 * 对原生SQL语句的一些支持
 *
 * @author dty
 */
public class SQLiteRawUtil {
    public static final String CHAT_MESSAGE_TABLE_PREFIX = "msg_";

    public static String getCreateChatMessageTableSql(String tableName) {
        // 增加字段isReadDel,isEncrypt
        String sql = "CREATE TABLE IF NOT EXISTS "
                + tableName
                + " (_id INTEGER PRIMARY KEY AUTOINCREMENT," +
                "type INTEGER NOT NULL," +
                "timeSend INTEGER NOT NULL," +
                "deleteTime INTEGER NOT NULL," +
                "packetId VARCHAR NOT NULL," +
                "timeReceive INTEGER," +
                "fromUserId VARCHAR," +
                "toUserId VARCHAR," +
                "fromUserName VARCHAR," +
                "toUserName VARCHAR," +
                "isMySend SMALLINT," +
                "content VARCHAR," +
                "filePath VARCHAR," +
                "location_y VARCHAR,location_x VARCHAR," +
                "sendRead SMALLINT," +
                "isUpload SMALLINT," +
                "uploadSchedule INTEGER," +
                "isDownload SMALLINT," +
                "messageState INTEGER, timeLen INTEGER, " +
                "fileSize INTEGER,objectId VARCHAR," +
                "sipStatus INTEGER,sipDuration INTEGER," +
                "isReadDel INTEGER,isEncrypt INTEGER," +
                "fromId VARCHAR," +
                "isExpired INTEGER," +
                "reSendCount INTEGER,readPersons INTEGER,readTime INTEGER)";
        return sql;
    }

    public static void createTableIfNotExist(SQLiteDatabase db, String tableName, String createTableSql) {
        if (isTableExist(db, tableName)) {
            return;
        }
        db.execSQL(createTableSql);
    }

    public static boolean isTableExist(SQLiteDatabase db, String tableName) {
        boolean result = false;
        if (TextUtils.isEmpty(tableName.trim())) {
            return false;
        }
        Cursor cursor = null;
        try {
            String sql = "select count(*) as c from Sqlite_master  where type ='table' and name ='" + tableName.trim() + "' ";
            cursor = db.rawQuery(sql, null);
            if (cursor.moveToNext()) {
                int count = cursor.getInt(0);
                if (count > 0) {
                    result = true;
                }
            }
        } catch (Exception e) {
            e.printStackTrace();
        } finally {
            if (cursor != null) {
                cursor.close();
            }
        }
        return result;
    }

    public static void dropTable(SQLiteDatabase db, String tableName) {
        String sql = "drop table " + tableName;
        try {
            db.execSQL(sql);
        } catch (Exception e) {
            // AndroidRuntime: FATAL EXCEPTION
            // no such table: msg_id
        }
    }

    /**
     * 获取当前用户的消息表
     *
     * @param db
     * @param ownerId
     * @return
     */
    public static List<String> getUserChatMessageTables(SQLiteDatabase db, String ownerId) {
        String tablePrefix = CHAT_MESSAGE_TABLE_PREFIX + ownerId;
        Cursor cursor = null;
        try {
            String sql = "select name from Sqlite_master where type ='table' and name like '" + tablePrefix + "%'";
            cursor = db.rawQuery(sql, null);
            if (cursor != null) {
                List<String> tables = new ArrayList<String>();
                while (cursor.moveToNext()) {
                    String name = cursor.getString(0);
                    if (!TextUtils.isEmpty(name)) {
                        tables.add(name);
                    }
                }
                return tables;
            }
        } catch (Exception e) {
            e.printStackTrace();
        } finally {
            if (cursor != null) {
                cursor.close();
            }
        }
        return null;
    }
}
