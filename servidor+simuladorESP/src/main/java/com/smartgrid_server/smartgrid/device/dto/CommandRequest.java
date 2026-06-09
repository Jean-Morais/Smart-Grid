package com.smartgrid_server.smartgrid.device.dto;

import com.smartgrid_server.smartgrid.device.model.CommandType;
import jakarta.validation.constraints.NotNull;

public record CommandRequest(
        @NotNull(message = "O campo 'type' é obrigatório: OUTLET_ON ou OUTLET_OFF")
        CommandType type
) {}