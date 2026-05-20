package main.services;

import java.util.List;
import main.entidade.Medicao;

/**
 * Serviço de cálculos elétricos.
 * Utilizado internamente por ServicoMedicaoImpl — os resultados
 * chegam ao cliente via invocação remota dos métodos de ServicoMedicao.
 */
public class MedicaoService {

    /**
     * Potência ativa: P = V × I × FP  (Watts)
     */
    public float potencia(Medicao m) {
        return m.getCorrente() * m.getTensao() * m.getFatorPotencia();
    }

    /**
     * Energia consumida: E = P × t  (Wh)
     * Divida por 1000 para obter kWh.
     */
    public float energia(Medicao m, float tempoHoras) {
        return potencia(m) * tempoHoras;
    }

    /**
     * Média da potência ativa sobre uma lista de medições históricas.
     */
    public float mediaPotencia(List<Medicao> lista) {
        if (lista == null || lista.isEmpty()) return 0f;
        float soma = 0f;
        for (Medicao m : lista) {
            soma += potencia(m);
        }
        return soma / lista.size();
    }
}
