# Smart Grid

![Status](https://img.shields.io/badge/status-em%20desenvolvimento-yellow)
![Language](https://img.shields.io/badge/language-Java-orange)

> Sistema local de Smart Grid para monitoramento de energia em residências, com arquitetura distribuída baseada em sockets TCP.

## Descrição do Projeto

O **Smart Grid** é um projeto voltado para **IoT, Redes e Sistemas Distribuídos** cujo objetivo é monitorar e disponibilizar informações energéticas de uma residência em uma rede local.

A proposta do sistema é acompanhar tanto a energia **gerada na casa** quanto a energia **recebida da concessionária**, permitindo visualizar métricas importantes do consumo e do comportamento elétrico dos dispositivos conectados.

O projeto foi modelado com uma arquitetura distribuída baseada em **comunicação TCP**, onde diferentes nós da rede trocam mensagens e dados de medição de forma organizada. Atualmente, o ambiente é simulado, mas a estrutura foi pensada para facilitar a futura integração com **hardwares reais**, como microcontroladores e sensores.

## Funcionalidades

<!-- - Medição do consumo energético mensal.-->
<!-- - Medição em tempo real do consumo de dispositivos conectados a qualquer tomada.-->
- Coleta de métricas elétricas detalhadas:
  - Tensão
  - Corrente
  - Fator de Potência
- Disponibilização dos dados via servidor para qualquer cliente/usuário na mesma rede local.
- Comunicação entre nós por meio de sockets TCP.
>Observação: Ocorrerá possíveis alterações nesse processo
- Serialização e troca de objetos de domínio entre cliente, servidor e controlador.

## Arquitetura do Sistema

A arquitetura atual foi organizada em três camadas principais de comunicação:

### 1. Cliente TCP
É a interface do usuário com o sistema.  
Sua função é solicitar informações ao servidor, como medições de consumo e dados energéticos específicos.

### 2. Servidor Simulado
Recebe as requisições do cliente e atua como intermediário na comunicação.  
Ao receber uma solicitação, o servidor encaminha o comando ao controlador responsável pela medição e, em seguida, devolve a resposta ao cliente.

### 3. ESP Simulado (Controlador)
Representa o nó que executa a medição.  
Na implementação atual, ele simula o comportamento de um ESP ou controlador embarcado, retornando os dados solicitados pelo servidor.

## Fluxo de Comunicação

1. O cliente envia uma solicitação ao servidor.
2. O servidor interpreta o comando recebido.
3. O servidor encaminha o pedido ao ESP simulado.
4. O ESP simulado retorna a medição.
5. O servidor repassa a resposta ao cliente.

## Estrutura de Domínio

O projeto utiliza classes POJO para representar os dados trafegados na rede:

- **`Comando`**: representa a solicitação enviada pelo cliente ao servidor e também o comando repassado do servidor ao ESP.  
  
- **`Medicao`**: representa os dados coletados, incluindo informações como tensão, corrente, potência e fator de potência.

- **Serviço de Medição**: responsável pelos cálculos elétricos, como o cálculo de potência a partir dos parâmetros medidos.

## Comunicação e Serialização

A troca de informações entre os nós é feita com **`InputStream`** e **`OutputStream`**, utilizando serialização dos objetos `Comando` e `Medicao`.

Essa abordagem permite:

- enviar objetos de forma estruturada entre processos distintos;
- manter a comunicação organizada dentro da rede local;
- facilitar a evolução futura.

## Pré-requisitos

Antes de executar o projeto, verifique se o ambiente possui:

- **Java Development Kit (JDK)** instalado.
- Ferramenta de build compatível com o projeto, caso seja utilizada.
- Ambiente de rede local configurado para testes com sockets TCP.
- Permissão para execução de múltiplas aplicações/processos no mesmo computador ou na mesma rede.

## Como Rodar

> Os passos abaixo assumem uma organização típica de projeto Java com execução separada para cliente, servidor e ESP simulado.

### 1. Clone o repositório
```bash
git clone https://github.com/Jean-Morais/Smart-Grid.git
cd Smart-Grid
```

### 2. Compile o projeto
```bash
javac -d out src/main/**/*.java
```

### 3. Inicie o servidor simulado
```bash
java -cp out main.entidade.ServerSimulator
```

### 4. Em outro terminal inicie o ESP simulado
```bash
java -cp out main.entidade.EspSimulator
```

### 5. E em outro execute o cliente TCP
```bash
java -cp out main.entidade.ClienteTCP
```


## Exemplo de Uso

Após iniciar os processos, o cliente pode solicitar medições, consultar consumo e receber os valores atualizados em tempo real por meio do servidor.

Exemplo de interação esperada:

- Cliente solicita dados energéticos através do comando informado no sistema.
- Servidor processa a requisição.
- ESP simulado retorna a medição.
- Cliente recebe a resposta com os valores coletados.

<!--
## Próximos Passos / Roadmap

- Substituir a simulação por **hardware real**.
- Integrar sensores e microcontroladores para leitura física das grandezas elétricas.
- Revisar a dinâmica de requisição entre `Comando`, servidor e controlador para tornar a arquitetura mais clara e extensível.
- Evoluir o sistema para suportar mais tipos de dispositivos e métricas.
- Melhorar a persistência dos dados para histórico de consumo.
- Adicionar interface visual para consulta e análise das medições.
- Possibilitar autenticação de usuários na rede local.
-->

## Tecnologias Utilizadas

- **Java**
- **Sockets TCP**
- **InputStream / OutputStream**
- **Serialização de objetos**
- **Programação Orientada a Objetos**

## Organização Atual do Projeto

```text
Smart-Grid/
├── src/
│   └── main/
│       ├── entidades/
│       ├── services/
│       ├── streams/
│       └── testes/
├── out/
└── README.md
```

---

Projeto desenvolvido para fins acadêmicos e de estudo em **Redes e Sistemas Distribuídos**.

##Contribuidores do projeto
Jean Morais da Silva
João Pedro Holanda Amorim
