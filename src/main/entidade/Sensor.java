package main.entidade;

import java.io.Serializable;

/**
 * Representa um sensor físico acoplado a um Dispositivo.
 * Composição "tem-um" Medicao — a medição mais recente coletada pelo sensor.
 */
public class Sensor implements Serializable {

    private String  id;
    private String  tipo;   // ex.: "eletrico", "temperatura"
    private boolean ativo;
    private Medicao medicao; // composição "tem-um"

    public Sensor() {}

    public Sensor(String id, String tipo, boolean ativo, Medicao medicao) {
        this.id      = id;
        this.tipo    = tipo;
        this.ativo   = ativo;
        this.medicao = medicao;
    }

    // ── Serialização JSON ────────────────────────────────────────────────────

    public String toJson() {
        return "{" 
            + "\"id\":\""   + Dispositivo.escapeJson(id)   + "\"," 
            + "\"tipo\":\"" + Dispositivo.escapeJson(tipo) + "\"," 
            + "\"ativo\":"  + ativo + ","
            + "\"medicao\":" + (medicao != null ? medicao.toJson() : "null")
            + "}";
    }

    @Override
    public String toString() {
        return "Sensor{id='" + id + "', tipo='" + tipo
             + "', ativo=" + ativo + ", medicao=" + medicao + "}";
    }

    // ── Getters / Setters ────────────────────────────────────────────────────

    public String  getId()      { return id; }
    public String  getTipo()    { return tipo; }
    public boolean isAtivo()    { return ativo; }
    public Medicao getMedicao() { return medicao; }

    public void setId(String id)          { this.id = id; }
    public void setTipo(String tipo)      { this.tipo = tipo; }
    public void setAtivo(boolean ativo)   { this.ativo = ativo; }
    public void setMedicao(Medicao m)     { this.medicao = m; }
}
