package com.example.roadmap;

import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.support.v7.app.ActionBar;
import android.support.v7.app.ActionBarActivity;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ListAdapter;
import android.widget.ListView;
import android.widget.SimpleAdapter;
import android.widget.Toast;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import com.example.roadmap.common.ActionMode;
import com.example.roadmap.model.Track;
import com.example.roadmap.sqlhelper.TrackDBManager;


public class HistoryActivity extends ActionBarActivity {

    private ListView lvHistory;
    TrackDBManager manager;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_history);

        ActionBar actionbar = getSupportActionBar();
        actionbar.setHomeButtonEnabled(true);
        actionbar.setDisplayShowHomeEnabled(true);
        actionbar.setDisplayHomeAsUpEnabled(true);

        manager = new TrackDBManager(this);

        lvHistory = (ListView) findViewById(R.id.lvHistory);

        ListAdapter adapter = new SimpleAdapter(this,
                getData(getApplicationContext()),
                R.layout.activity_listview_history,
                new String[]{ "timeline", "distance", "actionMode"},
                new int[]{R.id.tv_timeline, R.id.tv_distance, R.id.imageView});
        lvHistory.setAdapter(adapter);

        lvHistory.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> adapterView, View view, int i, long l) {
                Map<String, Object> map = (HashMap<String, Object>) lvHistory.getItemAtPosition(i);
                int id = Integer.parseInt(map.get("_id").toString());
                Track track = manager.querySingle(id);
                Intent intent = new Intent(HistoryActivity.this, HistoryDetailActivity.class);
                intent.putExtra("track_id", track._id);
                startActivity(intent);//跳到详细页面
            }
        });
    }


    public List<Map<String, Object>> getData(Context context) {
        TrackDBManager dbManager = new TrackDBManager(context);
        List<Track> list = dbManager.query();
        List<Map<String, Object>> dataList = new ArrayList<Map<String, Object>>();
        for (Track track : list) {
            Map<String, Object> map = new HashMap<String, Object>();
            map.put("_id", track._id);
            map.put("timeline", track.timeline);
            map.put("distance", track.distance);
            switch (track.actionMode) {//图片
                case ActionMode.ActionMode_WALK:
                    map.put("actionMode", R.drawable.common_topbar_route_foot_normal);
                    break;
                case ActionMode.ActionMode_CYCLE:
                    map.put("actionMode", R.drawable.ic_launcher);
                    break;
                default:
                    map.put("actionMode", R.drawable.ic_launcher);
                    break;
            }
            dataList.add(map);
            //Toast.makeText(this, String.valueOf(dataList.size())+"列",Toast.LENGTH_LONG).show();
        }
        return dataList;
    }


    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.history, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        // Handle action bar item clicks here. The action bar will
        // automatically handle clicks on the Home/Up button, so long
        // as you specify a parent activity in AndroidManifest.xml.
        int id = item.getItemId();
        switch (id) {
            case R.id.action_settings:
                return true;
            case android.R.id.home:
                Intent intent = new Intent(this, MyActivity.class);
                intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
                startActivity(intent);
                return true;
        }
        return super.onOptionsItemSelected(item);
    }
}
