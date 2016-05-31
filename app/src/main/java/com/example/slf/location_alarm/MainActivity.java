package com.example.slf.location_alarm;

import android.app.AlertDialog;
import android.app.Dialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.SharedPreferences;
import android.graphics.Point;
import android.media.MediaPlayer;
import android.media.Ringtone;
import android.media.RingtoneManager;
import android.net.Uri;
import android.os.Bundle;
import android.os.Vibrator;
import android.support.design.widget.FloatingActionButton;
import android.support.design.widget.Snackbar;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.Menu;
import android.view.MenuItem;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;

import com.baidu.location.BDLocation;
import com.baidu.location.BDLocationListener;
import com.baidu.location.BDNotifyListener;
import com.baidu.location.LocationClient;
import com.baidu.location.LocationClientOption;
import com.baidu.mapapi.SDKInitializer;
import com.baidu.mapapi.map.BaiduMap;
import com.baidu.mapapi.map.BitmapDescriptor;
import com.baidu.mapapi.map.BitmapDescriptorFactory;
import com.baidu.mapapi.map.MapStatus;
import com.baidu.mapapi.map.MapStatusUpdate;
import com.baidu.mapapi.map.MapStatusUpdateFactory;
import com.baidu.mapapi.map.MapView;
import com.baidu.mapapi.map.Marker;
import com.baidu.mapapi.map.MarkerOptions;
import com.baidu.mapapi.map.MyLocationData;
import com.baidu.mapapi.map.OverlayOptions;
import com.baidu.mapapi.model.LatLng;
import com.baidu.mapapi.utils.DistanceUtil;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.List;

public class MainActivity extends AppCompatActivity implements View.OnLongClickListener {

    private MapView mMapView = null;
    private BaiduMap mBaiduMap=null;
    public LocationClient mLocationClient = null;
    public BDLocationListener myListener =null;
    private Boolean TimeUp=true;
    private LatLng point;
    private NotifyLister mNotifyer;
    private LatLng touchedPoint;
    private Ringtone r;
    private LatLng longTouchedPoint;
    private Marker long_marker=null;
    //储存marker，方便删除等
    private List<Marker> markerList;
    String in_content="";
    private String this_date;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        //在使用SDK各组件之前初始化context信息，传入ApplicationContext
        //注意该方法要再setContentView方法之前实现
        SDKInitializer.initialize(getApplicationContext());
        setContentView(R.layout.activity_main);
        Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);
        markerList=new ArrayList<Marker>();
        FloatingActionButton fab = (FloatingActionButton) findViewById(R.id.fab);
        fab.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
//                Snackbar.make(view, "长按可在你当前地点设置闹钟", Snackbar.LENGTH_LONG)
//                        .setAction("Action", null).show();
//                //定义地图状态
                MapStatus mMapStatus = new MapStatus.Builder()
                        .target(point)
                        .zoom(18)
                        .build();
                //定义MapStatusUpdate对象，以便描述地图状态将要发生的变化
                MapStatusUpdate mMapStatusUpdate = MapStatusUpdateFactory.newMapStatus(mMapStatus);
                //改变地图状态
                mBaiduMap.setMapStatus(mMapStatusUpdate);
                TimeUp = false;
            }
        });
      //  fab.setOnLongClickListener(this);


        //获取地图控件引用
        mMapView = (MapView) findViewById(R.id.bmapView);
        mBaiduMap = mMapView.getMap();
        //普通地图
        mBaiduMap.setMapType(BaiduMap.MAP_TYPE_NORMAL);
        // 开启定位图层
        mBaiduMap.setMyLocationEnabled(true);
        mBaiduMap.setOnMapLongClickListener(long_listener);
        myListener = new BDLocationListener() {
            @Override
            public void onReceiveLocation(BDLocation bdLocation) {
                Log.d("test", bdLocation.getLongitude() + "");
                //定义Maker坐标点
                point = new LatLng(bdLocation.getLatitude(), bdLocation.getLongitude());
                //构建Marker图标
                BitmapDescriptor bitmap = BitmapDescriptorFactory
                        .fromResource(R.drawable.location4);
                OverlayOptions option;
                Marker marker=null;
                if (TimeUp) {
                    //构建MarkerOption，用于在地图上添加Marker
                    option= new MarkerOptions()
                            .position(point)
                            .icon(bitmap);
                    //在地图上添加Marker，并显示
                    marker = (Marker) (mBaiduMap.addOverlay(option));
                    //定义地图状态
                    MapStatus mMapStatus = new MapStatus.Builder()
                            .target(point)
                            .zoom(18)
                            .build();
                    //定义MapStatusUpdate对象，以便描述地图状态将要发生的变化


                    MapStatusUpdate mMapStatusUpdate = MapStatusUpdateFactory.newMapStatus(mMapStatus);
                    //改变地图状态
                    mBaiduMap.setMapStatus(mMapStatusUpdate);
                    TimeUp=false;
                }else {
                    marker.setPosition(point);
                }

            }
        };
        mLocationClient = new LocationClient(getApplicationContext());     //声明LocationClient类
        mLocationClient.registerLocationListener(myListener);    //注册监听函数
        initLocation();
        mLocationClient.start();
    }

    private void initLocation(){
        LocationClientOption option = new LocationClientOption();
        option.setLocationMode(LocationClientOption.LocationMode.Hight_Accuracy
        );//可选，默认高精度，设置定位模式，高精度，低功耗，仅设备
        option.setCoorType("bd09ll");//可选，默认gcj02，设置返回的定位结果坐标系
        int span=1000;
        option.setScanSpan(span);//可选，默认0，即仅定位一次，设置发起定位请求的间隔需要大于等于1000ms才是有效的
        option.setIsNeedAddress(true);//可选，设置是否需要地址信息，默认不需要
        option.setOpenGps(true);//可选，默认false,设置是否使用gps
        mLocationClient.setLocOption(option);
    }
    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.menu_main, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        // Handle action bar item clicks here. The action bar will
        // automatically handle clicks on the Home/Up button, so long
        // as you specify a parent activity in AndroidManifest.xml.
        int id = item.getItemId();

        //noinspection SimplifiableIfStatement
        if (id == R.id.action_settings) {
            return true;
        }

        return super.onOptionsItemSelected(item);
    }

    @Override
    public boolean onLongClick(View v) {
        if (v.getId()==R.id.fab){
            dialog_1();
        }
        return false;
    }
    public class NotifyLister extends BDNotifyListener {
        public void onNotify(BDLocation mlocation, float distance){
            //取消位置提醒
            mLocationClient.removeNotifyEvent(mNotifyer);
            Toast.makeText(MainActivity.this, "你已经到达当前位置", Toast.LENGTH_SHORT).show();//显示提示
            Uri notification = RingtoneManager.getDefaultUri(RingtoneManager.TYPE_ALARM);
            r = RingtoneManager.getRingtone(MainActivity.this, notification);
            r.play();
            dialog_2();
            LatLng p1= new LatLng(mlocation.getLatitude(),mlocation.getLongitude());
            for (int i=0;i<markerList.size();i++){
                Marker thisMark= markerList.get(i);
                Log.d("test",markerList.size()+"     size");
                Log.d("test","i......"+i);
                LatLng p2=thisMark.getPosition();
                Log.d("test","distance....."+distance);
                Log.d("test","otherdistance....."+DistanceUtil. getDistance(p1, p2));
                if ( DistanceUtil. getDistance(p1, p2)<=3000){
                    thisMark.remove();
                    markerList.remove(i);
                    i=i-1;
                    this_date=p2.latitude+p2.longitude+"";
                    Log.d("test1","la..."+p2.latitude+"lo......."+p2.longitude);
                }

            }


        }
    }
    private void dialog_1(){

        //dialog参数设置
        AlertDialog.Builder builder=new AlertDialog.Builder(this);  //先得到构造器
        //    通过LayoutInflater来加载一个xml的布局文件作为一个View对象
        final View view = LayoutInflater.from(MainActivity.this).inflate(R.layout.dialog_2, null);
        //    设置我们自己定义的布局文件作为弹出框的Content
        builder.setView(view);

        builder.setPositiveButton("确定", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                Toast.makeText(MainActivity.this, "确认" + which, Toast.LENGTH_SHORT).show();
                //位置提醒相关代码
                mNotifyer = new NotifyLister();
                mNotifyer.SetNotifyLocation(point.latitude,point.longitude,3000,"bd09ll");//4个参数代表要位置提醒的点的坐标，具体含义依次为：纬度，经度，距离范围，坐标系类型(gcj02,gps,bd09,bd09ll)
                mLocationClient.registerNotify(mNotifyer);
                //注册位置提醒监听事件后，可以通过SetNotifyLocation 来修改位置提醒设置，修改后立刻生效。
                //BDNotifyListner实现
                final EditText editContent= (EditText)view.findViewById(R.id.content1);

                in_content=editContent.getText().toString();
                SharedPreferences.Editor editor=getSharedPreferences("1", Context.MODE_PRIVATE).edit();
                editor.putString("content", in_content);
                editor.putString("time","2016-6-1");
                editor.commit();

            }
        });
        builder.setNegativeButton("取消", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {


            }
        });
        builder.create().show();
    }

    private void dialog_2(){
        //先new出一个监听器，设置好监听
        DialogInterface.OnClickListener dialogOnclicListener=new DialogInterface.OnClickListener(){

            @Override
            public void onClick(DialogInterface dialog, int which) {
                switch(which){
                    case Dialog.BUTTON_POSITIVE:
                        dialog_3();
                        r.stop();
                        break;
                    case Dialog.BUTTON_NEGATIVE:
                        r.stop();
                        break;
                }
            }
        };
        //dialog参数设置
        AlertDialog.Builder builder=new AlertDialog.Builder(this);  //先得到构造器
        builder.setTitle("提示"); //设置标题
        builder.setMessage("你到了你设定的提醒地点范围内"); //设置内容
        builder.setIcon(R.drawable.location2);//设置图标，图片id即可
        builder.setPositiveButton("查看", dialogOnclicListener);
        builder.setNegativeButton("取消", dialogOnclicListener);
        builder.create().show();

    }

    public void dialog_3(){
        //弹出框
        AlertDialog.Builder builder = new AlertDialog.Builder(MainActivity.this);
        //    通过LayoutInflater来加载一个xml的布局文件作为一个View对象
        View view = LayoutInflater.from(MainActivity.this).inflate(R.layout.dialog, null);
        //    设置我们自己定义的布局文件作为弹出框的Content
        builder.setView(view);
        Log.d("test1",this_date);
        SharedPreferences pref=getSharedPreferences(this_date, MODE_PRIVATE);
        TextView textTime = (TextView)view.findViewById(R.id.time);
        TextView textContent = (TextView)view.findViewById(R.id.content);
        textTime.setText(pref.getString("time", ""));
        textContent.setText(pref.getString("content", ""));

        builder.setPositiveButton("确定", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {


            }
        });
        builder.show();
    }

    private void dialog_4(){

        //dialog参数设置
        AlertDialog.Builder builder=new AlertDialog.Builder(this);  //先得到构造器
        //    通过LayoutInflater来加载一个xml的布局文件作为一个View对象
        final View view = LayoutInflater.from(MainActivity.this).inflate(R.layout.dialog_2, null);
        //    设置我们自己定义的布局文件作为弹出框的Content
        builder.setView(view);

        builder.setPositiveButton("确定", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                //位置提醒相关代码
                mNotifyer = new NotifyLister();
                mNotifyer.SetNotifyLocation(longTouchedPoint.latitude, longTouchedPoint.longitude, 3000, "bd09ll");//4个参数代表要位置提醒的点的坐标，具体含义依次为：纬度，经度，距离范围，坐标系类型(gcj02,gps,bd09,bd09ll)
                mLocationClient.registerNotify(mNotifyer);
                //注册位置提醒监听事件后，可以通过SetNotifyLocation 来修改位置提醒设置，修改后立刻生效。
                //BDNotifyListner实现
                final EditText editContent = (EditText) view.findViewById(R.id.content1);
                SimpleDateFormat sDateFormat = new SimpleDateFormat("yyyy-MM-dd hh:mm:ss");
                String date = sDateFormat.format(new java.util.Date());
                in_content = editContent.getText().toString();
                SharedPreferences.Editor editor = getSharedPreferences(longTouchedPoint.latitude+longTouchedPoint.longitude+"", Context.MODE_PRIVATE).edit();
                Log.d("test1","..."+longTouchedPoint.latitude+longTouchedPoint.longitude+"");
                editor.putString("content", in_content);
                editor.putString("time", date);
                editor.commit();
                markerList.add(long_marker);

            }
        });
        builder.setNegativeButton("取消", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                long_marker.remove();

            }
        });
        builder.create().show();
    }
    //地图长按点击事件
    BaiduMap.OnMapLongClickListener long_listener = new BaiduMap.OnMapLongClickListener() {
        /**
         * 地图长按事件监听回调函数
         * @param point 长按的地理坐标
         */
        public void onMapLongClick(LatLng point){
            longTouchedPoint=point;
            //构建Marker图标
            BitmapDescriptor bitmap = BitmapDescriptorFactory
                    .fromResource(R.drawable.point);
            OverlayOptions option;
                //构建MarkerOption，用于在地图上添加Marker
                option= new MarkerOptions()
                        .position(point)
                        .icon(bitmap);
                //在地图上添加Marker，并显示
            long_marker= (Marker) mBaiduMap.addOverlay(option);
            dialog_4();

        }
    };

}
