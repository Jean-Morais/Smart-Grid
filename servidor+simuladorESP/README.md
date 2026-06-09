# Smart Grid

Projeto Spring Boot com MQTT para simulação de controladoras ESP.

## O que ficou nesta versão

- API REST em Spring Boot
- servidor MQTT publicando comandos por tópico específico
- simulador de ESP em Java publicando telemetria por tópico específico
- simulador Java com múltiplos ESPs no mesmo processo
- suporte para rodar no IntelliJ com uma execução separada para o servidor e outra para o simulador

## Convenção de tópicos

Para cada ESP com id `esp-001`:

- telemetria: `smartgrid/esp-001/telemetry`
- comandos: `smartgrid/esp-001/commands`
- anúncio: `smartgrid/esp-001/announce`

## Endpoints da API

- `GET /api/devices`
- `GET /api/devices/{id}`
- `POST /api/devices/{id}/command`
- `POST /api/devices/{id}/refresh`

Exemplo de comando:

```json
{
  "type": "OUTLET_ON"
}
```

## Rodando no IntelliJ

1. Inicie o Mosquitto na porta `1883`.
2. Abra o projeto Maven no IntelliJ.
3. Rode `com.smartgrid_server.smartgrid.SmartgridApplication`.
4. Rode `com.smartgrid_server.smartgrid.esp.EspSimulatorApplication`.

### Modo multi-ESP no simulador

Sem parâmetros, o simulador sobe três ESPs padrão:
- `esp-001`
- `esp-002`
- `esp-003`

Também é possível passar uma lista compacta:

```text
--devices esp-001:Sala:Sala:5000:127:0.75:0.92,esp-002:Quarto:Quarto:6000:220:0.50:0.88,esp-003:Cozinha:Cozinha:7000:127:0.80:0.90
```

Cada item segue o formato:

```text
espId:nome:local:intervaloMs:tensao:corrente:fp
```

## Rodando pelo terminal

### 1. Broker MQTT

```bash
mosquitto
```

### 2. Servidor Spring Boot

Na raiz do projeto:

```bash
./mvnw spring-boot:run
```

### 3. Simulador de ESP

Depois de compilar o projeto no IntelliJ ou gerar o `target/classes`, execute:

```bash
java -cp target/classes:lib/org.eclipse.paho.client.mqttv3-1.2.5.jar com.smartgrid_server.smartgrid.esp.EspSimulatorApplication esp-001 "Tomada Sala" Sala 5000 127 0.75 0.92
```

No Windows, troque `:` por `;`.

## Observação importante

A aplicação do servidor depende do Mosquitto ativo. Se o broker não estiver rodando, o Spring Boot sobe, mas a camada MQTT não consegue se conectar até o broker ficar disponível.
