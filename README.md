# Smart Grid

![Status](https://img.shields.io/badge/status-em%20desenvolvimento-yellow)
![Language](https://img.shields.io/badge/language-Java-orange)
![Architecture](https://img.shields.io/badge/arquitetura-RMI-blue)

> Sistema local de Smart Grid para monitoramento de energia em residências, com arquitetura distribuída baseada em **Java RMI**.

## Descrição do Projeto

O **Smart Grid** é um projeto voltado para **IoT, Redes e Sistemas Distribuídos** cujo objetivo é monitorar e disponibilizar informações energéticas de uma residência em uma rede local.

A proposta do sistema é acompanhar tanto a energia **gerada na casa** quanto a energia **recebida da concessionária**, permitindo visualizar métricas importantes do consumo e do comportamento elétrico dos dispositivos conectados.

O projeto foi modelado com uma arquitetura distribuída baseada em **Java RMI (Remote Method Invocation)**, onde cliente e servidor trocam mensagens por meio de invocação remota de métodos, seguindo o protocolo **requisição-resposta**. O ambiente é simulado, mas a estrutura foi pensada para facilitar a futura integração com **hardwares reais**, como microcontroladores e sensores.

## Funcionalidades

- Coleta de métricas elétricas detalhadas:
  - Tensão (V)
  - Corrente (A)
  - Fator de Potência
- Cálculo de **potência ativa**: `P = V × I × FP`
- Cálculo de **energia consumida**: `E = P × t`
- Controle remoto de tomadas (ligar/desligar)
- Consulta do estado completo de um dispositivo em JSON
- Distinção entre modo **consumo** e modo **geração** de energia
- Disponibilização dos dados via servidor para qualquer cliente na mesma rede local

> **Observação:** atualmente o sistema opera apenas em modo de consulta. Os dados de dispositivos e medições são pré-cadastrados diretamente no servidor para fins de teste — não há, por enquanto, uma interface para cadastrar novos dispositivos ou inserir leituras em tempo de execução.

## Arquitetura do Sistema

A arquitetura atual foi organizada em duas camadas principais:

### 1. Cliente RMI
É a interface do usuário com o sistema.
Todas as chamadas passam pelo `ClienteProxy.doOperation()`, que implementa o protocolo requisição-resposta: empacota os argumentos em JSON, invoca o método remoto e devolve a resposta ao usuário.

### 2. Servidor RMI
Recebe as requisições e gerencia dois objetos remotos:
- **`ServicoTomada`** — interface principal; controla o estado das tomadas e retorna referências remotas de medição.
- **`ServicoMedicao`** — interface secundária; executa leituras e cálculos elétricos no servidor.

Para cada tomada registrada, o servidor mantém um par **`Controlador` + `Medidor`** compartilhando o mesmo sensor, garantindo coerência entre comando e leitura.

## Fluxo de Comunicação

1. O cliente chama `doOperation(objectReference, methodId, arguments)`.
2. O proxy empacota os argumentos em JSON e invoca o método remoto via stub RMI.
3. O método é executado no servidor.
4. O servidor retorna o resultado (objeto por valor ou stub por referência).
5. O proxy serializa a resposta em JSON e entrega ao cliente.

## Estrutura de Domínio

O projeto utiliza classes para representar os dados trafegados na rede:

- **`Dispositivo`**: superclasse base da hierarquia; carrega id, nome, IP, local e sensor.
  - **`Controlador`** *é-um* `Dispositivo`: toma decisões e envia comandos às tomadas.
  - **`Medidor`** *é-um* `Dispositivo`: coleta leituras de consumo ou geração de energia; *tem-uma* lista de `Medicao` como histórico de leituras registradas.
- **`Sensor`**: sensor físico acoplado a um `Dispositivo`; *tem-uma* `Medicao` com a leitura mais recente.
- **`Medicao`**: dados elétricos coletados — corrente, tensão, fator de potência e timestamp.
- **`Reply`**: envelope da resposta RMI, contendo o resultado em JSON.

## Comunicação e Serialização

A troca de informações entre cliente e servidor é feita via **Java RMI**, com dois modos de passagem de parâmetro:

- **Por valor**: o objeto é serializado em **JSON** e enviado como cópia ao cliente (ex.: `consultarDispositivo()`, `obterMedicaoAtual()`).
- **Por referência**: o cliente recebe um **stub remoto** e a execução permanece no servidor (ex.: `getServicoMedicao()`).

Essa abordagem permite:

- invocar métodos remotos de forma transparente, como se fossem locais;
- manter a comunicação organizada por interfaces bem definidas;
- facilitar a evolução futura para outros protocolos ou transportes.

## Pré-requisitos

Antes de executar o projeto, verifique se o ambiente possui:

- **Java Development Kit (JDK) 11+** instalado.
- Permissão para execução de múltiplas instâncias JVM no mesmo computador ou na mesma rede.
- Rede local configurada para testes com RMI, caso cliente e servidor rodem em máquinas diferentes.

## Como Rodar

### 1. Clone o repositório
```bash
git clone https://github.com/Jean-Morais/Smart-Grid.git
cd Smart-Grid
```

### 2. Compile o projeto
```bash
javac -d out src/main/**/*.java
```

### 3. Inicie o Servidor
```bash
java -cp out main.servidor.Servidor
```

### 4. Em outro terminal, execute o Cliente
```bash
java -cp out main.cliente.Cliente
```

## Exemplo de Uso

Após iniciar os processos, o cliente exibe um menu interativo. Exemplo de interação esperada:

- Cliente solicita ligar a tomada da Sala (id 1).
- `ClienteProxy` envia: `doOperation("ServicoTomada", "ligarTomada", {"id":1})`.
- Servidor executa, o `Controlador` registra o comando e retorna confirmação em JSON.
- Cliente exibe a resposta formatada no terminal.

Para operações de medição, o cliente obtém um **stub remoto** do `ServicoMedicao` e chama os métodos diretamente — a execução ocorre no servidor, os resultados chegam por valor.

## Organização Atual do Projeto

```text
Smart-Grid/
├── src/
│   └── main/
│       ├── entidade/
│       │   ├── Dispositivo.java
│       │   ├── Controlador.java
│       │   ├── Medidor.java
│       │   ├── Sensor.java
│       │   ├── Medicao.java
│       │   └── Reply.java
│       │
│       ├── remoto/
│       │   ├── ServicoTomada.java
│       │   └── ServicoMedicao.java
│       │
│       ├── servidor/
│       │   ├── Servidor.java
│       │   ├── ServicoTomadaImpl.java
│       │   └── ServicoMedicaoImpl.java
│       │
│       ├── cliente/
│       │   ├── Cliente.java
│       │   └── ClienteProxy.java
│       │
│       └── services/
│           └── MedicaoService.java
│
├── out/
└── README.md
```

---

Projeto desenvolvido para fins acadêmicos e de estudo em Redes e Sistemas Distribuídos.
### Contribuidores
Jean Morais da Silva e João Pedro Holanda Amorim