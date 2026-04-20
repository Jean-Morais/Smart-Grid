package main.entidade;

import java.io.IOException;
import java.io.OutputStream;
import java.net.ServerSocket;
import java.net.Socket;
import main.streams.*;

public class ServerSimulator {

    static volatile Socket espSocket;
    static final Object espLock = new Object();

    public static void main(String[] args) throws IOException {

        ServerSocket serverESP = new ServerSocket(12346);
        ServerSocket serverCliente = new ServerSocket(12345);

        Thread aceitarESP = new Thread(() -> {
            while (true) {
                try {
                    System.out.println("Aguardando ESP...");
                    Socket novaESP = serverESP.accept();

                    synchronized (espLock) {
                        if (espSocket != null && !espSocket.isClosed()) {
                            try {
                                espSocket.close();
                            } catch (IOException ignored) {}
                        }

                        espSocket = novaESP;
                        espLock.notifyAll();
                    }

                    System.out.println("ESP conectada!");

                } catch (IOException e) {
                    e.printStackTrace();
                }
            }
        });

        Thread aceitarClientes = new Thread(() -> {
            while (true) {
                try {
                    System.out.println("Aguardando Cliente...");
                    Socket clienteSocket = serverCliente.accept();
                    System.out.println("Cliente conectado: " + clienteSocket.getRemoteSocketAddress());

                    Thread atendimentoCliente = new Thread(new AtendimentoCliente(clienteSocket));
                    atendimentoCliente.start();

                } catch (IOException e) {
                    e.printStackTrace();
                }
            }
        });

        aceitarESP.start();
        aceitarClientes.start();
    }

    static class AtendimentoCliente implements Runnable {

        private final Socket clienteSocket;

        public AtendimentoCliente(Socket clienteSocket) {
            this.clienteSocket = clienteSocket;
        }

        @Override
        public void run() {
            try {
                ComandoInputStream cisCliente =
                        new ComandoInputStream(clienteSocket.getInputStream());

                OutputStream saidaCliente = clienteSocket.getOutputStream();

                while (true) {

                    Comando[] comando = cisCliente.receber();

                    if (comando == null || comando.length == 0 || comando[0] == null) {
                        continue;
                    }

                    if (comando[0].get_tipo().equalsIgnoreCase("medir")) {
                        System.out.println("Cliente pediu medição: " + clienteSocket.getRemoteSocketAddress());

                        Medicao medicao = solicitarMedicaoDaESP();

                        if (medicao != null) {
                            Medicao[] resposta = { medicao };

                            MedicaoOutputStream mos =
                                    new MedicaoOutputStream(resposta, resposta.length, saidaCliente);

                            mos.enviar();

                            System.out.println("Medição enviada ao cliente: " + clienteSocket.getRemoteSocketAddress());
                        } else {
                            System.out.println("Não foi possível obter medição da ESP.");
                        }
                    }
                }

            } catch (Exception e) {
                System.out.println("Cliente desconectado: " + clienteSocket.getRemoteSocketAddress());
            } finally {
                try {
                    clienteSocket.close();
                } catch (IOException ignored) {}
            }
        }
    }

    static Medicao solicitarMedicaoDaESP() {
        synchronized (espLock) {
            try {
                while (espSocket == null || espSocket.isClosed() || !espSocket.isConnected()) {
                    System.out.println("ESP indisponível. Aguardando conexão...");
                    espLock.wait();
                }

                Socket espAtual = espSocket;

                Comando[] comando = {
                        new Comando("medir", "sensor", true, 1)
                };

                ComandoOutputStream cos =
                        new ComandoOutputStream(comando, comando.length, espAtual.getOutputStream());
                cos.enviar();

                Medicao[] m = new Medicao[1];
                MedicaoInputStream mis =
                        new MedicaoInputStream(m, m.length, espAtual.getInputStream());

                mis.receber();

                return m[0];

            } catch (Exception e) {
                try {
                    if (espSocket != null) {
                        espSocket.close();
                    }
                } catch (IOException ignored) {}

                espSocket = null;
                espLock.notifyAll();

                return null;
            }
        }
    }


}