package com.smartgrid_server.smartgrid.device.service;

import com.smartgrid_server.smartgrid.device.dto.*;
import com.smartgrid_server.smartgrid.device.model.*;
import com.smartgrid_server.smartgrid.mqtt.MqttGateway;
import com.smartgrid_server.smartgrid.mqtt.MqttProperties;
import com.smartgrid_server.smartgrid.mqtt.MqttTopics;
import com.smartgrid_server.smartgrid.mqtt.dto.DeviceCommandMessage;
import com.smartgrid_server.smartgrid.mqtt.dto.EspAnnouncementMessage;
import com.smartgrid_server.smartgrid.mqtt.dto.EspTelemetryMessage;
import jakarta.annotation.PostConstruct;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.*;
import java.util.concurrent.ConcurrentHashMap;
import java.util.stream.Collectors;

@Service
public class DeviceService {

    private final Map<String, Device> devices = new ConcurrentHashMap<>();
    private final Map<String, DeviceMeasurement> currentMeasurements = new ConcurrentHashMap<>();

    private final MqttGateway mqttGateway;
    private final MqttProperties mqttProperties;

    public DeviceService(MqttGateway mqttGateway, MqttProperties mqttProperties) {
        this.mqttGateway = mqttGateway;
        this.mqttProperties = mqttProperties;
    }


    @PostConstruct
    public void initDefaultDevices() {
        registerDefaultDevice("esp-001", "ESP 001", "Sala");
        registerDefaultDevice("esp-002", "ESP 002", "Quarto");
        registerDefaultDevice("esp-003", "ESP 003", "Cozinha");
    }

    private void registerDefaultDevice(String id, String name, String location) {
        devices.putIfAbsent(id, new Device(id, name, location, false, DeviceStatus.OFFLINE));
    }

    public void registerAnnouncement(EspAnnouncementMessage announcement) {
        if (announcement == null || announcement.deviceId() == null || announcement.deviceId().isBlank()) {
            return;
        }

        devices.compute(announcement.deviceId(), (id, existing) -> {
            if (existing == null) {
                return new Device(
                        id,
                        defaultText(announcement.deviceName(), id),
                        defaultText(announcement.location(), "Indefinido"),
                        false,
                        DeviceStatus.OFFLINE
                );
            }

            if (hasText(announcement.deviceName())) {
                existing.setName(announcement.deviceName());
            }
            if (hasText(announcement.location())) {
                existing.setLocation(announcement.location());
            }
            return existing;
        });

        Device device = devices.get(announcement.deviceId());
        if (device != null) {
            device.setLastSeen(LocalDateTime.now());
        }
    }

    public void updateFromEsp(EspTelemetryMessage telemetry) {
        if (telemetry == null || telemetry.deviceId() == null || telemetry.deviceId().isBlank()) {
            return;
        }

        Device device = devices.compute(telemetry.deviceId(), (id, existing) -> {
            if (existing == null) {
                return new Device(
                        id,
                        defaultText(telemetry.deviceName(), id),
                        defaultText(telemetry.location(), "Indefinido"),
                        telemetry.outletOn(),
                        DeviceStatus.ONLINE
                );
            }

            if (hasText(telemetry.deviceName())) {
                existing.setName(telemetry.deviceName());
            }
            if (hasText(telemetry.location())) {
                existing.setLocation(telemetry.location());
            }
            existing.setOutletOn(telemetry.outletOn());
            existing.setStatus(DeviceStatus.ONLINE);
            existing.setLastSeen(LocalDateTime.now());
            return existing;
        });

        double power = round2(telemetry.voltage() * telemetry.current() * telemetry.powerFactor());
        double energyWh = round3(power * (telemetry.connectedSeconds() / 3600.0));

        DeviceMeasurement measurement = new DeviceMeasurement(
                device.getId(),
                telemetry.voltage(),
                telemetry.current(),
                telemetry.powerFactor(),
                telemetry.connectedSeconds(),
                power,
                energyWh,
                LocalDateTime.now()
        );

        currentMeasurements.put(device.getId(), measurement);
        device.setStatus(DeviceStatus.ONLINE);
        device.setLastSeen(measurement.receivedAt());
    }

    public CommandResponse applyCommand(String deviceId, CommandRequest request) {
        if (!devices.containsKey(deviceId)) {
            devices.put(
                    deviceId,
                    new Device(deviceId, deviceId, "Indefinido", false, DeviceStatus.OFFLINE)
            );
        }

        Device device = devices.get(deviceId);

        return switch (request.type()) {
            case OUTLET_ON -> {
                device.setOutletOn(true);
                publishCommand(deviceId, CommandType.OUTLET_ON);
                yield new CommandResponse(deviceId, CommandType.OUTLET_ON, true, "Tomada ligada com sucesso.");
            }
            case OUTLET_OFF -> {
                device.setOutletOn(false);
                publishCommand(deviceId, CommandType.OUTLET_OFF);
                yield new CommandResponse(deviceId, CommandType.OUTLET_OFF, false, "Tomada desligada com sucesso.");
            }
        };
    }

    public MeasurementDTO requestRefresh(String deviceId) {
        if (!devices.containsKey(deviceId)) {
            devices.put(deviceId, new Device(deviceId, deviceId, "Indefinido", false, DeviceStatus.OFFLINE));
        }

        LocalDateTime previousSeen = devices.get(deviceId).getLastSeen();
        publishRead(deviceId);

        long deadline = System.currentTimeMillis() + 2500;
        while (System.currentTimeMillis() < deadline) {
            Device device = devices.get(deviceId);
            if (device != null && device.getLastSeen() != null && device.getLastSeen().isAfter(previousSeen)) {
                return getCurrentMeasurement(deviceId)
                        .orElseThrow(() -> new IllegalStateException(
                                "Ainda não há medições para o controlador: " + deviceId));
            }

            try {
                Thread.sleep(80);
            } catch (InterruptedException e) {
                Thread.currentThread().interrupt();
                break;
            }
        }

        return getCurrentMeasurement(deviceId)
                .orElseThrow(() -> new IllegalStateException(
                        "Ainda não há medições para o controlador: " + deviceId));
    }

    public List<DeviceSummaryDTO> getAllDevices() {
        return devices.values().stream()
                .map(this::toSummaryDTO)
                .sorted(Comparator.comparing(DeviceSummaryDTO::location)
                        .thenComparing(DeviceSummaryDTO::name))
                .collect(Collectors.toList());
    }

    public Optional<DeviceDetailDTO> getDeviceById(String id) {
        return Optional.ofNullable(devices.get(id)).map(this::toDetailDTO);
    }

    public Optional<MeasurementDTO> getCurrentMeasurement(String id) {
        if (!devices.containsKey(id)) return Optional.empty();
        return Optional.ofNullable(currentMeasurements.get(id))
                .map(this::toMeasurementDTO);
    }

    private void publishCommand(String deviceId, CommandType type) {
        try {
            DeviceCommandMessage message = new DeviceCommandMessage(deviceId, type.name(), LocalDateTime.now());
            mqttGateway.publish(MqttTopics.commands(mqttProperties.getTopicPrefix(), deviceId), message);
        } catch (Exception e) {
            throw new IllegalStateException("Falha ao publicar comando MQTT para " + deviceId + ": " + e.getMessage(), e);
        }
    }

    private void publishRead(String deviceId) {
        try {
            DeviceCommandMessage message = new DeviceCommandMessage(deviceId, "READ", LocalDateTime.now());
            mqttGateway.publish(MqttTopics.commands(mqttProperties.getTopicPrefix(), deviceId), message);
        } catch (Exception e) {
            throw new IllegalStateException("Falha ao publicar leitura MQTT para " + deviceId + ": " + e.getMessage(), e);
        }
    }

    private DeviceSummaryDTO toSummaryDTO(Device d) {
        DeviceMeasurement m = currentMeasurements.get(d.getId());
        Double power = (m != null && d.isOutletOn()) ? m.power() : null;
        return new DeviceSummaryDTO(
                d.getId(), d.getName(), d.getLocation(),
                d.isOutletOn(), d.getStatus(),
                power, d.getLastSeen());
    }

    private DeviceDetailDTO toDetailDTO(Device d) {
        DeviceMeasurement m = currentMeasurements.get(d.getId());
        return new DeviceDetailDTO(
                d.getId(), d.getName(), d.getLocation(),
                d.isOutletOn(), d.getStatus(),
                d.getLastSeen(),
                m != null ? toMeasurementDTO(m) : null);
    }

    private MeasurementDTO toMeasurementDTO(DeviceMeasurement m) {
        return new MeasurementDTO(
                m.deviceId(),
                m.voltage(), m.current(), m.powerFactor(),
                m.connectedSeconds(), formatDuration(m.connectedSeconds()),
                m.power(), m.energyWh(),
                m.receivedAt());
    }

    static String formatDuration(long seconds) {
        long h = seconds / 3600;
        long min = (seconds % 3600) / 60;
        long sec = seconds % 60;
        if (h > 0) return String.format("%dh %02dmin", h, min);
        if (min > 0) return String.format("%dmin %02ds", min, sec);
        return String.format("%ds", sec);
    }

    private static String defaultText(String value, String fallback) {
        return hasText(value) ? value : fallback;
    }

    private static boolean hasText(String value) {
        return value != null && !value.isBlank();
    }

    private static double round2(double v) { return Math.round(v * 100.0) / 100.0; }
    private static double round3(double v) { return Math.round(v * 1000.0) / 1000.0; }
}
