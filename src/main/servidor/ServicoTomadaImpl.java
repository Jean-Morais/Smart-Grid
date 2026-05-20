package main.servidor;

import java.rmi.RemoteException;
import java.rmi.server.UnicastRemoteObject;
import java.util.HashMap;
import java.util.Map;

import main.entidade.Controlador;
import main.entidade.Dispositivo;
import main.entidade.Medicao;
import main.entidade.Medidor;
import main.entidade.Reply;
import main.entidade.Sensor;
import main.remoto.ServicoMedicao;
import main.remoto.ServicoTomada;

/**
 * Implementação remota de ServicoTomada.
 *
 * "é-um" UnicastRemoteObject (1ª composição por extensão do trabalho).
 *
 * Mantém, para cada ID, um Controlador e um Medidor compartilhando o mesmo
 * Sensor/Medicao. O cliente consulta o Controlador, e o serviço de medições
 * expõe o Medidor correspondente.
 */
public class ServicoTomadaImpl extends UnicastRemoteObject implements ServicoTomada {

    private final Map<Integer, Controlador>    controladores   = new HashMap<>();
    private final Map<Integer, Medidor>        medidores       = new HashMap<>();
    private final Map<Integer, ServicoMedicao> servicosMedicao = new HashMap<>();

    public ServicoTomadaImpl() throws RemoteException {
        super();
        inicializarDispositivos();
    }

    /**
     * Pré-carrega alguns dispositivos simulados para o servidor já iniciar
     * com dados, facilitando os testes.
     */
    private void inicializarDispositivos() throws RemoteException {
        registrar(1, "Tomada Sala",    "192.168.1.10", "Sala",    false);
        registrar(2, "Tomada Quarto",  "192.168.1.11", "Quarto",  false);
        registrar(3, "Tomada Cozinha", "192.168.1.12", "Cozinha", false);
    }

    private void registrar(int id, String nome, String ip, String lugar, boolean gerando)
            throws RemoteException {
        Sensor sensor = new Sensor("S-" + id, "eletrico", true, new Medicao(0f, 0f, 0f));
        Controlador controlador = new Controlador(id, nome, ip, lugar, sensor);
        Medidor medidor = new Medidor(id, nome, ip, lugar, sensor, gerando);

        controladores.put(id, controlador);
        medidores.put(id, medidor);
        servicosMedicao.put(id, new ServicoMedicaoImpl(medidor));
    }

    private Controlador obterControlador(int id) {
        return controladores.get(id);
    }

    private Medidor obterMedidor(int id) {
        return medidores.get(id);
    }

    // ── Métodos remotos ───────────────────────────────────────────────────────

    @Override
    public Reply ligarTomada(int id) throws RemoteException {
        Controlador c = obterControlador(id);
        Medidor m = obterMedidor(id);
        if (c == null || m == null) return new Reply("{\"erro\":\"Dispositivo " + id + " nao encontrado\"}");

        c.enviarComando(true);
        m.setLigado(true);
        System.out.println("[Servidor] Tomada " + id + " (" + c.getNome() + ") LIGADA");
        return new Reply("{\"status\":\"OK\",\"id\":" + id + ",\"ligado\":true,\"tipo\":\"" + c.getTipo() + "\"}");
    }

    @Override
    public Reply desligarTomada(int id) throws RemoteException {
        Controlador c = obterControlador(id);
        Medidor m = obterMedidor(id);
        if (c == null || m == null) return new Reply("{\"erro\":\"Dispositivo " + id + " nao encontrado\"}");

        c.enviarComando(false);
        m.setLigado(false);
        System.out.println("[Servidor] Tomada " + id + " (" + c.getNome() + ") DESLIGADA");
        return new Reply("{\"status\":\"OK\",\"id\":" + id + ",\"ligado\":false,\"tipo\":\"" + c.getTipo() + "\"}");
    }

    /**
     * Retorna o JSON completo do Dispositivo — passagem por valor
     * com representação externa de dados (JSON).
     */
    @Override
    public Reply consultarDispositivo(int id) throws RemoteException {
        Dispositivo d = obterControlador(id);
        if (d == null) return new Reply("{\"erro\":\"Dispositivo " + id + " nao encontrado\"}");
        return new Reply(d.toJson());
    }

    /**
     * Retorna um stub remoto de ServicoMedicao para o dispositivo solicitado.
     * O cliente receberá uma referência remota — passagem por referência.
     */
    @Override
    public ServicoMedicao getServicoMedicao(int id) throws RemoteException {
        return servicosMedicao.get(id);
    }
}
