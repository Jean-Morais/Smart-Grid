"""SmartGrid — cliente em Python."""

from __future__ import annotations

import argparse
import json
import sys
from dataclasses import dataclass
from datetime import datetime
from typing import Any, Optional, Sequence
from urllib import error, request


# ─── Códigos ANSI ─────────────────────────────────────────────────────────
RST = "\033[0m"
BLD = "\033[1m"
DIM = "\033[2m"
RED = "\033[31m"
GRN = "\033[32m"
YEL = "\033[33m"
MAG = "\033[35m"
CYN = "\033[36m"


# ─── DTOs ─────────────────────────────────────────────────────────────────
@dataclass(slots=True)
class Measurement:
    device_id: str
    voltage: float
    current: float
    power_factor: float
    connected_seconds: int
    connected_time: str
    power: float
    energy_wh: float
    measured_at: str


@dataclass(slots=True)
class DeviceSummary:
    id: str
    name: str
    location: str
    outlet_on: bool
    status: str
    current_power_w: Optional[float]
    last_seen: str


@dataclass(slots=True)
class DeviceDetail:
    id: str
    name: str
    location: str
    outlet_on: bool
    status: str
    last_seen: str
    measurement: Optional[Measurement]


@dataclass(slots=True)
class CommandResult:
    device_id: str
    command_applied: str
    outlet_on: bool
    message: str


# ─── Parser JSON ─────────────────────────────────────────────────────────
def _parse_measurement(data: dict[str, Any]) -> Measurement:
    return Measurement(
        device_id=str(data.get("deviceId", "")),
        voltage=float(data.get("voltage", 0.0)),
        current=float(data.get("current", 0.0)),
        power_factor=float(data.get("powerFactor", 0.0)),
        connected_seconds=int(data.get("connectedSeconds", 0)),
        connected_time=str(data.get("connectedTime", "")),
        power=float(data.get("power", 0.0)),
        energy_wh=float(data.get("energyWh", 0.0)),
        measured_at=str(data.get("measuredAt", "")),
    )


def _parse_summary(data: dict[str, Any]) -> DeviceSummary:
    return DeviceSummary(
        id=str(data.get("id", "")),
        name=str(data.get("name", "")),
        location=str(data.get("location", "")),
        outlet_on=bool(data.get("outletOn", False)),
        status=str(data.get("status", "")),
        current_power_w=(None if data.get("currentPowerW") is None else float(data["currentPowerW"])),
        last_seen=str(data.get("lastSeen", "")),
    )


def _parse_detail(data: dict[str, Any]) -> DeviceDetail:
    measurement = None
    if data.get("measurement") is not None:
        measurement = _parse_measurement(data["measurement"])
    return DeviceDetail(
        id=str(data.get("id", "")),
        name=str(data.get("name", "")),
        location=str(data.get("location", "")),
        outlet_on=bool(data.get("outletOn", False)),
        status=str(data.get("status", "")),
        last_seen=str(data.get("lastSeen", "")),
        measurement=measurement,
    )


def _parse_command_result(data: dict[str, Any]) -> CommandResult:
    return CommandResult(
        device_id=str(data.get("deviceId", "")),
        command_applied=str(data.get("commandApplied", "")),
        outlet_on=bool(data.get("outletOn", False)),
        message=str(data.get("message", "")),
    )


# ─── Cliente HTTP ────────────────────────────────────────────────────────
class ApiClient:
    def __init__(self, host: str, port: int) -> None:
        self.base_url = f"http://{host}:{port}"
        self.timeout = 10

    def _request(self, method: str, path: str, body: Optional[dict[str, Any]] = None) -> Any:
        url = self.base_url + path
        headers = {"Accept": "application/json"}
        data_bytes = None

        if body is not None:
            headers["Content-Type"] = "application/json"
            data_bytes = json.dumps(body).encode("utf-8")

        req = request.Request(url, data=data_bytes, headers=headers, method=method)

        try:
            with request.urlopen(req, timeout=self.timeout) as resp:
                raw = resp.read().decode("utf-8") if resp.readable() else ""
                if not raw:
                    return None
                return json.loads(raw)

        except error.HTTPError as exc:
            body_text = exc.read().decode("utf-8", errors="replace") if exc.fp else ""
            msg = body_text.strip() or f"HTTP {exc.code}"
            try:
                parsed = json.loads(body_text)
                if isinstance(parsed, dict) and "error" in parsed:
                    msg = str(parsed["error"])
            except Exception:
                pass
            raise RuntimeError(f"[{exc.code}] {msg}") from None
        except error.URLError as exc:
            reason = getattr(exc, "reason", exc)
            raise RuntimeError(f"{method} {path} — erro de rede: {reason}") from None

    def list_devices(self) -> list[DeviceSummary]:
        payload = self._request("GET", "/api/devices")
        return [_parse_summary(item) for item in (payload or [])]

    def get_device(self, device_id: str) -> DeviceDetail:
        payload = self._request("GET", f"/api/devices/{device_id}")
        return _parse_detail(payload)

    def send_command(self, device_id: str, command_type: str) -> CommandResult:
        payload = self._request("POST", f"/api/devices/{device_id}/command", {"type": command_type})
        return _parse_command_result(payload)

    def refresh(self, device_id: str) -> Measurement:
        payload = self._request("POST", f"/api/devices/{device_id}/refresh")
        return _parse_measurement(payload)


# ─── Formatação de tela ──────────────────────────────────────────────────
def pad_to(text: str, width: int) -> str:
    if width <= 0:
        return ""
    if len(text) >= width:
        return text[:width]
    return text + (" " * (width - len(text)))


def fmt_float(value: float, precision: int = 2) -> str:
    return f"{value:.{precision}f}"


def fmt_date(iso: str) -> str:
    if len(iso) >= 19:
        s = iso[:19]
        return s.replace("T", " ", 1)
    return iso


def show_error(message: str) -> None:
    print(f"\n  {RED}✖  {message}{RST}\n")


def show_menu() -> None:
    print(
        f"""
{CYN}{BLD}  Smart Grid — Cliente Python{RST}
  [1]  Listar todos os dispositivos
  [2]  Detalhes de um dispositivo
  [3]  Enviar comando  (liga / desliga)
  [4]  Solicitar atualização  (refresh)
  [0]  Sair
""".rstrip()
    )


def show_connection(host: str, port: int, count: int) -> None:
    print(f"{GRN}  ✔  Conectado! {count} controlador(es) disponível(is).{RST}")
    print(f"{DIM}  Servidor: {host}:{port}{RST}")


def _choose_device(devs: Sequence[DeviceSummary]) -> str:
    print("  Selecione (número ou ID): ", end="", flush=True)
    try:
        raw = input().strip()
    except EOFError:
        return ""

    if not raw:
        return ""

    if raw.isdigit():
        idx = int(raw)
        if 1 <= idx <= len(devs):
            return devs[idx - 1].id
        show_error(f"Número fora do intervalo (1–{len(devs)}).")
        return ""

    return raw


def show_device_list(devs: Sequence[DeviceSummary]) -> None:
    W_NUM, W_ID, W_NM, W_LC, W_OUT, W_POW, W_STA = 2, 9, 24, 10, 4, 10, 7

    def seg(w: int) -> str:
        return "─" * (w + 2)

    def line(l: str, m: str, r: str) -> None:
        print(f"{l}{seg(W_NUM)}{m}{seg(W_ID)}{m}{seg(W_NM)}{m}{seg(W_LC)}{m}{seg(W_OUT)}{m}{seg(W_POW)}{m}{seg(W_STA)}{r}")

    print(f"\n{CYN}{BLD}  ⚡  Controladores Smart Grid{RST}")
    line("┌", "┬", "┐")
    print(
        f"{BLD}│ {pad_to('#', W_NUM)} │ {pad_to('ID', W_ID)} │ {pad_to('Nome', W_NM)} │ "
        f"{pad_to('Local', W_LC)} │ {pad_to('OUT', W_OUT)} │ {pad_to('Potência', W_POW)} │ "
        f"{pad_to('Status', W_STA)} │{RST}"
    )
    line("├", "┼", "┤")

    if not devs:
        print(f"│ {pad_to('-', W_NUM)} │ {pad_to('(vazio)', W_ID)} │ {pad_to('Nenhum', W_NM)} │ {pad_to('', W_LC)} │ {pad_to('', W_OUT)} │ {pad_to('', W_POW)} │ {pad_to('', W_STA)} │")
        line("└", "┴", "┘")
        print()
        return

    for i, d in enumerate(devs, start=1):
        num = str(i)
        out = "ON" if d.outlet_on else "OFF"
        power = f"{fmt_float(d.current_power_w)} W" if d.current_power_w is not None else "-"
        print(
            f"│ {pad_to(num, W_NUM)} │ {pad_to(d.id, W_ID)} │ {pad_to(d.name, W_NM)} │ "
            f"{pad_to(d.location, W_LC)} │ {pad_to(out, W_OUT)} │ {pad_to(power, W_POW)} │ "
            f"{pad_to(d.status, W_STA)} │"
        )

    line("└", "┴", "┘")
    print()


def show_device_detail(dev: DeviceDetail) -> None:
    print(f"\n{MAG}{BLD}  Detalhes do dispositivo{RST}")
    print(f"  ID:       {dev.id}")
    print(f"  Nome:     {dev.name}")
    print(f"  Local:    {dev.location}")
    print(f"  Tomada:   {'ON' if dev.outlet_on else 'OFF'}")
    print(f"  Status:   {dev.status}")
    print(f"  LastSeen: {fmt_date(dev.last_seen)}")
    if dev.measurement is None:
        print(f"  Medição:  {YEL}indisponível{RST}\n")
        return

    m = dev.measurement
    print(f"  Voltagem: {m.voltage:.2f} V")
    print(f"  Corrente: {m.current:.2f} A")
    print(f"  FP:       {m.power_factor:.2f}")
    print(f"  Potência: {m.power:.2f} W")
    print(f"  Energia:  {m.energy_wh:.2f} Wh")
    print(f"  Tempo:    {m.connected_time}")
    print(f"  Medido:   {fmt_date(m.measured_at)}\n")


def show_command_result(res: CommandResult) -> None:
    print(f"\n{GRN}{BLD}  Comando aplicado{RST}")
    print(f"  Dispositivo: {res.device_id}")
    print(f"  Tipo:        {res.command_applied}")
    print(f"  Tomada:      {'ON' if res.outlet_on else 'OFF'}")
    print(f"  Mensagem:    {res.message}\n")


def show_measurement(m: Measurement) -> None:
    print(f"\n{CYN}{BLD}  Atualização recebida{RST}")
    print(f"  Dispositivo: {m.device_id}")
    print(f"  Voltagem:    {m.voltage:.2f} V")
    print(f"  Corrente:    {m.current:.2f} A")
    print(f"  FP:          {m.power_factor:.2f}")
    print(f"  Tempo:       {m.connected_time}")
    print(f"  Potência:    {m.power:.2f} W")
    print(f"  Energia:     {m.energy_wh:.2f} Wh")
    print(f"  Medido:      {fmt_date(m.measured_at)}\n")


# ─── Ações do menu ───────────────────────────────────────────────────────
def action_list(api: ApiClient) -> None:
    show_device_list(api.list_devices())


def action_detail(api: ApiClient) -> None:
    devs = api.list_devices()
    show_device_list(devs)
    device_id = _choose_device(devs)
    if not device_id:
        return
    show_device_detail(api.get_device(device_id))


def action_command(api: ApiClient) -> None:
    devs = api.list_devices()
    show_device_list(devs)
    device_id = _choose_device(devs)
    if not device_id:
        return

    print("  Comando: [1] OUTLET_ON   [2] OUTLET_OFF : ", end="", flush=True)
    try:
        sel = input().strip()
    except EOFError:
        return

    if sel in {"1", "OUTLET_ON"}:
        command = "OUTLET_ON"
    elif sel in {"2", "OUTLET_OFF"}:
        command = "OUTLET_OFF"
    else:
        show_error("Opção inválida. Use 1 (ligar) ou 2 (desligar).")
        return

    show_command_result(api.send_command(device_id, command))


def action_refresh(api: ApiClient) -> None:
    devs = api.list_devices()
    show_device_list(devs)
    device_id = _choose_device(devs)
    if not device_id:
        return
    show_measurement(api.refresh(device_id))


def parse_args(argv: Sequence[str]) -> tuple[str, int]:
    parser = argparse.ArgumentParser(
        prog="smartgrid_python_client",
        description="Cliente de terminal para a API SmartGrid.",
    )
    parser.add_argument("host", nargs="?", default="localhost")
    parser.add_argument("port", nargs="?", type=int, default=8080)
    ns = parser.parse_args(list(argv))
    return ns.host, ns.port


def main(argv: Sequence[str] | None = None) -> int:
    host, port = parse_args(sys.argv[1:] if argv is None else argv)
    api = ApiClient(host, port)

    print(f"{CYN}{BLD}\n  Smart Grid — Cliente Python{RST}")
    try:
        devs = api.list_devices()
        show_connection(host, port, len(devs))
    except Exception as exc:
        show_error(f"Não foi possível conectar: {exc}")
        print(f"  Confirme que o servidor está em {host}:{port}")
        return 1

    while True:
        show_menu()
        try:
            line = input().strip()
        except EOFError:
            break

        opt = line[:1] if line else ""
        try:
            if opt == "1":
                action_list(api)
            elif opt == "2":
                action_detail(api)
            elif opt == "3":
                action_command(api)
            elif opt == "4":
                action_refresh(api)
            elif opt == "0":
                print(f"\n{CYN}  Encerrando. Até logo!{RST}\n")
                return 0
            else:
                print("\n  Opção inválida. Digite 0–4.\n")
        except Exception as exc:
            show_error(str(exc))

    return 0


if __name__ == "__main__":
    raise SystemExit(main())
