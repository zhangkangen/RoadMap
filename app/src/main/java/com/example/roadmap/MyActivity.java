package com.example.roadmap;

import android.app.Activity;
import android.app.Service;
import android.content.Context;
import android.content.Intent;
import android.graphics.Bitmap;
import android.location.LocationManager;
import android.os.Handler;
import android.os.Message;
import android.support.v7.app.ActionBar;
import android.support.v7.app.ActionBarActivity;
import android.os.Bundle;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewConfiguration;
import android.widget.Button;
import android.widget.ImageButton;
import android.widget.LinearLayout;
import android.widget.Toast;

import com.baidu.location.BDLocation;
import com.baidu.location.BDLocationListener;
import com.baidu.location.LocationClient;
import com.baidu.location.LocationClientOption;
import com.baidu.mapapi.SDKInitializer;
import com.baidu.mapapi.map.BaiduMap;
import com.baidu.mapapi.map.DotOptions;
import com.baidu.mapapi.map.MapPoi;
import com.baidu.mapapi.map.MapStatusUpdate;
import com.baidu.mapapi.map.MapStatusUpdateFactory;
import com.baidu.mapapi.map.MapView;
import com.baidu.mapapi.map.Marker;
import com.baidu.mapapi.map.MyLocationData;
import com.baidu.mapapi.map.OverlayOptions;
import com.baidu.mapapi.map.PolylineOptions;
import com.baidu.mapapi.model.LatLng;
import com.baidu.mapapi.overlayutil.OverlayManager;
import com.baidu.mapapi.utils.DistanceUtil;

import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.lang.reflect.Field;
import java.net.MalformedURLException;
import java.net.SocketTimeoutException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.List;
import java.util.TimerTask;

import com.example.roadmap.common.ActionMode;
import com.example.roadmap.fragment.GPSDialogFragment;
import com.example.roadmap.model.Track;
import com.example.roadmap.sqlhelper.TrackDBManager;
import com.tencent.connect.share.QQShare;
import com.tencent.open.utils.HttpUtils;
import com.tencent.tauth.IRequestListener;
import com.tencent.tauth.IUiListener;
import com.tencent.tauth.Tencent;
import com.tencent.tauth.UiError;

import org.apache.http.conn.ConnectTimeoutException;
import org.json.JSONException;
import org.json.JSONObject;


public class MyActivity extends ActionBarActivity {

    private MapView mMapView;
    private BaiduMap mBaiduMap;

    private Button btnMapType;
    private ImageButton btnDis;
    private LinearLayout trackModeLayout;

    List<LatLng> pointList = new ArrayList<LatLng>();
    List<LatLng> disPointList = new ArrayList<LatLng>();

    private float DIS = 0;
    private boolean BTN_DIS_ISCLICK = false;
    private int TOTAL_DISTANCE = 0;

    private LocationManager locManager;
    private OverlayManager overlayManager;
    private Context context;
    private static Tencent mTencent;

    private BDLocationListener bdLocationListener = new MyBDLocationListener();
    private IUiListener tencentUiListener = new IUiListener() {
        @Override
        public void onComplete(Object o) {
            displayToast(o.toString());
            Log.e("Error", "ok");
        }

        @Override
        public void onError(UiError uiError) {
            displayToast(uiError.errorMessage);
        }

        @Override
        public void onCancel() {

        }
    };
    Handler handler = new android.os.Handler() {
        @Override
        public void handleMessage(Message msg) {
            // TODO Auto-generated method stub
            super.handleMessage(msg);
            switch (msg.what) {
                case 0:
                    LatLng point = (LatLng) msg.obj;
                    motionTrail(point);
                    break;

                default:
                    break;
            }

        }
    };
    BaiduMap.SnapshotReadyCallback snapshotReadyCallback = new BaiduMap.SnapshotReadyCallback() {
        @Override
        public void onSnapshotReady(Bitmap bitmap) {
            try {
                File image = File.createTempFile("snapshot", ".jpg");
                String filepath = image.getCanonicalPath();
                displayToast(filepath);
                FileOutputStream outputStream = new FileOutputStream(filepath);
                outputStream.write(bitMapToBytes(bitmap));
                outputStream.close();
                Bundle params = new Bundle();
                params.putInt(QQShare.SHARE_TO_QQ_KEY_TYPE, QQShare.SHARE_TO_QQ_TYPE_IMAGE);
                params.putString(QQShare.SHARE_TO_QQ_IMAGE_LOCAL_URL,filepath);
//                /*params.putString(QQShare.SHARE_TO_QQ_TITLE, "test");
//                params.putString(QQShare.SHARE_TO_QQ_SUMMARY, "test");
//                params.putString(QQShare.SHARE_TO_QQ_TARGET_URL, "http://www.baidu.com");
//                ArrayList<String> imageUrls = new ArrayList<String>();
//                imageUrls.add(filepath);
//                params.putStringArrayList(QQShare.SHARE_TO_QQ_IMAGE_URL, imageUrls);*/
               params.putInt(QQShare.SHARE_TO_QQ_EXT_INT,QQShare.SHARE_TO_QQ_FLAG_QZONE_AUTO_OPEN);
                displayToast("doShare");
                mTencent.shareToQQ(MyActivity.this, params, tencentUiListener);
            } catch (Exception e) {
                e.printStackTrace();
                displayToast(e.getMessage());
            }
            //displayToast("截图" + bitmap.getHeight());
        }
    };

    private void shareToQzone(final Bundle params) {
        final Activity activity = MyActivity.this;
/*        new Thread(new Runnable() {
            @Override
            public void run() {
                MyActivity.mTencent.shareToQzone(activity,params,tencentUiListener);
            }
        }).start();*/
        mTencent.shareToQzone(MyActivity.this, params, tencentUiListener);

    }

    private byte[] bitMapToBytes(Bitmap bitmap) {
        ByteArrayOutputStream baos = new ByteArrayOutputStream();
        bitmap.compress(Bitmap.CompressFormat.PNG, 100, baos);
        return baos.toByteArray();
    }

    public void initTrack() {
        TrackDBManager manager = new TrackDBManager(context);
        Track track = new Track();
        track.distance = 0;
        track.actionMode = ActionMode.ActionMode_WALK;
        SimpleDateFormat df = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
        String timeline = df.format(new java.util.Date());
        track.timeline = timeline;
        track.trackTime = "0";
        manager.insertSingle(track);
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        SDKInitializer.initialize(getApplicationContext());
        setContentView(R.layout.activity_my);
        context = getApplicationContext();

        mTencent = Tencent.createInstance("1103581090", context);
        //initTrack();
        trackModeLayout = (LinearLayout) findViewById(R.id.trackModeLayout);
        initUi();//ui初始化
        //login();

        ActionBar actionBar = getSupportActionBar();
        actionBar.setDisplayShowHomeEnabled(true);
        getOverflowMenu();

//        try {
//            SharedPreferences preferences = getSharedPreferences("Latlng", Context.MODE_PRIVATE);
//            double latitude = preferences.getLong("latitude", (long) 39.93923);
//            double longitude = preferences.getLong("longitude", (long) 116.357428);
//
//            mBaiduMap.animateMapStatus(MapStatusUpdateFactory.newLatLngZoom(new LatLng(latitude, longitude), 16));
//        }catch (Exception e){
//            Log.i("Z.XK",e.getMessage());
//        }
        mMapView = (MapView) findViewById(R.id.bmapView);
        mMapView.showZoomControls(false);//不显示缩放按钮
        mBaiduMap = mMapView.getMap();

        mBaiduMap.setMapType(BaiduMap.MAP_TYPE_NORMAL);


        mBaiduMap.setOnMapClickListener(new BaiduMap.OnMapClickListener() {
            @Override
            public void onMapClick(LatLng latLng) {
                if (BTN_DIS_ISCLICK) {
                    getDistance(latLng);
                }
            }

            @Override
            public boolean onMapPoiClick(MapPoi mapPoi) {
                if (BTN_DIS_ISCLICK) {
                    getDistance(mapPoi.getPosition());
                    return true;
                } else {
                    displayToast(mapPoi.getName());
                    return true;
                }
            }
        });

        //切换地图模式
        btnMapType = (Button) findViewById(R.id.btn_mapType);
        btnMapType.setText("卫星");
        btnMapType.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                if (btnMapType.getText() == "卫星") {
                    if (mBaiduMap.getMapType() != BaiduMap.MAP_TYPE_SATELLITE) {
                        mBaiduMap.setMapType(BaiduMap.MAP_TYPE_SATELLITE);
                    }
                    btnMapType.setText("地图");
                } else if (btnMapType.getText() == "地图") {
                    if (mBaiduMap.getMapType() != BaiduMap.MAP_TYPE_NORMAL) {
                        mBaiduMap.setMapType(BaiduMap.MAP_TYPE_NORMAL);
                    }
                    btnMapType.setText("卫星");
                }
            }
        });

        //测距离
        btnDis = (ImageButton) findViewById(R.id.btn_distance);
        btnDis.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                if (BTN_DIS_ISCLICK) {
                    BTN_DIS_ISCLICK = false;
                    mBaiduMap.clear();
                    TOTAL_DISTANCE = 0;
                    disPointList.clear();
                } else {
                    BTN_DIS_ISCLICK = true;
                }
            }
        });

        //开启定位图层
        mBaiduMap.setMyLocationEnabled(true);

        locManager = (LocationManager) getApplicationContext()
                .getSystemService(Service.LOCATION_SERVICE);
        if (locManager.isProviderEnabled(LocationManager.GPS_PROVIDER)) {
            Toast.makeText(context, "GPS已达开", Toast.LENGTH_SHORT).show();
        } else {
            Toast.makeText(context, "请开启GPS！", Toast.LENGTH_SHORT).show();
            // Intent intent = new
            // Intent(Settings.ACTION_LOCATION_SOURCE_SETTINGS);// 跳转到位置设置界面
            // startActivityForResult(intent, R.layout.activity_main); //
            // 此为设置完成后返回到获取界面
            GPSDialogFragment dialogFragment = GPSDialogFragment
                    .newInstance("打开 GPS ？");
            dialogFragment.show(getFragmentManager(), "dialog");

        }

        LocationClient locClient = new LocationClient(
                context);

        LocationClientOption clientOption = new LocationClientOption();
        clientOption.setOpenGps(true);// 打开GPS
        clientOption.setCoorType("bd09ll");// 坐标类型
        clientOption.setScanSpan(5000);// 定位间隔

        locClient.setLocOption(clientOption);

        locClient.registerLocationListener(bdLocationListener);

        locClient.start();

        overlayManager = new OverlayManager(mBaiduMap) {
            @Override
            public List<OverlayOptions> getOverlayOptions() {
                return null;
            }

            @Override
            public boolean onMarkerClick(Marker marker) {
                return false;
            }
        };
    }

    private void initUi() {
        trackModeLayout.setVisibility(View.INVISIBLE);
    }

    /**
     * qq登陆
     */
    public void login() {
        if (!mTencent.isSessionValid()) {
            mTencent.login(this, "", tencentUiListener);
        }
    }

    public void onClickShare() {
        final Bundle params = new Bundle();
        params.putInt(QQShare.SHARE_TO_QQ_KEY_TYPE, QQShare.SHARE_TO_QQ_TYPE_DEFAULT);
        params.putString(QQShare.SHARE_TO_QQ_TITLE, "要分享的标题");
        params.putString(QQShare.SHARE_TO_QQ_SUMMARY, "要分享的摘要");
        params.putString(QQShare.SHARE_TO_QQ_TARGET_URL, "http://www.qq.com/news/1.html");//点击跳转页面
        params.putString(QQShare.SHARE_TO_QQ_IMAGE_URL, "");//图片地址
        params.putString(QQShare.SHARE_TO_QQ_IMAGE_LOCAL_URL, "");//本地图片地址
        params.putString(QQShare.SHARE_TO_QQ_APP_NAME, "RoadMap");
        mTencent.shareToQzone(MyActivity.this, params, tencentUiListener);
    }

    /**
     * 运动轨迹
     *
     * @param latLng
     */
    private void motionTrail(LatLng latLng) {
        //如果是第一个定位点，直接添加
        if (pointList.size() < 1) {
            pointList.add(latLng);
        } else if (DistanceUtil.getDistance(latLng,
                pointList.get(pointList.size() - 1)) > 10) {
            pointList.add(latLng);
            //画线

        }

        if (pointList.size() >= 3) {
            OverlayOptions options = new PolylineOptions().width(6)
                    .color(0xAAFF0000).points(pointList);
            mBaiduMap.addOverlay(options);
        }
    }

    /**
     * 对距离数据转换
     *
     * @param dis 距离
     * @return 转换后的距离
     */
    private String formatDis(int dis) {
        try {
            float d = (float) (((float) dis) * 1.0 / 1000);
            if (d > 1) {
                return d + "千米";
            } else {
                return dis + "米";
            }
        } catch (Exception e) {
            // TODO: handle exception
            e.printStackTrace();
            return "0";
        }
    }


    /**
     * 测距离方法
     *
     * @param latLng
     */
    private void getDistance(LatLng latLng) {
        if (BTN_DIS_ISCLICK) {
            // 画线
            if (disPointList.size() > 0) {
                LatLng lastLatLng = disPointList.get(disPointList.size() - 1);
                drawLine(lastLatLng, latLng);
                TOTAL_DISTANCE += DistanceUtil.getDistance(lastLatLng, latLng);
                displayToast(formatDis(TOTAL_DISTANCE));
            }

            disPointList.add(latLng);
            // 画点
            drawDot(disPointList);

        }
    }


    /**
     * 地图上画点
     *
     * @param list
     */
    private void drawDot(List<LatLng> list) {

        if (list == null || list.size() == 0)
            return;
        for (LatLng latLng : list) {
            OverlayOptions ops = new DotOptions().center(latLng).radius(6)
                    .color(0xFF0000FF);
            mBaiduMap.addOverlay(ops);
        }
    }

    /**
     * 地图上画线
     *
     * @param lastLatLng
     * @param currentLatLng
     */
    private void drawLine(LatLng lastLatLng, LatLng currentLatLng) {
        List<LatLng> list = new ArrayList<LatLng>();
        list.add(lastLatLng);
        list.add(currentLatLng);
        OverlayOptions options = new PolylineOptions().color(0xAA000000)
                .points(list);
        mBaiduMap.addOverlay(options);
    }

    // menu按钮
    public void getOverflowMenu() {
        try {
            ViewConfiguration config = ViewConfiguration.get(this);
            Field menuKeyField = ViewConfiguration.class
                    .getDeclaredField("sHasPermanentMenuKey");
            if (menuKeyField != null) {
                menuKeyField.setAccessible(true);
                menuKeyField.setBoolean(config, false);
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    public void displayToast(String str) {
        // TODO Auto-generated method stub
        Toast.makeText(context, str, Toast.LENGTH_LONG).show();
    }

    private void shareToQzoneClick() {
        mBaiduMap.snapshot(snapshotReadyCallback);
    }


    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.my, menu);
        return true;
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        if (null != mTencent) {
            mTencent.onActivityResult(requestCode, resultCode, data);
        }
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
            case R.id.action_history:
                Intent intent = new Intent(MyActivity.this, HistoryActivity.class);
                startActivity(intent);
                return true;
            case R.id.action_share:
                shareToQzoneClick();
                return true;
        }

        return super.onOptionsItemSelected(item);
    }

    public class MyBDLocationListener implements BDLocationListener {


        @Override
        public void onReceiveLocation(BDLocation location) {
            // TODO Auto-generated method stub
            if (location == null || mMapView == null)
                return;

            int locType = location.getLocType();


            if (locType == BDLocation.TypeGpsLocation
                    || locType == BDLocation.TypeNetWorkLocation) {


                Log.i("TAG", String.valueOf(locType));

                float direction = location.getDirection();
                // displayToast(String.valueOf(direction));
                if (direction < 0) {
                    direction = 0;
                }

                MyLocationData locData = new MyLocationData.Builder()
                        .accuracy(location.getRadius())
                                // 此处设置开发者获取到的方向信息，顺时针0-360
                        .direction(direction).latitude(location.getLatitude())
                        .longitude(location.getLongitude()).build();
                mBaiduMap.setMyLocationData(locData);

                LatLng point = new LatLng(location.getLatitude(),
                        location.getLongitude());
                MapStatusUpdate u = MapStatusUpdateFactory.newLatLngZoom(point,
                        mBaiduMap.getMapStatus().zoom);
                mBaiduMap.animateMapStatus(u);

                Message msg = new Message();
                msg.what = 0;
                msg.obj = point;
                handler.sendMessage(msg);
            }
        }

    }

    public class BaseApiListener implements IRequestListener {
        @Override
        public void onComplete(JSONObject jsonObject) {

        }

        @Override
        public void onIOException(IOException e) {

        }

        @Override
        public void onMalformedURLException(MalformedURLException e) {

        }

        @Override
        public void onJSONException(JSONException e) {

        }

        @Override
        public void onConnectTimeoutException(ConnectTimeoutException e) {

        }

        @Override
        public void onSocketTimeoutException(SocketTimeoutException e) {

        }

        @Override
        public void onNetworkUnavailableException(HttpUtils.NetworkUnavailableException e) {

        }

        @Override
        public void onHttpStatusException(HttpUtils.HttpStatusException e) {

        }

        @Override
        public void onUnknowException(Exception e) {

        }
    }

}
