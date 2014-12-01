package com.example.roadmap.sqlhelper;

import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.util.Log;

import java.util.ArrayList;
import java.util.List;

import com.example.roadmap.model.Track;

/**
 * Created by Administrator on 2014/10/27.
 */
public class TrackDBManager {

    public DBHelper dbHelper;
    public SQLiteDatabase db;

    public TrackDBManager(Context context) {
        dbHelper = new DBHelper(context);
        db = dbHelper.getWritableDatabase();
    }

    /**
     * 查询所有的数据
     *
     * @return List<Track>
     */
    public List<Track> query() {
        ArrayList<Track> list = new ArrayList<Track>();
        Cursor cursor = db.rawQuery("select * from track", null);
        while (cursor.moveToNext()) {
            Track track = new Track();
            track._id = cursor.getInt(cursor.getColumnIndex("_id"));
            track.timeline = cursor.getString(cursor.getColumnIndex("timeline"));
            track.distance = cursor.getDouble(cursor.getColumnIndex("distance"));
            track.actionMode = cursor.getInt(cursor.getColumnIndex("actionMode"));
            //track.trackTime = cursor.getString(cursor.getColumnIndex("trackTime"));
            list.add(track);
        }
        cursor.close();
        db.close();
        return list;
    }

    public void insertSingle(Track track) {
        ContentValues cv = new ContentValues();
        cv.put("timeline", track.timeline);
        cv.put("distance", track.distance);
        cv.put("actionMode", track.actionMode);
        cv.put("trackTime", track.trackTime);
        db.insert("track", null, cv);
        db.close();
    }

    public Track querySingle(int id) {
        Track track = new Track();
        // TODO Auto-generated method stub 根据id查询数据
        try {
            Cursor c = db.rawQuery("select * from track where _id=?", new String[]{String.valueOf(id)});
            if (c.moveToFirst()) {
                track._id = id;
                track.actionMode = c.getInt(c.getColumnIndex("actionMode"));
                track.timeline = c.getString(c.getColumnIndex("timeline"));
                track.distance = c.getDouble(c.getColumnIndex("distance"));
                track.trackTime = c.getString(c.getColumnIndex("trackTime"));
            }
            c.close();

        } catch (Exception e) {
            Log.e("Sql Error", e.getMessage());

        }
        db.close();
        return track;
    }

    public int delete(int id) {
        try {
            int count = db.delete("track", "_id=?", new String[]{String.valueOf(id)});
            db.close();
            return count;

        } catch (Exception e) {
            db.close();
            e.printStackTrace();
            return -1;
        }

    }
}
