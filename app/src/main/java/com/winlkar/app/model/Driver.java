package com.winlkar.app.model;

public class Driver {
    private String username;
    private String name;
    private String password;
    private String assignedBusId;

    public Driver() {
    }

    public Driver(String username, String name, String password, String assignedBusId) {
        this.username = username;
        this.name = name;
        this.password = password;
        this.assignedBusId = assignedBusId;
    }

    public String getUsername() {
        return username;
    }

    public void setUsername(String username) {
        this.username = username;
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
