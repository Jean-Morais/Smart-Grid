package main.servidor;

import java.rmi.registry.LocateRegistry;
import java.rmi.registry.Registry;

/**
 * Ponto de entrada do servidor RMI do Smart Grid.
 *
 * Sobe o registry na porta 1099 e registra o ServicoTomada.
 * Os dispositivos são inicializados automaticamente em ServicoTomadaImpl.
 *
 * Como rodar:
 *   java -cp out main.servidor.Servidor
 */
public class Servidor {

    public static void main(String[] args) {
        try {
            ServicoTomadaImpl servico = new ServicoTomadaImpl();
            Registry registry = LocateRegistry.createRegistry(1099);
            registry.rebind("ServicoTomada", servico);

            System.out.println("╔══════════════════════════════════════╗");
            System.out.println("║   Smart Grid — Servidor RMI pronto   ║");
            System.out.println("╚══════════════════════════════════════╝");
            System.out.println("Dispositivos registrados: Sala (1), Quarto (2), Cozinha (3)");
            System.out.println("Aguardando clientes na porta 1099...\n");

        } catch (Exception e) {
            System.err.println("Erro ao iniciar servidor: " + e.getMessage());
            e.printStackTrace();
        }
    }
}
