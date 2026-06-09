package com.smartgrid_server.smartgrid.esp;

import com.smartgrid_server.smartgrid.device.model.CommandType;
import com.smartgrid_server.smartgrid.mqtt.MqttTopics;
import org.eclipse.paho.client.mqttv3.MqttClient;
import org.eclipse.paho.client.mqttv3.MqttConnectOptions;
import org.eclipse.paho.client.mqttv3.MqttMessage;

import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.List;
import java.util.Objects;
import java.util.Random;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.concurrent.atomic.AtomicLong;

/**
 * Simulador Java de múltiplos ESPs no mesmo processo.
 *
 * Cada ESP virtual cria seu próprio client MQTT, publica telemetria e escuta
 * comandos no tópico:
 *
 *   smartgrid/<espId>/commands
 *
 * Uso:
 *   java ... EspSimulatorApplication
 *   java ... EspSimulatorApplication esp-001 "Tomada Sala" Sala 5000 127 0.75 0.92
 *   java ... EspSimulatorApplication --devices esp-001:Sala:5,esp-002:Quarto:6,esp-003:Cozinha:7
 */
public class EspSimulatorApplication {

    private static final String DEFAULT_BROKER_URI = "tcp://localhost:1883";

    public static void main(String[] args) {
        List<DeviceSpec> specs = parseArguments(args);
        new EspSimulatorApplication().run(specs);
    }

    private void run(List<DeviceSpec> specs) {
        if (specs.isEmpty()) {
            specs = defaultSpecs();
        }

        System.out.println("Iniciando simulador Java com " + specs.size() + " ESP(s).");
        for (DeviceSpec spec : specs) {
            System.out.println(" - " + spec);
        }

        List<SimulatedEsp> runningDevices = new ArrayList<>();
        try {
            for (DeviceSpec spec : specs) {
                SimulatedEsp esp = new SimulatedEsp(spec);
                runningDevices.add(esp);
                esp.start();
            }

            Runtime.getRuntime().addShutdownHook(new Thread(() -> {
                for (SimulatedEsp esp : runningDevices) {
                    esp.stop();
                }
            }));

            // Mantém o processo vivo enquanto os ESPs publicam.
            while (true) {
                Thread.sleep(1000L);
            }
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
        } catch (Exception e) {
            e.printStackTrace();
        } finally {
            for (SimulatedEsp esp : runningDevices) {
                esp.stop();
            }
        }
    }

    private static List<DeviceSpec> parseArguments(String[] args) {
        if (args == null || args.length == 0) {
            return defaultSpecs();
        }

        if (args.length == 1 && ("--help".equalsIgnoreCase(args[0]) || "-h".equalsIgnoreCase(args[0]))) {
            printUsage();
            return List.of();
        }

        if (args.length >= 1 && "--devices".equalsIgnoreCase(args[0])) {
            String raw = args.length >= 2 ? String.join(" ", java.util.Arrays.copyOfRange(args, 1, args.length)) : "";
            if (raw.isBlank()) {
                printUsage();
                return List.of();
            }
            return parseDeviceList(raw);
        }

        if (args.length == 1 && args[0].startsWith("--devices=")) {
            String raw = valueAfterEquals(args[0]);
            if (raw == null || raw.isBlank()) {
                printUsage();
                return List.of();
            }
            return parseDeviceList(raw);
        }

        // Compatibilidade com o modo anterior de 1 ESP por execução:
        // args = deviceId, deviceName, location, intervalMs, baseVoltage, baseCurrent, basePf
        if (looksLikeSingleDevice(args)) {
            return List.of(parseSingleDevice(args));
        }

        // Se vier um único argumento com múltiplos dispositivos separados por ; ou ,
        if (args.length == 1 && (args[0].contains(";") || args[0].contains(","))) {
            return parseDeviceList(args[0]);
        }

        // Se o usuário passar valores soltos sem indicar múltiplos dispositivos,
        // mantém compatibilidade com o modo antigo e usa o primeiro bloco como device.
        return List.of(parseSingleDevice(args));
    }

    private static boolean looksLikeSingleDevice(String[] args) {
        return args.length >= 1
                && !args[0].contains(",")
                && !args[0].contains(";")
                && !args[0].startsWith("--");
    }

    private static DeviceSpec parseSingleDevice(String[] args) {
        String deviceId = args.length > 0 ? args[0] : "esp-001";
        String deviceName = args.length > 1 ? args[1] : "ESP 001";
        String location = args.length > 2 ? args[2] : "Sala";
        long intervalMs = args.length > 3 ? parseLong(args[3], 5000L) : 5000L;
        double baseVoltage = args.length > 4 ? parseDouble(args[4], 127.0) : 127.0;
        double baseCurrent = args.length > 5 ? parseDouble(args[5], 0.75) : 0.75;
        double basePf = args.length > 6 ? parseDouble(args[6], 0.92) : 0.92;

        return new DeviceSpec(deviceId, deviceName, location, intervalMs, baseVoltage, baseCurrent, basePf);
    }

    private static List<DeviceSpec> parseDeviceList(String raw) {
        List<DeviceSpec> specs = new ArrayList<>();
        for (String token : raw.split("[;,]")) {
            String trimmed = token.trim();
            if (trimmed.isBlank()) {
                continue;
            }
            specs.add(parseCompactDevice(trimmed));
        }
        return specs;
    }

    /**
     * Formato compacto:
     *   esp-001[:nome[:local[:intervaloMs[:tensao[:corrente[:fp]]]]]]
     *
     * Exemplo:
     *   esp-001:Sala:Sala:5000:127:0.75:0.92
     */
    private static DeviceSpec parseCompactDevice(String token) {
        String[] parts = token.split(":");
        String deviceId = parts.length > 0 && !parts[0].isBlank() ? parts[0].trim() : "esp-" + UUID.randomUUID();
        String deviceName = parts.length > 1 && !parts[1].isBlank() ? parts[1].trim() : "ESP " + deviceId;
        String location = parts.length > 2 && !parts[2].isBlank() ? parts[2].trim() : "Indefinido";
        long intervalMs = parts.length > 3 ? parseLong(parts[3], 5000L) : 5000L;
        double baseVoltage = parts.length > 4 ? parseDouble(parts[4], 127.0) : 127.0;
        double baseCurrent = parts.length > 5 ? parseDouble(parts[5], 0.75) : 0.75;
        double basePf = parts.length > 6 ? parseDouble(parts[6], 0.92) : 0.92;
        return new DeviceSpec(deviceId, deviceName, location, intervalMs, baseVoltage, baseCurrent, basePf);
    }

    private static void printUsage() {
        System.out.println("""
                Uso:
                  java ... EspSimulatorApplication
                  java ... EspSimulatorApplication esp-001 "Tomada Sala" Sala 5000 127 0.75 0.92
                  java ... EspSimulatorApplication --devices esp-001:Sala:Sala:5000:127:0.75:0.92,esp-002:Quarto:Quarto:6000:220:0.50:0.88

                Tópicos:
                  smartgrid/<espId>/telemetry
                  smartgrid/<espId>/commands
                  smartgrid/<espId>/announce
                """);
    }

    private static String valueAfterEquals(String arg) {
        int idx = arg.indexOf('=');
        return idx >= 0 ? arg.substring(idx + 1).trim() : "";
    }

    private static long parseLong(String raw, long fallback) {
        try {
            return Long.parseLong(raw.trim());
        } catch (Exception ignored) {
            return fallback;
        }
    }

    private static double parseDouble(String raw, double fallback) {
        try {
            return Double.parseDouble(raw.trim());
        } catch (Exception ignored) {
            return fallback;
        }
    }

    private static List<DeviceSpec> defaultSpecs() {
        return List.of(
                new DeviceSpec("esp-001", "ESP 001", "Sala", 5000L, 127.0, 0.75, 0.92),
                new DeviceSpec("esp-002", "ESP 002", "Quarto", 6000L, 127.0, 0.55, 0.90),
                new DeviceSpec("esp-003", "ESP 003", "Cozinha", 7000L, 220.0, 0.85, 0.89)
        );
    }

    private static final class DeviceSpec {
        private final String deviceId;
        private final String deviceName;
        private final String location;
        private final long intervalMs;
        private final double baseVoltage;
        private final double baseCurrent;
        private final double basePf;

        private DeviceSpec(
                String deviceId,
                String deviceName,
                String location,
                long intervalMs,
                double baseVoltage,
                double baseCurrent,
                double basePf) {
            this.deviceId = Objects.requireNonNullElse(deviceId, "esp-unknown");
            this.deviceName = Objects.requireNonNullElse(deviceName, this.deviceId);
            this.location = Objects.requireNonNullElse(location, "Indefinido");
            this.intervalMs = intervalMs <= 0 ? 5000L : intervalMs;
            this.baseVoltage = baseVoltage;
            this.baseCurrent = baseCurrent;
            this.basePf = basePf;
        }

        @Override
        public String toString() {
            return deviceId + " | " + deviceName + " | " + location + " | intervalo=" + intervalMs + "ms";
        }
    }

    private static final class SimulatedEsp {
        private final DeviceSpec spec;
        private final Random random = new Random();
        private final AtomicBoolean outletOn = new AtomicBoolean(true);
        private final AtomicLong outletOnSince = new AtomicLong(System.currentTimeMillis());
        private final AtomicBoolean running = new AtomicBoolean(false);
        private final ConcurrentHashMap<String, Boolean> ignored = new ConcurrentHashMap<>();

        private MqttClient client;
        private Thread worker;

        private SimulatedEsp(DeviceSpec spec) {
            this.spec = spec;
        }

        private void start() throws Exception {
            client = new MqttClient(
                    DEFAULT_BROKER_URI,
                    "smartgrid-esp-" + spec.deviceId + "-" + UUID.randomUUID()
            );

            MqttConnectOptions options = new MqttConnectOptions();
            options.setAutomaticReconnect(true);
            options.setCleanSession(true);
            options.setConnectionTimeout(10);
            options.setKeepAliveInterval(30);

            client.connect(options);
            client.subscribe(MqttTopics.commands("smartgrid", spec.deviceId), this::handleCommand);

            publishAnnouncement();
            running.set(true);

            worker = new Thread(this::loop, "esp-sim-" + spec.deviceId);
            worker.setDaemon(true);
            worker.start();

            System.out.println("[" + spec.deviceId + "] conectado ao broker MQTT e publicando em " +
                    MqttTopics.telemetry("smartgrid", spec.deviceId));
        }

        private void stop() {
            running.set(false);
            try {
                if (worker != null) {
                    worker.interrupt();
                }
            } catch (Exception ignored) {
            }
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

        private void loop() {
            while (running.get()) {
                try {
                    publishTelemetry();
                    Thread.sleep(spec.intervalMs);
                } catch (InterruptedException e) {
                    Thread.currentThread().interrupt();
                    return;
                } catch (Exception e) {
                    System.err.println("[" + spec.deviceId + "] falha ao publicar telemetria: " + e.getMessage());
                    try {
                        Thread.sleep(1000L);
                    } catch (InterruptedException ex) {
                        Thread.currentThread().interrupt();
                        return;
                    }
                }
            }
        }

        private void handleCommand(String topic, MqttMessage message) {
            try {
                String payload = new String(message.getPayload(), StandardCharsets.UTF_8);
                String cmd = extractField(payload, "command");
                if (cmd == null || cmd.isBlank()) {
                    return;
                }

                if ("READ".equalsIgnoreCase(cmd)) {
                    publishTelemetry();
                    return;
                }

                if (CommandType.OUTLET_ON.name().equalsIgnoreCase(cmd)) {
                    outletOn.set(true);
                    outletOnSince.set(System.currentTimeMillis());
                    System.out.println("[" + spec.deviceId + "] comando recebido: OUTLET_ON");
                    publishTelemetry();
                } else if (CommandType.OUTLET_OFF.name().equalsIgnoreCase(cmd)) {
                    System.out.println("[" + spec.deviceId + "] comando recebido: OUTLET_OFF");
                    publishTelemetry();
                    outletOn.set(false);
                    outletOnSince.set(0L);
                    publishTelemetry();
                }
            } catch (Exception e) {
                System.err.println("[" + spec.deviceId + "] falha ao processar comando: " + e.getMessage());
            }
        }

        private void publishAnnouncement() throws Exception {
            String json = "{"
                    + "\"deviceId\":\"" + escape(spec.deviceId) + "\","
                    + "\"deviceName\":\"" + escape(spec.deviceName) + "\","
                    + "\"location\":\"" + escape(spec.location) + "\""
                    + "}";
            publishJson(MqttTopics.announce("smartgrid", spec.deviceId), json);
            System.out.println("[" + spec.deviceId + "] anúncio publicado");
        }

        private void publishTelemetry() throws Exception {
            boolean isOn = outletOn.get();
            long connectedSeconds = isOn
                    ? Math.max(0L, (System.currentTimeMillis() - outletOnSince.get()) / 1000L)
                    : 0L;

            double voltage = isOn ? round2(spec.baseVoltage + jitter(spec.baseVoltage * 0.02)) : 0.0;
            double current = isOn ? round3(Math.max(0.0, spec.baseCurrent + jitter(spec.baseCurrent * 0.05))) : 0.0;
            double pf = isOn ? round3(Math.min(1.0, Math.max(0.5, spec.basePf + jitter(0.02)))) : 0.0;

            String json = "{"
                    + "\"deviceId\":\"" + escape(spec.deviceId) + "\","
                    + "\"deviceName\":\"" + escape(spec.deviceName) + "\","
                    + "\"location\":\"" + escape(spec.location) + "\","
                    + "\"outletOn\":" + isOn + ","
                    + "\"voltage\":" + voltage + ","
                    + "\"current\":" + current + ","
                    + "\"powerFactor\":" + pf + ","
                    + "\"connectedSeconds\":" + connectedSeconds
                    + "}";

            publishJson(MqttTopics.telemetry("smartgrid", spec.deviceId), json);
            System.out.println("[" + spec.deviceId + "] publicado: " + json);
        }

        private void publishJson(String topic, String payload) throws Exception {
            MqttMessage msg = new MqttMessage(payload.getBytes(StandardCharsets.UTF_8));
            msg.setQos(1);
            client.publish(topic, msg);
        }

        private double jitter(double range) {
            return (random.nextDouble() * 2.0 - 1.0) * range;
        }

        private double round2(double v) {
            return Math.round(v * 100.0) / 100.0;
        }

        private double round3(double v) {
            return Math.round(v * 1000.0) / 1000.0;
        }

        private String extractField(String json, String fieldName) {
            String needle = "\"" + fieldName + "\":\"";
            int start = json.indexOf(needle);
            if (start < 0) {
                return null;
            }
            start += needle.length();
            int end = json.indexOf('"', start);
            if (end < 0) {
                return null;
            }
            return json.substring(start, end);
        }

        private String escape(String text) {
            return text == null ? "" : text.replace("\\", "\\\\").replace("\"", "\\\"");
        }
    }
}
