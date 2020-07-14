package com.example.jayde.a4ease;

public class Notification {
    private String type;
    private String status;
    private String message;
    private String id;
    private String bookingId;
    private String renterId;
    private String vehicleId;


    public void setRenterId(String renterId) {
        this.renterId = renterId;
    }

    public void setVehicleId(String vehicleId) {
        this.vehicleId = vehicleId;
    }

    public String getRenterId() {
        return renterId;
    }

    public String getVehicleId() {
        return vehicleId;
    }

    public void setBookingId(String bookingId) {
        this.bookingId = bookingId;
    }

    public String getBookingId() {
        return bookingId;
    }

    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }

    public String getStatus() {
        return status;
    }

    public String getMessage() {
        return message;
    }

    public String getType() {
        return type;
    }

    public void setStatus(String status) {
        this.status = status;
    }

    public void setMessage(String message) {
        this.message = message;
    }

    public void setType(String type) {
        this.type = type;
    }

}
