# Smart Grid

![Status](https://img.shields.io/badge/status-em%20desenvolvimento-yellow)
![Java](https://img.shields.io/badge/Java-21-orange)
![Spring Boot](https://img.shields.io/badge/Spring%20Boot-4.0.6-brightgreen)
![Python](https://img.shields.io/badge/Python-3%2B-blue)
![C%2B%2B](https://img.shields.io/badge/C%2B%2B-17-00599C)
![MQTT](https://img.shields.io/badge/MQTT-Publish%20%26%20Subscribe-lightgrey)
![API REST](https://img.shields.io/badge/API%20REST-HTTP-blueviolet)

> Plataforma local para monitoramento e controle de energia em uma residência inteligente, concebida para representar o comportamento de uma solução real com integração futura a hardware físico.

---

## Visão geral

O **Smart Grid** é um projeto voltado ao monitoramento do consumo e do estado operacional de dispositivos conectados em uma residência. A proposta é acompanhar grandezas elétricas, controlar o acionamento de tomadas e disponibilizar as informações de forma centralizada para consulta e supervisão em rede local.

O sistema foi pensado como uma base próxima de um cenário real de automação residencial. Em vez de depender exclusivamente de hardware físico durante o desenvolvimento, a solução utiliza elementos simulados apenas para validação, testes e demonstração do comportamento esperado. Isso permite evoluir a arquitetura com mais segurança até uma implementação real, mantendo o foco no problema de domínio: gestão inteligente de energia.

Na prática, a aplicação permite observar o estado dos dispositivos, registrar leituras elétricas, calcular potência e energia e executar comandos como ligar e desligar tomadas. Todo o fluxo foi organizado para ser modular, claro e fácil de evoluir.

---

## O que o sistema entrega

- monitoramento de dispositivos em uma rede local;
- acompanhamento do estado das tomadas e da conectividade;
- registro da última telemetria recebida de cada dispositivo;
- cálculo de potência e energia a partir de tensão, corrente e fator de potência;
- envio de comandos para ligar ou desligar dispositivos;
- comunicação entre servidor e dispositivos por MQTT;
- exposição de dados por API REST;
- clientes de terminal para consulta e controle;
- base preparada para adaptação a hardware real no futuro.

---

## Arquitetura geral

A solução está organizada em três partes principais:

### Servidor
Centraliza a lógica da aplicação, administra os dispositivos, processa mensagens recebidas, calcula os dados derivados e expõe a API utilizada pelos clientes.

### Simulação de dispositivos
Representa dispositivos virtuais usados para teste e validação do sistema. Essa camada reproduz o comportamento de hardware real, enviando telemetria, recebendo comandos e mantendo o estado operacional dos dispositivos simulados.

### Clientes
Disponibilizam uma interface simples em terminal para consumir os dados do sistema e interagir com os dispositivos monitorados.

---

## Estrutura do repositório

```text
Smart-Grid/
├── servidor+simuladorESP/
│   ├── src/
│   │   ├── main/
│   │   │   ├── java/
│   │   │   │   └── com/smartgrid_server/smartgrid/
│   │   │   │       ├── config/
│   │   │   │       ├── device/
│   │   │   │       │   ├── controller/
│   │   │   │       │   ├── dto/
│   │   │   │       │   ├── model/
│   │   │   │       │   └── service/
│   │   │   │       ├── esp/
│   │   │   │       └── mqtt/
│   │   │   └── resources/
│   │   │       └── application.properties
│   │   └── test/
│   ├── pom.xml
│   ├── mvnw
│   ├── mvnw.cmd
│   └── README.md
│
├── clientes/
│   ├── python_client/
│   │   ├── main.py
│   │   └── README.md
│   └── cpp_client/
│       ├── src/
│       │   ├── main.cpp
│       │   ├── api_client.hpp
│       │   └── display.hpp
│       ├── CMakeLists.txt
│       └── README.md
│
└── README.md
```

---

## Pré-requisitos

Antes de executar o projeto, verifique se o ambiente possui:

- **Java 21** ou superior;
- **Maven**;
- **Python 3.10+**;
- **CMake 3.16+**;
- compilador compatível com **C++17**;
- **Mosquitto** instalado e disponível na porta `1883`;
- **IntelliJ IDEA**, caso deseje executar o backend pela IDE.

---

## Como executar a partir de um clone do repositório

### 1. Clone o projeto

```bash
git clone https://github.com/Jean-Morais/Smart-Grid
cd Smart-Grid
```

---

### 2. Inicie o broker MQTT

Abra um terminal separado e inicie o broker:

```bash
mosquitto -v
```

Se o broker estiver em outra máquina, ajuste o endereço configurado no arquivo `application.properties`.

---

### 3. Execute o servidor

Em um novo terminal, acesse a pasta do backend:

```bash
cd servidor+simuladorESP
```

Na sequência, inicie a aplicação principal:

```bash
./mvnw spring-boot:run
```

Se estiver no Windows, use:

```powershell
mvnw.cmd spring-boot:run
```

O servidor ficará disponível no endereço:

```text
http://localhost:8080
```

---

### 4. Execute a simulação de dispositivos

Com o servidor ativo, abra outro terminal e volte para a pasta do backend:

```bash
cd servidor+simuladorESP
```

Compile o projeto e gere o classpath necessário:

```bash
./mvnw clean compile dependency:build-classpath -Dmdep.outputFile=cp.txt
```

Em seguida, execute a classe principal da simulação:

```bash
java -cp "target/classes:$(cat cp.txt)" com.smartgrid_server.smartgrid.esp.EspSimulatorApplication
```

No Windows, utilize:

```powershell
java -cp "target/classes;$(Get-Content cp.txt)" com.smartgrid_server.smartgrid.esp.EspSimulatorApplication
```

Essa etapa inicia os dispositivos virtuais usados apenas para teste e validação do comportamento do sistema.

---

### 5. Execute o cliente Python

Em outro terminal, acesse a pasta do cliente Python:

```bash
cd clientes/python_client
python3 main.py
```

Se desejar informar host e porta manualmente:

```bash
python3 main.py 192.168.1.10 8080
```

---

### 6. Execute o cliente C++

Em outro terminal, acesse a pasta do cliente C++:

```bash
cd clientes/cpp_client
mkdir build
cd build
cmake ..
cmake --build .
```

Depois execute o binário gerado:

```bash
./smartgrid_client
```

Para informar host e porta:

```bash
./smartgrid_client 192.168.1.10 8080
```

---

## Como executar no IntelliJ IDEA

### 1. Abra o projeto
Abra a pasta `servidor+simuladorESP` como projeto Maven no IntelliJ IDEA.

### 2. Aguarde o carregamento
Espere o IntelliJ concluir a indexação do projeto e o download das dependências.

### 3. Inicie o broker MQTT
Deixe o Mosquitto em execução na porta `1883`.

### 4. Execute o servidor
Crie uma configuração de execução para a classe principal do backend.

### 5. Execute a simulação de dispositivos
Crie uma segunda configuração de execução para a classe principal da simulação.

As duas execuções devem permanecer ativas ao mesmo tempo para que o sistema funcione corretamente.

---

## API REST

Base padrão:

```text
http://localhost:8080
```

### Listar todos os dispositivos

```http
GET /api/devices
```

### Consultar um dispositivo pelo ID

```http
GET /api/devices/{id}
```

### Enviar comando para o dispositivo

```http
POST /api/devices/{id}/command
```

Exemplo de corpo da requisição:

```json
{
  "type": "OUTLET_ON"
}
```

ou:

```json
{
  "type": "OUTLET_OFF"
}
```

### Solicitar atualização de leitura

```http
POST /api/devices/{id}/refresh
```

---

## Tópicos MQTT

O sistema utiliza o prefixo `smartgrid` por padrão.

### Telemetria

```text
smartgrid/<espId>/telemetry
```

### Comandos

```text
smartgrid/<espId>/commands
```

### Anúncio de presença

```text
smartgrid/<espId>/announce
```

---

## Menu dos clientes

Os clientes de terminal oferecem as mesmas operações:

- listar dispositivos;
- ver detalhes de um dispositivo;
- enviar comando para ligar ou desligar;
- solicitar atualização de leitura;
- sair.

---

## Comportamento esperado

- os dispositivos simulados anunciam presença ao iniciar;
- o servidor registra e atualiza os dispositivos automaticamente;
- as leituras chegam periodicamente via MQTT;
- os clientes exibem os dados atualizados da rede;
- ao enviar um comando, o estado do dispositivo é refletido na simulação e no servidor.

---

## Observações importantes

- o servidor depende do broker MQTT ativo;
- a aplicação foi pensada para uso em rede local;
- os dados são mantidos em memória;
- ao reiniciar o servidor, o estado anterior é recriado a partir da configuração inicial;
- a simulação existe apenas para testes e validação do sistema, não como objetivo final do projeto.

---

## Contribuidores

- Jean Morais da Silva
- João Pedro Holanda Amorim

---

## Contexto acadêmico

Projeto desenvolvido como aprendizado de Redes e Sistemas Distribuídos no curso de graduação da Universidade Federal do Ceará — Campus de Quixadá.
