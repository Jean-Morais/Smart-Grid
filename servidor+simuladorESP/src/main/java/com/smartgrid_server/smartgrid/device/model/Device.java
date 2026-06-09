package com.smartgrid_server.smartgrid.device.model;

import java.time.LocalDateTime;

public class Device {

    private final String id;
    private String name;
    private String location;
    private volatile boolean outletOn;
    private volatile DeviceStatus status;
    private volatile LocalDateTime lastSeen;

    public Device(String id, String name, String location,
                  boolean outletOn, DeviceStatus status) {
        this.id = id;
        this.name = name;
        this.location = location;
        this.outletOn = outletOn;
        this.status = status;
        this.lastSeen = LocalDateTime.now();
    }

    public String getId() { return id; }
    public String getName() { return name; }
    public String getLocation() { return location; }
    public boolean isOutletOn() { return outletOn; }
    public DeviceStatus getStatus() { return status; }
    public LocalDateTime getLastSeen() { return lastSeen; }

    public void setName(String name) { this.name = name; }
    public void setLocation(String location) { this.location = location; }
    public void setOutletOn(boolean outletOn) { this.outletOn = outletOn; }
    public void setStatus(DeviceStatus status) { this.status = status; }
    public void setLastSeen(LocalDateTime lastSeen) { this.lastSeen = lastSeen; }
}
