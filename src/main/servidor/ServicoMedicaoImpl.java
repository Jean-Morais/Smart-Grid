package main.servidor;

import java.rmi.RemoteException;
import java.rmi.server.UnicastRemoteObject;
import java.util.ArrayList;
import java.util.List;
import java.util.Random;

import main.entidade.Medicao;
import main.entidade.Medidor;
import main.remoto.ServicoMedicao;
import main.services.MedicaoService;


public class ServicoMedicaoImpl extends UnicastRemoteObject implements ServicoMedicao {

    private final Medidor        medidor;
    private final MedicaoService service;
    private final List<Medicao>  historico;
    private final Random         random;

    public ServicoMedicaoImpl(Medidor medidor) throws RemoteException {
        super();
        this.medidor   = medidor;
        this.service   = new MedicaoService();
        this.historico = new ArrayList<>();
        this.random    = new Random();
    }

    private Medicao gerarNovaMedicao() {
        return new Medicao(
            0.5f  + random.nextFloat() * 4.5f,
            210f  + random.nextFloat() * 20f,
            0.80f + random.nextFloat() * 0.19f
        );
    }

    // ── Métodos remotos ───────────────────────────────────────────────────────

    /**
     * Simula uma leitura do sensor do dispositivo.
     * Em produção, aqui viria a leitura real do hardware.
     * Retorna Medicao por valor (Serializable).
     */
    @Override
    public Medicao obterMedicaoAtual() throws RemoteException {
        Medicao m = gerarNovaMedicao();
        historico.add(m);
        medidor.registrarMedicao(m);
        System.out.printf("[Sensor %s] Nova medição: %s (%s)%n",
                medidor.getSensor().getId(),
                m,
                medidor.isGerando() ? "geração" : "consumo");
        return m;
    }

    /** P = V × I × FP  (Watts) — delega para MedicaoService. */
    @Override
    public float calcularPotencia() throws RemoteException {
        Medicao m = obterMedicaoAtual();
        return service.potencia(m);
    }

    /** E = P × t  (Wh) — delega para MedicaoService. */
    @Override
    public float calcularEnergia(float tempoHoras) throws RemoteException {
        Medicao m = obterMedicaoAtual();
        return service.energia(m, tempoHoras);
    }

    /**
     * Resumo em JSON com medição atual, potência calculada e média histórica.
     * Passagem por valor com representação externa de dados.
     */
    @Override
    public String obterResumoJson() throws RemoteException {
        Medicao m      = obterMedicaoAtual();
        float potencia  = service.potencia(m);
        float mediaPot  = service.mediaPotencia(historico);

        return "{" 
            + "\"medidor\":" + medidor.toJson() + ","
            + "\"modo\":\"" + (medidor.isGerando() ? "geracao" : "consumo") + "\"," 
            + "\"medicaoAtual\":" + m.toJson() + ","
            + "\"potenciaW\":" + potencia + ","
            + "\"mediaPotenciaW\":" + mediaPot + ","
            + "\"totalMedicoes\":" + historico.size()
            + "}";
    }
}
