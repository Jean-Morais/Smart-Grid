package main.entidade;

public class Comando {
    private String tipo; // tipo do comando, exemplo enviar mediçao, ligar/desligar interruptor
    private String alvo; // tomada, interruptor, esp32, sensor.
    private boolean estado; // ligar ou desligar 
    private int id;// id unico da esp32

    public Comando(){}

    public Comando(String tipo,String alvo, boolean estado, int id){
        this.tipo = tipo;
        this.alvo = alvo;
        this.estado = estado;
        this.id = id;
    }

    // Set ---------------------------------------------------

    public void set_id(int id){
        this.id = id;
    }

    public void set_estado(boolean estado){
        this.estado = estado;
    }

    public void set_alvo(String alvo){
        this.alvo = alvo;
    }

    public void set_tipo(String tipo){
        this.tipo = tipo;
    }

    // Get ---------------------------------------------------

    public int get_id(){
        return id;
    }

    public boolean get_estado(){
        return estado;
    }

    public String get_alvo(){
        return alvo;
    }

    public String get_tipo(){
        return tipo;
    }
}