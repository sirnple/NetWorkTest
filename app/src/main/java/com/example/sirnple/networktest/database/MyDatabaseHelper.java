package com.example.sirnple.networktest.database;

import android.content.Context;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;
import android.widget.Toast;

public class MyDatabaseHelper extends SQLiteOpenHelper {
    private  static MyDatabaseHelper instance;
    public static final String CREATE_NETWORK = "create table NetWorkState("
            + "location integer primary key autoincrement,"
            + "latitude DECIMAL(9,6),"
            + "longtitude DECIMAL(9,6),"
            + "sim_state text,"
            + "network_type text,"
            + "phone_type text,"
            + "network_operator text,"
            + "signalstrength integer," //信号强度dbm
            + "time integer)";
    private Context mContext;

    private MyDatabaseHelper(Context context, String name, SQLiteDatabase.CursorFactory cursorFactory, int version){
        super(context, name, cursorFactory, version);
        mContext = context;
    }

    @Override
    public void onCreate(SQLiteDatabase db) {
        db.execSQL(CREATE_NETWORK);
        Toast.makeText(mContext, "Create Database succeeded", Toast.LENGTH_SHORT).show();
    }

    @Override
    public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {
        db.execSQL("drop table if exists NetWorkState");
        onCreate(db);
    }

    public synchronized static MyDatabaseHelper getInstance(Context context, String name, SQLiteDatabase.CursorFactory cursorFactory, int version){
        if(instance == null){
            instance = new MyDatabaseHelper(context, name, cursorFactory, version);
        }
        return instance;
    }
}
