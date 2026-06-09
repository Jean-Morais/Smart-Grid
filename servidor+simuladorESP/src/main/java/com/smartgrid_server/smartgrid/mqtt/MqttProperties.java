package com.smartgrid_server.smartgrid.mqtt;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

@Component
public class MqttProperties {

    @Value("${smartgrid.mqtt.broker-uri:tcp://localhost:1883}")
    private String brokerUri;

    @Value("${smartgrid.mqtt.server-client-id:smartgrid-server}")
    private String serverClientId;

    @Value("${smartgrid.mqtt.topic-prefix:smartgrid}")
    private String topicPrefix;

    public String getBrokerUri() { return brokerUri; }
    public String getServerClientId() { return serverClientId; }
    public String getTopicPrefix() { return topicPrefix; }
}
