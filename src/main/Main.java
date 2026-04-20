import main.streams.MedicaoOutputStream;

import java.io.IOException;
import main.entidade.Medicao;

public class Main {
    public static void main(String[] args) throws IOException {
        Medicao[] arr = {
            new Medicao(1.2f, 220f, 0.95f),
            new Medicao(0.8f, 110f, 0.90f)
        };

        MedicaoOutputStream mos = new MedicaoOutputStream(arr, arr.length, System.out);
        mos.enviar();
    }
}