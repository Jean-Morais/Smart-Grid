package main.services;

import java.util.List;
import main.entidade.Medicao;

public class MedicaoService {

    public float potencia(Medicao m){
        return m.getCorrente() * m.getTensao() * m.getFatorPotencia();
    }

    public float energia(Medicao m, float tempoHoras) {
        return potencia(m) * tempoHoras;
    }

    public float mediaPotencia(List<Medicao> lista) {
        float soma = 0;
        for (Medicao m : lista) {
            soma += potencia(m);
        }
        return soma / lista.size();
    }
}