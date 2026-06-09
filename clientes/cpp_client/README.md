# SmartGrid — Cliente C++

Cliente de terminal que consome a REST API do servidor **Java / Spring Boot 4**.

---

## Estrutura do projeto

```
cpp_client/
├── CMakeLists.txt
└── src/
    ├── main.cpp          ← menu interativo, entrada do usuário
    ├── api_client.hpp    ← DTOs + wrapper HTTP (cpp-httplib + nlohmann/json)
    └── display.hpp       ← renderização do terminal (tabelas, cards, cores)
```

---

## Dependências (gerenciadas pelo CMake)

| Biblioteca | Papel |
|---|---|
| [cpp-httplib v0.15.3](https://github.com/yhirose/cpp-httplib) | Requisições HTTP (header-only) |
| [nlohmann/json v3.11.3](https://github.com/nlohmann/json) | Parse de JSON (header-only) |

O `CMakeLists.txt` baixa e compila as duas na primeira execução do `cmake`.  
Não é necessário instalar nada manualmente.

---

## Como compilar

### Linux / macOS

```bash
# 1. Gerar o sistema de build (primeira vez: baixa as dependências)
mkdir build && cd build
cmake ..

# 2. Compilar
cmake --build .

# 3. Executável gerado em:
./smartgrid_client
```

### Windows (PowerShell + Visual Studio)

```powershell
mkdir build; cd build
cmake ..
cmake --build . --config Release
.\Release\smartgrid_client.exe
```

---

## Como usar

```bash
# Servidor em localhost:8080 (padrão)
./smartgrid_client

# Servidor em outro endereço
./smartgrid_client 192.168.1.10

# Servidor em outro endereço e porta
./smartgrid_client 192.168.1.10 9090
```

---

## Menu interativo

```
  ┌─────────────────────────────────────┐
  │     Smart Grid  —  Cliente C++      │
  └─────────────────────────────────────┘
  [1]  Listar todos os dispositivos
  [2]  Detalhes de um dispositivo
  [3]  Enviar comando  (liga / desliga)
  [4]  Solicitar atualização  (refresh)
  [0]  Sair
```

Nas opções 2, 3 e 4, o cliente exibe a tabela de dispositivos e pede uma seleção.
Você pode digitar o **número da linha** (ex.: `2`) ou o **ID direto** (ex.: `ctrl-002`).

---

## Endpoints consumidos

| Opção | Método | Endpoint |
|---|---|---|
| 1 — Listar | `GET` | `/api/devices` |
| 2 — Detalhes | `GET` | `/api/devices/{id}` |
| 3 — Comando | `POST` | `/api/devices/{id}/command` |
| 4 — Refresh | `POST` | `/api/devices/{id}/refresh` |

---

## Compatibilidade com o servidor

Desenvolvido e testado contra **Spring Boot 4.0.6 / Java 21**.  
Os endpoints, nomes de campos JSON e códigos de erro são idênticos  
aos da versão anterior (Spring Boot 3.2.0 / Java 17).
