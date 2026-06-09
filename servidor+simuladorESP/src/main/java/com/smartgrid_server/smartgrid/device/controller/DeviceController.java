package com.smartgrid_server.smartgrid.device.controller;

import com.smartgrid_server.smartgrid.device.dto.*;
import com.smartgrid_server.smartgrid.device.service.DeviceService;
import jakarta.validation.Valid;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.NoSuchElementException;

@RestController
@RequestMapping("/api/devices")
public class DeviceController {

    private final DeviceService deviceService;

    // Spring injeta o DeviceService automaticamente (injeção de dependência)
    public DeviceController(DeviceService deviceService) {
        this.deviceService = deviceService;
    }

    /**
     * GET /api/devices
     * Retorna todos os controladores com estado e potência instantânea.
     *
     * Exemplo de resposta:
     * [
     *   { "id": "ctrl-001", "name": "Sala - Tomada 1", "outletOn": true,
     *     "currentPowerW": 75.43, "status": "ONLINE", ... },
     *   ...
     * ]
     */
    @GetMapping
    public ResponseEntity<List<DeviceSummaryDTO>> listarTodos() {
        return ResponseEntity.ok(deviceService.getAllDevices());
    }

    /**
     * GET /api/devices/{id}
     * Retorna os detalhes completos + todas as medições de um controlador.
     * 404 se o id não existir.
     *
     * Exemplo de resposta:
     * {
     *   "id": "ctrl-001", "name": "Sala - Tomada 1", "outletOn": true, ...
     *   "measurement": {
     *     "voltage": 127.3, "current": 0.71, "powerFactor": 0.85,
     *     "connectedTime": "1h 20min", "power": 76.82, "energyWh": 102.43
     *   }
     * }
     */
    @GetMapping("/{id}")
    public ResponseEntity<DeviceDetailDTO> buscarPorId(@PathVariable String id) {
        return deviceService.getDeviceById(id)
                .map(ResponseEntity::ok)
                .orElseThrow(() -> new NoSuchElementException(
                        "Controlador não encontrado: " + id));
    }

    /**
     * POST /api/devices/{id}/command
     * Envia um comando de ligar ou desligar a tomada.
     *
     * Body obrigatório: { "type": "OUTLET_ON" }
     *                ou { "type": "OUTLET_OFF" }
     *
     * 400 se o body for inválido ou "type" ausente.
     * 404 se o controlador não existir.
     *
     * Fase 2: publicará o comando via MQTT antes de retornar.
     */
    @PostMapping("/{id}/command")
    public ResponseEntity<CommandResponse> enviarComando(
            @PathVariable String id,
            @RequestBody @Valid CommandRequest request) {
        return ResponseEntity.ok(deviceService.applyCommand(id, request));
    }

    /**
     * POST /api/devices/{id}/refresh
     * Solicita uma leitura atualizada do controlador.
     *
     * Fase 1: retorna a última medição armazenada em memória.
     * Fase 2: publicará "READ" no tópico MQTT do controlador e
     *         o ESP responderá com uma nova leitura.
     *
     * 404 se o controlador não existir.
     * 409 se ainda não houver nenhuma medição disponível.
     */
    @PostMapping("/{id}/refresh")
    public ResponseEntity<MeasurementDTO> solicitarAtualizacao(@PathVariable String id) {
        return ResponseEntity.ok(deviceService.requestRefresh(id));
    }
}