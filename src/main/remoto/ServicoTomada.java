package main.remoto;

import java.rmi.Remote;
import java.rmi.RemoteException;
import main.entidade.Reply;

/**
 * Interface remota principal do sistema Smart Grid.
 *
 * Expõe 4 métodos para invocação remota (requisito do trabalho):
 *   1. ligarTomada       — altera estado do dispositivo
 *   2. desligarTomada    — altera estado do dispositivo
 *   3. consultarDispositivo — retorna JSON do Dispositivo (passagem por valor)
 *   4. getServicoMedicao — retorna stub remoto (passagem por referência)
 */
public interface ServicoTomada extends Remote {

    /** Liga a tomada de id informado. Retorna "OK" ou mensagem de erro. */
    Reply ligarTomada(int id) throws RemoteException;

    /** Desliga a tomada de id informado. Retorna "OK" ou mensagem de erro. */
    Reply desligarTomada(int id) throws RemoteException;

    /**
     * Consulta o estado completo do dispositivo.
     * O Reply contém o JSON do Dispositivo — passagem por valor
     * com representação externa de dados.
     */
    Reply consultarDispositivo(int id) throws RemoteException;

    /**
     * Retorna um stub remoto para o serviço de medição daquele dispositivo.
     * Demonstra passagem por referência — a execução ocorre no servidor.
     */
    ServicoMedicao getServicoMedicao(int id) throws RemoteException;
}
