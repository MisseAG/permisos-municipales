package co.edu.uniquindio.permisosmunicipales.service;

import co.edu.uniquindio.permisosmunicipales.model.Ciudadano;
import co.edu.uniquindio.permisosmunicipales.model.enums.EstadoCiudadano;
import co.edu.uniquindio.permisosmunicipales.model.enums.ResultadoEliminacion;
import co.edu.uniquindio.permisosmunicipales.model.enums.TipoIdentificacion;
import co.edu.uniquindio.permisosmunicipales.repository.CiudadanoRepository;
import co.edu.uniquindio.permisosmunicipales.repository.PermisoRepository;
import co.edu.uniquindio.permisosmunicipales.service.exception.ReglaNegocioException;

import java.util.List;
import java.util.Optional;
import java.util.regex.Pattern;

public class CiudadanoService {

    private static final Pattern PATRON_EMAIL =
            Pattern.compile("^[\\w.+-]+@[\\w-]+\\.[a-zA-Z]{2,}$");

    private final CiudadanoRepository repositorio;

    private final PermisoRepository permisoRepository;

    public CiudadanoService(CiudadanoRepository repositorio, PermisoRepository permisoRepository) {
        this.repositorio = repositorio;
        this.permisoRepository = permisoRepository;
    }

    public Ciudadano crear(TipoIdentificacion tipo, String otroTipo, String numeroIdentificacion,
                           String nombres, String apellidos, String email,
                           String telefono, String direccion) {
        validarEmail(email);
        validarOtroTipo(tipo, otroTipo);
        if (repositorio.existePorNumeroIdentificacion(numeroIdentificacion)) {
            throw new ReglaNegocioException("RN-DUP-ID",
                    "Ya existe un ciudadano con el número de identificación " + numeroIdentificacion);
        }
        Ciudadano ciudadano = new Ciudadano(null, tipo, otroTipo, numeroIdentificacion,
                nombres, apellidos, email, telefono, direccion, EstadoCiudadano.ACTIVO);
        return repositorio.guardar(ciudadano);
    }

    public Ciudadano actualizar(Long id, String nombres, String apellidos, String email,
                                String telefono, String direccion) {
        Ciudadano ciudadano = repositorio.buscarPorId(id)
                .orElseThrow(() -> new ReglaNegocioException("RN-NOENCONTRADO",
                        "No existe un ciudadano con id " + id));
        validarEmail(email);
        ciudadano.setNombres(nombres);
        ciudadano.setApellidos(apellidos);
        ciudadano.setEmail(email);
        ciudadano.setTelefono(telefono);
        ciudadano.setDireccion(direccion);
        // numeroIdentificacion y tipoIdentificacion no se editan aquí a propósito:
        // cambiarlos después de que el ciudadano ya radicó permisos rompería la trazabilidad
        return repositorio.guardar(ciudadano);
    }

    public ResultadoEliminacion eliminar(Long id) {
        repositorio.buscarPorId(id)
                .orElseThrow(() -> new ReglaNegocioException("RN-NOENCONTRADO",
                        "No existe un ciudadano con id " + id));

        boolean tienePermisos = !permisoRepository.buscarPorCiudadano(id).isEmpty();
        if (tienePermisos) {
            repositorio.eliminar(id); // RN-08: inactivar
            return ResultadoEliminacion.INACTIVADO;
        } else {
            repositorio.eliminarFisicamente(id);
            return ResultadoEliminacion.ELIMINADO;
        }
    }

    // para que el controller decida el texto del modal (eliminar o inactivar RN-08)
    public boolean tienePermisosAsociados(Long id) {
        return !permisoRepository.buscarPorCiudadano(id).isEmpty();
    }

    public List<Ciudadano> buscar(String textoLibre) {
        return repositorio.buscar(textoLibre);
    }

    public List<Ciudadano> listar(int pagina, int tamanoPagina, EstadoCiudadano filtroEstado) {
        return repositorio.listar(pagina, tamanoPagina, filtroEstado);
    }

    private void validarEmail(String email) {
        if (!PATRON_EMAIL.matcher(email).matches()) {
            throw new ReglaNegocioException("RN-EMAIL", "El correo '" + email + "' no es válido");
        }
    }

    private void validarOtroTipo(TipoIdentificacion tipo, String otroTipo) {
        if (tipo == TipoIdentificacion.OTRO && (otroTipo == null || otroTipo.isBlank())) {
            throw new ReglaNegocioException("RN-OTRO-ID",
                    "Debe especificar el tipo de identificación cuando selecciona 'Otro'");
        }
    }

    public Optional<Ciudadano> buscarPorNumeroIdentificacion(String numeroIdentificacion) {
        return repositorio.buscarPorNumeroIdentificacion(numeroIdentificacion);
    }
}