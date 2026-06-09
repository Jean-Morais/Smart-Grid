package com.smartgrid_server.smartgrid.device.model;

import java.time.LocalDateTime;

public record DeviceMeasurement(
        String deviceId,

        // ── Dados recebidos do ESP ────────────────────────────────────────────
        double voltage,          // Volts (V)
        double current,          // Ampères (A)
        double powerFactor,      // adimensional, 0.0 – 1.0
        long connectedSeconds,   // segundos que o dispositivo está ligado na tomada

        // ── Calculados pelo servidor ──────────────────────────────────────────
        double power,            // Watts (W)  = V × I × FP
        double energyWh,         // Watt-hora  = W × (connectedSeconds / 3600)

        LocalDateTime receivedAt // quando o servidor recebeu esta leitura
) {}
