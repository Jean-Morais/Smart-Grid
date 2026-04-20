package main.streams;

import java.io.IOException;
import java.io.OutputStream;
import java.nio.charset.StandardCharsets;
import main.entidade.Comando;

public class ComandoOutputStream extends OutputStream {

    private Comando[] dados;
    private int totalObjetos;
    private OutputStream destino;

    public ComandoOutputStream(Comando[] dados, int totalObjetos, OutputStream destino) {
        if (totalObjetos > dados.length) {
            throw new IllegalArgumentException("totalObjetos maior que o tamanho do array");
        }
        this.dados = dados;
        this.totalObjetos = totalObjetos;
        this.destino = destino;
    }

    @Override
    public void write(int b) throws IOException {
        destino.write(b);
    }

    public void enviar() throws IOException {

        enviarInt(totalObjetos);

        for (int i = 0; i < totalObjetos; i++) {
            Comando c = dados[i];

            enviarString(c.get_tipo()); // tipo
            enviarString(c.get_alvo());        // alvo
            enviarBoolean(c.get_estado());     // estado
            enviarInt(c.get_id());             // id
        }

        destino.flush();
    }

    private void enviarString(String s) throws IOException {
        if (s == null) s = "";

        byte[] bytes = s.getBytes(StandardCharsets.UTF_8);

        enviarInt(bytes.length); // tamanho
        destino.write(bytes);    // conteúdo
    }

    private void enviarBoolean(boolean b) throws IOException {
        destino.write(b ? 1 : 0);
    }

    private void enviarInt(int valor) throws IOException {
        destino.write((valor >> 24) & 0xFF);
        destino.write((valor >> 16) & 0xFF);
        destino.write((valor >> 8) & 0xFF);
        destino.write(valor & 0xFF);
    }
}