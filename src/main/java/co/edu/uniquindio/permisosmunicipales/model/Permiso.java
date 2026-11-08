package co.edu.uniquindio.permisosmunicipales.model;

import co.edu.uniquindio.permisosmunicipales.model.enums.EstadoPermiso;
import co.edu.uniquindio.permisosmunicipales.model.enums.TipoPermiso;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Objects;

public class Permiso {

    private Long id;
    private Ciudadano ciudadano;
    private String numeroRadicado;
    private TipoPermiso tipoPermiso;
    private String otroTipoPermiso;
    private String asunto;
    private String direccionPredio;
    private LocalDate fechaRadicacion;
    private LocalDate fechaLimiteRespuesta;
    private EstadoPermiso estado;
    private Funcionario usuarioOperador;
    private String observaciones;
    private final List<Documento> documentos = new ArrayList<>();

    public Permiso(Long id, Ciudadano ciudadano, String numeroRadicado, TipoPermiso tipoPermiso,
                   String otroTipoPermiso, String asunto, String direccionPredio,
                   LocalDate fechaRadicacion, LocalDate fechaLimiteRespuesta,
                   EstadoPermiso estado, Funcionario usuarioOperador) {
        this.id = id;
        this.ciudadano = Objects.requireNonNull(ciudadano, "ciudadano no puede ser nulo");
        this.numeroRadicado = Objects.requireNonNull(numeroRadicado);
        this.tipoPermiso = Objects.requireNonNull(tipoPermiso);
        this.otroTipoPermiso = otroTipoPermiso;
        this.asunto = Objects.requireNonNull(asunto);
        this.direccionPredio = direccionPredio;
        this.fechaRadicacion = Objects.requireNonNull(fechaRadicacion);
        this.fechaLimiteRespuesta = Objects.requireNonNull(fechaLimiteRespuesta);
        this.estado = Objects.requireNonNull(estado);
        this.usuarioOperador = Objects.requireNonNull(usuarioOperador);
    }

    public void agregarDocumento(Documento documento) {
        documentos.add(Objects.requireNonNull(documento));
    }

    public List<Documento> getDocumentos() {
        return Collections.unmodifiableList(documentos);
    }

    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }
    public Ciudadano getCiudadano() { return ciudadano; }
    public String getNumeroRadicado() { return numeroRadicado; }
    public TipoPermiso getTipoPermiso() { return tipoPermiso; }
    public String getOtroTipoPermiso() { return otroTipoPermiso; }
    public String getAsunto() { return asunto; }
    public String getDireccionPredio() { return direccionPredio; }
    public LocalDate getFechaRadicacion() { return fechaRadicacion; }
    public LocalDate getFechaLimiteRespuesta() { return fechaLimiteRespuesta; }
    public EstadoPermiso getEstado() { return estado; }
    public void setEstado(EstadoPermiso estado) { this.estado = estado; }
    public Funcionario getUsuarioOperador() { return usuarioOperador; }
    public String getObservaciones() { return observaciones; }
    public void setObservaciones(String observaciones) { this.observaciones = observaciones; }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (!(o instanceof Permiso)) return false;
        Permiso that = (Permiso) o;
        return Objects.equals(id, that.id);
    }

    @Override
    public int hashCode() { return Objects.hash(id); }

    @Override
    public String toString() {
        return "Permiso{numeroRadicado='" + numeroRadicado + "', estado=" + estado + ", documentos=" + documentos.size() + "}";
    }
}