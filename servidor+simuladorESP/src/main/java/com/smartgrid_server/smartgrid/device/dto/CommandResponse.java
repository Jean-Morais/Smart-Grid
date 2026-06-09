package com.smartgrid_server.smartgrid.device.dto;

import com.smartgrid_server.smartgrid.device.model.CommandType;

public record CommandResponse(
        String deviceId,
        CommandType commandApplied,
        boolean outletOn,   // novo estado da tomada após o comando
        String message
) {}