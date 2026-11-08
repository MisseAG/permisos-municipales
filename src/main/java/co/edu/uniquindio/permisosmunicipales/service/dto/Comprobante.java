package co.edu.uniquindio.permisosmunicipales.service.dto;

import java.time.LocalDate;
import java.util.List;

public class Comprobante {

    private final String numeroRadicado;
    private final String nombreCiudadano;
    private final String documentoSolicitante;     // ej. "CC 1098765432"
    private final String tipoPermisoDescripcion;
    private final LocalDate fechaRadicacion;
    private final LocalDate fechaLimiteRespuesta;
    private final int cantidadDocumentos;
    private final List<DocumentoRecibido> documentosRecibidos;
    private final String nombreFuncionario;

    public Comprobante(String numeroRadicado, String nombreCiudadano, String documentoSolicitante,
                       String tipoPermisoDescripcion, LocalDate fechaRadicacion,
                       LocalDate fechaLimiteRespuesta, List<DocumentoRecibido> documentosRecibidos,
                       String nombreFuncionario) {
        this.numeroRadicado = numeroRadicado;
        this.nombreCiudadano = nombreCiudadano;
        this.documentoSolicitante = documentoSolicitante;
        this.tipoPermisoDescripcion = tipoPermisoDescripcion;
        this.fechaRadicacion = fechaRadicacion;
        this.fechaLimiteRespuesta = fechaLimiteRespuesta;
        this.documentosRecibidos = documentosRecibidos;
        this.cantidadDocumentos = documentosRecibidos.size();
        this.nombreFuncionario = nombreFuncionario;
    }

    public String getNumeroRadicado() { return numeroRadicado; }
    public String getNombreCiudadano() { return nombreCiudadano; }
    public String getDocumentoSolicitante() { return documentoSolicitante; }
    public String getTipoPermisoDescripcion() { return tipoPermisoDescripcion; }
    public LocalDate getFechaRadicacion() { return fechaRadicacion; }
    public LocalDate getFechaLimiteRespuesta() { return fechaLimiteRespuesta; }
    public int getCantidadDocumentos() { return cantidadDocumentos; }
    public List<DocumentoRecibido> getDocumentosRecibidos() { return documentosRecibidos; }
    public String getNombreFuncionario() { return nombreFuncionario; }

    // DTO liviano solo para mostrar en el comprobante — no es el modelo Documento completo
    // (que vive en model/Documento.java con su tipoDocumentoAdjunto, ruta, etc.)
    public static class DocumentoRecibido {
        private final String nombreArchivo;
        private final long tamanoBytes;

        public DocumentoRecibido(String nombreArchivo, long tamanoBytes) {
            this.nombreArchivo = nombreArchivo;
            this.tamanoBytes = tamanoBytes;
        }

        public String getNombreArchivo() { return nombreArchivo; }
        public long getTamanoBytes() { return tamanoBytes; }

        public String getTamanoFormateado() {
            if (tamanoBytes < 1024) return tamanoBytes + " B";
            double kb = tamanoBytes / 1024.0;
            if (kb < 1024) return String.format("%.0f KB", kb);
            double mb = kb / 1024.0;
            return String.format("%.1f MB", mb).replace('.', ','); // "1,2 MB", formato local
        }
    }
}