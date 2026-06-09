cmake_minimum_required(VERSION 3.16)
project(smartgrid_cpp_client CXX)

# ── Padrão C++ ─────────────────────────────────────────────────────────────
set(CMAKE_CXX_STANDARD 17)
set(CMAKE_CXX_STANDARD_REQUIRED ON)
set(CMAKE_CXX_EXTENSIONS OFF)

# ── Dependências (baixadas na primeira vez que você roda cmake) ─────────────
#
#   Alternativa manual: copie httplib.h e json.hpp para src/ e remova o
#   bloco FetchContent inteiro. Inclua-os normalmente no código.
#
include(FetchContent)

# cpp-httplib — cliente HTTP header-only
# Compatível com o servidor Spring Boot 4.0.6 via HTTP simples (sem TLS).
FetchContent_Declare(
    cpp_httplib
    GIT_REPOSITORY https://github.com/yhirose/cpp-httplib.git
    GIT_TAG        v0.15.3
)

# nlohmann/json — parser JSON header-only
FetchContent_Declare(
    nlohmann_json
    GIT_REPOSITORY https://github.com/nlohmann/json.git
    GIT_TAG        v3.11.3
)

FetchContent_MakeAvailable(cpp_httplib nlohmann_json)

# ── Executável ─────────────────────────────────────────────────────────────
add_executable(smartgrid_client src/main.cpp)

target_include_directories(smartgrid_client PRIVATE src)

target_link_libraries(smartgrid_client PRIVATE
    httplib::httplib
    nlohmann_json::nlohmann_json
)

find_package(Threads REQUIRED)
target_link_libraries(smartgrid_client PRIVATE Threads::Threads)

if(WIN32)
    target_link_libraries(smartgrid_client PRIVATE ws2_32)
endif()

# ── Modo de uso ────────────────────────────────────────────────────────────
#
#   mkdir build && cd build && cmake .. && cmake --build .
#
#   Executável gerado: build/smartgrid_client  (ou build\smartgrid_client.exe)
#
#   Argumentos opcionais:
#     ./smartgrid_client                   → localhost:8080 (padrão)
#     ./smartgrid_client 192.168.1.5       → host customizado, porta 8080
#     ./smartgrid_client 192.168.1.5 9090  → host e porta customizados
