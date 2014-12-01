package com.example.roadmap.sqlhelper;

import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;

import com.example.roadmap.model.TrackDtl;

import java.util.ArrayList;
import java.util.List;

/**
 * Created by Administrator on 2014/10/29.
 */
public class TrackDtlDBManager {

    private DBHelper dbHelper;
    private SQLiteDatabase db;

    public TrackDtlDBManager(Context context) {
        dbHelper = new DBHelper(context);
        db = dbHelper.getWritableDatabase();
    }


    public List<TrackDtl> queryByTrackId(int id) {
        List<TrackDtl> list = new ArrayList<TrackDtl>();
        Cursor c = db.rawQuery("select * from track_dtl where _id = ?", new String[]{String.valueOf(id)});
        while(c.moveToNext()){
            TrackDtl trackDtl = new TrackDtl();
            trackDtl.id = c.getInt(c.getColumnIndex("_id"));
            trackDtl.trackId = c.getInt(c.getColumnIndex("trackId"));
            trackDtl.latitude = c.getDouble(c.getColumnIndex("latitude"));
            trackDtl.longitude = c.getDouble(c.getColumnIndex("longitude"));
            list.add(trackDtl);
        }
        c.close();
        return list;
    }

}
