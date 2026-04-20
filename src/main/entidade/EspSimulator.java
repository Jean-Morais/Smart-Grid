package main.entidade;

import java.io.OutputStream;
import java.net.Socket;
import java.util.Random;
import main.streams.*;

public class EspSimulator {

    private static final Random random = new Random();

    public static void main(String[] args) {

        while (true) {
            try (Socket socket = new Socket("localhost", 12346)) {

                System.out.println("ESP conectada ao servidor!");

                ComandoInputStream cis =
                        new ComandoInputStream(socket.getInputStream());

                OutputStream saida = socket.getOutputStream();

                while (true) {
                    Comando[] comando = cis.receber();

                    if (comando == null || comando.length == 0 || comando[0] == null) {
                        continue;
                    }

                    if (comando[0].get_tipo().equalsIgnoreCase("medir")) {
                        System.out.println("main.entidade.Comando MEDIR recebido");

                        Medicao[] m = {
                                new Medicao(
                                        0.5f + random.nextFloat() * 2.0f,
                                        220f + random.nextFloat() * 5f,
                                        0.90f + random.nextFloat() * 0.09f
                                )
                        };

                        MedicaoOutputStream mos =
                                new MedicaoOutputStream(m, m.length, saida);

                        mos.enviar();

                        System.out.println("Medição enviada!");
                    }
                }

            } catch (Exception e) {
                System.out.println("Conexão com o servidor perdida. Tentando reconectar...");
                try {
                    Thread.sleep(1000);
                } catch (InterruptedException ignored) {}
            }
        }
    }


}