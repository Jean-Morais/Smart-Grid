package main.testes;

import java.io.FileOutputStream;
import java.io.IOException;
import main.entidade.Medicao;
import main.entidade.Medicao;
import main.streams.*;

public class TestOutput {
    public static void main(String[] args) throws IOException {
        Medicao[] arr = {
            new Medicao(1.2f, 220f, 0.95f),
            new Medicao(0.8f, 110f, 0.90f)
        };

        try (FileOutputStream fos = new FileOutputStream("medicoes.dat")) {
            MedicaoOutputStream mos = new MedicaoOutputStream(arr, arr.length, fos);
            mos.enviar();
        }
    }
}