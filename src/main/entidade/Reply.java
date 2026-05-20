package main.entidade;

import java.io.Serializable;

/**
 * Empacota a resposta do servidor ao cliente no protocolo requisição-resposta.
 * O resultado é sempre uma String JSON, garantindo representação externa de dados.
 */
public class Reply implements Serializable {

    private String resultado; // JSON ou texto simples

    public Reply() {}

    public Reply(String resultado) {
        this.resultado = resultado;
    }

    public String getResultado() {
        return resultado;
    }

    @Override
    public String toString() {
        return resultado;
    }
}
