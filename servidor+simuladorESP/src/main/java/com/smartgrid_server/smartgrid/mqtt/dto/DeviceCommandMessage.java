package com.smartgrid_server.smartgrid.mqtt.dto;

import java.time.LocalDateTime;

public record DeviceCommandMessage(
        String deviceId,
        String command,
        LocalDateTime sentAt
) {}
