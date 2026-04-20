package main.entidade;

import java.io.OutputStream;
import java.net.Socket;
import java.util.Scanner;
import main.streams.*;

public class ClienteTCP {

    public static void main(String[] args) {

        try (Socket socket = new Socket("localhost", 12345)) {

            System.out.println("Cliente conectado ao servidor!");

            Thread enviar = new Thread(() -> {
                try {
                    Scanner sc = new Scanner(System.in);
                    OutputStream saida = socket.getOutputStream();

                    while (true) {
                        System.out.print("Digite comando (medir ou sair): ");
                        String cmd = sc.nextLine();

                        if (cmd.equalsIgnoreCase("sair")) {
                            socket.close();
                            break;
                        }

                        if (cmd.equalsIgnoreCase("medir")) {
                            Comando[] comando = {
                                    new Comando("medir", "sensor", true, 1)
                            };

                            ComandoOutputStream cos =
                                    new ComandoOutputStream(comando, comando.length, saida);

                            cos.enviar();

                            System.out.println("main.entidade.Comando enviado ao servidor");
                        }
                    }

                } catch (Exception e) {
                    System.out.println("Falha ao enviar comando. Conexão encerrada.");
                }
            });

            Thread receber = new Thread(() -> {
                try {
                    Medicao[] m = new Medicao[1];
                    MedicaoInputStream mis =
                            new MedicaoInputStream(m, m.length, socket.getInputStream());

                    while (true) {
                        mis.receber();

                        if (m[0] != null) {
                            System.out.println("MEDIÇÃO RECEBIDA:");
                            System.out.println("Corrente: " + m[0].getCorrente());
                            System.out.println("Tensão: " + m[0].getTensao());
                            System.out.println("FP: " + m[0].getFatorPotencia());
                            System.out.println("--------------------");
                        }
                    }

                } catch (Exception e) {
                    System.out.println("Conexão com o servidor encerrada.");
                }
            });

            enviar.start();
            receber.start();

            enviar.join();
            receber.join();

        } catch (Exception e) {
            e.printStackTrace();
        }
    }
}