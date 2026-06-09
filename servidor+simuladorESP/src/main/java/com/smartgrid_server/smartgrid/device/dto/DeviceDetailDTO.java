package com.smartgrid_server.smartgrid.device.dto;

import com.smartgrid_server.smartgrid.device.model.DeviceStatus;
import java.time.LocalDateTime;

public record DeviceDetailDTO(
        String id,
        String name,
        String location,
        boolean outletOn,
        DeviceStatus status,
        LocalDateTime lastSeen,
        MeasurementDTO measurement
) {}
