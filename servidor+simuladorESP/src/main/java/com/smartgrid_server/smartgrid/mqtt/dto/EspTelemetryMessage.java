package com.smartgrid_server.smartgrid.mqtt.dto;

public record EspTelemetryMessage(
        String deviceId,
        String deviceName,
        String location,
        boolean outletOn,
        double voltage,
        double current,
        double powerFactor,
        long connectedSeconds
) {}
