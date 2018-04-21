package com.example.sirnple.networktest.activity;

import android.content.ContentValues;
import android.content.DialogInterface;
import android.content.Intent;
import android.database.sqlite.SQLiteDatabase;
import android.os.Handler;
import android.provider.Settings;
import android.support.design.widget.FloatingActionButton;
import android.Manifest;
import android.content.Context;
import android.content.pm.PackageManager;
import android.support.annotation.NonNull;
import android.support.design.widget.NavigationView;
import android.support.v4.app.ActivityCompat;
import android.support.v4.content.ContextCompat;
import android.support.v4.view.GravityCompat;
import android.support.v4.widget.DrawerLayout;
import android.support.v7.app.ActionBarDrawerToggle;
import android.support.v7.app.AlertDialog;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.support.v7.widget.Toolbar;
import android.telephony.PhoneStateListener;
import android.telephony.ServiceState;
import android.telephony.SignalStrength;
import android.telephony.TelephonyManager;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;

import com.baidu.location.BDLocation;
import com.baidu.location.BDLocationListener;
import com.baidu.location.LocationClient;
import com.baidu.location.LocationClientOption;
import com.baidu.mapapi.SDKInitializer;
import com.baidu.mapapi.map.BaiduMap;
import com.baidu.mapapi.map.MapStatusUpdate;
import com.baidu.mapapi.map.MapStatusUpdateFactory;
import com.baidu.mapapi.map.MapView;
import com.baidu.mapapi.map.MyLocationData;
import com.baidu.mapapi.model.LatLng;
import com.example.sirnple.networktest.NetWorkInfo.NetWorkInfo;
import com.example.sirnple.networktest.R;
import com.example.sirnple.networktest.database.MyDatabaseHelper;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;

public class MainActivity extends AppCompatActivity
        implements NavigationView.OnNavigationItemSelectedListener {

    //SIM卡状态常量
    private static final String SIM_ABSENT = "Absent"; //手机内无SIM卡
    private static final String SIM_READY = "Ready"; //SIM卡已准备好
    private static final String SIM_PIN_REQUIRED = "PIN required"; //需要SIM卡的PIN解锁
    private static final String SIM_PUK_REQUIRED = "PUK required"; //需要SIM卡的PUK解锁
    private static final String SIM_NETWORK_LOCKED = "Network locked"; //需要Network PIN解锁
    private static final String SIM_UNKNOWN = "Unknown"; //状态未知

    //网络类型常量
    private static final String NETWORK_CDMA = "CDMA: Either IS95A or IS95B (2G)";
    private static final String NETWORK_EDGE = "EDGE (2.75G)";
    private static final String NETWORK_GPRS = "GPRS (2.5G)";
    private static final String NETWORK_UMTS = "UMTS (3G)";
    private static final String NETWORK_EVDO_0 = "EVDO revision 0 (3G)";
    private static final String NETWORK_EVDO_A = "EVDO revision A (3G - Transitional)";
    private static final String NETWORK_EVDO_B = "EVDO revision B (3G - Transitional)";
    private static final String NETWORK_1X_RTT = "1xRTT  (2G - Transitional)";
    private static final String NETWORK_HSDPA = "HSDPA (3G - Transitional)";
    private static final String NETWORK_HSUPA = "HSUPA (3G - Transitional)";
    private static final String NETWORK_HSPA = "HSPA (3G - Transitional)";
    private static final String NETWORK_IDEN = "iDen (2G)";
    private static final String NETWORK_UNKOWN = "Unknown";

    //手机制式类型常量
    private static final String PHONE_CDMA = "CDMA";
    private static final String PHONE_GSM = "GSM";
    private static final String PHONE_NONE = "No radio";

    private TextView view, gpsView;
    private MapView mapView;
    private BaiduMap baiduMap;
    private boolean isFirstLocate = true;
    public TelephonyManager telephonyManager;
    public PhoneStateListener phoneStateListener;
    public LocationClient locationClient;

    private NetWorkInfo netWorkInfo = NetWorkInfo.getInstance();

    private MyDatabaseHelper myDatabaseHelper;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        //获取telephonyManager
        telephonyManager = (TelephonyManager) getSystemService(Context.TELEPHONY_SERVICE);

        //百度地图

        locationClient = new LocationClient(getApplicationContext());//获取locationClient
        locationClient.registerLocationListener(new MyLocationListener());//坐标监听

        SDKInitializer.initialize(getApplicationContext());//location


        setContentView(R.layout.activity_main);



        Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);

        DrawerLayout drawer = (DrawerLayout) findViewById(R.id.drawer_layout);
        ActionBarDrawerToggle toggle = new ActionBarDrawerToggle(
                this, drawer, toolbar, R.string.navigation_drawer_open, R.string.navigation_drawer_close);
        drawer.addDrawerListener(toggle);
        toggle.syncState();

        NavigationView navigationView = (NavigationView) findViewById(R.id.nav_view);
        navigationView.setNavigationItemSelectedListener(this);

        mapView = (MapView) findViewById(R.id.bmapView);
        baiduMap = mapView.getMap();
        baiduMap.setMyLocationEnabled(true);

        //右下角悬浮按钮
        final FloatingActionButton fab = (FloatingActionButton) findViewById(R.id.fab);
        fab.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                //悬浮按钮点击采集数据
                AlertDialog.Builder dialog = new AlertDialog.Builder(MainActivity.this);
                dialog.setTitle("确定采集？");
                dialog.setCancelable(false);
                dialog.setPositiveButton("确定", new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        //创建数据库
                        myDatabaseHelper = MyDatabaseHelper.getInstance(MainActivity.this, "NetWork_State.db", null, 1);
                        SQLiteDatabase db = myDatabaseHelper.getWritableDatabase();
                        //存储数据
                        ContentValues contentValues = new ContentValues();
                        //组装sql
                        contentValues.put("latitude", netWorkInfo.latitude);
                        contentValues.put("longtitude", netWorkInfo.longtitude);
                        contentValues.put("sim_state", netWorkInfo.sim_state);
                        contentValues.put("network_type", netWorkInfo.network_type);
                        contentValues.put("network_operator", netWorkInfo.network_operator);
                        contentValues.put("phone_type", netWorkInfo.phone_type);
                        contentValues.put("signalstrength", netWorkInfo.mSignalstrength);
                        contentValues.put("time", netWorkInfo.time);
                        db.insert("NetWorkState", null, contentValues);//插入数据
                        Toast.makeText(MainActivity.this, "采集成功", Toast.LENGTH_SHORT).show();
                        contentValues.clear();

                    }
                });
                dialog.setNegativeButton("取消", new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        //取消存储
                    }
                });
                dialog.show();
            }
        });

        //自动采集按钮
        FloatingActionButton fab2 = (FloatingActionButton) findViewById(R.id.fab2);
        fab2.setOnClickListener(new View.OnClickListener() {

            Handler handler = new Handler();
            Runnable runnable = new Runnable() {
                @Override
                public void run() {
                    //创建数据库
                    myDatabaseHelper = MyDatabaseHelper.getInstance(MainActivity.this, "NetWork_State.db", null, 1);
                    SQLiteDatabase db = myDatabaseHelper.getWritableDatabase();
                    //存储数据
                    ContentValues contentValues = new ContentValues();
                    //组装sql
                    contentValues.put("latitude", netWorkInfo.latitude);
                    contentValues.put("longtitude", netWorkInfo.longtitude);
                    contentValues.put("sim_state", netWorkInfo.sim_state);
                    contentValues.put("network_type", netWorkInfo.network_type);
                    contentValues.put("network_operator", netWorkInfo.network_operator);
                    contentValues.put("phone_type", netWorkInfo.phone_type);
                    contentValues.put("signalstrength", netWorkInfo.mSignalstrength);
                    contentValues.put("time", netWorkInfo.time);
                    db.insert("NetWorkState", null, contentValues);//插入数据
                    Toast.makeText(MainActivity.this, "采集成功", Toast.LENGTH_SHORT).show();
                    contentValues.clear();
                    handler.postDelayed(this, inputTime*1000);
                }
            };
            boolean flag = false;
            int inputTime;
            @Override
            public void onClick(View view) {
                if(!flag) {
                    flag = true;
                    //悬浮按钮点击采集数据
                    AlertDialog.Builder dialog = new AlertDialog.Builder(MainActivity.this);
                    dialog.setTitle("自动采集");
                    dialog.setMessage("请输入采集间隔(单位：s)");
                    final EditText editText = new EditText(MainActivity.this);
                    dialog.setView(editText);
                    dialog.setCancelable(false);
                    dialog.setPositiveButton("确定", new DialogInterface.OnClickListener() {
                        @Override
                        public void onClick(DialogInterface dialog, int which) {

                            //得到输入的时间间隔
                            String input = editText.getText().toString();
                            inputTime = Integer.parseInt(input);
                            handler.postDelayed(runnable, 1000);
                        }
                    });
                    dialog.setNegativeButton("取消", new DialogInterface.OnClickListener() {
                        @Override
                        public void onClick(DialogInterface dialog, int which) {
                            //取消存储
                        }
                    });
                    dialog.show();
                }else {
                    flag = false;
                    handler.removeCallbacks(runnable);
                }

            }
        });

        //定位按钮
        FloatingActionButton fab1 = (FloatingActionButton) findViewById(R.id.fab1);
        fab1.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                locationClient.requestLocation();
            }
        });
//        dw.setOnClickListener(new View.OnClickListener() {
//            @Override
//            public void onClick(View v) {
//                baiduMap.setMyLocationEnabled(true);
//                locationClient.start();
//            }
//        });


        updateView(telephonyManager);
        updateTime();
        updateGps(locationClient);
        //获取phoneStateListener
        phoneStateListener = new PhoneStateListener(){
            @Override
            public void onSignalStrengthsChanged(SignalStrength signalStrength) {
                super.onSignalStrengthsChanged(signalStrength);
                //反射获取getDbm方法得到信号强度
                try {
                    view = (TextView) findViewById(R.id.signal_strength);
                    netWorkInfo.mSignalstrength = (int)signalStrength.getClass().getMethod("getDbm").invoke(signalStrength);
                    view.setText("Signal Strength:" + netWorkInfo.mSignalstrength + "dBm");
                }catch (Exception e){
                    e.printStackTrace();
                }
            }

            @Override
            public void onDataConnectionStateChanged(int state, int networkType) {
                super.onDataConnectionStateChanged(state, networkType);
                updateView(telephonyManager);
            }

            @Override
            public void onServiceStateChanged(ServiceState serviceState) {
                super.onServiceStateChanged(serviceState);
                updateView(telephonyManager);
            }
        };

        //监听信号
        telephonyManager.listen(phoneStateListener, PhoneStateListener.LISTEN_DATA_CONNECTION_STATE|PhoneStateListener.LISTEN_SERVICE_STATE|PhoneStateListener.LISTEN_SIGNAL_STRENGTHS);
    }



    @Override
    public void onBackPressed() {
        DrawerLayout drawer = (DrawerLayout) findViewById(R.id.drawer_layout);
        if (drawer.isDrawerOpen(GravityCompat.START)) {
            drawer.closeDrawer(GravityCompat.START);
        } else {
            super.onBackPressed();
        }
    }

    //导航界面逻辑
    @SuppressWarnings("StatementWithEmptyBody")
    @Override
    public boolean onNavigationItemSelected(MenuItem item) {
        // Handle navigation view item clicks here.
        int id = item.getItemId();

        if (id == R.id.document) {
            // Handle the camera action
            Intent intent = new Intent(MainActivity.this, FileExploreActivity.class);
            startActivity(intent);
        } else if (id == R.id.home) {
            Intent intent = new Intent(MainActivity.this, MainActivity.class);
            startActivity(intent);
        }else if (id == R.id.speedtest){

        }

        DrawerLayout drawer = (DrawerLayout) findViewById(R.id.drawer_layout);
        drawer.closeDrawer(GravityCompat.START);
        return true;
    }

    //toolbar逻辑
    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.toolbar, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()){
            case R.id.networkchange:
                //跳转到网络切换界面
                Intent intent = new Intent(Settings.ACTION_DATA_ROAMING_SETTINGS);
                startActivity(intent);
            default:
                break;
        }
        return true;
    }

    //用户切换活动时，调用
    @Override
    protected void onResume() {
        super.onResume();
        mapView.onResume();
        telephonyManager.listen(phoneStateListener, PhoneStateListener.LISTEN_DATA_CONNECTION_STATE|PhoneStateListener.LISTEN_SERVICE_STATE|PhoneStateListener.LISTEN_SIGNAL_STRENGTHS);
        updateView(telephonyManager);
        updateGps(locationClient);
    }

    @Override
    protected void onPause() {
        super.onPause();
        mapView.onPause();
        telephonyManager.listen(phoneStateListener, PhoneStateListener.LISTEN_NONE);
        updateView(telephonyManager);
        updateGps(locationClient);
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        mapView.onDestroy();
        locationClient.stop();
        baiduMap.setMyLocationEnabled(false);
    }

    //updateView
    private final void updateView(TelephonyManager telephoneManager){

        view = (TextView) findViewById(R.id.sim_state);
        netWorkInfo.sim_state = mapSimStateToName(telephoneManager.getSimState());
        view.setText("SIM State:" + netWorkInfo.sim_state);

        view = (TextView) findViewById(R.id.network_type);
        netWorkInfo.network_type = mapNetworkTypeToName(telephoneManager.getNetworkType());
        view.setText("NetWork Type:" + netWorkInfo.network_type);

        view = (TextView) findViewById(R.id.phone_type);
        netWorkInfo.phone_type = mapPhoneTypeToName(telephoneManager.getPhoneType());
        view.setText("Phone Type:" + netWorkInfo.phone_type);

        view = (TextView) findViewById(R.id.network_operator);
        netWorkInfo.network_operator = telephoneManager.getNetworkOperator();
        view.setText("NetWork Operator:" + netWorkInfo.network_operator);
    }
    //update time
    private final void updateTime(){
        SimpleDateFormat format = new SimpleDateFormat("yyyyMMddHHmmss");
        Date date = new Date(System.currentTimeMillis());
        netWorkInfo.time = format.format(date);
    }

    /**
     * 将手机制式值以字符串形式返回
     * @param phoneType
     * @return
     */
    private String mapPhoneTypeToName(int phoneType) {
        switch (phoneType) {
            case TelephonyManager.PHONE_TYPE_CDMA:
                return PHONE_CDMA;
            case TelephonyManager.PHONE_TYPE_GSM:
                return PHONE_GSM;
            case TelephonyManager.PHONE_TYPE_NONE:
                return PHONE_NONE;
            default:
                //不应该走到这个分支
                return null;
        }
    }

    /**
     * 将网络类型值以字符串形式返回
     * @param networkType
     * @return
     */
    private String mapNetworkTypeToName(int networkType) {
        switch (networkType) {
            case TelephonyManager.NETWORK_TYPE_CDMA:
                return NETWORK_CDMA;
            case TelephonyManager.NETWORK_TYPE_EDGE:
                return NETWORK_EDGE;
            case TelephonyManager.NETWORK_TYPE_GPRS:
                return NETWORK_GPRS;
            case TelephonyManager.NETWORK_TYPE_UMTS:
                return NETWORK_UMTS;
            case TelephonyManager.NETWORK_TYPE_EVDO_0:
                return NETWORK_EVDO_0;
            case TelephonyManager.NETWORK_TYPE_EVDO_A:
                return NETWORK_EVDO_A;
            case TelephonyManager.NETWORK_TYPE_EVDO_B:
                return NETWORK_EVDO_B;
            case TelephonyManager.NETWORK_TYPE_1xRTT:
                return NETWORK_1X_RTT;
            case TelephonyManager.NETWORK_TYPE_HSDPA:
                return NETWORK_HSDPA;
            case TelephonyManager.NETWORK_TYPE_HSPA:
                return NETWORK_HSPA;
            case TelephonyManager.NETWORK_TYPE_HSUPA:
                return NETWORK_HSUPA;
            case TelephonyManager.NETWORK_TYPE_IDEN:
                return NETWORK_IDEN;
            case TelephonyManager.NETWORK_TYPE_UNKNOWN:
            default:
                return NETWORK_UNKOWN;
        }
    }

    /**
     * 将SIM卡状态值以字符串形式返回
     * @param simState
     * @return
     */
    private static String mapSimStateToName(int simState) {
        switch (simState) {
            case TelephonyManager.SIM_STATE_ABSENT:
                return SIM_ABSENT;
            case TelephonyManager.SIM_STATE_READY:
                return SIM_READY;
            case TelephonyManager.SIM_STATE_PIN_REQUIRED:
                return SIM_PIN_REQUIRED;
            case TelephonyManager.SIM_STATE_PUK_REQUIRED:
                return SIM_PUK_REQUIRED;
            case TelephonyManager.SIM_STATE_NETWORK_LOCKED:
                return SIM_NETWORK_LOCKED;
            case TelephonyManager.SIM_STATE_UNKNOWN:
                return SIM_UNKNOWN;
            default:
                //不应该走到这个分支
                return null;
        }
    }


    //GPS部分

    //跑GPS线程
    private void requestLocation(){
        initLocation();
        locationClient.start();
    }

    private void initLocation(){
        LocationClientOption locationClientOption = new LocationClientOption();
        locationClientOption.setScanSpan(5000);
        locationClientOption.setLocationMode(LocationClientOption.LocationMode.Hight_Accuracy);
        locationClientOption.setIsNeedAddress(true);
        locationClient.setLocOption(locationClientOption);
    }



    //updategps view
    private final void updateGps(LocationClient locationClient){
        gpsView = (TextView) findViewById(R.id.gps);
        //权限表
        List<String> permissionList = new ArrayList<>();
        if(ContextCompat.checkSelfPermission(MainActivity.this, Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED){
            permissionList.add(Manifest.permission.ACCESS_FINE_LOCATION);
        }
        if(ContextCompat.checkSelfPermission(MainActivity.this, Manifest.permission.READ_PHONE_STATE) != PackageManager.PERMISSION_GRANTED){
            permissionList.add(Manifest.permission.READ_PHONE_STATE);
        }
        if(ContextCompat.checkSelfPermission(MainActivity.this, Manifest.permission.WRITE_EXTERNAL_STORAGE) != PackageManager.PERMISSION_GRANTED){
            permissionList.add(Manifest.permission.WRITE_EXTERNAL_STORAGE);
        }
        if(!permissionList.isEmpty()){
            String[] permissions = permissionList.toArray(new String[permissionList.size()]);
            ActivityCompat.requestPermissions(MainActivity.this, permissions, 1);
        }else {
            requestLocation();//满足权限，则获取location
        }
    }

    //权限审核
    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        switch (requestCode){
            case 1:
                if(grantResults.length > 0){
                    for(int result : grantResults){
                        if(result != PackageManager.PERMISSION_GRANTED){
                            Toast.makeText(this, "必须同意所有权限才能使用本程序", Toast.LENGTH_SHORT).show();
                            finish();
                            return;
                        }
                    }
                    requestLocation();
                }else {
                    Toast.makeText(this, "发生未知错误", Toast.LENGTH_SHORT).show();
                    finish();
                }
                break;
                default:
        }
    }

    //GPS监听类
    public class MyLocationListener implements BDLocationListener{
        @Override
        public void onReceiveLocation(final BDLocation bdLocation) {
            if(bdLocation.getLocType() == BDLocation.TypeGpsLocation||bdLocation.getLocType() == BDLocation.TypeNetWorkLocation){
                navigateTo(bdLocation);
            }
            runOnUiThread(new Runnable(){
                @Override
                public void run() {
                    StringBuilder currentPosition = new StringBuilder();
                    netWorkInfo.latitude = bdLocation.getLatitude();
                    netWorkInfo.longtitude = bdLocation.getLongitude();
                    netWorkInfo.country = bdLocation.getCountry();
                    netWorkInfo.province = bdLocation.getProvince();
                    netWorkInfo.city = bdLocation.getCity();
                    netWorkInfo.district = bdLocation.getDistrict();
                    netWorkInfo.street = bdLocation.getStreet();
                    currentPosition.append("维度:").append(netWorkInfo.latitude).append("\n");
                    currentPosition.append("经度:").append(netWorkInfo.longtitude).append("\n");
                    currentPosition.append("国家：").append(netWorkInfo.country).append("\n");
                    currentPosition.append("省：").append(netWorkInfo.province).append("\n");
                    currentPosition.append("市：").append(netWorkInfo.city).append("\n");
                    currentPosition.append("区：").append(netWorkInfo.district).append("\n");
                    currentPosition.append("街道：").append(netWorkInfo.street).append("\n");
                    currentPosition.append("定位方式:");
                    if(bdLocation.getLocType() == BDLocation.TypeGpsLocation){
                        currentPosition.append("GPS");
                        netWorkInfo.locateVia = "GPS";
                    }else if(bdLocation.getLocType() == BDLocation.TypeNetWorkLocation){
                        currentPosition.append("网络");
                        netWorkInfo.locateVia = "网络";
                    }
                    gpsView.setText(currentPosition);
                }
            });
        }
    }

    private void navigateTo(BDLocation location){
        if(isFirstLocate){
            LatLng ll = new LatLng(location.getLatitude(), location.getLongitude());
            MapStatusUpdate update = MapStatusUpdateFactory.newLatLng(ll);
            baiduMap.animateMapStatus(update);
            update = MapStatusUpdateFactory.zoomTo(16f);
            baiduMap.animateMapStatus(update);
            isFirstLocate = false;
        }
        MyLocationData.Builder locationBuilder = new MyLocationData.Builder();
        locationBuilder.latitude(location.getLatitude());
        locationBuilder.longitude(location.getLongitude());
        MyLocationData locationData = locationBuilder.build();
        baiduMap.setMyLocationData(locationData);
    }
}

