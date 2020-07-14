package com.example.jayde.a4ease;

import java.util.ArrayList;

public class User {
    private String userRole;
    private String password;
    private String cnic;
    private String name;
    private String phNo;
    private Double lat;
    private Double lng;
    private String officeTitle;
    private String status;
    private ArrayList<Car> carsArrayList;

    public void setStatus(String status) {
        this.status = status;
    }

    public String getStatus() {
        return status;
    }

    public void setCarsArrayList(ArrayList<Car> carsArrayList) {
        this.carsArrayList = carsArrayList;
    }

    public ArrayList<Car> getCarsArrayList() {
        return carsArrayList;
    }

    public String getCnic() {
        return cnic;
    }

    public void setCnic(String cnic) {
        this.cnic = cnic;
    }

    public Double getLat() {
        return lat;
    }

    public Double  getLng() {
        return lng;
    }

    public String  getPhNo() {
        return phNo;
    }

    public String getName() {
        return name;
    }

    public String getUserRole() {
        return userRole;
    }

    public void setLat(Double lat) {
        this.lat = lat;
    }

    public void setLng(Double lng) {
        this.lng = lng;
    }

    public void setName(String name) {
        this.name = name;
    }

    public void setPhNo(String phNo) {
        this.phNo = phNo;
    }

    public void setUserRole(String userRole) {
        this.userRole = userRole;
    }

    public String getPassword() {
        return password;
    }

    public void setPassword(String password) {
        this.password = password;
    }

    public String getOfficeTitle() {
        return officeTitle;
    }

    public void setOfficeTitle(String officeTitle) {
        this.officeTitle = officeTitle;
    }
}

