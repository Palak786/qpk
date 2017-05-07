package com.qpk;

/**
 * Created by neha on 4/1/2017.
 */


public class Transaction {
    private String transid, date, time,vehivleno,location;

    public Transaction() {
    }

    public Transaction(String transid, String date, String time, String vehivleno, String location) {
        this.transid = transid;
        this.date = date;
        this.time = time;
        this.vehivleno = vehivleno;
        this.location = location;

    }

    public String getTransactionid() {
        return transid;
    }

    public void setTransactionid(String name) {
        this.transid = name;
    }

    public String getDate() {
        return date;
    }

    public void setDate(String date) {
        this.date = date;
    }

    public String getTime() {
        return time;
    }

    public void setTime(String time) {
        this.time = time;
    }

    public String getVehivleno() {
        return date;
    }

    public void setVehivleno(String vehivleno) {
        this.vehivleno = vehivleno;
    }
    public String getLocation() {
        return location;
    }

    public void setLocation(String location) {
        this.location = location;
    }

}
