package co.edu.uniquindio.permisosmunicipales.service.dto;

import java.util.List;

public class ResultadoRadicacion {

    private final boolean exitosa;
    private final List<ResultadoRegla> resultados;
    private final Comprobante comprobante; // null si no fue exitosa

    private ResultadoRadicacion(boolean exitosa, List<ResultadoRegla> resultados, Comprobante comprobante) {
        this.exitosa = exitosa;
        this.resultados = resultados;
        this.comprobante = comprobante;
    }

    public static ResultadoRadicacion exitosa(List<ResultadoRegla> resultados, Comprobante comprobante) {
        return new ResultadoRadicacion(true, resultados, comprobante);
    }

    public static ResultadoRadicacion fallida(List<ResultadoRegla> resultados) {
        return new ResultadoRadicacion(false, resultados, null);
    }

    public boolean isExitosa() { return exitosa; }
    public List<ResultadoRegla> getResultados() { return resultados; }
    public Comprobante getComprobante() { return comprobante; }
}