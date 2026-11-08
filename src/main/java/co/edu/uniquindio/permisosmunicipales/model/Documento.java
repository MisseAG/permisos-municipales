package co.edu.uniquindio.permisosmunicipales.model;

import co.edu.uniquindio.permisosmunicipales.model.enums.TipoDocumentoAdjunto;
import java.time.LocalDateTime;
import java.util.Objects;

public class Documento {

    private Long id;
    private TipoDocumentoAdjunto tipoDocumentoAdjunto;
    private String otroTipoDocumento; // solo si tipoDocumentoAdjunto == OTRO
    private String nombreArchivo;
    private String rutaArchivo;
    private String formato;
    private long tamanoBytes;
    private LocalDateTime fechaCarga;

    public Documento(Long id, TipoDocumentoAdjunto tipoDocumentoAdjunto, String otroTipoDocumento,
                     String nombreArchivo, String rutaArchivo, String formato,
                     long tamanoBytes, LocalDateTime fechaCarga) {
        this.id = id;
        this.tipoDocumentoAdjunto = Objects.requireNonNull(tipoDocumentoAdjunto);
        this.otroTipoDocumento = otroTipoDocumento;
        this.nombreArchivo = Objects.requireNonNull(nombreArchivo);
        this.rutaArchivo = Objects.requireNonNull(rutaArchivo);
        this.formato = Objects.requireNonNull(formato);
        if (tamanoBytes <= 0) {
            throw new IllegalArgumentException("tamanoBytes debe ser mayor a 0");
        }
        this.tamanoBytes = tamanoBytes;
        this.fechaCarga = Objects.requireNonNull(fechaCarga);
    }

    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }
    public TipoDocumentoAdjunto getTipoDocumentoAdjunto() { return tipoDocumentoAdjunto; }
    public String getOtroTipoDocumento() { return otroTipoDocumento; }
    public String getNombreArchivo() { return nombreArchivo; }
    public String getRutaArchivo() { return rutaArchivo; }
    public String getFormato() { return formato; }
    public long getTamanoBytes() { return tamanoBytes; }
    public LocalDateTime getFechaCarga() { return fechaCarga; }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (!(o instanceof Documento)) return false;
        Documento that = (Documento) o;
        return Objects.equals(id, that.id);
    }

    @Override
    public int hashCode() { return Objects.hash(id); }

    @Override
    public String toString() {
        return "Documento{id=" + id + ", tipo=" + tipoDocumentoAdjunto + ", nombreArchivo='" + nombreArchivo + "'}";
    }
}