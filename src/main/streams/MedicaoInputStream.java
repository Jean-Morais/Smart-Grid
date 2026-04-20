package main.streams;

import java.io.IOException;
import java.io.InputStream;
import main.entidade.Medicao;

public class MedicaoInputStream extends InputStream {

    private Medicao[] dados;
    private int totalObjetos;
    private InputStream origem;

    public MedicaoInputStream(Medicao[] dados, int totalObjetos, InputStream origem) {
        if (totalObjetos > dados.length) {
            throw new IllegalArgumentException("totalObjetos maior que o tamanho do array");
        }
        this.dados = dados;
        this.totalObjetos = totalObjetos;
        this.origem = origem;
    }

    @Override
    public int read() throws IOException {
        return origem.read(); // obrigatório
    }

    // Método para receber os dados e preencher o array
    public void receber() throws IOException {
        for (int i = 0; i < totalObjetos; i++) {

            float corrente = lerFloat();
            float tensao = lerFloat();
            float fatorPotencia = lerFloat();

            dados[i] = new Medicao(corrente, tensao, fatorPotencia);
        }
    }

    private float lerFloat() throws IOException {
        int b1 = origem.read();
        int b2 = origem.read();
        int b3 = origem.read();
        int b4 = origem.read();

        if ((b1 | b2 | b3 | b4) < 0) {
            throw new IOException("Fim do stream inesperado");
        }

        int intBits = (b1 << 24) | (b2 << 16) | (b3 << 8) | b4;

        return Float.intBitsToFloat(intBits);
    }
}