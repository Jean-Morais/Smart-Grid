package main.entidade;

import java.io.Serializable;


public class Medicao implements Serializable {

    private float corrente;       // Amperes
    private float tensao;         // Volts
    private float fator_potencia; // 0.0 – 1.0
    private long  timestamp;      // epoch millis

    public Medicao() {}

    public Medicao(float corrente, float tensao, float fator_potencia) {
        this.corrente       = corrente;
        this.tensao         = tensao;
        this.fator_potencia = fator_potencia;
        this.timestamp      = System.currentTimeMillis();
    }

    // ── Serialização externa (JSON) ──────────────────────────────────────────

    /** Serializa para JSON — representação externa de dados (passagem por valor). */
    public String toJson() {
        return "{"
            + "\"corrente\":"      + corrente       + ","
            + "\"tensao\":"        + tensao         + ","
            + "\"fatorPotencia\":" + fator_potencia + ","
            + "\"timestamp\":"     + timestamp
            + "}";
    }

    /** Desserializa a partir de JSON simples (sem biblioteca externa). */
    public static Medicao fromJson(String json) {
        Medicao m = new Medicao();
        m.corrente       = parseFloat(json, "corrente");
        m.tensao         = parseFloat(json, "tensao");
        m.fator_potencia = parseFloat(json, "fatorPotencia");
        m.timestamp      = parseLong(json,  "timestamp");
        return m;
    }

    private static float parseFloat(String json, String key) {
        String search = "\"" + key + "\":";
        int i = json.indexOf(search);
        if (i < 0) return 0f;
        int start = i + search.length();
        int end   = json.indexOf(',', start);
        if (end < 0) end = json.indexOf('}', start);
        return Float.parseFloat(json.substring(start, end).trim());
    }

    private static long parseLong(String json, String key) {
        String search = "\"" + key + "\":";
        int i = json.indexOf(search);
        if (i < 0) return 0L;
        int start = i + search.length();
        int end   = json.indexOf(',', start);
        if (end < 0) end = json.indexOf('}', start);
        return Long.parseLong(json.substring(start, end).trim());
    }

    // ── toString ─────────────────────────────────────────────────────────────

    @Override
    public String toString() {
        return "Medicao{corrente=" + corrente
             + "A, tensao=" + tensao
             + "V, fatorPotencia=" + fator_potencia
             + ", timestamp=" + timestamp + "}";
    }

    // ── Getters / Setters ────────────────────────────────────────────────────

    public float getCorrente()       { return corrente; }
    public float getTensao()         { return tensao; }
    public float getFatorPotencia()  { return fator_potencia; }
    public long  getTimestamp()      { return timestamp; }

    public void setCorrente(float corrente)             { this.corrente = corrente; }
    public void setTensao(float tensao)                 { this.tensao = tensao; }
    public void setFatorPotencia(float fator_potencia)  { this.fator_potencia = fator_potencia; }
    public void setTimestamp(long timestamp)            { this.timestamp = timestamp; }
}
