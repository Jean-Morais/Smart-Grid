#pragma once

/**
 * api_client.hpp
 * ──────────────
 * DTOs e cliente HTTP para a API SmartGrid (servidor Java / Spring Boot 4).
 *
 * Cada struct espelha o Java record correspondente; os nomes dos campos são
 * idênticos para que o JSON deserialize corretamente sem mapeamentos extras.
 *
 * Endpoints cobertos:
 *   GET  /api/devices                → Client::listDevices()
 *   GET  /api/devices/{id}           → Client::getDevice(id)
 *   POST /api/devices/{id}/command   → Client::sendCommand(id, type)
 *   POST /api/devices/{id}/refresh   → Client::refresh(id)
 */

#include <optional>
#include <stdexcept>
#include <string>
#include <vector>

#include <httplib.h>
#include <nlohmann/json.hpp>

namespace sg {

using json = nlohmann::json;

// ═══════════════════════════════════════════════════════════════════════════
// DTOs — espelham os Java records do servidor
// ═══════════════════════════════════════════════════════════════════════════

/**
 * MeasurementDTO.java
 * Snapshot das leituras elétricas de um controlador em um determinado instante.
 *   power    = voltage × current × powerFactor        (W)
 *   energyWh = power  × (connectedSeconds / 3600.0)   (Wh)
 */
struct Measurement {
    std::string   deviceId;
    double        voltage{};           // V
    double        current{};           // A
    double        powerFactor{};       // 0.0 – 1.0
    long long     connectedSeconds{};
    std::string   connectedTime;       // "2h 15min" | "45min 30s" | "10s"
    double        power{};             // W
    double        energyWh{};          // Wh
    std::string   measuredAt;          // ISO-8601, ex.: "2025-06-07T10:30:00"
};

/**
 * DeviceSummaryDTO.java
 * Visão resumida: usada na listagem geral GET /api/devices.
 * currentPowerW é null quando a tomada está desligada ou sem leitura.
 */
struct DeviceSummary {
    std::string            id;
    std::string            name;
    std::string            location;
    bool                   outletOn{};
    std::string            status;           // "ONLINE" | "OFFLINE"
    std::optional<double>  currentPowerW;
    std::string            lastSeen;
};

/**
 * DeviceDetailDTO.java
 * Detalhes completos: GET /api/devices/{id}.
 * measurement é null apenas nos primeiros segundos após o boot.
 */
struct DeviceDetail {
    std::string                id;
    std::string                name;
    std::string                location;
    bool                       outletOn{};
    std::string                status;
    std::string                lastSeen;
    std::optional<Measurement> measurement;
};

/**
 * CommandResponse.java
 * Resposta ao POST /api/devices/{id}/command.
 */
struct CommandResult {
    std::string deviceId;
    std::string commandApplied;  // "OUTLET_ON" | "OUTLET_OFF"
    bool        outletOn{};      // novo estado da tomada após o comando
    std::string message;
};

// ═══════════════════════════════════════════════════════════════════════════
// Parsers JSON  (json → struct)
// ═══════════════════════════════════════════════════════════════════════════

inline Measurement parseMeasurement(const json& j) {
    Measurement m;
    m.deviceId         = j.value("deviceId",         "");
    m.voltage          = j.value("voltage",          0.0);
    m.current          = j.value("current",          0.0);
    m.powerFactor      = j.value("powerFactor",      0.0);
    m.connectedSeconds = j.value("connectedSeconds", 0LL);
    m.connectedTime    = j.value("connectedTime",    "");
    m.power            = j.value("power",            0.0);
    m.energyWh         = j.value("energyWh",         0.0);
    m.measuredAt       = j.value("measuredAt",       "");
    return m;
}

inline DeviceSummary parseSummary(const json& j) {
    DeviceSummary d;
    d.id       = j.value("id",       "");
    d.name     = j.value("name",     "");
    d.location = j.value("location", "");
    d.outletOn = j.value("outletOn", false);
    d.status   = j.value("status",   "");
    d.lastSeen = j.value("lastSeen", "");
    // currentPowerW é nullable no Java (Double, não double)
    const auto& pw = j.at("currentPowerW");
    if (!pw.is_null())
        d.currentPowerW = pw.get<double>();
    return d;
}

inline DeviceDetail parseDetail(const json& j) {
    DeviceDetail d;
    d.id       = j.value("id",       "");
    d.name     = j.value("name",     "");
    d.location = j.value("location", "");
    d.outletOn = j.value("outletOn", false);
    d.status   = j.value("status",   "");
    d.lastSeen = j.value("lastSeen", "");
    // measurement é nullable no Java (null antes do primeiro tick do mock)
    if (j.contains("measurement") && !j.at("measurement").is_null())
        d.measurement = parseMeasurement(j.at("measurement"));
    return d;
}

inline CommandResult parseCommandResult(const json& j) {
    CommandResult r;
    r.deviceId       = j.value("deviceId",       "");
    r.commandApplied = j.value("commandApplied", "");
    r.outletOn       = j.value("outletOn",       false);
    r.message        = j.value("message",        "");
    return r;
}

// ═══════════════════════════════════════════════════════════════════════════
// Client — wrapper sobre cpp-httplib
// ═══════════════════════════════════════════════════════════════════════════

class Client {
public:
    /**
     * @param host  Hostname ou IP do servidor (ex.: "localhost")
     * @param port  Porta TCP (application.properties → server.port=8080)
     */
    explicit Client(const std::string& host, int port)
        : http_(host, port)
    {
        http_.set_connection_timeout(5, 0);
        http_.set_read_timeout(10, 0);
        http_.set_write_timeout(5, 0);
    }

    // ── GET /api/devices ──────────────────────────────────────────────────
    std::vector<DeviceSummary> listDevices() {
        auto res = doGet("/api/devices");
        std::vector<DeviceSummary> out;
        for (const auto& item : json::parse(res->body))
            out.push_back(parseSummary(item));
        return out;
    }

    // ── GET /api/devices/{id} ─────────────────────────────────────────────
    // Lança runtime_error("[404] ...") se o id não existir.
    DeviceDetail getDevice(const std::string& id) {
        return parseDetail(json::parse(doGet("/api/devices/" + id)->body));
    }

    // ── POST /api/devices/{id}/command ────────────────────────────────────
    // @param type  "OUTLET_ON" ou "OUTLET_OFF"
    // Lança runtime_error("[400] ...") se type for inválido.
    CommandResult sendCommand(const std::string& id, const std::string& type) {
        const json body = {{"type", type}};
        return parseCommandResult(
            json::parse(doPost("/api/devices/" + id + "/command", body.dump())->body)
        );
    }

    // ── POST /api/devices/{id}/refresh ────────────────────────────────────
    // Lança runtime_error("[409] ...") se ainda não há medições disponíveis.
    Measurement refresh(const std::string& id) {
        return parseMeasurement(
            json::parse(doPost("/api/devices/" + id + "/refresh", "")->body)
        );
    }

private:
    httplib::Client http_;

    // Executa GET e verifica erros de rede / HTTP
    httplib::Result doGet(const std::string& path) {
        auto res = http_.Get(path);
        check(res, "GET " + path);
        return res;
    }

    // Executa POST com body JSON e verifica erros de rede / HTTP
    httplib::Result doPost(const std::string& path, const std::string& body) {
        auto res = http_.Post(path, body, "application/json");
        check(res, "POST " + path);
        return res;
    }

    /**
     * Verifica o resultado da requisição.
     *
     * Falha de rede  → lança com descrição do erro (sem resposta do servidor).
     * Erro HTTP 4xx  → lança "[<status>] <campo error do JSON>" se disponível.
     */
    static void check(const httplib::Result& res, const std::string& ctx) {
        if (!res) {
            std::string reason;
            switch (res.error()) {
                case httplib::Error::Connection: reason = "conexão recusada";     break;
                case httplib::Error::Read:       reason = "timeout na leitura";   break;
                case httplib::Error::Write:      reason = "timeout na escrita";   break;
                default:                         reason = "erro de rede";         break;
            }
            throw std::runtime_error(ctx + " — " + reason);
        }

        if (res->status >= 400) {
            // GlobalExceptionHandler serializa { "error": "<mensagem>" }
            std::string msg;
            const auto j = json::parse(res->body, nullptr, false);
            if (!j.is_discarded() && j.is_object() && j.contains("error"))
                msg = j["error"].get<std::string>();
            else
                msg = res->body.empty() ? "HTTP " + std::to_string(res->status)
                                        : res->body;
            throw std::runtime_error("[" + std::to_string(res->status) + "] " + msg);
        }
    }
};

} // namespace sg
