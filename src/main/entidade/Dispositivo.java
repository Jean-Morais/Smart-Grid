package main.entidade;

import java.io.Serializable;

/**
 * Representa um dispositivo físico na rede.
 *
 * O campo "tipo" é derivado da classe concreta em tempo de execução.
 * Assim, Controlador e Medidor passam a aparecer corretamente na
 * comunicação do sistema sem exigir biblioteca externa.
 */
public class Dispositivo implements Serializable {

    private int    id;
    private String nome;
    private String lugar;
    private String ip;
    private boolean ligado;
    private Sensor  sensor; // composição "tem-um"

    public Dispositivo() {}

    public Dispositivo(int id, String nome, String ip, String lugar, Sensor sensor) {
        this.id     = id;
        this.nome   = nome;
        this.ip     = ip;
        this.lugar  = lugar;
        this.ligado = false;
        this.sensor = sensor;
    }

    // ── Serialização JSON ────────────────────────────────────────────────────

    public String toJson() {
        return "{" 
            + "\"id\":"      + id      + ","
            + "\"tipo\":\"" + getTipo() + "\"," 
            + "\"nome\":\"" + escapeJson(nome)  + "\"," 
            + "\"ip\":\""   + escapeJson(ip)    + "\"," 
            + "\"lugar\":\"" + escapeJson(lugar) + "\"," 
            + "\"ligado\":"  + ligado  + ","
            + "\"sensor\":"  + (sensor != null ? sensor.toJson() : "null")
            + "}";
    }

    /** Retorna o nome simples da classe concreta para refletir a hierarquia. */
    public String getTipo() {
        return getClass().getSimpleName();
    }

    protected static String escapeJson(String texto) {
        if (texto == null) return "";
        return texto.replace("\\", "\\\\").replace("\"", "\\\"");
    }

    @Override
    public String toString() {
        return getTipo() + "{id=" + id + ", nome='" + nome
             + "', lugar='" + lugar + "', ligado=" + ligado + "}";
    }

    // ── Getters / Setters ────────────────────────────────────────────────────

    public int     getId()      { return id; }
    public String  getNome()    { return nome; }
    public String  getIp()      { return ip; }
    public String  getLugar()   { return lugar; }
    public boolean isLigado()   { return ligado; }
    public Sensor  getSensor()  { return sensor; }

    public void setId(int id)           { this.id = id; }
    public void setNome(String nome)    { this.nome = nome; }
    public void setIp(String ip)        { this.ip = ip; }
    public void setLugar(String lugar)  { this.lugar = lugar; }
    public void setLigado(boolean l)    { this.ligado = l; }
    public void setSensor(Sensor s)     { this.sensor = s; }
}
