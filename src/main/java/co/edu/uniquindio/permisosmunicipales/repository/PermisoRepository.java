package co.edu.uniquindio.permisosmunicipales.repository;

import co.edu.uniquindio.permisosmunicipales.model.Permiso;
import co.edu.uniquindio.permisosmunicipales.model.enums.TipoPermiso;
import java.util.List;
import java.util.Optional;

public interface PermisoRepository {
    Permiso guardar(Permiso permiso);
    Optional<Permiso> buscarPorId(Long id);
    Optional<Permiso> buscarPorNumeroRadicado(String numeroRadicado);
    boolean existeRadicadoActivo(Long ciudadanoId, TipoPermiso tipoPermiso); // para RN-02
    List<Permiso> buscarPorCiudadano(Long ciudadanoId);
    long siguienteConsecutivo(int anio); // para generar RAD-AAAA-NNNNN
    List<Permiso> listarTodos();
}