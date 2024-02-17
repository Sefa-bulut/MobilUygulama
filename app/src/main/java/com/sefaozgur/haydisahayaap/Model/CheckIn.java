package com.sefaozgur.haydisahayaap.Model;

public class CheckIn {
    //attributes
    private String id;
    private String name;
    private String phone;
    private String position;
    private long date; //appointment date
    private double latitude;
    private double longitude;

    public CheckIn() {
    }

    public CheckIn(String id, String name, String phone, String position, long date, double latitude, double longitude) {
        this.id = id;
        this.name = name;
        this.phone = phone;
        this.position = position;
        this.date = date;
        this.latitude = latitude;
        this.longitude = longitude;
    }

    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getPhone() {
        return phone;
    }

    public void setPhone(String phone) {
        this.phone = phone;
    }

    public String getPosition() {
        return position;
    }

    public void setPosition(String position) {
        this.position = position;
    }

    public long getDate() {
        return date;
    }

    public void setDate(long date) {
        this.date = date;
    }

    public double getLatitude() {
        return latitude;
    }

    public void setLatitude(double latitude) {
        this.latitude = latitude;
    }

    public double getLongitude() {
        return longitude;
    }

    public void setLongitude(double longitude) {
        this.longitude = longitude;
    }
}
