package main.streams;

import java.io.IOException;
import java.io.OutputStream;
import main.entidade.Medicao;

public class MedicaoOutputStream extends OutputStream {

    private Medicao[] dados;
    private int totalObjetos;
    private OutputStream destino;

    public MedicaoOutputStream(Medicao[] dados, int totalObjetos, OutputStream destino) {
        if (totalObjetos > dados.length) {
            throw new IllegalArgumentException("totalObjetos maior que o tamanho do array");
        }
        this.dados = dados;
        this.totalObjetos = totalObjetos;
        this.destino = destino;
    }

    @Override
    public void write(int b) throws IOException {
        // Não usado nesse contexto, mas é obrigatório implementar
        destino.write(b);
    }

    // Método para enviar o array de POJOs
    public void enviar() throws IOException {
        for (int i = 0; i < totalObjetos; i++) {
            Medicao m = dados[i];

            // Para cada main.entidade.Medicao, envia os 3 atributos como bytes
            // Aqui usamos 4 bytes por float (IEEE 754)
            enviarFloat(m.getCorrente());
            enviarFloat(m.getTensao());
            enviarFloat(m.getFatorPotencia());
        }
        destino.flush();
    }

    private void enviarFloat(float valor) throws IOException {
        int intBits = Float.floatToIntBits(valor);
        destino.write((intBits >> 24) & 0xFF);
        destino.write((intBits >> 16) & 0xFF);
        destino.write((intBits >> 8) & 0xFF);
        destino.write(intBits & 0xFF);
    }
}