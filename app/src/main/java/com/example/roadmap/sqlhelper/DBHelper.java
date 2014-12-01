package com.example.roadmap.sqlhelper;

import android.content.Context;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;

/**
 * Created by Administrator on 2014/10/27.
 */
public class DBHelper extends SQLiteOpenHelper {

    private static final String DATABASE_NAME = "mydata.db";
    private static final int DATABASE_VERSION = 1;

    public DBHelper(Context context) {
        super(context, DATABASE_NAME, null, DATABASE_VERSION);
    }

    @Override
    public void onCreate(SQLiteDatabase db) {

        db.execSQL("CREATE TABLE IF NOT EXISTS track "
                + "(_id INTEGER PRIMARY KEY AUTOINCREMENT,timeline TEXT,trackTime TEXT,distance double default 0,actionMode integer)");
        db.execSQL("create table if not exists track_dtl "
                + "(_id INTEGER PRIMARY KEY AUTOINCREMENT,trackId INTEGER,latitude double,longitude double)");
    }

    @Override
    public void onUpgrade(SQLiteDatabase db, int i, int i2) {

    }
}
