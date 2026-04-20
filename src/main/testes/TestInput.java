package main.testes;

import java.io.FileInputStream;
import java.io.IOException;
import main.entidade.Medicao;
import main.streams.*;

public class TestInput {
    public static void main(String[] args) throws IOException {

        Medicao[] arr = new Medicao[2];

        try (FileInputStream fis = new FileInputStream("medicoes.dat")) {
            MedicaoInputStream mis = new MedicaoInputStream(arr, arr.length, fis);
            mis.receber();
        }

        // Mostrar os dados recebidos
        for (Medicao m : arr) {
             System.out.println(m.getCorrente() + " " +
                       m.getTensao() + " " +
                       m.getFatorPotencia());
        }
    }
}