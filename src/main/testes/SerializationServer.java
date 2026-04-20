package main.testes;

import main.streams.MedicaoInputStream;

import java.io.IOException;
import java.net.ServerSocket;
import java.net.Socket;
import main.entidade.Medicao;

public class SerializationServer {
    public static void main(String[] args) throws IOException {

        Medicao[] arr = new Medicao[2];
        
        ServerSocket server = new ServerSocket(12345);
        Socket socket = server.accept();

        MedicaoInputStream mis = new MedicaoInputStream(arr, arr.length,socket.getInputStream());
        mis.receber();

        // Mostrar os dados recebidos
        for (Medicao m : arr) {
             System.out.println(m.getCorrente() + " " +
                       m.getTensao() + " " +
                       m.getFatorPotencia());
        }
    }
}