package co.edu.uniquindio.permisosmunicipales.model;

import java.util.Objects;

public class Funcionario {

    private Long id;
    private String usuario; // login único — opcional, quítalo si no lo necesitas
    private String nombres;
    private String apellidos;

    public Funcionario(Long id, String usuario, String nombres, String apellidos) {
        this.id = Objects.requireNonNull(id, "id no puede ser nulo");
        this.usuario = Objects.requireNonNull(usuario, "usuario no puede ser nulo");
        this.nombres = Objects.requireNonNull(nombres, "nombres no puede ser nulo");
        this.apellidos = Objects.requireNonNull(apellidos, "apellidos no puede ser nulo");
    }

    public Long getId() { return id; }
    public String getUsuario() { return usuario; }
    public String getNombres() { return nombres; }
    public String getApellidos() { return apellidos; }

    public String getNombreCompleto() { return nombres + " " + apellidos; }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (!(o instanceof Funcionario)) return false;
        Funcionario that = (Funcionario) o;
        return Objects.equals(id, that.id);
    }

    @Override
    public int hashCode() { return Objects.hash(id); }

    @Override
    public String toString() {
        return "Funcionario{id=" + id + ", usuario='" + usuario + "'}";
    }
}