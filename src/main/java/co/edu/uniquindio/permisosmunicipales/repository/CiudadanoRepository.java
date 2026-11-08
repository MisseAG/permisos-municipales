package co.edu.uniquindio.permisosmunicipales.repository;

import co.edu.uniquindio.permisosmunicipales.model.Ciudadano;
import co.edu.uniquindio.permisosmunicipales.model.enums.EstadoCiudadano;
import java.util.List;
import java.util.Optional;

public interface CiudadanoRepository {
    Ciudadano guardar(Ciudadano ciudadano);
    Optional<Ciudadano> buscarPorId(Long id);
    Optional<Ciudadano> buscarPorNumeroIdentificacion(String numeroIdentificacion);
    boolean existePorNumeroIdentificacion(String numeroIdentificacion);
    List<Ciudadano> buscar(String textoLibre); // por nombre, apellido o identificación
    List<Ciudadano> listar(int pagina, int tamanoPagina, EstadoCiudadano filtroEstado);
    void eliminar(Long id); // internamente solo inactiva (RN-08), no borra
    // se agrega a la interfaz existente
    void eliminarFisicamente(Long id);
}