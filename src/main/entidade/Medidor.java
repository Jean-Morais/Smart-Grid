package main.entidade;

/**
 * Medidor é um Dispositivo que coleta consumo/geração.
 *
 * Ele compartilha o mesmo Sensor/Medicao do Controlador correspondente,
 * garantindo coerência entre comando e leitura no sistema distribuído.
 */
public class Medidor extends Dispositivo {

    private boolean gerando;

    public Medidor() {
        super();
    }

    public Medidor(int id, String nome, String ip, String lugar, Sensor sensor) {
        this(id, nome, ip, lugar, sensor, false);
    }

    public Medidor(int id, String nome, String ip, String lugar, Sensor sensor, boolean gerando) {
        super(id, nome, ip, lugar, sensor);
        this.gerando = gerando;
    }

    public boolean isGerando() {
        return gerando;
    }

    public void setGerando(boolean gerando) {
        this.gerando = gerando;
    }

    /** Atualiza a medição armazenada no sensor associado. */
    public void registrarMedicao(Medicao medicao) {
        if (getSensor() != null) {
            getSensor().setMedicao(medicao);
        }
    }

    /** Retorna a última medição conhecida pelo sensor. */
    public Medicao obterMedicaoAtual() {
        return getSensor() != null ? getSensor().getMedicao() : null;
    }

    /** Calcula a potência ativa a partir da medição atual. */
    public float coletarPotenciaW() {
        Medicao medicao = obterMedicaoAtual();
        if (medicao == null) return 0f;
        return medicao.getCorrente() * medicao.getTensao() * medicao.getFatorPotencia();
    }

    /**
     * Se o medidor estiver em modo de consumo, retorna o consumo em watts.
     * Caso esteja em modo de geração, retorna 0.
     */
    public float coletarConsumoW() {
        return gerando ? 0f : coletarPotenciaW();
    }

    /**
     * Se o medidor estiver em modo de geração, retorna a geração em watts.
     * Caso esteja em modo de consumo, retorna 0.
     */
    public float coletarGeracaoW() {
        return gerando ? coletarPotenciaW() : 0f;
    }
}
