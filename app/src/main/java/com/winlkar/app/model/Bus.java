package com.winlkar.app.model;

public class Bus {
    private String busId;
    private String plateNumber;
    private String model;
    private boolean active;

    public Bus() {
    }

    public Bus(String busId, String plateNumber, String model) {
        this.busId = busId;
        this.plateNumber = plateNumber;
        this.model = model;
        this.active = true;
    }

    public String getBusId() {
        return busId;
    }

    public void setBusId(String busId) {
        this.busId = busId;
    }

    public String getPlateNumber() {
        return plateNumber;
    }

    public void setPlateNumber(String plateNumber) {
        this.plateNumber = plateNumber;
    }

    public String getModel() {
        return model;
    }

    public void setModel(String model) {
        this.model = model;
    }

    public boolean isActive() {
        return active;
    }

    public void setActive(boolean active) {
        this.active = active;
    }
}
