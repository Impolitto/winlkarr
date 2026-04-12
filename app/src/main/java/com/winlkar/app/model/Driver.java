package com.winlkar.app.model;

public class Driver {
    private String driverId;
    private String name;
    private String password;
    private String assignedBusId;

    public Driver() {
    }

    public Driver(String driverId, String name, String password, String assignedBusId) {
        this.driverId = driverId;
        this.name = name;
        this.password = password;
        this.assignedBusId = assignedBusId;
    }

    public String getDriverId() {
        return driverId;
    }

    public void setDriverId(String driverId) {
        this.driverId = driverId;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getPassword() {
        return password;
    }

    public void setPassword(String password) {
        this.password = password;
    }

    public String getAssignedBusId() {
        return assignedBusId;
    }

    public void setAssignedBusId(String assignedBusId) {
        this.assignedBusId = assignedBusId;
    }
}
