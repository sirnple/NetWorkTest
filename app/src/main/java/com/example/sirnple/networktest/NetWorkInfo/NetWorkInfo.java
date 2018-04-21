package com.example.sirnple.networktest.NetWorkInfo;

public class NetWorkInfo {
    private static NetWorkInfo instance;

    public String sim_state, network_type, network_operator, phone_type;//网络状态
    public int mSignalstrength;//信号强度dBm
    public double latitude, longtitude;//经纬度
    public String country, province, city, district, street;//地理位置
    public String locateVia;//定位方式
    public String time;//时间

    private NetWorkInfo(){

    }

    public synchronized static NetWorkInfo getInstance() {
        if(instance == null){
            instance = new NetWorkInfo();
        }
        return instance;
    }

    public void setLatitude(double latitude){
        this.latitude = latitude;
    }
    public double getLatitude() {
        return latitude;
    }

    public void setLongtitude(double longtitude){
        this.longtitude = longtitude;
    }
    public double getLongtitude() {
        return longtitude;
    }

    public void setmSignalstrength(int mSignalstrength){
        this.mSignalstrength = mSignalstrength;
    }
    public int getmSignalstrength() {
        return mSignalstrength;
    }

    public void setNetwork_type(String network_type){
        this.network_type = network_type;
    }
    public String getNetwork_type() {
        return network_type;
    }

    public void setTime(String time){ this.time = time; }
    public String getTime(){ return time; }
}
