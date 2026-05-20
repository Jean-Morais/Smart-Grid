package main.entidade;

/**
 * Controlador é um Dispositivo que toma decisões ou envia comandos.
 *
 * Na arquitetura do Smart Grid, ele representa a parte ativa da tomada:
 * liga, desliga e pode decidir com base na potência recebida.
 */
public class Controlador extends Dispositivo {

    public Controlador() {
        super();
    }

    public Controlador(int id, String nome, String ip, String lugar, Sensor sensor) {
        super(id, nome, ip, lugar, sensor);
    }

    /** Envia um comando explícito para ligar ou desligar o dispositivo. */
    public void enviarComando(boolean ligar) {
        setLigado(ligar);
    }

    /**
     * Toma decisão com base na potência medida.
     * Retorna true para manter/lidar ligado, false para desligar.
     */
    public boolean tomarDecisao(float potenciaAtualW, float limiteW) {
        boolean manterLigado = potenciaAtualW <= limiteW;
        setLigado(manterLigado);
        return manterLigado;
    }
}
