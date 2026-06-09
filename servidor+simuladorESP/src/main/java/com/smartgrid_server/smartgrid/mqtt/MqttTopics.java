package com.smartgrid_server.smartgrid.mqtt;

public final class MqttTopics {

    private MqttTopics() {}

    public static String telemetry(String prefix, String deviceId) {
        return prefix + "/" + deviceId + "/telemetry";
    }

    public static String commands(String prefix, String deviceId) {
        return prefix + "/" + deviceId + "/commands";
    }

    public static String announce(String prefix, String deviceId) {
        return prefix + "/" + deviceId + "/announce";
    }

    public static String telemetryWildcard(String prefix) {
        return prefix + "/+/telemetry";
    }

    public static String announceWildcard(String prefix) {
        return prefix + "/+/announce";
    }
}
