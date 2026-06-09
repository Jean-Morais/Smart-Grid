package com.smartgrid_server.smartgrid.mqtt;

import com.smartgrid_server.smartgrid.device.service.DeviceService;
import jakarta.annotation.PostConstruct;
import org.springframework.stereotype.Service;
import java.nio.charset.StandardCharsets;

@Service
public class MqttTelemetrySubscriber {

    private final MqttGateway mqttGateway;
    private final DeviceService deviceService;
    private final MqttProperties properties;

    public MqttTelemetrySubscriber(
            MqttGateway mqttGateway,
            DeviceService deviceService,
            MqttProperties properties) {
        this.mqttGateway = mqttGateway;
        this.deviceService = deviceService;
        this.properties = properties;
    }

    @PostConstruct
    public void subscribe() throws Exception {
        mqttGateway.subscribe(MqttTopics.telemetryWildcard(properties.getTopicPrefix()), (topic, message) -> {
            try {
                String json = new String(message.getPayload(), StandardCharsets.UTF_8);
                deviceService.updateFromEsp(MqttJsonCodec.parseTelemetry(json));
            } catch (Exception e) {
                System.err.println("Falha ao processar telemetria MQTT em " + topic + ": " + e.getMessage());
            }
        });

        mqttGateway.subscribe(MqttTopics.announceWildcard(properties.getTopicPrefix()), (topic, message) -> {
            try {
                String json = new String(message.getPayload(), StandardCharsets.UTF_8);
                deviceService.registerAnnouncement(MqttJsonCodec.parseAnnouncement(json));
            } catch (Exception e) {
                System.err.println("Falha ao processar anúncio MQTT em " + topic + ": " + e.getMessage());
            }
        });
    }
}
