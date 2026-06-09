/**
 * main.cpp — Cliente terminal Smart Grid (C++)
 * ─────────────────────────────────────────────
 * Consome a REST API do servidor Java / Spring Boot 4.
 *
 * Uso:
 *   ./smartgrid_client                    → localhost:8080  (padrão)
 *   ./smartgrid_client <host>             → host:8080
 *   ./smartgrid_client <host> <porta>     → host:porta
 */

#include <iostream>
#include <stdexcept>
#include <string>
#include <vector>

#include "api_client.hpp"
#include "display.hpp"

// ─── Resolução de seleção do usuário ─────────────────────────────────────

/**
 * Lê uma linha do stdin e devolve o ID do controlador.
 *
 * Aceita:
 *   "2"        → segundo dispositivo da lista exibida (índice 1-based)
 *   "ctrl-003" → ID direto
 *
 * Retorna string vazia se o usuário pressionar Enter sem digitar nada,
 * o número estiver fora do intervalo, ou ocorrer EOF (Ctrl+D / Ctrl+Z).
 */
static std::string pickDevice(const std::vector<sg::DeviceSummary>& devs) {
    std::cout << "  Selecione (número ou ID): ";
    std::string input;
    if (!std::getline(std::cin, input) || input.empty())
        return {};

    // Tenta tratar como índice numérico
    try {
        const int idx = std::stoi(input);
        if (idx >= 1 && idx <= static_cast<int>(devs.size()))
            return devs[idx - 1].id;
        ui::showError("Número fora do intervalo (1–" +
                      std::to_string(devs.size()) + ").");
        return {};
    } catch (...) {
        // Não é número → trata como ID literal (ex.: "ctrl-003")
        return input;
    }
}

// ─── Ações do menu ────────────────────────────────────────────────────────

static void actionList(sg::Client& api) {
    ui::showDeviceList(api.listDevices());
}

static void actionDetail(sg::Client& api) {
    auto devs = api.listDevices();
    ui::showDeviceList(devs);
    const std::string id = pickDevice(devs);
    if (id.empty()) return;
    ui::showDeviceDetail(api.getDevice(id));
}

static void actionCommand(sg::Client& api) {
    auto devs = api.listDevices();
    ui::showDeviceList(devs);

    const std::string id = pickDevice(devs);
    if (id.empty()) return;

    std::cout << "  Comando: [1] OUTLET_ON   [2] OUTLET_OFF : ";
    std::string sel;
    if (!std::getline(std::cin, sel) || sel.empty()) return;

    std::string type;
    if      (sel == "1" || sel == "OUTLET_ON")  type = "OUTLET_ON";
    else if (sel == "2" || sel == "OUTLET_OFF") type = "OUTLET_OFF";
    else { ui::showError("Opção inválida. Use 1 (ligar) ou 2 (desligar)."); return; }

    ui::showCommandResult(api.sendCommand(id, type));
}

static void actionRefresh(sg::Client& api) {
    auto devs = api.listDevices();
    ui::showDeviceList(devs);
    const std::string id = pickDevice(devs);
    if (id.empty()) return;
    ui::showMeasurement(api.refresh(id));
}

// ─── main ─────────────────────────────────────────────────────────────────

int main(int argc, char* argv[]) {
    std::string host = "localhost";
    int         port = 8080;

    if (argc >= 2) host = argv[1];
    if (argc >= 3) {
        try { port = std::stoi(argv[2]); }
        catch (...) { /* porta inválida → mantém padrão */ }
    }

    // Cabeçalho
    std::cout << ui::CYN << ui::BLD
              << "\n  Smart Grid — Cliente C++\n"
              << ui::RST
              << ui::DIM << "  Servidor: " << host << ':' << port << '\n'
              << ui::RST;

    sg::Client api(host, port);

    // Teste de conexão inicial ─────────────────────────────────────────────
    try {
        const auto devs = api.listDevices();
        std::cout << ui::GRN << "  ✔  Conectado! "
                  << devs.size() << " controlador(es) disponível(is).\n"
                  << ui::RST;
    } catch (const std::exception& e) {
        ui::showError(std::string("Não foi possível conectar: ") + e.what());
        std::cout << "  Confirme que o servidor está em "
                  << host << ':' << port << '\n';
        return 1;
    }

    // Loop do menu ─────────────────────────────────────────────────────────
    for (;;) {
        ui::showMenu();

        std::string line;
        if (!std::getline(std::cin, line)) break;  // EOF (Ctrl+D)

        const char opt = line.empty() ? '\0' : line[0];

        try {
            switch (opt) {
            case '1': actionList(api);    break;
            case '2': actionDetail(api);  break;
            case '3': actionCommand(api); break;
            case '4': actionRefresh(api); break;
            case '0':
                std::cout << '\n' << ui::CYN
                          << "  Encerrando. Até logo!\n\n" << ui::RST;
                return 0;
            default:
                std::cout << "\n  Opção inválida. Digite 0–4.\n";
            }
        } catch (const std::exception& e) {
            // Erros de rede ou HTTP chegam aqui com mensagem descritiva
            ui::showError(e.what());
        }
    }

    return 0;
}
