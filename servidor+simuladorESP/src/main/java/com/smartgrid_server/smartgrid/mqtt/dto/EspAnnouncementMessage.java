package com.smartgrid_server.smartgrid.mqtt.dto;

public record EspAnnouncementMessage(
        String deviceId,
        String deviceName,
        String location
) {}
