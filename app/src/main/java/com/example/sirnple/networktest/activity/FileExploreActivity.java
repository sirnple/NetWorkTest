package com.example.sirnple.networktest.activity;

import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.ListView;
import android.widget.TextView;

import com.example.sirnple.networktest.NetWorkInfo.NetWorkInfo;
import com.example.sirnple.networktest.R;
import com.example.sirnple.networktest.database.MyDatabaseHelper;

import java.util.ArrayList;

public class FileExploreActivity extends AppCompatActivity {

    private MyDatabaseHelper myDatabaseHelper;
    private SQLiteDatabase sqLiteDatabase;
    private ArrayList<Info> infoList;
    private ListView listView;
    private Cursor cursor;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_file_explore);
        //创建或打开数据库
        myDatabaseHelper = MyDatabaseHelper.getInstance(this, "NetWork_State.db", null, 1);
        sqLiteDatabase = myDatabaseHelper.getReadableDatabase();
        infoList = new ArrayList<>();
        //扫描数据库
//        String[] argsString = {"network_type", "signalstrength", "latitude", "longtitude"};
        try {
            cursor = sqLiteDatabase.rawQuery("select network_type,signalstrength,latitude,longtitude from NetWorkState",null);
            while(cursor.moveToNext()) {
                String networktype = cursor.getString(cursor.getColumnIndex("network_type"));

                int signalstrength = cursor.getInt(cursor.getColumnIndex("signalstrength"));

                double latitude = cursor.getDouble(cursor.getColumnIndex("latitude"));

                double longtitude = cursor.getDouble(cursor.getColumnIndex("longtitude"));

                Info info = new Info();
                info.network_type = networktype;
                info.mSignalstrength = signalstrength;
                info.latitude = latitude;
                info.longtitude = longtitude;

                infoList.add(info);
            }
            cursor.close();
        }catch (Exception e){
            e.printStackTrace();
        }






        //获取ListView，并将信息显示到ListView
        listView = (ListView) findViewById(R.id.file_explore_list_view);
        listView.setAdapter(new BaseAdapter() {
            @Override
            public int getCount() {
                return infoList.size();
            }

            @Override
            public Object getItem(int position) {
                return null;
            }

            @Override
            public long getItemId(int position) {
                return 0;
            }

            @Override
            public View getView(int position, View convertView, ViewGroup parent) {
                View view;
                if(convertView == null){
                    view = View.inflate(getBaseContext(), R.layout.networkinfo, null);
                }else {
                    view = convertView;
                }

                //逐行取数据
                Info info = infoList.get(position);
                TextView network_type = (TextView) view.findViewById(R.id.network_type);
                TextView signalstrength = (TextView) view.findViewById(R.id.signal_strength);
                TextView latitude = (TextView) view.findViewById(R.id.latitude);
                TextView longtitude = (TextView) view.findViewById(R.id.longtitude);
                network_type.setText(info.network_type);
                signalstrength.setText(String.valueOf(info.mSignalstrength));
                latitude.setText(String.valueOf(info.latitude));
                longtitude.setText(String.valueOf(info.longtitude));
                return view;
            }
        });
    }
}
class Info {
    public String sim_state, network_type, network_operator, phone_type;//网络状态
    public int mSignalstrength;//信号强度dBm
    public double latitude, longtitude;//经纬度
    public String country, province, city, district, street;//地理位置
    public String locateVia;//定位方式
    public String time;//时间
}