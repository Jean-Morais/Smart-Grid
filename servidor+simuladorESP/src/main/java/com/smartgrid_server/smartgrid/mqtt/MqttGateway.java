package com.smartgrid_server.smartgrid.mqtt;

import jakarta.annotation.PostConstruct;
import jakarta.annotation.PreDestroy;
import org.eclipse.paho.client.mqttv3.IMqttMessageListener;
import org.eclipse.paho.client.mqttv3.MqttClient;
import org.eclipse.paho.client.mqttv3.MqttConnectOptions;
import org.eclipse.paho.client.mqttv3.MqttMessage;
import org.springframework.stereotype.Component;

import java.nio.charset.StandardCharsets;
import java.util.UUID;

@Component
public class MqttGateway {

    private final MqttProperties properties;
    private MqttClient client;

    public MqttGateway(MqttProperties properties) {
        this.properties = properties;
    }

    @PostConstruct
    public void connect() throws Exception {
        String clientId = properties.getServerClientId() + "-" + UUID.randomUUID();
        client = new MqttClient(properties.getBrokerUri(), clientId);

        MqttConnectOptions options = new MqttConnectOptions();
        options.setAutomaticReconnect(true);
        options.setCleanSession(true);
        options.setConnectionTimeout(10);
        options.setKeepAliveInterval(30);

        client.connect(options);
        System.out.println("Servidor conectado ao broker MQTT em " + properties.getBrokerUri());
    }

    public void subscribe(String topic, IMqttMessageListener listener) throws Exception {
        ensureConnected();
        client.subscribe(topic, listener);
        System.out.println("Inscrito em: " + topic);
    }

    public void publish(String topic, Object payload) throws Exception {
        ensureConnected();
        String json = MqttJsonCodec.serialize(payload);
        MqttMessage message = new MqttMessage(json.getBytes(StandardCharsets.UTF_8));
        message.setQos(1);
        client.publish(topic, message);
        System.out.println("Publicado em " + topic + ": " + json);
    }

    public void publishRaw(String topic, String payload) throws Exception {
        ensureConnected();
        MqttMessage message = new MqttMessage(payload.getBytes(StandardCharsets.UTF_8));
        message.setQos(1);
        client.publish(topic, message);
        System.out.println("Publicado em " + topic + ": " + payload);
    }

    private void ensureConnected() {
        if (client == null || !client.isConnected()) {
            throw new IllegalStateException("Broker MQTT indisponível. Inicie o Mosquitto antes da aplicação.");
        }
    }

    @PreDestroy
    public void shutdown() {
        try {
            if (client != null && client.isConnected()) {
                client.disconnect();
            }
        } catch (Exception ignored) {
        }
        try {
            if (client != null) {
                client.close();
            }
        } catch (Exception ignored) {
        }
    }
}
