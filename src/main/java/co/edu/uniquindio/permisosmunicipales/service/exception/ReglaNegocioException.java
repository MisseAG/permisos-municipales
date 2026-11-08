package co.edu.uniquindio.permisosmunicipales.service.exception;

public class ReglaNegocioException extends RuntimeException {

    private final String codigoRegla; // ej. "RN-01"

    public ReglaNegocioException(String codigoRegla, String mensaje) {
        super(mensaje);
        this.codigoRegla = codigoRegla;
    }

    public String getCodigoRegla() { return codigoRegla; }
}