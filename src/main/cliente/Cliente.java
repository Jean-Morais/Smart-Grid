package main.cliente;

import java.rmi.registry.LocateRegistry;
import java.rmi.registry.Registry;
import java.util.Scanner;
import main.remoto.ServicoTomada;

/**
 * Cliente interativo do Smart Grid.
 *
 * Todas as chamadas passam pelo ClienteProxy.doOperation(), que implementa
 * o protocolo requisição-resposta da seção 5.2 do livro texto.
 *
 * Os argumentos são empacotados em JSON (representação externa de dados).
 * As respostas também chegam em JSON.
 *
 * Como rodar (após iniciar o Servidor):
 *   java -cp out main.cliente.Cliente
 */
public class Cliente {

    public static void main(String[] args) {

        try {
            Registry registry = LocateRegistry.getRegistry("localhost", 1099);
            ServicoTomada servico = (ServicoTomada) registry.lookup("ServicoTomada");

            // Proxy que implementa doOperation — protocolo req/resp seção 5.2
            ClienteProxy proxy = new ClienteProxy(servico);

            Scanner scanner = new Scanner(System.in);

            System.out.println("╔══════════════════════════════════════════╗");
            System.out.println("║     Smart Grid — Cliente RMI             ║");
            System.out.println("╚══════════════════════════════════════════╝");

            while (true) {
                System.out.println("\n── Operações de Tomada ──────────────────");
                System.out.println(" 1 - Ligar tomada");
                System.out.println(" 2 - Desligar tomada");
                System.out.println(" 3 - Consultar dispositivo (JSON)");
                System.out.println("── Operações de Medição (ref. remota) ──");
                System.out.println(" 4 - Obter medição atual");
                System.out.println(" 5 - Calcular potência (W)");
                System.out.println(" 6 - Calcular energia (Wh)");
                System.out.println(" 7 - Resumo completo (JSON)");
                System.out.println("─────────────────────────────────────────");
                System.out.println(" 0 - Sair");
                System.out.print("Opção: ");

                int op = scanner.nextInt();
                if (op == 0) break;

                System.out.print("ID da tomada (1=Sala, 2=Quarto, 3=Cozinha): ");
                int id = scanner.nextInt();

                String resultado;

                switch (op) {

                    case 1:
                        // doOperation: ServicoTomada / ligarTomada / {id}
                        resultado = proxy.doOperation(
                            "ServicoTomada", "ligarTomada",
                            "{\"id\":" + id + "}"
                        );
                        break;

                    case 2:
                        resultado = proxy.doOperation(
                            "ServicoTomada", "desligarTomada",
                            "{\"id\":" + id + "}"
                        );
                        break;

                    case 3:
                        // Retorna JSON do Dispositivo — passagem por valor
                        resultado = proxy.doOperation(
                            "ServicoTomada", "consultarDispositivo",
                            "{\"id\":" + id + "}"
                        );
                        break;

                    case 4:
                        // Obtém stub remoto → chama obterMedicaoAtual no servidor
                        resultado = proxy.doOperation(
                            "ServicoMedicao", "obterMedicaoAtual",
                            "{\"id\":" + id + "}"
                        );
                        break;

                    case 5:
                        // Obtém stub remoto → executa calcularPotencia no servidor
                        resultado = proxy.doOperation(
                            "ServicoMedicao", "calcularPotencia",
                            "{\"id\":" + id + "}"
                        );
                        break;

                    case 6:
                        System.out.print("Período em horas: ");
                        float horas = scanner.nextFloat();
                        resultado = proxy.doOperation(
                            "ServicoMedicao", "calcularEnergia",
                            "{\"id\":" + id + ",\"horas\":" + horas + "}"
                        );
                        break;

                    case 7:
                        // Resumo completo em JSON — passagem por valor com repr. externa
                        resultado = proxy.doOperation(
                            "ServicoMedicao", "obterResumoJson",
                            "{\"id\":" + id + "}"
                        );
                        break;

                    default:
                        resultado = "Opção inválida.";
                }

                System.out.println("\nResposta:");
                System.out.println(formatarJson(resultado));
            }

            scanner.close();
            System.out.println("Encerrando cliente.");

        } catch (Exception e) {
            System.err.println("Erro no cliente: " + e.getMessage());
            e.printStackTrace();
        }
    }

    /**
     * Formata JSON minimamente para leitura no terminal.
     * Sem bibliotecas externas — apenas indenta chaves e vírgulas.
     */
    private static String formatarJson(String json) {
        if (json == null || !json.trim().startsWith("{")) return json;
        StringBuilder sb  = new StringBuilder();
        int indent = 0;
        boolean inString = false;
        for (char c : json.toCharArray()) {
            if (c == '"') inString = !inString;
            if (!inString) {
                if (c == '{' || c == '[') {
                    sb.append(c).append('\n');
                    indent++;
                    sb.append("  ".repeat(indent));
                    continue;
                }
                if (c == '}' || c == ']') {
                    sb.append('\n');
                    indent--;
                    sb.append("  ".repeat(indent)).append(c);
                    continue;
                }
                if (c == ',') {
                    sb.append(c).append('\n').append("  ".repeat(indent));
                    continue;
                }
            }
            sb.append(c);
        }
        return sb.toString();
    }
}
