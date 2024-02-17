package com.sefaozgur.haydisahayaap.Model;

public class Rating {
    //attributes
    private String userID;
    private String rateValue;
    //constructors
    public Rating() {
    }
    public Rating(String userID, String rateValue) {
        this.userID = userID;
        this.rateValue = rateValue;
    }
    //getter and setter
    public String getUserID() {
        return userID;
    }

    public void setUserID(String userID) {
        this.userID = userID;
    }

    public String getRateValue() {
        return rateValue;
    }

    public void setRateValue(String rateValue) {
        this.rateValue = rateValue;
    }
}
