package main.testes;

import main.streams.MedicaoOutputStream;

import java.io.IOException;
import java.net.Socket;
import main.entidade.Medicao;

public class SerializationClient {
    public static void main(String[] args) throws IOException {
        Medicao[] arr = {
            new Medicao(1.2f, 220f, 0.95f),
            new Medicao(0.8f, 110f, 0.90f)
        };

        Socket socket = new Socket("localhost", 12345);
        
        MedicaoOutputStream mos = new MedicaoOutputStream(arr, arr.length,socket.getOutputStream());
        mos.enviar();
        
    }
}