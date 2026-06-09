#pragma once

/**
 * display.hpp
 * ───────────
 * Funções de saída para o terminal: menu, tabela de dispositivos,
 * card de detalhes, resultado de comando e mensagens de erro.
 *
 * Usa códigos ANSI para cor/negrito (Linux, macOS, Windows Terminal ≥ Win10).
 * A técnica para alinhar colunas coloridas: aplique padding ANTES da cor —
 * assim o terminal enxerga o comprimento visual correto e a tabela não torce.
 */

#include <iomanip>
#include <iostream>
#include <sstream>
#include <string>
#include <vector>

#include "api_client.hpp"

namespace ui {

// ─── Códigos ANSI ─────────────────────────────────────────────────────────
inline const std::string RST = "\033[0m";
inline const std::string BLD = "\033[1m";
inline const std::string DIM = "\033[2m";
inline const std::string RED = "\033[31m";
inline const std::string GRN = "\033[32m";
inline const std::string YEL = "\033[33m";
inline const std::string MAG = "\033[35m";
inline const std::string CYN = "\033[36m";

// ─── Utilitários de texto ─────────────────────────────────────────────────

/** Preenche ou trunca `s` para exatamente `w` caracteres visíveis. */
static std::string padTo(const std::string& s, int w) {
    if (w <= 0) return {};
    if (static_cast<int>(s.size()) >= w) return s.substr(0, w);
    return s + std::string(w - static_cast<int>(s.size()), ' ');
}

/** Formata `v` com `p` casas decimais. */
static std::string fmtF(double v, int p = 2) {
    std::ostringstream o;
    o << std::fixed << std::setprecision(p) << v;
    return o.str();
}

/**
 * Formata um timestamp ISO-8601 para "AAAA-MM-DD HH:MM:SS".
 * O Java serializa LocalDateTime com 'T' separando data e hora.
 */
static std::string fmtDate(const std::string& iso) {
    if (iso.size() < 19) return iso;
    std::string s = iso.substr(0, 19);
    if (s[10] == 'T') s[10] = ' ';
    return s;
}

// ─── Helpers de célula para tabelas ──────────────────────────────────────

/**
 * Célula sem cor: " <texto padTo w> │"
 * Sempre produz exatamente w+3 bytes imprimíveis (1 espaço + w + 1 espaço + │).
 */
static std::string cell(const std::string& s, int w) {
    return " " + padTo(s, w) + " │";
}

/**
 * Célula colorida, alinhamento correto.
 *
 * Faz o padding do TEXTO PURO primeiro, depois envolve com ANSI.
 * O terminal conta só os caracteres visíveis para o alinhamento, então
 * a tabela fica alinhada mesmo com os bytes extras dos códigos de cor.
 *
 *   " " + <color> + padTo(text,w) + RST + " │"
 *
 * Se text > w, é truncado antes de colorir.
 */
static std::string colorCell(const std::string& text,
                             const std::string& color, int w) {
    return " " + color + padTo(text, w) + RST + " │";
}

// ─── Tabela de dispositivos ───────────────────────────────────────────────

/**
 * Exibe todos os controladores em tabela com bordas Unicode.
 * Linhas numeradas para facilitar a seleção pelo usuário.
 */
inline void showDeviceList(const std::vector<sg::DeviceSummary>& devs) {
    // Larguras das colunas (apenas caracteres visíveis, sem bordas)
    constexpr int W_NUM = 2;   // "1"–"99"
    constexpr int W_ID  = 9;   // "ctrl-005"
    constexpr int W_NM  = 24;  // "Cozinha - Micro-ondas"
    constexpr int W_LC  = 10;  // "Quarto 1"
    constexpr int W_OUT = 4;   // "ON" / "OFF"
    constexpr int W_POW = 10;  // "1234.56 W"
    constexpr int W_STA = 7;   // "ONLINE"

    // Linha horizontal ─────────────────────────────────────────────────────
    auto seg  = [](int w) { return std::string(w + 2, '─'); };
    auto line = [&](char l, char m, char r) {
        std::cout << l
                  << seg(W_NUM) << m << seg(W_ID)  << m << seg(W_NM)
                  << m << seg(W_LC)  << m << seg(W_OUT) << m << seg(W_POW)
                  << m << seg(W_STA) << r << '\n';
    };

    std::cout << '\n' << CYN << BLD << "  ⚡  Controladores Smart Grid\n" << RST;
    line('┌','┬','┐');

    // Cabeçalho (negrito, sem ANSI nas células para não distorcer o padding)
    std::cout << BLD << "│"
              << cell("#",        W_NUM) << cell("ID",       W_ID)
              << cell("Nome",     W_NM)  << cell("Cômodo",   W_LC)
              << cell("Tom.",     W_OUT) << cell("Potência", W_POW)
              << cell("Status",   W_STA) << RST << '\n';

    line('├','┼','┤');

    for (int i = 0; i < static_cast<int>(devs.size()); ++i) {
        const auto& d = devs[i];

        const std::string outStr = d.outletOn ? "ON" : "OFF";
        const std::string powStr = d.currentPowerW.has_value()
            ? fmtF(*d.currentPowerW) + " W"
            : "—";

        std::cout << "│"
                  << cell(std::to_string(i + 1), W_NUM)
                  << cell(d.id,       W_ID)
                  << cell(d.name,     W_NM)
                  << cell(d.location, W_LC)
                  << colorCell(outStr, d.outletOn ? GRN : RED,                   W_OUT)
                  << colorCell(powStr, d.currentPowerW.has_value() ? YEL : DIM,  W_POW)
                  << colorCell(d.status, d.status == "ONLINE" ? GRN : RED,       W_STA)
                  << '\n';
    }

    line('└','┴','┘');
    std::cout << '\n';
}

// ─── Card de detalhes de um dispositivo ──────────────────────────────────

inline void showDeviceDetail(const sg::DeviceDetail& d) {
    // Label fixo de 14 chars + valor (com cor opcional)
    auto row = [](const std::string& label, const std::string& val,
                  const std::string& color = "") {
        std::cout << "  │  " << BLD << padTo(label, 14) << RST
                  << ' ' << (color.empty() ? val : color + val + RST) << '\n';
    };

    std::cout << '\n' << CYN << BLD << "  ┌─ " << d.name << "\n" << RST;

    row("ID",       d.id);
    row("Cômodo",   d.location);
    row("Status",   d.status,            d.status == "ONLINE" ? GRN : RED);
    row("Tomada",   d.outletOn ? "ON" : "OFF", d.outletOn ? GRN : RED);
    row("Visto em", fmtDate(d.lastSeen), DIM);

    if (d.measurement.has_value()) {
        const auto& m = *d.measurement;
        std::cout << "  ├─ Medição\n";
        row("Tensão",     fmtF(m.voltage)      + " V",  YEL);
        row("Corrente",   fmtF(m.current, 3)   + " A");
        row("Fat. pot.",  fmtF(m.powerFactor, 3));
        row("Potência",   fmtF(m.power)        + " W",  GRN);
        row("Energia",    fmtF(m.energyWh, 4)  + " Wh", CYN);
        row("Tempo lig.", m.connectedTime,               MAG);
        row("Medido em",  fmtDate(m.measuredAt),         DIM);
    } else {
        std::cout << "  │  " << DIM << "Sem medições disponíveis ainda.\n" << RST;
    }

    std::cout << "  └" << std::string(38, '─') << "\n\n";
}

// ─── Resultado de comando ─────────────────────────────────────────────────

inline void showCommandResult(const sg::CommandResult& r) {
    const std::string stateColor = r.outletOn ? GRN : RED;
    const std::string stateStr   = r.outletOn ? "ON" : "OFF";

    std::cout << '\n'
              << "  " << GRN << "✔  " << r.message << RST << '\n'
              << "  Dispositivo " << BLD << '[' << r.deviceId << ']' << RST
              << " → tomada " << stateColor << BLD << stateStr << RST << "\n\n";
}

// ─── Leitura atualizada (resultado do refresh) ───────────────────────────

inline void showMeasurement(const sg::Measurement& m) {
    auto row = [](const std::string& label, const std::string& val,
                  const std::string& color = "") {
        std::cout << "  " << BLD << padTo(label, 14) << RST
                  << ' ' << (color.empty() ? val : color + val + RST) << '\n';
    };

    std::cout << '\n' << "  " << GRN << "✔  Leitura atualizada!\n" << RST;
    row("Potência:",   fmtF(m.power)       + " W",  YEL);
    row("Tensão:",     fmtF(m.voltage)     + " V");
    row("Corrente:",   fmtF(m.current, 3)  + " A");
    row("Energia:",    fmtF(m.energyWh, 4) + " Wh", CYN);
    row("Tempo lig.:", m.connectedTime,              MAG);
    row("Medido em:",  fmtDate(m.measuredAt),        DIM);
    std::cout << '\n';
}

// ─── Menu principal ───────────────────────────────────────────────────────

inline void showMenu() {
    std::cout << CYN
              << "\n  ┌─────────────────────────────────────┐\n"
              <<   "  │     Smart Grid  —  Cliente C++      │\n"
              <<   "  └─────────────────────────────────────┘\n"
              << RST
              << "  [1]  Listar todos os dispositivos\n"
              << "  [2]  Detalhes de um dispositivo\n"
              << "  [3]  Enviar comando  (liga / desliga)\n"
              << "  [4]  Solicitar atualização  (refresh)\n"
              << "  [0]  Sair\n"
              << "\n  Opção: ";
}

// ─── Mensagem de erro ─────────────────────────────────────────────────────

inline void showError(const std::string& msg) {
    std::cerr << "\n  " << RED << "✘  " << msg << RST << "\n\n";
}

} // namespace ui
