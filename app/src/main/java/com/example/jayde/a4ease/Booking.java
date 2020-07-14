package com.example.jayde.a4ease;

import java.util.Date;

public class Booking {

    private String vehicleId;
    private String customerId;
    private String renterId;
    private Date bookedFrom;
    private Date bookedTo;
    private String withInCity;
    private String withDriver;
    private String id;
    private int pendingCost;
    private int paidCost;
    private int cost;
    private double lat;
    private double lng;
    private String status;

    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }

    public void setStatus(String status) {
        this.status = status;
    }

    public String getStatus() {
        return status;
    }

    public int getPaidCost() {
        return paidCost;
    }

    public int getPendingCost() {
        return pendingCost;
    }

    public void setPaidCost(int paidCost) {
        this.paidCost = paidCost;
    }

    public void setPendingCost(int pendingCost) {
        this.pendingCost = pendingCost;
    }

    public void setCost(int cost) {
        this.cost = cost;
    }

    public int getCost() {
        return cost;
    }

    public String getWithDriver() {
        return withDriver;
    }

    public String getWithInCity() {
        return withInCity;
    }

    public void setWithDriver(String withDriver) {
        this.withDriver = withDriver;
    }

    public void setWithInCity(String withInCity) {
        this.withInCity = withInCity;
    }

    public double getLat() {
        return lat;
    }

    public double getLng() {
        return lng;
    }

    public String getVehicleId() {
        return vehicleId;
    }

    public Date getBookedFrom() {
        return bookedFrom;
    }

    public Date getBookedTo() {
        return bookedTo;
    }

    public String getCustomerId() {
        return customerId;
    }

    public String getRenterId() {
        return renterId;
    }

    public void setBookedTo(Date bookedTo) {
        this.bookedTo = bookedTo;
    }

    public void setBookedFrom(Date bookedFrom) {
        this.bookedFrom = bookedFrom;
    }

    public void setVehicleId(String vehicleId) {
        this.vehicleId = vehicleId;
    }

    public void setCustomerId(String customerId) {
        this.customerId = customerId;
    }

    public void setRenterId(String renterId) {
        this.renterId = renterId;
    }

    public void setLng(double lng) {
        this.lng = lng;
    }

    public void setLat(double lat) {
        this.lat = lat;
    }
}