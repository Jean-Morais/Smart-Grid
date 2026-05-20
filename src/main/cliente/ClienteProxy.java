package main.cliente;

import main.entidade.Medicao;
import main.entidade.Reply;
import main.remoto.ServicoMedicao;
import main.remoto.ServicoTomada;


public class ClienteProxy {

    private final ServicoTomada servico;

    public ClienteProxy(ServicoTomada servico) {
        this.servico = servico;
    }

    /**
     * Protocolo requisição-resposta conforme
     *
     * @param objectReference  nome do objeto remoto alvo
     * @param methodId         identificador do método
     * @param arguments        argumentos em JSON
     * @return                 resultado em JSON/texto
     */
    public String doOperation(String objectReference, String methodId, String arguments)
            throws Exception {

        // Extrai o id do argumento JSON simples: {"id":N}
        int id = extrairId(arguments);

        switch (objectReference) {

            case "ServicoTomada":
                return doServicoTomada(methodId, id, arguments);

            case "ServicoMedicao":
                return doServicoMedicao(methodId, id, arguments);

            default:
                return "{\"erro\":\"objectReference desconhecido: " + objectReference + "\"}";
        }
    }

    // ── Despacho para ServicoTomada ───────────────────────────────────────────

    private String doServicoTomada(String methodId, int id, String arguments) throws Exception {
        Reply reply;
        switch (methodId) {
            case "ligarTomada":
                reply = servico.ligarTomada(id);
                return reply.getResultado();

            case "desligarTomada":
                reply = servico.desligarTomada(id);
                return reply.getResultado();

            case "consultarDispositivo":
                reply = servico.consultarDispositivo(id);
                return reply.getResultado();

            default:
                return "{\"erro\":\"Metodo desconhecido: " + methodId + "\"}";
        }
    }

    // ── Despacho para ServicoMedicao (passagem por referência) ───────────────

    private String doServicoMedicao(String methodId, int id, String arguments) throws Exception {
        ServicoMedicao sm = servico.getServicoMedicao(id);
        if (sm == null) {
            return "{\"erro\":\"Dispositivo " + id + " nao encontrado\"}";
        }

        switch (methodId) {
            case "obterMedicaoAtual":
                Medicao m = sm.obterMedicaoAtual();
                return m.toJson();

            case "calcularPotencia":
                float potencia = sm.calcularPotencia();
                return "{\"potenciaW\":" + potencia + "}";

            case "calcularEnergia":
                float horas   = extrairHoras(arguments);
                float energia = sm.calcularEnergia(horas);
                return "{\"energiaWh\":" + energia + ",\"tempoHoras\":" + horas + "}";

            case "obterResumoJson":
                return sm.obterResumoJson();

            default:
                return "{\"erro\":\"Metodo desconhecido: " + methodId + "\"}";
        }
    }

    // ── Utilitários de parse de argumentos JSON simples ──────────────────────

    private int extrairId(String json) {
        try {
            String s = json.replaceAll("\\s", "");
            int i = s.indexOf("\"id\":");
            if (i < 0) return -1;
            int start = i + 5;
            int end = start;
            while (end < s.length() && (Character.isDigit(s.charAt(end)) || s.charAt(end) == '-'))
                end++;
            return Integer.parseInt(s.substring(start, end));
        } catch (Exception e) {
            return -1;
        }
    }

    private float extrairHoras(String json) {
        try {
            String s = json.replaceAll("\\s", "");
            int i = s.indexOf("\"horas\":");
            if (i < 0) return 1f;
            int start = i + 8;
            int end = start;
            while (end < s.length() && (Character.isDigit(s.charAt(end)) || s.charAt(end) == '.' || s.charAt(end) == '-'))
                end++;
            return Float.parseFloat(s.substring(start, end));
        } catch (Exception e) {
            return 1f;
        }
    }
}
