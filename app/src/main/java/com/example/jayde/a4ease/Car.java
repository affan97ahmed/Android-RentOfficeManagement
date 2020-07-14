package com.example.jayde.a4ease;

public class Car{
    private String name;
    private String model;
    private String company;
    private int costPerDay;
    private String number;
    private String booked;
    private String vehicleId;
    private String bookedBy;
    private String bookedFrom;
    private String bookedTo;
    private String owner;
    private String trackerId;
    private String status;

    public void setStatus(String status) {
        this.status = status;
    }

    public String getStatus() {
        return status;
    }

    public void setTrackerId(String trackerId) {
        this.trackerId = trackerId;
    }

    public String getTrackerId() {
        return trackerId;
    }

    public void setOwner(String owner) {
        this.owner = owner;
    }

    public String getOwner() {
        return owner;
    }

    public void setBookedBy(String bookedBy) {
        this.bookedBy = bookedBy;
    }

    public void setBookedFrom(String bookedFrom) {
        this.bookedFrom = bookedFrom;
    }

    public void setBookedTo(String bookedTo) {
        this.bookedTo = bookedTo;
    }

    public String getBookedBy() {
        return bookedBy;
    }

    public String getBookedFrom() {
        return bookedFrom;
    }

    public String getBookedTo() {
        return bookedTo;
    }

    public void setVehicleId(String vehicleId) {
        this.vehicleId = vehicleId;
    }

    public String getVehicleId() {
        return vehicleId;
    }

    public void setBooked(String booked) {
        this.booked = booked;
    }

    public String getBooked() {
        return booked;
    }

    public void setNumber(String number) {
        this.number = number;
    }

    public String getNumber() {
        return number;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getCompany() {
        return company;
    }

    public void setCompany(String company) {
        this.company = company;
    }

    public int getCostPerDay() {
        return costPerDay;
    }

    public void setCostPerDay(int costPerDay) {
        this.costPerDay = costPerDay;
    }

    public String getModel() {
        return model;
    }

    public void setModel(String model) {
        this.model = model;
    }
}
