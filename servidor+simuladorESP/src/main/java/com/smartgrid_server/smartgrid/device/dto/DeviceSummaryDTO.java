package com.smartgrid_server.smartgrid.device.dto;

import com.smartgrid_server.smartgrid.device.model.DeviceStatus;
import java.time.LocalDateTime;

public record DeviceSummaryDTO(
        String id,
        String name,
        String location,
        boolean outletOn,
        DeviceStatus status,
        Double currentPowerW,   // null se tomada desligada ou sem leitura
        LocalDateTime lastSeen
) {}