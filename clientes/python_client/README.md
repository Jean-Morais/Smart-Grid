# SmartGrid — Cliente Python

Cliente de terminal para consumir a API REST do servidor Spring Boot.

## Requisitos
- Python 3.10 ou superior
- Servidor rodando em `http://localhost:8080` por padrão

## Como executar

```bash
python main.py
python main.py 192.168.1.10
python main.py 192.168.1.10 9090
```

## Endpoints usados

- `GET  /api/devices`
- `GET  /api/devices/{id}`
- `POST /api/devices/{id}/command`
- `POST /api/devices/{id}/refresh`

## Observações
- O cliente mostra menu interativo no terminal.
- Você pode selecionar dispositivo pelo número da lista ou pelo ID direto.
- O código usa apenas a biblioteca padrão do Python, sem dependências externas.
