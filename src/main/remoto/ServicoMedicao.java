package main.remoto;

import java.rmi.Remote;
import java.rmi.RemoteException;
import main.entidade.Medicao;

/**
 * Interface remota secundária — representa o serviço de medição
 * de um dispositivo específico.
 *
 * Um stub dessa interface é retornado por ServicoTomada.getServicoMedicao(),
 * demonstrando passagem por referência: o cliente obtém uma referência remota
 * e a execução de cada método ocorre no servidor.
 *
 * Os objetos Medicao retornados trafegam por valor (Serializable + JSON).
 */
public interface ServicoMedicao extends Remote {

    /** Retorna a medição atual do sensor do dispositivo (passagem por valor). */
    Medicao obterMedicaoAtual() throws RemoteException;

    /** Calcula e retorna a potência ativa em Watts (P = V × I × FP). */
    float calcularPotencia() throws RemoteException;

    /**
     * Calcula a energia consumida em kWh dado um período em horas.
     * Usa os cálculos já existentes em MedicaoService.
     */
    float calcularEnergia(float tempoHoras) throws RemoteException;

    /**
     * Retorna um resumo em JSON com medição + potência + histórico de médias.
     * Passagem por valor com representação externa de dados.
     */
    String obterResumoJson() throws RemoteException;
}
