package com.winlkar.app.model;

public class Feedback {
    private String id;
    private String driverId;
    private String driverName;
    private String message;
    private long timestamp;

    public Feedback() {
    }

    public Feedback(String id, String driverId, String driverName, String message, long timestamp) {
        this.id = id;
        this.driverId = driverId;
        this.driverName = driverName;
        this.message = message;
        this.timestamp = timestamp;
    }

    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }

    public String getDriverId() {
        return driverId;
    }

    public void setDriverId(String driverId) {
        this.driverId = driverId;
    }

    public String getDriverName() {
        return driverName;
    }

    public void setDriverName(String driverName) {
        this.driverName = driverName;
    }

    public String getMessage() {
        return message;
    }

    public void setMessage(String message) {
        this.message = message;
    }

    public long getTimestamp() {
        return timestamp;
    }

    public void setTimestamp(long timestamp) {
        this.timestamp = timestamp;
    }
}
