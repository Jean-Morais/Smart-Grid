package main.streams;

import java.io.IOException;
import java.io.InputStream;
import java.nio.charset.StandardCharsets;
import main.entidade.Comando;

public class ComandoInputStream extends InputStream {

    private InputStream origem;

    public ComandoInputStream(InputStream origem) {
        this.origem = origem;
    }

    @Override
    public int read() throws IOException {
        return origem.read();
    }

    public Comando[] receber() throws IOException {

        int totalObjetos = lerInt(); // lê quantidade

        Comando[] dados = new Comando[totalObjetos];

        for (int i = 0; i < totalObjetos; i++) {

            String tipo = lerString();
            String alvo = lerString();
            boolean estado = lerBoolean();
            int id = lerInt();

            dados[i] = new Comando(tipo, alvo, estado, id);
        }

        return dados;
    }

    private String lerString() throws IOException {
        int tamanho = lerInt();

        byte[] bytes = new byte[tamanho];

        int lidos = 0;
        while (lidos < tamanho) {
            int r = origem.read(bytes, lidos, tamanho - lidos);
            if (r == -1) {
                throw new IOException("Fim do stream inesperado");
            }
            lidos += r;
        }

        return new String(bytes, StandardCharsets.UTF_8);
    }

    private boolean lerBoolean() throws IOException {
        int b = origem.read();
        if (b == -1) {
            throw new IOException("Fim do stream inesperado");
        }
        return b != 0;
    }

    private int lerInt() throws IOException {
        int b1 = origem.read();
        int b2 = origem.read();
        int b3 = origem.read();
        int b4 = origem.read();

        if ((b1 | b2 | b3 | b4) < 0) {
            throw new IOException("Fim do stream inesperado");
        }

        return (b1 << 24) | (b2 << 16) | (b3 << 8) | b4;
    }
}