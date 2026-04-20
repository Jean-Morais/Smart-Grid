package main.entidade;

public class Medicao {
    private float corrente;
    private float tensao;
    private float fator_potencia;
    private int time;

    public Medicao(){}

    public Medicao(float corrente,float tensao, float fator_potencia, int time){
        this.corrente = corrente;
        this.tensao = tensao;
        this.fator_potencia = fator_potencia;
        this.time = time;
    }

    public Medicao(float corrente,float tensao, float fator_potencia){
        this.corrente = corrente;
        this.tensao = tensao;
        this.fator_potencia = fator_potencia;
    }

    @Override
    public String toString() {
        return "main.entidade.Medicao{" +
               "corrente=" + corrente +
               ", tensao=" + tensao +
               ", fatorPotencia=" + fator_potencia +
               '}';
    }

    // Set ---------------------------------------------------

    public void setCorrente(float corrente){
        this.corrente = corrente;
    }

    public void setTensao(float tensao){
        this.tensao = tensao;
    }

    public void setFatorpotencia(float fator_potencia){
        this.fator_potencia = fator_potencia;
    }

    public void setTime(int time){
        this.time = time;
    }

    // Get ---------------------------------------------------

    public float getCorrente(){
        return corrente;
    }

    public float getTensao(){
        return tensao;
    }

    public float getFatorPotencia(){
        return fator_potencia;
    }

    public int getTime(){
        return time;
    }
}