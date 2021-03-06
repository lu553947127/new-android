package com.ktw.bitbit.db;

import android.content.Context;
import android.database.sqlite.SQLiteDatabase;

import com.j256.ormlite.android.AndroidDatabaseConnection;
import com.j256.ormlite.android.apptools.OrmLiteSqliteOpenHelper;
import com.j256.ormlite.support.ConnectionSource;
import com.j256.ormlite.support.DatabaseConnection;
import com.j256.ormlite.table.TableUtils;
import com.ktw.bitbit.R;
import com.ktw.bitbit.bean.Company;
import com.ktw.bitbit.bean.Contact;
import com.ktw.bitbit.bean.Friend;
import com.ktw.bitbit.bean.Label;
import com.ktw.bitbit.bean.MsgRoamTask;
import com.ktw.bitbit.bean.MyPhoto;
import com.ktw.bitbit.bean.MyZan;
import com.ktw.bitbit.bean.UploadingFile;
import com.ktw.bitbit.bean.User;
import com.ktw.bitbit.bean.UserAvatar;
import com.ktw.bitbit.bean.VideoFile;
import com.ktw.bitbit.bean.circle.CircleMessage;
import com.ktw.bitbit.bean.message.NewFriendMessage;
import com.ktw.bitbit.db.dao.CircleMessageDao;
import com.ktw.bitbit.db.dao.ContactDao;
import com.ktw.bitbit.db.dao.FriendDao;
import com.ktw.bitbit.db.dao.LabelDao;
import com.ktw.bitbit.db.dao.MsgRoamTaskDao;
import com.ktw.bitbit.db.dao.MyPhotoDao;
import com.ktw.bitbit.db.dao.MyZanDao;
import com.ktw.bitbit.db.dao.NewFriendDao;
import com.ktw.bitbit.db.dao.UploadingFileDao;
import com.ktw.bitbit.db.dao.UserAvatarDao;
import com.ktw.bitbit.db.dao.UserDao;
import com.ktw.bitbit.db.dao.VideoFileDao;
import com.ktw.bitbit.util.ThreadManager;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.sql.SQLException;


public class SQLiteHelper extends OrmLiteSqliteOpenHelper {
    public static final String DATABASE_NAME = "shiku.db";
    private static final int DATABASE_VERSION = 13;
    private static SQLiteHelper mInstance;

    // public static final String DATABASE_PATH = Config.SDCARD_PATH +
    // File.separator + "shiku" + File.separator + "shiku.db";
    public SQLiteHelper(Context context) {
        super(context, DATABASE_NAME, null, DATABASE_VERSION);
    }

    public static synchronized SQLiteHelper getInstance(Context context) {
        if (mInstance == null) {
            synchronized (SQLiteHelper.class) {
                if (mInstance == null) {
                    mInstance = new SQLiteHelper(context);
                }
            }
        }

        return mInstance;
    }

    /**
     * ??????????????????
     */
    public static void rebuildDatabase(Context ctx) {
        ctx.deleteDatabase(SQLiteHelper.DATABASE_NAME);
        copyDatabaseFile(ctx);
    }

    /**
     * ?????????data??????????????????????????????????????????????????????
     */
    public static void copyDatabaseFile(Context context) {
        File dbFile = context.getDatabasePath(SQLiteHelper.DATABASE_NAME);
        if (dbFile.exists()) {
            return;
        }
        File parentFile = dbFile.getParentFile();
        if (!parentFile.exists()) {
            try {
                parentFile.mkdirs();
            } catch (Exception e) {
                e.printStackTrace();
            }
        }
        InputStream in = null;
        FileOutputStream out = null;
        try {
            dbFile.createNewFile();
            in = context.getResources().openRawResource(R.raw.shiku);
            int size = in.available();
            byte buf[] = new byte[size];
            in.read(buf);
            out = new FileOutputStream(dbFile);
            out.write(buf);
            out.flush();
        } catch (Exception e) {
            e.printStackTrace();
        } finally {
            try {
                if (in != null) {
                    in.close();
                }
                if (out != null) {
                    out.close();
                }
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
    }

    @Override
    public void onCreate(SQLiteDatabase db, ConnectionSource connSource) {
        createTables(connSource);
    }

    @Override
    public void onUpgrade(SQLiteDatabase db, ConnectionSource connSource, int oldVersion, int newVersion) {
        version2Drop(connSource);
        createTables(connSource);
    }

    /**
     * ???????????? {@link OrmLiteSqliteOpenHelper#onUpgrade(android.database.sqlite.SQLiteDatabase, int, int)}
     */
    @Override
    public void onDowngrade(SQLiteDatabase db, int oldVersion, int newVersion) {
        ConnectionSource cs = this.getConnectionSource();
        DatabaseConnection conn = cs.getSpecialConnection();
        boolean clearSpecial = false;
        if (conn == null) {
            conn = new AndroidDatabaseConnection(db, true, this.cancelQueriesEnabled);

            try {
                cs.saveSpecialConnection((DatabaseConnection) conn);
                clearSpecial = true;
            } catch (SQLException var11) {
                throw new IllegalStateException("Could not save special connection", var11);
            }
        }

        try {
            this.onDowngrade(db, cs, oldVersion, newVersion);
        } finally {
            if (clearSpecial) {
                cs.clearSpecialConnection((DatabaseConnection) conn);
            }

        }
    }

    private void onDowngrade(SQLiteDatabase db, ConnectionSource connSource, int oldVersion, int newVersion) {
        version2Drop(connSource);
        createTables(connSource);
    }

    private void createTables(ConnectionSource connSource) {
        try {
            TableUtils.createTableIfNotExists(connSource, Company.class);
            TableUtils.createTableIfNotExists(connSource, User.class);
            TableUtils.createTableIfNotExists(connSource, Friend.class);
            TableUtils.createTableIfNotExists(connSource, NewFriendMessage.class);
            TableUtils.createTableIfNotExists(connSource, VideoFile.class);
            TableUtils.createTableIfNotExists(connSource, MyPhoto.class);
            TableUtils.createTableIfNotExists(connSource, CircleMessage.class);
            TableUtils.createTableIfNotExists(connSource, MyZan.class);
            TableUtils.createTableIfNotExists(connSource, UserAvatar.class);
            TableUtils.createTableIfNotExists(connSource, Label.class);
            TableUtils.createTableIfNotExists(connSource, Contact.class);
            TableUtils.createTableIfNotExists(connSource, MsgRoamTask.class);
            TableUtils.createTableIfNotExists(connSource, UploadingFile.class);
        } catch (SQLException e) {
            e.printStackTrace();
        }
    }

    private void version2Drop(ConnectionSource connSource) {
        try {
            TableUtils.dropTable(connSource, Company.class, false);
            TableUtils.dropTable(connSource, User.class, false);
            TableUtils.dropTable(connSource, Friend.class, false);
            TableUtils.dropTable(connSource, NewFriendMessage.class, false);
            TableUtils.dropTable(connSource, VideoFile.class, false);
            TableUtils.dropTable(connSource, MyPhoto.class, false);
            TableUtils.dropTable(connSource, CircleMessage.class, false);
            TableUtils.dropTable(connSource, MyZan.class, false);
            TableUtils.dropTable(connSource, UserAvatar.class, false);
            TableUtils.dropTable(connSource, Label.class, false);
            TableUtils.dropTable(connSource, Contact.class, false);
            TableUtils.dropTable(connSource, MsgRoamTask.class, false);
            TableUtils.dropTable(connSource, UploadingFile.class, false);
        } catch (SQLException e) {
            e.printStackTrace();
        }
    }

    public void clearCacheByUserId(String userId) {

        ThreadManager.getPool().execute(() -> {
            UserDao.getInstance().deleteUser(userId);
            FriendDao.getInstance().deleteFriend(userId);
            NewFriendDao.getInstance().deleteNewFriendMessage(userId);
            VideoFileDao.getInstance().deleteUserVideoHistory(userId);
            MyPhotoDao.getInstance().deletePhoto(userId);
            CircleMessageDao.getInstance().deleteCircleMessageByUserId(userId);
            MyZanDao.getInstance().deleteZan(userId);
            UserAvatarDao.getInstance().deleteUserAvatar(userId);
            LabelDao.getInstance().deleteLabel(userId);
            ContactDao.getInstance().deleteContact(userId);
            MsgRoamTaskDao.getInstance().deleteMsgRoamTask(userId);
            UploadingFileDao.getInstance().deleteAllUploadingFiles(userId);
        });
    }
}
