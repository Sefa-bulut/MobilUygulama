package com.sefaozgur.haydisahayaap.Model;

public class Users {

    //attributes
    private String id;
    private String username;
    private String imageURL;
    private String userPhone;
    private String status;
    //one signal
    private String token;

    //empty constructors for firebase
    public Users() {
        //firebase için boş constructor gerekli
    }

    //Constructors
    public Users(String id, String username, String imageURL, String userPhone, String status, String token) {
        this.id = id;
        this.username = username;
        this.imageURL = imageURL;
        this.userPhone = userPhone;
        this.status = status;
        this.token = token;
    }

    //getter and setter
    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }

    public String getUsername() {
        return username;
    }

    public void setUsername(String username) {
        this.username = username;
    }

    public String getImageURL() {
        return imageURL;
    }

    public void setImageURL(String imageURL) {
        this.imageURL = imageURL;
    }

    public String getUserPhone() {
        return userPhone;
    }

    public void setUserPhone(String userPhone) {
        this.userPhone = userPhone;
    }

    public String getStatus() {
        return status;
    }

    public void setStatus(String status) {
        this.status = status;
    }

    public String getToken() {
        return token;
    }

    public void setToken(String token) {
        this.token = token;
    }
}


