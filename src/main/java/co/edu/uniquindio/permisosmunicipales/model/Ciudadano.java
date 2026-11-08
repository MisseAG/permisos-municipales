package co.edu.uniquindio.permisosmunicipales.model;

import co.edu.uniquindio.permisosmunicipales.model.enums.EstadoCiudadano;
import co.edu.uniquindio.permisosmunicipales.model.enums.TipoIdentificacion;

import java.util.Objects;

public class Ciudadano {
    private Long id;
    private TipoIdentificacion tipoIdentificacion;
    private String otroTipoIdentificacion; // solo si tipoIdentificacion == OTRO; null en los demás casos
    private String numeroIdentificacion;
    private String nombres;
    private String apellidos;
    private String email;
    private String telefono;
    private String direccion;
    private EstadoCiudadano estado;

    public Ciudadano(Long id, TipoIdentificacion tipoIdentificacion, String otroTipoIdentificacion,
                     String numeroIdentificacion, String nombres, String apellidos, String email,
                     String telefono, String direccion, EstadoCiudadano estado) {
        this.id = id;
        this.tipoIdentificacion = Objects.requireNonNull(tipoIdentificacion);
        this.otroTipoIdentificacion = otroTipoIdentificacion; // sin requireNonNull: es opcional
        this.numeroIdentificacion = Objects.requireNonNull(numeroIdentificacion);
        this.nombres = Objects.requireNonNull(nombres);
        this.apellidos = Objects.requireNonNull(apellidos);
        this.email = Objects.requireNonNull(email);
        this.telefono = telefono;
        this.direccion = direccion;
        this.estado = Objects.requireNonNull(estado);
    }

    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }

    public TipoIdentificacion getTipoIdentificacion() { return tipoIdentificacion; }
    public void setTipoIdentificacion(TipoIdentificacion tipoIdentificacion) { this.tipoIdentificacion = tipoIdentificacion; }

    public String getNumeroIdentificacion() { return numeroIdentificacion; }
    public void setNumeroIdentificacion(String numeroIdentificacion) { this.numeroIdentificacion = numeroIdentificacion; }

    public String getOtroTipoIdentificacion() { return otroTipoIdentificacion; }
    public void setOtroTipoIdentificacion(String otroTipoIdentificacion) { this.otroTipoIdentificacion = otroTipoIdentificacion; }

    public String getNombres() { return nombres; }
    public void setNombres(String nombres) { this.nombres = nombres; }

    public String getApellidos() { return apellidos; }
    public void setApellidos(String apellidos) { this.apellidos = apellidos; }

    public String getEmail() { return email; }
    public void setEmail(String email) { this.email = email; }

    public String getTelefono() { return telefono; }
    public void setTelefono(String telefono) { this.telefono = telefono; }

    public String getDireccion() { return direccion; }
    public void setDireccion(String direccion) { this.direccion = direccion; }

    public EstadoCiudadano getEstado() { return estado; }
    public void setEstado(EstadoCiudadano estado) { this.estado = estado; }

    public String getNombreCompleto() { return nombres + " " + apellidos; }



    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (!(o instanceof Ciudadano)) return false;
        Ciudadano that = (Ciudadano) o;
        return Objects.equals(id, that.id);
    }

    @Override
    public int hashCode() { return Objects.hash(id); }

    @Override
    public String toString() {
        return "Ciudadano{id=" + id + ", numeroIdentificacion='" + numeroIdentificacion + "', estado=" + estado + "}";
    }
}
