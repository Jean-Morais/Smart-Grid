package com.smartgrid_server.smartgrid.device.dto;

import java.time.LocalDateTime;

public record MeasurementDTO(
        String deviceId,
        double voltage,           // V
        double current,           // A
        double powerFactor,       // 0.0 – 1.0
        long connectedSeconds,
        String connectedTime,     // ex.: "2h 15min" ou "45min 30s"
        double power,             // W
        double energyWh,          // Wh
        LocalDateTime measuredAt
) {}