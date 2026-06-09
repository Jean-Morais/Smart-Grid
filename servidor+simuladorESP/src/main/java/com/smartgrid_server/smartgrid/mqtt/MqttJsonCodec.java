package com.smartgrid_server.smartgrid.mqtt;

import com.smartgrid_server.smartgrid.mqtt.dto.DeviceCommandMessage;
import com.smartgrid_server.smartgrid.mqtt.dto.EspAnnouncementMessage;
import com.smartgrid_server.smartgrid.mqtt.dto.EspTelemetryMessage;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;

public final class MqttJsonCodec {

    private static final DateTimeFormatter DATE_TIME = DateTimeFormatter.ISO_LOCAL_DATE_TIME;

    private MqttJsonCodec() {
    }

    public static String serialize(Object payload) {
        if (payload instanceof String s) {
            return s;
        }
        if (payload instanceof DeviceCommandMessage message) {
            return "{" +
                    jsonField("deviceId", message.deviceId()) + "," +
                    jsonField("command", message.command()) + "," +
                    jsonField("sentAt", message.sentAt() != null ? DATE_TIME.format(message.sentAt()) : null) +
                    "}";
        }
        if (payload instanceof EspAnnouncementMessage message) {
            return "{" +
                    jsonField("deviceId", message.deviceId()) + "," +
                    jsonField("deviceName", message.deviceName()) + "," +
                    jsonField("location", message.location()) +
                    "}";
        }
        if (payload instanceof EspTelemetryMessage message) {
            return "{" +
                    jsonField("deviceId", message.deviceId()) + "," +
                    jsonField("deviceName", message.deviceName()) + "," +
                    jsonField("location", message.location()) + "," +
                    jsonField("outletOn", message.outletOn()) + "," +
                    jsonField("voltage", message.voltage()) + "," +
                    jsonField("current", message.current()) + "," +
                    jsonField("powerFactor", message.powerFactor()) + "," +
                    jsonField("connectedSeconds", message.connectedSeconds()) +
                    "}";
        }
        return payload.toString();
    }

    public static EspAnnouncementMessage parseAnnouncement(String json) {
        return new EspAnnouncementMessage(
                extractString(json, "deviceId"),
                extractString(json, "deviceName"),
                extractString(json, "location")
        );
    }

    public static EspTelemetryMessage parseTelemetry(String json) {
        return new EspTelemetryMessage(
                extractString(json, "deviceId"),
                extractString(json, "deviceName"),
                extractString(json, "location"),
                extractBoolean(json, "outletOn"),
                extractDouble(json, "voltage"),
                extractDouble(json, "current"),
                extractDouble(json, "powerFactor"),
                extractLong(json, "connectedSeconds")
        );
    }

    private static String jsonField(String name, String value) {
        return "\"" + escape(name) + "\":" + (value == null ? "null" : "\"" + escape(value) + "\"");
    }

    private static String jsonField(String name, boolean value) {
        return "\"" + escape(name) + "\":" + value;
    }

    private static String jsonField(String name, double value) {
        return "\"" + escape(name) + "\":" + value;
    }

    private static String jsonField(String name, long value) {
        return "\"" + escape(name) + "\":" + value;
    }

    private static String extractString(String json, String fieldName) {
        String raw = extractRaw(json, fieldName);
        if (raw == null || raw.equals("null")) {
            return null;
        }
        if (raw.startsWith("\"") && raw.endsWith("\"")) {
            raw = raw.substring(1, raw.length() - 1);
        }
        return unescape(raw);
    }

    private static boolean extractBoolean(String json, String fieldName) {
        String raw = extractRaw(json, fieldName);
        return raw != null && Boolean.parseBoolean(raw.trim());
    }

    private static double extractDouble(String json, String fieldName) {
        String raw = extractRaw(json, fieldName);
        if (raw == null || raw.isBlank() || raw.equals("null")) {
            return 0.0;
        }
        return Double.parseDouble(raw.trim());
    }

    private static long extractLong(String json, String fieldName) {
        String raw = extractRaw(json, fieldName);
        if (raw == null || raw.isBlank() || raw.equals("null")) {
            return 0L;
        }
        return Long.parseLong(raw.trim());
    }

    private static String extractRaw(String json, String fieldName) {
        if (json == null) {
            return null;
        }
        String needle = "\"" + fieldName + "\":";
        int start = json.indexOf(needle);
        if (start < 0) {
            return null;
        }
        start += needle.length();
        while (start < json.length() && Character.isWhitespace(json.charAt(start))) {
            start++;
        }
        if (start >= json.length()) {
            return null;
        }

        char first = json.charAt(start);
        if (first == '"') {
            int i = start + 1;
            boolean escaped = false;
            while (i < json.length()) {
                char c = json.charAt(i);
                if (c == '"' && !escaped) {
                    return json.substring(start, i + 1);
                }
                escaped = c == '\\' && !escaped;
                if (c != '\\') {
                    escaped = false;
                }
                i++;
            }
            return json.substring(start);
        }

        int end = start;
        while (end < json.length()) {
            char c = json.charAt(end);
            if (c == ',' || c == '}') {
                break;
            }
            end++;
        }
        return json.substring(start, end).trim();
    }

    private static String escape(String value) {
        if (value == null) {
            return null;
        }
        return value.replace("\\", "\\\\").replace("\"", "\\\"");
    }

    private static String unescape(String value) {
        return value.replace("\\\"", "\"").replace("\\\\", "\\");
    }
}
