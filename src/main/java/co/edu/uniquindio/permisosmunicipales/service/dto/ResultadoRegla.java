package co.edu.uniquindio.permisosmunicipales.service.dto;

public class ResultadoRegla {
    private final String codigo;      // "RN-01"
    private final boolean cumple;
    private final String titulo;      // "Ciudadano activo"
    private final String detalle;     // "Estado consultado" / mensaje de error si no cumple

    public ResultadoRegla(String codigo, boolean cumple, String titulo, String detalle) {
        this.codigo = codigo;
        this.cumple = cumple;
        this.titulo = titulo;
        this.detalle = detalle;
    }

    public String getCodigo() { return codigo; }
    public boolean isCumple() { return cumple; }
    public String getTitulo() { return titulo; }
    public String getDetalle() { return detalle; }
}